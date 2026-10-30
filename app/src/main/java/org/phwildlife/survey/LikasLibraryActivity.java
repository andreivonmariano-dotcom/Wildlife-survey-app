package org.phwildlife.survey;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** Reading space. No survey event, effort or observation is created here. */
public final class LikasLibraryActivity extends Activity {
    private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
    private void add(LinearLayout p,String name,String caption,String icon,
            Class<?> destination){
        View action=LikasPortalUi.action(this,name,caption,icon,
            ()->startActivity(new Intent(this,destination)));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
        lp.bottomMargin=dp(LikasDesignSystem.SPACE_SM);p.addView(action,lp);
    }
    private void discover(LinearLayout p,String title,String subtitle,String icon,String view){
        View card=LikasPortalUi.libraryFeature(this,title,subtitle,icon,()->
            startActivity(new Intent(this,LikasDiscoveryActivity.class)
                .putExtra("view",view)));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
        lp.bottomMargin=dp(LikasDesignSystem.SPACE_MD);p.addView(card,lp);
    }
    /** Local section treatment keeps page title visually dominant at 200% font. */
    private LinearLayout librarySection(String title,String caption){
        LinearLayout section=FieldVisuals.section(this,title,caption);
        TextView heading=(TextView)section.getChildAt(0);
        heading.setTextSize(14);
        heading.setLetterSpacing(.06f);
        heading.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));
        heading.setTextColor(UiKit.FOREST);
        return section;
    }
    private boolean stackPortalPairs(){
        android.content.res.Configuration config=getResources().getConfiguration();
        return FieldLayoutPolicy.stackPortalPair(config.screenWidthDp,config.fontScale);
    }
    private void toolPair(LinearLayout parent,String first,String firstDetail,
            String firstIcon,Class<?> firstTarget,String second,String secondDetail,
            String secondIcon,Class<?> secondTarget){
        LinearLayout row=new LinearLayout(this);
        // Phones keep a uniform one-column action list; tablets may retain paired mini cards.
        boolean stacked=stackPortalPairs() ||
            getResources().getConfiguration().screenWidthDp<600;
        row.setOrientation(stacked?LinearLayout.VERTICAL:LinearLayout.HORIZONTAL);
        View a=stacked?
            LikasPortalUi.action(this,first,firstDetail,firstIcon,
                ()->startActivity(new Intent(this,firstTarget))):
            LikasPortalUi.mini(this,first,firstDetail,firstIcon,
                ()->startActivity(new Intent(this,firstTarget)));
        View b=stacked?
            LikasPortalUi.action(this,second,secondDetail,secondIcon,
                ()->startActivity(new Intent(this,secondTarget))):
            LikasPortalUi.mini(this,second,secondDetail,secondIcon,
                ()->startActivity(new Intent(this,secondTarget)));
        LinearLayout.LayoutParams aLp=new LinearLayout.LayoutParams(
            stacked?-1:0,-2,1);
        aLp.bottomMargin=dp(LikasDesignSystem.SPACE_SM);row.addView(a,aLp);
        LinearLayout.LayoutParams bLp=new LinearLayout.LayoutParams(
            stacked?-1:0,-2,1);
        if(!stacked)bLp.leftMargin=dp(LikasDesignSystem.SPACE_SM);
        bLp.bottomMargin=dp(LikasDesignSystem.SPACE_SM);row.addView(b,bLp);
        parent.addView(row);
    }
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        getWindow().setStatusBarColor(UiKit.BG);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        LinearLayout body=new LinearLayout(this);
        LinearLayout root=UiKit.page(this,"Library","Home",this::finish,body);
        // Reserve a small strip for the system scroll indicator so it never paints
        // over a card outline or its arrow. Restrict this to the Library hub.
        ScrollView libraryScroll=(ScrollView)root.getChildAt(1);
        libraryScroll.setPadding(0,0,dp(8),0);
        libraryScroll.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_INSET);
        body.addView(librarySection("Species & habitats","Offline field guides"));
        discover(body,"Species","Illustrated accounts","species","species");
        discover(body,"Habitats","Habitat profiles","habitat","habitats");
        body.addView(librarySection("Reference tools",null));
        toolPair(body,"Similar species","Compare traits","research",
            Rc9DifferentialActivity.class,"Photos","Offline images","photo",
            Rc8PhotoComparisonActivity.class);
        toolPair(body,"Conservation","Dated assessments","conservation-pixel-shield",
            Rc24ConservationDashboardActivity.class,"Sources","Checklists","protocol",
            CatalogActivity.class);
        add(body,"Habitat links","Cited associations","habitat",
            HabitatSpeciesEvidenceActivity.class);
        setContentView(root);
        UiMotion.enter(this,body);
    }
}
