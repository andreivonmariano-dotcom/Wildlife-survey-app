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
    private String currentScreen="home";
    private AutoCompleteTextView stationEntry;
    private EditText durationEntry,observerEntry,lineBearingEntry,regionEntry,areaEntry,radiusEntry,transectDurationEntry,routeLengthEntry,customSpacingEntry;
    private Spinner spacingChoice;
    private TextView stationCountPreview;
    private Spinner projectChoice, geographicChoice;
    private BoundaryLookup.Match suggested;
    private String geographicMethod=GeoRegions.BOUNDARY_METHOD;
    private String suggestedStationId="";
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
    @Override public void onCreate(Bundle state){super.onCreate(state);buildShell();
        String screen=getPreferences(MODE_PRIVATE).getString("screen","home");
        String rememberedProfile=getPreferences(MODE_PRIVATE).getString("profile","Birds");
        boolean rememberedCustom=getPreferences(MODE_PRIVATE).getBoolean("custom",false);
        if(screen.equals("setup")&&!isSurveyActive()){setup(rememberedProfile,rememberedCustom);restoreSetup();}
        else if(screen.equals("custom")&&!isSurveyActive())customPicker();
        else home();
        if(isSurveyActive()&&getSharedPreferences("field_resume",MODE_PRIVATE).getString("screen","home").equals("survey"))
            startActivity(new Intent(this,MainActivity.class).putExtra("resume",true));
    }
    @Override protected void onResume(){
        super.onResume();
        if(root!=null&&homeVisible)home();
        else if(root!=null&&currentScreen.equals("setup")&&stationEntry!=null){
            String typed=stationEntry.getText().toString();
            updateStations();
            stationEntry.setText(typed);
        }
    }
    @Override protected void onPause(){persistSetup();super.onPause();}
    @Override protected void onNewIntent(Intent intent){super.onNewIntent(intent);setIntent(intent);home();}
    private boolean isSurveyActive(){return activeSurvey()!=null;}
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
        homeVisible=true;currentScreen="home";main.removeAllViews();
                LinearLayout banner=vertical();banner.setPadding(dp(16),dp(15),dp(16),dp(15));
        android.graphics.drawable.GradientDrawable hero=new android.graphics.drawable.GradientDrawable();
        hero.setColor(UiKit.INK);hero.setCornerRadius(dp(17));banner.setBackground(hero);
        TextView tag=text("PHILIPPINE WILDLIFE SURVEY",12,true);tag.setTextColor(Color.rgb(183,220,195));
        TextView welcome=text("Welcome back",25,true);welcome.setTextColor(Color.WHITE);
        TextView desc=text("Field surveys, saved offline and ready to review.",13,false);desc.setTextColor(Color.rgb(217,234,222));
        banner.addView(tag);banner.addView(welcome);banner.addView(desc);
        LinearLayout.LayoutParams heroLp=new LinearLayout.LayoutParams(-1,-2);heroLp.bottomMargin=dp(15);main.addView(banner,heroLp);
        if(activeSurvey()!=null){
            main.addView(text("An unfinished survey is available. Resume it to continue recording.",14,true));
            Button resume=UiKit.button(this,"Resume active survey","primary");
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
        card("Fieldwork dashboard","Survey counts, monitoring activity and detected-taxa summaries.",
            ()->startActivity(new Intent(this,DashboardActivity.class)));
        card("Survey history","Review visits, edit records and inspect correction history.",
            ()->startActivity(new Intent(this,HistoryActivity.class)));
        card("Projects & stations","Manage projects, reusable stations and habitat details.",
            ()->startActivity(new Intent(this,SitesActivity.class)));
        card("Offline species checklist","Review installed taxa and import an attributed, versioned taxonomic CSV.",
            ()->startActivity(new Intent(this,CatalogActivity.class)));
        card("Import v0.7 backup","Restore the CSV ZIP from the previous Beta into this separate v0.8 installation.",
            ()->startActivity(new Intent(this,ImportActivity.class)));
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
        wrapper.setBackground(UiKit.surface(this));
        TextView h=text(name+"  ›",19,true);wrapper.addView(h);
        TextView sub=text(desc,13,false);sub.setTextColor(MUTED);wrapper.addView(sub);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(11);
        main.addView(wrapper,lp);wrapper.setOnClickListener(v->action.run());
    }
    private void customPicker(){
        homeVisible=false;currentScreen="custom";main.removeAllViews();
        Button back=UiKit.button(this,"← Back","secondary");main.addView(back);back.setOnClickListener(v->home());
        heading("Custom survey","Choose one category. The survey will use only methods suited to that category.");
        card("Birds","Custom bird survey",()->setup("Birds",true));
        card("Herpetofauna","Custom reptile and amphibian survey",()->setup("Herpetofauna",true));
        card("Mammals","Custom mammal survey",()->setup("Mammals",true));
    }
    private void setup(String selected,boolean isCustom){
        homeVisible=false;currentScreen="setup";profile=selected;custom=isCustom;
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
        main.addView(text("Geographic checklist area",16,true));
        geographicChoice=new Spinner(this);
        geographicChoice.setAdapter(new ArrayAdapter<String>(
            this,android.R.layout.simple_spinner_dropdown_item,GeoRegions.LABELS));
        geographicChoice.setSelection(0);
        main.addView(geographicChoice);
        geographicChoice.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){
            public void onNothingSelected(android.widget.AdapterView<?> parent){}
            public void onItemSelected(android.widget.AdapterView<?> parent,View view,int position,long id){
                if(suggested!=null&&!GeoRegions.idAt(position).equals(suggested.checklistRegion)){
                    suggested=null;geographicMethod=GeoRegions.BOUNDARY_METHOD;
                }
            }
        });
        Button detect=UiKit.button(this,"Suggest province from recent GPS","secondary");
        main.addView(detect);
        boundaryMessage=text("Offline provincial polygons: PSGC 3Q 2026. Tap to compare a station position or recent precise GPS fix. This never verifies a species distribution.",12,false);
        boundaryMessage.setTextColor(MUTED);main.addView(boundaryMessage);
        detect.setOnClickListener(v->suggestBoundary());
        TextView geographyNote=text("Manual island-group selection for offline species suggestions. This is not GPS boundary verification or the study-area stratum. Regional evidence is incomplete, and unexpected species remain recordable.",12,false);
        geographyNote.setTextColor(MUTED);
        main.addView(geographyNote);
        stationEntry=new AutoCompleteTextView(this);stationEntry.setThreshold(1);
        stationEntry.setTextSize(16);stationEntry.setSingleLine(true);
        stationEntry.setHint("Registered station ID");
        // Survey setup selects a project. Sampling stations are assigned after starting.
        projectChoice.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){
            public void onNothingSelected(android.widget.AdapterView<?> p){}
            public void onItemSelected(android.widget.AdapterView<?> p,View v,int i,long id){updateStations();}
        });
        TextView note=text("Select the project and survey design here. Point-count stations will be generated for this survey, then their GPS locations recorded as you reach them. Transect methods record a continuous route.",12,false);
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
        main.addView(text("Planned survey route",17,true));
        routeLengthEntry=new EditText(this);
        routeLengthEntry.setSingleLine(true);routeLengthEntry.setTextSize(17);
        routeLengthEntry.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        routeLengthEntry.setHint("Planned route length (km)");routeLengthEntry.setText("2");
        main.addView(routeLengthEntry);
        spacingChoice=new Spinner(this);
        spacingChoice.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,
            new String[]{"100 m","250 m","500 m","Custom spacing"}));
        main.addView(text("Distance between point-count stations",13,false));
        main.addView(spacingChoice);
        spacingChoice.setSelection(1);
        customSpacingEntry=new EditText(this);
        customSpacingEntry.setHint("Custom spacing (meters)");
        customSpacingEntry.setSingleLine(true);customSpacingEntry.setTextSize(16);
        customSpacingEntry.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        customSpacingEntry.setVisibility(View.GONE);main.addView(customSpacingEntry);
        stationCountPreview=text("",13,true);stationCountPreview.setTextColor(GREEN);main.addView(stationCountPreview);
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
            }
        });
        durationEntry.setVisibility(choices[0].code.equals("point_count")?View.VISIBLE:View.GONE);
        radiusEntry.setVisibility(choices[0].code.equals("point_count")?View.VISIBLE:View.GONE);
        transectDurationEntry.setVisibility(!choices[0].code.equals("point_count")&&SurveyMethods.timedInterval(choices[0].code)?View.VISIBLE:View.GONE);
        lineBearingEntry.setVisibility(View.GONE);
        updateRoutePreview();
        regionEntry=new EditText(this);regionEntry.setSingleLine(true);
        regionEntry.setHint("Stratum / region label (optional)");
        main.addView(regionEntry);
        areaEntry=new EditText(this);areaEntry.setSingleLine(true);
        areaEntry.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        areaEntry.setHint("Stratum area (km², optional)");
        main.addView(areaEntry);
        TextView help=text("The whole-survey clock always runs. Timed methods get a separate station/interval clock; it continues into overtime after its target. Specialist equipment checks use elapsed time and device-effort logs.",12,false);
        help.setTextColor(MUTED);main.addView(help);
        Button start=UiKit.button(this,"Start "+profile+" survey","primary");
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
        ArrayList<String> choices=new ArrayList<>();
        try(FieldDb db=new FieldDb(this);
            Cursor c=db.getReadableDatabase().rawQuery(
                "SELECT id,name FROM sites WHERE project_id=? ORDER BY name,id",
                new String[]{projectId()})){
            while(c.moveToNext()){
                String id=c.getString(0),name=c.getString(1);
                ids.add(id);choices.add(name+"  ["+id+"]");
            }
        }
        stationEntry.setHint("Search station by name or choose from list");
        stationEntry.setAdapter(new ArrayAdapter<String>(this,
            android.R.layout.simple_dropdown_item_1line,choices));
        stationEntry.setOnItemClickListener((p,view,index,rowId)->{
            if(index>=0&&index<ids.size()){
                String selectedId=ids.get(index);
                stationEntry.setText(selectedId,false);
                stationEntry.setSelection(selectedId.length());
                stationEntry.dismissDropDown();
            }
        });
        stationEntry.setText("");
    }
    private void suggestBoundary(){
        final double[] ll=new double[2];float accuracy=Float.NaN;
        boolean got=false;String origin="station";
        String id=stationEntry==null?"":stationEntry.getText().toString().trim();
        if(!id.isEmpty())try(FieldDb db=new FieldDb(this);
            Cursor c=db.getReadableDatabase().rawQuery(
                "SELECT latitude,longitude,coordinate_uncertainty_m FROM sites WHERE id=? AND project_id=?",
                new String[]{id,projectId()})){
            if(c.moveToFirst()&&!c.isNull(0)&&!c.isNull(1)&&!c.isNull(2)){
                ll[0]=c.getDouble(0);ll[1]=c.getDouble(1);accuracy=(float)c.getDouble(2);
                got=true;
            }
        }catch(Exception ignored){}
        if(!got)try{
            android.location.LocationManager manager=(android.location.LocationManager)getSystemService(LOCATION_SERVICE);
            if(checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)==android.content.pm.PackageManager.PERMISSION_GRANTED){
                android.location.Location gps=manager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER);
                if(gps!=null&&System.currentTimeMillis()-gps.getTime()<300000&&gps.hasAccuracy()){
                    ll[0]=gps.getLatitude();ll[1]=gps.getLongitude();accuracy=gps.getAccuracy();
                    got=true;origin="recent phone GPS";
                }
            }
        }catch(Exception ignored){}
        if(!got){
            boundaryMessage.setText("No acceptable station coordinates or recent precise GPS fix. Save station latitude, longitude and uncertainty first, or select the area manually.");
            return;
        }
        BoundaryLookup.Match match=BoundaryLookup.find(this,ll[0],ll[1],accuracy);
        boundaryMessage.setText(origin+": "+match.province+" ("+match.psgc+") · "+match.status+
            " · accuracy ~"+Math.round(accuracy)+" m · "+BoundaryLookup.VERSION);
        if(!match.status.equals("suggestion_only")){
            new android.app.AlertDialog.Builder(this).setTitle("Location needs review")
                .setMessage("Boundary result: "+match.status+". Selected region remains unchanged. Confirm station GPS accuracy or choose the area manually.")
                .setPositiveButton("Close",null).show();
            return;
        }
        new android.app.AlertDialog.Builder(this).setTitle("Apply geographic suggestion?")
            .setMessage("Province: "+match.province+"\nPSGC: "+match.psgc+
                "\nSuggested checklist: "+GeoRegions.label(match.checklistRegion)+
                "\nThis is a low-resolution administrative polygon estimate, not a biogeographic verification.")
            .setNegativeButton("Keep manual",null)
            .setPositiveButton("Use suggestion",(d,w)->{
                suggested=match;
                suggestedStationId=stationEntry==null?"":stationEntry.getText().toString().trim();
                geographicChoice.setSelection(GeoRegions.index(match.checklistRegion));
                geographicMethod="gps_station_polygon_suggested_confirmed";
            }).show();
    }
    private boolean supportsRoute(String code){
        return code.equals("point_count")||code.equals("line_transect")||code.equals("ves_transect");
    }
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
            int stations=(int)Math.floor((length+1e-7)/spacing)+1;
            double last=(stations-1)*spacing;
            stationCountPreview.setText(stations+" planned points at 0, "+(int)spacing+
                " ... "+(int)last+" m. Remaining route beyond final point: "+
                Math.max(0,(int)Math.round(length-last))+" m.");
        }else stationCountPreview.setText(String.format(Locale.US,
            "Continuous %.2f km transect. No point-count stations will be generated.",length/1000.0));
    }
    private void startSelected(){
        int choice=methodChoices.getCheckedRadioButtonId()-1000;
        if(choice<0||choice>=choices.length){Toast.makeText(this,"Select survey method",Toast.LENGTH_SHORT).show();return;}
        SurveyMethods.Method m=choices[choice];
        // A survey belongs to its project. Station selection occurs within the field session.
        String station="";
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
                int points=(int)Math.floor((routeLengthM+1e-7)/spacingM)+1;
                if(points<2||points>201){
                    routeLengthEntry.setError("Choose a route/spacing combination yielding 2–201 stations");return;
                }
            }
        }
        Intent i=new Intent(this,MainActivity.class);
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
        i.putExtra("station_id",station);
        i.putExtra("observer_name",observerEntry.getText().toString().trim());
        i.putExtra("point_target_s",duration);
        i.putExtra("point_radius_m",radius==null?Double.NaN:radius);
        i.putExtra("planned_duration_s",plannedDuration);
        i.putExtra("planned_route_m",routeLengthM);
        i.putExtra("planned_spacing_m",spacingM);
        i.putExtra("custom_survey",custom);
        i.putExtra("basic_log_only",m.basicLog);
        currentScreen="home";homeVisible=true;
        getPreferences(MODE_PRIVATE).edit().putString("screen","home").commit();
        startActivity(i);
    }
    private void persistSetup(){
        android.content.SharedPreferences.Editor e=getPreferences(MODE_PRIVATE).edit()
           .putString("screen",currentScreen).putString("profile",profile).putBoolean("custom",custom);
        if(currentScreen.equals("setup")&&stationEntry!=null&&durationEntry!=null){
            e.putString("station",stationEntry.getText().toString())
             .putString("observer",observerEntry.getText().toString())
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
        stationEntry.setText("");
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
