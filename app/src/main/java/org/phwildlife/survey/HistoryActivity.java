package org.phwildlife.survey;
import android.app.Activity;
import android.content.Intent;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.graphics.Color;
import android.text.InputType;
import android.view.View;
import android.widget.*;
import org.json.JSONObject;
import java.util.*;

public class HistoryActivity extends Activity {
    private FieldDb db;
    private LinearLayout rows,root;
    private String eventFilter;
    private final int INK=Color.rgb(29,57,40);
    private int dp(int value){return (int)(value*getResources().getDisplayMetrics().density+0.5f);}
    private LinearLayout column(){LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);return c;}
    private TextView text(String val,int size){TextView t=new TextView(this);t.setText(val);t.setTextSize(size);t.setTextColor(INK);t.setPadding(dp(5),dp(7),dp(5),dp(7));return t;}
    private Button btn(String val){return UiKit.button(this,val,"secondary");}
    private EditText field(String hint,String init){EditText v=new EditText(this);v.setHint(hint);v.setText(init==null?"":init);v.setSingleLine(true);return v;}
    @Override public void onCreate(Bundle b){super.onCreate(b);db=new FieldDb(this);eventFilter=getIntent().getStringExtra("event_id");draw();}
    @Override protected void onDestroy(){super.onDestroy();db.close();}
    private void draw(){
        root=column();root.setBackgroundColor(Color.rgb(247,249,247));root.setPadding(dp(12),dp(12),dp(12),dp(12));
        root.addView(text(eventFilter==null?"Survey history":"Survey observations",23));
        Button back=btn(eventFilter==null?"← Home":"← All surveys");
        back.setOnClickListener(v->{if(eventFilter==null)finish();else{eventFilter=null;draw();}});
        root.addView(back);
        ScrollView scroll=new ScrollView(this);
        rows=column();scroll.addView(rows);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        setContentView(root);
        if(eventFilter==null)surveys();else observations(eventFilter);
    }
    private void surveys(){
        try(Cursor c=db.getReadableDatabase().rawQuery(
          "SELECT s.id,COALESCE(s.survey_profile,s.taxon_group,'Birds'),s.method,COALESCE(s.station_id,''),s.started,s.ended,s.complete,COUNT(r.id),COALESCE(s.outcome,'active') FROM surveys s LEFT JOIN field_records r ON r.event_id=s.id AND r.deleted_at IS NULL GROUP BY s.id ORDER BY s.started DESC",null)){
            if(c.getCount()==0)rows.addView(text("No saved surveys yet.",15));
            while(c.moveToNext()){
                String event=c.getString(0),profile=c.getString(1),method=c.getString(2),site=c.getString(3),date=c.getString(4);
                String state=c.getString(8).equals("not_counted")?"NOT COUNTED":c.isNull(5)?"Active":c.getInt(6)==1?"Completed":"Incomplete";
                Button item=UiKit.button(this,profile+" · "+method+"\n"+site+" · "+date+"\n"+state+" · "+c.getInt(7)+" records",state.equals("NOT COUNTED")?"danger":"secondary");
                item.setOnClickListener(v->{eventFilter=event;draw();});
                rows.addView(item);
            }
        }
    }
    private void observations(String event){
        try(Cursor meta=db.getReadableDatabase().rawQuery("SELECT COALESCE(station_id,''),method,started,ended,COALESCE(outcome,'active'),COALESCE(exclusion_reason,'') FROM surveys WHERE id=?",new String[]{event})){
            if(meta.moveToFirst())rows.addView(text("Station "+meta.getString(0)+" · "+meta.getString(1)+"\n"+meta.getString(2)+"\n"+meta.getString(4)+(meta.getString(4).equals("not_counted")?"\nNOT COUNTED REASON: "+meta.getString(5):""),14));
        }
        Button progress=UiKit.button(this,"Station progress","secondary");
        progress.setOnClickListener(v->startActivity(new Intent(this,StationProgressActivity.class)
            .putExtra("event_id",event)));
        rows.addView(progress);
        try(Cursor c=db.getReadableDatabase().rawQuery(
            "SELECT r.id,COALESCE(r.raw_identification,''),COALESCE(r.taxon_group,''),r.individual_count,r.horizontal_m,COALESCE(r.detection_method,''),r.detected_at,COALESCE(p.station_id,u.station_id,''),COALESCE(p.sequence_number,u.sequence_number,0),r.point_count_id,r.sampling_unit_id FROM field_records r LEFT JOIN point_counts p ON p.id=r.point_count_id LEFT JOIN sampling_units u ON u.id=r.sampling_unit_id WHERE r.event_id=? AND r.deleted_at IS NULL ORDER BY COALESCE(p.sequence_number,u.sequence_number,0),r.detected_at,r.id",new String[]{event})){
            if(c.getCount()==0)rows.addView(text("No detections recorded. The survey event remains saved.",14));
            while(c.moveToNext()){
                long record=c.getLong(0);
                String dist=c.isNull(4)?"distance unknown":String.format(Locale.US,"%.1f m",c.getDouble(4));
                String point=c.getInt(8)>0?"Station "+c.getString(7)+" · "+(!c.isNull(9)?"Point ":"Visit ")+c.getInt(8)+"\n":"";
                Button item=btn(point+c.getString(1)+" · "+c.getInt(3)+"\n"+c.getString(2)+" · "+c.getString(5)+" · "+dist+"\n"+c.getString(6));
                item.setOnClickListener(v->recordDetail(record));
                rows.addView(item);
            }
        }
    }
    private static Double num(String s){try{return s.trim().isEmpty()?null:Double.valueOf(s.trim());}catch(Exception e){return null;}}
    private static JSONObject snapshot(Cursor c) throws Exception{
        JSONObject o=new JSONObject();
        for(int i=0;i<c.getColumnCount();i++){
            String name=c.getColumnName(i);
            if(c.isNull(i))o.put(name,JSONObject.NULL);
            else if(c.getType(i)==Cursor.FIELD_TYPE_INTEGER)o.put(name,c.getLong(i));
            else if(c.getType(i)==Cursor.FIELD_TYPE_FLOAT)o.put(name,c.getDouble(i));
            else o.put(name,c.getString(i));
        }
        return o;
    }
    private void recordDetail(long id){
        try(Cursor c=db.getReadableDatabase().rawQuery("SELECT * FROM field_records WHERE id=? AND deleted_at IS NULL",new String[]{String.valueOf(id)})){
            if(!c.moveToFirst()){draw();return;}
            JSONObject original=snapshot(c);
            String title=original.optString("raw_identification","Observation "+id);
            new AlertDialog.Builder(this).setTitle(title)
              .setMessage("Review or correct this record. Original values remain in the audit history.")
              .setNegativeButton("Cancel",null)
              .setNeutralButton("Edit", (dlg,w)->edit(id,original))
              .setPositiveButton("More",(dlg,w)->new AlertDialog.Builder(this).setTitle("Record actions")
                  .setItems(new String[]{"Edit observation","View edit history","Delete observation"},(d,index)->{
                      if(index==0)edit(id,original);
                      else if(index==1)audit(id);
                      else deletePrompt(id,original);
                  }).show()).show();
        }catch(Exception e){Toast.makeText(this,"Cannot open record: "+e.getMessage(),Toast.LENGTH_LONG).show();}
    }
    private void edit(long id,JSONObject before) {
        LinearLayout form=column();form.setPadding(dp(14),0,dp(14),0);
        EditText taxon=field("Species or unidentified taxon",before.optString("raw_identification",""));
        EditText individuals=field("Individuals",String.valueOf(before.optInt("individual_count",1)));
        individuals.setInputType(InputType.TYPE_CLASS_NUMBER);
        EditText distance=field("Horizontal distance (m)",before.isNull("horizontal_m")?"":before.optString("horizontal_m"));
        distance.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        EditText notes=field("Natural history / notes",before.optString("notes",""));
        EditText reason=field("Reason for correction (recommended)","");
        Spinner type=new Spinner(this);
        String[] sources={"Unknown","Estimated","Measured"};
        type.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,sources));
        String oldMethod=before.optString("distance_method","Unknown");
        for(int i=0;i<sources.length;i++)if(sources[i].equalsIgnoreCase(oldMethod))type.setSelection(i);
        Spinner first=new Spinner(this);
        String[] position={"Unknown","Yes","No"};
        first.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,position));
        for(int i=0;i<position.length;i++)if(position[i].equalsIgnoreCase(before.optString("initial_position","Unknown")))first.setSelection(i);
        form.addView(text("Identification",13));form.addView(taxon);
        form.addView(text("Count",13));form.addView(individuals);
        form.addView(text("Horizontal distance",13));form.addView(distance);
        form.addView(text("Distance source",13));form.addView(type);
        form.addView(text("First detection position?",13));form.addView(first);
        Spinner movement=new Spinner(this);
        String[] movementOptions={"Unknown","Stationary","Moving","Flyover","Entering after start"};
        movement.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,movementOptions));
        for(int i=0;i<movementOptions.length;i++)
            if(movementOptions[i].equalsIgnoreCase(before.optString("movement_status","Unknown")))movement.setSelection(i);
        form.addView(text("Movement / availability",13));form.addView(movement);
        form.addView(text("Notes",13));form.addView(notes);form.addView(reason);
        ScrollView scroll=new ScrollView(this);scroll.addView(form);
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Edit observation "+id).setView(scroll)
            .setNegativeButton("Cancel",null).setPositiveButton("Save correction",null).create();
        dialog.setOnShowListener(x->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
            String name=taxon.getText().toString().trim();
            int count=0;try{count=Integer.parseInt(individuals.getText().toString().trim());}catch(Exception ignored){}
            Double d=num(distance.getText().toString());
            if(name.length()<2||count<1||d!=null&&(d<0||d>10000)){
                Toast.makeText(this,"Check identification, count and distance",Toast.LENGTH_LONG).show();return;
            }
            String[] split=Taxa.identify(name);
            SQLiteDatabase writable=db.getWritableDatabase();
            writable.beginTransaction();
            try{
                ContentValues values=new ContentValues();
                values.put("raw_identification",name);values.put("common_name",split[0]);values.put("scientific_name",split[1]);
                values.put("individual_count",count);
                if(d==null)values.putNull("horizontal_m");else values.put("horizontal_m",d);
                values.put("notes",notes.getText().toString());
                values.put("distance_method",type.getSelectedItem().toString());
                values.put("initial_position",first.getSelectedItem().toString());
                values.put("movement_status",movement.getSelectedItem().toString());
                String event=before.optString("event_id");
                String[] survey=new String[]{"","","",""};
                try(Cursor c=writable.rawQuery("SELECT method,transect_az,point_radius_m,planned_duration_s FROM surveys WHERE id=?",new String[]{event})){
                    if(c.moveToFirst()){
                        survey[0]=c.getString(0);survey[1]=c.isNull(1)?"":c.getString(1);
                        survey[2]=c.isNull(2)?"":c.getString(2);
                        survey[3]=c.isNull(3)?"":c.getString(3);
                    }
                }
                boolean candidate=d!=null&&first.getSelectedItem().toString().equals("Yes")
                    &&!type.getSelectedItem().toString().equals("Unknown")
                    &&(survey[0].equals("point_count")||survey[0].equals("line_transect"))
                    &&!movement.getSelectedItem().toString().equals("Flyover")
                    &&!movement.getSelectedItem().toString().equals("Entering after start")
                    &&(!survey[0].equals("line_transect")||(!survey[1].isEmpty()&&!before.isNull("bearing_magnetic")))
                    &&(!survey[0].equals("point_count")||survey[2].isEmpty()||d<=Double.parseDouble(survey[2]))
                    &&!before.optString("protocol_window_flag","within").equals("outside");
                values.put("distance_eligible",candidate?1:0);
                if(d!=null&&survey[0].equals("line_transect")&&!survey[1].isEmpty()&&!before.isNull("bearing_magnetic"))
                    values.put("perpendicular_m",Geometry.perpendicular(d,before.getDouble("bearing_magnetic"),Double.parseDouble(survey[1])));
                else values.putNull("perpendicular_m");
                values.put("updated_at",new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.US){{setTimeZone(TimeZone.getTimeZone("UTC"));}}.format(new Date()));
                writable.update("field_records",values,"id=?",new String[]{""+id});
                JSONObject after;
                try(Cursor c=writable.rawQuery("SELECT * FROM field_records WHERE id=?",new String[]{""+id})){
                    c.moveToFirst();after=snapshot(c);
                }
                auditWrite(writable,id,"edit",before,after,reason.getText().toString());
                writable.setTransactionSuccessful();
                dialog.dismiss();draw();
            }catch(Exception e){Toast.makeText(this,"Save failed: "+e.getMessage(),Toast.LENGTH_LONG).show();}
            finally{writable.endTransaction();}
        }));
        dialog.show();
    }
    private void deletePrompt(long id,JSONObject before){
        new AlertDialog.Builder(this).setTitle("Delete observation?")
            .setMessage("Hide this observation from normal survey exports. The deletion remains in its edit history.")
            .setNegativeButton("Cancel",null).setPositiveButton("Delete",(d,w)->{
                SQLiteDatabase dbw=db.getWritableDatabase();dbw.beginTransaction();
                try{
                    ContentValues v=new ContentValues();v.put("deleted_at",new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.US){{setTimeZone(TimeZone.getTimeZone("UTC"));}}.format(new Date()));
                    dbw.update("field_records",v,"id=?",new String[]{String.valueOf(id)});
                    JSONObject after;
                    try(Cursor c=dbw.rawQuery("SELECT * FROM field_records WHERE id=?",new String[]{String.valueOf(id)})){c.moveToFirst();after=snapshot(c);}
                    auditWrite(dbw,id,"soft_delete",before,after,"User deleted record");
                    dbw.setTransactionSuccessful();
                    draw();
                }catch(Exception e){Toast.makeText(this,"Delete error: "+e.getMessage(),Toast.LENGTH_LONG).show();}
                finally{dbw.endTransaction();}
            }).show();
    }
    private static void auditWrite(SQLiteDatabase db,long id,String action,JSONObject old,JSONObject after,String why){
        ContentValues v=new ContentValues();v.put("record_id",id);v.put("action",action);
        v.put("edited_at",new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.US){{setTimeZone(TimeZone.getTimeZone("UTC"));}}.format(new Date()));
        v.put("previous_json",old.toString());v.put("current_json",after.toString());v.put("reason",why);
        db.insertOrThrow("record_edits",null,v);
    }
    private void audit(long id){
        StringBuilder msg=new StringBuilder();
        try(Cursor c=db.getReadableDatabase().rawQuery("SELECT edited_at,action,COALESCE(reason,''),previous_json FROM record_edits WHERE record_id=? ORDER BY id DESC",new String[]{String.valueOf(id)})){
            if(c.getCount()==0)msg.append("No changes recorded.");
            while(c.moveToNext()){
                msg.append(c.getString(0)).append(" · ").append(c.getString(1)).append("\n");
                if(!c.getString(2).isEmpty())msg.append("Reason: ").append(c.getString(2)).append("\n");
                msg.append("Before: ").append(c.getString(3)).append("\n\n");
            }
        }
        new AlertDialog.Builder(this).setTitle("Record edit history").setMessage(msg.toString()).setPositiveButton("Close",null).show();
    }
}
