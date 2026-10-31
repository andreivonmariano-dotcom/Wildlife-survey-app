package org.phwildlife.survey;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.graphics.Typeface;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.util.Locale;

/**
 * Literature evidence is an independently versioned read-only layer.
 * The source list is not a detection record and is not a specialist-approved key.
 */
public final class Rc7LiteratureNotes {
    private Rc7LiteratureNotes(){}
    private static JSONArray entries;
    private static java.util.HashSet<String> names;
    private static final String[] FILES={
        "rc7_literature_birds_amphibians_v1.json",
        "rc7_literature_reptiles_mammals_v1.json",
        "rc7_literature_batch_b_v1.json"
    };
    private static int dp(Activity a,int v){
        return Math.round(v*a.getResources().getDisplayMetrics().density);
    }
    private static synchronized JSONArray entries(Activity a){
        if(entries!=null)return entries;
        JSONArray all=new JSONArray();
        for(String file:FILES){
            try(InputStream in=a.getAssets().open(file);
                ByteArrayOutputStream buffer=new ByteArrayOutputStream()){
                byte[] read=new byte[8192];int n;
                while((n=in.read(read))!=-1){
                    if(buffer.size()+n>200000)throw new IllegalArgumentException("Guide file too large");
                    buffer.write(read,0,n);
                }
                JSONObject pack=new JSONObject(buffer.toString("UTF-8"));
                if(!"literature_synthesis_pending_specialist_review".equals(pack.optString("status")))
                    continue;
                JSONArray records=pack.getJSONArray("records");
                for(int i=0;i<records.length();i++){
                    JSONObject account=records.optJSONObject(i);
                    if(account!=null)all.put(account);
                }
            }catch(Exception ignored){}
        }
        entries=all;
        names=new java.util.HashSet<>();
        for(int i=0;i<all.length();i++){
            JSONObject p=all.optJSONObject(i);
            if(p!=null)names.add(p.optString("scientificName")
                .toLowerCase(Locale.ROOT));
        }
        return entries;
    }
    private static JSONObject find(Activity a,String scientific){
        if(scientific==null)return null;
        JSONArray all=entries(a);
        for(int i=0;i<all.length();i++){
            JSONObject entry=all.optJSONObject(i);
            if(entry!=null&&scientific.equalsIgnoreCase(entry.optString("scientificName")))
                return entry;
        }
        return null;
    }
    public static boolean available(Activity a,String scientific){
        entries(a);
        return scientific!=null&&names!=null&&names.contains(
            scientific.toLowerCase(Locale.ROOT));
    }
    public static int count(Activity a){return entries(a).length();}
    private static TextView label(Activity a,String value,int sp,boolean bold){
        TextView t=new TextView(a);
        t.setText(value);t.setTextSize(sp);t.setTextColor(bold?UiKit.FOREST:UiKit.INK);
        if(bold)t.setTypeface(null,Typeface.BOLD);
        t.setLineSpacing(dp(a,2),1.08f);
        t.setPadding(dp(a,4),dp(a,bold?10:3),dp(a,4),dp(a,5));
        t.setTextIsSelectable(true);
        return t;
    }
    public static boolean add(Activity a,LinearLayout parent,String scientific){
        JSONObject entry=find(a,scientific);
        if(entry==null)return false;
        boolean identityOnly="source_taxonomy_reviewed_biology_pending".equals(
            entry.optString("reviewStatus"));
        parent.addView(FieldVisuals.section(a,
            identityOnly?"Taxonomic source and research notes":"Published species information",
            identityOnly?"Checklist-backed identity · species ecology still unreviewed":
                "Literature cited · identification still needs specialist validation"));
        JSONArray facts=entry.optJSONArray("facts");
        if(facts!=null){
            for(int i=0;i<facts.length();i++){
                JSONObject fact=facts.optJSONObject(i);
                if(fact==null)continue;
                parent.addView(label(a,fact.optString("field"),13,true));
                parent.addView(label(a,fact.optString("value"),15,false));
            }
        }
        String caution=entry.optString("caution");
        if(!caution.isEmpty()){
            parent.addView(label(a,"Identification and locality caution",13,true));
            parent.addView(label(a,caution,14,false));
        }
        Button cite=UiKit.button(a,"Literature citation and source  ▾","quiet");
        parent.addView(cite,new LinearLayout.LayoutParams(-1,dp(a,48)));
        LinearLayout references=new LinearLayout(a);
        references.setOrientation(LinearLayout.VERTICAL);
        references.setVisibility(View.GONE);
        String source=entry.optString("sourceTitle");
        references.addView(label(a,source,14,false));
        String url=entry.optString("sourceUrl");
        references.addView(label(a,url,12,false));
        Button open=UiKit.button(a,"Open original publication","secondary");
        open.setOnClickListener(v->{
            if(url.startsWith("https://"))
                a.startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url)));
        });
        references.addView(open);
        parent.addView(references);
        cite.setOnClickListener(v->{
            boolean visible=references.getVisibility()!=View.VISIBLE;
            references.setVisibility(visible?View.VISIBLE:View.GONE);
            cite.setText(visible?"Literature citation and source  ▴":
                "Literature citation and source  ▾");
        });
        return true;
    }
}
