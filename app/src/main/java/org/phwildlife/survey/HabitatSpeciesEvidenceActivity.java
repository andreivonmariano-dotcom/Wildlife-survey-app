package org.phwildlife.survey;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.app.AlertDialog;
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
    private ScrollView pageScroll;
    private int indexScrollY=0;
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
        boolean large=getResources().getConfiguration().fontScale>=1.6f;
        TextView back=PhLibrary2Ui.action(this,"Back",onBack,false);
        if(large){back.setTextSize(12);back.setPadding(dp(6),dp(5),dp(6),dp(5));}
        nav.addView(back,new LinearLayout.LayoutParams(dp(large?99:80),dp(48)));
        TextView label=PhLibrary2Ui.text(this,title,large?11:14,
            PhLibrary2Ui.FOREST,false,true);
        label.setGravity(Gravity.CENTER);
        nav.addView(label,new LinearLayout.LayoutParams(0,dp(48),1));
        root.addView(nav);
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);
        pageScroll=scroll;
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
        if(state!=null)indexScrollY=state.getInt("index_scroll",0);
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
        if(indexScrollY>0 && pageScroll!=null)
            pageScroll.post(()->pageScroll.scrollTo(0,indexScrollY));
        TextView heading=PhLibrary2Ui.text(this,"Mindanao landscapes",28,
            PhLibrary2Ui.INK,true,false);
        heading.setPadding(0,dp(12),0,dp(14));
        body.addView(heading);
        String[] categories={"Terrestrial","Edaphic","Aquatic","Coastal","Human-modified"};
        for(String group:categories){
            boolean first=true;
            for(JSONObject h:habitats){
                if(!group.equals(h.optString("group")))continue;
                if(first){
                    TextView label=PhLibrary2Ui.text(this,group,18,
                        PhLibrary2Ui.FOREST,true,true);
                    label.setPadding(0,dp(14),0,dp(8));body.addView(label);
                    first=false;
                }
                String id=h.optString("id"),name=h.optString("name");
                int count=countLinks(id);
                String subtitle=count==0?"":
                    count+(count==1?" source link":" source links");
                LinearLayout panel=PhLibrary2Ui.panel(this,name,subtitle,()->showHabitat(id));
                LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
                lp.bottomMargin=dp(8);body.addView(panel,lp);
            }
        }
    }
    private void line(LinearLayout body,String title,String value){
        if(value==null||value.trim().isEmpty())return;
        boolean initiallyOpen="Overview".equals(title);
        LinearLayout part=new LinearLayout(this);
        part.setOrientation(LinearLayout.VERTICAL);
        TextView toggle=PhLibrary2Ui.action(this,
            (initiallyOpen?"−  ":"+  ")+title,()->{},false);
        toggle.setGravity(Gravity.CENTER_VERTICAL);
        toggle.setTextSize(14);
        toggle.setContentDescription((initiallyOpen?"Collapse ":"Expand ")+title);
        part.addView(toggle);
        TextView paragraph=text(value,16,false);
        paragraph.setPadding(dp(10),dp(8),dp(10),dp(13));
        paragraph.setVisibility(initiallyOpen?View.VISIBLE:View.GONE);
        part.addView(paragraph);
        toggle.setOnClickListener(v->{
            boolean show=paragraph.getVisibility()!=View.VISIBLE;
            paragraph.setVisibility(show?View.VISIBLE:View.GONE);
            toggle.setText((show?"−  ":"+  ")+title);
            toggle.setContentDescription((show?"Collapse ":"Expand ")+title);
        });
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
        lp.bottomMargin=dp(7);body.addView(part,lp);
    }
    private void openSource(String sourceUrl){
        if(sourceUrl==null || !sourceUrl.startsWith("https://")){
            new AlertDialog.Builder(this).setMessage("No verified public source URL supplied.")
                .setPositiveButton("Close",null).show();return;
        }
        try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(sourceUrl)));}
        catch(Exception e){
            new AlertDialog.Builder(this).setMessage("A browser is unavailable on this device.")
                .setPositiveButton("Close",null).show();
        }
    }
    private void showHabitat(String id){
        JSONObject profile=null;
        for(JSONObject r:habitats)if(id.equals(r.optString("id"))){profile=r;break;}
        if(profile==null){showIndex();return;}
        if(currentHabitat==null&&pageScroll!=null)indexScrollY=pageScroll.getScrollY();
        currentHabitat=id;
        LinearLayout body=shell("HABITAT PROFILE",this::showIndex);
        TextView title=PhLibrary2Ui.text(this,profile.optString("name"),27,
            PhLibrary2Ui.INK,true,false);
        title.setPadding(0,dp(12),0,dp(6));body.addView(title);
        TextView group=PhLibrary2Ui.text(this,profile.optString("group"),14,
            PhLibrary2Ui.MUTED,false,true);
        group.setPadding(0,0,0,dp(12));body.addView(group);
        line(body,"Overview",profile.optString("overview"));
        line(body,"Field indicators",profile.optString("fieldIndicators"));
        line(body,"Water and substrate",profile.optString("waterAndSubstrate"));
        line(body,"Vegetation and wildlife",profile.optString("vegetationAndWildlife"));
        JSONArray measurements=profile.optJSONArray("measurements");
        if(measurements!=null){
            StringBuilder terms=new StringBuilder();
            for(int i=0;i<measurements.length();i++){
                if(i>0)terms.append(" · ");terms.append(measurements.optString(i));
            }
            line(body,"Field measurements",terms.toString());
        }
        line(body,"Interpretive limits",profile.optString("limitations"));
        TextView linkedTitle=PhLibrary2Ui.text(this,"Literature-linked species",18,
            PhLibrary2Ui.FOREST,true,true);
        linkedTitle.setPadding(0,dp(14),0,dp(8));
        body.addView(linkedTitle);
        int count=0;
        for(int i=0;i<links.length();i++){
            JSONObject edge=links.optJSONObject(i);
            if(edge==null||!id.equals(edge.optString("habitatId")))continue;
            count++;
            final String latin=edge.optString("scientificName");
            final String taxonKey=edge.optString("sourceTaxonKey");
            String evidence=edge.optString("evidenceText");
            String source=edge.optString("sourceTitle");
            String url=edge.optString("sourceUrl");
            LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(13),dp(12),dp(13),dp(12));
            card.setBackground(PhLibrary2Ui.bg(this,PhLibrary2Ui.IVORY,
                PhLibrary2Ui.LINE,13));
            TextView latinLabel=PhLibrary2Ui.text(this,latin,18,PhLibrary2Ui.FOREST,true,true);
            latinLabel.setOnClickListener(v->{
                Intent intent=new Intent(this,TaxonDetailActivity.class);
                if(PhLibraryBook01.has(this,latin))
                    intent.putExtra("phlibrary_scientific_name",latin);
                else intent.putExtra("taxon_key",taxonKey);
                startActivity(intent);
            });
            latinLabel.setClickable(true);latinLabel.setFocusable(true);
            latinLabel.setContentDescription("Read species "+latin);
            card.addView(latinLabel);
            TextView explanation=text(evidence,14,false);
            explanation.setPadding(0,dp(6),0,dp(8));card.addView(explanation);
            TextView sourceLabel=PhLibrary2Ui.text(this,"Source: "+source,12,
                PhLibrary2Ui.MUTED,false,false);
            sourceLabel.setMaxLines(3);card.addView(sourceLabel);
            TextView open=PhLibrary2Ui.action(this,"Open source",()->openSource(url),false);
            LinearLayout.LayoutParams op=new LinearLayout.LayoutParams(-1,dp(48));
            op.topMargin=dp(8);card.addView(open,op);
            LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2);
            cp.bottomMargin=dp(8);body.addView(card,cp);
        }
        if(count==0){
            TextView empty=text("No reviewed species links added yet.",14,false);
            empty.setTextColor(PhLibrary2Ui.MUTED);body.addView(empty);
        }
        if(count>0){
            TextView about=PhLibrary2Ui.action(this,"About these links",()->
                new AlertDialog.Builder(this).setTitle("Literature links")
                    .setMessage("These are provisional links to published sources. They do not establish presence, habitat selection or a detection at your survey site.")
                    .setPositiveButton("Close",null).show(),false);
            LinearLayout.LayoutParams aboutParams=new LinearLayout.LayoutParams(-1,dp(48));
            aboutParams.topMargin=dp(8);
            body.addView(about,aboutParams);
        }
        TextView find=PhLibrary2Ui.action(this,"Search account text",()->{
            startActivity(new Intent(this,PhLibraryDirectoryActivity.class).putExtra(
                "phlibrary_query",profileTextQuery(id)));
        },false);
        find.setContentDescription("Search habitat words in Book 01 account text, not confirmed occurrence");
        LinearLayout.LayoutParams findLp=new LinearLayout.LayoutParams(-1,dp(52));
        findLp.topMargin=dp(9);body.addView(find,findLp);
    }
    private String profileTextQuery(String id){
        switch(id){
            case "lowland-forest":return "lowland forest";
            case "montane-forest":return "montane forest";
            case "mossy-forest":return "mossy forest";
            case "riparian":return "riparian";
            case "mangrove":return "mangrove";
            case "limestone-karst":return "limestone";
            case "ultramafic-forest":return "ultramafic";
            case "freshwater-wetland":return "wetland";
            case "freshwater-swamp-forest":return "swamp forest";
            case "peat-swamp-forest":return "peat swamp";
            case "beach-forest":return "beach forest";
            case "disturbed-secondary":return "secondary forest";
            case "semi-evergreen-rainforest":return "semi-evergreen";
            case "moist-deciduous-forest":return "deciduous forest";
            case "tropical-subalpine":return "subalpine";
            default:return "";
        }
    }
    @Override protected void onSaveInstanceState(Bundle out){
        out.putString("habitat",currentHabitat);
        out.putInt("index_scroll",currentHabitat==null&&pageScroll!=null?
            pageScroll.getScrollY():indexScrollY);
        super.onSaveInstanceState(out);
    }
    @Override public void onBackPressed(){
        if(currentHabitat!=null)showIndex();else super.onBackPressed();
    }
}
