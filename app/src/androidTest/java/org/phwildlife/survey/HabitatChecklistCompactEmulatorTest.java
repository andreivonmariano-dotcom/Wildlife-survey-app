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
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.OutputStream;
import static org.junit.Assert.*;

/** GPS-missing, no-survey-created preflight views at two large font scales. */
@RunWith(AndroidJUnit4.class)
public final class HabitatChecklistCompactEmulatorTest {
    private static View text(View v,String fragment){
        if(v instanceof TextView&&((TextView)v).getText().toString().contains(fragment))return v;
        if(v instanceof ViewGroup){
            ViewGroup g=(ViewGroup)v;
            for(int i=0;i<g.getChildCount();i++){
                View found=text(g.getChildAt(i),fragment);
                if(found!=null)return found;
            }
        }
        return null;
    }
    private static View description(View v,String fragment){
        if(String.valueOf(v.getContentDescription()).startsWith(fragment))return v;
        if(v instanceof ViewGroup){
            ViewGroup g=(ViewGroup)v;
            for(int i=0;i<g.getChildCount();i++){
                View found=description(g.getChildAt(i),fragment);
                if(found!=null)return found;
            }
        }
        return null;
    }
    private static void screenshot(HabitatsSpeciesActivity a,String name){
        View root=a.getWindow().getDecorView();
        assertTrue("Activity must be laid out",root.getWidth()>0&&root.getHeight()>0);
        Bitmap bitmap=Bitmap.createBitmap(root.getWidth(),root.getHeight(),Bitmap.Config.ARGB_8888);
        root.draw(new Canvas(bitmap));
        ContentValues values=new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME,name);
        values.put(MediaStore.MediaColumns.MIME_TYPE,"image/png");
        values.put(MediaStore.MediaColumns.RELATIVE_PATH,Environment.DIRECTORY_PICTURES+"/APO");
        values.put(MediaStore.MediaColumns.IS_PENDING,1);
        Uri uri=a.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
        assertNotNull(uri);
        try(OutputStream out=a.getContentResolver().openOutputStream(uri)){
            assertNotNull(out);
            assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG,100,out));
        }catch(Exception error){throw new AssertionError("Screenshot failed",error);}
        values.clear();values.put(MediaStore.MediaColumns.IS_PENDING,0);
        a.getContentResolver().update(uri,values,null,null);
        bitmap.recycle();
    }
    private void verify(String group){
        Intent intent=new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(),
            HabitatsSpeciesActivity.class);
        intent.putExtra("create_new",true);
        intent.putExtra("start_without_gps",true);
        intent.putExtra("survey_profile",group);
        try(ActivityScenario<HabitatsSpeciesActivity> scenario=ActivityScenario.launch(intent)){
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            scenario.onActivity(a->{
                View root=a.getWindow().getDecorView();
                assertNotNull(text(root,"Habitats & Species"));
                assertNotNull(text(root,"Start GPS missing"));
                assertNotNull(text(root,"No habitats confirmed"));
                assertNotNull(text(root,"Choose habitats (16)"));
                assertNotNull(text(root,"Checklist: Suggested"));
                assertNotNull(text(root,"Matching does not confirm"));
                View begin=text(root,"Begin survey");
                assertNotNull(begin);assertTrue(begin.isShown());
                assertNotNull(description(root,"Show GPS and elevation context"));
                assertNotNull(description(root,"Show land-cover source, license and file management"));
                View site=description(root,"Show GPS and elevation context");
                site.performClick();
                assertNotNull(description(root,"Hide GPS and elevation context"));
                site.performClick();
                View all=text(root,"All habitats (16)");
                all.performClick();
                assertNotNull(text(root,"Hide habitats"));
                CheckBox candidate=null;
                for(String name:PaicHabitatPolicy.HABITATS){
                    View match=text(root,name);
                    if(match instanceof CheckBox){candidate=(CheckBox)match;break;}
                }
                assertNotNull("Full habitat list reachable",candidate);
                candidate.setChecked(true);
                assertNotNull(text(root,"1 habitat confirmed"));
                candidate.setChecked(false);
                assertNotNull(text(root,"No habitats confirmed"));
                text(root,"Hide habitats").performClick();
            });
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            scenario.onActivity(a->{
                String scale=a.getResources().getConfiguration().fontScale>=1.9f?"200":"160";
                screenshot(a,"apo_habitats_"+group.toLowerCase(java.util.Locale.US)+"_"+scale+".png");
            });
        }
    }
    @Test public void birdsPreflightCompactAndReversible(){verify("Birds");}
    @Test public void herpetofaunaPreflightCompactAndReversible(){verify("Herpetofauna");}
}
