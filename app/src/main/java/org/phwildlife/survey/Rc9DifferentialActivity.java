package org.phwildlife.survey;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
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
import java.util.ArrayList;

/** Curated cited contrasts, not a classifier or a certified identification key. */
public final class Rc9DifferentialActivity extends Activity {
    private final ArrayList<JSONObject> pairs=new ArrayList<>();
    private Spinner choose;
    private LinearLayout details;
    private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
    private TextView label(String s,int size){
        TextView t=new TextView(this);t.setText(s);t.setTextSize(size);
        t.setTextColor(UiKit.INK);t.setTextIsSelectable(true);
        t.setPadding(dp(4),dp(5),dp(4),dp(6));return t;
    }
    private static JSONObject read(Activity a)throws Exception{
        try(InputStream in=a.getAssets().open("rc9_differential_traits_v1.json");
            ByteArrayOutputStream out=new ByteArrayOutputStream()){
            byte[] b=new byte[8192];int n;
            while((n=in.read(b))!=-1){
                if(out.size()+n>90000)throw new IllegalArgumentException("Differential file too large");
                out.write(b,0,n);
            }
            return new JSONObject(out.toString("UTF-8"));
        }
    }
    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(14),dp(10),dp(14),dp(12));root.setBackgroundColor(UiKit.BG);
        root.addView(UiKit.navigationHeader(this,"Differential identification","Species",this::finish));
        root.addView(label("Research-level comparison of anatomical characters, age, sex and source conflicts. "+
            "No comparison below is a specialist-certified key. Do not force a field identification.",13));
        try{
            JSONArray data=read(this).getJSONArray("pairs");
            for(int i=0;i<data.length();i++)pairs.add(data.getJSONObject(i));
            if(pairs.isEmpty())throw new IllegalArgumentException("Missing curated pairs");
        }catch(Exception ex){
            root.addView(label("No source-checked differential comparisons are installed.",14));
            setContentView(root);return;
        }
        ArrayList<String> names=new ArrayList<>();
        for(JSONObject p:pairs)names.add(p.optString("a")+" / "+p.optString("b"));
        choose=new Spinner(this);
        choose.setContentDescription("Select source-checked species comparison pair");
        choose.setAdapter(new ArrayAdapter<String>(this,
            android.R.layout.simple_spinner_dropdown_item,names));
        root.addView(choose,new LinearLayout.LayoutParams(-1,dp(52)));
        ScrollView scroll=new ScrollView(this);
        details=new LinearLayout(this);details.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(details);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        setContentView(root);
        choose.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override public void onItemSelected(AdapterView<?> p,View v,int pos,long id){draw();}
            @Override public void onNothingSelected(AdapterView<?> p){}
        });
        String name=getIntent().getStringExtra("scientific_name");
        if(name!=null)for(int i=0;i<pairs.size();i++){
            JSONObject p=pairs.get(i);
            if(name.equalsIgnoreCase(p.optString("a"))||name.equalsIgnoreCase(p.optString("b"))){
                choose.setSelection(i);break;
            }
        }
        draw();
    }
    private void draw(){
        if(details==null||choose==null)return;
        int i=choose.getSelectedItemPosition();if(i<0||i>=pairs.size())return;
        JSONObject pair=pairs.get(i);
        details.removeAllViews();
        details.addView(FieldVisuals.section(this,"Published research comparison",
            pair.optString("a")+" vs "+pair.optString("b")));
        JSONArray traits=pair.optJSONArray("evidence");
        if(traits!=null)for(int j=0;j<traits.length();j++){
            JSONObject trait=traits.optJSONObject(j);if(trait==null)continue;
            LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);
            card.setBackground(UiKit.surface(this));
            card.setPadding(dp(10),dp(8),dp(10),dp(10));
            card.addView(label(trait.optString("part")+" · "+trait.optString("stage"),15));
            card.addView(label(pair.optString("a")+"\n"+trait.optString("valueA"),14));
            card.addView(label(pair.optString("b")+"\n"+trait.optString("valueB"),14));
            card.addView(label("Limit: "+trait.optString("caution")+
                "\nReview: published description, not specialist-approved",12));
            for(String field:new String[]{"sourceA","sourceB"}){
                String source=trait.optString(field);
                if(!source.startsWith("https://"))continue;
                Button open=UiKit.button(this,"Open "+(field.equals("sourceA")?"A":"B")+" scientific source","quiet");
                card.addView(open,new LinearLayout.LayoutParams(-1,dp(43)));
                open.setOnClickListener(v->startActivity(
                    new Intent(Intent.ACTION_VIEW,Uri.parse(source))));
            }
            LinearLayout.LayoutParams box=new LinearLayout.LayoutParams(-1,-2);
            box.bottomMargin=dp(9);details.addView(card,box);
        }
        details.addView(label("Missing characters are not zero or absent. "+
            "Published island range is supporting context, never standalone identification. "+
            "Additional same-genus taxa require actual comparative diagnoses.",13));
    }
}
