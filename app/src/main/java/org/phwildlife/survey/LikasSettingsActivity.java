package org.phwildlife.survey;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

/** Preferences and local data safety; destructive management stays in its confirmed workflow. */
public final class LikasSettingsActivity extends Activity {
    private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
    private void add(LinearLayout p,String label,String sub,String symbol,Class<?> destination){
        View view=LikasPortalUi.action(this,label,sub,symbol,
            ()->startActivity(new Intent(this,destination)));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
        lp.bottomMargin=dp(9);p.addView(view,lp);
    }
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        getWindow().setStatusBarColor(UiKit.BG);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(UiKit.BG);root.setPadding(dp(17),dp(12),dp(17),dp(8));
        root.addView(UiKit.navigationHeader(this,"Settings","Home",this::finish));
        ScrollView scroll=new ScrollView(this);
        LinearLayout body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(0,dp(8),0,dp(24));scroll.addView(body);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        body.addView(LikasPortalUi.kicker(this,"YOUR APP"));
        TextView headline=LikasPortalUi.text(this,"Make it yours.",29,
            LikasPortalUi.CANOPY,true);body.addView(headline);
        TextView sub=LikasPortalUi.text(this,"Your field data stays local.",13,
            UiKit.MUTED,false);sub.setPadding(0,dp(5),0,dp(20));body.addView(sub);
        Switch reduce=new Switch(this);
        reduce.setText("Less motion");
        reduce.setTextSize(16);
        reduce.setTextColor(UiKit.INK);
        reduce.setContentDescription("Less motion. Minimize optional screen animations");
        reduce.setPadding(dp(15),dp(11),dp(15),dp(11));
        reduce.setMinimumHeight(dp(64));
        reduce.setBackground(UiKit.surface(this));
        reduce.setChecked(getSharedPreferences("likas_ui",MODE_PRIVATE)
            .getBoolean("reduced_motion",false));
        reduce.setOnCheckedChangeListener((CompoundButton v,boolean checked)->
            getSharedPreferences("likas_ui",MODE_PRIVATE).edit()
                .putBoolean("reduced_motion",checked).apply());
        LinearLayout.LayoutParams rm=new LinearLayout.LayoutParams(-1,dp(64));
        rm.bottomMargin=dp(15);body.addView(reduce,rm);
        body.addView(LikasPortalUi.kicker(this,"DATA"));
        add(body,"Backup","Save · Restore","backup",FieldBackupActivity.class);
        add(body,"Storage","Manage records","settings",DataManagementActivity.class);
        add(body,"Import","Older files","protocol",ImportActivity.class);
        add(body,"Data check","Review records","shield",AuditActivity.class);
        body.addView(LikasPortalUi.kicker(this,"ABOUT"));
        View about=LikasPortalUi.action(this,"About LIKAS","Version · Privacy","library",()->{
            String version="Version unavailable";
            try{
                PackageInfo pkg=getPackageManager().getPackageInfo(getPackageName(),0);
                version=pkg.versionName;
            }catch(Exception ignored){}
            new AlertDialog.Builder(this).setTitle("LIKAS")
                .setMessage("Version "+version+"\n\nPhilippine biodiversity toolkit. "+
                    "Survey records and offline research stay on this device. "+
                    "References are not field observations.")
                .setPositiveButton("Close",null).show();
        });
        body.addView(about);
        setContentView(root);
        UiMotion.enter(this,body);
    }
}
