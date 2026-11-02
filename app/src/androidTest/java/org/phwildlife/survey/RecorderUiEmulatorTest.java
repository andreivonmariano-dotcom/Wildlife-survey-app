package org.phwildlife.survey;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

/** Disposable emulator-only live recorder smoke test, no production survey data. */
@RunWith(AndroidJUnit4.class)
public final class RecorderUiEmulatorTest {
    private View findText(View view,String text){
        if(view instanceof TextView &&
                text.equals(((TextView)view).getText().toString()))return view;
        if(view instanceof ViewGroup){
            ViewGroup group=(ViewGroup)view;
            for(int i=0;i<group.getChildCount();i++){
                View result=findText(group.getChildAt(i),text);
                if(result!=null)return result;
            }
        }
        return null;
    }
    @Test public void recorderHasPersistentSaveReviewAndEndSurvey() throws Exception {
        Intent launch=new Intent(ApplicationProvider.getApplicationContext(),MainActivity.class);
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        launch.putExtra("create_new",true);
        launch.putExtra("start_without_gps",true);
        launch.putExtra("survey_profile","Birds");
        launch.putExtra("survey_method","opportunistic");
        launch.putExtra("project_id","default");
        try(ActivityScenario<MainActivity> scenario=ActivityScenario.launch(launch)){
            scenario.onActivity(activity->{
                View root=activity.getWindow().getDecorView();
                View save=findText(root,"Save Observation");
                View review=findText(root,"Review Records");
                View end=findText(root,"End Survey");
                assertNotNull("Save control missing",save);
                assertNotNull("Review control missing",review);
                assertNotNull("End Survey must remain accessible",end);
                assertTrue("End Survey must stay visible",end.isShown());
                assertTrue("Save must remain visible",save.isShown());
                assertTrue("End Survey should be enabled in a running survey",end.isEnabled());
                assertNotNull("Session section missing",findText(root,"Sampling session"));
                assertNotNull("Detection section missing",findText(root,"Detection tools"));
                int[] pos=new int[2];
                end.getLocationOnScreen(pos);
                int height=activity.getResources().getDisplayMetrics().heightPixels;
                assertTrue("End Survey should fit within viewport",
                    pos[1]>=0&&pos[1]+end.getHeight()<=height);
                try{
                    Bitmap screenshot=InstrumentationRegistry.getInstrumentation()
                        .getUiAutomation().takeScreenshot();
                    assertNotNull("Emulator screenshot unavailable",screenshot);
                    try(FileOutputStream out=new FileOutputStream(
                            new File(activity.getFilesDir(),"apo_recorder_emulator.png"))){
                        assertTrue(screenshot.compress(Bitmap.CompressFormat.PNG,100,out));
                    }
                    screenshot.recycle();
                }catch(Exception exception){
                    throw new AssertionError("Cannot save emulator screen evidence",exception);
                }
            });
        }
    }
}
