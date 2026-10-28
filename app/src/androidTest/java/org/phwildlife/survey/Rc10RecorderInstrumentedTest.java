package org.phwildlife.survey;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import static org.junit.Assert.*;

/** Runs only in an empty, disposable Android emulator after backup recovery checks. */
@RunWith(AndroidJUnit4.class)
public final class Rc10RecorderInstrumentedTest {
    private static void find(View root,ArrayList<View> views){
        views.add(root);
        if(root instanceof ViewGroup){
            ViewGroup group=(ViewGroup)root;
            for(int i=0;i<group.getChildCount();i++)find(group.getChildAt(i),views);
        }
    }

    @Test public void liveSurveyRetainsEndAndMarkActionsAndReadableDock()throws Exception{
        Intent intent=new Intent(ApplicationProvider.getApplicationContext(),MainActivity.class);
        intent.putExtra("create_new",true);
        intent.putExtra("survey_profile","Birds");
        intent.putExtra("survey_method","opportunistic");
        intent.putExtra("observer_name","LIKAS-RC10-EMULATOR-QA");
        try(ActivityScenario<MainActivity> test=ActivityScenario.launch(intent)){
            test.onActivity(activity->{
                View root=activity.getWindow().getDecorView();
                ArrayList<View> views=new ArrayList<>();
                find(root,views);
                Button end=null,save=null,mark=null,review=null;
                TextView details=null;
                for(View view:views){
                    if(view instanceof Button){
                        Button b=(Button)view;
                        String name=b.getText().toString();
                        if("End Survey".equals(name))end=b;
                        if("Save Observation".equals(name))save=b;
                        if("Mark detection".equals(name))mark=b;
                        if("Review Records".equals(name))review=b;
                    }
                    if(view instanceof TextView){
                        TextView v=(TextView)view;
                        if(v.getText().toString().contains("More observation details"))
                            details=v;
                    }
                }
                assertNotNull("End Survey action exists",end);
                assertNotNull("Save Observation action exists",save);
                assertNotNull("Mark detection action exists",mark);
                assertNotNull("Review Records action exists",review);
                assertNotNull("Optional evidence controls exist",details);
                assertTrue("End Survey must remain visible above the keyboard",end.isShown());
                assertTrue("End Survey must remain enabled during an active survey",end.isEnabled());
                assertTrue("Mark detection describes GPS provenance",
                    mark.getContentDescription().toString().contains("fresh observer GPS"));
                assertTrue("Optional disclosure is accessible",
                    details.getContentDescription().toString().contains("Show optional"));
                assertTrue("Recorder has measured width",root.getWidth()>0);
                assertTrue("Recorder has measured height",root.getHeight()>0);
                Bitmap shot=Bitmap.createBitmap(root.getWidth(),root.getHeight(),
                    Bitmap.Config.ARGB_8888);
                root.draw(new Canvas(shot));
                File out=new File(activity.getFilesDir(),"rc10_recorder_screen.png");
                try(FileOutputStream stream=new FileOutputStream(out)){
                    assertTrue(shot.compress(Bitmap.CompressFormat.PNG,90,stream));
                }catch(Exception error){throw new IllegalStateException(error);}
                shot.recycle();
            });
        }
    }
}
