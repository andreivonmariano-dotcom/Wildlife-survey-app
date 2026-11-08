package org.phwildlife.survey;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

/** Source-linked ecology atlas. Provisional links never imply site occupancy. */
public final class HabitatSpeciesEvidenceActivity extends Activity {
    private final ArrayList<JSONObject> habitats=new ArrayList<>();
    private JSONArray links;
    private String currentHabitat=null;
    private int dp(float n){return PhLibrary2Ui.dp(this,n);}
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
    private TextView text(String value,int size,boolean serif){
        TextView t=PhLibrary2Ui.text(this,value,size,PhLibrary2Ui.INK,serif,false);
        t.setLineSpacing(dp(3),1.07f);t.setTextIsSelectable(true);
        return t;
    }
    private LinearLayout shell(String title,Runnable onBack){
        getWindow().setStatusBarColor(PhLibrary2Ui.PAPER);
        getWindow().setNavigationBarColor(PhLibrary2Ui.PAPER);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(PhLibrary2Ui.PAPER);
        root.setPadding(dp(15),dp(7),dp(15),0);
        LinearLayout nav=new LinearLayout(this);nav.setGravity(Gravity.CENTER_VERTICAL);
        TextView back=PhLibrary2Ui.action(this,"Back",onBack,false);
        nav.addView(back,new LinearLayout.LayoutParams(dp(80),dp(48)));
        TextView label=PhLibrary2Ui.text(this,title,14,PhLibrary2Ui.FOREST,false,true);
        label.setGravity(Gravity.CENTER);nav.addView(label,new LinearLayout.LayoutParams(0,dp(48),1));
        TextView count=PhLibrary2Ui.text(this,"15",13,PhLibrary2Ui.MUTED,false,true);
        nav.addView(count);root.addView(nav);
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);
        LinearLayout body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(1),dp(10),dp(1),dp(28));
        scroll.addView(body);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        setContentView(root);
        return body;
    }
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        try{
            JSONObject pack=read(this,"rc8_species_habitat_links_v1.json");
            if(!"rc8-species-habitat-links-v1".equals(pack.optString("schemaVersion")))
                throw new IllegalStateException("Unknown evidence schema");
            links=pack.getJSONArray("edges");
            JSONArray entries=read(this,"offline_habitat_profiles_v1.json").getJSONArray("profiles");
            for(int i=0;i<entries.length();i++){
                JSONObject r=entries.optJSONObject(i);if(r!=null)habitats.add(r);
            }
            if(habitats.size()!=15)throw new IllegalStateException("Unknown habitat source edition");
        }catch(Exception error){
            LinearLayout body=shell("HABITAT ATLAS",this::finish);
            body.addView(text("Habitat reference data are unavailable offline.",17,false));
            return;
        }
        String requested=state==null?getIntent().getStringExtra("habitat_id"):
            state.getString("habitat");
        if(requested!=null&&!requested.isEmpty())showHabitat(requested);
        else showIndex();
    }
    private int countLinks(String id){
        int count=0;
        for(int i=0;i<links.length();i++){
            JSONObject edge=links.optJSONObject(i);
            if(edge!=null&&id.equals(edge.optString("habitatId")))count++;
        }
        return count;
    }
    private void showIndex(){
        currentHabitat=null;
        LinearLayout body=shell("HABITAT ATLAS",this::finish);
        body.addView(PhLibrary2Ui.hero(this,"THE ECOLOGICAL ATLAS",
            "Habitats of the\nPhilippines",
            "15 source-informed landscape profiles with published species links.",false));
        TextView note=text("Profiles describe environments for field reference. Linked species " +
            "records are literature excerpts, not evidence that a species occurs at your site.",14,false);
        note.setTextColor(PhLibrary2Ui.MUTED);note.setPadding(dp(3),dp(15),dp(3),dp(8));body.addView(note);
        body.addView(PhLibrary2Ui.eyebrow(this,"EXPLORE LANDSCAPES"));
        for(JSONObject habitat:habitats){
            final String id=habitat.optString("id");
            String title=habitat.optString("name");
            String overview=habitat.optString("overview");
            String snippet=overview.length()>132?overview.substring(0,132).trim()+"…":overview;
            String description=habitat.optString("group")+" · "+countLinks(id)+
                " literature links\n"+snippet;
            LinearLayout card=PhLibrary2Ui.panel(this,title,description,()->showHabitat(id));
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
            lp.bottomMargin=dp(10);body.addView(card,lp);
        }
    }
    private void line(LinearLayout body,String title,String value){
        if(value==null||value.trim().isEmpty())return;
        TextView sub=PhLibrary2Ui.eyebrow(this,title);
        body.addView(sub);
        TextView paragraph=text(value,16,false);paragraph.setTextColor(PhLibrary2Ui.INK);
        body.addView(paragraph);
    }
    private void showHabitat(String id){
        JSONObject profile=null;
        for(JSONObject r:habitats)if(id.equals(r.optString("id"))){profile=r;break;}
        if(profile==null){showIndex();return;}
        currentHabitat=id;
        LinearLayout body=shell("HABITAT PROFILE",this::showIndex);
        body.addView(PhLibrary2Ui.hero(this,"HABITAT ATLAS  /  "+profile.optString("group"),
            profile.optString("name"),"Source-informed habitat description",true));
        line(body,"THE ENVIRONMENT",profile.optString("overview"));
        line(body,"FIELD INDICATORS",profile.optString("fieldIndicators"));
        line(body,"WATER AND SUBSTRATE",profile.optString("waterAndSubstrate"));
        line(body,"VEGETATION AND WILDLIFE",profile.optString("vegetationAndWildlife"));
        JSONArray measurements=profile.optJSONArray("measurements");
        if(measurements!=null){
            StringBuilder terms=new StringBuilder();
            for(int i=0;i<measurements.length();i++){
                if(i>0)terms.append(" · ");terms.append(measurements.optString(i));
            }
            line(body,"POSSIBLE FIELD MEASUREMENTS",terms.toString());
        }
        line(body,"INTERPRETIVE LIMITS",profile.optString("limitations"));
        body.addView(PhLibrary2Ui.eyebrow(this,"LITERATURE-LINKED SPECIES"));
        int count=0;
        for(int i=0;i<links.length();i++){
            JSONObject edge=links.optJSONObject(i);
            if(edge==null||!id.equals(edge.optString("habitatId")))continue;
            count++;
            final String latin=edge.optString("scientificName");
            final String taxonKey=edge.optString("sourceTaxonKey");
            LinearLayout card=PhLibrary2Ui.panel(this,latin,
                edge.optString("evidenceText")+"\nSource: "+edge.optString("sourceTitle"),
                ()->{
                    Intent intent=new Intent(this,TaxonDetailActivity.class);
                    if(PhLibraryBook01.has(this,latin))
                        intent.putExtra("phlibrary_scientific_name",latin);
                    else intent.putExtra("taxon_key",taxonKey);
                    startActivity(intent);
                });
            LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2);
            cp.bottomMargin=dp(8);body.addView(card,cp);
        }
        if(count==0){
            TextView empty=text("No source-reviewed species links have been recorded for this profile.",14,false);
            empty.setTextColor(PhLibrary2Ui.MUTED);body.addView(empty);
        }
        TextView provenance=text("These "+count+" associations are provisional literature crosswalks. "+
            "The source pack covers a reviewed subset, not all 715 Book 01 accounts.",13,false);
        provenance.setPadding(0,dp(14),0,dp(12));provenance.setTextColor(PhLibrary2Ui.MUTED);
        body.addView(provenance);
        TextView find=PhLibrary2Ui.action(this,"Search mentions in Book 01",()->{
            startActivity(new Intent(this,PhLibraryDirectoryActivity.class).putExtra(
                "phlibrary_query",profileTextQuery(id)));
        },false);
        body.addView(find);
    }
    private String profileTextQuery(String id){
        if("lowland-forest".equals(id))return "lowland forest";
        if("mossy-forest".equals(id))return "mossy forest";
        if("riparian".equals(id))return "stream";
        if("mangrove".equals(id))return "mangrove";
        if("limestone-karst".equals(id))return "limestone";
        if("freshwater-wetland".equals(id))return "wetland";
        for(JSONObject x:habitats)if(id.equals(x.optString("id")))
            return x.optString("name").split(" ")[0];
        return "";
    }
    @Override protected void onSaveInstanceState(Bundle out){
        out.putString("habitat",currentHabitat);super.onSaveInstanceState(out);
    }
    @Override public void onBackPressed(){
        if(currentHabitat!=null)showIndex();else super.onBackPressed();
    }
}
