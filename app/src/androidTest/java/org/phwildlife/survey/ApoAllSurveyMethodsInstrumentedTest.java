package org.phwildlife.survey;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.File;
import java.io.FileOutputStream;
import static org.junit.Assert.*;

/** All 17 profile/method entries, synthetic data on a disposable successor emulator only. */
@RunWith(AndroidJUnit4.class)
public final class ApoAllSurveyMethodsInstrumentedTest {
    private static final String OBSERVER="APO-METHOD-SWEEP-QA";
    private static View exact(View root,String text){
        if(root instanceof TextView&&root.isShown()&&
            text.contentEquals(((TextView)root).getText()))return root;
        if(root instanceof ViewGroup){
            ViewGroup group=(ViewGroup)root;
            for(int i=0;i<group.getChildCount();i++){
                View hit=exact(group.getChildAt(i),text);
                if(hit!=null)return hit;
            }
        }
        return null;
    }
    private static void checkEmulator(Context ctx){
        assertEquals("org.phwildlife.survey.apo",ctx.getPackageName());
        assertTrue("Disposable emulator only",
            Build.HARDWARE.contains("ranchu")||Build.HARDWARE.contains("goldfish"));
    }
    private static void finishFixture(Context ctx){
        checkEmulator(ctx);
        try(FieldDb db=new FieldDb(ctx)){
            db.getWritableDatabase().execSQL(
                "UPDATE surveys SET ended=datetime('now'),complete=0,outcome='incomplete' "+
                "WHERE observer_name=? AND ended IS NULL",new Object[]{OBSERVER});
        }
    }
    private static void assertFits(Activity a,View view,String label){
        assertNotNull(label,view);
        assertTrue(label+" has a usable touch height",
            view.getHeight()>=ApoVisualSystem.dp(a,48));
        assertTrue(label+" has a usable touch width",
            view.getWidth()>=ApoVisualSystem.dp(a,48));
        assertTrue(label+" is operable",view.isClickable());
        Rect bounds=new Rect();
        assertTrue(label+" is visible",view.getGlobalVisibleRect(bounds));
        assertEquals(label+" is not vertically clipped",view.getHeight(),bounds.height());
        if(view instanceof TextView){
            TextView t=(TextView)view;
            android.text.Layout layout=t.getLayout();
            assertNotNull(label+" is laid out",layout);
            for(int i=0;i<layout.getLineCount();i++)
                assertEquals(label+" must not ellipsize",0,layout.getEllipsisCount(i));
            assertEquals(label+" full label",
                t.length(),layout.getLineEnd(layout.getLineCount()-1));
            assertTrue(label+" text fits vertically",
                layout.getHeight()<=t.getHeight()-t.getCompoundPaddingTop()-t.getCompoundPaddingBottom());
        }
    }
    private static void shot(Activity a,String label){
        View root=a.getWindow().getDecorView();
        assertTrue(root.getWidth()>0&&root.getHeight()>0);
        Bitmap bitmap=Bitmap.createBitmap(root.getWidth(),root.getHeight(),Bitmap.Config.ARGB_8888);
        try{
            root.draw(new Canvas(bitmap));
            File dir=new File(a.getExternalFilesDir(Environment.DIRECTORY_PICTURES),"APO_SHELL_REVIEW");
            assertTrue(dir.isDirectory()||dir.mkdirs());
            String preset=InstrumentationRegistry.getArguments().getString("preset","unknown");
            try(FileOutputStream out=new FileOutputStream(new File(dir,preset+"-"+label+".png"))){
                assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG,100,out));
            }catch(Exception ex){throw new AssertionError(ex);}
        }finally{bitmap.recycle();}
    }
    private static void verify(String profile){
        Context ctx=ApplicationProvider.getApplicationContext();
        checkEmulator(ctx);
        for(SurveyMethods.Method method:SurveyMethods.forProfile(profile)){
            finishFixture(ctx);
            Intent intent=new Intent(ctx,MainActivity.class)
                .putExtra("create_new",true).putExtra("start_without_gps",true)
                .putExtra("survey_profile",profile).putExtra("survey_method",method.code)
                .putExtra("observer_name",OBSERVER).putExtra("project_id","default")
                .putExtra("transect_bearing","90").putExtra("basic_log_only",method.basicLog);
            try(ActivityScenario<MainActivity> scenario=ActivityScenario.launch(intent)){
                InstrumentationRegistry.getInstrumentation().waitForIdleSync();
                try(FieldDb db=new FieldDb(ctx);Cursor c=db.getReadableDatabase().rawQuery(
                    "SELECT survey_profile,method FROM surveys WHERE observer_name=? AND ended IS NULL",
                    new String[]{OBSERVER})){
                    assertEquals("Exactly one active synthetic method",1,c.getCount());
                    assertTrue(c.moveToFirst());
                    assertEquals(profile,c.getString(0));assertEquals(method.code,c.getString(1));
                }
                String label=profile.toLowerCase(java.util.Locale.ROOT)+"-"+method.code;
                scenario.onActivity(a->{
                    View root=a.getWindow().getDecorView();
                    assertFits(a,exact(root,"Save Observation"),label+" Save");
                    View end=exact(root,"End Survey");
                    assertFits(a,end,label+" End");
                    assertTrue("End remains enabled",end.isEnabled());
                    shot(a,label+"-recorder");
                    if(exact(root,method.label)==null){
                        View disclosure=exact(root,"Field tools");
                        assertNotNull("Compact method disclosure is accessible",disclosure);
                        disclosure.performClick();
                    }
                    assertNotNull("Actual selected method is discoverable",exact(root,method.label));
                    String action=RecorderMethodUiPolicy.actionLabel(profile,method.code);
                    if(!action.isEmpty()){
                        View control=exact(root,action);
                        assertNotNull("Method-specific action exists",control);
                        control.requestRectangleOnScreen(new Rect(0,0,control.getWidth(),control.getHeight()),true);
                    }
                });
                InstrumentationRegistry.getInstrumentation().waitForIdleSync();
                scenario.onActivity(a->{
                    String action=RecorderMethodUiPolicy.actionLabel(profile,method.code);
                    if(!action.isEmpty()){
                        assertFits(a,exact(a.getWindow().getDecorView(),action),label+" method tools");
                        shot(a,label+"-method-tools");
                    }
                });
            }finally{finishFixture(ctx);}
        }
    }
    @Test public void allBirdMethods(){verify("Birds");}
    @Test public void allHerpetofaunaMethods(){verify("Herpetofauna");}
    @Test public void allMammalMethods(){verify("Mammals");}
}
