package org.phwildlife.survey;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Icon-free editorial components shared by the PHLibrary hub and Book 01 index.
 *  Uses the same forest palette, text scale, and screen gutters as APO.
 *  All book/source views are read-only and never create field observations. */
public final class PhLibraryStyle {
    private PhLibraryStyle(){}
    public static int dp(Activity a,float n){
        return Math.round(n*a.getResources().getDisplayMetrics().density);
    }
    public static TextView text(Activity a,String value,float sp,int ink,boolean medium){
        TextView v=new TextView(a);
        v.setText(value);
        v.setTextSize(sp);
        v.setTextColor(ink);
        if(medium)v.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));
        return v;
    }
    private static GradientDrawable shape(Activity a,int color,int stroke,int radius){
        GradientDrawable d=new GradientDrawable();
        d.setColor(color);
        if(stroke!=Color.TRANSPARENT)d.setStroke(dp(a,1),stroke);
        d.setCornerRadius(dp(a,radius));
        return d;
    }
    public static RippleDrawable touch(Activity a,int fill,int edge,int radius){
        return new RippleDrawable(ColorStateList.valueOf(
            Color.argb(44,42,111,69)),shape(a,fill,edge,radius),
            shape(a,Color.WHITE,Color.TRANSPARENT,radius));
    }
    public static TextView section(Activity a,String label){
        TextView t=text(a,label,13,UiKit.FOREST,true);
        t.setLetterSpacing(.07f);
        t.setPadding(dp(a,2),dp(a,19),dp(a,2),dp(a,10));
        return t;
    }
    /** One whole text-only touch target; no decorative category art or arrow icons. */
    public static View row(Activity a,String title,String subtitle,Runnable open){
        LinearLayout card=new LinearLayout(a);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(a,16),dp(a,12),dp(a,16),dp(a,12));
        card.setMinimumHeight(dp(a,69));
        card.setBackground(touch(a,Color.WHITE,UiKit.BORDER,16));
        TextView heading=text(a,title,16,UiKit.INK,true);
        card.addView(heading,new LinearLayout.LayoutParams(-1,-2));
        if(subtitle!=null&&!subtitle.isEmpty()){
            TextView details=text(a,subtitle,13,UiKit.MUTED,false);
            details.setPadding(0,dp(a,4),0,0);
            card.addView(details,new LinearLayout.LayoutParams(-1,-2));
        }
        card.setContentDescription("Open "+title+
            (subtitle==null||subtitle.isEmpty()?"":". "+subtitle));
        card.setClickable(true);
        card.setFocusable(true);
        card.setOnClickListener(v->open.run());
        return card;
    }
    /** Unique book cover element: a typography-led dark card with no image or icon. */
    public static View bookCover(Activity a,Runnable open){
        LinearLayout card=new LinearLayout(a);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(a,20),dp(a,20),dp(a,20),dp(a,20));
        card.setBackground(touch(a,UiKit.FOREST,Color.TRANSPARENT,20));
        TextView eyebrow=text(a,"BOOK 01  /  GREATER MINDANAO",11,
            Color.rgb(215,236,206),true);
        eyebrow.setLetterSpacing(.10f);
        card.addView(eyebrow);
        TextView title=text(a,"Wildlife of Greater Mindanao",23,Color.WHITE,true);
        title.setPadding(0,dp(a,9),0,dp(a,5));
        card.addView(title);
        TextView summary=text(a,"714 species  ·  Birds, amphibians, reptiles and mammals",
            13,Color.rgb(229,240,224),false);
        summary.setLineSpacing(dp(a,2),1f);
        card.addView(summary);
        TextView action=text(a,"BROWSE ENCYCLOPEDIA",12,Color.WHITE,true);
        action.setLetterSpacing(.06f);
        action.setPadding(0,dp(a,17),0,0);
        card.addView(action);
        card.setContentDescription("Open Book 01, Greater Mindanao. 714 complete species accounts.");
        card.setFocusable(true);
        card.setClickable(true);
        card.setOnClickListener(v->open.run());
        return card;
    }
    public static TextView ruleNote(Activity a,String copy){
        TextView t=text(a,copy,12,UiKit.MUTED,false);
        t.setLineSpacing(dp(a,2),1.02f);
        t.setPadding(dp(a,2),dp(a,10),dp(a,2),dp(a,14));
        return t;
    }
}
