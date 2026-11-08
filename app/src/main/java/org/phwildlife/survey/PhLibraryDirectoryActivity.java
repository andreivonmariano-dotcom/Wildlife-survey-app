package org.phwildlife.survey;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** PHLibrary 2.0: virtualized, filterable source-faithful species explorer. */
public final class PhLibraryDirectoryActivity extends Activity {
    private static final String[] GROUPS={"All","Birds","Amphibians","Reptiles","Mammals"};
    private static final String[] TIERS={"All rarity tiers","Common","Uncommon","Rare","Exceptionally rare","Legendary","Unranked"};
    private static final String[] STATUSES={"All IUCN statuses","CR","EN","VU","NT","LC","DD","NE","Unassessed"};
    private static final String[] SORT={"Source order","Common name A–Z","Scientific name A–Z"};
    private final ArrayList<JSONObject> matches=new ArrayList<>();
    private EditText search;
    private TextView resultsCount,rarityButton,iucnButton,sortButton,savedButton;
    private LinearLayout groupChips;
    private ListView list;
    private SpeciesAdapter adapter;
    private int selectedGroup=0,selectedRarity=0,selectedIucn=0,selectedSort=0;
    private boolean savedOnly=false,restoring=false;
    private int restoreRow=0,restoreOffset=0;
    private int dp(float n){return PhLibrary2Ui.dp(this,n);}
    private TextView text(String s,int size,int color,boolean serif,boolean medium){
        return PhLibrary2Ui.text(this,s,size,color,serif,medium);
    }
    private static String lower(String s){return s==null?"":s.toLowerCase(Locale.ROOT);}
    private void goToSpecies(String latin){
        startActivity(new Intent(this,TaxonDetailActivity.class)
            .putExtra("phlibrary_scientific_name",latin));
    }
    private void setGroup(int group){
        selectedGroup=group;
        groupChips.removeAllViews();
        for(int i=0;i<GROUPS.length;i++){
            final int j=i;boolean active=i==selectedGroup;
            TextView chip=text(GROUPS[i],14,active?Color.WHITE:PhLibrary2Ui.FOREST,false,true);
            chip.setGravity(Gravity.CENTER);chip.setMinHeight(dp(48));
            chip.setPadding(dp(15),dp(9),dp(15),dp(9));
            chip.setBackground(PhLibrary2Ui.touch(this,
                active?PhLibrary2Ui.FOREST:PhLibrary2Ui.IVORY,
                active?Color.TRANSPARENT:PhLibrary2Ui.LINE,18));
            chip.setContentDescription("Wildlife group "+GROUPS[i]+(active?", selected":""));
            chip.setClickable(true);chip.setFocusable(true);
            chip.setOnClickListener(v->{setGroup(j);filter();});
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-2,-2);
            lp.rightMargin=dp(7);groupChips.addView(chip,lp);
        }
    }
    private void choose(String title,String[] choices,int selected,java.util.function.IntConsumer select){
        new AlertDialog.Builder(this).setTitle(title)
            .setSingleChoiceItems(choices,selected,(d,which)->{
                select.accept(which);d.dismiss();filter();
            }).setNegativeButton("Close",null).show();
    }
    private void filter(){
        if(adapter==null)return;
        matches.clear();
        if(!PhLibraryBook01.ready(this)){
            resultsCount.setText(PhLibraryBook01.availabilityMessage(this));
            adapter.notifyDataSetChanged();return;
        }
        String query=lower(search.getText().toString().trim());
        Set<String> bookmarks=savedOnly?PhLibraryBookmarks.all(this):null;
        for(JSONObject r:PhLibraryBook01.entries(this)){
            if(selectedGroup!=0 && !GROUPS[selectedGroup].equals(r.optString("g")))continue;
            if(selectedRarity!=0 && !TIERS[selectedRarity].equals(r.optString("t")))continue;
            if(selectedIucn!=0 && !STATUSES[selectedIucn].equals(
                    r.isNull("iucn")?"Unassessed":r.optString("iucn","Unassessed")))continue;
            if(savedOnly && !bookmarks.contains(r.optString("s")))continue;
            if(!query.isEmpty()&&!lower(r.optString("s")+" "+r.optString("c")+" "+
                    r.optString("f")+" "+r.optString("o")+" "+r.optString("p")).contains(query))
                continue;
            matches.add(r);
        }
        if(selectedSort==1)Collections.sort(matches,
            (a,b)->a.optString("c").compareToIgnoreCase(b.optString("c")));
        else if(selectedSort==2)Collections.sort(matches,
            (a,b)->a.optString("s").compareToIgnoreCase(b.optString("s")));
        else if(!query.isEmpty())Collections.sort(matches,
            Comparator.comparingInt(r->rank(r,query)));
        resultsCount.setText(matches.size()+" accounts found"+
            (savedOnly?" · Saved collection":""));
        savedButton.setText(savedOnly?"Saved only · On":"Saved only · Off");
        rarityButton.setText("Rarity: "+(selectedRarity==0?"All":TIERS[selectedRarity]));
        iucnButton.setText("IUCN: "+(selectedIucn==0?"All":STATUSES[selectedIucn]));
        sortButton.setText(selectedSort==0?"Sort: Source":"Sort: "+SORT[selectedSort]);
        adapter.notifyDataSetChanged();
        if(!restoring)list.setSelection(0);
    }
    private static int rank(JSONObject r,String q){
        String latin=lower(r.optString("s")),common=lower(r.optString("c"));
        if(latin.equals(q))return 0;
        if(common.equals(q))return 1;
        if(latin.startsWith(q))return 2;
        if(common.startsWith(q))return 3;
        if(latin.contains(q)||common.contains(q))return 4;
        if(lower(r.optString("f")+" "+r.optString("o")).contains(q))return 5;
        return 6;
    }
    private CharSequence emphasis(String source,String query){
        String low=lower(source),find=lower(query);
        if(find.length()<2)return source;
        int at=low.indexOf(find);
        if(at<0)return source;
        SpannableString span=new SpannableString(source);
        span.setSpan(new ForegroundColorSpan(PhLibrary2Ui.FOREST),at,
            Math.min(source.length(),at+find.length()),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return span;
    }
    private String snippet(JSONObject row,String q){
        if(q.isEmpty())return row.optString("f")+" · "+row.optString("t");
        String text=row.optString("p");
        int match=lower(text).indexOf(q);
        if(match>=0){
            int start=Math.max(0,match-26),end=Math.min(text.length(),match+q.length()+54);
            return "Text mention · "+(start>0?"…":"")+text.substring(start,end).trim()+
                (end<text.length()?"…":"");
        }
        return "Name or taxonomy match · "+row.optString("f")+" · "+row.optString("t");
    }
    private final class SpeciesAdapter extends BaseAdapter {
        @Override public int getCount(){return matches.size();}
        @Override public Object getItem(int i){return matches.get(i);}
        @Override public long getItemId(int i){return i;}
        @Override public View getView(int position,View reused,ViewGroup parent){
            LinearLayout card;
            TextView common,latin,meta,detail;
            if(reused instanceof LinearLayout && reused.getTag() instanceof TextView[]){
                card=(LinearLayout)reused;
                TextView[] v=(TextView[])card.getTag();
                common=v[0];latin=v[1];meta=v[2];detail=v[3];
            }else{
                card=new LinearLayout(PhLibraryDirectoryActivity.this);
                card.setOrientation(LinearLayout.VERTICAL);
                card.setPadding(dp(15),dp(13),dp(15),dp(12));
                common=text("",20,PhLibrary2Ui.INK,true,false);
                latin=text("",15,PhLibrary2Ui.MUTED,true,false);
                latin.setTypeface(Typeface.create("serif",Typeface.ITALIC));
                meta=text("",13,PhLibrary2Ui.FOREST,false,true);
                detail=text("",13,PhLibrary2Ui.MUTED,false,false);
                detail.setMaxLines(2);
                detail.setLineSpacing(dp(1),1.05f);
                LinearLayout.LayoutParams mid=new LinearLayout.LayoutParams(-1,-2);
                mid.topMargin=dp(4);
                card.addView(common);card.addView(latin,mid);
                LinearLayout.LayoutParams mp=new LinearLayout.LayoutParams(-1,-2);
                mp.topMargin=dp(5);card.addView(meta,mp);
                LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,-2);
                sp.topMargin=dp(5);card.addView(detail,sp);
                card.setTag(new TextView[]{common,latin,meta,detail});
            }
            JSONObject r=matches.get(position);
            String q=search.getText().toString().trim();
            String name=r.optString("c").isEmpty()?r.optString("s"):r.optString("c");
            common.setText(emphasis(name,q));
            latin.setText(emphasis(r.optString("s"),q));
            meta.setText(r.optString("g")+" · "+r.optString("t")+
                " · IUCN "+(r.isNull("iucn")?"Unassessed":r.optString("iucn")));
            detail.setText(emphasis(snippet(r,lower(q)),q));
            card.setBackground(PhLibrary2Ui.touch(PhLibraryDirectoryActivity.this,
                PhLibrary2Ui.IVORY,PhLibrary2Ui.LINE,13));
            card.setContentDescription("Read "+name+", "+r.optString("s")+
                ", "+r.optString("t")+", "+r.optString("g"));
            return card;
        }
    }
    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);
        getWindow().setStatusBarColor(PhLibrary2Ui.PAPER);
        getWindow().setNavigationBarColor(PhLibrary2Ui.PAPER);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if(saved==null){
            String group=getIntent().getStringExtra("phlibrary_group");
            String rarity=getIntent().getStringExtra("phlibrary_rarity");
            for(int i=1;i<GROUPS.length;i++)if(GROUPS[i].equals(group))selectedGroup=i;
            for(int i=1;i<TIERS.length;i++)if(TIERS[i].equals(rarity))selectedRarity=i;
            savedOnly=getIntent().getBooleanExtra("phlibrary_saved_only",
                "true".equals(getIntent().getStringExtra("phlibrary_saved_only")));
        }else{
            selectedGroup=saved.getInt("group");selectedRarity=saved.getInt("rarity");
            selectedIucn=saved.getInt("iucn");selectedSort=saved.getInt("sort");
            savedOnly=saved.getBoolean("savedOnly");restoring=true;
            restoreRow=saved.getInt("first");restoreOffset=saved.getInt("offset");
        }
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(PhLibrary2Ui.PAPER);
        root.setPadding(dp(15),dp(8),dp(15),dp(4));
        LinearLayout headingBar=new LinearLayout(this);headingBar.setGravity(Gravity.CENTER_VERTICAL);
        TextView back=PhLibrary2Ui.action(this,"Back",this::finish,false);
        headingBar.addView(back,new LinearLayout.LayoutParams(dp(
            getResources().getConfiguration().fontScale>=1.6f?105:74),dp(48)));
        TextView title=text("SPECIES ATLAS",14,PhLibrary2Ui.FOREST,false,true);
        title.setGravity(Gravity.CENTER);title.setLetterSpacing(.11f);
        headingBar.addView(title,new LinearLayout.LayoutParams(0,dp(48),1));
        TextView reset=PhLibrary2Ui.action(this,"Clear",()->{
            selectedRarity=0;selectedIucn=0;selectedSort=0;savedOnly=false;
            search.setText("");setGroup(0);filter();
        },false);
        headingBar.addView(reset,new LinearLayout.LayoutParams(dp(71),dp(48)));
        root.addView(headingBar);
        TextView eyebrow=PhLibrary2Ui.eyebrow(this,"GREATER MINDANAO  /  BOOK 01");
        root.addView(eyebrow);
        TextView heading=text("Explore all 715 accounts",29,PhLibrary2Ui.INK,true,false);
        heading.setPadding(0,0,0,dp(5));root.addView(heading);
        TextView sub=text("Search names, taxonomy or full article text",14,
            PhLibrary2Ui.MUTED,false,false);
        sub.setPadding(0,0,0,dp(12));root.addView(sub);

        search=new EditText(this);search.setSingleLine(true);
        search.setHint("Search species, habitats or places");
        search.setTextSize(16);search.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        search.setContentDescription("Search names, taxonomy and source text");
        UiKit.fieldInput(this,search);
        root.addView(search,new LinearLayout.LayoutParams(-1,dp(56)));
        HorizontalScrollView chips=new HorizontalScrollView(this);
        chips.setHorizontalScrollBarEnabled(true);
        chips.setContentDescription("Wildlife groups. Swipe horizontally for additional groups.");
        groupChips=new LinearLayout(this);groupChips.setOrientation(LinearLayout.HORIZONTAL);
        groupChips.setPadding(0,dp(11),0,dp(6));
        chips.addView(groupChips);root.addView(chips);
        setGroup(selectedGroup);

        boolean stacked=getResources().getConfiguration().screenWidthDp<360 ||
            getResources().getConfiguration().fontScale>=1.6f;
        LinearLayout filters=new LinearLayout(this);
        filters.setOrientation(stacked?LinearLayout.VERTICAL:LinearLayout.HORIZONTAL);
        filters.setPadding(0,dp(4),0,dp(3));
        rarityButton=PhLibrary2Ui.action(this,"Rarity",()->choose("Discovery Rarity",
            TIERS,selectedRarity,which->selectedRarity=which),false);
        iucnButton=PhLibrary2Ui.action(this,"IUCN",()->choose("IUCN global status",
            STATUSES,selectedIucn,which->selectedIucn=which),false);
        LinearLayout.LayoutParams fp1=new LinearLayout.LayoutParams(stacked?-1:0,dp(48),1);
        filters.addView(rarityButton,fp1);
        LinearLayout.LayoutParams fp2=new LinearLayout.LayoutParams(stacked?-1:0,dp(48),1);
        if(stacked)fp2.topMargin=dp(6);else fp2.leftMargin=dp(7);
        filters.addView(iucnButton,fp2);root.addView(filters);

        LinearLayout tools=new LinearLayout(this);tools.setGravity(Gravity.CENTER_VERTICAL);
        sortButton=PhLibrary2Ui.action(this,"Sort: Source",
            ()->choose("Order species",SORT,selectedSort,which->selectedSort=which),false);
        savedButton=PhLibrary2Ui.action(this,"Saved only · Off",()->{savedOnly=!savedOnly;filter();},false);
        LinearLayout.LayoutParams sl=new LinearLayout.LayoutParams(0,dp(48),1);tools.addView(sortButton,sl);
        LinearLayout.LayoutParams bl=new LinearLayout.LayoutParams(0,dp(48),1);bl.leftMargin=dp(7);
        tools.addView(savedButton,bl);root.addView(tools);

        resultsCount=text("",14,PhLibrary2Ui.FOREST,false,true);
        resultsCount.setPadding(dp(1),dp(11),0,dp(8));root.addView(resultsCount);
        list=new ListView(this);list.setDividerHeight(dp(8));
        list.setDivider(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
        list.setCacheColorHint(Color.TRANSPARENT);
        list.setBackgroundColor(PhLibrary2Ui.PAPER);
        list.setClipToPadding(false);
        list.setPadding(0,0,0,dp(12));
        adapter=new SpeciesAdapter();list.setAdapter(adapter);
        list.setOnItemClickListener((parent,v,index,id)->
            goToSpecies(matches.get(index).optString("s")));
        root.addView(list,new LinearLayout.LayoutParams(-1,0,1));
        setContentView(root);
        search.setText(saved!=null?saved.getString("query",""):
            getIntent().getStringExtra("phlibrary_query")==null?"":getIntent().getStringExtra("phlibrary_query"));
        search.addTextChangedListener(new TextWatcher(){
            public void beforeTextChanged(CharSequence s,int start,int count,int after){}
            public void onTextChanged(CharSequence s,int start,int before,int count){filter();}
            public void afterTextChanged(Editable e){}
        });
        filter();
        if(restoring){
            list.post(()->list.setSelectionFromTop(restoreRow,restoreOffset));
            restoring=false;
        }
    }
    @Override protected void onSaveInstanceState(Bundle out){
        out.putInt("group",selectedGroup);out.putInt("rarity",selectedRarity);
        out.putInt("iucn",selectedIucn);out.putInt("sort",selectedSort);
        out.putBoolean("savedOnly",savedOnly);
        out.putString("query",search.getText().toString());
        out.putInt("first",list.getFirstVisiblePosition());
        View first=list.getChildAt(0);out.putInt("offset",first==null?0:first.getTop());
        super.onSaveInstanceState(out);
    }
    @Override protected void onResume(){super.onResume();if(adapter!=null && savedOnly)filter();}
}
