package org.phwildlife.survey;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.RippleDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Consistent field-first navigation and readable native workspace components. */
public final class ApoHomeUi {
    private ApoHomeUi() {}

    private static int dp(Activity a,float value){return ApoVisualSystem.dp(a,value);}

    private static TextView label(Activity a,String value,int size,int color,boolean weight) {
        TextView text=new TextView(a);
        text.setText(value);
        text.setTextSize(size);
        text.setTextColor(color);
        text.setIncludeFontPadding(true);
        if(weight)text.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));
        return text;
    }

    private static RippleDrawable tapBackground(Activity a,int fill,int border,int radius) {
        return new RippleDrawable(ColorStateList.valueOf(0x27336245),
            ApoVisualSystem.surface(a,fill,border,radius),null);
    }

    public static View header(Activity a,Runnable settings){
        LinearLayout block=new LinearLayout(a);
        block.setOrientation(LinearLayout.VERTICAL);
        block.setPadding(dp(a,1),dp(a,5),dp(a,1),dp(a,12));
        LinearLayout top=new LinearLayout(a);
        top.setGravity(Gravity.CENTER_VERTICAL);
        ImageView emblem=new ImageView(a);
        emblem.setImageResource(R.drawable.apo_splash_mark);
        emblem.setScaleType(ImageView.ScaleType.FIT_CENTER);
        emblem.setContentDescription(null);
        emblem.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        LinearLayout.LayoutParams icon=new LinearLayout.LayoutParams(dp(a,40),dp(a,40));
        icon.rightMargin=dp(a,9);
        top.addView(emblem,icon);
        LinearLayout identity=new LinearLayout(a);
        identity.setOrientation(LinearLayout.VERTICAL);
        TextView brand=label(a,"APO",27,ApoVisualSystem.INK,true);
        brand.setLetterSpacing(.01f);
        identity.addView(brand);
        TextView edition=label(a,"PHILIPPINE WILDLIFE",11,ApoVisualSystem.MUTED,true);
        edition.setLetterSpacing(.075f);
        identity.addView(edition);
        top.addView(identity,new LinearLayout.LayoutParams(0,-2,1));
        TextView config=label(a,"Settings",14,ApoVisualSystem.FOREST,true);
        config.setGravity(Gravity.CENTER);
        config.setMinWidth(dp(a,76));
        config.setMinHeight(dp(a,48));
        config.setPadding(dp(a,10),dp(a,9),dp(a,10),dp(a,9));
        config.setBackground(tapBackground(a,ApoVisualSystem.FIELD_BACKGROUND,
            ApoVisualSystem.INPUT_BORDER,12));
        config.setContentDescription("Open settings");
        config.setClickable(true);
        config.setFocusable(true);
        config.setOnClickListener(v->settings.run());
        top.addView(config);
        block.addView(top);
        return block;
    }

    /** One accessible card component for top-level destinations. */
    public static View module(Activity a,String number,String title,String description,Runnable open) {
        return navigationCard(a,number,title,description,open,false);
    }

    /** Smaller card for a secondary, contextual tool. */
    public static View tool(Activity a,String number,String title,String description,Runnable open) {
        return navigationCard(a,number,title,description,open,true);
    }

    private static View navigationCard(Activity a,String number,String title,
            String description,Runnable open,boolean compact) {
        LinearLayout row=new LinearLayout(a);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setMinimumHeight(dp(a,compact?64:78));
        row.setPadding(dp(a,14),dp(a,compact?11:15),
            dp(a,13),dp(a,compact?11:15));
        row.setBackground(tapBackground(a,ApoVisualSystem.SURFACE,
            ApoVisualSystem.BORDER,16));

        TextView index=label(a,number,11,ApoVisualSystem.MUTED,true);
        index.setGravity(Gravity.TOP);
        index.setPadding(0,dp(a,4),dp(a,3),0);
        index.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        row.addView(index,new LinearLayout.LayoutParams(dp(a,29),-2));

        LinearLayout copy=new LinearLayout(a);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.addView(label(a,title,compact?16:18,ApoVisualSystem.INK,true));
        if(description!=null&&!description.trim().isEmpty()){
            TextView sub=label(a,description,13,ApoVisualSystem.MUTED,false);
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
            lp.topMargin=dp(a,4);
            copy.addView(sub,lp);
        }
        row.addView(copy,new LinearLayout.LayoutParams(0,-2,1f));

        TextView chevron=label(a,"›",26,ApoVisualSystem.FOREST,false);
        chevron.setGravity(Gravity.CENTER);
        chevron.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        row.addView(chevron,new LinearLayout.LayoutParams(dp(a,25),-2));
        row.setClickable(true);
        row.setFocusable(true);
        row.setContentDescription("Open "+title+
            (description==null||description.isEmpty()?"":". "+description));
        row.setOnClickListener(v->open.run());
        LinearLayout wrapper=new LinearLayout(a);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
        lp.bottomMargin=dp(a,8);
        wrapper.addView(row,lp);
        return wrapper;
    }

    public static View section(Activity a,String title,String description){
        LinearLayout block=new LinearLayout(a);
        block.setOrientation(LinearLayout.VERTICAL);
        block.setPadding(dp(a,2),dp(a,15),dp(a,2),dp(a,11));
        block.addView(label(a,title,20,ApoVisualSystem.INK,true));
        if(description!=null&&!description.trim().isEmpty()){
            TextView sub=label(a,description,14,ApoVisualSystem.MUTED,false);
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
            lp.topMargin=dp(a,5);block.addView(sub,lp);
        }
        return block;
    }

    public static TextView note(Activity a,String value) {
        TextView note=label(a,value,13,ApoVisualSystem.MUTED,false);
        note.setPadding(dp(a,2),dp(a,9),dp(a,2),dp(a,12));
        return note;
    }

    /** One dominant action per Home, above all editorial content. */
    public static View primary(Activity a,String title,String description,Runnable open) {
        LinearLayout content=new LinearLayout(a);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_VERTICAL);
        content.setPadding(dp(a,18),dp(a,15),dp(a,18),dp(a,15));
        content.setMinimumHeight(dp(a,76));
        content.setBackground(new RippleDrawable(ColorStateList.valueOf(0x50FFFFFF),
            ApoVisualSystem.surface(a,ApoVisualSystem.FOREST,Color.TRANSPARENT,18),null));
        content.addView(label(a,title,20,Color.WHITE,true));
        if(description!=null&&!description.trim().isEmpty()){
            TextView sub=label(a,description,13,0xFFE5F2E7,false);
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
            lp.topMargin=dp(a,4);content.addView(sub,lp);
        }
        content.setContentDescription(title+
            (description==null||description.isEmpty()?"":". "+description));
        content.setClickable(true);
        content.setFocusable(true);
        content.setOnClickListener(v->open.run());
        return content;
    }

    public static View resume(Activity a,Runnable open){
        return primary(a,"Resume Survey","Continue your active field session",open);
    }

    /** A truthful, noninteractive planned destination cannot become a dead-end route. */
    public static View preview(Activity a,String title,String status){
        LinearLayout row=new LinearLayout(a);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(dp(a,15),dp(a,13),dp(a,15),dp(a,13));
        row.setBackground(ApoVisualSystem.surface(a,ApoVisualSystem.LEAF_PALE,
            ApoVisualSystem.BORDER,16));
        row.addView(label(a,title,17,ApoVisualSystem.FOREST,true));
        TextView message=label(a,status,13,ApoVisualSystem.MUTED,false);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
        lp.topMargin=dp(a,3);row.addView(message,lp);
        return row;
    }
}
