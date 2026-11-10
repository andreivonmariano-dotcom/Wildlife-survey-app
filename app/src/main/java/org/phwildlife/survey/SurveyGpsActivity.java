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
    private boolean skipConfirmationOpen=false;
    // Package-visible solely for real Android dialog-button instrumentation.
    AlertDialog gpsSkipDialog;
    private boolean skyRegistered=false;
    private Location qualifiedFix;
    private TextView status,statusDetails,qualitySummary,satelliteStatus,signalStatus;
    private LinearLayout detailPanel,helpPanel;
    private Button helpToggle;
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
    private int dp(int value){
        return Math.round(value*getResources().getDisplayMetrics().density);
    }
    private void setStatus(String message){
        if(status!=null)status.setText(message);
    }
    private void setStatusDetails(String message){
        if(statusDetails!=null)statusDetails.setText(message);
    }
    private void setQuality(String message){
        if(qualitySummary!=null)qualitySummary.setText(message);
    }
    private void needsGpsHelp(){
        if(helpToggle!=null&&helpPanel!=null&&helpPanel.getVisibility()!=View.VISIBLE)
            helpToggle.setText("Fix GPS · troubleshooting");
    }
    /** Controls with different consequences remain separate and readable on small phones. */
    private void addAction(LinearLayout parent,Button button){
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
        lp.bottomMargin=dp(LikasDesignSystem.SPACE_SM);
        parent.addView(button,lp);
    }
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        manager=(LocationManager)getSystemService(LOCATION_SERVICE);
        stationMode=getIntent().getBooleanExtra("station_gps_mode",false);
        getWindow().setStatusBarColor(UiKit.BG);
        getWindow().setNavigationBarColor(UiKit.BG);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        LinearLayout content=new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        TextView intro=UiKit.caption(this,stationMode?
            "Waiting for a fresh station GPS fix. Verified coordinates continue automatically.":
            "GPS checks automatically. A fresh fix under 10 m continues to the next step.");
        intro.setTextSize(LikasDesignSystem.TEXT_BODY);
        intro.setPadding(dp(4),dp(4),dp(4),dp(10));
        content.addView(intro);

        // First-screen essentials: state, measured fix quality, optional evidence.
        LinearLayout card=new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14),dp(12),dp(14),dp(10));
        card.setBackground(UiKit.surface(this));
        status=new TextView(this);
        status.setTextSize(LikasDesignSystem.TEXT_SECTION);
        status.setTypeface(null,android.graphics.Typeface.BOLD);
        status.setTextColor(UiKit.INK);
        status.setText("Acquiring GPS…");
        status.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        card.addView(status);
        qualitySummary=UiKit.caption(this,"Accuracy unavailable · Fix age unavailable");
        qualitySummary.setTextColor(UiKit.MUTED);
        qualitySummary.setPadding(0,dp(6),0,dp(6));
        card.addView(qualitySummary);
        TextView requirement=UiKit.caption(this,
            "Required: device GPS accuracy under 10 m, age at most 10 s");
        card.addView(requirement);
        content.addView(card);

        detailPanel=new LinearLayout(this);
        detailPanel.setOrientation(LinearLayout.VERTICAL);
        detailPanel.setPadding(dp(10),dp(6),dp(10),dp(12));
        statusDetails=UiKit.caption(this,"Device coordinates, altitude and fix provenance unavailable");
        detailPanel.addView(statusDetails);
        if(!stationMode){
            satelliteStatus=UiKit.caption(this,"Satellites: waiting for receiver");
            satelliteStatus.setPadding(0,dp(10),0,0);
            detailPanel.addView(satelliteStatus);
            signalStatus=UiKit.caption(this,"Signal quality: waiting");
            detailPanel.addView(signalStatus);
            skyView=new SatelliteSkyView(this);
            detailPanel.addView(skyView,new LinearLayout.LayoutParams(-1,dp(220)));
            detailPanel.addView(UiKit.caption(this,
                "Green outline: satellite used in the device position fix"));
        }
        detailPanel.setVisibility(View.GONE);
        Button detailToggle=UiKit.button(this,"GPS details","quiet");
        detailToggle.setContentDescription("Show coordinates, altitude, accuracy and satellite diagnostics");
        detailToggle.setOnClickListener(v->{
            boolean open=detailPanel.getVisibility()!=View.VISIBLE;
            detailPanel.setVisibility(open?View.VISIBLE:View.GONE);
            detailToggle.setText(open?"Hide GPS details":"GPS details");
            detailToggle.setContentDescription(open?
                "Hide coordinates and GPS diagnostics":
                "Show coordinates, altitude, accuracy and satellite diagnostics");
        });
        content.addView(detailToggle);
        content.addView(detailPanel);

        // Troubleshooting is one tap away, not two permanent primary-looking actions.
        helpPanel=new LinearLayout(this);
        helpPanel.setOrientation(LinearLayout.VERTICAL);
        Button retry=UiKit.button(this,"Retry GPS","secondary");
        retry.setOnClickListener(v->{
            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                try{TrackService.startGnss(this);receiverStartedInThisScreen=true;}
                catch(Exception error){setStatus("GPS restart failed");}
            }
            ensureReceiver();
            updateLocation();
        });
        addAction(helpPanel,retry);
        Button settings=UiKit.button(this,"Location settings","secondary");
        settings.setContentDescription("Open Android device location settings");
        settings.setOnClickListener(v->
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)));
        addAction(helpPanel,settings);
        helpPanel.setVisibility(View.GONE);
        helpToggle=UiKit.button(this,"GPS troubleshooting","quiet");
        helpToggle.setContentDescription("Show GPS retry and Android location settings");
        helpToggle.setOnClickListener(v->{
            boolean open=helpPanel.getVisibility()!=View.VISIBLE;
            helpPanel.setVisibility(open?View.VISIBLE:View.GONE);
            helpToggle.setText(open?"Hide GPS troubleshooting":"GPS troubleshooting");
            helpToggle.setContentDescription(open?
                "Hide GPS retry and location settings":"Show GPS retry and Android location settings");
        });
        content.addView(helpToggle);
        content.addView(helpPanel);

        // A fresh verified fix advances automatically. This hidden button is retained
        // only for the existing internal readiness gate.
        startButton=UiKit.button(this,stationMode?"Start station with current GPS":
            "Start survey with verified GPS","primary");
        startButton.setEnabled(false);
        startButton.setOnClickListener(v->launchWithFix());
        startButton.setVisibility(View.GONE);

        LinearLayout page=UiKit.page(this,stationMode?"Station GPS":"Survey GPS",
            stationMode?"Survey":"Survey setup",this::cancel,content);
        LinearLayout footer=new LinearLayout(this);
        footer.setOrientation(LinearLayout.VERTICAL);
        footer.setPadding(0,dp(4),0,0);
        if(!stationMode){
            Button skip=UiKit.button(this,"Continue without GPS","warning");
            skip.setContentDescription("Continue without a verified survey-start GPS fix; mark start coordinates missing");
            skip.setOnClickListener(v->{
                if(launchCommitted||skipConfirmationOpen||!acquisitionVisible||isFinishing())return;
                skipConfirmationOpen=true;
                gpsSkipDialog=new AlertDialog.Builder(this)
                    .setTitle("Continue with missing GPS?")
                    .setMessage((getIntent().getBooleanExtra("prepare_field_checklist",false)?
                        "Habitat preparation comes first. Survey effort does not begin until Begin survey. ":
                        "The survey starts after this confirmation. ")+
                        "Starting coordinates will remain missing in the export. "+
                        "Later GPS readings cannot replace them.")
                    .setNegativeButton("Keep checking GPS",(dialog,which)->{
                        skipConfirmationOpen=false;updateLocation();
                    })
                    .setOnCancelListener(dialog->{skipConfirmationOpen=false;updateLocation();})
                    .setPositiveButton("Continue, flag missing GPS",(dialog,which)->{
                        skipConfirmationOpen=false;launchWithoutFix();
                    })
                    .setOnDismissListener(dialog->gpsSkipDialog=null)
                    .create();
                gpsSkipDialog.show();
            });
            addAction(footer,skip);
        }else{
            Button missing=UiKit.button(this,"Start station without GPS","warning");
            missing.setContentDescription("Start station visit with unverified station GPS coordinates");
            missing.setOnClickListener(v->{
                if(launchCommitted||isFinishing())return;
                new AlertDialog.Builder(this)
                    .setTitle("Start station without a GPS fix?")
                    .setMessage("Station coordinates will remain blank and flagged. "+
                        "The continuous phone GPS service remains active.")
                    .setNegativeButton("Keep checking GPS",null)
                    .setPositiveButton("Start flagged station",(dialog,which)->finishStation(null))
                    .show();
            });
            addAction(footer,missing);
        }
        // The page's navigation header already provides the Back action.
        page.addView(footer);
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
        // The periodic refresh must not mask permission denial or a disabled provider.
        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            qualifiedFix=null;
            startButton.setEnabled(false);
            setStatus("Precise location permission required");
            setQuality("No verified position");
            setStatusDetails("Enable precise location for APO in Android app permissions. "+
                "No starting coordinates have been recorded.");
            needsGpsHelp();
            return;
        }
        if(manager==null||!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            qualifiedFix=null;
            startButton.setEnabled(false);
            setStatus("Device GPS is switched off");
            setQuality("No verified position");
            setStatusDetails("Enable device location to obtain a GPS fix. "+
                "No starting coordinates have been recorded.");
            needsGpsHelp();
            return;
        }
        Location current=TrackService.currentFix();
        boolean good=validFix(current);
        qualifiedFix=good?new Location(current):null;
        startButton.setEnabled(good);
        if(good&&acquisitionVisible&&!launchCommitted&&!skipConfirmationOpen&&!isFinishing()){
            setStatus("GPS verified · continuing to "+(stationMode?"station":
                getIntent().getBooleanExtra("prepare_field_checklist",false)?
                "habitats and species":"survey")+"…");
            launchWithFix();
            return;
        }
        if(current==null){
            boolean running=TrackService.isRunning();
            setStatus(running?"Searching for GPS":"GPS receiver stopped");
            setQuality("Accuracy unavailable · Fix age unavailable");
            setStatusDetails(running?
                "No device GPS coordinates yet. Open sky may improve reception.":
                "GPS receiver stopped. Use GPS troubleshooting to retry.");
            if(!running)needsGpsHelp();
            return;
        }
        long age=(SystemClock.elapsedRealtimeNanos()-current.getElapsedRealtimeNanos())/1000000L;
        float shownAge=Math.max(0L,age)/1000f;
        String coords=String.format(Locale.US,"%.6f, %.6f",
            current.getLatitude(),current.getLongitude());
        String accuracy=current.hasAccuracy()?
            String.format(Locale.US,"±%.1f m",current.getAccuracy()):"unavailable";
        String altitude=(current.hasAltitude()&&Double.isFinite(current.getAltitude()))?
            String.format(Locale.US,"%.1f m%s",current.getAltitude(),
                current.hasVerticalAccuracy()?
                    String.format(Locale.US," (reported vertical accuracy ±%.1f m)",
                        current.getVerticalAccuracyMeters()):""):
            "unavailable";
        setStatus(good?"GPS verified · continuing":
            (current.isFromMockProvider()?"Mock device position rejected":
                "Waiting for a better GPS fix"));
        setQuality("Accuracy "+accuracy+" · Age "+
            String.format(Locale.US,"%.1f s",shownAge));
        setStatusDetails("Device GPS coordinates: "+coords+
            "\nHorizontal accuracy (reported): "+accuracy+
            "\nFix age: "+String.format(Locale.US,"%.1f s",shownAge)+
            "\nEllipsoidal altitude (device; not terrain elevation): "+altitude+
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
        Intent launch=new Intent(this,getIntent().getBooleanExtra(
            "prepare_field_checklist",false)?
            HabitatsSpeciesActivity.class:MainActivity.class);
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
    /** Explicit observer override. No GPS values or position estimates are invented. */
    private void launchWithoutFix(){
        if(stationMode||launchCommitted||!acquisitionVisible||isFinishing())return;
        launchCommitted=true;
        Intent launch=new Intent(this,getIntent().getBooleanExtra(
            "prepare_field_checklist",false)?
            HabitatsSpeciesActivity.class:MainActivity.class);
        if(getIntent().getExtras()!=null)launch.putExtras(getIntent().getExtras());
        launch.removeExtra("start_gps_latitude");
        launch.removeExtra("start_gps_longitude");
        launch.removeExtra("start_gps_accuracy_m");
        launch.removeExtra("start_gps_recorded_at_ms");
        launch.removeExtra("start_gps_elapsed_nanos");
        launch.putExtra("start_without_gps",true);
        try{
            startActivity(launch);
            finish();
        }catch(RuntimeException error){
            launchCommitted=false;
            status.setText("Could not open survey: "+error.getClass().getSimpleName());
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
