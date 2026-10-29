package org.phwildlife.survey;

import android.app.Activity;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.os.Handler;
import android.os.Looper;
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
    private final int BG=UiKit.BG,INK=UiKit.INK,GREEN=UiKit.FOREST,MUTED=UiKit.MUTED;
    private LinearLayout main,root,setupFooter;
    private Button setupStartButton;
    private String profile="Birds";
    private boolean custom=false;
    private String currentScreen="home";
    private static final int GPS_PERMISSION_REQUEST=71;
    private final Handler gpsHandler=new Handler(Looper.getMainLooper());
    private LocationListener pendingGpsListener;
    private int gpsLookupGeneration=0;
    private boolean gpsPolygonResolving=false;
    private Runnable gpsTimeout;
    private Button gpsLookupButton;
    private EditText durationEntry,observerEntry,lineBearingEntry,regionEntry,areaEntry,radiusEntry,transectDurationEntry,routeLengthEntry,customSpacingEntry;
    private Spinner spacingChoice,templateChoice;
    private final ArrayList<String> templateIds=new ArrayList<>();
    private EditText templateName;
    private CheckBox saveTemplateChoice;
    private TextView templateHint;
    private boolean updatingRouteChoices=false;
    private TextView stationCountPreview,routeHeader,spacingHeader,repeatHeader;
    private Spinner projectChoice, geographicChoice,studyChoice;
    private TextView studyToggle;
    private LinearLayout studyOptions;
    private final ArrayList<String> studyTemplateIds=new ArrayList<>();
    private BoundaryLookup.Match suggested;
    private String geographicMethod=GeoRegions.BOUNDARY_METHOD;
    private TextView boundaryMessage;
    private ArrayList<String> projectKeys=new ArrayList<>();
    private RadioGroup methodChoices;
    private SurveyMethods.Method[] choices;
    private int dp(float f){return (int)(f*getResources().getDisplayMetrics().density+0.5f);}
    private TextView text(String s,int sz,boolean strong) {
        TextView v=new TextView(this);v.setText(s);v.setTextSize(sz);v.setTextColor(INK);
        if(strong)v.setTypeface(null,Typeface.BOLD);
        v.setPadding(dp(3),dp(8),dp(3),dp(8));return v;
    }
    private Button button(String label){return UiKit.button(this,label,"secondary");}
    private boolean largeText(){return getResources().getConfiguration().fontScale>=1.60f;}
    private int actionDp(){return FieldLayoutPolicy.actionHeightDp(
        getResources().getConfiguration().fontScale);}
    private LinearLayout vertical(){LinearLayout v=new LinearLayout(this);v.setOrientation(LinearLayout.VERTICAL);return v;}
    @Override public void onCreate(Bundle state){super.onCreate(state);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        buildShell();
        String screen=getPreferences(MODE_PRIVATE).getString("screen","home");
        String rememberedProfile=getPreferences(MODE_PRIVATE).getString("profile","Birds");
        boolean rememberedCustom=getPreferences(MODE_PRIVATE).getBoolean("custom",false);
        boolean showMenu=getIntent().getBooleanExtra("show_menu",false);
        if(showMenu)home();
        else if(screen.equals("setup")&&!isSurveyActive()){setup(rememberedProfile,rememberedCustom);restoreSetup();}
        else if(screen.equals("custom")&&!isSurveyActive())customPicker();
        else home();
        if(!showMenu&&isSurveyActive()&&
            getSharedPreferences("field_resume",MODE_PRIVATE).getString("screen","home").equals("survey"))
            startActivity(new Intent(this,MainActivity.class).putExtra("resume",true));
    }
    @Override protected void onResume(){
        super.onResume();
        if(root!=null&&(homeVisible||"fieldwork".equals(currentScreen))){
            String current=activeSurvey();
            if(!java.util.Objects.equals(current,lastHomeActiveSurvey)){
                if(homeVisible)home();else fieldworkHub();
            }
        }else if(root!=null&&"setup".equals(currentScreen)&&projectChoice!=null){
            refreshProjectOptions();refreshStudyChoices();
        }
    }
    @Override protected void onPause(){
        cancelGpsLookup();persistSetup();super.onPause();
    }
    @Override protected void onNewIntent(Intent intent){super.onNewIntent(intent);setIntent(intent);home();}
    @Override public void onBackPressed(){
        if("setup".equals(currentScreen)){
            if(custom)customPicker();else fieldworkHub();
        }else if("custom".equals(currentScreen)){
            fieldworkHub();
        }else if("fieldwork".equals(currentScreen)){
            home();
        }else{
            super.onBackPressed();
        }
    }
    private boolean isSurveyActive(){return activeSurvey()!=null;}
    private boolean homeVisible=true;
    private boolean playedStartupHomeTransition=false;
    private String lastHomeActiveSurvey=null;
    private void buildShell(){
        root=vertical();root.setBackgroundColor(BG);root.setPadding(dp(16),dp(12),dp(16),dp(12));
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);
        main=vertical();main.setPadding(0,0,0,dp(24));scroll.addView(main);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1f));
        // One persistent GPS continuation action; never duplicate it in the form.
        setupFooter=vertical();
        setupFooter.setBackgroundColor(BG);
        setupFooter.setPadding(0,dp(5),0,dp(3));
        setupStartButton=UiKit.button(this,"Continue to GPS","primary");
        setupFooter.addView(setupStartButton,new LinearLayout.LayoutParams(-1,dp(actionDp())));
        setupStartButton.setOnClickListener(v->startSelected());
        setupFooter.setVisibility(View.GONE);
        root.addView(setupFooter);
        setContentView(root);
    }
    private void heading(String title,String subtitle) {
        TextView h=text(title,24,true);h.setPadding(dp(2),dp(6),0,0);
        main.addView(h);
        if(subtitle!=null&&!subtitle.isEmpty()){
            TextView s=text(subtitle,13,false);s.setTextColor(MUTED);
            s.setPadding(dp(2),dp(2),0,dp(10));main.addView(s);
        }
    }
    private TextView step(String number,String title){
        TextView label=text(number+"    "+title,16,true);
        label.setTextColor(UiKit.FOREST);
        label.setLetterSpacing(.025f);
        label.setMinHeight(dp(52));
        label.setGravity(Gravity.CENTER_VERTICAL);
        label.setPadding(dp(14),dp(9),dp(12),dp(9));
        android.graphics.drawable.GradientDrawable bg=new android.graphics.drawable.GradientDrawable();
        bg.setColor(Color.rgb(228,239,229));
        bg.setCornerRadius(dp(14));
        label.setBackground(bg);
        return label;
    }
    /** Native input hints, numeric keyboard types, validators and saved values remain intact. */
    private void styleSetupInput(EditText input){
        if(input==null)return;
        input.setTextColor(UiKit.INK);
        input.setHintTextColor(UiKit.MUTED);
        input.setMinHeight(dp(56));
        input.setPadding(dp(15),dp(11),dp(15),dp(11));
        android.graphics.drawable.GradientDrawable bg=new android.graphics.drawable.GradientDrawable();
        bg.setColor(Color.WHITE);
        bg.setStroke(dp(1),UiKit.BORDER);
        bg.setCornerRadius(dp(14));
        input.setBackground(bg);
    }
    private void surveyTile(LinearLayout parent,String title,String detail,Runnable tap){
        String art="field";
        if("Birds".equals(title))art="bird";
        if("Herpetofauna".equals(title))art="frog";
        if("Mammals".equals(title))art="mammal";
        if("Custom".equals(title))art="settings";
        View tile=LikasPortalUi.mini(this,title,detail,art,tap);
        boolean stacked=parent.getOrientation()==LinearLayout.VERTICAL;
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(
            stacked?-1:0,-2,1);
        if(stacked)lp.bottomMargin=dp(9);
        else if(parent.getChildCount()>0)lp.leftMargin=dp(10);
        parent.addView(tile,lp);
    }
    private void home(){
        homeVisible=true;currentScreen="home";main.removeAllViews();
        if(setupFooter!=null)setupFooter.setVisibility(View.GONE);
        lastHomeActiveSurvey=activeSurvey();
        main.addView(LikasPortalUi.hero(this),new LinearLayout.LayoutParams(-1,dp(152)));
        if(lastHomeActiveSurvey!=null){
            View session=LikasPortalUi.session(this,()->
                startActivity(new Intent(this,MainActivity.class).putExtra("resume",true)));
            session.setContentDescription("Resume survey in progress. Continue field recording.");
            LinearLayout.LayoutParams resumeLp=new LinearLayout.LayoutParams(-1,-2);
            resumeLp.topMargin=dp(12);
            main.addView(session,resumeLp);
        }
        TextView guidance=LikasPortalUi.kicker(this,"EXPLORE LIKAS");
        main.addView(guidance);
        View field=LikasPortalUi.tile(this,"Fieldwork","Surveys · Data · History",
            "field",LikasPortalUi.CANOPY,Color.WHITE,this::fieldworkHub);
        LinearLayout.LayoutParams fieldLp=new LinearLayout.LayoutParams(-1,-2);
        fieldLp.bottomMargin=dp(9);main.addView(field,fieldLp);
        View library=LikasPortalUi.tile(this,"Library","Species · Habitats · Guides",
            "library",Color.rgb(222,237,208),Color.WHITE,()->
                startActivity(new Intent(this,LikasLibraryActivity.class)));
        LinearLayout.LayoutParams libraryLp=new LinearLayout.LayoutParams(-1,-2);
        libraryLp.bottomMargin=dp(9);main.addView(library,libraryLp);
        View settings=LikasPortalUi.tile(this,"Settings","Backups · App · Storage",
            "settings",Color.rgb(231,239,238),Color.WHITE,()->
                startActivity(new Intent(this,LikasSettingsActivity.class)));
        main.addView(settings);
        if(!playedStartupHomeTransition &&
           getIntent().getBooleanExtra("show_menu",false)){
            playedStartupHomeTransition=true;
            UiMotion.enter(this,main);
        }
    }
    private void fieldworkHub(){
        homeVisible=false;currentScreen="fieldwork";main.removeAllViews();
        if(setupFooter!=null)setupFooter.setVisibility(View.GONE);
        main.addView(UiKit.navigationHeader(this,"Fieldwork","Home",this::home));
        main.addView(FieldVisuals.section(this,"New survey","Choose wildlife group"));
        String unfinished=activeSurvey();
        lastHomeActiveSurvey=unfinished;
        if(unfinished!=null){
            LinearLayout activeCard=vertical();
            activeCard.setPadding(dp(14),dp(10),dp(14),dp(12));
            activeCard.setBackground(UiKit.surface(this));
            TextView banner=text("Survey in progress",17,true);
            banner.setTextColor(GREEN);activeCard.addView(banner);
            try(FieldDb d=new FieldDb(this);Cursor c=d.getReadableDatabase().rawQuery(
                "SELECT COALESCE(survey_profile,taxon_group,'Birds'),method,COALESCE(project_id,'default') "+
                "FROM surveys WHERE id=?",new String[]{unfinished})){
                if(c.moveToFirst()){
                    String methodName=SurveyMethods.lookup(c.getString(0),c.getString(1)).label;
                    TextView detail=text(c.getString(0)+" · "+methodName+"\nProject: "+c.getString(2),13,false);
                    detail.setTextColor(MUTED);activeCard.addView(detail);
                }
            }catch(Exception ignored){}
            Button resume=UiKit.button(this,"Resume Survey","primary");
            resume.setOnClickListener(v->startActivity(
                new Intent(this,MainActivity.class).putExtra("resume",true)));
            activeCard.addView(resume,new LinearLayout.LayoutParams(-1,dp(actionDp())));
            main.addView(activeCard);
        }else{
            LinearLayout first=new LinearLayout(this);
            first.setOrientation(largeText()?LinearLayout.VERTICAL:LinearLayout.HORIZONTAL);
            surveyTile(first,"Birds","Counts · Transects",()->setup("Birds",false));
            surveyTile(first,"Herpetofauna","Calls · Encounters",()->setup("Herpetofauna",false));
            main.addView(first);
            LinearLayout second=new LinearLayout(this);
            second.setOrientation(largeText()?LinearLayout.VERTICAL:LinearLayout.HORIZONTAL);
            surveyTile(second,"Mammals","Signs · Stations",()->setup("Mammals",false));
            surveyTile(second,"Custom","Other methods",()->customPicker());
            LinearLayout.LayoutParams secondLp=new LinearLayout.LayoutParams(-1,-2);
            secondLp.topMargin=dp(10);main.addView(second,secondLp);
        }

        main.addView(FieldVisuals.section(this,"Records",null));
        toolRow(main,"Projects & stations",()->
            startActivity(new Intent(this,SitesActivity.class)));
        LinearLayout shortcutRow=new LinearLayout(this);
        shortcutRow.setOrientation(largeText()?LinearLayout.VERTICAL:LinearLayout.HORIZONTAL);
        View history=LikasPortalUi.mini(this,"History",recentSurveySummary(),"history",
            ()->startActivity(new Intent(this,HistoryActivity.class)));
        shortcutRow.addView(history,new LinearLayout.LayoutParams(
            largeText()?-1:0,-2,1));
        View dashboard=LikasPortalUi.mini(this,"Analysis","Survey data","chart",
            ()->startActivity(new Intent(this,DashboardActivity.class)));
        LinearLayout.LayoutParams dashLp=new LinearLayout.LayoutParams(
            largeText()?-1:0,-2,1);
        if(largeText())dashLp.topMargin=dp(9);
        else dashLp.leftMargin=dp(10);
        shortcutRow.addView(dashboard,dashLp);
        LinearLayout.LayoutParams shortcutsLp=new LinearLayout.LayoutParams(-1,-2);
        shortcutsLp.topMargin=dp(10);main.addView(shortcutRow,shortcutsLp);
        LinearLayout moreTools=vertical();
        moreTools.addView(LikasPortalUi.kicker(this,"FIELD TOOLS"));
        toolRow(moreTools,"Site habitats",()->
            startActivity(new Intent(this,HabitatRecordsActivity.class)));
        toolRow(moreTools,"Audio",()->
            startActivity(new Intent(this,CallLibraryActivity.class)));
        toolRow(moreTools,"Camera traps",()->
            startActivity(new Intent(this,TrapProtocolActivity.class)));
        toolRow(moreTools,"Protocols",()->
            startActivity(new Intent(this,FieldProtocolsActivity.class)));
        toolRow(moreTools,"Study plans",()->
            startActivity(new Intent(this,StudyProtocolsActivity.class)));
        boolean expanded=getSharedPreferences("likas_ui",MODE_PRIVATE)
            .getBoolean("field_tools_open",false);
        moreTools.setVisibility(expanded?View.VISIBLE:View.GONE);
        Button toolsToggle=UiKit.button(this,expanded?"Hide field tools":"More field tools","secondary");
        toolsToggle.setContentDescription(expanded?"Hide additional field tools":
            "Show additional field tools");
        LinearLayout.LayoutParams toggleLp=new LinearLayout.LayoutParams(-1,dp(actionDp()));
        toggleLp.topMargin=dp(13);
        main.addView(toolsToggle,toggleLp);
        main.addView(moreTools);
        toolsToggle.setOnClickListener(v->{
            boolean show=moreTools.getVisibility()!=View.VISIBLE;
            moreTools.setVisibility(show?View.VISIBLE:View.GONE);
            toolsToggle.setText(show?"Hide field tools":"More field tools");
            toolsToggle.setContentDescription(show?"Hide additional field tools":
                "Show additional field tools");
            getSharedPreferences("likas_ui",MODE_PRIVATE).edit()
                .putBoolean("field_tools_open",show).apply();
            if(show)UiMotion.reveal(this,moreTools);
        });
    }
    /** Short status caption for the single History destination. */
    private String recentSurveySummary(){
        try(FieldDb d=new FieldDb(this);
            Cursor c=d.getReadableDatabase().rawQuery(
                "SELECT COALESCE(survey_profile,taxon_group,'Birds'),"+
                "COALESCE(outcome,CASE WHEN ended IS NULL THEN 'active' "+
                "WHEN complete=1 THEN 'complete' ELSE 'incomplete' END) "+
                "FROM surveys ORDER BY started DESC LIMIT 1",null)){
            if(c.moveToFirst())
                return c.getString(0)+" · "+c.getString(1).replace('_',' ');
        }catch(Exception ignored){}
        return "Past surveys";
    }

    private void toolRow(LinearLayout parent,String name,Runnable action){
        String artwork="protocol";
        if("Audio".equals(name))artwork="audio";
        if("Camera traps".equals(name))artwork="photo";
        if("Site habitats".equals(name))artwork="habitat";
        if("Projects & stations".equals(name))artwork="map";
        View item=LikasPortalUi.action(this,name,"",artwork,action);
        LinearLayout.LayoutParams margin=new LinearLayout.LayoutParams(-1,-2);
        margin.bottomMargin=dp(8);parent.addView(item,margin);
    }
    private void card(String name,String desc,Runnable action){
        LinearLayout wrapper=vertical();
        wrapper.setPadding(dp(14),dp(7),dp(12),dp(7));
        wrapper.setBackground(UiKit.surface(this));
        TextView h=text(name+"   ›",17,true);
        h.setPadding(dp(2),dp(9),dp(2),dp(9));wrapper.addView(h);
        if(desc!=null&&!desc.isEmpty()){
            TextView sub=text(desc,13,false);sub.setTextColor(MUTED);wrapper.addView(sub);
        }
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(9);
        main.addView(wrapper,lp);wrapper.setOnClickListener(v->action.run());
    }
    private void customPicker(){
        homeVisible=false;currentScreen="custom";main.removeAllViews();
        if(setupFooter!=null)setupFooter.setVisibility(View.GONE);
        Button back=UiKit.button(this,"← Back","secondary");main.addView(back);back.setOnClickListener(v->fieldworkHub());
        heading("Custom survey","Select a wildlife group.");
        card("Birds","Custom bird survey",()->setup("Birds",true));
        card("Herpetofauna","Custom reptile and amphibian survey",()->setup("Herpetofauna",true));
        card("Mammals","Custom mammal survey",()->setup("Mammals",true));
    }
    /** Highlight the checked native radio control without replacing its semantics. */
    private void updateMethodSelection(int checked){
        if(methodChoices==null)return;
        for(int i=0;i<methodChoices.getChildCount();i++){
            View item=methodChoices.getChildAt(i);
            boolean selected=item.getId()==checked;
            android.graphics.drawable.GradientDrawable bg=new android.graphics.drawable.GradientDrawable();
            bg.setColor(selected?Color.rgb(230,243,231):Color.WHITE);
            bg.setStroke(dp(selected?2:1),selected?UiKit.POND:UiKit.BORDER);
            bg.setCornerRadius(dp(16));
            item.setBackground(bg);
            item.setSelected(selected);
        }
    }
    private void setup(String selected,boolean isCustom){
        homeVisible=false;currentScreen="setup";profile=selected;custom=isCustom;
        main.removeAllViews();
        setupStartButton.setText("Continue to GPS");
        setupStartButton.setContentDescription("Continue "+profile+" survey setup to GPS preflight");
        setupFooter.setVisibility(View.VISIBLE);
        Button back=button("‹ Back");
        LinearLayout.LayoutParams backLp=new LinearLayout.LayoutParams(
            dp(largeText()?96:112),dp(largeText()?actionDp():48));
        main.addView(back,backLp);
        back.setOnClickListener(v->{if(custom)customPicker();else fieldworkHub();});
        heading((custom?"Custom · ":"")+profile+" survey",null);
        choices=SurveyMethods.forProfile(profile);
        methodChoices=new RadioGroup(this);methodChoices.setOrientation(RadioGroup.VERTICAL);
        for(int i=0;i<choices.length;i++){
            SurveyMethods.Method m=choices[i];
            String label=m.label+(m.basicLog?" · basic log":"");
            RadioButton rb=new RadioButton(this);
            rb.setId(1000+i);rb.setText(label);rb.setTextSize(15);rb.setTextColor(INK);
            rb.setButtonTintList(ColorStateList.valueOf(UiKit.POND));
            rb.setMinHeight(dp(54));
            rb.setPadding(dp(12),dp(10),dp(12),dp(10));
            rb.setBackground(UiKit.surface(this));
            LinearLayout.LayoutParams methodLp=new LinearLayout.LayoutParams(-1,-2);
            methodLp.bottomMargin=dp(8);
            methodChoices.addView(rb,methodLp);

        }
        methodChoices.check(1000);
        methodChoices.setOnCheckedChangeListener((group,checked)->refreshStudyChoices());
        main.addView(step("01","Survey method"));
        LinearLayout methodsCard=vertical();
        methodsCard.setPadding(dp(12),dp(6),dp(10),dp(6));
        methodsCard.setBackground(UiKit.surface(this));
        methodsCard.addView(methodChoices);
        TextView methodHint=LikasPortalUi.text(this,choices[0].description,13,
            UiKit.MUTED,false);
        methodHint.setPadding(dp(8),dp(4),dp(8),dp(10));
        methodsCard.addView(methodHint);
        main.addView(methodsCard);
        updateMethodSelection(1000);
        main.addView(step("02","Project and location"));
        projectChoice=new Spinner(this);
        projectKeys.clear();
        ArrayList<String> names=new ArrayList<>();
        try(FieldDb db=new FieldDb(this);
            Cursor c=db.getReadableDatabase().rawQuery("SELECT id,name FROM projects ORDER BY name",null)){
            while(c.moveToNext()){projectKeys.add(c.getString(0));names.add(c.getString(1));}
        }
        if(names.isEmpty()){projectKeys.add("default");names.add("General field project");}
        projectChoice.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,names));
        main.addView(text("Project",13,false));
        projectChoice.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){
            public void onNothingSelected(android.widget.AdapterView<?> parent){}
            public void onItemSelected(android.widget.AdapterView<?> parent,View view,int position,long id){
                refreshRouteTemplates();
                refreshStudyChoices();
            }
        });
        LinearLayout projectRow=new LinearLayout(this);
        projectRow.setOrientation(largeText()?LinearLayout.VERTICAL:LinearLayout.HORIZONTAL);
        projectRow.setGravity(Gravity.CENTER_VERTICAL);
        projectChoice.setContentDescription("Choose survey project");
        projectRow.addView(projectChoice,new LinearLayout.LayoutParams(
            largeText()?-1:0,dp(largeText()?72:52),1f));
        Button projectManage=UiKit.button(this,"Manage","secondary");
        LinearLayout.LayoutParams manageWidth=new LinearLayout.LayoutParams(
            largeText()?-1:dp(110),dp(largeText()?actionDp():52));
        if(largeText())manageWidth.topMargin=dp(6);
        else manageWidth.leftMargin=dp(8);
        projectRow.addView(projectManage,manageWidth);
        main.addView(projectRow);
        projectManage.setContentDescription("Manage projects and sampling stations");
        projectManage.setOnClickListener(v->startActivity(new Intent(this,SitesActivity.class)));
        main.addView(text("Checklist region",16,true));
        geographicChoice=new Spinner(this);
        geographicChoice.setAdapter(new ArrayAdapter<String>(
            this,android.R.layout.simple_spinner_dropdown_item,GeoRegions.LABELS));
        geographicChoice.setSelection(0);
        main.addView(geographicChoice);
        geographicChoice.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){
            public void onNothingSelected(android.widget.AdapterView<?> parent){}
            public void onItemSelected(android.widget.AdapterView<?> parent,View view,int position,long id){
                if(gpsPolygonResolving){
                    gpsLookupGeneration++;
                    gpsPolygonResolving=false;
                }
                if(suggested!=null&&!GeoRegions.idAt(position).equals(suggested.checklistRegion)){
                    suggested=null;geographicMethod=GeoRegions.BOUNDARY_METHOD;
                }
            }
        });
        gpsLookupButton=UiKit.button(this,"Choose checklist area by GPS","secondary");
        main.addView(gpsLookupButton);
        boundaryMessage=text("",12,false);
        boundaryMessage.setTextColor(MUTED);main.addView(boundaryMessage);
        gpsLookupButton.setOnClickListener(v->suggestBoundary());
        // Sampling stations are assigned within field visits, never in survey setup.
        main.addView(step("03","Sampling effort"));
        observerEntry=new EditText(this);
        observerEntry.setTextSize(16);observerEntry.setSingleLine(true);
        observerEntry.setHint("Observer (optional)");
        durationEntry=new EditText(this);
        durationEntry.setTextSize(16);durationEntry.setInputType(InputType.TYPE_CLASS_NUMBER);
        durationEntry.setHint("Point count duration (minutes)");
        durationEntry.setText("10");
        TextView durationLabel=text("Point count duration (minutes)",13,true);
        main.addView(durationLabel);
        main.addView(durationEntry);
        transectDurationEntry=new EditText(this);
        transectDurationEntry.setTextSize(16);transectDurationEntry.setSingleLine(true);
        transectDurationEntry.setInputType(InputType.TYPE_CLASS_NUMBER);
        transectDurationEntry.setHint("Timed interval (minutes)");
        transectDurationEntry.setText("120");
        TextView timedLabel=text("Sampling duration (minutes)",13,true);
        main.addView(timedLabel);
        main.addView(transectDurationEntry);
        radiusEntry=new EditText(this);radiusEntry.setTextSize(16);radiusEntry.setSingleLine(true);
        radiusEntry.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        radiusEntry.setHint("Optional");
        TextView radiusLabel=text("Fixed radius (m)",13,true);
        main.addView(radiusLabel);
        main.addView(radiusEntry);
        routeHeader=text("Planned route length (km)",13,true);main.addView(routeHeader);
        routeLengthEntry=new EditText(this);
        routeLengthEntry.setSingleLine(true);routeLengthEntry.setTextSize(17);
        routeLengthEntry.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        routeLengthEntry.setHint("Kilometres");routeLengthEntry.setText("2");
        main.addView(routeLengthEntry);
        spacingChoice=new Spinner(this);
        spacingChoice.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,
            new String[]{"100 m","250 m","500 m","Custom spacing"}));
        spacingHeader=text("Station spacing",13,true);main.addView(spacingHeader);
        main.addView(spacingChoice);
        spacingChoice.setSelection(1);
        customSpacingEntry=new EditText(this);
        customSpacingEntry.setHint("Custom spacing (meters)");
        customSpacingEntry.setSingleLine(true);customSpacingEntry.setTextSize(16);
        customSpacingEntry.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        customSpacingEntry.setVisibility(View.GONE);main.addView(customSpacingEntry);
        stationCountPreview=text("",13,true);stationCountPreview.setTextColor(GREEN);main.addView(stationCountPreview);
        repeatHeader=text("Repeat monitoring route",14,true);main.addView(repeatHeader);
        templateChoice=new Spinner(this);
        main.addView(templateChoice);
        templateHint=text("Saved routes reuse permanent stations.",12,false);
        templateHint.setTextColor(MUTED);main.addView(templateHint);
        saveTemplateChoice=new CheckBox(this);
        saveTemplateChoice.setText("Save this route for future monitoring");
        saveTemplateChoice.setChecked(true);main.addView(saveTemplateChoice);
        templateName=new EditText(this);
        templateName.setTextSize(16);templateName.setSingleLine(true);
        templateName.setHint("Reusable route name");
        templateName.setText("Bird route "+new java.text.SimpleDateFormat("yyyyMMdd-HHmm",Locale.US).format(new java.util.Date()));
        main.addView(templateName);
        saveTemplateChoice.setOnCheckedChangeListener((button,checked)->
            templateName.setVisibility(checked?View.VISIBLE:View.GONE));
        templateChoice.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){
            public void onNothingSelected(android.widget.AdapterView<?> parent){}
            public void onItemSelected(android.widget.AdapterView<?> parent,View view,int position,long id){
                if(!updatingRouteChoices)applyTemplateSelection();
            }
        });
        refreshRouteTemplates();
        routeLengthEntry.addTextChangedListener(new android.text.TextWatcher(){
            public void beforeTextChanged(CharSequence s,int st,int c,int af){}
            public void onTextChanged(CharSequence s,int st,int before,int c){updateRoutePreview();}
            public void afterTextChanged(android.text.Editable s){}
        });
        customSpacingEntry.addTextChangedListener(new android.text.TextWatcher(){
            public void beforeTextChanged(CharSequence s,int st,int c,int af){}
            public void onTextChanged(CharSequence s,int st,int before,int c){updateRoutePreview();}
            public void afterTextChanged(android.text.Editable s){}
        });
        spacingChoice.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){
            public void onNothingSelected(android.widget.AdapterView<?> a){}
            public void onItemSelected(android.widget.AdapterView<?> a,View v,int idx,long id){
                customSpacingEntry.setVisibility(idx==3?View.VISIBLE:View.GONE);updateRoutePreview();
            }
        });

        lineBearingEntry=new EditText(this);
        lineBearingEntry.setSingleLine(true);
        lineBearingEntry.setTextSize(16);
        lineBearingEntry.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        lineBearingEntry.setHint("Transect magnetic bearing (0–359°)");
        main.addView(lineBearingEntry);
        methodChoices.setOnCheckedChangeListener((group,checked)->{
            updateMethodSelection(checked);
            int choice=checked-1000;
            if(choice>=0&&choice<choices.length)methodHint.setText(choices[choice].description);
            refreshStudyChoices();
            if(choice>=0&&choice<choices.length){
                durationLabel.setVisibility(choices[choice].code.equals("point_count")?View.VISIBLE:View.GONE);
                durationEntry.setVisibility(choices[choice].code.equals("point_count")?View.VISIBLE:View.GONE);
                radiusLabel.setVisibility(choices[choice].code.equals("point_count")?View.VISIBLE:View.GONE);
                radiusEntry.setVisibility(choices[choice].code.equals("point_count")?View.VISIBLE:View.GONE);
                timedLabel.setVisibility(!choices[choice].code.equals("point_count")&&SurveyMethods.timedInterval(choices[choice].code)?View.VISIBLE:View.GONE);
                transectDurationEntry.setVisibility(!choices[choice].code.equals("point_count")&&SurveyMethods.timedInterval(choices[choice].code)?View.VISIBLE:View.GONE);
                if(choices[choice].code.equals("amphibian_calls"))transectDurationEntry.setText("5");
                else if(choices[choice].code.equals("visual_encounter")||choices[choice].code.equals("area_search")||
                    choices[choice].code.equals("track_sign")||choices[choice].code.equals("coverboard_search"))transectDurationEntry.setText("30");
                else if(choices[choice].code.equals("spotlight")||choices[choice].code.equals("bat_acoustic"))transectDurationEntry.setText("60");
                else transectDurationEntry.setText("120");
                lineBearingEntry.setVisibility(profile.equals("Birds")&&choices[choice].code.equals("line_transect")?View.VISIBLE:View.GONE);
                updateRoutePreview();
                applyTemplateSelection();
            }
        });
        durationLabel.setVisibility(choices[0].code.equals("point_count")?View.VISIBLE:View.GONE);
        durationEntry.setVisibility(choices[0].code.equals("point_count")?View.VISIBLE:View.GONE);
        radiusLabel.setVisibility(choices[0].code.equals("point_count")?View.VISIBLE:View.GONE);
        radiusEntry.setVisibility(choices[0].code.equals("point_count")?View.VISIBLE:View.GONE);
        timedLabel.setVisibility(!choices[0].code.equals("point_count")&&SurveyMethods.timedInterval(choices[0].code)?View.VISIBLE:View.GONE);
        transectDurationEntry.setVisibility(!choices[0].code.equals("point_count")&&SurveyMethods.timedInterval(choices[0].code)?View.VISIBLE:View.GONE);
        lineBearingEntry.setVisibility(View.GONE);
        updateRoutePreview();
        regionEntry=new EditText(this);regionEntry.setSingleLine(true);
        regionEntry.setHint("Stratum / region label (optional)");
        LinearLayout extraDetails=vertical();
        extraDetails.addView(observerEntry);
        extraDetails.addView(regionEntry);
        areaEntry=new EditText(this);areaEntry.setSingleLine(true);
        areaEntry.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        areaEntry.setHint("Stratum area (km², optional)");
        extraDetails.addView(areaEntry);
        extraDetails.setVisibility(View.GONE);
        TextView advancedToggle=text("＋ Additional survey details",14,true);
        advancedToggle.setTextColor(GREEN);
        advancedToggle.setMinHeight(dp(48));
        advancedToggle.setGravity(Gravity.CENTER_VERTICAL);
        advancedToggle.setFocusable(true);
        main.addView(advancedToggle);
        main.addView(extraDetails);
        advancedToggle.setOnClickListener(v->{
            boolean open=extraDetails.getVisibility()!=View.VISIBLE;
            extraDetails.setVisibility(open?View.VISIBLE:View.GONE);
            advancedToggle.setText(open?"− Hide additional details":"＋ Additional survey details");
        });

        for(EditText entry:new EditText[]{observerEntry,durationEntry,transectDurationEntry,
            radiusEntry,routeLengthEntry,customSpacingEntry,lineBearingEntry,regionEntry,
            areaEntry,templateName})styleSetupInput(entry);
        main.addView(step("04","Begin survey"));
        studyOptions=vertical();
        studyOptions.setVisibility(View.GONE);
        studyChoice=new Spinner(this);
        studyOptions.addView(studyChoice,new LinearLayout.LayoutParams(-1,dp(52)));
        Button manageStudies=UiKit.button(this,"Manage study templates","quiet");
        studyOptions.addView(manageStudies,new LinearLayout.LayoutParams(-1,dp(actionDp())));
        manageStudies.setOnClickListener(v->
            startActivity(new Intent(this,StudyProtocolsActivity.class)));
        studyToggle=text("＋ Study rules (optional)",14,true);
        studyToggle.setTextColor(GREEN);studyToggle.setMinHeight(dp(48));
        studyToggle.setGravity(Gravity.CENTER_VERTICAL);studyToggle.setFocusable(true);
        studyToggle.setOnClickListener(v->{
            boolean open=studyOptions.getVisibility()!=View.VISIBLE;
            studyOptions.setVisibility(open?View.VISIBLE:View.GONE);
            refreshStudyTitle();
        });
        main.addView(studyToggle);main.addView(studyOptions);
        studyChoice.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){
            public void onNothingSelected(android.widget.AdapterView<?> parent){}
            public void onItemSelected(android.widget.AdapterView<?> parent,View view,int position,long id){
                refreshStudyTitle();
            }
        });
        refreshStudyChoices();
        // The only Continue to GPS action is pinned beneath the scroll area.
    }
    private void refreshStudyTitle(){
        if(studyToggle==null||studyChoice==null||studyOptions==null)return;
        if(studyOptions.getVisibility()==View.VISIBLE){
            studyToggle.setText("− Hide study rules");return;
        }
        String id=selectedStudyId();
        if(id.isEmpty())studyToggle.setText("＋ Study rules (optional)");
        else{
            String title=studyChoice.getSelectedItem()==null?"Custom":
                studyChoice.getSelectedItem().toString();
            int cutoff=title.indexOf(" · ");
            studyToggle.setText("＋ Study: "+(cutoff>0?title.substring(0,cutoff):title));
        }
    }
    private String selectedStudyId(){
        int index=studyChoice==null?-1:studyChoice.getSelectedItemPosition();
        return index>=0&&index<studyTemplateIds.size()?studyTemplateIds.get(index):"";
    }
    private void refreshStudyChoices(){
        if(studyChoice==null||projectChoice==null||methodChoices==null||
            methodChoices.getCheckedRadioButtonId()<1000||projectKeys.isEmpty())return;
        String earlier=selectedStudyId();
        ArrayList<String> labels=new ArrayList<>();labels.add("Standard LIKAS requirements");
        studyTemplateIds.clear();studyTemplateIds.add("");
        int methodIndex=methodChoices.getCheckedRadioButtonId()-1000;
        if(methodIndex<0||choices==null||methodIndex>=choices.length)return;
        try(FieldDb store=new FieldDb(this);
            Cursor c=store.getReadableDatabase().rawQuery(
                "SELECT id,name,min_visits,min_minutes,min_units,survey_profile "+
                "FROM study_protocol_templates WHERE project_id=? AND method=? "+
                "AND (survey_profile=? OR survey_profile IS NULL) "+
                "ORDER BY CASE WHEN survey_profile IS NULL THEN 1 ELSE 0 END,name",
                new String[]{projectId(),choices[methodIndex].code,profile})){
            while(c.moveToNext()){
                studyTemplateIds.add(c.getString(0));
                String scope=c.isNull(5)?" · shared legacy":"";
                labels.add(c.getString(1)+scope+" · "+c.getInt(2)+" visits, "+
                    c.getInt(3)+" min, "+c.getInt(4)+" units");
            }
        }
        studyChoice.setAdapter(new ArrayAdapter<String>(this,
            android.R.layout.simple_spinner_dropdown_item,labels));
        int selected=studyTemplateIds.indexOf(earlier);
        studyChoice.setSelection(Math.max(0,selected));
        refreshStudyTitle();
    }
    /** Project edits refresh without clearing method, route length or checklist controls. */
    private void refreshProjectOptions(){
        String previous=projectId();
        ArrayList<String> ids=new ArrayList<>(),names=new ArrayList<>();
        try(FieldDb d=new FieldDb(this);
            Cursor c=d.getReadableDatabase().rawQuery("SELECT id,name FROM projects ORDER BY name",null)){
            while(c.moveToNext()){ids.add(c.getString(0));names.add(c.getString(1));}
        }
        if(ids.isEmpty()){ids.add("default");names.add("General field project");}
        // Refresh labels too: project names can change even if their IDs stay the same.
        projectKeys.clear();projectKeys.addAll(ids);
        projectChoice.setAdapter(new ArrayAdapter<String>(
            this,android.R.layout.simple_spinner_dropdown_item,names));
        int index=projectKeys.indexOf(previous);
        if(index>=0)projectChoice.setSelection(index);
    }
    private String projectId(){
        int index=projectChoice==null?-1:projectChoice.getSelectedItemPosition();
        return index<0||index>=projectKeys.size()?"default":projectKeys.get(index);
    }
    /** The GPS selector uses the containing official polygon, never a nearest guess. */
    private boolean usableChecklistGps(Location gps){
        if(gps==null||!LocationManager.GPS_PROVIDER.equals(gps.getProvider())||
            !gps.hasAccuracy())return false;
        long age=(android.os.SystemClock.elapsedRealtimeNanos()-
            gps.getElapsedRealtimeNanos())/1000000L;
        return GeoChecklistPolicy.acceptableLocation(gps.getLatitude(),gps.getLongitude(),
            gps.getAccuracy(),age,gps.isFromMockProvider());
    }
    private void applyGpsChecklistScope(Location gps){
        if(gpsPolygonResolving)return;
        cancelGpsLookup();
        final int token=gpsLookupGeneration;
        gpsPolygonResolving=true;
        if(boundaryMessage!=null)boundaryMessage.setText("Matching GPS to checklist area…");
        final Location accepted=new Location(gps);
        new Thread(()->{
            final BoundaryLookup.Match match=BoundaryLookup.find(
                getApplicationContext(),accepted.getLatitude(),accepted.getLongitude(),
                accepted.getAccuracy());
            runOnUiThread(()->{
                if(gpsLookupGeneration!=token||isFinishing()||
                    !"setup".equals(currentScreen)||geographicChoice==null)return;
                gpsPolygonResolving=false;
                if(!GeoChecklistPolicy.canAutoSelect(match.status,match.checklistRegion)){
                    suggested=null;
                    geographicMethod=GeoRegions.BOUNDARY_METHOD;
                    boundaryMessage.setText("Area uncertain ("+match.status+"). "+
                        "Select the checklist area manually.");
                    return;
                }
                // The observer has already requested GPS area selection. No extra dialog.
                geographicChoice.setSelection(GeoRegions.index(match.checklistRegion));
                suggested=match;
                geographicMethod="fresh_phone_gps_polygon_auto";
                boundaryMessage.setText("Selected "+GeoRegions.label(match.checklistRegion)+
                    " · "+match.province+" · GPS ±"+
                    Math.round(accepted.getAccuracy())+" m"+
                    "\nArea selection does not confirm species presence.");
                persistSetup();
            });
        },"LIKAS-Checklist-GPS-Area").start();
    }
    private void suggestBoundary(){
        if(boundaryMessage==null||!"setup".equals(currentScreen))return;
        cancelGpsLookup();
        suggested=null;geographicMethod=GeoRegions.BOUNDARY_METHOD;
        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            boundaryMessage.setText("Precise location required. You can select an area manually.");
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION},GPS_PERMISSION_REQUEST);
            return;
        }
        LocationManager manager=(LocationManager)getSystemService(LOCATION_SERVICE);
        if(manager==null||!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            boundaryMessage.setText("Turn on phone GPS or choose an area manually.");
            return;
        }
        if(gpsLookupButton!=null){
            gpsLookupButton.setEnabled(false);gpsLookupButton.setText("Locating…");
        }
        boundaryMessage.setText("Waiting for a fresh GPS fix below 10 m…");
        Location live=TrackService.currentFix();
        if(usableChecklistGps(live)){applyGpsChecklistScope(live);return;}
        pendingGpsListener=new LocationListener(){
            @Override public void onLocationChanged(Location gps){
                if(pendingGpsListener!=this||!"setup".equals(currentScreen))return;
                if(!usableChecklistGps(gps)){
                    boundaryMessage.setText("Waiting for GPS below 10 m"+
                        (gps.hasAccuracy()?" · currently ±"+Math.round(gps.getAccuracy())+" m":"")+"…");
                    return;
                }
                applyGpsChecklistScope(gps);
            }
            @Override public void onProviderDisabled(String provider){
                cancelGpsLookup();
                if(boundaryMessage!=null)
                    boundaryMessage.setText("GPS disabled. Select the checklist area manually.");
            }
        };
        final LocationListener listener=pendingGpsListener;
        gpsTimeout=()->{
            if(pendingGpsListener!=listener)return;
            cancelGpsLookup();
            if(boundaryMessage!=null)boundaryMessage.setText(
                "GPS unavailable after 30 seconds. Select an area manually or retry.");
        };
        try{
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000L,0f,
                listener,Looper.getMainLooper());
            gpsHandler.postDelayed(gpsTimeout,30000L);
        }catch(Exception e){
            cancelGpsLookup();
            boundaryMessage.setText("GPS unavailable. Select the checklist area manually.");
        }
    }
    private void cancelGpsLookup(){
        gpsLookupGeneration++;
        gpsPolygonResolving=false;
        if(gpsTimeout!=null){gpsHandler.removeCallbacks(gpsTimeout);gpsTimeout=null;}
        if(pendingGpsListener!=null){
            try{
                LocationManager m=(LocationManager)getSystemService(LOCATION_SERVICE);
                if(m!=null)m.removeUpdates(pendingGpsListener);
            }catch(Exception ignored){}
            pendingGpsListener=null;
        }
        if(gpsLookupButton!=null){
            gpsLookupButton.setEnabled(true);
            gpsLookupButton.setText("Choose checklist area by GPS");
        }
    }
    @Override public void onRequestPermissionsResult(int request,String[] permissions,int[] results){
        super.onRequestPermissionsResult(request,permissions,results);
        if(request!=GPS_PERMISSION_REQUEST)return;
        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED)
            suggestBoundary();
        else if(boundaryMessage!=null)
            boundaryMessage.setText("Precise GPS access was not granted. Use the manual geographic checklist choice.");
    }
    private boolean supportsRoute(String code){return FieldUiPolicy.routeSetup(code);}
    private double spacingMeters(){
        int pos=spacingChoice==null?1:spacingChoice.getSelectedItemPosition();
        if(pos==0)return 100;
        if(pos==1)return 250;
        if(pos==2)return 500;
        try{return Double.parseDouble(customSpacingEntry.getText().toString().trim());}
        catch(Exception ignored){return -1;}
    }
    private void updateRoutePreview(){
        if(stationCountPreview==null||routeLengthEntry==null||methodChoices==null)return;
        int choice=methodChoices.getCheckedRadioButtonId()-1000;
        if(choice<0||choice>=choices.length)return;
        String method=choices[choice].code;
        boolean route=supportsRoute(method);
        routeHeader.setVisibility(route?View.VISIBLE:View.GONE);
        boolean points=FieldUiPolicy.pointStationSetup(method);
        repeatHeader.setVisibility(points?View.VISIBLE:View.GONE);
        spacingHeader.setVisibility(points?View.VISIBLE:View.GONE);
        routeLengthEntry.setVisibility(route?View.VISIBLE:View.GONE);
        if(spacingChoice!=null)spacingChoice.setVisibility(method.equals("point_count")?View.VISIBLE:View.GONE);
        if(customSpacingEntry!=null)customSpacingEntry.setVisibility(
            method.equals("point_count")&&spacingChoice.getSelectedItemPosition()==3?View.VISIBLE:View.GONE);
        stationCountPreview.setVisibility(route?View.VISIBLE:View.GONE);
        double length=-1;
        try{length=Double.parseDouble(routeLengthEntry.getText().toString().trim())*1000.0;}
        catch(Exception ignored){}
        if(length<=0){stationCountPreview.setText("Enter a planned route length.");return;}
        if(method.equals("point_count")){
            double spacing=spacingMeters();
            if(spacing<=0){stationCountPreview.setText("Select a positive station spacing.");return;}
            int stations=SurveyMath.stations(length,spacing);
            double last=(stations-1)*spacing;
            stationCountPreview.setText(stations+" points · "+(int)spacing+
                " m spacing · last station at "+(int)last+" m");
        }else stationCountPreview.setText(String.format(Locale.US,
            "Planned transect · %.2f km",length/1000.0));
    }
    private String selectedTemplateId(){
        if(templateChoice==null)return "";
        int n=templateChoice.getSelectedItemPosition();
        return n>=0&&n<templateIds.size()?templateIds.get(n):"";
    }
    private void refreshRouteTemplates(){
        if(templateChoice==null)return;
        String previous=selectedTemplateId();
        templateIds.clear();
        templateIds.add("");
        ArrayList<String> labels=new ArrayList<>();
        labels.add("New route · generate unique stations");
        try(FieldDb f=new FieldDb(this);Cursor c=f.getReadableDatabase().rawQuery(
                "SELECT id,name,planned_length_m,station_spacing_m,station_count FROM route_templates "+
                "WHERE project_id=? AND method='point_count' ORDER BY name",
                new String[]{projectId()})){
            while(c.moveToNext()){
                templateIds.add(c.getString(0));
                labels.add(c.getString(1)+" · "+c.getInt(4)+" points ("+
                    Math.round(c.getDouble(2))+" m / "+Math.round(c.getDouble(3))+" m)");
            }
        }catch(Exception ignored){}
        updatingRouteChoices=true;
        templateChoice.setAdapter(new ArrayAdapter<String>(this,
            android.R.layout.simple_spinner_dropdown_item,labels));
        int n=templateIds.indexOf(previous);
        templateChoice.setSelection(Math.max(0,n));
        updatingRouteChoices=false;
        applyTemplateSelection();
    }
    private void applyTemplateSelection(){
        if(templateChoice==null||routeLengthEntry==null||spacingChoice==null)return;
        String id=selectedTemplateId();
        boolean pointCount=false;
        int chosen=methodChoices.getCheckedRadioButtonId()-1000;
        if(chosen>=0&&chosen<choices.length)pointCount="point_count".equals(choices[chosen].code);
        boolean saved=pointCount&&!id.isEmpty();
        if(saved){
            try(FieldDb f=new FieldDb(this);Cursor c=f.getReadableDatabase().rawQuery(
                    "SELECT planned_length_m,station_spacing_m,station_count,name FROM route_templates "+
                    "WHERE id=? AND project_id=? AND method='point_count'",
                    new String[]{id,projectId()})){
                if(c.moveToFirst()){
                    double km=c.getDouble(0)/1000.0,spacing=c.getDouble(1);
                    routeLengthEntry.setText(String.format(Locale.US,"%.3f",km));
                    int idx=Math.abs(spacing-100)<0.001?0:Math.abs(spacing-250)<0.001?1:
                        Math.abs(spacing-500)<0.001?2:3;
                    spacingChoice.setSelection(idx);
                    if(idx==3)customSpacingEntry.setText(String.format(Locale.US,"%.1f",spacing));
                    templateHint.setText("Saved route: "+c.getString(3)+
                        " · same stations, fresh GPS at each visit");
                }else{
                    templateHint.setText("Saved route is unavailable. Select New route.");
                }
            }catch(Exception e){templateHint.setText("Route could not be read. Select New route.");}
        }else templateHint.setText("New route · permanent station IDs assigned at start");
        routeLengthEntry.setEnabled(!saved);
        spacingChoice.setEnabled(!saved);
        customSpacingEntry.setEnabled(!saved);
        templateChoice.setVisibility(pointCount?View.VISIBLE:View.GONE);
        templateHint.setVisibility(pointCount?View.VISIBLE:View.GONE);
        saveTemplateChoice.setVisibility(pointCount&&!saved?View.VISIBLE:View.GONE);
        templateName.setVisibility(pointCount&&!saved&&saveTemplateChoice.isChecked()?View.VISIBLE:View.GONE);
        updateRoutePreview();
    }
    private void startSelected(){
        int choice=methodChoices.getCheckedRadioButtonId()-1000;
        if(choice<0||choice>=choices.length){Toast.makeText(this,"Select survey method",Toast.LENGTH_SHORT).show();return;}
        SurveyMethods.Method m=choices[choice];
        // A survey belongs to its project. Station selection occurs within the field session.
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
        int plannedDuration=0;
        if(!m.code.equals("point_count")&&SurveyMethods.timedInterval(m.code)){
            try{plannedDuration=Integer.parseInt(transectDurationEntry.getText().toString().trim())*60;}
            catch(Exception ignored){plannedDuration=-1;}
            if(plannedDuration<60||plannedDuration>43200){
                transectDurationEntry.setError("Enter 1 to 720 minutes");
                transectDurationEntry.requestFocus();return;
            }
        }
        Double radius=null;
        if(m.code.equals("point_count")&&!radiusEntry.getText().toString().trim().isEmpty()){
            try{radius=Double.valueOf(radiusEntry.getText().toString().trim());}catch(Exception ignored){}
            if(radius==null||radius<1||radius>2000){
                radiusEntry.setError("Enter 1 to 2000 meters or leave blank");radiusEntry.requestFocus();return;
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
        double routeLengthM=0,spacingM=0;
        if(supportsRoute(m.code)){
            try{routeLengthM=Double.parseDouble(routeLengthEntry.getText().toString().trim())*1000.0;}
            catch(Exception ignored){routeLengthM=-1;}
            if(!Double.isFinite(routeLengthM)||routeLengthM<100||routeLengthM>50000){
                routeLengthEntry.setError("Route length: 0.1 to 50 km");routeLengthEntry.requestFocus();return;
            }
            if(m.code.equals("point_count")){
                spacingM=spacingMeters();
                if(!Double.isFinite(spacingM)||spacingM<50||spacingM>2000){
                    customSpacingEntry.setError("Spacing: 50 to 2000 m");return;
                }
                int points=SurveyMath.stations(routeLengthM,spacingM);
                if(points<2||points>201){
                    routeLengthEntry.setError("Choose a route/spacing combination yielding 2–201 stations");return;
                }
            }
        }
        String templateId=m.code.equals("point_count")?selectedTemplateId():"";
        boolean saveNewTemplate=m.code.equals("point_count")&&templateId.isEmpty()&&saveTemplateChoice.isChecked();
        String routeName=saveNewTemplate?templateName.getText().toString().trim():"";
        if(saveNewTemplate&&(routeName.length()<3||routeName.length()>100)){
            templateName.setError("Give the route a unique name (3–100 characters)");return;
        }
        if(saveNewTemplate){
            try(FieldDb f=new FieldDb(this);Cursor duplicate=f.getReadableDatabase().rawQuery(
                "SELECT 1 FROM route_templates WHERE project_id=? AND method='point_count' AND name=?",
                new String[]{projectId(),routeName})){
                if(duplicate.moveToFirst()){
                    templateName.setError("This route name already exists in the project. Reuse that route or enter another name.");
                    return;
                }
            }
        }
        Intent i=new Intent(this,SurveyGpsActivity.class);
        i.putExtra("transect_bearing",lineBearingEntry.getText().toString().trim());
        i.putExtra("create_new",true);
        i.putExtra("survey_profile",profile);
        i.putExtra("project_id",projectId());
        i.putExtra("geographic_region_id",GeoRegions.idAt(geographicChoice.getSelectedItemPosition()));
        i.putExtra("geographic_selection_method",geographicMethod);
        if(suggested!=null&&GeoRegions.idAt(geographicChoice.getSelectedItemPosition()).equals(suggested.checklistRegion)){
            i.putExtra("admin_province",suggested.province);
            i.putExtra("admin_province_psgc",suggested.psgc);
            i.putExtra("admin_boundary_status",suggested.status);
            i.putExtra("admin_boundary_version",BoundaryLookup.VERSION);
        }
        i.putExtra("region_label",regionEntry.getText().toString().trim());
        i.putExtra("stratum_area_km2",area==null?Double.NaN:area);
        i.putExtra("survey_method",m.code);
        i.putExtra("station_id","");
        i.putExtra("observer_name",observerEntry.getText().toString().trim());
        i.putExtra("point_target_s",duration);
        i.putExtra("point_radius_m",radius==null?Double.NaN:radius);
        i.putExtra("planned_duration_s",plannedDuration);
        i.putExtra("planned_route_m",routeLengthM);
        i.putExtra("planned_spacing_m",spacingM);
        i.putExtra("route_template_id",templateId);
        i.putExtra("save_route_template",saveNewTemplate);
        i.putExtra("route_template_name",routeName);
        i.putExtra("custom_survey",custom);
        i.putExtra("basic_log_only",m.basicLog);
        if(studyChoice!=null&&!studyTemplateIds.isEmpty()){
            int index=studyChoice.getSelectedItemPosition();
            i.putExtra("study_protocol_id",studyTemplateIds.get(
                Math.max(0,Math.min(index,studyTemplateIds.size()-1))));
        }
        // Preserve the setup selections until the survey has actually started.
        // Canceling GPS acquisition must return to this same configured screen.
        persistSetup();
        startActivity(i);
    }
    private void persistSetup(){
        android.content.SharedPreferences.Editor e=getPreferences(MODE_PRIVATE).edit()
           .putString("screen",currentScreen).putString("profile",profile).putBoolean("custom",custom);
        if(currentScreen.equals("setup")&&durationEntry!=null){
            e.putString("observer",observerEntry.getText().toString())
             .putString("duration",durationEntry.getText().toString())
             .putString("line",lineBearingEntry.getText().toString())
             .putString("area",areaEntry.getText().toString())
             .putString("region",regionEntry.getText().toString())
             .putString("radius",radiusEntry.getText().toString())
             .putString("transectDuration",transectDurationEntry.getText().toString())
             .putString("routeLength",routeLengthEntry.getText().toString())
             .putString("customSpacing",customSpacingEntry.getText().toString())
             .putInt("spacingChoice",spacingChoice.getSelectedItemPosition())
             .putInt("methodChoice",methodChoices.getCheckedRadioButtonId()-1000)
             .putInt("projectChoice",projectChoice.getSelectedItemPosition())
             .putInt("geographicChoice",geographicChoice.getSelectedItemPosition())
             .putString("geographicMethod",geographicMethod)
             .putString("studyTemplateId",selectedStudyId());
        }
        e.commit();
    }
    private void restoreSetup(){
        android.content.SharedPreferences p=getPreferences(MODE_PRIVATE);
        if(methodChoices==null)return;
        int choice=p.getInt("methodChoice",0);
        if(choice>=0&&choice<choices.length)methodChoices.check(1000+choice);
        observerEntry.setText(p.getString("observer",""));
        durationEntry.setText(p.getString("duration","10"));
        lineBearingEntry.setText(p.getString("line",""));
        areaEntry.setText(p.getString("area",""));
        regionEntry.setText(p.getString("region",""));
        radiusEntry.setText(p.getString("radius",""));
        transectDurationEntry.setText(p.getString("transectDuration","120"));
        routeLengthEntry.setText(p.getString("routeLength","2"));
        customSpacingEntry.setText(p.getString("customSpacing",""));
        spacingChoice.setSelection(p.getInt("spacingChoice",1));
        updateRoutePreview();
        int project=p.getInt("projectChoice",0);
        if(project>=0&&project<projectChoice.getCount())projectChoice.setSelection(project);
        int geo=p.getInt("geographicChoice",0);
        if(geo>=0&&geo<geographicChoice.getCount())geographicChoice.setSelection(geo);
        // Reconfirm polygon-based suggestions on a new session.
        geographicMethod=GeoRegions.BOUNDARY_METHOD;
        refreshStudyChoices();
        String savedStudy=p.getString("studyTemplateId","");
        int studyIndex=studyTemplateIds.indexOf(savedStudy);
        if(studyIndex>=0)studyChoice.setSelection(studyIndex);
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
