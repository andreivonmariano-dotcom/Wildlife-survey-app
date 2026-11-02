package org.phwildlife.survey;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** Offline Philippine biodiversity reference hub. Never creates a field observation. */
public final class LikasLibraryActivity extends Activity {
    private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
    private void add(LinearLayout parent,String title,String detail,String icon,
            Class<?> target){
        View card=LikasPortalUi.action(this,title,detail,icon,
            ()->startActivity(new Intent(this,target)));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
        lp.bottomMargin=dp(LikasDesignSystem.SPACE_SM);
        parent.addView(card,lp);
    }
    private void discover(LinearLayout parent,String title,String detail,
            String icon,String view){
        View card=LikasPortalUi.libraryFeature(this,title,detail,icon,()->
            startActivity(new Intent(this,LikasDiscoveryActivity.class)
                .putExtra("view",view)));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
        lp.bottomMargin=dp(LikasDesignSystem.SPACE_SM);
        parent.addView(card,lp);
    }
    private LinearLayout section(String title,String caption){
        LinearLayout block=FieldVisuals.section(this,title,caption);
        TextView heading=(TextView)block.getChildAt(0);
        heading.setTextSize(16);
        heading.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));
        heading.setTextColor(UiKit.FOREST);
        return block;
    }
    private void referencePair(LinearLayout parent,String first,String firstDetail,
            String firstIcon,Class<?> firstTarget,String second,String secondDetail,
            String secondIcon,Class<?> secondTarget){
        // Android phones use full-width readable targets. Only wider tablets get pairs.
        boolean stacked=getResources().getConfiguration().screenWidthDp<600 ||
            FieldLayoutPolicy.stackPortalPair(
                getResources().getConfiguration().screenWidthDp,
                getResources().getConfiguration().fontScale);
        LinearLayout row=new LinearLayout(this);
        row.setOrientation(stacked?LinearLayout.VERTICAL:LinearLayout.HORIZONTAL);
        View a=stacked?LikasPortalUi.action(this,first,firstDetail,firstIcon,
            ()->startActivity(new Intent(this,firstTarget))):
            LikasPortalUi.mini(this,first,firstDetail,firstIcon,
            ()->startActivity(new Intent(this,firstTarget)));
        View b=stacked?LikasPortalUi.action(this,second,secondDetail,secondIcon,
            ()->startActivity(new Intent(this,secondTarget))):
            LikasPortalUi.mini(this,second,secondDetail,secondIcon,
            ()->startActivity(new Intent(this,secondTarget)));
        LinearLayout.LayoutParams firstLp=new LinearLayout.LayoutParams(
            stacked?-1:0,-2,stacked?0:1f);
        firstLp.bottomMargin=dp(LikasDesignSystem.SPACE_SM);
        row.addView(a,firstLp);
        LinearLayout.LayoutParams secondLp=new LinearLayout.LayoutParams(
            stacked?-1:0,-2,stacked?0:1f);
        if(!stacked)secondLp.leftMargin=dp(LikasDesignSystem.SPACE_SM);
        secondLp.bottomMargin=dp(LikasDesignSystem.SPACE_SM);
        row.addView(b,secondLp);
        parent.addView(row);
    }
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        getWindow().setStatusBarColor(UiKit.BG);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        LinearLayout body=new LinearLayout(this);
        LinearLayout root=UiKit.page(this,"PHLibrary","Home",this::finish,body);
        LinearLayout nav=(LinearLayout)root.getChildAt(0);
        TextView pageTitle=(TextView)nav.getChildAt(1);
        pageTitle.setTextSize(LikasDesignSystem.TEXT_PAGE);
        ScrollView scroller=(ScrollView)root.getChildAt(1);
        scroller.setPadding(0,0,dp(8),0);
        scroller.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_INSET);

        TextView context=new TextView(this);
        context.setText("Offline species, habitat and conservation references");
        context.setTextSize(14);
        context.setTextColor(UiKit.MUTED);
        context.setPadding(dp(2),dp(2),dp(2),dp(14));
        body.addView(context);

        body.addView(section("Species checklist","Start with the source-linked taxon index"));
        add(body,"Search species checklist",
            "Greater Mindanao PAIC species · names, sources and coverage",
            "species",Rc7SpeciesLibraryActivity.class);

        body.addView(section("Illustrated field guides",
            "Selected accounts with identification and ecological references"));
        discover(body,"Wildlife guides","Photos and cited species accounts",
            "species","species");
        discover(body,"Habitat guides","Ecological profiles and habitat references",
            "habitat","habitats");

        body.addView(section("Reference tools",null));
        referencePair(body,"Conservation evidence","IUCN and Philippine assessments",
            "library-conservation",Rc24ConservationDashboardActivity.class,
            "Similar species","Compare identification traits",
            "research",Rc9DifferentialActivity.class);
        referencePair(body,"Photo comparisons","Offline wildlife photographs",
            "library-photo",Rc8PhotoComparisonActivity.class,
            "Source checklists","Taxonomic provenance and imports",
            "library-sources",CatalogActivity.class);
        add(body,"Habitat associations","Cited species and habitat links",
            "library-habitat-links",HabitatSpeciesEvidenceActivity.class);

        TextView scope=new TextView(this);
        scope.setText("Checklist coverage and illustrated accounts differ. An island " +
            "reference is not a verified observation at your survey site.");
        scope.setTextColor(UiKit.MUTED);
        scope.setTextSize(12);
        scope.setPadding(dp(3),dp(8),dp(3),dp(12));
        body.addView(scope);

        setContentView(root);
        UiMotion.enter(this,body);
    }
}
