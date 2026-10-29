package org.phwildlife.survey;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.json.JSONArray;
import org.json.JSONObject;
import static org.junit.Assert.*;

/** Disposable-emulator UI assertions for the 96-taxon shared species card. */
@RunWith(AndroidJUnit4.class)
public final class Rc11SpeciesAccountInstrumentedTest {
    private static ArrayList<View> descendants(View root){
        ArrayList<View> all=new ArrayList<>();
        all.add(root);
        if(root instanceof ViewGroup){
            ViewGroup parent=(ViewGroup)root;
            for(int i=0;i<parent.getChildCount();i++)
                all.addAll(descendants(parent.getChildAt(i)));
        }
        return all;
    }
    private static boolean containsText(ArrayList<View> all,String text){
        for(View v:all)if(v instanceof TextView&&
            ((TextView)v).getText().toString().contains(text))return true;
        return false;
    }
    @Test public void testIconSectionsAndCitationDrawer()throws Exception{
        Intent intent=new Intent(InstrumentationRegistry.getInstrumentation()
            .getTargetContext(),TaxonDetailActivity.class);
        intent.putExtra("taxon_query","Pithecophaga jefferyi");
        try(ActivityScenario<TaxonDetailActivity> scenario=ActivityScenario.launch(intent)){
            scenario.onActivity(activity->{
                View root=activity.getWindow().getDecorView();
                ArrayList<View> views=descendants(root);
                assertTrue(containsText(views,"Pithecophaga jefferyi"));
                assertTrue(containsText(views,"Identification"));
                assertTrue(containsText(views,"Habitat"));
                assertTrue(containsText(views,"Sources & details"));
                assertTrue("Cited topic coverage visible",containsText(views,"of 5 research topics documented"));
                assertTrue("Per-note research provenance visible",containsText(views,"Source: "));
                assertTrue("Comparison snapshot labeled",containsText(views,"Compare current cited traits"));
                assertTrue(containsText(views,"Photo reference"));
                assertTrue(containsText(views,"View photo & credits"));
                assertFalse("Long legacy photo caption must stay hidden by default",
                    containsText(views,"iNaturalist photographic reference; identification"));
                assertFalse("Legacy RC7 cards must not stack",
                    containsText(views,"RC7 priority research card"));
                assertFalse("No front-facing review boilerplate",
                    containsText(views,"specialist key review remains pending"));
                int icons=0;
                for(View v:views)
                    if(v.getClass().getName().contains("Rc11PrioritySpeciesView$Glyph"))
                        icons++;
                assertTrue("Vector field icons required",icons>=2);
                for(View v:views)
                    if(v instanceof Button&&((Button)v).getText().toString()
                        .startsWith("Sources & details")){
                        v.performClick();break;
                    }
                assertTrue(containsText(descendants(root),"Study details"));
                assertTrue(containsText(descendants(root),"Research references"));
                assertTrue(containsText(descendants(root),"Bacus et al."));
                assertTrue(containsText(descendants(root),"Open study"));
                assertTrue(containsText(descendants(root),"Filter references"));
                for(View v:descendants(root))
                    if(v instanceof Button&&((Button)v).getText().toString()
                        .startsWith("Filter references")){
                        assertTrue(v.performClick());break;
                    }
                assertTrue(containsText(descendants(root),"Publication year"));
                assertTrue(containsText(descendants(root),"Research topic"));
                if(root.getWidth()>0&&root.getHeight()>0){
                    Bitmap bitmap=Bitmap.createBitmap(root.getWidth(),root.getHeight(),
                        Bitmap.Config.ARGB_8888);
                    root.draw(new Canvas(bitmap));
                    File output=new File(activity.getFilesDir(),
                        "rc11_species_account_320dp.png");
                    try(FileOutputStream stream=new FileOutputStream(output)){
                        assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG,90,stream));
                    }catch(Exception e){throw new IllegalStateException(e);}
                    bitmap.recycle();
                }
            });
        }
    }
    @Test public void testSparsePriorityHasNoPlaceholderStack()throws Exception{
        Intent intent=new Intent(InstrumentationRegistry.getInstrumentation()
            .getTargetContext(),TaxonDetailActivity.class);
        intent.putExtra("taxon_query","Buceros hydrocorax");
        try(ActivityScenario<TaxonDetailActivity> scenario=ActivityScenario.launch(intent)){
            scenario.onActivity(activity->{
                ArrayList<View> views=descendants(activity.getWindow().getDecorView());
                assertTrue(containsText(views,"Buceros hydrocorax"));
                assertTrue(containsText(views,"Sources & details"));
                assertFalse(containsText(views,"Unassessed. No reviewed ecological profile installed."));
                assertFalse(containsText(views,"A detailed account is not yet source-checked"));
                assertFalse(containsText(views,"Review status: Not independently reviewed"));
            });
        }
    }
    @Test public void testOriginalDiagnosisAndChecklistRemainAccessible()throws Exception{
        Intent intent=new Intent(InstrumentationRegistry.getInstrumentation()
            .getTargetContext(),TaxonDetailActivity.class);
        intent.putExtra("taxon_query","Baletemys kampalili");
        try(ActivityScenario<TaxonDetailActivity> scenario=ActivityScenario.launch(intent)){
            scenario.onActivity(activity->{
                View root=activity.getWindow().getDecorView();
                ArrayList<View> initial=descendants(root);
                assertTrue("Original description character rendered",
                    containsText(initial,"Head and ears"));
                assertTrue("Original series measurements rendered",
                    containsText(initial,"Published body measures"));
                boolean opened=false;
                for(View v:initial)if(v instanceof Button&&
                    ((Button)v).getText().toString().startsWith("Sources & details")){
                    assertTrue(v.performClick());
                    opened=true;break;
                }
                assertTrue("Source drawer reachable",opened);
                ArrayList<View> expanded=descendants(root);
                assertTrue(containsText(expanded,"Literature coverage"));
                boolean checklist=false;
                for(View v:expanded)if(v instanceof Button&&
                    ((Button)v).getText().toString().startsWith("Literature coverage")){
                    assertTrue(v.performClick());
                    checklist=true;break;
                }
                assertTrue("Literature drawer opens",checklist);
                assertTrue("Publication evidence is visible",
                    containsText(descendants(root),"6 individually cited study notes"));
            });
        }
    }

    @Test public void testPublishedFrogDifferentialOpens()throws Exception{
        Intent intent=new Intent(InstrumentationRegistry.getInstrumentation()
            .getTargetContext(),TaxonDetailActivity.class);
        intent.putExtra("taxon_query","Platymantis pygmaeus");
        try(ActivityScenario<TaxonDetailActivity> scenario=ActivityScenario.launch(intent)){
            scenario.onActivity(activity->{
                View root=activity.getWindow().getDecorView();
                ArrayList<View> views=descendants(root);
                assertTrue(containsText(views,"Compare similar species"));
                assertTrue(containsText(views,"Adult body length"));
            });
        }
        Intent compare=new Intent(InstrumentationRegistry.getInstrumentation()
            .getTargetContext(),Rc9DifferentialActivity.class);
        compare.putExtra("scientific_name","Platymantis pygmaeus");
        try(ActivityScenario<Rc9DifferentialActivity> scenario=ActivityScenario.launch(compare)){
            scenario.onActivity(activity->{
                ArrayList<View> views=descendants(activity.getWindow().getDecorView());
                assertTrue(containsText(views,"Platymantis naomii"));
                assertTrue(containsText(views,"14.1"));
                assertTrue(containsText(views,"Read published source"));
                assertFalse(containsText(views,"specialist-certified"));
            });
        }
    }

    @Test public void testAllPriorityAccountCorpusFillsPreviouslyFactEmptyNames(){
        String[] names={"Dicaeum anthonyi","Desmalopex microleucoptera"};
        String[] documented={"Northern Luzon","Morphological differentiation"};
        for(int i=0;i<names.length;i++){
            final String species=names[i];
            final String detail=documented[i];
            Intent intent=new Intent(InstrumentationRegistry.getInstrumentation()
                .getTargetContext(),TaxonDetailActivity.class);
            intent.putExtra("taxon_query",species);
            try(ActivityScenario<TaxonDetailActivity> scenario=ActivityScenario.launch(intent)){
                scenario.onActivity(a->{
                    ArrayList<View> views=descendants(a.getWindow().getDecorView());
                    assertTrue(containsText(views,species));
                    assertTrue("Original literature fact visible for "+species,
                        containsText(views,detail));
                    assertTrue(containsText(views,"Sources & details"));
                    assertFalse(containsText(views,"A detailed account is not yet source-checked"));
                });
            }
        }
    }

    @Test public void testAll96ActualSpeciesScreensHaveCitedCardAndNoBoilerplate()
            throws Exception{
        android.content.Context target=InstrumentationRegistry.getInstrumentation()
            .getTargetContext();
        java.io.ByteArrayOutputStream json=new java.io.ByteArrayOutputStream();
        try(InputStream in=target.getAssets().open("rc18_unified_96_priority_accounts_v1.json")){
            byte[] buffer=new byte[8192];int length;
            while((length=in.read(buffer))!=-1)json.write(buffer,0,length);
        }
        JSONArray accounts=new JSONObject(json.toString("UTF-8")).getJSONArray("accounts");
        assertEquals(96,accounts.length());
        File dir=new File(target.getFilesDir(),"rc19-all-96-screen-audit");
        assertTrue(dir.isDirectory()||dir.mkdirs());
        StringBuilder manifest=new StringBuilder("index,scientificName,group,visibleCards,bytes\n");
        for(int i=0;i<accounts.length();i++){
            JSONObject species=accounts.getJSONObject(i);
            final int number=i;
            final String name=species.getString("scientificName");
            final String group=species.getString("group");
            Intent intent=new Intent(target,TaxonDetailActivity.class);
            intent.putExtra("taxon_query",name);
            try(ActivityScenario<TaxonDetailActivity> screen=ActivityScenario.launch(intent)){
                screen.onActivity(activity->{
                    View root=activity.getWindow().getDecorView();
                    ArrayList<View> all=descendants(root);
                    assertTrue("Missing species title "+name,containsText(all,name));
                    assertTrue("Missing references "+name,containsText(all,"Sources & details"));
                    assertTrue("Missing cited-topic coverage "+name,
                        containsText(all,"of 5 research topics documented"));
                    assertTrue("Missing on-card citation "+name,
                        containsText(all,"Source: "));
                    int count=0;
                    for(String heading:new String[]{"Identification","Habitat",
                        "Range","Natural history","Conservation"})
                        if(containsText(all,heading))count++;
                    int glyphs=0;
                    for(View view:all)
                        if(view.getClass().getName().contains("Rc11PrioritySpeciesView$Glyph"))
                            glyphs++;
                    assertTrue("No rendered priority research card "+name,glyphs>0);
                    assertTrue("No biological card for "+name,count>0);
                    for(String forbidden:new String[]{
                        "specialist key review remains pending",
                        "A detailed account is not yet source-checked",
                        "Unassessed. No reviewed ecological profile installed."})
                        assertFalse("Defensive copy for "+name,containsText(all,forbidden));
                    assertTrue("Screen was not laid out "+name,
                        root.getWidth()>0&&root.getHeight()>0);
                    Bitmap bitmap=Bitmap.createBitmap(root.getWidth(),root.getHeight(),
                        Bitmap.Config.RGB_565);
                    root.draw(new Canvas(bitmap));
                    File output=new File(dir,String.format(java.util.Locale.ROOT,
                        "%03d.jpg",number));
                    try(FileOutputStream stream=new FileOutputStream(output)){
                        assertTrue(bitmap.compress(Bitmap.CompressFormat.JPEG,65,stream));
                    }catch(Exception e){throw new IllegalStateException(e);}
                    bitmap.recycle();
                    manifest.append(number).append(",").append(name).append(",")
                        .append(group).append(",").append(count).append(",")
                        .append(output.length()).append("\n");
                });
            }
        }
        try(FileOutputStream out=new FileOutputStream(new File(target.getFilesDir(),
            "rc19-all-96-screen-audit.csv"))){
            out.write(manifest.toString().getBytes(StandardCharsets.UTF_8));
        }
    }

}
