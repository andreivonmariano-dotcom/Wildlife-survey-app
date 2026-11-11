package org.phwildlife.survey;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.RippleDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Four stable, keyboard-free navigation targets for APO's workspace shell. */
public final class ApoShellBar {
    private ApoShellBar() {}

    private static TextView tab(Activity activity,String label,String spokenName,
            String tag,boolean selected,Runnable action) {
        TextView item=new TextView(activity);
        item.setTag("apo_nav_"+tag);
        item.setText(label);
        item.setTextSize(ApoVisualSystem.largeText(activity)?12:13);
        item.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));
        item.setTextColor(selected?Color.WHITE:ApoVisualSystem.FOREST);
        item.setGravity(Gravity.CENTER);
        item.setMaxLines(2);
        item.setMinHeight(ApoVisualSystem.dp(activity,
            ApoVisualSystem.largeText(activity)?68:54));
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
        item.setOnClickListener(v->action.run());
        return item;
    }

    public static View create(Activity activity,String screen,Runnable home,
            Runnable fieldwork,Runnable atlas,Runnable data) {
        LinearLayout block=new LinearLayout(activity);
        block.setOrientation(LinearLayout.VERTICAL);
        block.setBackgroundColor(ApoVisualSystem.FIELD_BACKGROUND);
        View rule=new View(activity);
        rule.setBackgroundColor(ApoVisualSystem.BORDER);
        block.addView(rule,new LinearLayout.LayoutParams(-1,
            ApoVisualSystem.dp(activity,1)));

        LinearLayout row=new LinearLayout(activity);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0,ApoVisualSystem.dp(activity,8),
            0,ApoVisualSystem.dp(activity,4));
        boolean inData="data".equals(screen)||"analysis".equals(screen)||
            "utilities".equals(screen)||"records".equals(screen)||
            "biotools".equals(screen);
        String[] labels={"Home","Survey","Atlas","My Data"};
        String[] names={"Home","Fieldwork","Living Atlas","My Data"};
        String[] tags={"home","fieldwork","atlas","data"};
        boolean[] active={"home".equals(screen),"fieldwork".equals(screen),
            false,inData};
        Runnable[] actions={home,fieldwork,atlas,data};
        for(int i=0;i<labels.length;i++){
            TextView child=tab(activity,labels[i],names[i],tags[i],active[i],actions[i]);
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1f);
            lp.leftMargin=ApoVisualSystem.dp(activity,2);
            lp.rightMargin=ApoVisualSystem.dp(activity,2);
            row.addView(child,lp);
        }
        block.addView(row);
        return block;
    }
}
