package org.phwildlife.survey;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.content.Intent;
import android.content.pm.PackageManager;
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
public class MainActivity extends Activity implements SensorEventListener, SurfaceHolder.Callback, LocationListener {
    private final Handler ui = new Handler(Looper.getMainLooper());
    private SurveyDb db;
    private SensorManager sensors;
    private LocationManager locations;
    private SurfaceView cameraSurface;
    private Camera camera;
    private TextView bearingView, gpsView, surveyView, hintView;
    private EditText speciesInput, countInput, distanceInput, transectInput;
    private Spinner modeInput, detectionInput;
    private Button surveyButton, captureButton, saveButton;
    private float heading = Float.NaN, pitch = Float.NaN;
    private Double fixedBearing = null, fixedPitch = null;
    private Location latestLocation;
    private String surveyId = null;
    private long startEpoch = 0;
    private boolean active = false;
    private int bg = Color.rgb(244,248,244), dark = Color.rgb(26,60,46), accent = Color.rgb(31,105,72);
    private static final int EXPORT_DOC = 22;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        db = new SurveyDb(this);
        sensors = (SensorManager)getSystemService(SENSOR_SERVICE);
        locations = (LocationManager)getSystemService(LOCATION_SERVICE);
        buildUi();
        cameraSurface.getHolder().addCallback(this);
        if(checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},42);
        }
        tick();
    }
    private TextView text(String value, int size) {
        TextView t=new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(dark);
        t.setPadding(2,5,2,5);
        return t;
    }
    private EditText edit(String hint,String initial,int inputType) {
        EditText e=new EditText(this);
        e.setSingleLine(true); e.setHint(hint); e.setText(initial);
        e.setTextColor(dark); e.setTextSize(16);
        e.setInputType(inputType);
        return e;
    }
    private Button btn(String label) {
        Button b=new Button(this);b.setText(label);b.setAllCaps(false);
        b.setTextColor(Color.WHITE);b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(accent));
        return b;
    }
    private void buildUi() {
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(bg);root.setPadding(14,10,14,8);
        TextView title=text("Philippine Wildlife Survey",20);title.setTypeface(null,Typeface.BOLD);
        root.addView(title);
        surveyView=text("No active survey",13);root.addView(surveyView);
        cameraSurface=new SurfaceView(this);
        root.addView(cameraSurface,new LinearLayout.LayoutParams(-1,0,3f));
        bearingView=text("Camera bearing: --°  |  pitch: --°",19);
        bearingView.setTypeface(null,Typeface.BOLD);bearingView.setGravity(Gravity.CENTER);
        root.addView(bearingView);
        gpsView=text("GPS: waiting for fix",12);root.addView(gpsView);
        LinearLayout surveyRow=new LinearLayout(this);
        modeInput=new Spinner(this);
        modeInput.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"Point count","Line transect"}));
        surveyRow.addView(modeInput,new LinearLayout.LayoutParams(0,-2,1));
        transectInput=edit("Transect bearing °","",2);
        surveyRow.addView(transectInput,new LinearLayout.LayoutParams(0,-2,1));
        root.addView(surveyRow);
        surveyButton=btn("Start survey");root.addView(surveyButton);
        captureButton=btn("Capture bearing");
        root.addView(captureButton);
        LinearLayout entryRow=new LinearLayout(this);entryRow.setOrientation(LinearLayout.HORIZONTAL);
        speciesInput=edit("Bird species / identification","",1);
        entryRow.addView(speciesInput,new LinearLayout.LayoutParams(0,-2,2f));
        countInput=edit("Count","1",2);
        entryRow.addView(countInput,new LinearLayout.LayoutParams(0,-2,0.65f));
        root.addView(entryRow);
        LinearLayout details=new LinearLayout(this);
        distanceInput=edit("Horizontal distance (m)","",8194);
        details.addView(distanceInput,new LinearLayout.LayoutParams(0,-2,1f));
        detectionInput=new Spinner(this);
        detectionInput.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"Visual","Auditory","Both"}));
        details.addView(detectionInput,new LinearLayout.LayoutParams(0,-2,1f));
        root.addView(details);
        saveButton=btn("Save bird");root.addView(saveButton);
        Button export=btn("Export records");root.addView(export);
        hintView=text("Estimate horizontal distance. Bearing is optional for sound-only records. Coordinates are GPS-based when a fix is available.",11);
        root.addView(hintView);
        setContentView(root);
        saveButton.setEnabled(false);
        surveyButton.setOnClickListener(v-> toggleSurvey());
        captureButton.setOnClickListener(v-> {
            if(!active){ toast("Start a survey first");return; }
            if(Float.isNaN(heading)){toast("Camera orientation unavailable: bearing omitted");return;}
            fixedBearing=(double)heading; fixedPitch=(double)pitch;
            captureButton.setText(String.format(Locale.US,"Captured %.0f° · recapture",fixedBearing));
        });
        saveButton.setOnClickListener(v->saveBird());
        export.setOnClickListener(v->exportRecords());
        modeInput.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){
            public void onNothingSelected(android.widget.AdapterView<?> p){}
            public void onItemSelected(android.widget.AdapterView<?> p,View v,int pos,long id){
                transectInput.setVisibility(pos==1?View.VISIBLE:View.GONE);
            }
        });
    }
    private void toggleSurvey(){
        if(!active) {
            surveyId="SV-"+System.currentTimeMillis();
            startEpoch=System.currentTimeMillis();
            String mode=modeInput.getSelectedItemPosition()==0?"point":"line";
            Double line=number(transectInput.getText().toString());
            if(mode.equals("line") && (line==null || line<0 || line>=360)) {toast("Enter transect magnetic bearing (0 to 359.9°)");return;}
            db.getWritableDatabase().execSQL("INSERT INTO surveys(id,method,started,transect_az) VALUES(?,?,?,?)",new Object[]{surveyId,mode,iso(startEpoch),line});
            active=true;modeInput.setEnabled(false);transectInput.setEnabled(false);
            surveyButton.setText("Finish survey");saveButton.setEnabled(true);
        } else {
            db.getWritableDatabase().execSQL("UPDATE surveys SET ended=? WHERE id=?",new Object[]{iso(System.currentTimeMillis()),surveyId});
            active=false; surveyButton.setText("Start survey");
            modeInput.setEnabled(true);transectInput.setEnabled(true);saveButton.setEnabled(false);
            fixedBearing=null;fixedPitch=null;captureButton.setText("Capture bearing");
            toast("Survey saved, including surveys with no bird detections");
        }
        updateSurveyStatus();
    }
    private void updateSurveyStatus() {
        if(active) surveyView.setText("Survey active · "+(modeInput.getSelectedItemPosition()==0?"Point":"Line")+" · "+((System.currentTimeMillis()-startEpoch)/60000)+" min");
        else surveyView.setText("No active survey · records are stored offline");
    }
    private static Double number(String s) {
        try { return Double.valueOf(s.trim()); } catch(Exception e){return null;}
    }
    private void saveBird() {
        if(!active){toast("Start a survey");return;}
        String taxon=speciesInput.getText().toString().trim();
        if(taxon.isEmpty()){toast("Enter a bird identification");return;}
        Double countD=number(countInput.getText().toString());
        if(countD==null || countD<1 || countD != Math.floor(countD)){toast("Enter a positive whole-number count");return;}
        Double distance=number(distanceInput.getText().toString());
        if(distance!=null && (distance<0 || distance>10000)) {toast("Check the horizontal distance");return;}
        Double az=fixedBearing, inc=fixedPitch;
        Double perp=null;
        if(modeInput.getSelectedItemPosition()==1 && distance!=null && az!=null) {
            Double line=number(transectInput.getText().toString());
            if(line!=null) perp=Geometry.perpendicular(distance,az,line);
        }
        Location loc=latestLocation;
        Object lat=loc==null?null:loc.getLatitude(),lon=loc==null?null:loc.getLongitude(),accuracy=loc==null?null:loc.getAccuracy();
        db.getWritableDatabase().execSQL("INSERT INTO sightings(event_id,seen_at,taxon,individual_count,detection,horizontal_m,bearing_magnetic,pitch_deg,perpendicular_m,lat,lon,gps_accuracy_m) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)",
             new Object[]{surveyId,iso(System.currentTimeMillis()),taxon,countD.intValue(),detectionInput.getSelectedItem().toString(),distance,az,inc,perp,lat,lon,accuracy});
        speciesInput.setText("");countInput.setText("1");distanceInput.setText("");
        fixedBearing=null;fixedPitch=null;captureButton.setText("Capture bearing");
        toast(distance==null?"Saved as biodiversity observation (distance missing)":"Bird saved offline");
    }
    private void exportRecords() {
        Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("application/zip");
        i.putExtra(Intent.EXTRA_TITLE,"philippine-wildlife-survey.zip");
        startActivityForResult(i,EXPORT_DOC);
    }
    @Override protected void onActivityResult(int req,int result,Intent data) {
        super.onActivityResult(req,result,data);
        if(req!=EXPORT_DOC || result!=RESULT_OK || data==null)return;
        Uri uri=data.getData();if(uri==null)return;
        try(OutputStream os=getContentResolver().openOutputStream(uri);ZipOutputStream zip=new ZipOutputStream(os)){
            writeCsv(zip,"surveys.csv","SELECT id,method,started,ended,transect_az FROM surveys",
                new String[]{"id","method","started_utc","ended_utc","transect_magnetic_azimuth_degrees"});
            writeCsv(zip,"observations.csv","SELECT event_id,seen_at,taxon,individual_count,detection,horizontal_m,bearing_magnetic,pitch_deg,perpendicular_m,lat,lon,gps_accuracy_m FROM sightings",
                new String[]{"event_id","timestamp_utc","taxon","individual_count","detection_method","horizontal_distance_m","magnetic_bearing_degrees","pitch_degrees","perpendicular_distance_m","observer_lat","observer_lon","gps_accuracy_m"});
            toast("Survey ZIP exported");
        } catch(Exception e){toast("Export failed: "+e.getMessage());}
    }
    private void writeCsv(ZipOutputStream zip,String filename,String sql,String[] headers)throws Exception {
        zip.putNextEntry(new ZipEntry(filename));
        zip.write((String.join(",",headers)+"\n").getBytes(StandardCharsets.UTF_8));
        try(Cursor c=db.getReadableDatabase().rawQuery(sql,null)){
            while(c.moveToNext()){
                StringBuilder line=new StringBuilder();
                for(int j=0;j<c.getColumnCount();j++) {
                    if(j>0)line.append(',');
                    if(!c.isNull(j)){
                        String v=c.getString(j);
                        line.append('"').append(v.replace("\"","\"\"")).append('"');
                    }
                }
                line.append('\n');zip.write(line.toString().getBytes(StandardCharsets.UTF_8));
            }
        }
        zip.closeEntry();
    }
    private void tick(){updateSurveyStatus();ui.postDelayed(this::tick,1000);}
    @Override protected void onResume(){
        super.onResume();
        Sensor s=sensors.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        if(s!=null)sensors.registerListener(this,s,SensorManager.SENSOR_DELAY_UI);
        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
            try{ locations.requestLocationUpdates(LocationManager.GPS_PROVIDER,2000,1f,this); }catch(Exception ignored){}
        }
        startCamera();
    }
    @Override protected void onPause(){super.onPause();sensors.unregisterListener(this);locations.removeUpdates(this);stopCamera();}
    @Override protected void onDestroy(){super.onDestroy();ui.removeCallbacksAndMessages(null);db.close();}
    @Override public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType()!=Sensor.TYPE_ROTATION_VECTOR)return;
        float[] r=new float[9];SensorManager.getRotationMatrixFromVector(r,event.values);
        float[] hp=Geometry.cameraAxisHeadingAndPitch(r);heading=hp[0];pitch=hp[1];
        bearingView.setText(Float.isNaN(heading)?"Camera bearing unavailable (aim closer to horizon)":String.format(Locale.US,"Magnetic %.0f°   ·   Pitch %+.0f°",heading,pitch));
    }
    @Override public void onAccuracyChanged(Sensor s,int accuracy) {
        if(s!=null && s.getType()==Sensor.TYPE_ROTATION_VECTOR && accuracy==SensorManager.SENSOR_STATUS_UNRELIABLE)
            hintView.setText("Compass unreliable. Move away from metal or electronics; record manually if necessary.");
    }
    @Override public void onLocationChanged(Location l) {
        latestLocation=l;
        gpsView.setText(String.format(Locale.US,"GPS: %.5f, %.5f  ±%.0f m",l.getLatitude(),l.getLongitude(),l.getAccuracy()));
    }
    private void startCamera(){
        if(camera!=null || cameraSurface==null || !cameraSurface.getHolder().getSurface().isValid())return;
        if(checkSelfPermission(Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED)return;
        try{
            camera=Camera.open();
            camera.setDisplayOrientation(90);
            camera.setPreviewDisplay(cameraSurface.getHolder());
            camera.startPreview();
        }catch(Exception e){stopCamera();gpsView.setText("Camera unavailable; survey recording still works");}
    }
    private void stopCamera(){if(camera!=null){try{camera.stopPreview();}catch(Exception ignored){}camera.release();camera=null;}}
    @Override public void surfaceCreated(SurfaceHolder h){startCamera();}
    @Override public void surfaceChanged(SurfaceHolder h,int format,int width,int height){startCamera();}
    @Override public void surfaceDestroyed(SurfaceHolder h){stopCamera();}
    @Override public void onRequestPermissionsResult(int code,String[] perms,int[] results){super.onRequestPermissionsResult(code,perms,results);startCamera();}
    private void toast(String msg){Toast.makeText(this,msg,Toast.LENGTH_LONG).show();}
    private static String iso(long epoch){
        SimpleDateFormat d=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.US);
        d.setTimeZone(TimeZone.getTimeZone("UTC"));return d.format(new Date(epoch));
    }
    static class SurveyDb extends SQLiteOpenHelper {
        SurveyDb(Activity a){super(a,"wildlife_field_v03.db",null,1);}
        @Override public void onCreate(SQLiteDatabase d) {
            d.execSQL("CREATE TABLE surveys(id TEXT PRIMARY KEY,method TEXT,started TEXT,ended TEXT,transect_az REAL)");
            d.execSQL("CREATE TABLE sightings(id INTEGER PRIMARY KEY AUTOINCREMENT,event_id TEXT,seen_at TEXT,taxon TEXT,individual_count INTEGER,detection TEXT,horizontal_m REAL,bearing_magnetic REAL,pitch_deg REAL,perpendicular_m REAL,lat REAL,lon REAL,gps_accuracy_m REAL)");
        }
        @Override public void onUpgrade(SQLiteDatabase d,int a,int b){}
    }
}
