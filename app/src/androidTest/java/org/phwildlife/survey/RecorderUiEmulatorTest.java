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
    private View findSpeciesInput(View view){
        if(view instanceof android.widget.AutoCompleteTextView&&
            "Species".contentEquals(((android.widget.AutoCompleteTextView)view).getHint()))
            return view;
        if(view instanceof ViewGroup){
            ViewGroup group=(ViewGroup)view;
            for(int i=0;i<group.getChildCount();i++){
                View hit=findSpeciesInput(group.getChildAt(i));
                if(hit!=null)return hit;
            }
        }
        return null;
    }

    @Test public void keyboardKeepsSurveyActionsReachable() throws Exception {
        Intent launch=new Intent(ApplicationProvider.getApplicationContext(),MainActivity.class);
        launch.putExtra("create_new",true);
        launch.putExtra("start_without_gps",true);
        launch.putExtra("survey_profile","Birds");
        launch.putExtra("survey_method","opportunistic");
        launch.putExtra("project_id","default");
        try(ActivityScenario<MainActivity> scenario=ActivityScenario.launch(launch)){
            scenario.onActivity(a->{
                View field=findSpeciesInput(a.getWindow().getDecorView());
                assertNotNull("Species input is available",field);
                field.requestRectangleOnScreen(new android.graphics.Rect(0,0,field.getWidth(),field.getHeight()),true);
                assertTrue(field.requestFocus());
                field.postDelayed(()->{
                    android.view.inputmethod.InputMethodManager ime=
                        (android.view.inputmethod.InputMethodManager)a.getSystemService(
                            Activity.INPUT_METHOD_SERVICE);
                    ime.showSoftInput(field,android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
                    if(android.os.Build.VERSION.SDK_INT>=30&&field.getWindowInsetsController()!=null)
                        field.getWindowInsetsController().show(android.view.WindowInsets.Type.ime());
                },300);
            });
            boolean visible=false;
            for(int i=0;i<40&&!visible;i++){
                final boolean[] shown={false};
                scenario.onActivity(a->{
                    android.view.WindowInsets insets=a.getWindow().getDecorView().getRootWindowInsets();
                    if(insets!=null&&android.os.Build.VERSION.SDK_INT>=30)
                        shown[0]=insets.isVisible(android.view.WindowInsets.Type.ime());
                });
                visible=shown[0];
                if(!visible)android.os.SystemClock.sleep(150);
            }
            assertTrue("Keyboard actually displayed",visible);
            scenario.onActivity(a->{
                View root=a.getWindow().getDecorView();
                android.graphics.Rect viewport=new android.graphics.Rect();
                root.getWindowVisibleDisplayFrame(viewport);
                for(String label:new String[]{"Save Observation","End Survey"}){
                    View control=findText(root,label);
                    assertNotNull(label,control);
                    android.graphics.Rect bounds=new android.graphics.Rect();
                    assertTrue(label+" visible with keyboard",control.getGlobalVisibleRect(bounds));
                    assertTrue(label+" entire control fits above keyboard",bounds.height()>=control.getHeight()
                        && bounds.bottom<=viewport.bottom);
                }
            });
            android.graphics.Bitmap screenshot=androidx.test.platform.app.InstrumentationRegistry
                .getInstrumentation().getUiAutomation().takeScreenshot();
            assertNotNull("Native keyboard screenshot",screenshot);
            scenario.onActivity(a->{
                java.io.File directory=new java.io.File(
                    a.getExternalFilesDir(Environment.DIRECTORY_PICTURES),"APO_SHELL_REVIEW");
                assertTrue(directory.isDirectory()||directory.mkdirs());
                try(java.io.FileOutputStream out=new java.io.FileOutputStream(
                    new java.io.File(directory,"recorder-keyboard.png"))){
                    assertTrue(screenshot.compress(Bitmap.CompressFormat.PNG,100,out));
                }catch(Exception e){throw new AssertionError(e);}
            });
            screenshot.recycle();
        }
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
                View speciesList=findText(root,ultraLarge||activity.getResources().getConfiguration().fontScale>=1.55f?"List":"Species list");
                View countLabel=findText(root,"Individuals");
                assertNotNull("Species picker entry remains discoverable",speciesList);
                assertNotNull("Individual count remains available",countLabel);
                // Step one: mark before identification, confirm unsaved draft,
                // and reset without creating a field observation.
                View previousMark=findText(root,"Edit mark");
                if(previousMark!=null){
                    View clearPrevious=findText(root,"Clear mark");
                    assertNotNull(clearPrevious);clearPrevious.performClick();
                }
                View mark=findText(root,activity.getResources().getConfiguration().fontScale>=1.55f?"Mark time":"Mark detection");
                assertNotNull("Mark detection belongs in primary field flow",mark);
                assertTrue("Mark detection must be accessible without opening tools",
                    mark.isShown());
                int[] markY=new int[2],speciesMarkY=new int[2];
                mark.getLocationOnScreen(markY);
                speciesList.getLocationOnScreen(speciesMarkY);
                assertTrue("Detection must come before species identification",
                    markY[1]<speciesMarkY[1]);
                assertNotNull("Unmarked time provenance must be displayed",
                    findContainingText(root,"Unmarked · save time"));
                mark.performClick();
                assertNotNull("Marked time/GPS must be displayed as unsaved draft",
                    findContainingText(root,"Draft · "));
                assertNotNull("Marked time is not an observation save",
                    findText(root,"Save Observation"));
                View clearMark=findText(root,"Clear mark");
                assertNotNull("Marked draft must have a reversible clear action",clearMark);
                assertTrue(clearMark.isShown());
                clearMark.performClick();
                assertNotNull("Clearing a mark returns to unmarked state",
                    findContainingText(root,"Unmarked · save time"));
                int[] speciesY=new int[2],saveY=new int[2],countY=new int[2];
                speciesList.getLocationOnScreen(speciesY);
                countLabel.getLocationOnScreen(countY);
                save.getLocationOnScreen(saveY);
                assertTrue("Species picker button should be above fixed Save footer",
                    speciesY[1]>=0&&speciesY[1]+speciesList.getHeight()<=saveY[1]);
                assertTrue("Individuals should be above fixed Save footer",
                    countY[1]>=0&&countY[1]+countLabel.getHeight()<=saveY[1]);
                View samplingTools=findText(root,"Sampling session");
                View detectionTools=findText(root,"Detection tools");
                assertNotNull("Session section missing",samplingTools);
                assertNotNull("Detection section missing",detectionTools);
                if(activity.getResources().getConfiguration().fontScale>=1.60f){
                    View compactClock=findContainingText(root,ultraLarge?"Time ":"More details");
                    View compactGps=findContainingText(root,ultraLarge?"Start GPS missing":"Start GPS unverified");
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
                        "Field tools":"Field tools");
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
