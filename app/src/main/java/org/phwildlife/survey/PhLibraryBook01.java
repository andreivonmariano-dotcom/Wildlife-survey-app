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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Read-only companion to PHLibrary Book 01, edition 1.75, Discovery Rarity 0.3.1.
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
        return problem.isEmpty()?"Book 01 · Greater Mindanao · Edition 1.75":problem;
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
        // This helper checks the approved offline photo manifest and includes
        // its photographer, source and license only when a real asset exists.
        TextView kicker=label(a,"PHLIBRARY  ·  GREATER MINDANAO  ·  BOOK 01",11,UiKit.MUTED,true);
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
        TextView badges=label(a,record.optString("b").replace("     •     ","   ·   "),
            12.5f,UiKit.INK,true);
        badges.setPadding(dp(a,10),dp(a,8),dp(a,10),dp(a,8));
        badges.setBackground(UiKit.surface(a));
        parent.addView(badges);
        OfflinePhotoEvidence.addPhLibraryPlate(a,parent,scientific);
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
        TextView edition=label(a,"PHLibrary Book 01 · Edition 1.75 · Rarity v0.3.1",
            11,UiKit.MUTED,false);
        edition.setPadding(0,dp(a,5),0,dp(a,5));
        parent.addView(edition);
        addNav(a,parent,pos,navigate);
        return true;
    }
}
