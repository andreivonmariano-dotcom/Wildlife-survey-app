package org.phwildlife.survey;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Screen 3. Run against an empty, disposable Android emulator only. */
@RunWith(AndroidJUnit4.class)
public final class Rc54SurveyMethodChooserInstrumentedTest {
    private static View findDescription(View node,String prefix){
        if(String.valueOf(node.getContentDescription()).startsWith(prefix))return node;
        if(node instanceof ViewGroup){
            ViewGroup group=(ViewGroup)node;
            for(int i=0;i<group.getChildCount();i++){
                View child=findDescription(group.getChildAt(i),prefix);
                if(child!=null)return child;
            }
        }
        return null;
    }
    private static RadioGroup findMethods(View node){
        if(node instanceof RadioGroup){
            RadioGroup group=(RadioGroup)node;
            if(group.getChildCount()>0 && group.getChildAt(0) instanceof RadioButton)
                return group;
        }
        if(node instanceof ViewGroup){
            ViewGroup group=(ViewGroup)node;
            for(int i=0;i<group.getChildCount();i++){
                RadioGroup child=findMethods(group.getChildAt(i));
                if(child!=null)return child;
            }
        }
        return null;
    }
    private static boolean hasText(View node,String value){
        if(node instanceof TextView && value.contentEquals(((TextView)node).getText()))
            return true;
        if(node instanceof ViewGroup){
            ViewGroup group=(ViewGroup)node;
            for(int i=0;i<group.getChildCount();i++)
                if(hasText(group.getChildAt(i),value))return true;
        }
        return false;
    }
    private static Intent homeIntent(){
        Intent intent=new Intent(
            InstrumentationRegistry.getInstrumentation().getTargetContext(),
            WelcomeActivity.class);
        intent.putExtra("show_menu",true);
        return intent;
    }
    @Test public void eachTaxonHasReadableIndependentMethodCards(){
        for(String profile:new String[]{"Birds","Herpetofauna","Mammals"}){
            try(ActivityScenario<WelcomeActivity> scenario=
                    ActivityScenario.launch(homeIntent())){
                scenario.onActivity(activity->{
                    View root=activity.getWindow().getDecorView();
                    View fieldwork=findDescription(root,"Open Fieldwork.");
                    assertNotNull("Fieldwork navigation",fieldwork);
                    fieldwork.performClick();
                    View taxon=findDescription(root,"Open "+profile+".");
                    assertNotNull(profile+" survey entry",taxon);
                    taxon.performClick();

                    RadioGroup methods=findMethods(root);
                    assertNotNull(profile+" native RadioGroup",methods);
                    SurveyMethods.Method[] options=SurveyMethods.forProfile(profile);
                    assertEquals(options.length,methods.getChildCount());
                    assertEquals("First option selected by default",1000,
                        methods.getCheckedRadioButtonId());
                    for(int i=0;i<options.length;i++){
                        RadioButton card=(RadioButton)methods.getChildAt(i);
                        assertEquals(1000+i,card.getId());
                        assertTrue(card.getText().toString().contains(options[i].label));
                        assertTrue(card.getText().toString().contains(options[i].description));
                        assertTrue(String.valueOf(card.getContentDescription())
                            .contains(options[i].description));
                        assertTrue("Touch target height",card.getMinimumHeight()>=
                            activity.getResources().getDisplayMetrics().density*80-1);
                    }
                    ((RadioButton)methods.getChildAt(1)).performClick();
                    assertEquals("Selection updates",1001,methods.getCheckedRadioButtonId());
                    assertTrue(((RadioButton)methods.getChildAt(1)).isChecked());
                    assertFalse(((RadioButton)methods.getChildAt(0)).isChecked());
                    assertTrue("Pinned GPS continuation remains",hasText(root,"Continue to GPS"));
                });
            }
        }
    }
}
