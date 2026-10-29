package org.phwildlife.survey;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import static org.junit.Assert.*;

/** Runs only in an empty, disposable Android emulator after backup recovery checks. */
@RunWith(AndroidJUnit4.class)
public final class Rc10RecorderInstrumentedTest {
    private static void find(View root,ArrayList<View> views){
        views.add(root);
        if(root instanceof ViewGroup){
            ViewGroup group=(ViewGroup)root;
            for(int i=0;i<group.getChildCount();i++)find(group.getChildAt(i),views);
        }
    }

    private static boolean hasText(View root,String text){
        ArrayList<View> all=new ArrayList<>();
        find(root,all);
        for(View v:all)if(v instanceof TextView&&
            ((TextView)v).getText().toString().contains(text))return true;
        return false;
    }
    private static void capture(android.app.Activity a,String file){
        View root=a.getWindow().getDecorView();
        Bitmap bitmap=Bitmap.createBitmap(root.getWidth(),root.getHeight(),
            Bitmap.Config.ARGB_8888);
        root.draw(new Canvas(bitmap));
        try(FileOutputStream out=new FileOutputStream(new File(a.getFilesDir(),file))){
            if(!bitmap.compress(Bitmap.CompressFormat.PNG,90,out))
                throw new IllegalStateException("Could not save "+file);
        }catch(Exception e){throw new IllegalStateException(e);}
        bitmap.recycle();
    }

