package org.phwildlife.survey;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.graphics.Typeface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

/** Compact, offline, source-linked bibliography with local year and topic filters. */
public final class Rc14Bibliography {
    private Rc14Bibliography(){}
    private static JSONObject index;
    private static final String[] YEARS={
        "All years","2025–2026","2020–2024","Before 2020","Year not listed"
    };
    private static final String[] TOPICS={
        "All topics","Identification","Habitat","Distribution",
        "Ecology","Conservation","Taxonomy","Other"
    };
    private static int dp(Activity a,int px){
        return Math.round(px*a.getResources().getDisplayMetrics().density);
    }
    private static synchronized JSONObject load(Activity a){
        if(index!=null)return index;
        try(InputStream in=a.getAssets().open("rc14_96_species_literature_index_v1.json");
            ByteArrayOutputStream out=new ByteArrayOutputStream()){
            byte[] buffer=new byte[8192];int n;
            while((n=in.read(buffer))!=-1){
                if(out.size()+n>160000)throw new IllegalArgumentException("Bibliography too large");
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
        String authors=paper.optString("authors").trim();
        String journal=paper.optString("journal").trim();
        String doi=paper.isNull("doi")?"":paper.optString("doi").trim();
        int year=paper.optInt("publicationYear");
        LinearLayout item=new LinearLayout(a);item.setOrientation(LinearLayout.VERTICAL);
        item.setPadding(dp(a,4),dp(a,7),dp(a,4),dp(a,8));
        if(!authors.isEmpty()&&year>1800){
            item.addView(text(a,authors+" · "+year,true));
            String imprint=journal.isEmpty()?"Published research":journal;
            if(paper.optString("sourceType").startsWith("curated_"))
                imprint="Curated record · "+imprint;
            if(!doi.isEmpty())imprint+=" · DOI: "+doi;
            item.addView(text(a,imprint,false));
        }else{
            item.addView(text(a,citation,true));
        }
        Button open=UiKit.button(a,
            url.startsWith("https://doi.org/")?"Open study ↗":"Open source ↗","quiet");
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(a,41));
        lp.topMargin=dp(a,2);item.addView(open,lp);
        open.setContentDescription("Open original reference: "+citation);
        open.setOnClickListener(v->a.startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url))));
        where.addView(item);
    }
    private static boolean yearMatches(JSONObject paper,int choice){
        int year=paper.optInt("publicationYear",0);
        switch(choice){
            case 1:return year>=2025&&year<=2026;
            case 2:return year>=2020&&year<=2024;
            case 3:return year>0&&year<2020;
            case 4:return year==0;
            default:return true;
        }
    }
    private static boolean topicMatches(JSONObject paper,int choice){
        if(choice==0)return true;
        JSONArray tagged=paper.optJSONArray("topics");
        if(tagged==null)return false;
        for(int i=0;i<tagged.length();i++)
            if(TOPICS[choice].equals(tagged.optString(i)))return true;
        return false;
    }
    private static void render(Activity a,LinearLayout area,TextView count,
            JSONArray refs,int year,int topic){
        area.removeAllViews();
        ArrayList<JSONObject> matches=new ArrayList<>();
        for(int i=0;i<refs.length();i++){
            JSONObject p=refs.optJSONObject(i);
            if(p!=null&&yearMatches(p,year)&&topicMatches(p,topic))matches.add(p);
        }
        count.setText("Research references · "+matches.size());
        if(matches.isEmpty()){
            area.addView(text(a,"No sources match these filters.",false));
            return;
        }
        final int first=Math.min(3,matches.size());
        for(int i=0;i<first;i++)show(a,area,matches.get(i));
        if(matches.size()>first){
            LinearLayout more=new LinearLayout(a);
            more.setOrientation(LinearLayout.VERTICAL);
            more.setVisibility(View.GONE);
            for(int i=first;i<matches.size();i++)show(a,more,matches.get(i));
            final int remaining=matches.size()-first;
            Button toggle=UiKit.button(a,"Show "+remaining+" more sources  ▾","quiet");
            area.addView(toggle,new LinearLayout.LayoutParams(-1,dp(a,46)));
            area.addView(more);
            toggle.setOnClickListener(v->{
                boolean open=more.getVisibility()!=View.VISIBLE;
                more.setVisibility(open?View.VISIBLE:View.GONE);
                toggle.setText(open?"Show fewer sources  ▴":
                    "Show "+remaining+" more sources  ▾");
            });
        }
    }
    private static Spinner spinner(Activity a,String[] choices){
        Spinner spinner=new Spinner(a);
        ArrayAdapter<String> adapter=new ArrayAdapter<>(a,
            android.R.layout.simple_spinner_item,choices);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setMinimumHeight(dp(a,44));
        return spinner;
    }
    public static void add(Activity a,LinearLayout parent,String scientific){
        JSONArray profiles=load(a).optJSONArray("profiles");
        if(profiles==null)return;
        JSONArray found=null;
        for(int i=0;i<profiles.length();i++){
            JSONObject p=profiles.optJSONObject(i);
            if(p!=null&&scientific.equalsIgnoreCase(p.optString("scientificName"))){
                found=p.optJSONArray("publications");break;
            }
        }
        if(found==null)return;
        final JSONArray refs=found;
        TextView count=text(a,"Research references · "+refs.length(),true);
        parent.addView(count);
        LinearLayout filterPanel=new LinearLayout(a);
        filterPanel.setOrientation(LinearLayout.VERTICAL);
        filterPanel.setVisibility(View.GONE);
        filterPanel.setPadding(dp(a,4),dp(a,5),dp(a,4),dp(a,5));
        filterPanel.addView(text(a,"Publication year",false));
        Spinner year=spinner(a,YEARS);
        filterPanel.addView(year,new LinearLayout.LayoutParams(-1,dp(a,46)));
        filterPanel.addView(text(a,"Research topic",false));
        Spinner topic=spinner(a,TOPICS);
        filterPanel.addView(topic,new LinearLayout.LayoutParams(-1,dp(a,46)));
        Button filters=UiKit.button(a,"Filter references  ▾","quiet");
        filters.setContentDescription("Show publication year and research topic filters");
        parent.addView(filters,new LinearLayout.LayoutParams(-1,dp(a,45)));
        parent.addView(filterPanel);
        filters.setOnClickListener(v->{
            boolean open=filterPanel.getVisibility()!=View.VISIBLE;
            filterPanel.setVisibility(open?View.VISIBLE:View.GONE);
            filters.setText(open?"Hide filters  ▴":"Filter references  ▾");
        });
        LinearLayout results=new LinearLayout(a);
        results.setOrientation(LinearLayout.VERTICAL);
        parent.addView(results);
        Runnable refresh=()->render(a,results,count,refs,
            year.getSelectedItemPosition(),topic.getSelectedItemPosition());
        AdapterView.OnItemSelectedListener listener=new AdapterView.OnItemSelectedListener(){
            @Override public void onItemSelected(AdapterView<?> p,View v,int pos,long id){
                refresh.run();
            }
            @Override public void onNothingSelected(AdapterView<?> p){refresh.run();}
        };
        year.setOnItemSelectedListener(listener);
        topic.setOnItemSelectedListener(listener);
        render(a,results,count,refs,0,0);
    }
}
