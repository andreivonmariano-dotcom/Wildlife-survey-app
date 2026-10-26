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
    private final int BACK=Color.rgb(247,249,246),INK=Color.rgb(22,51,38),GREEN=Color.rgb(37,104,74),MUTED=Color.rgb(95,116,101);
    private SurveyDb db; private LocationManager locations;
    private TextView bearingStatusView,gpsView,eventView,countView,hintView,detailToggle;
    private Spinner methodSpinner,detectionSpinner,substrateSpinner;
    private AutoCompleteTextView taxonEntry;
    private EditText distanceEntry,lineEntry,notesEntry,habitatEntry,behaviorEntry;
    private Button beginButton,markButton,saveButton;
    private LinearLayout groupBar,advancedPanel,lineRow;
    private final String[] groups=Taxa.GROUPS;
    private String group="Birds",eventId=null,method="point";
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
        db=new SurveyDb();
        locations=(LocationManager)getSystemService(LOCATION_SERVICE);
        restoreEvent();
        draw();
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
        TextView title=label("Wildlife Survey",22,true);top.addView(title,new LinearLayout.LayoutParams(0,-2,1));
        Button export=button("Export");top.addView(export);export.setOnClickListener(v->exportZip());
        root.addView(top);
        eventView=label("Offline · ready",12,false);eventView.setTextColor(MUTED);root.addView(eventView);
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(false);scroll.setVerticalScrollBarEnabled(false);
        LinearLayout content=vertical();content.setPadding(dp(1),0,dp(1),dp(12));
        groupBar=horizontal();content.addView(groupBar);showGroups();
        LinearLayout methodRow=horizontal();
        methodSpinner=new Spinner(this);methodRow.addView(methodSpinner,weighted(1f));
        lineRow=horizontal();lineEntry=edit("Transect magnetic bearing °",InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        lineRow.addView(lineEntry,new LinearLayout.LayoutParams(-1,-2));
        methodRow.addView(lineRow,weighted(0.9f));
        content.addView(methodRow);
        beginButton=button(active?"Finish survey":"Start survey");
        content.addView(beginButton,new LinearLayout.LayoutParams(-1,dp(47)));
        beginButton.setOnClickListener(v->toggleEvent());
        content.addView(section("DETECTION"));
        LinearLayout detectRow=horizontal();
        Button markTime=button("Mark now");
        detectRow.addView(markTime,weighted(0.9f));
        markButton=button("Open camera");
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
        minus.setOnClickListener(v->{count=Math.max(1,count-1);countView.setText(""+count);});
        plus.setOnClickListener(v->{count=Math.min(9999,count+1);countView.setText(""+count);});
        LinearLayout row=horizontal();
        distanceEntry=edit("Horizontal distance (m)",InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        row.addView(distanceEntry,weighted(1.2f));
        detectionSpinner=new Spinner(this);
        detectionSpinner.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"Visual","Auditory","Both","Sign","Camera"}));
        row.addView(detectionSpinner,weighted(0.8f));content.addView(row);
        detailToggle=label("＋ Ecology, habitat and notes (optional)",14,true);
        detailToggle.setTextColor(GREEN);content.addView(detailToggle);
        advancedPanel=vertical();
        habitatEntry=edit("Habitat / microhabitat",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        behaviorEntry=edit("Behavior / reproductive evidence",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        notesEntry=edit("Observation notes",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        notesEntry.setSingleLine(false);notesEntry.setMinLines(2);
        advancedPanel.addView(habitatEntry);advancedPanel.addView(behaviorEntry);advancedPanel.addView(notesEntry);
        advancedPanel.setVisibility(View.GONE);content.addView(advancedPanel);
        detailToggle.setOnClickListener(v->{detailShown=!detailShown;advancedPanel.setVisibility(detailShown?View.VISIBLE:View.GONE);
            detailToggle.setText(detailShown?"− Hide ecology details":"＋ Ecology, habitat and notes (optional)");});
        hintView=label("Tip: Mark detection records the time and optional direction. Distance is entered manually.",12,false);
        hintView.setTextColor(MUTED);content.addView(hintView);
        scroll.addView(content);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        saveButton=button("Save observation");
        root.addView(saveButton,new LinearLayout.LayoutParams(-1,dp(55)));
        saveButton.setEnabled(active);
        saveButton.setOnClickListener(v->save());
        setContentView(root);
        updateMethods();refreshAutocomplete();status();
    }
    private void showGroups(){
        groupBar.removeAllViews();
        for(String name:groups){
            Button b=button(name);
            b.setTextSize(11);b.setPadding(0,0,0,0);
            b.setBackgroundTintList(ColorStateList.valueOf(name.equals(group)?GREEN:MUTED));
            b.setEnabled(!active || name.equals(group));
            groupBar.addView(b,new LinearLayout.LayoutParams(0,dp(45),1));
            b.setOnClickListener(v->{if(active)return;group=name;showGroups();updateMethods();refreshAutocomplete();});
        }
    }
    private void updateMethods(){
        String[] methods=group.equals("Birds")?new String[]{"Point count","Line transect","Opportunistic"}:new String[]{"Visual encounter","Opportunistic"};
        methodSpinner.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,methods));
        int idx=0;
        for(int i=0;i<methods.length;i++)if(methods[i].equalsIgnoreCase(method.replace('_',' ')))idx=i;
        methodSpinner.setSelection(idx);
        methodSpinner.setEnabled(!active);
        methodSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            public void onNothingSelected(android.widget.AdapterView<?> a){}
            public void onItemSelected(android.widget.AdapterView<?> a,View v,int pos,long id){
                lineRow.setVisibility(group.equals("Birds") && pos==1?View.VISIBLE:View.GONE);
            }
        });
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
    }
    private void restoreEvent(){
        try(Cursor c=db.getReadableDatabase().rawQuery("SELECT id,method,started,COALESCE(taxon_group,'Birds') FROM surveys WHERE ended IS NULL ORDER BY started DESC LIMIT 1",null)){
            if(c.moveToFirst()){
                eventId=c.getString(0);method=c.getString(1);String time=c.getString(2);
                group=c.getString(3);startedAt=parseIso(time);active=true;
            }
        }catch(Exception ignored){}
    }
    private void toggleEvent(){
        if(active){finishPrompt();return;}
        String selected=methodSpinner.getSelectedItem().toString();
        method=selected.toLowerCase(Locale.US).replace(' ','_');
        Double line=number(lineEntry.getText().toString());
        if(method.equals("line_transect") && (line==null||line<0||line>=360)){
            toast("Enter transect MAGNETIC bearing from 0 to 359°");return;
        }
        eventId="SV-"+System.currentTimeMillis();startedAt=System.currentTimeMillis();pointNotice=false;
        int target=method.equals("point_count")?600:0;
        db.getWritableDatabase().execSQL("INSERT INTO surveys(id,method,started,transect_az,taxon_group,point_target_s) VALUES(?,?,?,?,?,?)",
          new Object[]{eventId,method,iso(startedAt),method.equals("line_transect")?line:null,group,target});
        active=true;beginButton.setText("Finish survey");saveButton.setEnabled(true);
        methodSpinner.setEnabled(false);lineEntry.setEnabled(false);showGroups();status();
        toast("Survey started");
    }
    private void finishPrompt(){
        if(method.equals("line_transect")||method.equals("visual_encounter")){
            EditText effort=edit("Actual surveyed length (meters)",InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
            new AlertDialog.Builder(this).setTitle("Finish survey").setMessage("Enter actual surveyed route length if known. Leave blank if not measured.")
            .setView(effort).setNegativeButton("Cancel",null)
            .setNeutralButton("Incomplete",(d,w)->finish(false,number(effort.getText().toString())))
            .setPositiveButton("Completed",(d,w)->finish(true,number(effort.getText().toString()))).show();
        } else new AlertDialog.Builder(this).setTitle("Finish survey")
            .setMessage("Completed surveys with zero detections will remain in your event dataset.")
            .setNegativeButton("Cancel",null).setNeutralButton("Incomplete",(d,w)->finish(false,null))
            .setPositiveButton("Completed",(d,w)->finish(true,null)).show();
    }
    private void finish(boolean complete,Double length){
        db.getWritableDatabase().execSQL("UPDATE surveys SET ended=?,effort_length_m=?,complete=? WHERE id=?",
          new Object[]{iso(System.currentTimeMillis()),length,complete?1:0,eventId});
        active=false;eventId=null;clearMark();
        beginButton.setText("Start survey");
        saveButton.setEnabled(false);methodSpinner.setEnabled(true);lineEntry.setEnabled(true);showGroups();status();
        toast(complete?"Survey completed, including any nondetections":"Survey saved as incomplete");
    }
    private void markDetection(){
        if(!active){toast("Start a survey first");return;}
        markedAt=System.currentTimeMillis();
        showMarkStatus();
    }
    private void openCamera(){
        if(!active){toast("Start survey first");return;}
        Intent intent=new Intent(this,CameraActivity.class);
        startActivityForResult(intent,CAMERA_REQ);
    }
    private void clearMark(){
        markedAt=0;markedBearing=null;markedPitch=null;
        if(bearingStatusView!=null)bearingStatusView.setText("Time and bearing not marked");
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
        Double perp=null,line=number(lineEntry.getText().toString());
        if(group.equals("Birds")&&method.equals("line_transect")&&markedBearing!=null&&distance!=null&&line!=null)
            perp=Geometry.perpendicular(distance,markedBearing,line);
        Location loc=location;
        Object lat=loc==null?null:loc.getLatitude(),lon=loc==null?null:loc.getLongitude(),acc=loc==null?null:loc.getAccuracy();
        long first=markedAt>0?markedAt:System.currentTimeMillis();
        db.getWritableDatabase().execSQL(
            "INSERT INTO field_records(event_id,detected_at,taxon_group,common_name,scientific_name,raw_identification,individual_count,detection_method,horizontal_m,bearing_magnetic,pitch_deg,perpendicular_m,observer_lat,observer_lon,gps_accuracy_m,habitat,behavior,notes,compass_quality,name_source) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
            new Object[]{eventId,iso(first),group,id[0],id[1],raw,count,detectionSpinner.getSelectedItem().toString(),
                distance,markedBearing,markedPitch,perp,lat,lon,acc,
                habitatEntry.getText().toString().trim(),behaviorEntry.getText().toString().trim(),
                notesEntry.getText().toString().trim(),sensorQuality,raw.contains(" · ")?"offline_starter_v0.4":"user_entered"});
        taxonEntry.setText("");count=1;countView.setText("1");distanceEntry.setText("");
        habitatEntry.setText("");behaviorEntry.setText("");notesEntry.setText("");
        clearMark();
        refreshAutocomplete();
        toast(distance==null?"Observation saved · distance not recorded":"Saved offline");
    }
    private void status(){
        if(eventView==null)return;
        if(active){
            long elapsed=Math.max(0,(System.currentTimeMillis()-startedAt)/1000);
            if(method.equals("point_count")){
                long left=600-elapsed;eventView.setText(left>0?"● "+group+" · Point count · "+(left/60)+":"+String.format(Locale.US,"%02d",left%60)+" remaining":
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
                showMarkStatus();
            }
            return;
        }
        if(req!=EXPORT_DOC||result!=RESULT_OK||data==null)return;
        try(OutputStream os=getContentResolver().openOutputStream(data.getData());ZipOutputStream z=new ZipOutputStream(os)){
            csv(z,"surveys.csv",
              "SELECT id,method,started,ended,transect_az,taxon_group,point_target_s,effort_length_m,complete FROM surveys",
              "event_id,method,start_utc,end_utc,transect_magnetic_bearing_deg,taxon_group,point_target_seconds,actual_route_length_m,completed");
            csv(z,"observations.csv",
              "SELECT event_id,detected_at,taxon_group,common_name,scientific_name,raw_identification,individual_count,detection_method,horizontal_m,bearing_magnetic,pitch_deg,perpendicular_m,observer_lat,observer_lon,gps_accuracy_m,habitat,behavior,notes,compass_quality,name_source FROM field_records "+
              "UNION ALL SELECT event_id,seen_at,'Birds',NULL,NULL,taxon,individual_count,detection,horizontal_m,bearing_magnetic,pitch_deg,perpendicular_m,lat,lon,gps_accuracy_m,NULL,NULL,NULL,NULL,NULL FROM sightings",
              "event_id,detected_at_utc,taxon_group,common_name,scientific_name,raw_identification,individual_count,detection_method,horizontal_distance_m,magnetic_bearing_deg,pitch_deg,perpendicular_distance_m,observer_lat,observer_lon,gps_accuracy_m,habitat,behavior,notes,compass_quality,name_source");
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
    @Override protected void onResume(){
        super.onResume();
        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
            try{locations.requestLocationUpdates(LocationManager.GPS_PROVIDER,2000,1f,this);}catch(Exception ignored){}
        }
    }
    @Override protected void onPause(){
        super.onPause();
        try{locations.removeUpdates(this);}catch(Exception ignored){}
    }
    @Override protected void onDestroy(){super.onDestroy();handler.removeCallbacksAndMessages(null);db.close();}
    @Override public void onLocationChanged(Location l){
        location=l;
        if(gpsView!=null)gpsView.setText(String.format(Locale.US,"Observer GPS: %.5f, %.5f · ±%.0f m",l.getLatitude(),l.getLongitude(),l.getAccuracy()));
    }
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}

    final class SurveyDb extends SQLiteOpenHelper {
        SurveyDb(){super(MainActivity.this,"wildlife_field_v03.db",null,2);}
        @Override public void onCreate(SQLiteDatabase d){
            d.execSQL("CREATE TABLE surveys(id TEXT PRIMARY KEY,method TEXT,started TEXT,ended TEXT,transect_az REAL,taxon_group TEXT DEFAULT 'Birds',point_target_s INTEGER,effort_length_m REAL,complete INTEGER)");
            d.execSQL("CREATE TABLE sightings(id INTEGER PRIMARY KEY AUTOINCREMENT,event_id TEXT,seen_at TEXT,taxon TEXT,individual_count INTEGER,detection TEXT,horizontal_m REAL,bearing_magnetic REAL,pitch_deg REAL,perpendicular_m REAL,lat REAL,lon REAL,gps_accuracy_m REAL)");
            createRecords(d);
        }
        private void createRecords(SQLiteDatabase d){
            d.execSQL("CREATE TABLE IF NOT EXISTS field_records(id INTEGER PRIMARY KEY AUTOINCREMENT,event_id TEXT,detected_at TEXT,taxon_group TEXT,common_name TEXT,scientific_name TEXT,raw_identification TEXT,individual_count INTEGER,detection_method TEXT,horizontal_m REAL,bearing_magnetic REAL,pitch_deg REAL,perpendicular_m REAL,observer_lat REAL,observer_lon REAL,gps_accuracy_m REAL,habitat TEXT,behavior TEXT,notes TEXT,compass_quality INTEGER,name_source TEXT)");
            d.execSQL("CREATE INDEX IF NOT EXISTS record_event_idx ON field_records(event_id)");
        }
        @Override public void onUpgrade(SQLiteDatabase d,int from,int to){
            if(from<2){
                d.execSQL("ALTER TABLE surveys ADD COLUMN taxon_group TEXT DEFAULT 'Birds'");
                d.execSQL("ALTER TABLE surveys ADD COLUMN point_target_s INTEGER");
                d.execSQL("ALTER TABLE surveys ADD COLUMN effort_length_m REAL");
                d.execSQL("ALTER TABLE surveys ADD COLUMN complete INTEGER");
                createRecords(d);
            }
        }
    }
}
