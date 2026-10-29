package org.phwildlife.survey;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
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
        lp.bottomMargin=dp(10);p.addView(action,lp);
    }
    private void discover(LinearLayout p,String title,String subtitle,String icon,String view){
        View card=LikasPortalUi.tile(this,title,subtitle,icon,
            LikasPortalUi.MOSS,Color.WHITE,()->
            startActivity(new Intent(this,LikasDiscoveryActivity.class)
                .putExtra("view",view)));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
        lp.bottomMargin=dp(12);p.addView(card,lp);
    }
    private boolean largeText(){
        return getResources().getConfiguration().fontScale>=1.60f;
    }
    private void toolPair(LinearLayout parent,String first,String firstDetail,
            String firstIcon,Class<?> firstTarget,String second,String secondDetail,
            String secondIcon,Class<?> secondTarget){
        LinearLayout row=new LinearLayout(this);
        row.setOrientation(largeText()?LinearLayout.VERTICAL:LinearLayout.HORIZONTAL);
        View a=LikasPortalUi.mini(this,first,firstDetail,firstIcon,
            ()->startActivity(new Intent(this,firstTarget)));
        View b=LikasPortalUi.mini(this,second,secondDetail,secondIcon,
            ()->startActivity(new Intent(this,secondTarget)));
        LinearLayout.LayoutParams aLp=new LinearLayout.LayoutParams(
            largeText()?-1:0,-2,1);
        aLp.bottomMargin=dp(9);row.addView(a,aLp);
        LinearLayout.LayoutParams bLp=new LinearLayout.LayoutParams(
            largeText()?-1:0,-2,1);
        if(!largeText())bLp.leftMargin=dp(10);
        bLp.bottomMargin=dp(9);row.addView(b,bLp);
        parent.addView(row);
    }
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        getWindow().setStatusBarColor(UiKit.BG);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(UiKit.BG);root.setPadding(dp(17),dp(12),dp(17),dp(8));
        root.addView(UiKit.navigationHeader(this,"Library","Home",this::finish));
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);
        LinearLayout body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(0,dp(8),0,dp(20));scroll.addView(body);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        body.addView(LikasPortalUi.kicker(this,"BIODIVERSITY"));
        TextView headline=LikasPortalUi.text(this,"Know the wild.",29,
            LikasPortalUi.CANOPY,true);
        body.addView(headline);
        TextView sub=LikasPortalUi.text(this,"Offline. Source-linked.",13,UiKit.MUTED,false);
        sub.setPadding(0,dp(5),0,dp(19));body.addView(sub);
        discover(body,"Species","Browse wildlife","species","species");
        discover(body,"Habitats","Explore ecosystems","habitat","habitats");
        body.addView(LikasPortalUi.kicker(this,"REFERENCE TOOLS"));
        toolPair(body,"Similar species","Field traits","species",
            Rc9DifferentialActivity.class,"Photos","Offline images","photo",
            Rc8PhotoComparisonActivity.class);
        toolPair(body,"Conservation","IUCN · DENR","shield",
            Rc24ConservationDashboardActivity.class,"Sources","Checklists","protocol",
            CatalogActivity.class);
        add(body,"Habitat links","Published ecology","habitat",
            HabitatSpeciesEvidenceActivity.class);
        setContentView(root);
        UiMotion.enter(this,body);
    }
}
