package org.phwildlife.survey;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.View;
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
        content.setPadding(dp(a,3),dp(a,15),dp(a,3),dp(a,9));
        TextView heading=new TextView(a);
        heading.setText(title);
        heading.setTextSize(17);
        heading.setTypeface(null,Typeface.BOLD);
        heading.setTextColor(UiKit.FOREST);
        content.addView(heading);
        if(caption!=null&&!caption.isEmpty()){
            TextView note=new TextView(a);
            note.setText(caption);
            note.setTextSize(12);
            note.setTextColor(UiKit.MUTED);
            note.setPadding(0,dp(a,3),0,0);
            content.addView(note);
        }
        return content;
    }
    public static TextView context(Activity a,String value){
        TextView note=new TextView(a);
        note.setText(value);
        note.setTextSize(13);
        note.setTextColor(UiKit.INK);
        note.setBackground(UiKit.surface(a));
        note.setPadding(dp(a,12),dp(a,11),dp(a,12),dp(a,11));
        note.setMinHeight(dp(a,46));
        return note;
    }
}
