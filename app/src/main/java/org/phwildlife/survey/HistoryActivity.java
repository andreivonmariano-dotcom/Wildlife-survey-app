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
import android.text.TextWatcher;
import android.text.Editable;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.graphics.Typeface;
import android.view.View;
import android.widget.*;
import org.json.JSONObject;
import java.util.*;

public class HistoryActivity extends Activity {
    private FieldDb db;
    private LinearLayout rows,root;
    private String eventFilter;
    private String outcomeFilter="all";
    private String surveySearch="";
    private final Handler searchHandler=new Handler(Looper.getMainLooper());
    private Runnable pendingSearch;
    private static final String OUTCOME_SQL=SurveyOutcomeSql.RESOLVED;
    private static final String[] OUTCOME_LABELS={
        "All outcomes","Complete","Incomplete","Canceled / Not counted","Active"};
    private static final String[] OUTCOME_VALUES={
        "all","complete","incomplete","not_counted","active"};
    private static final int PAGE_SIZE=40;
    private int surveyOffset=0,observationOffset=0;
    private final int INK=UiKit.INK;
    private int dp(int value){return (int)(value*getResources().getDisplayMetrics().density+0.5f);}
    private LinearLayout column(){LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);return c;}
    private TextView text(String val,int size){TextView t=new TextView(this);t.setText(val);t.setTextSize(size);t.setTextColor(INK);t.setPadding(dp(5),dp(7),dp(5),dp(7));return t;}
    private Button btn(String val){return UiKit.button(this,val,"secondary");}
    private boolean largeText(){return getResources().getConfiguration().fontScale>=1.60f;}
    private int actionDp(){return FieldLayoutPolicy.actionHeightDp(
        getResources().getConfiguration().fontScale);}
    private EditText field(String hint,String init){
        EditText v=new EditText(this);
        v.setHint(hint);
        v.setText(init==null?"":init);
        v.setSingleLine(true);
        UiKit.fieldInput(this,v);
        return v;
    }
    @Override public void onCreate(Bundle b){super.onCreate(b);db=new FieldDb(this);eventFilter=getIntent().getStringExtra("event_id");draw();}
    @Override protected void onDestroy(){
        searchHandler.removeCallbacksAndMessages(null);
        db.close();
        super.onDestroy();
    }
    private void draw(){
        root=column();root.setBackgroundColor(UiKit.BG);
        root.setPadding(dp(LikasDesignSystem.SCREEN_GUTTER),
            dp(LikasDesignSystem.SPACE_MD),
            dp(LikasDesignSystem.SCREEN_GUTTER),
            dp(LikasDesignSystem.SPACE_SM));
        root.addView(UiKit.navigationHeader(this,
            eventFilter==null?(largeText()?"History":"Survey history"):"Observations",
            eventFilter==null?"Home":"All surveys",()->{
                if(eventFilter==null)finish();
                else{
                    if(pendingSearch!=null)searchHandler.removeCallbacks(pendingSearch);
                    eventFilter=null;surveyOffset=0;observationOffset=0;draw();
                }
            }));
        if(eventFilter==null){
            Spinner outcomes=new Spinner(this);
            outcomes.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item,OUTCOME_LABELS));
            for(int i=0;i<OUTCOME_VALUES.length;i++)
                if(OUTCOME_VALUES[i].equals(outcomeFilter)){outcomes.setSelection(i);break;}
            outcomes.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){
                @Override public void onItemSelected(android.widget.AdapterView<?> parent,
                        View view,int position,long id){
                    String selected=OUTCOME_VALUES[position];
                    if(selected.equals(outcomeFilter))return;
                    outcomeFilter=selected;surveyOffset=0;
                    if(rows!=null){rows.removeAllViews();surveys();}
                }
                @Override public void onNothingSelected(android.widget.AdapterView<?> parent){}
            });
            outcomes.setContentDescription("Filter saved surveys by outcome");
            root.addView(UiKit.caption(this,"Survey status"));
            root.addView(outcomes,new LinearLayout.LayoutParams(-1,dp(largeText()?72:56)));
            EditText search=new EditText(this);search.setSingleLine(true);
            search.setTextSize(LikasDesignSystem.TEXT_BODY);
            search.setHint("Search group, method, station, date");
            search.setContentDescription("Search survey group, method, station or date");
            UiKit.fieldInput(this,search);
            search.setText(surveySearch);
            LinearLayout searchRow=new LinearLayout(this);
            searchRow.setOrientation(LinearLayout.HORIZONTAL);
            searchRow.setGravity(Gravity.CENTER_VERTICAL);
            searchRow.addView(search,new LinearLayout.LayoutParams(0,-2,1f));
            Button clear=UiKit.button(this,"Clear","quiet");
            clear.setContentDescription("Clear survey search");
            LinearLayout.LayoutParams clearLp=new LinearLayout.LayoutParams(
                dp(76),-2);
            clearLp.leftMargin=dp(LikasDesignSystem.SPACE_SM);
            searchRow.addView(clear,clearLp);
            clear.setEnabled(!surveySearch.isEmpty());
            clear.setOnClickListener(v->search.setText(""));
            LinearLayout.LayoutParams searchLp=new LinearLayout.LayoutParams(-1,-2);
            searchLp.topMargin=dp(LikasDesignSystem.SPACE_SM);
            root.addView(searchRow,searchLp);
            search.addTextChangedListener(new TextWatcher(){
                public void beforeTextChanged(CharSequence s,int st,int n,int after){}
                public void onTextChanged(CharSequence s,int st,int before,int count){
                    surveySearch=s.toString().trim();
                    clear.setEnabled(!surveySearch.isEmpty());
                    if(pendingSearch!=null)searchHandler.removeCallbacks(pendingSearch);
                    pendingSearch=()->{
                        surveyOffset=0;
                        if(eventFilter==null&&rows!=null){rows.removeAllViews();surveys();}
                    };
                    searchHandler.postDelayed(pendingSearch,220L);
                }
                public void afterTextChanged(Editable value){}
            });
        }
        ScrollView scroll=new ScrollView(this);
        rows=column();scroll.addView(rows);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        setContentView(root);
        if(eventFilter==null)surveys();else observations(eventFilter);
    }
    private void surveys(){
        try(Cursor c=db.getReadableDatabase().rawQuery(
          "SELECT s.id,COALESCE(s.survey_profile,s.taxon_group,'Birds'),s.method,"+
          "COALESCE(s.station_id,''),s.started,s.ended,s.complete,COUNT(r.id),"+
          OUTCOME_SQL+" FROM surveys s "+
          "LEFT JOIN field_records r ON r.event_id=s.id AND r.deleted_at IS NULL "+
          "WHERE (?='all' OR "+OUTCOME_SQL+"=?) "+
          "AND (COALESCE(s.survey_profile,s.taxon_group,'Birds') LIKE ? OR "+
          "COALESCE(s.method,'') LIKE ? OR COALESCE(s.station_id,'') LIKE ? OR "+
          "COALESCE(s.started,'') LIKE ?) "+
          "GROUP BY s.id ORDER BY s.started DESC LIMIT ? OFFSET ?",
          new String[]{outcomeFilter,outcomeFilter,"%"+surveySearch+"%",
              "%"+surveySearch+"%","%"+surveySearch+"%","%"+surveySearch+"%",
              String.valueOf(PAGE_SIZE),String.valueOf(surveyOffset)})){
            int rowsThisPage=c.getCount();
            if(rowsThisPage==0&&surveyOffset==0)
                rows.addView(text("No matching surveys",15));
            while(c.moveToNext()){
                String event=c.getString(0),profile=c.getString(1),method=c.getString(2),site=c.getString(3),date=c.getString(4);
                String outcome=c.getString(8);
                String state="not_counted".equals(outcome)?"Not counted":
                    "complete".equals(outcome)?"Complete":
                    "incomplete".equals(outcome)?"Incomplete":
                    c.isNull(5)?"Active":c.getInt(6)==1?"Complete":"Incomplete";
                LinearLayout card=column();card.setPadding(
                    dp(LikasDesignSystem.SPACE_MD),dp(LikasDesignSystem.SPACE_SM),
                    dp(LikasDesignSystem.SPACE_MD),dp(LikasDesignSystem.SPACE_SM));
                card.setBackground(UiKit.surface(this));
                TextView name=text(profile+" · "+SurveyMethods.displayLabel(profile,method),16);
                name.setTypeface(null,Typeface.BOLD);card.addView(name);
                TextView meta=text((site.isEmpty()?"":site+" · ")+date+
                    "\n"+c.getInt(7)+" observations",12);
                meta.setTextSize(LikasDesignSystem.TEXT_CAPTION);
                meta.setTextColor(UiKit.MUTED);card.addView(meta);
                TextView badge=text(state+"  ›",13);badge.setTypeface(null,Typeface.BOLD);
                badge.setTextColor("Not counted".equals(state)?Color.rgb(163,58,48):
                    "Incomplete".equals(state)?UiKit.AMBER:UiKit.FOREST);
                card.addView(badge);
                card.setMinimumHeight(dp(100));card.setFocusable(true);
                card.setContentDescription(profile+", "+state+", "+c.getInt(7)+
                    " observations. Open survey.");
                LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
                lp.bottomMargin=dp(LikasDesignSystem.SPACE_SM);rows.addView(card,lp);
                card.setOnClickListener(v->{eventFilter=event;observationOffset=0;draw();});
            }
            if(rowsThisPage==PAGE_SIZE){
                Button more=UiKit.button(this,"Load older surveys","quiet");
                rows.addView(more);
                more.setOnClickListener(v->{rows.removeView(more);surveyOffset+=PAGE_SIZE;surveys();});
            }
        }
    }
    private void observations(String event){
        String outcome="active";
        String surveyMethod="";
        String surveyProfile="Birds";
        String exclusionReason="";
        try(Cursor meta=db.getReadableDatabase().rawQuery(
            "SELECT COALESCE(station_id,''),method,started,ended,"+
                "CASE WHEN outcome IS NOT NULL THEN outcome WHEN ended IS NULL "+
                "THEN 'active' WHEN COALESCE(complete,0)=1 THEN 'complete' "+
                "ELSE 'incomplete' END,COALESCE(exclusion_reason,''),"+
                "COALESCE(survey_profile,taxon_group,'Birds') "+
                "FROM surveys WHERE id=?",new String[]{event})){
            if(meta.moveToFirst()){
                outcome=meta.getString(4);
                surveyMethod=meta.getString(1);
                surveyProfile=meta.getString(6);
                exclusionReason=meta.getString(5);
                String state=outcome.equals("not_counted")?"Canceled · Not counted":
                    outcome.equals("incomplete")?"Incomplete":
                    outcome.equals("complete")?"Complete":"Active";
                LinearLayout summary=column();
                summary.setBackground(UiKit.surface(this));
                summary.setPadding(dp(LikasDesignSystem.SPACE_MD),
                    dp(LikasDesignSystem.SPACE_SM),dp(LikasDesignSystem.SPACE_MD),
                    dp(LikasDesignSystem.SPACE_SM));
                TextView stateTitle=text(state,LikasDesignSystem.TEXT_SECTION);
                stateTitle.setTypeface(null,Typeface.BOLD);
                stateTitle.setTextColor("Canceled · Not counted".equals(state)?
                    Color.rgb(163,58,48):"Incomplete".equals(state)?
                    UiKit.AMBER:UiKit.FOREST);
                summary.addView(stateTitle);
                TextView details=text(SurveyMethods.displayLabel(surveyProfile,meta.getString(1))+
                    (meta.getString(0).isEmpty()?"":"\nStation "+meta.getString(0))+
                    "\nStarted "+meta.getString(2)+
                    (meta.isNull(3)?"":"\nEnded "+meta.getString(3)),
                    LikasDesignSystem.TEXT_CAPTION);
                details.setTextColor(UiKit.MUTED);summary.addView(details);
                LinearLayout.LayoutParams summaryLp=new LinearLayout.LayoutParams(-1,-2);
                summaryLp.bottomMargin=dp(LikasDesignSystem.SPACE_SM);
                rows.addView(summary,summaryLp);
            }
        }
        if(outcome.equals("incomplete")||outcome.equals("not_counted")){
            boolean hasAudit=false;
            try(Cursor changes=db.getReadableDatabase().rawQuery(
                "SELECT reason,changed_at_utc,record_count "+
                "FROM survey_outcome_audit WHERE event_id=? "+
                "ORDER BY id DESC LIMIT 1",new String[]{event})){
                if(changes.moveToFirst()){
                    hasAudit=true;
                    String reason=changes.isNull(0)?"":changes.getString(0);
                    String changed=changes.isNull(1)?"":changes.getString(1);
                    int records=changes.getInt(2);
                    Button audit=UiKit.button(this,"Closure details","quiet");
                    LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(largeText()?actionDp():46));
                    rows.addView(audit,lp);
                    audit.setOnClickListener(v->new AlertDialog.Builder(this)
                        .setTitle("Survey closure audit")
                        .setMessage("Changed: "+changed+"\nReason: "+
                            (reason.trim().isEmpty()?"Not recorded":reason)+
                            "\nRecords at closure: "+records)
                        .setPositiveButton("Close",null).show());
                }
            }
            if(!hasAudit&&!exclusionReason.trim().isEmpty())
                rows.addView(text("Reason: "+exclusionReason,13));
        }
        boolean birdOrHerp="Birds".equals(surveyProfile)||
            "Herpetofauna".equals(surveyProfile);
        boolean trap=TrapProtocolPolicy.supported(surveyMethod);
        boolean field=FieldProtocolPolicy.supported(surveyMethod);
        if(birdOrHerp){
            Button sounds=UiKit.button(this,"Sound recordings","secondary");
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(largeText()?actionDp():48));
            lp.topMargin=dp(5);rows.addView(sounds,lp);
            sounds.setOnClickListener(v->startActivity(new Intent(this,CallLibraryActivity.class)
                .putExtra("event_id",event)));
        }
        if(trap||field){
            Button evidence=UiKit.button(this,trap?"Device checks & captures":"Method effort & evidence","secondary");
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(largeText()?actionDp():48));
            lp.topMargin=dp(5);rows.addView(evidence,lp);
            final Class<?> destination=trap?TrapProtocolActivity.class:FieldProtocolsActivity.class;
            evidence.setOnClickListener(v->startActivity(new Intent(this,destination)
                .putExtra("event_id",event)));
        }
        if("point_count".equals(surveyMethod)||SurveyMethods.timedInterval(surveyMethod)){
            Button progress=UiKit.button(this,"Station & effort progress","quiet");
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(largeText()?actionDp():48));
            lp.topMargin=dp(5);rows.addView(progress,lp);
            progress.setOnClickListener(v->startActivity(new Intent(this,StationProgressActivity.class)
                .putExtra("event_id",event)));
        }
        appendObservations(event);
    }
    private void appendObservations(String event){
        try(Cursor c=db.getReadableDatabase().rawQuery(
            "SELECT r.id,COALESCE(r.raw_identification,''),COALESCE(r.taxon_group,''),r.individual_count,r.horizontal_m,COALESCE(r.detection_method,''),r.detected_at,COALESCE(p.station_id,u.station_id,''),COALESCE(p.sequence_number,u.sequence_number,0),r.point_count_id,r.sampling_unit_id FROM field_records r LEFT JOIN point_counts p ON p.id=r.point_count_id LEFT JOIN sampling_units u ON u.id=r.sampling_unit_id WHERE r.event_id=? AND r.deleted_at IS NULL ORDER BY COALESCE(p.sequence_number,u.sequence_number,0),r.detected_at,r.id LIMIT ? OFFSET ?",
            new String[]{event,String.valueOf(PAGE_SIZE),String.valueOf(observationOffset)})){
            int pageCount=c.getCount();
            if(observationOffset==0)
                rows.addView(FieldVisuals.section(this,"Observations",
                    "Tap a record to review evidence, photographs or corrections"));
            if(pageCount==0&&observationOffset==0)
                rows.addView(text("No detections recorded. The survey event remains saved.",14));
            while(c.moveToNext()){
                long record=c.getLong(0);
                String dist=c.isNull(4)?"distance unknown":String.format(Locale.US,"%.1f m",c.getDouble(4));
                String point=c.getInt(8)>0?"Station "+c.getString(7)+" · "+(!c.isNull(9)?"Point ":"Visit ")+c.getInt(8)+"\n":"";
                LinearLayout card=column();card.setPadding(dp(13),dp(8),dp(13),dp(8));
                card.setBackground(UiKit.surface(this));
                TextView name=text(c.getString(1)+"  ×"+c.getInt(3),16);
                name.setTypeface(null,Typeface.BOLD);card.addView(name);
                TextView detail=text(point+c.getString(2)+" · "+c.getString(5)+
                    " · "+dist+"\n"+c.getString(6),12);
                detail.setTextColor(UiKit.MUTED);card.addView(detail);
                card.setMinimumHeight(dp(85));card.setFocusable(true);
                card.setContentDescription(c.getString(1)+", "+c.getInt(3)+
                    " individuals, "+dist+". Review observation and edit history.");
                LinearLayout.LayoutParams cardLp=new LinearLayout.LayoutParams(-1,-2);
                cardLp.bottomMargin=dp(7);rows.addView(card,cardLp);
                card.setOnClickListener(v->recordDetail(record));
            }
            if(pageCount==PAGE_SIZE){
                Button more=UiKit.button(this,"Load more observations","quiet");
                rows.addView(more);
                more.setOnClickListener(v->{
                    rows.removeView(more);observationOffset+=PAGE_SIZE;appendObservations(event);
                });
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
        try(Cursor c=db.getReadableDatabase().rawQuery(
            "SELECT * FROM field_records WHERE id=? AND deleted_at IS NULL",
            new String[]{String.valueOf(id)})){
            if(!c.moveToFirst()){draw();return;}
            JSONObject original=snapshot(c);
            String title=original.optString("raw_identification","Observation "+id);
            new AlertDialog.Builder(this).setTitle(title)
              .setItems(new String[]{"View recorded details","Edit observation",
                  "Photos","Edit history","Delete observation"},(dialog,index)->{
                  if(index==0)showRecordDetails(id,original);
                  else if(index==1)edit(id,original);
                  else if(index==2)viewPhotos(id);
                  else if(index==3)audit(id);
                  else deletePrompt(id,original);
              })
              .setNegativeButton("Back",null).show();
        }catch(Exception e){Toast.makeText(this,"Cannot open record: "+e.getMessage(),Toast.LENGTH_LONG).show();}
    }
    /** Present exactly the stored observation evidence, with missing values explicit. */
    private static String observed(JSONObject source,String key){
        if(!source.has(key)||source.isNull(key))return "Not recorded";
        String value=source.optString(key,"").trim();
        return value.isEmpty()?"Not recorded":value;
    }
    private void evidenceLine(LinearLayout parent,String label,String value){
        TextView heading=UiKit.caption(this,label);
        heading.setPadding(0,dp(LikasDesignSystem.SPACE_SM),0,0);
        parent.addView(heading);
        TextView data=text(value,LikasDesignSystem.TEXT_BODY);
        data.setPadding(0,0,0,0);
        parent.addView(data);
    }
    private void showRecordDetails(long id,JSONObject source){
        LinearLayout contents=column();
        contents.setPadding(dp(LikasDesignSystem.SPACE_LG),
            dp(LikasDesignSystem.SPACE_SM),
            dp(LikasDesignSystem.SPACE_LG),
            dp(LikasDesignSystem.SPACE_MD));
        evidenceLine(contents,"Observation ID",String.valueOf(id));
        evidenceLine(contents,"Current recorded identification",observed(source,"raw_identification"));
        evidenceLine(contents,"Scientific name recorded",observed(source,"scientific_name"));
        evidenceLine(contents,"Taxonomic group",observed(source,"taxon_group"));
        evidenceLine(contents,"Individuals recorded",observed(source,"individual_count"));
        evidenceLine(contents,"Detection method",observed(source,"detection_method"));
        evidenceLine(contents,"Detected at",observed(source,"detected_at"));
        evidenceLine(contents,"Horizontal distance (m)",observed(source,"horizontal_m"));
        evidenceLine(contents,"Distance source",observed(source,"distance_method"));
        evidenceLine(contents,"Identification confidence",observed(source,"identification_confidence"));
        evidenceLine(contents,"Identification evidence",observed(source,"identification_review_note"));
        evidenceLine(contents,"Habitat / microhabitat",observed(source,"habitat"));
        evidenceLine(contents,"Behavior",observed(source,"behavior"));
        evidenceLine(contents,"Field notes",observed(source,"notes"));
        evidenceLine(contents,"Observer latitude",observed(source,"observer_lat"));
        evidenceLine(contents,"Observer longitude",observed(source,"observer_lon"));
        evidenceLine(contents,"GPS accuracy (m)",observed(source,"gps_accuracy_m"));
        ScrollView scroll=new ScrollView(this);
        scroll.addView(contents);
        new AlertDialog.Builder(this).setTitle("Recorded observation")
            .setView(scroll).setPositiveButton("Close",null).show();
    }
    private void viewPhotos(long record){
        java.util.ArrayList<String> names=new java.util.ArrayList<>();
        try(Cursor c=db.getReadableDatabase().rawQuery(
            "SELECT file_name FROM observation_media WHERE record_id=? ORDER BY id",
            new String[]{String.valueOf(record)})){
            while(c.moveToNext())names.add(c.getString(0));
        }
        if(names.isEmpty()){Toast.makeText(this,"No photographs linked to this observation",Toast.LENGTH_SHORT).show();return;}
        LinearLayout column=column();column.setPadding(dp(10),dp(10),dp(10),dp(10));
        for(String name:names){
            ImageView image=new ImageView(this);image.setAdjustViewBounds(true);
            image.setMaxHeight(dp(400));PhotoEvidence.preview(this,name,image);
            column.addView(image);
            column.addView(text(name,11));
        }
        ScrollView scroll=new ScrollView(this);scroll.addView(column);
        new AlertDialog.Builder(this).setTitle("Observation photographs").setView(scroll)
          .setPositiveButton("Close",null).show();
    }
    private void edit(long id,JSONObject before) {
        LinearLayout form=column();form.setPadding(dp(14),0,dp(14),0);
        EditText taxon=field("Species or unidentified taxon",before.optString("raw_identification",""));
        EditText individuals=field("Individuals",String.valueOf(before.optInt("individual_count",1)));
        individuals.setInputType(InputType.TYPE_CLASS_NUMBER);
        EditText distance=field("Horizontal distance (m)",before.isNull("horizontal_m")?"":before.optString("horizontal_m"));
        distance.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        EditText notes=field("Natural history / notes",before.optString("notes",""));
        notes.setSingleLine(false);notes.setMinLines(3);
        notes.setInputType(InputType.TYPE_CLASS_TEXT|
            InputType.TYPE_TEXT_FLAG_MULTI_LINE|
            InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        EditText reason=field("Reason for correction (recommended)","");
        EditText confidenceNote=field("Identification evidence / notes",before.optString("identification_review_note",""));
        Spinner confidence=new Spinner(this);
        String[] levels={"Field-identified (not expert verified)","Probable","Uncertain","Unidentified"};
        confidence.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,levels));
        String oldConfidence=before.optString("identification_confidence","Field-identified (not expert verified)");
        for(int i=0;i<levels.length;i++)if(levels[i].equals(oldConfidence))confidence.setSelection(i);
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
        form.addView(text("Confidence",13));form.addView(confidence);form.addView(confidenceNote);
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
                boolean unknown=Taxa.isUnknown(name);
                boolean genusOnly=!unknown&&split[1]!=null&&(split[1].trim().endsWith(" sp.")||split[1].trim().endsWith(" spp."));
                String rank=unknown?"unidentified":genusOnly?"genus":
                    split[1]==null||split[1].trim().isEmpty()?"unverified_text":"species_name_unverified";
                values.put("identification_level",rank);
                String confidenceValue=unknown?"Unidentified":confidence.getSelectedItem().toString();
                values.put("identification_confidence",confidenceValue);
                values.put("identification_review_note",confidenceNote.getText().toString().trim());
                String geo=before.optString("geo_region_at_detection","PH");
                values.put("regional_evidence_status",
                    GeoRegions.hasPositiveEvidence(before.optString("taxon_group","Birds"),geo,split[1])?
                        "documented_regional_example":"distribution_not_assessed");
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
                boolean candidate=before.optString("taxon_group","Birds").equals("Birds")&&
                    !unknown&&!genusOnly&&!confidenceValue.equals("Uncertain")&&!confidenceValue.equals("Unidentified")&&
                    split[1]!=null&&!split[1].isEmpty()&&d!=null&&
                    first.getSelectedItem().toString().equals("Yes")
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
        try(Cursor c=db.getReadableDatabase().rawQuery(
            "SELECT edited_at,action,COALESCE(reason,''),previous_json,current_json "+
            "FROM record_edits WHERE record_id=? ORDER BY id DESC",
            new String[]{String.valueOf(id)})){
            if(c.getCount()==0)msg.append("No changes recorded.");
            while(c.moveToNext()){
                msg.append(c.getString(0)).append(" · ").append(c.getString(1)).append("\n");
                if(!c.getString(2).isEmpty())
                    msg.append("Reason: ").append(c.getString(2)).append("\n");
                msg.append("Before:\n")
                    .append(c.isNull(3)?"Not recorded":c.getString(3))
                    .append("\nAfter:\n")
                    .append(c.isNull(4)?"Not recorded":c.getString(4))
                    .append("\n\n");
            }
        }
        TextView history=text(msg.toString(),LikasDesignSystem.TEXT_CAPTION);
        history.setTextIsSelectable(true);
        history.setPadding(dp(LikasDesignSystem.SPACE_LG),
            dp(LikasDesignSystem.SPACE_MD),dp(LikasDesignSystem.SPACE_LG),
            dp(LikasDesignSystem.SPACE_MD));
        ScrollView scroll=new ScrollView(this);scroll.addView(history);
        new AlertDialog.Builder(this).setTitle("Record edit history")
            .setView(scroll).setPositiveButton("Close",null).show();
    }
}
