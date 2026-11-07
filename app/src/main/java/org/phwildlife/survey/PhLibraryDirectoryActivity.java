package org.phwildlife.survey;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Locale;

/**
 * PHLibrary Book 01: text-only species directory. No illustration, icons, photos,
 * network lookup, field record writes, or changes to scientific source content.
 */
public final class PhLibraryDirectoryActivity extends Activity {
    private static final String[] GROUPS={"All","Birds","Amphibians","Reptiles","Mammals"};
    private static final String[] TIERS={"All rarity tiers","Common","Uncommon","Rare",
        "Exceptionally rare","Legendary"};
    private static final int PAGE=24;
    private EditText search;
    private Button rarityButton;
    private LinearLayout groupChips,rows;
    private TextView resultsCount;
    private ScrollView listScroll;
    private Button showMore;
    private int selectedGroup=0,selectedRarity=0,shown=0;
    private final ArrayList<JSONObject> matches=new ArrayList<>();

    private int dp(float n){return PhLibraryStyle.dp(this,n);}
    private TextView text(String value,float sp,int color,boolean strong){
        return PhLibraryStyle.text(this,value,sp,color,strong);
    }
    private void groupSelect(int which){
        selectedGroup=which;
        groupChips.removeAllViews();
        for(int i=0;i<GROUPS.length;i++){
            final int next=i;
            boolean active=i==selectedGroup;
            TextView chip=text(GROUPS[i],14,active?Color.WHITE:UiKit.FOREST,true);
            chip.setGravity(Gravity.CENTER);
            chip.setPadding(dp(15),dp(11),dp(15),dp(11));
            chip.setMinHeight(dp(48));
            chip.setBackground(PhLibraryStyle.touch(this,
                active?UiKit.FOREST:Color.WHITE,
                active?Color.TRANSPARENT:UiKit.BORDER,15));
            chip.setContentDescription("Wildlife group "+GROUPS[i]+
                (active?", selected":""));
            chip.setClickable(true);
            chip.setFocusable(true);
            chip.setOnClickListener(v->{groupSelect(next);filter();});
            LinearLayout.LayoutParams item=new LinearLayout.LayoutParams(-2,-2);
            item.rightMargin=dp(7);
            groupChips.addView(chip,item);
        }
    }
    private void chooseRarity(){
        new AlertDialog.Builder(this).setTitle("Discovery Rarity")
            .setSingleChoiceItems(TIERS,selectedRarity,(dialog,which)->{
                selectedRarity=which;
                rarityButton.setText("Rarity: "+("All rarity tiers".equals(TIERS[which])?
                    "All":TIERS[which]));
                rarityButton.setContentDescription("Discovery Rarity filter, "+
                    TIERS[which]);
                dialog.dismiss();
                filter();
            }).setNegativeButton("Close",null).show();
    }
    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);
        getWindow().setStatusBarColor(UiKit.BG);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if(saved!=null){
            selectedGroup=Math.min(GROUPS.length-1,Math.max(0,saved.getInt("group",0)));
            selectedRarity=Math.min(TIERS.length-1,Math.max(0,saved.getInt("rarity",0)));
        }
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(UiKit.BG);
        root.setPadding(dp(16),dp(10),dp(16),dp(8));
        root.addView(PhLibraryStyle.navigation(this,"Book 01","PHLibrary",this::finish));
        TextView eyebrow=text("GREATER MINDANAO",11,UiKit.MUTED,true);
        eyebrow.setLetterSpacing(.13f);
        eyebrow.setPadding(dp(2),dp(14),0,dp(4));
        root.addView(eyebrow);
        TextView title=text("714 species",25,UiKit.INK,true);
        title.setPadding(dp(1),0,0,dp(5));
        root.addView(title);
        TextView subtitle=text("Browse the complete offline encyclopedia",13,UiKit.MUTED,false);
        subtitle.setPadding(dp(1),0,0,dp(13));
        root.addView(subtitle);

        search=new EditText(this);
        search.setSingleLine(true);
        search.setTextSize(16);
        search.setHint("Search species or family");
        search.setContentDescription("Search Greater Mindanao species, scientific names and families");
        UiKit.fieldInput(this,search);
        root.addView(search,new LinearLayout.LayoutParams(-1,-2));

        root.addView(PhLibraryStyle.section(this,"WILDLIFE GROUP"));
        HorizontalScrollView chipsScroll=new HorizontalScrollView(this);
        chipsScroll.setHorizontalScrollBarEnabled(false);
        chipsScroll.setFillViewport(false);
        groupChips=new LinearLayout(this);
        groupChips.setOrientation(LinearLayout.HORIZONTAL);
        groupSelect(selectedGroup);
        chipsScroll.addView(groupChips);
        root.addView(chipsScroll,new LinearLayout.LayoutParams(-1,-2));

        rarityButton=UiKit.button(this,"Rarity: "+
            (selectedRarity==0?"All":TIERS[selectedRarity]),"secondary");
        rarityButton.setContentDescription("Discovery Rarity filter, "+TIERS[selectedRarity]);
        rarityButton.setOnClickListener(v->chooseRarity());
        LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,-2);
        rp.topMargin=dp(10);
        root.addView(rarityButton,rp);

        resultsCount=text("",13,UiKit.MUTED,false);
        resultsCount.setPadding(dp(2),dp(13),dp(2),dp(8));
        root.addView(resultsCount);
        listScroll=new ScrollView(this);
        listScroll.setFillViewport(false);
        listScroll.setPadding(0,0,dp(5),0);
        listScroll.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_INSET);
        LinearLayout content=new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        rows=new LinearLayout(this);
        rows.setOrientation(LinearLayout.VERTICAL);
        content.addView(rows);
        showMore=UiKit.button(this,"Show more species","quiet");
        LinearLayout.LayoutParams more=new LinearLayout.LayoutParams(-1,-2);
        more.topMargin=dp(7);more.bottomMargin=dp(12);
        showMore.setMinHeight(dp(48));
        content.addView(showMore,more);
        showMore.setOnClickListener(v->nextPage());
        listScroll.addView(content);
        root.addView(listScroll,new LinearLayout.LayoutParams(-1,0,1));
        setContentView(root);

        search.setText(saved==null?"":saved.getString("query",""));
        search.addTextChangedListener(new TextWatcher(){
            @Override public void beforeTextChanged(CharSequence s,int st,int c,int a){}
            @Override public void onTextChanged(CharSequence s,int st,int before,int count){
                filter();
            }
            @Override public void afterTextChanged(Editable e){}
        });
        filter();
    }
    private void filter(){
        if(rows==null||resultsCount==null)return;
        rows.removeAllViews();matches.clear();shown=0;
        if(!PhLibraryBook01.ready(this)){
            resultsCount.setText(PhLibraryBook01.availabilityMessage(this));
            showMore.setVisibility(View.GONE);return;
        }
        String term=search.getText().toString().trim().toLowerCase(Locale.ROOT);
        for(JSONObject record:PhLibraryBook01.entries(this)){
            if(selectedGroup!=0 && !GROUPS[selectedGroup].equals(record.optString("g")))
                continue;
            if(selectedRarity!=0 && !TIERS[selectedRarity].equals(record.optString("t")))
                continue;
            if(!term.isEmpty()){
                String haystack=(record.optString("s")+" "+record.optString("c")+" "+
                    record.optString("o")+" "+record.optString("f")).toLowerCase(Locale.ROOT);
                if(!haystack.contains(term))continue;
            }
            matches.add(record);
        }
        resultsCount.setText(matches.size()+" species found");
        nextPage();
        if(listScroll!=null)listScroll.post(()->listScroll.scrollTo(0,0));
    }
    private void nextPage(){
        int end=Math.min(shown+PAGE,matches.size());
        for(int i=shown;i<end;i++){
            JSONObject row=matches.get(i);
            String latin=row.optString("s"),common=row.optString("c");
            LinearLayout item=new LinearLayout(this);
            item.setOrientation(LinearLayout.VERTICAL);
            item.setPadding(dp(15),dp(12),dp(15),dp(12));
            item.setBackground(PhLibraryStyle.touch(this,Color.WHITE,UiKit.BORDER,15));
            item.setMinimumHeight(dp(76));
            TextView title=text(common.isEmpty()?latin:common,16,UiKit.INK,true);
            item.addView(title);
            TextView scientific=text(latin,14,UiKit.MUTED,false);
            scientific.setTypeface(null,Typeface.ITALIC);
            scientific.setPadding(0,dp(3),0,dp(4));
            item.addView(scientific);
            TextView meta=text(row.optString("f")+"  ·  "+row.optString("t"),
                12,UiKit.MUTED,false);
            item.addView(meta);
            item.setContentDescription("Read "+common+", "+latin+
                ", "+row.optString("t"));
            item.setClickable(true);item.setFocusable(true);
            item.setOnClickListener(v->startActivity(
                new Intent(this,TaxonDetailActivity.class)
                    .putExtra("phlibrary_scientific_name",latin)));
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
            lp.bottomMargin=dp(7);
            rows.addView(item,lp);
        }
        shown=end;
        showMore.setVisibility(shown<matches.size()?View.VISIBLE:View.GONE);
        if(matches.isEmpty()){
            TextView empty=text("No matching species. Try another name or filter.",
                14,UiKit.MUTED,false);
            empty.setPadding(dp(2),dp(14),0,dp(16));
            rows.addView(empty);
        }
    }
    @Override protected void onSaveInstanceState(Bundle out){
        out.putInt("group",selectedGroup);
        out.putInt("rarity",selectedRarity);
        out.putString("query",search.getText().toString());
        super.onSaveInstanceState(out);
    }
}
