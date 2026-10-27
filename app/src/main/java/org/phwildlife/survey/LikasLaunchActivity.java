package org.phwildlife.survey;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;
import java.io.File;

/**
 * LIKAS startup entry. The forest is decorative, and both navigation choices
 * remain native, readable controls. Never starts GPS or changes survey data.
 */
public final class LikasLaunchActivity extends Activity {
    private static final int DEEP=Color.rgb(8,39,30);
    private static final int CREAM=Color.rgb(239,243,223);
    private static final int SAGE=Color.rgb(191,222,205);
    private Button resumeButton,homeButton;
    private TextView statusView;

    private int dp(float n){return Math.round(getResources().getDisplayMetrics().density*n);}

    private TextView label(String value,int size,int color,boolean strong){
        TextView t=new TextView(this);
        t.setText(value);
        t.setTextColor(color);
        t.setTextSize(size);
        t.setGravity(Gravity.CENTER);
        if(strong)t.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));
        return t;
    }

    /** One standard action shape: no duplicate drawables or developer-only labels. */
    private Button action(String title,boolean primary){
        Button b=new Button(this);
        b.setText(title);
        b.setAllCaps(false);
        b.setTextSize(16);
        b.setMaxLines(2);
        b.setGravity(Gravity.CENTER);
        b.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));
        float fontScale=getResources().getConfiguration().fontScale;
        b.setMinHeight(dp(FieldLayoutPolicy.actionHeightDp(fontScale)+8));
        b.setPadding(dp(14),dp(12),dp(14),dp(12));
        styleAction(b,primary);
        return b;
    }

    private void styleAction(Button b,boolean primary){
        int background=primary?CREAM:Color.argb(172,8,39,30);
        int foreground=primary?DEEP:CREAM;
        GradientDrawable shape=new GradientDrawable();
        shape.setColor(background);
        shape.setStroke(dp(1),primary?CREAM:Color.rgb(135,181,154));
        shape.setCornerRadius(dp(15));
        GradientDrawable mask=new GradientDrawable();
        mask.setColor(Color.WHITE);
        mask.setCornerRadius(dp(15));
        b.setBackground(new RippleDrawable(
            ColorStateList.valueOf(primary?Color.argb(40,8,39,30):Color.argb(45,255,255,255)),
            shape,mask));
        b.setBackgroundTintList(null);
        b.setTextColor(foreground);
    }

    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        getWindow().setStatusBarColor(DEEP);
        getWindow().setNavigationBarColor(DEEP);

        FrameLayout root=new FrameLayout(this);
        root.setBackgroundColor(DEEP);
        LikasForestView forest=new LikasForestView(this);
        root.addView(forest,new FrameLayout.LayoutParams(-1,-1));

        // Let the approved forest remain visible, with a darker action area.
        View contrast=new View(this);
        contrast.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        contrast.setBackground(new GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            new int[]{Color.argb(84,8,39,30),Color.argb(46,8,39,30),
                Color.argb(114,8,39,30),Color.argb(209,8,39,30)}));
        root.addView(contrast,new FrameLayout.LayoutParams(-1,-1));

        LinearLayout content=new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_HORIZONTAL);
        content.setPadding(dp(23),dp(16),dp(23),dp(24));
        root.addView(content,new FrameLayout.LayoutParams(-1,-1));

        content.addView(new Space(this),new LinearLayout.LayoutParams(1,0,.75f));

        ImageView icon=new ImageView(this);
        icon.setImageResource(R.drawable.likas_frog_icon);
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        // The adjacent LIKAS wordmark supplies the spoken identity.
        icon.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        int logoSize=getResources().getConfiguration().fontScale>=1.5f?dp(94):dp(116);
        content.addView(icon,new LinearLayout.LayoutParams(logoSize,logoSize));

        TextView brand=label("LIKAS",40,CREAM,true);
        brand.setLetterSpacing(.11f);
        LinearLayout.LayoutParams brandLp=new LinearLayout.LayoutParams(-1,-2);
        brandLp.topMargin=dp(4);
        content.addView(brand,brandLp);

        TextView subtitle=label("Philippine wildlife surveys",14,SAGE,false);
        LinearLayout.LayoutParams subLp=new LinearLayout.LayoutParams(-1,-2);
        subLp.topMargin=dp(2);
        content.addView(subtitle,subLp);

        content.addView(new Space(this),new LinearLayout.LayoutParams(1,0,2.0f));

        LinearLayout actions=new LinearLayout(this);
        actions.setOrientation(LinearLayout.VERTICAL);
        actions.setPadding(dp(14),dp(14),dp(14),dp(14));
        GradientDrawable panel=new GradientDrawable();
        panel.setColor(Color.argb(162,8,39,30));
        panel.setStroke(dp(1),Color.argb(80,191,222,205));
        panel.setCornerRadius(dp(21));
        actions.setBackground(panel);
        LinearLayout.LayoutParams panelLp=new LinearLayout.LayoutParams(-1,-2);
        content.addView(actions,panelLp);

        statusView=label("Unable to check saved surveys.",13,CREAM,false);
        statusView.setPadding(dp(4),0,dp(4),dp(12));
        statusView.setVisibility(View.GONE);
        actions.addView(statusView);

        resumeButton=action("Resume survey",true);
        actions.addView(resumeButton,new LinearLayout.LayoutParams(-1,-2));
        resumeButton.setOnClickListener(v->{
            if(!Boolean.TRUE.equals(activeSurveyStatus())){
                updateActions();
                return;
            }
            startActivity(new Intent(this,MainActivity.class).putExtra("resume",true));
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
        });

        // This always opens Home. It never silently starts a new field event.
        homeButton=action("Open LIKAS",false);
        LinearLayout.LayoutParams homeLp=new LinearLayout.LayoutParams(-1,-2);
        homeLp.topMargin=dp(9);
        actions.addView(homeButton,homeLp);
        homeButton.setOnClickListener(v->{
            Intent home=new Intent(this,WelcomeActivity.class);
            home.putExtra("show_menu",true);
            startActivity(home);
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
        });

        setContentView(root);
    }

    /** Null means unreadable database, never "there are no surveys". */
    private Boolean activeSurveyStatus(){
        File file=getDatabasePath(FieldDb.NAME);
        if(!file.exists())return false;
        try(SQLiteDatabase database=SQLiteDatabase.openDatabase(
                file.getAbsolutePath(),null,SQLiteDatabase.OPEN_READONLY);
            Cursor c=database.rawQuery(
                "SELECT 1 FROM surveys WHERE ended IS NULL LIMIT 1",null)){
            return c.moveToFirst();
        }catch(Exception ignored){
            // Unreadable is not equivalent to no unfinished survey.
            return null;
        }
    }

    private void updateActions(){
        Boolean status=activeSurveyStatus();
        boolean active=Boolean.TRUE.equals(status);
        statusView.setVisibility(status==null?View.VISIBLE:View.GONE);
        resumeButton.setVisibility(active?View.VISIBLE:View.GONE);
        resumeButton.setEnabled(LikasUiRules.canContinue(active));
        // The only visible action is primary if no unfinished survey exists.
        styleAction(homeButton,!active);
        homeButton.setContentDescription("Open LIKAS home and survey choices");
        resumeButton.setContentDescription("Resume unfinished survey");
    }

    @Override protected void onResume(){
        super.onResume();
        if(resumeButton!=null&&homeButton!=null)updateActions();
    }
}
