package org.phwildlife.survey;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Optional pre-survey preparation between GPS preflight and the recorder. */
public final class HabitatsSpeciesActivity extends Activity {
    private static final int PICK_MAP=943;
    private final Set<String> confirmed=new HashSet<>();
    private List<String> suggested=new ArrayList<>();
    private LinearLayout body,checks;
    private TextView mapSummary,speciesSummary,siteSummary,siteEvidence,selectedSummary;
    private Button checklistButton;
    private RadioGroup checklistMode;
    private boolean othersVisible=false;
    private boolean preparing=false;
    private double preflightLat=Double.NaN,preflightLon=Double.NaN;
    private int dp(int value){return Math.round(value*getResources().getDisplayMetrics().density);}
    private TextView label(String value,int size,boolean bold){
        TextView t=new TextView(this);t.setText(value);t.setTextSize(size);
        t.setTextColor(bold?UiKit.INK:UiKit.MUTED);
        t.setPadding(dp(2),dp(5),dp(2),dp(5));
        if(bold)t.setTypeface(null,android.graphics.Typeface.BOLD);
        return t;
    }
    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);
        if(!getIntent().getBooleanExtra("create_new",false)){finish();return;}
        preflightLat=getIntent().getDoubleExtra("start_gps_latitude",Double.NaN);
        preflightLon=getIntent().getDoubleExtra("start_gps_longitude",Double.NaN);
        if(saved!=null){
            String[] restored=saved.getStringArray("chosen");
            if(restored!=null)for(String item:restored)
                if(PaicHabitatPolicy.isKnown(item))confirmed.add(item);
            othersVisible=saved.getBoolean("others",false);
        }
        body=new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        // Essentials first. Source details stay one tap away.
        siteSummary=label("",13,true);
        body.addView(siteSummary);
        LinearLayout siteDetails=new LinearLayout(this);
        siteDetails.setOrientation(LinearLayout.VERTICAL);
        siteEvidence=label("",12,false);
        siteDetails.addView(siteEvidence);
        siteDetails.addView(label("One GPS point is not the whole survey area. "+
            "Device MSL elevation is screening context, not a habitat observation.",12,false));
        siteDetails.setVisibility(View.GONE);
        Button siteToggle=UiKit.button(this,"GPS details","quiet");
        siteToggle.setContentDescription("Show GPS and elevation context");
        siteToggle.setOnClickListener(v->{
            boolean open=siteDetails.getVisibility()!=View.VISIBLE;
            siteDetails.setVisibility(open?View.VISIBLE:View.GONE);
            siteToggle.setText(open?"Hide GPS details":"GPS details");
            siteToggle.setContentDescription(open?
                "Hide GPS and elevation context":"Show GPS and elevation context");
        });
        refreshSite();

        body.addView(FieldVisuals.section(this,"Habitats",null));
        // Habitat selections below are observer-confirmed only.
        mapSummary=label("",12,false);
        body.addView(mapSummary);
        selectedSummary=label("0 habitats confirmed",12,true);
        body.addView(selectedSummary);
        checks=new LinearLayout(this);
        checks.setOrientation(LinearLayout.VERTICAL);
        body.addView(checks);

        LinearLayout mapOptions=new LinearLayout(this);
        mapOptions.setOrientation(LinearLayout.VERTICAL);
        Button mapCredit=UiKit.button(this,"Source & license","quiet");
        mapCredit.setContentDescription("ESA WorldCover 2020 citation and CC BY 4.0 license");
        mapCredit.setOnClickListener(v->new AlertDialog.Builder(this)
            .setTitle("ESA WorldCover 2020 · source")
            .setMessage("© ESA WorldCover project 2020 / Contains modified "+
                "Copernicus Sentinel data (2020) processed by ESA WorldCover consortium.\n\n"+
                "Source: Zanaga et al. (2021), ESA WorldCover 10 m 2020 v100. "+
                "DOI: 10.5281/zenodo.5571936. License: CC BY 4.0.\n\n"+
                "APO aggregates land cover to approximately 500 m; these are "+
                "historical mapped suggestions, not verified current habitats.")
            .setPositiveButton("OK",null).show());
        mapOptions.addView(mapCredit);
        Button importMap=UiKit.button(this,PaicLandCoverPack.installed(this)?
            "Replace offline map":"Import offline map","secondary");
        importMap.setOnClickListener(v->pickMap());
        if(BundledPrivateReferences.hasBundledMap(this)&&
                PaicLandCoverPack.installed(this))
            importMap.setVisibility(View.GONE);
        mapOptions.addView(importMap,new LinearLayout.LayoutParams(-1,-2));
        mapOptions.setVisibility(View.GONE);
        Button mapToggle=UiKit.button(this,"Map source & files","quiet");
        mapToggle.setContentDescription("Show land-cover source, license and file management");
        mapToggle.setOnClickListener(v->{
            boolean open=mapOptions.getVisibility()!=View.VISIBLE;
            mapOptions.setVisibility(open?View.VISIBLE:View.GONE);
            mapToggle.setText(open?"Hide map options":"Map source & files");
            mapToggle.setContentDescription(open?
                "Hide land-cover source and file management":
                "Show land-cover source, license and file management");
        });
        body.addView(FieldVisuals.section(this,"Species checklist",null));
        checklistMode=new RadioGroup(this);
        checklistMode.setOrientation(RadioGroup.VERTICAL);
        RadioButton ranked=new RadioButton(this);
        ranked.setId(3001);ranked.setText("Suggested species first");
        checklistMode.addView(ranked);
        RadioButton full=new RadioButton(this);
        full.setId(3002);full.setText("Full checklist · no site ranking");
        checklistMode.addView(full);
        checklistMode.check(saved==null||saved.getBoolean("ranked",true)?3001:3002);
        // Keep exported mode unchanged while hiding the two permanent radio rows.
        checklistMode.setVisibility(View.GONE);
        checklistMode.setOnCheckedChangeListener((g,id)->{
            updateChecklistButton();
            refreshSpecies();
        });
        body.addView(checklistMode);
        checklistButton=UiKit.button(this,"Checklist: Suggested","secondary");
        checklistButton.setContentDescription("Choose suggested-first or full species checklist");
        checklistButton.setOnClickListener(v->new AlertDialog.Builder(this)
            .setTitle("Species checklist")
            .setSingleChoiceItems(new String[]{"Suggested first","Full checklist"},
                checklistMode.getCheckedRadioButtonId()==3001?0:1,
                (dialog,choice)->{
                    checklistMode.check(choice==0?3001:3002);
                    dialog.dismiss();
                })
            .setNegativeButton("Cancel",null).show());
        body.addView(checklistButton,new LinearLayout.LayoutParams(-1,-2));
        speciesSummary=label("",12,false);body.addView(speciesSummary);
        body.addView(label("Matching does not confirm species presence",12,false));
        body.addView(siteToggle);
        body.addView(siteDetails);
        body.addView(mapToggle);
        body.addView(mapOptions);
        LinearLayout page=UiKit.page(this,"Habitats & Species",
            "Survey GPS",this::finish,body);
        LinearLayout footer=new LinearLayout(this);
        footer.setOrientation(LinearLayout.VERTICAL);
        footer.setPadding(0,dp(6),0,dp(4));
        Button begin=UiKit.button(this,"Begin survey","primary");
        begin.setOnClickListener(v->beginSurvey());
        begin.setMinHeight(dp(56));
        footer.addView(begin,new LinearLayout.LayoutParams(-1,-2));
        page.addView(footer);
        setContentView(page);
        updateMap();
        renderHabitats();
        updateChecklistButton();
        refreshSpecies();
    }
    private void updateChecklistButton(){
        if(checklistButton==null||checklistMode==null)return;
        checklistButton.setText(checklistMode.getCheckedRadioButtonId()==3001?
            "Checklist: Suggested":"Checklist: Full");
    }
    private void updateSelectedSummary(){
        if(selectedSummary==null)return;
        selectedSummary.setText(confirmed.isEmpty()?"0 habitats confirmed":
            confirmed.size()+" habitat"+(confirmed.size()==1?"":"s")+" confirmed");
    }
    private void refreshSite(){
        boolean missing=getIntent().getBooleanExtra("start_without_gps",false);
        boolean hasCoords=!missing&&Double.isFinite(preflightLat)&&Double.isFinite(preflightLon);
        float accuracy=getIntent().getFloatExtra("start_gps_accuracy_m",Float.NaN);
        String accuracyText=Float.isFinite(accuracy)&&accuracy>0?
            String.format(Locale.US," · ±%.1f m",accuracy):"";
        String group=getIntent().getStringExtra("survey_profile");
        siteSummary.setText((group==null?"":group+" · ")+
            (hasCoords?"GPS recorded"+accuracyText:"Start GPS missing"));
        GpsElevation.Sample altitude=GpsElevation.read(TrackService.currentFix());
        String height=altitude.usableForHabitat?
            altitude.label():"MSL elevation unavailable";
        siteEvidence.setText((hasCoords?
            String.format(Locale.US,"Start: %.6f, %.6f%s",
                preflightLat,preflightLon,accuracyText):
            "Start coordinates unavailable")+"\\n"+height);
    }
    private void pickMap(){
        Intent select=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        select.setType("*/*");
        select.addCategory(Intent.CATEGORY_OPENABLE);
        select.putExtra(Intent.EXTRA_MIME_TYPES,new String[]{
            "text/plain","application/octet-stream"});
        startActivityForResult(select,PICK_MAP);
    }
    @Override protected void onActivityResult(int request,int result,Intent data){
        super.onActivityResult(request,result,data);
        if(request!=PICK_MAP||result!=RESULT_OK||data==null||data.getData()==null)return;
        Uri file=data.getData();
        mapSummary.setText("Validating offline land-cover file…");
        new Thread(()->{
            try(InputStream in=getContentResolver().openInputStream(file)){
                if(in==null)throw new IllegalArgumentException("File unavailable");
                PaicLandCoverPack.install(getApplicationContext(),in);
                runOnUiThread(()->{if(!isFinishing()){updateMap();renderHabitats();}});
            }catch(Exception error){
                runOnUiThread(()->{if(!isFinishing())
                    mapSummary.setText("Map not installed: "+error.getMessage()+
                        ". Choose habitats manually.");});
            }
        },"LIKAS-Offline-Habitat-Pack").start();
    }
    private void updateMap(){
        suggested=new ArrayList<>();
        if(getIntent().getBooleanExtra("start_without_gps",false)||
            !Double.isFinite(preflightLat)||!Double.isFinite(preflightLon)){
            mapSummary.setText("Map hints unavailable");
            return;
        }
        if(!PaicLandCoverPack.installed(this)){
            mapSummary.setText("Map unavailable · choose habitats manually");
            return;
        }
        try{
            suggested=PaicLandCoverPack.suggest(this,preflightLat,preflightLon);
            mapSummary.setText(suggested.isEmpty()?
                "No mapped classes · choose manually":
                "2020 map suggestions · ~500 m resolution · verify in field");
        }catch(Exception e){
            mapSummary.setText("Map unreadable · choose manually");
            suggested.clear();
        }
    }
    private void renderHabitats(){
        if(checks==null)return;
        checks.removeAllViews();
        updateSelectedSummary();
        if(!suggested.isEmpty()){
            for(String name:suggested)addCheck(name,true);
        }
        Button browse=UiKit.button(this,othersVisible?"Hide habitats":
            "Choose habitats (16)","secondary");
        browse.setOnClickListener(v->{othersVisible=!othersVisible;renderHabitats();});
        checks.addView(browse,new LinearLayout.LayoutParams(-1,dp(52)));
        for(String name:PaicHabitatPolicy.HABITATS)
            if(!suggested.contains(name)&&(othersVisible||confirmed.contains(name)))
                addCheck(name,false);
        if(suggested.isEmpty()&&!othersVisible){
            TextView noMap=label("Optional",12,false);
            checks.addView(noMap);
        }
    }
    private void addCheck(String name,boolean fromMap){
        CheckBox check=new CheckBox(this);
        check.setText(name);
        check.setTextColor(UiKit.INK);check.setTextSize(15);
        check.setMinHeight(dp(52));check.setPadding(dp(6),dp(4),dp(6),dp(4));
        check.setChecked(confirmed.contains(name));
        check.setContentDescription(name+(fromMap?
            ". Suggested from historical satellite land cover, not field confirmed.":""));
        check.setOnCheckedChangeListener((b,selected)->{
            if(selected)confirmed.add(name);else confirmed.remove(name);
            updateSelectedSummary();
            refreshSpecies();
        });
        checks.addView(check);
    }
    private void refreshSpecies(){
        if(speciesSummary==null)return;
        String profile=getIntent().getStringExtra("survey_profile");
        if(profile==null)profile="Birds";
        String[] groups="Herpetofauna".equals(profile)?
            new String[]{"Amphibians","Reptiles"}:new String[]{profile};
        GpsElevation.Sample sample=GpsElevation.read(TrackService.currentFix());
        Double altitude=sample.usableForHabitat?sample.mslMeters:null;
        int total=0,match=0,installed=0;
        try(FieldDb helper=new FieldDb(this)){
            SQLiteDatabase d=helper.getReadableDatabase();
            try(Cursor master=d.rawQuery(
                "SELECT COUNT(*) FROM taxon_catalog WHERE taxon_key LIKE 'LIKAS725:%'",null)){
                if(master.moveToFirst())installed=master.getInt(0);
            }
            if(installed!=714){
                speciesSummary.setText("Site matching unavailable · continue without ranking");
                return;
            }
            // The master attributes are installed privately, separately from the app APK.
            for(String group:groups){
                try(Cursor c=d.rawQuery("SELECT a.habitat_text,a.elevation_text "+
                    "FROM taxon_catalog t LEFT JOIN likas_master_attributes a "+
                    "ON a.taxon_key=t.taxon_key WHERE t.taxon_group=?",
                    new String[]{group})){
                    while(c.moveToNext()){
                        total++;
                        if(PaicHabitatPolicy.ranking(c.getString(0),c.getString(1),
                            confirmed,altitude)>0)match++;
                    }
                }
            }
        }catch(Exception e){
            speciesSummary.setText("Site matching unavailable · continue without ranking");
            return;
        }
        boolean ranked=checklistMode.getCheckedRadioButtonId()==3001;
        speciesSummary.setText(ranked?
            match+" / "+total+" references match habitat or elevation criteria":
            total+" references · no site sorting");
    }
    private void beginSurvey(){
        if(preparing)return;
        boolean deliberatelyMissing=getIntent().getBooleanExtra("start_without_gps",false);
        if(!deliberatelyMissing){
            Location live=TrackService.currentFix();
            if(live==null||!live.hasAccuracy()||
                    !"gps".equals(live.getProvider())||
                    !GpsFixPolicy.acceptableElapsed(
                    live.getLatitude(),live.getLongitude(),live.getAccuracy(),
                    (SystemClock.elapsedRealtimeNanos()-live.getElapsedRealtimeNanos())/1000000L,
                    live.isFromMockProvider())){
                new AlertDialog.Builder(this)
                    .setTitle("Fresh starting GPS needed")
                    .setMessage("The verified GPS fix from preflight has expired. "+
                        "Retry after GPS updates, or deliberately record a missing start location.")
                    .setNegativeButton("Retry GPS",null)
                    .setPositiveButton("Start with GPS missing",(dialog,which)->{
                        new AlertDialog.Builder(this)
                            .setTitle("Mark survey start GPS missing?")
                            .setMessage("No starting coordinates will be recorded. "+
                                "Later fixes cannot replace the original missing position.")
                            .setNegativeButton("Cancel",null)
                            .setPositiveButton("Confirm", (d,w)->launchSurvey(null)).show();
                    }).show();
                return;
            }
            launchSurvey(live);
        }else launchSurvey(null);
    }
    private void launchSurvey(Location valid){
        if(preparing)return;
        preparing=true;
        Intent launch=new Intent(this,MainActivity.class);
        if(getIntent().getExtras()!=null)launch.putExtras(getIntent().getExtras());
        if(valid!=null){
            launch.putExtra("start_without_gps",false);
            launch.putExtra("start_gps_latitude",valid.getLatitude());
            launch.putExtra("start_gps_longitude",valid.getLongitude());
            launch.putExtra("start_gps_accuracy_m",valid.getAccuracy());
            launch.putExtra("start_gps_recorded_at_ms",valid.getTime());
            launch.putExtra("start_gps_elapsed_nanos",valid.getElapsedRealtimeNanos());
        }else{
            launch.putExtra("start_without_gps",true);
            launch.removeExtra("start_gps_latitude");
            launch.removeExtra("start_gps_longitude");
            launch.removeExtra("start_gps_accuracy_m");
            launch.removeExtra("start_gps_recorded_at_ms");
            launch.removeExtra("start_gps_elapsed_nanos");
        }
        GpsElevation.Sample altitude=valid==null?null:GpsElevation.read(valid);
        if(altitude!=null&&altitude.usableForHabitat){
            launch.putExtra("prep_msl_elevation_m",altitude.mslMeters);
            launch.putExtra("prep_msl_uncertainty_m",altitude.verticalUncertaintyMeters);
        }
        launch.putExtra("prep_confirmed_habitats",PaicHabitatPolicy.encode(confirmed));
        launch.putExtra("prep_map_suggestions",
            PaicHabitatPolicy.encode(new HashSet<>(suggested)));
        launch.putExtra("prep_map_source",PaicLandCoverPack.installed(this)&&
            !suggested.isEmpty()?"ESA_WorldCover_2020_v100_500m_mode":"");
        launch.putExtra("prep_checklist_mode",
            checklistMode.getCheckedRadioButtonId()==3001?"suggested":"all");
        try{startActivity(launch);finish();}
        catch(RuntimeException failure){
            preparing=false;
            new AlertDialog.Builder(this).setMessage("Survey could not open: "+
                failure.getClass().getSimpleName()).setPositiveButton("OK",null).show();
        }
    }
    @Override protected void onSaveInstanceState(Bundle state){
        state.putStringArray("chosen",confirmed.toArray(new String[0]));
        state.putBoolean("others",othersVisible);
        state.putBoolean("ranked",checklistMode!=null&&
            checklistMode.getCheckedRadioButtonId()==3001);
        super.onSaveInstanceState(state);
    }
}
