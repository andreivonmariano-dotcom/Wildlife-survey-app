package org.phwildlife.survey;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.GnssStatus;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ScrollView;
import java.util.Locale;

/**
 * Survey-start GNSS gate. No survey row or survey effort begins here.
 * An accurate fix is a device-reported estimate, not a measured positional guarantee.
 */
public final class SurveyGpsActivity extends Activity implements LocationListener {
    private static final int REQUEST_FINE_LOCATION = 413;
    private LocationManager manager;
    private Location qualifiedFix;
    private TextView status;
    private Button startButton;
    private boolean listening = false;
    private boolean gnssListening = false;
    private TextView satelliteStatus;
    private TextView signalStatus;
    private SatelliteSkyView skyView;
    private boolean stationMode;
    private final Handler ui = new Handler(Looper.getMainLooper());
    private final GnssStatus.Callback satelliteCallback = new GnssStatus.Callback() {
        @Override public void onStarted() {
            if(satelliteStatus!=null)satelliteStatus.setText("GNSS receiver active · searching satellites");
        }
        @Override public void onStopped() {
            if(satelliteStatus!=null)satelliteStatus.setText("GNSS receiver not tracking");
            if(skyView!=null)skyView.clear();
        }
        @Override public void onSatelliteStatusChanged(GnssStatus state) {
            if(skyView!=null)skyView.update(state);
            int used=0,visible=state.getSatelliteCount();
            float sumCn0=0f;
            int cn0Count=0;
            for(int i=0;i<visible;i++){
                if(state.usedInFix(i))used++;
                float cn0=state.getCn0DbHz(i);
                if(Float.isFinite(cn0)&&cn0>0){sumCn0+=cn0;cn0Count++;}
            }
            if(satelliteStatus!=null)satelliteStatus.setText(
                "Satellites in fix: "+used+" · tracked: "+visible+
                "\nSatellite count does not prove coordinate accuracy.");
            if(signalStatus!=null)signalStatus.setText(
                cn0Count==0?"GNSS signal quality: no C/N0 readings":
                String.format(Locale.US,"Mean tracked C/N0: %.1f dB-Hz",sumCn0/cn0Count));
        }
    };

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        stationMode=getIntent().getBooleanExtra("station_gps_mode",false);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int gap = Math.round(18f * getResources().getDisplayMetrics().density);
        root.setPadding(gap, gap, gap, gap);
        root.setBackgroundColor(UiKit.BG);
        root.addView(UiKit.title(this,stationMode?"Acquire station GPS":"Acquire survey-start GPS"));

        TextView details = new TextView(this);
        details.setText((stationMode?"Station "+getIntent().getStringExtra("station_label")+
            ". The station visit timer starts only after GPS confirmation. ":
            "The survey clock starts only after a fresh GNSS position. ")+
            "Target: device-reported horizontal accuracy below 10 m, fix age at most 10 seconds. "+
            "Forest canopy can prevent this. Satellite information is diagnostic, not proof of field accuracy.");
        details.setTextSize(15);
        details.setTextColor(UiKit.INK);
        details.setPadding(0, gap, 0, gap);
        root.addView(details);

        status = new TextView(this);
        status.setTextSize(17);
        status.setTextColor(UiKit.INK);
        status.setText("Waiting for a new GPS position...");
        status.setPadding(0, gap, 0, gap);
        root.addView(status);
        satelliteStatus = new TextView(this);
        satelliteStatus.setText("Satellite information: waiting for receiver");
        satelliteStatus.setTextSize(15);
        satelliteStatus.setTextColor(UiKit.INK);
        satelliteStatus.setPadding(0, gap/2, 0, gap/2);
        root.addView(satelliteStatus);
        signalStatus = new TextView(this);
        signalStatus.setText("Signal strength: waiting");
        signalStatus.setTextSize(14);
        signalStatus.setTextColor(UiKit.INK);
        root.addView(signalStatus);
        skyView=new SatelliteSkyView(this);
        root.addView(skyView,new LinearLayout.LayoutParams(-1,Math.round(290f*getResources().getDisplayMetrics().density)));
        TextView legend=new TextView(this);
        legend.setText("North at top · Center: overhead (90°) · Outer ring: horizon (0°)\n"+
            "Colors: GPS green, Galileo blue, GLONASS purple, BeiDou amber, others gray. "+
            "Green outer ring = used in fix. Marker size reflects signal C/N0 (dB-Hz).");
        legend.setTextSize(12);
        legend.setTextColor(UiKit.INK);
        root.addView(legend);

        startButton = UiKit.button(this,stationMode?"Start station with GPS":"Start survey with verified GPS", "primary");
        startButton.setEnabled(false);
        startButton.setOnClickListener(v -> launchSurvey());
        root.addView(startButton, new LinearLayout.LayoutParams(-1, -2));

        if(stationMode){
            Button missing=UiKit.button(this,"Start station without GPS · flag missing","warning");
            missing.setOnClickListener(v->new android.app.AlertDialog.Builder(this)
                .setTitle("Start without reliable GPS?")
                .setMessage("This station will have no verified coordinates. The survey export will flag missing GPS. "+
                    "Use this only if satellite reception is inadequate.")
                .setNegativeButton("Keep acquiring",null)
                .setPositiveButton("Start flagged station",(dialog,which)->finishStation(null)).show());
            root.addView(missing);
        }

        Button retry = UiKit.button(this, "Retry GPS acquisition", "secondary");
        retry.setOnClickListener(v -> beginAcquisition());
        root.addView(retry);

