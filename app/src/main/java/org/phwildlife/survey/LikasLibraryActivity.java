package org.phwildlife.survey;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * PHLibrary home, refocused as a readable natural-history book index.
 * All decorative category/book icons removed; other APO modules are unchanged.
 */
public final class LikasLibraryActivity extends Activity {
    private int dp(float n){return PhLibraryStyle.dp(this,n);}
    private void link(LinearLayout parent,String title,String detail,Class<?> target){
        View row=PhLibraryStyle.row(this,title,detail,
            ()->startActivity(new Intent(this,target)));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
        lp.bottomMargin=dp(8);
        parent.addView(row,lp);
    }
    private void discover(LinearLayout parent,String title,String detail,String view){
        View row=PhLibraryStyle.row(this,title,detail,()->
            startActivity(new Intent(this,LikasDiscoveryActivity.class)
                .putExtra("view",view)));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
        lp.bottomMargin=dp(8);
        parent.addView(row,lp);
    }
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        getWindow().setStatusBarColor(UiKit.BG);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        LinearLayout body=new LinearLayout(this);
        LinearLayout root=UiKit.page(this,"PHLibrary","Home",this::finish,body);
        ScrollView scroll=(ScrollView)root.getChildAt(1);
        scroll.setPadding(0,0,dp(7),0);
        scroll.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_INSET);

        TextView preface=PhLibraryStyle.text(this,"PHILIPPINE BIODIVERSITY LIBRARY",
            11,UiKit.MUTED,true);
        preface.setLetterSpacing(.13f);
        preface.setPadding(dp(2),dp(13),0,dp(13));
        body.addView(preface);
        View book=PhLibraryStyle.bookCover(this,()->
            startActivity(new Intent(this,PhLibraryDirectoryActivity.class)));
        body.addView(book,new LinearLayout.LayoutParams(-1,-2));

        body.addView(PhLibraryStyle.section(this,"EXPLORE THE BOOK"));
        link(body,"Search book species","Search 714 names, families and rarity tiers",
            PhLibraryDirectoryActivity.class);

        body.addView(PhLibraryStyle.section(this,"REFERENCE TOOLS"));
        link(body,"Conservation evidence","IUCN and Philippine assessments",
            Rc24ConservationDashboardActivity.class);
        link(body,"Similar species","Compare documented identification characters",
            Rc9DifferentialActivity.class);
        link(body,"Source collections","Taxonomic checklists and provenance",
            CatalogActivity.class);
        link(body,"Habitat associations","Cited species and habitat links",
            HabitatSpeciesEvidenceActivity.class);

        body.addView(PhLibraryStyle.ruleNote(this,
            "Book 01 is an educational regional reference. Species listings are not "+
            "observations at a survey site."));
        setContentView(root);
    }
}
