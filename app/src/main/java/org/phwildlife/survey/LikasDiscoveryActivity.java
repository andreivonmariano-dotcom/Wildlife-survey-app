package org.phwildlife.survey;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.InputType;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Locale;

/** Offline, source-first discovery cards. No priority or unverified rarity tiers. */
public final class LikasDiscoveryActivity extends Activity {
    private final Map<String,JSONObject> statusIndex=new HashMap<>();
    private final ArrayList<JSONObject> species=new ArrayList<>();
    private final ArrayList<JSONObject> habitats=new ArrayList<>();
    private final Map<String,Integer> railOffsets=new HashMap<>();
    private LinearLayout content;
    private ScrollView pageScroll;
    private int speciesFilterX=0,habitatFilterX=0;
    private Button speciesTab,habitatTab;
    private EditText searchInput;
    private Button clearSearch;
    private final Handler searchHandler=new Handler(Looper.getMainLooper());
    private final Runnable searchRefresh=()->{
        railOffsets.clear();
        if(pageScroll!=null)pageScroll.scrollTo(0,0);
        render(false);
    };
    private String searchQuery="";
    private String speciesGroup="All",habitatGroup="All";
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
        // Most catalog species have no independently reviewed dated assessment yet.
        // A missing status must render a neutral card, not crash or imply LC/DD.
        if(status==null)return 0;
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
    private String wildlifeGlyph(String group){
        if("Birds".equals(group))return "bird";
        if("Amphibians".equals(group))return "frog";
        if("Reptiles".equals(group))return "snake";
        if("Mammals".equals(group))return "mammal";
        return "species";
    }
    private View speciesCard(JSONObject entry,JSONObject status) {
        String scientific=entry.optString("scientificName");
        String common=entry.optString("commonName");
        String label=common.isEmpty()?scientific:common;
        int severity=level(status),accent=edge(severity);
        LinearLayout card=new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(12),dp(12),dp(12),dp(12));
        // Quiet, category-specific frames use only a reviewed dated assessment.
        int bottom=severity==3?Color.rgb(253,244,241):
            severity==2?Color.rgb(252,246,238):
            severity==1?Color.rgb(250,247,237):Color.rgb(246,250,245);
        GradientDrawable frame=new GradientDrawable(
            GradientDrawable.Orientation.TL_BR,new int[]{Color.WHITE,bottom});
        frame.setCornerRadius(dp(22));
        frame.setStroke(dp(severity>0?2:1),accent);
        card.setBackground(frame);
        card.setElevation(dp(2));
        card.setMinimumHeight(dp(largeText()?336:280));
        if(severity>0){
            View accentLine=new View(this);
            accentLine.setBackground(surface(this,accent,accent,4));
            LinearLayout.LayoutParams accentLp=new LinearLayout.LayoutParams(-1,dp(4));
            accentLp.bottomMargin=dp(8);card.addView(accentLine,accentLp);
        }
        // No unsupported status displays as LC, DD or NE. Missing verification is unknown.
        FrameLayout media=new FrameLayout(this);
        GradientDrawable mediaTint=new GradientDrawable(
            GradientDrawable.Orientation.TL_BR,new int[]{
                Color.rgb(223,237,219),Color.rgb(240,246,236)});
        mediaTint.setCornerRadius(dp(15));
        media.setBackground(mediaTint);
        media.setClipToOutline(true);
        media.addView(new LikasPortalUi.Glyph(this,
            wildlifeGlyph(entry.optString("group")),UiKit.FOREST),
            new FrameLayout.LayoutParams(dp(70),dp(70),Gravity.CENTER));
        ImageView photo=OfflinePhotoEvidence.galleryPhotoForTaxon(this,scientific);
        media.addView(photo,new FrameLayout.LayoutParams(-1,-1));
        card.addView(media,new LinearLayout.LayoutParams(-1,dp(152)));
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
        if(status==null){
            TextView missing=note("Status unverified",11,UiKit.MUTED,false);
            LinearLayout.LayoutParams missingLp=new LinearLayout.LayoutParams(-1,-2);
            missingLp.topMargin=dp(5);card.addView(missing,missingLp);
        }
        TextView group=note(entry.optString("group"),11,UiKit.MUTED,false);
        LinearLayout.LayoutParams groupLp=new LinearLayout.LayoutParams(-1,-2);
        groupLp.topMargin=dp(8);card.addView(group,groupLp);
        card.setContentDescription(label+", "+scientific+
            (status!=null?", dated conservation classification available":
                ", conservation classification not independently reviewed in LIKAS")+
            ". Open species account.");
        TextView open=note("OPEN FIELD GUIDE   ›",11,UiKit.POND,true);
        open.setLetterSpacing(.07f);
        LinearLayout.LayoutParams openLp=new LinearLayout.LayoutParams(-1,-2);
        openLp.topMargin=dp(10);card.addView(open,openLp);
        card.setFocusable(true);card.setClickable(true);
        card.setOnClickListener(v->startActivity(new Intent(this,TaxonDetailActivity.class)
            .putExtra("taxon_key",entry.optString("sourceTaxonKey"))));
        return card;
    }
    private String habitatIcon(String group){
        if("Terrestrial".equals(group))return "forest";
        if("Edaphic".equals(group))return "karst";
        if("Aquatic".equals(group))return "wetland";
        if("Coastal".equals(group))return "coast";
        if("Human-modified".equals(group))return "fields";
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
        GradientDrawable habitatFrame=new GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            new int[]{Color.WHITE,Color.rgb(244,249,242)});
        habitatFrame.setCornerRadius(dp(22));
        habitatFrame.setStroke(dp(1),UiKit.BORDER);
        card.setBackground(habitatFrame);
        card.setElevation(dp(2));
        card.setMinimumHeight(dp(largeText()?355:296));
        FrameLayout art=new FrameLayout(this);
        art.setBackground(surface(this,fill,fill,16));
        art.setClipToOutline(true);
        art.addView(new LikasPortalUi.Glyph(this,habitatIcon(group),UiKit.FOREST),
            new FrameLayout.LayoutParams(dp(66),dp(66),Gravity.CENTER));
        ImageView habitatPhoto=OfflinePhotoEvidence.galleryPhotoForHabitat(
            this,profile.optString("photoId"));
        art.addView(habitatPhoto,new FrameLayout.LayoutParams(-1,-1));
        card.addView(art,new LinearLayout.LayoutParams(-1,dp(150)));
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
        TextView open=note("OPEN HABITAT GUIDE   ›",11,UiKit.POND,true);
        open.setLetterSpacing(.07f);
        LinearLayout.LayoutParams openLp=new LinearLayout.LayoutParams(-1,-2);
        openLp.topMargin=dp(11);card.addView(open,openLp);
        card.setFocusable(true);card.setClickable(true);
        card.setContentDescription("Open habitat reference: "+profile.optString("name"));
        card.setOnClickListener(v->startActivity(new Intent(this,LikasHabitatProfileActivity.class)
            .putExtra("habitat_id",profile.optString("id"))));
        return card;
    }
    private void moveCard(HorizontalScrollView scroll,int x){
        boolean lessMotion=getSharedPreferences("likas_ui",MODE_PRIVATE)
            .getBoolean("reduced_motion",false) ||
            !android.animation.ValueAnimator.areAnimatorsEnabled() || largeText();
        if(lessMotion)scroll.scrollTo(x,0);
        else scroll.smoothScrollTo(x,0);
    }
    /** Swipe rail with explicit progress, large tap targets and one-card step controls. */
    private void shelf(String title,ArrayList<JSONObject> data,boolean isHabitat){
        if(data.isEmpty())return;
        LinearLayout header=new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams headerLp=new LinearLayout.LayoutParams(-1,-2);
        headerLp.topMargin=dp(20);headerLp.bottomMargin=dp(9);
        content.addView(header,headerLp);
        TextView heading=note(title,19,UiKit.FOREST,true);
        header.addView(heading,new LinearLayout.LayoutParams(0,-2,1));
        TextView position=note("1 / "+data.size(),12,UiKit.MUTED,false);
        position.setGravity(Gravity.CENTER);
        header.addView(position,new LinearLayout.LayoutParams(dp(51),dp(48)));
        Button previous=UiKit.button(this,"‹","quiet");
        Button next=UiKit.button(this,"›","quiet");
        previous.setContentDescription("Previous "+title+" card");
        next.setContentDescription("Next "+title+" card");
        header.addView(previous,new LinearLayout.LayoutParams(dp(48),dp(48)));
        header.addView(next,new LinearLayout.LayoutParams(dp(48),dp(48)));
        String railKey=(isHabitat?"habitats:":"species:")+title;
        int restoreOffset=railOffsets.getOrDefault(railKey,0);
        HorizontalScrollView scroll=new HorizontalScrollView(this);
        scroll.setHorizontalScrollBarEnabled(false);
        scroll.setFillViewport(false);
        scroll.setSmoothScrollingEnabled(true);
        LinearLayout row=new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(dp(2),dp(2),dp(16),dp(10));
        // Keep positions stable while only building cards near the current viewport.
        ArrayList<FrameLayout> slots=new ArrayList<>();
        for(int i=0;i<data.size();i++){
            FrameLayout slot=new FrameLayout(this);
            slot.setMinimumHeight(dp(largeText()?625:410));
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(dp(cardWidth()),-2);
            lp.rightMargin=dp(11);
            row.addView(slot,lp);
            slots.add(slot);
        }
        scroll.addView(row,new HorizontalScrollView.LayoutParams(-2,-2));
        int stride=dp(cardWidth()+11);
        // At most five active cards per shelf, including one on either side of view.
        Runnable hydrate=()->{
            int center=Math.max(0,Math.min(data.size()-1,
                (scroll.getScrollX()+stride/2)/stride));
            for(int i=0;i<slots.size();i++){
                FrameLayout slot=slots.get(i);
                if(Math.abs(i-center)<=2){
                    if(slot.getChildCount()==0){
                        JSONObject item=data.get(i);
                        JSONObject status=statusIndex.get(item.optString("scientificName"));
                        View card=isHabitat?habitatCard(item):speciesCard(item,status);
                        slot.addView(card,new FrameLayout.LayoutParams(-1,-2));
                    }
                }else if(slot.getChildCount()>0){
                    slot.removeAllViews();
                }
            }
        };
        Runnable update=()->{
            int index=Math.max(0,Math.min(data.size()-1,
                (scroll.getScrollX()+stride/2)/stride));
            position.setText((index+1)+" / "+data.size());
            previous.setEnabled(index>0);
            next.setEnabled(index<data.size()-1);
        };
        previous.setOnClickListener(v->{
            int index=Math.max(0,(scroll.getScrollX()+stride/2)/stride);
            moveCard(scroll,Math.max(0,(index-1)*stride));
        });
        next.setOnClickListener(v->{
            int index=Math.max(0,(scroll.getScrollX()+stride/2)/stride);
            int endX=Math.max(0,row.getWidth()-scroll.getWidth());
            moveCard(scroll,Math.min(endX,(index+1)*stride));
        });
        scroll.setOnScrollChangeListener((View v,int x,int y,int oldX,int oldY)->{
            railOffsets.put(railKey,x);
            hydrate.run();
            update.run();
        });
        // Capture before the new view's first scroll callback changes the map.
        scroll.post(()->{
            scroll.scrollTo(restoreOffset,0);
            hydrate.run();
            update.run();
        });
        scroll.setContentDescription("Swipe "+title+" cards horizontally, "+
            data.size()+" references");
        content.addView(scroll);
    }
    /** Filter only the documented offline illustrated references, never survey observations. */
    private boolean matches(JSONObject item,boolean isHabitat){
        String group=item.optString("group");
        String selected=isHabitat?habitatGroup:speciesGroup;
        if(!"All".equals(selected)&&!selected.equals(group))return false;
        String query=searchQuery.trim().toLowerCase(Locale.ROOT);
        if(query.isEmpty())return true;
        String haystack=(isHabitat?
            item.optString("name")+" "+group+" "+item.optString("overview"):
            item.optString("commonName")+" "+item.optString("scientificName")+" "+group)
                .toLowerCase(Locale.ROOT);
        return haystack.contains(query);
    }
    private void filterRail(){
        String[] names=showingHabitat?
            new String[]{"All","Terrestrial","Edaphic","Aquatic","Coastal","Human-modified"}:
            new String[]{"All","Birds","Amphibians","Reptiles","Mammals"};
        String selected=showingHabitat?habitatGroup:speciesGroup;
        HorizontalScrollView rail=new HorizontalScrollView(this);
        rail.setHorizontalScrollBarEnabled(false);
        LinearLayout chips=new LinearLayout(this);
        chips.setOrientation(LinearLayout.HORIZONTAL);
        chips.setPadding(dp(2),dp(10),dp(10),dp(6));
        for(String name:names){
            boolean active=name.equals(selected);
            Button chip=UiKit.button(this,name,active?"pond":"quiet");
            chip.setTextSize(12);
            chip.setContentDescription("Filter "+(showingHabitat?"habitats":"species")+
                " by "+name+(active?", selected":""));
            chip.setMinHeight(dp(48));
            chip.setOnClickListener(v->{
                if(active)return;
                if(showingHabitat)habitatGroup=name;else speciesGroup=name;
                railOffsets.clear();
                if(pageScroll!=null)pageScroll.scrollTo(0,0);
                render(false);
            });
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-2,dp(48));
            lp.rightMargin=dp(8);
            chips.addView(chip,lp);
        }
        rail.addView(chips,new HorizontalScrollView.LayoutParams(-2,-2));
        final boolean habitatFilter=showingHabitat;
        final int restoreChipX=habitatFilter?habitatFilterX:speciesFilterX;
        rail.setOnScrollChangeListener((View v,int x,int y,int oldX,int oldY)->{
            if(habitatFilter)habitatFilterX=x;else speciesFilterX=x;
        });
        rail.post(()->rail.scrollTo(restoreChipX,0));
        content.addView(rail);
    }
    private void emptyState(){
        LinearLayout empty=new LinearLayout(this);
        empty.setOrientation(LinearLayout.VERTICAL);
        empty.setGravity(Gravity.CENTER);
        empty.setPadding(dp(22),dp(35),dp(22),dp(35));
        empty.setBackground(surface(this,Color.WHITE,UiKit.BORDER,20));
        empty.addView(new LikasPortalUi.Glyph(this,
            showingHabitat?"habitat":"species",UiKit.POND),
            new LinearLayout.LayoutParams(dp(52),dp(52)));
        TextView title=note("No matches",19,UiKit.INK,true);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0,dp(12),0,dp(5));
        empty.addView(title);
        TextView copy=note("Try another name or group.",13,UiKit.MUTED,false);
        copy.setGravity(Gravity.CENTER);
        empty.addView(copy);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
        lp.topMargin=dp(15);
        content.addView(empty,lp);
    }
    private boolean largeText(){
        return getResources().getConfiguration().fontScale>=1.60f;
    }
    private int cardWidth(){
        int screen=getResources().getConfiguration().screenWidthDp;
        return largeText()?Math.max(230,Math.min(310,screen-56)):
            Math.max(208,Math.min(286,screen-72));
    }
    private void action(String name,Runnable click){
        Button b=UiKit.button(this,name,"secondary");
        b.setOnClickListener(v->click.run());
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(largeText()?68:52));
        lp.topMargin=dp(16);content.addView(b,lp);
    }
    private void render(){render(true);}
    private void render(boolean animate){
        content.removeAllViews();
        speciesTab.setEnabled(showingHabitat);
        habitatTab.setEnabled(!showingHabitat);
        speciesTab.setText(showingHabitat?"Species":"Species ●");
        habitatTab.setText(showingHabitat?"Habitats ●":"Habitats");
        boolean narrowed=!searchQuery.trim().isEmpty() ||
            !(showingHabitat?"All".equals(habitatGroup):"All".equals(speciesGroup));
        if(showingHabitat){
            content.addView(LikasPortalUi.kicker(this,"ECOSYSTEMS"));
            content.addView(note("Habitats",27,UiKit.INK,true));
            TextView sub=note("Offline ecosystem field guides",13,UiKit.MUTED,false);
            sub.setPadding(0,dp(4),0,dp(3));content.addView(sub);
            filterRail();
            int found=0;
            for(JSONObject p:habitats)if(matches(p,true))found++;
            TextView count=note(found+" of "+habitats.size()+" illustrated habitats",
                12,UiKit.MUTED,false);
            count.setPadding(dp(2),dp(4),0,0);content.addView(count);
            if(found==0)emptyState();
            else if(narrowed){
                ArrayList<JSONObject> matches=new ArrayList<>();
                for(JSONObject p:habitats)if(matches(p,true))matches.add(p);
                shelf("Matches",matches,true);
            }else{
                for(String group:new String[]{"Terrestrial","Edaphic","Aquatic","Coastal","Human-modified"}){
                    ArrayList<JSONObject> matches=new ArrayList<>();
                    for(JSONObject p:habitats)if(group.equals(p.optString("group")))matches.add(p);
                    shelf(group,matches,true);
                }
            }
            action("All habitats",()->startActivity(new Intent(this,HabitatRecordsActivity.class)));
        }else{
            content.addView(LikasPortalUi.kicker(this,"WILDLIFE"));
            content.addView(note("Species",27,UiKit.INK,true));
            TextView sub=note("Offline species guides",
                13,UiKit.MUTED,false);
            sub.setPadding(0,dp(4),0,dp(3));content.addView(sub);
            filterRail();
            int found=0;
            for(JSONObject p:species)if(matches(p,false))found++;
            TextView count=note(found+" of "+species.size()+" illustrated accounts",
                12,UiKit.MUTED,false);
            count.setPadding(dp(2),dp(4),0,0);content.addView(count);
            if(found==0)emptyState();
            else if(narrowed){
                ArrayList<JSONObject> matches=new ArrayList<>();
                for(JSONObject p:species)if(matches(p,false))matches.add(p);
                shelf("Matches",matches,false);
            }else{
                ArrayList<JSONObject> conservation=new ArrayList<>();
                for(JSONObject s:species)
                    if(statusIndex.containsKey(s.optString("scientificName")))
                        conservation.add(s);
                conservation.sort((first,second)->Integer.compare(
                    level(statusIndex.get(second.optString("scientificName"))),
                    level(statusIndex.get(first.optString("scientificName")))));
                shelf("Conservation",conservation,false);
                for(String group:new String[]{"Birds","Amphibians","Reptiles","Mammals"}){
                    ArrayList<JSONObject> groupAccounts=new ArrayList<>();
                    for(JSONObject p:species)if(group.equals(p.optString("group")))
                        groupAccounts.add(p);
                    shelf(group,groupAccounts,false);
                }
            }
            action("Full Philippine species checklist",()->startActivity(new Intent(this,Rc7SpeciesLibraryActivity.class)));
        }
        if(animate)UiMotion.enter(this,content);
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
        showingHabitat=saved!=null?
            saved.getBoolean("habitats","habitats".equals(getIntent().getStringExtra("view"))):
            "habitats".equals(getIntent().getStringExtra("view"));
        if(saved!=null){
            Bundle previous=saved.getBundle("railOffsets");
            if(previous!=null)
                for(String key:previous.keySet())railOffsets.put(key,previous.getInt(key));
            searchQuery=saved.getString("searchQuery","");
            speciesGroup=saved.getString("speciesGroup","All");
            habitatGroup=saved.getString("habitatGroup","All");
            speciesFilterX=saved.getInt("speciesFilterX",0);
            habitatFilterX=saved.getInt("habitatFilterX",0);
        }
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
        LinearLayout searchBar=new LinearLayout(this);
        searchBar.setOrientation(LinearLayout.HORIZONTAL);
        searchBar.setGravity(Gravity.CENTER_VERTICAL);
        searchBar.setPadding(dp(8),0,dp(6),0);
        searchBar.setBackground(surface(this,Color.WHITE,UiKit.BORDER,17));
        LinearLayout.LayoutParams searchLp=new LinearLayout.LayoutParams(-1,dp(largeText()?68:58));
        searchLp.topMargin=dp(10);
        root.addView(searchBar,searchLp);
        TextView glass=note("⌕",27,UiKit.POND,false);
        glass.setGravity(Gravity.CENTER);
        glass.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        searchBar.addView(glass,new LinearLayout.LayoutParams(dp(34),-1));
        searchInput=new EditText(this);
        searchInput.setSingleLine(true);
        searchInput.setTextSize(15);
        searchInput.setHint("Find name or habitat");
        searchInput.setInputType(InputType.TYPE_CLASS_TEXT);
        searchInput.setBackgroundColor(Color.TRANSPARENT);
        searchInput.setContentDescription("Search illustrated offline species and habitat accounts");
        searchInput.setPadding(dp(4),0,dp(4),0);
        searchInput.setText(searchQuery);
        searchBar.addView(searchInput,new LinearLayout.LayoutParams(0,-1,1f));
        clearSearch=UiKit.button(this,"×","quiet");
        clearSearch.setTextSize(19);
        clearSearch.setContentDescription("Clear library search");
        clearSearch.setVisibility(searchQuery.isEmpty()?View.GONE:View.VISIBLE);
        clearSearch.setOnClickListener(v->searchInput.setText(""));
        searchBar.addView(clearSearch,new LinearLayout.LayoutParams(dp(48),dp(48)));
        searchInput.addTextChangedListener(new TextWatcher(){
            public void beforeTextChanged(CharSequence s,int start,int count,int after){}
            public void onTextChanged(CharSequence s,int start,int before,int count){
                searchQuery=s.toString();
                clearSearch.setVisibility(searchQuery.isEmpty()?View.GONE:View.VISIBLE);
                searchHandler.removeCallbacks(searchRefresh);
                searchHandler.postDelayed(searchRefresh,180);
            }
            public void afterTextChanged(Editable text){}
        });
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);
        pageScroll=scroll;
        content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(1),dp(9),dp(1),dp(20));
        scroll.addView(content);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        setContentView(root);render();
    }
    @Override protected void onSaveInstanceState(Bundle out){
        out.putBoolean("habitats",showingHabitat);
        Bundle positions=new Bundle();
        for(Map.Entry<String,Integer> entry:railOffsets.entrySet())
            positions.putInt(entry.getKey(),entry.getValue());
        out.putBundle("railOffsets",positions);
        out.putString("searchQuery",searchQuery);
        out.putString("speciesGroup",speciesGroup);
        out.putString("habitatGroup",habitatGroup);
        out.putInt("speciesFilterX",speciesFilterX);
        out.putInt("habitatFilterX",habitatFilterX);
        super.onSaveInstanceState(out);
    }
    @Override protected void onDestroy(){
        searchHandler.removeCallbacks(searchRefresh);
        super.onDestroy();
    }
}
