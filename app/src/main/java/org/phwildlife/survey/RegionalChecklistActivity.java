package org.phwildlife.survey;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
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
        "Davao del Norte","Davao Oriental","Davao de Oro"};
    private FieldDb db;
    private EditText search;
    private Spinner scope;
    private TextView status;
    private ListView list;
    private final ArrayList<String> ids=new ArrayList<>();
    private final ArrayList<String> titles=new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private int dp(float n){return Math.round(n*getResources().getDisplayMetrics().density);}
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        db=new FieldDb(this);
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16),dp(13),dp(16),dp(13));
        root.setBackgroundColor(UiKit.BG);
        root.addView(UiKit.title(this,"Regional species evidence"));
        TextView warning=new TextView(this);
        warning.setText("Published location references. They do not confirm presence at this station.");
        warning.setTextColor(UiKit.MUTED);
        warning.setTextSize(13);
        root.addView(warning);
        scope=new Spinner(this);
        scope.setAdapter(new ArrayAdapter<String>(this,
            android.R.layout.simple_spinner_dropdown_item,AREAS));
        root.addView(scope);
        search=new EditText(this);
        search.setSingleLine(true);
        search.setHint("Filter scientific or common name");
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
        Button close=UiKit.button(this,"Back","quiet");
        close.setOnClickListener(v->finish());
        root.addView(close);
        setContentView(root);
        scope.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override public void onItemSelected(AdapterView<?> parent,View view,
                    int position,long id){refresh();}
            @Override public void onNothingSelected(AdapterView<?> p){}
        });
        search.addTextChangedListener(new TextWatcher(){
            @Override public void beforeTextChanged(CharSequence s,int a,int c,int f){}
            @Override public void onTextChanged(CharSequence s,int start,int before,int count){
                refresh();
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
                "'Davao del Sur','Davao del Norte','Davao Oriental','Davao de Oro')";
        }else{
            where="e.area_type='province' AND e.area_label=?";
            args.add(selected);
        }
        String term=TaxonSearchPolicy.trim(search.getText().toString());
        if(!term.isEmpty()){
            where+=" AND (LOWER(c.common_name) LIKE ? ESCAPE '!' OR "+
                "LOWER(c.scientific_name) LIKE ? ESCAPE '!')";
            String pattern=TaxonSearchPolicy.contains(term);
            args.add(pattern);args.add(pattern);
        }
        ids.clear();titles.clear();
        try(Cursor c=db.getReadableDatabase().rawQuery(
            "SELECT DISTINCT e.taxon_key,c.common_name,c.scientific_name "+
                "FROM taxon_occurrence_evidence e JOIN taxon_catalog c "+
                "ON e.taxon_key=c.taxon_key WHERE "+where+
                " ORDER BY c.scientific_name COLLATE NOCASE LIMIT 220",
            args.toArray(new String[0]))){
            while(c.moveToNext()){
                ids.add(c.getString(0));
                String common=c.isNull(1)?"":c.getString(1);
                String scientific=c.getString(2);
                titles.add((common.isEmpty()?"":common+" · ")+scientific);
            }
        }catch(Exception ignored){
            ids.clear();titles.clear();
        }
        adapter.notifyDataSetChanged();
        status.setText(selected+" · "+ids.size()+" source-listed taxa"+
            (ids.isEmpty()?"\nImport regional evidence through Offline taxonomy first.":""));
    }
    @Override protected void onDestroy(){db.close();super.onDestroy();}
}
