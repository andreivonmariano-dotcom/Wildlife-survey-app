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

/** Offline, source-snapshot review register. No automated expert approvals. */
public final class Rc12ScientificReview {
    private Rc12ScientificReview(){}
    private static JSONObject registry;
    private static final String[] FIELDS={
        "taxonomy","diagnosis","differential","sex","life_stage","measurements",
        "habitat","range","behavior","threat","media","review"
    };
    private static final String[] LABELS={
        "Taxonomy","Morphology","Lookalike comparisons","Sex differences",
        "Age and life stages","Measurements","Habitat","Distribution",
        "Natural history","Dated IUCN & DENR","Photo identification & rights",
        "Independent specialist approval"
    };
    private static int dp(Activity a,int amount){
        return Math.round(amount*a.getResources().getDisplayMetrics().density);
    }
    private static synchronized JSONObject register(Activity a){
        if(registry!=null)return registry;
        try(InputStream input=a.getAssets().open("rc12_species_scientific_review_register_v1.json");
            ByteArrayOutputStream out=new ByteArrayOutputStream()){
            byte[] buf=new byte[8192];int count;
            while((count=input.read(buf))!=-1){
                if(out.size()+count>200000)throw new IllegalStateException("Review file oversized");
                out.write(buf,0,count);
            }
            JSONObject parsed=new JSONObject(out.toString("UTF-8"));
            if(!"rc12-species-scientific-review-register-v1".equals(
                parsed.optString("schemaVersion"))||
                parsed.optInt("profileCount")!=96||
                parsed.optInt("expertApprovedCount",-1)!=0)
                throw new IllegalArgumentException("Unknown review register");
            registry=parsed;
        }catch(Exception ex){registry=new JSONObject();}
        return registry;
    }
    private static TextView label(Activity a,String str,int size,boolean bold){
        TextView text=new TextView(a);
        text.setText(str);text.setTextSize(size);
        text.setTextColor(bold?UiKit.FOREST:UiKit.MUTED);
        if(bold)text.setTypeface(null,Typeface.BOLD);
        text.setPadding(dp(a,3),dp(a,4),dp(a,3),dp(a,4));
        return text;
    }
    public static void add(Activity a,LinearLayout parent,String scientific){
        JSONArray all=register(a).optJSONArray("profiles");
        if(all==null)return;
        JSONObject record=null;
        for(int i=0;i<all.length();i++){
            JSONObject p=all.optJSONObject(i);
            if(p!=null&&scientific.equalsIgnoreCase(p.optString("scientificName"))){
                record=p;break;
            }
        }
        if(record==null)return;
        JSONObject evidence=record.optJSONObject("fieldEvidence");
        if(evidence==null)return;
        LinearLayout panel=new LinearLayout(a);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setVisibility(View.GONE);
        int available=0;
        for(int i=0;i<FIELDS.length;i++){
            JSONObject area=evidence.optJSONObject(FIELDS[i]);
            boolean present=area!=null&&area.optInt("existingEvidenceCount",0)>0;
            if(present)available++;
            String state=present?"Source recorded · review pending":"Source not assessed";
            panel.addView(label(a,LABELS[i]+" · "+state,12,false));
        }
        JSONArray original=record.optJSONArray("originalDescriptionClaimIds");
        int originalCount=original==null?0:original.length();
        if(originalCount>0)
            panel.addView(label(a,originalCount+
                " original-description research notes available; specialist approval pending",12,false));
        panel.addView(label(a,"Specialist sign-off: pending",12,true));
        Button toggle=UiKit.button(a,"Scientific checklist  ▾","quiet");
        toggle.setContentDescription("Show source coverage for 12 specialist-review areas");
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(a,48));
        lp.topMargin=dp(a,8);parent.addView(toggle,lp);
        parent.addView(panel);
        toggle.setOnClickListener(v->{
            boolean open=panel.getVisibility()!=View.VISIBLE;
            panel.setVisibility(open?View.VISIBLE:View.GONE);
            toggle.setText(open?"Hide scientific checklist  ▴":"Scientific checklist  ▾");
        });
    }
}
