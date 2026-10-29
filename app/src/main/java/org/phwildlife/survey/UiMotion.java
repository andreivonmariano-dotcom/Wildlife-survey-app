package org.phwildlife.survey;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

/** Short, optional transitions. All states remain usable without animation. */
public final class UiMotion {
    private UiMotion(){}

    private static boolean animate(Activity a) {
        return ValueAnimator.areAnimatorsEnabled() &&
            a.getResources().getConfiguration().fontScale<1.60f;
    }
    private static float dp(Activity a,int n){
        return n*a.getResources().getDisplayMetrics().density;
    }

    /** Fade the page into view after layout. Never change view dimensions. */
    public static void enter(Activity a,View root){
        if(root==null)return;
        if(!animate(a)){root.setAlpha(1f);root.setTranslationY(0f);return;}
        root.animate().cancel();
        root.setAlpha(0f);
        root.setTranslationY(dp(a,4));
        root.animate().alpha(1f).translationY(0f).setDuration(180)
            .setInterpolator(new DecelerateInterpolator()).start();
    }

    /** Reveal only secondary content. Do not animate or delay main field actions. */
    public static void reveal(Activity a,View content){
        if(content==null)return;
        content.animate().cancel();
        content.setVisibility(View.VISIBLE);
        if(!animate(a)){content.setAlpha(1f);content.setTranslationY(0f);return;}
        content.setAlpha(0f);content.setTranslationY(dp(a,6));
        content.animate().alpha(1f).translationY(0f).setDuration(160)
            .setInterpolator(new DecelerateInterpolator()).start();
    }

    public static void hide(View content){
        if(content==null)return;
        content.animate().cancel();
        content.setAlpha(1f);
        content.setTranslationY(0f);
        content.setVisibility(View.GONE);
    }
}
