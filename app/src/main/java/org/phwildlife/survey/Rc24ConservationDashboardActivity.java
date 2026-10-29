package org.phwildlife.survey;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Gravity;
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
import java.util.Locale;

/**
 * Read-only conservation literature dashboard.
 * Uses the same versioned species facts shown on the 96 species pages.
 * IUCN, DENR national records and research never share an inferred status.
 */
public final class Rc24ConservationDashboardActivity extends Activity {
    private static final String CORPUS="rc18_unified_96_priority_accounts_v1.json";
    private static final String[] GROUPS={
        "All wildlife","Birds","Amphibians","Reptiles","Mammals"};
    private static final String[] SCOPES={
        "All cited records","Dated IUCN global","Philippine national","Studies and threats"};
    private static final int PAGE_SIZE=12;
    private final ArrayList<JSONObject> matching=new ArrayList<>();
    private JSONArray accounts;
    private EditText search;
    private Spinner group,scope;
    private TextView status;
    private LinearLayout cards;
    private int shown=0;
    private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
    private TextView text(String value,int size,boolean strong){
        TextView v=new TextView(this);
        v.setText(value);
        v.setTextSize(size);
        v.setTextColor(strong?UiKit.INK:UiKit.MUTED);
        if(strong)v.setTypeface(null,Typeface.BOLD);
        v.setTextIsSelectable(true);
        v.setPadding(dp(2),dp(3),dp(2),dp(5));
        return v;
    }
    private static JSONObject read(Activity activity)throws Exception{
        try(InputStream in=activity.getAssets().open(CORPUS);
            ByteArrayOutputStream out=new ByteArrayOutputStream()){
            byte[] b=new byte[8192];int n;
            while((n=in.read(b))!=-1){
                if(out.size()+n>750000)throw new IllegalStateException("Conservation corpus too large");
                out.write(b,0,n);
            }
            JSONObject data=new JSONObject(out.toString("UTF-8"));
            if(data.optInt("prioritySpeciesCount")!=96||
                data.getJSONArray("accounts").length()!=96)
                throw new IllegalStateException("Species reference incomplete");
            return data;
        }
    }
    private static boolean correctScope(JSONObject fact,int choice){
        String kind=fact.optString("sourceType");
        boolean global="iucn_dated_assessment".equals(kind);
        boolean national="government_national_conservation_record".equals(kind);
        switch(choice){
            case 1:return global;
            case 2:return national;
            case 3:return !global&&!national;
            default:return true;
        }
    }
    private static String scopeLabel(JSONObject fact){
        String kind=fact.optString("sourceType");
        if("iucn_dated_assessment".equals(kind))return "Dated IUCN global assessment";
        if("government_national_conservation_record".equals(kind))
            return "Philippine national conservation record";
        return "Research / documented threat";
    }
    private static JSONArray conservation(JSONObject account){
        JSONObject sections=account.optJSONObject("sections");
        if(sections==null)return new JSONArray();
        JSONArray records=sections.optJSONArray("Conservation");
        return records==null?new JSONArray():records;
    }
    private void openSource(String url){
        if(!url.startsWith("https://"))return;
        try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url)));}
        catch(Exception error){
            new AlertDialog.Builder(this)
                .setMessage("No application is available to open this reference.")
                .setPositiveButton("Close",null).show();
        }
    }
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        LinearLayout page=new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setBackgroundColor(UiKit.BG);
        page.setPadding(dp(15),dp(10),dp(15),dp(24));
        boolean largeText=getResources().getConfiguration().fontScale>=1.60f;
        page.addView(UiKit.navigationHeader(this,
            largeText?"Conservation":"Conservation evidence","Library",this::finish));
        String explanation="Offline research snapshot across 96 Philippine priority species. "+
            "Only individually cited conservation records appear. An unassessed species "+
            "is not Least Concern, and a dated assessment is not a current census.";
        TextView context=text(largeText?
            "Cited evidence, not current population counts. Tap for limits.":explanation,
            13,false);
        context.setContentDescription("Read conservation evidence limits. "+explanation);
        context.setFocusable(true);
        context.setOnClickListener(v->new AlertDialog.Builder(this)
            .setTitle("Conservation research limits").setMessage(explanation)
            .setPositiveButton("Close",null).show());
        page.addView(context);
        try{accounts=read(this).getJSONArray("accounts");}
        catch(Exception error){
            page.addView(text("Conservation research index unavailable offline. "+
                "Your saved field surveys remain unchanged.",14,false));
            ScrollView fallback=new ScrollView(this);fallback.addView(page);
            setContentView(fallback);return;
        }
        search=new EditText(this);
        search.setSingleLine(true);
        search.setTextSize(16);
        search.setHint(largeText?"Search species":"Filter scientific or common name");
        search.setContentDescription("Filter the conservation evidence by common or scientific name");
        page.addView(search,new LinearLayout.LayoutParams(-1,dp(54)));
        group=new Spinner(this);
        group.setContentDescription("Wildlife group filter");
        group.setAdapter(new ArrayAdapter<String>(this,
            android.R.layout.simple_spinner_dropdown_item,GROUPS));
        page.addView(group,new LinearLayout.LayoutParams(-1,dp(52)));
        scope=new Spinner(this);
        scope.setContentDescription("Conservation evidence source filter");
        scope.setAdapter(new ArrayAdapter<String>(this,
            android.R.layout.simple_spinner_dropdown_item,SCOPES));
        page.addView(scope,new LinearLayout.LayoutParams(-1,dp(52)));
        status=text("",13,true);
        page.addView(status);
        cards=new LinearLayout(this);
        cards.setOrientation(LinearLayout.VERTICAL);
        page.addView(cards,new LinearLayout.LayoutParams(-1,-2));
        ScrollView scroll=new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.addView(page);
        setContentView(scroll);
        AdapterView.OnItemSelectedListener listener=new AdapterView.OnItemSelectedListener(){
            @Override public void onItemSelected(AdapterView<?> p,View v,int pos,long id){
                refresh();
            }
            @Override public void onNothingSelected(AdapterView<?> p){}
        };
        group.setOnItemSelectedListener(listener);
        scope.setOnItemSelectedListener(listener);
        search.addTextChangedListener(new TextWatcher(){
            @Override public void beforeTextChanged(CharSequence s,int start,int count,int after){}
            @Override public void onTextChanged(CharSequence s,int start,int before,int count){
                refresh();
            }
            @Override public void afterTextChanged(Editable s){}
        });
        refresh();
    }
    private void refresh(){
        if(accounts==null||cards==null||status==null)return;
        matching.clear();cards.removeAllViews();shown=0;
        int groupChoice=group==null?0:group.getSelectedItemPosition();
        int scopeChoice=scope==null?0:scope.getSelectedItemPosition();
        String term=search==null?"":search.getText().toString().trim()
            .toLowerCase(Locale.ROOT);
        int available=0;
        for(int i=0;i<accounts.length();i++){
            JSONObject account=accounts.optJSONObject(i);
            if(account==null)continue;
            if(conservation(account).length()>0)available++;
            if(groupChoice>0&&!GROUPS[groupChoice].equals(account.optString("group")))
                continue;
            String searchText=(account.optString("scientificName")+" "+
                account.optString("commonName")).toLowerCase(Locale.ROOT);
            if(!searchText.contains(term))continue;
            boolean hasMatch=false;
            JSONArray facts=conservation(account);
            for(int k=0;k<facts.length();k++){
                JSONObject fact=facts.optJSONObject(k);
                if(fact!=null&&correctScope(fact,scopeChoice)){hasMatch=true;break;}
            }
            if(hasMatch)matching.add(account);
        }
        status.setText(available+" of 96 species have cited conservation notes · "+
            (96-available)+" without documented conservation topics. "+
            matching.size()+" species match these filters.");
        if(matching.isEmpty())cards.addView(text("No cited records match. "+
            "Missing information is not evidence of a safe conservation status.",14,false));
        else appendPage();
    }
    private void appendPage(){
        int end=Math.min(shown+PAGE_SIZE,matching.size());
        int scopeChoice=scope.getSelectedItemPosition();
        for(int i=shown;i<end;i++)addAccount(matching.get(i),scopeChoice);
        shown=end;
        if(shown<matching.size()){
            Button more=UiKit.button(this,"Show more cited species","secondary");
            more.setContentDescription("Show the next twelve conservation research accounts");
            LinearLayout.LayoutParams m=new LinearLayout.LayoutParams(-1,dp(52));
            m.topMargin=dp(5);cards.addView(more,m);
            more.setOnClickListener(v->{cards.removeView(more);appendPage();});
        }
    }
    private void addAccount(JSONObject account,int filter){
        String name=account.optString("scientificName"),
            common=account.optString("commonName");
        LinearLayout card=new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(13),dp(11),dp(13),dp(13));
        card.setBackground(UiKit.surface(this));
        LinearLayout.LayoutParams outer=new LinearLayout.LayoutParams(-1,-2);
        outer.bottomMargin=dp(12);cards.addView(card,outer);
        card.addView(text(common.isEmpty()?name:common,16,true));
        if(!common.isEmpty()){
            TextView latin=text(name,13,false);
            latin.setTypeface(null,Typeface.ITALIC);
            card.addView(latin);
        }
        JSONArray all=conservation(account);
        int displayed=0,matched=0;
        java.util.LinkedHashMap<String,String> cited=new java.util.LinkedHashMap<>();
        for(int i=0;i<all.length();i++){
            JSONObject fact=all.optJSONObject(i);
            if(fact==null||!correctScope(fact,filter))continue;
            matched++;
            if(displayed>=3)continue;
            displayed++;
            card.addView(text(scopeLabel(fact)+" · "+fact.optString("label"),12,true));
            String url=fact.optString("url");
            String source=fact.optString("source");
            int year=fact.isNull("sourceYear")?0:fact.optInt("sourceYear",0);
            card.addView(text(ResearchCitationFormatter.citeClaim(
                Rc13FieldGuideCopy.fieldText(fact.optString("text")),
                source,year,url),14,false));
            String scope=fact.isNull("studyScope")?"":fact.optString("studyScope");
            if(!scope.isEmpty()){
                TextView note=text("Study scope: "+scope,12,false);
                note.setTextColor(UiKit.MUTED);
                card.addView(note);
            }
            if(url.startsWith("https://"))cited.put(url,source);
        }
        if(!cited.isEmpty()){
            Button sourcesToggle=UiKit.button(this,
                "References ("+cited.size()+")  ▾","quiet");
            card.addView(sourcesToggle,new LinearLayout.LayoutParams(-1,dp(48)));
            LinearLayout references=new LinearLayout(this);
            references.setOrientation(LinearLayout.VERTICAL);
            references.setVisibility(View.GONE);
            card.addView(references);
            for(java.util.Map.Entry<String,String> pair:cited.entrySet()){
                String url=pair.getKey();
                TextView citation=text(ResearchCitationFormatter.sourceRecord(
                    pair.getValue(),0,url),12,false);
                citation.setTextColor(UiKit.MUTED);
                citation.setAutoLinkMask(android.text.util.Linkify.WEB_URLS);
                citation.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());
                references.addView(citation);
            }
            sourcesToggle.setOnClickListener(v->{
                boolean expanded=references.getVisibility()!=View.VISIBLE;
                if(expanded)UiMotion.reveal(this,references);
                else UiMotion.hide(references);
                sourcesToggle.setText(expanded?"Hide references  ▴":
                    "References ("+cited.size()+")  ▾");
            });
        }
        if(matched>displayed)card.addView(text("+"+(matched-displayed)+
            " more cited notes in the full account.",12,false));
        Button accountButton=UiKit.button(this,"Open species account","secondary");
        accountButton.setContentDescription("Open full cited species account for "+name);
        LinearLayout.LayoutParams action=new LinearLayout.LayoutParams(-1,dp(50));
        action.topMargin=dp(6);card.addView(accountButton,action);
        accountButton.setOnClickListener(v->startActivity(
            new Intent(this,TaxonDetailActivity.class).putExtra("taxon_query",name)));
    }
}
