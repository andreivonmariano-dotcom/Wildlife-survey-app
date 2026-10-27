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
    private final int BG=Color.rgb(245,249,245),INK=Color.rgb(23,58,39),GREEN=Color.rgb(34,102,72),MUTED=Color.rgb(99,116,104);
    private LinearLayout main,root; private String profile="Birds";
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
    private Spinner projectChoice, geographicChoice;
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
        if(root!=null&&homeVisible){
            String current=activeSurvey();
            if(!java.util.Objects.equals(current,lastHomeActiveSurvey))home();
        }else if(root!=null&&"setup".equals(currentScreen)&&projectChoice!=null)
            refreshProjectOptions();
    }
    @Override protected void onPause(){
        cancelGpsLookup();persistSetup();super.onPause();
    }
    @Override protected void onNewIntent(Intent intent){super.onNewIntent(intent);setIntent(intent);home();}
    private boolean isSurveyActive(){return activeSurvey()!=null;}
    private boolean homeVisible=true;
    private String lastHomeActiveSurvey=null;
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
        homeVisible=true;currentScreen="home";main.removeAllViews();
        LinearLayout header=new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setOrientation(LinearLayout.HORIZONTAL);
        ImageView logo=new ImageView(this);logo.setImageResource(R.drawable.likas_mark);
        logo.setContentDescription("LIKAS mark");
        header.addView(logo,new LinearLayout.LayoutParams(dp(52),dp(52)));
        LinearLayout brand=vertical();brand.setPadding(dp(12),0,0,0);
        TextView wordmark=text("LIKAS",25,true);
        wordmark.setPadding(0,0,0,0);brand.addView(wordmark);
        TextView sub=text("Philippine Wildlife Survey",12,false);
        sub.setPadding(0,dp(2),0,0);
        sub.setTextColor(MUTED);brand.addView(sub);
        header.addView(brand,new LinearLayout.LayoutParams(0,dp(56),1));
        Button back=UiKit.button(this,"‹ Back","quiet");
        back.setTextSize(13);back.setOnClickListener(v->finish());
        header.addView(back,new LinearLayout.LayoutParams(dp(88),dp(46)));
        LinearLayout.LayoutParams headerLp=new LinearLayout.LayoutParams(-1,-2);
        headerLp.bottomMargin=dp(18);main.addView(header,headerLp);
        String unfinished=activeSurvey();
        lastHomeActiveSurvey=unfinished;
        if(unfinished!=null){
            TextView banner=text("Unfinished survey",16,true);
            banner.setTextColor(GREEN);main.addView(banner);
            Button resume=UiKit.button(this,"Continue Survey","primary");
            resume.setOnClickListener(v->startActivity(
                new Intent(this,MainActivity.class).putExtra("resume",true)));
            main.addView(resume);
            TextView notice=text("Finish the active survey before creating another.",12,false);
            notice.setTextColor(MUTED);main.addView(notice);
        }else{
            TextView prompt=text("Select a survey group",16,true);
        prompt.setPadding(dp(3),dp(5),dp(3),dp(8));
        main.addView(prompt);
            card("Birds","",()->setup("Birds",false));
            card("Herpetofauna","",()->setup("Herpetofauna",false));
            card("Mammals","",()->setup("Mammals",false));
            card("Custom survey","",()->customPicker());
        }
        Button toolsToggle=UiKit.button(this,"Data & Tools  ▾","secondary");
        LinearLayout.LayoutParams toolMargin=new LinearLayout.LayoutParams(-1,dp(54));
        toolMargin.topMargin=dp(10);main.addView(toolsToggle,toolMargin);
        LinearLayout tools=vertical();
        tools.setVisibility(View.GONE);
        toolsToggle.setOnClickListener(v->{
            boolean expanded=tools.getVisibility()!=View.VISIBLE;
            tools.setVisibility(expanded?View.VISIBLE:View.GONE);
            toolsToggle.setText(expanded?"Data & Tools  ▴":"Data & Tools  ▾");
        });
        toolRow(tools,"Survey history",()->startActivity(new Intent(this,HistoryActivity.class)));
        toolRow(tools,"Projects & stations",()->startActivity(new Intent(this,SitesActivity.class)));
        toolRow(tools,"Offline checklist",()->startActivity(new Intent(this,CatalogActivity.class)));
        toolRow(tools,"Fieldwork dashboard",()->startActivity(new Intent(this,DashboardActivity.class)));
        toolRow(tools,"Scientific data review",()->startActivity(new Intent(this,AuditActivity.class)));
        toolRow(tools,"Legacy backup import",()->startActivity(new Intent(this,ImportActivity.class)));
        main.addView(tools);
    }
    private void toolRow(LinearLayout parent,String name,Runnable action){
        TextView item=text(name+"   ›",16,true);
        item.setPadding(dp(16),dp(14),dp(12),dp(14));
        item.setBackground(UiKit.surface(this));
        LinearLayout.LayoutParams margins=new LinearLayout.LayoutParams(-1,-2);
        margins.bottomMargin=dp(8);parent.addView(item,margins);
        item.setOnClickListener(v->action.run());
    }
    private void recentSurveys(){
        try{
            String path=getDatabasePath(FieldDb.NAME).getAbsolutePath();
            if(!new java.io.File(path).exists()){main.addView(text("No surveys yet.",13,false));return;}
            try(SQLiteDatabase d=SQLiteDatabase.openDatabase(path,null,SQLiteDatabase.OPEN_READONLY);
                Cursor c=d.rawQuery(
                    "SELECT s.id,COALESCE(s.survey_profile,s.taxon_group,'Birds'),"+
                    "COALESCE(p.name,s.project_id,'General field project'),s.started,s.ended,"+
                    "COALESCE(s.outcome,'active'),COALESCE(s.planned_station_count,0),"+
                    "COALESCE(s.planned_route_m,0) FROM surveys s "+
                    "LEFT JOIN projects p ON p.id=s.project_id ORDER BY s.started DESC LIMIT 5",null)){
                if(c.getCount()==0)main.addView(text("No surveys yet.",13,false));
                while(c.moveToNext()){
                    String id=c.getString(0),taxon=c.getString(1),project=c.getString(2);
                    String date=c.getString(3)==null?"":c.getString(3);
                    String outcome=c.getString(5).replace('_',' ');
                    String design=c.getInt(6)>0?c.getInt(6)+" planned stations":
                        c.getDouble(7)>0?String.format(Locale.US,"%.2f km route",c.getDouble(7)/1000.0):
                        "survey";
                    card(taxon+" · "+project,
                        date.substring(0,Math.min(10,date.length()))+" · "+design+" · "+outcome,
                        ()->startActivity(new Intent(this,HistoryActivity.class).putExtra("event_id",id)));
                }
            }
        }catch(Exception e){main.addView(text("History is accessible from the management screen.",12,false));}
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
        Button back=UiKit.button(this,"← Back","secondary");main.addView(back);back.setOnClickListener(v->home());
        heading("Custom survey","Select a wildlife group.");
        card("Birds","Custom bird survey",()->setup("Birds",true));
        card("Herpetofauna","Custom reptile and amphibian survey",()->setup("Herpetofauna",true));
        card("Mammals","Custom mammal survey",()->setup("Mammals",true));
    }
    private void setup(String selected,boolean isCustom){
        homeVisible=false;currentScreen="setup";profile=selected;custom=isCustom;
        main.removeAllViews();
        Button back=button("← Back");main.addView(back);
        back.setOnClickListener(v->{if(custom)customPicker();else home();});
        heading((custom?"Custom · ":"")+profile+" survey",
            "Select method and project.");
        choices=SurveyMethods.forProfile(profile);
        methodChoices=new RadioGroup(this);methodChoices.setOrientation(RadioGroup.VERTICAL);
        for(int i=0;i<choices.length;i++){
            SurveyMethods.Method m=choices[i];
            String label=m.label+(m.basicLog?" · basic log":"");
            RadioButton rb=new RadioButton(this);
            rb.setId(1000+i);rb.setText(label);rb.setTextSize(16);rb.setTextColor(INK);
            rb.setPadding(dp(4),dp(10),dp(4),dp(10));
            methodChoices.addView(rb);

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
        main.addView(text("Project",13,false));
        projectChoice.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){
            public void onNothingSelected(android.widget.AdapterView<?> parent){}
            public void onItemSelected(android.widget.AdapterView<?> parent,View view,int position,long id){
                refreshRouteTemplates();
            }
        });
        LinearLayout projectRow=new LinearLayout(this);
        projectRow.setOrientation(LinearLayout.HORIZONTAL);
        projectRow.setGravity(Gravity.CENTER_VERTICAL);
        projectRow.addView(projectChoice,new LinearLayout.LayoutParams(0,dp(52),1f));
        Button projectManage=UiKit.button(this,"Manage","secondary");
        LinearLayout.LayoutParams manageWidth=new LinearLayout.LayoutParams(dp(110),dp(52));
        manageWidth.leftMargin=dp(8);
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
        boundaryMessage=text("Select a region or use GPS to suggest one.",12,false);
        boundaryMessage.setTextColor(MUTED);main.addView(boundaryMessage);
        gpsLookupButton.setOnClickListener(v->suggestBoundary());
        // Sampling stations are assigned within field visits, never in survey setup.
        observerEntry=new EditText(this);
        observerEntry.setTextSize(16);observerEntry.setSingleLine(true);
        observerEntry.setHint("Observer (optional)");
        main.addView(observerEntry);
        durationEntry=new EditText(this);
        durationEntry.setTextSize(16);durationEntry.setInputType(InputType.TYPE_CLASS_NUMBER);
        durationEntry.setHint("Point count duration (minutes)");
        durationEntry.setText("10");
        main.addView(durationEntry);
        transectDurationEntry=new EditText(this);
        transectDurationEntry.setTextSize(16);transectDurationEntry.setSingleLine(true);
        transectDurationEntry.setInputType(InputType.TYPE_CLASS_NUMBER);
        transectDurationEntry.setHint("Timed interval (minutes)");
        transectDurationEntry.setText("120");
        main.addView(transectDurationEntry);
        radiusEntry=new EditText(this);radiusEntry.setTextSize(16);radiusEntry.setSingleLine(true);
        radiusEntry.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        radiusEntry.setHint("Fixed point-count radius (m, optional)");
        main.addView(radiusEntry);
        routeHeader=text("Planned route",17,true);main.addView(routeHeader);
        routeLengthEntry=new EditText(this);
        routeLengthEntry.setSingleLine(true);routeLengthEntry.setTextSize(17);
        routeLengthEntry.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        routeLengthEntry.setHint("Planned route length (km)");routeLengthEntry.setText("2");
        main.addView(routeLengthEntry);
        spacingChoice=new Spinner(this);
        spacingChoice.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,
            new String[]{"100 m","250 m","500 m","Custom spacing"}));
        spacingHeader=text("Distance between stations",13,false);main.addView(spacingHeader);
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
            int choice=checked-1000;
            if(choice>=0&&choice<choices.length){
                durationEntry.setVisibility(choices[choice].code.equals("point_count")?View.VISIBLE:View.GONE);
                radiusEntry.setVisibility(choices[choice].code.equals("point_count")?View.VISIBLE:View.GONE);
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
        durationEntry.setVisibility(choices[0].code.equals("point_count")?View.VISIBLE:View.GONE);
        radiusEntry.setVisibility(choices[0].code.equals("point_count")?View.VISIBLE:View.GONE);
        transectDurationEntry.setVisibility(!choices[0].code.equals("point_count")&&SurveyMethods.timedInterval(choices[0].code)?View.VISIBLE:View.GONE);
        lineBearingEntry.setVisibility(View.GONE);
        updateRoutePreview();
        regionEntry=new EditText(this);regionEntry.setSingleLine(true);
        regionEntry.setHint("Stratum / region label (optional)");
        LinearLayout extraDetails=vertical();
        extraDetails.addView(regionEntry);
        areaEntry=new EditText(this);areaEntry.setSingleLine(true);
        areaEntry.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        areaEntry.setHint("Stratum area (km², optional)");
        extraDetails.addView(areaEntry);
        extraDetails.setVisibility(View.GONE);
        TextView advancedToggle=text("＋ Additional survey details",14,true);
        advancedToggle.setTextColor(GREEN);
        main.addView(advancedToggle);
        main.addView(extraDetails);
        advancedToggle.setOnClickListener(v->{
            boolean open=extraDetails.getVisibility()!=View.VISIBLE;
            extraDetails.setVisibility(open?View.VISIBLE:View.GONE);
            advancedToggle.setText(open?"− Hide additional details":"＋ Additional survey details");
        });

        Button start=UiKit.button(this,"Start "+profile+" survey","primary");
        LinearLayout.LayoutParams startLp=new LinearLayout.LayoutParams(-1,dp(58));
        startLp.topMargin=dp(16);main.addView(start,startLp);
        start.setOnClickListener(v->startSelected());
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
             .putString("geographicMethod",geographicMethod);
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
