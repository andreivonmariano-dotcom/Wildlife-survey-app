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
    private TextView bearingStatusView,gpsView,eventView,countView,hintView,detailToggle;
    private Spinner methodSpinner,detectionSpinner,distanceSourceSpinner,firstPositionSpinner,movementSpinner;
    private AutoCompleteTextView taxonEntry;
    private EditText distanceEntry,lineEntry,notesEntry,habitatEntry,behaviorEntry;
    private Button beginButton,markButton,saveButton,reviewButton;
    private LinearLayout groupBar,advancedPanel,lineRow,birdMeasurements;
    private ScrollView fieldScroll;
    private boolean returnToHome=false;
    private final String[] groups=Taxa.GROUPS;
    private String group="Birds",eventId=null,method="point_count",profile="Birds",stationId="",observerName="",projectId="default",regionLabel="";
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
        eventView=label("Offline · ready",12,false);eventView.setTextColor(MUTED);root.addView(eventView);
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
        TextView countLabel=label("Individuals",14,true);countLine.addView(countLabel,weighted(1));
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
        saveButton.setEnabled(active);
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
        db.getWritableDatabase().execSQL("INSERT INTO surveys(id,method,started,transect_az,taxon_group,point_target_s,survey_profile,station_id,observer_name,protocol_status,project_id,region_label,area_km2,point_radius_m) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
          new Object[]{eventId,method,iso(startedAt),lineBearing,group,target,profile,stationId,observerName,basicLogOnly?"basic_log_only":"beta_standardized",projectId,regionLabel,stratumAreaKm2,pointRadiusM});

        active=true;beginButton.setText("Finish survey");saveButton.setEnabled(true);
        methodSpinner.setEnabled(false);lineEntry.setEnabled(false);showGroups();status();
        toast("Survey started");
    }
    private void finishPrompt(){
        if(SurveyMethods.needsRouteLength(method)){
            EditText effort=edit("Actual surveyed length (meters)",InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
            new AlertDialog.Builder(this).setTitle("Finish survey").setMessage("Enter actual surveyed route length if known. Leave blank if not measured.")
            .setView(effort).setNegativeButton("Cancel",null)
            .setNeutralButton("Incomplete",(d,w)->finish(false,number(effort.getText().toString())))
            .setPositiveButton("Completed",(d,w)->{
                Double length=number(effort.getText().toString());
                if(method.equals("line_transect")&&(length==null||length<=0)){
                    toast("Enter a positive transect length before completing. Survey remains active.");
                    return;
                }
                finish(true,length);
            }).show();
        } else new AlertDialog.Builder(this).setTitle("Finish survey")
            .setMessage("Completed surveys with zero detections will remain in your event dataset.")
            .setNegativeButton("Cancel",null).setNeutralButton("Incomplete",(d,w)->finish(false,null))
            .setPositiveButton("Completed",(d,w)->finish(true,null)).show();
    }
    private void finish(boolean complete,Double length){
        if(complete&&method.equals("point_count")&&System.currentTimeMillis()-startedAt<pointTargetSeconds*1000L){
            toast("Point count ended early. Saved as incomplete.");
            complete=false;
        }
        db.getWritableDatabase().execSQL("UPDATE surveys SET ended=?,effort_length_m=?,complete=? WHERE id=?",
          new Object[]{iso(System.currentTimeMillis()),length,complete?1:0,eventId});
        getSharedPreferences("field_drafts",MODE_PRIVATE).edit().remove("draft_"+eventId).commit();
        active=false;eventId=null;clearMark();
        returnToHome=true;
        getSharedPreferences("field_resume",MODE_PRIVATE).edit().putString("screen","home").commit();
        beginButton.setText("Start survey");
        saveButton.setEnabled(false);methodSpinner.setEnabled(false);lineEntry.setEnabled(false);showGroups();status();
        toast(complete?"Survey completed":"Survey saved as incomplete");
        navigateHome();
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
        String raw=taxonEntry.getText().toString().trim();
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
        boolean candidate=group.equals("Birds")&&distance!=null&&!distanceMethod.equals("Unknown")
          &&firstPosition.equals("Yes")&&(method.equals("point_count")||method.equals("line_transect"))
          &&(!method.equals("line_transect")||perp!=null)
          &&(!method.equals("point_count")||pointRadiusM==null||distance<=pointRadiusM)
          &&!movement.equals("Flyover")&&!movement.equals("Entering after start");
        Location loc=location;
        Object lat=loc==null?null:loc.getLatitude(),lon=loc==null?null:loc.getLongitude(),acc=loc==null?null:loc.getAccuracy();
        long first=markedAt>0?markedAt:System.currentTimeMillis();
        db.getWritableDatabase().execSQL(
            "INSERT INTO field_records(event_id,detected_at,taxon_group,common_name,scientific_name,raw_identification,individual_count,detection_method,horizontal_m,bearing_magnetic,pitch_deg,perpendicular_m,observer_lat,observer_lon,gps_accuracy_m,habitat,behavior,notes,compass_quality,name_source,distance_method,initial_position,movement_status,distance_eligible,updated_at) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
            new Object[]{eventId,iso(first),group,id[0],id[1],raw,count,detectionSpinner.getSelectedItem().toString(),
                distance,markedBearing,markedPitch,perp,lat,lon,acc,
                habitatEntry.getText().toString().trim(),behaviorEntry.getText().toString().trim(),
                notesEntry.getText().toString().trim(),sensorQuality,raw.contains(" · ")?"offline_starter_v0.4":"user_entered",
                distanceMethod,firstPosition,movement,candidate?1:0,iso(System.currentTimeMillis())});
        taxonEntry.setText("");count=1;countView.setText("1");distanceEntry.setText("");
        habitatEntry.setText("");behaviorEntry.setText("");notesEntry.setText("");
        clearMark();
        distanceSourceSpinner.setSelection(0);firstPositionSpinner.setSelection(0);movementSpinner.setSelection(0);
        refreshAutocomplete();
        persistDraft();
        toast(distance==null?"Observation saved · distance not recorded":"Saved offline");
    }
    private void status(){
        if(eventView==null)return;
        if(active){
            long elapsed=Math.max(0,(System.currentTimeMillis()-startedAt)/1000);
            if(method.equals("point_count")){
                long left=pointTargetSeconds-elapsed;eventView.setText(left>0?"● "+group+" · Point count · "+(left/60)+":"+String.format(Locale.US,"%02d",left%60)+" remaining":
                     "● "+group+" · Point count target reached");
                if(left<=0&&!pointNotice){pointNotice=true;toast("10-minute point-count target reached");}
            }else eventView.setText("● "+group+" · "+method.replace('_',' ')+" · "+(elapsed/60)+" min");
        }else eventView.setText("Offline · Select taxon group and start survey");
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
              "SELECT id,method,started,ended,transect_az,taxon_group,point_target_s,effort_length_m,complete,survey_profile,station_id,observer_name,protocol_status,project_id,region_label,area_km2,point_radius_m,weather FROM surveys",
              "event_id,method,start_utc,end_utc,transect_magnetic_bearing_deg,initial_taxon_group,point_target_seconds,actual_route_length_m,completed,survey_profile,station_id,observer_name,protocol_status,project_id,region_label,stratum_area_km2,point_radius_m,weather");
            csv(z,"observations.csv",
              "SELECT id,event_id,detected_at,taxon_group,common_name,scientific_name,raw_identification,individual_count,detection_method,horizontal_m,bearing_magnetic,pitch_deg,perpendicular_m,observer_lat,observer_lon,gps_accuracy_m,habitat,behavior,notes,compass_quality,name_source,distance_method,initial_position,movement_status,distance_eligible,updated_at,deleted_at,legacy_source_id FROM field_records WHERE deleted_at IS NULL",
              "record_id,event_id,detected_at_utc,taxon_group,common_name,scientific_name,raw_identification,individual_count,detection_method,horizontal_distance_m,magnetic_bearing_deg,pitch_deg,perpendicular_distance_m,observer_lat,observer_lon,gps_accuracy_m,habitat,behavior,notes,compass_quality,name_source,distance_method,initial_position,movement_status,distance_candidate_flag,updated_at,deleted_at,legacy_source_id");
            csv(z,"distance_samples.csv",
              "SELECT id,COALESCE(station_id,''),method,COALESCE(region_label,''),area_km2,CASE WHEN method='point_count' THEN 1.0 WHEN method='line_transect' AND effort_length_m>0 THEN effort_length_m/1000.0 ELSE NULL END AS effort,effort_length_m,point_target_s,point_radius_m,complete FROM surveys WHERE survey_profile='Birds' AND method IN ('point_count','line_transect')",
              "Sample.Label,station_id,method,Region.Label,Area_km2,Effort_line_km_or_point_visits,line_length_m,point_target_seconds,point_radius_m,completed");
            DistanceExport.write(z,db.getReadableDatabase());
            csv(z,"distance_review.csv",
              "SELECT r.id,r.event_id,r.taxon_group,r.raw_identification,r.horizontal_m,r.perpendicular_m,r.individual_count,r.distance_method,r.initial_position,r.movement_status,r.distance_eligible,s.method,s.station_id,s.effort_length_m,s.point_target_s,s.region_label,s.area_km2,s.complete FROM field_records r LEFT JOIN surveys s ON s.id=r.event_id WHERE r.deleted_at IS NULL AND r.taxon_group='Birds' AND (s.method='point_count' OR s.method='line_transect')",
              "record_id,event_id,taxon_group,taxon,horizontal_m,perpendicular_m,group_size,distance_method,initial_position,movement_status,candidate_flag,survey_method,station_id,effort_length_m,point_target_s,region_label,area_km2,completed");
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
        for(EditText e:new EditText[]{taxonEntry,distanceEntry,lineEntry,habitatEntry,behaviorEntry,notesEntry}){
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
            d.put("habitat",habitatEntry.getText().toString());
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
    @Override public void onLocationChanged(Location l){
        location=l;
        if(gpsView!=null)gpsView.setText(String.format(Locale.US,"Observer GPS: %.5f, %.5f · ±%.0f m",l.getLatitude(),l.getLongitude(),l.getAccuracy()));
    }
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}

}
