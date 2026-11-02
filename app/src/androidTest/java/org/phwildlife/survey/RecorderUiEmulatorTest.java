package org.phwildlife.survey;

import android.app.Activity;
import android.content.Intent;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
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
import java.io.OutputStream;

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
    private View findContainingText(View view,String fragment){
        if(view instanceof TextView &&
            ((TextView)view).getText().toString().contains(fragment))return view;
        if(view instanceof ViewGroup){
            ViewGroup group=(ViewGroup)view;
            for(int i=0;i<group.getChildCount();i++){
                View result=findContainingText(group.getChildAt(i),fragment);
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
                boolean ultraLarge=activity.getResources().getConfiguration().fontScale>=1.9f;
                View review=findText(root,ultraLarge?"Review":"Review Records");
                View end=findText(root,"End Survey");
                assertNotNull("Save control missing",save);
                assertNotNull("Review control missing",review);
                assertNotNull("End Survey must remain accessible",end);
                assertTrue("End Survey must stay visible",end.isShown());
                assertTrue("Save must remain visible",save.isShown());
                assertTrue("End Survey should be enabled in a running survey",end.isEnabled());
                View samplingTools=findText(root,"Sampling session");
                View detectionTools=findText(root,"Detection tools");
                assertNotNull("Session section missing",samplingTools);
                assertNotNull("Detection section missing",detectionTools);
                if(activity.getResources().getConfiguration().fontScale>=1.60f){
                    View compactClock=findContainingText(root,"More details");
                    View compactGps=findContainingText(root,"Start GPS unverified");
                    assertNotNull("Compact timing control missing",compactClock);
                    assertNotNull("Explicit survey-start GPS warning missing",compactGps);
                    assertTrue("Timing should be visible",compactClock.isShown());
                    assertTrue("GPS warning should be visible",compactGps.isShown());
                    View hiddenClock=findContainingText(root,"Session 00:");
                    assertNotNull("Total session clock missing",hiddenClock);
                    assertFalse("Detail should start collapsed",hiddenClock.isShown());
                    compactClock.performClick();
                    assertTrue("Total clock should expand",hiddenClock.isShown());
                    compactClock.performClick();
                    assertFalse("Detail should collapse",hiddenClock.isShown());
                    View disclosure=findText(root,ultraLarge?
                        "Sampling & detection tools":"Show sampling & detection tools");
                    assertNotNull("Compact survey-tool disclosure missing",disclosure);
                    assertTrue("Survey tools disclosure should be visible",disclosure.isShown());
                    assertFalse("Nonessential survey tools must begin collapsed",samplingTools.isShown());
                    disclosure.performClick();
                    assertTrue("Sampling tools should expand on demand",samplingTools.isShown());
                    disclosure.performClick();
                    assertFalse("Sampling tools should collapse again",samplingTools.isShown());
                }
                int[] pos=new int[2];
                end.getLocationOnScreen(pos);
                int height=activity.getResources().getDisplayMetrics().heightPixels;
                assertTrue("End Survey should fit within viewport",
                    pos[1]>=0&&pos[1]+end.getHeight()<=height);
                try{
                    // Capture only APO's actual View tree. System launcher ANR
                    // overlays on a headless emulator must not contaminate UI QA.
                    assertTrue("Recorder view was not laid out",
                        root.getWidth()>0&&root.getHeight()>0);
                    Bitmap screenshot=Bitmap.createBitmap(
                        root.getWidth(),root.getHeight(),Bitmap.Config.ARGB_8888);
                    root.draw(new Canvas(screenshot));
                    ContentValues metadata=new ContentValues();
                    String captureName=activity.getResources().getConfiguration().fontScale>=1.9f?
                        "apo_recorder_200pct_320dp.png":"apo_recorder_160pct.png";
                    metadata.put(MediaStore.MediaColumns.DISPLAY_NAME,captureName);
                    metadata.put(MediaStore.MediaColumns.MIME_TYPE,"image/png");
                    metadata.put(MediaStore.MediaColumns.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES+"/APO");
                    metadata.put(MediaStore.MediaColumns.IS_PENDING,1);
                    Uri saved=activity.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,metadata);
                    assertNotNull("Cannot create screenshot in emulator MediaStore",saved);
                    try(OutputStream out=activity.getContentResolver().openOutputStream(saved)){
                        assertNotNull("Cannot write emulator screenshot",out);
                        assertTrue(screenshot.compress(Bitmap.CompressFormat.PNG,100,out));
                    }
                    metadata.clear();
                    metadata.put(MediaStore.MediaColumns.IS_PENDING,0);
                    activity.getContentResolver().update(saved,metadata,null,null);
                    screenshot.recycle();
                }catch(Exception exception){
                    throw new AssertionError("Cannot save emulator screen evidence",exception);
                }
            });
        }
    }
}
