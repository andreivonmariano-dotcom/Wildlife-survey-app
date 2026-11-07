package org.phwildlife.survey;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Offline, read-only directory for the full 714-entry Book 01 edition. */
public final class PhLibraryDirectoryActivity extends Activity {
    private static final String[] GROUPS={"All wildlife","Birds","Amphibians","Reptiles","Mammals"};
    private static final String[] TIERS={"All rarity tiers","Common","Uncommon","Rare",
        "Exceptionally rare","Legendary"};
    private static final int PAGE_SIZE=24;
    private EditText search;
    private Spinner group,rarity;
    private TextView summary;
    private LinearLayout results;
    private ScrollView scroll;
    private Button more;
    private final ArrayList<JSONObject> selected=new ArrayList<>();
    private int shown=0;

    private int dp(float amount) {
        return Math.round(amount*getResources().getDisplayMetrics().density);
    }
    private TextView text(String value,float sp,int color,boolean bold) {
        TextView view=new TextView(this);
        view.setText(value);
        view.setTextSize(sp);
        view.setTextColor(color);
        if(bold)view.setTypeface(null,Typeface.BOLD);
        return view;
    }
    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(UiKit.BG);
        root.setPadding(dp(16),dp(10),dp(16),dp(9));
        root.addView(UiKit.navigationHeader(this,"PHLibrary","Library",this::finish));
        TextView title=text("Greater Mindanao",23,UiKit.INK,true);
        title.setPadding(0,dp(10),0,dp(4));root.addView(title);
        TextView caption=text("BOOK 01  ·  714 SPECIES  ·  EDITION 1.75",
            12,UiKit.MUTED,false);
        caption.setPadding(0,0,0,dp(11));root.addView(caption);

        search=new EditText(this);
        search.setTextSize(16);
        search.setSingleLine(true);
        search.setHint(getResources().getConfiguration().fontScale>=1.6f?
            "Search species":"Search name, family or order");
        search.setContentDescription("Search 714 Greater Mindanao species");
        root.addView(search,new LinearLayout.LayoutParams(-1,dp(54)));
        LinearLayout filters=new LinearLayout(this);
        boolean stacked=getResources().getConfiguration().screenWidthDp<390 ||
            getResources().getConfiguration().fontScale>=1.3f;
        filters.setOrientation(stacked?LinearLayout.VERTICAL:LinearLayout.HORIZONTAL);
        group=new Spinner(this);
        rarity=new Spinner(this);
        group.setContentDescription("Filter wildlife group");
        rarity.setContentDescription("Filter Discovery Rarity");
        group.setAdapter(new ArrayAdapter<String>(this,
            android.R.layout.simple_spinner_dropdown_item,GROUPS));
        rarity.setAdapter(new ArrayAdapter<String>(this,
            android.R.layout.simple_spinner_dropdown_item,TIERS));
        LinearLayout.LayoutParams gp=stacked?
            new LinearLayout.LayoutParams(-1,dp(50)):
            new LinearLayout.LayoutParams(0,dp(50),1f);
        filters.addView(group,gp);
        LinearLayout.LayoutParams rp=stacked?
            new LinearLayout.LayoutParams(-1,dp(50)):
            new LinearLayout.LayoutParams(0,dp(50),1f);
        if(!stacked)rp.leftMargin=dp(8);
        filters.addView(rarity,rp);
        filters.setPadding(0,dp(5),0,0);
        root.addView(filters);
        summary=text("",13,UiKit.MUTED,false);
        summary.setPadding(0,dp(8),0,dp(9));root.addView(summary);

