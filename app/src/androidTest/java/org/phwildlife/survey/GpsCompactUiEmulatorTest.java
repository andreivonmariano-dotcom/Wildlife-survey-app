package org.phwildlife.survey;

import android.content.Intent;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.OutputStream;

import static org.junit.Assert.*;

/** Native GPS preflight screen capture at enlarged fonts, no field data or fake fixes. */
@RunWith(AndroidJUnit4.class)
public final class GpsCompactUiEmulatorTest {
    private static View findText(View view,String value){
        if(view instanceof TextView &&
            ((TextView)view).getText().toString().contains(value))return view;
        if(view instanceof ViewGroup){
            ViewGroup group=(ViewGroup)view;
            for(int i=0;i<group.getChildCount();i++){
                View hit=findText(group.getChildAt(i),value);
                if(hit!=null)return hit;
            }
        }
        return null;
    }
    private static View findDescription(View view,String prefix){
        if(String.valueOf(view.getContentDescription()).startsWith(prefix))return view;
        if(view instanceof ViewGroup){
            ViewGroup group=(ViewGroup)view;
            for(int i=0;i<group.getChildCount();i++){
                View hit=findDescription(group.getChildAt(i),prefix);
                if(hit!=null)return hit;
            }
        }
        return null;
    }
    private static void screenshot(SurveyGpsActivity activity,String file){
        View root=activity.getWindow().getDecorView();
        assertTrue("GPS screen needs completed layout",root.getWidth()>0&&root.getHeight()>0);
        Bitmap image=Bitmap.createBitmap(root.getWidth(),root.getHeight(),Bitmap.Config.ARGB_8888);
        root.draw(new Canvas(image));
        ContentValues entry=new ContentValues();
        entry.put(MediaStore.MediaColumns.DISPLAY_NAME,file);
        entry.put(MediaStore.MediaColumns.MIME_TYPE,"image/png");
        entry.put(MediaStore.MediaColumns.RELATIVE_PATH,Environment.DIRECTORY_PICTURES+"/APO");
        entry.put(MediaStore.MediaColumns.IS_PENDING,1);
        Uri uri=activity.getContentResolver().insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,entry);
        assertNotNull("Cannot save native GPS screenshot",uri);
        try(OutputStream out=activity.getContentResolver().openOutputStream(uri)){
            assertNotNull(out);
            assertTrue(image.compress(Bitmap.CompressFormat.PNG,100,out));
        }catch(Exception e){throw new AssertionError("GPS screenshot failed",e);}
        entry.clear();entry.put(MediaStore.MediaColumns.IS_PENDING,0);
        activity.getContentResolver().update(uri,entry,null,null);
        image.recycle();
    }
    private void capture(boolean station){
        Intent launch=new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(),
            SurveyGpsActivity.class);
        launch.putExtra("station_gps_mode",station);
        try(ActivityScenario<SurveyGpsActivity> scenario=ActivityScenario.launch(launch)){
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            scenario.onActivity(activity->{
                View root=activity.getWindow().getDecorView();
                View status=findText(root,"Device GPS is switched off");
                assertNotNull("Testing GPS-disabled preflight state",status);
                assertTrue(status.isShown());
                View quality=findText(root,"No verified position");
                assertNotNull(quality);
                assertTrue(quality.isShown());
                View info=findDescription(root,"Show coordinates, altitude, accuracy");
                assertNotNull(info);
                assertTrue(info.isShown());
                View action=findText(root,station?"Start station without GPS":"Continue without GPS");
                assertNotNull(action);
                assertTrue("Missing GPS action pinned and visible",action.isShown());
                int[] pos=new int[2];action.getLocationOnScreen(pos);
                int h=activity.getResources().getDisplayMetrics().heightPixels;
                assertTrue("Missing GPS action must fit viewport",pos[1]>=0&&
                    pos[1]+action.getHeight()<=h);
                String scale=activity.getResources().getConfiguration().fontScale>=1.9f?
                    "200":"160";
                screenshot(activity,"apo_gps_"+(station?"station":"survey")+"_"+scale+".png");
            });
        }
    }
    @Test public void surveyGpsIsUsableAtCurrentTextScale(){capture(false);}
    @Test public void stationGpsIsUsableAtCurrentTextScale(){capture(true);}
}
