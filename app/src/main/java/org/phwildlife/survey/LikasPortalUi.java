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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Small, original vector symbols and spacious home cards. No external font or image dependency. */
public final class LikasPortalUi {
    private LikasPortalUi(){}
    public static final int CANOPY=Color.rgb(20,55,43);
    public static final int DEEP=Color.rgb(12,43,38);
    public static final int MOSS=Color.rgb(92,130,91);
    public static final int WATER=Color.rgb(49,116,124);
    public static final int CREAM=Color.rgb(255,252,241);
    public static final int LIME=Color.rgb(207,227,162);
    public static final int SAND=Color.rgb(245,238,222);
    private static int dp(Activity a,int value){
        return Math.round(a.getResources().getDisplayMetrics().density*value);
    }
    private static GradientDrawable rounded(Activity a,int fill,int stroke,int radius){
        GradientDrawable d=new GradientDrawable();d.setColor(fill);
        d.setCornerRadius(dp(a,radius));
        if(stroke!=Color.TRANSPARENT)d.setStroke(dp(a,1),stroke);
        return d;
    }
    private static android.graphics.drawable.Drawable tapBackground(Activity a,int fill,int stroke,int radius){
        GradientDrawable solid=rounded(a,fill,stroke,radius);
        GradientDrawable mask=rounded(a,Color.WHITE,Color.TRANSPARENT,radius);
        return new RippleDrawable(ColorStateList.valueOf(Color.argb(35,55,127,102)),solid,mask);
    }
    public static TextView text(Activity a,String label,int size,int color,boolean bold){
        TextView t=new TextView(a);t.setText(label);t.setTextSize(size);t.setTextColor(color);
        t.setFontFeatureSettings("kern");t.setIncludeFontPadding(true);
        if(bold)t.setTypeface(Typeface.create("sans-serif-medium",Typeface.BOLD));
        return t;
    }
    public static TextView kicker(Activity a,String s){
        TextView t=text(a,s,11,MOSS,true);
        t.setLetterSpacing(.16f);t.setPadding(dp(a,1),dp(a,16),0,dp(a,7));
        return t;
    }

    public static View hero(Activity a){
        FrameLayout cover=new FrameLayout(a);
        GradientDrawable backdrop=new GradientDrawable(GradientDrawable.Orientation.TL_BR,
            new int[]{DEEP,CANOPY,WATER});
        backdrop.setCornerRadius(dp(a,26));cover.setBackground(backdrop);
        cover.setClipToOutline(true);
        cover.setMinimumHeight(dp(a,177));
        // Vector-only contour lines gently evoke a shaded forest canopy.
        View contours=new View(a){
            private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
            @Override protected void onDraw(Canvas canvas){
                super.onDraw(canvas);
                float w=getWidth(),h=getHeight();
                p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(a,1));
                p.setColor(Color.argb(29,230,250,212));
                for(int j=0;j<5;j++){
                    Path path=new Path();
                    path.moveTo(w*(.36f+j*.13f),-h*.2f);
                    path.cubicTo(w*(.05f+j*.18f),h*.32f,
                        w*(.53f+j*.10f),h*.66f,w*(.16f+j*.25f),h*1.18f);
                    canvas.drawPath(path,p);
                }
                p.setStyle(Paint.Style.FILL);p.setColor(Color.argb(34,207,227,162));
                canvas.drawCircle(w*.91f,h*.17f,dp(a,66),p);
                p.setColor(Color.argb(28,245,255,211));
                canvas.drawCircle(w*.89f,h*.19f,dp(a,42),p);
            }
        };
        cover.addView(contours,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout words=new LinearLayout(a);words.setOrientation(LinearLayout.VERTICAL);
        words.setPadding(dp(a,22),dp(a,20),dp(a,18),dp(a,19));
        TextView mark=text(a,"LIKAS   /   PH",12,LIME,true);mark.setLetterSpacing(.14f);
        words.addView(mark);
        TextView title=text(a,"Nature, closer.",29,CREAM,true);
        LinearLayout.LayoutParams titleLp=new LinearLayout.LayoutParams(-1,-2);
        titleLp.topMargin=dp(a,19);words.addView(title,titleLp);
        TextView sub=text(a,"Field notes. Wild places.",14,Color.rgb(222,235,218),false);
        LinearLayout.LayoutParams subLp=new LinearLayout.LayoutParams(-1,-2);
        subLp.topMargin=dp(a,4);words.addView(sub,subLp);
        cover.addView(words,new FrameLayout.LayoutParams(-1,-1));
        ImageView frog=new ImageView(a);
        frog.setImageResource(R.drawable.likas_natator_frog);
        frog.setScaleType(ImageView.ScaleType.FIT_CENTER);
        frog.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        FrameLayout.LayoutParams iconLp=new FrameLayout.LayoutParams(
            dp(a,54),dp(a,54),Gravity.TOP|Gravity.RIGHT);
        iconLp.topMargin=dp(a,14);iconLp.rightMargin=dp(a,14);
        cover.addView(frog,iconLp);
        cover.setContentDescription("LIKAS. Nature, closer. Field notes and wild places.");
        return cover;
    }

