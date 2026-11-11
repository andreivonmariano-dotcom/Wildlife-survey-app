package org.phwildlife.survey;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.text.util.Linkify;
import android.text.method.LinkMovementMethod;
import android.util.Base64;
import android.util.Base64InputStream;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;

/** PHLibrary Book 01: atomic, read-only, offline v1.82 source loader.
 *  Original older v1.75–v1.80 assets remain packaged for audit/rollback but are
 *  never used as the canonical reader's runtime account data. */
public final class PhLibraryBook01 {
    public static final int EXPECTED_COUNT=715;
    private static final String PACK="phlibrary_book01_v182.json.gz.b64";
    private static final String EXPECTED_SHA256="a87021a615a93f64b56cb3c8974e57deaf69bdd7f0ca7478b70068bad533e885";
    private static final String BIB="phlibrary_book01_bibliography_v182.json.gz.b64";
    private static final String BIB_SHA256="cb597385f1fc10bcca5f3f9aa5ddafd64b37d762f481816ca479e26df3a9751e";
    private static final String[] GROUPS={"Birds","Amphibians","Reptiles","Mammals"};
    private static final int[] GROUP_COUNTS={409,53,154,99};
    private static final String[] TIERS={"Common","Uncommon","Rare","Exceptionally rare","Legendary","Unranked"};
    private static final int[] TIER_COUNTS={124,336,214,37,3,1};
    private static final ArrayList<JSONObject> ENTRIES=new ArrayList<>();
    private static final Map<String,Integer> BY_NAME=new HashMap<>();
    private static boolean attempted=false;
    private static String problem="";

