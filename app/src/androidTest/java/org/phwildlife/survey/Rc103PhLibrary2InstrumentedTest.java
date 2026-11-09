package org.phwildlife.survey;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import java.io.File;
import java.io.FileOutputStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Native PHLibrary 2 reader / search / hub screenshots and data invariants. */
@RunWith(AndroidJUnit4.class)
public final class Rc103PhLibrary2InstrumentedTest {
    private static String preset(){
        return InstrumentationRegistry.getArguments().getString("preset","p360f100");
    }
    private static View find(View v,Class<?> type){
        if(type.isInstance(v))return v;
        if(v instanceof ViewGroup){
            ViewGroup g=(ViewGroup)v;
            for(int i=0;i<g.getChildCount();i++){
                View result=find(g.getChildAt(i),type);
                if(result!=null)return result;
            }
        }
        return null;
    }
    private static View text(View v,String part){
        if(v instanceof TextView && ((TextView)v).getText().toString().contains(part))return v;
        if(v instanceof ViewGroup){
            ViewGroup g=(ViewGroup)v;
            for(int i=0;i<g.getChildCount();i++){
                View match=text(g.getChildAt(i),part);
                if(match!=null)return match;
            }
        }
        return null;
    }
    private static View named(View v,String simpleName){
        if(v.getClass().getSimpleName().equals(simpleName))return v;
        if(v instanceof ViewGroup){
            ViewGroup g=(ViewGroup)v;
            for(int i=0;i<g.getChildCount();i++){
                View found=named(g.getChildAt(i),simpleName);
                if(found!=null)return found;
            }
        }
        return null;
    }
    private static void shot(Activity a,String screen)throws Exception{
        View root=a.getWindow().getDecorView();
        assertTrue(root.getWidth()>0 && root.getHeight()>0);
        Bitmap bmp=Bitmap.createBitmap(root.getWidth(),root.getHeight(),Bitmap.Config.ARGB_8888);
        try {
            root.draw(new Canvas(bmp));
            File dir=new File(a.getExternalFilesDir(Environment.DIRECTORY_PICTURES),"RC103");
            assertTrue(dir.isDirectory()||dir.mkdirs());
            File f=new File(dir,preset()+"-"+screen+".png");
            try(FileOutputStream out=new FileOutputStream(f)){
                assertTrue(bmp.compress(Bitmap.CompressFormat.PNG,100,out));
            }
            assertTrue(f.length()>1000);
        }finally {bmp.recycle();}
    }
    @Test public void atlasHomeHasFourGroupsAndRealResearchRoutes() throws Exception {
        try(ActivityScenario<LikasLibraryActivity> scenario=ActivityScenario.launch(
            new Intent(ApplicationProvider.getApplicationContext(),LikasLibraryActivity.class))){
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            final Exception[] err={null};
            scenario.onActivity(a->{
                try {
                    View root=a.getWindow().getDecorView();
                    assertNotNull(text(root,"Greater Mindanao"));
                    assertNotNull(text(root,"715"));
                    assertNotNull(text(root,"Habitat Atlas"));
                    assertNotNull(text(root,"Saved species"));
                    assertNotNull(text(root,"References"));
                    assertNotNull(text(root,"Wildlife of"));
                    assertNotNull(text(root,"Species Atlas"));
                    assertNotNull("The bundled real Mindanao photo layer must decode",
                        named(root,"PhotoLayer"));
                    if(a.getResources().getConfiguration().fontScale>=1.6f){
                        TextView back=(TextView)text(root,"Back");
                        assertNotNull(back);
                        assertTrue("Home Back text must fit at 200% font",
                            back.getPaint().getFontSpacing()<=
                            back.getHeight()-back.getPaddingTop()-back.getPaddingBottom());
                        EditText search=(EditText)find(root,EditText.class);
                        assertNotNull(search);
                        assertTrue("Home search must be full width at 200% font",
                            search.getWidth()>=root.getWidth()*.7f);
                    }
                    shot(a,"home");
                }catch(Exception x){err[0]=x;}
            });
            if(err[0]!=null)throw err[0];
        }
    }
    @Test public void nativeExplorerSearchesSplitKingfishersAndUnrankedFrog() throws Exception {
        try(ActivityScenario<PhLibraryDirectoryActivity> scenario=ActivityScenario.launch(
            new Intent(ApplicationProvider.getApplicationContext(),PhLibraryDirectoryActivity.class))){
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            final Exception[] err={null};
            scenario.onActivity(a->{
                try{
                    View root=a.getWindow().getDecorView();
                    ListView lv=(ListView)find(root,ListView.class);
                    EditText input=(EditText)find(root,EditText.class);
                    assertNotNull(lv);assertNotNull(input);
                    assertEquals(715,lv.getAdapter().getCount());
                    TextView clear=(TextView)text(root,"Clear");
                    assertNotNull(clear);
                    float clearWidth=clear.getPaint().measureText("Clear");
                    int innerWidth=clear.getWidth()-clear.getPaddingLeft()-clear.getPaddingRight();
                    assertTrue("Clear label must fit inside its button at this text scale",
                        clearWidth<=innerWidth);
                    if(a.getResources().getConfiguration().fontScale>=1.6f){
                        for(String action:new String[]{"Sort","Filters"}){
                            TextView control=(TextView)text(root,action);
                            assertNotNull("Missing "+action+" action",control);
                            float needed=control.getPaint().getFontSpacing();
                            int available=control.getHeight()-control.getPaddingTop()-control.getPaddingBottom();
                            assertTrue(action+" text must fit vertically at 200% scale",
                                needed<=available);
                        }
                    }
                    if(a.getResources().getConfiguration().fontScale>=1.6f){
                        TextView badge=(TextView)text(root,"Uncommon");
                        assertNotNull(badge);
                        assertEquals("Uncommon badge must not wrap an isolated icon",
                            1,badge.getLineCount());
                    }
                    shot(a,"explorer");
                    input.setText("Ceyx mindanensis");
                    assertEquals(1,lv.getAdapter().getCount());
                    assertNotNull(text(root,"1 account found"));
                    input.setText("Oreophryne cf. nana");
                    assertEquals(1,lv.getAdapter().getCount());
                    input.setText("");
                    assertEquals(715,lv.getAdapter().getCount());
                }catch(Exception x){err[0]=x;}
            });
            if(err[0]!=null)throw err[0];
        }
    }
    @Test public void speciesReaderKeepsVerbatimTextAndSeparatedStatuses() throws Exception {
        Intent i=new Intent(ApplicationProvider.getApplicationContext(),
            TaxonDetailActivity.class).putExtra("phlibrary_scientific_name","Ceyx mindanensis");
        try(ActivityScenario<TaxonDetailActivity> scenario=ActivityScenario.launch(i)){
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            final Exception[] err={null};
            scenario.onActivity(a->{
                try {
                    View root=a.getWindow().getDecorView();
                    TextView name=(TextView)text(root,"South Philippine Dwarf-kingfisher");
                    assertNotNull(name);
                    if(a.getResources().getConfiguration().fontScale>=1.6f){
                        android.text.Layout layout=name.getLayout();
                        assertNotNull(layout);
                        for(int ln=0;ln<layout.getLineCount()-1;ln++){
                            int cut=layout.getLineEnd(ln);
                            if(cut>0 && cut<name.getText().length()){
                                char left=name.getText().charAt(cut-1);
                                char right=name.getText().charAt(cut);
                                assertFalse("Reader heading breaks in the middle of a word",
                                    Character.isLetter(left)&&Character.isLetter(right));
                            }
                        }
                    }
                    assertNotNull(text(root,"Ceyx mindanensis"));
                    assertNull("Old display label must be removed",text(root,"DISCOVERY RARITY"));
                    assertNull("Old display label must be removed",text(root,"IUCN GLOBAL"));
                    assertNotNull(text(root,"Uncommon"));
                    assertNotNull(text(root,"Least Concern"));
                    assertNotNull(text(root,"Source notes"));
                    assertNotNull(text(root,"REFERENCES"));
                    assertNotNull(text(root,"More in Alcedinidae"));
                    assertNotNull(text(root,"Next species"));
                    shot(a,"profile");
                }catch(Exception x){err[0]=x;}
            });
            if(err[0]!=null)throw err[0];
        }
    }
    @Test public void sourceLinkedHabitatsRemainDiscoverableAndReadable() throws Exception {
        try(ActivityScenario<HabitatSpeciesEvidenceActivity> scenario=ActivityScenario.launch(
            new Intent(ApplicationProvider.getApplicationContext(),
                HabitatSpeciesEvidenceActivity.class))){
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            final Exception[] err={null};
            scenario.onActivity(a->{
                try{
                    View root=a.getWindow().getDecorView();
                    assertNotNull(text(root,"Mindanao landscapes"));
                    assertNotNull(text(root,"Lowland tropical forest"));
                    assertNotNull(text(root,"Terrestrial"));
                    shot(a,"habitat-index");
                    View name=text(root,"Lowland tropical forest");
                    assertNotNull(name);
                    assertTrue("Habitat entry must remain tappable",
                        ((View)name.getParent()).performClick());
                }catch(Exception e){err[0]=e;}
            });
            if(err[0]!=null)throw err[0];
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            scenario.onActivity(a->{
                try{
                    View root=a.getWindow().getDecorView();
                    assertNotNull(text(root,"FIELD INDICATORS"));
                    assertNotNull(text(root,"LITERATURE-LINKED SPECIES"));
                    assertNotNull(text(root,"Open source"));
                    assertNotNull(text(root,"Find textual mentions"));
                    shot(a,"habitat-detail");
                }catch(Exception e){err[0]=e;}
            });
            if(err[0]!=null)throw err[0];
        }
    }

}
