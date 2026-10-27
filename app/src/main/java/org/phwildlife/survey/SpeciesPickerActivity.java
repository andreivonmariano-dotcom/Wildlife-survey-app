package org.phwildlife.survey;

import android.app.Activity;
import android.app.AlertDialog;
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
    private LinearLayout cards;
    private TextView status;
    private int generation=0,offset=0;
    private boolean more=false;
    private String[] availableGroups;
    private String geographicRegion="PH",province="",habitat="";
    private String lastQuery="",lastGroup="";
    private boolean lastLocal=false;
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
        TextView guide=text("Offline checklist · select a name to return to your observation",12,false);
        guide.setTextColor(UiKit.MUTED);root.addView(guide);

        search=new EditText(this);
        search.setSingleLine(true);search.setTextSize(16);
        search.setHint("Search common, scientific or older name");
        LinearLayout.LayoutParams searchParams=new LinearLayout.LayoutParams(-1,dp(56));
        searchParams.topMargin=dp(10);root.addView(search,searchParams);

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
        for(int i=0;i<availableGroups.length;i++){
            if(availableGroups[i].equals(group)){groups.setSelection(i);break;}
        }

        Location fix=TrackService.currentFix();
        String gps="GPS position unavailable";
        if(fix!=null&&fix.hasAccuracy()){
            long age=(SystemClock.elapsedRealtimeNanos()-fix.getElapsedRealtimeNanos())/1000000L;
            if(age>=0&&age<=10000L)
                gps=String.format(Locale.US,"Recent GPS ±%.1f m",fix.getAccuracy());
        }
        String scope=island(geographicRegion);
        TextView context=text((scope.isEmpty()?"National checklist":scope+" checklist area")+
            (province.isEmpty()?"":" · "+province)+" · "+gps,12,false);
        context.setTextColor(UiKit.FOREST);root.addView(context);
        TextView model=text("Occurrence probability: "+
            OccurrenceEvidencePolicy.probabilityStatus()+
            "\nElevation: not modeled · Landcover: not modeled"+
            (habitat.trim().isEmpty()?"":"\nField habitat: "+habitat+" (not scored)"),12,false);
        model.setTextColor(UiKit.MUTED);
        model.setPadding(0,dp(6),0,dp(8));root.addView(model);
        Button unknown=UiKit.button(this,"Use unidentified identification","secondary");
        root.addView(unknown);
        unknown.setOnClickListener(v->{
            String[] unknowns=Taxa.unknownLabels(currentGroup());
            new AlertDialog.Builder(this).setTitle("Unidentified observation")
                .setItems(unknowns,(dialog,which)->select(unknowns[which],currentGroup()))
                .setNegativeButton("Back",null).show();
        });
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
    private static final class Species {
        String key,group,common,scientific,family,origin,iucn,ph,source,version;
        int evidence;
    }
    private List<Species> searchPage(String group,String term,boolean local,int first)throws Exception{
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
            "c.source,c.source_version,"+evidence+" AS evidence_rank "+
            "FROM taxon_catalog c WHERE c.taxon_group=? "+
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
            String search="%"+TaxonSearchPolicy.escapeLike(term.toLowerCase(Locale.ROOT))+"%";
            args.add(search);args.add(search);args.add(search);
        }
        if(local){
            sql.append(" AND (").append(provinceMatch).append(" OR ")
                .append(islandMatch).append(")");
            args.add(province);args.add(island);
        }
        sql.append(" ORDER BY evidence_rank DESC,"+
            "CASE WHEN c.taxon_key LIKE 'STARTER:%' THEN 1 ELSE 0 END,"+
            "c.common_name COLLATE NOCASE,c.scientific_name COLLATE NOCASE LIMIT ? OFFSET ?");
        args.add(String.valueOf(PAGE+1));args.add(String.valueOf(first));
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
                    item.evidence=c.getInt(10);list.add(item);
                }
            }
        }
        return list;
    }
    private void refresh(){
        if(search==null||cards==null)return;
        offset=0;generation++;
        cards.removeAllViews();
        more=false;
        lastQuery=search.getText().toString().trim();
        lastGroup=currentGroup();
        lastLocal=views.getSelectedItemPosition()==1;
        appendPage();
    }
    private void appendPage(){
        final int ticket=generation,first=offset;
        final String term=lastQuery,group=lastGroup;
        final boolean onlyLocal=lastLocal;
        status.setText("Searching "+group+"…");
        worker.execute(()->{
            try{
                List<Species> result=searchPage(group,term,onlyLocal,first);
                ui.post(()->{
                    if(isFinishing()||isDestroyed()||ticket!=generation)return;
                    int shown=Math.min(result.size(),PAGE);
                    for(int i=0;i<shown;i++)addSpecies(result.get(i));
                    offset=first+shown;
                    more=result.size()>PAGE;
                    if(more){
                        Button next=UiKit.button(this,"Load more species","secondary");
                        cards.addView(next);
                        next.setOnClickListener(v->{cards.removeView(next);appendPage();});
                    }
                    status.setText(offset+" names shown"+(more?" · more available":"")+
                        (onlyLocal?" · documented area records":" · national catalog"));
                    if(offset==0)
                        cards.addView(text(onlyLocal?
                            "No documented records for this area in the installed sources. "+
                            "Switch to All species to record any identification.":
                            "No catalog match. Use an unidentified or verbatim entry.",14,false));
                });
            }catch(Exception error){
                ui.post(()->{if(!isFinishing()&&!isDestroyed()&&ticket==generation)
                    status.setText("Catalog search error: "+error.getClass().getSimpleName());});
            }
        });
    }
    private void addSpecies(Species item){
        LinearLayout card=new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(13),dp(11),dp(13),dp(10));
        card.setBackground(UiKit.surface(this));
        LinearLayout.LayoutParams cardLp=new LinearLayout.LayoutParams(-1,-2);
        cardLp.bottomMargin=dp(8);cards.addView(card,cardLp);
        String name=item.common.isEmpty()?item.scientific:item.common;
        TextView heading=text(name,17,true);card.addView(heading);
        if(!name.equals(item.scientific)){
            TextView scientific=text(item.scientific,14,false);
            scientific.setTypeface(null,Typeface.ITALIC);card.addView(scientific);
        }
        StringBuilder identifiers=new StringBuilder(item.family);
        if(!item.origin.isEmpty())identifiers.append(" · ").append(item.origin);
        if(!item.iucn.isEmpty())identifiers.append(" · IUCN ").append(item.iucn);
        if(!item.ph.isEmpty())identifiers.append(" · PH ").append(item.ph);
        if(identifiers.length()>0){
            TextView meta=text(identifiers.toString(),12,false);
            meta.setTextColor(UiKit.MUTED);card.addView(meta);
        }
        TextView range=text(OccurrenceEvidencePolicy.title(item.evidence),13,true);
        range.setTextColor(item.evidence==0?UiKit.MUTED:UiKit.FOREST);
        range.setPadding(0,dp(7),0,dp(3));card.addView(range);
        TextView citation=text(item.source+" · "+item.version,11,false);
        citation.setTextColor(UiKit.MUTED);citation.setMaxLines(2);
        card.addView(citation);
        LinearLayout buttons=new LinearLayout(this);
        Button details=UiKit.button(this,"Details","secondary");
        buttons.addView(details,new LinearLayout.LayoutParams(0,dp(48),1f));
        Button select=UiKit.button(this,"Select species","primary");
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
    @Override protected void onDestroy(){
        generation++;
        ui.removeCallbacksAndMessages(null);
        worker.shutdownNow();
        super.onDestroy();
    }
}
