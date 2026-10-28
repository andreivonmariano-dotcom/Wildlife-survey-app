package org.phwildlife.survey;

import android.app.Activity;
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
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Photo reference viewer: reads approved offline images, not new observation
 * identifications. No fabricated pixel coordinates or automated anatomy labels.
 */
public final class Rc8PhotoComparisonActivity extends Activity {
    private final LinkedHashMap<String,ArrayList<JSONObject>> photoByTaxon=new LinkedHashMap<>();
    private final LinkedHashMap<String,JSONObject> claimsByTaxon=new LinkedHashMap<>();
    private final ArrayList<String> taxa=new ArrayList<>();
    private Spinner taxonA,taxonB,photoA,photoB;
    private LinearLayout panels;
    private TextView help;
    private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
    private static JSONObject read(Activity a,String asset)throws Exception{
        try(InputStream in=a.getAssets().open(asset);
            ByteArrayOutputStream out=new ByteArrayOutputStream()){
            byte[] chunk=new byte[8192];int n;
            while((n=in.read(chunk))!=-1){
                if(out.size()+n>480000)throw new IllegalStateException("Reference asset too large");
                out.write(chunk,0,n);
            }
            return new JSONObject(out.toString("UTF-8"));
        }
    }
    private TextView text(String value,int size){
        TextView t=new TextView(this);t.setText(value);t.setTextSize(size);
        t.setTextColor(UiKit.INK);t.setPadding(dp(4),dp(5),dp(4),dp(6));
        t.setTextIsSelectable(true);return t;
    }
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(UiKit.BG);
        root.setPadding(dp(12),dp(9),dp(12),dp(10));
        root.addView(UiKit.navigationHeader(this,"Offline photo comparisons","Species",this::finish));
        root.addView(text("Choose two photographs, including separate views of one species. "+
            "Photography is contextual reference, not a diagnosis or a LIKAS detection. "+
            "Visible-character notes come from published sources, not automated image recognition.",13));
        try{
            JSONArray photos=read(this,"offline_inaturalist_manifest_v1.json").getJSONArray("items");
            JSONArray species=read(this,"rc8_evidence_comparisons_v1.json").getJSONArray("species");
            for(int i=0;i<species.length();i++){
                JSONObject p=species.optJSONObject(i);
                if(p!=null)claimsByTaxon.put(p.optString("scientificName"),p);
            }
            for(int i=0;i<photos.length();i++){
                JSONObject p=photos.optJSONObject(i);
                if(p==null)continue;
                String scientific=p.optString("taxon");
                if(!claimsByTaxon.containsKey(scientific))continue;
                String path=p.optString("offlineAsset");
                if(!path.matches("offline_photos/inat_[0-9]+\\.jpg"))continue;
                try(InputStream in=getAssets().open(path)){
                    if(in.read()==-1)continue;
                }
                ArrayList<JSONObject> list=photoByTaxon.get(scientific);
                if(list==null){list=new ArrayList<>();photoByTaxon.put(scientific,list);}
                list.add(p);
            }
            taxa.addAll(photoByTaxon.keySet());
            if(taxa.size()<2)throw new IllegalStateException("Not enough reviewed photographs");
        }catch(Exception e){
            root.addView(text("Offline source photographs are not available for comparisons.",14));
            setContentView(root);return;
        }
        ScrollView scroll=new ScrollView(this);
        LinearLayout body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(body);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        taxonA=new Spinner(this);taxonB=new Spinner(this);
        photoA=new Spinner(this);photoB=new Spinner(this);
        taxonA.setContentDescription("Reference A species");
        taxonB.setContentDescription("Reference B species");
        photoA.setContentDescription("Reference A individual photograph");
        photoB.setContentDescription("Reference B individual photograph");
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,
            android.R.layout.simple_spinner_dropdown_item,taxa);
        taxonA.setAdapter(adapter);taxonB.setAdapter(adapter);
        body.addView(text("Photographic reference A",15));body.addView(taxonA);
        body.addView(text("Select photo A",12));body.addView(photoA);
        body.addView(text("Photographic reference B",15));body.addView(taxonB);
        body.addView(text("Select photo B",12));body.addView(photoB);
        Button same=UiKit.button(this,"Compare another photograph of species A","quiet");
        body.addView(same,new LinearLayout.LayoutParams(-1,dp(45)));
        same.setOnClickListener(v->{taxonB.setSelection(taxonA.getSelectedItemPosition());
            if(photoB.getAdapter()!=null&&photoB.getAdapter().getCount()>1)photoB.setSelection(1);});
        panels=new LinearLayout(this);
        float width=getResources().getConfiguration().screenWidthDp;
        panels.setOrientation(width>=600?LinearLayout.HORIZONTAL:LinearLayout.VERTICAL);
        body.addView(panels);
        help=text("Published field characters appear below each photo. "+
            "No anatomical regions have been drawn over the photographs: a reviewer must "+
            "approve species-specific photo coordinates before such marks can be trusted.",12);
        body.addView(help);
        setContentView(root);
        AdapterView.OnItemSelectedListener update=new AdapterView.OnItemSelectedListener(){
            @Override public void onItemSelected(AdapterView<?> p,View v,int i,long id){render();}
            @Override public void onNothingSelected(AdapterView<?> p){}
        };
        photoA.setOnItemSelectedListener(update);photoB.setOnItemSelectedListener(update);
        taxonA.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override public void onItemSelected(AdapterView<?> p,View v,int i,long id){photosFor(taxonA,photoA);}
            @Override public void onNothingSelected(AdapterView<?> p){}
        });
        taxonB.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override public void onItemSelected(AdapterView<?> p,View v,int i,long id){photosFor(taxonB,photoB);}
            @Override public void onNothingSelected(AdapterView<?> p){}
        });
        taxonB.setSelection(1);
        String requested=getIntent().getStringExtra("scientific_name");
        if(requested!=null&&taxa.contains(requested))taxonA.setSelection(taxa.indexOf(requested));
        photosFor(taxonA,photoA);photosFor(taxonB,photoB);
        render();
    }
    private void photosFor(Spinner taxon,Spinner photos){
        if(taxon.getSelectedItem()==null)return;
        String key=taxon.getSelectedItem().toString();
        ArrayList<JSONObject> available=photoByTaxon.get(key);
        if(available==null)return;
        ArrayList<String> labels=new ArrayList<>();
        for(JSONObject p:available){
            labels.add("Photo #"+p.optString("photoId")+" · "+p.optString("observer"));
        }
        photos.setAdapter(new ArrayAdapter<String>(this,
            android.R.layout.simple_spinner_dropdown_item,labels));
        render();
    }
    private void panel(LinearLayout parent,Spinner taxon,Spinner photo,String label){
        if(taxon.getSelectedItem()==null)return;
        String scientific=taxon.getSelectedItem().toString();
        ArrayList<JSONObject> available=photoByTaxon.get(scientific);
        int index=photo.getSelectedItemPosition();
        if(available==null||index<0||index>=available.size())return;
        JSONObject selected=available.get(index);
        LinearLayout column=new LinearLayout(this);column.setOrientation(LinearLayout.VERTICAL);
        column.setPadding(dp(5),dp(5),dp(5),dp(10));
        column.setBackground(UiKit.surface(this));
        column.addView(text(label+" · "+scientific,16));
        // Original licensed / owner-granted photo is displayed unchanged with source credits.
        OfflinePhotoEvidence.add(this,column,selected.optString("id"));
        Rc9ApprovedPhotoMarkers.add(this,column,selected);
        if("dreidrei".equalsIgnoreCase(selected.optString("observer"))&&
            "owner_direct_private_research_permission".equals(selected.optString("rightsBasis"))){
            Button editor=UiKit.button(this,"Draft anatomical photo annotation","quiet");
            column.addView(editor,new LinearLayout.LayoutParams(-1,dp(47)));
            editor.setOnClickListener(v->startActivity(
                new android.content.Intent(this,Rc9PhotoAnnotationReviewActivity.class)
                    .putExtra("scientific_name",scientific)
                    .putExtra("photo_id",selected.optInt("photoId"))));
        }
        column.addView(text("Published characters to inspect (not marked on image)",13));
        JSONObject species=claimsByTaxon.get(scientific);
        JSONArray traits=species==null?null:species.optJSONArray("traits");
        int count=0;
        if(traits!=null)for(int i=0;i<traits.length();i++){
            JSONObject trait=traits.optJSONObject(i);
            if(trait==null||!"Identification".equals(trait.optString("category")))continue;
            if(count++>=3)break;
            column.addView(text("• "+trait.optString("label")+": "+trait.optString("value"),13));
            column.addView(text("Citation: "+trait.optString("source")+
                "\nReview: "+trait.optString("review"),11));
        }
        if(count==0)column.addView(text("Not assessed: no cited visual identification "+
            "characters are assigned yet. Do not infer missing characters.",13));
        LinearLayout.LayoutParams box=panels.getOrientation()==LinearLayout.HORIZONTAL?
            new LinearLayout.LayoutParams(0,-2,1):
            new LinearLayout.LayoutParams(-1,-2);
        box.bottomMargin=dp(10);parent.addView(column,box);
    }
    private void render(){
        if(panels==null||photoA==null||photoB==null)return;
        panels.removeAllViews();
        panel(panels,taxonA,photoA,"A");panel(panels,taxonB,photoB,"B");
    }
}
