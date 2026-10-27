package org.phwildlife.survey;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
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
 * LIKAS launch destination. No database migrations, camera or GPS subscriptions
 * are started here. Existing surveys are resumed explicitly by the observer.
 */
public final class LikasLaunchActivity extends Activity {
    private Button continueButton,newSurveyButton;
    private int dp(float n){return Math.round(getResources().getDisplayMetrics().density*n);}
    private TextView label(String text,float size,int color,boolean bold){
        TextView t=new TextView(this);t.setText(text);t.setTextColor(color);t.setTextSize(size);
        t.setGravity(Gravity.CENTER);
        if(bold)t.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));
        return t;
    }
    private Button action(String title,boolean primary){
        Button b=new Button(this);
        b.setText(title);b.setTextSize(17);b.setAllCaps(false);
        b.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));
        GradientDrawable bg=new GradientDrawable();
        bg.setColor(primary?Color.rgb(237,245,234):Color.argb(45,16,45,35));
        bg.setCornerRadius(dp(16));
        bg.setStroke(dp(1),primary?Color.rgb(237,245,234):Color.rgb(222,238,224));
        b.setBackground(bg);b.setBackgroundTintList(null);
        b.setTextColor(primary?Color.rgb(21,64,45):Color.WHITE);
        b.setMinHeight(dp(59));
        return b;
    }
    private String currentVersion(){
        try{return getPackageManager().getPackageInfo(getPackageName(),0).versionName;}
        catch(Exception e){return "development";}
    }
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(15,43,34));
        getWindow().setNavigationBarColor(Color.rgb(14,37,29));
        FrameLayout root=new FrameLayout(this);
        root.setBackgroundColor(Color.rgb(12,40,32));
        root.addView(new LikasForestView(this),new FrameLayout.LayoutParams(-1,-1));
        View scrim=new View(this);
        // Keep text contrast stable; the forest animates its own gradual daylight palette.
        scrim.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
            new int[]{Color.argb(80,4,25,20),Color.argb(105,2,22,18),
                Color.argb(197,4,25,19)}));
        root.addView(scrim,new FrameLayout.LayoutParams(-1,-1));

        LinearLayout content=new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_HORIZONTAL);
        content.setPadding(dp(24),dp(24),dp(24),dp(24));
        FrameLayout.LayoutParams cover=new FrameLayout.LayoutParams(-1,-1);
        root.addView(content,cover);

        content.addView(new Space(this),new LinearLayout.LayoutParams(1,0,2.3f));
        ImageView icon=new ImageView(this);
        icon.setImageResource(R.drawable.likas_launcher_art);
        icon.setContentDescription("Approved LIKAS pixel-art rainforest emblem");
        content.addView(icon,new LinearLayout.LayoutParams(dp(164),dp(164)));


        TextView subtitle=label("PHILIPPINE WILDLIFE SURVEY",13,
            Color.rgb(230,245,225),true);
        subtitle.setLetterSpacing(.11f);
        LinearLayout.LayoutParams subtitleLp=new LinearLayout.LayoutParams(-1,-2);
        subtitleLp.topMargin=dp(16);
        content.addView(subtitle,subtitleLp);
        content.addView(new Space(this),new LinearLayout.LayoutParams(1,0,2.4f));

        continueButton=action("Resume Survey",true);
        content.addView(continueButton,new LinearLayout.LayoutParams(-1,dp(62)));
        newSurveyButton=action("New Survey",false);
        LinearLayout.LayoutParams startLp=new LinearLayout.LayoutParams(-1,dp(60));
        startLp.topMargin=dp(12);
        content.addView(newSurveyButton,startLp);
        newSurveyButton.setOnClickListener(v->{
            Intent home=new Intent(this,WelcomeActivity.class);
            home.putExtra("show_menu",true);
            startActivity(home);
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
        });
        continueButton.setOnClickListener(v->{
            if(!hasActiveSurvey())return;
            Intent resume=new Intent(this,MainActivity.class).putExtra("resume",true);
            startActivity(resume);
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
        });

        content.addView(new Space(this),new LinearLayout.LayoutParams(1,0,.9f));
        TextView version=label("v"+currentVersion(),12,
            Color.rgb(218,235,220),false);
        version.setLetterSpacing(.06f);
        content.addView(version);
        setContentView(root);
    }
    private boolean hasActiveSurvey(){
        File file=getDatabasePath(FieldDb.NAME);
        if(!file.exists())return false;
        try(SQLiteDatabase db=SQLiteDatabase.openDatabase(file.getAbsolutePath(),null,
                SQLiteDatabase.OPEN_READONLY);
            Cursor cursor=db.rawQuery("SELECT 1 FROM surveys WHERE ended IS NULL LIMIT 1",null)){
            return cursor.moveToFirst();
        }catch(Exception e){return false;}
    }
    @Override protected void onResume(){
        super.onResume();
        boolean available=hasActiveSurvey();
        continueButton.setVisibility(available?View.VISIBLE:View.GONE);
        // Without an unfinished survey, New Survey becomes the primary action.
        GradientDrawable newBg=new GradientDrawable();
        newBg.setColor(available?Color.argb(70,16,45,35):Color.rgb(237,245,234));
        newBg.setCornerRadius(dp(16));
        newBg.setStroke(dp(1),Color.rgb(222,238,224));
        newSurveyButton.setBackground(newBg);
        newSurveyButton.setBackgroundTintList(null);
        newSurveyButton.setTextColor(available?Color.WHITE:Color.rgb(21,64,45));
        newSurveyButton.setText(available?"Home & Tools":"New Survey");
        continueButton.setEnabled(LikasUiRules.canContinue(available));
        continueButton.setContentDescription(available?
            "Continue the unfinished survey":"Continue survey unavailable. No active survey.");
    }
}
