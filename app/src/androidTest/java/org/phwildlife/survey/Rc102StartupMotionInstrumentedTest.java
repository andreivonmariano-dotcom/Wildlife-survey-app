package org.phwildlife.survey;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import java.io.File;
import java.io.FileOutputStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** RC102 actual centered startup view on small screens and large accessibility text. */
@RunWith(AndroidJUnit4.class)
public final class Rc102StartupMotionInstrumentedTest {
    private static int dp(LikasLaunchActivity activity, int pixels) {
        return Math.round(activity.getResources().getDisplayMetrics().density * pixels);
    }
    private static String preset() {
        return InstrumentationRegistry.getArguments().getString("preset","p360f100");
    }
    private static void screenshot(LikasLaunchActivity activity) throws Exception {
        View decor=activity.getWindow().getDecorView();
        Bitmap bitmap=Bitmap.createBitmap(decor.getWidth(),decor.getHeight(),Bitmap.Config.ARGB_8888);
        try {
            decor.draw(new Canvas(bitmap));
            File folder=new File(activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "RC102");
            assertTrue(folder.isDirectory() || folder.mkdirs());
            File destination=new File(folder,preset()+".png");
            try(FileOutputStream out=new FileOutputStream(destination)){
                assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG,100,out));
            }
            assertTrue(destination.length()>1000);
        } finally { bitmap.recycle(); }
    }

    @Test public void centeredIdentityIsIndependentOfBottomStatusAndFontScale() throws Exception {
        // A debug-only hold keeps the splash rendered during real view-geometry checks.
        ApplicationProvider.getApplicationContext().getSharedPreferences(
            "likas_ui",android.content.Context.MODE_PRIVATE)
            .edit().putBoolean("reduced_motion",true).commit();
        Intent intent=new Intent(ApplicationProvider.getApplicationContext(),
            LikasLaunchActivity.class).putExtra("qa_hold_splash",true);
        try(ActivityScenario<LikasLaunchActivity> scenario=ActivityScenario.launch(intent)){
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            final Exception[] error={null};
            scenario.onActivity(a->{
                try {
                    ViewGroup content=a.findViewById(android.R.id.content);
                    assertNotNull(content);
                    assertTrue(content.getChildAt(0) instanceof FrameLayout);
                    FrameLayout root=(FrameLayout)content.getChildAt(0);
                    assertTrue(root.getChildAt(0) instanceof LinearLayout);
                    LinearLayout identity=(LinearLayout)root.getChildAt(0);
                    FrameLayout.LayoutParams lp=(FrameLayout.LayoutParams)identity.getLayoutParams();
                    assertEquals(Gravity.CENTER,lp.gravity);
                    assertEquals("No artificial downward bias",0,lp.topMargin);
                    assertEquals(0,lp.bottomMargin);
                    assertTrue(root.getChildAt(1) instanceof TextView);
                    int[] rootPos=new int[2],identityPos=new int[2];
                    root.getLocationOnScreen(rootPos);
                    identity.getLocationOnScreen(identityPos);
                    float rootMid=rootPos[1]+root.getHeight()/2f;
                    float identityMid=identityPos[1]+identity.getHeight()/2f;
                    assertTrue("Logo and wordmark must occupy the actual vertical center: "+
                        rootMid+" versus "+identityMid,
                        Math.abs(rootMid-identityMid)<=dp(a,2));
                    assertEquals(3,identity.getChildCount());
                    assertTrue(identity.getChildAt(0) instanceof ImageView);
                    ImageView mark=(ImageView)identity.getChildAt(0);
                    assertNotNull(mark.getDrawable());
                    assertTrue(identity.getChildAt(1) instanceof TextView);
                    assertEquals("APO",((TextView)identity.getChildAt(1)).getText().toString());
                    View line=identity.getChildAt(2);
                    assertEquals(View.IMPORTANT_FOR_ACCESSIBILITY_NO,
                        line.getImportantForAccessibility());
                    assertEquals(1f,line.getScaleX(),0.001f);
                    assertEquals(1f,mark.getScaleX(),0.001f);
                    assertEquals(1f,mark.getScaleY(),0.001f);
                    screenshot(a);
                } catch(Exception failure){error[0]=failure;}
            });
            if(error[0]!=null)throw error[0];
        }finally {
            ApplicationProvider.getApplicationContext().getSharedPreferences(
                "likas_ui",android.content.Context.MODE_PRIVATE)
                .edit().remove("reduced_motion").commit();
        }
    }
}
