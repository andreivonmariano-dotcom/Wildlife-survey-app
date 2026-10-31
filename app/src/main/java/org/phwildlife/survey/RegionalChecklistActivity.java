package org.phwildlife.survey;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Locale;

/** Source-listed islands/provinces, not field-presence predictions. */
public final class RegionalChecklistActivity extends Activity {
    private static final String[] AREAS={"Mindanao","Davao Region","Davao del Sur",
        "Davao del Norte","Davao Oriental","Davao de Oro","Davao Occidental"};
    private static final String[] GROUPS={"All wildlife","Birds","Amphibians",
        "Reptiles","Mammals"};
    private FieldDb db;
    private EditText search;
    private Spinner scope,groupFilter;
    private TextView status;
    private ListView list;
    private final ArrayList<String> ids=new ArrayList<>();
    private final ArrayList<String> titles=new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private final Handler searchHandler=new Handler(Looper.getMainLooper());
    private final Runnable searchRefresh=this::refresh;
    private int dp(float n){return Math.round(n*getResources().getDisplayMetrics().density);}
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        db=new FieldDb(this);
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16),dp(13),dp(16),dp(13));
        root.setBackgroundColor(UiKit.BG);
        root.addView(UiKit.navigationHeader(this,"Regional species evidence","Library",this::finish));
        TextView warning=new TextView(this);
        warning.setText("Published location references. They do not confirm presence at this station.");
        warning.setTextColor(UiKit.MUTED);
        warning.setTextSize(13);
        root.addView(warning);
        scope=new Spinner(this);
        scope.setAdapter(new ArrayAdapter<String>(this,
            android.R.layout.simple_spinner_dropdown_item,AREAS));
        root.addView(scope);
        groupFilter=new Spinner(this);
        groupFilter.setAdapter(new ArrayAdapter<String>(this,
            android.R.layout.simple_spinner_dropdown_item,GROUPS));
        root.addView(groupFilter);
        search=new EditText(this);
        search.setSingleLine(true);
        search.setHint("Filter common, scientific, or former name");
        search.setTextSize(15);
        root.addView(search);
        status=new TextView(this);
        status.setTextColor(UiKit.INK);
        status.setTextSize(13);
        status.setPadding(0,dp(7),0,dp(9));
        root.addView(status);
        list=new ListView(this);
        adapter=new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,titles);
        list.setAdapter(adapter);
        list.setOnItemClickListener((parent,view,pos,id)->{
            if(pos>=ids.size())return;
            startActivity(new Intent(this,TaxonDetailActivity.class)
                .putExtra("taxon_key",ids.get(pos)));
        });
        root.addView(list,new LinearLayout.LayoutParams(-1,0,1f));

        setContentView(root);
        scope.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override public void onItemSelected(AdapterView<?> parent,View view,
                    int position,long id){refresh();}
            @Override public void onNothingSelected(AdapterView<?> p){}
        });
        groupFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override public void onItemSelected(AdapterView<?> parent,View view,
                    int position,long id){refresh();}
            @Override public void onNothingSelected(AdapterView<?> parent){}
        });
        search.addTextChangedListener(new TextWatcher(){
            @Override public void beforeTextChanged(CharSequence s,int a,int c,int f){}
            @Override public void onTextChanged(CharSequence s,int start,int before,int count){
                searchHandler.removeCallbacks(searchRefresh);
                searchHandler.postDelayed(searchRefresh,180L);
            }
            @Override public void afterTextChanged(Editable e){}
        });
        refresh();
    }
    private void refresh(){
        if(scope==null||adapter==null)return;
        String selected=AREAS[scope.getSelectedItemPosition()<0?0:scope.getSelectedItemPosition()];
        String where;
        ArrayList<String> args=new ArrayList<>();
        if("Mindanao".equals(selected)){
            where="e.area_type='island' AND e.area_label='Mindanao'";
        }else if("Davao Region".equals(selected)){
            where="e.area_type='province' AND e.area_label IN ("+
                "'Davao del Sur','Davao del Norte','Davao Oriental','Davao de Oro',"+
                "'Davao Occidental')";
        }else{
            where="e.area_type='province' AND e.area_label=?";
            args.add(selected);
        }
        // 'unreviewed' imports are candidates, never positive regional listings.
        where+=" AND e.review_status='source_listed'";
        int selectedGroup=groupFilter==null?-1:groupFilter.getSelectedItemPosition();
        if(selectedGroup>0){
            where+=" AND c.taxon_group=?";
            args.add(GROUPS[selectedGroup]);
        }
        String term=TaxonSearchPolicy.trim(search.getText().toString());
        if(!term.isEmpty()){
            where+=" AND (LOWER(c.common_name) LIKE ? ESCAPE '!' OR "+
                "LOWER(c.scientific_name) LIKE ? ESCAPE '!' OR EXISTS ("+
                "SELECT 1 FROM taxon_synonyms a WHERE a.taxon_key=c.taxon_key "+
                "AND LOWER(a.alias_name) LIKE ? ESCAPE '!'))";
            String pattern=TaxonSearchPolicy.contains(term);
            args.add(pattern);args.add(pattern);args.add(pattern);
        }
        ids.clear();titles.clear();
        boolean capped=false;
        String queryError=null;
        try(Cursor c=db.getReadableDatabase().rawQuery(
            "SELECT DISTINCT e.taxon_key,c.common_name,c.scientific_name "+
                "FROM taxon_occurrence_evidence e JOIN taxon_catalog c "+
                "ON e.taxon_key=c.taxon_key WHERE "+where+
                " ORDER BY c.taxon_group,c.scientific_name COLLATE NOCASE LIMIT 1001",
            args.toArray(new String[0]))){
            while(c.moveToNext()){
                if(ids.size()==1000){capped=true;break;}
                ids.add(c.getString(0));
                String common=c.isNull(1)?"":c.getString(1);
                String scientific=c.getString(2);
                titles.add((common.isEmpty()?"":common+" · ")+scientific);
            }
        }catch(Exception failure){
            ids.clear();titles.clear();
            queryError="Regional catalog unavailable: "+failure.getClass().getSimpleName();
        }
        adapter.notifyDataSetChanged();
        if(queryError!=null){status.setText(queryError);return;}
        status.setText(selected+" · "+ids.size()+" source-listed taxa"+
            ("Davao Region".equals(selected)?
                "\nProvince records only. Davao City evidence is not separately assessed here.":"")+
            (capped?"\nShowing first 1,000 results. Narrow the name or group filter.":"")+
            (ids.isEmpty()?"\nNo matching reviewed source listings in the installed catalog.":""));
    }
    @Override protected void onDestroy(){
        searchHandler.removeCallbacks(searchRefresh);
        db.close();super.onDestroy();
    }
}
