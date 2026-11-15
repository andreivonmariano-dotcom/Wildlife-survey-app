package org.phwildlife.survey;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

/** One transition policy for native screens, including the reduced-motion preference. */
public final class ApoApplication extends Application {
    private void applyMotion(Activity activity){
        boolean reduced=activity.getSharedPreferences("likas_ui",Context.MODE_PRIVATE)
            .getBoolean("reduced_motion",false) || !ValueAnimator.areAnimatorsEnabled();
        activity.getWindow().setWindowAnimations(reduced?0:R.style.ApoWindowAnimations);
    }

    @Override public void onCreate(){
        super.onCreate();
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks(){
            @Override public void onActivityCreated(Activity a,Bundle state){applyMotion(a);}
            @Override public void onActivityStarted(Activity a){}
            @Override public void onActivityResumed(Activity a){applyMotion(a);}
            @Override public void onActivityPaused(Activity a){}
            @Override public void onActivityStopped(Activity a){}
            @Override public void onActivitySaveInstanceState(Activity a,Bundle b){}
            @Override public void onActivityDestroyed(Activity a){}
        });
    }
}
