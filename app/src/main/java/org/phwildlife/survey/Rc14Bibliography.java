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

/** One DOI-linked bibliography for each priority species, retaining all older references. */
public final class Rc14Bibliography {
    private Rc14Bibliography(){}
    private static JSONObject index;
    private static int dp(Activity a,int px){
        return Math.round(px*a.getResources().getDisplayMetrics().density);
    }
    private static synchronized JSONObject load(Activity a){
        if(index!=null)return index;
        try(InputStream in=a.getAssets().open("rc14_96_species_literature_index_v1.json");
            ByteArrayOutputStream out=new ByteArrayOutputStream()){
            byte[] buffer=new byte[8192];int n;
            while((n=in.read(buffer))!=-1){
                if(out.size()+n>140000)throw new IllegalArgumentException("Bibliography too large");
                out.write(buffer,0,n);
            }
            JSONObject obj=new JSONObject(out.toString("UTF-8"));
            if(!"rc14-96-species-literature-index-v1".equals(obj.optString("schemaVersion"))
                ||obj.optInt("profileCount")!=96)throw new IllegalArgumentException("Unknown bibliography");
            index=obj;
        }catch(Exception ignored){index=new JSONObject();}
        return index;
    }
    private static TextView text(Activity a,String value,boolean title){
        TextView t=new TextView(a);
        t.setText(value);t.setTextSize(title?13:12);
        t.setTextColor(title?UiKit.INK:UiKit.MUTED);
        if(title)t.setTypeface(null,Typeface.BOLD);
        t.setTextIsSelectable(true);
        return t;
    }
    private static void show(Activity a,LinearLayout where,JSONObject paper){
        String citation=paper.optString("source").trim();
        String url=paper.optString("url").trim();
        if(citation.isEmpty()||!url.startsWith("https://"))return;
        LinearLayout item=new LinearLayout(a);item.setOrientation(LinearLayout.VERTICAL);
        item.setPadding(dp(a,4),dp(a,8),dp(a,4),dp(a,8));
        if(paper.optBoolean("latestConfirmed"))
            item.addView(text(a,"2025–26 research",false));
        item.addView(text(a,citation,true));
        Button open=UiKit.button(a,
            url.startsWith("https://doi.org/")?"Open study ↗":"Open source ↗","quiet");
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(a,43));
        lp.topMargin=dp(a,3);item.addView(open,lp);
        open.setContentDescription("Open original reference: "+citation);
        open.setOnClickListener(v->a.startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url))));
        where.addView(item);
    }
    public static void add(Activity a,LinearLayout parent,String scientific){
        JSONArray profiles=load(a).optJSONArray("profiles");
        if(profiles==null)return;
        JSONArray refs=null;
        for(int i=0;i<profiles.length();i++){
            JSONObject p=profiles.optJSONObject(i);
            if(p!=null&&scientific.equalsIgnoreCase(p.optString("scientificName"))){
                refs=p.optJSONArray("publications");break;
            }
        }
        if(refs==null)return;
        parent.addView(text(a,"Research references · "+refs.length(),true));
        LinearLayout more=new LinearLayout(a);more.setOrientation(LinearLayout.VERTICAL);
        more.setVisibility(View.GONE);
        for(int i=0;i<refs.length();i++){
            JSONObject entry=refs.optJSONObject(i);
            if(entry==null)continue;
            show(a,i<3?parent:more,entry);
        }
        if(refs.length()>3){
            final int remaining=refs.length()-3;
            Button toggle=UiKit.button(a,"Show "+remaining+" more sources  ▾","quiet");
            parent.addView(toggle,new LinearLayout.LayoutParams(-1,dp(a,46)));
            parent.addView(more);
            toggle.setOnClickListener(v->{
                boolean open=more.getVisibility()!=View.VISIBLE;
                more.setVisibility(open?View.VISIBLE:View.GONE);
                toggle.setText(open?"Show fewer sources  ▴":
                    "Show "+remaining+" more sources  ▾");
            });
        }
    }
}
