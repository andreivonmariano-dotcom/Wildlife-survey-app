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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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

/** Read-only visual acceptance; only a disposable Android 14 test device is used. */
@RunWith(AndroidJUnit4.class)
public final class Rc62SpeciesDiscoverInstrumentedTest {
    private String preset(){
        return InstrumentationRegistry.getArguments().getString("preset","p360f100");
    }
    private static View described(View root,String prefix){
        CharSequence label=root.getContentDescription();
        if(label!=null&&label.toString().startsWith(prefix))return root;
        if(root instanceof ViewGroup){
            ViewGroup group=(ViewGroup)root;
            for(int i=0;i<group.getChildCount();i++){
                View hit=described(group.getChildAt(i),prefix);
                if(hit!=null)return hit;
            }
        }
        return null;
    }
    private static TextView heading(View root,String title){
        if(root instanceof TextView&&!(root instanceof Button)
            &&title.contentEquals(((TextView)root).getText()))return (TextView)root;
        if(root instanceof ViewGroup){
            ViewGroup group=(ViewGroup)root;
            for(int i=0;i<group.getChildCount();i++){
                TextView hit=heading(group.getChildAt(i),title);
                if(hit!=null)return hit;
            }
        }
        return null;
    }
    private static ScrollView scroll(View root){
        if(root instanceof ScrollView)return (ScrollView)root;
        if(root instanceof ViewGroup){
            ViewGroup group=(ViewGroup)root;
            for(int i=0;i<group.getChildCount();i++){
                ScrollView hit=scroll(group.getChildAt(i));
                if(hit!=null)return hit;
            }
        }
        return null;
    }
    private static void capture(Activity a,String filename){
        View root=a.getWindow().getDecorView();
        assertTrue("Screen measured",root.getWidth()>0&&root.getHeight()>0);
        Bitmap bitmap=Bitmap.createBitmap(root.getWidth(),root.getHeight(),
            Bitmap.Config.ARGB_8888);
        root.draw(new Canvas(bitmap));
        ContentValues row=new ContentValues();
        row.put(MediaStore.Images.Media.DISPLAY_NAME,filename);
        row.put(MediaStore.Images.Media.MIME_TYPE,"image/png");
        row.put(MediaStore.Images.Media.RELATIVE_PATH,
            Environment.DIRECTORY_PICTURES+"/LIKAS_RC62");
        row.put(MediaStore.Images.Media.IS_PENDING,1);
        Uri target=a.getContentResolver().insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,row);
        assertNotNull("Screenshot MediaStore entry",target);
        try(OutputStream out=a.getContentResolver().openOutputStream(target)){
            assertNotNull(out);
            assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG,100,out));
        }catch(Exception e){throw new IllegalStateException(e);}
        ContentValues done=new ContentValues();
        done.put(MediaStore.Images.Media.IS_PENDING,0);
        a.getContentResolver().update(target,done,null,null);
        bitmap.recycle();
    }
    private void discovery(String mode,String shelf,String filename){
        Intent intent=new Intent(ApplicationProvider.getApplicationContext(),
            LikasDiscoveryActivity.class).putExtra("view",mode);
        try(ActivityScenario<LikasDiscoveryActivity> scenario=ActivityScenario.launch(intent)){
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            scenario.onActivity(a->{
                View root=a.getWindow().getDecorView();
                View next=described(root,"Next "+shelf+" card");
                assertNotNull("Next control: "+shelf,next);
                assertTrue("Next enabled: "+shelf,next.isEnabled());
                TextView title=heading(root,shelf);
                assertNotNull("Shelf title: "+shelf,title);
                EditText search=(EditText)described(root,
                    "Search illustrated offline species and habitat accounts");
                assertNotNull("Discover search present",search);
                boolean large=a.getResources().getConfiguration().fontScale>=1.60f;
                assertEquals(large?"Search":"Find name or habitat",
                    String.valueOf(search.getHint()));
                if(large){
                    assertEquals("Long shelf titles do not wrap",1,title.getLineCount());
                    assertEquals("Shelf title occupies own line",
                        LinearLayout.VERTICAL,
                        ((LinearLayout)title.getParent()).getOrientation());
                    // Intro text hidden at 200% while species context remains.
                    assertNull("Redundant kicker hidden for large type",
                        heading(root,"WILDLIFE"));
                    assertNull("Redundant subtitle hidden for large type",
                        heading(root,"Offline species guides"));
                }
                View filter=described(root,
                    "Species group filters. Swipe horizontally for more groups.");
                assertNotNull("Off-screen group-filter affordance",filter);
                assertTrue("Filter rail has a fading overflow edge",
                    filter.isHorizontalFadingEdgeEnabled());
                assertNotNull("Scientific name remains on a card",
                    heading(root,"Ilin Bushy-tailed Cloud Rat"));
                assertNotNull("Illustrated media marks its source appropriately",
                    heading(root,"ILLUSTRATION"));
                capture(a,"rc62-"+preset()+"-"+filename+"-top.png");
            });
            scenario.onActivity(a->{
                ScrollView page=scroll(a.getWindow().getDecorView());
                assertNotNull("Discovery vertical scroll",page);
                page.scrollTo(0,Math.round(380*
                    a.getResources().getDisplayMetrics().density));
            });
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            scenario.onActivity(a->capture(a,
                "rc62-"+preset()+"-"+filename+"-scrolled.png"));
        }
    }
    @Test public void speciesShelfRemainsReadable(){
        discovery("species","Conservation","species");
    }


    @Test public void searchingScientificNameAndClearingPreservesOfflineEvidence(){
        Intent entry=new Intent(ApplicationProvider.getApplicationContext(),
            LikasDiscoveryActivity.class).putExtra("view","species");
        try(ActivityScenario<LikasDiscoveryActivity> scenario=ActivityScenario.launch(entry)){
            scenario.onActivity(a->{
                EditText search=(EditText)described(a.getWindow().getDecorView(),
                    "Search illustrated offline species and habitat accounts");
                assertNotNull(search);
                search.setText("Pithecophaga jefferyi");
            });
            try{Thread.sleep(350);}catch(InterruptedException e){Thread.currentThread().interrupt();}
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            scenario.onActivity(a->{
                View root=a.getWindow().getDecorView();
                assertNotNull("Scientific-name search returns a rail",
                    described(root,"Swipe Matches cards horizontally"));
                assertNotNull("Filtered Philippine Eagle is accessible",
                    described(root,"Philippine Eagle,"));
                capture(a,"rc62-"+preset()+"-search.png");
                EditText search=(EditText)described(root,
                    "Search illustrated offline species and habitat accounts");
                search.setText("zzzz_unrecorded_wildlife_q91");
            });
            try{Thread.sleep(350);}catch(InterruptedException e){Thread.currentThread().interrupt();}
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            scenario.onActivity(a->{
                assertNotNull("Unknown search gives a neutral empty state",
                    heading(a.getWindow().getDecorView(),"No matches"));
                EditText search=(EditText)described(a.getWindow().getDecorView(),
                    "Search illustrated offline species and habitat accounts");
                search.setText("");
            });
        }
    }
}
