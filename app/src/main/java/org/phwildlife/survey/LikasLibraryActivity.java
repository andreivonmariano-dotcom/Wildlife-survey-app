package org.phwildlife.survey;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** A short, image-led entry to the complete offline 715-account Book 01 atlas. */
public final class LikasLibraryActivity extends Activity {
    private static final String[] GROUPS={"Birds","Amphibians","Reptiles","Mammals"};
    private int dp(float n){return PhLibrary2Ui.dp(this,n);}
    private void openAtlas(String extra,String filter){
        Intent i=new Intent(this,PhLibraryDirectoryActivity.class);
        if(extra!=null)i.putExtra(extra,filter);
        startActivity(i);
    }
    private void tile(LinearLayout body,String label,Runnable open){
        TextView v=PhLibrary2Ui.action(this,label,open,false);
        v.setGravity(Gravity.CENTER_VERTICAL|Gravity.LEFT);
        v.setTextSize(18);
        v.setPadding(dp(18),dp(10),dp(18),dp(10));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(58));
        p.bottomMargin=dp(8);body.addView(v,p);
    }
    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);
        getWindow().setStatusBarColor(PhLibrary2Ui.PAPER);
        getWindow().setNavigationBarColor(PhLibrary2Ui.PAPER);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(PhLibrary2Ui.PAPER);
        root.setPadding(dp(16),dp(8),dp(16),0);
        LinearLayout nav=new LinearLayout(this);nav.setGravity(Gravity.CENTER_VERTICAL);
        TextView back=PhLibrary2Ui.action(this,"Back",this::finish,false);
        nav.addView(back,new LinearLayout.LayoutParams(dp(
            getResources().getConfiguration().fontScale>=1.6f?100:74),dp(48)));
        TextView title=PhLibrary2Ui.text(this,"PHLibrary",15,PhLibrary2Ui.FOREST,false,true);
        title.setGravity(Gravity.CENTER);
        nav.addView(title,new LinearLayout.LayoutParams(0,dp(48),1));
        root.addView(nav);
        ScrollView scroll=new ScrollView(this);
        scroll.setFillViewport(true);scroll.setVerticalScrollBarEnabled(false);
        LinearLayout body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(0,dp(9),0,dp(26));scroll.addView(body);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        setContentView(root);

        body.addView(PhLibraryPhotoHero.create(this,"Wildlife of\nGreater Mindanao","715 species accounts"));
        TextView searchTitle=PhLibrary2Ui.text(this,"Find a species",17,PhLibrary2Ui.INK,true,true);
        searchTitle.setPadding(0,dp(17),0,dp(9));body.addView(searchTitle);
        LinearLayout bar=new LinearLayout(this);bar.setGravity(Gravity.CENTER_VERTICAL);
        EditText search=new EditText(this);search.setSingleLine(true);search.setTextSize(16);
        search.setHint("Name, family or habitat");
        search.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        search.setContentDescription("Search the 715-account species atlas");
        UiKit.fieldInput(this,search);
        bar.addView(search,new LinearLayout.LayoutParams(0,dp(54),1));
        TextView go=PhLibrary2Ui.action(this,"Search",()->
            openAtlas("phlibrary_query",search.getText().toString().trim()),true);
        LinearLayout.LayoutParams gp=new LinearLayout.LayoutParams(-2,dp(54));
        gp.leftMargin=dp(8);bar.addView(go,gp);body.addView(bar);
        search.setOnEditorActionListener((v,action,event)->{
            if(action==EditorInfo.IME_ACTION_SEARCH){
                openAtlas("phlibrary_query",search.getText().toString().trim());return true;
            }
            return false;
        });

        TextView browse=PhLibrary2Ui.text(this,"Explore",22,PhLibrary2Ui.INK,true,false);
        browse.setPadding(0,dp(20),0,dp(9));body.addView(browse);
        tile(body,"Species Atlas",()->openAtlas(null,null));
        tile(body,"Habitat Atlas",()->startActivity(
            new Intent(this,HabitatSpeciesEvidenceActivity.class)));
        tile(body,"References",()->startActivity(
            new Intent(this,PhLibraryBibliographyActivity.class)));
        tile(body,"Saved species",()->openAtlas("phlibrary_saved_only","true"));

        TextView groups=PhLibrary2Ui.text(this,"Wildlife groups",21,PhLibrary2Ui.INK,true,false);
        groups.setPadding(0,dp(14),0,dp(9));body.addView(groups);
        for(String group:GROUPS)
            tile(body,group,()->openAtlas("phlibrary_group",group));
    }
}
