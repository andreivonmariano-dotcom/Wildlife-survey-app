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
                    assertNull("Duplicate wildlife shortcuts removed",text(root,"Wildlife groups"));
                    View photo=named(root,"PhotoLayer");
                    assertNotNull("The bundled real Mindanao photo layer must decode",photo);
                    View cover=(View)photo.getParent();
                    int limit=PhLibrary2Ui.dp(a,
                        a.getResources().getConfiguration().fontScale>=1.6f?248:202);
                    assertTrue("The Home hero must not push navigation off-screen",
                        cover.getHeight()>0&&cover.getHeight()<=limit);
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
                    TextView reset=(TextView)text(root,"Clear");
                    assertNotNull("Reset control remains available when needed",reset);
                    assertEquals("Clear is hidden until a filter or search is active",
                        View.GONE,reset.getVisibility());
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
                    assertNotNull(text(root,"1 account"));
                }catch(Exception x){err[0]=x;}
            });
            if(err[0]!=null)throw err[0];
            // A formerly GONE button has no valid measured width until the next
            // UI traversal. Test the *rendered* visible control, not its stale size.
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            scenario.onActivity(a->{
                try{
                    View root=a.getWindow().getDecorView();
                    ListView lv=(ListView)find(root,ListView.class);
                    EditText input=(EditText)find(root,EditText.class);
                    TextView reset=(TextView)text(root,"Clear");
                    assertNotNull("Clear shown with an active search",reset);
                    assertEquals(View.VISIBLE,reset.getVisibility());
                    assertTrue("Clear must be laid out",reset.getWidth()>0);
                    float clearWidth=reset.getPaint().measureText("Clear");
                    int innerWidth=reset.getWidth()-reset.getPaddingLeft()-reset.getPaddingRight();
                    assertTrue("Clear must fit within its button: label="+clearWidth+
                        ", content="+innerWidth,clearWidth<=innerWidth);
                    input.setText("Oreophryne cf. nana");
                    assertEquals(1,lv.getAdapter().getCount());
                    input.setText("");
                    assertEquals(715,lv.getAdapter().getCount());
                    assertEquals("Clear hidden after reset",View.GONE,reset.getVisibility());
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
                    assertNotNull(text(root,"Citations ("));
                    assertNotNull(text(root,"Same family · Alcedinidae"));
                    TextView save=(TextView)text(root,"☆  Save");
                    assertNotNull("Compact Save action remains discoverable",save);
                    assertTrue(save.isClickable());
                    if(a.getResources().getConfiguration().fontScale>=1.6f){
                        assertTrue("Save label must fit vertically at 200% scale",
                            save.getPaint().getFontSpacing()<=save.getHeight()-
                                save.getPaddingTop()-save.getPaddingBottom());
                    }
                    assertNotNull(text(root,"Next species"));
                    shot(a,"profile");
                    View next=text(root,"Next species");
                    assertTrue(next.performClick());
                    a.onBackPressed();
                    assertNotNull("Back must return to the previous species",
                        text(a.getWindow().getDecorView(),
                            "South Philippine Dwarf-kingfisher"));
                }catch(Exception x){err[0]=x;}
            });
            if(err[0]!=null)throw err[0];
        }
    }
    @Test public void habitatAccountsReadAsOneParagraphWithoutSpeciesCards() throws Exception {
        try(ActivityScenario<HabitatSpeciesEvidenceActivity> scenario=ActivityScenario.launch(
            new Intent(ApplicationProvider.getApplicationContext(),
                HabitatSpeciesEvidenceActivity.class))){
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            final Exception[] err={null};
            scenario.onActivity(a->{
                try{
                    View root=a.getWindow().getDecorView();
                    assertNotNull(text(root,"Habitat Atlas"));
                    assertNotNull(text(root,"Lowland tropical forest"));
                    assertNull("No duplicate index heading",text(root,"Mindanao landscapes"));
                    shot(a,"habitat-index");
                    View name=text(root,"Lowland tropical forest");
                    assertNotNull(name);
                    assertTrue(((View)name.getParent()).performClick());
                }catch(Exception e){err[0]=e;}
            });
            if(err[0]!=null)throw err[0];
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            scenario.onActivity(a->{
                try{
                    View root=a.getWindow().getDecorView();
                    assertNotNull(text(root,"Lowland tropical forests occur"));
                    assertNotNull(text(root,"Browse species"));
                    assertNull("Legacy cards removed",text(root,"Literature-linked species"));
                    assertNull("Fragmented accordion removed",text(root,"Field indicators"));
                    assertNull("No source-card action",text(root,"Open source"));
                    shot(a,"habitat-detail");
                }catch(Exception e){err[0]=e;}
            });
            if(err[0]!=null)throw err[0];
        }
    }

    @Test public void habitatSelectionUsesMoreAccountTermsAndStaysSelected() throws Exception{
        Intent intent=new Intent(ApplicationProvider.getApplicationContext(),
            PhLibraryDirectoryActivity.class)
            .putExtra("phlibrary_habitat_id","riparian");
        try(ActivityScenario<PhLibraryDirectoryActivity> scenario=ActivityScenario.launch(intent)){
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            final Exception[] err={null};
            scenario.onActivity(a->{
                try{
                    int previous=0;
                    for(org.json.JSONObject row:PhLibraryBook01.entries(a))
                        if(row.optString("p").toLowerCase(java.util.Locale.ROOT).contains("riparian"))
                            previous++;
                    ListView species=(ListView)find(a.getWindow().getDecorView(),ListView.class);
                    assertNotNull(species);
                    assertTrue("Habitat finder must include alternate ecological terms",
                        species.getAdapter().getCount()>previous);
                    assertNotNull(text(a.getWindow().getDecorView(),"Filters"));
                    assertNotNull("Habitat selection must remain visible in the result summary",
                        text(a.getWindow().getDecorView(),"accounts mention riparian"));
                    shot(a,"habitat-species");
                }catch(Exception e){err[0]=e;}
            });
            if(err[0]!=null)throw err[0];
        }
    }

    @Test public void savedSpeciesClearDeletesBookmarksWithoutLeavingSavedScreen()
        throws Exception{
        android.content.Context app=ApplicationProvider.getApplicationContext();
        PhLibraryBookmarks.clear(app);
        PhLibraryBookmarks.toggle(app,"Ceyx mindanensis");
        PhLibraryBookmarks.toggle(app,"Ceyx melanurus");
        assertEquals(2,PhLibraryBookmarks.all(app).size());
        Intent intent=new Intent(app,PhLibraryDirectoryActivity.class)
            .putExtra("phlibrary_saved_only","true");
        try(ActivityScenario<PhLibraryDirectoryActivity> scenario=ActivityScenario.launch(intent)){
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            final Exception[] err={null};
            scenario.onActivity(a->{
                try{
                    View root=a.getWindow().getDecorView();
                    assertNotNull(text(root,"Saved"));
                    ListView species=(ListView)find(root,ListView.class);
                    assertEquals(2,species.getAdapter().getCount());
                    shot(a,"saved-before");
                }catch(Exception e){err[0]=e;}
            });
            if(err[0]!=null)throw err[0];
            androidx.test.espresso.Espresso.onView(
                androidx.test.espresso.matcher.ViewMatchers.withText("Clear"))
                .perform(androidx.test.espresso.action.ViewActions.click());
            androidx.test.espresso.Espresso.onView(
                androidx.test.espresso.matcher.ViewMatchers.withText("Clear saved"))
                .inRoot(androidx.test.espresso.matcher.RootMatchers.isDialog())
                .perform(androidx.test.espresso.action.ViewActions.click());
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            scenario.onActivity(a->{
                try{
                    View root=a.getWindow().getDecorView();
                    assertNotNull("Saved navigation context must persist",
                        text(root,"Saved"));
                    assertNotNull(text(root,"No saved species"));
                    ListView species=(ListView)find(root,ListView.class);
                    assertEquals(0,species.getAdapter().getCount());
                    assertTrue(PhLibraryBookmarks.all(a).isEmpty());
                    shot(a,"saved-after");
                }catch(Exception e){err[0]=e;}
            });
            if(err[0]!=null)throw err[0];
        }finally{PhLibraryBookmarks.clear(app);}
    }

    @Test public void bibliographyIsCleanAndCitationSearchReturnsUnverifiedCandidates()
        throws Exception {
        final Exception[] err={null};
        try(ActivityScenario<PhLibraryBibliographyActivity> scenario=ActivityScenario.launch(
            new Intent(ApplicationProvider.getApplicationContext(),
                PhLibraryBibliographyActivity.class))){
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            scenario.onActivity(a->{
                try{
                    View root=a.getWindow().getDecorView();
                    assertNotNull(text(root,"1159 references"));
                    assertNull("Old explanatory preface must be removed",
                        text(root,"not yet completely reconciled"));
                    assertNotNull(find(root,EditText.class));
                    assertNotNull(find(root,ListView.class));
                    shot(a,"references");
                }catch(Exception e){err[0]=e;}
            });
        }
        if(err[0]!=null)throw err[0];
        try(ActivityScenario<PhLibraryBibliographyActivity> scenario=ActivityScenario.launch(
            new Intent(ApplicationProvider.getApplicationContext(),
                PhLibraryBibliographyActivity.class)
                    .putExtra("phlibrary_bib_citation","Lint & Stott, 1948"))){
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            scenario.onActivity(a->{
                try{
                    View root=a.getWindow().getDecorView();
                    assertNotNull("Author–year citations should discover candidate references",
                        text(root,"possible reference"));
                    assertNotNull(text(root,"Not verified"));
                    assertTrue(((ListView)find(root,ListView.class)).getAdapter().getCount()>0);
                    shot(a,"reference-candidates");
                }catch(Exception e){err[0]=e;}
            });
        }
        if(err[0]!=null)throw err[0];
    }

}