    public static View tile(Activity a,String title,String detail,String icon,
            int tint,int background,Runnable open){
        LinearLayout row=new LinearLayout(a);row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(a,17),dp(a,14),dp(a,16),dp(a,14));
        row.setBackground(tapBackground(a,background,Color.rgb(225,230,221),22));
        row.setMinimumHeight(dp(a,104));row.setElevation(dp(a,1));
        FrameLayout art=new FrameLayout(a);
        art.setBackground(rounded(a,tint,Color.TRANSPARENT,17));
        Glyph glyph=new Glyph(a,icon,title.equals("Fieldwork")?CREAM:CANOPY);
        FrameLayout.LayoutParams gp=new FrameLayout.LayoutParams(dp(a,43),dp(a,43),Gravity.CENTER);
        art.addView(glyph,gp);
        row.addView(art,new LinearLayout.LayoutParams(dp(a,64),dp(a,64)));
        LinearLayout copy=new LinearLayout(a);copy.setOrientation(LinearLayout.VERTICAL);
        TextView name=text(a,title,21,CANOPY,true);
        copy.addView(name);
        TextView caption=text(a,detail,12,UiKit.MUTED,false);
        LinearLayout.LayoutParams cc=new LinearLayout.LayoutParams(-1,-2);
        cc.topMargin=dp(a,5);copy.addView(caption,cc);
        LinearLayout.LayoutParams contentLp=new LinearLayout.LayoutParams(0,-2,1);
        contentLp.leftMargin=dp(a,16);
        row.addView(copy,contentLp);
        TextView next=text(a,"›",29,WATER,false);
        next.setGravity(Gravity.CENTER);
        row.addView(next,new LinearLayout.LayoutParams(dp(a,23),dp(a,50)));
        row.setFocusable(true);row.setClickable(true);
        row.setContentDescription(title+". "+detail);
        row.setOnClickListener(v->open.run());
        return row;
    }

    public static View action(Activity a,String title,String subtitle,String symbol,
            Runnable open){
        LinearLayout item=new LinearLayout(a);item.setOrientation(LinearLayout.HORIZONTAL);
        item.setGravity(Gravity.CENTER_VERTICAL);
        item.setBackground(tapBackground(a,Color.WHITE,Color.rgb(226,233,225),17));
        item.setPadding(dp(a,14),dp(a,12),dp(a,12),dp(a,12));
        item.setMinimumHeight(dp(a,75));
        FrameLayout plate=new FrameLayout(a);
        plate.setBackground(rounded(a,Color.rgb(231,241,231),Color.TRANSPARENT,13));
        plate.addView(new Glyph(a,symbol,CANOPY),
            new FrameLayout.LayoutParams(dp(a,28),dp(a,28),Gravity.CENTER));
        item.addView(plate,new LinearLayout.LayoutParams(dp(a,47),dp(a,47)));
        LinearLayout copy=new LinearLayout(a);copy.setOrientation(LinearLayout.VERTICAL);
        TextView name=text(a,title,16,CANOPY,true);copy.addView(name);
        if(subtitle!=null&&!subtitle.isEmpty()){
            TextView detail=text(a,subtitle,12,UiKit.MUTED,false);
            LinearLayout.LayoutParams x=new LinearLayout.LayoutParams(-1,-2);
            x.topMargin=dp(a,3);copy.addView(detail,x);
        }
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1);
        lp.leftMargin=dp(a,13);item.addView(copy,lp);
        TextView arrow=text(a,"›",24,WATER,false);item.addView(arrow);
        item.setFocusable(true);item.setClickable(true);
        item.setContentDescription("Open "+title);
        item.setOnClickListener(v->open.run());
        return item;
    }

    /** Hand-drawn vector glyphs read well at 24–48dp. Pure decoration, parent has the label. */
    public static final class Glyph extends View {
        private final String kind;
        private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        private final int color;
        Glyph(Activity a,String kind,int color){super(a);this.kind=kind;this.color=color;setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
        private void line(Canvas c,float x,float y,float xx,float yy){c.drawLine(x,y,xx,yy,p);}
        private void circle(Canvas c,float x,float y,float r){c.drawCircle(x,y,r,p);}
        @Override protected void onDraw(Canvas canvas){
            super.onDraw(canvas);
            canvas.save();
            float k=Math.min(getWidth(),getHeight())/100f;
            canvas.translate((getWidth()-100*k)/2,(getHeight()-100*k)/2);
            canvas.scale(k,k);
            p.setColor(color);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(5);
            p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);
            Path path=new Path();
            switch(kind){
                case "field":
                    circle(canvas,50,50,36);
                    path.moveTo(68,29);path.lineTo(57,57);path.lineTo(31,69);
                    path.lineTo(43,43);path.close();canvas.drawPath(path,p);
                    p.setStyle(Paint.Style.FILL);circle(canvas,50,50,3);break;
                case "library":case "species":
                    path.moveTo(29,78);path.cubicTo(19,49,38,22,78,18);
                    path.cubicTo(82,58,59,82,29,78);canvas.drawPath(path,p);
                    line(canvas,24,85,65,43);line(canvas,44,64,44,44);
                    line(canvas,44,64,63,66);break;
                case "settings":
                    line(canvas,24,26,76,26);line(canvas,24,50,76,50);
                    line(canvas,24,74,76,74);
                    p.setStyle(Paint.Style.FILL);circle(canvas,39,26,9);
                    circle(canvas,64,50,9);circle(canvas,45,74,9);break;
                case "habitat":
                    path.moveTo(12,72);path.lineTo(35,39);path.lineTo(49,57);
                    path.lineTo(67,24);path.lineTo(89,72);canvas.drawPath(path,p);
                    line(canvas,14,81,88,81);break;
                case "history":
                    canvas.drawArc(19,19,81,81,-75,285,false,p);
                    path.moveTo(16,28);path.lineTo(17,49);path.lineTo(37,41);canvas.drawPath(path,p);
                    line(canvas,51,32,51,51);line(canvas,51,51,65,58);break;
                case "chart":
                    line(canvas,19,81,83,81);line(canvas,23,81,23,59);
                    line(canvas,44,81,44,36);line(canvas,65,81,65,20);break;
                case "audio":
                    line(canvas,18,49,28,49);line(canvas,32,36,32,63);
                    line(canvas,44,24,44,76);line(canvas,56,39,56,60);
                    line(canvas,68,30,68,69);line(canvas,79,49,86,49);break;
                case "backup":
                    line(canvas,49,17,49,64);line(canvas,32,49,49,65);
                    line(canvas,66,49,49,65);line(canvas,22,75,22,83);
                    line(canvas,22,83,77,83);line(canvas,77,83,77,75);break;
                case "photo":
                    canvas.drawRoundRect(14,22,86,77,7,7,p);
                    circle(canvas,33,39,7);
                    path.moveTo(22,70);path.lineTo(45,49);path.lineTo(58,62);
                    path.lineTo(70,51);path.lineTo(83,66);canvas.drawPath(path,p);break;
                case "map":case "protocol":
                    path.moveTo(14,29);path.lineTo(35,20);path.lineTo(64,29);
                    path.lineTo(86,20);path.lineTo(86,71);path.lineTo(64,80);
                    path.lineTo(35,71);path.lineTo(14,80);path.close();canvas.drawPath(path,p);
                    line(canvas,35,20,35,71);line(canvas,64,29,64,80);break;
                case "shield":
                    path.moveTo(50,13);path.lineTo(80,25);path.lineTo(77,57);
                    path.quadTo(69,79,50,87);path.quadTo(31,79,23,57);
                    path.lineTo(20,25);path.close();canvas.drawPath(path,p);
                    line(canvas,36,50,46,61);line(canvas,46,61,67,38);break;
                default:
                    circle(canvas,50,50,30);line(canvas,36,50,64,50);break;
            }
            canvas.restore();
        }
    }
}
