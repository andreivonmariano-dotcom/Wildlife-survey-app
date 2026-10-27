package org.phwildlife.survey;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.os.VibrationEffect;
import android.os.SystemClock;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.*;
import java.io.OutputStream;
import org.json.JSONObject;
import android.text.TextWatcher;
import android.text.Editable;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@SuppressWarnings("deprecation")
public class MainActivity extends Activity implements LocationListener {
    private static final int EXPORT_DOC=22, CAMERA_REQ=23, PHOTO_PICK=24, STATION_GPS_REQ=25, IDENTIFY_SPECIES_REQ=26;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final Runnable delayedDraftSave=()->persistDraft();
    private final Runnable visibleTimer=new Runnable(){
        @Override public void run(){
            Location live=TrackService.currentFix();
            if(live!=null){
                location=live;
                updateGpsLabel(live);
            }else updateGpsLabel(null);
            status();
            handler.postDelayed(this,1000);
        }
    };
    private final int BACK=UiKit.BG,INK=UiKit.INK,GREEN=UiKit.FOREST,MUTED=UiKit.MUTED;
    private FieldDb db; private SurveyHudReader hudReader;
    private LocationManager locations;
    private TextView bearingStatusView,gpsView,eventView,countView,hintView,detailToggle,sessionClockView,progressView,pauseStatusView;
    private LinearLayout markRowView;
    private Button pauseButton,progressButton;
    private CheckBox vibrationChoice;
    private Spinner confidenceSpinner;
    private EditText identificationNote;
    private TextView photoStatus;
    private ImageView photoPreview;
    private String pendingPhoto=null;
    private Button favoriteButton;
    private TextView callScoreLabel,noiseLabel;
    private boolean surveyPaused=false;
    private long pauseStartedAt=0,accumulatedPausedMs=0;
    private long unitPausedMs=0;
    private Spinner detectionSpinner,distanceSourceSpinner,firstPositionSpinner,movementSpinner,substrateSpinner,callScoreSpinner,lifeStageSpinner,evidenceSpinner,noiseSpinner;
    private TaxonSuggestionAdapter taxonSuggestionAdapter;
    private AutoCompleteTextView taxonEntry;
    private EditText distanceEntry,lineEntry,notesEntry,habitatEntry,behaviorEntry,tempEntry,humidityEntry,weatherEntry,microhabitatEntry,perchEntry,herpHeightEntry,deviceEntry;
    private Button endSurveyButton,markButton,saveButton,reviewButton,pointNextButton;
    private LinearLayout groupBar,advancedPanel,lineRow,birdMeasurements,herpPanel,mammalPanel;
    private ScrollView fieldScroll;
    private boolean returnToHome=false;
    private final String[] groups=Taxa.GROUPS;
    private String samplingUnitId=null;
    private long unitStartedAt=0;
    private int unitSequence=0;
    private String group="Birds",eventId=null,pointCountId=null,method="point_count",profile="Birds",stationId="",observerName="",projectId="default",regionLabel="",geographicRegion="PH";
    private String adminProvince="",adminPsgc="",adminBoundaryStatus="",adminBoundaryVersion="",geographicSelectionMethod=GeoRegions.BOUNDARY_METHOD;
    private long pointStartedAt=0;
    private int pointSequence=0,plannedDurationSeconds=0;
    private boolean nearEndNotified=false;
    private Double pendingSearchedArea=null;
    private boolean plannedRouteCompleted=false;
    private String pendingEndReason=null;
    private String startGpsStatus="";
    private TextView startGpsBanner;
    private TextView trackingView;
    private Double stratumAreaKm2=null,lineBearing=null,pointRadiusM=null;
    private int pointTargetSeconds=600;
    private double plannedRouteMeters=0,plannedSpacingMeters=0;
    private int plannedStationCount=0;
    private String routeTemplateId="",routeTemplateName="";
    private boolean saveRouteTemplate=false;
    private boolean basicLogOnly=false;
    private boolean active=false,detailShown=false;
    private long startedAt=0,markedAt=0;
    private int count=1,sensorQuality=-1;
    private float bearing=Float.NaN,pitch=Float.NaN;
    private Double markedBearing=null,markedPitch=null;
    private Location location;
    // Observer position belongs to the detection time, not the later Save tap.
    private Location markedObserverFix;
    private volatile boolean exportInProgress=false;
    private boolean pointNotice=false;
    private String cachedPausedUnit="";
    private long pausedUnitCacheMs=-1,pausedUnitFetchedMs=0;
    private long lastProgressFetchMs=0,lastTrackFetchMs=0;
    private int dp(float n){return (int)(getResources().getDisplayMetrics().density*n+0.5f);}

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        db=new FieldDb(this);
        hudReader=new SurveyHudReader(this);
        locations=(LocationManager)getSystemService(LOCATION_SERVICE);
        restoreEvent();
        Intent launch=getIntent();
        boolean newEvent=!active&&launch.getBooleanExtra("create_new",false);
        if(newEvent){
            profile=launch.getStringExtra("survey_profile");
            if(profile==null)profile="Birds";
            group=profile.equals("Herpetofauna")?"Reptiles":profile;
            method=launch.getStringExtra("survey_method");
            if(method==null)method="opportunistic";
            if(method.equals("amphibian_calls"))group="Amphibians";
            stationId=launch.getStringExtra("station_id");
            if(stationId==null)stationId="";
            observerName=launch.getStringExtra("observer_name");
            if(observerName==null)observerName="";
            pointTargetSeconds=launch.getIntExtra("point_target_s",600);
            basicLogOnly=launch.getBooleanExtra("basic_log_only",false);
            projectId=launch.getStringExtra("project_id");
            if(projectId==null)projectId="default";
            regionLabel=launch.getStringExtra("region_label");
            if(regionLabel==null)regionLabel="";
            geographicRegion=launch.getStringExtra("geographic_region_id");
            if(geographicRegion==null)geographicRegion="PH";
            adminProvince=nonNull(launch.getStringExtra("admin_province"));
            adminPsgc=nonNull(launch.getStringExtra("admin_province_psgc"));
            adminBoundaryStatus=nonNull(launch.getStringExtra("admin_boundary_status"));
            adminBoundaryVersion=nonNull(launch.getStringExtra("admin_boundary_version"));
            geographicSelectionMethod=nonNull(launch.getStringExtra("geographic_selection_method"));
            if(geographicSelectionMethod.isEmpty())geographicSelectionMethod=GeoRegions.BOUNDARY_METHOD;
            double area=launch.getDoubleExtra("stratum_area_km2",Double.NaN);
            stratumAreaKm2=Double.isNaN(area)?null:area;
            double radius=launch.getDoubleExtra("point_radius_m",Double.NaN);
            pointRadiusM=Double.isNaN(radius)?null:radius;
            plannedDurationSeconds=launch.getIntExtra("planned_duration_s",0);
            plannedRouteMeters=launch.getDoubleExtra("planned_route_m",0);
            plannedSpacingMeters=launch.getDoubleExtra("planned_spacing_m",0);
            routeTemplateId=nonNull(launch.getStringExtra("route_template_id"));
            routeTemplateName=nonNull(launch.getStringExtra("route_template_name"));
            saveRouteTemplate=launch.getBooleanExtra("save_route_template",false);
            if(method.equals("point_count")&&plannedSpacingMeters>0)
                plannedStationCount=RoutePlan.count(plannedRouteMeters,plannedSpacingMeters);
        }
        if(!active&&!newEvent){finish();return;}
        draw();
        if(newEvent){
            String initialAz=launch.getStringExtra("transect_bearing");
            if(initialAz!=null&&!initialAz.isEmpty())lineEntry.setText(initialAz);
            toggleEvent();
        }else if(lineBearing!=null)lineEntry.setText(String.valueOf(lineBearing));
        if(active)restoreDraft();
        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},71);
        }
        // Screen timer starts in onResume and stops in onPause. Elapsed time comes from stored timestamps.
    }
    private TextView label(String text,int size,boolean bold) {
        TextView t=new TextView(this);t.setText(text);t.setTextSize(size);t.setTextColor(INK);
        if(bold)t.setTypeface(null,Typeface.BOLD);t.setPadding(dp(2),dp(3),dp(2),dp(3));return t;
    }
    private Button button(String txt){
        Button b=new Button(this);b.setText(txt);b.setAllCaps(false);b.setTextColor(Color.WHITE);
        b.setBackgroundTintList(ColorStateList.valueOf(GREEN));b.setTextSize(14);b.setMinHeight(dp(44));return b;
    }
    private EditText edit(String hint,int type){
        EditText e=new EditText(this);e.setHint(hint);e.setTextColor(INK);
        e.setTextSize(16);e.setInputType(type);e.setSingleLine(true);return e;
    }
    private LinearLayout vertical(){LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);return c;}
    private LinearLayout horizontal(){LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.HORIZONTAL);c.setGravity(Gravity.CENTER_VERTICAL);return c;}
    private LinearLayout.LayoutParams weighted(float wt){return new LinearLayout.LayoutParams(0,-2,wt);}
    private TextView section(String title){
        TextView t=label(title,14,true);
        t.setTextColor(UiKit.FOREST);
        t.setPadding(dp(2),dp(14),dp(2),dp(8));
        return t;
    }
    private void disclosureTarget(TextView view){
        view.setMinHeight(dp(46));
        view.setGravity(Gravity.CENTER_VERTICAL);
        view.setFocusable(true);
    }
    private void draw(){
        LinearLayout root=vertical();root.setBackgroundColor(BACK);root.setPadding(dp(12),dp(5),dp(12),dp(7));
        LinearLayout top=horizontal();
        Button home=UiKit.button(this,"‹ Home","secondary");
        home.setTextSize(13);top.addView(home,new LinearLayout.LayoutParams(dp(78),dp(49)));
        home.setOnClickListener(v->navigateHome());
        TextView title=label(profile+" Survey",19,true);title.setGravity(Gravity.CENTER);
        top.addView(title,new LinearLayout.LayoutParams(0,-2,1));
        Button export=UiKit.button(this,"Export","secondary");export.setTextSize(13);
        top.addView(export,new LinearLayout.LayoutParams(dp(84),dp(49)));export.setOnClickListener(v->exportZip());
        root.addView(top);
        // Fixed status HUD: short, high-contrast survey state, always visible.
        LinearLayout hud=vertical();
        hud.setBackground(UiKit.surface(this));
        hud.setPadding(dp(12),dp(8),dp(12),dp(8));
        LinearLayout.LayoutParams hudLp=new LinearLayout.LayoutParams(-1,-2);
        hudLp.topMargin=dp(5);hudLp.bottomMargin=dp(8);
        sessionClockView=label("Session 00:00:00 · Active 00:00:00",16,true);
        sessionClockView.setTextColor(UiKit.FOREST);hud.addView(sessionClockView);
        eventView=label("Sampling unit timer",14,true);
        eventView.setTextColor(MUTED);hud.addView(eventView);
        progressView=label("",12,false);progressView.setTextColor(MUTED);hud.addView(progressView);
        pauseStatusView=label("",12,true);
        pauseStatusView.setTextColor(UiKit.AMBER);hud.addView(pauseStatusView);
        gpsView=label("GPS searching · details",12,true);
        gpsView.setTextColor(UiKit.AMBER);
        gpsView.setOnClickListener(v->showGpsDetail());
        gpsView.setMinHeight(dp(44));
        gpsView.setGravity(Gravity.CENTER_VERTICAL);
        gpsView.setFocusable(true);
        gpsView.setContentDescription("Current GPS status. Open GPS details");
        hud.addView(gpsView);
        startGpsBanner=label("Initial GPS missing · location unverified",12,true);
        startGpsBanner.setTextColor(UiKit.AMBER);
        hud.addView(startGpsBanner);
        refreshStartGpsBanner();
        root.addView(hud,hudLp);
        fieldScroll=new ScrollView(this);fieldScroll.setFillViewport(false);fieldScroll.setVerticalScrollBarEnabled(false);
        LinearLayout content=vertical();content.setPadding(dp(1),0,dp(1),dp(12));
        groupBar=horizontal();content.addView(groupBar);showGroups();
        LinearLayout methodRow=horizontal();
        // The method was already chosen in survey setup. Do not offer a second, disabled selector.
        TextView methodName=label(SurveyMethods.displayLabel(profile,method),14,true);
        methodName.setTextColor(UiKit.FOREST);
        methodRow.addView(methodName,weighted(1f));
        lineRow=horizontal();lineEntry=edit("Transect magnetic bearing °",InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        lineRow.addView(lineEntry,new LinearLayout.LayoutParams(-1,-2));
        content.addView(methodRow);
        // Avoid splitting the method name and bearing input on narrow phones.
        content.addView(lineRow,new LinearLayout.LayoutParams(-1,-2));
        LinearLayout utilityRow=horizontal();
        pauseButton=UiKit.button(this,"Pause effort","secondary");
        utilityRow.addView(pauseButton,new LinearLayout.LayoutParams(0,dp(47),1));
        progressButton=UiKit.button(this,"Station progress","secondary");
        LinearLayout.LayoutParams progressLp=new LinearLayout.LayoutParams(0,dp(47),1);
        progressLp.leftMargin=dp(7);utilityRow.addView(progressButton,progressLp);
        content.addView(utilityRow);
        progressButton.setVisibility(hasStationUnits()?View.VISIBLE:View.GONE);
        pauseButton.setOnClickListener(v->togglePause());
        progressButton.setOnClickListener(v->openProgress());
        vibrationChoice=new CheckBox(this);
        vibrationChoice.setText("Vibrate near interval end (optional)");
        vibrationChoice.setTextSize(12);
        vibrationChoice.setChecked(getSharedPreferences("field_prefs",MODE_PRIVATE).getBoolean("vibrate_warnings",false));
        vibrationChoice.setOnCheckedChangeListener((button,enabled)->
            getSharedPreferences("field_prefs",MODE_PRIVATE).edit().putBoolean("vibrate_warnings",enabled).apply());
        LinearLayout sessionSettings=vertical();
        sessionSettings.setVisibility(View.GONE);
        sessionSettings.addView(vibrationChoice);
        TextView sessionToggle=label("Haptic alert  ▾",13,true);
        sessionToggle.setTextColor(GREEN);disclosureTarget(sessionToggle);
        sessionToggle.setPadding(dp(6),dp(7),dp(6),dp(8));
        sessionToggle.setOnClickListener(v->{
            boolean expanded=sessionSettings.getVisibility()!=View.VISIBLE;
            sessionSettings.setVisibility(expanded?View.VISIBLE:View.GONE);
            sessionToggle.setText(expanded?"Haptic alert  ▴":"Haptic alert  ▾");
        });
        // Optional settings are placed below the observation form.
        pointNextButton=UiKit.button(this,"Finish point · Next station","camera");
        content.addView(pointNextButton,new LinearLayout.LayoutParams(-1,dp(53)));
        pointNextButton.setVisibility(hasStationUnits()?View.VISIBLE:View.GONE);
        pointNextButton.setOnClickListener(v->{if(method.equals("point_count"))nextPoint();else nextSamplingUnit();});
        trackingView=label("Track: awaiting GPS",12,true);
        trackingView.setTextColor(UiKit.BLUE);
        content.addView(trackingView);
        trackingView.setVisibility(method.equals("line_transect")||method.equals("ves_transect")?View.VISIBLE:View.GONE);

        LinearLayout detectRow=horizontal();
        Button markTime=UiKit.button(this,"Mark now","secondary");
        detectRow.addView(markTime,weighted(0.9f));
        markButton=UiKit.button(this,"Measure bearing","secondary");
        markButton.setContentDescription("Open sighting camera to capture detection bearing and pitch");
        detectRow.addView(markButton,weighted(1.1f));
        content.addView(detectRow);
        markTime.setOnClickListener(v->markDetection());
        markButton.setOnClickListener(v->openCamera());
        if(FieldUiPolicy.birdDetails(profile)||FieldUiPolicy.herpetofaunaDetails(profile)){
            Button sound=UiKit.button(this,"♪ Record / Review Calls","secondary");
            sound.setContentDescription("Record, view spectrogram and label bird or amphibian calls");
            LinearLayout.LayoutParams soundLp=new LinearLayout.LayoutParams(-1,dp(52));
            soundLp.topMargin=dp(7);content.addView(sound,soundLp);
            sound.setOnClickListener(v->openCallLibrary());
        }
        if(TrapProtocolPolicy.supported(method)){
            Button devices=UiKit.button(this,"▣ Devices, Checks & Captures","secondary");
            LinearLayout.LayoutParams deviceLp=new LinearLayout.LayoutParams(-1,dp(52));
            deviceLp.topMargin=dp(7);content.addView(devices,deviceLp);
            devices.setOnClickListener(v->openTrapProtocol());
        }
        if(FieldProtocolPolicy.supported(method)){
            Button methodRecords=UiKit.button(this,"▣ Method Effort & Evidence","secondary");
            LinearLayout.LayoutParams methodLp=new LinearLayout.LayoutParams(-1,dp(52));
            methodLp.topMargin=dp(7);content.addView(methodRecords,methodLp);
            methodRecords.setOnClickListener(v->openFieldProtocol());
        }
        LinearLayout markRow=horizontal();
        markRowView=markRow;
        markRow.setVisibility(View.GONE);
        bearingStatusView=label("",12,false);
        bearingStatusView.setTextColor(MUTED);
        markRow.addView(bearingStatusView,weighted(1));
        TextView clear=label("Clear mark",12,true);
        clear.setTextColor(GREEN);
        clear.setPadding(dp(10),dp(8),dp(6),dp(8));
        clear.setOnClickListener(v->clearMark());
        markRow.addView(clear);
        content.addView(markRow);
        // One row combines direct entry and the complete offline checklist.
        // Complete species selection now lives on its own page.
        // Retain direct text entry for novel/unidentified field observations.
        boolean stackSpecies=FieldLayoutPolicy.stackSpeciesEntry(
            getResources().getConfiguration().screenWidthDp,
            getResources().getConfiguration().fontScale);
        int fieldControlHeight=FieldLayoutPolicy.actionHeightDp(
            getResources().getConfiguration().fontScale);
        LinearLayout speciesRow=horizontal();
        if(stackSpecies)speciesRow.setOrientation(LinearLayout.VERTICAL);
        taxonEntry=new AutoCompleteTextView(this);
        taxonEntry.setTextSize(16);taxonEntry.setTextColor(INK);taxonEntry.setSingleLine(true);
        taxonEntry.setThreshold(100);taxonEntry.setHint("Species or field ID");
        taxonEntry.setSelectAllOnFocus(true);
        speciesRow.addView(taxonEntry,stackSpecies?
            new LinearLayout.LayoutParams(-1,dp(fieldControlHeight)):
            new LinearLayout.LayoutParams(0,dp(fieldControlHeight),1));
        Button identifySpecies=UiKit.button(this,"Browse","secondary");
        identifySpecies.setContentDescription("Search offline species checklist");
        LinearLayout.LayoutParams browseLp=stackSpecies?
            new LinearLayout.LayoutParams(-1,dp(fieldControlHeight)):
            new LinearLayout.LayoutParams(dp(94),dp(fieldControlHeight));
        if(stackSpecies)browseLp.topMargin=dp(5);
        else browseLp.leftMargin=dp(8);
        speciesRow.addView(identifySpecies,browseLp);
        identifySpecies.setOnClickListener(v->openSpeciesPicker());
        content.addView(speciesRow);
        LinearLayout speciesActions=horizontal();
        Button unknownBtn=UiKit.button(this,"Unidentified","quiet");
        unknownBtn.setTextSize(13);
        LinearLayout.LayoutParams unknownLp=new LinearLayout.LayoutParams(0,dp(46),1);
        speciesActions.addView(unknownBtn,unknownLp);
        favoriteButton=UiKit.button(this,"☆","quiet");
        favoriteButton.setTextSize(22);
        LinearLayout.LayoutParams starP=new LinearLayout.LayoutParams(dp(52),dp(46));
        starP.leftMargin=dp(6);speciesActions.addView(favoriteButton,starP);
        favoriteButton.setContentDescription("Add or remove favorite species");
        favoriteButton.setOnClickListener(v->toggleFavorite());
        content.addView(speciesActions);
        unknownBtn.setOnClickListener(v->{
            String[] choices=Taxa.unknownLabels(group);
            new AlertDialog.Builder(this).setTitle("Record unidentified "+group.toLowerCase(Locale.US))
               .setItems(choices,(dialog,which)->{
                   taxonEntry.setText(choices[which]);taxonEntry.setSelection(taxonEntry.length());
                   confidenceSpinner.setSelection(3);
                   queueDraft();
               }).setNegativeButton("Cancel",null).show();
        });
        taxonEntry.setOnClickListener(v->taxonEntry.showDropDown());
        taxonEntry.setTooltipText("Hold a species name to open its offline reference");
        taxonEntry.setOnLongClickListener(v->{
            Intent reference=new Intent(this,TaxonDetailActivity.class);
            reference.putExtra("taxon_query",taxonEntry.getText().toString());
            startActivity(reference);
            return true;
        });
        taxonEntry.addTextChangedListener(new TextWatcher(){
            public void beforeTextChanged(CharSequence a,int b,int c,int d){}
            public void onTextChanged(CharSequence a,int b,int before,int count){refreshFavoriteIcon();}
            public void afterTextChanged(Editable e){}
        });
        LinearLayout countLine=horizontal();
        String countBasis=method.equals("track_sign")||method.equals("camera_trap_check")||
            method.equals("live_trap_check")||method.equals("bat_acoustic")||
            method.equals("drift_fence_check")?"Individuals / evidence count":"Individuals";
        TextView countLabel=label(countBasis,14,true);countLine.addView(countLabel,weighted(1));
        Button minus=button("−");countLine.addView(minus,new LinearLayout.LayoutParams(dp(55),dp(46)));
        countView=label("1",19,true);countView.setGravity(Gravity.CENTER);
        countLine.addView(countView,new LinearLayout.LayoutParams(dp(48),-2));
        Button plus=button("+");countLine.addView(plus,new LinearLayout.LayoutParams(dp(55),dp(46)));
        content.addView(countLine);
        LinearLayout confidenceBlock=vertical();
        confidenceBlock.addView(label("Identification confidence",12,true));
        confidenceSpinner=new Spinner(this);
        confidenceSpinner.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,
            new String[]{"Field-identified (not expert verified)","Probable","Uncertain","Unidentified"}));
        confidenceBlock.addView(confidenceSpinner);
        minus.setOnClickListener(v->{count=Math.max(1,count-1);countView.setText(""+count);queueDraft();});
        plus.setOnClickListener(v->{count=Math.min(9999,count+1);countView.setText(""+count);queueDraft();});
        LinearLayout row=horizontal();
        distanceEntry=edit("Horizontal distance (m)",InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        row.addView(distanceEntry,weighted(1.2f));
        detectionSpinner=new Spinner(this);
        detectionSpinner.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"Visual","Auditory","Both","Sign","Camera"}));
        row.addView(detectionSpinner,weighted(0.8f));content.addView(row);
        LinearLayout birdQuick=vertical();
        birdMeasurements=vertical();
        distanceSourceSpinner=new Spinner(this);
        distanceSourceSpinner.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,
            new String[]{"Unknown","Estimated","Measured"}));
        birdQuick.addView(label("Distance method",13,true));
        birdQuick.addView(distanceSourceSpinner);
        if(FieldUiPolicy.birdDetails(profile)){
            content.addView(section("Bird distance sampling"));
            content.addView(birdQuick);
        }
        firstPositionSpinner=new Spinner(this);
        firstPositionSpinner.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,
            new String[]{"Unknown","Yes","No"}));
        birdMeasurements.addView(label("Distance refers to first detection?",12,false));
        birdMeasurements.addView(firstPositionSpinner);
        movementSpinner=new Spinner(this);
        movementSpinner.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,
            new String[]{"Unknown","Stationary","Moving","Flyover","Entering after start"}));
        birdMeasurements.addView(label("Movement at first detection",12,false));
        birdMeasurements.addView(movementSpinner);
        if(FieldUiPolicy.birdDetails(profile))content.addView(birdMeasurements);
        TextView birdToggle=label("＋ First position and movement",13,true);
        birdToggle.setTextColor(GREEN);disclosureTarget(birdToggle);
        if(profile.equals("Birds")){
            // Keep the distance sampling quality items accessible in one tap.
            content.addView(birdToggle);
            birdMeasurements.setVisibility(View.GONE);
            birdToggle.setOnClickListener(v->{
                boolean show=birdMeasurements.getVisibility()!=View.VISIBLE;
                birdMeasurements.setVisibility(show?View.VISIBLE:View.GONE);
                birdToggle.setText(show?"− Hide first position and movement":"＋ First position and movement");
            });
        }
        LinearLayout herpQuick=vertical();
        herpPanel=vertical();
        microhabitatEntry=edit("Microhabitat (stream edge, litter, canopy...)",
            InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        herpQuick.addView(microhabitatEntry);
        herpHeightEntry=edit("Height above ground (m, optional)",InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        herpPanel.addView(herpHeightEntry);
        tempEntry=edit("Air temperature °C",InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL|InputType.TYPE_NUMBER_FLAG_SIGNED);
        herpPanel.addView(tempEntry);
        humidityEntry=edit("Relative humidity %",InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        herpPanel.addView(humidityEntry);
        weatherEntry=edit("Weather / search conditions",InputType.TYPE_CLASS_TEXT);
        herpPanel.addView(weatherEntry);
        substrateSpinner=new Spinner(this);
        substrateSpinner.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,
            new String[]{"Not recorded","Leaf litter","Soil","Rock","Log","Vegetation","Water","Human structure","Other"}));
        herpPanel.addView(label("Substrate",12,false));herpPanel.addView(substrateSpinner);
        callScoreSpinner=new Spinner(this);
        callScoreSpinner.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,
            new String[]{"Not scored","0 — no calls","1 — individuals distinguishable","2 — overlapping calls","3 — full chorus"}));
        callScoreLabel=label("Amphibian call index (0–3)",12,false);
        herpQuick.addView(callScoreLabel);herpQuick.addView(callScoreSpinner);
        lifeStageSpinner=new Spinner(this);
        lifeStageSpinner.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,
            new String[]{"Life stage not recorded","Egg","Larva / tadpole","Juvenile","Subadult","Adult","Unknown"}));
        herpQuick.addView(label("Life stage",13,true));herpQuick.addView(lifeStageSpinner);
        noiseSpinner=new Spinner(this);
        noiseSpinner.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,
            new String[]{"Noise not assessed","0 — none","1 — slight","2 — moderate","3 — serious","4 — severe"}));
        noiseLabel=label("Background noise during amphibian listening",12,false);
        herpQuick.addView(noiseLabel);herpQuick.addView(noiseSpinner);
        if(FieldUiPolicy.herpetofaunaDetails(profile)){
            content.addView(section("Herpetofauna field evidence"));
            content.addView(herpQuick);
            content.addView(herpPanel);
        }
        TextView herpToggle=label("＋ Weather and substrate details",13,true);
        herpToggle.setTextColor(GREEN);disclosureTarget(herpToggle);
        if(profile.equals("Herpetofauna")){
            content.addView(herpToggle);herpPanel.setVisibility(View.GONE);
            herpToggle.setOnClickListener(v->{
                boolean open=herpPanel.getVisibility()!=View.VISIBLE;
                herpPanel.setVisibility(open?View.VISIBLE:View.GONE);
                herpToggle.setText(open?"− Hide weather and substrate":"＋ Weather and substrate details");
            });
        }
        LinearLayout mammalQuick=vertical();
        mammalPanel=vertical();
        evidenceSpinner=new Spinner(this);
        evidenceSpinner.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,
            new String[]{"Evidence not recorded","Direct sighting","Track","Scat","Burrow / den","Camera image","Acoustic detection","Live capture","Other sign"}));
        mammalQuick.addView(label("Type of evidence",13,true));mammalQuick.addView(evidenceSpinner);
        deviceEntry=edit("Equipment or trap ID (optional)",InputType.TYPE_CLASS_TEXT);
        mammalQuick.addView(deviceEntry);
        perchEntry=edit("Height above ground (m, optional)",InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        mammalPanel.addView(perchEntry);
        if(FieldUiPolicy.mammalDetails(profile)){
            content.addView(section("Mammal evidence"));
            content.addView(mammalQuick);
            content.addView(mammalPanel);
        }
        TextView mammalToggle=label("＋ Additional mammal measurements",13,true);
        mammalToggle.setTextColor(GREEN);disclosureTarget(mammalToggle);
        if(profile.equals("Mammals")){
            content.addView(mammalToggle);mammalPanel.setVisibility(View.GONE);
            mammalToggle.setOnClickListener(v->{
                boolean open=mammalPanel.getVisibility()!=View.VISIBLE;
                mammalPanel.setVisibility(open?View.VISIBLE:View.GONE);
                mammalToggle.setText(open?"− Hide additional measurements":"＋ Additional mammal measurements");
            });
        }
        LinearLayout photoRow=horizontal();
        Button photoButton=UiKit.button(this,"Add photo","secondary");
        photoRow.addView(photoButton,new LinearLayout.LayoutParams(0,dp(49),2));
        photoStatus=label("",12,false);
        photoStatus.setVisibility(View.GONE);
        photoRow.addView(photoStatus,new LinearLayout.LayoutParams(0,-2,2));
        content.addView(photoRow);
        photoButton.setOnClickListener(v->pickPhoto());
        photoPreview=new ImageView(this);
        photoPreview.setAdjustViewBounds(true);
        photoPreview.setMaxHeight(dp(180));
        photoPreview.setVisibility(View.GONE);content.addView(photoPreview);
        detailToggle=label("＋ More observation details",14,true);
        detailToggle.setTextColor(GREEN);disclosureTarget(detailToggle);content.addView(detailToggle);
        advancedPanel=vertical();
        habitatEntry=edit("Habitat / microhabitat",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        behaviorEntry=edit("Behavior / reproductive evidence",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        notesEntry=edit("Observation notes",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        notesEntry.setSingleLine(false);notesEntry.setMinLines(2);
        identificationNote=edit("Identification evidence / uncertainty (optional)",
            InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        advancedPanel.addView(confidenceBlock);
        advancedPanel.addView(identificationNote);
        advancedPanel.addView(habitatEntry);advancedPanel.addView(behaviorEntry);advancedPanel.addView(notesEntry);
        advancedPanel.setVisibility(View.GONE);content.addView(advancedPanel);
        detailToggle.setOnClickListener(v->{detailShown=!detailShown;advancedPanel.setVisibility(detailShown?View.VISIBLE:View.GONE);queueDraft();
            detailToggle.setText(detailShown?"− Hide observation details":"＋ More observation details");});
        content.addView(sessionToggle);
        content.addView(sessionSettings);
        // The field form stays focused on observations. Method tips live in Help.
        fieldScroll.addView(content);
        root.addView(fieldScroll,new LinearLayout.LayoutParams(-1,0,1));
        // Two-tier fixed action dock. Save is the only filled primary action.
        LinearLayout footer=vertical();
        footer.setPadding(0,dp(5),0,0);
        saveButton=UiKit.button(this,"Save Observation","primary");
        footer.addView(saveButton,new LinearLayout.LayoutParams(-1,dp(
            FieldLayoutPolicy.saveHeightDp(getResources().getConfiguration().fontScale))));
        LinearLayout footerOptions=horizontal();
        reviewButton=UiKit.button(this,"Review Records","secondary");
        LinearLayout.LayoutParams reviewParams=new LinearLayout.LayoutParams(0,dp(
            FieldLayoutPolicy.actionHeightDp(getResources().getConfiguration().fontScale)),1);
        footerOptions.addView(reviewButton,reviewParams);
        // Ending a survey remains accessible even with the keyboard open.
        endSurveyButton=UiKit.button(this,"End Survey","outline-danger");
        LinearLayout.LayoutParams endParams=new LinearLayout.LayoutParams(0,dp(
            FieldLayoutPolicy.actionHeightDp(getResources().getConfiguration().fontScale)),1);
        endParams.leftMargin=dp(8);
        footerOptions.addView(endSurveyButton,endParams);
        LinearLayout.LayoutParams optionsParams=new LinearLayout.LayoutParams(-1,-2);
        optionsParams.topMargin=dp(6);
        footer.addView(footerOptions,optionsParams);
        root.addView(footer);
        endSurveyButton.setEnabled(active);
        endSurveyButton.setOnClickListener(v->finishPrompt());
        saveButton.setEnabled(active&&(!method.equals("point_count")||pointCountId!=null));
        if(pointNextButton!=null&&pointCountId==null&&method.equals("point_count"))
            pointNextButton.setText("Start next point count");
        if(plannedStationCount>0&&method.equals("point_count"))refreshPlannedPointButton();
        if(pointNextButton!=null&&requiresGenericUnit()&&samplingUnitId==null)
            pointNextButton.setText("Start next station");
        if(requiresGenericUnit()&&samplingUnitId==null)saveButton.setEnabled(false);
        saveButton.setOnClickListener(v->save());
        reviewButton.setOnClickListener(v->{
            persistDraft();
            startActivity(new Intent(this,HistoryActivity.class).putExtra("event_id",eventId));
        });
        setContentView(root);
        updateMethods();refreshAutocomplete();status();
        watchDraftFields();

    }
    private void showGroups(){
        groupBar.removeAllViews();
        boolean canSwitch=profile.equals("Herpetofauna")&&!method.equals("amphibian_calls");
        groupBar.setVisibility(canSwitch?View.VISIBLE:View.GONE);
        if(!canSwitch)return;
        for(String name:new String[]{"Reptiles","Amphibians"}){
            Button b=button(name);
            b.setTextSize(14);
            b.setBackgroundTintList(ColorStateList.valueOf(name.equals(group)?GREEN:MUTED));
            groupBar.addView(b,new LinearLayout.LayoutParams(0,dp(45),1));
            b.setOnClickListener(v->{persistDraft();group=name;showGroups();refreshAutocomplete();queueDraft();});
        }
    }
    private void updateMethods(){
        lineRow.setVisibility(SurveyMethods.isBirdLine(profile,method)?View.VISIBLE:View.GONE);
    }
    private void toggleFavorite(){
        if(taxonEntry==null)return;
        String term=taxonEntry.getText().toString().trim();
        if(term.length()<2){toast("Select or type a taxon before marking favorite");return;}
        SQLiteDatabase d=db.getWritableDatabase();
        int exists=0;
        try(Cursor c=d.rawQuery("SELECT COUNT(*) FROM taxa_favorites WHERE taxon_group=? AND raw_identification=?",
            new String[]{group,term})){
            if(c.moveToFirst())exists=c.getInt(0);
        }
        if(exists>0)d.execSQL("DELETE FROM taxa_favorites WHERE taxon_group=? AND raw_identification=?",
            new Object[]{group,term});
        else d.execSQL("INSERT OR IGNORE INTO taxa_favorites(taxon_group,raw_identification) VALUES(?,?)",
            new Object[]{group,term});
        refreshFavoriteIcon();updateTaxaSuggestions(false);
        toast(exists>0?"Removed favorite":"Favorite saved");
    }
    private void refreshFavoriteIcon(){
        if(favoriteButton==null||taxonEntry==null||db==null)return;
        String name=taxonEntry.getText().toString().trim();
        boolean starred=false;
        if(!name.isEmpty())try(Cursor c=db.getReadableDatabase().rawQuery(
            "SELECT 1 FROM taxa_favorites WHERE taxon_group=? AND raw_identification=?",
            new String[]{group,name})){
            starred=c.moveToFirst();
        }catch(Exception ignored){}
        favoriteButton.setText(starred?"★":"☆");
    }
    private void openSpeciesPicker(){
        if(!active){toast("Start a survey first");return;}
        persistDraft();
        Intent screen=new Intent(this,SpeciesPickerActivity.class);
        screen.putExtra("group",group);
        screen.putExtra("profile",profile);
        screen.putExtra("survey_id",eventId);
        String currentVisit=pointCountId!=null?pointCountId:
            samplingUnitId!=null?samplingUnitId:"SURVEY-"+eventId;
        screen.putExtra("visit_id",currentVisit);
        screen.putExtra("station_id",stationId);
        screen.putExtra("geo_region",geographicRegion);
        screen.putExtra("province",adminProvince);
        if(habitatEntry!=null)screen.putExtra("habitat",habitatEntry.getText().toString());
        startActivityForResult(screen,IDENTIFY_SPECIES_REQ);
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
    }
    private void pickPhoto(){
        Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent,PHOTO_PICK);
    }
    private void showPendingPhoto(){
        if(photoStatus!=null){
            photoStatus.setText(pendingPhoto==null?"":"Photo saved locally");
            photoStatus.setVisibility(pendingPhoto==null?View.GONE:View.VISIBLE);
        }
        if(photoPreview!=null){
            photoPreview.setVisibility(pendingPhoto==null?View.GONE:View.VISIBLE);
            if(pendingPhoto!=null)PhotoEvidence.preview(this,pendingPhoto,photoPreview);
            else photoPreview.setImageDrawable(null);
        }
    }
    private void refreshAutocomplete(){updateTaxaSuggestions(true);}
    private static String nonNull(String value){return value==null?"":value;}
    private void updateTaxaSuggestions(boolean clear){
        if(taxonEntry==null)return;
        // Full and local catalogs are selected in Identify species. The inline field accepts any name.
        boolean regional=false;
        if(taxonSuggestionAdapter==null){
            taxonSuggestionAdapter=new TaxonSuggestionAdapter(this,db,group,
                geographicRegion,adminPsgc,regional);
            taxonEntry.setAdapter(taxonSuggestionAdapter);
        }else taxonSuggestionAdapter.setScope(group,geographicRegion,adminPsgc,regional);
        taxonEntry.setHint(group.equals("Birds")?"Bird name or unidentified bird":
            "Species or unidentified "+group.toLowerCase(Locale.US));
        if(clear)taxonEntry.setText("");
        else taxonSuggestionAdapter.getFilter().filter(taxonEntry.getText());
        if(distanceEntry!=null)distanceEntry.setHint(group.equals("Birds")?
            "Horizontal distance (m)":"Distance (m, optional)");
        if(birdMeasurements!=null&&!group.equals("Birds"))
            birdMeasurements.setVisibility(View.GONE);
        if(herpPanel!=null&&!profile.equals("Herpetofauna"))
            herpPanel.setVisibility(View.GONE);
        if(mammalPanel!=null&&!profile.equals("Mammals"))
            mammalPanel.setVisibility(View.GONE);
        boolean calls=FieldUiPolicy.callIndex(profile,group);
        if(callScoreSpinner!=null)callScoreSpinner.setVisibility(calls?View.VISIBLE:View.GONE);
        if(callScoreLabel!=null)callScoreLabel.setVisibility(calls?View.VISIBLE:View.GONE);
        boolean listening=FieldUiPolicy.listeningNoise(profile,group,method);
        if(noiseSpinner!=null)noiseSpinner.setVisibility(listening?View.VISIBLE:View.GONE);
        if(noiseLabel!=null)noiseLabel.setVisibility(listening?View.VISIBLE:View.GONE);
    }
    private void refreshStartGpsBanner(){
        if(startGpsBanner==null)return;
        boolean missing="missing_at_survey_start".equals(startGpsStatus);
        startGpsBanner.setVisibility(missing?View.VISIBLE:View.GONE);
    }
    private void restoreEvent(){
        try(Cursor c=db.getReadableDatabase().rawQuery("SELECT id,method,started,COALESCE(taxon_group,'Birds'),COALESCE(survey_profile,'Birds'),COALESCE(station_id,''),COALESCE(observer_name,''),COALESCE(point_target_s,600),COALESCE(protocol_status,'legacy'),COALESCE(project_id,'default'),COALESCE(region_label,''),area_km2,transect_az,point_radius_m,COALESCE(start_gps_status,'legacy_start_location_unrecorded') FROM surveys WHERE ended IS NULL ORDER BY started DESC LIMIT 1",null)){
            if(c.moveToFirst()){
                eventId=c.getString(0);method=c.getString(1);String time=c.getString(2);
                group=c.getString(3);profile=c.getString(4);stationId=c.getString(5);
                observerName=c.getString(6);pointTargetSeconds=c.getInt(7);
                basicLogOnly="basic_log_only".equals(c.getString(8));
                projectId=c.getString(9);regionLabel=c.getString(10);
                stratumAreaKm2=c.isNull(11)?null:c.getDouble(11);
                lineBearing=c.isNull(12)?null:c.getDouble(12);
                pointRadiusM=c.isNull(13)?null:c.getDouble(13);
                startGpsStatus=c.getString(14);
                startedAt=parseIso(time);active=true;
            }
        }catch(Exception ignored){}
        if(active){
            try(Cursor region=db.getReadableDatabase().rawQuery(
                "SELECT COALESCE(geographic_region_id,'PH') FROM surveys WHERE id=?",new String[]{eventId})){
                if(region.moveToFirst())geographicRegion=region.getString(0);
                try(Cursor admin=db.getReadableDatabase().rawQuery(
                    "SELECT COALESCE(admin_province_name,''),COALESCE(admin_province_psgc,'') FROM surveys WHERE id=?",
                    new String[]{eventId})){
                    if(admin.moveToFirst()){adminProvince=admin.getString(0);adminPsgc=admin.getString(1);}
                }
            }catch(Exception ignored){}
            try(Cursor pc=db.getReadableDatabase().rawQuery(
                "SELECT COALESCE(total_paused_ms,0),COALESCE(paused_at_utc,''),COALESCE(effort_paused,0) FROM surveys WHERE id=?",
                new String[]{eventId})){
                if(pc.moveToFirst()){
                    accumulatedPausedMs=pc.getLong(0);
                    surveyPaused=pc.getInt(2)==1;
                    pauseStartedAt=surveyPaused&&!pc.getString(1).isEmpty()?parseIso(pc.getString(1)):0;
                }
            }catch(Exception ignored){}
            try(Cursor c=db.getReadableDatabase().rawQuery(
                "SELECT id,station_id,sequence_number,started FROM point_counts WHERE event_id=? AND ended IS NULL ORDER BY sequence_number DESC LIMIT 1",
                new String[]{eventId})){
                if(c.moveToFirst()){
                    pointCountId=c.getString(0);stationId=c.getString(1);pointSequence=c.getInt(2);
                    pointStartedAt=parseIso(c.getString(3));
                }
            }catch(Exception ignored){}
            try(Cursor c=db.getReadableDatabase().rawQuery(
                "SELECT id,station_id,sequence_number,started FROM sampling_units WHERE event_id=? AND ended IS NULL ORDER BY sequence_number DESC LIMIT 1",
                new String[]{eventId})){
                if(c.moveToFirst()){
                    samplingUnitId=c.getString(0);stationId=c.getString(1);unitSequence=c.getInt(2);
                    unitStartedAt=parseIso(c.getString(3));
                }
            }catch(Exception ignored){}
            try(Cursor c=db.getReadableDatabase().rawQuery(
                "SELECT COALESCE(planned_duration_s,0),COALESCE(planned_route_m,0),"+
                "COALESCE(planned_station_spacing_m,0),COALESCE(planned_station_count,0) "+
                "FROM surveys WHERE id=?",new String[]{eventId})){
                if(c.moveToFirst()){
                    plannedDurationSeconds=c.getInt(0);plannedRouteMeters=c.getDouble(1);
                    plannedSpacingMeters=c.getDouble(2);plannedStationCount=c.getInt(3);
                }
            }catch(Exception ignored){}
        }
    }
    private void toggleEvent(){
        if(active){finishPrompt();return;}
        // The survey method comes from project setup; do not silently replace it
        // with a default spinner selection during first launch.
        if(method==null||method.isEmpty()){toast("Survey method missing. Return to setup.");return;}
        Double line=number(lineEntry.getText().toString());
        if(SurveyMethods.isBirdLine(profile,method) && (line==null||line<0||line>=360)){
            toast("Enter transect MAGNETIC bearing from 0 to 359°");return;
        }
        // Default start requires verified GNSS. The observer can explicitly flag a missing fix.
        Intent launchGps=getIntent();
        boolean withoutGps=launchGps.getBooleanExtra("start_without_gps",false);
        double startLatitude=launchGps.getDoubleExtra("start_gps_latitude",Double.NaN);
        double startLongitude=launchGps.getDoubleExtra("start_gps_longitude",Double.NaN);
        float startAccuracy=launchGps.getFloatExtra("start_gps_accuracy_m",Float.NaN);
        long startGpsTime=launchGps.getLongExtra("start_gps_recorded_at_ms",0L);
        long startElapsedNanos=launchGps.getLongExtra("start_gps_elapsed_nanos",0L);
        long monotonicAgeMs=(SystemClock.elapsedRealtimeNanos()-startElapsedNanos)/1000000L;
        String startQuality=SurveyStartGpsPolicy.classify(withoutGps,
            startLatitude,startLongitude,startAccuracy,monotonicAgeMs,false);
        if(startElapsedNanos<=0&&!withoutGps||startQuality==null){
            toast("Survey-start GPS missing, too old, or not better than 10 m. Retry setup.");
            finish();return;
        }
        startGpsStatus=startQuality;
        eventId="SV-"+System.currentTimeMillis();startedAt=System.currentTimeMillis();pointNotice=false;
        int target=method.equals("point_count")?pointTargetSeconds:0;
        lineBearing=SurveyMethods.isBirdLine(profile,method)?line:null;
        SQLiteDatabase database=db.getWritableDatabase();
        database.beginTransaction();
        try{
            database.execSQL("INSERT INTO surveys(id,method,started,transect_az,taxon_group,point_target_s,"+
              "survey_profile,station_id,observer_name,protocol_status,project_id,region_label,"+
              "area_km2,point_radius_m,planned_duration_s,geographic_region_id,"+
              "geographic_selection_method,regional_taxa_version,admin_province_name,"+
              "admin_province_psgc,admin_boundary_version,admin_boundary_status,"+
              "planned_route_m,planned_station_spacing_m,planned_station_count,route_design,"+
              "start_gps_latitude,start_gps_longitude,start_gps_accuracy_m,start_gps_recorded_at,"+
              "start_gps_status) "+
              "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
              new Object[]{eventId,method,iso(startedAt),lineBearing,group,target,profile,
                "",observerName,basicLogOnly?"basic_log_only":"beta_standardized",projectId,
                regionLabel,stratumAreaKm2,pointRadiusM,plannedDurationSeconds,geographicRegion,
                geographicSelectionMethod,GeoRegions.DATASET,adminProvince,adminPsgc,
                adminBoundaryVersion,adminBoundaryStatus,plannedRouteMeters,
                method.equals("point_count")?plannedSpacingMeters:null,
                method.equals("point_count")?plannedStationCount:null,
                method.equals("point_count")?"planned_chainage_point_route":
                method.equals("line_transect")||method.equals("ves_transect")?
                    "continuous_planned_route":"unsegmented_method",
                withoutGps?null:startLatitude,withoutGps?null:startLongitude,
                withoutGps?null:(double)startAccuracy,withoutGps?null:iso(startGpsTime),
                startGpsStatus});
            if(method.equals("point_count")&&plannedStationCount>0){
                int created=routeTemplateId.isEmpty()?
                    RoutePlan.create(database,eventId,projectId,plannedRouteMeters,plannedSpacingMeters,startedAt):
                    RoutePlan.reuseTemplate(database,routeTemplateId,projectId,eventId,
                        plannedRouteMeters,plannedSpacingMeters);
                if(created!=plannedStationCount)
                    throw new IllegalStateException("Planned station count mismatch");
                if(routeTemplateId.isEmpty()&&saveRouteTemplate){
                    routeTemplateId=RoutePlan.saveTemplate(database,eventId,projectId,
                        routeTemplateName,plannedRouteMeters,plannedSpacingMeters,startedAt);
                }
            }
            StudyProtocols.apply(database,eventId,projectId,method,profile,
                getIntent().getStringExtra("study_protocol_id"),iso(startedAt));
            database.setTransactionSuccessful();
        }catch(Exception ex){
            toast("Could not start survey: "+ex.getMessage());
            eventId=null;return;
        }finally{database.endTransaction();}
        // No station is visited at survey launch. The first GPS point is captured on arrival.
        if(method.equals("point_count")&&plannedStationCount>0)
            stationId="";
        else if(requiresGenericUnit())
            stationId="";
        active=true;
        if(endSurveyButton!=null)endSurveyButton.setEnabled(true);
        if(gpsView!=null)gpsView.setText(withoutGps?
            "GPS pending · no initial survey position":
            String.format(Locale.US,"Survey-start GPS: %.6f, %.6f · ±%.1f m",
                startLatitude,startLongitude,startAccuracy));
        refreshStartGpsBanner();
        saveButton.setEnabled(!hasStationUnits());
        if(hasStationUnits()&&pointNextButton!=null){
            if(method.equals("point_count")&&plannedStationCount>0)
                pointNextButton.setText("Start point 1 · 0 m");
            else pointNextButton.setText("Start first sampling station");
            pointNextButton.setEnabled(true);
        }
        surveyPaused=false;pauseStartedAt=0;accumulatedPausedMs=0;unitPausedMs=0;
        if(method.equals("line_transect")||method.equals("ves_transect"))startTracking();
        lineEntry.setEnabled(false);showGroups();status();
        toast(withoutGps?"Survey started · initial GPS missing and flagged":
            method.equals("point_count")&&plannedStationCount>0?
            plannedStationCount+" stations planned. Start the first count on arrival.":"Survey started");
    }

    /** Wall-clock total is never paused. Pauses affect active effort and interval time only. */
    private long currentPausedMs(long now){
        return accumulatedPausedMs+(surveyPaused&&pauseStartedAt>0?Math.max(0,now-pauseStartedAt):0);
    }
    private void refreshPauseControls(){
        if(pauseButton!=null){
            pauseButton.setText(surveyPaused?"Resume effort":"Pause effort");
            pauseButton.setEnabled(active);
        }
        if(pauseStatusView!=null){
            pauseStatusView.setText(surveyPaused?"Effort paused · resume to continue":"");
            pauseStatusView.setVisibility(surveyPaused?View.VISIBLE:View.GONE);
        }
        if(saveButton!=null&&active)saveButton.setEnabled(!surveyPaused&&
            (!method.equals("point_count")||pointCountId!=null)&&
            (!requiresGenericUnit()||samplingUnitId!=null));
    }
    private void togglePause(){
        if(!active){toast("Start a survey to pause effort");return;}
        if(surveyPaused){endPause();return;}
        final EditText reason=edit("Reason for break (optional)",InputType.TYPE_CLASS_TEXT);
        new AlertDialog.Builder(this).setTitle("Pause sampling effort")
            .setMessage("The total survey clock keeps running. Current unit time and effort pause, and GPS path tracking stops during the break.")
            .setView(reason).setNegativeButton("Cancel",null)
            .setPositiveButton("Pause now",(d,w)->{
                long time=System.currentTimeMillis();
                pauseStartedAt=time;surveyPaused=true;
                SQLiteDatabase writable=db.getWritableDatabase();
                writable.execSQL("UPDATE surveys SET effort_paused=1,paused_at_utc=? WHERE id=?",
                    new Object[]{iso(time),eventId});
                writable.execSQL("INSERT INTO survey_effort_pauses(event_id,paused_at_utc,reason) VALUES(?,?,?)",
                    new Object[]{eventId,iso(time),reason.getText().toString().trim()});
                if(method.equals("line_transect")||method.equals("ves_transect"))stopTracking();
                refreshPauseControls();status();persistDraft();
            }).show();
    }
    private void endPause(){
        if(!surveyPaused||eventId==null)return;
        long now=System.currentTimeMillis();
        long interval=Math.max(0,now-pauseStartedAt);
        accumulatedPausedMs+=interval;
        SQLiteDatabase writable=db.getWritableDatabase();
        writable.beginTransaction();
        try{
            writable.execSQL("UPDATE surveys SET effort_paused=0,paused_at_utc=NULL,total_paused_ms=? WHERE id=?",
                new Object[]{accumulatedPausedMs,eventId});
            writable.execSQL("UPDATE survey_effort_pauses SET resumed_at_utc=?,duration_ms=? WHERE id=("+
                "SELECT id FROM survey_effort_pauses WHERE event_id=? AND resumed_at_utc IS NULL ORDER BY id DESC LIMIT 1)",
                new Object[]{iso(now),interval,eventId});
            if(pointCountId!=null)
                writable.execSQL("UPDATE point_counts SET unit_paused_ms=COALESCE(unit_paused_ms,0)+? WHERE id=?",
                    new Object[]{interval,pointCountId});
            if(samplingUnitId!=null)
                writable.execSQL("UPDATE sampling_units SET unit_paused_ms=COALESCE(unit_paused_ms,0)+? WHERE id=?",
                    new Object[]{interval,samplingUnitId});
            writable.setTransactionSuccessful();
        }finally{writable.endTransaction();}
        surveyPaused=false;pauseStartedAt=0;
        cachedPausedUnit="";lastProgressFetchMs=0;
        if(method.equals("line_transect")||method.equals("ves_transect"))startTracking();
        refreshPauseControls();status();persistDraft();
    }
    private long unitElapsedActiveMs(long started,String unitTable,String id,long now){
        if(started<=0)return 0;
        long paused=0;
        if(id!=null){
            String key=unitTable+"|"+id;
            if(!key.equals(cachedPausedUnit)||now-pausedUnitFetchedMs>=5000L){
                cachedPausedUnit=key;pausedUnitFetchedMs=now;pausedUnitCacheMs=0;
                try(Cursor c=db.getReadableDatabase().rawQuery(
                    "SELECT COALESCE(unit_paused_ms,0) FROM "+unitTable+" WHERE id=?",
                    new String[]{id})){
                    if(c.moveToFirst())pausedUnitCacheMs=c.getLong(0);
                }catch(Exception ignored){}
            }
            paused=pausedUnitCacheMs;
        }
        if(surveyPaused&&pauseStartedAt>0)paused+=Math.max(0,now-pauseStartedAt);
        return Math.max(0,now-started-paused);
    }
    private void vibrateIfEnabled(int ms){
        if(!getSharedPreferences("field_prefs",MODE_PRIVATE).getBoolean("vibrate_warnings",false))return;
        try{
            Vibrator vib=(Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
            if(vib!=null&&vib.hasVibrator())vib.vibrate(VibrationEffect.createOneShot(ms,VibrationEffect.DEFAULT_AMPLITUDE));
        }catch(Exception ignored){}
    }
    private void openProgress(){
        if(eventId==null){toast("Start a survey to see station progress");return;}
        persistDraft();
        startActivity(new Intent(this,StationProgressActivity.class).putExtra("event_id",eventId));
    }
    private void showNotCountedReason(){
        if(!active)return;
        final EditText reason=edit("Why should this survey not be counted? (required)",InputType.TYPE_CLASS_TEXT|
            InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        LinearLayout wrapper=vertical();wrapper.setPadding(dp(18),dp(12),dp(18),dp(10));
        wrapper.addView(label("All records will remain in the archive with the exclusion reason, but the survey is excluded from scientific summaries and analytical outputs.",13,false));
        wrapper.addView(reason);
        AlertDialog dialog=new AlertDialog.Builder(this)
            .setTitle("Exclude this survey from counting?")
            .setView(wrapper).setNegativeButton("Cancel",null)
            .setPositiveButton("Mark NOT COUNTED",null).create();
        dialog.setOnShowListener(v->{
            Button confirm=dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            confirm.setTextColor(Color.rgb(188,35,35));
            confirm.setOnClickListener(w->{
                String why=reason.getText().toString().trim();
                if(why.length()<5){reason.setError("Enter a specific reason (at least 5 characters)");return;}
                dialog.dismiss();
                finish(false,null,"not_counted",why);
            });
        });
        dialog.show();
    }
    /**
     * Survey disposition is available at all times through the pinned footer.
     * Each outcome is deliberate and distinguishable, including cancellation.
     */
    private void finishPrompt(){
        if(!active)return;
        if(taxonEntry!=null&&!taxonEntry.getText().toString().trim().isEmpty()){
            new AlertDialog.Builder(this).setTitle("Unsaved observation")
                .setMessage("Save this observation before ending the survey, "+
                    "or discard only the unfinished entry.")
                .setNegativeButton("Keep recording",null)
                .setPositiveButton("Discard entry",(d,w)->{
                    taxonEntry.setText("");distanceEntry.setText("");clearMark();
                    finishPrompt();
                }).show();
            return;
        }
        LinearLayout actions=vertical();
        actions.setPadding(dp(14),dp(5),dp(14),dp(6));
        Button complete=UiKit.button(this,"Finish Complete","primary");
        Button incomplete=UiKit.button(this,"End Incomplete","secondary");
        Button cancel=UiKit.button(this,"Cancel Survey · Not Counted","danger");
        for(Button button:new Button[]{complete,incomplete,cancel}){
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(54));
            lp.bottomMargin=dp(10);
            actions.addView(button,lp);
        }
        TextView details=label("Complete: requirements met. Incomplete: save partial work. "+
            "Not Counted: exclude from analysis.",12,false);
        details.setTextColor(MUTED);actions.addView(details);
        AlertDialog dialog=new AlertDialog.Builder(this)
            .setTitle("End Survey")
            .setView(actions)
            .setNegativeButton("Keep recording",null).create();
        complete.setOnClickListener(v->{dialog.dismiss();showFinalizationForm(true);});
        incomplete.setOnClickListener(v->{dialog.dismiss();showFinalizationForm(false);});
        cancel.setOnClickListener(v->{dialog.dismiss();showNotCountedReason();});
        dialog.show();
    }
    /** Record true effort, then validate the complete/incomplete classification. */
    private String completionChecklist(){
        if(eventId==null)return "Survey has not started";
        StringBuilder result=new StringBuilder();
        if(hasStationUnits()){
            int[] visits=visitProgress();
            int waiting=0;
            if(method.equals("point_count")&&plannedStationCount>0){
                try(Cursor c=db.getReadableDatabase().rawQuery(
                    "SELECT COUNT(*) FROM survey_stations WHERE event_id=? "+
                    "AND visit_state<>'completed'",new String[]{eventId})){
                    if(c.moveToFirst())waiting=c.getInt(0);
                }catch(Exception ignored){}
            }
            result.append(visits[0]>0?"✓":"○")
                .append(" Recorded sampling visits: ").append(visits[0]).append("\n");
            result.append(visits[0]>0&&visits[0]==visits[1]?"✓":"○")
                .append(" Completed sampling visits: ").append(visits[1])
                .append("/").append(visits[0]).append("\n");
            if(plannedStationCount>0)
                result.append(waiting==0?"✓":"○")
                    .append(" Planned stations completed: ")
                    .append(plannedStationCount-waiting).append("/")
                    .append(plannedStationCount).append("\n");
            if(pointCountId!=null||samplingUnitId!=null)
                result.append("○ Current sampling visit closes with the survey\n");
        }else if(plannedDurationSeconds>0){
            long activeMs=Math.max(0,System.currentTimeMillis()-startedAt-
                currentPausedMs(System.currentTimeMillis()));
            result.append(activeMs>=1000L*plannedDurationSeconds?"✓":"○")
                .append(" Planned active effort: ").append(activeMs/1000)
                .append("/").append(plannedDurationSeconds).append(" seconds\n");
        }
        if(SurveyClosePolicy.routeLengthRequired(method))
            result.append("○ Measured transect length and route confirmation required\n");
        if(SurveyClosePolicy.searchedAreaRequired(method))
            result.append("○ Measured search area required\n");
        if(FieldProtocolPolicy.supported(method)){
            int[] f=methodCompleteness();
            boolean complete=FieldProtocolPolicy.canComplete(method,f[0],f[1],f[2]);
            result.append(complete?"✓":"○").append(" Protocol effort: ")
                .append(f[0]).append(" units or passes, ")
                .append(f[1]).append(" uninspected boards, ")
                .append(f[2]).append(" invalid effort entries\n");
        }
        StudyProtocols.Rule study=StudyProtocols.snapshot(db.getReadableDatabase(),eventId);
        if(study!=null){
            int totalUnits=studyEvidenceUnits();
            long elapsed=Math.max(0,System.currentTimeMillis()-startedAt-
                currentPausedMs(System.currentTimeMillis()));
            if(hasStationUnits()){
                elapsed=0;
                String table=method.equals("point_count")?"point_counts":"sampling_units";
                try(Cursor checks=db.getReadableDatabase().rawQuery(
                    "SELECT started,ended,COALESCE(unit_paused_ms,0) FROM "+table+
                    " WHERE event_id=? AND ended IS NOT NULL",new String[]{eventId})){
                    while(checks.moveToNext())elapsed+=Math.max(0,
                        parseIso(checks.getString(1))-parseIso(checks.getString(0))-
                        checks.getLong(2));
                }
            }
            int visits=hasStationUnits()?visitProgress()[1]:0;
            boolean eligible=StudyProtocols.passes(study,visits,elapsed,totalUnits,
                "verified_start_fix_under_10m".equals(startGpsStatus),
                StudyProtocols.photoCount(db.getReadableDatabase(),eventId));
            result.append(eligible?"✓":"○").append(" Study template: ")
                .append(study.name).append(" · visits ").append(visits)
                .append("/").append(study.visits).append(" · minutes ")
                .append(elapsed/60000).append("/").append(study.minutes)
                .append(" · units ").append(totalUnits).append("/")
                .append(study.units).append("\n");
        }
        if(TrapProtocolPolicy.supported(method)){
            int[] d=deviceCompleteness();
            boolean ready=TrapProtocolPolicy.canComplete(d[0],d[1],d[2],d[3]);
            result.append(ready?"✓":"○").append(" Device deployment, check, retrieval and capture history: ")
                .append(d[0]).append(" devices, ").append(d[1]).append(" unchecked, ")
                .append(d[2]).append(" still deployed (allowed), ").append(d[3])
                .append(" undocumented captures\n");
        }
        result.append("✓ Saved detections remain in the field archive");
        return result.toString();
    }
    private void showFinalizationForm(boolean requestedComplete){
        LinearLayout panel=vertical();
        panel.setPadding(dp(15),dp(7),dp(15),dp(6));
        boolean route=SurveyClosePolicy.routeLengthRequired(method);
        boolean searchArea=SurveyClosePolicy.searchedAreaRequired(method);
        boolean optionalLength=SurveyMethods.needsRouteLength(method)&&!route;
        EditText length=edit(route?"Measured route length (m)":
            "Searched length (m, optional)",
            InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        EditText area=edit(searchArea?"Measured area searched (m²)":
            "Area searched (m², optional)",
            InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        if(requestedComplete){
            TextView checklist=label(completionChecklist(),13,false);
            checklist.setTextColor(INK);
            checklist.setPadding(dp(3),dp(3),dp(3),dp(12));
            panel.addView(checklist);
        }
        if(route||optionalLength)panel.addView(length);
        final CheckBox confirmedRoute=new CheckBox(this);
        confirmedRoute.setText("I completed the planned transect route");
        confirmedRoute.setTextColor(INK);
        if(route&&requestedComplete)panel.addView(confirmedRoute);
        if(searchArea||method.equals("visual_encounter")||
            method.equals("ves_transect"))panel.addView(area);
        EditText incompleteReason=edit("Reason for ending early (optional)",
            InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        if(!requestedComplete)panel.addView(incompleteReason);
        TextView note=label(requestedComplete?
            "Effort and stations are checked. Unfinished surveys are saved as Incomplete.":
            "Partial effort and observations remain archived as Incomplete.",13,false);
        note.setTextColor(MUTED);panel.addView(note);
        ScrollView reviewScroll=new ScrollView(this);
        reviewScroll.addView(panel);
        AlertDialog dialog=new AlertDialog.Builder(this)
            .setTitle(requestedComplete?"Finish Complete":"End Incomplete")
            .setView(reviewScroll)
            .setNegativeButton("Back",null)
            .setPositiveButton(requestedComplete?"Confirm finish":"Save incomplete",null)
            .create();
        dialog.setOnShowListener(v->dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            .setOnClickListener(w->{
                Double measured=number(length.getText().toString());
                Double searched=number(area.getText().toString());
                if(measured!=null&&measured<=0){length.setError("Enter positive length");return;}
                if(searched!=null&&searched<=0){area.setError("Enter positive area");return;}
                pendingSearchedArea=searched;
                pendingEndReason=requestedComplete?null:incompleteReason.getText().toString().trim();
                plannedRouteCompleted=route&&requestedComplete&&confirmedRoute.isChecked();
                dialog.dismiss();
                finish(requestedComplete,measured);
            }));
        dialog.show();
    }
    private void finish(boolean complete,Double length){
        finish(complete,length,null,null);
    }
    private void finish(boolean complete,Double length,String disposition,String reason){
        if(eventId==null||!active)return;
        if(surveyPaused)endPause();
        boolean excluded="not_counted".equals(disposition);
        long endedAt=System.currentTimeMillis();
        // A recorded target is evaluated against active effort, not transport or pauses.
        long effortTime=Math.max(0,endedAt-startedAt-accumulatedPausedMs);
        if(requiresGenericUnit()&&samplingUnitId!=null)closeUnit(null,null,null,null,null);
        if(method.equals("point_count")&&pointCountId!=null)closePoint();
        if(method.equals("line_transect")||method.equals("ves_transect"))stopTracking();
        TrackService.stopGnss(this);
        // Stationary and station-based search effort excludes travel between visits.
        // Preserve session clock separately. Never substitute the full survey duration
        // for sampling effort in an analytical effort denominator.
        if(method.equals("point_count")||requiresGenericUnit()){
            long visitsMs=0;
            String visitTable=method.equals("point_count")?"point_counts":"sampling_units";
            try(Cursor c=db.getReadableDatabase().rawQuery(
                "SELECT started,ended,COALESCE(unit_paused_ms,0) FROM "+visitTable+
                " WHERE event_id=? AND ended IS NOT NULL",new String[]{eventId})){
                while(c.moveToNext()){
                    long began=parseIso(c.getString(0)),stopped=parseIso(c.getString(1));
                    if(began>0&&stopped>=began)
                        visitsMs+=Math.max(0,stopped-began-c.getLong(2));
                }
            }
            effortTime=visitsMs;
        }
        boolean stationBased=method.equals("point_count")||requiresGenericUnit();
        int visitCount=0,unfinishedVisits=0,missedPlanned=0;
        if(stationBased){
            String table=method.equals("point_count")?"point_counts":"sampling_units";
            try(Cursor c=db.getReadableDatabase().rawQuery(
                "SELECT COUNT(*),COALESCE(SUM(CASE WHEN completed=1 THEN 0 ELSE 1 END),0) "+
                "FROM "+table+" WHERE event_id=?",new String[]{eventId})){
                if(c.moveToFirst()){
                    visitCount=c.getInt(0);
                    unfinishedVisits=c.getInt(1);
                }
            }
            if(method.equals("point_count")&&plannedStationCount>0){
                try(Cursor c=db.getReadableDatabase().rawQuery(
                    "SELECT COUNT(*) FROM survey_stations WHERE event_id=? "+
                    "AND visit_state<>'completed'",new String[]{eventId})){
                    if(c.moveToFirst())missedPlanned=c.getInt(0);
                }
            }
        }
        String outcome=SurveyClosePolicy.resolve(complete,excluded,stationBased,
            visitCount,unfinishedVisits,missedPlanned,
            plannedDurationSeconds,effortTime,
            SurveyClosePolicy.routeLengthRequired(method),plannedRouteCompleted,
            length,SurveyClosePolicy.searchedAreaRequired(method),pendingSearchedArea);
        if(complete&&!excluded){
            StudyProtocols.Rule study=StudyProtocols.snapshot(db.getReadableDatabase(),eventId);
            if(study!=null&&!StudyProtocols.passes(study,visitCount,effortTime,
                studyEvidenceUnits(),"verified_start_fix_under_10m".equals(startGpsStatus),
                StudyProtocols.photoCount(db.getReadableDatabase(),eventId)))
                outcome=SurveyClosePolicy.INCOMPLETE;
        }
        if(complete&&!excluded&&FieldProtocolPolicy.supported(method)){
            int[] data=methodCompleteness();
            if(!FieldProtocolPolicy.canComplete(method,data[0],data[1],data[2]))
                outcome=SurveyClosePolicy.INCOMPLETE;
        }
        if(complete&&!excluded&&TrapProtocolPolicy.supported(method)){
            int[] device=deviceCompleteness();
            if(!TrapProtocolPolicy.canComplete(device[0],device[1],device[2],device[3]))
                outcome=SurveyClosePolicy.INCOMPLETE;
        }
        if(complete&&SurveyClosePolicy.INCOMPLETE.equals(outcome))
            toast("Required effort, stations or device checks unfinished: saved Incomplete");
        String finalReason=reason;
        if(SurveyClosePolicy.INCOMPLETE.equals(outcome)){
            String statusReason="Survey effort, route, or sampling requirements not completed";
            finalReason=pendingEndReason==null||pendingEndReason.trim().isEmpty()?
                statusReason:pendingEndReason.trim()+" | "+statusReason;
        }
        complete=SurveyClosePolicy.COMPLETE.equals(outcome);
        SQLiteDatabase writable=db.getWritableDatabase();
        writable.beginTransaction();
        try{
            writable.execSQL("UPDATE surveys SET ended=?,effort_length_m=?,complete=?,observer_effort_minutes=?,"+
               "searched_area_m2=?,outcome=?,exclusion_reason=?,outcome_changed_at=? WHERE id=?",
               new Object[]{iso(endedAt),length,complete?1:0,effortTime/60000.0,
                   pendingSearchedArea,outcome,excluded?reason:null,iso(endedAt),eventId});
            int records=0;
            try(Cursor c=writable.rawQuery(
                "SELECT COUNT(*) FROM field_records WHERE event_id=? AND deleted_at IS NULL",
                new String[]{eventId})){
                if(c.moveToFirst())records=c.getInt(0);
            }
            writable.execSQL("INSERT INTO survey_outcome_audit(event_id,previous_outcome,new_outcome,changed_at_utc,reason,record_count) VALUES(?,?,?,?,?,?)",
                new Object[]{eventId,"active",outcome,iso(endedAt),finalReason,records});
            writable.setTransactionSuccessful();
        }finally{writable.endTransaction();}
        pendingSearchedArea=null;
        pendingEndReason=null;
        getSharedPreferences("field_drafts",MODE_PRIVATE).edit().remove("draft_"+eventId).commit();
        active=false;eventId=null;clearMark();
        getSharedPreferences("field_resume",MODE_PRIVATE).edit().putString("screen","home").commit();
        toast(excluded?"Survey archived · NOT COUNTED":complete?"Survey completed":"Survey saved as incomplete");
        navigateHome();
    }

    private boolean requiresGenericUnit(){
        return SurveyMethods.structured(method) &&
            !method.equals("point_count")&&!method.equals("line_transect")&&!method.equals("ves_transect");
    }
    private boolean hasStationUnits(){
        return method.equals("point_count")||requiresGenericUnit();
    }
    private int[] visitProgress(){
        if(eventId==null)return new int[]{0,0,0};
        String table=method.equals("point_count")?"point_counts":"sampling_units";
        try(Cursor c=db.getReadableDatabase().rawQuery(
            "SELECT COUNT(*),SUM(CASE WHEN completed=1 THEN 1 ELSE 0 END),"+
            "COUNT(DISTINCT station_id) FROM "+table+" WHERE event_id=?",new String[]{eventId})){
            if(c.moveToFirst())return new int[]{c.getInt(0),c.getInt(1),c.getInt(2)};
        }catch(Exception ignored){}
        return new int[]{0,0,0};
    }
    private void startUnit(String station,Location stationFix){
        int seq=1;
        try(Cursor c=db.getReadableDatabase().rawQuery(
            "SELECT COALESCE(MAX(sequence_number),0)+1 FROM sampling_units WHERE event_id=?",new String[]{eventId})){
            if(c.moveToFirst())seq=c.getInt(0);
        }
        unitSequence=seq;
        samplingUnitId="UNIT-"+eventId+"-"+unitSequence;
        unitStartedAt=System.currentTimeMillis();
        stationId=station;nearEndNotified=false;pointNotice=false;
        boolean gpsValid=verifiedStationFix(stationFix);
        Object lat=gpsValid?stationFix.getLatitude():null,lon=gpsValid?stationFix.getLongitude():null,
            accuracy=gpsValid?stationFix.getAccuracy():null;
        db.getWritableDatabase().execSQL(
            "INSERT INTO sampling_units(id,event_id,station_id,sequence_number,method,started,target_seconds,"+
            "gps_latitude,gps_longitude,gps_accuracy_m,gps_recorded_at,gps_status) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)",
            new Object[]{samplingUnitId,eventId,station,unitSequence,method,iso(unitStartedAt),
                plannedDurationSeconds>0?plannedDurationSeconds:null,lat,lon,accuracy,
                gpsValid?iso(stationFix.getTime()):null,
                gpsValid?"verified_station_fix_under_10m":"missing_or_weak_gps_at_start"});
        StationEnvironmentStore.recordStationFix(db.getWritableDatabase(),
            eventId,samplingUnitId,station,stationFix);
        if(gpsValid)db.getWritableDatabase().execSQL(
            "UPDATE sites SET latitude=?,longitude=?,coordinate_uncertainty_m=?,updated_at=? "+
            "WHERE id=? AND latitude IS NULL AND longitude IS NULL",
            new Object[]{lat,lon,accuracy,iso(unitStartedAt),station});
        else toast("Sampling unit started without reliable GPS · flagged");
        if(pointNextButton!=null)pointNextButton.setText("Finish visit · Next station");
        if(saveButton!=null)saveButton.setEnabled(active);
    }
    private void closeUnit(Double area,Double length,String equipment,Integer devices,Double hours){
        if(samplingUnitId==null)return;
        long time=System.currentTimeMillis();
        long effectiveUnitMs=unitElapsedActiveMs(unitStartedAt,"sampling_units",samplingUnitId,time);
        double minutes=effectiveUnitMs/60000.0;
        int completed=plannedDurationSeconds<=0||effectiveUnitMs>=plannedDurationSeconds*1000L?1:0;
        db.getWritableDatabase().execSQL(
            "UPDATE sampling_units SET ended=?,completed=?,effort_minutes=?,effort_area_m2=?,"+
            "effort_length_m=?,equipment_id=?,number_devices=?,trap_effort_hours=? WHERE id=?",
            new Object[]{iso(time),completed,minutes,area,length,equipment,devices,hours,samplingUnitId});
        samplingUnitId=null;
        if(pointNextButton!=null)pointNextButton.setText("Start next station visit");
        if(saveButton!=null)saveButton.setEnabled(false);
    }
    private void nextSamplingUnit(){
        if(!active||!requiresGenericUnit())return;
        if(surveyPaused){toast("Resume effort to change stations");return;}
        if(!taxonEntry.getText().toString().trim().isEmpty()){
            new AlertDialog.Builder(this).setTitle("Unsaved observation")
                .setMessage("Save the current detection before ending this visit.")
                .setNegativeButton("Keep recording",null)
                .setPositiveButton("Clear entry",(d,w)->{
                    taxonEntry.setText("");distanceEntry.setText("");clearMark();genericUnitPicker();
                }).show();
            return;
        }
        genericUnitPicker();
    }
    /** One-off station generated on site for non-point standardized methods. */
    private String createFieldStation(Location stationFix){
        String id=RoutePlan.newSiteId();
        long now=System.currentTimeMillis();
        boolean ok=verifiedStationFix(stationFix);
        db.getWritableDatabase().execSQL(
            "INSERT INTO sites(id,project_id,name,latitude,longitude,coordinate_uncertainty_m,notes,created_at,updated_at) "+
            "VALUES(?,?,?,?,?,?,?,?,?)",
            new Object[]{id,projectId,"Field station "+(unitSequence+1),
                ok?stationFix.getLatitude():null,ok?stationFix.getLongitude():null,
                ok?stationFix.getAccuracy():null,
                "Auto-created during "+profile+" survey "+eventId+
                (ok?" · GPS captured":" · GPS missing at creation"),
                iso(now),iso(now)});
        return id;
    }
    private void genericUnitPicker(){
        String[] existing=stationSuggestions();
        String[] list=new String[existing.length+1];
        list[0]="＋ Generate station at my current site";
        System.arraycopy(existing,0,list,1,existing.length);
        LinearLayout panel=vertical();panel.setPadding(dp(16),0,dp(16),0);
        TextView title=label("Generate a station for this visit, or select an existing station for repeat monitoring.",13,true);panel.addView(title);
        Spinner stations=new Spinner(this);
        stations.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,list));
        panel.addView(stations);
        EditText area=edit("Verified area searched m² (optional)",InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        EditText length=edit("Verified route/search length m (optional)",InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        EditText equipment=edit("Trap/camera/recorder ID (optional)",InputType.TYPE_CLASS_TEXT);
        EditText devices=edit("Number of devices (optional)",InputType.TYPE_CLASS_NUMBER);
        EditText hours=edit("Verified device-hours (optional)",InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        EditText cameraDays=edit("Verified camera-days (sum of operational camera days)",
            InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        EditText trapNights=edit("Verified trap-nights (sum across functioning traps)",
            InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        EditText recorderHours=edit("Verified acoustic recorder-hours",
            InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        EditText downtime=edit("Known device downtime (hours)",
            InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        EditText effortNotes=edit("How effort was verified, outages, notes",InputType.TYPE_CLASS_TEXT);
        EditText noise=edit("Noise code 0–4 (optional)",InputType.TYPE_CLASS_NUMBER);
        EditText wind=edit("Wind code 0–6 (optional)",InputType.TYPE_CLASS_NUMBER);
        EditText rain=edit("Rain / sky conditions (optional)",InputType.TYPE_CLASS_TEXT);
        if(samplingUnitId!=null){
            if(method.equals("visual_encounter")||method.equals("area_search")||method.equals("track_sign"))
                panel.addView(area);
            if(method.equals("spotlight")||method.equals("track_sign")||method.equals("area_search"))
                panel.addView(length);
            if(method.equals("coverboard_search")||method.equals("drift_fence_check")||
               method.equals("camera_trap_check")||method.equals("live_trap_check")||method.equals("bat_acoustic")){
                panel.addView(equipment);panel.addView(devices);panel.addView(hours);
                if(method.equals("camera_trap_check"))panel.addView(cameraDays);
                if(method.equals("drift_fence_check")||method.equals("live_trap_check"))panel.addView(trapNights);
                if(method.equals("bat_acoustic"))panel.addView(recorderHours);
                panel.addView(downtime);panel.addView(effortNotes);
            }
            if(profile.equals("Herpetofauna")){
                panel.addView(label("Conditions at this visit",13,true));
                panel.addView(noise);panel.addView(wind);panel.addView(rain);
            }
        }
        ScrollView sc=new ScrollView(this);sc.addView(panel);
        AlertDialog dialog=new AlertDialog.Builder(this)
            .setTitle(samplingUnitId==null?"Start sampling visit":"Finish visit · next station")
            .setView(sc).setNegativeButton("Cancel",null)
            .setNeutralButton("Close visit only",null)
            .setPositiveButton("Start next station",null).create();
        dialog.setOnShowListener(v->{
            android.view.View.OnClickListener close=w->{
                if(!validateUnitEffort(area,length,devices,hours))return;
                Integer nv=integerOrNull(noise.getText().toString());
                Integer windVal=integerOrNull(wind.getText().toString());
                if(nv!=null&&(nv<0||nv>4)||windVal!=null&&(windVal<0||windVal>6)){
                    toast("Noise code 0–4; wind code 0–6");return;
                }
                Double cd=number(cameraDays.getText().toString());
                Double tn=number(trapNights.getText().toString());
                Double rh=number(recorderHours.getText().toString());
                Double down=number(downtime.getText().toString());
                if((cd!=null&&cd<0)||(tn!=null&&tn<0)||(rh!=null&&rh<0)||(down!=null&&down<0)){
                    toast("Verified effort and downtime cannot be negative");return;
                }
                if(samplingUnitId!=null){
                    String finished=samplingUnitId;
                    closeUnit(number(area.getText().toString()),number(length.getText().toString()),
                        equipment.getText().toString(),integerOrNull(devices.getText().toString()),number(hours.getText().toString()));
                    db.getWritableDatabase().execSQL(
                        "UPDATE sampling_units SET noise_score=?,wind_score=?,rain_condition=?,"+
                        "camera_days_verified=?,trap_nights_verified=?,recorder_hours_verified=?,"+
                        "downtime_hours=?,effort_remarks=? WHERE id=?",
                        new Object[]{nv,windVal,rain.getText().toString().trim(),
                          cd,tn,rh,down,effortNotes.getText().toString().trim(),finished});
                }
                if(w==dialog.getButton(AlertDialog.BUTTON_POSITIVE)){
                    int selected=stations.getSelectedItemPosition();
                    boolean generate=selected==0;
                    String chosen=generate?"":existing[selected-1];
                    launchStationGps("generic",chosen,generate,generate?"New field station":chosen);
                }
                persistDraft();status();dialog.dismiss();
            };
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(close);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(close);
        });
        dialog.show();
    }
    private static Integer integerOrNull(String text){
        try{return text.trim().isEmpty()?null:Integer.parseInt(text.trim());}catch(Exception e){return null;}
    }
    private boolean validateUnitEffort(EditText area,EditText length,EditText devices,EditText hours){
        Double a=number(area.getText().toString()),l=number(length.getText().toString()),
            h=number(hours.getText().toString());
        Integer n=integerOrNull(devices.getText().toString());
        if(a!=null&&a<=0||l!=null&&l<=0||h!=null&&h<0||n!=null&&n<=0){
            toast("Effort and device counts must be positive");return false;
        }
        if(!area.getText().toString().trim().isEmpty()&&a==null||
           !length.getText().toString().trim().isEmpty()&&l==null||
           !hours.getText().toString().trim().isEmpty()&&h==null||
           !devices.getText().toString().trim().isEmpty()&&n==null){
            toast("Check the numeric survey effort entries");return false;
        }
        return true;
    }
    private String[] stationSuggestions(){
        java.util.ArrayList<String> stations=new java.util.ArrayList<>();
        String table=method.equals("point_count")?"point_counts":"sampling_units";
        String sql="SELECT s.id,COUNT(u.id) AS seen FROM sites s LEFT JOIN "+table+
            " u ON u.station_id=s.id AND u.event_id=? WHERE s.project_id=? GROUP BY s.id ORDER BY seen ASC,s.id";
        try(Cursor c=db.getReadableDatabase().rawQuery(sql,new String[]{eventId,projectId})){
            while(c.moveToNext())stations.add(c.getString(0));
        }catch(Exception ignored){}
        return stations.toArray(new String[0]);
    }
    /**
     * Planned bird surveys visit each generated station exactly once. Distances
     * are chainages, never interpolated coordinates. Fresh GPS is captured here.
     */
    private void plannedPointAction(){
        if(!active||!method.equals("point_count")||eventId==null)return;
        if(pointCountId!=null){
            new AlertDialog.Builder(this).setTitle("Finish point "+pointSequence+"?")
                .setMessage("Closes this stationary count and records its completion status. "+
                    "Travel to the next point, then tap Start point there. The total session timer keeps running.")
                .setNegativeButton("Continue this count",null)
                .setPositiveButton("Finish this point",(d,w)->{
                    closePoint();persistDraft();status();refreshPlannedPointButton();
                }).show();
            return;
        }
        RoutePlan.Next next=RoutePlan.next(db.getReadableDatabase(),eventId);
        if(next==null){toast("All planned points have been visited");refreshPlannedPointButton();return;}
        if(surveyPaused){toast("Resume effort before starting the point count");return;}
        launchStationGps("planned",next.stationId,false,
            "Point "+next.sequence+" · "+Math.round(next.chainage)+" m");
    }
    private void beginPlannedPoint(RoutePlan.Next next,Location fresh){
        if(!active||pointCountId!=null||!method.equals("point_count"))return;
        if(fresh!=null&&!verifiedStationFix(fresh)){toast("GPS expired. Reacquire at the station.");return;}
        if(surveyPaused){toast("Resume effort first");return;}
        RoutePlan.Next expected=RoutePlan.next(db.getReadableDatabase(),eventId);
        if(expected==null||!expected.stationId.equals(next.stationId)){
            toast("Station plan has changed. Reopen Start point.");return;
        }
        long now=System.currentTimeMillis();
        int newSequence=next.sequence;
        String id="PC-"+eventId+"-"+newSequence;
        RoutePlan.start(db.getWritableDatabase(),eventId,next,id,pointTargetSeconds,now,fresh);
        StationEnvironmentStore.recordStationFix(db.getWritableDatabase(),
            eventId,id,next.stationId,fresh);
        stationId=next.stationId;pointSequence=newSequence;pointCountId=id;pointStartedAt=now;
        pointNotice=false;nearEndNotified=false;clearMark();
        if(saveButton!=null)saveButton.setEnabled(true);
        refreshPlannedPointButton();status();persistDraft();
        toast(fresh==null?"Point "+pointSequence+" started · GPS missing":"Point "+
            pointSequence+" GPS saved · ±"+Math.round(fresh.getAccuracy())+" m");
    }
    private void refreshPlannedPointButton(){
        if(pointNextButton==null||!method.equals("point_count")||plannedStationCount<=0)return;
        if(pointCountId!=null){
            pointNextButton.setText("Finish point "+pointSequence+" · then travel");
            pointNextButton.setEnabled(true);
        }else{
            RoutePlan.Next next=eventId==null?null:RoutePlan.next(db.getReadableDatabase(),eventId);
            if(next==null){pointNextButton.setText("All planned points visited");pointNextButton.setEnabled(false);}
            else{
                pointNextButton.setText("Start point "+next.sequence+" · "+Math.round(next.chainage)+" m");
                pointNextButton.setEnabled(true);
            }
        }
    }
    private void startPoint(String station,Location fresh){
        SQLiteDatabase d=db.getWritableDatabase();
        int sequence=1;
        try(Cursor c=d.rawQuery("SELECT COALESCE(MAX(sequence_number),0)+1 FROM point_counts WHERE event_id=?",new String[]{eventId})){
            if(c.moveToFirst())sequence=c.getInt(0);
        }
        pointSequence=Math.max(1,sequence);
        pointCountId="PC-"+eventId+"-"+pointSequence;
        stationId=station;pointStartedAt=System.currentTimeMillis();
        pointNotice=false;nearEndNotified=false;
        boolean good=verifiedStationFix(fresh);
        Object lat=good?fresh.getLatitude():null,lon=good?fresh.getLongitude():null,
            accuracy=good?fresh.getAccuracy():null;
        d.execSQL("INSERT INTO point_counts(id,event_id,station_id,sequence_number,started,target_seconds,"+
            "gps_latitude,gps_longitude,gps_accuracy_m,gps_recorded_at,gps_status) VALUES(?,?,?,?,?,?,?,?,?,?,?)",
            new Object[]{pointCountId,eventId,stationId,pointSequence,iso(pointStartedAt),pointTargetSeconds,
                lat,lon,accuracy,good?iso(fresh.getTime()):null,
                good?"verified_station_fix_under_10m":"missing_or_weak_gps_at_start"});
        StationEnvironmentStore.recordStationFix(d,eventId,pointCountId,station,fresh);
        if(good)d.execSQL("UPDATE sites SET latitude=?,longitude=?,coordinate_uncertainty_m=?,updated_at=? "+
            "WHERE id=? AND latitude IS NULL AND longitude IS NULL",
            new Object[]{lat,lon,accuracy,iso(pointStartedAt),station});
        if(pointNextButton!=null)pointNextButton.setText("Finish point · Next station");
        if(saveButton!=null)saveButton.setEnabled(true);
    }
    private void closePoint(){
        if(pointCountId==null)return;
        int complete=unitElapsedActiveMs(pointStartedAt,"point_counts",pointCountId,
            System.currentTimeMillis())>=pointTargetSeconds*1000L?1:0;
        db.getWritableDatabase().execSQL("UPDATE point_counts SET ended=?,completed=? WHERE id=?",
            new Object[]{iso(System.currentTimeMillis()),complete,pointCountId});
        if(plannedStationCount>0&&eventId!=null)
            RoutePlan.close(db.getWritableDatabase(),eventId,stationId,complete==1);
        pointCountId=null;
        if(pointNextButton!=null)pointNextButton.setText("Start next point count");
        if(saveButton!=null)saveButton.setEnabled(false);
    }
    private void nextPoint(){
        if(!active||!method.equals("point_count"))return;
        if(surveyPaused){toast("Resume effort to change stations");return;}
        if(!taxonEntry.getText().toString().trim().isEmpty()){
            new AlertDialog.Builder(this).setTitle("Unsaved observation")
                .setMessage("Save the current bird record before switching stations, or clear the draft.")
                .setPositiveButton("Keep recording",null)
                .setNegativeButton("Clear draft",(d,w)->{
                    taxonEntry.setText("");distanceEntry.setText("");clearMark();
                    if(plannedStationCount>0)plannedPointAction();
                    else pointPicker();
                }).show();
            return;
        }
        if(plannedStationCount>0){plannedPointAction();return;}
        pointPicker();
    }
    private void pointPicker(){
        String[] names=stationSuggestions();
        if(names.length==0){toast("Register sampling stations from Home first");return;}
        final int[] selected={0};
        new AlertDialog.Builder(this).setTitle(pointCountId==null?"Start next point count":"Finish current point, then start next")
            .setMessage("Choose the next station. Previous observations remain linked to their station.")
            .setSingleChoiceItems(names,0,(d,which)->selected[0]=which)
            .setNegativeButton("Cancel",null)
            .setNeutralButton("Close point only",(d,w)->{
                if(pointCountId!=null)closePoint();persistDraft();status();
            })
            .setPositiveButton("Start next station",(d,w)->{
                String next=names[selected[0]];
                if(pointCountId!=null)closePoint();
                launchStationGps("custom",next,false,next);
                persistDraft();status();
            }).show();
    }
    private void startTracking(){
        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            toast("Location permission is needed to record GPS track");return;
        }
        if(eventId==null||TrackService.wasStopped(this))return;
        Intent i=new Intent(this,TrackService.class);
        i.setAction(TrackService.ACTION_START);i.putExtra("event_id",eventId);
        try{startForegroundService(i);}catch(Exception ex){toast("GPS tracking unavailable: "+ex.getMessage());}
    }
    private void stopTracking(){
        try{
            Intent i=new Intent(this,TrackService.class);
            i.setAction(TrackService.ACTION_STOP);startService(i);
        }catch(Exception ignored){}
    }
    private void refreshTrack(){
        if(trackingView==null||eventId==null)return;
        try(Cursor c=db.getReadableDatabase().rawQuery(
            "SELECT COALESCE(gps_track_m,0),COALESCE(gps_track_status,'waiting') FROM surveys WHERE id=?",new String[]{eventId})){
            if(c.moveToFirst())trackingView.setText(String.format(Locale.US,
                "GPS walked path ~%.0f m · planned %.0f m · %s (route estimate only)",
                c.getDouble(0),plannedRouteMeters,c.getString(1)));
        }catch(Exception ignored){}
    }
    /** Freeze actual, fresh GNSS coordinates when a detection is first marked. */
    private Location freshObserverFix(){
        Location fix=TrackService.currentFix();
        if(fix==null)fix=location;
        if(fix==null||!LocationManager.GPS_PROVIDER.equals(fix.getProvider())||!fix.hasAccuracy())return null;
        long age=(SystemClock.elapsedRealtimeNanos()-fix.getElapsedRealtimeNanos())/1000000L;
        return GpsFixPolicy.acceptableElapsed(fix.getLatitude(),fix.getLongitude(),
            fix.getAccuracy(),age,fix.isFromMockProvider())?new Location(fix):null;
    }
    private void markDetection(){
        if(!active){toast("Start a survey first");return;}
        markedAt=System.currentTimeMillis();
        markedObserverFix=freshObserverFix();
        showMarkStatus();persistDraft();
    }
    private void openFieldProtocol(){
        if(!active||eventId==null){toast("Start a survey to record method effort");return;}
        if(requiresGenericUnit()&&samplingUnitId==null){
            toast("Start a sampling station before recording protocol effort");return;
        }
        persistDraft();
        Intent i=new Intent(this,FieldProtocolsActivity.class);
        i.putExtra("event_id",eventId);
        i.putExtra("station_id",stationId);
        startActivity(i);
    }
    private int[] methodCompleteness(){
        int units=0,unchecked=0,invalid=0;
        if(eventId==null||!FieldProtocolPolicy.supported(method))
            return new int[]{0,0,0};
        if(method.equals("coverboard_search")){
            try(Cursor c=db.getReadableDatabase().rawQuery(
                "SELECT COUNT(*),COALESCE(SUM(CASE WHEN NOT EXISTS ("+
                "SELECT 1 FROM coverboard_checks x WHERE x.board_pk=b.id "+
                "AND x.board_condition<>'Missing / not inspected') THEN 1 ELSE 0 END),0) "+
                "FROM coverboard_units b WHERE b.event_id=?",
                new String[]{eventId})){
                if(c.moveToFirst()){units=c.getInt(0);unchecked=c.getInt(1);}
            }
        }else if(method.equals("spotlight")){
            try(Cursor c=db.getReadableDatabase().rawQuery(
                "SELECT COUNT(*),COALESCE(SUM(CASE WHEN beam_width_m<=0 OR "+
                "visibility_m<=0 OR distance_m<=0 OR ended_at_utc<=started_at_utc OR "+
                "TRIM(duplicate_control_notes)='' THEN 1 ELSE 0 END),0) "+
                "FROM spotlight_passes WHERE event_id=?",new String[]{eventId})){
                if(c.moveToFirst()){units=c.getInt(0);invalid=c.getInt(1);}
            }
        }else if(method.equals("area_search")){
            try(Cursor c=db.getReadableDatabase().rawQuery(
                "SELECT COUNT(*),COALESCE(SUM(CASE WHEN measured_area_m2<=0 "+
                "OR ended_at_utc<=started_at_utc OR TRIM(duplicate_control_notes)='' "+
                "THEN 1 ELSE 0 END),0) "+
                "FROM area_search_passes WHERE event_id=?",new String[]{eventId})){
                if(c.moveToFirst()){units=c.getInt(0);invalid=c.getInt(1);}
            }
        }else{
            try(Cursor c=db.getReadableDatabase().rawQuery(
                "SELECT COUNT(*),COALESCE(SUM(CASE WHEN verified_length_m<=0 "+
                "OR ended_at_utc<=started_at_utc OR TRIM(substrate)='' "+
                "THEN 1 ELSE 0 END),0) "+
                "FROM track_sign_efforts WHERE event_id=?",new String[]{eventId})){
                if(c.moveToFirst()){units=c.getInt(0);invalid=c.getInt(1);}
            }
        }
        return new int[]{units,unchecked,invalid};
    }
    private int studyEvidenceUnits(){
        if(FieldProtocolPolicy.supported(method))return methodCompleteness()[0];
        if(TrapProtocolPolicy.supported(method))return deviceCompleteness()[0];
        if(hasStationUnits())return visitProgress()[1];
        return 0;
    }
    private void openTrapProtocol(){
        if(!active||eventId==null){toast("Start survey before device checks");return;}
        persistDraft();
        Intent i=new Intent(this,TrapProtocolActivity.class);
        i.putExtra("event_id",eventId);
        i.putExtra("station_id",stationId);
        startActivity(i);
    }
    private int[] deviceCompleteness(){
        int deployments=0,unchecked=0,open=0,missingCaptures=0;
        try(Cursor c=db.getReadableDatabase().rawQuery(
            "SELECT COUNT(*),COALESCE(SUM(CASE WHEN d.retrieved_at_utc IS NULL THEN 1 ELSE 0 END),0),"+
            "COALESCE(SUM(CASE WHEN NOT EXISTS (SELECT 1 FROM trap_checks x WHERE x.deployment_id=d.id) "+
            "THEN 1 ELSE 0 END),0) FROM trap_deployments d WHERE d.event_id=? AND d.method=?",
            new String[]{eventId,method})){
            if(c.moveToFirst()){
                deployments=c.getInt(0);open=c.getInt(1);unchecked=c.getInt(2);
            }
        }
        if(TrapProtocolPolicy.needsCaptureDetails(method)){
            try(Cursor c=db.getReadableDatabase().rawQuery(
                "SELECT COALESCE(SUM(MAX(0,x.observations-(SELECT COUNT(*) "+
                "FROM trap_captures z WHERE z.check_id=x.id))),0) "+
                "FROM trap_checks x JOIN trap_deployments d ON d.id=x.deployment_id "+
                "WHERE d.event_id=? AND d.method=?",
                new String[]{eventId,method})){
                if(c.moveToFirst())missingCaptures=c.getInt(0);
            }
        }
        return new int[]{deployments,unchecked,open,missingCaptures};
    }
    private void openCallLibrary(){
        if(!active||eventId==null){toast("Start a survey before recording sound");return;}
        persistDraft();
        Intent audio=new Intent(this,CallLibraryActivity.class);
        audio.putExtra("event_id",eventId);
        audio.putExtra("taxon_group",group);
        audio.putExtra("method",method);
        audio.putExtra("station_id",stationId);
        startActivity(audio);
    }
    private void openCamera(){
        if(!active){toast("Start survey first");return;}
        Intent intent=new Intent(this,CameraActivity.class);
        startActivityForResult(intent,CAMERA_REQ);
    }
    private void clearMark(){
        markedAt=0;markedObserverFix=null;markedBearing=null;markedPitch=null;
        if(bearingStatusView!=null)bearingStatusView.setText("");
        if(markRowView!=null)markRowView.setVisibility(View.GONE);
        queueDraft();
    }
    private void showMarkStatus(){
        if(bearingStatusView==null)return;
        if(markRowView!=null)markRowView.setVisibility(markedAt>0?View.VISIBLE:View.GONE);
        if(markedBearing!=null)
            bearingStatusView.setText(String.format(Locale.US,"Bearing %.0f° magnetic · pitch %+.0f°",markedBearing,markedPitch));
        else if(markedAt>0) bearingStatusView.setText("Detection time marked · bearing optional");
        else bearingStatusView.setText("Time and bearing not marked");
    }
    private static Double number(String v){try{return Double.parseDouble(v.trim());}catch(Exception e){return null;}}
    /** Catalog provenance comes from source records, never from display punctuation. */
    private String scientificSource(String taxonGroup,String scientific){
        if(scientific!=null&&!scientific.isEmpty()){
            try(Cursor source=db.getReadableDatabase().rawQuery(
                "SELECT source,source_version FROM taxon_catalog "+
                "WHERE taxon_group=? AND scientific_name=? "+
                "ORDER BY CASE WHEN taxon_key LIKE 'STARTER:%' THEN 1 ELSE 0 END,"+
                "CASE WHEN review_status='reviewed' THEN 0 ELSE 1 END "+
                "LIMIT 1",
                new String[]{taxonGroup,scientific})){
                if(source.moveToFirst())return "catalog:"+source.getString(0)+"|"+source.getString(1);
            }catch(Exception ignored){}
            for(String entry:Taxa.forGroup(taxonGroup)){
                String[] parts=entry.split("\\|",2);
                if(parts.length==2&&scientific.equalsIgnoreCase(parts[1]))
                    return "offline_starter_v0.4";
            }
        }
        return "user_entered";
    }
    private void save(){
        if(!active){toast("Start survey first");return;}
        if(method.equals("point_count")&&pointCountId==null){
            toast("Start a new point count station before recording");return;
        }
        if(requiresGenericUnit()&&samplingUnitId==null){
            toast("Start a sampling station before recording");return;
        }
        if(surveyPaused){toast("Effort is paused · resume before recording detections");return;}
        if(method.equals("amphibian_calls")&&callScoreSpinner.getSelectedItemPosition()==1){
            toast("No calls heard: close the sampling visit without a detection record");return;
        }
        String raw=taxonEntry.getText().toString().trim();
        Double temp=number(tempEntry.getText().toString());
        Double humidity=number(humidityEntry.getText().toString());
        if(profile.equals("Herpetofauna")&&
            (temp!=null&&(temp< -25||temp>65)||humidity!=null&&(humidity<0||humidity>100))){
            toast("Check temperature and humidity values");return;
        }
        if(raw.length()<2){toast("Enter a taxon or unidentified species");return;}
        String[] id=Taxa.identify(raw);
        boolean unidentified=Taxa.isUnknown(raw);
        boolean genusOnly=!unidentified&&id[1]!=null&&
            (id[1].trim().endsWith(" sp.")||id[1].trim().endsWith(" spp."));
        String identificationLevel=unidentified?"unidentified":genusOnly?"genus":
            id[1]==null||id[1].trim().isEmpty()?"unverified_text":"species_name_unverified";
        String geoSupport=geographicRegion.equals("PH")||geographicRegion.equals("OTHER")?
            "not_applicable":GeoRegions.hasPositiveEvidence(group,geographicRegion,id[1])?
            "documented_regional_example":"distribution_not_assessed";
        Double distance=number(distanceEntry.getText().toString());
        if(distance!=null && (distance<0||distance>10000)){toast("Check distance value");return;}
        Double perp=null,line=lineBearing;
        if(SurveyMethods.isBirdLine(profile,method)&&markedBearing!=null&&distance!=null&&line!=null)
            perp=Geometry.perpendicular(distance,markedBearing,line);
        String distanceMethod=group.equals("Birds")?distanceSourceSpinner.getSelectedItem().toString():"Not applicable";
        String firstPosition=group.equals("Birds")?firstPositionSpinner.getSelectedItem().toString():"Not applicable";
        String movement=group.equals("Birds")?movementSpinner.getSelectedItem().toString():"Not applicable";
        long first=markedAt>0?markedAt:System.currentTimeMillis();
        boolean inWindow;
        if(method.equals("point_count"))
            inWindow=pointCountId!=null&&first>=pointStartedAt&&
                unitElapsedActiveMs(pointStartedAt,"point_counts",pointCountId,first)<=pointTargetSeconds*1000L;
        else if(requiresGenericUnit())
            inWindow=samplingUnitId!=null&&first>=unitStartedAt&&
                (plannedDurationSeconds<=0||unitElapsedActiveMs(unitStartedAt,"sampling_units",samplingUnitId,first)<=plannedDurationSeconds*1000L);
        else inWindow=plannedDurationSeconds<=0||
            Math.max(0,first-startedAt-currentPausedMs(first))<=plannedDurationSeconds*1000L;
        Double height=profile.equals("Herpetofauna")?number(herpHeightEntry.getText().toString()):number(perchEntry.getText().toString());
        if(height!=null&&(height<0||height>150)){
            toast("Check height above ground");return;
        }
        String confidence=confidenceSpinner.getSelectedItem().toString();
        if(unidentified)confidence="Unidentified";
        boolean candidate=group.equals("Birds")&&!confidence.equals("Uncertain")&&!confidence.equals("Unidentified")&&!unidentified&&!genusOnly&&
            !id[1].isEmpty()&&distance!=null&&!distanceMethod.equals("Unknown")
          &&firstPosition.equals("Yes")&&inWindow&&(method.equals("point_count")||method.equals("line_transect"))
          &&(!method.equals("line_transect")||perp!=null)
          &&(!method.equals("point_count")||pointRadiusM==null||distance<=pointRadiusM)
          &&!movement.equals("Flyover")&&!movement.equals("Entering after start");
        Location loc=markedAt>0?markedObserverFix:freshObserverFix();
        Object lat=loc==null?null:loc.getLatitude(),lon=loc==null?null:loc.getLongitude(),acc=loc==null?null:loc.getAccuracy();
        db.getWritableDatabase().execSQL(
            "INSERT INTO field_records(event_id,detected_at,taxon_group,common_name,scientific_name,raw_identification,individual_count,detection_method,horizontal_m,bearing_magnetic,pitch_deg,perpendicular_m,observer_lat,observer_lon,gps_accuracy_m,habitat,behavior,notes,compass_quality,name_source,distance_method,initial_position,movement_status,distance_eligible,updated_at,point_count_id,protocol_window_flag,air_temp_c,humidity_pct,substrate,weather_notes,amphibian_call_score,microhabitat,sampling_unit_id,life_stage,evidence_type,perch_height_m,call_noise_score,unit_time_status,equipment_id,count_basis,identification_level,regional_evidence_status,geo_region_at_detection,identification_confidence,identification_review_note) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
            new Object[]{eventId,iso(first),group,id[0],id[1],raw,count,detectionSpinner.getSelectedItem().toString(),
                distance,markedBearing,markedPitch,perp,lat,lon,acc,
                habitatEntry.getText().toString().trim(),behaviorEntry.getText().toString().trim(),
                notesEntry.getText().toString().trim(),sensorQuality,scientificSource(group,id[1]),
                distanceMethod,firstPosition,movement,candidate?1:0,iso(System.currentTimeMillis()),
                method.equals("point_count")?pointCountId:null,inWindow?"within":"outside",
                profile.equals("Herpetofauna")?temp:null,profile.equals("Herpetofauna")?humidity:null,
                profile.equals("Herpetofauna")?substrateSpinner.getSelectedItem().toString():null,
                weatherEntry.getText().toString().trim(),
                profile.equals("Herpetofauna")&&group.equals("Amphibians")&&callScoreSpinner.getSelectedItemPosition()>0?callScoreSpinner.getSelectedItemPosition()-1:null,
                microhabitatEntry.getText().toString().trim(),samplingUnitId,
                profile.equals("Herpetofauna")?lifeStageSpinner.getSelectedItem().toString():null,
                profile.equals("Mammals")?evidenceSpinner.getSelectedItem().toString():null,
                height,
                method.equals("amphibian_calls")&&noiseSpinner.getSelectedItemPosition()>0?noiseSpinner.getSelectedItemPosition()-1:null,
                inWindow?"within":"overtime",
                profile.equals("Mammals")?deviceEntry.getText().toString().trim():null,
                method.equals("track_sign")?"sign_event":
                    method.equals("camera_trap_check")?"image_detection":
                    method.equals("bat_acoustic")?"acoustic_detection":
                    method.equals("live_trap_check")?"capture":
                    method.equals("coverboard_search")||method.equals("drift_fence_check")?"encounter":
                    "individuals",
                identificationLevel,geoSupport,geographicRegion,
                confidence,identificationNote.getText().toString().trim()});
        if(pendingPhoto!=null){
            try(Cursor c=db.getReadableDatabase().rawQuery("SELECT last_insert_rowid()",null)){
                if(c.moveToFirst()){
                    long recordId=c.getLong(0);
                    PhotoEvidence.attach(this,db.getWritableDatabase(),recordId,pendingPhoto);
                    pendingPhoto=null;showPendingPhoto();
                }
            }catch(Exception photoError){
                toast("Observation saved, but photo link needs review: "+photoError.getMessage());
            }
        }
        taxonEntry.setText("");count=1;countView.setText("1");distanceEntry.setText("");
        confidenceSpinner.setSelection(0);identificationNote.setText("");
        habitatEntry.setText("");behaviorEntry.setText("");notesEntry.setText("");
        // Retain environmental measurements through each visit. Reset identification-specific evidence.
        if(evidenceSpinner!=null)evidenceSpinner.setSelection(0);
        if(lifeStageSpinner!=null)lifeStageSpinner.setSelection(0);
        clearMark();
        distanceSourceSpinner.setSelection(0);firstPositionSpinner.setSelection(0);movementSpinner.setSelection(0);
        refreshAutocomplete();
        persistDraft();
        toast(distance==null?"Observation saved · distance not recorded":"Saved offline");
    }
    private static String timeFmt(long sec){
        long seconds=Math.max(0,sec);
        return String.format(Locale.US,"%02d:%02d:%02d",seconds/3600,seconds/60%60,seconds%60);
    }
    private void status(){
        if(eventView==null||sessionClockView==null)return;
        refreshPauseControls();
        if(!active){
            sessionClockView.setText("Survey not started");
            eventView.setText("No active sampling unit");
            progressView.setText("");
            progressView.setVisibility(View.GONE);
            return;
        }
        long now=System.currentTimeMillis();
        long wallElapsed=Math.max(0,now-startedAt);
        long activeElapsed=Math.max(0,wallElapsed-currentPausedMs(now));
        sessionClockView.setText("Session "+timeFmt(wallElapsed/1000)+
            "   ·   Active "+timeFmt(activeElapsed/1000));
        long unitElapsed=-1,target=0;
        String label="SURVEY";
        if(method.equals("point_count")){
            if(pointCountId!=null){
                unitElapsed=unitElapsedActiveMs(pointStartedAt,"point_counts",pointCountId,now)/1000;
                target=pointTargetSeconds;
                label="POINT "+pointSequence+" · "+stationId;
            }else label="Point closed · select next station";
        }else if(requiresGenericUnit()){
            if(samplingUnitId!=null){
                unitElapsed=unitElapsedActiveMs(unitStartedAt,"sampling_units",samplingUnitId,now)/1000;
                target=plannedDurationSeconds;
                label="VISIT "+unitSequence+" · "+stationId;
            }else label="Visit closed · select next station";
        }else{
            unitElapsed=activeElapsed/1000;
            target=plannedDurationSeconds;
            label=method.replace('_',' ').toUpperCase(Locale.US);
        }
        long left=unitElapsed<0||target<=0?Long.MAX_VALUE:target-unitElapsed;
        if(unitElapsed<0)eventView.setText(label);
        else if(target<=0)eventView.setText(label+" · ELAPSED "+timeFmt(unitElapsed));
        else if(left>0)eventView.setText(label+" · "+timeFmt(left)+" left");
        else eventView.setText(label+" · OVERTIME +"+timeFmt(-left));
        boolean near=left!=Long.MAX_VALUE&&left>0&&left<=30;
        boolean overdue=left!=Long.MAX_VALUE&&left<=0;
        eventView.setTextColor(surveyPaused?UiKit.AMBER:
            near||overdue?Color.rgb(185,39,39):MUTED);
        eventView.setAlpha(!surveyPaused&&near?(now/650%2==0?1f:0.45f):1f);
        if(!surveyPaused&&near&&!nearEndNotified){
            nearEndNotified=true;toast("30 seconds remaining");vibrateIfEnabled(160);
        }
        if(!surveyPaused&&overdue&&!pointNotice){
            pointNotice=true;toast("Sampling interval ended · recording continues in overtime");
            vibrateIfEnabled(300);
        }
        // Timer rendering stays on UI. SQLite progress and GNSS path refresh
        // are serialized in the background; the one-second HUD never reads them.
        if(lastProgressFetchMs==0||now-lastProgressFetchMs>=6000L){
            lastProgressFetchMs=now;
            hudReader.request(eventId,method,plannedStationCount,plannedRouteMeters,
                hasStationUnits(),result->{
                    if(!active||!result.eventId.equals(eventId))return;
                    if(progressView!=null){
                        progressView.setText(result.progress);
                        progressView.setVisibility(result.progress.isEmpty()?View.GONE:View.VISIBLE);
                    }
                    if(trackingView!=null&&!result.track.isEmpty())
                        trackingView.setText(result.track);
                });
        }
    }
    private void exportZip(){
        Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("application/zip");i.putExtra(Intent.EXTRA_TITLE,"wildlife-survey-export.zip");
        startActivityForResult(i,EXPORT_DOC);
    }
    private void launchStationGps(String kind,String id,boolean generate,String label){
        if(!active||surveyPaused){toast("Resume the survey before starting the station");return;}
        // Reuse the continuous foreground receiver. A qualifying fix starts this
        // station immediately without a second acquisition page or cold start.
        Location current=TrackService.currentFix();
        if(verifiedStationFix(current)){
            Intent accepted=new Intent();
            accepted.putExtra("station_kind",kind);
            accepted.putExtra("station_id",id);
            accepted.putExtra("station_generate",generate);
            accepted.putExtra("station_event_id",eventId);
            accepted.putExtra("station_gps_missing",false);
            accepted.putExtra("station_gps_location",current);
            stationGpsResult(accepted);
            return;
        }
        Intent intent=new Intent(this,SurveyGpsActivity.class);
        intent.putExtra("station_gps_mode",true);
        intent.putExtra("station_kind",kind);
        intent.putExtra("station_id",id);
        intent.putExtra("station_generate",generate);
        intent.putExtra("station_label",label);
        intent.putExtra("station_event_id",eventId);
        startActivityForResult(intent,STATION_GPS_REQ);
    }
    private boolean verifiedStationFix(Location fix){
        if(fix==null||!LocationManager.GPS_PROVIDER.equals(fix.getProvider())||!fix.hasAccuracy())return false;
        long ageMs=(SystemClock.elapsedRealtimeNanos()-fix.getElapsedRealtimeNanos())/1000000L;
        return GpsFixPolicy.acceptableElapsed(fix.getLatitude(),fix.getLongitude(),
            fix.getAccuracy(),ageMs,fix.isFromMockProvider());
    }
    private void stationGpsResult(Intent data){
        if(!active||surveyPaused||eventId==null)return;
        if(!eventId.equals(data.getStringExtra("station_event_id"))){toast("Station belongs to a different survey");return;}
        String kind=data.getStringExtra("station_kind"),id=data.getStringExtra("station_id");
        boolean generate=data.getBooleanExtra("station_generate",false);
        boolean missing=data.getBooleanExtra("station_gps_missing",false);
        Location fix=data.getParcelableExtra("station_gps_location");
        if(missing)fix=null;
        else if(!verifiedStationFix(fix)){toast("GPS fix expired. Reacquire station position.");return;}
        if("planned".equals(kind)){
            if(!method.equals("point_count")||pointCountId!=null)return;
            RoutePlan.Next next=RoutePlan.next(db.getReadableDatabase(),eventId);
            if(next==null||!next.stationId.equals(id)){toast("Station order changed");return;}
            beginPlannedPoint(next,fix);
        }else if("custom".equals(kind)){
            if(!method.equals("point_count")||plannedStationCount>0||id==null)return;
            startPoint(id,fix);
            taxonEntry.setText("");distanceEntry.setText("");clearMark();
            persistDraft();status();
        }else if("generic".equals(kind)){
            if(!requiresGenericUnit()||samplingUnitId!=null)return;
            String station=generate?createFieldStation(fix):id;
            if(station==null||station.isEmpty()){toast("Station ID missing");return;}
            startUnit(station,fix);
            taxonEntry.setText("");distanceEntry.setText("");clearMark();
            persistDraft();status();
        }
    }
    @Override protected void onActivityResult(int req,int result,Intent data){
        super.onActivityResult(req,result,data);
        if(req==IDENTIFY_SPECIES_REQ){
            if(result==RESULT_OK&&data!=null&&taxonEntry!=null){
                String label=data.getStringExtra("species_label");
                String selectedGroup=data.getStringExtra("species_group");
                if(label!=null&&!label.trim().isEmpty()){
                    if(profile.equals("Herpetofauna")&&selectedGroup!=null&&
                        (selectedGroup.equals("Amphibians")||selectedGroup.equals("Reptiles"))&&
                        !method.equals("amphibian_calls")){
                        group=selectedGroup;
                        showGroups();
                        updateTaxaSuggestions(false);
                    }
                    taxonEntry.setText(label);
                    taxonEntry.setSelection(taxonEntry.length());
                    refreshFavoriteIcon();
                    queueDraft();
                }
            }
            return;
        }
        if(req==STATION_GPS_REQ){
            if(result==RESULT_OK&&data!=null)stationGpsResult(data);
            return;
        }
        if(req==PHOTO_PICK){
            if(result==RESULT_OK&&data!=null&&data.getData()!=null){
                try{
                    pendingPhoto=PhotoEvidence.addPending(this,data.getData());
                    showPendingPhoto();persistDraft();
                }catch(Exception ex){toast("Photo selection failed: "+ex.getMessage());}
            }
            return;
        }
        if(req==CAMERA_REQ){
            if(result==RESULT_OK && data!=null){
                float az=data.getFloatExtra("bearing_deg",Float.NaN);
                float inclination=data.getFloatExtra("pitch_deg",Float.NaN);
                if(markedAt==0){
                    markedAt=data.getLongExtra("captured_at",System.currentTimeMillis());
                    markedObserverFix=data.getParcelableExtra("capture_observer_fix");
                }
                markedBearing=Float.isNaN(az)?null:(double)az;
                markedPitch=Float.isNaN(inclination)?null:(double)inclination;
                sensorQuality=data.getIntExtra("sensor_quality",-1);
                showMarkStatus();persistDraft();
            }
            return;
        }
        if(req!=EXPORT_DOC||result!=RESULT_OK||data==null)return;
        final Uri exportUri=data.getData();
        if(exportUri==null)return;
        if(exportInProgress){toast("An export is already running");return;}
        exportInProgress=true;
        toast("Preparing survey ZIP");
        new Thread(()->{
            try(FieldDb exportDb=new FieldDb(getApplicationContext());
                OutputStream os=getContentResolver().openOutputStream(exportUri);
                ZipOutputStream z=new ZipOutputStream(os)){
            csv(exportDb,z,"projects.csv",
              "SELECT id,name,description,created_at FROM projects",
              "project_id,name,description,created_at");
            csv(exportDb,z,"sites.csv",
              "SELECT id,project_id,name,habitat,latitude,longitude,coordinate_uncertainty_m,notes,created_at,updated_at FROM sites",
              "station_id,project_id,name,habitat,latitude,longitude,coordinate_uncertainty_m,notes,created_at,updated_at");
            csv(exportDb,z,"surveys.csv",
              "SELECT id,method,started,ended,transect_az,taxon_group,point_target_s,effort_length_m,complete,survey_profile,station_id,observer_name,protocol_status,project_id,region_label,area_km2,point_radius_m,weather,planned_duration_s,gps_track_m,gps_track_status,searched_area_m2,observer_effort_minutes,outcome,exclusion_reason,outcome_changed_at,effort_paused,total_paused_ms,paused_at_utc,geographic_region_id,geographic_selection_method,regional_taxa_version,admin_province_name,admin_province_psgc,admin_boundary_version,admin_boundary_status,planned_route_m,planned_station_spacing_m,planned_station_count,route_design,route_template_id,start_gps_latitude,start_gps_longitude,start_gps_accuracy_m,start_gps_recorded_at,start_gps_status FROM surveys",
              "event_id,method,start_utc,end_utc,transect_magnetic_bearing_deg,initial_taxon_group,point_target_seconds,actual_route_length_m,completed,survey_profile,station_id,observer_name,protocol_status,project_id,region_label,stratum_area_km2,point_radius_m,weather,planned_duration_s,gps_path_m_estimate,gps_track_status,searched_area_m2,observer_effort_minutes,outcome,exclusion_reason,outcome_changed_at,effort_paused,total_paused_ms,paused_at_utc,geographic_region_id,geographic_selection_method,regional_taxa_version,admin_province_name,admin_province_psgc,admin_boundary_version,admin_boundary_status,planned_route_m,planned_station_spacing_m,planned_station_count,route_design,route_template_id,start_gps_latitude,start_gps_longitude,start_gps_accuracy_m,start_gps_recorded_at_utc,start_gps_status");
            csv(exportDb,z,"observations.csv",
              "SELECT r.id,r.event_id,r.detected_at,r.taxon_group,r.common_name,r.scientific_name,r.raw_identification,r.individual_count,r.detection_method,r.horizontal_m,r.bearing_magnetic,r.pitch_deg,r.perpendicular_m,r.observer_lat,r.observer_lon,r.gps_accuracy_m,r.habitat,r.behavior,r.notes,r.compass_quality,r.name_source,r.distance_method,r.initial_position,r.movement_status,r.distance_eligible,r.updated_at,r.deleted_at,r.legacy_source_id,r.point_count_id,r.protocol_window_flag,r.air_temp_c,r.humidity_pct,r.substrate,r.weather_notes,r.amphibian_call_score,r.microhabitat,r.sampling_unit_id,r.life_stage,r.evidence_type,r.perch_height_m,r.call_noise_score,r.unit_time_status,r.equipment_id,r.count_basis,COALESCE(s.outcome,'active'),r.identification_level,r.regional_evidence_status,r.geo_region_at_detection,r.identification_confidence,r.identification_review_note FROM field_records r LEFT JOIN surveys s ON s.id=r.event_id WHERE r.deleted_at IS NULL",
              "record_id,event_id,detected_at_utc,taxon_group,common_name,scientific_name,raw_identification,individual_count,detection_method,horizontal_distance_m,magnetic_bearing_deg,pitch_deg,perpendicular_distance_m,observer_lat,observer_lon,gps_accuracy_m,habitat,behavior,notes,compass_quality,name_source,distance_method,initial_position,movement_status,distance_candidate_flag,updated_at,deleted_at,legacy_source_id,point_count_id,protocol_window_flag,air_temp_c,humidity_pct,substrate,weather_notes,amphibian_call_score,microhabitat,sampling_unit_id,life_stage,evidence_type,perch_height_m,call_noise_score,unit_time_status,equipment_id,count_basis,survey_outcome,identification_level,regional_evidence_status,geographic_region_at_detection,identification_confidence,identification_review_note");
            csv(exportDb,z,"sampling_units.csv",
              "SELECT u.id,u.event_id,u.station_id,u.sequence_number,u.method,u.started,u.ended,u.target_seconds,u.completed,u.effort_minutes,u.effort_area_m2,u.effort_length_m,u.noise_score,u.wind_score,u.rain_condition,u.equipment_id,u.number_devices,u.trap_effort_hours,u.unit_paused_ms,u.camera_days_verified,u.trap_nights_verified,u.recorder_hours_verified,u.deployment_started_utc,u.deployment_ended_utc,u.downtime_hours,u.effort_remarks,COALESCE(s.outcome,'active'),u.gps_latitude,u.gps_longitude,u.gps_accuracy_m,u.gps_recorded_at,u.gps_status FROM sampling_units u JOIN surveys s ON s.id=u.event_id ORDER BY u.event_id,u.sequence_number",
              "sampling_unit_id,event_id,station_id,sequence,method,start_utc,end_utc,target_seconds,completed,active_effort_minutes,searched_area_m2,searched_length_m,noise_score,wind_score,rain_condition,equipment_id,number_devices,device_hours,unit_paused_ms,verified_camera_days,verified_trap_nights,verified_recorder_hours,deployment_start_utc,deployment_end_utc,downtime_hours,effort_notes,survey_outcome,station_gps_lat,station_gps_lon,station_accuracy_m,gps_fix_at_utc,gps_status");
            csv(exportDb,z,"point_counts.csv",
              "SELECT p.id,p.event_id,p.station_id,p.sequence_number,p.started,p.ended,p.target_seconds,p.completed,p.notes,p.unit_paused_ms,COALESCE(s.outcome,'active'),p.planned_chainage_m,p.gps_latitude,p.gps_longitude,p.gps_accuracy_m,p.gps_recorded_at,p.gps_status FROM point_counts p JOIN surveys s ON s.id=p.event_id ORDER BY p.event_id,p.sequence_number",
              "point_count_id,event_id,station_id,sequence,start_utc,end_utc,target_seconds,completed,notes,unit_paused_ms,survey_outcome,planned_chainage_m,station_gps_lat,station_gps_lon,station_gps_accuracy_m,gps_fix_at_utc,gps_status");
            csv(exportDb,z,"route_templates.csv",
              "SELECT id,project_id,name,method,planned_length_m,station_spacing_m,station_count,created_at_utc,notes FROM route_templates",
              "route_template_id,project_id,route_name,method,planned_length_m,station_spacing_m,station_count,created_at_utc,notes");
            csv(exportDb,z,"route_template_stations.csv",
              "SELECT template_id,station_id,sequence_number,chainage_m FROM route_template_stations ORDER BY template_id,sequence_number",
              "route_template_id,station_id,sequence_number,planned_chainage_m");
            csv(exportDb,z,"survey_stations.csv",
              "SELECT r.event_id,r.station_id,r.sequence_number,r.planned_chainage_m,r.visit_state,r.captured_at,r.gps_latitude,r.gps_longitude,r.gps_accuracy_m,r.gps_status,COALESCE(s.outcome,'active') FROM survey_stations r JOIN surveys s ON s.id=r.event_id ORDER BY r.event_id,r.sequence_number",
              "event_id,station_id,sequence_number,planned_chainage_m,visit_state,count_started_at_utc,station_lat,station_lon,station_gps_accuracy_m,gps_status,survey_outcome");
            csv(exportDb,z,"survey_outcome_audit.csv",
              "SELECT id,event_id,previous_outcome,new_outcome,changed_at_utc,reason,record_count FROM survey_outcome_audit",
              "audit_id,event_id,previous_outcome,new_outcome,changed_utc,reason,record_count");
            csv(exportDb,z,"effort_pauses.csv",
              "SELECT id,event_id,paused_at_utc,resumed_at_utc,duration_ms,reason FROM survey_effort_pauses",
              "pause_id,event_id,paused_at_utc,resumed_at_utc,duration_ms,reason");
            csv(exportDb,z,"track_points.csv",
              "SELECT id,event_id,recorded_at,latitude,longitude,accuracy_m,segment_m,accepted,reason FROM track_points ORDER BY id",
              "track_point_id,event_id,timestamp_utc,observer_lat,observer_lon,accuracy_m,accepted_segment_m,accepted,quality_reason");
            csv(exportDb,z,"distance_samples.csv",
              "SELECT p.id,p.station_id,'point_count',COALESCE(s.region_label,''),s.area_km2,1.0,NULL,p.target_seconds,s.point_radius_m,p.completed FROM point_counts p JOIN surveys s ON s.id=p.event_id WHERE s.survey_profile='Birds' AND COALESCE(s.outcome,'active')='complete' UNION ALL SELECT id,COALESCE(station_id,''),'line_transect',COALESCE(region_label,''),area_km2,CASE WHEN effort_length_m>0 THEN effort_length_m/1000.0 ELSE NULL END,effort_length_m,planned_duration_s,NULL,complete FROM surveys WHERE survey_profile='Birds' AND method='line_transect' AND COALESCE(outcome,'active')='complete'",
              "Sample.Label,station_id,method,Region.Label,Area_km2,Effort_line_km_or_point_visits,line_length_m,planned_duration_seconds,point_radius_m,completed");
            DistanceExport.write(z,exportDb.getReadableDatabase());
            ResearchExports.write(z,exportDb.getReadableDatabase());
            PhotoEvidence.writeZip(getApplicationContext(),z,exportDb.getReadableDatabase());
            AcousticEvidence.writeZip(getApplicationContext(),z,exportDb.getReadableDatabase());
            TrapEvidence.writeZip(z,exportDb.getReadableDatabase());
            FieldProtocolEvidence.writeZip(z,exportDb.getReadableDatabase());
            ProtocolEvidencePhotos.writeZip(getApplicationContext(),z,
                exportDb.getReadableDatabase());
            StudyProtocolExports.writeZip(z,exportDb.getReadableDatabase());
            DataAudit.writeZip(getApplicationContext(),z,exportDb.getReadableDatabase());
            csv(exportDb,z,"distance_review.csv",
              "SELECT r.id,r.event_id,r.taxon_group,r.raw_identification,r.horizontal_m,r.perpendicular_m,r.individual_count,r.distance_method,r.initial_position,r.movement_status,r.distance_eligible,s.method,COALESCE(p.station_id,s.station_id),s.effort_length_m,s.point_target_s,s.region_label,s.area_km2,s.complete,r.point_count_id,r.protocol_window_flag FROM field_records r LEFT JOIN surveys s ON s.id=r.event_id LEFT JOIN point_counts p ON p.id=r.point_count_id WHERE r.deleted_at IS NULL AND r.taxon_group='Birds' AND COALESCE(s.outcome,'active')='complete' AND (s.method='point_count' OR s.method='line_transect')",
              "record_id,event_id,taxon_group,taxon,horizontal_m,perpendicular_m,group_size,distance_method,initial_position,movement_status,candidate_flag,survey_method,station_id,effort_length_m,point_target_s,region_label,area_km2,completed,point_count_id,window_flag");
            csv(exportDb,z,"edit_history.csv",
              "SELECT id,record_id,action,edited_at,previous_json,current_json,reason FROM record_edits",
              "audit_id,record_id,action,edited_at,previous_json,current_json,reason");
            runOnUiThread(()->toast("Survey ZIP exported"));
            }catch(Exception e){
                runOnUiThread(()->toast("Export error: "+e.getMessage()));
            }finally{exportInProgress=false;}
        },"LIKAS-Export").start();
    }
    private void csv(FieldDb exportDb,ZipOutputStream z,String name,String sql,String header)throws Exception{
        z.putNextEntry(new ZipEntry(name));z.write((header+"\n").getBytes(StandardCharsets.UTF_8));
        try(Cursor c=exportDb.getReadableDatabase().rawQuery(sql,null)){
            while(c.moveToNext()){
                StringBuilder line=new StringBuilder();
                for(int j=0;j<c.getColumnCount();j++){
                    if(j>0)line.append(',');
                    if(!c.isNull(j))line.append('"').append(c.getString(j).replace("\"","\"\"")).append('"');
                }
                z.write((line.toString()+"\n").getBytes(StandardCharsets.UTF_8));
            }
        }z.closeEntry();
    }
    private static String iso(long ms) {
        SimpleDateFormat f=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.US);
        f.setTimeZone(TimeZone.getTimeZone("UTC"));return f.format(new Date(ms));
    }
    private static long parseIso(String s) {
        try{SimpleDateFormat f=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.US);
            f.setTimeZone(TimeZone.getTimeZone("UTC"));return f.parse(s).getTime();
        }catch(Exception e){return System.currentTimeMillis();}
    }

    private void queueDraft(){
        if(!active)return;
        handler.removeCallbacks(delayedDraftSave);
        handler.postDelayed(delayedDraftSave,800);
    }
    private void watchDraftFields(){
        for(EditText e:new EditText[]{taxonEntry,distanceEntry,lineEntry,identificationNote,habitatEntry,behaviorEntry,notesEntry,tempEntry,humidityEntry,weatherEntry,microhabitatEntry,perchEntry,herpHeightEntry,deviceEntry}){
            e.addTextChangedListener(new TextWatcher(){
                public void beforeTextChanged(CharSequence s,int start,int count,int after){}
                public void onTextChanged(CharSequence s,int start,int before,int count){queueDraft();}
                public void afterTextChanged(Editable e){}
            });
        }
        android.widget.AdapterView.OnItemSelectedListener onSelection=new android.widget.AdapterView.OnItemSelectedListener(){
            public void onNothingSelected(android.widget.AdapterView<?> a){}
            public void onItemSelected(android.widget.AdapterView<?> a,View v,int p,long id){queueDraft();}
        };
        detectionSpinner.setOnItemSelectedListener(onSelection);
        distanceSourceSpinner.setOnItemSelectedListener(onSelection);
        firstPositionSpinner.setOnItemSelectedListener(onSelection);
        movementSpinner.setOnItemSelectedListener(onSelection);
        substrateSpinner.setOnItemSelectedListener(onSelection);
        callScoreSpinner.setOnItemSelectedListener(onSelection);
        evidenceSpinner.setOnItemSelectedListener(onSelection);
        lifeStageSpinner.setOnItemSelectedListener(onSelection);
        noiseSpinner.setOnItemSelectedListener(onSelection);
        confidenceSpinner.setOnItemSelectedListener(onSelection);
    }
    /** Foreground survey draft is persisted independent of Android Activity lifetime. */
    private void persistDraft(){
        if(!active||eventId==null||taxonEntry==null)return;
        try{
            JSONObject d=new JSONObject();
            d.put("group",group);d.put("taxon",taxonEntry.getText().toString());
            d.put("confidence",confidenceSpinner.getSelectedItemPosition());
            d.put("identificationNote",identificationNote.getText().toString());
            d.put("pendingPhoto",pendingPhoto==null?"":pendingPhoto);
            d.put("count",count);d.put("distance",distanceEntry.getText().toString());
            d.put("line",lineEntry.getText().toString());
            d.put("detection",detectionSpinner.getSelectedItemPosition());
            d.put("point_count_id",pointCountId==null?JSONObject.NULL:pointCountId);
            d.put("habitat",habitatEntry.getText().toString());
            d.put("air_temp",tempEntry.getText().toString());
            d.put("humidity",humidityEntry.getText().toString());
            d.put("weather",weatherEntry.getText().toString());
            d.put("microhabitat",microhabitatEntry.getText().toString());
            d.put("substrate",substrateSpinner.getSelectedItemPosition());
            d.put("callIndex",callScoreSpinner.getSelectedItemPosition());
            d.put("noise",noiseSpinner.getSelectedItemPosition());
            d.put("lifeStage",lifeStageSpinner.getSelectedItemPosition());
            d.put("evidence",evidenceSpinner.getSelectedItemPosition());
            d.put("device",deviceEntry.getText().toString());
            d.put("height",perchEntry.getText().toString());
            d.put("herpHeight",herpHeightEntry.getText().toString());
            d.put("behavior",behaviorEntry.getText().toString());
            d.put("notes",notesEntry.getText().toString());
            d.put("details",detailShown);
            d.put("distanceSource",distanceSourceSpinner.getSelectedItemPosition());
            d.put("firstPosition",firstPositionSpinner.getSelectedItemPosition());
            d.put("movement",movementSpinner.getSelectedItemPosition());
            d.put("markedTime",markedAt);
            if(markedObserverFix!=null){
                d.put("markedGpsLat",markedObserverFix.getLatitude());
                d.put("markedGpsLon",markedObserverFix.getLongitude());
                d.put("markedGpsAccuracy",markedObserverFix.getAccuracy());
                d.put("markedGpsTime",markedObserverFix.getTime());
                d.put("markedGpsElapsed",markedObserverFix.getElapsedRealtimeNanos());
            }
            d.put("bearing",markedBearing==null?JSONObject.NULL:markedBearing);
            d.put("pitch",markedPitch==null?JSONObject.NULL:markedPitch);
            d.put("sensorQuality",sensorQuality);
            d.put("scrollY",fieldScroll==null?0:fieldScroll.getScrollY());
            getSharedPreferences("field_drafts",MODE_PRIVATE).edit().putString("draft_"+eventId,d.toString()).apply();
        }catch(Exception ignored){}
    }
    private void restoreDraft(){
        if(eventId==null||taxonEntry==null)return;
        String raw=getSharedPreferences("field_drafts",MODE_PRIVATE).getString("draft_"+eventId,null);
        if(raw==null)return;
        try{
            JSONObject d=new JSONObject(raw);
            String g=d.optString("group",group);
            if(profile.equals("Herpetofauna")&&(g.equals("Reptiles")||g.equals("Amphibians"))&&!method.equals("amphibian_calls")){
                group=g;showGroups();refreshAutocomplete();
            }
            taxonEntry.setText(d.optString("taxon",""));
            confidenceSpinner.setSelection(d.optInt("confidence",0));
            identificationNote.setText(d.optString("identificationNote",""));
            pendingPhoto=d.optString("pendingPhoto","").isEmpty()?null:d.optString("pendingPhoto");
            showPendingPhoto();
            distanceEntry.setText(d.optString("distance",""));
            if(!d.optString("line","").isEmpty())lineEntry.setText(d.optString("line",""));
            count=d.optInt("count",1);countView.setText(""+count);
            detectionSpinner.setSelection(d.optInt("detection",0));
            habitatEntry.setText(d.optString("habitat",""));
            tempEntry.setText(d.optString("air_temp",""));
            humidityEntry.setText(d.optString("humidity",""));
            weatherEntry.setText(d.optString("weather",""));
            microhabitatEntry.setText(d.optString("microhabitat",""));
            substrateSpinner.setSelection(d.optInt("substrate",0));
            callScoreSpinner.setSelection(d.optInt("callIndex",0));
            noiseSpinner.setSelection(d.optInt("noise",0));
            lifeStageSpinner.setSelection(d.optInt("lifeStage",0));
            evidenceSpinner.setSelection(d.optInt("evidence",0));
            deviceEntry.setText(d.optString("device",""));
            perchEntry.setText(d.optString("height",""));
            herpHeightEntry.setText(d.optString("herpHeight",""));
            behaviorEntry.setText(d.optString("behavior",""));
            notesEntry.setText(d.optString("notes",""));
            distanceSourceSpinner.setSelection(d.optInt("distanceSource",0));
            firstPositionSpinner.setSelection(d.optInt("firstPosition",0));
            movementSpinner.setSelection(d.optInt("movement",0));
            detailShown=d.optBoolean("details",false);
            advancedPanel.setVisibility(detailShown?View.VISIBLE:View.GONE);
            detailToggle.setText(detailShown?"− Hide observation details":"＋ More observation details");
            markedAt=d.optLong("markedTime",0);
            markedObserverFix=null;
            if(markedAt>0&&d.has("markedGpsLat")&&d.has("markedGpsLon")&&
                d.has("markedGpsAccuracy")&&d.has("markedGpsTime")){
                markedObserverFix=new Location(LocationManager.GPS_PROVIDER);
                markedObserverFix.setLatitude(d.optDouble("markedGpsLat"));
                markedObserverFix.setLongitude(d.optDouble("markedGpsLon"));
                markedObserverFix.setAccuracy((float)d.optDouble("markedGpsAccuracy"));
                markedObserverFix.setTime(d.optLong("markedGpsTime"));
                markedObserverFix.setElapsedRealtimeNanos(d.optLong("markedGpsElapsed",0));
            }
            markedBearing=d.isNull("bearing")?null:d.optDouble("bearing");
            markedPitch=d.isNull("pitch")?null:d.optDouble("pitch");
            sensorQuality=d.optInt("sensorQuality",-1);
            showMarkStatus();
            int y=d.optInt("scrollY",0);
            if(fieldScroll!=null)fieldScroll.post(()->fieldScroll.scrollTo(0,y));
        }catch(Exception e){toast("Draft could not be restored. Saved observations remain intact.");}
    }
    private void navigateHome(){
        persistDraft();
        returnToHome=true;
        getSharedPreferences("field_resume",MODE_PRIVATE).edit().putString("screen","home").commit();
        Intent intent=new Intent(this,WelcomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
    @Override public void onBackPressed(){
        if(active){
            new AlertDialog.Builder(this).setTitle("Leave survey running?")
              .setMessage("Your unfinished entry is saved on this device. Continue the survey later from Home.")
              .setNegativeButton("Keep recording",null)
              .setPositiveButton("Go Home",(d,w)->navigateHome()).show();
        }else navigateHome();
    }
    @Override protected void onSaveInstanceState(Bundle out){
        persistDraft();
        out.putString("saved_event_id",eventId);
        super.onSaveInstanceState(out);
    }
    @Override protected void onResume(){
        super.onResume();
        handler.removeCallbacks(visibleTimer);
        status();refreshPauseControls();
        handler.postDelayed(visibleTimer,1000);
        if(active&&!surveyPaused&&!TrackService.wasStopped(this)&&
            (method.equals("line_transect")||method.equals("ves_transect")))startTracking();
        if(active)getSharedPreferences("field_resume",MODE_PRIVATE).edit().putString("screen","survey").commit();
        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
            if(active&&!TrackService.isRunning()&&!TrackService.wasStopped(this)){
                try{TrackService.startGnss(this);}catch(Exception e){
                    toast("Continuous GPS unavailable: "+e.getClass().getSimpleName());
                }
            }
        }
    }
    @Override protected void onPause(){
        handler.removeCallbacks(visibleTimer);
        handler.removeCallbacks(delayedDraftSave);
        persistDraft();
        super.onPause();
        try{locations.removeUpdates(this);}catch(Exception ignored){}
    }
    @Override protected void onDestroy(){
        handler.removeCallbacksAndMessages(null);
        if(hudReader!=null)hudReader.close();
        db.close();
        super.onDestroy();
    }
    @Override public void onRequestPermissionsResult(int code,String[] perms,int[] results){
        super.onRequestPermissionsResult(code,perms,results);
        if(code==71&&results.length>0&&results[0]==PackageManager.PERMISSION_GRANTED
            &&active&&(method.equals("line_transect")||method.equals("ves_transect")))startTracking();
    }
    private void updateGpsLabel(Location fix){
        if(gpsView==null)return;
        if(fix==null||!fix.hasAccuracy()){
            gpsView.setText(TrackService.wasStopped(this)?"GPS stopped · restart":
                "GPS searching · details");
            gpsView.setTextColor(UiKit.AMBER);
            return;
        }
        long age=(SystemClock.elapsedRealtimeNanos()-fix.getElapsedRealtimeNanos())/1000000L;
        boolean qualified=GpsFixPolicy.acceptableElapsed(fix.getLatitude(),fix.getLongitude(),
            fix.getAccuracy(),age,fix.isFromMockProvider());
        gpsView.setText(String.format(Locale.US,"GPS ±%.1f m · %s · details",
            fix.getAccuracy(),qualified?"ready":"low quality"));
        gpsView.setTextColor(qualified?UiKit.FOREST:UiKit.AMBER);
    }
    private void showGpsDetail(){
        Location fix=TrackService.currentFix();
        if(fix==null)fix=location;
        String message=fix==null?"No recent GPS reading":
            String.format(Locale.US,"%.6f, %.6f\nReported accuracy: ±%.1f m\nProvider: %s",
                fix.getLatitude(),fix.getLongitude(),
                fix.hasAccuracy()?fix.getAccuracy():Float.NaN,fix.getProvider());
        if("missing_at_survey_start".equals(startGpsStatus))
            message="Survey-start coordinates are missing and remain unverified. "+
                "New detections or station visits can capture fresh GPS.\n\n"+message;
        AlertDialog.Builder details=new AlertDialog.Builder(this)
            .setTitle("Observer GPS").setMessage(message)
            .setPositiveButton("Close",null);
        if(TrackService.wasStopped(this)||!TrackService.isRunning()){
            details.setNeutralButton("Start GPS",(dialog,which)->{
                try{
                    TrackService.startGnss(this);
                    if(!surveyPaused&&(method.equals("line_transect")||
                        method.equals("ves_transect")))startTracking();
                }catch(Exception error){toast("GPS restart failed: "+error.getMessage());}
            });
        }
        details.show();
    }
    @Override public void onLocationChanged(Location l){
        location=l;
        updateGpsLabel(l);
    }
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}

}