        Button settings = UiKit.button(this, "Location settings", "secondary");
        settings.setOnClickListener(v -> startActivity(
            new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)));
        root.addView(settings);

        Button cancel = UiKit.button(this, "Cancel and return home", "secondary");
        cancel.setOnClickListener(v -> finish());
        root.addView(cancel);

        ScrollView page=new ScrollView(this);
        page.setFillViewport(true);
        page.addView(root);
        setContentView(page);
    }

    @Override protected void onResume() {
        super.onResume();
        beginAcquisition();
    }

    private void beginAcquisition() {
        stopListening();
        qualifiedFix = null;
        startButton.setEnabled(false);
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            status.setText("Allow PRECISE location, not approximate-only location, to start a survey.");
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_FINE_LOCATION);
            return;
        }
        if (manager == null || !manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            status.setText("GPS is disabled. Turn on phone location services and retry.");
            return;
        }
        status.setText("Searching for satellites. Waiting for a live fix better than 10 m...");
        try {
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 0f, this);
            listening = true;
            if(Build.VERSION.SDK_INT>=30)gnssListening=manager.registerGnssStatusCallback(
                getMainExecutor(),satelliteCallback);
            else gnssListening=manager.registerGnssStatusCallback(satelliteCallback,ui);
        } catch (SecurityException ex) {
            status.setText("Precise location permission is required.");
        } catch (RuntimeException ex) {
            status.setText("GPS could not start: " + ex.getMessage());
        }
    }

    @Override public void onLocationChanged(Location fix) {
        long now = System.currentTimeMillis();
        long ageMs=(SystemClock.elapsedRealtimeNanos()-fix.getElapsedRealtimeNanos())/1000000L;
        boolean valid = LocationManager.GPS_PROVIDER.equals(fix.getProvider())
            && fix.hasAccuracy() && ageMs>=0 && ageMs<=GpsFixPolicy.MAX_FIX_AGE_MS
            && GpsFixPolicy.acceptable(fix.getLatitude(), fix.getLongitude(),
                fix.getAccuracy(), fix.getTime(), now, fix.isFromMockProvider());
        qualifiedFix = valid ? new Location(fix) : null;
        startButton.setEnabled(valid);
        String quality = fix.hasAccuracy()
            ? String.format(Locale.US, "±%.1f m", fix.getAccuracy())
            : "unknown accuracy";
        if (fix.isFromMockProvider()) {
            status.setText("Mock location rejected. Acquire a real GPS fix.");
        } else if (valid) {
            status.setText(String.format(Locale.US,
                "GPS ready: %.6f, %.6f\nReported accuracy: %s\nFix age: %.1f s",
                fix.getLatitude(), fix.getLongitude(), quality,
                (now - fix.getTime()) / 1000f));
        } else {
            status.setText("Acquiring better GPS... Current: " + quality
                + " (required: <10 m), fix age "
                + String.format(Locale.US, "%.1f s", (now - fix.getTime()) / 1000f)
                + ". Keep the phone stationary under clear sky.");
        }
    }

    private void finishStation(Location fix){
        Intent result=new Intent();
        result.putExtra("station_kind",getIntent().getStringExtra("station_kind"));
        result.putExtra("station_id",getIntent().getStringExtra("station_id"));
        result.putExtra("station_generate",getIntent().getBooleanExtra("station_generate",false));
        result.putExtra("station_event_id",getIntent().getStringExtra("station_event_id"));
        result.putExtra("station_gps_missing",fix==null);
        if(fix!=null)result.putExtra("station_gps_location",fix);
        setResult(RESULT_OK,result);
        finish();
    }

    private void launchSurvey() {
        Location fix = qualifiedFix;
        long now = System.currentTimeMillis();
        if (fix == null || !LocationManager.GPS_PROVIDER.equals(fix.getProvider())
            || !fix.hasAccuracy()
            || !GpsFixPolicy.acceptable(fix.getLatitude(), fix.getLongitude(),
                fix.getAccuracy(), fix.getTime(), now, fix.isFromMockProvider())) {
            qualifiedFix = null;
            startButton.setEnabled(false);
            status.setText("GPS fix expired or lost accuracy. Waiting for another live fix.");
            return;
        }
        if(stationMode){finishStation(new Location(fix));return;}
        Intent launch = new Intent(this, MainActivity.class);
        if (getIntent().getExtras() != null) launch.putExtras(getIntent().getExtras());
        launch.putExtra("start_gps_latitude", fix.getLatitude());
        launch.putExtra("start_gps_longitude", fix.getLongitude());
        launch.putExtra("start_gps_accuracy_m", fix.getAccuracy());
        launch.putExtra("start_gps_recorded_at_ms", fix.getTime());
        stopListening();
        startActivity(launch);
        finish();
    }

    private void stopListening() {
        if(skyView!=null)skyView.clear();
        if(manager==null)return;
        if(listening){
            try { manager.removeUpdates(this); } catch (RuntimeException ignored) {}
            listening=false;
        }
        if(gnssListening){
            try { manager.unregisterGnssStatusCallback(satelliteCallback); }
            catch(RuntimeException ignored) {}
            gnssListening=false;
        }
    }

    @Override protected void onPause() {
        stopListening();
        super.onPause();
    }
    @Override public void onProviderDisabled(String provider) {
        qualifiedFix = null;
        startButton.setEnabled(false);
        status.setText("GPS provider disabled. Turn on location services.");
    }
    @Override public void onProviderEnabled(String provider) {
        if (LocationManager.GPS_PROVIDER.equals(provider)) beginAcquisition();
    }
    @SuppressWarnings("deprecation")
    @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grants) {
        super.onRequestPermissionsResult(requestCode, permissions, grants);
        if (requestCode == REQUEST_FINE_LOCATION) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) beginAcquisition();
            else status.setText("Precise location access is needed. Grant it in app permissions and retry.");
        }
    }
}
