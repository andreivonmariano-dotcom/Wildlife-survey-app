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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.OutputStream;
import static org.junit.Assert.*;

/** Main Library phone density regression motivated by the user's physical-device screenshot. */
@RunWith(AndroidJUnit4.class)
public final class Rc64LibraryPhoneIconsInstrumentedTest {
    private static final String[] NAMES={"Species","Habitats","Similar species",
        "Photos","Conservation","Sources","Habitat links"};
    private String preset(){return InstrumentationRegistry.getArguments().getString("preset","p360d160");}
    private View described(View root,String label){
        CharSequence c=root.getContentDescription();
        if(c!=null&&c.toString().startsWith(label))return root;
        if(root instanceof ViewGroup){
            ViewGroup g=(ViewGroup)root;
            for(int i=0;i<g.getChildCount();i++){
                View result=described(g.getChildAt(i),label);
                if(result!=null)return result;
            }
        }
        return null;
    }
    private ScrollView scroll(View root){
        if(root instanceof ScrollView)return (ScrollView)root;
        if(root instanceof ViewGroup){
            ViewGroup g=(ViewGroup)root;
            for(int i=0;i<g.getChildCount();i++){
                ScrollView s=scroll(g.getChildAt(i));
                if(s!=null)return s;
            }
        }
        return null;
    }
    private void capture(Activity a,String name){
        View root=a.getWindow().getDecorView();
        Bitmap bmp=Bitmap.createBitmap(root.getWidth(),root.getHeight(),Bitmap.Config.ARGB_8888);
        root.draw(new Canvas(bmp));
        ContentValues values=new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME,name);
        values.put(MediaStore.Images.Media.MIME_TYPE,"image/png");
        values.put(MediaStore.Images.Media.RELATIVE_PATH,Environment.DIRECTORY_PICTURES+"/LIKAS_RC64");
        values.put(MediaStore.Images.Media.IS_PENDING,1);
        Uri uri=a.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
        assertNotNull(uri);
        try(OutputStream out=a.getContentResolver().openOutputStream(uri)){
            assertNotNull(out);
            assertTrue(bmp.compress(Bitmap.CompressFormat.PNG,100,out));
        }catch(Exception e){throw new IllegalStateException(e);}
        ContentValues done=new ContentValues();done.put(MediaStore.Images.Media.IS_PENDING,0);
        a.getContentResolver().update(uri,done,null,null);
        bmp.recycle();
    }
    private int visibleArtworkExtent(ImageView image){
        Bitmap bmp=Bitmap.createBitmap(image.getWidth(),image.getHeight(),Bitmap.Config.ARGB_8888);
        image.draw(new Canvas(bmp));
        int left=bmp.getWidth(),right=-1,top=bmp.getHeight(),bottom=-1;
        for(int y=0;y<bmp.getHeight();y++){
            for(int x=0;x<bmp.getWidth();x++){
                if((bmp.getPixel(x,y)>>>24)>=192){
                    left=Math.min(left,x);right=Math.max(right,x);
                    top=Math.min(top,y);bottom=Math.max(bottom,y);
                }
            }
        }
        int visible=Math.min(right-left+1,bottom-top+1);
        bmp.recycle();
        return Math.max(0,visible);
    }
    private void checkIcon(Activity a,String label){
        View card=described(a.getWindow().getDecorView(),"Open "+label+".");
        assertNotNull("Library destination "+label,card);
        float density=a.getResources().getDisplayMetrics().density;
        assertTrue("Touch height "+label,card.getHeight()>=48*density-1);
        assertTrue("Focusable "+label,card.isFocusable());
        assertTrue("Clickable "+label,card.isClickable());
        assertTrue("Card row "+label,card instanceof LinearLayout);
        View leading=((LinearLayout)card).getChildAt(0);
        assertTrue("Icon plate "+label,leading instanceof FrameLayout);
        FrameLayout plate=(FrameLayout)leading;
        assertEquals("Only one icon "+label,1,plate.getChildCount());
        assertTrue("Image view "+label,plate.getChildAt(0) instanceof ImageView);
        ImageView image=(ImageView)plate.getChildAt(0);
        assertNotNull("Drawable "+label,image.getDrawable());
        assertTrue("50dp plate "+label,Math.abs(plate.getWidth()-Math.round(50*density))<=1);
        assertTrue("50dp image "+label,Math.abs(image.getWidth()-Math.round(50*density))<=1);
        if(label.equals("Conservation")||label.equals("Habitat links")||
           label.equals("Photos")||label.equals("Sources")){
            assertEquals("Non-crop high-density scaling "+label,
                ImageView.ScaleType.FIT_CENTER,image.getScaleType());
            assertTrue("Artwork occupies at least 75% of icon plate "+label,
                visibleArtworkExtent(image)>=Math.round(50*density*.75f));
        }
        if(label.equals("Conservation")||label.equals("Habitat links"))
            assertEquals("Display derivative is 256 pixels",256,
                image.getDrawable().getIntrinsicWidth());
        if(label.equals("Photos")||label.equals("Sources"))
            assertEquals("Library display derivative is 256 pixels",256,
                image.getDrawable().getIntrinsicWidth());
    }
    @Test public void allSevenIconsRenderAtPhoneDensitiesWithoutCrop(){
        Intent entry=new Intent(ApplicationProvider.getApplicationContext(),LikasLibraryActivity.class);
        try(ActivityScenario<LikasLibraryActivity> scenario=ActivityScenario.launch(entry)){
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            scenario.onActivity(a->{
                ScrollView list=scroll(a.getWindow().getDecorView());
                assertNotNull(list);
                for(String label:NAMES)checkIcon(a,label);
                capture(a,"rc64-"+preset()+"-top.png");
                list.fullScroll(View.FOCUS_DOWN);
            });
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            scenario.onActivity(a->{
                ScrollView list=scroll(a.getWindow().getDecorView());
                list.scrollTo(0,Math.max(0,list.getChildAt(0).getHeight()-list.getHeight()));
                View last=described(a.getWindow().getDecorView(),"Open Habitat links.");
                assertNotNull(last);
                android.graphics.Rect visible=new android.graphics.Rect();
                assertTrue("Last Library card remains reachable",last.getGlobalVisibleRect(visible));
                capture(a,"rc64-"+preset()+"-bottom.png");
            });
        }
    }
}
