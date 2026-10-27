package org.phwildlife.survey;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.location.Location;
import android.os.SystemClock;
import android.net.Uri;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ImageView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

/**
 * Offline protocol records. These supplement detections and sampling-unit effort,
 * and can be inspected again after the parent survey closes.
 */
public final class FieldProtocolsActivity extends Activity {
    private FieldDb db;
    private LinearLayout records;
    private String eventId,stationId="",method="";
    private long surveyStartMs=0;
    private static final int PICK_EVIDENCE=7191;
    private String pendingTargetType;
    private long pendingTargetId=0;

    private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
    private LinearLayout col(){LinearLayout l=new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);return l;}
    private TextView text(String s,int size){
        TextView t=new TextView(this);t.setText(s);t.setTextSize(size);
        t.setTextColor(UiKit.INK);t.setPadding(dp(4),dp(8),dp(4),dp(8));return t;
    }
    private EditText field(String hint,boolean number){
        EditText e=new EditText(this);e.setHint(hint);e.setTextSize(15);
        if(number)e.setInputType(InputType.TYPE_CLASS_NUMBER|
            InputType.TYPE_NUMBER_FLAG_DECIMAL);
        return e;
    }
    private Button action(String title,LinearLayout target,boolean primary){
        Button b=UiKit.button(this,title,primary?"primary":"secondary");
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(50));
        lp.bottomMargin=dp(8);target.addView(b,lp);return b;
    }
    private void toast(String s){android.widget.Toast.makeText(this,s,android.widget.Toast.LENGTH_LONG).show();}
    private static String utc(long time){
        SimpleDateFormat fmt=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.US);
        fmt.setTimeZone(TimeZone.getTimeZone("UTC"));return fmt.format(new Date(time));
    }
    private static Long millis(String text){
        try{
            SimpleDateFormat fmt=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.US);
            fmt.setTimeZone(TimeZone.getTimeZone("UTC"));fmt.setLenient(false);
            Date d=fmt.parse(text.trim());
            if(d==null||!utc(d.getTime()).equals(text.trim()))return null;
            return d.getTime();
        }catch(Exception e){return null;}
    }
    private static Double quantity(EditText e){
        try{
            double n=Double.parseDouble(e.getText().toString().trim());
            return Double.isFinite(n)?n:null;
        }catch(Exception err){return null;}
    }
    private static Integer count(EditText e){
        try{return Integer.parseInt(e.getText().toString().trim());}
        catch(Exception err){return null;}
    }
    private boolean active(){
        if(eventId==null||stationId.isEmpty())return false;
        try(Cursor c=db.getReadableDatabase().rawQuery(
            "SELECT ended FROM surveys WHERE id=? AND method=?",
            new String[]{eventId,method})){
            return c.moveToFirst()&&c.isNull(0);
        }
    }
    /** Only a fresh under-10 m GNSS location is recorded as verified. */
    private void stampGps(ContentValues values){
        Location fix=TrackService.currentFix();
        if(fix!=null){
            long age=(SystemClock.elapsedRealtimeNanos()-fix.getElapsedRealtimeNanos())/1_000_000L;
            if(GpsFixPolicy.acceptableElapsed(fix.getLatitude(),fix.getLongitude(),
                    fix.getAccuracy(),age,fix.isFromMockProvider())){
                values.put("latitude",fix.getLatitude());
                values.put("longitude",fix.getLongitude());
                values.put("gps_accuracy_m",fix.getAccuracy());
                values.put("gps_timestamp_utc",utc(fix.getTime()));
                values.put("gps_status","verified_under_10m");
                return;
            }
        }
        values.put("gps_status","missing_or_unverified");
    }
    private String gpsSummary(Cursor c,int start){
        return "verified_under_10m".equals(c.getString(start))?
            String.format(Locale.US,"GPS %.6f, %.6f ±%.1f m",
                c.getDouble(start+1),c.getDouble(start+2),c.getDouble(start+3)):
            "GPS missing / unverified";
    }
    private int photoCount(String type,long target){
        try(Cursor c=db.getReadableDatabase().rawQuery(
            "SELECT COUNT(*) FROM protocol_media WHERE target_type=? AND target_id=?",
            new String[]{type,String.valueOf(target)})){
            return c.moveToFirst()?c.getInt(0):0;
        }
    }
    private void showPhotos(String type,long target){
        ScrollView scroll=new ScrollView(this);
        LinearLayout content=col();
        content.setPadding(dp(12),dp(10),dp(12),dp(12));
        scroll.addView(content);
        try(Cursor c=db.getReadableDatabase().rawQuery(
            "SELECT file_name,added_utc FROM protocol_media "+
            "WHERE target_type=? AND target_id=? ORDER BY id",
            new String[]{type,String.valueOf(target)})){
            while(c.moveToNext()){
                content.addView(text("Captured in evidence archive · "+c.getString(1),12));
                ImageView photo=new ImageView(this);
                photo.setAdjustViewBounds(true);
                photo.setContentDescription("Original photo linked to this field record");
                PhotoEvidence.preview(this,c.getString(0),photo);
                content.addView(photo,new LinearLayout.LayoutParams(-1,dp(240)));
            }
        }
        new AlertDialog.Builder(this).setTitle("Linked evidence photographs")
            .setView(scroll).setPositiveButton("Close",null).show();
    }
    private void choosePhoto(String type,long target){
        if(!active()){toast("Photo capture requires the active station");return;}
        pendingTargetType=type;pendingTargetId=target;
        Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(intent,PICK_EVIDENCE);
    }
    @Override protected void onActivityResult(int request,int result,Intent data){
        super.onActivityResult(request,result,data);
        if(request!=PICK_EVIDENCE)return;
        if(result==RESULT_OK&&data!=null&&data.getData()!=null&&
            pendingTargetType!=null&&active()){
            try{
                ProtocolEvidencePhotos.attach(this,db.getWritableDatabase(),eventId,
                    pendingTargetType,pendingTargetId,data.getData());
                toast("Photo linked to original field evidence");
                draw();
            }catch(Exception err){toast("Photo not saved: "+err.getMessage());}
        }
        pendingTargetType=null;pendingTargetId=0;
    }
    private boolean validInterval(EditText start,EditText end){
        Long from=millis(start.getText().toString());
        Long to=millis(end.getText().toString());
        if(from==null||to==null||!FieldProtocolPolicy.validInterval(from,to)||
            to>System.currentTimeMillis()+1000L||
            (surveyStartMs>0&&from<surveyStartMs-1000L)){
            end.setError("Use a real interval inside this survey (UTC)");
            return false;
        }
        return true;
    }
    @Override public void onCreate(Bundle bundle){
        super.onCreate(bundle);db=new FieldDb(this);
        eventId=getIntent().getStringExtra("event_id");
        String station=getIntent().getStringExtra("station_id");
        stationId=station==null?"":station;
        resolveSurvey();draw();
    }
    private void resolveSurvey(){
        if(eventId==null)return;
        try(Cursor c=db.getReadableDatabase().rawQuery(
            "SELECT method,started FROM surveys WHERE id=?",new String[]{eventId})){
            if(c.moveToFirst()){
                method=c.getString(0);
                Long t=millis(c.getString(1));surveyStartMs=t==null?0:t;
            }
        }
    }
    private void draw(){
        LinearLayout root=col();root.setPadding(dp(14),dp(9),dp(14),dp(10));
        root.setBackgroundColor(UiKit.BG);
        Button back=UiKit.button(this,"‹ Back","secondary");
        root.addView(back,new LinearLayout.LayoutParams(dp(110),dp(48)));
        back.setOnClickListener(v->{
            if(eventId!=null&&getIntent().getStringExtra("event_id")==null){
                eventId=null;method="";draw();
            }else finish();
        });
        root.addView(text("Field protocol records",22));
        ScrollView sc=new ScrollView(this);records=col();
        records.setPadding(0,dp(3),0,dp(20));sc.addView(records);
        root.addView(sc,new LinearLayout.LayoutParams(-1,0,1));
        setContentView(root);
        if(eventId==null){showSurveys();return;}
        if(!FieldProtocolPolicy.supported(method)){
            records.addView(text("This method has no coverboard, area-search, or track-and-sign module.",14));
            return;
        }
        String description="spotlight".equals(method)?
            "Spotlight passes · beam, visibility, distance and repeat visits":
            "coverboard_search".equals(method)?
            "Coverboards · individual board checks":
            "area_search".equals(method)?
            "Area search · timed search passes and repeated coverage":
            "Mammal track and sign · measured routes and sign encounters";
        records.addView(text(description,16));
        if(!active())records.addView(text("Read only · start an active sampling visit to add effort",13));
        if("coverboard_search".equals(method))showBoards();
        else if("area_search".equals(method))showAreas();
        else if("spotlight".equals(method))showSpotlight();
        else showSignEffort();
    }
    private void showSurveys(){
        records.addView(text("Open a completed or active survey's field protocol.",14));
        try(Cursor c=db.getReadableDatabase().rawQuery(
            "SELECT id,method,started,COALESCE(outcome,'active') FROM surveys "+
            "WHERE method IN ('coverboard_search','area_search','track_sign','spotlight') "+
            "ORDER BY started DESC LIMIT 200",null)){
            if(c.getCount()==0)records.addView(text("No matching surveys yet.",14));
            while(c.moveToNext()){
                String id=c.getString(0),met=c.getString(1);
                Button open=action(met.replace('_',' ')+" · "+c.getString(2)+
                    "\n"+c.getString(3),records,false);
                open.setOnClickListener(v->{eventId=id;resolveSurvey();draw();});
            }
        }
    }
    private void heading(String value){records.addView(text(value,17));}
    private LinearLayout panel(String title){
        LinearLayout c=col();c.setPadding(dp(12),dp(6),dp(12),dp(9));
        c.setBackground(UiKit.surface(this));
        c.addView(text(title,16));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
        lp.bottomMargin=dp(11);records.addView(c,lp);
        return c;
    }
    // A board persists between station visits; a zero-detection inspection counts.
    private void showBoards(){
        if(active())action("＋ Register coverboard",records,true).setOnClickListener(v->newBoard());
        try(Cursor c=db.getReadableDatabase().rawQuery(
            "SELECT b.id,b.board_id,b.station_id,b.installed_at_utc,b.board_material,"+
            "COALESCE(b.habitat,''),COALESCE(b.setup_notes,''),"+
            "(SELECT COUNT(*) FROM coverboard_checks x WHERE x.board_pk=b.id) "+
            "FROM coverboard_units b WHERE b.event_id=? ORDER BY b.board_id",
            new String[]{eventId})){
            if(c.getCount()==0)records.addView(text("No boards registered.",14));
            while(c.moveToNext()){
                String pk=c.getString(0),board=c.getString(1);
                LinearLayout card=panel(board+" · "+c.getInt(7)+" inspections");
                card.addView(text("Installed "+c.getString(3)+" · "+c.getString(4)+
                    "\nStation "+c.getString(2)+" · "+c.getString(5)+
                    "\n"+c.getString(6),12));
                if(active())action("Record board inspection",card,false)
                    .setOnClickListener(v->inspectBoard(pk,board));
                try(Cursor checks=db.getReadableDatabase().rawQuery(
                    "SELECT checked_at_utc,observer,board_condition,individuals_seen,"+
                    "COALESCE(taxon_label,''),COALESCE(notes,''),id,"+
                    "gps_status,latitude,longitude,gps_accuracy_m FROM coverboard_checks "+
                    "WHERE board_pk=? ORDER BY checked_at_utc DESC LIMIT 30",
                    new String[]{pk})){
                    while(checks.moveToNext()){
                        long checkId=checks.getLong(6);
                        card.addView(text(checks.getString(0)+" · "+checks.getString(1)+
                            " · "+checks.getString(2)+"\n"+
                            checks.getInt(3)+" individuals · "+checks.getString(4)+
                            " · "+checks.getString(5)+"\n"+
                            gpsSummary(checks,7)+" · "+photoCount("coverboard_check",checkId)+
                            " photos",12));
                        if(photoCount("coverboard_check",checkId)>0)
                            action("View inspection photos",card,false)
                                .setOnClickListener(v->showPhotos("coverboard_check",checkId));
                        if(active())action("Attach photo to this inspection",card,false)
                            .setOnClickListener(v->choosePhoto("coverboard_check",checkId));
                    }
                }
            }
        }
    }
    private void newBoard(){
        LinearLayout form=col();form.setPadding(dp(12),dp(3),dp(12),dp(3));
        EditText code=field("Unique board ID",false);
        EditText installed=field("Installation UTC (may predate this survey)",false);
        installed.setText(utc(System.currentTimeMillis()));
        EditText material=field("Board material and dimensions",false);
        EditText habitat=field("Microhabitat or placement",false);
        EditText notes=field("Setup notes",false);
        for(EditText e:new EditText[]{code,installed,material,habitat,notes})form.addView(e);
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Register coverboard")
            .setView(form).setNegativeButton("Back",null)
            .setPositiveButton("Register",null).create();
        dialog.setOnShowListener(v->dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            .setOnClickListener(w->{
                if(!active()){toast("Survey is no longer active");return;}
                String id=code.getText().toString().trim();
                if(id.isEmpty()){code.setError("Board ID required");return;}
                Long time=millis(installed.getText().toString());
                if(time==null||time>System.currentTimeMillis()){
                    installed.setError("Enter a valid past UTC date");return;
                }
                ContentValues row=new ContentValues();
                row.put("id",UUID.randomUUID().toString());
                row.put("event_id",eventId);row.put("station_id",stationId);
                row.put("board_id",id);row.put("installed_at_utc",utc(time));
                row.put("board_material",material.getText().toString().trim());
                row.put("habitat",habitat.getText().toString().trim());
                row.put("setup_notes",notes.getText().toString().trim());
                try{
                    db.getWritableDatabase().insertOrThrow("coverboard_units",null,row);
                    dialog.dismiss();draw();
                }catch(Exception e){code.setError("Board ID is already used in this survey");}
            }));
        dialog.show();
    }
    private void inspectBoard(String pk,String board){
        LinearLayout form=col();form.setPadding(dp(12),0,dp(12),0);
        EditText observer=field("Observer name or ID",false);
        Spinner condition=new Spinner(this);
        condition.setAdapter(new ArrayAdapter<String>(this,
            android.R.layout.simple_spinner_dropdown_item,
            new String[]{"Intact and checked","Damaged but inspected","Missing / not inspected"}));
        EditText substrate=field("Soil and cover condition",false);
        EditText temp=field("Temperature °C (optional)",true);
        EditText humidity=field("Relative humidity % (optional)",true);
        EditText n=field("Animals observed (0 allowed)",true);
        n.setInputType(InputType.TYPE_CLASS_NUMBER);n.setText("0");
        EditText taxon=field("Taxon or unidentified (if detected)",false);
        EditText notes=field("Placement disturbance, weather and inspection notes",false);
        form.addView(observer);form.addView(condition);
        for(EditText e:new EditText[]{substrate,temp,humidity,n,taxon,notes})form.addView(e);
        ScrollView view=new ScrollView(this);view.addView(form);
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Inspect "+board)
            .setView(view).setNegativeButton("Back",null).setPositiveButton("Save",null).create();
        dialog.setOnShowListener(v->dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            .setOnClickListener(w->{
                if(!active()){toast("Survey is no longer active");return;}
                String who=observer.getText().toString().trim();
                if(who.isEmpty()){observer.setError("Observer required");return;}
                Integer count=count(n);
                if(count==null||count<0||count>10000){
                    n.setError("Enter 0 or more animals");return;
                }
                Double degrees=quantity(temp),percent=quantity(humidity);
                if(!temp.getText().toString().trim().isEmpty()&&
                    (degrees==null||degrees< -30||degrees>70)){
                    temp.setError("Check temperature");return;
                }
                if(!humidity.getText().toString().trim().isEmpty()&&
                    (percent==null||percent<0||percent>100)){
                    humidity.setError("Use 0–100%");return;
                }
                if(count>0&&taxon.getText().toString().trim().isEmpty()){
                    taxon.setError("Use species, genus or unidentified");return;
                }
                if(condition.getSelectedItemPosition()==2&&count>0){
                    n.setError("A missing board cannot have observed individuals");return;
                }
                ContentValues values=new ContentValues();
                values.put("board_pk",pk);values.put("checked_at_utc",utc(System.currentTimeMillis()));
                values.put("observer",who);
                values.put("board_condition",condition.getSelectedItem().toString());
                values.put("substrate",substrate.getText().toString().trim());
                if(degrees!=null)values.put("temperature_c",degrees);
                if(percent!=null)values.put("humidity_pct",percent);
                values.put("individuals_seen",count);
                values.put("taxon_label",taxon.getText().toString().trim());
                values.put("notes",notes.getText().toString().trim());
                stampGps(values);
                long checkId=db.getWritableDatabase().insertOrThrow(
                    "coverboard_checks",null,values);
                dialog.dismiss();draw();
                new AlertDialog.Builder(this).setTitle("Inspection saved")
                    .setMessage("GNSS quality and time were recorded with this inspection. "+
                        "Attach a source photograph to this exact board check?")
                    .setNegativeButton("Later",null)
                    .setPositiveButton("Attach photo",(d,i)->
                        choosePhoto("coverboard_check",checkId)).show();
            }));
        dialog.show();
    }
    private void showAreas(){
        if(active())action("＋ Record timed area-search pass",records,true)
            .setOnClickListener(v->newAreaPass());
        records.addView(text("A repeated pass is separate effort, not an extra unique area. "+
            "Record bird movements to avoid double-counting.",12));
        try(Cursor c=db.getReadableDatabase().rawQuery(
            "SELECT area_id,started_at_utc,ended_at_utc,observer,measured_area_m2,"+
            "COALESCE(route_length_m,0),repeat_pass,duplicate_control_notes,"+
            "COALESCE(habitat,''),COALESCE(weather,'') FROM area_search_passes "+
            "WHERE event_id=? ORDER BY started_at_utc,id",
            new String[]{eventId})){
            if(c.getCount()==0)records.addView(text("No measured area passes yet.",14));
            while(c.moveToNext()){
                LinearLayout card=panel(c.getString(0)+
                    (c.getInt(6)==1?" · repeat":" · primary pass"));
                card.addView(text(c.getString(1)+" to "+c.getString(2)+
                    "\n"+c.getString(3)+" · "+c.getDouble(4)+" m² · route "+
                    c.getDouble(5)+" m\n"+c.getString(8)+" · "+c.getString(9)+
                    "\nDuplicate control: "+c.getString(7),12));
            }
        }
    }
    private void newAreaPass(){
        LinearLayout form=col();form.setPadding(dp(12),0,dp(12),0);
        EditText area=field("Defined survey area ID or polygon reference",false);
        EditText observer=field("Observer name or ID",false);
        EditText started=field("Pass start UTC yyyy-MM-ddTHH:mm:ssZ",false);
        EditText ended=field("Pass finish UTC yyyy-MM-ddTHH:mm:ssZ",false);
        ended.setText(utc(System.currentTimeMillis()));
        EditText measured=field("Measured footprint area m²",true);
        EditText route=field("Measured distance walked m (optional)",true);
        EditText habitat=field("Habitat / area boundaries",false);
        EditText weather=field("Weather / visibility",false);
        CheckBox repeat=new CheckBox(this);repeat.setText("Repeated search of an earlier area");
        EditText duplicates=field("How were repeat individuals and movements excluded?",false);
        EditText notes=field("Coverage and limitations",false);
        for(EditText e:new EditText[]{area,observer,started,ended,measured,route,habitat,weather})
            form.addView(e);
        form.addView(repeat);form.addView(duplicates);form.addView(notes);
        ScrollView scroll=new ScrollView(this);scroll.addView(form);
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Timed area-search pass")
            .setView(scroll).setNegativeButton("Back",null)
            .setPositiveButton("Save effort",null).create();
        dialog.setOnShowListener(v->dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            .setOnClickListener(w->{
                if(!active()){toast("Survey closed");return;}
                if(area.getText().toString().trim().isEmpty()){
                    area.setError("Define the searched area");return;
                }
                if(observer.getText().toString().trim().isEmpty()){
                    observer.setError("Observer required");return;
                }
                if(!validInterval(started,ended))return;
                boolean alreadySearched=false;
                try(Cursor previous=db.getReadableDatabase().rawQuery(
                    "SELECT COUNT(*) FROM area_search_passes WHERE event_id=? AND area_id=?",
                    new String[]{eventId,area.getText().toString().trim()})){
                    if(previous.moveToFirst())alreadySearched=previous.getInt(0)>0;
                }
                if(alreadySearched&&!repeat.isChecked()){
                    repeat.setError("This area already has a pass; flag repeated coverage");
                    toast("Mark this search as a repeated pass to prevent double counting");
                    return;
                }
                Double areaSize=quantity(measured),distance=quantity(route);
                if(areaSize==null||!FieldProtocolPolicy.validArea(areaSize)){
                    measured.setError("Measured area must be positive");return;
                }
                if(!route.getText().toString().trim().isEmpty()&&
                    (distance==null||!FieldProtocolPolicy.validDistance(distance))){
                    route.setError("Positive route length required");return;
                }
                String duplicatePlan=duplicates.getText().toString().trim();
                if(duplicatePlan.isEmpty()){
                    duplicates.setError("Describe repeat-count control or state none observed");return;
                }
                ContentValues row=new ContentValues();
                row.put("event_id",eventId);row.put("station_id",stationId);
                row.put("area_id",area.getText().toString().trim());
                row.put("started_at_utc",started.getText().toString().trim());
                row.put("ended_at_utc",ended.getText().toString().trim());
                row.put("observer",observer.getText().toString().trim());
                row.put("measured_area_m2",areaSize);
                if(distance!=null)row.put("route_length_m",distance);
                row.put("habitat",habitat.getText().toString().trim());
                row.put("weather",weather.getText().toString().trim());
                row.put("repeat_pass",repeat.isChecked()||alreadySearched?1:0);
                row.put("duplicate_control_notes",duplicatePlan);
                row.put("notes",notes.getText().toString().trim());
                db.getWritableDatabase().insertOrThrow("area_search_passes",null,row);
                dialog.dismiss();draw();
            }));
        dialog.show();
    }
    private void showSpotlight(){
        if(active())action("＋ Record spotlight survey pass",records,true)
            .setOnClickListener(v->newSpotlightPass());
        records.addView(text("Record beam coverage and visibility for each pass. "+
            "A repeated pass must remain separate from new unique route effort.",12));
        try(Cursor c=db.getReadableDatabase().rawQuery(
            "SELECT route_id,observer,started_at_utc,ended_at_utc,beam_width_m,"+
            "visibility_m,distance_m,beam_type,COALESCE(weather,''),repeat_pass,"+
            "duplicate_control_notes FROM spotlight_passes "+
            "WHERE event_id=? ORDER BY started_at_utc,id",new String[]{eventId})){
            if(c.getCount()==0)records.addView(text("No spotlight passes yet.",14));
            while(c.moveToNext()){
                LinearLayout card=panel(c.getString(0)+
                    (c.getInt(9)==1?" · repeat pass":" · first pass"));
                card.addView(text(c.getString(2)+" to "+c.getString(3)+
                    "\\nObserver "+c.getString(1)+" · route "+c.getDouble(6)+" m"+
                    "\\nBeam "+c.getDouble(4)+" m · visibility "+c.getDouble(5)+" m"+
                    "\\nLight: "+c.getString(7)+" · weather: "+c.getString(8)+
                    "\\nDuplicate control: "+c.getString(10),12));
            }
        }
    }
    private void newSpotlightPass(){
        LinearLayout form=col();form.setPadding(dp(12),0,dp(12),0);
        EditText route=field("Route / area ID (stable across passes)",false);
        EditText observer=field("Observer name or ID",false);
        EditText start=field("Start UTC yyyy-MM-ddTHH:mm:ssZ",false);
        EditText end=field("End UTC yyyy-MM-ddTHH:mm:ssZ",false);
        end.setText(utc(System.currentTimeMillis()));
        EditText width=field("Measured or specified beam search width (m)",true);
        EditText visibility=field("Effective visual detection distance (m)",true);
        EditText distance=field("Measured length walked or searched (m)",true);
        EditText beam=field("Lamp model / beam intensity / observers / technique",false);
        EditText weather=field("Cloud, moon, rain and obstruction",false);
        CheckBox repeat=new CheckBox(this);
        repeat.setText("Repeated pass over an earlier route");
        EditText duplicate=field("How were individuals and repeat sightings tracked?",false);
        EditText notes=field("Speed, transect width method and search limitations",false);
        for(EditText item:new EditText[]{route,observer,start,end,width,visibility,distance,
            beam,weather})form.addView(item);
        form.addView(repeat);form.addView(duplicate);form.addView(notes);
        ScrollView scroll=new ScrollView(this);scroll.addView(form);
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Spotlight effort")
            .setView(scroll).setNegativeButton("Back",null)
            .setPositiveButton("Save pass",null).create();
        dialog.setOnShowListener(v->dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            .setOnClickListener(w->{
                if(!active()){toast("Survey closed");return;}
                if(route.getText().toString().trim().isEmpty()){
                    route.setError("Route ID required");return;
                }
                if(observer.getText().toString().trim().isEmpty()){
                    observer.setError("Observer required");return;
                }
                if(!validInterval(start,end))return;
                Double beamWidth=quantity(width),view=quantity(visibility),length=quantity(distance);
                if(beamWidth==null||beamWidth<=0||beamWidth>10000){
                    width.setError("Enter positive beam width (m)");return;
                }
                if(view==null||view<=0||view>10000){
                    visibility.setError("Enter positive visibility (m)");return;
                }
                if(length==null||!FieldProtocolPolicy.validDistance(length)){
                    distance.setError("Measured route length required");return;
                }
                if(beam.getText().toString().trim().isEmpty()){
                    beam.setError("Document lamp and technique");return;
                }
                String control=duplicate.getText().toString().trim();
                if(control.isEmpty()){
                    duplicate.setError("Record repeat count controls, even with zero sightings");return;
                }
                boolean earlier=false;
                try(Cursor previous=db.getReadableDatabase().rawQuery(
                    "SELECT COUNT(*) FROM spotlight_passes WHERE event_id=? AND route_id=?",
                    new String[]{eventId,route.getText().toString().trim()})){
                    if(previous.moveToFirst())earlier=previous.getInt(0)>0;
                }
                if(earlier&&!repeat.isChecked()){
                    toast("Mark the previously searched route as a repeated pass");return;
                }
                ContentValues row=new ContentValues();
                row.put("event_id",eventId);row.put("station_id",stationId);
                row.put("route_id",route.getText().toString().trim());
                row.put("observer",observer.getText().toString().trim());
                row.put("started_at_utc",start.getText().toString().trim());
                row.put("ended_at_utc",end.getText().toString().trim());
                row.put("beam_width_m",beamWidth);row.put("visibility_m",view);
                row.put("distance_m",length);
                row.put("beam_type",beam.getText().toString().trim());
                row.put("weather",weather.getText().toString().trim());
                row.put("repeat_pass",earlier||repeat.isChecked()?1:0);
                row.put("duplicate_control_notes",control);
                row.put("notes",notes.getText().toString().trim());
                db.getWritableDatabase().insertOrThrow("spotlight_passes",null,row);
                dialog.dismiss();draw();
            }));
        dialog.show();
    }
    private void showSignEffort(){
        if(active())action("＋ Record measured track/sign search",records,true)
            .setOnClickListener(v->newSignEffort());
        records.addView(text("Log measured searches with zero signs. Log each distinct "+
            "track set or other sign once, not every footprint.",12));
        try(Cursor c=db.getReadableDatabase().rawQuery(
            "SELECT id,route_id,started_at_utc,ended_at_utc,observer,"+
            "verified_length_m,substrate,COALESCE(weather,''),COALESCE(visibility,'') "+
            "FROM track_sign_efforts WHERE event_id=? ORDER BY started_at_utc,id",
            new String[]{eventId})){
            if(c.getCount()==0)records.addView(text("No measured sign-search routes yet.",14));
            while(c.moveToNext()){
                long id=c.getLong(0);
                LinearLayout card=panel(c.getString(1)+" · "+c.getDouble(5)+" m");
                card.addView(text(c.getString(2)+" to "+c.getString(3)+" · "+
                    c.getString(4)+"\nSubstrate "+c.getString(6)+" · "+
                    c.getString(7)+" · "+c.getString(8),12));
                if(active())action("＋ Log independent sign",card,false)
                    .setOnClickListener(v->newSign(id));
                try(Cursor signs=db.getReadableDatabase().rawQuery(
                    "SELECT sign_type,taxon_label,age_class,identification_confidence,"+
                    "COALESCE(independent_event_key,''),COALESCE(notes,''),id,"+
                    "gps_status,latitude,longitude,gps_accuracy_m "+
                    "FROM track_sign_encounters WHERE effort_id=? ORDER BY id",
                    new String[]{String.valueOf(id)})){
                    if(signs.getCount()==0)card.addView(text("No signs detected · valid zero result",12));
                    while(signs.moveToNext()){
                        long signId=signs.getLong(6);
                        card.addView(text(signs.getString(0)+" · "+signs.getString(1)+
                            " · "+signs.getString(2)+" · "+signs.getString(3)+
                            "\nEncounter key "+signs.getString(4)+
                            " · "+signs.getString(5)+"\n"+
                            gpsSummary(signs,7)+" · "+photoCount("track_sign",signId)+
                            " photos",12));
                        if(photoCount("track_sign",signId)>0)
                            action("View sign photographs",card,false)
                                .setOnClickListener(v->showPhotos("track_sign",signId));
                        if(active())action("Attach photo to this sign",card,false)
                            .setOnClickListener(v->choosePhoto("track_sign",signId));
                    }
                }
            }
        }
    }
    private void newSignEffort(){
        LinearLayout form=col();form.setPadding(dp(12),0,dp(12),0);
        EditText route=field("Sign transect / route ID",false);
        EditText observer=field("Observer name or ID",false);
        EditText start=field("Start UTC yyyy-MM-ddTHH:mm:ssZ",false);
        EditText end=field("End UTC yyyy-MM-ddTHH:mm:ssZ",false);
        end.setText(utc(System.currentTimeMillis()));
        EditText length=field("Measured search length m",true);
        EditText substrate=field("Track visibility / substrate (required)",false);
        EditText weather=field("Recent rain / weather",false);
        EditText visibility=field("Ground visibility, vegetation and effort bias",false);
        EditText notes=field("Route, repeat visits and equipment notes",false);
        for(EditText e:new EditText[]{route,observer,start,end,length,substrate,weather,visibility,notes})
            form.addView(e);
        ScrollView scroll=new ScrollView(this);scroll.addView(form);
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Measured sign search")
            .setView(scroll).setNegativeButton("Back",null)
            .setPositiveButton("Save effort",null).create();
        dialog.setOnShowListener(v->dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            .setOnClickListener(w->{
                if(!active()){toast("Survey closed");return;}
                if(route.getText().toString().trim().isEmpty()){
                    route.setError("Transect ID required");return;
                }
                if(observer.getText().toString().trim().isEmpty()){
                    observer.setError("Observer required");return;
                }
                if(!validInterval(start,end))return;
                Double distance=quantity(length);
                if(distance==null||!FieldProtocolPolicy.validDistance(distance)){
                    length.setError("Measured search length required");return;
                }
                if(substrate.getText().toString().trim().isEmpty()){
                    substrate.setError("Record sign visibility substrate");return;
                }
                ContentValues values=new ContentValues();
                values.put("event_id",eventId);values.put("station_id",stationId);
                values.put("route_id",route.getText().toString().trim());
                values.put("observer",observer.getText().toString().trim());
                values.put("started_at_utc",start.getText().toString().trim());
                values.put("ended_at_utc",end.getText().toString().trim());
                values.put("verified_length_m",distance);
                values.put("substrate",substrate.getText().toString().trim());
                values.put("weather",weather.getText().toString().trim());
                values.put("visibility",visibility.getText().toString().trim());
                values.put("notes",notes.getText().toString().trim());
                db.getWritableDatabase().insertOrThrow("track_sign_efforts",null,values);
                dialog.dismiss();draw();
            }));
        dialog.show();
    }
    private void newSign(long effortId){
        LinearLayout form=col();form.setPadding(dp(12),0,dp(12),0);
        Spinner type=new Spinner(this);
        type.setAdapter(new ArrayAdapter<String>(this,
            android.R.layout.simple_spinner_dropdown_item,
            new String[]{"Track set","Scat/dung","Scrape","Feeding sign",
                "Hair","Den/burrow","Other sign"}));
        EditText taxon=field("Species/genus or unidentified",false);
        Spinner age=new Spinner(this);
        age.setAdapter(new ArrayAdapter<String>(this,
            android.R.layout.simple_spinner_dropdown_item,
            new String[]{"Fresh","Recent","Old","Unknown"}));
        Spinner confidence=new Spinner(this);
        confidence.setAdapter(new ArrayAdapter<String>(this,
            android.R.layout.simple_spinner_dropdown_item,
            new String[]{"Uncertain","Probable","Field identified","Unidentified"}));
        EditText key=field("Unique encounter key to prevent double-counting (optional)",false);
        EditText substrate=field("Local tracking substrate",false);
        EditText notes=field("Evidence, size, weathering, photo reference",false);
        form.addView(text("One independent track set per entry",13));
        form.addView(type);form.addView(taxon);
        form.addView(text("Estimated age and identification confidence",13));
        form.addView(age);form.addView(confidence);
        form.addView(key);form.addView(substrate);form.addView(notes);
        ScrollView scroll=new ScrollView(this);scroll.addView(form);
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Record track or sign")
            .setView(scroll).setNegativeButton("Back",null)
            .setPositiveButton("Save sign",null).create();
        dialog.setOnShowListener(v->dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            .setOnClickListener(w->{
                if(!active()){toast("Survey closed");return;}
                String name=taxon.getText().toString().trim();
                if(name.isEmpty()){taxon.setError("Use unidentified if uncertain");return;}
                ContentValues row=new ContentValues();
                row.put("event_id",eventId);row.put("effort_id",effortId);
                row.put("recorded_at_utc",utc(System.currentTimeMillis()));
                row.put("sign_type",type.getSelectedItem().toString());
                row.put("taxon_label",name);
                row.put("age_class",age.getSelectedItem().toString());
                row.put("identification_confidence",confidence.getSelectedItem().toString());
                String eventKey=key.getText().toString().trim();
                if(!eventKey.isEmpty())row.put("independent_event_key",eventKey);
                row.put("substrate",substrate.getText().toString().trim());
                row.put("notes",notes.getText().toString().trim());
                stampGps(row);
                try{
                    long signId=db.getWritableDatabase().insertOrThrow(
                        "track_sign_encounters",null,row);
                    dialog.dismiss();draw();
                    new AlertDialog.Builder(this).setTitle("Sign saved")
                        .setMessage("GNSS quality was saved. Attach a photograph "+
                            "to this individual sign encounter?")
                        .setNegativeButton("Later",null)
                        .setPositiveButton("Attach photo",(d,i)->
                            choosePhoto("track_sign",signId)).show();
                }catch(Exception ex){key.setError("Encounter key already used in this survey");}
            }));
        dialog.show();
    }
    @Override protected void onDestroy(){
        db.close();super.onDestroy();
    }
}
