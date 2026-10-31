package org.phwildlife.survey;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.inputmethod.InputMethodManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Button;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.OutputStream;
import static org.junit.Assert.*;

/** Full Philippine checklist only; disposable Android 14 emulator. */
@RunWith(AndroidJUnit4.class)
public final class Rc63SpeciesChecklistInstrumentedTest {
    private String preset(){
        return InstrumentationRegistry.getArguments().getString("preset","p360f100");
    }
    private static View described(View root,String prefix){
        CharSequence description=root.getContentDescription();
        if(description!=null&&description.toString().startsWith(prefix))return root;
        if(root instanceof ViewGroup){
            ViewGroup group=(ViewGroup)root;
            for(int i=0;i<group.getChildCount();i++){
                View item=described(group.getChildAt(i),prefix);
                if(item!=null)return item;
            }
        }
        return null;
    }
    private static TextView text(View root,String value){
        if(root instanceof TextView&&value.equals(((TextView)root).getText().toString()))
            return (TextView)root;
        if(root instanceof ViewGroup){
            ViewGroup g=(ViewGroup)root;
            for(int i=0;i<g.getChildCount();i++){
                TextView t=text(g.getChildAt(i),value);
                if(t!=null)return t;
            }
        }
        return null;
    }
    private static ScrollView scroll(View root){
        if(root instanceof ScrollView)return (ScrollView)root;
        if(root instanceof ViewGroup){
            ViewGroup g=(ViewGroup)root;
            for(int i=0;i<g.getChildCount();i++){
                ScrollView s=scroll(g.getChildAt(i));
                if(s!=null)return s;
            }
        }
        return null;
    }
    private static boolean inside(View ancestor,View child){
        android.view.ViewParent parent=child.getParent();
        while(parent!=null){
            if(parent==ancestor)return true;
            parent=parent.getParent();
        }
        return false;
    }
    private static void capture(Activity activity,String filename){
        View decor=activity.getWindow().getDecorView();
        assertTrue(decor.getWidth()>0&&decor.getHeight()>0);
        Bitmap bitmap=Bitmap.createBitmap(decor.getWidth(),decor.getHeight(),
            Bitmap.Config.ARGB_8888);
        decor.draw(new Canvas(bitmap));
        ContentValues row=new ContentValues();
        row.put(MediaStore.Images.Media.DISPLAY_NAME,filename);
        row.put(MediaStore.Images.Media.MIME_TYPE,"image/png");
        row.put(MediaStore.Images.Media.RELATIVE_PATH,
            Environment.DIRECTORY_PICTURES+"/LIKAS_RC63");
        row.put(MediaStore.Images.Media.IS_PENDING,1);
        Uri uri=activity.getContentResolver().insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,row);
        assertNotNull("Emulator screenshot",uri);
        try(OutputStream output=activity.getContentResolver().openOutputStream(uri)){
            assertNotNull(output);
            assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG,100,output));
        }catch(Exception ex){throw new IllegalStateException(ex);}
        ContentValues done=new ContentValues();
        done.put(MediaStore.Images.Media.IS_PENDING,0);
        activity.getContentResolver().update(uri,done,null,null);
        bitmap.recycle();
    }
    private static void settle(){
        try{Thread.sleep(700);}catch(InterruptedException e){Thread.currentThread().interrupt();}
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
    }
    @Test public void checklistLayoutAccessibilityAndResultNavigation(){
        Intent entry=new Intent(ApplicationProvider.getApplicationContext(),
            Rc7SpeciesLibraryActivity.class);
        try(ActivityScenario<Rc7SpeciesLibraryActivity> scenario=ActivityScenario.launch(entry)){
            settle();
            scenario.onActivity(a->{
                View root=a.getWindow().getDecorView();
                EditText search=(EditText)described(root,"Search Philippine taxon catalog");
                assertNotNull("Full checklist search visible",search);
                ScrollView list=scroll(root);
                assertNotNull("Scrollable checklist",list);
                Spinner group=(Spinner)described(root,"Wildlife group");
                Spinner coverage=(Spinner)described(root,"Species library content filter");
                assertNotNull("Taxonomic group filter",group);
                assertNotNull("Reference evidence coverage filter",coverage);
                assertNotNull("Visible group label",text(root,"Wildlife group"));
                assertNotNull("Visible coverage label",text(root,"Reference coverage"));
                boolean large=a.getResources().getConfiguration().fontScale>=1.60f;
                assertTrue("Group filter scrolls instead of obscuring the IME viewport",
                    inside(list,group));
                assertTrue("Coverage filter scrolls instead of obscuring results",
                    inside(list,coverage));
                if(large){
                    assertEquals("Short search hint at 200%", "Search taxon or family",
                        String.valueOf(search.getHint()));
                }
                View first=described(root,"Open species reference:");
                assertNotNull("Checklist row accessible",first);
                assertTrue("Clickable record row",first.isClickable());
                assertTrue("Record row focusable",first.isFocusable());
                View evidence=described(root,"");
                TextView status=null;
                if(root instanceof ViewGroup){
                    // Use the text's content description rather than incidental visual positioning.
                    status=findEvidence(root);
                }
                assertNotNull("Accessible evidence limitation disclosure",status);
                AccessibilityNodeInfo info=status.createAccessibilityNodeInfo();
                assertEquals("Evidence limit announces button role",
                    Button.class.getName(),String.valueOf(info.getClassName()));
                assertTrue("Evidence limits exposes click action",info.isClickable());
                info.recycle();
                capture(a,"rc63-"+preset()+"-checklist-top.png");
            });
            scenario.onActivity(a->{
                ScrollView list=scroll(a.getWindow().getDecorView());
                assertNotNull(list);
                list.scrollTo(0,Math.round(260*a.getResources().getDisplayMetrics().density));
            });
            settle();
            scenario.onActivity(a->capture(a,
                "rc63-"+preset()+"-checklist-scrolled.png"));
        }
    }
    private static TextView findEvidence(View root){
        if(root instanceof TextView){
            CharSequence description=root.getContentDescription();
            if(description!=null&&description.toString().contains("Tap for evidence limits."))
                return (TextView)root;
        }
        if(root instanceof ViewGroup){
            ViewGroup g=(ViewGroup)root;
            for(int i=0;i<g.getChildCount();i++){
                TextView v=findEvidence(g.getChildAt(i));
                if(v!=null)return v;
            }
        }
        return null;
    }
    @Test public void keyboardOpenLeavesChecklistUsableAndSearchHonest(){
        Intent entry=new Intent(ApplicationProvider.getApplicationContext(),
            Rc7SpeciesLibraryActivity.class);
        try(ActivityScenario<Rc7SpeciesLibraryActivity> scenario=ActivityScenario.launch(entry)){
            settle();
            scenario.onActivity(a->{
                View root=a.getWindow().getDecorView();
                EditText input=(EditText)described(root,"Search Philippine taxon catalog");
                assertNotNull(input);
                input.setShowSoftInputOnFocus(true);
                input.requestFocus();
                input.performClick();
                a.getWindow().setSoftInputMode(
                    android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE |
                    android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                InputMethodManager imm=(InputMethodManager)a.getSystemService(Context.INPUT_METHOD_SERVICE);
                assertNotNull(imm);
                input.post(()->imm.showSoftInput(input,InputMethodManager.SHOW_FORCED));
            });
            settle();
            try{Thread.sleep(1100);}catch(InterruptedException e){Thread.currentThread().interrupt();}
            scenario.onActivity(a->{
                View root=a.getWindow().getDecorView();
                capture(a,"rc63-"+preset()+"-ime-attempt.png");
                WindowInsets insets=root.getRootWindowInsets();
                assertNotNull("Android window insets",insets);
                assertTrue("Actual IME must be visible in acceptance screenshot",
                    insets.isVisible(WindowInsets.Type.ime()));
                ScrollView list=scroll(root);
                assertNotNull(list);
                Rect visible=new Rect();
                assertTrue("Checklist remains on screen while keyboard is open",
                    list.getGlobalVisibleRect(visible));
                float dp=a.getResources().getDisplayMetrics().density;
                assertTrue("Checklist remains usable with open keyboard",
                    visible.height()>=Math.round(72*dp));
                capture(a,"rc63-"+preset()+"-keyboard-open.png");
                EditText input=(EditText)described(root,"Search Philippine taxon catalog");
                input.setText("Pithecophaga jefferyi");
            });
            settle();
            scenario.onActivity(a->{
                View root=a.getWindow().getDecorView();
                TextView result=findEvidence(root);
                assertNotNull(result);
                assertTrue("Search returns source evidence metadata",
                    result.getText().toString().contains("source records")
                    ||result.getText().toString().contains("names"));
                assertNotNull("Scientific-name query returns actual source row",
                    described(root,"Open species reference:"));
                EditText input=(EditText)described(root,"Search Philippine taxon catalog");
                input.setText("no_such_taxon_please_q63");
            });
            settle();
            scenario.onActivity(a->{
                assertNotNull("Missing search gives neutral no-match message",
                    text(a.getWindow().getDecorView(),
                    "No matches. Try another name or group."));
            });
        }
    }
}
