package org.phwildlife.survey;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.util.Locale;
import java.util.ArrayList;

/** Home and survey setup. Never starts the camera or a survey without a user choice. */
public class WelcomeActivity extends Activity {
    private final int BG=Color.rgb(245,249,245),INK=Color.rgb(23,58,39),GREEN=Color.rgb(34,102,72),MUTED=Color.rgb(99,116,104);
    private LinearLayout main,root; private String profile="Birds";
    private boolean custom=false;
    private EditText stationEntry,durationEntry,observerEntry,lineBearingEntry,regionEntry,areaEntry;
    private Spinner projectChoice;
    private ArrayList<String> projectKeys=new ArrayList<>();
    private RadioGroup methodChoices;
    private SurveyMethods.Method[] choices;
    private int dp(float f){return (int)(f*getResources().getDisplayMetrics().density+0.5f);}
    private TextView text(String s,int sz,boolean strong) {
        TextView v=new TextView(this);v.setText(s);v.setTextSize(sz);v.setTextColor(INK);
        if(strong)v.setTypeface(null,Typeface.BOLD);
        v.setPadding(dp(3),dp(8),dp(3),dp(8));return v;
    }
    private Button button(String label){
        Button b=new Button(this);b.setText(label);b.setTextSize(16);b.setAllCaps(false);
        b.setMinHeight(dp(56));b.setTextColor(Color.WHITE);
        b.setBackgroundTintList(ColorStateList.valueOf(GREEN));return b;
    }
    private LinearLayout vertical(){LinearLayout v=new LinearLayout(this);v.setOrientation(LinearLayout.VERTICAL);return v;}
    @Override public void onCreate(Bundle state){super.onCreate(state);buildShell();home();}
    @Override protected void onResume(){super.onResume();if(root!=null)home();}
    private boolean homeVisible=true;
    private void buildShell(){
        root=vertical();root.setBackgroundColor(BG);root.setPadding(dp(18),dp(18),dp(18),dp(18));
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);
        main=vertical();main.setPadding(0,0,0,dp(22));scroll.addView(main);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,-1));
        setContentView(root);
    }
    private void heading(String title,String subtitle) {
        main.addView(text(title,28,true));
        TextView s=text(subtitle,14,false);s.setTextColor(MUTED);main.addView(s);
        Space space=new Space(this);main.addView(space,new LinearLayout.LayoutParams(1,dp(20)));
    }
    private void home(){
        homeVisible=true;main.removeAllViews();
        heading("Philippine Wildlife Survey","Field surveys · Offline recording · Reviewable records");
        if(activeSurvey()!=null){
            main.addView(text("An unfinished survey is available. Resume it to continue recording.",14,true));
            Button resume=button("Resume active survey");
            resume.setOnClickListener(v->startActivity(new Intent(this,MainActivity.class).putExtra("resume",true)));
            main.addView(resume);
            main.addView(text("Finish the active survey to start a different survey.",12,false));
        } else {
            main.addView(text("New survey",18,true));
            card("Bird survey","Point counts, line transects and observations.",()->setup("Birds",false));
            card("Herpetofauna survey","Reptiles and amphibians under a suitable shared method.",()->setup("Herpetofauna",false));
            card("Mammal survey","Direct observations, tracks and signs, specialist logs.",()->setup("Mammals",false));
            card("Custom survey","Choose category, method, station and duration.",()->customPicker());
        }
        main.addView(text("Field data & management",18,true));
        card("Survey history","Review visits, edit records and inspect correction history.",
            ()->startActivity(new Intent(this,HistoryActivity.class)));
        card("Projects & stations","Manage projects, reusable stations and habitat details.",
            ()->startActivity(new Intent(this,SitesActivity.class)));
        main.addView(text("Recent surveys",18,true));
        recentSurveys();
        TextView note=text("The camera stays off until you open it during a survey.",12,false);
        note.setTextColor(MUTED);main.addView(note);
    }
    private void recentSurveys(){
        try{
            String path=getDatabasePath(FieldDb.NAME).getAbsolutePath();
            if(!new java.io.File(path).exists()){main.addView(text("No surveys yet.",13,false));return;}
            try(SQLiteDatabase db=SQLiteDatabase.openDatabase(path,null,SQLiteDatabase.OPEN_READONLY);
                Cursor c=db.rawQuery("SELECT id,COALESCE(survey_profile,taxon_group,'Birds'),COALESCE(station_id,''),started,ended FROM surveys ORDER BY started DESC LIMIT 5",null)){
                if(c.getCount()==0)main.addView(text("No surveys yet.",13,false));
                while(c.moveToNext()){
                    final String id=c.getString(0);
                    String profile=c.getString(1),station=c.getString(2),date=c.getString(3);
                    String status=c.isNull(4)?"Active":"Saved";
                    card(profile+" · "+(station.isEmpty()?"No station":station),
                        (date==null?"":date.substring(0,Math.min(10,date.length())))+" · "+status,
                        ()->startActivity(new Intent(this,HistoryActivity.class).putExtra("event_id",id)));
                }
            }
        }catch(Exception e){main.addView(text("History is accessible from the management screen.",12,false));}
    }
    private void card(String name,String desc,Runnable action){
        LinearLayout wrapper=vertical();
        wrapper.setPadding(dp(12),dp(11),dp(12),dp(13));
        android.graphics.drawable.GradientDrawable bg=new android.graphics.drawable.GradientDrawable();
        bg.setColor(Color.WHITE);bg.setCornerRadius(dp(14));bg.setStroke(dp(1),Color.rgb(218,232,219));
        wrapper.setBackground(bg);
        TextView h=text(name+"  ›",19,true);wrapper.addView(h);
        TextView sub=text(desc,13,false);sub.setTextColor(MUTED);wrapper.addView(sub);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(11);
        main.addView(wrapper,lp);wrapper.setOnClickListener(v->action.run());
    }
    private void customPicker(){
        homeVisible=false;main.removeAllViews();
        Button back=button("← Back");main.addView(back);back.setOnClickListener(v->home());
        heading("Custom survey","Choose one category. The survey will use only methods suited to that category.");
        card("Birds","Custom bird survey",()->setup("Birds",true));
        card("Herpetofauna","Custom reptile and amphibian survey",()->setup("Herpetofauna",true));
        card("Mammals","Custom mammal survey",()->setup("Mammals",true));
    }
    private void setup(String selected,boolean isCustom){
        homeVisible=false;profile=selected;custom=isCustom;
        main.removeAllViews();
        Button back=button("← Back");main.addView(back);
        back.setOnClickListener(v->{if(custom)customPicker();else home();});
        heading((custom?"Custom · ":"")+profile+" survey","Select a field method, then start recording.");
        choices=SurveyMethods.forProfile(profile);
        methodChoices=new RadioGroup(this);methodChoices.setOrientation(RadioGroup.VERTICAL);
        for(int i=0;i<choices.length;i++){
            SurveyMethods.Method m=choices[i];
            String label=m.label+(m.basicLog?" · basic log":"");
            RadioButton rb=new RadioButton(this);
            rb.setId(1000+i);rb.setText(label);rb.setTextSize(16);rb.setTextColor(INK);
            rb.setPadding(dp(4),dp(10),dp(4),dp(10));
            methodChoices.addView(rb);
            TextView desc=text(m.description,12,false);
            desc.setTextColor(MUTED);desc.setPadding(dp(36),0,dp(4),dp(7));
            methodChoices.addView(desc);
        }
        methodChoices.check(1000);
        main.addView(text("Survey method",18,true));main.addView(methodChoices);
        main.addView(text("Survey details",18,true));
        projectChoice=new Spinner(this);
        projectKeys.clear();
        ArrayList<String> names=new ArrayList<>();
        try(FieldDb db=new FieldDb(this);
            Cursor c=db.getReadableDatabase().rawQuery("SELECT id,name FROM projects ORDER BY name",null)){
            while(c.moveToNext()){projectKeys.add(c.getString(0));names.add(c.getString(1));}
        }
        if(names.isEmpty()){projectKeys.add("default");names.add("General field project");}
        projectChoice.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,names));
        main.addView(text("Project",13,false));main.addView(projectChoice);
        stationEntry=new AutoCompleteTextView(this);stationEntry.setThreshold(1);
        stationEntry.setTextSize(16);stationEntry.setSingleLine(true);
        stationEntry.setHint("Registered station ID");
        main.addView(stationEntry);
        projectChoice.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){
            public void onNothingSelected(android.widget.AdapterView<?> p){}
            public void onItemSelected(android.widget.AdapterView<?> p,View v,int i,long id){updateStations();}
        });
        Button manage=button("Manage stations");
        manage.setOnClickListener(v->startActivity(new Intent(this,SitesActivity.class)));
        main.addView(manage);
        TextView note=text("Registered stations are required for structured surveys.",12,false);
        note.setTextColor(MUTED);main.addView(note);
        observerEntry=new EditText(this);
        observerEntry.setTextSize(16);observerEntry.setSingleLine(true);
        observerEntry.setHint("Observer (optional)");
        main.addView(observerEntry);
        durationEntry=new EditText(this);
        durationEntry.setTextSize(16);durationEntry.setInputType(InputType.TYPE_CLASS_NUMBER);
        durationEntry.setHint("Point count duration (minutes)");
        durationEntry.setText("10");
        main.addView(durationEntry);
        lineBearingEntry=new EditText(this);
        lineBearingEntry.setSingleLine(true);
        lineBearingEntry.setTextSize(16);
        lineBearingEntry.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        lineBearingEntry.setHint("Transect magnetic bearing (0–359°)");
        main.addView(lineBearingEntry);
        methodChoices.setOnCheckedChangeListener((group,checked)->{
            int choice=checked-1000;
            if(choice>=0&&choice<choices.length){
                durationEntry.setVisibility(choices[choice].code.equals("point_count")?View.VISIBLE:View.GONE);
                lineBearingEntry.setVisibility(profile.equals("Birds")&&choices[choice].code.equals("line_transect")?View.VISIBLE:View.GONE);
            }
        });
        durationEntry.setVisibility(choices[0].code.equals("point_count")?View.VISIBLE:View.GONE);
        lineBearingEntry.setVisibility(View.GONE);
        regionEntry=new EditText(this);regionEntry.setSingleLine(true);
        regionEntry.setHint("Stratum / region label (optional)");
        main.addView(regionEntry);
        areaEntry=new EditText(this);areaEntry.setSingleLine(true);
        areaEntry.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        areaEntry.setHint("Stratum area (km², optional)");
        main.addView(areaEntry);
        TextView help=text("Standardized methods retain survey effort separately. Basic-log methods need more equipment/effort fields before formal analysis.",12,false);
        help.setTextColor(MUTED);main.addView(help);
        Button start=button("Start survey");
        LinearLayout.LayoutParams startLp=new LinearLayout.LayoutParams(-1,dp(58));
        startLp.topMargin=dp(16);main.addView(start,startLp);
        start.setOnClickListener(v->startSelected());
    }
    private String projectId(){
        int index=projectChoice==null?-1:projectChoice.getSelectedItemPosition();
        return index<0||index>=projectKeys.size()?"default":projectKeys.get(index);
    }
    private void updateStations(){
        if(stationEntry==null)return;
        ArrayList<String> ids=new ArrayList<>();
        try(FieldDb db=new FieldDb(this);
            Cursor c=db.getReadableDatabase().rawQuery("SELECT id FROM sites WHERE project_id=? ORDER BY id",new String[]{projectId()})){
            while(c.moveToNext())ids.add(c.getString(0));
        }
        stationEntry.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line,ids));
        stationEntry.setText("");
    }
    private void startSelected(){
        int choice=methodChoices.getCheckedRadioButtonId()-1000;
        if(choice<0||choice>=choices.length){Toast.makeText(this,"Select survey method",Toast.LENGTH_SHORT).show();return;}
        SurveyMethods.Method m=choices[choice];
        String station=stationEntry.getText().toString().trim();
        if(SurveyMethods.structured(m.code)){
            if(station.isEmpty()){stationEntry.setError("Select a registered station ID");stationEntry.requestFocus();return;}
            int found=0;
            try(FieldDb db=new FieldDb(this);
                Cursor c=db.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM sites WHERE id=? AND project_id=?",new String[]{station,projectId()})){
                if(c.moveToFirst())found=c.getInt(0);
            }
            if(found==0){stationEntry.setError("Register station in Projects & stations first");stationEntry.requestFocus();return;}
        }
        Double area=null;
        try{if(!areaEntry.getText().toString().trim().isEmpty())area=Double.parseDouble(areaEntry.getText().toString().trim());}catch(Exception ignored){}
        if(!areaEntry.getText().toString().trim().isEmpty()&&(area==null||area<=0)){
            areaEntry.setError("Enter positive area in km²");return;
        }
        int duration=600;
        if("point_count".equals(m.code)){
            try{duration=Integer.parseInt(durationEntry.getText().toString().trim())*60;}
            catch(Exception ignored){duration=-1;}
            if(duration<60||duration>7200){
                durationEntry.setError("Enter 1 to 120 minutes");durationEntry.requestFocus();return;
            }
        }
        if(profile.equals("Birds")&&m.code.equals("line_transect")){
            try{
                double az=Double.parseDouble(lineBearingEntry.getText().toString().trim());
                if(az<0||az>=360)throw new NumberFormatException();
            }catch(Exception ex){
                lineBearingEntry.setError("Enter a magnetic transect bearing from 0–359°");
                lineBearingEntry.requestFocus();return;
            }
        }
        Intent i=new Intent(this,MainActivity.class);
        i.putExtra("transect_bearing",lineBearingEntry.getText().toString().trim());
        i.putExtra("create_new",true);
        i.putExtra("survey_profile",profile);
        i.putExtra("project_id",projectId());
        i.putExtra("region_label",regionEntry.getText().toString().trim());
        i.putExtra("stratum_area_km2",area==null?Double.NaN:area);
        i.putExtra("survey_method",m.code);
        i.putExtra("station_id",station);
        i.putExtra("observer_name",observerEntry.getText().toString().trim());
        i.putExtra("point_target_s",duration);
        i.putExtra("custom_survey",custom);
        i.putExtra("basic_log_only",m.basicLog);
        startActivity(i);
    }
    private String activeSurvey(){
        try{
            String path=getDatabasePath("wildlife_field_v03.db").getAbsolutePath();
            java.io.File file=new java.io.File(path);if(!file.exists())return null;
            try(SQLiteDatabase db=SQLiteDatabase.openDatabase(path,null,SQLiteDatabase.OPEN_READONLY);
                Cursor c=db.rawQuery("SELECT id FROM surveys WHERE ended IS NULL ORDER BY started DESC LIMIT 1",null)){
                if(c.moveToFirst())return c.getString(0);
            }
        }catch(Exception ignored){}
        return null;
    }
}
