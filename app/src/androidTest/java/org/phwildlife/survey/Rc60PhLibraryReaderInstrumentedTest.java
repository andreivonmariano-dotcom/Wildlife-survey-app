package org.phwildlife.survey;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
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
import org.json.JSONObject;
import java.io.OutputStream;
import static org.junit.Assert.*;

/** RC60 real Android UI smoke checks; source assets stay separate from field observations. */
@RunWith(AndroidJUnit4.class)
public final class Rc60PhLibraryReaderInstrumentedTest {
    private static String preset(){
        return InstrumentationRegistry.getArguments().getString("preset","p360f100");
    }
    private static View findText(View view,String partial){
        if(view instanceof TextView && ((TextView)view).getText().toString().contains(partial))
            return view;
        if(view instanceof ViewGroup){
            ViewGroup group=(ViewGroup)view;
            for(int i=0;i<group.getChildCount();i++){
                View result=findText(group.getChildAt(i),partial);
                if(result!=null)return result;
            }
        }
        return null;
    }
    private static ScrollView findScroll(View view){
        if(view instanceof ScrollView)return (ScrollView)view;
        if(view instanceof ViewGroup){
            ViewGroup group=(ViewGroup)view;
            for(int i=0;i<group.getChildCount();i++){
                ScrollView result=findScroll(group.getChildAt(i));
                if(result!=null)return result;
            }
        }
        return null;
    }
    private static View findRefs(View view){
        CharSequence description=view.getContentDescription();
        if(description!=null && description.toString().startsWith("Account references."))
            return view;
        if(view instanceof ViewGroup){
            ViewGroup group=(ViewGroup)view;
            for(int i=0;i<group.getChildCount();i++){
                View result=findRefs(group.getChildAt(i));
                if(result!=null)return result;
            }
        }
        return null;
    }
    private static void capture(Activity activity,String name){
        View decor=activity.getWindow().getDecorView();
        assertTrue(decor.getWidth()>0 && decor.getHeight()>0);
        Bitmap bitmap=Bitmap.createBitmap(decor.getWidth(),decor.getHeight(),
            Bitmap.Config.ARGB_8888);
        decor.draw(new Canvas(bitmap));
        ContentValues values=new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME,name);
        values.put(MediaStore.Images.Media.MIME_TYPE,"image/png");
        values.put(MediaStore.Images.Media.RELATIVE_PATH,
            Environment.DIRECTORY_PICTURES+"/LIKAS_RC60");
        values.put(MediaStore.Images.Media.IS_PENDING,1);
        Uri target=activity.getContentResolver().insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
        assertNotNull(target);
        try(OutputStream out=activity.getContentResolver().openOutputStream(target)){
            assertNotNull(out);
            assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG,100,out));
        } catch(Exception error){throw new IllegalStateException(error);}
        ContentValues done=new ContentValues();done.put(MediaStore.Images.Media.IS_PENDING,0);
        activity.getContentResolver().update(target,done,null,null);
        bitmap.recycle();
    }
    @Test public void offlineDirectoryHas714Entries() {
        Intent intent=new Intent(ApplicationProvider.getApplicationContext(),
            PhLibraryDirectoryActivity.class);
        try(ActivityScenario<PhLibraryDirectoryActivity> scenario=ActivityScenario.launch(intent)) {
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            scenario.onActivity(a->{
                assertTrue("Complete edition",PhLibraryBook01.ready(a));
                assertEquals(714,PhLibraryBook01.entries(a).size());
                assertNotNull(findText(a.getWindow().getDecorView(),"Greater Mindanao"));
                assertNotNull(findText(a.getWindow().getDecorView(),"714 species"));
                capture(a,"rc60-"+preset()+"-directory.png");
            });
        }
    }
    @Test public void singleContinuousParagraphAndSmallBottomSources() {
        Intent intent=new Intent(ApplicationProvider.getApplicationContext(),
            TaxonDetailActivity.class)
                .putExtra("phlibrary_scientific_name","Pithecophaga jefferyi");
        try(ActivityScenario<TaxonDetailActivity> scenario=ActivityScenario.launch(intent)){
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            scenario.onActivity(a->{
                View decor=a.getWindow().getDecorView();
                assertNotNull(findText(decor,"Philippine Eagle"));
                assertNotNull(findText(decor,"Legendary"));
                assertNotNull(findText(decor,"In flight, the Philippine Eagle"));
                assertNotNull(findText(decor,"REFERENCES"));
                View refs=findRefs(decor);
                assertNotNull("Tappable compact references",refs);
                assertTrue("Reference list is clickable",refs.isClickable());
                assertTrue("References remain subordinate to body type",
                    ((TextView)refs).getTextSize() <
                    ((TextView)findText(decor,"In flight, the Philippine Eagle")).getTextSize());
                assertNotNull("Previous/next species navigation",
                    findText(decor,"Next species"));
                capture(a,"rc60-"+preset()+"-profile-top.png");
            });
            scenario.onActivity(a->{
                ScrollView scroll=findScroll(a.getWindow().getDecorView());
                assertNotNull(scroll);
                int maximum=Math.max(0,scroll.getChildAt(0).getHeight()-scroll.getHeight());
                assertTrue("Long species account is vertically scrollable",maximum>0);
                scroll.scrollTo(0,maximum);
            });
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            scenario.onActivity(a->capture(a,"rc60-"+preset()+"-profile-bottom.png"));
        }
    }
}
