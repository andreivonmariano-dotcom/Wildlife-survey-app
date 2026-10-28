package org.phwildlife.survey;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashSet;

/** Detailed offline reference descriptions never mutate field detections or locations. */
public final class OfflineFieldLibrary {
    private OfflineFieldLibrary(){}
    private static int dp(Activity a,int n){
        return Math.round(n*a.getResources().getDisplayMetrics().density);
    }
    private static JSONObject read(Activity a,String filename){
        try(InputStream in=a.getAssets().open(filename)){
            ByteArrayOutputStream out=new ByteArrayOutputStream();
            byte[] buf=new byte[4096];int n;
            while((n=in.read(buf))>0){
                if(out.size()+n>250000)throw new IllegalStateException("Reference too large");
                out.write(buf,0,n);
            }
            return new JSONObject(out.toString("UTF-8"));
        }catch(Exception ignored){return new JSONObject();}
    }
    private static TextView text(Activity a,String value,boolean heading){
        TextView label=new TextView(a);
        label.setText(value);
        label.setTextSize(heading?15:13);
        label.setTextColor(heading?UiKit.FOREST:UiKit.INK);
        label.setPadding(dp(a,4),dp(a,heading?10:3),dp(a,4),dp(a,3));
        if(heading)label.setTypeface(null,Typeface.BOLD);
        label.setTextIsSelectable(true);
        return label;
    }
    private static void paragraph(Activity a,LinearLayout out,String name,String body){
        if(body==null||body.trim().isEmpty())return;
        out.addView(text(a,name,true));
        out.addView(text(a,body,false));
    }
    private static JSONObject source(JSONArray sources,String id){
        if(sources==null)return null;
        for(int i=0;i<sources.length();i++){
            JSONObject src=sources.optJSONObject(i);
            if(src!=null&&id.equals(src.optString("id")))return src;
        }
        return null;
    }
    private static void citationList(Activity a,LinearLayout target,
            JSONArray sources,JSONArray ids){
        if(ids==null||sources==null)return;
        HashSet<String> used=new HashSet<>();
        target.addView(text(a,"References and source editions",true));
        for(int i=0;i<ids.length();i++){
            String id=ids.optString(i);
            if(!used.add(id))continue;
            JSONObject src=source(sources,id);
            if(src==null)continue;
            String title=src.optString("title");
            TextView citation=text(a,src.optString("publisher")+
                " · "+title+"\nAccessed "+src.optString("accessed")+
                "\n"+src.optString("url"),false);
            citation.setTextColor(UiKit.MUTED);
            citation.setAutoLinkMask(android.text.util.Linkify.WEB_URLS);
            citation.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());
            target.addView(citation);
        }
    }
    /** Source checked only; absent account is clearly labeled, not populated by guesswork. */
    public static boolean addSpecies(Activity a,LinearLayout target,String scientific){
        JSONObject bundle=read(a,"diagnostic_profiles_v1.json");
        JSONArray list=bundle.optJSONArray("profiles");
        if(list==null)return false;
        JSONObject profile=null;
        for(int i=0;i<list.length();i++){
            JSONObject entry=list.optJSONObject(i);
            if(entry!=null&&scientific.equalsIgnoreCase(entry.optString("scientificName"))){
                profile=entry;break;
            }
        }
        if(profile==null)return false;
        JSONObject account=profile.optJSONObject("account");
        if(account==null)return false;
        target.addView(FieldVisuals.section(a,"Field identification and ecology",
            "Source-checked reference account, not a verified station observation"));
        OfflinePhotoEvidence.add(a,target,account.optString("photoId"));
        paragraph(a,target,"Description",account.optString("overview"));
        paragraph(a,target,"Habitat and microhabitat",account.optString("habitat"));
        paragraph(a,target,"Natural history and activity",account.optString("behavior"));
        paragraph(a,target,"Useful field documentation",account.optString("fieldNotes"));
        paragraph(a,target,"Identification cautions",account.optString("cautions"));
        citationList(a,target,bundle.optJSONArray("sourceList"),
            account.optJSONArray("sourceIds"));
        return true;
    }
    /** Display nine evidence-linked habitat descriptions in collapsed cards. */
    public static void addHabitats(Activity a,LinearLayout target){
        JSONObject pack=read(a,"offline_habitat_profiles_v1.json");
        JSONArray profiles=pack.optJSONArray("profiles");
        if(profiles==null){
            target.addView(text(a,"Habitat reference pack unavailable offline.",false));
            return;
        }
        target.addView(FieldVisuals.section(a,"Philippine Habitat Library",
            profiles.length()+" source-linked profiles · tap a habitat to read"));
        for(int i=0;i<profiles.length();i++){
            JSONObject profile=profiles.optJSONObject(i);
            if(profile==null)continue;
            LinearLayout card=new LinearLayout(a);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(a,9),dp(a,8),dp(a,9),dp(a,8));
            card.setBackground(UiKit.surface(a));
            Button toggle=UiKit.button(a,profile.optString("name")+"  ▾","quiet");
            toggle.setContentDescription("Show "+profile.optString("name")+" reference details");
            card.addView(toggle,new LinearLayout.LayoutParams(-1,dp(a,52)));
            TextView teaser=text(a,profile.optString("overview"),false);
            teaser.setMaxLines(3);card.addView(teaser);
            LinearLayout detail=new LinearLayout(a);
            detail.setOrientation(LinearLayout.VERTICAL);
            detail.setVisibility(View.GONE);
            OfflinePhotoEvidence.add(a,detail,profile.optString("photoId"));
            paragraph(a,detail,"Habitat description",profile.optString("overview"));
            paragraph(a,detail,"Field recognition",profile.optString("fieldIndicators"));
            paragraph(a,detail,"Water, geology and substrate",
                profile.optString("waterAndSubstrate"));
            paragraph(a,detail,"Vegetation and wildlife context",
                profile.optString("vegetationAndWildlife"));
            JSONArray metrics=profile.optJSONArray("measurements");
            if(metrics!=null){
                StringBuilder measurements=new StringBuilder();
                for(int n=0;n<metrics.length();n++){
                    if(n>0)measurements.append("\n");
                    measurements.append("• ").append(metrics.optString(n));
                }
                paragraph(a,detail,"Recommended field measurements",measurements.toString());
            }
            paragraph(a,detail,"Scientific cautions",profile.optString("limitations"));
            citationList(a,detail,pack.optJSONArray("sources"),
                profile.optJSONArray("sourceIds"));
            card.addView(detail);
            String name=profile.optString("name");
            toggle.setOnClickListener(v->{
                boolean show=detail.getVisibility()!=View.VISIBLE;
                detail.setVisibility(show?View.VISIBLE:View.GONE);
                teaser.setVisibility(show?View.GONE:View.VISIBLE);
                toggle.setText(name+(show?"  ▴":"  ▾"));
                toggle.setContentDescription((show?"Hide ":"Show ")+name+" reference details");
            });
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
            lp.bottomMargin=dp(a,9);
            target.addView(card,lp);
        }
    }
}