        scroll=new ScrollView(this);
        scroll.setFillViewport(false);
        LinearLayout body=new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        results=new LinearLayout(this);
        results.setOrientation(LinearLayout.VERTICAL);
        body.addView(results);
        more=UiKit.button(this,"Show more species","secondary");
        LinearLayout.LayoutParams nextLp=new LinearLayout.LayoutParams(-1,dp(54));
        nextLp.topMargin=dp(8);nextLp.bottomMargin=dp(8);
        body.addView(more,nextLp);
        more.setOnClickListener(v->appendPage());
        scroll.addView(body);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1f));
        setContentView(root);
        AdapterView.OnItemSelectedListener filterListener=new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent,View v,int pos,long id) {
                filter();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        };
        group.setOnItemSelectedListener(filterListener);
        rarity.setOnItemSelectedListener(filterListener);
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int start,int count,int after){}
            @Override public void onTextChanged(CharSequence s,int start,int before,int count) {
                filter();
            }
            @Override public void afterTextChanged(Editable value){}
        });
        filter();
    }
    private void filter() {
        if(results==null || group==null || rarity==null || search==null)return;
        selected.clear();
        results.removeAllViews();
        shown=0;
        if(!PhLibraryBook01.ready(this)) {
            summary.setText(PhLibraryBook01.availabilityMessage(this));
            more.setVisibility(View.GONE);
            return;
        }
        String searchText=search.getText().toString().trim().toLowerCase(Locale.ROOT);
        String groupChoice=GROUPS[group.getSelectedItemPosition()];
        String rarityChoice=TIERS[rarity.getSelectedItemPosition()];
        List<JSONObject> all=PhLibraryBook01.entries(this);
        for(JSONObject record:all) {
            if(!"All wildlife".equals(groupChoice) &&
                !groupChoice.equals(record.optString("g")))continue;
            if(!"All rarity tiers".equals(rarityChoice) &&
                !rarityChoice.equals(record.optString("t")))continue;
            if(!searchText.isEmpty()) {
                String searchable=(record.optString("c")+" "+record.optString("s")+" "+
                    record.optString("f")+" "+record.optString("o")).toLowerCase(Locale.ROOT);
                if(!searchable.contains(searchText))continue;
            }
            selected.add(record);
        }
        summary.setText(selected.size()+" species · Greater Mindanao · Discovery Rarity v0.3.1");
        appendPage();
        if(scroll!=null)scroll.post(()->scroll.scrollTo(0,0));
    }
    private void appendPage() {
        int end=Math.min(shown+PAGE_SIZE,selected.size());
        for(int i=shown;i<end;i++) {
            JSONObject record=selected.get(i);
            String scientific=record.optString("s");
            LinearLayout card=new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(13),dp(12),dp(13),dp(12));
            card.setBackground(UiKit.surface(this));
            card.setClickable(true);card.setFocusable(true);
            card.setMinimumHeight(dp(76));
            String common=record.optString("c");
            TextView heading=text(common.isEmpty()?scientific:common,16,UiKit.INK,true);
            card.addView(heading);
            TextView latin=text(scientific,14,UiKit.MUTED,false);
            latin.setTypeface(null,Typeface.ITALIC);
            latin.setPadding(0,dp(3),0,dp(4));card.addView(latin);
            TextView tags=text(record.optString("g")+" · "+record.optString("f")+
                " · "+record.optString("t"),12,UiKit.MUTED,false);
            card.addView(tags);
            card.setContentDescription("Read "+common+"; "+scientific+
                "; "+record.optString("t"));
            card.setOnClickListener(v->startActivity(
                new Intent(this,TaxonDetailActivity.class)
                    .putExtra("phlibrary_scientific_name",scientific)));
            LinearLayout.LayoutParams spacing=new LinearLayout.LayoutParams(-1,-2);
            spacing.bottomMargin=dp(8);
            results.addView(card,spacing);
        }
        shown=end;
        more.setVisibility(shown<selected.size()?View.VISIBLE:View.GONE);
        if(selected.isEmpty()) {
            TextView empty=text("No matching species in Book 01.",14,UiKit.MUTED,false);
            empty.setPadding(0,dp(10),0,dp(10));
            results.addView(empty);
        }
    }
}
