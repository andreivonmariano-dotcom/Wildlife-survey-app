package org.phwildlife.survey;

import android.app.Activity;
import android.content.Intent;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.OutputStream;
import java.util.Locale;
import static org.junit.Assert.*;

/** Disposable emulator setup: three wildlife groups; never starts a real survey. */
@RunWith(AndroidJUnit4.class)
public final class SetupCompactUiEmulatorTest {
    private View byDescription(View root,String description){
        if(String.valueOf(root.getContentDescription()).startsWith(description))return root;
        if(root instanceof ViewGroup){
            ViewGroup group=(ViewGroup)root;
            for(int i=0;i<group.getChildCount();i++){
                View found=byDescription(group.getChildAt(i),description);
                if(found!=null)return found;
            }
        }
        return null;
    }
    private View byText(View root,String content){
        if(root instanceof TextView && content.equals(((TextView)root).getText().toString()))
            return root;
        if(root instanceof ViewGroup){
            ViewGroup group=(ViewGroup)root;
            for(int i=0;i<group.getChildCount();i++){
                View found=byText(group.getChildAt(i),content);
                if(found!=null)return found;
            }
        }
        return null;
    }
    private void snapshot(Activity a,String profile) {
        try {
            View root=a.getWindow().getDecorView();
            assertTrue("Setup screen not laid out",root.getWidth()>0&&root.getHeight()>0);
            Bitmap bitmap=Bitmap.createBitmap(root.getWidth(),root.getHeight(),Bitmap.Config.ARGB_8888);
            root.draw(new Canvas(bitmap));
            String size=a.getResources().getConfiguration().fontScale>=1.9f?"200":"160";
            String fileName="apo_setup_"+profile.toLowerCase(Locale.US)+"_"+size+".png";
            ContentValues values=new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME,fileName);
            values.put(MediaStore.MediaColumns.MIME_TYPE,"image/png");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH,Environment.DIRECTORY_PICTURES+"/APO");
            values.put(MediaStore.MediaColumns.IS_PENDING,1);
            Uri destination=a.getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
            assertNotNull("Cannot save screenshot",destination);
            try(OutputStream output=a.getContentResolver().openOutputStream(destination)){
                assertNotNull(output);
                assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG,100,output));
            }
            values.clear();values.put(MediaStore.MediaColumns.IS_PENDING,0);
            a.getContentResolver().update(destination,values,null,null);
            bitmap.recycle();
        }catch(Exception e){throw new AssertionError("Screenshot failed",e);}
    }
    @Test public void eachWildlifeGroupHasShortSetupAndReachableAdvancedControls(){
        for(String profile:new String[]{"Birds","Herpetofauna","Mammals"}){
            Intent home=new Intent(ApplicationProvider.getApplicationContext(),WelcomeActivity.class);
            home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            home.putExtra("show_menu",true);
            try(ActivityScenario<WelcomeActivity> scenario=ActivityScenario.launch(home)){
                scenario.onActivity(activity->{
                    View root=activity.getWindow().getDecorView();
                    View bioTools=byDescription(root,"Open BioTools.");
                    assertNotNull(bioTools);bioTools.performClick();
                    View surveys=byDescription(root,"Open Surveys.");
                    assertNotNull(surveys);surveys.performClick();
                    View choice=byDescription(root,"Open "+profile+".");
                    assertNotNull(choice);choice.performClick();
                    View method=byDescription(root,"Choose survey method");
                    assertTrue("Selected method must be visible",method instanceof Spinner&&method.isShown());
                    assertNotNull(byText(root,profile+" survey"));
                    assertNotNull(byText(root,"Project and location"));
                    assertNotNull(byText(root,"Sampling effort"));
                    View gps=byText(root,"Continue to GPS");
                    assertNotNull(gps);assertTrue(gps.isShown());
                    View comparison=byDescription(root,"Show all survey methods");
                    assertNotNull(comparison);assertTrue(comparison.isShown());
                    String option=profile.equals("Birds")?"Line transect":
                        profile.equals("Herpetofauna")?"Amphibian calling":"Bat acoustic survey · basic log";
                    View comparisonChoice=byText(root,option);
                    assertNotNull(comparisonChoice);
                    assertFalse("Method catalog must begin collapsed",comparisonChoice.isShown());
                    comparison.performClick();
                    assertTrue("Method catalog must open",comparisonChoice.isShown());
                    comparison.performClick();
                    assertFalse("Method catalog must close",comparisonChoice.isShown());
                    View helper=byDescription(root,"Show GPS checklist area assistance");
                    assertNotNull(helper);assertTrue(helper.isShown());
                    View gpsAssist=byText(root,"Suggest checklist area using GPS");
                    assertNotNull(gpsAssist);assertFalse(gpsAssist.isShown());
                    helper.performClick();assertTrue(gpsAssist.isShown());
                    helper.performClick();assertFalse(gpsAssist.isShown());
                    if(profile.equals("Birds")){
                        View route=byDescription(root,"Show repeat route options");
                        assertNotNull(route);assertTrue(route.isShown());
                        View save=byText(root,"Save this route for future monitoring");
                        assertTrue(save instanceof CheckBox);
                        assertFalse(save.isShown());
                        route.performClick();
                        assertTrue(save.isShown());
                        assertFalse("Saving a route must be deliberate",((CheckBox)save).isChecked());
                        route.performClick();assertFalse(save.isShown());
                    }
                    snapshot(activity,profile);
                });
            }
        }
    }
}
