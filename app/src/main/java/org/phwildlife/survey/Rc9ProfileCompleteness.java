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

/** Evidence-readiness status, never a self-declared validated species key. */
public final class Rc9ProfileCompleteness {
    private Rc9ProfileCompleteness(){}
    private static JSONObject pack;
    private static JSONObject evidence;
    private static final int REFERENCE_LIMIT=450000;
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
    /** Reuses existing cited source statements; never invents traits for unassessed gaps. */
    private static synchronized JSONObject sourceEvidence(Activity a){
        if(evidence!=null)return evidence;
        try(InputStream in=a.getAssets().open("rc8_evidence_comparisons_v1.json");
            ByteArrayOutputStream out=new ByteArrayOutputStream()){
            byte[] b=new byte[8192];int n;
            while((n=in.read(b))!=-1){
                if(out.size()+n>REFERENCE_LIMIT)throw new IllegalArgumentException("Source matrix too large");
                out.write(b,0,n);
            }
            JSONObject data=new JSONObject(out.toString("UTF-8"));
            if(!"rc8-evidence-comparison-v1".equals(data.optString("schemaVersion")))
                throw new IllegalArgumentException("Unknown scientific evidence edition");
            evidence=data;
        }catch(Exception ex){evidence=new JSONObject();}
        return evidence;
    }
    private static JSONArray sourceStatements(Activity a,String scientific,String area){
        String category;
        switch(area){
            case "taxonomy": category="Taxonomy"; break;
            case "diagnosis": category="Identification"; break;
            case "habitat": category="Habitat & ecology"; break;
            case "range": category="Range"; break;
            case "behavior": category="Behavior & sound"; break;
            case "threat": category="Conservation"; break;
            default: return new JSONArray();
        }
        JSONArray species=sourceEvidence(a).optJSONArray("species");
        JSONArray found=new JSONArray();
        if(species==null)return found;
        for(int i=0;i<species.length();i++){
            JSONObject row=species.optJSONObject(i);
            if(row==null||!scientific.equalsIgnoreCase(row.optString("scientificName")))continue;
            JSONArray traits=row.optJSONArray("traits");
            if(traits==null)break;
            for(int j=0;j<traits.length();j++){
                JSONObject t=traits.optJSONObject(j);
                if(t!=null&&category.equals(t.optString("category")) &&
                    t.optString("url").startsWith("https://"))
                    found.put(t);
            }
            break;
        }
        return found;
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
            evidenced+" / "+definitions.length()+" areas contain preliminary evidence · "+
            load(a).optInt("certifiedComplete",0)+" certified profiles"));
        parent.addView(line(a,"Source-backed research notes are shown below. Their presence is not scientific approval. "+
            "Check original references, sex, age, locality and assessment dates.",13,false));
        LinearLayout body=new LinearLayout(a);body.setOrientation(LinearLayout.VERTICAL);
        body.setVisibility(View.GONE);parent.addView(body);
        for(int i=0;i<definitions.length();i++){
            JSONObject field=definitions.optJSONObject(i);if(field==null)continue;
            JSONObject record=statuses.optJSONObject(field.optString("id"));
            String status=record==null?"not_assessed":record.optString("status");
            String message="not_assessed".equals(status)?"Not assessed":
                "Source evidence present · specialist review pending";
            String area=field.optString("id");
            body.addView(line(a,field.optString("label")+"\n"+message,13,false));
            if("not_assessed".equals(status))continue;
            JSONArray statements=sourceStatements(a,scientific,area);
            for(int j=0;j<Math.min(4,statements.length());j++){
                JSONObject claim=statements.optJSONObject(j);
                if(claim==null)continue;
                String title=claim.optString("label");
                String value=claim.optString("value");
                String source=claim.optString("source");
                String url=claim.optString("url");
                body.addView(line(a,title+": "+value+"\nSource: "+source+
                    "\nReview: "+claim.optString("review"),12,false));
                Button open=UiKit.button(a,"Read cited scientific source","quiet");
                body.addView(open,new LinearLayout.LayoutParams(-1,dp(a,44)));
                open.setOnClickListener(v->a.startActivity(
                    new Intent(Intent.ACTION_VIEW,Uri.parse(url))));
            }
            if(statements.length()>4)
                body.addView(line(a,"+"+(statements.length()-4)+
                    " additional cited statements in the comparison reference.",12,false));
        }
        Button toggle=UiKit.button(a,"Read source evidence and gaps  ▾","quiet");
        parent.addView(toggle,new LinearLayout.LayoutParams(-1,dp(a,48)));
        toggle.setOnClickListener(v->{
            boolean show=body.getVisibility()!=View.VISIBLE;
            body.setVisibility(show?View.VISIBLE:View.GONE);
            toggle.setText(show?"Hide source evidence and gaps  ▴":"Read source evidence and gaps  ▾");
        });
    }
}