    @Test public void liveSurveyRetainsEndAndMarkActionsAndReadableDock()throws Exception{
        Intent intent=new Intent(ApplicationProvider.getApplicationContext(),MainActivity.class);
        intent.putExtra("create_new",true);
        intent.putExtra("survey_profile","Birds");
        intent.putExtra("survey_method","opportunistic");
        // Explicitly record unavailable location on this disposable emulator, never invent GPS.
        intent.putExtra("start_without_gps",true);
        intent.putExtra("observer_name","LIKAS-RC10-EMULATOR-QA");
        try(ActivityScenario<MainActivity> test=ActivityScenario.launch(intent)){
            test.onActivity(activity->{
                View root=activity.getWindow().getDecorView();
                ArrayList<View> views=new ArrayList<>();
                find(root,views);
                Button end=null,save=null,mark=null,review=null;
                TextView details=null;
                for(View view:views){
                    if(view instanceof Button){
                        Button b=(Button)view;
                        String name=b.getText().toString();
                        if("End Survey".equals(name))end=b;
                        if("Save Observation".equals(name))save=b;
                        if("Mark detection".equals(name))mark=b;
                        if("Review Records".equals(name))review=b;
                    }
                    if(view instanceof TextView){
                        TextView v=(TextView)view;
                        if(v.getText().toString().contains("More observation details"))
                            details=v;
                    }
                }
                assertNotNull("End Survey action exists",end);
                assertNotNull("Save Observation action exists",save);
                assertNotNull("Mark detection action exists",mark);
                assertNotNull("Review Records action exists",review);
                assertNotNull("Optional evidence controls exist",details);
                assertTrue("End Survey must remain visible above the keyboard",end.isShown());
                assertTrue("End Survey must remain enabled during an active survey",end.isEnabled());
                assertTrue("Mark detection describes GPS provenance",
                    mark.getContentDescription().toString().contains("fresh observer GPS"));
                assertTrue("Optional disclosure is accessible",
                    details.getContentDescription().toString().contains("Show optional"));
                assertTrue("Recorder has measured width",root.getWidth()>0);
                assertTrue("Recorder has measured height",root.getHeight()>0);
                Bitmap shot=Bitmap.createBitmap(root.getWidth(),root.getHeight(),
                    Bitmap.Config.ARGB_8888);
                root.draw(new Canvas(shot));
                File out=new File(activity.getFilesDir(),"rc10_recorder_screen.png");
                try(FileOutputStream stream=new FileOutputStream(out)){
                    assertTrue(shot.compress(Bitmap.CompressFormat.PNG,90,stream));
                }catch(Exception error){throw new IllegalStateException(error);}
                shot.recycle();
            });
        }
    }
    @Test public void largeTextKeepsEndSurveyAndGpsAccessible()throws Exception{
        android.app.Instrumentation instrumentation=
            androidx.test.platform.app.InstrumentationRegistry.getInstrumentation();
        try{
            try(android.os.ParcelFileDescriptor fd=instrumentation.getUiAutomation()
                    .executeShellCommand("settings put system font_scale 2.0")){}
            android.os.SystemClock.sleep(1200);
            Intent intent=new Intent(ApplicationProvider.getApplicationContext(),
                MainActivity.class);
            intent.putExtra("create_new",true);
            intent.putExtra("survey_profile","Birds");
            intent.putExtra("survey_method","opportunistic");
            intent.putExtra("start_without_gps",true);
            intent.putExtra("observer_name","LIKAS-RC21-ACCESSIBILITY-QA");
            try(ActivityScenario<MainActivity> scenario=ActivityScenario.launch(intent)){
                scenario.onActivity(activity->{
                    View root=activity.getWindow().getDecorView();
                    ArrayList<View> all=new ArrayList<>();
                    find(root,all);
                    Button end=null;
                    TextView gps=null;
                    for(View v:all){
                        if(v instanceof Button &&
                                "End Survey".equals(((Button)v).getText().toString()))
                            end=(Button)v;
                        if(v instanceof TextView &&
                                ((TextView)v).getText().toString().contains("GPS"))
                            if(v.getContentDescription()!=null &&
                                v.getContentDescription().toString()
                                    .contains("Open GPS details"))gps=(TextView)v;
                    }
                    assertNotNull("200 percent text: persistent End Survey",end);
                    assertTrue("End Survey action visible",end.isShown());
                    assertTrue("End Survey action usable",end.isEnabled());
                    assertTrue("End Survey minimum touch height",end.getHeight()>=
                        Math.round(48*activity.getResources().getDisplayMetrics().density));
                    android.graphics.Rect visible=new android.graphics.Rect();
                    assertTrue("End Survey has a screen-visible rectangle",
                        end.getGlobalVisibleRect(visible));
                    assertTrue("Entire End Survey action fits inside display at 200% font",
                        visible.height()>=end.getHeight()-2);
                    assertTrue(end.getContentDescription().toString().contains("Not Counted"));
                    assertNotNull("GPS has a spoken current state",gps);
                    String displayedGps=gps.getText().toString();
                    String accessibleGps=gps.getContentDescription().toString();
                    // Visual "· details" is an action cue, not part of the fix state.
                    String measuredState=displayedGps.replace(" · details","").trim();
                    assertTrue("GPS spoken status agrees with displayed fix state",
                        accessibleGps.startsWith(measuredState));
                    assertTrue("GPS spoken action remains available",
                        accessibleGps.contains("Open GPS details"));
                    Bitmap image=Bitmap.createBitmap(root.getWidth(),root.getHeight(),
                        Bitmap.Config.ARGB_8888);
                    root.draw(new Canvas(image));
                    try(FileOutputStream out=new FileOutputStream(
                        new File(activity.getFilesDir(),"rc21_recorder_large_text.png"))){
                        assertTrue(image.compress(Bitmap.CompressFormat.PNG,90,out));
                    }catch(Exception error){throw new IllegalStateException(error);}
                    image.recycle();
                });
            }
            Intent compareIntent=new Intent(ApplicationProvider.getApplicationContext(),
                Rc8TraitComparisonActivity.class);
            compareIntent.putExtra("scientific_name","Pithecophaga jefferyi");
            try(ActivityScenario<Rc8TraitComparisonActivity> compare=
                    ActivityScenario.launch(compareIntent)){
                compare.onActivity(a->{
                    View root=a.getWindow().getDecorView();
                    assertTrue("Current evidence comparison reachable at 200% font",
                        hasText(root,"latest cited 96-species accounts"));
                    assertTrue("Open species from side-by-side comparison",
                        hasText(root,"Open current species account"));
                    capture(a,"rc24_compare_200percent.png");
                });
            }
            try(ActivityScenario<Rc24ConservationDashboardActivity> dashboard=
                    ActivityScenario.launch(Rc24ConservationDashboardActivity.class)){
                dashboard.onActivity(a->{
                    View root=a.getWindow().getDecorView();
                    assertTrue("Dated assessment boundaries explained",
                        hasText(root,"Least Concern"));
                    assertTrue("Conservation study denominator rendered",
                        hasText(root,"of 96 species"));
                    capture(a,"rc24_conservation_200percent.png");
                });
            }
        }finally{
            try(android.os.ParcelFileDescriptor fd=instrumentation.getUiAutomation()
                    .executeShellCommand("settings put system font_scale 1.0")){}
            android.os.SystemClock.sleep(800);
        }
    }

}
