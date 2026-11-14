package org.phwildlife.survey;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import java.io.File;
import java.io.FileOutputStream;
import org.json.JSONArray;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public final class ApoResearchBookmarksInstrumentedTest {
    private static boolean includes(View root,String text){
        if(root instanceof TextView&&((TextView)root).getText().toString()
                .contains(text))return true;
        if(root instanceof ViewGroup){
            ViewGroup group=(ViewGroup)root;
            for(int i=0;i<group.getChildCount();i++)
                if(includes(group.getChildAt(i),text))return true;
        }
        return false;
    }
    private static View matching(View root,String label){
        if(root instanceof TextView&&label.equals(
                ((TextView)root).getText().toString()))return root;
        if(root instanceof ViewGroup){
            ViewGroup group=(ViewGroup)root;
            for(int i=0;i<group.getChildCount();i++){
                View found=matching(group.getChildAt(i),label);
                if(found!=null)return found;
            }
        }
        return null;
    }
    private static void screenshot(PhLibraryBibliographyActivity a){
        View root=a.getWindow().getDecorView();
        assertTrue(root.getWidth()>70&&root.getHeight()>70);
        Bitmap image=Bitmap.createBitmap(root.getWidth(),root.getHeight(),
            Bitmap.Config.ARGB_8888);
        try{
            root.draw(new Canvas(image));
            File folder=new File(a.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "APO_SHELL_REVIEW");
            assertTrue(folder.exists()||folder.mkdirs());
            String preset=InstrumentationRegistry.getArguments().getString(
                "preset","p360f100");
            File output=new File(folder,preset+"-saved-references.png");
            try(FileOutputStream stream=new FileOutputStream(output)){
                assertTrue(image.compress(Bitmap.CompressFormat.PNG,100,stream));
            }catch(Exception error){throw new AssertionError(error);}
            assertTrue(output.length()>2500);
        }finally{image.recycle();}
    }
    @Test public void savedFilterUsesOfflineCitationAndPreservesEdition(){
        Context context=ApplicationProvider.getApplicationContext();
        JSONArray sources=PhLibraryBook01.bibliography(context);
        assertEquals(1159,sources.length());
        String citation=sources.optJSONObject(0).optString("text");
        assertFalse(citation.isEmpty());
        ApoReferenceBookmarks.setSaved(context,citation,true);
        try(ActivityScenario<PhLibraryBibliographyActivity> activity=
                ActivityScenario.launch(PhLibraryBibliographyActivity.class)){
            activity.onActivity(a->{
                View root=a.getWindow().getDecorView();
                assertTrue(includes(root,"Saved only (1)"));
                View toggle=matching(root,"Saved only (1)");
                assertNotNull(toggle);
                toggle.performClick();
                assertTrue(includes(root,"1 saved references"));
                assertTrue(includes(root,"All references"));
                screenshot(a);
            });
        }finally{
            ApoReferenceBookmarks.setSaved(context,citation,false);
        }
    }
}
