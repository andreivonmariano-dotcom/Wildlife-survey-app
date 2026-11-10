package org.phwildlife.survey;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;

/** APO preferences and owner-controlled local records, using the shared field UI. */
public final class LikasSettingsActivity extends Activity {
    private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
    private void link(LinearLayout body,String number,String title,
                      String detail,Class<?> destination){
        body.addView(ApoHomeUi.tool(this,number,title,detail,
            ()->startActivity(new Intent(this,destination))));
    }
    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);
        getWindow().setStatusBarColor(UiKit.BG);
        getWindow().setNavigationBarColor(UiKit.BG);
        getWindow().getDecorView().setSystemUiVisibility(
            android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|
            android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        LinearLayout body=new LinearLayout(this);
        LinearLayout root=UiKit.page(this,"Settings","Home",this::finish,body);

        body.addView(ApoHomeUi.section(this,"Field preferences",
            "Change display preferences without affecting saved survey evidence."));
        Switch reduce=new Switch(this);
        reduce.setText("Reduce motion");
        reduce.setTextColor(UiKit.INK);
        reduce.setTextSize(LikasDesignSystem.TEXT_CARD);
        reduce.setMinHeight(dp(60));
        reduce.setPadding(dp(12),dp(8),dp(12),dp(8));
        reduce.setBackground(UiKit.surface(this));
        reduce.setChecked(getSharedPreferences("likas_ui",MODE_PRIVATE)
            .getBoolean("reduced_motion",false));
        reduce.setContentDescription("Reduce motion. Minimize optional animations.");
        reduce.setOnCheckedChangeListener((CompoundButton b,boolean checked)->
            getSharedPreferences("likas_ui",MODE_PRIVATE).edit()
                .putBoolean("reduced_motion",checked).apply());
        LinearLayout.LayoutParams setting=new LinearLayout.LayoutParams(-1,-2);
        setting.bottomMargin=dp(14);
        body.addView(reduce,setting);

        body.addView(ApoHomeUi.section(this,"Data and research",
            "Backups, source imports and record integrity checks."));
        link(body,"01","Back up or restore","Keep an independent copy of your survey records",
            FieldBackupActivity.class);
        link(body,"02","Storage and records","Review local files and retained research data",
            DataManagementActivity.class);
        link(body,"03","Import source files","Add supported reference inputs",
            ImportActivity.class);
        link(body,"04","Data quality review","Inspect saved records and integrity flags",
            AuditActivity.class);

        body.addView(ApoHomeUi.section(this,"About APO",
            "Philippine biodiversity reference and field survey toolkit."));
        body.addView(ApoHomeUi.tool(this,"","App version and privacy",
            "Offline survey records remain on this device",()->{
                String version="Unavailable";
                try{
                    PackageInfo pkg=getPackageManager().getPackageInfo(getPackageName(),0);
                    version=pkg.versionName;
                }catch(Exception ignored){}
                new AlertDialog.Builder(this).setTitle("APO")
                    .setMessage("Version "+version+"\n\n"+
                        "Survey records, saved observations and research evidence remain "+
                        "stored locally unless you explicitly export or back them up. "+
                        "Species reference information is separate from field observations.")
                    .setPositiveButton("Close",null).show();
            }));
        setContentView(root);
        UiMotion.enter(this,body);
    }
}
