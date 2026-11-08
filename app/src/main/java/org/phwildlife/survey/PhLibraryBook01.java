package org.phwildlife.survey;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.text.util.Linkify;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Read-only companion to PHLibrary Book 01 v1.75 and the source-audited
 * editorial overlay v1.76, retaining Discovery Rarity v0.3.1.
 * Species narratives and badges come directly from the owner-supplied edition.
 * Never writes survey/taxon data and does not infer station occurrence.
 */
public final class PhLibraryBook01 {
    private static final ArrayList<JSONObject> ENTRIES = new ArrayList<>();
    private static final Map<String,Integer> BY_NAME = new HashMap<>();
    private static boolean initialized = false;
    private static String problem = "";
    private PhLibraryBook01() {}

    private static int dp(Activity a,float value) {
        return Math.round(value*a.getResources().getDisplayMetrics().density);
    }
    private static TextView label(Activity a,String value,float sp,int color,boolean strong) {
        TextView text = new TextView(a);
        text.setText(value);
        text.setTextSize(sp);
        text.setTextColor(color);
        if(strong)text.setTypeface(null,Typeface.BOLD);
        return text;
    }
    private static String key(String value) {
        return value==null?"":value.trim().toLowerCase(Locale.ROOT);
    }
    private static JSONObject readPart(Context context,int part) throws Exception {
        String path=String.format(Locale.ROOT,"phlibrary_book01_v175_%02d.json",part);
        try(InputStream input=context.getAssets().open(path);
            ByteArrayOutputStream bytes=new ByteArrayOutputStream()) {
            byte[] buffer=new byte[8192];
            int length;
            while((length=input.read(buffer))!=-1) {
                if(bytes.size()+length>350000)throw new IllegalStateException("Book asset exceeds limit");
                bytes.write(buffer,0,length);
            }
            return new JSONObject(bytes.toString("UTF-8"));
        }
    }
    /** Apply a validated, reversible text-only editorial layer on top of the
     *  original, unchanged Book 01 accounts. Scientific names, status labels,
     *  geographic fields, group and rarity classifications are never overridden. */
    private static void applyEditorial(Context context,
            ArrayList<JSONObject> rows,HashMap<String,Integer> index)
            throws Exception {
        JSONObject file;
        try(InputStream input=context.getAssets().open("phlibrary_editorial_v176.json");
            ByteArrayOutputStream bytes=new ByteArrayOutputStream()){
            byte[] buffer=new byte[8192];
            int count;
            while((count=input.read(buffer))!=-1){
                if(bytes.size()+count>900000)
                    throw new IllegalStateException("Editorial source pack exceeds limit");
                bytes.write(buffer,0,count);
            }
            file=new JSONObject(bytes.toString("UTF-8"));
        }
        if(!"1.76-editorial".equals(file.optString("edition")) ||
           !"1.75".equals(file.optString("baseEdition")) ||
           !"0.3.1".equals(file.optString("rarityModel")) ||
           file.getInt("speciesCount")!=714)
            throw new IllegalStateException("Unexpected PHLibrary editorial edition");
        JSONArray revised=file.getJSONArray("records");
        if(revised.length()!=file.getInt("changed") || revised.length()<400)
            throw new IllegalStateException("Incomplete editorial overlay");
        HashSet<String> updated=new HashSet<>();
        for(int i=0;i<revised.length();i++){
            JSONObject fix=revised.getJSONObject(i);
            String scientific=fix.getString("s");
            String normalized=key(scientific);
            Integer position=index.get(normalized);
            String paragraph=fix.getString("p").trim();
            JSONArray sources=fix.getJSONArray("r");
            if(position==null || !updated.add(normalized) ||
                !rows.get(position).optString("s").equals(scientific) ||
                paragraph.length()<100 || paragraph.contains("\\n") ||
                sources.length()==0)
                throw new IllegalStateException("Invalid editorial account "+scientific);
            for(int j=0;j<sources.length();j++){
                JSONObject citation=sources.getJSONObject(j);
                if(!paragraph.contains(citation.getString("short")))
                    throw new IllegalStateException("Source citation missing in "+scientific);
            }
            JSONObject target=rows.get(position);
            target.put("p",paragraph);
            target.put("r",sources);
        }
    }
    /** Readability edits retain each account's taxon, source list, and status. */
    private static void applyReaderRefinements(Context context,
            ArrayList<JSONObject> rows,HashMap<String,Integer> index)
            throws Exception {
        JSONObject pack;
        try(InputStream input=context.getAssets().open("phlibrary_reader_refinements_v178.json");
            ByteArrayOutputStream bytes=new ByteArrayOutputStream()){
            byte[] buffer=new byte[8192]; int n;
            while((n=input.read(buffer))!=-1){
                if(bytes.size()+n>120000)throw new IllegalStateException("Reader pack exceeds limit");
                bytes.write(buffer,0,n);
            }
            pack=new JSONObject(bytes.toString("UTF-8"));
        }
        if(!"1.78-fluent-reader".equals(pack.optString("edition")) ||
           !"1.75".equals(pack.optString("baseEdition")) ||
           !"1.76-editorial".equals(pack.optString("editorialEdition")) ||
           !"0.3.1".equals(pack.optString("rarityModel")) ||
           pack.getInt("speciesCount")!=714)
            throw new IllegalStateException("Unexpected reader refinement edition");
        JSONArray edits=pack.getJSONArray("records");
        if(edits.length()!=pack.getInt("changed") || edits.length()<40)
            throw new IllegalStateException("Incomplete reader refinements");
        HashSet<String> touched=new HashSet<>();
        for(int i=0;i<edits.length();i++){
            JSONObject entry=edits.getJSONObject(i);
            String scientific=entry.getString("s");
            Integer at=index.get(key(scientific));
            String old=entry.getString("old");
            String revised=entry.getString("revised");
            if(at==null || !touched.add(scientific) ||
               old.length()<30 || revised.length()<25 ||
               revised.contains("rather than") || revised.contains("\\n"))
                throw new IllegalStateException("Invalid reader correction "+scientific);
            JSONObject target=rows.get(at);
            if(!scientific.equals(target.optString("s")))
                throw new IllegalStateException("Reader taxon mismatch "+scientific);
            String before=target.getString("p");
            int pos=before.indexOf(old);
            if(pos<0 || before.indexOf(old,pos+old.length())>=0)
                throw new IllegalStateException("Reader correction anchor mismatch "+scientific);
            String after=before.substring(0,pos)+revised+before.substring(pos+old.length());
            JSONArray sources=target.getJSONArray("r");
            for(int j=0;j<sources.length();j++){
                String cited=sources.getJSONObject(j).getString("short");
                if(before.contains(cited) && !after.contains(cited))
                    throw new IllegalStateException("Lost citation in "+scientific);
            }
            target.put("p",after);
        }
    }

