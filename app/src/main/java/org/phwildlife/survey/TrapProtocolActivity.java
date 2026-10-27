package org.phwildlife.survey;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.text.InputType;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

/** Device-by-device field audit for cameras, pitfall fences and live traps. */
public final class TrapProtocolActivity extends Activity {
    private FieldDb db;
    private LinearLayout rows;
    private String eventId,method="",station="";
    private int dp(int x){return Math.round(getResources().getDisplayMetrics().density*x);}
    private LinearLayout column(){
        LinearLayout v=new LinearLayout(this);v.setOrientation(LinearLayout.VERTICAL);return v;
    }
    private TextView label(String value,int sp){
        TextView t=new TextView(this);t.setText(value);t.setTextSize(sp);
        t.setTextColor(UiKit.INK);t.setPadding(dp(5),dp(8),dp(5),dp(8));return t;
    }
    private EditText field(String hint,boolean numeric){
        EditText x=new EditText(this);x.setHint(hint);x.setTextSize(15);
        if(numeric)x.setInputType(InputType.TYPE_CLASS_NUMBER|
            InputType.TYPE_NUMBER_FLAG_DECIMAL);
        return x;
    }
    private void toast(String message){
        android.widget.Toast.makeText(this,message,android.widget.Toast.LENGTH_LONG).show();
    }
    private static String now(){
        SimpleDateFormat f=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.US);
        f.setTimeZone(TimeZone.getTimeZone("UTC"));return f.format(new Date());
    }
    private boolean writable(){
        if(eventId==null)return false;
        try(Cursor c=db.getReadableDatabase().rawQuery(
            "SELECT ended FROM surveys WHERE id=?",new String[]{eventId})){
            return c.moveToFirst()&&c.isNull(0);
        }
    }
    @Override public void onCreate(Bundle state){
        super.onCreate(state);db=new FieldDb(this);
        eventId=getIntent().getStringExtra("event_id");
        station=getIntent().getStringExtra("station_id");
        if(station==null)station="";
        if(eventId!=null){
            try(Cursor c=db.getReadableDatabase().rawQuery(
                "SELECT method FROM surveys WHERE id=?",new String[]{eventId})){
                if(c.moveToFirst())method=c.getString(0);
            }
        }
        draw();
    }
    private void draw(){
        LinearLayout screen=column();screen.setPadding(dp(14),dp(9),dp(14),dp(12));
        screen.setBackgroundColor(UiKit.BG);
        Button back=UiKit.button(this,"‹ Back","secondary");
        back.setOnClickListener(v->finish());
        screen.addView(back,new LinearLayout.LayoutParams(dp(112),dp(48)));
        screen.addView(label("Trap and camera records",22));
        ScrollView scroll=new ScrollView(this);
        rows=column();scroll.addView(rows);
        screen.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        setContentView(screen);
        if(eventId==null){
            rows.addView(label("Recorded device surveys",16));
            try(Cursor c=db.getReadableDatabase().rawQuery(
                "SELECT DISTINCT d.event_id,s.method,s.started "+
                "FROM trap_deployments d JOIN surveys s ON s.id=d.event_id "+
                "ORDER BY s.started DESC LIMIT 100",null)){
                while(c.moveToNext()){
                    String id=c.getString(0);
                    Button entry=UiKit.button(this,c.getString(1)+" · "+c.getString(2),"secondary");
                    rows.addView(entry,new LinearLayout.LayoutParams(-1,dp(65)));
                    entry.setOnClickListener(v->{eventId=id;method=cMethod(id);draw();});
                }
            }
            return;
        }
        if(!TrapProtocolPolicy.supported(method)){
            rows.addView(label("This survey has no camera/pitfall/live-trap device protocol.",14));
            return;
        }
        rows.addView(label("Survey "+eventId+" · "+SurveyMethods.lookup(
            "drift_fence_check".equals(method)?"Herpetofauna":"Mammals",method).label,13));
        if(writable()){
            Button create=UiKit.button(this,"＋ Register device deployment","primary");
            rows.addView(create,new LinearLayout.LayoutParams(-1,dp(55)));
            create.setOnClickListener(v->addDeployment());
        }else rows.addView(label("Archived record · read only after survey closure",13));
        try(Cursor c=db.getReadableDatabase().rawQuery(
            "SELECT d.id,d.device_id,d.deployed_at_utc,d.retrieved_at_utc,d.status,"+
            "d.station_id,COALESCE(d.fence_length_m,0),COALESCE(d.setup_notes,''),"+
            "(SELECT COUNT(*) FROM trap_checks x WHERE x.deployment_id=d.id) "+
            "FROM trap_deployments d WHERE d.event_id=? ORDER BY d.deployed_at_utc",
            new String[]{eventId})){
            if(c.getCount()==0)rows.addView(label("No devices registered yet.",14));
            while(c.moveToNext()){
                String id=c.getString(0),device=c.getString(1),
                    deployed=c.getString(2),retrieved=c.getString(3),status=c.getString(4);
                LinearLayout card=column();
                card.setBackground(UiKit.surface(this));
                card.setPadding(dp(12),dp(9),dp(12),dp(12));
                card.addView(label(device+" · "+status,17));
                card.addView(label("Station "+c.getString(5)+" · installed "+deployed+
                    (retrieved==null?"":" · retrieved "+retrieved)+
                    "\nChecks: "+c.getInt(8)+" · "+c.getString(7),12));
                if(writable()&&retrieved==null){
                    Button check=UiKit.button(this,"Record check","secondary");
                    Button retire=UiKit.button(this,"Retrieve / close device","secondary");
                    card.addView(check,new LinearLayout.LayoutParams(-1,dp(48)));
                    card.addView(retire,new LinearLayout.LayoutParams(-1,dp(48)));
                    check.setOnClickListener(v->recordCheck(id,device));
                    retire.setOnClickListener(v->new AlertDialog.Builder(this)
                        .setTitle("Close device "+device+"?")
                        .setMessage("Record retrieval at the current UTC time. Checks remain archived.")
                        .setNegativeButton("Back",null)
                        .setPositiveButton("Retrieve",(dlg,which)->{
                            ContentValues update=new ContentValues();
                            update.put("retrieved_at_utc",now());update.put("status","retrieved");
                            db.getWritableDatabase().update("trap_deployments",update,
                                "id=? AND retrieved_at_utc IS NULL",new String[]{id});
                            draw();
                        }).show());
                }
                showChecks(card,id,device);
                LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
                lp.bottomMargin=dp(11);rows.addView(card,lp);
            }
        }
    }
    private String cMethod(String event){
        try(Cursor c=db.getReadableDatabase().rawQuery(
            "SELECT method FROM surveys WHERE id=?",new String[]{event})){
            return c.moveToFirst()?c.getString(0):"";
        }
    }
    private void addDeployment(){
        LinearLayout form=column();form.setPadding(dp(14),dp(6),dp(14),dp(6));
        EditText device=field("Unique camera/trap identifier",false);
        EditText length=field("Pitfall fence length in m (if relevant)",true);
        EditText notes=field("Equipment type, layout, bait and setup notes",false);
        form.addView(device);
        if("drift_fence_check".equals(method))form.addView(length);
        form.addView(notes);
        AlertDialog dialog=new AlertDialog.Builder(this)
            .setTitle("Register deployment").setView(form)
            .setNegativeButton("Cancel",null).setPositiveButton("Deploy",null).create();
        dialog.setOnShowListener(v->dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            .setOnClickListener(w->{
                String key=device.getText().toString().trim();
                if(key.isEmpty()){device.setError("Device ID required");return;}
                Double meters=parse(length.getText().toString());
                if(!length.getText().toString().trim().isEmpty()&&
                    (meters==null||meters<=0)){length.setError("Positive fence length required");return;}
                ContentValues row=new ContentValues();
                row.put("id",UUID.randomUUID().toString());
                row.put("event_id",eventId);row.put("station_id",station);
                row.put("method",method);row.put("device_id",key);
                row.put("deployed_at_utc",now());row.put("status","deployed");
                if(meters!=null)row.put("fence_length_m",meters);
                row.put("setup_notes",notes.getText().toString().trim());
                try{
                    db.getWritableDatabase().insertOrThrow("trap_deployments",null,row);
                    dialog.dismiss();draw();
                }catch(Exception error){device.setError("ID already registered in this survey");}
            }));
        dialog.show();
    }
    private static Double parse(String raw){
        if(raw.trim().isEmpty())return null;
        try{
            double x=Double.parseDouble(raw.trim());
            return Double.isFinite(x)?x:null;
        }catch(Exception e){return null;}
    }
    private void recordCheck(String deployment,String device){
        LinearLayout form=column();form.setPadding(dp(15),dp(4),dp(15),dp(6));
        Spinner state=new Spinner(this);
        state.setAdapter(new ArrayAdapter<String>(this,
            android.R.layout.simple_spinner_dropdown_item,
            new String[]{"Operational","Offline or damaged"}));
        form.addView(label("Equipment condition at check",14));
        form.addView(state);
        EditText count=field("Independent images/detections or captured individuals",true);
        count.setInputType(InputType.TYPE_CLASS_NUMBER);form.addView(count);
        EditText downtime=field("Verified downtime since previous check (minutes)",true);
        form.addView(downtime);
        EditText notes=field("Battery, memory, trap condition, weather, observer notes",false);
        form.addView(notes);
        AlertDialog d=new AlertDialog.Builder(this).setTitle("Inspect "+device)
            .setView(form).setNegativeButton("Cancel",null)
            .setPositiveButton("Save check",null).create();
        d.setOnShowListener(v->d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(w->{
            int n=0;
            try{n=Integer.parseInt(count.getText().toString().trim());}
            catch(Exception ignored){count.setError("Enter 0 or more detections");return;}
            if(n<0||n>10000){count.setError("Check count");return;}
            Double down=parse(downtime.getText().toString());
            if(!downtime.getText().toString().trim().isEmpty()&&
                (down==null||down<0)){downtime.setError("Use positive minutes");return;}
            ContentValues record=new ContentValues();
            record.put("deployment_id",deployment);record.put("checked_at_utc",now());
            record.put("operational",state.getSelectedItemPosition()==0?1:0);
            record.put("observations",n);
            if(down!=null)record.put("outage_minutes",down);
            record.put("device_condition",state.getSelectedItem().toString());
            record.put("observer_notes",notes.getText().toString().trim());
            long id=db.getWritableDatabase().insertOrThrow("trap_checks",null,record);
            d.dismiss();draw();
            if(n>0&&TrapProtocolPolicy.needsCaptureDetails(method))
                addCapture(id);
        }));
        d.show();
    }
    private void showChecks(LinearLayout card,String deployment,String device){
        try(Cursor c=db.getReadableDatabase().rawQuery(
            "SELECT x.id,x.checked_at_utc,x.operational,x.observations,"+
            "COALESCE(x.outage_minutes,0),COALESCE(x.observer_notes,''),"+
            "(SELECT COUNT(*) FROM trap_captures z WHERE z.check_id=x.id) "+
            "FROM trap_checks x WHERE x.deployment_id=? ORDER BY x.checked_at_utc",
            new String[]{deployment})){
            while(c.moveToNext()){
                long checkId=c.getLong(0);int count=c.getInt(3),captures=c.getInt(6);
                card.addView(label(c.getString(1)+" · "+
                    (c.getInt(2)==1?"Operational":"Offline")+
                    " · "+count+" detections"+
                    (TrapProtocolPolicy.needsCaptureDetails(method)?
                        " · "+captures+" capture records":"")+
                    "\nDowntime "+c.getDouble(4)+" min · "+c.getString(5),12));
                if(writable()&&TrapProtocolPolicy.needsCaptureDetails(method)&&
                    captures<count){
                    Button detail=UiKit.button(this,"＋ Capture and release record","secondary");
                    card.addView(detail,new LinearLayout.LayoutParams(-1,dp(48)));
                    detail.setOnClickListener(v->addCapture(checkId));
                }
            }
        }
    }
    private void addCapture(long checkId){
        LinearLayout form=column();form.setPadding(dp(15),dp(6),dp(15),dp(6));
        EditText taxon=field("Species or unidentified taxon",false);
        EditText tag=field("Individual mark / tag ID (optional)",false);
        Spinner fate=new Spinner(this);
        fate.setAdapter(new ArrayAdapter<String>(this,
            android.R.layout.simple_spinner_dropdown_item,
            new String[]{"Released alive now","Retained for review","Found deceased","Unresolved"}));
        EditText notes=field("Condition, measurements and release notes",false);
        form.addView(taxon);form.addView(tag);form.addView(fate);form.addView(notes);
        AlertDialog dialog=new AlertDialog.Builder(this)
            .setTitle("Log capture or pitfall encounter").setView(form)
            .setNegativeButton("Cancel",null).setPositiveButton("Save capture",null).create();
        dialog.setOnShowListener(v->dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            .setOnClickListener(w->{
                String name=taxon.getText().toString().trim();
                if(name.isEmpty()){taxon.setError("Taxon or unidentified required");return;}
                ContentValues capture=new ContentValues();
                capture.put("check_id",checkId);capture.put("taxon_label",name);
                capture.put("individual_mark",tag.getText().toString().trim());
                capture.put("captured_at_utc",now());
                capture.put("disposition",fate.getSelectedItem().toString());
                if(fate.getSelectedItemPosition()==0)capture.put("released_at_utc",now());
                capture.put("notes",notes.getText().toString().trim());
                db.getWritableDatabase().insertOrThrow("trap_captures",null,capture);
                dialog.dismiss();draw();
            }));
        dialog.show();
    }
    @Override protected void onDestroy(){db.close();super.onDestroy();}
}
