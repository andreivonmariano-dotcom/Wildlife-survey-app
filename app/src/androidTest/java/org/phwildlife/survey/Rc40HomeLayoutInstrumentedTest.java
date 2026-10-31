package org.phwildlife.survey;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Screenshot regression: all three destinations are real, content-sized actions. */
@RunWith(AndroidJUnit4.class)
public final class Rc40HomeLayoutInstrumentedTest {
    private static View find(View root,String exact){
        if(exact.equals(String.valueOf(root.getContentDescription())))return root;
        if(root instanceof ViewGroup){
            ViewGroup group=(ViewGroup)root;
            for(int i=0;i<group.getChildCount();i++){
                View found=find(group.getChildAt(i),exact);
                if(found!=null)return found;
            }
        }
        return null;
    }
    private static boolean hasText(View root,String text){
        if(root instanceof TextView &&
            ((TextView)root).getText().toString().contains(text))return true;
        if(root instanceof ViewGroup){
            ViewGroup group=(ViewGroup)root;
            for(int i=0;i<group.getChildCount();i++)
                if(hasText(group.getChildAt(i),text))return true;
        }
        return false;
    }
    @Test public void mainMenuShowsThreeCompactEntries(){
        Intent intent=new Intent(InstrumentationRegistry.getInstrumentation()
            .getTargetContext(),WelcomeActivity.class);
        intent.putExtra("show_menu",true);
        try(ActivityScenario<WelcomeActivity> scenario=ActivityScenario.launch(intent)){
            scenario.onActivity(a->{
                View root=a.getWindow().getDecorView();
                View field=find(root,"Open Fieldwork. Surveys · Data · History");
                View library=find(root,"Open Library. Species · Habitats · Guides");
                View settings=find(root,"Open Settings. Backups · App · Storage");
                assertNotNull("Fieldwork",field);
                assertNotNull("Library",library);
                assertNotNull("Settings",settings);
                float density=a.getResources().getDisplayMetrics().density;
                assertTrue("Fieldwork tile should be rendered",field.getHeight()>0);
                assertTrue("Fieldwork must not occupy most of screen",
                    field.getHeight()/density<170f);
                assertTrue("Library must not occupy most of screen",
                    library.getHeight()/density<170f);
                assertTrue("Settings must not occupy most of screen",
                    settings.getHeight()/density<170f);
                assertTrue(field.isClickable()&&library.isClickable()&&settings.isClickable());
            });
        }
    }
    @Test public void surveySetupDisplaysUnitsForPrefilledNumbers(){
        Intent intent=new Intent(InstrumentationRegistry.getInstrumentation()
            .getTargetContext(),WelcomeActivity.class);
        intent.putExtra("show_menu",true);
        try(ActivityScenario<WelcomeActivity> scenario=ActivityScenario.launch(intent)){
            scenario.onActivity(a->{
                View root=a.getWindow().getDecorView();
                View field=find(root,"Open Fieldwork. Surveys · Data · History");
                assertNotNull(field);
                field.performClick();
                View birds=find(root,"Open Birds. Counts · Transects");
                assertNotNull(birds);
                birds.performClick();
                assertTrue(hasText(root,"Point count duration (minutes)"));
                assertTrue(hasText(root,"Planned route length (km)"));
                assertTrue(hasText(root,"Station spacing"));
                assertTrue(hasText(root,"Continue to GPS"));
            });
        }
    }
}
