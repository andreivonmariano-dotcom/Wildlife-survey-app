package org.phwildlife.survey;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;

/** Evidence comparison only. Never identifies a field observation or writes to SQLite. */
public final class Rc8TraitComparisonActivity extends Activity {
    private static final String ASSET="rc8_evidence_comparisons_v1.json";
    private static final String[] GROUPS={"Birds","Amphibians","Reptiles","Mammals"};
    private static final String[] SPECIES_CATEGORIES={
        "All evidence","Identification","Habitat & ecology","Range","Behavior & sound",
        "Conservation","Taxonomy","Evidence limits"};
    private static final String[] HABITAT_CATEGORIES={
        "All evidence","Field indicators","Substrate & water","Vegetation",
        "Measurements","Scientific cautions"};
    private JSONObject pack;
    private JSONArray species,habitats;
    private boolean habitatMode=false;
    private String requestedScientific="";
    private Spinner group,first,second,category;
    private LinearLayout results;
    private Button speciesMode,habitatModeButton;
    private final ArrayList<JSONObject> displayed=new ArrayList<>();
    private int dp(int size){return Math.round(size*getResources().getDisplayMetrics().density);}
    private TextView text(String value,int size,boolean heading){
        TextView v=new TextView(this);
        v.setText(value);v.setTextSize(size);
        v.setTextColor(heading?UiKit.FOREST:UiKit.INK);
        if(heading)v.setTypeface(null,Typeface.BOLD);
        v.setPadding(dp(4),dp(heading?9:3),dp(4),dp(5));
        v.setLineSpacing(dp(2),1.06f);v.setTextIsSelectable(true);
        return v;
    }
    private static JSONObject read(Activity activity)throws Exception{
        try(InputStream input=activity.getAssets().open(ASSET);
            ByteArrayOutputStream output=new ByteArrayOutputStream()){
            byte[] block=new byte[8192];int n;
            while((n=input.read(block))!=-1){
                if(output.size()+n>480000)throw new IllegalStateException("Trait evidence too large");
                output.write(block,0,n);
            }
            JSONObject result=new JSONObject(output.toString("UTF-8"));
            if(!"rc8-evidence-comparison-v1".equals(result.optString("schemaVersion")))
                throw new IllegalStateException("Incompatible trait comparison evidence");
            if(result.getJSONArray("species").length()!=96||
                    result.getJSONArray("habitats").length()!=15)
                throw new IllegalStateException("Incomplete comparison library");
            return result;
        }
    }
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        requestedScientific=getIntent().getStringExtra("scientific_name");
        habitatMode="habitat".equals(getIntent().getStringExtra("comparison_mode"));
        LinearLayout outer=new LinearLayout(this);
        outer.setOrientation(LinearLayout.VERTICAL);
        outer.setPadding(dp(14),dp(10),dp(14),dp(12));
        outer.setBackgroundColor(UiKit.BG);
        outer.addView(UiKit.navigationHeader(this,"Trait comparisons","Library",this::finish));
        try{
            pack=read(this);
            species=pack.getJSONArray("species");
            habitats=pack.getJSONArray("habitats");
        }catch(Exception e){
            outer.addView(text("Evidence comparison library unavailable offline. Recorded surveys remain unchanged.",15,false));
            setContentView(outer);return;
        }
        outer.addView(text("Compare cited biological statements, not automatically predicted traits. "+
            "Missing = not assessed, never absent. Site occurrence requires your own recorded evidence.",13,false));
        LinearLayout modes=new LinearLayout(this);
        modes.setOrientation(LinearLayout.HORIZONTAL);
        speciesMode=UiKit.button(this,"Species (96)","secondary");
        habitatModeButton=UiKit.button(this,"Habitats (15)","secondary");
        modes.addView(speciesMode,new LinearLayout.LayoutParams(0,dp(50),1));
        LinearLayout.LayoutParams mr=new LinearLayout.LayoutParams(0,dp(50),1);
        mr.leftMargin=dp(7);modes.addView(habitatModeButton,mr);
        outer.addView(modes);
        speciesMode.setOnClickListener(v->{habitatMode=false;resetMode();});
        habitatModeButton.setOnClickListener(v->{habitatMode=true;resetMode();});
        group=new Spinner(this);first=new Spinner(this);second=new Spinner(this);category=new Spinner(this);
        group.setContentDescription("Compare wildlife group or habitat category");
        first.setContentDescription("First reference");
        second.setContentDescription("Second reference");
        category.setContentDescription("Evidence category");
        outer.addView(text("Wildlife group or habitat type",12,true));
        outer.addView(group,new LinearLayout.LayoutParams(-1,dp(46)));
        outer.addView(text("Reference A",12,true));
        outer.addView(first,new LinearLayout.LayoutParams(-1,dp(48)));
        outer.addView(text("Reference B",12,true));
        outer.addView(second,new LinearLayout.LayoutParams(-1,dp(48)));
        outer.addView(text("Compare evidence category",12,true));
        outer.addView(category,new LinearLayout.LayoutParams(-1,dp(46)));
        ScrollView scroll=new ScrollView(this);
        results=new LinearLayout(this);results.setOrientation(LinearLayout.VERTICAL);
        results.setPadding(0,dp(7),0,dp(20));scroll.addView(results);
        outer.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        setContentView(outer);
        AdapterView.OnItemSelectedListener update=new AdapterView.OnItemSelectedListener(){
            @Override public void onItemSelected(AdapterView<?> p,View v,int index,long id){render();}
            @Override public void onNothingSelected(AdapterView<?> p){}
        };
        first.setOnItemSelectedListener(update);
        second.setOnItemSelectedListener(update);
        category.setOnItemSelectedListener(update);
        group.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override public void onItemSelected(AdapterView<?> p,View v,int index,long id){rebuildSelection();}
            @Override public void onNothingSelected(AdapterView<?> p){}
        });
        resetMode();
    }
    private void resetMode(){
        if(speciesMode==null||group==null)return;
        speciesMode.setText(habitatMode?"Species (96)":"Species (96)  ●");
        habitatModeButton.setText(habitatMode?"Habitats (15)  ●":"Habitats (15)");
        String[] groups=habitatMode?new String[]{
            "All habitats","Terrestrial","Edaphic","Coastal","Aquatic","Human-modified"}:GROUPS;
        String[] categories=habitatMode?HABITAT_CATEGORIES:SPECIES_CATEGORIES;
        group.setAdapter(new ArrayAdapter<String>(this,
            android.R.layout.simple_spinner_dropdown_item,groups));
        category.setAdapter(new ArrayAdapter<String>(this,
            android.R.layout.simple_spinner_dropdown_item,categories));
        if(!habitatMode&&requestedScientific!=null){
            for(int i=0;i<species.length();i++){
                JSONObject p=species.optJSONObject(i);
                if(p!=null&&requestedScientific.equalsIgnoreCase(p.optString("scientificName"))){
                    String target=p.optString("group");
                    for(int n=0;n<GROUPS.length;n++)if(GROUPS[n].equals(target)){
                        group.setSelection(n);break;
                    }
                    break;
                }
            }
        }
        rebuildSelection();
    }
    private static String label(JSONObject entry,boolean habitat){
        if(habitat)return entry.optString("name");
        String common=entry.optString("commonName");
        return (common.isEmpty()?"":common+" · ")+entry.optString("scientificName");
    }
    private void rebuildSelection(){
        if(first==null||second==null)return;
        displayed.clear();ArrayList<String> labels=new ArrayList<>();
        String selected=group.getSelectedItem()==null?"":group.getSelectedItem().toString();
        JSONArray candidates=habitatMode?habitats:species;
        for(int i=0;i<candidates.length();i++){
            JSONObject p=candidates.optJSONObject(i);
            if(p==null)continue;
            if(!habitatMode||"All habitats".equals(selected)||selected.equals(p.optString("group"))){
                if(habitatMode||selected.equals(p.optString("group"))){
                    displayed.add(p);labels.add(label(p,habitatMode));
                }
            }
        }
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,
            android.R.layout.simple_spinner_dropdown_item,labels);
        first.setAdapter(adapter);second.setAdapter(adapter);
        int selectedFirst=0;
        if(!habitatMode&&requestedScientific!=null){
            for(int n=0;n<displayed.size();n++)
                if(requestedScientific.equalsIgnoreCase(displayed.get(n).optString("scientificName"))){
                    selectedFirst=n;break;
                }
        }
        if(displayed.size()>0)first.setSelection(selectedFirst);
        if(displayed.size()>1)second.setSelection((selectedFirst+1)%displayed.size());
        render();
    }
    private static LinkedHashSet<String> categories(JSONArray traits){
        LinkedHashSet<String> all=new LinkedHashSet<>();
        for(int i=0;i<traits.length();i++){
            JSONObject t=traits.optJSONObject(i);
            if(t!=null)all.add(t.optString("category"));
        }
        return all;
    }
    private static ArrayList<JSONObject> claims(JSONObject p,String category){
        ArrayList<JSONObject> items=new ArrayList<>();
        JSONArray all=p.optJSONArray("traits");
        if(all==null)return items;
        for(int i=0;i<all.length();i++){
            JSONObject item=all.optJSONObject(i);
            if(item!=null&&category.equals(item.optString("category")))items.add(item);
        }
        return items;
    }
    private void addClaims(LinearLayout parent,JSONObject profile,String category){
        parent.addView(text(label(profile,habitatMode),14,true));
        if(!habitatMode){
            ImageView image=OfflinePhotoEvidence.thumbnailForTaxon(
                this,profile.optString("scientificName"));
            if(image!=null){
                LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(dp(125),dp(85));
                params.bottomMargin=dp(5);parent.addView(image,params);
            }
        }
        ArrayList<JSONObject> list=claims(profile,category);
        if(list.isEmpty()){
            TextView note=text("Not assessed in the installed source pack. Do not infer absence.",13,false);
            note.setTextColor(UiKit.MUTED);parent.addView(note);
            return;
        }
        int count=0;
        for(JSONObject item:list){
            if(count++>=4){parent.addView(text("More statements appear in the full library account.",12,false));break;}
            parent.addView(text(item.optString("label")+": "+item.optString("value"),14,false));
            String source=item.optString("source"),url=item.optString("url");
            TextView citation=text("Source: "+source+"\nReview: "+item.optString("review"),11,false);
            citation.setTextColor(UiKit.MUTED);parent.addView(citation);
            if(url.startsWith("https://")){
                Button open=UiKit.button(this,"Open cited source","quiet");
                parent.addView(open,new LinearLayout.LayoutParams(-1,dp(41)));
                open.setOnClickListener(v->{
                    try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url)));}
                    catch(Exception e){
                        new android.app.AlertDialog.Builder(this)
                            .setMessage("No application is available to open this source.")
                            .setPositiveButton("Close",null).show();
                    }
                });
            }
        }
    }
    private void render(){
        if(results==null||first==null||second==null||category==null)return;
        results.removeAllViews();
        int ai=first.getSelectedItemPosition(),bi=second.getSelectedItemPosition();
        if(ai<0||bi<0||ai>=displayed.size()||bi>=displayed.size()){
            results.addView(text("Choose two reference records.",14,false));return;
        }
        if(ai==bi){
            results.addView(text("Select two different records to compare.",14,false));return;
        }
        JSONObject a=displayed.get(ai),b=displayed.get(bi);
        String focus=category.getSelectedItem()==null?"All evidence":
            category.getSelectedItem().toString();
        JSONArray at=a.optJSONArray("traits"),bt=b.optJSONArray("traits");
        if(at==null||bt==null)return;
        LinkedHashSet<String> fields=new LinkedHashSet<>();
        if("All evidence".equals(focus)){
            fields.addAll(categories(at));fields.addAll(categories(bt));
        }else fields.add(focus);
        for(String field:fields){
            LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(11),dp(9),dp(11),dp(10));
            card.setBackground(UiKit.surface(this));
            card.addView(text(field,16,true));
            addClaims(card,a,field);
            TextView divider=text("Compared with",12,false);
            divider.setTextColor(UiKit.POND);card.addView(divider);
            addClaims(card,b,field);
            LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(-1,-2);
            params.bottomMargin=dp(12);results.addView(card,params);
        }
        results.addView(text(habitatMode?
            "Habitat comparisons describe published vegetation and substrate reference. "+
            "They never classify an unmeasured station or determine wildlife presence.":
            "The comparison highlights what cited sources say and what remains unassessed. "+
            "Different wording is not proof of a diagnostic difference. "+
            "For uncertain wildlife records, preserve the original identification and evidence.",
            12,false));
    }
}
