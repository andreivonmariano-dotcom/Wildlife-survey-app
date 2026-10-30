package org.phwildlife.survey;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Real Activity screen 3 checks on a clean/disposable AVD; no user survey starts. */
@RunWith(AndroidJUnit4.class)
public final class Rc54SurveySetupInstrumentedTest {
    private static View findDescription(View v,String prefix){
        if(String.valueOf(v.getContentDescription()).startsWith(prefix))return v;
        if(v instanceof ViewGroup){
            ViewGroup g=(ViewGroup)v;
            for(int i=0;i<g.getChildCount();i++){
                View result=findDescription(g.getChildAt(i),prefix);
                if(result!=null)return result;
            }
        }
        return null;
    }
    private static TextView findText(View v,String value){
        if(v instanceof TextView&&value.equals(((TextView)v).getText().toString()))
            return (TextView)v;
        if(v instanceof ViewGroup){
            ViewGroup g=(ViewGroup)v;
            for(int i=0;i<g.getChildCount();i++){
                TextView result=findText(g.getChildAt(i),value);
                if(result!=null)return result;
            }
        }
        return null;
    }
    private static Intent home(){
        Intent i=new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(),
            WelcomeActivity.class);
        i.putExtra("show_menu",true);
        return i;
    }
    private static void enterSetup(WelcomeActivity a,String group){
        View root=a.getWindow().getDecorView();
        View field=findDescription(root,"Open Fieldwork.");
        assertNotNull(field);field.performClick();
        View choice=findDescription(root,"Open "+group+".");
        assertNotNull(choice);choice.performClick();
    }
    private static void checkBase(WelcomeActivity a,String profile){
        View root=a.getWindow().getDecorView();
        assertNotNull(findText(root,profile+" survey"));
        for(String title:new String[]{"Survey method","Project and location","Sampling effort",
                                    "Begin survey","Project","Checklist region"}){
            assertNotNull(profile+" missing "+title,findText(root,title));
        }
        View project=findDescription(root,"Choose survey project");
        View region=findDescription(root,"Choose checklist region");
        assertNotNull(project);assertNotNull(region);
        TextView gps=findText(root,"Continue to GPS");
        assertNotNull("Pinned GPS action",gps);
        assertTrue("GPS remains clickable",gps.isClickable());
        assertTrue("GPS text has minimum touch",gps.getMinimumHeight()>=48);
        assertNotNull(findDescription(root,"Show additional survey details"));
        assertNotNull(findDescription(root,"Show optional study rules"));
    }
    @Test public void birdsHaveVisibleMethodLabelsAndBearingOnlyWhenRelevant(){
        try(ActivityScenario<WelcomeActivity> sc=ActivityScenario.launch(home())){
            sc.onActivity(a->{
                enterSetup(a,"Birds");
                checkBase(a,"Birds");
                View root=a.getWindow().getDecorView();
                TextView bearing=findText(root,"Transect magnetic bearing (0–359°)");
                assertNotNull(bearing);
                assertEquals(View.GONE,bearing.getVisibility());
                TextView point=findText(root,"Point count duration (minutes)");
                assertNotNull(point);assertEquals(View.VISIBLE,point.getVisibility());
                TextView line=findText(root,"Line transect");
                assertNotNull(line);assertTrue(line instanceof RadioButton);
                ((RadioButton)line).setChecked(true);
                assertEquals(View.VISIBLE,bearing.getVisibility());
                assertEquals(View.GONE,point.getVisibility());
                assertNotNull(findText(root,"Planned route length (km)"));
            });
        }
    }
    @Test public void herpetofaunaMethodAndAdvancedInputsRemainReachable(){
        try(ActivityScenario<WelcomeActivity> sc=ActivityScenario.launch(home())){
            sc.onActivity(a->{
                enterSetup(a,"Herpetofauna");
                checkBase(a,"Herpetofauna");
                View root=a.getWindow().getDecorView();
                assertNotNull(findText(root,"Visual encounter (VES)"));
                TextView calls=findText(root,"Amphibian calling");
                assertNotNull(calls);
                ((RadioButton)calls).setChecked(true);
                assertEquals(View.VISIBLE,
                    findText(root,"Sampling duration (minutes)").getVisibility());
                View toggle=findDescription(root,"Show additional survey details");
                assertNotNull(toggle);toggle.performClick();
                assertNotNull(findDescription(root,"Hide additional survey details"));
                assertNotNull(findText(root,"Observer (optional)") == null
                  ?findDescription(root,"Hide additional survey details")
                  :findText(root,"Observer (optional)"));
            });
        }
    }
    @Test public void mammalsKeepBasicLogLabelAndOptionalStudyRules(){
        try(ActivityScenario<WelcomeActivity> sc=ActivityScenario.launch(home())){
            sc.onActivity(a->{
                enterSetup(a,"Mammals");
                checkBase(a,"Mammals");
                View root=a.getWindow().getDecorView();
                assertNotNull(findText(root,"Bat acoustic survey · basic log"));
                assertNotNull(findText(root,"Line transect"));
                View toggle=findDescription(root,"Show optional study rules");
                assertNotNull(toggle);toggle.performClick();
                assertNotNull(findDescription(root,"Hide optional study rules"));
                assertNotNull(findDescription(root,"Select optional study protocol requirements"));
            });
        }
    }
}
