package org.phwildlife.survey;

import android.content.ContentValues;
import android.content.Intent;
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
    @Test public void birdPickerSearchAndList(){verify("Birds","Birds");}
    @Test public void herpetofaunaPickerGroup(){verify("Herpetofauna","Amphibians");}
}
