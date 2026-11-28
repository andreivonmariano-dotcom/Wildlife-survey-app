package org.phwildlife.survey;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Environment;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import static org.junit.Assert.*;

/** Actual Android shell navigation, access, Back and large-font screenshot coverage. */
@RunWith(AndroidJUnit4.class)
public final class ApoShellNavigationInstrumentedTest {
    private static String preset(){
        return InstrumentationRegistry.getArguments().getString("preset","p360f100");
    }
    private static void idle(){
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        SystemClock.sleep(200);
    }
    private static boolean hasText(View root,String value){
        if(root instanceof TextView &&
            ((TextView)root).getText().toString().contains(value))return true;
        if(root instanceof ViewGroup){
            ViewGroup parent=(ViewGroup)root;
            for(int i=0;i<parent.getChildCount();i++)
                if(hasText(parent.getChildAt(i),value))return true;
        }
        return false;
    }
    private static View nav(Activity a,String key){
        return a.getWindow().getDecorView().findViewWithTag("apo_nav_"+key);
    }
    private static void select(Activity a,String key){
        View target=nav(a,key);
        assertNotNull("Missing top-level navigation "+key,target);
        assertTrue(target.isClickable());
        assertTrue(target.isFocusable());
        assertTrue(target.getHeight()>=ApoVisualSystem.dp(a,48));
        target.performClick();
    }
    /** All five peer destinations must remain legible at enlarged system text. */
    private static void assertNavLabelsFit(Activity a){
        android.content.res.Configuration cfg=a.getResources().getConfiguration();
        boolean enlarged=cfg.screenWidthDp<350||cfg.fontScale>=1.3f;
        String[] tags={"fieldwork","atlas","map","research","data"};
        String[] expected=enlarged?
            new String[]{"Field","Atlas","Map","Papers","Data"}:
            new String[]{"Fieldwork","Atlas","Map","Research","My Data"};
        for(int i=0;i<tags.length;i++){
            View view=nav(a,tags[i]);
            assertTrue("Navigation tab missing: "+tags[i],view instanceof TextView);
            TextView text=(TextView)view;
            assertEquals("Unexpected tab label: "+tags[i],expected[i],text.getText().toString());
            assertNotNull(text.getLayout());
            assertTrue(text.getLineCount()<=2);
            for(int line=0;line<text.getLineCount();line++)
                assertEquals("Clipped bottom tab "+tags[i],0,text.getLayout().getEllipsisCount(line));
            assertTrue("Target width "+tags[i],text.getWidth()>=ApoVisualSystem.dp(a,48));
        }
    }
    private static void capture(Activity a,String label){
        View root=a.getWindow().getDecorView();
        assertTrue(root.getWidth()>50&&root.getHeight()>50);
        Bitmap b=Bitmap.createBitmap(root.getWidth(),root.getHeight(),Bitmap.Config.ARGB_8888);
        try{
            root.draw(new Canvas(b));
            File dir=new File(a.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "APO_SHELL_REVIEW");
            assertTrue(dir.isDirectory()||dir.mkdirs());
            File target=new File(dir,preset()+"-"+label+".png");
            try(FileOutputStream out=new FileOutputStream(target)){
                assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,out));
            }catch(Exception e){throw new AssertionError(e);}
            assertTrue("Screenshot empty: "+label,target.length()>2500);
        }finally{b.recycle();}
    }

    private static void assertHomeShortcutsFit(Activity a){
        View root=a.getWindow().getDecorView();
        View map=root.findViewWithTag("apo_home_shortcut_Offline Map");
        View research=root.findViewWithTag("apo_home_shortcut_Research Library");
        assertNotNull(map);assertNotNull(research);
        for(View view:new View[]{map,research}){
            assertTrue(view.isClickable()&&view.isFocusable());
            assertTrue(view.getHeight()>=ApoVisualSystem.dp(a,48));
            assertTrue(view.getWidth()>=ApoVisualSystem.dp(a,48));
            TextView label=(TextView)((ViewGroup)view).getChildAt(0);
            assertNotNull(label.getLayout());
            for(int i=0;i<label.getLineCount();i++)
                assertEquals("Shortcut text clipped",0,label.getLayout().getEllipsisCount(i));
        }
        android.content.res.Configuration cfg=a.getResources().getConfiguration();
        boolean stacked=cfg.screenWidthDp<360||cfg.fontScale>=1.3f;
        assertEquals(stacked?android.widget.LinearLayout.VERTICAL:android.widget.LinearLayout.HORIZONTAL,
            ((android.widget.LinearLayout)map.getParent()).getOrientation());
        assertSame(map.getParent(),research.getParent());
    }

    @Test public void shellHomeFieldworkDataAndBack(){
        Intent launch=new Intent(ApplicationProvider.getApplicationContext(),WelcomeActivity.class)
            .putExtra("show_menu",true);
        try(ActivityScenario<WelcomeActivity> scenario=ActivityScenario.launch(launch)){
            idle();
            scenario.onActivity(a->{
                assertTrue(hasText(a.getWindow().getDecorView(),"Start Survey"));
                assertTrue(hasText(a.getWindow().getDecorView(),"Living Atlas"));
                assertTrue(hasText(a.getWindow().getDecorView(),"Research Library"));
                assertTrue(hasText(a.getWindow().getDecorView(),"Offline Map"));
                assertFalse(hasText(a.getWindow().getDecorView(),"BioTools"));
                for(String name:new String[]{"fieldwork","atlas","map","research","data"}){
                    assertNotNull(nav(a,name));
                    assertFalse(nav(a,name).isSelected());
                }
                assertNavLabelsFit(a);
                assertHomeShortcutsFit(a);
                capture(a,"home");
                select(a,"fieldwork");
            });
            idle();
            scenario.onActivity(a->{
                assertTrue(hasText(a.getWindow().getDecorView(),"Birds"));
                assertFalse(hasText(a.getWindow().getDecorView(),"New survey"));
                assertTrue(hasText(a.getWindow().getDecorView(),"Herpetofauna"));
                assertTrue(hasText(a.getWindow().getDecorView(),"Amphibians and reptiles"));
                assertTrue(nav(a,"fieldwork").isSelected());
                capture(a,"fieldwork");
                select(a,"data");
            });
            idle();
            scenario.onActivity(a->{
                assertTrue(hasText(a.getWindow().getDecorView(),"Survey history"));
                assertTrue(hasText(a.getWindow().getDecorView(),"Analysis & exports"));
                assertTrue(nav(a,"data").isSelected());
                capture(a,"my-data");
                a.onBackPressed();
            });
            idle();
            scenario.onActivity(a->{
                assertTrue(hasText(a.getWindow().getDecorView(),"Start Survey"));
                assertFalse(nav(a,"fieldwork").isSelected());
                assertFalse(nav(a,"data").isSelected());
                capture(a,"back-home");
            });
        }
    }

    @Test public void setupHidesTabsAndBackRestoresFieldwork(){
        Intent launch=new Intent(ApplicationProvider.getApplicationContext(),WelcomeActivity.class)
            .putExtra("show_menu",true);
        try(ActivityScenario<WelcomeActivity> scenario=ActivityScenario.launch(launch)){
            idle();
            scenario.onActivity(a->{
                try{
                    Method method=a.getClass().getDeclaredMethod("setup",String.class);
                    method.setAccessible(true);
                    method.invoke(a,"Birds");
                }catch(Exception ex){throw new AssertionError(ex);}
            });
            idle();
            scenario.onActivity(a->{
                View navbar=a.getWindow().getDecorView().findViewWithTag(
                    "apo_shell_navigation");
                assertNotNull(navbar);
                assertEquals(View.GONE,navbar.getVisibility());
                assertTrue(hasText(a.getWindow().getDecorView(),"Continue to GPS"));
                capture(a,"setup");
                a.onBackPressed();
            });
            idle();
            scenario.onActivity(a->{
                assertTrue(hasText(a.getWindow().getDecorView(),"Birds"));
                assertFalse(hasText(a.getWindow().getDecorView(),"New survey"));
                assertTrue(nav(a,"fieldwork").isSelected());
                capture(a,"back-fieldwork");
            });
        }
    }

    @Test public void atlasHasPersistentSharedNavigation(){
        Intent intent=new Intent(ApplicationProvider.getApplicationContext(),
            LikasLibraryActivity.class);
        try(ActivityScenario<LikasLibraryActivity> scenario=ActivityScenario.launch(intent)){
            idle();
            scenario.onActivity(a->{
                assertNotNull(nav(a,"map"));
                assertNotNull(nav(a,"research"));
                assertNotNull(nav(a,"fieldwork"));
                assertNotNull(nav(a,"atlas"));
                assertNotNull(nav(a,"data"));
                assertTrue(nav(a,"atlas").isSelected());
                assertNavLabelsFit(a);
                assertTrue(hasText(a.getWindow().getDecorView(),"Living Atlas"));
                capture(a,"atlas");
                select(a,"atlas");
                assertFalse(a.isFinishing());
            });
        }
    }

    @Test public void sourceFactKeepsAbbreviatedMountainNames(){
        String paragraph="Specimens discussed from Mt. Apo Natural Park in Mindanao. "
            +"The species occurs near forest streams.";
        assertEquals("Specimens discussed from Mt. Apo Natural Park in Mindanao.",
            ApoSpeciesOfDay.firstSourceSentence(paragraph));
    }

}
