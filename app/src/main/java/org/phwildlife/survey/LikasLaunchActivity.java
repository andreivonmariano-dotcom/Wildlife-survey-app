package org.phwildlife.survey;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * APO identity splash. Startup must never manufacture survey data or imply
 * that unavailable references are installed. This screen is intentionally
 * not buildable without the exact approved APO artwork and derivatives.
 */
public final class LikasLaunchActivity extends Activity {
    private static final int FOREST=Color.rgb(8,32,26);
    private static final int IVORY=Color.rgb(245,240,226);
    private static final int SAGE=Color.rgb(176,199,184);
    private final Handler handler=new Handler(Looper.getMainLooper());
    private boolean entered=false;
    private boolean loadingReferences=false;
    private boolean animationDone=false;
    private boolean referenceErrorShown=false;
    private boolean exitStarted=false;
    private String referenceFailure=null;
    private LinearLayout identity;
    private ImageView mark;
    private TextView wordmark;
    private TextView status;
    private FrameLayout root;

    private int dp(float n) {
        return Math.round(getResources().getDisplayMetrics().density*n);
    }

    private boolean reducedMotion() {
        return !ValueAnimator.areAnimatorsEnabled() ||
            getSharedPreferences("likas_ui",MODE_PRIVATE)
                .getBoolean("reduced_motion",false);
    }

    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        getWindow().setStatusBarColor(FOREST);
        getWindow().setNavigationBarColor(FOREST);

        root=new FrameLayout(this);
        GradientDrawable backdrop=new GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            new int[]{Color.rgb(10,38,29),Color.rgb(5,25,22)});
        root.setBackground(backdrop);

        identity=new LinearLayout(this);
        identity.setGravity(Gravity.CENTER);
        identity.setOrientation(LinearLayout.VERTICAL);
        identity.setPadding(dp(20),dp(8),dp(20),dp(8));
        identity.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        FrameLayout.LayoutParams identityParams=new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.CENTER);
        identityParams.leftMargin=dp(12);
        identityParams.rightMargin=dp(12);
        identityParams.bottomMargin=dp(38);
        root.addView(identity,identityParams);

        mark=new ImageView(this);
        // The drawable must be a verified, traceable derivative of the exact
        // hard-locked owner PNG; do NOT fall back to the retired frog.
        mark.setImageResource(R.drawable.apo_splash_mark);
        mark.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams markParams=new LinearLayout.LayoutParams(
            dp(128),dp(128));
        markParams.bottomMargin=dp(16);
        identity.addView(mark,markParams);

        wordmark=new TextView(this);
        wordmark.setText("APO");
        wordmark.setTextColor(IVORY);
        wordmark.setGravity(Gravity.CENTER);
        wordmark.setTypeface(Typeface.create("sans-serif-light",Typeface.NORMAL));
        wordmark.setLetterSpacing(.19f);
        wordmark.setTextSize(
            getResources().getConfiguration().fontScale>1.5f ? 32f : 40f);
        identity.addView(wordmark,new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT));

        status=new TextView(this);
        status.setText("Preparing offline references");
        status.setTextSize(14);
        status.setTextColor(SAGE);
        status.setGravity(Gravity.CENTER);
        status.setVisibility(View.GONE);
        status.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        FrameLayout.LayoutParams statusParams=new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL);
        statusParams.leftMargin=dp(24);
        statusParams.rightMargin=dp(24);
        statusParams.bottomMargin=dp(32);
        root.addView(status,statusParams);

        root.setContentDescription("Apo biodiversity tools");
        setContentView(root);
        startReferenceBootstrap();

        if(reducedMotion()) {
            animationDone=true;
            maybeEnterHome();
        } else {
            mark.setAlpha(0f);
            mark.setTranslationY(dp(9));
            wordmark.setAlpha(0f);
            wordmark.setTranslationY(dp(5));
            mark.animate()
                .alpha(1f).translationY(0f).setDuration(280).start();
            wordmark.animate()
                .alpha(1f).translationY(0f).setStartDelay(90)
                .setDuration(220)
                .withEndAction(()->{
                    animationDone=true;
                    maybeEnterHome();
                }).start();
            root.setOnClickListener(v->{
                mark.animate().cancel();
                wordmark.animate().cancel();
                mark.setAlpha(1f);
                wordmark.setAlpha(1f);
                mark.setTranslationY(0f);
                wordmark.setTranslationY(0f);
                animationDone=true;
                maybeEnterHome();
            });
        }
    }

    private void startReferenceBootstrap() {
        if(!BundledPrivateReferences.packaged(this)) return;
        loadingReferences=true;
        new Thread(()->{
            String failure=null;
            try {
                BundledPrivateReferences.ensureInstalled(getApplicationContext());
            } catch(Exception error) {
                failure=error.getMessage()==null
                    ? error.getClass().getSimpleName() : error.getMessage();
            }
            final String result=failure;
            runOnUiThread(()->{
                if(isFinishing()||isDestroyed()) return;
                loadingReferences=false;
                referenceFailure=result;
                status.setVisibility(View.GONE);
                maybeEnterHome();
            });
        },"APO-Reference-Bootstrap").start();
    }

    private void maybeEnterHome() {
        if(entered||exitStarted||isFinishing()||isDestroyed()||!animationDone)
            return;
        if(loadingReferences) {
            status.setVisibility(View.VISIBLE);
            return;
        }
        if(referenceFailure!=null&&!referenceErrorShown) {
            referenceErrorShown=true;
            new AlertDialog.Builder(this)
                .setTitle("Offline references not installed")
                .setMessage("The offline reference package could not be installed: "
                    +referenceFailure+". Existing surveys remain available. "
                    +"Continue without the new references?")
                .setCancelable(false)
                .setPositiveButton("Continue",(dialog,which)->maybeEnterHome())
                .show();
            return;
        }

        exitStarted=true;
        if(reducedMotion()) {
            enterHome();
        } else {
            identity.animate().alpha(0f).translationY(-dp(8))
                .setDuration(150).withEndAction(this::enterHome).start();
        }
    }

    private void enterHome() {
        if(entered||isFinishing()||isDestroyed()) return;
        entered=true;
        Intent home=new Intent(this,WelcomeActivity.class);
        home.putExtra("show_menu",true);
        startActivity(home);
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
        finish();
    }

    @Override protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if(identity!=null) identity.animate().cancel();
        if(mark!=null) mark.animate().cancel();
        if(wordmark!=null) wordmark.animate().cancel();
        super.onDestroy();
    }
}
