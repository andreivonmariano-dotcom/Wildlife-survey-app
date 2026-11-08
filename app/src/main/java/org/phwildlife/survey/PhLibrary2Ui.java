package org.phwildlife.survey;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Editorial components for the offline Greater Mindanao atlas.
 * Forest drawing is decorative, not a species depiction or a habitat record. */
public final class PhLibrary2Ui {
    public static final int PAPER=Color.rgb(245,242,233);
    public static final int IVORY=Color.rgb(254,252,245);
    public static final int FOREST=Color.rgb(27,67,51);
    public static final int DEEP=Color.rgb(14,45,39);
    public static final int INK=Color.rgb(31,52,41);
    public static final int MUTED=Color.rgb(78,99,84);
    public static final int MOSS=Color.rgb(218,231,210);
    public static final int OCHRE=Color.rgb(195,151,84);
    public static final int LINE=Color.rgb(208,216,202);
    private PhLibrary2Ui() {}
    public static int dp(Activity a,float n){return Math.round(n*a.getResources().getDisplayMetrics().density);}
    public static GradientDrawable bg(Activity a,int fill,int border,int radius){
        GradientDrawable d=new GradientDrawable();
        d.setColor(fill);d.setCornerRadius(dp(a,radius));
        if(border!=Color.TRANSPARENT)d.setStroke(dp(a,1),border);
        return d;
    }
    public static RippleDrawable touch(Activity a,int fill,int border,int radius){
        return new RippleDrawable(ColorStateList.valueOf(0x243E7B5A),
            bg(a,fill,border,radius),null);
    }
    public static TextView text(Activity a,String value,float sp,int color,boolean serif,boolean medium){
        TextView t=new TextView(a);
        t.setText(value);t.setTextColor(color);t.setTextSize(sp);
        if(serif)t.setTypeface(Typeface.create("serif",medium?Typeface.BOLD:Typeface.NORMAL));
        else if(medium)t.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));
        return t;
    }
    public static TextView eyebrow(Activity a,String label){
        TextView t=text(a,label,12,MUTED,false,true);t.setLetterSpacing(.085f);
        t.setPadding(0,dp(a,13),0,dp(a,9));return t;
    }
    public static TextView action(Activity a,String label,Runnable tap,boolean filled){
        TextView t=text(a,label,15,filled?Color.WHITE:FOREST,false,true);
        t.setGravity(Gravity.CENTER);t.setMinHeight(dp(a,48));
        t.setPadding(dp(a,14),dp(a,11),dp(a,14),dp(a,11));
        t.setBackground(touch(a,filled?FOREST:IVORY,filled?Color.TRANSPARENT:LINE,13));
        t.setClickable(true);t.setFocusable(true);t.setOnClickListener(v->tap.run());
        return t;
    }
    public static View separator(Activity a){
        View v=new View(a);v.setBackgroundColor(LINE);
        v.setLayoutParams(new LinearLayout.LayoutParams(-1,dp(a,1)));
        return v;
    }
    public static LinearLayout panel(Activity a,String title,String description,Runnable action){
        LinearLayout p=new LinearLayout(a);p.setOrientation(LinearLayout.VERTICAL);
        p.setPadding(dp(a,17),dp(a,14),dp(a,17),dp(a,14));
        p.setBackground(touch(a,IVORY,LINE,17));p.setMinimumHeight(dp(a,91));
        TextView head=text(a,title,21,INK,true,false);
        p.addView(head);
        TextView sub=text(a,description,13,MUTED,false,false);sub.setLineSpacing(dp(a,2),1.03f);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
        lp.topMargin=dp(a,5);p.addView(sub,lp);
        p.setClickable(true);p.setFocusable(true);p.setOnClickListener(v->action.run());
        p.setContentDescription(title+". "+description);
        return p;
    }
    public static View hero(Activity a,String eyebrow,String title,String subtitle,boolean compact){
        FrameLayout frame=new FrameLayout(a);
        boolean large=a.getResources().getConfiguration().fontScale>=1.6f;
        int height=dp(a,compact?(large?226:154):282);
        frame.setMinimumHeight(height);
        frame.setBackground(bg(a,DEEP,Color.TRANSPARENT,18));
        frame.setClipToOutline(true);
        ForestCanvas forest=new ForestCanvas(a);
        frame.addView(forest,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout content=new LinearLayout(a);content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.TOP);content.setPadding(dp(a,22),dp(a,compact?18:24),dp(a,20),dp(a,16));
        TextView meta=text(a,eyebrow,11,0xFFE5E8CF,false,true);
        meta.setLetterSpacing(.095f);content.addView(meta);
        TextView heading=text(a,title,compact?(large?20:27):(large?27:36),
            Color.WHITE,true,false);
        heading.setLineSpacing(dp(a,1),.98f);
        LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(-1,-2);hp.topMargin=dp(a,11);content.addView(heading,hp);
        TextView caption=text(a,subtitle,large?12:(compact?13:15),
            0xFFE4EEDB,false,false);
        caption.setLineSpacing(dp(a,2),1.05f);
        LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2);cp.topMargin=dp(a,8);content.addView(caption,cp);
        frame.addView(content,new FrameLayout.LayoutParams(-1,-2,Gravity.TOP));
        frame.setLayoutParams(new LinearLayout.LayoutParams(-1,height));
        return frame;
    }
    /** Layered Philippine rainforest atmosphere, not a species-specific habitat illustration. */
    private static final class ForestCanvas extends View {
        private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        ForestCanvas(Activity a){super(a);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
        private void color(int c){p.setColor(c);p.setStyle(Paint.Style.FILL);p.setStrokeWidth(1);}
        private void hill(Canvas c,int shade,float y,float depth){
            Path h=new Path();h.moveTo(0,300);h.lineTo(0,y);
            h.cubicTo(70,y-depth,94,y+depth*.4f,151,y-depth*.3f);
            h.cubicTo(211,y-depth*1.6f,260,y+depth*.5f,315,y-depth*.2f);
            h.cubicTo(354,y-depth*.7f,381,y,400,y-depth*.7f);
            h.lineTo(400,300);h.close();color(shade);c.drawPath(h,p);
        }
        private void tree(Canvas c,float x,float bottom,float size,int color){
            color(color);
            p.setStrokeWidth(3.2f*size);p.setStyle(Paint.Style.STROKE);
            c.drawLine(x,bottom,x,bottom-72*size,p);p.setStyle(Paint.Style.FILL);
            c.drawOval(x-19*size,bottom-115*size,x+18*size,bottom-66*size,p);
            c.drawOval(x-30*size,bottom-102*size,x+6*size,bottom-67*size,p);
            c.drawOval(x-5*size,bottom-128*size,x+30*size,bottom-75*size,p);
        }
        @Override protected void onDraw(Canvas canvas){
            float w=getWidth(),h=getHeight();if(w<=0||h<=0)return;
            canvas.save();canvas.scale(w/400f,h/300f);
            color(0xFF153E35);canvas.drawRect(0,0,400,300,p);
            color(0xFF255546);canvas.drawOval(225,-95,500,175,p);
            color(0xFF396654);canvas.drawCircle(346,38,42,p);
            hill(canvas,0xFF345F4D,182,22);
            hill(canvas,0xFF244F43,213,31);
            hill(canvas,0xFF173F38,246,23);
            for(int i=0;i<10;i++)tree(canvas,12+i*47,310,(i%3==0?1.2f:.79f),0xFF102F2E);
            color(0xFF789777);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.4f);
            Path stream=new Path();stream.moveTo(246,300);stream.cubicTo(234,272,284,260,260,226);stream.cubicTo(244,215,273,205,270,192);
            canvas.drawPath(stream,p);p.setStyle(Paint.Style.FILL);
            canvas.restore();
        }
    }
}