    /** Educational field-search cues are independent of the research source paragraph.
     *  Read-only, species-keyed and never used to calculate Discovery Rarity.
     *  Each inference is phrased as field guidance, not confirmed habitat selection. */
    private static void applyHabitatCues(Context context,
            ArrayList<JSONObject> rows,HashMap<String,Integer> index)
            throws Exception {
        JSONObject file;
        try(InputStream input=context.getAssets().open("phlibrary_habitat_cues_v177.json");
            ByteArrayOutputStream bytes=new ByteArrayOutputStream()){
            byte[] buffer=new byte[8192];
            int count;
            while((count=input.read(buffer))!=-1){
                if(bytes.size()+count>150000)
                    throw new IllegalStateException("Habitat cue pack exceeds limit");
                bytes.write(buffer,0,count);
            }
            file=new JSONObject(bytes.toString("UTF-8"));
        }
        if(!"1.77-habitat-cues".equals(file.optString("edition")) ||
           !"1.75".equals(file.optString("baseEdition")) ||
           !"1.76-editorial".equals(file.optString("editorialEdition")) ||
           !"0.3.1".equals(file.optString("rarityModel")) ||
           file.getInt("speciesCount")!=714)
            throw new IllegalStateException("Unexpected PHLibrary habitat pack");
        JSONArray details=file.getJSONArray("records");
        if(details.length()!=file.getInt("changed") ||
           details.length()<100 || details.length()>714)
            throw new IllegalStateException("Incomplete habitat field notes");
        HashSet<String> updated=new HashSet<>();
        for(int i=0;i<details.length();i++){
            JSONObject detail=details.getJSONObject(i);
            String scientific=detail.getString("s");
            String normalized=key(scientific);
            Integer position=index.get(normalized);
            String cue=detail.getString("fieldCue").trim();
            String basis=detail.getString("basis");
            if(position==null || !updated.add(normalized) ||
               cue.length()<45 || cue.length()>450 ||
               cue.contains("\\n") || cue.contains("\\r") ||
               !("source_summary".equals(basis) ||
                 "field_guide_inference_or_prior-account-synthesis".equals(basis)))
                throw new IllegalStateException("Invalid habitat cue for "+scientific);
            JSONObject target=rows.get(position);
            if(!scientific.equals(target.optString("s")) ||
               !detail.getString("group").equals(target.optString("g")))
                throw new IllegalStateException("Taxon mismatch in habitat cue");
            String narrative=target.getString("p").trim();
            String anchor=detail.getString("anchor").trim();
            String mode=detail.getString("mode");
            int insertion=narrative.indexOf(anchor);
            if(anchor.length()<35 || insertion<0 ||
                narrative.indexOf(anchor,insertion+anchor.length())>=0 ||
                !("insert".equals(mode) || "replace".equals(mode) ||
                  "retain".equals(mode)))
                throw new IllegalStateException("Habitat placement mismatch "+scientific);
            String expanded="retain".equals(mode)?narrative:
                narrative.substring(0,insertion)+
                ("replace".equals(mode)?cue:anchor+" "+cue)+
                narrative.substring(insertion+anchor.length());
            if(detail.has("newReference")){
                JSONObject source=detail.getJSONObject("newReference");
                String shortCitation=source.getString("short");
                if(!cue.contains("("+shortCitation+")") ||
                   source.getString("full").trim().isEmpty() ||
                   source.getString("url").trim().isEmpty())
                    throw new IllegalStateException("Habitat source missing "+scientific);
                JSONArray bibliography=target.getJSONArray("r");
                boolean alreadyPresent=false;
                for(int j=0;j<bibliography.length();j++){
                    if(shortCitation.equals(bibliography.getJSONObject(j).optString("short"))){
                        alreadyPresent=true; break;
                    }
                }
                if(!alreadyPresent)bibliography.put(source);
            }
            target.put("p",expanded);
        }
    }
    /** Final positive-prose pass, applied after source-specific habitat integration. */
    private static void applyReaderPolish(Context context,
            ArrayList<JSONObject> rows,HashMap<String,Integer> index)
            throws Exception {
        JSONObject pack;
        try(InputStream input=context.getAssets().open("phlibrary_reader_polish_v179.json");
            ByteArrayOutputStream bytes=new ByteArrayOutputStream()){
            byte[] buffer=new byte[8192]; int n;
            while((n=input.read(buffer))!=-1){
                if(bytes.size()+n>130000)throw new IllegalStateException("Final prose pack exceeds limit");
                bytes.write(buffer,0,n);
            }
            pack=new JSONObject(bytes.toString("UTF-8"));
        }
        if(!"1.79-reader-polish".equals(pack.optString("edition")) ||
           !"1.75".equals(pack.optString("baseEdition")) ||
           !"1.78-fluent-reader".equals(pack.optString("readerEdition")) ||
           !"1.77-habitat-cues".equals(pack.optString("habitatEdition")) ||
           !"0.3.1".equals(pack.optString("rarityModel")) ||
           pack.getInt("speciesCount")!=714)
            throw new IllegalStateException("Unexpected final PHLibrary prose pack");
        JSONArray edits=pack.getJSONArray("records");
        if(edits.length()!=pack.getInt("changed") || edits.length()<70)
            throw new IllegalStateException("Incomplete final prose edit");
        HashSet<String> changed=new HashSet<>();
        for(int i=0;i<edits.length();i++){
            JSONObject entry=edits.getJSONObject(i);
            String scientific=entry.getString("s");
            Integer at=index.get(key(scientific));
            String old=entry.getString("old");
            String revised=entry.getString("revised");
            if(at==null || !changed.add(scientific+"::"+old) ||
               old.length()<30 || revised.length()<25 ||
               revised.contains("rather than") || revised.contains("\\n"))
                throw new IllegalStateException("Invalid final prose correction "+scientific);
            JSONObject target=rows.get(at);
            if(!scientific.equals(target.optString("s")))
                throw new IllegalStateException("Final prose taxon mismatch "+scientific);
            String before=target.getString("p");
            int pos=before.indexOf(old);
            if(pos<0 || before.indexOf(old,pos+old.length())>=0)
                throw new IllegalStateException("Final prose anchor mismatch "+scientific);
            String after=before.substring(0,pos)+revised+before.substring(pos+old.length());
            JSONArray sources=target.getJSONArray("r");
            for(int j=0;j<sources.length();j++){
                String cited=sources.getJSONObject(j).getString("short");
                if(before.contains(cited) && !after.contains(cited))
                    throw new IllegalStateException("Lost final prose citation "+scientific);
            }
            target.put("p",after);
        }
    }

