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
import android.widget.ImageView;
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

/** Real emulator layout and text-only contract for the APO PHLibrary. */
@RunWith(AndroidJUnit4.class)
public final class Rc97PhLibraryVisualInstrumentedTest {
    private static String preset(){
        return InstrumentationRegistry.getArguments().getString("preset","p360f100");
    }
    private static View find(View root,String label){
        if(root instanceof TextView &&
           ((TextView)root).getText().toString().contains(label))return root;
        if(root instanceof ViewGroup){
            ViewGroup group=(ViewGroup)root;
            for(int i=0;i<group.getChildCount();i++){
                View found=find(group.getChildAt(i),label);
                if(found!=null)return found;
            }
        }
        return null;
    }
    private static ScrollView scroll(View root){
        if(root instanceof ScrollView)return (ScrollView)root;
        if(root instanceof ViewGroup){
            ViewGroup group=(ViewGroup)root;
            for(int i=0;i<group.getChildCount();i++){
                ScrollView found=scroll(group.getChildAt(i));
                if(found!=null)return found;
            }
        }
        return null;
    }
    private static boolean hasImage(View root){
        if(root instanceof ImageView)return true;
        if(root instanceof ViewGroup){
            ViewGroup group=(ViewGroup)root;
            for(int i=0;i<group.getChildCount();i++)
                if(hasImage(group.getChildAt(i)))return true;
        }
        return false;
    }
    private static View references(View root){
        CharSequence description=root.getContentDescription();
        if(description!=null && description.toString().startsWith("Account references."))
            return root;
        if(root instanceof ViewGroup){
            ViewGroup group=(ViewGroup)root;
            for(int i=0;i<group.getChildCount();i++){
                View found=references(group.getChildAt(i));
                if(found!=null)return found;
            }
        }
        return null;
    }
    private static void capture(Activity a,String name){
        View decor=a.getWindow().getDecorView();
        assertTrue(decor.getWidth()>0 && decor.getHeight()>0);
        Bitmap image=Bitmap.createBitmap(decor.getWidth(),decor.getHeight(),
            Bitmap.Config.ARGB_8888);
        decor.draw(new Canvas(image));
        ContentValues values=new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME,name);
        values.put(MediaStore.Images.Media.MIME_TYPE,"image/png");
        values.put(MediaStore.Images.Media.RELATIVE_PATH,
            Environment.DIRECTORY_PICTURES+"/APO_RC97");
        values.put(MediaStore.Images.Media.IS_PENDING,1);
        Uri target=a.getContentResolver().insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
        assertNotNull(target);
        try(OutputStream out=a.getContentResolver().openOutputStream(target)){
            assertNotNull(out);
            assertTrue(image.compress(Bitmap.CompressFormat.PNG,100,out));
        }catch(Exception error){throw new IllegalStateException(error);}
        ContentValues done=new ContentValues();done.put(MediaStore.Images.Media.IS_PENDING,0);
        a.getContentResolver().update(target,done,null,null);
        image.recycle();
    }
    @Test public void hubHasSingleBookAndNoDecorativeMedia(){
        Intent intent=new Intent(ApplicationProvider.getApplicationContext(),
            LikasLibraryActivity.class);
        try(ActivityScenario<LikasLibraryActivity> scenario=ActivityScenario.launch(intent)){
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            scenario.onActivity(a->{
                View root=a.getWindow().getDecorView();
                assertNotNull(find(root,"Greater Mindanao"));
                assertNotNull(find(root,"714 species"));
                assertNotNull(find(root,"Browse wildlife groups"));
                assertNotNull(find(root,"Discovery Rarity"));
                View back=find(root,"Back");
                assertNotNull(back);
                if(a.getResources().getConfiguration().fontScale>=1.6f)
                    assertTrue("Back text must not crowd its outline",
                        back.getWidth()>=a.getResources().getDisplayMetrics().density*100);
                assertNull(find(root,"Photo comparisons"));
                assertFalse("No photographic or decorative ImageView in PHLibrary",hasImage(root));
                capture(a,"rc97-"+preset()+"-hub-top.png");
            });
            scenario.onActivity(a->{
                ScrollView list=scroll(a.getWindow().getDecorView());
                assertNotNull(list);
                int maximum=Math.max(0,list.getChildAt(0).getHeight()-list.getHeight());
                list.scrollTo(0,maximum);
            });
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            scenario.onActivity(a->capture(a,"rc97-"+preset()+"-hub-bottom.png"));
        }
    }
    @Test public void directoryUsesTextFiltersAndBookSpecies(){
        Intent intent=new Intent(ApplicationProvider.getApplicationContext(),
            PhLibraryDirectoryActivity.class);
        try(ActivityScenario<PhLibraryDirectoryActivity> scenario=ActivityScenario.launch(intent)){
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            scenario.onActivity(a->{
                View root=a.getWindow().getDecorView();
                assertEquals(714,PhLibraryBook01.entries(a).size());
                assertNotNull(find(root,"714 species"));
                assertNotNull(find(root,"GREATER MINDANAO"));
                assertNotNull(find(root,"Jerdon's Baza"));
                assertNotNull(find(root,"Rarity: All"));
                assertFalse("No species thumbnails",hasImage(root));
                capture(a,"rc97-"+preset()+"-directory.png");
                View birds=find(root,"Birds");
                assertNotNull(birds);
                assertTrue(birds.isClickable());
                birds.performClick();
            });
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            scenario.onActivity(a->{
                assertNotNull(find(a.getWindow().getDecorView(),"408 species found"));
            });
        }
    }
    @Test public void profileShowsFullSingleParagraphAndBottomReferences(){
        Intent intent=new Intent(ApplicationProvider.getApplicationContext(),
            TaxonDetailActivity.class).putExtra(
                "phlibrary_scientific_name","Pithecophaga jefferyi");
        try(ActivityScenario<TaxonDetailActivity> scenario=ActivityScenario.launch(intent)){
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            scenario.onActivity(a->{
                View root=a.getWindow().getDecorView();
                assertNotNull(find(root,"Philippine Eagle"));
                assertNotNull(find(root,"Legendary"));
                assertNotNull(find(root,"In flight, the Philippine Eagle"));
                assertNotNull(find(root,"REFERENCES"));
                assertNotNull(find(root,"Back"));
                assertFalse("No species photographs",hasImage(root));
                View refs=references(root);
                assertNotNull("Bottom reference citation list",refs);
                assertTrue(refs.isClickable());
                assertNotNull(find(root,"Next species"));
                capture(a,"rc97-"+preset()+"-profile-top.png");
            });
            scenario.onActivity(a->{
                ScrollView list=scroll(a.getWindow().getDecorView());
                assertNotNull(list);
                int maximum=Math.max(0,list.getChildAt(0).getHeight()-list.getHeight());
                assertTrue("Paragraph scroll reaches citations",maximum>0);
                list.scrollTo(0,maximum);
            });
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            scenario.onActivity(a->capture(a,"rc97-"+preset()+"-profile-bottom.png"));
        }
    }
}
