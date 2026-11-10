package org.phwildlife.survey;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.OutputStream;
import static org.junit.Assert.*;

/** Disposable emulator visual evidence only. Never changes field survey records. */
@RunWith(AndroidJUnit4.class)
public final class Rc66SpeciesAccountInstrumentedTest {
    private static View findText(View view,String value) {
        if(view instanceof TextView && value.equals(((TextView)view).getText().toString()))
            return view;
        if(view instanceof ViewGroup) {
            ViewGroup group=(ViewGroup)view;
            for(int i=0;i<group.getChildCount();i++) {
                View hit=findText(group.getChildAt(i),value);
                if(hit!=null)return hit;
            }
        }
        return null;
    }
    private static ScrollView findScroll(View view) {
        if(view instanceof ScrollView)return (ScrollView)view;
        if(view instanceof ViewGroup) {
            ViewGroup group=(ViewGroup)view;
            for(int i=0;i<group.getChildCount();i++) {
                ScrollView hit=findScroll(group.getChildAt(i));
                if(hit!=null)return hit;
            }
        }
        return null;
    }
    private static void capture(Activity a,String filename) {
        Bitmap bmp=InstrumentationRegistry.getInstrumentation()
            .getUiAutomation().takeScreenshot();
        assertNotNull("Actual emulator screen screenshot",bmp);
        ContentValues values=new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME,filename);
        values.put(MediaStore.Images.Media.MIME_TYPE,"image/png");
        values.put(MediaStore.Images.Media.RELATIVE_PATH,
            Environment.DIRECTORY_PICTURES+"/LIKAS_RC66");
        values.put(MediaStore.Images.Media.IS_PENDING,1);
        Uri uri=a.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
        assertNotNull(uri);
        try(OutputStream out=a.getContentResolver().openOutputStream(uri)) {
            assertNotNull(out);
            assertTrue(bmp.compress(Bitmap.CompressFormat.PNG,100,out));
        }catch(Exception error){throw new IllegalStateException(error);}
        values.clear();
        values.put(MediaStore.Images.Media.IS_PENDING,0);
        a.getContentResolver().update(uri,values,null,null);
        bmp.recycle();
    }
    private static String preset() {
        return InstrumentationRegistry.getArguments().getString("preset","p360f100");
    }
    private static void idle() {
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        try{Thread.sleep(550);}catch(InterruptedException e){Thread.currentThread().interrupt();}
    }
    private static String knownKey() {
        Context ctx=ApplicationProvider.getApplicationContext();
        FieldDb field=new FieldDb(ctx);
        try(Cursor c=field.getReadableDatabase().rawQuery(
            "SELECT taxon_key FROM taxon_catalog "+
            "WHERE scientific_name='Pithecophaga jefferyi' LIMIT 1",null)) {
            if(!c.moveToFirst())throw new AssertionError("Philippine Eagle source account missing");
            return c.getString(0);
        }finally{field.close();}
    }

    @Test public void individualAccountPrioritizesCitedReading() {
        Intent intent=new Intent(ApplicationProvider.getApplicationContext(),
            TaxonDetailActivity.class).putExtra("taxon_key",knownKey());
        try(ActivityScenario<TaxonDetailActivity> scenario=ActivityScenario.launch(intent)) {
            idle();
            scenario.onActivity(a->{
                View decor=a.getWindow().getDecorView();
                View conservation=findText(decor,"Conservation research · 96 species");
                assertNotNull("Conservation hub action retained for search mode",conservation);
                assertEquals("Conservation hub hidden above individual record",
                    View.GONE,conservation.getVisibility());
                View searchAnother=findText(decor,"⌕  Search another species");
                assertNotNull("Return to catalog search",searchAnother);
                assertTrue(searchAnother.isShown());
                ScrollView scroll=findScroll(decor);
                assertNotNull("Scientific account scrolls",scroll);
                assertNotNull("Scientific name visible",findText(decor,"Pithecophaga jefferyi"));
                capture(a,"rc66-"+preset()+"-account-top.png");
                scroll.scrollTo(0,scroll.getChildAt(0).getHeight());
            });
            idle();
            scenario.onActivity(a->capture(a,"rc66-"+preset()+"-account-bottom.png"));
        }
    }

    @Test public void searchActionsRemainReachableAtLargeFont() {
        Intent intent=new Intent(ApplicationProvider.getApplicationContext(),
            TaxonDetailActivity.class);
        try(ActivityScenario<TaxonDetailActivity> scenario=ActivityScenario.launch(intent)) {
            idle();
            scenario.onActivity(a->{
                View decor=a.getWindow().getDecorView();
                View conservation=findText(decor,"Conservation research · 96 species");
                assertNotNull(conservation);
                assertTrue(conservation.isShown());
                assertNotNull(findText(decor,"Find"));
                assertNotNull(findText(decor,"▾"));
                capture(a,"rc66-"+preset()+"-search.png");
            });
        }
    }
}
