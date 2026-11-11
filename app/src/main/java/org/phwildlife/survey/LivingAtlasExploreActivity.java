package org.phwildlife.survey;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Living Atlas first visual slice. Read-only scientific discovery.
 * Taxonomic family links come from Book 01. Habitat text matches are
 * explicitly retrieval aids. Ecological edges require editorial approval.
 * No user survey database or online reference service is accessed here.
 */
public final class LivingAtlasExploreActivity extends Activity {
    public static final String EXTRA_SCIENTIFIC_NAME = "living_atlas_scientific_name";
    private static final String[] GROUPS =
        {"All", "Birds", "Amphibians", "Reptiles", "Mammals"};
    private LivingAtlasDataLayer.Snapshot atlas;
    private LinearLayout body;
    private ScrollView scroll;
    private String selectedScientific = "";
    private String filterGroup = "All";
    private String lastQuery = "";
    private LinearLayout searchMatches;

    private int dp(float value) { return PhLibrary2Ui.dp(this,value); }
    private TextView text(String value,float size,int color,boolean serif,boolean strong) {
        return PhLibrary2Ui.text(this,value,size,color,serif,strong);
    }
    private static String key(String value) {
        return value==null?"":value.trim().toLowerCase(Locale.ROOT);
    }
    private LinearLayout.LayoutParams space(int top,int bottom) {
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
        lp.topMargin=dp(top);lp.bottomMargin=dp(bottom);
        return lp;
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(PhLibrary2Ui.PAPER);
        getWindow().setNavigationBarColor(PhLibrary2Ui.PAPER);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|
            View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        try {
            atlas=LivingAtlasDataLayer.open(this);
        } catch (IllegalStateException error) {
            TextView unavailable=text("Living Atlas reference edition unavailable",
                18,PhLibrary2Ui.INK,false,true);
            unavailable.setPadding(dp(22),dp(32),dp(22),dp(32));
            setContentView(unavailable);
            return;
        }
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16),dp(5),dp(16),0);
        root.setBackgroundColor(PhLibrary2Ui.PAPER);
        LinearLayout rail=new LinearLayout(this);
        rail.setGravity(Gravity.CENTER_VERTICAL);
        boolean largeText=getResources().getConfiguration().fontScale>=1.6f;
        int headerHeight=dp(largeText?66:52);
        TextView back=PhLibrary2Ui.action(this,"Back",this::backToAtlas,false);
        if(largeText){
            back.setTextSize(12);
            back.setPadding(dp(10),dp(5),dp(10),dp(5));
        }
        back.setTag("living_atlas_back");
        rail.addView(back,new LinearLayout.LayoutParams(dp(largeText?92:75),headerHeight));
        TextView title=text("LIVING ATLAS",largeText?10.5f:13,
            PhLibrary2Ui.FOREST,false,true);
        title.setGravity(Gravity.CENTER);
        title.setLetterSpacing(largeText?.04f:.11f);
        title.setSingleLine(true);
        title.setContentDescription("Living Atlas");
        title.setTag("living_atlas_nav_title");
        rail.addView(title,new LinearLayout.LayoutParams(0,headerHeight,1));
        root.addView(rail,new LinearLayout.LayoutParams(-1,-2));
        scroll=new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setVerticalScrollBarEnabled(false);
        body=new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(0,dp(8),0,dp(35));
        scroll.addView(body,new ScrollView.LayoutParams(-1,-2));
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        setContentView(root);
        if(state!=null) {
            filterGroup=state.getString("living_atlas_group","All");
            lastQuery=state.getString("living_atlas_query","");
            selectedScientific=state.getString("living_atlas_selected","");
        } else {
            selectedScientific=getIntent().getStringExtra(EXTRA_SCIENTIFIC_NAME);
            if(selectedScientific==null)selectedScientific="";
        }
        if(!selectedScientific.isEmpty()) {
            showSpecies(selectedScientific);
        } else {
            renderExplore();
        }
    }

    private void section(String heading,String descriptor) {
        TextView title=text(heading,24,PhLibrary2Ui.INK,true,false);
        body.addView(title,space(18,descriptor.isEmpty()?8:1));
        if(!descriptor.isEmpty()) {
            TextView label=text(descriptor,13,PhLibrary2Ui.MUTED,false,false);
            label.setLineSpacing(dp(2),1.05f);
            body.addView(label,space(0,10));
        }
    }
    private void panel(String heading,String subtitle,Runnable open) {
        body.addView(PhLibrary2Ui.panel(this,heading,subtitle,open),space(4,5));
    }

    private void renderExplore() {
        selectedScientific="";
        body.removeAllViews();
        body.addView(PhLibrary2Ui.hero(this,"GREATER MINDANAO",
            "Explore the living atlas","715 species  ·  Four wildlife groups",true),
            space(2,15));
        TextView label=PhLibrary2Ui.eyebrow(this,"EXPLORE SPECIES");
        body.addView(label);
        EditText query=new EditText(this);
        query.setSingleLine(true);
        query.setTextSize(16);
        query.setHint("Search species or family");
        query.setContentDescription("Search the offline Living Atlas by species or family");
        query.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH);
        UiKit.fieldInput(this,query);
        body.addView(query,new LinearLayout.LayoutParams(-1,dp(55)));
        TextView groupLabel=PhLibrary2Ui.eyebrow(this,"WILDLIFE GROUP");
        body.addView(groupLabel,space(8,0));
        HorizontalScrollView chipsScroll=new HorizontalScrollView(this);
        chipsScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout chips=new LinearLayout(this);
        chips.setOrientation(LinearLayout.HORIZONTAL);
        chips.setPadding(0,dp(3),dp(2),dp(7));
        for(String group:GROUPS) {
            boolean active=group.equals(filterGroup);
            TextView chip=text(group,14,active?Color.WHITE:PhLibrary2Ui.FOREST,
                false,true);
            chip.setPadding(dp(15),dp(10),dp(15),dp(10));
            chip.setMinHeight(dp(45));
            chip.setGravity(Gravity.CENTER);
            chip.setBackground(PhLibrary2Ui.touch(this,
                active?PhLibrary2Ui.FOREST:PhLibrary2Ui.IVORY,
                active?Color.TRANSPARENT:PhLibrary2Ui.LINE,22));
            chip.setClickable(true);chip.setFocusable(true);
            chip.setContentDescription(group+" group"+(active?", selected":""));
            chip.setOnClickListener(v->{
                lastQuery=query.getText().toString();
                filterGroup=group;
                renderExplore();
            });
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-2,-2);
            lp.rightMargin=dp(7);
            chips.addView(chip,lp);
        }
        chipsScroll.addView(chips,new HorizontalScrollView.LayoutParams(-2,-2));
        body.addView(chipsScroll);
        TextView matchTitle=text("Species",24,PhLibrary2Ui.INK,true,false);
        body.addView(matchTitle,space(14,6));
        searchMatches=new LinearLayout(this);
        searchMatches.setOrientation(LinearLayout.VERTICAL);
        body.addView(searchMatches);
        query.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int start,int count,int after){}
            @Override public void onTextChanged(CharSequence s,int start,int before,int count){
                lastQuery=s.toString();renderMatches(lastQuery);
            }
            @Override public void afterTextChanged(Editable e){}
        });
        query.setText(lastQuery);
        query.setSelection(query.length());
        renderMatches(lastQuery);
        TextView edition=text("Book 01  ·  Edition 1.82  ·  Offline",
            12,PhLibrary2Ui.MUTED,false,false);
        body.addView(edition,space(12,0));
        scroll.post(()->scroll.scrollTo(0,0));
    }

    private void renderMatches(String value) {
        if(searchMatches==null)return;
        searchMatches.removeAllViews();
        String q=key(value);
        List<LivingAtlasDataLayer.Taxon> candidates=q.isEmpty()?
            atlas.taxa():atlas.search(q,150);
        int shown=0, total=0;
        for(LivingAtlasDataLayer.Taxon t:candidates) {
            if(!"All".equals(filterGroup)&&!filterGroup.equals(t.group))continue;
            total++;
            if(shown>=10)continue;
            String heading=t.commonName.isEmpty()?t.scientificName:t.commonName;
            String subtitle=t.scientificName+"  ·  "+t.family;
            searchMatches.addView(PhLibrary2Ui.panel(this,heading,subtitle,
                ()->showSpecies(t.scientificName)),space(3,5));
            shown++;
        }
        if(total==0) {
            TextView empty=text("No matching species in Book 01",
                15,PhLibrary2Ui.MUTED,false,false);
            searchMatches.addView(empty,space(6,12));
        }
        if(total>shown) {
            TextView hint=text(q.isEmpty()?
                    "Search to explore additional species":
                    "Refine your search to see more matches",
                13,PhLibrary2Ui.MUTED,false,false);
            searchMatches.addView(hint,space(2,4));
        }
    }

    private JSONObject sourceAccount(String scientific) {
        for(JSONObject r:PhLibraryBook01.entries(this))
            if(scientific.equalsIgnoreCase(r.optString("s")))return r;
        return null;
    }

    /** Package-visible so instrumentation can exercise real screen transitions. */
    void showSpecies(String scientific) {
        LivingAtlasDataLayer.Taxon selected=atlas.findScientificName(scientific);
        if(selected==null) {
            selectedScientific="";
            renderExplore();
            return;
        }
        selectedScientific=selected.scientificName;
        body.removeAllViews();
        String common=selected.commonName.isEmpty()?selected.scientificName:
            selected.commonName;
        body.addView(PhLibrary2Ui.hero(this,"SPECIES · "+selected.group.toUpperCase(Locale.ROOT),
            common,selected.scientificName,false),space(0,12));

        FrameLayout image=new FrameLayout(this);
        image.setBackground(PhLibrary2Ui.bg(this,PhLibrary2Ui.MOSS,
            Color.TRANSPARENT,18));
        image.setClipToOutline(true);
        View imageContent=PhLibrarySpeciesMedia.photoOrPlaceholder(this,
            selected.scientificName);
        image.addView(imageContent,new FrameLayout.LayoutParams(-1,-1));
        body.addView(image,new LinearLayout.LayoutParams(-1,dp(178)));

        TextView family=text("FAMILY  "+selected.family+"    ·    ORDER  "+
            selected.order,12,PhLibrary2Ui.FOREST,false,true);
        family.setLineSpacing(dp(3),1.05f);
        body.addView(family,space(12,2));

        section("Explore this species","Curated Book 01 reference");
        panel("Read species account","Natural history and source citations",
            ()->startActivity(new Intent(this,TaxonDetailActivity.class)
                .putExtra("phlibrary_scientific_name",selected.scientificName)));

        section("Taxonomic connections","Other species in "+selected.family);
        int related=0;
        for(LivingAtlasDataLayer.Taxon t:atlas.taxa()) {
            if(related>=3)break;
            if(t.scientificName.equalsIgnoreCase(selected.scientificName)||
                    !t.family.equals(selected.family))continue;
            panel(t.commonName.isEmpty()?t.scientificName:t.commonName,
                t.scientificName,()->showSpecies(t.scientificName));
            related++;
        }
        if(related==0) {
            TextView lone=text("One account in this family",
                14,PhLibrary2Ui.MUTED,false,false);
            body.addView(lone,space(2,9));
        }

        section("Habitat context","Source-text mentions");
        JSONObject account=sourceAccount(selected.scientificName);
        int mentionCount=0;
        if(account!=null) {
            for(int i=1;i<PhLibraryHabitatFilter.IDS.length;i++) {
                if(!PhLibraryHabitatFilter.mentions(account,i))continue;
                if(mentionCount>=3)break;
                TextView mention=text(PhLibraryHabitatFilter.NAMES[i],
                    15,PhLibrary2Ui.INK,false,false);
                mention.setPadding(dp(14),dp(11),dp(14),dp(11));
                mention.setBackground(PhLibrary2Ui.bg(this,PhLibrary2Ui.IVORY,
                    PhLibrary2Ui.LINE,12));
                body.addView(mention,space(2,6));
                mentionCount++;
            }
        }
        if(mentionCount==0) {
            TextView empty=text("Habitat terms: unindexed",
                14,PhLibrary2Ui.MUTED,false,false);
            body.addView(empty,space(2,9));
        }
        List<LivingAtlasDataLayer.HabitatEdge> approved=
            atlas.approvedHabitatEdges(selected.scientificName);
        TextView evidence=text("Reviewed habitat links: "+approved.size(),
            13,PhLibrary2Ui.MUTED,false,true);
        body.addView(evidence,space(7,6));
        panel("Explore habitat guides","15 offline habitat reference profiles",
            ()->startActivity(new Intent(this,HabitatSpeciesEvidenceActivity.class)));

        section("Research sources","Edition 1.82 bibliography");
        JSONArray citations=account==null?null:account.optJSONArray("r");
        int count=citations==null?0:citations.length();
        TextView citationCount=text("Account citations: "+count,
            14,PhLibrary2Ui.MUTED,false,false);
        body.addView(citationCount,space(0,7));
        panel("Browse reference library",
            atlas.bibliographyCount+" bibliography entries",
            ()->startActivity(new Intent(this,PhLibraryBibliographyActivity.class)));

        TextView source=text("Greater Mindanao  ·  Book 01 v1.82",
            12,PhLibrary2Ui.MUTED,false,false);
        body.addView(source,space(15,0));
        scroll.post(()->scroll.scrollTo(0,0));
    }

    private void backToAtlas() {
        if(!selectedScientific.isEmpty())renderExplore();
        else finish();
    }

    @Override public void onBackPressed() {
        backToAtlas();
    }

    @Override protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putString("living_atlas_group",filterGroup);
        out.putString("living_atlas_query",lastQuery);
        out.putString("living_atlas_selected",selectedScientific);
    }
}
