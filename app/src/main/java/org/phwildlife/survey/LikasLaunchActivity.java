package org.phwildlife.survey;

import android.animation.ValueAnimator;
import android.content.pm.ApplicationInfo;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.Typeface;
import android.os.Bundle;
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
    private static final int FOREST=Color.rgb(11,75,57);
    private static final int DARK_FOREST=Color.rgb(2,25,24);
    private static final int IVORY=Color.rgb(245,240,226);
    private static final int SAGE=Color.rgb(176,199,184);
    private boolean entered=false;
    private boolean loadingReferences=false;
    private boolean animationDone=false;
    private boolean referenceErrorShown=false;
    private boolean exitStarted=false;
    private String referenceFailure=null;
    private LinearLayout identity;
    private ImageView mark;
    private TextView status;
    private TextView wordmark;
    private FrameLayout root;
    private View underline;
    private ValueAnimator letterReveal;
    private boolean qaHoldSplash;

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
        // A held preview is available only to instrumentation inside a debug build.
        qaHoldSplash=(getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE)!=0 &&
            getIntent().getBooleanExtra("qa_hold_splash",false);
        getWindow().setStatusBarColor(FOREST);
        getWindow().setNavigationBarColor(DARK_FOREST);

        root=new FrameLayout(this);
        GradientDrawable backdrop=new GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            new int[]{FOREST,DARK_FOREST});
        root.setBackground(backdrop);

        identity=new LinearLayout(this);
        identity.setGravity(Gravity.CENTER);
        identity.setOrientation(LinearLayout.VERTICAL);
        identity.setPadding(dp(20),dp(8),dp(20),dp(8));
        FrameLayout.LayoutParams identityParams=new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.CENTER);
        identityParams.leftMargin=dp(12);
        identityParams.rightMargin=dp(12);
        // Gravity.CENTER is the optical baseline: do not bias the splash downward.
        root.addView(identity,identityParams);

        mark=new ImageView(this);
        // Borderless transparent ribbon derivative from the unchanged locked master.
        // The dark rounded-square launcher tile does not belong on the splash.
        mark.setImageResource(R.drawable.apo_splash_mark);
        mark.setScaleType(ImageView.ScaleType.FIT_CENTER);
        int availableDp=Math.round(getResources().getDisplayMetrics().widthPixels /
            getResources().getDisplayMetrics().density);
        int markWidthDp=Math.min(276,Math.max(180,availableDp-88));
        LinearLayout.LayoutParams markParams=new LinearLayout.LayoutParams(
            dp(markWidthDp),dp(markWidthDp*0.55f));
        identity.addView(mark,markParams);

        wordmark=new TextView(this);
        wordmark.setText("APO");
        wordmark.setTextColor(IVORY);
        wordmark.setTextSize(getResources().getConfiguration().fontScale>1.5f ? 34f : 43f);
        wordmark.setTypeface(Typeface.create("sans-serif-light",Typeface.NORMAL));
        wordmark.setLetterSpacing(.12f);
        wordmark.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams wordParams=new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        wordParams.topMargin=dp(16);
        identity.addView(wordmark,wordParams);

        // One quiet pond-line accent expands from the center as the mark settles.
        // It is decorative and deliberately hidden from accessibility services.
        underline=new View(this);
        underline.setBackgroundColor(SAGE);
        underline.setAlpha(0.88f);
        underline.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        LinearLayout.LayoutParams underlineParams=new LinearLayout.LayoutParams(
            dp(72),dp(2));
        underlineParams.topMargin=dp(14);
        identity.addView(underline,underlineParams);

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

        mark.setContentDescription("APO mountain emblem");
        // Keep the mountain and wordmark individually available to TalkBack.
        // An ancestor description would mask their separate accessibility names.
        wordmark.setContentDescription("Apo");
        setContentView(root);
        startReferenceBootstrap();

        if(reducedMotion()) {
            // Keep the fully composed identity static when motion is disabled.
            animationDone=true;
            maybeEnterHome();
        } else {
            mark.setAlpha(0f);
            mark.setTranslationY(dp(6));
            mark.setScaleX(0.93f);
            mark.setScaleY(0.93f);
            wordmark.setAlpha(0f);
            wordmark.setTranslationY(dp(4));
            wordmark.setLetterSpacing(.24f);
            underline.setScaleX(0f);
            underline.setAlpha(0f);

            // A single, restrained scale-settle complements the existing fade.
            mark.animate().alpha(1f).translationY(0f).scaleX(1f).scaleY(1f)
                .setInterpolator(new OvershootInterpolator(0.48f))
                .setDuration(540).start();
            wordmark.animate().alpha(1f).translationY(0f)
                .setInterpolator(new DecelerateInterpolator(1.4f))
                .setStartDelay(90).setDuration(370).start();
            letterReveal=ValueAnimator.ofFloat(.24f,.12f);
            letterReveal.setStartDelay(90);
            letterReveal.setDuration(370);
            letterReveal.setInterpolator(new DecelerateInterpolator(1.5f));
            letterReveal.addUpdateListener(a->
                wordmark.setLetterSpacing((Float)a.getAnimatedValue()));
            letterReveal.start();
            underline.animate().scaleX(1f).alpha(.88f)
                .setStartDelay(150).setDuration(460)
                .setInterpolator(new DecelerateInterpolator(1.5f))
                .withEndAction(()->{
                    animationDone=true;
                    maybeEnterHome();
                }).start();
            // Tap skips every effect, including letter spacing and line expansion.
            root.setOnClickListener(v->{
                mark.animate().cancel();
                wordmark.animate().cancel();
                underline.animate().cancel();
                if(letterReveal!=null)letterReveal.cancel();
                mark.setAlpha(1f);
                mark.setTranslationY(0f);
                mark.setScaleX(1f);
                mark.setScaleY(1f);
                wordmark.setAlpha(1f);
                wordmark.setTranslationY(0f);
                wordmark.setLetterSpacing(.12f);
                underline.setAlpha(.88f);
                underline.setScaleX(1f);
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
        if(entered||exitStarted||qaHoldSplash||isFinishing()||isDestroyed()||!animationDone)
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
            // Keep the gradient visible while the identity gently clears;
            // Android then cross-fades the outgoing splash to Home.
            identity.animate().alpha(0f).translationY(-dp(6))
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(190).withEndAction(this::enterHome).start();
        }
    }

    private void enterHome() {
        if(entered||isFinishing()||isDestroyed()) return;
        entered=true;
        Intent home=new Intent(this,WelcomeActivity.class);
        home.putExtra("show_menu",true);
        startActivity(home);
        if(reducedMotion()) {
            overridePendingTransition(0,0);
        } else {
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
        }
        finish();
    }

    @Override protected void onDestroy() {
        if(identity!=null) identity.animate().cancel();
        if(mark!=null) mark.animate().cancel();
        if(wordmark!=null) wordmark.animate().cancel();
        if(underline!=null) underline.animate().cancel();
        if(letterReveal!=null) letterReveal.cancel();
        super.onDestroy();
    }
}