    private PhLibraryBook01(){}
    private static int dp(Activity a,float size){return PhLibraryStyle.dp(a,size);}
    private static TextView label(Activity a,String value,float size,int color,boolean strong){
        TextView v=PhLibraryStyle.text(a,value,size,color,strong);
        return v;
    }
    private static String key(String s){return s==null?"":s.trim().toLowerCase(Locale.ROOT);}
    private static String hex(byte[] input){
        StringBuilder b=new StringBuilder(input.length*2);
        for(byte n:input){int value=n&255;b.append("0123456789abcdef".charAt(value>>>4));b.append("0123456789abcdef".charAt(value&15));}
        return b.toString();
    }
    private static String readCompressedJson(Context ctx,String name,String expectedSha,int maxBytes)throws Exception{
        byte[] bytes;
        try(InputStream in=ctx.getAssets().open(name);
            Base64InputStream decoder=new Base64InputStream(in,Base64.DEFAULT);
            GZIPInputStream gz=new GZIPInputStream(decoder);
            ByteArrayOutputStream result=new ByteArrayOutputStream()){
            byte[] buf=new byte[8192];int n;
            while((n=gz.read(buf))!=-1){
                if(result.size()+n>maxBytes)throw new IllegalStateException("PHLibrary source pack too large");
                result.write(buf,0,n);
            }
            bytes=result.toByteArray();
        }
        String digest=hex(MessageDigest.getInstance("SHA-256").digest(bytes));
        if(!expectedSha.equals(digest))throw new IllegalStateException("PHLibrary source checksum mismatch");
        return new String(bytes,StandardCharsets.UTF_8);
    }
    private static void verify(JSONObject root,JSONArray entries)throws Exception{
        if(!"1.82".equals(root.getString("edition")) ||
           !"0.3.4".equals(root.getString("rarityModel")) ||
           root.getInt("speciesCount")!=EXPECTED_COUNT || entries.length()!=EXPECTED_COUNT)
            throw new IllegalStateException("Incorrect PHLibrary source edition or count");
        HashSet<String> names=new HashSet<>();
        int[] groupCounts=new int[GROUPS.length],tierCounts=new int[TIERS.length];
        int frogs=0,north=0,south=0;
        for(int i=0;i<entries.length();i++){
            JSONObject r=entries.getJSONObject(i);
            String scientific=r.getString("s"),paragraph=r.getString("p"),tier=r.getString("t");
            if(!names.add(key(scientific))|| paragraph.trim().isEmpty() || paragraph.contains("\n") ||
               r.getString("c").trim().isEmpty() ||r.getString("o").trim().isEmpty() ||
               r.getString("f").trim().isEmpty() ||r.getString("b").trim().isEmpty())
                throw new IllegalStateException("Invalid PHLibrary account "+scientific);
            int g=-1,t=-1;
            for(int j=0;j<GROUPS.length;j++)if(GROUPS[j].equals(r.getString("g")))g=j;
            for(int j=0;j<TIERS.length;j++)if(TIERS[j].equals(tier))t=j;
            if(g<0||t<0)throw new IllegalStateException("Unknown group or rarity "+scientific);
            groupCounts[g]++;tierCounts[t]++;
            if("Oreophryne cf. nana".equals(scientific)){
                frogs++;if(!"Unranked".equals(tier)||!r.isNull("iucn"))
                    throw new IllegalStateException("Tentative frog has inherited assessment");
            }
            if("Ceyx melanurus".equals(scientific))north++;
            if("Ceyx mindanensis".equals(scientific))south++;
        }
        for(int i=0;i<GROUPS.length;i++)if(groupCounts[i]!=GROUP_COUNTS[i])
            throw new IllegalStateException("Unexpected "+GROUPS[i]+" count");
        for(int i=0;i<TIERS.length;i++)if(tierCounts[i]!=TIER_COUNTS[i])
            throw new IllegalStateException("Unexpected "+TIERS[i]+" count");
        if(frogs!=1||north!=1||south!=1||names.contains(key("Aphantophryne nana")))
            throw new IllegalStateException("PHLibrary species split/identity mismatch");
    }
    private static synchronized void load(Context context){
        if(attempted)return;
        attempted=true;
        try{
            String content=readCompressedJson(context,PACK,EXPECTED_SHA256,1800000);
            JSONObject root=new JSONObject(content);
            JSONArray source=root.getJSONArray("records");
            verify(root,source);
            ArrayList<JSONObject> draft=new ArrayList<>(EXPECTED_COUNT);
            HashMap<String,Integer> nameIndex=new HashMap<>();
            for(int i=0;i<source.length();i++){
                JSONObject r=source.getJSONObject(i);
                draft.add(r);nameIndex.put(key(r.getString("s")),i);
            }
            ENTRIES.clear();ENTRIES.addAll(draft);
            BY_NAME.clear();BY_NAME.putAll(nameIndex);
            problem="";
        }catch(Exception e){
            ENTRIES.clear();BY_NAME.clear();
            problem="Book 01 content could not be verified. Reopen or retry the offline reference pack.";
        }
    }
    public static synchronized boolean reload(Context context){attempted=false;return ready(context);}
    public static synchronized boolean ready(Context context){load(context);return ENTRIES.size()==EXPECTED_COUNT;}
    public static synchronized String availabilityMessage(Context context){load(context);return problem.isEmpty()?"Book 01 · Greater Mindanao · Text v1.82":problem;}
    public static synchronized int count(Context context){load(context);return ENTRIES.size();}
    public static synchronized List<JSONObject> entries(Context context){load(context);return new ArrayList<>(ENTRIES);}
    public static synchronized boolean has(Context context,String scientific){load(context);return BY_NAME.containsKey(key(scientific));}
    private static synchronized JSONObject find(Context context,String scientific){load(context);Integer i=BY_NAME.get(key(scientific));return i==null?null:ENTRIES.get(i);}
    public static JSONArray bibliography(Context context){
        try{
            JSONObject book=new JSONObject(readCompressedJson(context,BIB,BIB_SHA256,500000));
            if(!"1.82".equals(book.getString("edition")))throw new IllegalStateException("Wrong bibliography version");
            JSONArray refs=book.getJSONArray("bibliography");
            if(refs.length()!=1159)throw new IllegalStateException("Wrong bibliography count");
            return refs;
        }catch(Exception ex){return new JSONArray();}
    }
    public static String sourceEdition(){return "Book 01 · Text v1.82 · Rarity v0.3.4";}

