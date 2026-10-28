package org.phwildlife.survey;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;

/** Evidence-readiness status, never a self-declared validated species key. */
public final class Rc9ProfileCompleteness {
    private Rc9ProfileCompleteness(){}
    private static JSONObject pack;
    private static int dp(Activity a,int n){return Math.round(n*a.getResources().getDisplayMetrics().density);}
    private static synchronized JSONObject load(Activity a){
        if(pack!=null)return pack;
        try(InputStream in=a.getAssets().open("rc9_profile_completion_v1.json");
            ByteArrayOutputStream out=new ByteArrayOutputStream()){
            byte[] b=new byte[8192];int n;
            while((n=in.read(b))!=-1){
                if(out.size()+n>180000)throw new IllegalArgumentException("Profile contract too large");
                out.write(b,0,n);
            }
            JSONObject content=new JSONObject(out.toString("UTF-8"));
            if(!"rc9-profile-completion-v1".equals(content.optString("schemaVersion")))
                throw new IllegalArgumentException("Unknown completion contract");
            pack=content;
        }catch(Exception ignored){pack=new JSONObject();}
        return pack;
    }
    public static JSONObject find(Activity a,String scientific){
        JSONArray rows=load(a).optJSONArray("profiles");
        if(rows==null||scientific==null)return null;
        for(int i=0;i<rows.length();i++){
            JSONObject row=rows.optJSONObject(i);
            if(row!=null&&scientific.equalsIgnoreCase(row.optString("scientificName")))return row;
        }
        return null;
    }
    private static TextView line(Activity a,String value,int size,boolean bold){
        TextView t=new TextView(a);t.setText(value);t.setTextSize(size);
        t.setTextColor(bold?UiKit.FOREST:UiKit.INK);
        if(bold)t.setTypeface(null,Typeface.BOLD);
        t.setPadding(dp(a,5),dp(a,3),dp(a,5),dp(a,6));return t;
    }
    public static void add(Activity a,LinearLayout parent,String scientific){
        JSONObject profile=find(a,scientific);
        if(profile==null)return;
        JSONObject statuses=profile.optJSONObject("requirements");
        JSONArray definitions=load(a).optJSONArray("requirementDefinitions");
        if(statuses==null||definitions==null)return;
        int evidenced=0;
        for(int i=0;i<definitions.length();i++){
            JSONObject field=definitions.optJSONObject(i);
            if(field==null)continue;
            JSONObject val=statuses.optJSONObject(field.optString("id"));
            if(val!=null&&!"not_assessed".equals(val.optString("status")))evidenced++;
        }
        parent.addView(FieldVisuals.section(a,"Profile coverage",
            evidenced+" / "+definitions.length()+" areas contain preliminary evidence · 0 certified keys"));
        parent.addView(line(a,"Research status: incomplete, specialist review required. "+
            "Evidence presence does not mean the trait or species identification is validated.",13,false));
        LinearLayout body=new LinearLayout(a);body.setOrientation(LinearLayout.VERTICAL);
        body.setVisibility(View.GONE);parent.addView(body);
        for(int i=0;i<definitions.length();i++){
            JSONObject field=definitions.optJSONObject(i);if(field==null)continue;
            JSONObject record=statuses.optJSONObject(field.optString("id"));
            String status=record==null?"not_assessed":record.optString("status");
            String message="not_assessed".equals(status)?"Not assessed":
                "Source evidence present · specialist review pending";
            body.addView(line(a,field.optString("label")+"\n"+message,13,false));
        }
        Button toggle=UiKit.button(a,"Show profile evidence gaps  ▾","quiet");
        parent.addView(toggle,new LinearLayout.LayoutParams(-1,dp(a,48)));
        toggle.setOnClickListener(v->{
            boolean show=body.getVisibility()!=View.VISIBLE;
            body.setVisibility(show?View.VISIBLE:View.GONE);
            toggle.setText(show?"Hide profile evidence gaps  ▴":"Show profile evidence gaps  ▾");
        });
    }
}
