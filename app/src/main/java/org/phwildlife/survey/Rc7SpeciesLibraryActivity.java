package org.phwildlife.survey;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;

/**
 * Offline RC7 Species Library: searchable private WBCP, Sy and MDD source records,
 * with evidence-specific priority and published-guide filters. Read only.
 */
public final class Rc7SpeciesLibraryActivity extends Activity {
    private static final String[] GROUPS={"All groups","Birds","Amphibians","Reptiles","Mammals"};
    private static final String[] FILTERS={"All source records","Priority species (96)",
        "Published literature notes","Offline photograph"};
    private FieldDb db;
    private EditText search;
    private Spinner group,scope;
    private LinearLayout results;
    private TextView status;
    private Button more;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private Runnable delayed;
    private final ArrayList<Summary> selected=new ArrayList<>();
    private final HashSet<String> guides=new HashSet<>();
    private int rendered=0;
    private static final int PAGE=24;
    private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
    private static final class Summary {
        final String key,name,common,family,group,source;
        Summary(String key,String name,String common,String family,String group,String source){
            this.key=key;this.name=name;this.common=common;
            this.family=family;this.group=group;this.source=source;
        }
    }
    private static String field(Cursor c,int col){
        return c.isNull(col)?"":c.getString(col);
    }
    private TextView label(String text,int size,boolean bold){
        TextView t=new TextView(this);t.setText(text);t.setTextSize(size);
        t.setTextColor(bold?UiKit.INK:UiKit.MUTED);
        if(bold)t.setTypeface(null,Typeface.BOLD);
        t.setPadding(dp(3),dp(4),dp(3),dp(7));
        return t;
    }
    private void loadGuides(){
        try(InputStream in=getAssets().open("diagnostic_profiles_v1.json");
            ByteArrayOutputStream out=new ByteArrayOutputStream()){
            byte[] b=new byte[4096];int n;
            while((n=in.read(b))!=-1){
                if(out.size()+n>200000)return;
                out.write(b,0,n);
            }
            JSONArray profiles=new JSONObject(out.toString("UTF-8")).getJSONArray("profiles");
            for(int i=0;i<profiles.length();i++){
                JSONObject p=profiles.optJSONObject(i);
                if(p!=null)guides.add(p.optString("scientificName").toLowerCase(Locale.ROOT));
            }
        }catch(Exception ignored){}
    }
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        db=new FieldDb(this);
        loadGuides();
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(15),dp(11),dp(15),dp(12));
        root.setBackgroundColor(UiKit.BG);
        root.addView(UiKit.navigationHeader(this,"Philippine Species Library","Home",this::finish));
        root.addView(label("Source-linked offline taxonomy · WBCP 2025 · Sy 2025/2026 · MDD 2.5",
            13,false));
        search=new EditText(this);
        search.setSingleLine(true);
        search.setTextSize(16);
        search.setHint("Name, former name, family or source");
        search.setContentDescription("Search Philippine taxon catalog");
        root.addView(search,new LinearLayout.LayoutParams(-1,dp(54)));
        LinearLayout filters=new LinearLayout(this);filters.setOrientation(LinearLayout.HORIZONTAL);
        group=new Spinner(this);scope=new Spinner(this);
        group.setContentDescription("Wildlife group");
        scope.setContentDescription("Species library content filter");
        group.setAdapter(new ArrayAdapter<String>(this,
            android.R.layout.simple_spinner_dropdown_item,GROUPS));
        scope.setAdapter(new ArrayAdapter<String>(this,
            android.R.layout.simple_spinner_dropdown_item,FILTERS));
        float screenDp=getResources().getDisplayMetrics().widthPixels/
            getResources().getDisplayMetrics().density;
        if(screenDp<385f){
            filters.setOrientation(LinearLayout.VERTICAL);
            filters.addView(group,new LinearLayout.LayoutParams(-1,dp(48)));
            filters.addView(scope,new LinearLayout.LayoutParams(-1,dp(48)));
        }else{
            filters.addView(group,new LinearLayout.LayoutParams(0,dp(50),1));
            LinearLayout.LayoutParams right=new LinearLayout.LayoutParams(0,dp(50),1);
            right.leftMargin=dp(8);filters.addView(scope,right);
        }
        root.addView(filters);
        LinearLayout tools=new LinearLayout(this);
        tools.setOrientation(screenDp<385f?LinearLayout.VERTICAL:LinearLayout.HORIZONTAL);
        Button region=UiKit.button(this,"Regional evidence","quiet");
        region.setOnClickListener(v->startActivity(new Intent(this,RegionalChecklistActivity.class)));
        tools.addView(region,screenDp<385f?
            new LinearLayout.LayoutParams(-1,dp(46)):
            new LinearLayout.LayoutParams(0,dp(48),1));
        Button compare=UiKit.button(this,"Compare traits","quiet");
        compare.setOnClickListener(v->startActivity(new Intent(this,Rc8TraitComparisonActivity.class)));
        tools.addView(compare,screenDp<385f?
            new LinearLayout.LayoutParams(-1,dp(46)):
            new LinearLayout.LayoutParams(0,dp(48),1));
        root.addView(tools);
        status=label("",13,false);root.addView(status);
        ScrollView scroll=new ScrollView(this);
        results=new LinearLayout(this);results.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(results);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1f));
        more=UiKit.button(this,"Show more species","secondary");
        root.addView(more,new LinearLayout.LayoutParams(-1,dp(51)));
        more.setOnClickListener(v->appendPage());
        setContentView(root);
        AdapterView.OnItemSelectedListener listener=new AdapterView.OnItemSelectedListener(){
            @Override public void onItemSelected(AdapterView<?> a,View v,int pos,long id){refresh();}
            @Override public void onNothingSelected(AdapterView<?> a){}
        };
        group.setOnItemSelectedListener(listener);
        scope.setOnItemSelectedListener(listener);
        search.addTextChangedListener(new TextWatcher(){
            @Override public void beforeTextChanged(CharSequence s,int start,int count,int after){}
            @Override public void onTextChanged(CharSequence s,int start,int before,int count){
                if(delayed!=null)handler.removeCallbacks(delayed);
                delayed=Rc7SpeciesLibraryActivity.this::refresh;
                handler.postDelayed(delayed,180);
            }
            @Override public void afterTextChanged(Editable e){}
        });
        scope.setSelection(1);
        refresh();
    }
    private void refresh(){
        if(db==null||results==null)return;
        selected.clear();
        String raw=search.getText().toString().trim();
        String q=TaxonSearchPolicy.escapeLike(raw.toLowerCase(Locale.ROOT));
        String pat="%"+q+"%";
        String g=group.getSelectedItemPosition()==0?"":GROUPS[group.getSelectedItemPosition()];
        int filter=scope.getSelectedItemPosition();
        HashSet<String> seen=new HashSet<>();
        SQLiteDatabase sqlite=db.getReadableDatabase();
        String sql="SELECT taxon_key,scientific_name,common_name,family,taxon_group,source "+
            "FROM taxon_catalog WHERE "+
            "(?='' OR taxon_group=?) AND (?='' OR "+
            "LOWER(scientific_name) LIKE ? ESCAPE '!' OR "+
            "LOWER(common_name) LIKE ? ESCAPE '!' OR "+
            "LOWER(family) LIKE ? ESCAPE '!' OR "+
            "LOWER(source) LIKE ? ESCAPE '!' OR EXISTS ("+
            "SELECT 1 FROM taxon_synonyms s WHERE s.taxon_key=taxon_catalog.taxon_key "+
            "AND LOWER(s.alias_name) LIKE ? ESCAPE '!')) "+
            "ORDER BY CASE WHEN taxon_key LIKE 'STARTER:%' THEN 1 ELSE 0 END,"+
            "scientific_name COLLATE NOCASE LIMIT 2200";
        try(Cursor c=sqlite.rawQuery(sql,new String[]{g,g,raw,pat,pat,pat,pat,pat})){
            while(c.moveToNext()){
                Summary item=new Summary(field(c,0),field(c,1),field(c,2),
                    field(c,3),field(c,4),field(c,5));
                if(!seen.add(item.name.toLowerCase(Locale.ROOT)))continue;
                if(filter==1&&!Rc7FieldAccounts.priority(this,item.name))continue;
                if(filter==2&&!Rc7LiteratureNotes.available(this,item.name))continue;
                if(filter==3&&!OfflinePhotoEvidence.hasOfflineTaxon(this,item.name))continue;
                selected.add(item);
            }
        }catch(Exception e){
            status.setText("Cannot read the offline source catalog");
            results.removeAllViews();more.setVisibility(View.GONE);
            return;
        }
        rendered=0;results.removeAllViews();
        status.setText(selected.size()+" matching source records · list is not site occurrence. "+
            (filter==1?"Priority identities are checklist-backed; detailed field biology is still under review.":""));
        appendPage();
    }
    private void appendPage(){
        int end=Math.min(rendered+PAGE,selected.size());
        for(int n=rendered;n<end;n++){
            Summary item=selected.get(n);
            LinearLayout card=new LinearLayout(this);
            card.setOrientation(LinearLayout.HORIZONTAL);
            card.setGravity(android.view.Gravity.CENTER_VERTICAL);
            card.setPadding(dp(10),dp(8),dp(10),dp(8));
            card.setBackground(UiKit.surface(this));
            card.setContentDescription("Open species reference: "+
                (item.common.isEmpty()?item.name:item.common)+", "+item.name);
            card.setClickable(true);
            card.setFocusable(true);
            android.widget.ImageView photo=OfflinePhotoEvidence.thumbnailForTaxon(
                this,item.name);
            if(photo!=null){
                LinearLayout.LayoutParams imageLp=
                    new LinearLayout.LayoutParams(dp(74),dp(74));
                imageLp.rightMargin=dp(12);
                card.addView(photo,imageLp);
            }
            LinearLayout labels=new LinearLayout(this);
            labels.setOrientation(LinearLayout.VERTICAL);
            TextView title=label(item.common.isEmpty()?item.name:item.common,15,true);
            title.setTextColor(UiKit.INK);labels.addView(title);
            if(!item.common.isEmpty()){
                TextView scientific=label(item.name,14,false);
                scientific.setTypeface(null,Typeface.ITALIC);
                labels.addView(scientific);
            }
            String description=item.group+" · "+item.family;
            if(Rc7FieldAccounts.priority(this,item.name))
                description+=" · Priority";
            if(Rc7LiteratureNotes.available(this,item.name))
                description+=" · Literature";
            if(photo!=null)description+=" · Photo";
            TextView meta=label(description,12,false);
            labels.addView(meta);
            card.addView(labels,new LinearLayout.LayoutParams(0,-2,1f));
            card.setOnClickListener(v->startActivity(
                new Intent(this,TaxonDetailActivity.class)
                    .putExtra("taxon_key",item.key)));
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
            lp.bottomMargin=dp(8);
            results.addView(card,lp);
        }
        rendered=end;
        more.setVisibility(rendered<selected.size()?View.VISIBLE:View.GONE);
        if(selected.isEmpty())results.addView(label(
            "No source-listed name matches these filters. Unassessed does not mean absent.",14,false));
    }
    @Override protected void onDestroy(){
        if(delayed!=null)handler.removeCallbacks(delayed);
        handler.removeCallbacksAndMessages(null);
        if(db!=null)db.close();
        super.onDestroy();
    }
}