    private static void referencesDialog(Activity a,JSONArray refs){
        if(refs==null||refs.length()==0){
            new AlertDialog.Builder(a).setMessage("This account preserves its author–date citations in the article. Use the full book bibliography to inspect sources.")
                .setPositiveButton("References",(d,w)->a.startActivity(new Intent(a,PhLibraryBibliographyActivity.class)))
                .setNegativeButton("Close",null).show();return;
        }
        String[] titles=new String[refs.length()];
        for(int i=0;i<refs.length();i++){
            JSONObject r=refs.optJSONObject(i);
            titles[i]=r==null?"Unresolved citation":r.optString("short","Source");
        }
        new AlertDialog.Builder(a).setTitle("Account citations")
            .setItems(titles,(dialog,index)->{
                JSONObject source=refs.optJSONObject(index);if(source==null)return;
                String full=source.optString("full");
                String shortRef=source.optString("short");
                String text=full.trim().isEmpty()?shortRef:full;
                TextView body=label(a,text,15,UiKit.INK,false);
                body.setPadding(dp(a,20),dp(a,14),dp(a,20),dp(a,10));
                body.setTextIsSelectable(true);body.setAutoLinkMask(Linkify.WEB_URLS);
                body.setMovementMethod(LinkMovementMethod.getInstance());
                new AlertDialog.Builder(a).setTitle("Citation")
                    .setView(body)
                    .setNeutralButton("Find in bibliography",(d,w)->a.startActivity(new Intent(a,PhLibraryBibliographyActivity.class).putExtra("phlibrary_bib_citation",shortRef)))
                    .setPositiveButton("Close",null).show();
            })
            .setNeutralButton("Bibliography",(d,w)->a.startActivity(new Intent(a,PhLibraryBibliographyActivity.class)))
            .setNegativeButton("Close",null).show();
    }
    private static void addNavigation(Activity a,LinearLayout target,int index,Consumer<String> onName){
        boolean vertical=a.getResources().getConfiguration().screenWidthDp<350 || a.getResources().getConfiguration().fontScale>=1.6f;
        LinearLayout nav=new LinearLayout(a);nav.setOrientation(vertical?LinearLayout.VERTICAL:LinearLayout.HORIZONTAL);
        nav.setPadding(0,dp(a,14),0,dp(a,8));
        if(index>0){JSONObject prev=ENTRIES.get(index-1);
            Button b=UiKit.button(a,"Previous species","quiet");
            b.setContentDescription("Previous: "+prev.optString("s"));
            b.setOnClickListener(v->onName.accept(prev.optString("s")));
            nav.addView(b,vertical?new LinearLayout.LayoutParams(-1,dp(a,52)):new LinearLayout.LayoutParams(0,dp(a,52),1));}
        if(index<ENTRIES.size()-1){JSONObject next=ENTRIES.get(index+1);
            Button b=UiKit.button(a,"Next species","quiet");
            b.setContentDescription("Next: "+next.optString("s"));
            b.setOnClickListener(v->onName.accept(next.optString("s")));
            LinearLayout.LayoutParams lp=vertical?new LinearLayout.LayoutParams(-1,dp(a,52)):new LinearLayout.LayoutParams(0,dp(a,52),1);
            if(index>0){if(vertical)lp.topMargin=dp(a,8);else lp.leftMargin=dp(a,8);}
            nav.addView(b,lp);}
        target.addView(nav);
    }
    /** One continuous, verbatim Edition 1.82 narrative in an editorial reader. */
    public static boolean render(Activity a,LinearLayout parent,String scientific,Consumer<String> navigate){
        JSONObject record=find(a,scientific);
        if(record==null)return false;
        Integer pos=BY_NAME.get(key(scientific));if(pos==null)return false;
        boolean large=a.getResources().getConfiguration().fontScale>=1.6f;
        int edge=PhLibrary2Ui.dp(a,16);
        parent.removeAllViews();parent.setPadding(0,PhLibrary2Ui.dp(a,10),0,PhLibrary2Ui.dp(a,24));
        String common=record.optString("c");
        String headingText=common.isEmpty()?scientific:common;

        LinearLayout hero=new LinearLayout(a);hero.setOrientation(LinearLayout.VERTICAL);
        hero.setPadding(PhLibrary2Ui.dp(a,large?12:18),
            PhLibrary2Ui.dp(a,large?10:15),
            PhLibrary2Ui.dp(a,large?12:18),
            PhLibrary2Ui.dp(a,large?11:15));
        hero.setBackground(PhLibrary2Ui.bg(a,PhLibrary2Ui.DEEP,
            android.graphics.Color.TRANSPARENT,18));
        TextView heading=PhLibrary2Ui.text(a,headingText,
            large?16.5f:30,android.graphics.Color.WHITE,true,false);
        heading.setLineSpacing(PhLibrary2Ui.dp(a,1),1.02f);
        LinearLayout.LayoutParams h=new LinearLayout.LayoutParams(-1,-2);
        h.topMargin=0;hero.addView(heading,h);
        TextView latin=PhLibrary2Ui.text(a,record.optString("s"),
            large?15:18,0xFFE2EBD7,true,false);
        latin.setTypeface(Typeface.create("serif",Typeface.ITALIC));
        LinearLayout.LayoutParams l=new LinearLayout.LayoutParams(-1,-2);
        l.topMargin=PhLibrary2Ui.dp(a,6);hero.addView(latin,l);
        TextView taxonomy=PhLibrary2Ui.text(a,
            record.optString("o")+" · "+record.optString("f"),
            large?12:14,0xFFDEE9D8,false,false);
        LinearLayout.LayoutParams tx=new LinearLayout.LayoutParams(-1,-2);
        tx.topMargin=PhLibrary2Ui.dp(a,10);hero.addView(taxonomy,tx);
        parent.addView(hero,new LinearLayout.LayoutParams(-1,-2));
        PhLibrarySpeciesMedia.addReaderPlate(a,parent,scientific);

        boolean stack=a.getResources().getConfiguration().fontScale>=1.6f ||
            a.getResources().getConfiguration().screenWidthDp<350;
        LinearLayout statusRow=new LinearLayout(a);
        statusRow.setOrientation(stack?LinearLayout.VERTICAL:LinearLayout.HORIZONTAL);
        statusRow.setPadding(0,PhLibrary2Ui.dp(a,11),0,PhLibrary2Ui.dp(a,5));
        String global=record.isNull("iucn")?"Unassessed":record.optString("iucn","Unassessed");
        String tier=record.optString("t");
        TextView rarity=PhLibraryStatusBadge.makeCompact(a,tier,true);
        TextView iucn=PhLibraryStatusBadge.makeCompact(a,global,false);
        statusRow.addView(rarity,new LinearLayout.LayoutParams(stack?-1:0,-2,1));
        LinearLayout.LayoutParams ilp=new LinearLayout.LayoutParams(stack?-1:0,-2,1);
        if(stack)ilp.topMargin=PhLibrary2Ui.dp(a,8);
        else ilp.leftMargin=PhLibrary2Ui.dp(a,8);
        statusRow.addView(iucn,ilp);
        parent.addView(statusRow);
        String badges=record.optString("b").replace("↝","").replace("◆","")
            .replace("◉","").replace("✦","");
        int dot=badges.indexOf('•');
        String context=(dot>=0?badges.substring(0,dot):"").trim();
        if(!context.isEmpty()){
            TextView natural=PhLibrary2Ui.text(a,context,13,PhLibrary2Ui.FOREST,false,true);
            natural.setPadding(PhLibrary2Ui.dp(a,2),PhLibrary2Ui.dp(a,4),0,PhLibrary2Ui.dp(a,6));
            parent.addView(natural);
        }

        TextView save=PhLibrary2Ui.action(a,
            PhLibraryBookmarks.saved(a,scientific)?"✓  Saved":"☆  Save",
            ()->{},false);
        if(large){save.setTextSize(12);save.setPadding(PhLibrary2Ui.dp(a,8),
            PhLibrary2Ui.dp(a,5),PhLibrary2Ui.dp(a,8),PhLibrary2Ui.dp(a,5));}
        save.setContentDescription(PhLibraryBookmarks.saved(a,scientific)?
            "Remove species from saved collection":"Save species to collection");
        save.setOnClickListener(v->{
            boolean selected=PhLibraryBookmarks.toggle(a,scientific);
            save.setText(selected?"✓  Saved":"☆  Save");
            save.setContentDescription(selected?
                "Remove species from saved collection":"Save species to collection");
            save.announceForAccessibility(selected?"Species saved":"Species removed from collection");
        });
        LinearLayout.LayoutParams saveLp=new LinearLayout.LayoutParams(-2,PhLibrary2Ui.dp(a,48));
        saveLp.bottomMargin=PhLibrary2Ui.dp(a,10);parent.addView(save,saveLp);
        LinearLayout tools=new LinearLayout(a);tools.setGravity(android.view.Gravity.CENTER_VERTICAL);
        TextView original=PhLibrary2Ui.text(a,"Text size",13,
            PhLibrary2Ui.MUTED,false,false);
        tools.addView(original,new LinearLayout.LayoutParams(0,-2,1));
        TextView smaller=PhLibrary2Ui.action(a,"A−",()->{},false);
        TextView larger=PhLibrary2Ui.action(a,"A+",()->{},false);
        tools.addView(smaller,new LinearLayout.LayoutParams(PhLibrary2Ui.dp(a,51),
            PhLibrary2Ui.dp(a,48)));
        LinearLayout.LayoutParams plus=new LinearLayout.LayoutParams(PhLibrary2Ui.dp(a,51),
            PhLibrary2Ui.dp(a,48));plus.leftMargin=PhLibrary2Ui.dp(a,7);
        if(large){
            for(TextView adjust:new TextView[]{smaller,larger}){
                adjust.setTextSize(12);
                adjust.setPadding(PhLibrary2Ui.dp(a,7),PhLibrary2Ui.dp(a,5),
                    PhLibrary2Ui.dp(a,7),PhLibrary2Ui.dp(a,5));
            }
        }
        tools.addView(larger,plus);parent.addView(tools);

        TextView article=PhLibrary2Ui.text(a,record.optString("p"),17.5f,
            PhLibrary2Ui.INK,false,false);
        article.setLineSpacing(PhLibrary2Ui.dp(a,5),1.12f);
        article.setTextIsSelectable(true);
        article.setPadding(0,PhLibrary2Ui.dp(a,14),0,PhLibrary2Ui.dp(a,17));
        android.content.SharedPreferences prefs=a.getSharedPreferences("phlibrary_reader_v2",
            Context.MODE_PRIVATE);
        int initial=Math.max(-2,Math.min(4,prefs.getInt("font_step",0)));
        article.setTextSize(17.5f+initial);
        smaller.setOnClickListener(v->changeFont(a,prefs,article,-1));
        larger.setOnClickListener(v->changeFont(a,prefs,article,1));
        parent.addView(article,new LinearLayout.LayoutParams(-1,-2));



        JSONArray refs=record.optJSONArray("r");
        TextView refsAction=PhLibrary2Ui.action(a,
            "Citations ("+(refs==null?0:refs.length())+")",
            ()->referencesDialog(a,refs),false);
        refsAction.setContentDescription("Open account citations and bibliography");
        if(large){refsAction.setTextSize(12);refsAction.setPadding(PhLibrary2Ui.dp(a,8),
            PhLibrary2Ui.dp(a,5),PhLibrary2Ui.dp(a,8),PhLibrary2Ui.dp(a,5));}
        LinearLayout.LayoutParams citationLp=new LinearLayout.LayoutParams(-1,
            PhLibrary2Ui.dp(a,52));
        citationLp.topMargin=PhLibrary2Ui.dp(a,14);
        parent.addView(refsAction,citationLp);
        TextView explore=PhLibrary2Ui.action(a,"Explore connections",()->
            a.startActivity(new Intent(a,LivingAtlasExploreActivity.class)
                .putExtra(LivingAtlasExploreActivity.EXTRA_SCIENTIFIC_NAME,scientific)),true);
        LinearLayout.LayoutParams connectionsLp=new LinearLayout.LayoutParams(-1,PhLibrary2Ui.dp(a,52));
        connectionsLp.topMargin=PhLibrary2Ui.dp(a,9);
        parent.addView(explore,connectionsLp);
        TextView related=PhLibrary2Ui.text(a,"Related · "+record.optString("f"),
            24,PhLibrary2Ui.INK,true,false);
        related.setPadding(0,PhLibrary2Ui.dp(a,19),0,PhLibrary2Ui.dp(a,8));
        parent.addView(related);
        int listed=0;
        for(JSONObject r:ENTRIES){
            if(listed>=3)break;
            if(r.optString("s").equals(scientific) ||
                !r.optString("f").equals(record.optString("f")))continue;
            final String name=r.optString("s");
            TextView more=PhLibrary2Ui.action(a,r.optString("c")+
                " · "+name,()->navigate.accept(name),false);
            LinearLayout.LayoutParams mlp=new LinearLayout.LayoutParams(-1,-2);
            mlp.bottomMargin=PhLibrary2Ui.dp(a,7);parent.addView(more,mlp);
            listed++;
        }
        TextView edition=PhLibrary2Ui.text(a,sourceEdition(),12,
            PhLibrary2Ui.MUTED,false,false);
        edition.setPadding(0,PhLibrary2Ui.dp(a,10),0,PhLibrary2Ui.dp(a,8));
        parent.addView(edition);
        addNavigation(a,parent,pos,navigate);
        return true;
    }

    private static void changeFont(Activity a,android.content.SharedPreferences prefs,
                                   TextView article,int delta){
        int old=prefs.getInt("font_step",0);
        int now=Math.max(-2,Math.min(4,old+delta));
        article.setTextSize(17.5f+now);
        prefs.edit().putInt("font_step",now).apply();
    }
}
