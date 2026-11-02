package org.phwildlife.survey;

import android.app.UiAutomation;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
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
    private static boolean hasAccessibleLabel(AccessibilityNodeInfo root,String text){
        if(root==null)return false;
        List<AccessibilityNodeInfo> found=root.findAccessibilityNodeInfosByText(text);
        if(found==null)return false;
        for(AccessibilityNodeInfo node:found){
            if(node!=null && node.isVisibleToUser()){
                CharSequence label=node.getText();
                CharSequence description=node.getContentDescription();
                if(label!=null&&label.toString().contains(text)||
                   description!=null&&description.toString().contains(text))
                    return true;
            }
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
            });
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            UiAutomation automation=InstrumentationRegistry.getInstrumentation().getUiAutomation();
            AccessibilityNodeInfo root=automation.getRootInActiveWindow();
            assertNotNull("End Survey dialog must open",root);
            assertTrue("Complete remains an explicit choice",
                hasAccessibleLabel(root,"Finish Complete"));
            assertTrue("Partial effort remains preservable",
                hasAccessibleLabel(root,"End Incomplete"));
            assertTrue("Cancelled sampling stays auditable",
                hasAccessibleLabel(root,"Cancel Survey"));
            assertTrue("Device record gaps must be visible",
                hasAccessibleLabel(root,"Device records need review"));
            assertTrue("A direct repair route to real device records is present",
                hasAccessibleLabel(root,"Review device checks"));
            assertTrue("The method review is not a completion classification",
                hasAccessibleLabel(root,"Complete checks the protocol"));
            Bitmap screenshot=automation.takeScreenshot();
            assertNotNull("Actual Android screenshot required",screenshot);
            File folder=context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            assertNotNull(folder);
            int font=Math.round(context.getResources().getConfiguration().fontScale*100);
            File png=new File(folder,font>=190?
                "apo_end_review_200.png":"apo_end_review_160.png");
            try(FileOutputStream output=new FileOutputStream(png)){
                assertTrue(screenshot.compress(Bitmap.CompressFormat.PNG,100,output));
            }
            screenshot.recycle();
        }
    }
}
