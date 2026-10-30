package org.phwildlife.survey;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import static org.junit.Assert.*;

/** Synthetic new survey on fresh CI AVD. No owner data, detection or location is fabricated. */
@RunWith(AndroidJUnit4.class)
public final class Rc56RecorderUiInstrumentedTest {
    private static void all(View root,ArrayList<View> out){
        out.add(root);
        if(root instanceof ViewGroup){
            ViewGroup group=(ViewGroup)root;
            for(int n=0;n<group.getChildCount();n++)all(group.getChildAt(n),out);
        }
    }
    private static Button button(View root,String name){
        ArrayList<View> views=new ArrayList<>();all(root,views);
        for(View v:views)if(v instanceof Button
                &&name.equals(((Button)v).getText().toString()))return (Button)v;
        return null;
    }
    private static View description(View root,String prefix){
        ArrayList<View> views=new ArrayList<>();all(root,views);
        for(View v:views)if(v.getContentDescription()!=null &&
                v.getContentDescription().toString().startsWith(prefix))return v;
        return null;
    }
    private static void capture(MainActivity a,String name){
        View v=a.getWindow().getDecorView();
        Bitmap image=Bitmap.createBitmap(v.getWidth(),v.getHeight(),Bitmap.Config.ARGB_8888);
        v.draw(new Canvas(image));
        try(FileOutputStream out=new FileOutputStream(new File(a.getFilesDir(),name))){
            assertTrue(image.compress(Bitmap.CompressFormat.PNG,95,out));
        }catch(Exception ex){throw new IllegalStateException(ex);}
        image.recycle();
    }
    private static void footer(MainActivity a){
        View root=a.getWindow().getDecorView();
        Button save=button(root,"Save Observation"),
            end=button(root,"End Survey"),review=button(root,"Review Records");
        assertNotNull(save);assertNotNull(end);assertNotNull(review);
        assertTrue("End Survey enabled",end.isEnabled());
        assertTrue("End Survey shown",end.isShown());
        assertTrue("Save shown",save.isShown());
        assertTrue("Review shown",review.isShown());
        assertTrue(end.getContentDescription().toString().contains("Not Counted"));
        Rect r=new Rect();
        assertTrue("End Survey visible on screen",end.getGlobalVisibleRect(r));
        assertTrue("No End Survey vertical clipping",r.height()>=end.getHeight()-2);
        assertTrue("End Survey at least 48dp",end.getHeight()>=
            48*a.getResources().getDisplayMetrics().density-1);
    }
    @Test public void liveRecorderAccessibleAtMultipleSizesAndGroups(){
        String profile=InstrumentationRegistry.getArguments().getString("profile","Birds");
        assertTrue(profile,profile.equals("Birds")||profile.equals("Herpetofauna")
            ||profile.equals("Mammals"));
        Intent intent=new Intent(ApplicationProvider.getApplicationContext(),MainActivity.class);
        intent.putExtra("create_new",true);
        intent.putExtra("survey_profile",profile);
        intent.putExtra("survey_method","opportunistic");
        intent.putExtra("start_without_gps",true);
        intent.putExtra("observer_name","RC56-DISPOSABLE-EMULATOR");
        try(ActivityScenario<MainActivity> sc=ActivityScenario.launch(intent)){
            sc.onActivity(a->{
                View root=a.getWindow().getDecorView();
                footer(a);
                assertNotNull(description(root,"Export research ZIP"));
                assertNotNull(description(root,"Back to Home"));
                assertNotNull(description(root,"Choose observation detection type"));
                assertNotNull(description(root,"Decrease individuals or evidence count"));
                assertNotNull(description(root,"Increase individuals or evidence count"));
                assertNotNull(description(root,"Choose an unidentified taxon label"));
                assertNotNull(description(root,"Show optional haptic interval alert"));
                assertNotNull(description(root,"Mark sighting time"));
                assertNotNull("GPS indicator should have a spoken fix state",
                    description(root,"GPS"));
                if(profile.equals("Herpetofauna")){
                    View selected=description(root,"Reptiles observation group, selected");
                    assertNotNull(selected);
                    View amphibians=description(root,"Amphibians observation group");
                    assertNotNull(amphibians);
                    amphibians.performClick();
                    assertNotNull(description(root,"Amphibians observation group, selected"));
                }
                if(profile.equals("Birds"))
                    assertNotNull(description(root,"Show first-detection position"));
                if(profile.equals("Mammals"))
                    assertNotNull(description(root,"Show additional mammal measurements"));
                capture(a,"rc56-recorder-"+profile+"-top.png");
            });
            sc.onActivity(a->{
                ArrayList<View> all=new ArrayList<>();all(a.getWindow().getDecorView(),all);
                ScrollView scroll=null;
                for(View v:all)if(v instanceof ScrollView){scroll=(ScrollView)v;break;}
                assertNotNull("Observation form scroll",scroll);
                int max=Math.max(0,scroll.getChildAt(0).getHeight()-scroll.getHeight());
                assertTrue("Observation form is scrollable",max>0);
                scroll.scrollTo(0,Math.min(max,Math.round(max*0.72f)));
            });
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            sc.onActivity(a->{
                footer(a);
                capture(a,"rc56-recorder-"+profile+"-scrolled.png");
            });
        }
    }
}
