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
    @Test public void mainMenuShowsFieldFirstAndTwoLiveReferenceEntries(){
        Intent intent=new Intent(InstrumentationRegistry.getInstrumentation()
            .getTargetContext(),WelcomeActivity.class);
        intent.putExtra("show_menu",true);
        try(ActivityScenario<WelcomeActivity> scenario=ActivityScenario.launch(intent)){
            scenario.onActivity(a->{
                View root=a.getWindow().getDecorView();
                View field=find(root,"Start Survey. Choose a method and begin field recording");
                View atlas=find(root,"Open Living Atlas. Species, habitats and the Greater Mindanao field guide");
                View papers=find(root,"Open Research Library. Search the offline source bibliography");
                View settings=find(root,"Open settings");
                assertNotNull("Start Survey",field);
                assertNotNull("Living Atlas",atlas);
                assertNotNull("Research Library",papers);
                assertNotNull("Settings",settings);
                assertTrue(field.isClickable()&&atlas.isClickable()&&papers.isClickable()&&settings.isClickable());
                float density=a.getResources().getDisplayMetrics().density;
                assertTrue("Field CTA should be visible",field.getHeight()>0);
                if(a.getResources().getConfiguration().fontScale<1.30f){
                    assertTrue("Field CTA must remain compact",field.getHeight()/density<170f);
                    assertTrue("Atlas tile must remain compact",atlas.getHeight()/density<170f);
                }
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
                View field=find(root,"Start Survey. Choose a method and begin field recording");
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
