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
    private int dp(float n){return PhLibrary2Ui.dp(this,n);}

    /** Return from Living Atlas to the one canonical workspace without duplicating its state. */
    private void workspace(String target){
        Intent intent=new Intent(this,WelcomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("workspace_destination",target);
        startActivity(intent);
        finish();
    }
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
        boolean largeText=getResources().getConfiguration().fontScale>=1.6f;
        TextView back=PhLibrary2Ui.action(this,"Back",this::finish,false);
        if(largeText){
            back.setTextSize(12);back.setPadding(dp(7),dp(5),dp(7),dp(5));
        }
        nav.addView(back,new LinearLayout.LayoutParams(dp(
            getResources().getConfiguration().fontScale>=1.6f?100:74),dp(48)));
        TextView title=PhLibrary2Ui.text(this,"Living Atlas",15,PhLibrary2Ui.FOREST,false,true);
        title.setGravity(Gravity.CENTER);
        if(getResources().getConfiguration().fontScale>=1.6f)title.setTextSize(12);
        nav.addView(title,new LinearLayout.LayoutParams(0,dp(48),1));
        root.addView(nav);
        ScrollView scroll=new ScrollView(this);
        scroll.setFillViewport(true);scroll.setVerticalScrollBarEnabled(false);
        LinearLayout body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(0,dp(9),0,dp(26));scroll.addView(body);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        root.addView(ApoShellBar.create(this,"atlas",
            ()->workspace("home"),()->workspace("fieldwork"),
            ()->scroll.smoothScrollTo(0,0),()->workspace("data")));
        setContentView(root);

        body.addView(PhLibraryPhotoHero.create(this,"Wildlife of\nGreater Mindanao","715 species accounts"));
        View searchGap=new View(this);
        body.addView(searchGap,new LinearLayout.LayoutParams(-1,dp(17)));
        LinearLayout bar=new LinearLayout(this);
        bar.setOrientation(largeText?LinearLayout.VERTICAL:LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        EditText search=new EditText(this);search.setSingleLine(true);search.setTextSize(16);
        search.setHint("Search species");
        search.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        search.setContentDescription("Search the 715-account species atlas");
        UiKit.fieldInput(this,search);
        bar.addView(search,largeText?
            new LinearLayout.LayoutParams(-1,dp(58)):
            new LinearLayout.LayoutParams(0,dp(54),1));
        TextView go=PhLibrary2Ui.action(this,"Search",()->
            openAtlas("phlibrary_query",search.getText().toString().trim()),true);
        if(largeText){go.setTextSize(12);go.setPadding(dp(9),dp(5),dp(9),dp(5));}
        LinearLayout.LayoutParams gp=new LinearLayout.LayoutParams(
            largeText?-1:-2,dp(largeText?56:54));
        if(largeText)gp.topMargin=dp(7);
        else gp.leftMargin=dp(8);
        bar.addView(go,gp);body.addView(bar);
        search.setOnEditorActionListener((v,action,event)->{
            if(action==EditorInfo.IME_ACTION_SEARCH){
                openAtlas("phlibrary_query",search.getText().toString().trim());return true;
            }
            return false;
        });

        View tileGap=new View(this);
        body.addView(tileGap,new LinearLayout.LayoutParams(-1,dp(16)));
        body.addView(PhLibrary2Ui.panel(this,"Explore connections",
            "Species, taxonomy, habitats and references",()->
                startActivity(new Intent(this,LivingAtlasExploreActivity.class))),
            new LinearLayout.LayoutParams(-1,-2));
        View connectionsGap=new View(this);
        body.addView(connectionsGap,new LinearLayout.LayoutParams(-1,dp(10)));
        tile(body,"Species Atlas",()->openAtlas(null,null));
        tile(body,"Habitat Atlas",()->startActivity(
            new Intent(this,HabitatSpeciesEvidenceActivity.class)));
        tile(body,"References",()->startActivity(
            new Intent(this,PhLibraryBibliographyActivity.class)));
        tile(body,"Saved species",()->openAtlas("phlibrary_saved_only","true"));
        tile(body,"Offline Map",()->
            startActivity(new Intent(this,ApoOfflineMapActivity.class)));
        // Native photos ship inside the signed APK; no photo import is needed.

    }
}
