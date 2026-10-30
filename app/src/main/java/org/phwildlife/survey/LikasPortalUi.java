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

/** Compact field portal with approved pixel-art tiles and vector fallback symbols. */
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
    /** Exact owner-approved green pixel art; the launcher remains a separate, untouched resource. */
    private static int approvedIconResource(String symbol){
        if(symbol==null)return 0;
        switch(symbol){
            case "field":case "fieldwork":return R.drawable.likas_approved_fieldwork;
            case "library":case "species":case "guide":
                return R.drawable.likas_approved_species_library;
            case "settings":return R.drawable.likas_approved_settings;
            case "bird":case "birds":return R.drawable.likas_approved_birds_writhed_hornbill;
            case "frog":case "herpetofauna":
                return R.drawable.likas_approved_herpetofauna_staurois_natator;
            case "mammal":case "mammals":return R.drawable.likas_approved_mammals_rusa_marianna;
            case "habitat":case "habitats":return R.drawable.likas_approved_habitats;
            case "research":return R.drawable.likas_approved_research;
            case "news":return R.drawable.likas_approved_news;
            case "history":return R.drawable.likas_approved_history;
            case "chart":case "analysis":return R.drawable.likas_approved_analysis;
            case "backup":return R.drawable.likas_approved_backup;
            case "photo":case "camera":case "trap":return R.drawable.likas_approved_camera;
            case "gps":return R.drawable.likas_approved_gps;
            case "map":case "project":case "projects":return R.drawable.likas_approved_projects;
            case "protocol":case "report":case "reports":return R.drawable.likas_approved_reports;
            case "offline":return R.drawable.likas_approved_offline;
            case "help":return R.drawable.likas_approved_help;
            default:return 0; // The approved set has no audio/conservation glyph.
        }
    }
    /** Preserve precise pixel edges at all device densities; unknown symbols keep legacy glyphs. */
    private static void addApprovedIcon(FrameLayout frame,Activity a,String symbol,int glyphColor,
            int approvedDp,int fallbackDp){
        int res=approvedIconResource(symbol);
        View art;
        int size;
        if(res!=0){
            ImageView image=new ImageView(a);
            android.graphics.drawable.Drawable drawable=a.getResources().getDrawable(res,a.getTheme());
            if(drawable instanceof android.graphics.drawable.BitmapDrawable){
                ((android.graphics.drawable.BitmapDrawable)drawable).setFilterBitmap(false);
            }
            image.setImageDrawable(drawable);
            image.setScaleType(ImageView.ScaleType.FIT_CENTER);
            image.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            art=image;size=approvedDp;
        }else{
            art=new Glyph(a,symbol,glyphColor);size=fallbackDp;
        }
        frame.addView(art,new FrameLayout.LayoutParams(dp(a,size),dp(a,size),Gravity.CENTER));
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
        cover.setMinimumHeight(dp(a,151));
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
        TextView title=text(a,"Philippine wildlife",26,CREAM,true);
        LinearLayout.LayoutParams titleLp=new LinearLayout.LayoutParams(-1,-2);
        titleLp.topMargin=dp(a,19);words.addView(title,titleLp);
        TextView sub=text(a,"Surveys · Species · Habitats",13,Color.rgb(222,235,218),false);
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
        cover.setContentDescription("LIKAS. Philippine wildlife surveys, species, and habitats.");
        return cover;
    }

    /** Compact content-sized navigation card. Never uses match-parent children in a scrolling row. */
    public static View tile(Activity a,String title,String detail,String icon,
            int tint,int background,Runnable open){
        final boolean field="Fieldwork".equals(title);
        final boolean large=a.getResources().getConfiguration().fontScale>=1.60f;
        final int fill=field?CANOPY:("Settings".equals(title)?
            Color.rgb(231,240,236):Color.rgb(236,245,231));
        final int ink=field?CREAM:CANOPY;
        LinearLayout panel=new LinearLayout(a);
        panel.setOrientation(LinearLayout.HORIZONTAL);
        panel.setGravity(Gravity.CENTER_VERTICAL);
        panel.setPadding(dp(a,18),dp(a,12),dp(a,14),dp(a,12));
        panel.setMinimumHeight(dp(a,108));
        panel.setBackground(tapBackground(a,fill,Color.TRANSPARENT,19));
        panel.setElevation(dp(a,1));
        LinearLayout copy=new LinearLayout(a);
        copy.setOrientation(LinearLayout.VERTICAL);
        TextView name=text(a,title,large?20:21,ink,true);
        copy.addView(name,new LinearLayout.LayoutParams(-1,-2));
        if(detail!=null&&!detail.isEmpty()){
            TextView caption=text(a,detail,large?13:12,
                field?Color.rgb(225,237,215):MOSS,false);
            caption.setMaxLines(large?3:2);
            LinearLayout.LayoutParams captionLp=new LinearLayout.LayoutParams(-1,-2);
            captionLp.topMargin=dp(a,5);
            copy.addView(caption,captionLp);
        }
        panel.addView(copy,new LinearLayout.LayoutParams(0,-2,1f));
        FrameLayout art=new FrameLayout(a);
        art.setBackground(rounded(a,field?Color.argb(42,236,247,218):
            Color.argb(90,255,255,255),Color.TRANSPARENT,14));
        addApprovedIcon(art,a,icon,field?CREAM:CANOPY,62,36);
        LinearLayout.LayoutParams artLp=new LinearLayout.LayoutParams(dp(a,62),dp(a,62));
        artLp.leftMargin=dp(a,10);
        panel.addView(art,artLp);
        panel.setContentDescription("Open "+title+
            (detail==null||detail.isEmpty()?"":". "+detail));
        panel.setFocusable(true);
        panel.setClickable(true);
        panel.setOnClickListener(v->open.run());
        return panel;
    }

    /** Persistent field-session shortcut shown before the general app destinations. */
    public static View session(Activity a,Runnable resume){
        FrameLayout panel=new FrameLayout(a);
        GradientDrawable bg=new GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            new int[]{Color.rgb(25,73,65),WATER});
        bg.setCornerRadius(dp(a,20));
        panel.setBackground(bg);
        panel.setClipToOutline(true);
        panel.setMinimumHeight(dp(a,92));
        LinearLayout row=new LinearLayout(a);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(dp(a,17),dp(a,12),dp(a,14),dp(a,12));
        View dot=new View(a);
        dot.setBackground(rounded(a,LIME,Color.TRANSPARENT,20));
        LinearLayout.LayoutParams dotLp=new LinearLayout.LayoutParams(dp(a,10),dp(a,10));
        dotLp.rightMargin=dp(a,12);
        row.addView(dot,dotLp);
        LinearLayout text=new LinearLayout(a);
        text.setOrientation(LinearLayout.VERTICAL);
        TextView title=text(a,"Survey in progress",17,CREAM,true);
        text.addView(title);
        TextView caption=text(a,"Resume field recording",12,Color.rgb(226,243,225),false);
        text.addView(caption);
        row.addView(text,new LinearLayout.LayoutParams(0,-2,1f));
        TextView arrow=text(a,"›",26,LIME,true);
        row.addView(arrow,new LinearLayout.LayoutParams(dp(a,28),-2));
        panel.addView(row,new FrameLayout.LayoutParams(-1,-2,Gravity.CENTER_VERTICAL));
        panel.setFocusable(true);
        panel.setClickable(true);
        panel.setContentDescription("Resume survey in progress. Return to field recording");
        panel.setOnClickListener(v->resume.run());
        return panel;
    }

    /** Compact illustrated tile for field methods, library tools and analysis. */
    public static View mini(Activity a,String title,String detail,String symbol,Runnable open){
        LinearLayout panel=new LinearLayout(a);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(a,15),dp(a,13),dp(a,12),dp(a,13));
        panel.setBackground(tapBackground(a,Color.WHITE,Color.rgb(219,230,215),18));
        panel.setMinimumHeight(dp(a,126));
        FrameLayout art=new FrameLayout(a);
        art.setBackground(rounded(a,Color.rgb(230,240,227),Color.TRANSPARENT,13));
        addApprovedIcon(art,a,symbol,CANOPY,49,30);
        panel.addView(art,new LinearLayout.LayoutParams(dp(a,49),dp(a,49)));
        TextView name=text(a,title,16,CANOPY,true);
        LinearLayout.LayoutParams nameLp=new LinearLayout.LayoutParams(-1,-2);
        nameLp.topMargin=dp(a,9);
        panel.addView(name,nameLp);
        if(detail!=null&&!detail.isEmpty()){
            TextView sub=text(a,detail,11,UiKit.MUTED,false);
            sub.setMaxLines(2);
            LinearLayout.LayoutParams subLp=new LinearLayout.LayoutParams(-1,-2);
            subLp.topMargin=dp(a,3);
            panel.addView(sub,subLp);
        }
        panel.setContentDescription("Open "+title+
            (detail==null||detail.isEmpty()?"":". "+detail));
        panel.setFocusable(true);
        panel.setClickable(true);
        panel.setOnClickListener(v->open.run());
        return panel;
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
        addApprovedIcon(plate,a,symbol,CANOPY,47,28);
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
                case "bird":
                    path.moveTo(15,64);path.cubicTo(30,43,55,43,75,62);
                    path.cubicTo(58,72,38,78,15,64);canvas.drawPath(path,p);
                    path.reset();path.moveTo(32,56);
                    path.quadTo(48,28,76,26);
                    path.quadTo(66,50,43,66);canvas.drawPath(path,p);
                    circle(canvas,71,52,3);line(canvas,79,56,90,61);break;
                case "frog":
                    canvas.drawArc(18,31,82,87,185,170,false,p);
                    circle(canvas,32,34,12);circle(canvas,68,34,12);
                    circle(canvas,32,34,3);circle(canvas,68,34,3);
                    canvas.drawArc(31,49,69,66,5,170,false,p);
                    line(canvas,8,82,34,82);line(canvas,66,82,92,82);break;
                case "snake":
                    path.moveTo(20,60);path.cubicTo(30,22,68,19,74,39);
                    path.cubicTo(82,66,24,61,40,77);
                    path.cubicTo(48,84,65,75,71,72);canvas.drawPath(path,p);
                    canvas.drawOval(60,58,85,73,p);
                    circle(canvas,77,62,2);
                    line(canvas,86,66,95,66);break;
                case "forest":
                    path.moveTo(18,73);path.lineTo(35,23);path.lineTo(52,73);
                    path.moveTo(49,73);path.lineTo(66,16);path.lineTo(84,73);
                    canvas.drawPath(path,p);
                    line(canvas,35,72,35,89);line(canvas,66,72,66,89);
                    line(canvas,13,89,88,89);break;
                case "karst":
                    path.moveTo(10,82);path.lineTo(34,23);path.lineTo(44,46);
                    path.lineTo(63,17);path.lineTo(91,82);canvas.drawPath(path,p);
                    line(canvas,34,23,28,67);line(canvas,63,17,67,57);
                    line(canvas,14,86,86,86);break;
                case "wetland":
                    path.moveTo(23,15);path.lineTo(23,68);
                    path.moveTo(38,21);path.lineTo(38,65);
                    path.moveTo(55,30);path.lineTo(55,67);
                    canvas.drawPath(path,p);
                    canvas.drawArc(13,15,33,44,90,180,false,p);
                    canvas.drawArc(29,24,48,53,90,180,false,p);
                    line(canvas,10,77,90,77);
                    line(canvas,23,87,65,87);break;
                case "coast":
                    path.moveTo(15,24);path.lineTo(15,73);path.lineTo(40,73);
                    path.lineTo(40,47);path.lineTo(68,47);canvas.drawPath(path,p);
                    canvas.drawArc(48,58,88,82,20,140,false,p);
                    canvas.drawArc(20,74,64,95,20,140,false,p);break;
                case "fields":
                    line(canvas,11,84,90,84);
                    path.moveTo(50,80);path.lineTo(50,36);
                    path.moveTo(49,54);path.cubicTo(25,51,20,34,27,27);
                    path.cubicTo(44,27,51,41,49,54);
                    path.moveTo(51,57);path.cubicTo(74,49,79,34,73,27);
                    path.cubicTo(59,30,50,43,51,57);
                    canvas.drawPath(path,p);break;
                case "mammal":
                    canvas.drawArc(20,19,45,51,180,215,false,p);
                    canvas.drawArc(55,19,80,51,150,215,false,p);
                    canvas.drawOval(23,34,77,84,p);
                    circle(canvas,38,56,3);circle(canvas,62,56,3);
                    canvas.drawOval(46,65,54,73,p);break;
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
