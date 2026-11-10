package org.phwildlife.survey;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;

/** Preferences and local data safety; destructive management stays in its confirmed workflow. */
public final class LikasSettingsActivity extends Activity {
    private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
    private boolean stackPortalPairs(){
        android.content.res.Configuration config=getResources().getConfiguration();
        return FieldLayoutPolicy.stackPortalPair(config.screenWidthDp,config.fontScale);
    }
    /** Plain full-width actions shared with the APO field workspace. */
    private void options(LinearLayout parent,String first,String firstHint,String firstArt,
            Class<?> firstTarget,String second,String secondHint,String secondArt,
            Class<?> secondTarget){
        parent.addView(ApoHomeUi.tool(this,"",first,firstHint,
            ()->startActivity(new Intent(this,firstTarget))));
        parent.addView(ApoHomeUi.tool(this,"",second,secondHint,
            ()->startActivity(new Intent(this,secondTarget))));
    }
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        getWindow().setStatusBarColor(UiKit.BG);
        getWindow().setNavigationBarColor(UiKit.BG);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        LinearLayout body=new LinearLayout(this);
        LinearLayout root=UiKit.page(this,"Settings","Home",this::finish,body);
        body.addView(FieldVisuals.section(this,"Preferences",null));
        Switch reduce=new Switch(this);
        reduce.setText("Less motion");
        reduce.setTextSize(LikasDesignSystem.TEXT_CARD);
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
        LinearLayout.LayoutParams rm=new LinearLayout.LayoutParams(-1,-2);
        rm.bottomMargin=dp(LikasDesignSystem.SPACE_MD);body.addView(reduce,rm);
        body.addView(FieldVisuals.section(this,"Data",null));
        options(body,"Backup","Save · Restore","backup",FieldBackupActivity.class,
            "Storage","My records","settings",DataManagementActivity.class);
        options(body,"Import","Source files","protocol",ImportActivity.class,
            "Data check","Review records","reports",AuditActivity.class);
        body.addView(FieldVisuals.section(this,"About",null));
        View about=ApoHomeUi.tool(this,"","About APO","Version and privacy",()->{
            String version="Version unavailable";
            try{
                PackageInfo pkg=getPackageManager().getPackageInfo(getPackageName(),0);
                version=pkg.versionName;
            }catch(Exception ignored){}
            new AlertDialog.Builder(this).setTitle("APO")
                .setMessage("Version "+version+"\n\nPhilippine biodiversity toolkit. "+
                    "Survey records and offline research stay on this device. "+
                    "References are separate from field records.")
                .setPositiveButton("Close",null).show();
        });
        body.addView(about);
        setContentView(root);
        UiMotion.enter(this,body);
    }
}
