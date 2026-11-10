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
import android.widget.AutoCompleteTextView;
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
    private static final String[] VIEWS={"Name · A-Z","Family · A-Z","Genus · A-Z","Local source records"};
    private static final String[] PAIC_VIEWS={"Name · A-Z","Family · A-Z","Genus · A-Z","Habitat first"};
    private final Handler ui=new Handler(Looper.getMainLooper());
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private final Runnable delayed=this::refresh;
    private AutoCompleteTextView search;
    private SpeciesReferenceSuggestionAdapter nameSuggestions;
    private FieldDb suggestionDb;
    private Spinner groups,views,scope;
    private Button shortlistToggle;
    private boolean shortlistEnabled=true;
    private boolean archivedCatalogAvailable=false;
    private LinearLayout cards;
    private TextView status,siteSummary;
    private int generation=0,offset=0,sortMode=0,statusFilter=0;
    private String habitatCode="";
    private Double siteElevation=null,elevationUncertainty=null;
    private int elevationMode=0; // 0 GPS MSL, 1 manual MSL, 2 unassessed
    private String visitId="",stationId="",surveyId="";
    private SurveyHabitatSetup.State surveyPreparation;
    private boolean paicCatalogActive=false;
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
    private int savedGroupIndex=0,savedViewIndex=0,savedScopeIndex=0;
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
        // Legacy probability-like score sorts/filters are no longer shown.
        sortMode=checked(surveyOptions.getInt(pref("sort"),0),3);
        statusFilter=0;
        savedGroupIndex=checked(surveyOptions.getInt(pref("group"),0),availableGroups.length);
        savedViewIndex=checked(surveyOptions.getInt(pref("view"),0),VIEWS.length);
        if(paicCatalogActive&&!surveyOptions.getBoolean(pref("order_v2"),false)&&
                savedViewIndex==1)savedViewIndex=3;
        savedScopeIndex=checked(surveyOptions.getInt(pref("scope"),0),2);
        shortlistEnabled=surveyOptions.getBoolean(pref("habitat_shortlist"),true);
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
            .putBoolean(pref("order_v2"),true)
            .putInt(pref("view"),views==null?0:views.getSelectedItemPosition())
            .putInt(pref("scope"),scope==null?0:scope.getSelectedItemPosition())
            .putBoolean(pref("habitat_shortlist"),shortlistEnabled)
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
        try(FieldDb helper=new FieldDb(this)){
            SQLiteDatabase sql=helper.getReadableDatabase();
            surveyPreparation=SurveyHabitatSetup.load(sql,this.surveyId);
            try(Cursor count=sql.rawQuery(
                    "SELECT COUNT(*) FROM taxon_catalog WHERE "+
                    "taxon_key LIKE 'LIKAS725:%'",null)){
                paicCatalogActive=count.moveToFirst()&&count.getInt(0)==714;
            }
            try(Cursor archive=sql.rawQuery("SELECT COUNT(*) FROM sqlite_master "+
                    "WHERE type='table' AND name='likas_prior_taxon_catalog'",null)){
                archivedCatalogAvailable=paicCatalogActive&&archive.moveToFirst()&&
                    archive.getInt(0)>0;
            }
        }catch(Exception ignored){}
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
        root.addView(UiKit.navigationHeader(this,"Species","Survey",this::finish));

        search=new AutoCompleteTextView(this){
            @Override public boolean enoughToFilter(){return true;}
        };
        search.setSingleLine(true);search.setTextSize(16);
        search.setThreshold(1);
        search.setHint("Search species");
        search.setContentDescription("Search offline species by common, scientific or former name");
        suggestionDb=new FieldDb(this);
        nameSuggestions=new SpeciesReferenceSuggestionAdapter(this,
            suggestionDb,availableGroups[Math.max(0,savedGroupIndex)]);
        search.setAdapter(nameSuggestions);
        nameSuggestions.setArchivedCatalog(archivedCatalogAvailable&&savedScopeIndex==1);
        search.setDropDownHeight(dp(300));
        search.setOnItemClickListener((parent,view,pos,id)->{
            String label=parent.getItemAtPosition(pos).toString();
            int separator=label.lastIndexOf(" · ");
            search.setText(separator<0?label:label.substring(separator+3));
            search.setSelection(search.length());
        });
        LinearLayout searchRow=new LinearLayout(this);
        searchRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams searchParams=new LinearLayout.LayoutParams(0,dp(54),1f);
        searchRow.addView(search,searchParams);
        Button dropdown=UiKit.button(this,"▾","quiet");
        dropdown.setContentDescription("Show species name suggestions");
        dropdown.setOnClickListener(v->{
            search.requestFocus();
            nameSuggestions.getFilter().filter(search.getText().toString(),
                count->{if(count>0)search.showDropDown();});
        });
        searchRow.addView(dropdown,new LinearLayout.LayoutParams(dp(48),dp(54)));
        root.addView(searchRow);
        // Keep unidentified records visible without taking width from species search.
        LinearLayout unidentifiedRow=new LinearLayout(this);
        unidentifiedRow.setGravity(Gravity.END);
        Button recent=UiKit.button(this,"Recent","quiet");
        recent.setTextSize(13);
        recent.setContentDescription("Search species names from recently saved field records");
        LinearLayout.LayoutParams recentSpace=new LinearLayout.LayoutParams(-2,dp(48));
        recentSpace.topMargin=dp(6);
        recentSpace.rightMargin=dp(7);
        unidentifiedRow.addView(recent,recentSpace);
        recent.setOnClickListener(v->showRecentSearches());
        Button unknown=UiKit.button(this,"Unidentified","secondary");
        unknown.setTextSize(13);
        unknown.setContentDescription("Record an unidentified species");
        LinearLayout.LayoutParams unknownSpace=new LinearLayout.LayoutParams(-2,dp(48));
        unknownSpace.topMargin=dp(6);
        unidentifiedRow.addView(unknown,unknownSpace);
        root.addView(unidentifiedRow);
        unknown.setOnClickListener(v->{
            String[] unknowns=Taxa.unknownLabels(currentGroup());
            new AlertDialog.Builder(this).setTitle("Unidentified observation")
                .setItems(unknowns,(dialog,which)->select(unknowns[which],currentGroup()))
                .setNegativeButton("Back",null).show();
        });

        // One ordering control and a separate, explicit reference scope.
        LinearLayout quick=new LinearLayout(this);
        Button conditions=UiKit.button(this,"Site context","secondary");
        quick.addView(conditions,new LinearLayout.LayoutParams(-1,dp(52)));
        conditions.setOnClickListener(v->new AlertDialog.Builder(this)
            .setTitle("Site context")
            .setMessage(siteSummary==null?"":siteSummary.getText().toString())
            .setNegativeButton("Close",null)
            .setPositiveButton("Edit conditions",(dialog,which)->openSiteConditions())
            .show());
        root.addView(quick);
        shortlistToggle=UiKit.button(this,"Habitat shortlist","pond");
        shortlistToggle.setTextSize(13);
        shortlistToggle.setMinHeight(dp(56));
        shortlistToggle.setSingleLine(false);
        shortlistToggle.setContentDescription("Switch between habitat-guide shortlist and all catalog species");
        LinearLayout.LayoutParams shortlistSpace=new LinearLayout.LayoutParams(-1,-2);
        shortlistSpace.topMargin=dp(5);
        root.addView(shortlistToggle,shortlistSpace);
        shortlistToggle.setOnClickListener(v->{
            shortlistEnabled=!shortlistEnabled;
            savePickerSettings();
            refresh();
        });
        LinearLayout controls=new LinearLayout(this);
        controls.setOrientation(LinearLayout.VERTICAL);
        if(archivedCatalogAvailable){
            scope=new Spinner(this);
            scope.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Mindanao PAIC (714)","Philippine catalog"}){
                @Override public View getView(int position,View convertView,
                        android.view.ViewGroup parent){
                    TextView selected=(TextView)super.getView(position,convertView,parent);
                    selected.setText(position==0?"PAIC · 714":"Philippines");
                    selected.setContentDescription(position==0?
                        "Mindanao PAIC 714 species":"Archived Philippine catalog");
                    return selected;
                }
            });
            controls.addView(text("Checklist",12,true));
            controls.addView(scope,new LinearLayout.LayoutParams(-1,dp(54)));
            scope.setSelection(savedScopeIndex);
            scope.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
                @Override public void onItemSelected(AdapterView<?> parent,View view,int pos,long id){
                    if(nameSuggestions!=null){
                        nameSuggestions.setArchivedCatalog(pos==1);
                        nameSuggestions.getFilter().filter(search.getText().toString());
                    }
                    refresh();
                }
                @Override public void onNothingSelected(AdapterView<?> parent){}
            });
        }
        groups=new Spinner(this);
        groups.setAdapter(new ArrayAdapter<String>(this,
            android.R.layout.simple_spinner_dropdown_item,availableGroups));
        if(availableGroups.length>1){
            controls.addView(text("Wildlife group",12,true));
            controls.addView(groups,new LinearLayout.LayoutParams(-1,dp(54)));
        }
        views=new Spinner(this);
        views.setAdapter(new ArrayAdapter<String>(this,
            android.R.layout.simple_spinner_dropdown_item,
            paicCatalogActive?PAIC_VIEWS:VIEWS));
        controls.addView(text("Order",12,true));
        controls.addView(views,new LinearLayout.LayoutParams(-1,dp(54)));
        root.addView(controls);
        groups.setSelection(savedGroupIndex);
        if(paicCatalogActive&&surveyPreparation!=null&&
            "suggested".equals(surveyPreparation.mode)&&
            (!PaicHabitatPolicy.decode(surveyPreparation.habitats).isEmpty()||
                GpsElevation.read(TrackService.currentFix()).usableForHabitat)&&
            !surveyOptions.contains(pref("view")))views.setSelection(3);
        else views.setSelection(savedViewIndex);

        siteSummary=text("",12,false);
        siteSummary.setTextColor(UiKit.FOREST);
        siteSummary.setPadding(0,0,0,dp(4));
        // Site evidence remains available in the checklist context information.
        updateSiteSummary();
        status=text("Loading offline species…",12,false);
        status.setTextColor(UiKit.MUTED);status.setPadding(0,dp(9),0,dp(7));
        status.setContentDescription("Species results and checklist region");
        status.setMinHeight(dp(40));
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
        groups.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override public void onItemSelected(AdapterView<?> parent,View view,int pos,long id){
                if(nameSuggestions!=null)nameSuggestions.setGroup(currentGroup());
                refresh();
            }
            @Override public void onNothingSelected(AdapterView<?> parent){}
        });
        views.setOnItemSelectedListener(chosen);
        refresh();
    }
    /** Search-only convenience; never declares a new sighting or identity. */
    private void showRecentSearches(){
        String[] names;
        try(FieldDb helper=new FieldDb(getApplicationContext())){
            names=RecentTaxa.names(helper.getReadableDatabase(),currentGroup());
        }
        if(names.length==0){
            status.setText("No saved species names for this wildlife group");
            return;
        }
        new AlertDialog.Builder(this).setTitle("Recent field records")
            .setItems(names,(dialog,which)->{
                String label=names[which];
                int separator=label.lastIndexOf(" · ");
                search.setText(separator<0?label:label.substring(separator+3));
                search.setSelection(search.length());
            })
            .setNegativeButton("Back",null).show();
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
        if(paicCatalogActive&&surveyPreparation!=null){
            String chosen=surveyPreparation.habitats.replace("|",", ");
            if(!chosen.isEmpty())habitatName="Survey habitats: "+chosen+
                " · station habitat: "+habitatName;
        }
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
                shortlistEnabled=!habitatCode.isEmpty();
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
        String paicHabitat="",paicElevation="";
        int paicRank=0,siteRelevance=-1;
        int evidence;
        HabitatLikelihood.Result screening;
    }
    private List<Species> searchCatalog(String group,String term,boolean local,
            boolean broadCatalog,String siteCode,Double altitude,
            Double uncertainty)throws Exception{
        final String catalog=broadCatalog?"likas_prior_taxon_catalog":"taxon_catalog";
        final String evidenceTable=broadCatalog?
            "likas_prior_taxon_occurrence_evidence":"taxon_occurrence_evidence";
        final String habitatTable=broadCatalog?
            "likas_prior_taxon_habitat_profiles":"taxon_habitat_profiles";
        final String synonymTable=broadCatalog?
            "likas_prior_taxon_synonyms":"taxon_synonyms";
        List<Species> list=new ArrayList<>();
        String island=island(geographicRegion);
        String provinceMatch="EXISTS(SELECT 1 FROM "+evidenceTable+" e "+
            "WHERE e.taxon_key=c.taxon_key AND e.review_status='source_listed' AND e.area_type='province' "+
            "AND LOWER(e.area_label)=LOWER(?))";
        String islandMatch="EXISTS(SELECT 1 FROM "+evidenceTable+" e "+
            "WHERE e.taxon_key=c.taxon_key AND e.review_status='source_listed' AND e.area_type='island' "+
            "AND LOWER(e.area_label)=LOWER(?))";
        // Only reviewed source listings confer geographic evidence rank.
        // Imported unreviewed records remain visible on source detail pages.
        String evidence="CASE WHEN "+provinceMatch+" THEN 2 WHEN "+islandMatch+
            " THEN 1 ELSE 0 END";
        StringBuilder sql=new StringBuilder(
            "SELECT c.taxon_key,c.taxon_group,c.common_name,c.scientific_name,"+
            "c.family,c.distribution_status,c.iucn_red_list,c.ph_red_list,"+
            "c.source,c.source_version,"+evidence+" AS evidence_rank,"+
            "p.habitat_codes,p.strict_habitat,p.elevation_min_m,"+
            "p.elevation_max_m,p.review_status,ma.habitat_text,ma.elevation_text "+
            "FROM "+catalog+" c LEFT JOIN "+habitatTable+" p "+
            "ON p.taxon_key=c.taxon_key "+
            "LEFT JOIN likas_master_attributes ma ON ma.taxon_key=c.taxon_key "+
            "WHERE c.taxon_group=? "+
            "AND (c.taxon_key NOT LIKE 'STARTER:%' OR NOT EXISTS "+
            "(SELECT 1 FROM "+catalog+" newer WHERE newer.taxon_group=c.taxon_group "+
            "AND LOWER(newer.scientific_name)=LOWER(c.scientific_name) "+
            "AND newer.taxon_key NOT LIKE 'STARTER:%'))");
        ArrayList<String> args=new ArrayList<>();
        args.add(province);args.add(island);args.add(group);
        if(!term.isEmpty()){
            sql.append(" AND (LOWER(c.common_name) LIKE ? ESCAPE '!' OR "+
                "LOWER(c.scientific_name) LIKE ? ESCAPE '!' OR EXISTS "+
                "(SELECT 1 FROM "+synonymTable+" s WHERE s.taxon_key=c.taxon_key "+
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
                    item.paicHabitat=string(c,16);
                    item.paicElevation=string(c,17);
                    if(paicCatalogActive&&!broadCatalog)
                        item.siteRelevance=PaicHabitatPolicy.siteHabitatRelevance(
                            item.paicHabitat,siteCode);
                    if(paicCatalogActive&&!broadCatalog&&surveyPreparation!=null)
                        item.paicRank=PaicHabitatPolicy.ranking(item.paicHabitat,
                            item.paicElevation,PaicHabitatPolicy.decode(
                                surveyPreparation.habitats),altitude);
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
        final boolean broadCatalog=archivedCatalogAvailable&&scope!=null&&
            scope.getSelectedItemPosition()==1;
        final boolean siteSuggestions=paicCatalogActive&&!broadCatalog&&
            views.getSelectedItemPosition()==3;
        final boolean onlyLocal=!paicCatalogActive&&
            views.getSelectedItemPosition()==3;
        final int sorting=views.getSelectedItemPosition()<3?
            views.getSelectedItemPosition():0,filter=statusFilter;
        sortMode=sorting;
        final String siteCode=habitatCode;
        final boolean habitatShortlist=paicCatalogActive&&!broadCatalog&&
            !siteCode.isEmpty()&&shortlistEnabled;
        if(shortlistToggle!=null){
            shortlistToggle.setVisibility(paicCatalogActive&&!broadCatalog&&
                !siteCode.isEmpty()?View.VISIBLE:View.GONE);
            shortlistToggle.setText(shortlistEnabled?
                "Habitat shortlist · Show all species":
                "All species · Show habitat shortlist");
            shortlistToggle.setContentDescription(shortlistEnabled?
                "Showing species with matching or broad habitat guide labels. Show all species":
                "Showing all species. Filter to habitat-guide shortlist");
        }
        final Double altitude=effectiveElevation(),uncertainty=effectiveUncertainty();
        offset=0;loaded=new ArrayList<>();truncated=false;
        cards.removeAllViews();
        if(onlyLocal&&province.trim().isEmpty()&&island(geographicRegion).isEmpty()){
            status.setText("Choose a region to view local records");
            cards.addView(text("Switch to All species or set a survey region.",14,false));
            return;
        }
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
                    broadCatalog,siteCode,altitude,uncertainty);
                boolean capped=results.size()>MAX_QUERY_ROWS;
                if(capped)results=results.subList(0,MAX_QUERY_ROWS);
                List<Species> filtered=new ArrayList<>();
                for(Species species:results)
                    if(SpeciesSortPolicy.keep(filter,species.screening.label)&&
                            (!habitatShortlist||species.siteRelevance>0))
                        filtered.add(species);
                Collections.sort(filtered,(a,b)->{
                    if(habitatShortlist){
                        int relevance=Integer.compare(b.siteRelevance,a.siteRelevance);
                        if(relevance!=0)return relevance;
                    }
                    if(paicCatalogActive&&surveyPreparation!=null&&siteSuggestions){
                        int rank=Integer.compare(b.paicRank,a.paicRank);
                        if(rank!=0)return rank;
                    }
                    return SpeciesSortPolicy.compare(sorting,
                        a.common,a.scientific,a.family,a.screening.scorePercent,
                        b.common,b.scientific,b.family,b.screening.scorePercent);
                });
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
    private String siteSortLabel(){
        return paicCatalogActive&&views!=null&&views.getSelectedItemPosition()==3&&
            (scope==null||scope.getSelectedItemPosition()==0)?
            " · habitat first":"";
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
        String region=island(geographicRegion);
        status.setText(offset+" / "+loaded.size()+" species"+
            (paicCatalogActive&&!habitatCode.isEmpty()&&shortlistEnabled&&
                (scope==null||scope.getSelectedItemPosition()==0)?
                " · habitat shortlist":"")+
            (region.isEmpty()?"":" · "+region)+
            (truncated?" · refine search":"")+
            (siteSortLabel())+
            (archivedCatalogAvailable&&scope!=null&&scope.getSelectedItemPosition()==1?
                " · Philippine catalog":""));
        if(offset<loaded.size()){
            Button next=UiKit.button(this,"Load more species","secondary");
            cards.addView(next);
            next.setOnClickListener(v->{
                cards.removeView(next);
                appendPage();
            });
        }else if(offset==0){
            cards.addView(text(shortlistEnabled&&!habitatCode.isEmpty()&&
                paicCatalogActive&&(scope==null||scope.getSelectedItemPosition()==0)?
                "No matching habitat guide labels. Choose Show all species to inspect other taxa.":
                "No matches · change the search or filters",14,false));
        }
    }
    /** Show field-useful taxonomy and available conservation/ecology data first. */
    private void addSpecies(Species item){
        LinearLayout card=new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(12),dp(9),dp(12),dp(9));
        card.setBackground(UiKit.surface(this));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
        lp.bottomMargin=dp(7);
        cards.addView(card,lp);

        String name=item.common.isEmpty()?item.scientific:item.common;
        TextView heading=text(name,16,true);
        card.addView(heading);
        if(!name.equals(item.scientific)&&!item.scientific.isEmpty()){
            TextView scientific=text(item.scientific,14,false);
            scientific.setTypeface(null,Typeface.ITALIC);
            scientific.setTextColor(UiKit.FOREST);
            card.addView(scientific);
        }
        // Taxonomy, recorded distribution and published red-list fields.
        StringBuilder taxonomy=new StringBuilder();
        if(!item.family.isEmpty())taxonomy.append(item.family);
        if(displayValue(item.origin)){
            if(taxonomy.length()>0)taxonomy.append(" · ");
            taxonomy.append(item.origin);
        }
        if(taxonomy.length()>0){
            TextView meta=text(taxonomy.toString(),12,false);
            meta.setTextColor(UiKit.MUTED);
            meta.setMaxLines(2);
            card.addView(meta);
        }
        StringBuilder conservation=new StringBuilder();
        if(displayValue(item.iucn))conservation.append("IUCN: ").append(item.iucn);
        if(displayValue(item.ph)){
            if(conservation.length()>0)conservation.append(" · ");
            conservation.append("DENR: ").append(item.ph);
        }
        if(conservation.length()>0){
            TextView redlist=text(conservation.toString(),12,true);
            redlist.setTextColor(UiKit.FOREST);
            card.addView(redlist);
        }
        if(item.evidence>0){
            TextView source=text(OccurrenceEvidencePolicy.title(item.evidence),12,false);
            source.setTextColor(UiKit.FOREST);
            card.addView(source);
        }
        if(paicCatalogActive&&!habitatCode.isEmpty()&&item.siteRelevance>0){
            TextView siteLabel=text(item.siteRelevance==2?
                "Observed habitat · named in guide":
                "Observed habitat · broad guide category",12,true);
            siteLabel.setTextColor(UiKit.FOREST);
            card.addView(siteLabel);
        }
        if(item.paicRank>0&&paicCatalogActive){
            TextView match=text("Habitat / elevation overlap",12,true);
            match.setTextColor(UiKit.FOREST);
            card.addView(match);
        }
        String habitats=paicCatalogActive&&!item.paicHabitat.isEmpty()?
            item.paicHabitat:item.reviewedHabitats;
        String elevations=paicCatalogActive&&!item.paicElevation.isEmpty()?
            item.paicElevation:item.reviewedElevation;
        if(displayValue(habitats)){
            TextView ecological=text("Habitat: "+habitats,12,false);
            ecological.setMaxLines(2);
            ecological.setEllipsize(android.text.TextUtils.TruncateAt.END);
            card.addView(ecological);
        }
        if(displayValue(elevations)){
            TextView height=text("Elevation: "+elevations,12,false);
            height.setMaxLines(1);
            height.setEllipsize(android.text.TextUtils.TruncateAt.END);
            card.addView(height);
        }
        LinearLayout buttons=new LinearLayout(this);
        buttons.setGravity(Gravity.CENTER_VERTICAL);
        Button details=UiKit.button(this,"ⓘ","quiet");
        details.setContentDescription("Open species taxonomy, sources and full ecological profile");
        buttons.addView(details,new LinearLayout.LayoutParams(dp(48),dp(48)));
        Button select=UiKit.button(this,"Select","primary");
        select.setContentDescription("Select "+name+" as the observation identification");
        LinearLayout.LayoutParams selectLp=new LinearLayout.LayoutParams(0,dp(48),1f);
        selectLp.leftMargin=dp(7);
        buttons.addView(select,selectLp);
        LinearLayout.LayoutParams controls=new LinearLayout.LayoutParams(-1,dp(48));
        controls.topMargin=dp(8);
        card.addView(buttons,controls);
        details.setOnClickListener(v->startActivity(
            new Intent(this,TaxonDetailActivity.class).putExtra("taxon_key",item.key)));
        select.setOnClickListener(v->select(
            item.common.isEmpty()?item.scientific:item.common+" · "+item.scientific,
            item.group,item.key,item.version));
    }
    private static boolean displayValue(String value){
        return value!=null&&!value.trim().isEmpty()&&
            !value.trim().equals("—")&&!value.trim().equals("-");
    }
    private void select(String label,String group){
        select(label,group,"","");
    }
    private void select(String label,String group,String key,String version){
        Intent result=new Intent();
        result.putExtra("species_label",label);
        result.putExtra("species_group",group);
        result.putExtra("taxon_key",key);
        result.putExtra("taxon_source_version",version);
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
        if(suggestionDb!=null)suggestionDb.close();
        super.onDestroy();
    }
}
