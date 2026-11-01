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
    private TextView mapSummary,speciesSummary,siteSummary;
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
        body.addView(FieldVisuals.section(this,"Site context",
            "GPS coordinates describe the survey starting point, not an entire transect."));
        siteSummary=label("",13,false);body.addView(siteSummary);
        refreshSite();
        body.addView(FieldVisuals.section(this,"Suggested habitats nearby",
            "ESA WorldCover 2020 · generalized land-cover map · observer confirmation required"));
        mapSummary=label("",13,false);body.addView(mapSummary);
        Button importMap=UiKit.button(this,PaicLandCoverPack.installed(this)?
            "Replace offline land-cover file":"Import offline land-cover file","secondary");
        importMap.setOnClickListener(v->pickMap());
        // Personal build loads the map before leaving the startup screen.
        if(BundledPrivateReferences.hasBundledMap(this)&&
                PaicLandCoverPack.installed(this))
            importMap.setVisibility(View.GONE);
        body.addView(importMap,new LinearLayout.LayoutParams(-1,dp(52)));
        TextView intro=label("Select every habitat actually encountered. Suggested "+
            "habitats are not preselected. Other habitats can always be added.",13,false);
        body.addView(intro);
        checks=new LinearLayout(this);
        checks.setOrientation(LinearLayout.VERTICAL);
        body.addView(checks);
        body.addView(FieldVisuals.section(this,"Species checklist",
            "Site-based ordering is advisory. Every species remains searchable."));
        checklistMode=new RadioGroup(this);
        checklistMode.setOrientation(RadioGroup.VERTICAL);
        RadioButton ranked=new RadioButton(this);
        ranked.setId(3001);ranked.setText("Suggested species first");
        ranked.setMinHeight(dp(52));ranked.setTextSize(15);
        ranked.setTextColor(UiKit.INK);
        checklistMode.addView(ranked);
        RadioButton full=new RadioButton(this);
        full.setId(3002);full.setText("Full checklist · no site ranking");
        full.setMinHeight(dp(52));full.setTextSize(15);
        full.setTextColor(UiKit.INK);
        checklistMode.addView(full);
        checklistMode.check(saved==null||saved.getBoolean("ranked",true)?3001:3002);
        checklistMode.setOnCheckedChangeListener((g,id)->refreshSpecies());
        body.addView(checklistMode);
        speciesSummary=label("",13,false);body.addView(speciesSummary);
        LinearLayout page=UiKit.page(this,"Habitats & Species",
            "Survey GPS",this::finish,body);
        LinearLayout footer=new LinearLayout(this);
        footer.setOrientation(LinearLayout.VERTICAL);
        footer.setPadding(0,dp(6),0,dp(4));
        Button begin=UiKit.button(this,"Begin survey","primary");
        begin.setOnClickListener(v->beginSurvey());
        footer.addView(begin,new LinearLayout.LayoutParams(-1,dp(56)));
        page.addView(footer);
        setContentView(page);
        updateMap();
        renderHabitats();
        refreshSpecies();
    }
    private void refreshSite(){
        boolean missing=getIntent().getBooleanExtra("start_without_gps",false);
        String coords=missing||!Double.isFinite(preflightLat)||!Double.isFinite(preflightLon)?
            "Survey starting GPS not recorded":
            String.format(Locale.US,"Starting GPS %.6f, %.6f · ±%.1f m",
                preflightLat,preflightLon,
                getIntent().getFloatExtra("start_gps_accuracy_m",Float.NaN));
        GpsElevation.Sample altitude=GpsElevation.read(TrackService.currentFix());
        String height=altitude.usableForHabitat?
            altitude.label():"MSL elevation not available for site screening";
        siteSummary.setText(coords+"\n"+height);
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
            mapSummary.setText("GPS not recorded. Map suggestions unavailable; select habitats manually.");
            return;
        }
        if(!PaicLandCoverPack.installed(this)){
            mapSummary.setText("No offline map file installed. Import the optional "+
                "WorldCover 2020 land-cover file, or select habitats manually.");
            return;
        }
        try{
            suggested=PaicLandCoverPack.suggest(this,preflightLat,preflightLon);
            mapSummary.setText(suggested.isEmpty()?
                "No reliably mapped habitat classes near this location. Choose manually.":
                "Nearby mapped classes around your GPS starting point (about 2.5 km across). "+
                "Map resolution is generalized to ~500 m; land cover may have changed since 2020.");
        }catch(Exception e){
            mapSummary.setText("Map cannot be read; select habitats manually.");
            suggested.clear();
        }
    }
    private void renderHabitats(){
        if(checks==null)return;
        checks.removeAllViews();
        if(!suggested.isEmpty()){
            for(String name:suggested)addCheck(name,true);
        }
        Button browse=UiKit.button(this,othersVisible?"Hide other habitats":
            "Browse all 16 habitats","secondary");
        browse.setOnClickListener(v->{othersVisible=!othersVisible;renderHabitats();});
        checks.addView(browse,new LinearLayout.LayoutParams(-1,dp(52)));
        for(String name:PaicHabitatPolicy.HABITATS)
            if(!suggested.contains(name)&&(othersVisible||confirmed.contains(name)))
                addCheck(name,false);
        if(suggested.isEmpty()&&!othersVisible){
            TextView noMap=label("Choose Browse all 16 habitats to make manual "+
                "selections. You can continue with no habitat recorded.",13,false);
            checks.addView(noMap);
        }
    }
    private void addCheck(String name,boolean fromMap){
        CheckBox check=new CheckBox(this);
        check.setText(name+(fromMap?" · mapped suggestion":""));
        check.setTextColor(UiKit.INK);check.setTextSize(15);
        check.setMinHeight(dp(52));check.setPadding(dp(6),dp(4),dp(6),dp(4));
        check.setChecked(confirmed.contains(name));
        check.setContentDescription(name+(fromMap?
            ". Suggested from historical satellite land cover, not field confirmed.":""));
        check.setOnCheckedChangeListener((b,selected)->{
            if(selected)confirmed.add(name);else confirmed.remove(name);
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
                speciesSummary.setText("Activate the private 714-species Mindanao PAIC "+
                    "catalog in Species Library to enable site-matched suggestions. "+
                    "You can continue without it.");
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
            speciesSummary.setText("Activate the 714-species private checklist in "+
                "Species Library before expecting site-matched suggestions.");
            return;
        }
        boolean ranked=checklistMode.getCheckedRadioButtonId()==3001;
        speciesSummary.setText(ranked?
            match+" of "+total+" "+profile.toLowerCase(Locale.US)+
                " references match at least one available habitat/elevation criterion. "+
                "Matching is not a presence prediction. All "+total+" remain available.":
            "Full "+profile.toLowerCase(Locale.US)+" reference list: "+total+
                " species. No species will be hidden by habitat or elevation.");
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
