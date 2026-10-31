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
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.accessibility.AccessibilityNodeInfo;
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
 * with published-guide and approved offline-photo filters. Read only.
 */
public final class Rc7SpeciesLibraryActivity extends Activity {
    private static final String[] GROUPS={"All groups","Birds","Amphibians","Reptiles","Mammals"};
    private static final String[] FILTERS={"All species","Published research",
        "Offline photos"};
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
    private boolean largeText;
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
        // Do not open the search keyboard before the researcher selects Search.
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN |
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        db=new FieldDb(this);
        largeText=getResources().getConfiguration().fontScale>=1.60f;
        loadGuides();
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(15),dp(11),dp(15),dp(12));
        root.setBackgroundColor(UiKit.BG);
        root.addView(UiKit.navigationHeader(this,
            largeText?"Species":"Philippine species checklist","Library",this::finish));
        String fullSourceLine="Source-linked offline taxonomy · WBCP 2025 · Sy 2025/2026 · MDD 2.5";
        TextView sourceBanner=label("Search Philippine wildlife references",13,false);
        sourceBanner.setContentDescription(fullSourceLine);
        if(!largeText)root.addView(sourceBanner);
        search=new EditText(this);
        search.setSingleLine(true);
        search.setTextSize(largeText?15:16);
        search.setHint(largeText?"Search taxon or family":"Species, family or former name");
        search.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        search.setContentDescription("Search Philippine taxon catalog");
        root.addView(search,new LinearLayout.LayoutParams(-1,dp(largeText?68:54)));
        LinearLayout filters=new LinearLayout(this);
        filters.setOrientation(LinearLayout.HORIZONTAL);
        group=new Spinner(this);scope=new Spinner(this);
        group.setId(View.generateViewId());scope.setId(View.generateViewId());
        group.setContentDescription("Wildlife group");
        scope.setContentDescription("Species library content filter");
        group.setAdapter(new ArrayAdapter<String>(this,
            android.R.layout.simple_spinner_dropdown_item,GROUPS));
        scope.setAdapter(new ArrayAdapter<String>(this,
            android.R.layout.simple_spinner_dropdown_item,FILTERS));
        // Visible labels distinguish taxonomic groups from content/evidence coverage.
        LinearLayout groupField=new LinearLayout(this);
        groupField.setOrientation(LinearLayout.VERTICAL);
        TextView groupLabel=label("Wildlife group",12,true);
        groupLabel.setLabelFor(group.getId());
        groupField.addView(groupLabel);
        groupField.addView(group,new LinearLayout.LayoutParams(-1,dp(largeText?64:48)));
        LinearLayout scopeField=new LinearLayout(this);
        scopeField.setOrientation(LinearLayout.VERTICAL);
        TextView scopeLabel=label("Reference coverage",12,true);
        scopeLabel.setLabelFor(scope.getId());
        scopeField.addView(scopeLabel);
        scopeField.addView(scope,new LinearLayout.LayoutParams(-1,dp(largeText?64:48)));
        float screenDp=getResources().getDisplayMetrics().widthPixels/
            getResources().getDisplayMetrics().density;
        if(screenDp<385f||largeText){
            filters.setOrientation(LinearLayout.VERTICAL);
            filters.addView(groupField,new LinearLayout.LayoutParams(-1,-2));
            filters.addView(scopeField,new LinearLayout.LayoutParams(-1,-2));
        }else{
            filters.addView(groupField,new LinearLayout.LayoutParams(0,-2,1));
            LinearLayout.LayoutParams right=new LinearLayout.LayoutParams(0,-2,1);
            right.leftMargin=dp(8);filters.addView(scopeField,right);
        }
        // At 200% font, keeping both filters fixed hid the entire results list.
        if(!largeText)root.addView(filters);
        Button researchToggle=UiKit.button(this,largeText?
            "Research tools  ▾":"Research tools  ▾","secondary");
        researchToggle.setContentDescription("Expand research comparisons, photographic evidence and regional sources");
        if(!largeText)root.addView(researchToggle,
            new LinearLayout.LayoutParams(-1,dp(48)));
        LinearLayout researchPanel=new LinearLayout(this);
        researchPanel.setOrientation(LinearLayout.VERTICAL);
        researchPanel.setVisibility(View.GONE);
        // Expandable tools live inside the scrolling results region, not above it.
        researchPanel.addView(label("Identification, habitat and photo references",12,false));
        researchToggle.setOnClickListener(v->{
            boolean open=researchPanel.getVisibility()!=View.VISIBLE;
            if(open)UiMotion.reveal(this,researchPanel);
            else UiMotion.hide(researchPanel);
            researchToggle.setText(open?
                (largeText?"Hide tools  ▴":"Hide research tools  ▴"):
                (largeText?"Research tools  ▾":"Research and comparison tools  ▾"));
        });
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
        researchPanel.addView(tools);
        Button comparePhotos=UiKit.button(this,"Compare offline wildlife photos","secondary");
        comparePhotos.setContentDescription("Compare two offline wildlife photographs with cited identification traits");
        comparePhotos.setOnClickListener(v->startActivity(
            new Intent(this,Rc8PhotoComparisonActivity.class)));
        researchPanel.addView(comparePhotos,new LinearLayout.LayoutParams(-1,dp(48)));
        Button differences=UiKit.button(this,"Cited differential characters","quiet");
        differences.setOnClickListener(v->startActivity(
            new Intent(this,Rc9DifferentialActivity.class)));
        researchPanel.addView(differences,new LinearLayout.LayoutParams(-1,dp(45)));
        status=label("",13,false);root.addView(status);
        status.setFocusable(true);
        status.setClickable(true);
        status.setAccessibilityDelegate(new View.AccessibilityDelegate(){
            @Override public void onInitializeAccessibilityNodeInfo(View host,
                    AccessibilityNodeInfo info){
                super.onInitializeAccessibilityNodeInfo(host,info);
                info.setClassName(Button.class.getName());
            }
        });
        status.setOnClickListener(v->new android.app.AlertDialog.Builder(this)
            .setTitle("Species evidence limits")
            .setMessage(status.getContentDescription())
            .setPositiveButton("Close",null).show());
        ScrollView scroll=new ScrollView(this);
        LinearLayout scrollBody=new LinearLayout(this);
        scrollBody.setOrientation(LinearLayout.VERTICAL);
        if(largeText){
            scrollBody.addView(sourceBanner);
            scrollBody.addView(filters);
            scrollBody.addView(researchToggle,new LinearLayout.LayoutParams(-1,dp(58)));
        }
        scrollBody.addView(researchPanel);
        results=new LinearLayout(this);results.setOrientation(LinearLayout.VERTICAL);
        scrollBody.addView(results);
        scroll.addView(scrollBody);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1f));
        more=UiKit.button(this,largeText?"More species":"Show more species","secondary");
        more.setContentDescription("Show next 24 Philippine species source records");
        root.addView(more,new LinearLayout.LayoutParams(-1,dp(largeText?58:51)));
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
        scope.setSelection(0);
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
                if(filter==1&&!Rc7LiteratureNotes.available(this,item.name))continue;
                if(filter==2&&!OfflinePhotoEvidence.hasOfflineTaxon(this,item.name))continue;
                selected.add(item);
            }
        }catch(Exception e){
            status.setText("Cannot read the offline source catalog");
            results.removeAllViews();more.setVisibility(View.GONE);
            return;
        }
        rendered=0;results.removeAllViews();
        String evidenceStatus=selected.size()+" names · source checklists are not complete species accounts. "+
            "IUCN and DENR status must be reviewed separately. Unverified is not Least Concern.";
        status.setText(largeText?selected.size()+" source records · Evidence limits ›":evidenceStatus);
        status.setContentDescription(evidenceStatus+" Tap for evidence limits.");
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
            card.setMinimumHeight(dp(largeText?104:90));
            card.setBackground(UiKit.surface(this));
            card.setContentDescription("Open species reference: "+
                (item.common.isEmpty()?item.name:item.common+", "+item.name));
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
            if(guides.contains(item.name.toLowerCase(Locale.ROOT)))
                description+=" · Introductory morphology";
            else if(Rc7LiteratureNotes.available(this,item.name))
                description+=" · Research summary";
            else description+=" · Checklist only";
            if(photo!=null)description+=" · Photo";
            TextView meta=label(description,12,false);
            labels.addView(meta);
            card.addView(labels,new LinearLayout.LayoutParams(0,-2,1f));
            TextView arrow=label("›",18,true);
            arrow.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            card.addView(arrow,new LinearLayout.LayoutParams(dp(21),-2));
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
            "No matches. Try another name or group.",14,false));
    }
    @Override protected void onDestroy(){
        if(delayed!=null)handler.removeCallbacks(delayed);
        handler.removeCallbacksAndMessages(null);
        if(db!=null)db.close();
        super.onDestroy();
    }
}