    private static synchronized void load(Context context) {
        if(initialized)return;
        initialized=true;
        ArrayList<JSONObject> rows=new ArrayList<>();
        HashMap<String,Integer> index=new HashMap<>();
        try {
            for(int part=1;part<=8;part++) {
                JSONObject source=readPart(context,part);
                if(!"1.75".equals(source.optString("edition")) ||
                   !"0.3.1".equals(source.optString("rarityModel")))
                    throw new IllegalStateException("Unexpected PHLibrary source edition");
                JSONArray batch=source.getJSONArray("records");
                for(int i=0;i<batch.length();i++) {
                    JSONObject row=batch.getJSONObject(i);
                    String scientific=row.optString("s").trim();
                    if(scientific.isEmpty() || row.optString("p").trim().isEmpty() ||
                       row.optString("t").trim().isEmpty() ||
                       index.containsKey(key(scientific)))
                        throw new IllegalStateException("Incomplete or duplicate book record");
                    index.put(key(scientific),rows.size());
                    rows.add(row);
                }
            }
            if(rows.size()!=714)throw new IllegalStateException("Incomplete PHLibrary edition");
            applyEditorial(context,rows,index);
            applyReaderRefinements(context,rows,index);
            applyHabitatCues(context,rows,index);
            applyReaderPolish(context,rows,index);
            // The source PAIC714 checklist retains an older combination for
            // this bat. The Book 01 account documents the named transfer.
            Integer formerBat=index.get(key("Mops plicatus"));
            if(formerBat!=null)index.put(key("Chaerephon plicata"),formerBat);
            ENTRIES.addAll(rows);
            BY_NAME.putAll(index);
        } catch(Exception ex) {
            problem="PHLibrary Book 01 data is unavailable. Please use the source catalog.";
            ENTRIES.clear();
            BY_NAME.clear();
        }
    }
    public static boolean ready(Context context) {
        load(context);
        return ENTRIES.size()==714;
    }
    public static String availabilityMessage(Context context) {
        load(context);
        return problem.isEmpty()?"Book 01 · Greater Mindanao · Reader v1.79":problem;
    }
    public static List<JSONObject> entries(Context context) {
        load(context);
        return new ArrayList<>(ENTRIES);
    }
    public static boolean has(Context context,String scientific) {
        load(context);
        return BY_NAME.containsKey(key(scientific));
    }
    private static JSONObject find(Context context,String scientific) {
        load(context);
        Integer position=BY_NAME.get(key(scientific));
        return position==null?null:ENTRIES.get(position);
    }
    private static void referenceDialog(Activity a,JSONArray refs) {
        if(refs==null || refs.length()==0)return;
        String[] titles=new String[refs.length()];
        for(int i=0;i<refs.length();i++) {
            JSONObject row=refs.optJSONObject(i);
            titles[i]=row==null?"Source":row.optString("short","Source");
        }
        new AlertDialog.Builder(a)
            .setTitle("Account references")
            .setItems(titles,(dialog,selected)->{
                JSONObject selectedRef=refs.optJSONObject(selected);
                if(selectedRef==null)return;
                String brief=selectedRef.optString("short");
                String full=selectedRef.optString("full");
                String detail=full.trim().isEmpty()?brief:full;
                TextView text=label(a,detail,14,UiKit.INK,false);
                text.setPadding(dp(a,22),dp(a,14),dp(a,22),dp(a,8));
                text.setTextIsSelectable(true);
                text.setAutoLinkMask(Linkify.WEB_URLS);
                text.setMovementMethod(LinkMovementMethod.getInstance());
                new AlertDialog.Builder(a).setTitle("Reference")
                    .setView(text).setPositiveButton("Close",null).show();
            })
            .setNegativeButton("Close",null).show();
    }
    private static void addNav(Activity a,LinearLayout parent,int position,
                               Consumer<String> navigate) {
        boolean stacked=a.getResources().getConfiguration().screenWidthDp<350 ||
            a.getResources().getConfiguration().fontScale>=1.6f;
        LinearLayout bar=new LinearLayout(a);
        bar.setOrientation(stacked?LinearLayout.VERTICAL:LinearLayout.HORIZONTAL);
        bar.setPadding(0,dp(a,12),0,dp(a,8));
        if(position>0) {
            JSONObject before=ENTRIES.get(position-1);
            Button previous=UiKit.button(a,"Previous species","quiet");
            previous.setContentDescription("Previous: "+before.optString("c")+" ("+
                before.optString("s")+")");
            previous.setOnClickListener(v->navigate.accept(before.optString("s")));
            bar.addView(previous,stacked?new LinearLayout.LayoutParams(-1,dp(a,52)):
                new LinearLayout.LayoutParams(0,dp(a,52),1));
        }
        if(position<ENTRIES.size()-1) {
            JSONObject after=ENTRIES.get(position+1);
            Button next=UiKit.button(a,"Next species","quiet");
            next.setContentDescription("Next: "+after.optString("c")+" ("+
                after.optString("s")+")");
            next.setOnClickListener(v->navigate.accept(after.optString("s")));
            LinearLayout.LayoutParams lp=stacked?
                new LinearLayout.LayoutParams(-1,dp(a,52)):
                new LinearLayout.LayoutParams(0,dp(a,52),1);
            if(!stacked && position>0)lp.leftMargin=dp(a,8);
            else if(stacked && position>0)lp.topMargin=dp(a,8);
            bar.addView(next,lp);
        }
        parent.addView(bar);
    }
    /**
     * The entire body is exactly one continuous native TextView paragraph.
     * Author-year source strings remain unchanged in that paragraph; the
     * compact reference strip at the bottom allows bibliography inspection.
     */
    public static boolean render(Activity a,LinearLayout parent,String scientific,
                                 Consumer<String> navigate) {
        JSONObject record=find(a,scientific);
        if(record==null)return false;
        Integer pos=BY_NAME.get(key(scientific));
        if(pos==null)return false;
        parent.removeAllViews();
        parent.setPadding(0,dp(a,9),0,dp(a,18));
        // Text-only educational book reading; photographs are deliberately omitted.
        TextView kicker=label(a,
            a.getResources().getConfiguration().fontScale>=1.6f?
            "GREATER MINDANAO":"PHLIBRARY  ·  GREATER MINDANAO  ·  BOOK 01",
            11,UiKit.MUTED,true);
        kicker.setLetterSpacing(0.04f);
        kicker.setPadding(0,dp(a,13),0,dp(a,8));
        parent.addView(kicker);
        String common=record.optString("c").trim();
        TextView title=label(a,common.isEmpty()?scientific:common,23,UiKit.INK,true);
        title.setPadding(0,0,0,dp(a,3));
        parent.addView(title);
        TextView latin=label(a,record.optString("s"),17,UiKit.INK,false);
        latin.setTypeface(null,Typeface.ITALIC);
        latin.setPadding(0,0,0,dp(a,5));
        parent.addView(latin);
        TextView taxonomy=label(a,record.optString("o")+"  ·  "+
            record.optString("f"),13,UiKit.MUTED,false);
        taxonomy.setPadding(0,0,0,dp(a,9));
        parent.addView(taxonomy);
        // Rarity, IUCN and origin retain their separate source-defined labels.
        // Original Word edition badge order and wording are preserved.
        String badgeLine=record.optString("b")
            .replace("↝","").replace("◉","").replace("◆","").replace("✦","")
            .replace("     •     ","   ·   ").replaceAll(" +"," ").trim();
        TextView badges=label(a,badgeLine,13,UiKit.FOREST,true);
        badges.setPadding(dp(a,1),dp(a,8),dp(a,1),dp(a,9));
        badges.setLineSpacing(dp(a,3),1.08f);
        parent.addView(badges);
        View separator=new View(a);
        separator.setBackgroundColor(UiKit.BORDER);
        LinearLayout.LayoutParams separatorLp=new LinearLayout.LayoutParams(-1,dp(a,1));
        separatorLp.topMargin=dp(a,17);
        separatorLp.bottomMargin=dp(a,17);
        parent.addView(separator,separatorLp);
        TextView article=label(a,record.optString("p"),16.5f,UiKit.INK,false);
        article.setLineSpacing(dp(a,4),1.1f);
        article.setTextIsSelectable(true);
        parent.addView(article,new LinearLayout.LayoutParams(-1,-2));

        JSONArray refs=record.optJSONArray("r");
        if(refs!=null && refs.length()>0) {
            View line=new View(a);
            line.setBackgroundColor(UiKit.BORDER);
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(a,1));
            lp.topMargin=dp(a,20); lp.bottomMargin=dp(a,12);
            parent.addView(line,lp);
            TextView heading=label(a,"REFERENCES",12,UiKit.MUTED,true);
            heading.setLetterSpacing(.08f);
            parent.addView(heading);
            StringBuilder listing=new StringBuilder();
            for(int i=0;i<refs.length();i++) {
                JSONObject ref=refs.optJSONObject(i);
                if(ref==null)continue;
                if(listing.length()>0)listing.append("  ·  ");
                listing.append(ref.optString("short"));
            }
            TextView small=label(a,listing.toString(),12,UiKit.MUTED,false);
            small.setLineSpacing(dp(a,3),1.03f);
            small.setPadding(0,dp(a,6),0,dp(a,7));
            small.setContentDescription("Account references. Tap for bibliographic details. "+
                listing);
            small.setClickable(true);
            small.setFocusable(true);
            small.setOnClickListener(v->referenceDialog(a,refs));
            parent.addView(small);
        }
        TextView edition=label(a,"PHLibrary Book 01 · Text v1.79 · Rarity v0.3.1",
            11,UiKit.MUTED,false);
        edition.setPadding(0,dp(a,5),0,dp(a,5));
        parent.addView(edition);
        addNav(a,parent,pos,navigate);
        return true;
    }
}
