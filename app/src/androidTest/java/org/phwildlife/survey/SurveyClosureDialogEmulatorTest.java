package org.phwildlife.survey;

import android.app.UiAutomation;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
import android.content.ContentValues;
import android.net.Uri;
import android.provider.MediaStore;
import java.io.OutputStream;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

/** Real Android14 end-sheet display, test-only survey, no fake field records. */
@RunWith(AndroidJUnit4.class)
public final class SurveyClosureDialogEmulatorTest {
    private static View findExactVisible(View node,String label){
        if(node instanceof TextView && node.isShown() &&
            label.equals(((TextView)node).getText().toString()))return node;
        if(node instanceof ViewGroup){
            ViewGroup group=(ViewGroup)node;
            for(int i=0;i<group.getChildCount();i++){
                View hit=findExactVisible(group.getChildAt(i),label);
                if(hit!=null)return hit;
            }
        }
        return null;
    }
    private static boolean containsDialogText(View root,String text){
        if(root instanceof TextView &&
            ((TextView)root).getText().toString().contains(text))return true;
        if(root instanceof ViewGroup){
            ViewGroup group=(ViewGroup)root;
            for(int i=0;i<group.getChildCount();i++)
                if(containsDialogText(group.getChildAt(i),text))return true;
        }
        return false;
    }
    @Test public void missingCameraChecksAreShownWithoutRemovingAnyEndOutcome()throws Exception{
        Context context=ApplicationProvider.getApplicationContext();
        try(FieldDb helper=new FieldDb(context)){
            helper.getWritableDatabase().execSQL(
                "UPDATE surveys SET ended=datetime('now'),complete=0 WHERE ended IS NULL");
        }
        Intent intent=new Intent(context,MainActivity.class)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .putExtra("create_new",true)
            .putExtra("start_without_gps",true)
            .putExtra("survey_profile","Mammals")
            .putExtra("survey_method","camera_trap_check")
            .putExtra("project_id","default");
        try(ActivityScenario<MainActivity> scenario=ActivityScenario.launch(intent)){
            scenario.onActivity(activity->{
                View end=findExactVisible(activity.getWindow().getDecorView(),"End Survey");
                assertNotNull("End Survey must remain accessible for device methods",end);
                end.performClick();
                android.app.AlertDialog dialog=activity.activeEndSurveyDialogForQa();
                assertNotNull("End Survey dialog must open",dialog);
                assertTrue(dialog.isShowing());
                assertNotNull(dialog.getWindow());
                View root=dialog.getWindow().getDecorView();
                assertTrue("Complete remains an explicit choice",
                    containsDialogText(root,"Finish Complete"));
                assertTrue("Partial effort remains preservable",
                    containsDialogText(root,"End Incomplete"));
                assertTrue("Cancelled sampling stays auditable",
                    containsDialogText(root,"Cancel Survey"));
                assertTrue("Device record gaps must be visible",
                    containsDialogText(root,"Device records need review"));
                assertTrue("A direct repair route to real device records is present",
                    containsDialogText(root,"Review device checks"));
                assertTrue("The method review is not a completion classification",
                    containsDialogText(root,"Complete checks the protocol"));
            });
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            UiAutomation automation=InstrumentationRegistry.getInstrumentation().getUiAutomation();
            Bitmap screenshot=automation.takeScreenshot();
            assertNotNull("Actual Android screenshot required",screenshot);
            String name=context.getResources().getConfiguration().fontScale>=1.9f?
                "apo_end_review_200.png":"apo_end_review_160.png";
            ContentValues meta=new ContentValues();
            meta.put(MediaStore.MediaColumns.DISPLAY_NAME,name);
            meta.put(MediaStore.MediaColumns.MIME_TYPE,"image/png");
            meta.put(MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES+"/APO");
            meta.put(MediaStore.MediaColumns.IS_PENDING,1);
            Uri saved=context.getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,meta);
            assertNotNull("Cannot create persistent emulator screenshot",saved);
            try(OutputStream output=context.getContentResolver().openOutputStream(saved)){
                assertNotNull(output);
                assertTrue(screenshot.compress(Bitmap.CompressFormat.PNG,100,output));
            }
            meta.clear();
            meta.put(MediaStore.MediaColumns.IS_PENDING,0);
            context.getContentResolver().update(saved,meta,null,null);
            screenshot.recycle();
        }
    }
}
