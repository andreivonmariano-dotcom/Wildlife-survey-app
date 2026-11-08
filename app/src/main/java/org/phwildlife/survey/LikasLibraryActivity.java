package org.phwildlife.survey;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** PHLibrary 2.0 landing: an accessible natural-history atlas, entirely offline. */
public final class LikasLibraryActivity extends Activity {
    private static final String[] GROUPS={"Birds","Amphibians","Reptiles","Mammals"};
    private static final int[] COUNTS={409,53,154,99};
    private static final String[] TIERS={"Common","Uncommon","Rare","Exceptionally rare","Legendary","Unranked"};
    private int dp(float n){return PhLibrary2Ui.dp(this,n);}
    private void openBook(String filter,String value){
        Intent i=new Intent(this,PhLibraryDirectoryActivity.class);
        if(filter!=null && value!=null)i.putExtra(filter,value);
        startActivity(i);
    }
    private void chooseRarity(){
        new AlertDialog.Builder(this).setTitle("Discovery Rarity")
            .setMessage("Educational discovery categories, not IUCN threat categories.")
            .setItems(TIERS,(d,which)->openBook("phlibrary_rarity",TIERS[which]))
            .setNegativeButton("Close",null).show();
    }
    private void groupCard(LinearLayout row,int idx,boolean second){
        String group=GROUPS[idx];
        LinearLayout tile=PhLibrary2Ui.panel(this,group,COUNTS[idx]+" source accounts",
            ()->openBook("phlibrary_group",group));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1);
        if(second)lp.leftMargin=dp(10);
        row.addView(tile,lp);
    }
    private void sectionCard(LinearLayout parent,String title,String description,Runnable run){
        LinearLayout card=PhLibrary2Ui.panel(this,title,description,run);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
        lp.bottomMargin=dp(10);parent.addView(card,lp);
    }
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        getWindow().setStatusBarColor(PhLibrary2Ui.PAPER);
        getWindow().setNavigationBarColor(PhLibrary2Ui.PAPER);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(PhLibrary2Ui.PAPER);
        root.setPadding(dp(16),dp(9),dp(16),0);
        LinearLayout top=new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        TextView back=PhLibrary2Ui.action(this,"Back",this::finish,false);
        top.addView(back,new LinearLayout.LayoutParams(dp(
            getResources().getConfiguration().fontScale>=1.6f?104:74),dp(48)));
        TextView label=PhLibrary2Ui.text(this,"PHLIBRARY",14,PhLibrary2Ui.FOREST,false,true);
        label.setLetterSpacing(.12f);label.setGravity(Gravity.CENTER);
        top.addView(label,new LinearLayout.LayoutParams(0,dp(48),1));
        TextView offline=PhLibrary2Ui.text(this,"OFFLINE",11,PhLibrary2Ui.MUTED,false,true);
        offline.setGravity(Gravity.CENTER_VERTICAL);
        top.addView(offline);
        root.addView(top);
        ScrollView scroll=new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setVerticalScrollBarEnabled(false);
        LinearLayout body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(1),dp(12),dp(1),dp(28));
        scroll.addView(body);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        setContentView(root);

        body.addView(PhLibrary2Ui.hero(this,"BOOK 01  /  GREATER MINDANAO",
            "Wildlife of\nGreater Mindanao",
            "715 wildlife accounts. Four groups. One connected natural-history library.",false));

        TextView findLabel=PhLibrary2Ui.eyebrow(this,"FIND IN THE ENCYCLOPEDIA");
        LinearLayout.LayoutParams flp=new LinearLayout.LayoutParams(-1,-2);
        flp.topMargin=dp(8);body.addView(findLabel,flp);
        LinearLayout searchRow=new LinearLayout(this);searchRow.setGravity(Gravity.CENTER_VERTICAL);
        EditText search=new EditText(this);
        search.setSingleLine(true);search.setTextSize(16);
        search.setHint("Species, habitats, or places");
        search.setContentDescription("Search 715 wildlife accounts");
        search.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        UiKit.fieldInput(this,search);
        searchRow.addView(search,new LinearLayout.LayoutParams(0,dp(55),1));
        TextView go=PhLibrary2Ui.action(this,"Search",()->openBook(
            "phlibrary_query",search.getText().toString().trim()),true);
        LinearLayout.LayoutParams glp=new LinearLayout.LayoutParams(-2,dp(55));
        glp.leftMargin=dp(8);searchRow.addView(go,glp);
        search.setOnEditorActionListener((v,action,event)->{
            if(action==EditorInfo.IME_ACTION_SEARCH){openBook(
                "phlibrary_query",search.getText().toString().trim());return true;}
            return false;
        });
        body.addView(searchRow);

        body.addView(PhLibrary2Ui.eyebrow(this,"THE FOUR WILDLIFE GROUPS"));
        boolean stack=getResources().getConfiguration().fontScale>=1.6f ||
            getResources().getConfiguration().screenWidthDp<330;
        for(int row=0;row<2;row++){
            LinearLayout groupRow=new LinearLayout(this);
            groupRow.setOrientation(stack?LinearLayout.VERTICAL:LinearLayout.HORIZONTAL);
            if(stack){
                for(int j=0;j<2;j++){
                    int index=row*2+j;
                    LinearLayout panel=PhLibrary2Ui.panel(this,GROUPS[index],
                        COUNTS[index]+" accounts",()->openBook("phlibrary_group",GROUPS[index]));
                    LinearLayout.LayoutParams l=new LinearLayout.LayoutParams(-1,-2);
                    l.bottomMargin=dp(10);groupRow.addView(panel,l);
                }
            }else {
                groupCard(groupRow,row*2,false);groupCard(groupRow,row*2+1,true);
            }
            LinearLayout.LayoutParams gp=new LinearLayout.LayoutParams(-1,-2);
            gp.bottomMargin=dp(stack?0:10);body.addView(groupRow,gp);
        }

        body.addView(PhLibrary2Ui.eyebrow(this,"EXPLORE MORE"));
        sectionCard(body,"Habitat atlas","Browse original literature-linked habitats and species.",
            ()->startActivity(new Intent(this,HabitatSpeciesEvidenceActivity.class)));
        sectionCard(body,"Discovery Rarity","Explore editorial rarity independently of conservation status.",
            this::chooseRarity);
        sectionCard(body,"Full bibliography","Search 1,159 offline source references.",
            ()->startActivity(new Intent(this,PhLibraryBibliographyActivity.class)));
        sectionCard(body,"My collection","Saved species, stored on this device and separate from survey records.",
            ()->openBook("phlibrary_saved_only","true"));
        TextView footer=PhLibrary2Ui.text(this,
            "Book 01 · Edition 1.82 · Discovery Rarity v0.3.4",
            12,PhLibrary2Ui.MUTED,false,false);
        footer.setPadding(dp(2),dp(5),0,0);body.addView(footer);
    }
}
