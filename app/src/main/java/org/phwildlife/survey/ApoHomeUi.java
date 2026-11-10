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

/** Text-first APO Home navigation. No image, pictogram or legacy tile resources. */
public final class ApoHomeUi {
    private ApoHomeUi(){}
    private static final int INK=Color.rgb(23,61,50);
    private static final int MUTED=Color.rgb(103,123,111);
    private static final int RULE=Color.rgb(219,228,220);
    private static int dp(Activity a,int n){
        return Math.round(a.getResources().getDisplayMetrics().density*n);
    }
    private static TextView label(Activity a,String text,int size,int color,boolean medium){
        TextView view=new TextView(a);
        view.setText(text);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setIncludeFontPadding(true);
        view.setFontFeatureSettings("kern");
        if(medium)view.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));
        return view;
    }
    private static View rule(Activity a){
        View rule=new View(a);
        rule.setBackgroundColor(RULE);
        return rule;
    }
    private static void divider(Activity a,LinearLayout container){
        container.addView(rule(a),new LinearLayout.LayoutParams(-1,dp(a,1)));
    }
    public static View header(Activity a,Runnable settings){
        LinearLayout block=new LinearLayout(a);
        block.setOrientation(LinearLayout.VERTICAL);
        block.setPadding(dp(a,3),dp(a,8),dp(a,3),dp(a,12));
        LinearLayout top=new LinearLayout(a);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setOrientation(LinearLayout.HORIZONTAL);
        TextView brand=label(a,"APO",32,INK,true);
        brand.setLetterSpacing(.015f);
        brand.setContentDescription("APO home");
        top.addView(brand,new LinearLayout.LayoutParams(0,-2,1f));
        TextView config=label(a,"Settings",14,MUTED,true);
        config.setGravity(Gravity.CENTER);
        config.setMinimumHeight(dp(a,48));
        config.setMinimumWidth(dp(a,80));
        config.setPadding(dp(a,9),dp(a,7),dp(a,9),dp(a,7));
        config.setBackground(new RippleDrawable(ColorStateList.valueOf(
            Color.argb(30,23,82,60)),null,null));
        config.setContentDescription("Open settings");
        config.setFocusable(true);
        config.setClickable(true);
        config.setOnClickListener(v->settings.run());
        top.addView(config);
        block.addView(top);
        TextView kicker=label(a,"PHILIPPINE BIODIVERSITY WORKSPACE",11,MUTED,true);
        kicker.setLetterSpacing(.10f);
        LinearLayout.LayoutParams kickerLp=new LinearLayout.LayoutParams(-1,-2);
        kickerLp.topMargin=dp(a,5);block.addView(kicker,kickerLp);
        TextView desc=label(a,"Field tools, biodiversity references and research in one place.",
            14,MUTED,false);
        LinearLayout.LayoutParams descLp=new LinearLayout.LayoutParams(-1,-2);
        descLp.topMargin=dp(a,7);block.addView(desc,descLp);
        return block;
    }
    /** One immediately visible survey entry point, sized for glove-friendly use. */
    public static View primary(Activity a,String title,String description,Runnable open){
        LinearLayout card=new LinearLayout(a);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(a,18),dp(a,15),dp(a,18),dp(a,15));
        card.setMinimumHeight(dp(a,82));
        GradientDrawable base=new GradientDrawable();
        base.setColor(UiKit.DEEP);
        base.setCornerRadius(dp(a,16));
        card.setBackground(new RippleDrawable(ColorStateList.valueOf(
            Color.argb(45,255,255,255)),base,null));
        card.addView(label(a,title,21,Color.WHITE,true));
        TextView detail=label(a,description,13,Color.rgb(221,240,225),false);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);
        p.topMargin=dp(a,4);
        card.addView(detail,p);
        card.setFocusable(true);card.setClickable(true);
        card.setContentDescription("Open "+title+". "+description);
        card.setOnClickListener(v->open.run());
        return card;
    }
    public static View module(Activity a,String number,String title,String description,Runnable open){
        LinearLayout wrapper=new LinearLayout(a);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        LinearLayout row=new LinearLayout(a);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(a,3),dp(a,16),dp(a,3),dp(a,17));
        row.setMinimumHeight(dp(a,88));
        GradientDrawable fill=new GradientDrawable();
        fill.setColor(UiKit.BG);
        row.setBackground(new RippleDrawable(ColorStateList.valueOf(
            Color.argb(25,25,87,61)),fill,null));
        TextView index=label(a,number,12,Color.rgb(128,148,134),true);
        index.setGravity(Gravity.TOP);
        index.setPadding(0,dp(a,4),0,0);
        row.addView(index,new LinearLayout.LayoutParams(dp(a,34),-2));
        LinearLayout copy=new LinearLayout(a);
        copy.setOrientation(LinearLayout.VERTICAL);
        TextView name=label(a,title,21,INK,true);
        copy.addView(name);
        TextView detail=label(a,description,13,MUTED,false);
        LinearLayout.LayoutParams detailLp=new LinearLayout.LayoutParams(-1,-2);
        detailLp.topMargin=dp(a,5);
        copy.addView(detail,detailLp);
        row.addView(copy,new LinearLayout.LayoutParams(0,-2,1f));
        row.setFocusable(true);
        row.setClickable(true);
        row.setContentDescription("Open "+title+". "+description);
        row.setOnClickListener(v->open.run());
        wrapper.addView(row,new LinearLayout.LayoutParams(-1,-2));
        divider(a,wrapper);
        return wrapper;
    }
    /** Smaller numbered action for nested tool screens; no images or fixed text height. */
    public static View tool(Activity a,String number,String title,String description,Runnable open){
        LinearLayout wrapper=new LinearLayout(a);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        LinearLayout line=new LinearLayout(a);
        line.setOrientation(LinearLayout.HORIZONTAL);
        line.setGravity(Gravity.CENTER_VERTICAL);
        line.setPadding(dp(a,3),dp(a,12),dp(a,3),dp(a,12));
        line.setMinimumHeight(dp(a,72));
        GradientDrawable base=new GradientDrawable();
        base.setColor(UiKit.BG);
        line.setBackground(new RippleDrawable(ColorStateList.valueOf(
            Color.argb(27,25,87,61)),base,null));
        TextView count=label(a,number,12,Color.rgb(116,137,121),true);
        line.addView(count,new LinearLayout.LayoutParams(dp(a,32),-2));
        LinearLayout detail=new LinearLayout(a);
        detail.setOrientation(LinearLayout.VERTICAL);
        detail.addView(label(a,title,17,INK,true));
        if(description!=null&&!description.isEmpty()){
            TextView sub=label(a,description,13,MUTED,false);
            LinearLayout.LayoutParams subLp=new LinearLayout.LayoutParams(-1,-2);
            subLp.topMargin=dp(a,4);
            detail.addView(sub,subLp);
        }
        line.addView(detail,new LinearLayout.LayoutParams(0,-2,1f));
        line.setFocusable(true);
        line.setClickable(true);
        line.setContentDescription("Open "+title+
            (description==null||description.isEmpty()?"":". "+description));
        line.setOnClickListener(v->open.run());
        wrapper.addView(line,new LinearLayout.LayoutParams(-1,-2));
        divider(a,wrapper);
        return wrapper;
    }
    /** Plain text/status content for small screens and scalable Android text. */
    public static TextView note(Activity a,String value){
        TextView text=label(a,value,13,MUTED,false);
        text.setPadding(dp(a,3),dp(a,10),dp(a,3),dp(a,12));
        return text;
    }

    public static View section(Activity a,String title,String description){
        LinearLayout block=new LinearLayout(a);
        block.setOrientation(LinearLayout.VERTICAL);
        block.setPadding(dp(a,3),dp(a,14),dp(a,3),dp(a,20));
        block.addView(label(a,title,22,INK,true));
        TextView detail=label(a,description,14,MUTED,false);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
        lp.topMargin=dp(a,9);block.addView(detail,lp);
        return block;
    }
    /** Contextual survey shortcut, small enough not to dominate the Home menu. */
    public static View resume(Activity a,Runnable open){
        TextView link=label(a,"Resume current survey",14,INK,true);
        link.setPadding(dp(a,13),dp(a,12),dp(a,13),dp(a,12));
        link.setMinHeight(dp(a,48));
        GradientDrawable fill=new GradientDrawable();
        fill.setColor(Color.rgb(230,240,229));
        fill.setCornerRadius(dp(a,12));
        link.setBackground(new RippleDrawable(ColorStateList.valueOf(
            Color.argb(35,18,72,48)),fill,null));
        link.setContentDescription("Resume current active survey");
        link.setFocusable(true);
        link.setClickable(true);
        link.setOnClickListener(v->open.run());
        return link;
    }
}
