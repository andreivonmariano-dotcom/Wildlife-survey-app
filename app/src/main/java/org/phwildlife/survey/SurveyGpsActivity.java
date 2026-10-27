package org.phwildlife.survey;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.Locale;

/**
 * Survey-start and station GPS check. The service owns the continuous receiver.
 * Only survey start shows the GNSS sky chart; station visits stay compact.
 */
public final class SurveyGpsActivity extends Activity {
    private static final int REQUEST_FINE=413, REQUEST_NOTIFY=414;
    private LocationManager manager;
    private boolean stationMode;
    private boolean receiverStartedInThisScreen=false;
    private boolean acquisitionVisible=false;
    private boolean launchCommitted=false;
    private boolean skyRegistered=false;
    private Location qualifiedFix;
    private TextView status,satelliteStatus,signalStatus;
    private SatelliteSkyView skyView;
    private Button startButton;
    private final Handler ui=new Handler(Looper.getMainLooper());
    private final Runnable refresh=new Runnable(){
        @Override public void run(){
            updateLocation();
            ui.postDelayed(this,850L);
        }
    };
    private final GnssStatus.Callback satelliteCallback=new GnssStatus.Callback(){
        @Override public void onStarted(){
            if(satelliteStatus!=null)satelliteStatus.setText("Satellites: receiver searching");
        }
        @Override public void onStopped(){
            if(satelliteStatus!=null)satelliteStatus.setText("Satellites: receiver stopped");
            if(skyView!=null)skyView.clear();
        }
        @Override public void onSatelliteStatusChanged(GnssStatus g){
            if(skyView!=null)skyView.update(g);
            int used=0,visible=g.getSatelliteCount(),signals=0;
            float sum=0f;
            for(int i=0;i<visible;i++){
                if(g.usedInFix(i))used++;
                float cn0=g.getCn0DbHz(i);
                if(Float.isFinite(cn0)&&cn0>0){sum+=cn0;signals++;}
            }
            if(satelliteStatus!=null)satelliteStatus.setText(
                "Satellites in fix: "+used+" · tracked: "+visible);
            if(signalStatus!=null)signalStatus.setText(signals==0?
                "Signal C/N0 unavailable":
                String.format(Locale.US,"Mean tracked signal: %.1f dB-Hz",sum/signals));
        }
    };
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        manager=(LocationManager)getSystemService(LOCATION_SERVICE);
        stationMode=getIntent().getBooleanExtra("station_gps_mode",false);
        int gap=Math.round(16f*getResources().getDisplayMetrics().density);
        LinearLayout content=new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(gap,gap,gap,gap);
        content.setBackgroundColor(UiKit.BG);
        content.addView(UiKit.title(this,stationMode?"Station GPS":"Survey GPS"));
        TextView explanation=new TextView(this);
        explanation.setText("Starts automatically after a fresh GPS fix with reported accuracy under 10 m.");
        explanation.setTextSize(15);
        explanation.setTextColor(UiKit.INK);
        explanation.setPadding(0,gap,0,gap);
        content.addView(explanation);
        status=new TextView(this);
        status.setTextSize(17);
        status.setTextColor(UiKit.INK);
        status.setText("Waiting for GPS updates from the receiver...");
        status.setPadding(0,gap,0,gap);
        content.addView(status);
        if(!stationMode){
            satelliteStatus=new TextView(this);
            satelliteStatus.setText("Satellites: waiting for receiver");
            satelliteStatus.setTextSize(14);
            satelliteStatus.setTextColor(UiKit.INK);
            content.addView(satelliteStatus);
            signalStatus=new TextView(this);
            signalStatus.setText("Signal quality: waiting");
            signalStatus.setTextSize(14);
            signalStatus.setTextColor(UiKit.INK);
            content.addView(signalStatus);
            skyView=new SatelliteSkyView(this);
            content.addView(skyView,new LinearLayout.LayoutParams(
                -1,Math.round(290f*getResources().getDisplayMetrics().density)));
            TextView legend=new TextView(this);
            legend.setText("Green outline = satellite used for position fix");
            legend.setTextColor(UiKit.INK);
            legend.setTextSize(12);
            content.addView(legend);
        }
        startButton=UiKit.button(this,stationMode?"Start station with current GPS":
            "Start survey with verified GPS","primary");
        startButton.setEnabled(false);
        startButton.setOnClickListener(v->launchWithFix());
        // Survey and station setup are already explicit actions. No second Start tap.
        startButton.setVisibility(View.GONE);
        content.addView(startButton);
        if(stationMode){
            Button missing=UiKit.button(this,"Start without GPS · flag missing","warning");
            missing.setOnClickListener(v->new AlertDialog.Builder(this)
                .setTitle("Start without a verified station position?")
                .setMessage("This station's GPS coordinates will be left blank and flagged in the export. "+
                    "The continuous phone GPS service will remain active.")
                .setNegativeButton("Keep checking GPS",null)
                .setPositiveButton("Start flagged station",(dialog,which)->finishStation(null)).show());
            content.addView(missing);
        }
        Button retry=UiKit.button(this,"Check current GPS again","secondary");
        retry.setOnClickListener(v->{
            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                try{TrackService.startGnss(this);receiverStartedInThisScreen=true;}
                catch(Exception e){status.setText("GPS could not restart");}
            }
            ensureReceiver();updateLocation();
        });
        content.addView(retry);
        Button settings=UiKit.button(this,"Location settings","secondary");
        settings.setOnClickListener(v->startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)));
        content.addView(settings);
        Button cancel=UiKit.button(this,stationMode?"Return to survey without starting station":
            "Cancel GPS and return to setup","secondary");
        cancel.setOnClickListener(v->cancel());
        content.addView(cancel);
        ScrollView page=new ScrollView(this);
        page.setFillViewport(true);
        page.addView(content);
        setContentView(page);
    }
    @Override protected void onResume(){
        super.onResume();
        acquisitionVisible=true;
        ensureReceiver();
        if(!stationMode)registerSkyStatus();
        ui.removeCallbacks(refresh);
        ui.post(refresh);
    }
    private void ensureReceiver(){
        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            status.setText("Grant PRECISE location, not approximate-only location.");
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION},REQUEST_FINE);
            return;
        }
        if(manager==null||!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            status.setText("Phone GPS is disabled. Open Location settings and return.");
            return;
        }
        if(!TrackService.isRunning()){
            if(TrackService.wasStopped(this)&&(stationMode||receiverStartedInThisScreen)){
                status.setText("GPS was stopped. Tap Check current GPS again to restart.");
                return;
            }
            try{TrackService.startGnss(this);receiverStartedInThisScreen=true;}
            catch(Exception e){status.setText("Continuous GPS could not start: "+e.getClass().getSimpleName());}
        }
        if(Build.VERSION.SDK_INT>=33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED &&
            !getPreferences(MODE_PRIVATE).getBoolean("asked_notifications",false)){
            getPreferences(MODE_PRIVATE).edit().putBoolean("asked_notifications",true).apply();
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},REQUEST_NOTIFY);
        }
    }
    private void registerSkyStatus(){
        if(stationMode||skyRegistered||manager==null||
            checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED)return;
        try{
            if(Build.VERSION.SDK_INT>=30)skyRegistered=manager.registerGnssStatusCallback(
                getMainExecutor(),satelliteCallback);
            else skyRegistered=manager.registerGnssStatusCallback(satelliteCallback,ui);
        }catch(Exception ignored){}
    }
    private static boolean validFix(Location fix){
        if(fix==null||!LocationManager.GPS_PROVIDER.equals(fix.getProvider())||!fix.hasAccuracy())
            return false;
        long age=(SystemClock.elapsedRealtimeNanos()-fix.getElapsedRealtimeNanos())/1000000L;
        return GpsFixPolicy.acceptableElapsed(fix.getLatitude(),fix.getLongitude(),
            fix.getAccuracy(),age,fix.isFromMockProvider());
    }
    private void updateLocation(){
        if(status==null)return;
        Location current=TrackService.currentFix();
        boolean good=validFix(current);
        qualifiedFix=good?new Location(current):null;
        startButton.setEnabled(good);
        if(good&&acquisitionVisible&&!launchCommitted&&!isFinishing()){
            status.setText("GPS verified · starting "+(stationMode?"station":"survey")+"…");
            launchWithFix();
            return;
        }
        if(current==null){
            status.setText("GNSS running. Waiting for first location fix. "+
                "Move to open sky if satellite signals are weak.");
            return;
        }
        long age=(SystemClock.elapsedRealtimeNanos()-current.getElapsedRealtimeNanos())/1000000L;
        float shownAge=Math.max(0L,age)/1000f;
        String coords=String.format(Locale.US,"%.6f, %.6f",
            current.getLatitude(),current.getLongitude());
        String accuracy=current.hasAccuracy()?
            String.format(Locale.US,"±%.1f m",current.getAccuracy()):"unknown";
        status.setText((good?"GPS ready · starting": "Waiting for a fresher, more accurate fix")+
            "\nCoordinates: "+coords+"\nReported accuracy: "+accuracy+
            "\nFix age: "+String.format(Locale.US,"%.1f s",shownAge)+
            (current.isFromMockProvider()?"\nMock location rejected":"")+
            "\nContinuous GPS: "+(TrackService.isRunning()?"active":"stopped"));
    }
    private void launchWithFix(){
        if(launchCommitted||!acquisitionVisible||isFinishing())return;
        Location fix=TrackService.currentFix();
        if(!validFix(fix)){
            qualifiedFix=null;
            startButton.setEnabled(false);
            status.setText("GPS fix expired or no longer meets 10 m. Waiting for another update.");
            return;
        }
        launchCommitted=true;
        if(stationMode){finishStation(fix);return;}
        Intent launch=new Intent(this,MainActivity.class);
        if(getIntent().getExtras()!=null)launch.putExtras(getIntent().getExtras());
        launch.putExtra("start_gps_latitude",fix.getLatitude());
        launch.putExtra("start_gps_longitude",fix.getLongitude());
        launch.putExtra("start_gps_accuracy_m",fix.getAccuracy());
        launch.putExtra("start_gps_recorded_at_ms",fix.getTime());
        launch.putExtra("start_gps_elapsed_nanos",fix.getElapsedRealtimeNanos());
        try{
            startActivity(launch);
            finish();
        }catch(RuntimeException e){
            launchCommitted=false;
            status.setText("Could not open survey: "+e.getClass().getSimpleName());
        }
    }
    private void finishStation(Location fix){
        if(isFinishing())return;
        launchCommitted=true;
        Intent result=new Intent();
        result.putExtra("station_kind",getIntent().getStringExtra("station_kind"));
        result.putExtra("station_id",getIntent().getStringExtra("station_id"));
        result.putExtra("station_generate",getIntent().getBooleanExtra("station_generate",false));
        result.putExtra("station_event_id",getIntent().getStringExtra("station_event_id"));
        result.putExtra("station_gps_missing",fix==null);
        if(fix!=null)result.putExtra("station_gps_location",new Location(fix));
        setResult(RESULT_OK,result);
        finish();
    }
    private void cancel(){
        if(!stationMode)TrackService.stopGnss(this);
        finish();
    }
    @Override public void onBackPressed(){cancel();}
    @Override protected void onPause(){
        acquisitionVisible=false;
        ui.removeCallbacks(refresh);
        if(skyRegistered&&manager!=null){
            try{manager.unregisterGnssStatusCallback(satelliteCallback);}catch(Exception ignored){}
            skyRegistered=false;
        }
        // Never stop the continuous GPS service simply when Android moves the app home.
        super.onPause();
    }
    @Override public void onRequestPermissionsResult(int request,String[] names,int[] grants){
        super.onRequestPermissionsResult(request,names,grants);
        if(request==REQUEST_FINE){
            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                ensureReceiver();registerSkyStatus();
            }else status.setText("Precise location permission denied. Turn on Precise in app settings.");
        }
    }
}
