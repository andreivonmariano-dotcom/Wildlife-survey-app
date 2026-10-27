package org.phwildlife.survey;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ProgressBar;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.text.InputType;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Offline species reference and selection during an active survey.
 * Bounded paginated SQL keeps the phone responsive with larger checklists.
 * This screen never suspends the survey timer, station identity or GNSS service.
 */
public final class SpeciesPickerActivity extends Activity {
    private static final int PAGE=35;
    private static final String[] VIEWS={"All species","Local source records"};
    private final Handler ui=new Handler(Looper.getMainLooper());
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private final Runnable delayed=this::refresh;
    private EditText search;
    private Spinner groups,views;
    private Button sortButton;
    private LinearLayout cards;
    private TextView status,siteSummary;
    private int generation=0,offset=0,sortMode=0,statusFilter=0;
    private String habitatCode="";
    private Double siteElevation=null,elevationUncertainty=null;
    private int elevationMode=0; // 0 GPS MSL, 1 manual MSL, 2 unassessed
    private String visitId="",stationId="",surveyId="";
    private boolean gpsAutoTracking=false;
    private String lastGpsElevationKey="";
    private Double lastScoredGpsMeters=null;
    private final Runnable gpsMonitor=new Runnable(){
        @Override public void run(){
            if(!gpsAutoTracking||isFinishing()||isDestroyed())return;
            if(elevationMode==0){
                GpsElevation.Sample fix=GpsElevation.read(TrackService.currentFix());
                String state=fix.usableForHabitat?"available":"unavailable";
                boolean availabilityChanged=!state.equals(lastGpsElevationKey);
                boolean movedEnough=fix.usableForHabitat&&
                    lastScoredGpsMeters!=null&&
                    Math.abs(fix.mslMeters-lastScoredGpsMeters)>=25.0;
                lastGpsElevationKey=state;
                lastScoredGpsMeters=fix.usableForHabitat?fix.mslMeters:null;
                updateSiteSummary();
                // Avoid resetting scroll position for ordinary GNSS altitude
                // noise. Re-rank only on acquisition/loss or material change.
                if(availabilityChanged||movedEnough)refresh();
            }
            ui.postDelayed(this,5000L);
        }
    };
    private List<Species> loaded=new ArrayList<>();
    private boolean truncated=false;
    private SharedPreferences surveyOptions;
    private String preferencePrefix="";
    private int savedGroupIndex=0,savedViewIndex=0;
    private String[] availableGroups;
    private String geographicRegion="PH",province="",habitat="";
    private static final int MAX_QUERY_ROWS=5000;
    private int dp(int n){return Math.round(getResources().getDisplayMetrics().density*n);}
    private TextView text(String value,int size,boolean strong){
        TextView v=new TextView(this);
        v.setText(value);v.setTextSize(size);v.setTextColor(UiKit.INK);
        if(strong)v.setTypeface(null,Typeface.BOLD);
        return v;
    }
    private static String string(Cursor c,int index){
        return c.isNull(index)?"":c.getString(index);
    }
    private static String island(String region){
        switch(region){
            case "MINDANAO":return "Mindanao";
            case "LUZON":return "Luzon";
            case "VISAYAS":return "Visayas";
            case "PALAWAN":return "Palawan";
            case "MINDORO":return "Mindoro";
            case "SULU":return "Sulu";
            default:return "";
        }
    }
    private int checked(int value,int upper){
        return Math.max(0,Math.min(value,upper-1));
    }
    private String pref(String suffix){return preferencePrefix+suffix;}
    private String visitPref(String suffix){return preferencePrefix+"visit_"+visitId+"_"+suffix;}
    private void restorePickerSettings(){
        sortMode=checked(surveyOptions.getInt(pref("sort"),0),
            SpeciesSortPolicy.SORT_LABELS.length);
        statusFilter=checked(surveyOptions.getInt(pref("filter"),0),
            SpeciesSortPolicy.FILTER_LABELS.length);
        savedGroupIndex=checked(surveyOptions.getInt(pref("group"),0),availableGroups.length);
        savedViewIndex=checked(surveyOptions.getInt(pref("view"),0),VIEWS.length);
        habitatCode=surveyOptions.getString(pref("habitat"),habitatCode);
        if(HabitatLikelihood.habitatIndex(habitatCode)==0)habitatCode="";
        elevationMode=checked(surveyOptions.getInt(visitPref("mode"),0),3);
        siteElevation=parseNumber(surveyOptions.getString(visitPref("elevation"),""));
        elevationUncertainty=parseNumber(surveyOptions.getString(visitPref("uncertainty"),""));
    }
    private void savePickerSettings(){
        if(surveyOptions==null)return;
        surveyOptions.edit().putInt(pref("sort"),sortMode)
            .putInt(pref("filter"),statusFilter)
            .putInt(pref("group"),groups==null?0:groups.getSelectedItemPosition())
            .putInt(pref("view"),views==null?0:views.getSelectedItemPosition())
            .putString(pref("habitat"),habitatCode)
            .putInt(visitPref("mode"),elevationMode)
            .putString(visitPref("elevation"),siteElevation==null?"":siteElevation.toString())
            .putString(visitPref("uncertainty"),
                elevationUncertainty==null?"":elevationUncertainty.toString())
            .apply();
    }
    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);
        Intent request=getIntent();
        String group=request.getStringExtra("group");
        if(group==null)group="Birds";
        String profile=request.getStringExtra("profile");
        availableGroups="Herpetofauna".equals(profile)?
            new String[]{"Amphibians","Reptiles"}:new String[]{group};
        geographicRegion=request.getStringExtra("geo_region");
        if(geographicRegion==null)geographicRegion="PH";
        province=request.getStringExtra("province");
        if(province==null)province="";
        habitat=request.getStringExtra("habitat");
        if(habitat==null)habitat="";
        habitatCode=guessHabitat(habitat);
        String surveyId=request.getStringExtra("survey_id");
        this.surveyId=surveyId==null?"":surveyId;
        visitId=request.getStringExtra("visit_id");
        if(visitId==null||visitId.isEmpty())visitId="SURVEY-"+this.surveyId;
        stationId=request.getStringExtra("station_id");
        if(stationId==null)stationId="";
        preferencePrefix="survey_"+(surveyId==null||surveyId.isEmpty()?"unknown":surveyId)+"_";
        surveyOptions=getSharedPreferences("likas_species_picker",MODE_PRIVATE);
        restorePickerSettings();
        getWindow().setStatusBarColor(UiKit.BG);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(UiKit.BG);
        root.setPadding(dp(16),dp(8),dp(16),dp(12));
        LinearLayout header=new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        TextView title=UiKit.title(this,"Identify species");
        header.addView(title,new LinearLayout.LayoutParams(0,-2,1));
        Button back=UiKit.button(this,"Back","quiet");
        header.addView(back,new LinearLayout.LayoutParams(dp(82),dp(48)));
        back.setOnClickListener(v->finish());
        root.addView(header);

        search=new EditText(this);
        search.setSingleLine(true);search.setTextSize(16);
        search.setHint("Common or scientific name");
        LinearLayout searchRow=new LinearLayout(this);
        searchRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams searchParams=new LinearLayout.LayoutParams(0,dp(54),1f);
        searchRow.addView(search,searchParams);
        Button unknown=UiKit.button(this,"Unidentified","secondary");
        unknown.setTextSize(12);
        unknown.setContentDescription("Record an unidentified species");
        LinearLayout.LayoutParams unknownSpace=new LinearLayout.LayoutParams(dp(122),dp(54));
        unknownSpace.leftMargin=dp(7);
        searchRow.addView(unknown,unknownSpace);
        root.addView(searchRow);
        unknown.setOnClickListener(v->{
            String[] unknowns=Taxa.unknownLabels(currentGroup());
            new AlertDialog.Builder(this).setTitle("Unidentified observation")
                .setItems(unknowns,(dialog,which)->select(unknowns[which],currentGroup()))
                .setNegativeButton("Back",null).show();
        });

        LinearLayout quick=new LinearLayout(this);
        quick.setGravity(Gravity.CENTER_VERTICAL);
        sortButton=UiKit.button(this,
            "Sort · "+SpeciesSortPolicy.SORT_LABELS[sortMode],"secondary");
        sortButton.setOnClickListener(v->openSortFilter());
        quick.addView(sortButton,new LinearLayout.LayoutParams(0,dp(52),1.25f));
        Button conditions=UiKit.button(this,"Site conditions","secondary");
        LinearLayout.LayoutParams conditionSpace=new LinearLayout.LayoutParams(0,dp(52),1f);
        conditionSpace.leftMargin=dp(6);
        quick.addView(conditions,conditionSpace);
        conditions.setOnClickListener(v->openSiteConditions());
        root.addView(quick);

        LinearLayout controls=new LinearLayout(this);
        controls.setGravity(Gravity.CENTER_VERTICAL);
        groups=new Spinner(this);
        groups.setAdapter(new ArrayAdapter<String>(this,
            android.R.layout.simple_spinner_dropdown_item,availableGroups));
        controls.addView(groups,new LinearLayout.LayoutParams(0,dp(48),1f));
        views=new Spinner(this);
        views.setAdapter(new ArrayAdapter<String>(this,
            android.R.layout.simple_spinner_dropdown_item,VIEWS));
        controls.addView(views,new LinearLayout.LayoutParams(0,dp(48),1f));
        root.addView(controls);
        groups.setSelection(savedGroupIndex);
        groups.setVisibility(availableGroups.length>1?View.VISIBLE:View.GONE);
        views.setSelection(savedViewIndex);

        Location fix=TrackService.currentFix();
        String gps="GPS unavailable";
        if(fix!=null&&fix.hasAccuracy()){
            long age=(SystemClock.elapsedRealtimeNanos()-fix.getElapsedRealtimeNanos())/1000000L;
            if(age>=0&&age<=10000L)
                gps=String.format(Locale.US,"GPS ±%.1f m",fix.getAccuracy());
        }
        String scope=island(geographicRegion);
        TextView context=text("ⓘ  "+(scope.isEmpty()?"Philippine checklist":scope+" checklist")+
            (province.isEmpty()?"":" · "+province)+" · "+gps,12,false);
        context.setTextColor(UiKit.FOREST);
        context.setMinHeight(dp(48));
        context.setGravity(Gravity.CENTER_VERTICAL);
        context.setFocusable(true);
        context.setContentDescription("Checklist context and species screening information");
        context.setOnClickListener(v->new AlertDialog.Builder(this)
            .setTitle("Checklist and species screening")
            .setMessage("Regional and habitat evidence help organize the offline list. "+
                "Scores summarize reviewed habitat and elevation data. "+
                "They are screening indexes, not probabilities of occurrence or "+
                "proof of absence. Unassessed species remain selectable.\n\n"+
                (siteSummary==null?"":siteSummary.getText().toString()))
            .setPositiveButton("Close",null).show());
        root.addView(context);
        siteSummary=text("",12,false);
        siteSummary.setTextColor(UiKit.FOREST);
        siteSummary.setPadding(0,0,0,dp(4));
        // Site evidence remains available in the checklist context information.
        updateSiteSummary();
        status=text("Loading offline species…",12,false);
        status.setTextColor(UiKit.MUTED);status.setPadding(0,dp(9),0,dp(7));
        root.addView(status);

        ScrollView scroller=new ScrollView(this);
        cards=new LinearLayout(this);
        cards.setOrientation(LinearLayout.VERTICAL);
        cards.setPadding(0,0,0,dp(20));
        scroller.addView(cards);
        root.addView(scroller,new LinearLayout.LayoutParams(-1,0,1f));
        setContentView(root);

        search.addTextChangedListener(new TextWatcher(){
            @Override public void beforeTextChanged(CharSequence s,int start,int count,int after){}
            @Override public void onTextChanged(CharSequence s,int start,int before,int count){
                ui.removeCallbacks(delayed);ui.postDelayed(delayed,230L);
            }
            @Override public void afterTextChanged(Editable value){}
        });
        AdapterView.OnItemSelectedListener chosen=new AdapterView.OnItemSelectedListener(){
            @Override public void onItemSelected(AdapterView<?> parent,View view,int pos,long id){
                refresh();
            }
            @Override public void onNothingSelected(AdapterView<?> parent){}
        };
        groups.setOnItemSelectedListener(chosen);
        views.setOnItemSelectedListener(chosen);
        refresh();
    }
    private String currentGroup(){
        return availableGroups[Math.max(0,groups.getSelectedItemPosition())];
    }
    private static String guessHabitat(String notes){
        if(notes==null)return "";
        String value=notes.toLowerCase(Locale.ROOT);
        if(value.contains("mangrove")||value.contains("coastal"))return "mangrove";
        if(value.contains("mossy"))return "mossy";
        if(value.contains("montane"))return "montane";
        if(value.contains("secondary"))return "secondary";
        if(value.contains("dipterocarp")||value.contains("lowland"))return "lowland";
        if(value.contains("grassland"))return "grassland";
        if(value.contains("plantation")||value.contains("agricult"))return "agriculture";
        if(value.contains("wetland")||value.contains("marsh"))return "wetland";
        if(value.contains("riparian")||value.contains("stream"))return "riparian";
        if(value.contains("urban")||value.contains("settlement"))return "urban";
        return "";
    }
    private void openSortFilter(){
        LinearLayout panel=new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(18),dp(8),dp(18),dp(8));
        panel.addView(text("Order by",14,true));
        Spinner order=new Spinner(this);
        order.setAdapter(new ArrayAdapter<String>(this,
            android.R.layout.simple_spinner_dropdown_item,SpeciesSortPolicy.SORT_LABELS));
        order.setSelection(sortMode);
        panel.addView(order);
        TextView filterLabel=text("Show likelihood status",14,true);
        filterLabel.setPadding(0,dp(15),0,0);
        panel.addView(filterLabel);
        Spinner byStatus=new Spinner(this);
        byStatus.setAdapter(new ArrayAdapter<String>(this,
            android.R.layout.simple_spinner_dropdown_item,SpeciesSortPolicy.FILTER_LABELS));
        byStatus.setSelection(statusFilter);
        panel.addView(byStatus);
        TextView note=text("Percentage sorts use a source-based habitat match index. "+
            "Scores with missing evidence appear last.",12,false);
        note.setTextColor(UiKit.MUTED);note.setPadding(0,dp(14),0,0);
        panel.addView(note);
        new AlertDialog.Builder(this).setTitle("Sort & Filter")
            .setView(panel)
            .setPositiveButton("Apply",(dialog,which)->{
                sortMode=order.getSelectedItemPosition();
                statusFilter=byStatus.getSelectedItemPosition();
                sortButton.setText("Sort · "+SpeciesSortPolicy.SORT_LABELS[sortMode]);
                savePickerSettings();
                refresh();
            })
            .setNeutralButton("Reset",(dialog,which)->{
                sortMode=0;statusFilter=0;
                sortButton.setText("Sort · A–Z");
                savePickerSettings();
                refresh();
            })
            .setNegativeButton("Cancel",null).show();
    }
    private static Double parseNumber(String value){
        if(value==null||value.trim().isEmpty())return null;
        try{
            double numeric=Double.parseDouble(value.trim());
            return Double.isFinite(numeric)?numeric:null;
        }catch(NumberFormatException ignored){return null;}
    }
    private GpsElevation.Sample liveElevation(){
        return GpsElevation.read(TrackService.currentFix());
    }
    private Double effectiveElevation(){
        if(elevationMode==1)return siteElevation;
        if(elevationMode==0){
            GpsElevation.Sample fix=liveElevation();
            return fix.usableForHabitat?fix.mslMeters:null;
        }
        return null;
    }
    private Double effectiveUncertainty(){
        if(elevationMode==1)return elevationUncertainty;
        if(elevationMode==0){
            GpsElevation.Sample fix=liveElevation();
            return fix.usableForHabitat?fix.verticalUncertaintyMeters:null;
        }
        return null;
    }
    private void updateSiteSummary(){
        if(siteSummary==null)return;
        String habitatName=HabitatLikelihood.HABITATS[
            HabitatLikelihood.habitatIndex(habitatCode)];
        String reading;
        if(elevationMode==2)reading="elevation unassessed";
        else if(elevationMode==1)
            reading=siteElevation==null?"manual elevation missing":
                "Manual MSL "+siteElevation+" m"+
                (elevationUncertainty==null?"":" ±"+elevationUncertainty+" m");
        else reading=liveElevation().label();
        siteSummary.setText("Habitat: "+habitatName+" · "+reading+
            "");
    }
    private void openSiteConditions(){
        LinearLayout panel=new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(18),dp(7),dp(18),dp(7));
        panel.addView(text("Observed habitat",14,true));
        Spinner site=new Spinner(this);
        site.setAdapter(new ArrayAdapter<String>(this,
            android.R.layout.simple_spinner_dropdown_item,HabitatLikelihood.HABITATS));
        site.setSelection(HabitatLikelihood.habitatIndex(habitatCode));
        panel.addView(site);
        TextView gpsStatus=text(liveElevation().label(),12,false);
        gpsStatus.setTextColor(UiKit.FOREST);
        gpsStatus.setPadding(0,dp(10),0,dp(7));
        panel.addView(gpsStatus);
        panel.addView(text("Elevation source",14,true));
        Spinner source=new Spinner(this);
        source.setAdapter(new ArrayAdapter<String>(this,
            android.R.layout.simple_spinner_dropdown_item,new String[]{
                "Automatic · fresh GPS mean sea level",
                "Manual · measured elevation above sea level",
                "Unassessed · do not score elevation"}));
        source.setSelection(elevationMode);
        panel.addView(source);
        TextView elevationLabel=text("Manual MSL elevation (m)",14,true);
        elevationLabel.setPadding(0,dp(12),0,0);
        panel.addView(elevationLabel);
        EditText elevation=new EditText(this);
        elevation.setSingleLine(true);
        elevation.setHint("Manually measured elevation");
        elevation.setInputType(InputType.TYPE_CLASS_NUMBER|
            InputType.TYPE_NUMBER_FLAG_DECIMAL|InputType.TYPE_NUMBER_FLAG_SIGNED);
        if(siteElevation!=null)elevation.setText(String.valueOf(siteElevation));
        panel.addView(elevation);
        panel.addView(text("Elevation uncertainty (m, optional)",13,false));
        EditText accuracy=new EditText(this);
        accuracy.setSingleLine(true);accuracy.setHint("Default ±100 m");
        accuracy.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        if(elevationUncertainty!=null)
            accuracy.setText(String.valueOf(elevationUncertainty));
        panel.addView(accuracy);
        source.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override public void onItemSelected(AdapterView<?> parent,View view,
                    int position,long id){
                boolean manual=position==1;
                elevationLabel.setVisibility(manual?View.VISIBLE:View.GONE);
                elevation.setVisibility(manual?View.VISIBLE:View.GONE);
                accuracy.setVisibility(manual?View.VISIBLE:View.GONE);
            }
            @Override public void onNothingSelected(AdapterView<?> parent){}
        });
        TextView note=text("GPS horizontal accuracy does not indicate altitude accuracy. "+
            "WGS84 ellipsoid height is shown for reference but not used as MSL. "+
            "Automatic scores require a fresh GPS MSL value with reported "+
            "vertical uncertainty at most 50 m.",12,false);
        note.setTextColor(UiKit.MUTED);note.setPadding(0,dp(9),0,0);
        panel.addView(note);
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Site conditions")
            .setView(panel).setNegativeButton("Cancel",null)
            .setPositiveButton("Apply",null).create();
        dialog.setOnShowListener(v->dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            .setOnClickListener(w->{
                String rawElevation=elevation.getText().toString().trim();
                String rawAccuracy=accuracy.getText().toString().trim();
                Double meters=parseNumber(rawElevation);
                Double uncertainty=parseNumber(rawAccuracy);
                int selectedMode=source.getSelectedItemPosition();
                if(selectedMode==1&&(meters==null||!Double.isFinite(meters))){
                    elevation.setError("Enter measured MSL elevation");return;
                }
                if(selectedMode==1&&!rawAccuracy.isEmpty()&&
                    (uncertainty==null||uncertainty<0)){
                    accuracy.setError("Enter nonnegative meters");return;
                }
                elevationMode=selectedMode;
                habitatCode=HabitatLikelihood.codeAt(site.getSelectedItemPosition());
                siteElevation=meters;elevationUncertainty=uncertainty;
                savePickerSettings();
                if(!surveyId.isEmpty()){
                    try(FieldDb helper=new FieldDb(getApplicationContext())){
                        StationEnvironmentStore.recordHabitat(helper.getWritableDatabase(),
                            surveyId,visitId,stationId,habitatCode);
                        StationEnvironmentStore.recordManualElevation(
                            helper.getWritableDatabase(),surveyId,visitId,stationId,
                            elevationMode==1?siteElevation:null,
                            elevationMode==1?elevationUncertainty:null);
                        if(elevationMode==0)
                            StationEnvironmentStore.recordStationFix(
                                helper.getWritableDatabase(),surveyId,visitId,stationId,
                                TrackService.currentFix());
                    }catch(Exception ignored){}
                }
                updateSiteSummary();
                dialog.dismiss();
                refresh();
            }));
        dialog.show();
    }
    private static final class Species {
        String key,group,common,scientific,family,origin,iucn,ph,source,version;
        String reviewedHabitats="",reviewedElevation="";
        int evidence;
        HabitatLikelihood.Result screening;
    }
    private List<Species> searchCatalog(String group,String term,boolean local,
            String siteCode,Double altitude,Double uncertainty)throws Exception{
        List<Species> list=new ArrayList<>();
        String island=island(geographicRegion);
        String provinceMatch="EXISTS(SELECT 1 FROM taxon_occurrence_evidence e "+
            "WHERE e.taxon_key=c.taxon_key AND e.area_type='province' "+
            "AND LOWER(e.area_label)=LOWER(?))";
        String islandMatch="EXISTS(SELECT 1 FROM taxon_occurrence_evidence e "+
            "WHERE e.taxon_key=c.taxon_key AND e.area_type='island' "+
            "AND LOWER(e.area_label)=LOWER(?))";
        String evidence="CASE WHEN "+provinceMatch+" THEN 2 WHEN "+islandMatch+
            " THEN 1 ELSE 0 END";
        StringBuilder sql=new StringBuilder(
            "SELECT c.taxon_key,c.taxon_group,c.common_name,c.scientific_name,"+
            "c.family,c.distribution_status,c.iucn_red_list,c.ph_red_list,"+
            "c.source,c.source_version,"+evidence+" AS evidence_rank,"+
            "p.habitat_codes,p.strict_habitat,p.elevation_min_m,"+
            "p.elevation_max_m,p.review_status "+
            "FROM taxon_catalog c LEFT JOIN taxon_habitat_profiles p "+
            "ON p.taxon_key=c.taxon_key WHERE c.taxon_group=? "+
            "AND (c.taxon_key NOT LIKE 'STARTER:%' OR NOT EXISTS "+
            "(SELECT 1 FROM taxon_catalog newer WHERE newer.taxon_group=c.taxon_group "+
            "AND LOWER(newer.scientific_name)=LOWER(c.scientific_name) "+
            "AND newer.taxon_key NOT LIKE 'STARTER:%'))");
        ArrayList<String> args=new ArrayList<>();
        args.add(province);args.add(island);args.add(group);
        if(!term.isEmpty()){
            sql.append(" AND (LOWER(c.common_name) LIKE ? ESCAPE '!' OR "+
                "LOWER(c.scientific_name) LIKE ? ESCAPE '!' OR EXISTS "+
                "(SELECT 1 FROM taxon_synonyms s WHERE s.taxon_key=c.taxon_key "+
                "AND LOWER(s.alias_name) LIKE ? ESCAPE '!'))");
            String pattern="%"+TaxonSearchPolicy.escapeLike(
                term.toLowerCase(Locale.ROOT))+"%";
            args.add(pattern);args.add(pattern);args.add(pattern);
        }
        if(local){
            sql.append(" AND (").append(provinceMatch).append(" OR ")
                .append(islandMatch).append(")");
            args.add(province);args.add(island);
        }
        // Current national catalogs contain <1000 taxa per group. This cap
        // prevents memory exhaustion from future unreviewed giant uploads.
        sql.append(" ORDER BY c.scientific_name COLLATE NOCASE LIMIT ?");
        args.add(String.valueOf(MAX_QUERY_ROWS+1));
        try(FieldDb helper=new FieldDb(getApplicationContext())){
            SQLiteDatabase db=helper.getReadableDatabase();
            try(Cursor c=db.rawQuery(sql.toString(),args.toArray(new String[0]))){
                while(c.moveToNext()){
                    Species item=new Species();
                    item.key=string(c,0);item.group=string(c,1);
                    item.common=string(c,2);item.scientific=string(c,3);
                    item.family=string(c,4);item.origin=string(c,5);
                    item.iucn=string(c,6);item.ph=string(c,7);
                    item.source=string(c,8);item.version=string(c,9);
                    item.evidence=c.getInt(10);
                    String profileHabitats=string(c,11);
                    boolean strict=!c.isNull(12)&&c.getInt(12)!=0;
                    Double min=c.isNull(13)?null:c.getDouble(13);
                    Double max=c.isNull(14)?null:c.getDouble(14);
                    boolean reviewed="reviewed".equals(string(c,15));
                    if(reviewed){
                        item.reviewedHabitats=profileHabitats.replace("|",", ");
                        if(min!=null||max!=null)item.reviewedElevation=
                            (min==null?"?":Math.round(min)+"")+
                            "–"+(max==null?"?":Math.round(max)+"")+" m";
                    }
                    item.screening=HabitatLikelihood.assess(item.evidence,
                        siteCode,altitude,uncertainty,
                        profileHabitats,strict,min,max,reviewed);
                    list.add(item);
                }
            }
        }
        return list;
    }
    private void refresh(){
        if(search==null||cards==null)return;
        savePickerSettings();
        final int ticket=++generation;
        final String term=search.getText().toString().trim();
        final String group=currentGroup();
        final boolean onlyLocal=views.getSelectedItemPosition()==1;
        final int sorting=sortMode,filter=statusFilter;
        final String siteCode=habitatCode;
        final Double altitude=effectiveElevation(),uncertainty=effectiveUncertainty();
        offset=0;loaded=new ArrayList<>();truncated=false;
        cards.removeAllViews();
        status.setText("Finding "+group+"…");
        worker.execute(()->{
            try{
                if(elevationMode==0&&!surveyId.isEmpty()){
                    try(FieldDb helper=new FieldDb(getApplicationContext())){
                        StationEnvironmentStore.recordImprovedMsl(
                            helper.getWritableDatabase(),surveyId,visitId,stationId,
                            TrackService.currentFix());
                    }
                }
                List<Species> results=searchCatalog(group,term,onlyLocal,
                    siteCode,altitude,uncertainty);
                boolean capped=results.size()>MAX_QUERY_ROWS;
                if(capped)results=results.subList(0,MAX_QUERY_ROWS);
                List<Species> filtered=new ArrayList<>();
                for(Species species:results)
                    if(SpeciesSortPolicy.keep(filter,species.screening.label))
                        filtered.add(species);
                Collections.sort(filtered,(a,b)->SpeciesSortPolicy.compare(sorting,
                    a.common,a.scientific,a.family,a.screening.scorePercent,
                    b.common,b.scientific,b.family,b.screening.scorePercent));
                ui.post(()->{
                    if(isFinishing()||isDestroyed()||ticket!=generation)return;
                    loaded=filtered;truncated=capped;
                    appendPage();
                });
            }catch(Exception error){
                ui.post(()->{
                    if(isFinishing()||isDestroyed()||ticket!=generation)return;
                    status.setText("Catalog unavailable: "+
                        error.getClass().getSimpleName());
                });
            }
        });
    }
    private void appendPage(){
        int stop=Math.min(offset+PAGE,loaded.size());
        for(int i=offset;i<stop;i++){
            Species item=loaded.get(i);
            if(sortMode==SpeciesSortPolicy.FAMILY||sortMode==SpeciesSortPolicy.GENUS){
                String group=sortMode==SpeciesSortPolicy.FAMILY?item.family:
                    SpeciesSortPolicy.genus(item.scientific);
                String preceding=i==0?"":(sortMode==SpeciesSortPolicy.FAMILY?
                    loaded.get(i-1).family:SpeciesSortPolicy.genus(loaded.get(i-1).scientific));
                if(i==0||!group.equalsIgnoreCase(preceding)){
                    TextView section=text(group.isEmpty()?"Unassigned family/genus":group,
                        14,true);
                    section.setTextColor(UiKit.FOREST);
                    section.setPadding(dp(3),dp(9),0,dp(8));
                    cards.addView(section);
                }
            }
            addSpecies(item);
        }
        offset=stop;
        status.setText(offset+" of "+loaded.size()+" species"+
            (truncated?" · refine search for more":""));
        if(offset<loaded.size()){
            Button next=UiKit.button(this,"Load more species","secondary");
            cards.addView(next);
            next.setOnClickListener(v->{
                cards.removeView(next);
                appendPage();
            });
        }else if(offset==0){
            cards.addView(text("No matches for these filters. "+
                "Change Sort & Filter or show All species.",14,false));
        }
    }
    private void addSpecies(Species item){
        LinearLayout card=new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14),dp(10),dp(14),dp(10));
        card.setBackground(UiKit.surface(this));
        LinearLayout.LayoutParams cardLp=new LinearLayout.LayoutParams(-1,-2);
        cardLp.bottomMargin=dp(8);cards.addView(card,cardLp);
        String name=item.common.isEmpty()?item.scientific:item.common;
        TextView heading=text(name,16,true);card.addView(heading);
        if(!name.equals(item.scientific)){
            TextView scientific=text(item.scientific,14,false);
            scientific.setTypeface(null,Typeface.ITALIC);card.addView(scientific);
        }
        // Source and conservation detail remain available on the Details page.
        StringBuilder identifiers=new StringBuilder(item.family);
        if(identifiers.length()>0){
            TextView meta=text(identifiers.toString(),12,false);
            meta.setTextColor(UiKit.MUTED);card.addView(meta);
        }
        String label=item.screening.scorePercent==null?
            "Insufficient Data · not scored":
            item.screening.scorePercent+"% screening index · "+item.screening.label;
        int indicator="Unlikely".equals(item.screening.label)?
            Color.rgb(167,64,56):"Likely".equals(item.screening.label)?
            Color.rgb(36,112,74):Color.rgb(123,94,31);
        if(item.screening.scorePercent==null)indicator=UiKit.MUTED;
        TextView likelihood=text(label,14,true);
        likelihood.setTextColor(indicator);
        likelihood.setPadding(0,dp(5),0,dp(2));
        card.addView(likelihood);
        // Text preserves the score without suggesting a measured probability.
        likelihood.setContentDescription(label+
            ". Tap for the documented basis of this screening assessment.");
        likelihood.setOnClickListener(v->new AlertDialog.Builder(this)
            .setTitle("Preliminary field-screening score")
            .setMessage(item.screening.explanation+
                "\n\nThis percentage is a weighted screening index, not the "+
                "statistical chance the species occurs here.")
            .setPositiveButton("Close",null).show());
        TextView range=text(OccurrenceEvidencePolicy.title(item.evidence),13,true);
        range.setTextColor(item.evidence==0?UiKit.MUTED:UiKit.FOREST);
        range.setPadding(0,dp(2),0,dp(3));card.addView(range);
        // Source, ecological references and profile detail are available under Details.
        LinearLayout buttons=new LinearLayout(this);
        Button details=UiKit.button(this,"Details","secondary");
        buttons.addView(details,new LinearLayout.LayoutParams(0,dp(48),1f));
        Button select=UiKit.button(this,"Use species","primary");
        LinearLayout.LayoutParams selectLp=new LinearLayout.LayoutParams(0,dp(48),1.5f);
        selectLp.leftMargin=dp(9);
        buttons.addView(select,selectLp);
        LinearLayout.LayoutParams controlLp=new LinearLayout.LayoutParams(-1,dp(48));
        controlLp.topMargin=dp(9);card.addView(buttons,controlLp);
        details.setOnClickListener(v->startActivity(
            new Intent(this,TaxonDetailActivity.class).putExtra("taxon_key",item.key)));
        select.setOnClickListener(v->select(
            item.common.isEmpty()?item.scientific:item.common+" · "+item.scientific,item.group));
    }
    private void select(String label,String group){
        Intent result=new Intent();
        result.putExtra("species_label",label);
        result.putExtra("species_group",group);
        setResult(RESULT_OK,result);
        finish();
    }
    @Override protected void onResume(){
        super.onResume();
        gpsAutoTracking=true;
        ui.post(gpsMonitor);
    }
    @Override protected void onPause(){
        gpsAutoTracking=false;
        ui.removeCallbacks(gpsMonitor);
        savePickerSettings();
        super.onPause();
    }
    @Override protected void onDestroy(){
        generation++;
        ui.removeCallbacksAndMessages(null);
        worker.shutdownNow();
        super.onDestroy();
    }
}
