package org.phwildlife.survey;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Environment;
import android.os.SystemClock;
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
import java.io.File;
import java.io.FileOutputStream;
import static org.junit.Assert.*;

/** Real emulator captures of native Living Atlas screens and interactions. */
@RunWith(AndroidJUnit4.class)
public final class LivingAtlasExplorerInstrumentedTest {
    private static String preset() {
        return InstrumentationRegistry.getArguments().getString("preset","p360f100");
    }
    private static void settle() {
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        SystemClock.sleep(220);
    }
    private static ScrollView findScroll(View root) {
        if(root instanceof ScrollView)return (ScrollView)root;
        if(root instanceof ViewGroup) {
            ViewGroup p=(ViewGroup)root;
            for(int i=0;i<p.getChildCount();i++){
                ScrollView s=findScroll(p.getChildAt(i));
                if(s!=null)return s;
            }
        }
        return null;
    }
    private static boolean hasText(View root,String text) {
        if(root instanceof TextView &&
            ((TextView)root).getText().toString().contains(text))return true;
        if(root instanceof ViewGroup) {
            ViewGroup p=(ViewGroup)root;
            for(int i=0;i<p.getChildCount();i++)
                if(hasText(p.getChildAt(i),text))return true;
        }
        return false;
    }
    private static void capture(Activity activity,String name,int percent) {
        ScrollView scroll=findScroll(activity.getWindow().getDecorView());
        if(scroll!=null && scroll.getChildCount()>0) {
            int range=Math.max(0,scroll.getChildAt(0).getHeight()-scroll.getHeight());
            scroll.scrollTo(0,range*percent/100);
        }
        View root=activity.getWindow().getDecorView();
        assertTrue(root.getWidth()>50&&root.getHeight()>50);
        Bitmap bitmap=Bitmap.createBitmap(root.getWidth(),root.getHeight(),
            Bitmap.Config.ARGB_8888);
        try {
            root.draw(new Canvas(bitmap));
            File dir=new File(activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "LIVING_ATLAS_REVIEW");
            assertTrue(dir.isDirectory()||dir.mkdirs());
            File file=new File(dir,preset()+"-"+name+".png");
            try(FileOutputStream output=new FileOutputStream(file)){
                assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG,100,output));
            }catch(Exception ex){throw new AssertionError(ex);}
            assertTrue(file.length()>2500);
        }finally{bitmap.recycle();}
    }

    @Test public void aExploreSpeciesNavigateAndCapture() {
        Intent launch=new Intent(ApplicationProvider.getApplicationContext(),
            LivingAtlasExploreActivity.class);
        try(ActivityScenario<LivingAtlasExploreActivity> s=
                ActivityScenario.launch(launch)) {
            settle();
            s.onActivity(a->{
                assertTrue(hasText(a.getWindow().getDecorView(),"Explore the living atlas"));
                capture(a,"explore",0);
                a.showSpecies("Pithecophaga jefferyi");
            });
            settle();
            s.onActivity(a->{
                assertTrue(hasText(a.getWindow().getDecorView(),"Philippine Eagle"));
                assertTrue(hasText(a.getWindow().getDecorView(),"Taxonomic connections"));
                assertTrue(hasText(a.getWindow().getDecorView(),"Habitat context"));
                assertTrue(hasText(a.getWindow().getDecorView(),"Research sources"));
                capture(a,"eagle-top",0);
                capture(a,"eagle-bottom",100);
                a.onBackPressed();
            });
            settle();
            s.onActivity(a->{
                assertTrue(hasText(a.getWindow().getDecorView(),"Explore the living atlas"));
                capture(a,"back-to-explore",0);
            });
        }
    }
}
