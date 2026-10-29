package org.phwildlife.survey;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/** Offline, source-first discovery cards. No priority or unverified rarity tiers. */
public final class LikasDiscoveryActivity extends Activity {
    private final Map<String,JSONObject> statusIndex=new HashMap<>();
    private final ArrayList<JSONObject> species=new ArrayList<>();
    private final ArrayList<JSONObject> habitats=new ArrayList<>();
    private LinearLayout content;
    private Button speciesTab,habitatTab;
    private boolean showingHabitat;
    private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
    private JSONObject load(String filename,int maxBytes)throws Exception {
        try(InputStream in=getAssets().open(filename);
            ByteArrayOutputStream bytes=new ByteArrayOutputStream()){
            byte[] buffer=new byte[8192];int n;
            while((n=in.read(buffer))!=-1){
                if(bytes.size()+n>maxBytes)throw new IllegalStateException("Reference pack exceeds limit");
                bytes.write(buffer,0,n);
            }
            return new JSONObject(bytes.toString("UTF-8"));
        }
    }
    private static GradientDrawable surface(Activity a,int fill,int outline,int radius){
        GradientDrawable d=new GradientDrawable();d.setColor(fill);
        d.setCornerRadius(Math.round(a.getResources().getDisplayMetrics().density*radius));
        d.setStroke(Math.max(1,Math.round(a.getResources().getDisplayMetrics().density)),outline);
        return d;
    }
    private TextView note(String s,int size,int color,boolean bold){
        TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);
        if(bold)t.setTypeface(null,Typeface.BOLD);
        return t;
    }
    private int categoryLevel(JSONObject data){
        if(data==null)return 0;
        String x=data.optString("code");
        if("CR".equals(x))return 3;
        if("EN".equals(x))return 2;
        if("VU".equals(x))return 1;
        return 0;
    }
    private int level(JSONObject status){
        return Math.max(categoryLevel(status.optJSONObject("iucn")),
            categoryLevel(status.optJSONObject("denr")));
    }
    private int edge(int severity){
        if(severity==3)return Color.rgb(149,70,58);
        if(severity==2)return Color.rgb(181,112,59);
        if(severity==1)return Color.rgb(170,139,74);
        return UiKit.BORDER;
    }
    private void appendLabel(LinearLayout body,String value,int foreground,int background){
        TextView badge=note(value,12,foreground,true);
        badge.setPadding(dp(9),dp(5),dp(9),dp(5));
        badge.setBackground(surface(this,background,background,10));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-2,-2);
        lp.topMargin=dp(5);lp.rightMargin=dp(6);
        body.addView(badge,lp);
    }
    private View speciesCard(JSONObject entry,JSONObject status) {
        String scientific=entry.optString("scientificName");
        String common=entry.optString("commonName");
        String label=common.isEmpty()?scientific:common;
        int severity=level(status),accent=edge(severity);
        LinearLayout card=new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(12),dp(12),dp(12),dp(12));
        card.setBackground(surface(this,Color.WHITE,accent,22));
        card.setElevation(dp(1));
        // No unsupported status displays as LC, DD or NE. Missing verification is unknown.
        FrameLayout media=new FrameLayout(this);
        media.setBackground(surface(this,Color.rgb(231,239,225),
            Color.rgb(231,239,225),15));
        ImageView photo=OfflinePhotoEvidence.thumbnailForTaxon(this,scientific);
        if(photo!=null){
            media.addView(photo,new FrameLayout.LayoutParams(-1,-1));
        }else{
            FrameLayout icon=new FrameLayout(this);
            icon.addView(new LikasPortalUi.Glyph(this,"species",UiKit.FOREST),
                new FrameLayout.LayoutParams(dp(70),dp(70),Gravity.CENTER));
            media.addView(icon,new FrameLayout.LayoutParams(-1,-1));
        }
        card.addView(media,new LinearLayout.LayoutParams(-1,dp(116)));
        TextView title=note(label,16,UiKit.INK,true);
        title.setMaxLines(2);
        LinearLayout.LayoutParams titleLp=new LinearLayout.LayoutParams(-1,-2);
        titleLp.topMargin=dp(10);card.addView(title,titleLp);
        TextView latin=note(scientific,12,UiKit.MUTED,false);
        latin.setTypeface(null,Typeface.ITALIC);latin.setMaxLines(2);
        card.addView(latin);
        if(status!=null){
            for(String authority:new String[]{"iucn","denr"}){
                JSONObject assessment=status.optJSONObject(authority);
                if(assessment==null)continue;
                String code=assessment.optString("code");
                int year=assessment.optInt("year",0);
                if(!code.matches("CR|EN|VU|NT|LC|DD|EX|EW"))continue;
                String labelText=("iucn".equals(authority)?"IUCN ":"DENR ")+code+
                    (year>0?" · "+year:"");
                appendLabel(card,labelText,severity>0?accent:UiKit.FOREST,
                    severity>0?Color.rgb(252,246,238):Color.rgb(241,245,237));
            }
        }
        TextView group=note(entry.optString("group"),11,UiKit.MUTED,false);
        LinearLayout.LayoutParams groupLp=new LinearLayout.LayoutParams(-1,-2);
        groupLp.topMargin=dp(8);card.addView(group,groupLp);
        card.setContentDescription(label+", "+scientific+
            (status!=null?", dated conservation classification available":
                ", conservation classification not independently reviewed in LIKAS")+
            ". Open species account.");
        card.setFocusable(true);card.setClickable(true);
        card.setOnClickListener(v->startActivity(new Intent(this,TaxonDetailActivity.class)
            .putExtra("taxon_key",entry.optString("sourceTaxonKey"))));
        return card;
    }
    private String habitatIcon(String group){
        if("Aquatic".equals(group))return "habitat";
        if("Coastal".equals(group))return "map";
        return "habitat";
    }
    private View habitatCard(JSONObject profile){
        String group=profile.optString("group");
        int fill="Aquatic".equals(group)?Color.rgb(224,239,236):
            "Coastal".equals(group)?Color.rgb(237,237,219):
            "Edaphic".equals(group)?Color.rgb(239,232,219):
            Color.rgb(230,239,226);
        LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14),dp(14),dp(14),dp(14));
        card.setBackground(surface(this,Color.WHITE,UiKit.BORDER,22));
        FrameLayout art=new FrameLayout(this);
        art.setBackground(surface(this,fill,fill,16));
        art.addView(new LikasPortalUi.Glyph(this,habitatIcon(group),UiKit.FOREST),
            new FrameLayout.LayoutParams(dp(66),dp(66),Gravity.CENTER));
        card.addView(art,new LinearLayout.LayoutParams(-1,dp(108)));
        TextView name=note(profile.optString("name"),16,UiKit.INK,true);name.setMaxLines(2);
        LinearLayout.LayoutParams n=new LinearLayout.LayoutParams(-1,-2);
        n.topMargin=dp(11);card.addView(name,n);
        TextView classification=note(group,12,UiKit.POND,true);
        LinearLayout.LayoutParams l=new LinearLayout.LayoutParams(-1,-2);
        l.topMargin=dp(5);card.addView(classification,l);
        TextView description=note(profile.optString("overview"),12,UiKit.MUTED,false);
        description.setMaxLines(3);
        description.setEllipsize(android.text.TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams o=new LinearLayout.LayoutParams(-1,-2);
        o.topMargin=dp(7);card.addView(description,o);
        card.setFocusable(true);card.setClickable(true);
        card.setContentDescription("Open habitat reference: "+profile.optString("name"));
        card.setOnClickListener(v->startActivity(new Intent(this,LikasHabitatProfileActivity.class)
            .putExtra("habitat_id",profile.optString("id"))));
        return card;
    }
    private void shelf(String title,ArrayList<JSONObject> data,boolean isHabitat){
        if(data.isEmpty())return;
        TextView h=note(title,19,UiKit.FOREST,true);
        LinearLayout.LayoutParams headerLp=new LinearLayout.LayoutParams(-1,-2);
        headerLp.topMargin=dp(17);headerLp.bottomMargin=dp(11);content.addView(h,headerLp);
        HorizontalScrollView scroll=new HorizontalScrollView(this);
        scroll.setHorizontalScrollBarEnabled(false);
        scroll.setFillViewport(false);scroll.setSmoothScrollingEnabled(true);
        LinearLayout row=new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(dp(2),dp(2),dp(16),dp(10));
        for(JSONObject item:data){
            JSONObject status=statusIndex.get(item.optString("scientificName"));
            View card=isHabitat?habitatCard(item):speciesCard(item,status);
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(dp(largeText()?226:212),-2);
            lp.rightMargin=dp(11);row.addView(card,lp);
        }
        scroll.addView(row,new HorizontalScrollView.LayoutParams(-2,-2));
        scroll.setContentDescription("Swipe horizontally through "+title+" cards");
        content.addView(scroll);
    }
    private boolean largeText(){
        return getResources().getConfiguration().fontScale>=1.60f;
    }
    private void action(String name,Runnable click){
        Button b=UiKit.button(this,name,"secondary");
        b.setOnClickListener(v->click.run());
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(largeText()?68:52));
        lp.topMargin=dp(16);content.addView(b,lp);
    }
    private void render(){
        content.removeAllViews();
        speciesTab.setEnabled(showingHabitat);
        habitatTab.setEnabled(!showingHabitat);
        speciesTab.setText(showingHabitat?"Species":"Species ●");
        habitatTab.setText(showingHabitat?"Habitats ●":"Habitats");
        if(showingHabitat){
            content.addView(LikasPortalUi.kicker(this,"ECOSYSTEMS"));
            content.addView(note("Habitats",27,UiKit.INK,true));
            TextView sub=note("Swipe the cards. Open a field guide.",13,UiKit.MUTED,false);
            sub.setPadding(0,dp(5),0,dp(6));content.addView(sub);
            for(String group:new String[]{"Terrestrial","Edaphic","Aquatic","Coastal","Human-modified"}){
                ArrayList<JSONObject> matches=new ArrayList<>();
                for(JSONObject p:habitats)if(group.equals(p.optString("group")))matches.add(p);
                shelf(group,matches,true);
            }
            action("All habitats",()->startActivity(new Intent(this,HabitatRecordsActivity.class)));
        }else{
            content.addView(LikasPortalUi.kicker(this,"WILDLIFE"));
            content.addView(note("Species",27,UiKit.INK,true));
            TextView sub=note("Dated IUCN and DENR records. Rarity unassessed.",13,UiKit.MUTED,false);
            sub.setPadding(0,dp(5),0,dp(6));content.addView(sub);
            ArrayList<JSONObject> conservation=new ArrayList<>();
            for(JSONObject s:species)if(statusIndex.containsKey(s.optString("scientificName")))conservation.add(s);
            conservation.sort((a,b)->Integer.compare(
                level(statusIndex.get(b.optString("scientificName"))),
                level(statusIndex.get(a.optString("scientificName")))));
            shelf("Conservation",conservation,false);
            for(String group:new String[]{"Birds","Amphibians","Reptiles","Mammals"}){
                ArrayList<JSONObject> groupAccounts=new ArrayList<>();
                for(JSONObject a:species)if(group.equals(a.optString("group")))groupAccounts.add(a);
                shelf(group,groupAccounts,false);
            }
            action("All species",()->startActivity(new Intent(this,Rc7SpeciesLibraryActivity.class)));
            TextView caveat=note("Unverified means LIKAS has not checked the status. "+
                "Threat level is not local rarity. Open source records for dated context.",
                12,UiKit.MUTED,false);
            caveat.setPadding(dp(2),dp(16),dp(2),dp(7));content.addView(caveat);
        }
        UiMotion.enter(this,content);
    }
    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);
        try{
            JSONObject index=load("rc34_conservation_card_status_v1.json",25000);
            JSONArray reviews=index.getJSONArray("items");
            for(int i=0;i<reviews.length();i++){
                JSONObject q=reviews.getJSONObject(i);
                statusIndex.put(q.getString("scientificName"),q);
            }
            JSONArray accounts=load("rc18_unified_96_priority_accounts_v1.json",520000)
                .getJSONArray("accounts");
            for(int i=0;i<accounts.length();i++)species.add(accounts.getJSONObject(i));
            JSONArray habitat=load("offline_habitat_profiles_v1.json",220000)
                .getJSONArray("profiles");
            for(int i=0;i<habitat.length();i++)habitats.add(habitat.getJSONObject(i));
        }catch(Exception e){
            new android.app.AlertDialog.Builder(this).setTitle("Reference unavailable")
                .setMessage("The offline card library could not be opened.")
                .setPositiveButton("Close",(d,w)->finish()).show();
            return;
        }
        showingHabitat="habitats".equals(getIntent().getStringExtra("view"));
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(15),dp(10),dp(15),dp(7));
        root.setBackgroundColor(UiKit.BG);
        root.addView(UiKit.navigationHeader(this,"Discover","Library",this::finish));
        LinearLayout tabs=new LinearLayout(this);tabs.setOrientation(LinearLayout.HORIZONTAL);
        speciesTab=UiKit.button(this,"Species","secondary");
        habitatTab=UiKit.button(this,"Habitats","secondary");
        tabs.addView(speciesTab,new LinearLayout.LayoutParams(0,dp(largeText()?68:52),1));
        LinearLayout.LayoutParams h=new LinearLayout.LayoutParams(0,dp(largeText()?68:52),1);
        h.leftMargin=dp(7);tabs.addView(habitatTab,h);
        speciesTab.setOnClickListener(v->{showingHabitat=false;render();});
        habitatTab.setOnClickListener(v->{showingHabitat=true;render();});
        root.addView(tabs);
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);
        content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(1),dp(9),dp(1),dp(20));
        scroll.addView(content);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        setContentView(root);render();
    }
}
