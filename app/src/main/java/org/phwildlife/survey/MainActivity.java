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
    private static final int EXPORT_DOC=22, CAMERA_REQ=23;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final Runnable delayedDraftSave=()->persistDraft();
    private final int BACK=Color.rgb(247,249,246),INK=Color.rgb(22,51,38),GREEN=Color.rgb(37,104,74),MUTED=Color.rgb(95,116,101);
    private FieldDb db; private LocationManager locations;
    private TextView bearingStatusView,gpsView,eventView,countView,hintView,detailToggle,sessionClockView,progressView;
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
    private String group="Birds",eventId=null,pointCountId=null,method="point_count",profile="Birds",stationId="",observerName="",projectId="default",regionLabel="";
    private long pointStartedAt=0;
    private int pointSequence=0,plannedDurationSeconds=0;
    private boolean nearEndNotified=false;
    private Double pendingSearchedArea=null;
    private TextView trackingView;
    private Double stratumAreaKm2=null,lineBearing=null,pointRadiusM=null;
    private int pointTargetSeconds=600;
    private boolean basicLogOnly=false;
    private boolean active=false,detailShown=false;
    private long startedAt=0,markedAt=0;
    private int count=1,sensorQuality=-1;
    private float bearing=Float.NaN,pitch=Float.NaN;
    private Double markedBearing=null,markedPitch=null;
    private Location location;
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
            double area=launch.getDoubleExtra("stratum_area_km2",Double.NaN);
            stratumAreaKm2=Double.isNaN(area)?null:area;
            double radius=launch.getDoubleExtra("point_radius_m",Double.NaN);
            pointRadiusM=Double.isNaN(radius)?null:radius;
            plannedDurationSeconds=launch.getIntExtra("planned_duration_s",0);
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
        handler.postDelayed(new Runnable(){public void run(){status();handler.postDelayed(this,1000);}},1000);
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
        eventView=label("Sampling unit timer",15,true);eventView.setTextColor(MUTED);root.addView(eventView);
        progressView=label("",12,false);progressView.setTextColor(MUTED);root.addView(progressView);
        fieldScroll=new ScrollView(this);fieldScroll.setFillViewport(false);fieldScroll.setVerticalScrollBarEnabled(false);
        LinearLayout content=vertical();content.setPadding(dp(1),0,dp(1),dp(12));
        groupBar=horizontal();content.addView(groupBar);showGroups();
        LinearLayout methodRow=horizontal();
        methodSpinner=new Spinner(this);methodRow.addView(methodSpinner,weighted(1f));
        lineRow=horizontal();lineEntry=edit("Transect magnetic bearing °",InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        lineRow.addView(lineEntry,new LinearLayout.LayoutParams(-1,-2));
        methodRow.addView(lineRow,weighted(0.9f));
        content.addView(methodRow);
        beginButton=UiKit.button(this,active?"Finish survey":"Start survey","warning");
        content.addView(beginButton,new LinearLayout.LayoutParams(-1,dp(47)));
        beginButton.setOnClickListener(v->toggleEvent());
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
        content.addView(section("SPECIES / IDENTIFICATION"));
        taxonEntry=new AutoCompleteTextView(this);
        taxonEntry.setTextSize(18);taxonEntry.setTextColor(INK);taxonEntry.setSingleLine(true);
        taxonEntry.setThreshold(1);taxonEntry.setHint("Search common or scientific name");
        taxonEntry.setSelectAllOnFocus(true);content.addView(taxonEntry);
        taxonEntry.setOnClickListener(v->taxonEntry.showDropDown());
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
        detailToggle=label("＋ Ecology, habitat and notes (optional)",14,true);
        detailToggle.setTextColor(GREEN);content.addView(detailToggle);
        advancedPanel=vertical();
        habitatEntry=edit("Habitat / microhabitat",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        behaviorEntry=edit("Behavior / reproductive evidence",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        notesEntry=edit("Observation notes",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        notesEntry.setSingleLine(false);notesEntry.setMinLines(2);
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
        String[] names=SurveyMethods.displayNames(profile);
        methodSpinner.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,names));
        int idx=0;
        SurveyMethods.Method[] all=SurveyMethods.forProfile(profile);
        for(int i=0;i<all.length;i++)if(all[i].code.equals(method))idx=i;
        methodSpinner.setSelection(idx);
        methodSpinner.setEnabled(false);
        lineRow.setVisibility(SurveyMethods.isBirdLine(profile,method)?View.VISIBLE:View.GONE);
    }
    private void refreshAutocomplete(){
        java.util.LinkedHashSet<String> all=new java.util.LinkedHashSet<>();
        try(Cursor c=db.getReadableDatabase().rawQuery("SELECT common_name,scientific_name FROM field_records WHERE taxon_group=? ORDER BY id DESC LIMIT 12",new String[]{group})){
            while(c.moveToNext()){
                String a=c.isNull(0)?"":c.getString(0),b=c.isNull(1)?"":c.getString(1);
                if(!a.isEmpty()&&!b.isEmpty())all.add(a+" · "+b);else if(!a.isEmpty())all.add(a);else if(!b.isEmpty())all.add(b);
            }
        }
        for(String s:Taxa.sortedLabels(group))all.add(s);
        taxonEntry.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_dropdown_item_1line,new java.util.ArrayList<>(all)));
        taxonEntry.setHint(group.equals("Birds")?"Search bird (offline starter list)":"Search or enter "+group.toLowerCase(Locale.US)+" taxon");
        taxonEntry.setText("");
        distanceEntry.setHint(group.equals("Birds")?"Horizontal distance (m)":"Distance (m, optional)");
        if(birdMeasurements!=null)birdMeasurements.setVisibility(group.equals("Birds")?View.VISIBLE:View.GONE);
        if(herpPanel!=null)herpPanel.setVisibility(profile.equals("Herpetofauna")?View.VISIBLE:View.GONE);
        if(mammalPanel!=null)mammalPanel.setVisibility(profile.equals("Mammals")?View.VISIBLE:View.GONE);
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
            try(Cursor c=db.getReadableDatabase().rawQuery("SELECT COALESCE(planned_duration_s,0) FROM surveys WHERE id=?",new String[]{eventId})){
                if(c.moveToFirst())plannedDurationSeconds=c.getInt(0);
            }catch(Exception ignored){}
        }
    }
    private void toggleEvent(){
        if(active){finishPrompt();return;}
        method=SurveyMethods.codeForIndex(profile,methodSpinner.getSelectedItemPosition());
        Double line=number(lineEntry.getText().toString());
        if(SurveyMethods.isBirdLine(profile,method) && (line==null||line<0||line>=360)){
            toast("Enter transect MAGNETIC bearing from 0 to 359°");return;
        }
        eventId="SV-"+System.currentTimeMillis();startedAt=System.currentTimeMillis();pointNotice=false;
        int target=method.equals("point_count")?pointTargetSeconds:0;
        lineBearing=SurveyMethods.isBirdLine(profile,method)?line:null;
        db.getWritableDatabase().execSQL("INSERT INTO surveys(id,method,started,transect_az,taxon_group,point_target_s,survey_profile,station_id,observer_name,protocol_status,project_id,region_label,area_km2,point_radius_m,planned_duration_s) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
          new Object[]{eventId,method,iso(startedAt),lineBearing,group,target,profile,stationId,observerName,basicLogOnly?"basic_log_only":"beta_standardized",projectId,regionLabel,stratumAreaKm2,pointRadiusM,plannedDurationSeconds});
        if(method.equals("point_count"))startPoint(stationId);
        else if(requiresGenericUnit())startUnit(stationId);

        active=true;beginButton.setText("Finish survey");saveButton.setEnabled(true);
        if(method.equals("line_transect")||method.equals("ves_transect"))startTracking();
        methodSpinner.setEnabled(false);lineEntry.setEnabled(false);showGroups();status();
        toast("Survey started");
    }
    private void finishPrompt(){
        if(taxonEntry!=null&&!taxonEntry.getText().toString().trim().isEmpty()){
            new AlertDialog.Builder(this).setTitle("Unfinished observation")
                .setMessage("Save the entered observation, or clear it before ending the survey.")
                .setNegativeButton("Keep recording",null)
                .setPositiveButton("Clear unfinished entry",(d,w)->{
                    taxonEntry.setText("");distanceEntry.setText("");clearMark();finishPrompt();
                }).show();
            return;
        }
        if(SurveyMethods.needsRouteLength(method)){
            LinearLayout panel=vertical();panel.setPadding(dp(20),0,dp(20),0);
            EditText effort=edit("Verified surveyed route length (meters)",InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
            panel.addView(effort);
            EditText area=edit("Area actively searched (m², optional)",InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
            boolean ves=method.equals("visual_encounter")||method.equals("ves_transect");
            if(ves)panel.addView(area);
            TextView warning=label("GPS walking distance is an estimate, not validated survey effort.",12,false);
            panel.addView(warning);
            new AlertDialog.Builder(this).setTitle("Finish survey")
                .setMessage("Enter measured effort. Empty effort is permitted only for incomplete surveys.")
                .setView(panel).setNegativeButton("Cancel",null)
                .setNeutralButton("Incomplete",(d,w)->{
                    pendingSearchedArea=number(area.getText().toString());
                    finish(false,number(effort.getText().toString()));
                })
                .setPositiveButton("Completed",(d,w)->{
                    Double length=number(effort.getText().toString());
                    pendingSearchedArea=number(area.getText().toString());
                    if(method.equals("line_transect")&&(length==null||length<=0)){
                        toast("Measure transect length to complete the survey");return;
                    }
                    if(pendingSearchedArea!=null&&pendingSearchedArea<=0){
                        toast("Surveyed area must be greater than zero");return;
                    }
                    finish(true,length);
                }).show();
        }else new AlertDialog.Builder(this).setTitle("Finish survey")
            .setMessage("The survey remains in your records even with zero detections.")
            .setNegativeButton("Cancel",null).setNeutralButton("Incomplete",(d,w)->finish(false,null))
            .setPositiveButton("Completed",(d,w)->finish(true,null)).show();
    }
    private void finish(boolean complete,Double length){
        if(complete&&plannedDurationSeconds>0&&System.currentTimeMillis()-startedAt<plannedDurationSeconds*1000L){
            complete=false;toast("Survey ended before the planned duration; marked incomplete");
        }
        if(requiresGenericUnit()&&samplingUnitId!=null)closeUnit(null,null,null,null,null);
        if(requiresGenericUnit()){
            try(Cursor c=db.getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM sampling_units WHERE event_id=? AND COALESCE(completed,0)=0",new String[]{eventId})){
                if(c.moveToFirst()&&c.getInt(0)>0)complete=false;
            }
        }
        if(method.equals("point_count")){
            if(pointCountId!=null)closePoint();
            // The session may contain multiple finished points, each with its own status.
            try(Cursor c=db.getReadableDatabase().rawQuery(
                 "SELECT COUNT(*) FROM point_counts WHERE event_id=? AND COALESCE(completed,0)=0",new String[]{eventId})){
                if(c.moveToFirst()&&c.getInt(0)>0)complete=false;
            }
        }
        if(method.equals("line_transect")||method.equals("ves_transect"))stopTracking();
        db.getWritableDatabase().execSQL("UPDATE surveys SET ended=?,effort_length_m=?,complete=?,observer_effort_minutes=?,searched_area_m2=? WHERE id=?",
          new Object[]{iso(System.currentTimeMillis()),length,complete?1:0,(System.currentTimeMillis()-startedAt)/60000.0,pendingSearchedArea,eventId});
        pendingSearchedArea=null;
        getSharedPreferences("field_drafts",MODE_PRIVATE).edit().remove("draft_"+eventId).commit();
        active=false;eventId=null;clearMark();
        returnToHome=true;
        getSharedPreferences("field_resume",MODE_PRIVATE).edit().putString("screen","home").commit();
        beginButton.setText("Start survey");
        saveButton.setEnabled(false);methodSpinner.setEnabled(false);lineEntry.setEnabled(false);showGroups();status();
        toast(complete?"Survey completed":"Survey saved as incomplete");
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
        db.getWritableDatabase().execSQL(
            "INSERT INTO sampling_units(id,event_id,station_id,sequence_number,method,started,target_seconds) VALUES(?,?,?,?,?,?,?)",
            new Object[]{samplingUnitId,eventId,station,unitSequence,method,iso(unitStartedAt),
                plannedDurationSeconds>0?plannedDurationSeconds:null});
        if(pointNextButton!=null)pointNextButton.setText("Finish visit · Next station");
        if(saveButton!=null)saveButton.setEnabled(active);
    }
    private void closeUnit(Double area,Double length,String equipment,Integer devices,Double hours){
        if(samplingUnitId==null)return;
        long time=System.currentTimeMillis();
        double minutes=Math.max(0,(time-unitStartedAt)/60000.0);
        int completed=plannedDurationSeconds<=0||time-unitStartedAt>=plannedDurationSeconds*1000L?1:0;
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
    private void genericUnitPicker(){
        String[] list=stationSuggestions();
        if(list.length==0){toast("Register a station from Home → Projects & stations");return;}
        LinearLayout panel=vertical();panel.setPadding(dp(16),0,dp(16),0);
        TextView title=label("Next station, unvisited stations first",13,true);panel.addView(title);
        Spinner stations=new Spinner(this);
        stations.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,list));
        panel.addView(stations);
        EditText area=edit("Verified area searched m² (optional)",InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        EditText length=edit("Verified route/search length m (optional)",InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        EditText equipment=edit("Trap/camera/recorder ID (optional)",InputType.TYPE_CLASS_TEXT);
        EditText devices=edit("Number of devices (optional)",InputType.TYPE_CLASS_NUMBER);
        EditText hours=edit("Verified device-hours (optional)",InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
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
                if(samplingUnitId!=null){
                    String finished=samplingUnitId;
                    closeUnit(number(area.getText().toString()),number(length.getText().toString()),
                        equipment.getText().toString(),integerOrNull(devices.getText().toString()),number(hours.getText().toString()));
                    db.getWritableDatabase().execSQL(
                        "UPDATE sampling_units SET noise_score=?,wind_score=?,rain_condition=? WHERE id=?",
                        new Object[]{nv,windVal,rain.getText().toString().trim(),finished});
                }
                if(w==dialog.getButton(AlertDialog.BUTTON_POSITIVE)){
                    startUnit(list[stations.getSelectedItemPosition()]);
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
        int complete=System.currentTimeMillis()-pointStartedAt>=pointTargetSeconds*1000L?1:0;
        db.getWritableDatabase().execSQL("UPDATE point_counts SET ended=?,completed=? WHERE id=?",
            new Object[]{iso(System.currentTimeMillis()),complete,pointCountId});
        pointCountId=null;
        if(pointNextButton!=null)pointNextButton.setText("Start next point count");
        if(saveButton!=null)saveButton.setEnabled(false);
    }
    private void nextPoint(){
        if(!active||!method.equals("point_count"))return;
        if(!taxonEntry.getText().toString().trim().isEmpty()){
            new AlertDialog.Builder(this).setTitle("Unsaved observation")
                .setMessage("Save the current bird record before switching stations, or clear the draft.")
                .setPositiveButton("Keep recording",null)
                .setNegativeButton("Clear draft",(d,w)->{
                    taxonEntry.setText("");distanceEntry.setText("");clearMark();pointPicker();
                }).show();
            return;
        }
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
                "GPS walked path ~%.0f m · %s",c.getDouble(0),c.getString(1)));
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
            inWindow=pointCountId!=null&&first>=pointStartedAt&&first<=pointStartedAt+pointTargetSeconds*1000L;
        else if(requiresGenericUnit())
            inWindow=samplingUnitId!=null&&(plannedDurationSeconds<=0||
                first<=unitStartedAt+plannedDurationSeconds*1000L)&&first>=unitStartedAt;
        else inWindow=plannedDurationSeconds<=0||first<=startedAt+plannedDurationSeconds*1000L;
        Double height=profile.equals("Herpetofauna")?number(herpHeightEntry.getText().toString()):number(perchEntry.getText().toString());
        if(height!=null&&(height<0||height>150)){
            toast("Check height above ground");return;
        }
        boolean candidate=group.equals("Birds")&&distance!=null&&!distanceMethod.equals("Unknown")
          &&firstPosition.equals("Yes")&&inWindow&&(method.equals("point_count")||method.equals("line_transect"))
          &&(!method.equals("line_transect")||perp!=null)
          &&(!method.equals("point_count")||pointRadiusM==null||distance<=pointRadiusM)
          &&!movement.equals("Flyover")&&!movement.equals("Entering after start");
        Location loc=location;
        Object lat=loc==null?null:loc.getLatitude(),lon=loc==null?null:loc.getLongitude(),acc=loc==null?null:loc.getAccuracy();
        db.getWritableDatabase().execSQL(
            "INSERT INTO field_records(event_id,detected_at,taxon_group,common_name,scientific_name,raw_identification,individual_count,detection_method,horizontal_m,bearing_magnetic,pitch_deg,perpendicular_m,observer_lat,observer_lon,gps_accuracy_m,habitat,behavior,notes,compass_quality,name_source,distance_method,initial_position,movement_status,distance_eligible,updated_at,point_count_id,protocol_window_flag,air_temp_c,humidity_pct,substrate,weather_notes,amphibian_call_score,microhabitat,sampling_unit_id,life_stage,evidence_type,perch_height_m,call_noise_score,unit_time_status,equipment_id,count_basis) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
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
                    "individuals"});
        taxonEntry.setText("");count=1;countView.setText("1");distanceEntry.setText("");
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
        if(!active){
            sessionClockView.setText("Survey not started");
            eventView.setText("No active sampling unit");
            progressView.setText("");
            return;
        }
        long now=System.currentTimeMillis();
        long sessionElapsed=Math.max(0,(now-startedAt)/1000);
        sessionClockView.setText("TOTAL SURVEY TIME  "+timeFmt(sessionElapsed));
        long unitElapsed=-1,target=0;
        String label="Survey";
        if(method.equals("point_count")){
            if(pointCountId!=null){
                unitElapsed=Math.max(0,(now-pointStartedAt)/1000);
                target=pointTargetSeconds;label="POINT "+pointSequence+" · "+stationId;
            }else label="Point finished · start the next station";
        }else if(requiresGenericUnit()){
            if(samplingUnitId!=null){
                unitElapsed=Math.max(0,(now-unitStartedAt)/1000);
                target=plannedDurationSeconds;
                label="VISIT "+unitSequence+" · "+stationId;
            }else label="Visit closed · start the next station";
        }else{
            unitElapsed=sessionElapsed;
            target=plannedDurationSeconds;
            label=method.replace('_',' ').toUpperCase(Locale.US);
        }
        long remaining=unitElapsed<0||target<=0?Long.MAX_VALUE:target-unitElapsed;
        if(unitElapsed<0)eventView.setText(label);
        else if(target<=0)eventView.setText(label+" · ELAPSED "+timeFmt(unitElapsed));
        else if(remaining>0)eventView.setText(label+" · "+timeFmt(remaining)+" left");
        else eventView.setText(label+" · OVERTIME +"+timeFmt(-remaining));
        boolean countdown=remaining!=Long.MAX_VALUE;
        boolean near=countdown&&remaining>0&&remaining<=30;
        boolean overtime=countdown&&remaining<=0;
        eventView.setTextColor(near||overtime?Color.rgb(189,38,38):MUTED);
        // Pulse only in the final 30 seconds; overtime stays solid red.
        eventView.setAlpha(near?((now/600)%2==0?1f:0.36f):1f);
        if(near&&!nearEndNotified){nearEndNotified=true;toast("30 seconds left in this sampling interval");}
        if(overtime&&!pointNotice){pointNotice=true;toast("Interval target reached. Recording continues in overtime.");}
        if(hasStationUnits()){
            int[] visits=visitProgress();
            progressView.setText(visits[1]+" completed of "+visits[0]+" visits · "+visits[2]+" distinct stations");
        }else if(target>0)progressView.setText("Scheduled interval "+timeFmt(target)+" · session continues in overtime");
        else progressView.setText("Session clock continues until Finish survey");
        if(method.equals("line_transect")||method.equals("ves_transect"))refreshTrack();
    }
    private void exportZip(){
        Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("application/zip");i.putExtra(Intent.EXTRA_TITLE,"wildlife-survey-export.zip");
        startActivityForResult(i,EXPORT_DOC);
    }
    @Override protected void onActivityResult(int req,int result,Intent data){
        super.onActivityResult(req,result,data);
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
              "SELECT id,method,started,ended,transect_az,taxon_group,point_target_s,effort_length_m,complete,survey_profile,station_id,observer_name,protocol_status,project_id,region_label,area_km2,point_radius_m,weather,planned_duration_s,gps_track_m,gps_track_status,searched_area_m2,observer_effort_minutes FROM surveys",
              "event_id,method,start_utc,end_utc,transect_magnetic_bearing_deg,initial_taxon_group,point_target_seconds,actual_route_length_m,completed,survey_profile,station_id,observer_name,protocol_status,project_id,region_label,stratum_area_km2,point_radius_m,weather,planned_duration_s,gps_path_m_estimate,gps_track_status,searched_area_m2,observer_effort_minutes");
            csv(z,"observations.csv",
              "SELECT id,event_id,detected_at,taxon_group,common_name,scientific_name,raw_identification,individual_count,detection_method,horizontal_m,bearing_magnetic,pitch_deg,perpendicular_m,observer_lat,observer_lon,gps_accuracy_m,habitat,behavior,notes,compass_quality,name_source,distance_method,initial_position,movement_status,distance_eligible,updated_at,deleted_at,legacy_source_id,point_count_id,protocol_window_flag,air_temp_c,humidity_pct,substrate,weather_notes,amphibian_call_score,microhabitat FROM field_records WHERE deleted_at IS NULL",
              "record_id,event_id,detected_at_utc,taxon_group,common_name,scientific_name,raw_identification,individual_count,detection_method,horizontal_distance_m,magnetic_bearing_deg,pitch_deg,perpendicular_distance_m,observer_lat,observer_lon,gps_accuracy_m,habitat,behavior,notes,compass_quality,name_source,distance_method,initial_position,movement_status,distance_candidate_flag,updated_at,deleted_at,legacy_source_id,point_count_id,protocol_window_flag,air_temp_c,humidity_pct,substrate,weather_notes,amphibian_call_score,microhabitat");
            csv(z,"track_points.csv",
              "SELECT id,event_id,recorded_at,latitude,longitude,accuracy_m,segment_m,accepted,reason FROM track_points ORDER BY id",
              "track_point_id,event_id,timestamp_utc,observer_lat,observer_lon,accuracy_m,accepted_segment_m,accepted,quality_reason");
            csv(z,"distance_samples.csv",
              "SELECT p.id,p.station_id,'point_count',COALESCE(s.region_label,''),s.area_km2,1.0,NULL,p.target_seconds,s.point_radius_m,p.completed FROM point_counts p JOIN surveys s ON s.id=p.event_id WHERE s.survey_profile='Birds' UNION ALL SELECT id,COALESCE(station_id,''),'line_transect',COALESCE(region_label,''),area_km2,CASE WHEN effort_length_m>0 THEN effort_length_m/1000.0 ELSE NULL END,effort_length_m,planned_duration_s,NULL,complete FROM surveys WHERE survey_profile='Birds' AND method='line_transect'",
              "Sample.Label,station_id,method,Region.Label,Area_km2,Effort_line_km_or_point_visits,line_length_m,planned_duration_seconds,point_radius_m,completed");
            DistanceExport.write(z,db.getReadableDatabase());
            ResearchExports.write(z,db.getReadableDatabase());
            csv(z,"distance_review.csv",
              "SELECT r.id,r.event_id,r.taxon_group,r.raw_identification,r.horizontal_m,r.perpendicular_m,r.individual_count,r.distance_method,r.initial_position,r.movement_status,r.distance_eligible,s.method,COALESCE(p.station_id,s.station_id),s.effort_length_m,s.point_target_s,s.region_label,s.area_km2,s.complete,r.point_count_id,r.protocol_window_flag FROM field_records r LEFT JOIN surveys s ON s.id=r.event_id LEFT JOIN point_counts p ON p.id=r.point_count_id WHERE r.deleted_at IS NULL AND r.taxon_group='Birds' AND (s.method='point_count' OR s.method='line_transect')",
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
        for(EditText e:new EditText[]{taxonEntry,distanceEntry,lineEntry,habitatEntry,behaviorEntry,notesEntry,tempEntry,humidityEntry,weatherEntry,microhabitatEntry,perchEntry,herpHeightEntry,deviceEntry}){
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
    }
    /** Foreground survey draft is persisted independent of Android Activity lifetime. */
    private void persistDraft(){
        if(!active||eventId==null||taxonEntry==null)return;
        try{
            JSONObject d=new JSONObject();
            d.put("group",group);d.put("taxon",taxonEntry.getText().toString());
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
        if(active&&(method.equals("line_transect")||method.equals("ves_transect")))startTracking();
        if(active)getSharedPreferences("field_resume",MODE_PRIVATE).edit().putString("screen","survey").commit();
        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
            try{locations.requestLocationUpdates(LocationManager.GPS_PROVIDER,2000,1f,this);}catch(Exception ignored){}
        }
    }
    @Override protected void onPause(){
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
        location=l;
        if(gpsView!=null)gpsView.setText(String.format(Locale.US,"Observer GPS: %.5f, %.5f · ±%.0f m",l.getLatitude(),l.getLongitude(),l.getAccuracy()));
    }
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}

}
