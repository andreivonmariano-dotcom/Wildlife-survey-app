package org.phwildlife.survey;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.OutputStream;
import static org.junit.Assert.*;

/** Native species picker usability at 160/200% fonts; no fake species or locations. */
@RunWith(AndroidJUnit4.class)
public final class RecorderPickerCompactEmulatorTest {
    private static View text(View v,String phrase){
        if(v instanceof TextView&&String.valueOf(((TextView)v).getText()).contains(phrase))
            return v;
        if(v instanceof ViewGroup){
            ViewGroup g=(ViewGroup)v;
            for(int i=0;i<g.getChildCount();i++){
                View hit=text(g.getChildAt(i),phrase);
                if(hit!=null)return hit;
            }
        }
        return null;
    }
    private static View description(View v,String prefix){
        if(String.valueOf(v.getContentDescription()).startsWith(prefix))return v;
        if(v instanceof ViewGroup){
            ViewGroup g=(ViewGroup)v;
            for(int i=0;i<g.getChildCount();i++){
                View hit=description(g.getChildAt(i),prefix);
                if(hit!=null)return hit;
            }
        }
        return null;
    }
    private static void screenshot(SpeciesPickerActivity a,String file){
        View root=a.getWindow().getDecorView();
        assertTrue(root.getWidth()>0&&root.getHeight()>0);
        Bitmap image=Bitmap.createBitmap(root.getWidth(),root.getHeight(),Bitmap.Config.ARGB_8888);
        root.draw(new Canvas(image));
        ContentValues data=new ContentValues();
        data.put(MediaStore.MediaColumns.DISPLAY_NAME,file);
        data.put(MediaStore.MediaColumns.MIME_TYPE,"image/png");
        data.put(MediaStore.MediaColumns.RELATIVE_PATH,Environment.DIRECTORY_PICTURES+"/APO");
        data.put(MediaStore.MediaColumns.IS_PENDING,1);
        Uri uri=a.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,data);
        assertNotNull(uri);
        try(OutputStream out=a.getContentResolver().openOutputStream(uri)){
            assertNotNull(out);assertTrue(image.compress(Bitmap.CompressFormat.PNG,100,out));
        }catch(Exception e){throw new AssertionError("Species picker screenshot failed",e);}
        data.clear();data.put(MediaStore.MediaColumns.IS_PENDING,0);
        a.getContentResolver().update(uri,data,null,null);
        image.recycle();
    }
    private void verify(String profile,String group){
        Intent i=new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(),
            SpeciesPickerActivity.class);
        i.putExtra("profile",profile);
        i.putExtra("group",group);
        i.putExtra("geo_region","MINDANAO");
        try{
            BundledPrivateReferences.ensureInstalled(
                InstrumentationRegistry.getInstrumentation().getTargetContext());
        }catch(Exception error){throw new AssertionError("PAIC installation failed",error);}
        try(FieldDb db=new FieldDb(
                InstrumentationRegistry.getInstrumentation().getTargetContext());
            Cursor count=db.getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM taxon_catalog",null)){
            assertTrue(count.moveToFirst());
            assertEquals("Active survey species roster",714,count.getInt(0));
        }
        try(ActivityScenario<SpeciesPickerActivity> scenario=ActivityScenario.launch(i)){
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            scenario.onActivity(a->{
                View root=a.getWindow().getDecorView();
                assertNotNull("Search input present",description(root,"Search offline species by"));
                assertNotNull("Species header",text(root,"Species"));
                assertNotNull("Unknown ID path",description(root,"Record an unidentified species"));
                assertNotNull("Readable ordering control",text(root,"Order"));
                assertNotNull("Site conditions control",text(root,"Site"));
            });
            // The native card query runs asynchronously; do not capture a loading page.
            boolean loaded=false;
            for(int attempt=0;attempt<30&&!loaded;attempt++){
                InstrumentationRegistry.getInstrumentation().waitForIdleSync();
                final boolean[] ready={false};
                scenario.onActivity(a->ready[0]=text(a.getWindow().getDecorView(),
                    "Select")!=null);
                loaded=ready[0];
                if(!loaded)try{Thread.sleep(300);}catch(InterruptedException e){
                    Thread.currentThread().interrupt();break;
                }
            }
            assertTrue("Species cards must finish loading",loaded);
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            scenario.onActivity(a->{
                View root=a.getWindow().getDecorView();
                View list=text(root,"species");
                assertNotNull("Results count shown",list);
                View choose=text(root,"Select");
                assertNotNull("Loaded species card",choose);
                assertTrue("Loaded species can be selected",choose.isClickable());
                String scale=a.getResources().getConfiguration().fontScale>=1.9f?"200":"160";
                screenshot(a,"apo_picker_"+group.toLowerCase(java.util.Locale.US)+"_"+scale+".png");
            });
        }
    }
    @Test public void launchPathActivatesPaicReferences(){
        Intent launch=new Intent(
            InstrumentationRegistry.getInstrumentation().getTargetContext(),
            LikasLaunchActivity.class);
        try(ActivityScenario<LikasLaunchActivity> scenario=ActivityScenario.launch(launch)){
            try{Thread.sleep(2500);}catch(InterruptedException e){
                Thread.currentThread().interrupt();
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        }
        try(FieldDb db=new FieldDb(
                InstrumentationRegistry.getInstrumentation().getTargetContext());
            Cursor count=db.getReadableDatabase().rawQuery(
                "SELECT taxon_group,COUNT(*) FROM taxon_catalog GROUP BY taxon_group",null)){
            int[] actual=new int[4];
            while(count.moveToNext()){
                switch(count.getString(0)){
                    case "Birds":actual[0]=count.getInt(1);break;
                    case "Reptiles":actual[1]=count.getInt(1);break;
                    case "Mammals":actual[2]=count.getInt(1);break;
                    case "Amphibians":actual[3]=count.getInt(1);break;
                }
            }
            assertArrayEquals("PAIC group counts after startup",
                new int[]{408,154,99,53},actual);
        }
    }
    @Test public void montaneContextHidesCoastalTernButAllSpeciesCanFindIt(){
        try{
            BundledPrivateReferences.ensureInstalled(
                InstrumentationRegistry.getInstrumentation().getTargetContext());
        }catch(Exception error){throw new AssertionError(error);}
        Intent intent=new Intent(
            InstrumentationRegistry.getInstrumentation().getTargetContext(),
            SpeciesPickerActivity.class);
        intent.putExtra("profile","Birds");
        intent.putExtra("group","Birds");
        intent.putExtra("geo_region","MINDANAO");
        intent.putExtra("survey_id","RC87-MONTANE-EMULATOR");
        intent.putExtra("habitat","montane forest");
        try(ActivityScenario<SpeciesPickerActivity> scenario=ActivityScenario.launch(intent)){
            scenario.onActivity(a->{
                View root=a.getWindow().getDecorView();
                View search=description(root,"Search offline species by");
                assertTrue(search instanceof android.widget.AutoCompleteTextView);
                ((android.widget.AutoCompleteTextView)search).setText("Aleutian Tern");
                assertNotNull("Shortlist switch should be visible",
                    text(root,"Habitat shortlist · Show all species"));
            });
            boolean excluded=false;
            for(int attempt=0;attempt<30&&!excluded;attempt++){
                InstrumentationRegistry.getInstrumentation().waitForIdleSync();
                final boolean[] done={false};
                scenario.onActivity(a->done[0]=text(a.getWindow().getDecorView(),
                    "No matching habitat guide labels")!=null);
                excluded=done[0];
                if(!excluded)try{Thread.sleep(300);}catch(InterruptedException e){
                    Thread.currentThread().interrupt();break;
                }
            }
            assertTrue("Coastal-only tern must be absent from montane shortlist",excluded);
            scenario.onActivity(a->{
                View toggle=text(a.getWindow().getDecorView(),
                    "Habitat shortlist · Show all species");
                assertNotNull(toggle);toggle.performClick();
            });
            boolean recovered=false;
            for(int attempt=0;attempt<30&&!recovered;attempt++){
                InstrumentationRegistry.getInstrumentation().waitForIdleSync();
                final boolean[] done={false};
                scenario.onActivity(a->{
                    View root=a.getWindow().getDecorView();
                    done[0]=text(root,"Aleutian Tern")!=null&&
                        text(root,"Select")!=null;
                });
                recovered=done[0];
                if(!recovered)try{Thread.sleep(300);}catch(InterruptedException e){
                    Thread.currentThread().interrupt();break;
                }
            }
            assertTrue("All-species fallback must retain Aleutian Tern",recovered);
        }
    }
    @Test public void birdPickerSearchAndList(){verify("Birds","Birds");}
    @Test public void herpetofaunaPickerGroup(){verify("Herpetofauna","Amphibians");}
}
