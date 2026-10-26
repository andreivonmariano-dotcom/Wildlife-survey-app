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

/** Home and survey setup. Never starts the camera or a survey without a user choice. */
public class WelcomeActivity extends Activity {
    private final int BG=Color.rgb(245,249,245),INK=Color.rgb(23,58,39),GREEN=Color.rgb(34,102,72),MUTED=Color.rgb(99,116,104);
    private LinearLayout main,root; private String profile="Birds";
    private boolean custom=false;
    private EditText stationEntry,durationEntry,observerEntry,lineBearingEntry;
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
        heading("Philippine Wildlife Survey","Offline field recording for Philippine birds, herpetofauna and mammals.");
        String active=activeSurvey();
        if(active!=null){
            TextView label=text("A survey is still active. Resume it before starting another.",14,true);
            main.addView(label);
            Button resume=button("Resume active survey");
            resume.setOnClickListener(v->startActivity(new Intent(this,MainActivity.class).putExtra("resume",true)));
            main.addView(resume);
            Space pad=new Space(this);main.addView(pad,new LinearLayout.LayoutParams(1,dp(16)));
            TextView tip=text("Finish the current survey to start a different survey.",12,false);
            tip.setTextColor(MUTED);main.addView(tip);
            return;
        }
        main.addView(text("Start a survey",18,true));
        card("Bird survey","Point counts, line transects, area searches and opportunistic records.",()->setup("Birds",false));
        card("Herpetofauna survey","Reptiles and amphibians together. Switch between them during the same survey.",()->setup("Herpetofauna",false));
        card("Mammal survey","Direct observations, sign surveys, camera trap and bat records.",()->setup("Mammals",false));
        card("Custom survey","Select one taxonomic category, protocol, station and timed survey duration.",()->customPicker());
        TextView tip=text("The camera stays off until you open it from a survey.",12,false);
        tip.setTextColor(MUTED);main.addView(tip);
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
        stationEntry=new EditText(this);
        stationEntry.setTextSize(16);stationEntry.setSingleLine(true);
        stationEntry.setHint("Station / site ID (e.g. PC-01)");
        main.addView(stationEntry);
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
        TextView help=text("Standardized methods retain survey effort separately. Basic-log methods need more equipment/effort fields before formal analysis.",12,false);
        help.setTextColor(MUTED);main.addView(help);
        Button start=button("Start survey");
        LinearLayout.LayoutParams startLp=new LinearLayout.LayoutParams(-1,dp(58));
        startLp.topMargin=dp(16);main.addView(start,startLp);
        start.setOnClickListener(v->startSelected());
    }
    private void startSelected(){
        int choice=methodChoices.getCheckedRadioButtonId()-1000;
        if(choice<0||choice>=choices.length){Toast.makeText(this,"Select survey method",Toast.LENGTH_SHORT).show();return;}
        SurveyMethods.Method m=choices[choice];
        String station=stationEntry.getText().toString().trim();
        if(station.isEmpty()&&SurveyMethods.structured(m.code)){
            stationEntry.setError("Station/site ID required for a structured survey");
            stationEntry.requestFocus();return;
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
