package org.phwildlife.survey;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import static org.junit.Assert.*;

/** Real Android 320dp UI and search smoke test. Run on disposable emulator only. */
@RunWith(AndroidJUnit4.class)
public final class SpeciesLibraryInstrumentedTest {
    private static void traverse(View v,ArrayList<View> out){
        out.add(v);
        if(v instanceof ViewGroup){
            ViewGroup group=(ViewGroup)v;
            for(int i=0;i<group.getChildCount();i++)traverse(group.getChildAt(i),out);
        }
    }
    @Test public void testSourceCatalogFiltersAnd320dpAccessibility()throws Exception{
        try(ActivityScenario<Rc7SpeciesLibraryActivity> scenario=
                ActivityScenario.launch(Rc7SpeciesLibraryActivity.class)){
            final long[] start={SystemClock.elapsedRealtime()};
            scenario.onActivity(a->{
                ArrayList<View> all=new ArrayList<>();
                View root=a.getWindow().getDecorView();
                traverse(root,all);
                EditText edit=null;
                int spinners=0;
                for(View v:all){
                    if(v instanceof EditText)edit=(EditText)v;
                    if(v instanceof Spinner){
                        spinners++;
                        assertTrue("Spinner accessible",v.getContentDescription()!=null
                            &&v.getContentDescription().length()>0);
                    }
                }
                assertEquals("Taxon and content filters",2,spinners);
                assertNotNull("Search field visible",edit);
                assertTrue(edit.getHint().toString().contains("former name"));
                assertTrue("96 priority records present",Rc7FieldAccounts.read(a).length()==96);
                assertTrue("Cited biology notes present",Rc7LiteratureNotes.count(a)>=80);
                edit.setText("Pithecophaga");
            });
            Thread.sleep(800);
            scenario.onActivity(a->{
                ArrayList<View> all=new ArrayList<>();
                traverse(a.getWindow().getDecorView(),all);
                boolean results=false,species=false,action=false;
                for(View v:all){
                    if(v instanceof TextView){
                        String label=((TextView)v).getText().toString();
                        if(label.contains("matching source records"))results=true;
                        if(label.contains("Pithecophaga")||label.contains("Philippine Eagle"))species=true;
                    }
                    if(v.isClickable()&&v.isFocusable()&&v.getContentDescription()!=null
                        &&v.getContentDescription().toString().contains("Open species reference"))
                        action=true;
                }
                assertTrue("Search returns count",results);
                assertTrue("Scientific or common name visible",species);
                assertTrue("Species card has accessible action",action);
                View root=a.getWindow().getDecorView();
                if(root.getWidth()>0&&root.getHeight()>0){
                    Bitmap capture=Bitmap.createBitmap(root.getWidth(),root.getHeight(),
                        Bitmap.Config.ARGB_8888);
                    root.draw(new Canvas(capture));
                    File path=new File(a.getFilesDir(),"rc7_library_screen.png");
                    try(FileOutputStream stream=new FileOutputStream(path)){
                        assertTrue(capture.compress(Bitmap.CompressFormat.PNG,90,stream));
                    }catch(Exception e){throw new IllegalStateException(e);}
                    capture.recycle();
                }
            });
            long duration=SystemClock.elapsedRealtime()-start[0];
            assertTrue("Species browse test stalled for over 20 seconds",duration<20000);
        }
    }
}
