package org.phwildlife.survey;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import java.util.ArrayList;
import static org.junit.Assert.*;

/** Main Library screen only. All media and observations on the CI AVD are disposable. */
@RunWith(AndroidJUnit4.class)
public final class Rc59LibraryHubInstrumentedTest {
    private static final String[] LABELS={
        "Open Species.","Open Habitats.","Open Similar species.",
        "Open Photos.","Open Conservation.","Open Sources.","Open Habitat links."
    };
    private static String preset(){
        return InstrumentationRegistry.getArguments().getString("preset","p360f100");
    }
    private static View described(View view,String label){
        CharSequence desc=view.getContentDescription();
        if(desc!=null&&desc.toString().startsWith(label))return view;
        if(view instanceof ViewGroup){
            ViewGroup parent=(ViewGroup)view;
            for(int i=0;i<parent.getChildCount();i++){
                View found=described(parent.getChildAt(i),label);
                if(found!=null)return found;
            }
        }
        return null;
    }
    private static ScrollView scroll(View view){
        if(view instanceof ScrollView)return (ScrollView)view;
        if(view instanceof ViewGroup){
            ViewGroup parent=(ViewGroup)view;
            for(int i=0;i<parent.getChildCount();i++){
                ScrollView found=scroll(parent.getChildAt(i));
                if(found!=null)return found;
            }
        }
        return null;
    }
    private static boolean hasApprovedImage(View v){
        if(v instanceof ImageView){
            ImageView image=(ImageView)v;
            return image.getDrawable()!=null;
        }
        if(v instanceof ViewGroup){
            ViewGroup p=(ViewGroup)v;
            for(int i=0;i<p.getChildCount();i++)
                if(hasApprovedImage(p.getChildAt(i)))return true;
        }
        return false;
    }
    private static void verifyNoEllipsizedText(View root){
        if(root instanceof TextView){
            TextView t=(TextView)root;
            if(t.getLayout()!=null)
                for(int line=0;line<t.getLayout().getLineCount();line++)
                    assertEquals("Clipped label: "+t.getText(),
                        0,t.getLayout().getEllipsisCount(line));
        }
        if(root instanceof ViewGroup){
            ViewGroup p=(ViewGroup)root;
            for(int i=0;i<p.getChildCount();i++)
                verifyNoEllipsizedText(p.getChildAt(i));
        }
    }
    private static void capture(Activity a,String file){
        View decor=a.getWindow().getDecorView();
        assertTrue(decor.getWidth()>0&&decor.getHeight()>0);
        Bitmap bitmap=Bitmap.createBitmap(decor.getWidth(),decor.getHeight(),
            Bitmap.Config.ARGB_8888);
        decor.draw(new Canvas(bitmap));
        ContentValues data=new ContentValues();
        data.put(MediaStore.Images.Media.DISPLAY_NAME,file);
        data.put(MediaStore.Images.Media.MIME_TYPE,"image/png");
        data.put(MediaStore.Images.Media.RELATIVE_PATH,
            Environment.DIRECTORY_PICTURES+"/LIKAS_RC59");
        data.put(MediaStore.Images.Media.IS_PENDING,1);
        Uri target=a.getContentResolver().insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,data);
        assertNotNull("Screenshot output",target);
        try(OutputStream stream=a.getContentResolver().openOutputStream(target)){
            assertNotNull(stream);
            assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG,100,stream));
        }catch(Exception error){throw new IllegalStateException(error);}
        ContentValues done=new ContentValues();
        done.put(MediaStore.Images.Media.IS_PENDING,0);
        a.getContentResolver().update(target,done,null,null);
        bitmap.recycle();
    }
    @Test public void hubA11yIconsAndCompleteBottomNavigation(){
        Intent entry=new Intent(ApplicationProvider.getApplicationContext(),
            LikasLibraryActivity.class);
        try(ActivityScenario<LikasLibraryActivity> scenario=ActivityScenario.launch(entry)){
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            scenario.onActivity(a->{
                View root=a.getWindow().getDecorView();
                ScrollView list=scroll(root);
                assertNotNull("Single main Library scroll",list);
                assertTrue("Library scroll indicator reserved edge",
                    list.getPaddingRight()>=Math.round(8*a.getResources().getDisplayMetrics().density));
                for(String label:LABELS){
                    View card=described(root,label);
                    assertNotNull("Missing destination "+label,card);
                    assertTrue(label+" must be clickable",card.isClickable());
                    assertTrue(label+" must be focusable",card.isFocusable());
                    assertTrue(label+" must have 48dp touch height",
                        card.getHeight()>=48*a.getResources().getDisplayMetrics().density-1);
                    assertTrue(label+" starts inside page horizontal bounds",card.getLeft()>=0);
                    assertTrue(label+" remains inside scroll width",card.getRight()<=list.getWidth());
                }
                View similar=described(root,"Open Similar species.");
                View species=described(root,"Open Species.");
                assertTrue("Similar species uses approved art",hasApprovedImage(similar));
                assertTrue("Species uses approved art",hasApprovedImage(species));
                assertTrue("Library navigation back to Home",
                    described(root,"Back to Home")!=null);
                verifyNoEllipsizedText(root);
                capture(a,"rc59-"+preset()+"-top.png");
            });
            scenario.onActivity(a->{
                ScrollView list=scroll(a.getWindow().getDecorView());
                int max=Math.max(0,list.getChildAt(0).getHeight()-list.getHeight());
                assertTrue("Reference cards must scroll",max>0);
                list.scrollTo(0,max);
            });
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            scenario.onActivity(a->{
                View root=a.getWindow().getDecorView();
                View last=described(root,"Open Habitat links.");
                assertNotNull(last);
                Rect visible=new Rect();
                assertTrue("Last reference action reachable",last.getGlobalVisibleRect(visible));
                assertTrue("Last action fully visible",visible.height()>=last.getHeight()-2);
                verifyNoEllipsizedText(root);
                capture(a,"rc59-"+preset()+"-bottom.png");
            });
        }
    }
}
