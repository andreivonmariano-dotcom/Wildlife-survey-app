package org.phwildlife.survey;

import android.app.Activity;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Quiet research-first section hierarchy shared by non-startup screens. */
public final class FieldVisuals {
    private FieldVisuals(){}
    private static int dp(Activity a,int value){
        return Math.round(value*a.getResources().getDisplayMetrics().density);
    }
    public static LinearLayout section(Activity a,String title,String caption){
        LinearLayout content=new LinearLayout(a);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(0,0,0,dp(a,LikasDesignSystem.SPACE_SM));
        content.addView(UiKit.sectionTitle(a,title));
        if(caption!=null&&!caption.isEmpty()){
            TextView note=UiKit.caption(a,caption);
            note.setPadding(dp(a,LikasDesignSystem.SPACE_XS),0,
                dp(a,LikasDesignSystem.SPACE_XS),0);
            content.addView(note);
        }
        return content;
    }
    public static TextView context(Activity a,String value){
        TextView note=new TextView(a);
        note.setText(value);
        note.setTextSize(LikasDesignSystem.TEXT_CAPTION);
        note.setTextColor(UiKit.INK);
        note.setBackground(UiKit.surface(a));
        note.setPadding(dp(a,12),dp(a,11),dp(a,12),dp(a,11));
        note.setMinHeight(dp(a,LikasDesignSystem.MIN_TOUCH_DP));
        return note;
    }
}
