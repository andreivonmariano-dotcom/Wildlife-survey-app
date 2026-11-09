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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import android.text.InputType;

/** PHLibrary 2.0: virtualized, filterable source-faithful species explorer. */
public final class PhLibraryDirectoryActivity extends Activity {
    private static final String[] GROUPS={"All","Birds","Amphibians","Reptiles","Mammals"};
    private static final String[] TIERS={"All rarity tiers","Common","Uncommon","Rare","Exceptionally rare","Legendary","Unranked"};
    private static final String[] STATUSES={"All IUCN statuses","CR","EN","VU","NT","LC","DD","NE","Unassessed"};
    private static final String[] SORT={"Source order","Common name A–Z","Scientific name A–Z"};
    private static final String[] HABITATS={"Any habitat mention","Lowland forest","Montane forest",
        "Mossy forest","Limestone and karst","Ultramafic forest","Mangrove",
        "Freshwater wetland","Riparian forest","Secondary forest","Subalpine forest",
        "Beach forest","Swamp forest","Peat swamp forest","Semi-evergreen forest","Deciduous forest"};
    private static final String[] HABITAT_PHRASES={"","lowland forest","montane forest",
        "mossy forest","limestone","ultramafic","mangrove",
        "wetland","riparian","secondary forest","subalpine",
        "beach forest","swamp forest","peat swamp","semi-evergreen","deciduous forest"};
    private String family="",genus="";
    private int selectedHabitat=0;
    private final ArrayList<JSONObject> matches=new ArrayList<>();
    private EditText search;
    private TextView resultsCount,sortButton,filterButton,resetButton;
    private LinearLayout groupChips;
    private ListView list;
    private SpeciesAdapter adapter;
    private int selectedGroup=0,selectedRarity=0,selectedIucn=0,selectedSort=0;
    private boolean savedOnly=false,restoring=false,compactFilters=false;
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
            chip.setGravity(Gravity.CENTER);chip.setMinHeight(dp(44));
            chip.setPadding(dp(12),dp(7),dp(12),dp(7));
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
    private void chooseString(String title,boolean byFamily){
        TreeSet<String> terms=new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for(JSONObject row:PhLibraryBook01.entries(this)){
            String name=byFamily?row.optString("f"):row.optString("s").split(" ")[0];
            if(!name.isEmpty())terms.add(name);
        }
        ArrayList<String> items=new ArrayList<>();items.add("Any");
        items.addAll(terms);
        LinearLayout content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(14),dp(6),dp(14),dp(6));
        EditText find=new EditText(this);find.setSingleLine(true);
        find.setTextSize(16);find.setHint("Search "+title.toLowerCase(Locale.ROOT));
        find.setInputType(InputType.TYPE_CLASS_TEXT);
        content.addView(find,new LinearLayout.LayoutParams(-1,dp(52)));
        ListView options=new ListView(this);
        ArrayAdapter<String> values=new ArrayAdapter<>(this,
            android.R.layout.simple_list_item_1,new ArrayList<>(items));
        options.setAdapter(values);
        content.addView(options,new LinearLayout.LayoutParams(-1,dp(340)));
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle(title)
            .setView(content).setNegativeButton("Close",null).create();
        find.addTextChangedListener(new TextWatcher(){
            public void beforeTextChanged(CharSequence s,int start,int count,int after){}
            public void onTextChanged(CharSequence s,int start,int before,int count){
                values.getFilter().filter(s);
            }
            public void afterTextChanged(Editable e){}
        });
        options.setOnItemClickListener((parent,view,position,id)->{
            String chosen=String.valueOf(parent.getItemAtPosition(position));
            if("Any".equals(chosen))chosen="";
            if(byFamily)family=chosen;else genus=chosen;
            dialog.dismiss();filter();
        });
        dialog.getWindow();
        dialog.show();
    }
    private void clearFilters(){
        selectedGroup=0;selectedRarity=0;selectedIucn=0;selectedSort=0;
        family="";genus="";selectedHabitat=0;savedOnly=false;
        search.setText("");setGroup(0);filter();
    }
    private void chooseCompactFilters(){
        String[] options={
            "Family: "+(family.isEmpty()?"Any":family),
            "Genus: "+(genus.isEmpty()?"Any":genus),
            "Habitat mention: "+(selectedHabitat==0?"Any":HABITATS[selectedHabitat]),
            "Discovery Rarity: "+(selectedRarity==0?"Any":TIERS[selectedRarity]),
            "IUCN: "+(selectedIucn==0?"Any":PhLibraryStatusBadge.status(STATUSES[selectedIucn])),
            savedOnly?"Saved species: Only saved":"Saved species: All"
        };
        new AlertDialog.Builder(this).setTitle("Filter species")
            .setItems(options,(d,index)->{
                switch(index){
                    case 0:chooseString("Family",true);break;
                    case 1:chooseString("Genus",false);break;
                    case 2:choose("Habitat mentions in Book 01",HABITATS,
                        selectedHabitat,i->selectedHabitat=i);break;
                    case 3:choose("Discovery Rarity",TIERS,selectedRarity,
                        i->selectedRarity=i);break;
                    case 4:choose("IUCN global status",STATUSES,selectedIucn,
                        i->selectedIucn=i);break;
                    case 5:savedOnly=!savedOnly;filter();break;
                    default:break;
                }
            }).setNegativeButton("Close",null)
            .setNeutralButton("About habitat mentions",(d,w)->
                new AlertDialog.Builder(this).setTitle("Habitat text search")
                    .setMessage("A habitat filter searches the original account wording. A text match does not establish occupancy, habitat selection, or occurrence at a survey site.")
                    .setPositiveButton("Close",null).show()).show();
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
            if(!family.isEmpty()&&!family.equalsIgnoreCase(r.optString("f")))continue;
            if(!genus.isEmpty()&&!genus.equalsIgnoreCase(r.optString("s").split(" ")[0]))continue;
            if(selectedHabitat>0&&!lower(r.optString("p")).contains(
                HABITAT_PHRASES[selectedHabitat]))continue;
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
        resultsCount.setText(matches.isEmpty()?(savedOnly?"No saved matches":"No matching accounts"):
            matches.size()+(matches.size()==1?" account":" accounts")+
                (savedOnly?" · Saved":""));
        int active=(selectedRarity!=0?1:0)+(selectedIucn!=0?1:0)+
            (selectedHabitat!=0?1:0)+(family.isEmpty()?0:1)+
            (genus.isEmpty()?0:1)+(savedOnly?1:0);
        filterButton.setText(active==0?"Filters":"Filters · "+active);
        sortButton.setText("Sort");
        if(resetButton!=null)resetButton.setVisibility(
            selectedGroup!=0||selectedRarity!=0||selectedIucn!=0||
            selectedSort!=0||selectedHabitat!=0||!family.isEmpty()||
            !genus.isEmpty()||savedOnly||!query.isEmpty()?View.VISIBLE:View.GONE);
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
        if(q.isEmpty())return "Family · "+row.optString("f");
        String text=row.optString("p");
        int match=lower(text).indexOf(q);
        if(match>=0){
            int start=Math.max(0,match-26),end=Math.min(text.length(),match+q.length()+54);
            return "Text mention · "+(start>0?"…":"")+text.substring(start,end).trim()+
                (end<text.length()?"…":"");
        }
        return "Name or taxonomy match · "+row.optString("f");
    }
    private final class SpeciesAdapter extends BaseAdapter {
        private final class Holder {
            TextView common,latin,family,snippet;
            LinearLayout badges;
        }
        @Override public int getCount(){return matches.size();}
        @Override public Object getItem(int i){return matches.get(i);}
        @Override public long getItemId(int i){return i;}
        @Override public View getView(int position,View reused,ViewGroup parent){
            LinearLayout card;Holder h;
            if(reused instanceof LinearLayout && reused.getTag() instanceof Holder){
                card=(LinearLayout)reused;h=(Holder)card.getTag();
            }else{
                card=new LinearLayout(PhLibraryDirectoryActivity.this);
                card.setOrientation(LinearLayout.VERTICAL);
                card.setPadding(dp(14),dp(11),dp(14),dp(10));
                h=new Holder();
                h.common=text("",18,PhLibrary2Ui.INK,true,false);
                h.latin=text("",14,PhLibrary2Ui.MUTED,true,false);
                h.latin.setTypeface(Typeface.create("serif",Typeface.ITALIC));
                h.family=text("",12,PhLibrary2Ui.MUTED,false,false);
                h.badges=new LinearLayout(PhLibraryDirectoryActivity.this);
                h.badges.setOrientation(LinearLayout.HORIZONTAL);
                h.snippet=text("",12,PhLibrary2Ui.MUTED,false,false);
                h.snippet.setMaxLines(2);
                card.addView(h.common);
                LinearLayout.LayoutParams ln=new LinearLayout.LayoutParams(-1,-2);
                ln.topMargin=dp(2);card.addView(h.latin,ln);
                LinearLayout.LayoutParams fm=new LinearLayout.LayoutParams(-1,-2);
                fm.topMargin=dp(3);card.addView(h.family,fm);
                LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,-2);
                bp.topMargin=dp(7);card.addView(h.badges,bp);
                LinearLayout.LayoutParams sn=new LinearLayout.LayoutParams(-1,-2);
                sn.topMargin=dp(6);card.addView(h.snippet,sn);
                card.setTag(h);
            }
            JSONObject row=matches.get(position);
            String q=search.getText().toString().trim();
            String common=row.optString("c").isEmpty()?row.optString("s"):row.optString("c");
            h.common.setText(emphasis(common,q));
            h.latin.setText(emphasis(row.optString("s"),q));
            h.family.setText(row.optString("f")+" · "+row.optString("g"));
            h.badges.removeAllViews();
            TextView rarity=PhLibraryStatusBadge.make(PhLibraryDirectoryActivity.this,
                row.optString("t"),true);
            TextView conservation=PhLibraryStatusBadge.make(PhLibraryDirectoryActivity.this,
                row.isNull("iucn")?"Unassessed":row.optString("iucn"),false);
            // Keep ListView rows tappable; status explanations live on the species profile.
            rarity.setClickable(false);rarity.setFocusable(false);
            conservation.setClickable(false);conservation.setFocusable(false);
            boolean enlarged=getResources().getConfiguration().fontScale>=1.6f;
            // Keep a complete readable rarity name on a 320dp/200%-font result row.
            rarity.setTextSize(enlarged?9:11);conservation.setTextSize(enlarged?9:11);
            rarity.setPadding(dp(enlarged?5:8),dp(5),dp(enlarged?5:8),dp(5));
            conservation.setPadding(dp(enlarged?5:8),dp(5),dp(enlarged?5:8),dp(5));
            LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(0,-2,enlarged?.44f:1f);
            h.badges.addView(rarity,rp);
            LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,-2,enlarged?.56f:1f);
            cp.leftMargin=dp(7);h.badges.addView(conservation,cp);
            if(q.isEmpty())h.snippet.setVisibility(View.GONE);
            else {
                h.snippet.setVisibility(View.VISIBLE);
                h.snippet.setText(emphasis(snippet(row,lower(q)),q));
            }
            card.setBackground(PhLibrary2Ui.touch(PhLibraryDirectoryActivity.this,
                PhLibrary2Ui.IVORY,PhLibrary2Ui.LINE,13));
            card.setContentDescription("Read "+common+", "+row.optString("s")+
                ", "+row.optString("t")+", IUCN "+
                PhLibraryStatusBadge.status(row.isNull("iucn")?"Unassessed":row.optString("iucn")));
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
        compactFilters=getResources().getConfiguration().fontScale>=1.6f ||
            getResources().getConfiguration().screenWidthDp<350;
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
            family=saved.getString("family","");genus=saved.getString("genus","");
            selectedHabitat=saved.getInt("habitat",0);
            if(selectedHabitat<0||selectedHabitat>=HABITATS.length)selectedHabitat=0;
            restoreRow=saved.getInt("first");restoreOffset=saved.getInt("offset");
        }
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(PhLibrary2Ui.PAPER);
        root.setPadding(dp(15),dp(8),dp(15),dp(4));
        LinearLayout headingBar=new LinearLayout(this);headingBar.setGravity(Gravity.CENTER_VERTICAL);
        TextView back=PhLibrary2Ui.action(this,"Back",this::finish,false);
        if(compactFilters){back.setTextSize(12);back.setPadding(dp(6),dp(5),dp(6),dp(5));}
        headingBar.addView(back,new LinearLayout.LayoutParams(dp(
            compactFilters?83:74),dp(48)));
        TextView title=text(compactFilters?"Species":"Species Atlas",
            compactFilters?12:14,PhLibrary2Ui.FOREST,false,true);
        title.setGravity(Gravity.CENTER);title.setLetterSpacing(.11f);
        headingBar.addView(title,new LinearLayout.LayoutParams(0,dp(48),1));
        resetButton=PhLibrary2Ui.action(this,"Clear",this::clearFilters,false);
        if(compactFilters){resetButton.setTextSize(12);resetButton.setPadding(dp(6),dp(5),dp(6),dp(5));}
        headingBar.addView(resetButton,new LinearLayout.LayoutParams(dp(compactFilters?82:71),dp(48)));
        root.addView(headingBar);
        View searchGap=new View(this);
        root.addView(searchGap,new LinearLayout.LayoutParams(-1,dp(8)));

        search=new EditText(this);search.setSingleLine(true);
        search.setHint(compactFilters?"Search":"Search species, habitats or places");
        search.setTextSize(16);search.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        search.setContentDescription("Search names, taxonomy and source text");
        UiKit.fieldInput(this,search);
        root.addView(search,new LinearLayout.LayoutParams(-1,dp(56)));
        HorizontalScrollView chips=new HorizontalScrollView(this);
        chips.setHorizontalScrollBarEnabled(true);
        chips.setContentDescription("Wildlife groups. Swipe horizontally for additional groups.");
        groupChips=new LinearLayout(this);groupChips.setOrientation(LinearLayout.HORIZONTAL);
        groupChips.setPadding(0,dp(6),0,dp(3));
        chips.addView(groupChips);root.addView(chips);
        setGroup(selectedGroup);

        // All advanced filters stay in one searchable sheet.
        LinearLayout tools=new LinearLayout(this);tools.setGravity(Gravity.CENTER_VERTICAL);
        filterButton=PhLibrary2Ui.action(this,"Filters",this::chooseCompactFilters,false);
        sortButton=PhLibrary2Ui.action(this,"Sort",
            ()->choose("Sort species",SORT,selectedSort,i->selectedSort=i),false);
        if(compactFilters){
            filterButton.setTextSize(12);sortButton.setTextSize(12);
            // 200% system font must fit inside the 48dp toolbar height.
            filterButton.setPadding(dp(6),dp(5),dp(6),dp(5));
            sortButton.setPadding(dp(6),dp(5),dp(6),dp(5));
        }
        LinearLayout.LayoutParams fl=new LinearLayout.LayoutParams(0,dp(48),1);
        tools.addView(filterButton,fl);
        LinearLayout.LayoutParams sl=new LinearLayout.LayoutParams(0,dp(48),1);
        sl.leftMargin=dp(8);tools.addView(sortButton,sl);
        tools.setPadding(0,dp(2),0,0);root.addView(tools);

        resultsCount=text("",14,PhLibrary2Ui.FOREST,false,true);
        resultsCount.setPadding(dp(1),dp(7),0,dp(7));root.addView(resultsCount);
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
        out.putString("family",family);out.putString("genus",genus);
        out.putInt("habitat",selectedHabitat);
        out.putString("query",search.getText().toString());
        out.putInt("first",list.getFirstVisiblePosition());
        View first=list.getChildAt(0);out.putInt("offset",first==null?0:first.getTop());
        super.onSaveInstanceState(out);
    }
    @Override protected void onResume(){
        super.onResume();
        if(adapter!=null && savedOnly){
            int position=list.getFirstVisiblePosition();
            View first=list.getChildAt(0);
            int offset=first==null?0:first.getTop();
            restoring=true;
            filter();
            list.post(()->list.setSelectionFromTop(position,offset));
            restoring=false;
        }
    }
}
