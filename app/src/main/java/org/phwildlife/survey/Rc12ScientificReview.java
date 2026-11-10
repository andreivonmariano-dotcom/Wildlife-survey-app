package org.phwildlife.survey;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import org.json.JSONArray;
import org.json.JSONObject;

/** Cited publication coverage, read-only and separate from field observations. */
public final class Rc12ScientificReview {
    private Rc12ScientificReview(){}
    private static JSONObject registry;
    private static final String[] FIELDS={
        "taxonomy","diagnosis","differential","sex","life_stage","measurements",
        "habitat","range","behavior","threat","media"
    };
    private static final String[] LABELS={
        "Taxonomy","Identification","Similar species","Sex variation",
        "Life stages","Body measurements","Habitat","Distribution",
        "Ecology and calls","Conservation","Photographs"
    };
    private static int dp(Activity a,int n){
        return Math.round(n*a.getResources().getDisplayMetrics().density);
    }
    private static synchronized JSONObject register(Activity a){
        if(registry!=null)return registry;
        try(InputStream in=a.getAssets().open("rc13_publication_coverage_v1.json");
            ByteArrayOutputStream out=new ByteArrayOutputStream()){
            byte[] buf=new byte[8192];int n;
            while((n=in.read(buf))!=-1){
                if(out.size()+n>200000)throw new IllegalArgumentException("Bibliography too large");
                out.write(buf,0,n);
            }
            JSONObject data=new JSONObject(out.toString("UTF-8"));
            if(!"rc13-publication-coverage-v1".equals(data.optString("schemaVersion"))
                ||data.optInt("profileCount")!=96)
                throw new IllegalArgumentException("Unexpected source catalog");
            registry=data;
        }catch(Exception ignored){registry=new JSONObject();}
        return registry;
    }
    private static TextView text(Activity a,String value,boolean strong){
        TextView t=new TextView(a);
        t.setText(value);t.setTextSize(strong?13:12);
        t.setTextColor(strong?UiKit.FOREST:UiKit.MUTED);
        t.setPadding(dp(a,3),dp(a,5),dp(a,3),dp(a,5));
        if(strong)t.setTypeface(null,Typeface.BOLD);
        return t;
    }
    public static void add(Activity a,LinearLayout parent,String scientific){
        JSONArray list=register(a).optJSONArray("profiles");
        if(list==null)return;
        JSONObject row=null;
        for(int i=0;i<list.length();i++){
            JSONObject item=list.optJSONObject(i);
            if(item!=null&&scientific.equalsIgnoreCase(item.optString("scientificName"))){
                row=item;break;
            }
        }
        if(row==null)return;
        JSONObject fields=row.optJSONObject("fields");
        if(fields==null)return;
        LinearLayout details=new LinearLayout(a);
        details.setOrientation(LinearLayout.VERTICAL);
        details.setVisibility(View.GONE);
        int added=row.optInt("publicationClaimCount");
        if(added>0)
            details.addView(text(a,added+" individually cited study notes",true));
        for(int i=0;i<FIELDS.length;i++){
            JSONObject record=fields.optJSONObject(FIELDS[i]);
            if(record==null)continue;
            int previous=record.optInt("previousSourceCount");
            JSONArray citations=record.optJSONArray("addedPublicationClaims");
            int newCount=citations==null?0:citations.length();
            if(previous==0&&newCount==0)continue;
            String count=newCount>0?newCount+" publication notes":"published reference";
            details.addView(text(a,LABELS[i]+" · "+count,false));
        }
        Button toggle=UiKit.button(a,"Literature coverage  ▾","quiet");
        toggle.setContentDescription("Expand cited research coverage");
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(a,46));
        p.topMargin=dp(a,7);
        parent.addView(toggle,p);
        parent.addView(details);
        toggle.setOnClickListener(v->{
            boolean open=details.getVisibility()!=View.VISIBLE;
            details.setVisibility(open?View.VISIBLE:View.GONE);
            toggle.setText(open?"Hide literature coverage  ▴":"Literature coverage  ▾");
        });
    }
}
