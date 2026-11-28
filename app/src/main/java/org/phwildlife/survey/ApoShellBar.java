package org.phwildlife.survey;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.RippleDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Shared workspace navigation. Contextual details retain their own Back stack. */
public final class ApoShellBar {
    private ApoShellBar() {}
    public static final String ROOT = "apo_workspace_root";

    public static void workspace(Activity activity,String target) {
        Intent intent=new Intent(activity,WelcomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("workspace_destination",target);
        activity.startActivity(intent);
        if(!(activity instanceof WelcomeActivity))activity.finish();
    }

    private static TextView tab(Activity activity,String label,String spokenName,
            String tag,boolean selected,Runnable action) {
        TextView item=new TextView(activity);
        item.setTag("apo_nav_"+tag);
        item.setText(label);
        item.setTextSize(13);
        item.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));
        item.setTextColor(selected?Color.WHITE:ApoVisualSystem.FOREST);
        item.setGravity(Gravity.CENTER);
        item.setMaxLines(2);
        item.setMinHeight(ApoVisualSystem.dp(activity,54));
        item.setPadding(ApoVisualSystem.dp(activity,3),
            ApoVisualSystem.dp(activity,8),ApoVisualSystem.dp(activity,3),
            ApoVisualSystem.dp(activity,8));
        item.setSelected(selected);
        item.setContentDescription(spokenName+(selected?", selected":""));
        item.setBackground(new RippleDrawable(
            ColorStateList.valueOf(selected?0x50FFFFFF:0x25336245),
            ApoVisualSystem.surface(activity,
                selected?ApoVisualSystem.FOREST:ApoVisualSystem.FIELD_BACKGROUND,
                Color.TRANSPARENT,13),null));
        item.setClickable(true);
        item.setFocusable(true);
        item.setOnClickListener(v->{if(!selected)action.run();});
        return item;
    }

    /** Home is a dedicated workspace-level action, outside the five peer tabs. */
    public static View rootHeader(Activity activity,String title,Runnable home) {
        LinearLayout header=new LinearLayout(activity);
        header.setGravity(Gravity.CENTER_VERTICAL);
        TextView back=tab(activity,"Home","Go Home","home",false,home);
        back.setMinWidth(ApoVisualSystem.dp(activity,76));
        header.addView(back,new LinearLayout.LayoutParams(-2,-2));
        TextView heading=new TextView(activity);
        heading.setText(title);
        heading.setTextSize(20);
        heading.setTextColor(ApoVisualSystem.INK);
        heading.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        heading.setPadding(ApoVisualSystem.dp(activity,12),0,0,0);
        header.addView(heading,new LinearLayout.LayoutParams(0,-2,1f));
        return header;
    }

    public static View create(Activity activity,String screen,Runnable home,
            Runnable fieldwork,Runnable atlas,Runnable data) {
        LinearLayout block=new LinearLayout(activity);
        block.setTag("apo_workspace_tabs");
        block.setOrientation(LinearLayout.VERTICAL);
        block.setBackgroundColor(ApoVisualSystem.FIELD_BACKGROUND);
        View rule=new View(activity);
        rule.setBackgroundColor(ApoVisualSystem.BORDER);
        block.addView(rule,new LinearLayout.LayoutParams(-1,ApoVisualSystem.dp(activity,1)));
        boolean inData="data".equals(screen)||"analysis".equals(screen)||
            "utilities".equals(screen)||"records".equals(screen)||"biotools".equals(screen);
        android.content.res.Configuration cfg=activity.getResources().getConfiguration();
        boolean stacked=cfg.screenWidthDp<350||cfg.fontScale>=1.3f;
        String[] labels=stacked?new String[]{"Field","Atlas","Map","Papers","Data"}:
            new String[]{"Fieldwork","Atlas","Map","Research","My Data"};
        String[] names={"Fieldwork","Living Atlas","Offline Map","Research Library","My Data"};
        String[] tags={"fieldwork","atlas","map","research","data"};
        boolean[] active={"fieldwork".equals(screen),"atlas".equals(screen),
            "map".equals(screen),"research".equals(screen),inData};
        Runnable[] actions={fieldwork,atlas,()->workspace(activity,"map"),
            ()->workspace(activity,"research"),data};
        LinearLayout row=null;
        for(int i=0;i<labels.length;i++){
            if(i==0||(stacked&&i==3)){
                row=new LinearLayout(activity);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setPadding(0,ApoVisualSystem.dp(activity,4),0,ApoVisualSystem.dp(activity,2));
                block.addView(row,new LinearLayout.LayoutParams(-1,-2));
            }
            TextView child=tab(activity,labels[i],names[i],tags[i],active[i],actions[i]);
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1f);
            lp.leftMargin=ApoVisualSystem.dp(activity,2);
            lp.rightMargin=ApoVisualSystem.dp(activity,2);
            row.addView(child,lp);
        }
        return block;
    }
    public static View createRoot(Activity activity,String screen){
        return create(activity,screen,()->workspace(activity,"home"),
            ()->workspace(activity,"fieldwork"),()->workspace(activity,"atlas"),
            ()->workspace(activity,"data"));
    }
}
