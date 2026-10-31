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
    public static final int CANOPY=UiKit.INK;
    public static final int DEEP=UiKit.DEEP;
    public static final int MOSS=UiKit.MUTED;
    public static final int WATER=UiKit.POND;
    public static final int CREAM=Color.rgb(250,253,243);
    public static final int LIME=UiKit.HIGHLIGHT;
    public static final int SAND=UiKit.BG;
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
            // New generated art stays separate from the 18 immutable owner-approved assets.
            case "library-conservation":return R.drawable.likas_library_conservation_display;
            case "library-habitat-links":return R.drawable.likas_library_habitat_links_display;
            case "research":return R.drawable.likas_approved_research;
            case "news":return R.drawable.likas_approved_news;
            case "history":return R.drawable.likas_approved_history;
            case "chart":case "analysis":return R.drawable.likas_approved_analysis;
            case "backup":return R.drawable.likas_approved_backup;
            case "library-photo":return R.drawable.likas_library_photos_display;
            case "photo":case "camera":case "trap":return R.drawable.likas_approved_camera;
            case "gps":return R.drawable.likas_approved_gps;
            case "map":case "project":case "projects":return R.drawable.likas_approved_projects;
            case "library-sources":return R.drawable.likas_library_sources_display;
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
            // Library-only cropped 256px PNGs share the same optical framing on high-density
            // phones. FIT_CENTER fills the same 50dp plate without crop;
            // setFilterBitmap(false) above preserves source pixel edges. The approved
            // 512px approved source images remain byte-identical, including Photos and Sources.
            boolean libraryArt="library-conservation".equals(symbol) ||
                "library-habitat-links".equals(symbol) ||
                "library-photo".equals(symbol) ||
                "library-sources".equals(symbol);
            image.setScaleType(libraryArt?
                ImageView.ScaleType.FIT_CENTER:ImageView.ScaleType.CENTER_INSIDE);
            image.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            art=image;size=approvedDp;
        }else{
            art=new Glyph(a,symbol,glyphColor);size=fallbackDp;
        }
        // Clip the image itself with the holder's rounded outline. Keep PNG bytes intact.
        frame.setClipToOutline(true);
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
        return new RippleDrawable(ColorStateList.valueOf(Color.argb(28,46,105,65)),solid,mask);
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
        return hero(a,false);
    }
    /** Active surveys receive a smaller header; font-scaled text controls actual height. */
    public static View hero(Activity a,boolean compact){
        // Draw contours behind real text; never let a decorative MATCH_PARENT
        // child force a wrap-content card to occupy an entire scroll viewport.
        FrameLayout cover=new FrameLayout(a){
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
        cover.setWillNotDraw(false);
        GradientDrawable backdrop=new GradientDrawable(GradientDrawable.Orientation.TL_BR,
            new int[]{DEEP,CANOPY,UiKit.FOREST});
        backdrop.setCornerRadius(dp(a,26));cover.setBackground(backdrop);
        cover.setClipToOutline(true);
        // Reduce only the large-font Home hero; preserve the normal-size design.
        final boolean largeHomeText=!compact && a.getResources().getConfiguration().fontScale>=1.60f;
        cover.setMinimumHeight(dp(a,compact?88:(largeHomeText?118:151)));
        LinearLayout words=new LinearLayout(a);words.setOrientation(LinearLayout.VERTICAL);
        boolean showFrog=a.getResources().getConfiguration().fontScale<1.30f;
        // Reserve illustration clearance only when art is drawn beside the text.
        words.setPadding(dp(a,22),dp(a,compact?12:(largeHomeText?11:20)),
            dp(a,showFrog?76:20),dp(a,compact?12:(largeHomeText?11:19)));
        TextView mark=text(a,"LIKAS",
            compact?22:12,compact?CREAM:LIME,true);
        mark.setLetterSpacing(compact ? 0.05f : 0.14f);
        words.addView(mark);
        TextView title=text(a,"Philippine wildlife",compact?14:(largeHomeText?21:26),
            compact?Color.rgb(222,235,218):CREAM,!compact);
        LinearLayout.LayoutParams titleLp=new LinearLayout.LayoutParams(-1,-2);
        titleLp.topMargin=dp(a,compact?1:(largeHomeText?8:19));words.addView(title,titleLp);
        if(!compact){
            TextView sub=text(a,"Explore. Record. Conserve.",largeHomeText?12:13,Color.rgb(222,235,218),false);
            LinearLayout.LayoutParams subLp=new LinearLayout.LayoutParams(-1,-2);
            subLp.topMargin=dp(a,4);words.addView(sub,subLp);
        }
        cover.addView(words,new FrameLayout.LayoutParams(-1,-2,Gravity.TOP));
        ImageView frog=new ImageView(a);
        frog.setImageResource(R.drawable.likas_natator_frog);
        frog.setScaleType(ImageView.ScaleType.FIT_CENTER);
        frog.setBackground(rounded(a,UiKit.LEAF_LIGHT,Color.TRANSPARENT,12));
        frog.setClipToOutline(true);
        frog.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        FrameLayout.LayoutParams iconLp=new FrameLayout.LayoutParams(
            dp(a,compact?46:54),dp(a,compact?46:54),Gravity.TOP|Gravity.RIGHT);
        iconLp.topMargin=dp(a,compact?10:14);iconLp.rightMargin=dp(a,14);
        if(showFrog)cover.addView(frog,iconLp);
        cover.setContentDescription("LIKAS. Philippine wildlife surveys, species, and habitats.");
        return cover;
    }

    /** Compact content-sized navigation card. Never uses match-parent children in a scrolling row. */
    public static View tile(Activity a,String title,String detail,String icon,
            int tint,int background,Runnable open){
        final boolean field="Fieldwork".equals(title);
        final boolean large=a.getResources().getConfiguration().fontScale>=1.60f;
        // Use the caller's semantic card surface without changing approved art.
        final int fill=tint;
        final int ink=field?CREAM:CANOPY;
        LinearLayout panel=new LinearLayout(a);
        panel.setOrientation(LinearLayout.HORIZONTAL);
        panel.setGravity(Gravity.CENTER_VERTICAL);
        panel.setPadding(dp(a,18),dp(a,12),dp(a,14),dp(a,12));
        panel.setMinimumHeight(dp(a,LikasDesignSystem.HOME_CARD_MIN_DP));
        panel.setBackground(tapBackground(a,fill,
            field?Color.TRANSPARENT:UiKit.CONTROL_BORDER,
            LikasDesignSystem.RADIUS_CARD));
        panel.setElevation(dp(a,1));
        LinearLayout copy=new LinearLayout(a);
        copy.setOrientation(LinearLayout.VERTICAL);
        TextView name=text(a,title,large?18:LikasDesignSystem.TEXT_HOME_ACTION,ink,true);
        copy.addView(name,new LinearLayout.LayoutParams(-1,-2));
        if(detail!=null&&!detail.isEmpty()){
            TextView caption=text(a,detail,LikasDesignSystem.TEXT_CAPTION,
                field?Color.rgb(225,237,215):MOSS,false);
            caption.setMaxLines(large?3:2);
            LinearLayout.LayoutParams captionLp=new LinearLayout.LayoutParams(-1,-2);
            captionLp.topMargin=dp(a,5);
            copy.addView(caption,captionLp);
        }
        panel.addView(copy,new LinearLayout.LayoutParams(0,-2,1f));
        FrameLayout art=new FrameLayout(a);
        int iconSurface=field?UiKit.FOREST:
            (background==Color.WHITE?UiKit.LEAF_MID:background);
        art.setBackground(rounded(a,iconSurface,Color.TRANSPARENT,14));
        addApprovedIcon(art,a,icon,field?CREAM:CANOPY,
            LikasDesignSystem.HOME_ICON_DP,36);
        LinearLayout.LayoutParams artLp=new LinearLayout.LayoutParams(
            dp(a,LikasDesignSystem.HOME_ICON_DP),dp(a,LikasDesignSystem.HOME_ICON_DP));
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
            new int[]{DEEP,UiKit.FOREST});
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
        panel.setPadding(dp(a,14),dp(a,12),dp(a,12),dp(a,12));
        panel.setBackground(tapBackground(a,Color.WHITE,UiKit.CONTROL_BORDER,18));
        panel.setMinimumHeight(dp(a,126));
        FrameLayout art=new FrameLayout(a);
        art.setBackground(rounded(a,UiKit.LEAF_MID,Color.TRANSPARENT,13));
        addApprovedIcon(art,a,symbol,CANOPY,52,30);
        panel.addView(art,new LinearLayout.LayoutParams(dp(a,52),dp(a,52)));
        TextView name=text(a,title,LikasDesignSystem.TEXT_CARD,CANOPY,true);
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
        item.setBackground(tapBackground(a,Color.WHITE,UiKit.CONTROL_BORDER,17));
        item.setPadding(dp(a,14),dp(a,12),dp(a,12),dp(a,12));
        item.setMinimumHeight(dp(a,75));
        FrameLayout plate=new FrameLayout(a);
        plate.setBackground(rounded(a,UiKit.LEAF_MID,Color.TRANSPARENT,12));
        addApprovedIcon(plate,a,symbol,CANOPY,50,28);
        item.addView(plate,new LinearLayout.LayoutParams(dp(a,50),dp(a,50)));
        LinearLayout copy=new LinearLayout(a);copy.setOrientation(LinearLayout.VERTICAL);
        TextView name=text(a,title,LikasDesignSystem.TEXT_CARD,CANOPY,true);copy.addView(name);
        if(subtitle!=null&&!subtitle.isEmpty()){
            TextView detail=text(a,subtitle,12,UiKit.MUTED,false);
            LinearLayout.LayoutParams x=new LinearLayout.LayoutParams(-1,-2);
            x.topMargin=dp(a,3);copy.addView(detail,x);
        }
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1);
        lp.leftMargin=dp(a,13);item.addView(copy,lp);
        TextView arrow=text(a,"›",24,WATER,false);item.addView(arrow);
        item.setFocusable(true);item.setClickable(true);
        item.setContentDescription("Open "+title+
            (subtitle==null||subtitle.isEmpty()?"":". "+subtitle));
        item.setOnClickListener(v->open.run());
        return item;
    }

    /** Library-only featured navigation, with the same icon-left and arrow-right affordance as other actions. */
    public static View libraryFeature(Activity a,String title,String subtitle,
            String icon,Runnable open){
        LinearLayout panel=(LinearLayout)action(a,title,subtitle,icon,open);
        panel.setBackground(tapBackground(a,UiKit.LEAF_LIGHT,UiKit.CONTROL_BORDER,
            LikasDesignSystem.RADIUS_CARD));
        panel.setMinimumHeight(dp(a,LikasDesignSystem.HOME_CARD_MIN_DP));
        return panel;
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
