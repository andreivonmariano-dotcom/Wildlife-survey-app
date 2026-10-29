package org.phwildlife.survey;

import android.app.Activity;
import android.content.Intent;
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
    private void addHabitat(LinearLayout p){
        View action=LikasPortalUi.action(this,"Habitats","15 offline guides","habitat",
            ()->startActivity(new Intent(this,HabitatRecordsActivity.class)));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
        lp.bottomMargin=dp(10);p.addView(action,lp);
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
        add(body,"Species","Search · Identify","species",Rc7SpeciesLibraryActivity.class);
        addHabitat(body);
        add(body,"Similar species","Compare traits","species",Rc9DifferentialActivity.class);
        add(body,"Photos","Offline references","photo",Rc8PhotoComparisonActivity.class);
        add(body,"Conservation","IUCN · DENR evidence","shield",Rc24ConservationDashboardActivity.class);
        add(body,"Sources","Checklists · Citations","protocol",CatalogActivity.class);
        add(body,"Habitat links","Published ecology","habitat",HabitatSpeciesEvidenceActivity.class);
        setContentView(root);
        UiMotion.enter(this,body);
    }
}
