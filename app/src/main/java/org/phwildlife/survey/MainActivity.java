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
import android.view.MotionEvent;
import android.view.HapticFeedbackConstants;
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
    private static final int EXPORT_DOC=22, CAMERA_REQ=23, PHOTO_PICK=24;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final Runnable delayedDraftSave=()->persistDraft();
    private final Runnable visibleTimer=new Runnable(){
        @Override public void run(){
            status();
            updateGpsReadout();
            handler.postDelayed(this,1000);
        }
    };
    private final int BACK=Color.rgb(247,249,246),INK=Color.rgb(22,51,38),GREEN=Color.rgb(37,104,74),MUTED=Color.rgb(95,116,101);
    private FieldDb db; private LocationManager locations;
    private TextView bearingStatusView,gpsView,elevationView,eventView,countView,hintView,detailToggle,sessionClockView,progressView,pauseStatusView;
    private Button pauseButton,progressButton;
    private CheckBox vibrationChoice,regionalOnlyChoice;
    private Spinner confidenceSpinner;
    private EditText identificationNote;
    private TextView photoStatus;
    private ImageView photoPreview;
    private String pendingPhoto=null;
    private Button favoriteButton;
    private TextView checklistNote;
    private boolean surveyPaused=false;
    private long pauseStartedAt=0,accumulatedPausedMs=0;
    private long unitPausedMs=0;
    private Spinner methodSpinner,detectionSpinner,distanceSourceSpinner,firstPositionSpinner,movementSpinner,substrateSpinner,callScoreSpinner,lifeStageSpinner,evidenceSpinner,noiseSpinner;
    private AutoCompleteTextView taxonEntry;
    private EditText distanceEntry,lineEntry,notesEntry,habitatEntry,behaviorEntry,tempEntry,humidityEntry,weatherEntry,microhabitatEntry,perchEntry,herpHeightEntry,deviceEntry;
    private Button beginButton,markButton,saveButton,reviewButton,pointNextButton;
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
    // A manual elevation applies only to the next saved detection, not future visits.
    private Double manualElevationMeters=null;
    private boolean pointNotice=false;
    private int dp(float n){return (int)(getResources().getDisplayMetrics().density*n+0.5f);}

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        db=new FieldDb(this);
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
    private TextView section(String title){TextView t=label(title,13,true);t.setTextColor(MUTED);return t;}
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
        sessionClockView=label("Total survey · 00:00:00",16,true);
        sessionClockView.setTextColor(UiKit.FOREST);root.addView(sessionClockView);
        pauseStatusView=label("",12,true);pauseStatusView.setTextColor(UiKit.AMBER);root.addView(pauseStatusView);
        eventView=label("Sampling unit timer",15,true);eventView.setTextColor(MUTED);root.addView(eventView);
        progressView=label("",12,false);progressView.setTextColor(MUTED);root.addView(progressView);
        fieldScroll=new ScrollView(this);fieldScroll.setFillViewport(false);fieldScroll.setVerticalScrollBarEnabled(false);
        LinearLayout content=vertical();content.setPadding(dp(1),0,dp(1),dp(12));
        groupBar=horizontal();content.addView(groupBar);showGroups();
        LinearLayout methodRow=horizontal();
        // The method was already chosen in survey setup. Do not offer a second, disabled selector.
        methodSpinner=new Spinner(this);
        TextView methodName=label("Method · "+method.replace('_',' '),14,true);
        methodName.setTextColor(UiKit.FOREST);
        methodRow.addView(methodName,weighted(1f));
        lineRow=horizontal();lineEntry=edit("Transect magnetic bearing °",InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        lineRow.addView(lineEntry,new LinearLayout.LayoutParams(-1,-2));
        methodRow.addView(lineRow,weighted(0.9f));
        content.addView(methodRow);
        beginButton=UiKit.button(this,active?"Finish survey":"Start survey","warning");
        content.addView(beginButton,new LinearLayout.LayoutParams(-1,dp(47)));
        beginButton.setOnClickListener(v->toggleEvent());
        LinearLayout utilityRow=horizontal();
        pauseButton=UiKit.button(this,"Pause effort","secondary");
        utilityRow.addView(pauseButton,new LinearLayout.LayoutParams(0,dp(47),1));
        progressButton=UiKit.button(this,"Station progress","secondary");
        LinearLayout.LayoutParams progressLp=new LinearLayout.LayoutParams(0,dp(47),1);
        progressLp.leftMargin=dp(7);utilityRow.addView(progressButton,progressLp);
        content.addView(utilityRow);
        pauseButton.setOnClickListener(v->togglePause());
        progressButton.setOnClickListener(v->openProgress());
        vibrationChoice=new CheckBox(this);
        vibrationChoice.setText("Vibrate near interval end (optional)");
        vibrationChoice.setTextSize(12);
        vibrationChoice.setChecked(getSharedPreferences("field_prefs",MODE_PRIVATE).getBoolean("vibrate_warnings",false));
        vibrationChoice.setOnCheckedChangeListener((button,enabled)->
            getSharedPreferences("field_prefs",MODE_PRIVATE).edit().putBoolean("vibrate_warnings",enabled).apply());
        content.addView(vibrationChoice);
        pointNextButton=UiKit.button(this,"Finish point · Next station","camera");
        content.addView(pointNextButton,new LinearLayout.LayoutParams(-1,dp(53)));
        pointNextButton.setVisibility(hasStationUnits()?View.VISIBLE:View.GONE);
        pointNextButton.setOnClickListener(v->{if(method.equals("point_count"))nextPoint();else nextSamplingUnit();});
        trackingView=label("GPS track: awaiting location",14,true);
        trackingView.setTextColor(UiKit.BLUE);
        content.addView(trackingView);
        trackingView.setVisibility(method.equals("line_transect")||method.equals("ves_transect")?View.VISIBLE:View.GONE);
        content.addView(section("DETECTION"));
        LinearLayout detectRow=horizontal();
        Button markTime=UiKit.button(this,"Mark now","secondary");
        detectRow.addView(markTime,weighted(0.9f));
        markButton=UiKit.button(this,"Open camera","camera");
        detectRow.addView(markButton,weighted(1.1f));
        content.addView(detectRow);
        markTime.setOnClickListener(v->markDetection());
        markButton.setOnClickListener(v->openCamera());
        LinearLayout markRow=horizontal();
        bearingStatusView=label("Time and bearing not marked",12,false);
        bearingStatusView.setTextColor(MUTED);
        markRow.addView(bearingStatusView,weighted(1));
        TextView clear=label("Clear mark",12,true);
        clear.setTextColor(GREEN);
        clear.setPadding(dp(10),dp(8),dp(6),dp(8));
        clear.setOnClickListener(v->clearMark());
        markRow.addView(clear);
        content.addView(markRow);
        gpsView=label("GPS: awaiting fix",11,false);
        gpsView.setTextColor(MUTED);
        content.addView(gpsView);
        elevationView=label("Elevation: awaiting fresh GPS · tap to edit",12,false);
        elevationView.setTextColor(UiKit.BLUE);
        elevationView.setOnClickListener(v->editElevation());
        content.addView(elevationView);
        content.addView(section("SPECIES / IDENTIFICATION"));
        checklistNote=label("Geographic suggestions: "+GeoRegions.label(geographicRegion),13,true);
        checklistNote.setTextColor(UiKit.FOREST);content.addView(checklistNote);
        regionalOnlyChoice=new CheckBox(this);
        regionalOnlyChoice.setText("Show regional suggestions only (partial curated list)");
        regionalOnlyChoice.setTextSize(13);
        regionalOnlyChoice.setChecked(!geographicRegion.equals("PH")&&!geographicRegion.equals("OTHER"));
        content.addView(regionalOnlyChoice);
        regionalOnlyChoice.setOnCheckedChangeListener((b,only)->updateTaxaSuggestions(false));
        LinearLayout speciesRow=horizontal();
        taxonEntry=new AutoCompleteTextView(this);
        taxonEntry.setTextSize(18);taxonEntry.setTextColor(INK);taxonEntry.setSingleLine(true);
        taxonEntry.setThreshold(1);taxonEntry.setHint("Search common or scientific name");
        taxonEntry.setSelectAllOnFocus(true);
        speciesRow.addView(taxonEntry,new LinearLayout.LayoutParams(0,-2,1));
        Button unknownBtn=UiKit.button(this,"Unidentified","secondary");
        unknownBtn.setTextSize(12);
        LinearLayout.LayoutParams unknownLp=new LinearLayout.LayoutParams(dp(127),dp(52));
        unknownLp.leftMargin=dp(6);speciesRow.addView(unknownBtn,unknownLp);
        favoriteButton=UiKit.button(this,"☆","secondary");
        favoriteButton.setTextSize(22);
        LinearLayout.LayoutParams starP=new LinearLayout.LayoutParams(dp(48),dp(52));
        starP.leftMargin=dp(4);speciesRow.addView(favoriteButton,starP);
        favoriteButton.setContentDescription("Add or remove favorite species");
        favoriteButton.setOnClickListener(v->toggleFavorite());
        content.addView(speciesRow);
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
        content.addView(label("Identification confidence",12,true));
        confidenceSpinner=new Spinner(this);
        confidenceSpinner.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,
            new String[]{"Field-identified (not expert verified)","Probable","Uncertain","Unidentified"}));
        content.addView(confidenceSpinner);
        minus.setOnClickListener(v->{count=Math.max(1,count-1);countView.setText(""+count);queueDraft();});
        plus.setOnClickListener(v->{count=Math.min(9999,count+1);countView.setText(""+count);queueDraft();});
        LinearLayout row=horizontal();
        distanceEntry=edit("Horizontal distance (m)",InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        row.addView(distanceEntry,weighted(1.2f));
        detectionSpinner=new Spinner(this);
        detectionSpinner.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"Visual","Auditory","Both","Sign","Camera"}));
        row.addView(detectionSpinner,weighted(0.8f));content.addView(row);
        birdMeasurements=vertical();
        birdMeasurements.addView(section("BIRD DISTANCE QUALITY"));
        distanceSourceSpinner=new Spinner(this);
        distanceSourceSpinner.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,
            new String[]{"Unknown","Estimated","Measured"}));
        birdMeasurements.addView(label("Distance method",12,false));
        birdMeasurements.addView(distanceSourceSpinner);
        firstPositionSpinner=new Spinner(this);
        firstPositionSpinner.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,
            new String[]{"Unknown","Yes","No"}));
        birdMeasurements.addView(label("Distance to initial detection position?",12,false));
        birdMeasurements.addView(firstPositionSpinner);
        movementSpinner=new Spinner(this);
        movementSpinner.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,
            new String[]{"Unknown","Stationary","Moving","Flyover","Entering after start"}));
        birdMeasurements.addView(label("Movement at first detection",12,false));
        birdMeasurements.addView(movementSpinner);
        content.addView(birdMeasurements);
        TextView birdToggle=label("▾ Bird distance details",13,true);
        birdToggle.setTextColor(GREEN);
        if(profile.equals("Birds")){
            // Keep the distance sampling quality items accessible in one tap.
            content.addView(birdToggle);
            birdMeasurements.setVisibility(View.GONE);
            birdToggle.setOnClickListener(v->{
                boolean show=birdMeasurements.getVisibility()!=View.VISIBLE;
                birdMeasurements.setVisibility(show?View.VISIBLE:View.GONE);
                birdToggle.setText(show?"▴ Hide bird distance details":"▾ Bird distance details");
            });
        }
        herpPanel=vertical();
        herpPanel.addView(section("HERPETOFAUNA / FIELD CONDITIONS (OPTIONAL)"));
        microhabitatEntry=edit("Microhabitat (stream edge, litter, canopy...)",
            InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        herpPanel.addView(microhabitatEntry);
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
        herpPanel.addView(label("Amphibian call index (ordinal, method-dependent)",12,false));herpPanel.addView(callScoreSpinner);
        lifeStageSpinner=new Spinner(this);
        lifeStageSpinner.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,
            new String[]{"Life stage not recorded","Egg","Larva / tadpole","Juvenile","Subadult","Adult","Unknown"}));
        herpPanel.addView(label("Life stage",12,false));herpPanel.addView(lifeStageSpinner);
        noiseSpinner=new Spinner(this);
        noiseSpinner.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,
            new String[]{"Noise not assessed","0 — none","1 — slight","2 — moderate","3 — serious","4 — severe"}));
        herpPanel.addView(label("Background noise during amphibian listening",12,false));
        herpPanel.addView(noiseSpinner);
        content.addView(herpPanel);
        TextView herpToggle=label("▾ Amphibian / reptile environment",13,true);
        herpToggle.setTextColor(GREEN);
        if(profile.equals("Herpetofauna")){
            content.addView(herpToggle);herpPanel.setVisibility(View.GONE);
            herpToggle.setOnClickListener(v->{
                boolean open=herpPanel.getVisibility()!=View.VISIBLE;
                herpPanel.setVisibility(open?View.VISIBLE:View.GONE);
                herpToggle.setText(open?"▴ Hide environment":"▾ Amphibian / reptile environment");
            });
        }
        mammalPanel=vertical();
        mammalPanel.addView(section("MAMMAL SURVEY EVIDENCE / EQUIPMENT"));
        evidenceSpinner=new Spinner(this);
        evidenceSpinner.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,
            new String[]{"Evidence not recorded","Direct sighting","Track","Scat","Burrow / den","Camera image","Acoustic detection","Live capture","Other sign"}));
        mammalPanel.addView(label("Type of evidence",12,false));mammalPanel.addView(evidenceSpinner);
        deviceEntry=edit("Equipment or trap ID (optional)",InputType.TYPE_CLASS_TEXT);
        mammalPanel.addView(deviceEntry);
        perchEntry=edit("Height above ground (m, optional)",InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        mammalPanel.addView(perchEntry);
        content.addView(mammalPanel);
        TextView mammalToggle=label("▾ Mammal evidence details",13,true);
        mammalToggle.setTextColor(GREEN);
        if(profile.equals("Mammals")){
            content.addView(mammalToggle);mammalPanel.setVisibility(View.GONE);
            mammalToggle.setOnClickListener(v->{
                boolean open=mammalPanel.getVisibility()!=View.VISIBLE;
                mammalPanel.setVisibility(open?View.VISIBLE:View.GONE);
                mammalToggle.setText(open?"▴ Hide mammal evidence":"▾ Mammal evidence details");
            });
        }
        content.addView(section("PHOTO EVIDENCE"));
        LinearLayout photoRow=horizontal();
        Button photoButton=UiKit.button(this,"Attach photo from phone","secondary");
        photoRow.addView(photoButton,new LinearLayout.LayoutParams(0,dp(49),2));
        photoStatus=label("No photo attached",12,false);
        photoRow.addView(photoStatus,new LinearLayout.LayoutParams(0,-2,2));
        content.addView(photoRow);
        photoButton.setOnClickListener(v->pickPhoto());
        photoPreview=new ImageView(this);
        photoPreview.setAdjustViewBounds(true);
        photoPreview.setMaxHeight(dp(180));
        photoPreview.setVisibility(View.GONE);content.addView(photoPreview);
        detailToggle=label("＋ Ecology, habitat and notes (optional)",14,true);
        detailToggle.setTextColor(GREEN);content.addView(detailToggle);
        advancedPanel=vertical();
        habitatEntry=edit("Habitat / microhabitat",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        behaviorEntry=edit("Behavior / reproductive evidence",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        notesEntry=edit("Observation notes",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        notesEntry.setSingleLine(false);notesEntry.setMinLines(2);
        identificationNote=edit("Identification evidence / uncertainty (optional)",
            InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        advancedPanel.addView(identificationNote);
        advancedPanel.addView(habitatEntry);advancedPanel.addView(behaviorEntry);advancedPanel.addView(notesEntry);
        advancedPanel.setVisibility(View.GONE);content.addView(advancedPanel);
        detailToggle.setOnClickListener(v->{detailShown=!detailShown;advancedPanel.setVisibility(detailShown?View.VISIBLE:View.GONE);queueDraft();
            detailToggle.setText(detailShown?"− Hide ecology details":"＋ Ecology, habitat and notes (optional)");});
        hintView=label("Tip: Mark detection records the time and optional direction. Distance is entered manually.",12,false);
        hintView.setTextColor(MUTED);content.addView(hintView);
        fieldScroll.addView(content);
        root.addView(fieldScroll,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout footer=horizontal();
        saveButton=UiKit.button(this,"Save observation","primary");
        footer.addView(saveButton,new LinearLayout.LayoutParams(0,dp(56),2f));
        reviewButton=UiKit.button(this,"Review","secondary");
        LinearLayout.LayoutParams reviewParams=new LinearLayout.LayoutParams(0,dp(56),1f);
        reviewParams.leftMargin=dp(8);footer.addView(reviewButton,reviewParams);
        root.addView(footer);
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
        updateMethods();refreshAutocomplete();status();updateGpsReadout();
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
        String[] names=SurveyMethods.displayNames(profile);
        methodSpinner.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,names));
        int idx=0;
        SurveyMethods.Method[] all=SurveyMethods.forProfile(profile);
        for(int i=0;i<all.length;i++)if(all[i].code.equals(method))idx=i;
        methodSpinner.setSelection(idx);
        methodSpinner.setEnabled(false);
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
    private void pickPhoto(){
        Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent,PHOTO_PICK);
    }
    private void showPendingPhoto(){
        if(photoStatus!=null)photoStatus.setText(pendingPhoto==null?"No photo selected":"Photo ready · copied offline");
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
        java.util.LinkedHashSet<String> items=new java.util.LinkedHashSet<>();
        boolean regional=regionalOnlyChoice!=null&&regionalOnlyChoice.isChecked()&&
            !geographicRegion.equals("PH")&&!geographicRegion.equals("OTHER");
        for(String unknown:Taxa.unknownLabels(group))items.add(unknown);
        try(Cursor fav=db.getReadableDatabase().rawQuery(
            "SELECT raw_identification FROM taxa_favorites WHERE taxon_group=? ORDER BY raw_identification",
            new String[]{group})){
            while(fav.moveToNext())items.add(fav.getString(0));
        }catch(Exception ignored){}
        try(Cursor cat=db.getReadableDatabase().rawQuery(
            "SELECT common_name,scientific_name,region_codes,review_status FROM taxon_catalog "+
            "WHERE taxon_group=? ORDER BY common_name,scientific_name",
            new String[]{group})){
            while(cat.moveToNext()){
                String common=cat.isNull(0)?"":cat.getString(0);
                String scientific=cat.getString(1);
                String codes=cat.isNull(2)?"":cat.getString(2);
                boolean matched=!"PH".equals(geographicRegion)&&
                    (("|"+codes+"|").contains("|"+geographicRegion+"|")||
                     (!nonNull(adminPsgc).isEmpty()&&("|"+codes+"|").contains("|P:"+adminPsgc+"|")))&&
                    cat.getString(3).equals("reviewed");
                if(!regional||matched)items.add(common.isEmpty()?scientific:common+" · "+scientific);
            }
        }catch(Exception ignored){}
        String[] curated=Taxa.sortedLabels(group,geographicRegion,regional);
        for(String v:curated)items.add(v);
        if(!regional){
            try(Cursor c=db.getReadableDatabase().rawQuery(
                "SELECT common_name,scientific_name FROM field_records WHERE taxon_group=? AND deleted_at IS NULL ORDER BY id DESC LIMIT 12",
                new String[]{group})){
                while(c.moveToNext()){
                    String n=c.isNull(0)?"":c.getString(0),latin=c.isNull(1)?"":c.getString(1);
                    if(!n.isEmpty()&&!latin.isEmpty())items.add(n+" · "+latin);
                    else if(!n.isEmpty())items.add(n);
                }
            }catch(Exception ignored){}
        }
        taxonEntry.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_dropdown_item_1line,
            new java.util.ArrayList<>(items)));
        if(checklistNote!=null)checklistNote.setText(
            (nonNull(adminProvince).isEmpty()?"":adminProvince+" · ")+
            (geographicRegion.equals("PH")?"Philippines · all starter taxa":GeoRegions.label(geographicRegion))+
            (regional?" · documented regional subset only":" · includes unassessed taxa")+
            "\nRegional tags are incomplete; typing another species remains allowed.");
        taxonEntry.setHint(group.equals("Birds")?"Bird name or unidentified bird":
            "Name or unidentified "+group.toLowerCase(Locale.US).replace("s",""));
        if(clear)taxonEntry.setText("");
        if(distanceEntry!=null)distanceEntry.setHint(group.equals("Birds")?"Horizontal distance (m)":"Distance (m, optional)");
        if(birdMeasurements!=null&&!group.equals("Birds"))birdMeasurements.setVisibility(View.GONE);
        if(herpPanel!=null&&!profile.equals("Herpetofauna"))herpPanel.setVisibility(View.GONE);
        if(mammalPanel!=null&&!profile.equals("Mammals"))mammalPanel.setVisibility(View.GONE);
        if(noiseSpinner!=null)noiseSpinner.setVisibility(method.equals("amphibian_calls")?View.VISIBLE:View.GONE);
        if(callScoreSpinner!=null&&profile.equals("Herpetofauna"))callScoreSpinner.setVisibility(group.equals("Amphibians")?View.VISIBLE:View.GONE);
    }
    private void restoreEvent(){
        try(Cursor c=db.getReadableDatabase().rawQuery("SELECT id,method,started,COALESCE(taxon_group,'Birds'),COALESCE(survey_profile,'Birds'),COALESCE(station_id,''),COALESCE(observer_name,''),COALESCE(point_target_s,600),COALESCE(protocol_status,'legacy'),COALESCE(project_id,'default'),COALESCE(region_label,''),area_km2,transect_az,point_radius_m FROM surveys WHERE ended IS NULL ORDER BY started DESC LIMIT 1",null)){
            if(c.moveToFirst()){
                eventId=c.getString(0);method=c.getString(1);String time=c.getString(2);
                group=c.getString(3);profile=c.getString(4);stationId=c.getString(5);
                observerName=c.getString(6);pointTargetSeconds=c.getInt(7);
                basicLogOnly="basic_log_only".equals(c.getString(8));
                projectId=c.getString(9);regionLabel=c.getString(10);
                stratumAreaKm2=c.isNull(11)?null:c.getDouble(11);
                lineBearing=c.isNull(12)?null:c.getDouble(12);
                pointRadiusM=c.isNull(13)?null:c.getDouble(13);
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
              "planned_route_m,planned_station_spacing_m,planned_station_count,route_design) "+
              "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
              new Object[]{eventId,method,iso(startedAt),lineBearing,group,target,profile,
                "",observerName,basicLogOnly?"basic_log_only":"beta_standardized",projectId,
                regionLabel,stratumAreaKm2,pointRadiusM,plannedDurationSeconds,geographicRegion,
                geographicSelectionMethod,GeoRegions.DATASET,adminProvince,adminPsgc,
                adminBoundaryVersion,adminBoundaryStatus,plannedRouteMeters,
                method.equals("point_count")?plannedSpacingMeters:null,
                method.equals("point_count")?plannedStationCount:null,
                method.equals("point_count")?"planned_chainage_point_route":
                method.equals("line_transect")||method.equals("ves_transect")?
                    "continuous_planned_route":"unsegmented_method"});
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
        active=true;beginButton.setText("Finish survey");
        saveButton.setEnabled(!hasStationUnits());
        if(hasStationUnits()&&pointNextButton!=null){
            if(method.equals("point_count")&&plannedStationCount>0)
                pointNextButton.setText("Start point 1 · 0 m");
            else pointNextButton.setText("Start first sampling station");
            pointNextButton.setEnabled(true);
        }
        surveyPaused=false;pauseStartedAt=0;accumulatedPausedMs=0;unitPausedMs=0;
        if(method.equals("line_transect")||method.equals("ves_transect"))startTracking();
        methodSpinner.setEnabled(false);lineEntry.setEnabled(false);showGroups();status();
        toast(method.equals("point_count")&&plannedStationCount>0?
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
            pauseStatusView.setText(surveyPaused?"EFFORT PAUSED · records stay stored · resume to continue":"");
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
        if(method.equals("line_transect")||method.equals("ves_transect"))startTracking();
        refreshPauseControls();status();persistDraft();
    }
    private long unitElapsedActiveMs(long started,String unitTable,String id,long now){
        if(started<=0)return 0;
        long paused=0;
        if(id!=null){
            try(Cursor c=db.getReadableDatabase().rawQuery(
                "SELECT COALESCE(unit_paused_ms,0) FROM "+unitTable+" WHERE id=?",new String[]{id})){
                if(c.moveToFirst())paused=c.getLong(0);
            }catch(Exception ignored){}
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
    /** Deliberately different from Android's default 500 ms long click. */
    private void armNotCountedHold(Button redButton){
        final long[] down={0};
        final boolean[] fired={false};
        final Runnable action=()->{
            if(down[0]==0||SystemClock.uptimeMillis()-down[0]<1800)return;
            fired[0]=true;down[0]=0;
            redButton.setText("Not counted · hold 2 seconds");
            redButton.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            showNotCountedReason();
        };
        redButton.setOnClickListener(v->toast("Press and hold for 2 seconds to use Not Counted"));
        redButton.setOnTouchListener((v,e)->{
            int a=e.getActionMasked();
            if(a==MotionEvent.ACTION_DOWN){
                down[0]=SystemClock.uptimeMillis();fired[0]=false;
                redButton.setText("Keep holding · Not Counted");
                handler.postDelayed(action,1800);
                return true;
            }
            if(a==MotionEvent.ACTION_MOVE){
                if(e.getX()<0||e.getY()<0||e.getX()>v.getWidth()||e.getY()>v.getHeight()){
                    down[0]=0;handler.removeCallbacks(action);
                    redButton.setText("Not counted · hold 2 seconds");
                }
                return true;
            }
            if(a==MotionEvent.ACTION_UP||a==MotionEvent.ACTION_CANCEL){
                down[0]=0;handler.removeCallbacks(action);
                redButton.setText("Not counted · hold 2 seconds");
                if(a==MotionEvent.ACTION_UP&&!fired[0])toast("Hold the red button for 2 seconds");
                return true;
            }
            return true;
        });
    }
    private void finishPrompt(){
        if(taxonEntry!=null&&!taxonEntry.getText().toString().trim().isEmpty()){
            new AlertDialog.Builder(this).setTitle("Unsaved observation")
                .setMessage("Save this entry before finishing, or clear the unfinished draft.")
                .setNegativeButton("Keep recording",null)
                .setPositiveButton("Clear draft",(d,w)->{
                    taxonEntry.setText("");distanceEntry.setText("");clearMark();finishPrompt();
                }).show();
            return;
        }
        LinearLayout panel=vertical();panel.setPadding(dp(16),dp(6),dp(16),dp(8));
        TextView explanation=label("Choose how this survey should appear in field records and analyses.",13,false);
        panel.addView(explanation);
        boolean effortRequired=SurveyMethods.needsRouteLength(method);
        EditText length=edit("Verified route length (meters)",InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        EditText area=edit("Area searched (m², optional)",InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        if(effortRequired)panel.addView(length);
        if(method.equals("visual_encounter")||method.equals("ves_transect"))panel.addView(area);
        TextView note=label("Complete: retain for eligible analyses after data review.\nIncomplete: retain and flag insufficient effort.\nNot counted: archive records but exclude the whole survey from scientific summaries.",12,false);
        panel.addView(note);
        Button reject=UiKit.button(this,"Not counted · hold 2 seconds","danger");
        LinearLayout.LayoutParams dangerParams=new LinearLayout.LayoutParams(-1,dp(54));
        dangerParams.topMargin=dp(18);panel.addView(reject,dangerParams);
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Finish survey")
            .setView(panel).setNegativeButton("Continue recording",null)
            .setNeutralButton("Incomplete",null)
            .setPositiveButton("Complete",null).create();
        dialog.setOnShowListener(v->{
            armNotCountedHold(reject);
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(w->{
                if(surveyPaused)endPause();
                pendingSearchedArea=number(area.getText().toString());
                dialog.dismiss();finish(false,number(length.getText().toString()));
            });
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(w->{
                Double meters=number(length.getText().toString());
                Double sq=number(area.getText().toString());
                if(effortRequired&&method.equals("line_transect")&&(meters==null||meters<=0)){
                    length.setError("Validated line length is required for a completed transect");return;
                }
                if(sq!=null&&sq<=0){area.setError("Area must be positive");return;}
                if(surveyPaused)endPause();
                pendingSearchedArea=sq;dialog.dismiss();finish(true,meters);
            });
        });
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
        if(!excluded&&complete&&plannedDurationSeconds>0&&effortTime<plannedDurationSeconds*1000L){
            complete=false;toast("Shorter than planned effort: saved as incomplete");
        }
        if(requiresGenericUnit()&&samplingUnitId!=null)closeUnit(null,null,null,null,null);
        if(requiresGenericUnit()){
            try(Cursor c=db.getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM sampling_units WHERE event_id=? AND COALESCE(completed,0)=0",
                new String[]{eventId})){
                if(c.moveToFirst()&&c.getInt(0)>0)complete=false;
            }
        }
        if(method.equals("point_count")){
            if(pointCountId!=null)closePoint();
            if(plannedStationCount>0){
                int remaining=0;
                try(Cursor planned=db.getReadableDatabase().rawQuery(
                    "SELECT COUNT(*) FROM survey_stations WHERE event_id=? AND visit_state='planned'",
                    new String[]{eventId})){
                    if(planned.moveToFirst())remaining=planned.getInt(0);
                }
                if(remaining>0){complete=false;toast(remaining+" planned points not yet visited; survey marked incomplete");}
            }
            try(Cursor c=db.getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM point_counts WHERE event_id=? AND COALESCE(completed,0)=0",
                new String[]{eventId})){
                if(c.moveToFirst()&&c.getInt(0)>0)complete=false;
            }
        }
        if(method.equals("line_transect")||method.equals("ves_transect"))stopTracking();
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
        String outcome=excluded?"not_counted":complete?"complete":"incomplete";
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
                new Object[]{eventId,"active",outcome,iso(endedAt),reason,records});
            writable.setTransactionSuccessful();
        }finally{writable.endTransaction();}
        pendingSearchedArea=null;
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
    private void startUnit(String station){
        int seq=1;
        try(Cursor c=db.getReadableDatabase().rawQuery(
            "SELECT COALESCE(MAX(sequence_number),0)+1 FROM sampling_units WHERE event_id=?",new String[]{eventId})){
            if(c.moveToFirst())seq=c.getInt(0);
        }
        unitSequence=seq;
        samplingUnitId="UNIT-"+eventId+"-"+unitSequence;
        unitStartedAt=System.currentTimeMillis();
        stationId=station;nearEndNotified=false;pointNotice=false;
        boolean gpsValid=RoutePlan.acceptable(location,unitStartedAt);
        Object lat=gpsValid?location.getLatitude():null,lon=gpsValid?location.getLongitude():null,
            accuracy=gpsValid?location.getAccuracy():null;
        db.getWritableDatabase().execSQL(
            "INSERT INTO sampling_units(id,event_id,station_id,sequence_number,method,started,target_seconds,"+
            "gps_latitude,gps_longitude,gps_accuracy_m,gps_recorded_at,gps_status) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)",
            new Object[]{samplingUnitId,eventId,station,unitSequence,method,iso(unitStartedAt),
                plannedDurationSeconds>0?plannedDurationSeconds:null,lat,lon,accuracy,
                gpsValid?iso(location.getTime()):null,
                gpsValid?"gps_fix_at_visit_start":"missing_or_weak_gps_at_start"});
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
    private String createFieldStation(){
        String id=RoutePlan.newSiteId();
        long now=System.currentTimeMillis();
        boolean ok=RoutePlan.acceptable(location,now);
        db.getWritableDatabase().execSQL(
            "INSERT INTO sites(id,project_id,name,latitude,longitude,coordinate_uncertainty_m,notes,created_at,updated_at) "+
            "VALUES(?,?,?,?,?,?,?,?,?)",
            new Object[]{id,projectId,"Field station "+(unitSequence+1),
                ok?location.getLatitude():null,ok?location.getLongitude():null,
                ok?location.getAccuracy():null,
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
                    String chosen=selected==0?createFieldStation():existing[selected-1];
                    startUnit(chosen);
                    taxonEntry.setText("");distanceEntry.setText("");clearMark();
                    toast("Visit "+unitSequence+" · "+stationId);
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
        long now=System.currentTimeMillis();
        if(RoutePlan.acceptable(location,now)){
            beginPlannedPoint(next,location);return;
        }
        new AlertDialog.Builder(this).setTitle("Station GPS not ready")
            .setMessage("Point "+next.sequence+" at planned chainage "+Math.round(next.chainage)+" m.\n"+
                "The app needs a GPS fix from the last 30 seconds, accuracy 75 m or better. "+
                "You can start without coordinates, but that station will be flagged in exports.")
            .setNegativeButton("Wait for fresh GPS",null)
            .setPositiveButton("Start, flag GPS missing",(d,w)->beginPlannedPoint(next,null)).show();
    }
    private void beginPlannedPoint(RoutePlan.Next next,Location fresh){
        if(!active||pointCountId!=null||!method.equals("point_count"))return;
        if(surveyPaused){toast("Resume effort first");return;}
        RoutePlan.Next expected=RoutePlan.next(db.getReadableDatabase(),eventId);
        if(expected==null||!expected.stationId.equals(next.stationId)){
            toast("Station plan has changed. Reopen Start point.");return;
        }
        long now=System.currentTimeMillis();
        int newSequence=next.sequence;
        String id="PC-"+eventId+"-"+newSequence;
        RoutePlan.start(db.getWritableDatabase(),eventId,next,id,pointTargetSeconds,now,fresh);
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
    private void startPoint(String station){
        SQLiteDatabase d=db.getWritableDatabase();
        int sequence=1;
        try(Cursor c=d.rawQuery("SELECT COALESCE(MAX(sequence_number),0)+1 FROM point_counts WHERE event_id=?",new String[]{eventId})){
            if(c.moveToFirst())sequence=c.getInt(0);
        }
        pointSequence=Math.max(1,sequence);
        pointCountId="PC-"+eventId+"-"+pointSequence;
        stationId=station;pointStartedAt=System.currentTimeMillis();
        pointNotice=false;nearEndNotified=false;
        d.execSQL("INSERT INTO point_counts(id,event_id,station_id,sequence_number,started,target_seconds) VALUES(?,?,?,?,?,?)",
            new Object[]{pointCountId,eventId,stationId,pointSequence,iso(pointStartedAt),pointTargetSeconds});
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
                startPoint(next);
                taxonEntry.setText("");distanceEntry.setText("");clearMark();
                persistDraft();status();
                toast("Station "+next+" · new timer started");
            }).show();
    }
    private void startTracking(){
        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            toast("Location permission is needed to record GPS track");return;
        }
        if(eventId==null)return;
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
    private void markDetection(){
        if(!active){toast("Start a survey first");return;}
        markedAt=System.currentTimeMillis();
        showMarkStatus();persistDraft();
    }
    private void openCamera(){
        if(!active){toast("Start survey first");return;}
        Intent intent=new Intent(this,CameraActivity.class);
        startActivityForResult(intent,CAMERA_REQ);
    }
    private void clearMark(){
        markedAt=0;markedBearing=null;markedPitch=null;
        if(bearingStatusView!=null)bearingStatusView.setText("Time and bearing not marked");
        queueDraft();
    }
    private void showMarkStatus(){
        if(bearingStatusView==null)return;
        if(markedBearing!=null)
            bearingStatusView.setText(String.format(Locale.US,"Bearing %.0f° magnetic · pitch %+.0f°",markedBearing,markedPitch));
        else if(markedAt>0) bearingStatusView.setText("Detection time marked · bearing optional");
        else bearingStatusView.setText("Time and bearing not marked");
    }
    private static Double number(String v){try{return Double.parseDouble(v.trim());}catch(Exception e){return null;}}
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
        Location loc=location;
        Object lat=loc==null?null:loc.getLatitude(),lon=loc==null?null:loc.getLongitude(),acc=loc==null?null:loc.getAccuracy();
        db.getWritableDatabase().execSQL(
            "INSERT INTO field_records(event_id,detected_at,taxon_group,common_name,scientific_name,raw_identification,individual_count,detection_method,horizontal_m,bearing_magnetic,pitch_deg,perpendicular_m,observer_lat,observer_lon,gps_accuracy_m,habitat,behavior,notes,compass_quality,name_source,distance_method,initial_position,movement_status,distance_eligible,updated_at,point_count_id,protocol_window_flag,air_temp_c,humidity_pct,substrate,weather_notes,amphibian_call_score,microhabitat,sampling_unit_id,life_stage,evidence_type,perch_height_m,call_noise_score,unit_time_status,equipment_id,count_basis,identification_level,regional_evidence_status,geo_region_at_detection,identification_confidence,identification_review_note) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
            new Object[]{eventId,iso(first),group,id[0],id[1],raw,count,detectionSpinner.getSelectedItem().toString(),
                distance,markedBearing,markedPitch,perp,lat,lon,acc,
                habitatEntry.getText().toString().trim(),behaviorEntry.getText().toString().trim(),
                notesEntry.getText().toString().trim(),sensorQuality,raw.contains(" · ")?"offline_starter_v0.4":"user_entered",
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
            return;
        }
        long now=System.currentTimeMillis();
        long wallElapsed=Math.max(0,now-startedAt);
        long activeElapsed=Math.max(0,wallElapsed-currentPausedMs(now));
        sessionClockView.setText("TOTAL SESSION "+timeFmt(wallElapsed/1000)+
            "  ·  SESSION EXCL. PAUSES "+timeFmt(activeElapsed/1000));
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
        if(method.equals("point_count")&&plannedStationCount>0){
            int counted=RoutePlan.recordedCount(db.getReadableDatabase(),eventId);
            int[] visits=visitProgress();
            progressView.setText("Planned "+plannedStationCount+" stations · "+counted+
                " visited · "+visits[1]+" complete. Travel and point effort are separate.");
        }else if(hasStationUnits()){
            int[] counts=visitProgress();
            progressView.setText(counts[1]+" completed / "+counts[0]+" started visits · "+
                counts[2]+" distinct stations. Open Station progress for details.");
        }else if(target>0)progressView.setText("Target "+timeFmt(target)+
            " · overtime continues until Finish survey");
        else progressView.setText("The session clock continues until Finish survey");
        if(method.equals("line_transect")||method.equals("ves_transect"))refreshTrack();
    }
    private void exportZip(){
        Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("application/zip");i.putExtra(Intent.EXTRA_TITLE,"wildlife-survey-export.zip");
        startActivityForResult(i,EXPORT_DOC);
    }
    @Override protected void onActivityResult(int req,int result,Intent data){
        super.onActivityResult(req,result,data);
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
                if(markedAt==0)markedAt=data.getLongExtra("captured_at",System.currentTimeMillis());
                markedBearing=Float.isNaN(az)?null:(double)az;
                markedPitch=Float.isNaN(inclination)?null:(double)inclination;
                sensorQuality=data.getIntExtra("sensor_quality",-1);
                showMarkStatus();persistDraft();
            }
            return;
        }
        if(req!=EXPORT_DOC||result!=RESULT_OK||data==null)return;
        try(OutputStream os=getContentResolver().openOutputStream(data.getData());ZipOutputStream z=new ZipOutputStream(os)){
            csv(z,"projects.csv",
              "SELECT id,name,description,created_at FROM projects",
              "project_id,name,description,created_at");
            csv(z,"sites.csv",
              "SELECT id,project_id,name,habitat,latitude,longitude,coordinate_uncertainty_m,notes,created_at,updated_at FROM sites",
              "station_id,project_id,name,habitat,latitude,longitude,coordinate_uncertainty_m,notes,created_at,updated_at");
            csv(z,"surveys.csv",
              "SELECT id,method,started,ended,transect_az,taxon_group,point_target_s,effort_length_m,complete,survey_profile,station_id,observer_name,protocol_status,project_id,region_label,area_km2,point_radius_m,weather,planned_duration_s,gps_track_m,gps_track_status,searched_area_m2,observer_effort_minutes,outcome,exclusion_reason,outcome_changed_at,effort_paused,total_paused_ms,paused_at_utc,geographic_region_id,geographic_selection_method,regional_taxa_version,admin_province_name,admin_province_psgc,admin_boundary_version,admin_boundary_status,planned_route_m,planned_station_spacing_m,planned_station_count,route_design,route_template_id FROM surveys",
              "event_id,method,start_utc,end_utc,transect_magnetic_bearing_deg,initial_taxon_group,point_target_seconds,actual_route_length_m,completed,survey_profile,station_id,observer_name,protocol_status,project_id,region_label,stratum_area_km2,point_radius_m,weather,planned_duration_s,gps_path_m_estimate,gps_track_status,searched_area_m2,observer_effort_minutes,outcome,exclusion_reason,outcome_changed_at,effort_paused,total_paused_ms,paused_at_utc,geographic_region_id,geographic_selection_method,regional_taxa_version,admin_province_name,admin_province_psgc,admin_boundary_version,admin_boundary_status,planned_route_m,planned_station_spacing_m,planned_station_count,route_design,route_template_id");
            csv(z,"observations.csv",
              "SELECT r.id,r.event_id,r.detected_at,r.taxon_group,r.common_name,r.scientific_name,r.raw_identification,r.individual_count,r.detection_method,r.horizontal_m,r.bearing_magnetic,r.pitch_deg,r.perpendicular_m,r.observer_lat,r.observer_lon,r.gps_accuracy_m,r.habitat,r.behavior,r.notes,r.compass_quality,r.name_source,r.distance_method,r.initial_position,r.movement_status,r.distance_eligible,r.updated_at,r.deleted_at,r.legacy_source_id,r.point_count_id,r.protocol_window_flag,r.air_temp_c,r.humidity_pct,r.substrate,r.weather_notes,r.amphibian_call_score,r.microhabitat,r.sampling_unit_id,r.life_stage,r.evidence_type,r.perch_height_m,r.call_noise_score,r.unit_time_status,r.equipment_id,r.count_basis,COALESCE(s.outcome,'active'),r.identification_level,r.regional_evidence_status,r.geo_region_at_detection,r.identification_confidence,r.identification_review_note FROM field_records r LEFT JOIN surveys s ON s.id=r.event_id WHERE r.deleted_at IS NULL",
              "record_id,event_id,detected_at_utc,taxon_group,common_name,scientific_name,raw_identification,individual_count,detection_method,horizontal_distance_m,magnetic_bearing_deg,pitch_deg,perpendicular_distance_m,observer_lat,observer_lon,gps_accuracy_m,habitat,behavior,notes,compass_quality,name_source,distance_method,initial_position,movement_status,distance_candidate_flag,updated_at,deleted_at,legacy_source_id,point_count_id,protocol_window_flag,air_temp_c,humidity_pct,substrate,weather_notes,amphibian_call_score,microhabitat,sampling_unit_id,life_stage,evidence_type,perch_height_m,call_noise_score,unit_time_status,equipment_id,count_basis,survey_outcome,identification_level,regional_evidence_status,geographic_region_at_detection,identification_confidence,identification_review_note");
            csv(z,"sampling_units.csv",
              "SELECT u.id,u.event_id,u.station_id,u.sequence_number,u.method,u.started,u.ended,u.target_seconds,u.completed,u.effort_minutes,u.effort_area_m2,u.effort_length_m,u.noise_score,u.wind_score,u.rain_condition,u.equipment_id,u.number_devices,u.trap_effort_hours,u.unit_paused_ms,u.camera_days_verified,u.trap_nights_verified,u.recorder_hours_verified,u.deployment_started_utc,u.deployment_ended_utc,u.downtime_hours,u.effort_remarks,COALESCE(s.outcome,'active'),u.gps_latitude,u.gps_longitude,u.gps_accuracy_m,u.gps_recorded_at,u.gps_status FROM sampling_units u JOIN surveys s ON s.id=u.event_id ORDER BY u.event_id,u.sequence_number",
              "sampling_unit_id,event_id,station_id,sequence,method,start_utc,end_utc,target_seconds,completed,active_effort_minutes,searched_area_m2,searched_length_m,noise_score,wind_score,rain_condition,equipment_id,number_devices,device_hours,unit_paused_ms,verified_camera_days,verified_trap_nights,verified_recorder_hours,deployment_start_utc,deployment_end_utc,downtime_hours,effort_notes,survey_outcome,station_gps_lat,station_gps_lon,station_accuracy_m,gps_fix_at_utc,gps_status");
            csv(z,"point_counts.csv",
              "SELECT p.id,p.event_id,p.station_id,p.sequence_number,p.started,p.ended,p.target_seconds,p.completed,p.notes,p.unit_paused_ms,COALESCE(s.outcome,'active'),p.planned_chainage_m,p.gps_latitude,p.gps_longitude,p.gps_accuracy_m,p.gps_recorded_at,p.gps_status FROM point_counts p JOIN surveys s ON s.id=p.event_id ORDER BY p.event_id,p.sequence_number",
              "point_count_id,event_id,station_id,sequence,start_utc,end_utc,target_seconds,completed,notes,unit_paused_ms,survey_outcome,planned_chainage_m,station_gps_lat,station_gps_lon,station_gps_accuracy_m,gps_fix_at_utc,gps_status");
            csv(z,"route_templates.csv",
              "SELECT id,project_id,name,method,planned_length_m,station_spacing_m,station_count,created_at_utc,notes FROM route_templates",
              "route_template_id,project_id,route_name,method,planned_length_m,station_spacing_m,station_count,created_at_utc,notes");
            csv(z,"route_template_stations.csv",
              "SELECT template_id,station_id,sequence_number,chainage_m FROM route_template_stations ORDER BY template_id,sequence_number",
              "route_template_id,station_id,sequence_number,planned_chainage_m");
            csv(z,"survey_stations.csv",
              "SELECT r.event_id,r.station_id,r.sequence_number,r.planned_chainage_m,r.visit_state,r.captured_at,r.gps_latitude,r.gps_longitude,r.gps_accuracy_m,r.gps_status,COALESCE(s.outcome,'active') FROM survey_stations r JOIN surveys s ON s.id=r.event_id ORDER BY r.event_id,r.sequence_number",
              "event_id,station_id,sequence_number,planned_chainage_m,visit_state,count_started_at_utc,station_lat,station_lon,station_gps_accuracy_m,gps_status,survey_outcome");
            csv(z,"survey_outcome_audit.csv",
              "SELECT id,event_id,previous_outcome,new_outcome,changed_at_utc,reason,record_count FROM survey_outcome_audit",
              "audit_id,event_id,previous_outcome,new_outcome,changed_utc,reason,record_count");
            csv(z,"effort_pauses.csv",
              "SELECT id,event_id,paused_at_utc,resumed_at_utc,duration_ms,reason FROM survey_effort_pauses",
              "pause_id,event_id,paused_at_utc,resumed_at_utc,duration_ms,reason");
            csv(z,"track_points.csv",
              "SELECT id,event_id,recorded_at,latitude,longitude,accuracy_m,segment_m,accepted,reason FROM track_points ORDER BY id",
              "track_point_id,event_id,timestamp_utc,observer_lat,observer_lon,accuracy_m,accepted_segment_m,accepted,quality_reason");
            csv(z,"distance_samples.csv",
              "SELECT p.id,p.station_id,'point_count',COALESCE(s.region_label,''),s.area_km2,1.0,NULL,p.target_seconds,s.point_radius_m,p.completed FROM point_counts p JOIN surveys s ON s.id=p.event_id WHERE s.survey_profile='Birds' AND COALESCE(s.outcome,'active')='complete' UNION ALL SELECT id,COALESCE(station_id,''),'line_transect',COALESCE(region_label,''),area_km2,CASE WHEN effort_length_m>0 THEN effort_length_m/1000.0 ELSE NULL END,effort_length_m,planned_duration_s,NULL,complete FROM surveys WHERE survey_profile='Birds' AND method='line_transect' AND COALESCE(outcome,'active')='complete'",
              "Sample.Label,station_id,method,Region.Label,Area_km2,Effort_line_km_or_point_visits,line_length_m,planned_duration_seconds,point_radius_m,completed");
            DistanceExport.write(z,db.getReadableDatabase());
            ResearchExports.write(z,db.getReadableDatabase());
            PhotoEvidence.writeZip(this,z,db.getReadableDatabase());
            DataAudit.writeZip(this,z,db.getReadableDatabase());
            csv(z,"distance_review.csv",
              "SELECT r.id,r.event_id,r.taxon_group,r.raw_identification,r.horizontal_m,r.perpendicular_m,r.individual_count,r.distance_method,r.initial_position,r.movement_status,r.distance_eligible,s.method,COALESCE(p.station_id,s.station_id),s.effort_length_m,s.point_target_s,s.region_label,s.area_km2,s.complete,r.point_count_id,r.protocol_window_flag FROM field_records r LEFT JOIN surveys s ON s.id=r.event_id LEFT JOIN point_counts p ON p.id=r.point_count_id WHERE r.deleted_at IS NULL AND r.taxon_group='Birds' AND COALESCE(s.outcome,'active')='complete' AND (s.method='point_count' OR s.method='line_transect')",
              "record_id,event_id,taxon_group,taxon,horizontal_m,perpendicular_m,group_size,distance_method,initial_position,movement_status,candidate_flag,survey_method,station_id,effort_length_m,point_target_s,region_label,area_km2,completed,point_count_id,window_flag");
            csv(z,"edit_history.csv",
              "SELECT id,record_id,action,edited_at,previous_json,current_json,reason FROM record_edits",
              "audit_id,record_id,action,edited_at,previous_json,current_json,reason");
            toast("Exported survey events and observations");
        }catch(Exception e){toast("Export error: "+e.getMessage());}
    }
    private void csv(ZipOutputStream z,String name,String sql,String header)throws Exception{
        z.putNextEntry(new ZipEntry(name));z.write((header+"\n").getBytes(StandardCharsets.UTF_8));
        try(Cursor c=db.getReadableDatabase().rawQuery(sql,null)){
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
            d.put("bearing",markedBearing==null?JSONObject.NULL:markedBearing);
            d.put("pitch",markedPitch==null?JSONObject.NULL:markedPitch);
            d.put("sensorQuality",sensorQuality);
            d.put("scrollY",fieldScroll==null?0:fieldScroll.getScrollY());
            getSharedPreferences("field_drafts",MODE_PRIVATE).edit().putString("draft_"+eventId,d.toString()).commit();
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
            detailToggle.setText(detailShown?"− Hide ecology details":"＋ Ecology, habitat and notes (optional)");
            markedAt=d.optLong("markedTime",0);
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
        if(active&&!surveyPaused&&(method.equals("line_transect")||method.equals("ves_transect")))startTracking();
        if(active)getSharedPreferences("field_resume",MODE_PRIVATE).edit().putString("screen","survey").commit();
        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
            try{locations.requestLocationUpdates(LocationManager.GPS_PROVIDER,2000,1f,this);}catch(Exception ignored){}
        }
    }
    @Override protected void onPause(){
        handler.removeCallbacks(visibleTimer);
        handler.removeCallbacks(delayedDraftSave);
        persistDraft();
        super.onPause();
        try{locations.removeUpdates(this);}catch(Exception ignored){}
    }
    @Override protected void onDestroy(){super.onDestroy();handler.removeCallbacksAndMessages(null);db.close();}
    @Override public void onRequestPermissionsResult(int code,String[] perms,int[] results){
        super.onRequestPermissionsResult(code,perms,results);
        if(code==71&&results.length>0&&results[0]==PackageManager.PERMISSION_GRANTED
            &&active&&(method.equals("line_transect")||method.equals("ves_transect")))startTracking();
    }
    @Override public void onLocationChanged(Location l){
        if(location==null||l.getElapsedRealtimeNanos()>=location.getElapsedRealtimeNanos())
            location=new Location(l);
        updateGpsReadout();
    }
    private void editElevation(){
        EditText entry=edit("Elevation in meters",InputType.TYPE_CLASS_NUMBER|
            InputType.TYPE_NUMBER_FLAG_DECIMAL|InputType.TYPE_NUMBER_FLAG_SIGNED);
        if(manualElevationMeters!=null)
            entry.setText(String.format(Locale.US,"%.1f",manualElevationMeters));
        new AlertDialog.Builder(this).setTitle("Elevation for next observation")
            .setMessage("GPS elevation is automatic. A manual correction affects the next saved observation only. Its height reference is recorded as unspecified.")
            .setView(entry).setNegativeButton("Cancel",null)
            .setNeutralButton("Use GPS",(d,w)->{
                manualElevationMeters=null;updateGpsReadout();
            })
            .setPositiveButton("Apply manual",(d,w)->{
                Double n=number(entry.getText().toString());
                if(n==null||n< -500||n>9000||!Double.isFinite(n)){
                    toast("Enter a valid elevation from -500 to 9000 meters");return;
                }
                manualElevationMeters=n;updateGpsReadout();
            }).show();
    }
    private void updateGpsReadout(){
        long now=System.currentTimeMillis();
        Location fresh=RoutePlan.acceptable(location,now)?location:null;
        if(gpsView!=null){
            if(fresh==null)gpsView.setText("Observer GPS: waiting for a fresh fix (30 s, <=75 m)");
            else gpsView.setText(String.format(Locale.US,
                "Observer GPS: %.5f, %.5f · horizontal ±%.0f m",
                fresh.getLatitude(),fresh.getLongitude(),fresh.getAccuracy()));
        }
        if(elevationView==null)return;
        ElevationFix.Reading value=manualElevationMeters!=null?
            ElevationFix.manual(manualElevationMeters):ElevationFix.fromGps(fresh,now);
        if(value.meters!=null){
            String reference="mean_sea_level".equals(value.datum)?"MSL":
                "wgs84_ellipsoid".equals(value.datum)?"WGS84 ellipsoid":"datum unspecified";
            String uncertainty=value.verticalAccuracyM==null?" · vertical accuracy unknown":
                String.format(Locale.US," · vertical ±%.0f m",value.verticalAccuracyM);
            elevationView.setText(String.format(Locale.US,
                "Elevation: %.1f m %s%s · tap to edit",value.meters,reference,
                manualElevationMeters!=null?" · manual":uncertainty));
        }else if("poor_vertical_accuracy".equals(value.status))
            elevationView.setText(String.format(Locale.US,
                "Elevation: uncertain (vertical ±%.0f m) · tap to edit",value.verticalAccuracyM));
        else if("altitude_unavailable".equals(value.status))
            elevationView.setText("Elevation: unavailable in GPS fix · tap to edit");
        else elevationView.setText("Elevation: awaiting fresh GPS fix · tap to edit");
    }
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}

}
