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
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/** Source-cited research additions. Does not modify taxa, occurrence or survey records. */
public final class Rc10ResearchBatch {
    private Rc10ResearchBatch(){}
    private static JSONObject source;
    private static JSONObject secondSource;
    private static int dp(Activity a,int v){
        return Math.round(a.getResources().getDisplayMetrics().density*v);
    }
    private static JSONObject readBatch(Activity a,String file){
        try(InputStream in=a.getAssets().open(file);
            ByteArrayOutputStream out=new ByteArrayOutputStream()){
            byte[] bytes=new byte[4096];int n;
            while((n=in.read(bytes))!=-1){
                if(out.size()+n>65000)throw new IllegalArgumentException("Research batch too large");
                out.write(bytes,0,n);
            }
            JSONObject pack=new JSONObject(out.toString("UTF-8"));
            if(!"rc10-research-batch-v1".equals(pack.optString("schemaVersion")))
                throw new IllegalArgumentException("Unknown source edition");
            return pack;
        }catch(Exception ignored){return new JSONObject();}
    }
    private static synchronized void load(Activity a){
        if(source==null)source=readBatch(a,"rc10_research_batch1_v1.json");
        if(secondSource==null)secondSource=readBatch(a,"rc10_research_batch2_v1.json");
    }
    private static JSONObject forTaxon(Activity a,String scientific){
        if(scientific==null)return null;
        load(a);
        for(JSONObject batch:new JSONObject[]{source,secondSource}){
            JSONArray list=batch.optJSONArray("records");
            if(list==null)continue;
            for(int i=0;i<list.length();i++){
                JSONObject row=list.optJSONObject(i);
                if(row!=null&&scientific.equalsIgnoreCase(row.optString("scientificName")))
                    return row;
            }
        }
        return null;
    }
    private static TextView label(Activity a,String value,int sp,boolean heading){
        TextView label=new TextView(a);
        label.setText(value);
        label.setTextSize(sp);
        label.setTextColor(heading?UiKit.FOREST:UiKit.INK);
        if(heading)label.setTypeface(null,Typeface.BOLD);
        label.setTextIsSelectable(true);
        label.setPadding(dp(a,5),dp(a,4),dp(a,5),dp(a,7));
        return label;
    }
    public static void add(Activity a,LinearLayout parent,String scientific){
        JSONObject record=forTaxon(a,scientific);
        if(record==null)return;
        JSONArray facts=record.optJSONArray("items");
        if(facts==null||facts.length()==0)return;
        parent.addView(FieldVisuals.section(a,"Expanded field research",
            facts.length()+" cited notes · published descriptions, not certified identification"));
        Button show=UiKit.button(a,"Read expanded field notes  ▾","secondary");
        parent.addView(show,new LinearLayout.LayoutParams(-1,dp(a,48)));
        LinearLayout list=new LinearLayout(a);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setVisibility(View.GONE);
        parent.addView(list);
        list.addView(label(a,"Independent zoological review pending. Source records are not local occurrence evidence.",13,false));
        for(int i=0;i<facts.length();i++){
            JSONObject item=facts.optJSONObject(i);
            if(item==null)continue;
            String url=item.optString("url");
            if(!url.startsWith("https://"))continue;
            LinearLayout card=new LinearLayout(a);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(a,10),dp(a,8),dp(a,10),dp(a,10));
            card.setBackground(UiKit.surface(a));
            card.addView(label(a,item.optString("category")+" · "+item.optString("label"),14,true));
            card.addView(label(a,item.optString("statement"),14,false));
            card.addView(label(a,"Field limitation: "+item.optString("limitation"),12,false));
            String extra=item.has("sourceType")?
                "\nSource type: "+item.optString("sourceType")+
                " · "+item.optInt("publicationYear")+
                "\nAccessed: "+item.optString("retrievedDate"):"";
            card.addView(label(a,"Source: "+item.optString("source")+extra+
                "\nReview: "+item.optString("reviewState"),12,false));
            Button reference=UiKit.button(a,"Read original reference","quiet");
            card.addView(reference,new LinearLayout.LayoutParams(-1,dp(a,44)));
            reference.setOnClickListener(v->a.startActivity(
                new Intent(Intent.ACTION_VIEW,Uri.parse(url))));
            LinearLayout.LayoutParams box=new LinearLayout.LayoutParams(-1,-2);
            box.bottomMargin=dp(a,8);
            list.addView(card,box);
        }
        show.setOnClickListener(v->{
            boolean expanded=list.getVisibility()!=View.VISIBLE;
            list.setVisibility(expanded?View.VISIBLE:View.GONE);
            show.setText(expanded?"Hide expanded field notes  ▴":"Read expanded field notes  ▾");
        });
    }
}
