package org.phwildlife.survey;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashSet;

/** A full, cited habitat profile reached from the horizontal ecological deck. */
public final class LikasHabitatProfileActivity extends Activity {
    private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
    private TextView text(String value,int size,boolean bold){
        return LikasPortalUi.text(this,value,size,bold?UiKit.INK:UiKit.MUTED,bold);
    }
    private void section(LinearLayout parent,String title,String body){
        if(body==null||body.isEmpty())return;
        parent.addView(FieldVisuals.section(this,title,null));
        TextView content=text(body,14,false);
        content.setTextColor(UiKit.INK);
        content.setLineSpacing(dp(3),1.03f);
        content.setPadding(dp(6),dp(5),dp(6),dp(12));
        parent.addView(content);
    }
    private JSONObject read()throws Exception{
        try(InputStream in=getAssets().open("offline_habitat_profiles_v1.json");
            ByteArrayOutputStream out=new ByteArrayOutputStream()){
            byte[] buffer=new byte[8192];int n;
            while((n=in.read(buffer))!=-1){
                if(out.size()+n>220000)throw new IllegalStateException("Habitat source oversized");
                out.write(buffer,0,n);
            }
            return new JSONObject(out.toString("UTF-8"));
        }
    }
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        JSONObject pack,profile=null;
        try{
            pack=read();
            String requested=getIntent().getStringExtra("habitat_id");
            JSONArray profiles=pack.getJSONArray("profiles");
            for(int i=0;i<profiles.length();i++){
                JSONObject value=profiles.getJSONObject(i);
                if(value.optString("id").equals(requested)){profile=value;break;}
            }
            if(profile==null)throw new IllegalStateException("Unknown habitat reference");
        }catch(Exception e){
            new android.app.AlertDialog.Builder(this).setTitle("Habitat unavailable")
                .setMessage("This habitat has no reviewed offline profile.")
                .setPositiveButton("Close",(d,w)->finish()).show();
            return;
        }
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(UiKit.BG);
        root.setPadding(dp(15),dp(10),dp(15),dp(8));
        root.addView(UiKit.navigationHeader(this,"Habitat","Habitats",this::finish));
        ScrollView scroll=new ScrollView(this);
        LinearLayout content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(0,dp(10),0,dp(22));
        scroll.addView(content);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        content.addView(LikasPortalUi.kicker(this,profile.optString("group").toUpperCase()));
        TextView heading=text(profile.optString("name"),25,true);
        content.addView(heading);
        OfflinePhotoEvidence.add(this,content,profile.optString("photoId"));
        section(content,"Overview",profile.optString("overview"));
        section(content,"Field signs",profile.optString("fieldIndicators"));
        section(content,"Water & soils",profile.optString("waterAndSubstrate"));
        section(content,"Vegetation",profile.optString("vegetationAndWildlife"));
        JSONArray measurements=profile.optJSONArray("measurements");
        if(measurements!=null&&measurements.length()>0){
            StringBuilder list=new StringBuilder();
            for(int i=0;i<measurements.length();i++){
                if(i>0)list.append("\n");
                list.append("• ").append(measurements.optString(i));
            }
            section(content,"Record",list.toString());
        }
        section(content,"Limits",profile.optString("limitations"));
        JSONArray src=pack.optJSONArray("sources");
        JSONArray refs=profile.optJSONArray("sourceIds");
        if(src!=null&&refs!=null){
            content.addView(FieldVisuals.section(this,"Sources",null));
            HashSet<String> used=new HashSet<>();
            for(int i=0;i<refs.length();i++){
                String id=refs.optString(i);
                if(!used.add(id))continue;
                for(int k=0;k<src.length();k++){
                    JSONObject j=src.optJSONObject(k);
                    if(j==null||!id.equals(j.optString("id")))continue;
                    String url=j.optString("url");
                    Button button=UiKit.button(this,j.optString("publisher")+" · Source","secondary");
                    if(url.startsWith("https://"))button.setOnClickListener(v->
                        startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url))));
                    else button.setEnabled(false);
                    LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(62));
                    lp.bottomMargin=dp(7);content.addView(button,lp);
                    break;
                }
            }
        }
        TextView caveat=text("Habitat references are not site measurements or species detections.",12,false);
        caveat.setPadding(0,dp(12),0,dp(12));content.addView(caveat);
        setContentView(root);UiMotion.enter(this,content);
    }
}
