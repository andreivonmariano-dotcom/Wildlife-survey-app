package org.phwildlife.survey;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/** Offline, source-attributed morphology comparison; never writes observations. */
public final class DiagnosticGuideActivity extends Activity {
    private JSONObject bundle;
    private JSONArray profiles;
    private Spinner left,right;
    private LinearLayout rows;
    private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
    private TextView text(String value,int sp,boolean bold){
        TextView t=new TextView(this);
        t.setText(value);t.setTextSize(sp);t.setTextColor(UiKit.INK);
        if(bold)t.setTypeface(null,Typeface.BOLD);
        t.setPadding(dp(3),dp(4),dp(3),dp(4));
        return t;
    }
    private static JSONObject load(Context context) throws Exception {
        try(InputStream in=context.getAssets().open("diagnostic_profiles_v1.json");
            ByteArrayOutputStream out=new ByteArrayOutputStream()){
            byte[] buffer=new byte[4096];
            int size;
            while((size=in.read(buffer))!=-1){
                if(out.size()+size>128000)throw new IllegalStateException("Profile file too large");
                out.write(buffer,0,size);
            }
            return new JSONObject(new String(out.toByteArray(),StandardCharsets.UTF_8));
        }
    }
    public static boolean hasProfile(Context context,String scientific){
        if(scientific==null||scientific.trim().isEmpty())return false;
        try{
            JSONArray list=load(context).getJSONArray("profiles");
            for(int i=0;i<list.length();i++)
                if(scientific.equalsIgnoreCase(list.getJSONObject(i).getString("scientificName")))
                    return true;
        }catch(Exception ignored){}
        return false;
    }
    private JSONObject profile(int index){
        return profiles.optJSONObject(index);
    }
    private String label(JSONObject profile){
        return profile.optString("commonName")+" · "+profile.optString("scientificName");
    }
    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16),dp(12),dp(16),dp(12));
        root.setBackgroundColor(UiKit.BG);
        root.addView(UiKit.title(this,"Field identification"));
        TextView note=text("Two introductory Philippine raptor profiles. Morphology is "+
            "source-checked, not independently reviewed as a taxonomic key.",13,false);
        note.setTextColor(UiKit.MUTED);root.addView(note);
        try{
            bundle=load(this);
            profiles=bundle.getJSONArray("profiles");
            if(profiles.length()<2)throw new IllegalStateException("Comparison requires two profiles");
        }catch(Exception failure){
            root.addView(text("Diagnostic reference pack unavailable. "+
                "Existing survey and species catalog records are unchanged.",14,false));
            setContentView(root);return;
        }
        ArrayList<String> options=new ArrayList<>();
        for(int i=0;i<profiles.length();i++)options.add(label(profile(i)));
        root.addView(UiKit.sectionTitle(this,"Compare visible characters"));
        left=new Spinner(this);
        right=new Spinner(this);
        ArrayAdapter<String> adapter=new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_dropdown_item,options);
        left.setAdapter(adapter);
        right.setAdapter(adapter);
        int selected=0;
        String requested=getIntent().getStringExtra("scientific_name");
        for(int i=0;i<profiles.length();i++){
            if(profile(i).optString("scientificName").equalsIgnoreCase(requested)){
                selected=i;break;
            }
        }
        left.setSelection(selected);
        right.setSelection((selected+1)%profiles.length());
        root.addView(text("Reference species",12,true));root.addView(left);
        root.addView(text("Compare with",12,true));root.addView(right);
        ScrollView scroll=new ScrollView(this);
        rows=new LinearLayout(this);
        rows.setOrientation(LinearLayout.VERTICAL);
        rows.setPadding(0,dp(7),0,dp(18));
        scroll.addView(rows);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        Button back=UiKit.button(this,"Back to species reference","quiet");
        back.setOnClickListener(v->finish());
        root.addView(back);
        setContentView(root);
        AdapterView.OnItemSelectedListener listener=new AdapterView.OnItemSelectedListener(){
            @Override public void onItemSelected(AdapterView<?> parent,
                    android.view.View view,int position,long id){showComparison();}
            @Override public void onNothingSelected(AdapterView<?> parent){}
        };
        left.setOnItemSelectedListener(listener);
        right.setOnItemSelectedListener(listener);
        showComparison();
    }
    private Map<String,JSONObject> traits(JSONObject profile){
        LinkedHashMap<String,JSONObject> values=new LinkedHashMap<>();
        JSONArray a=profile.optJSONArray("traits");
        if(a!=null)for(int i=0;i<a.length();i++){
            JSONObject trait=a.optJSONObject(i);
            if(trait!=null)values.put(trait.optString("field"),trait);
        }
        return values;
    }
    private String citationText(JSONObject trait){
        if(trait==null)return "Not assessed in this starter profile";
        JSONArray citations=trait.optJSONArray("sourceIds");
        ArrayList<String> names=new ArrayList<>();
        JSONArray sources=bundle.optJSONArray("sourceList");
        if(citations!=null&&sources!=null)for(int j=0;j<citations.length();j++){
            String id=citations.optString(j);
            for(int i=0;i<sources.length();i++){
                JSONObject source=sources.optJSONObject(i);
                if(source!=null&&id.equals(source.optString("id"))){
                    names.add(source.optString("publisher"));
                    break;
                }
            }
        }
        return names.isEmpty()?"Reference not identified":android.text.TextUtils.join(", ",names);
    }
    private void evidenceLine(LinearLayout item,JSONObject species,JSONObject trait){
        item.addView(text(species.optString("commonName"),14,true));
        TextView value=text(trait==null?"Not assessed":
            trait.optString("value","Not assessed"),14,false);
        value.setTextIsSelectable(true);item.addView(value);
        TextView source=text("Source: "+citationText(trait),11,false);
        source.setTextColor(UiKit.MUTED);item.addView(source);
    }
    private void showComparison(){
        if(rows==null||left==null||right==null)return;
        int a=left.getSelectedItemPosition(),b=right.getSelectedItemPosition();
        if(a<0||b<0)return;
        rows.removeAllViews();
        if(a==b){
            rows.addView(text("Select a different species to compare characters.",14,false));
            return;
        }
        JSONObject first=profile(a),second=profile(b);
        Map<String,JSONObject> traitsA=traits(first),traitsB=traits(second);
        LinkedHashMap<String,Boolean> fields=new LinkedHashMap<>();
        for(String field:traitsA.keySet())fields.put(field,true);
        for(String field:traitsB.keySet())fields.put(field,true);
        for(String field:fields.keySet()){
            LinearLayout card=new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(12),dp(9),dp(12),dp(10));
            card.setBackground(UiKit.surface(this));
            card.addView(text(field,16,true));
            evidenceLine(card,first,traitsA.get(field));
            TextView divider=text("Compared with",11,false);
            divider.setTextColor(UiKit.POND);card.addView(divider);
            evidenceLine(card,second,traitsB.get(field));
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
            lp.bottomMargin=dp(10);rows.addView(card,lp);
        }
        rows.addView(UiKit.sectionTitle(this,"Source records"));
        JSONArray sources=bundle.optJSONArray("sourceList");
        if(sources!=null)for(int i=0;i<sources.length();i++){
            JSONObject entry=sources.optJSONObject(i);
            if(entry==null)continue;
            String url=entry.optString("url");
            TextView credit=text(entry.optString("publisher")+" · "+
                entry.optString("title")+"\nChecked "+entry.optString("accessed"),12,false);
            credit.setTextColor(UiKit.MUTED);rows.addView(credit);
            if(url.startsWith("https://")){
                Button open=UiKit.button(this,"Open publisher source","secondary");
                rows.addView(open);
                open.setOnClickListener(v->{
                    try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url)));}
                    catch(Exception ignored){
                        new android.app.AlertDialog.Builder(this)
                            .setMessage("No application can open this source link.")
                            .setPositiveButton("Close",null).show();
                    }
                });
            }
        }
        TextView foot=text("These visible characters aid comparison. They do not "+
            "confirm identity, subspecies or locality. Record uncertain detections "+
            "without forcing a species-level determination.",12,false);
        foot.setTextColor(UiKit.MUTED);rows.addView(foot);
    }
}
