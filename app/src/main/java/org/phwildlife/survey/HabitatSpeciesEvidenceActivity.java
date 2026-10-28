package org.phwildlife.survey;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

/** Offline habitat-to-species literature evidence, never a predicted occupancy map. */
public final class HabitatSpeciesEvidenceActivity extends Activity {
    private JSONArray links;
    private final ArrayList<JSONObject> habitats=new ArrayList<>();
    private Spinner selector;
    private LinearLayout results;
    private int dp(int p){return Math.round(p*getResources().getDisplayMetrics().density);}
    private static JSONObject read(Activity a,String file)throws Exception{
        try(InputStream in=a.getAssets().open(file);
            ByteArrayOutputStream out=new ByteArrayOutputStream()){
            byte[] buf=new byte[8192];int n;
            while((n=in.read(buf))!=-1){
                if(out.size()+n>250000)throw new IllegalStateException("Habitat pack too large");
                out.write(buf,0,n);
            }
            return new JSONObject(out.toString("UTF-8"));
        }
    }
    private TextView text(String v,int size){
        TextView t=new TextView(this);t.setText(v);t.setTextSize(size);
        t.setTextColor(UiKit.INK);t.setTextIsSelectable(true);
        t.setPadding(dp(4),dp(6),dp(4),dp(7));return t;
    }
    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(UiKit.BG);
        root.setPadding(dp(14),dp(12),dp(14),dp(12));
        root.addView(UiKit.navigationHeader(this,"Published habitat associations","Habitat",this::finish));
        root.addView(text("Published species–habitat context is not an occurrence prediction. "+
            "No links means unreviewed, not absent. Habitat classification and literature links "+
            "still require ecological specialist review.",13));
        try{
            JSONObject pack=read(this,"rc8_species_habitat_links_v1.json");
            if(!"rc8-species-habitat-links-v1".equals(pack.optString("schemaVersion")))
                throw new IllegalStateException("Unknown ecological evidence schema");
            links=pack.getJSONArray("edges");
            JSONArray entries=read(this,"offline_habitat_profiles_v1.json").getJSONArray("profiles");
            ArrayList<String> labels=new ArrayList<>();
            for(int i=0;i<entries.length();i++){
                JSONObject p=entries.optJSONObject(i);
                if(p!=null){habitats.add(p);labels.add(p.optString("name"));}
            }
            if(habitats.size()!=15)throw new IllegalStateException("Unexpected habitat source edition");
            selector=new Spinner(this);
            selector.setContentDescription("Select habitat evidence reference");
            selector.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item,labels));
            root.addView(selector,new LinearLayout.LayoutParams(-1,dp(50)));
        }catch(Exception e){
            root.addView(text("Cited habitat associations are unavailable offline.",15));
            setContentView(root);return;
        }
        ScrollView scroll=new ScrollView(this);
        results=new LinearLayout(this);results.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(results);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        setContentView(root);
        selector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override public void onItemSelected(AdapterView<?> a,View v,int index,long id){render();}
            @Override public void onNothingSelected(AdapterView<?> a){}
        });
        String requested=getIntent().getStringExtra("habitat_id");
        if(requested!=null)for(int i=0;i<habitats.size();i++){
            if(requested.equals(habitats.get(i).optString("id"))){selector.setSelection(i);break;}
        }
        render();
    }
    private void render(){
        if(selector==null||results==null)return;
        int index=selector.getSelectedItemPosition();
        if(index<0||index>=habitats.size())return;
        String id=habitats.get(index).optString("id");
        results.removeAllViews();
        results.addView(FieldVisuals.section(this,"Cited wildlife associations",
            "Reference context only · unrelated to the user's sampling sites"));
        int count=0;
        for(int i=0;i<links.length();i++){
            JSONObject edge=links.optJSONObject(i);
            if(edge==null||!id.equals(edge.optString("habitatId")))continue;
            count++;
            LinearLayout card=new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackground(UiKit.surface(this));
            card.setPadding(dp(12),dp(9),dp(12),dp(10));
            card.addView(text(edge.optString("scientificName"),16));
            card.addView(text(edge.optString("evidenceText"),14));
            card.addView(text("Published source: "+edge.optString("sourceTitle")+
                "\nReview: provisional habitat crosswalk; site presence not established",12));
            Button open=UiKit.button(this,"Open species source record","secondary");
            card.addView(open);
            String taxonKey=edge.optString("sourceTaxonKey");
            open.setOnClickListener(v->startActivity(new Intent(this,TaxonDetailActivity.class)
                .putExtra("taxon_key",taxonKey)));
            LinearLayout.LayoutParams box=new LinearLayout.LayoutParams(-1,-2);
            box.bottomMargin=dp(8);results.addView(card,box);
        }
        if(count==0)results.addView(text("No taxon-specific habitat association has been reviewed "+
            "for this reference type. This is not evidence that any species is absent.",14));
        results.addView(text(count+" published association entries for this habitat. "+
            "These are literature links, not local survey records or suitability scores.",12));
    }
}
