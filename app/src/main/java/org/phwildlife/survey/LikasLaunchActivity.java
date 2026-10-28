package org.phwildlife.survey;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

/** Minimal typographic splash. Never changes research data or starts a survey. */
public final class LikasLaunchActivity extends Activity {
    private static final int FOREST=Color.rgb(11,45,35);
    private static final int CREAM=Color.rgb(245,247,235);
    private final Handler handler=new Handler(Looper.getMainLooper());
    private boolean entered=false;
    private TextView brand;
    private FrameLayout root;
    private int dp(float n){
        return Math.round(getResources().getDisplayMetrics().density*n);
    }
    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);
        getWindow().setStatusBarColor(FOREST);
        getWindow().setNavigationBarColor(FOREST);
        root=new FrameLayout(this);
        root.setBackgroundColor(FOREST);
        brand=new TextView(this);
        brand.setText("LIKAS");
        brand.setTextColor(CREAM);
        brand.setTextSize(getResources().getConfiguration().fontScale>1.4f?43:58);
        brand.setTypeface(Typeface.create("sans-serif-light",Typeface.NORMAL));
        brand.setLetterSpacing(.20f);
        brand.setGravity(Gravity.CENTER);
        brand.setContentDescription("LIKAS biodiversity fieldwork");
        FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(-1,dp(110),Gravity.CENTER);
        lp.leftMargin=dp(18);lp.rightMargin=dp(18);
        root.addView(brand,lp);
        setContentView(root);

        // Android accessibility and device settings determine whether motion runs.
        if(!ValueAnimator.areAnimatorsEnabled()){
            enterHome();
            return;
        }
        brand.setAlpha(0f);
        brand.setScaleX(.91f);brand.setScaleY(.91f);
        brand.animate().alpha(1f).scaleX(1f).scaleY(1f)
            .setDuration(460).withEndAction(()->handler.postDelayed(this::leaveSplash,280))
            .start();
        root.setOnClickListener(v->leaveSplash()); // tap-to-skip on slow devices
    }
    private void leaveSplash(){
        if(entered||isFinishing()||isDestroyed())return;
        if(!ValueAnimator.areAnimatorsEnabled()){enterHome();return;}
        brand.animate().cancel();
        brand.animate().alpha(0f).translationY(-dp(16))
            .setDuration(210).withEndAction(this::enterHome).start();
    }
    private void enterHome(){
        if(entered||isFinishing()||isDestroyed())return;
        entered=true;
        Intent home=new Intent(this,WelcomeActivity.class);
        home.putExtra("show_menu",true);
        startActivity(home);
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
        finish();
    }
    @Override protected void onDestroy(){
        handler.removeCallbacksAndMessages(null);
        if(brand!=null)brand.animate().cancel();
        super.onDestroy();
    }
}
