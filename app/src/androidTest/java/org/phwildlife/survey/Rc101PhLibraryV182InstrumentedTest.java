package org.phwildlife.survey;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Owner-locked PHLibrary Book v1.82: metadata, splits and original bibliography. */
@RunWith(AndroidJUnit4.class)
public final class Rc101PhLibraryV182InstrumentedTest {
    @Test public void canonical715AccountsAndRarityAreLoadedAtomically() throws Exception {
        Context context=ApplicationProvider.getApplicationContext();
        assertTrue(PhLibraryBook01.availabilityMessage(context),PhLibraryBook01.ready(context));
        List<JSONObject> all=PhLibraryBook01.entries(context);
        assertEquals(715,all.size());
        int common=0,uncommon=0,rare=0,exceptional=0,legendary=0,unranked=0;
        boolean frog=false,north=false,south=false;
        for(JSONObject row:all){
            String t=row.getString("t");
            switch(t){
                case "Common":common++;break;
                case "Uncommon":uncommon++;break;
                case "Rare":rare++;break;
                case "Exceptionally rare":exceptional++;break;
                case "Legendary":legendary++;break;
                case "Unranked":unranked++;break;
                default:fail("Unknown rarity "+t);
            }
            assertFalse(row.getString("p").contains("\\n"));
            assertFalse(row.getString("p").toLowerCase(java.util.Locale.ROOT).contains("associated with"));
            if("Oreophryne cf. nana".equals(row.getString("s"))){
                frog=true;assertEquals("Unranked",t);assertTrue(row.isNull("iucn"));
            }
            if("Ceyx melanurus".equals(row.getString("s")))north=true;
            if("Ceyx mindanensis".equals(row.getString("s")))south=true;
        }
        assertEquals(124,common);assertEquals(336,uncommon);assertEquals(214,rare);
        assertEquals(37,exceptional);assertEquals(3,legendary);assertEquals(1,unranked);
        assertTrue(frog&&north&&south);
        assertFalse(PhLibraryBook01.has(context,"Aphantophryne nana"));
    }
    @Test public void fullOfflineBibliographyAndReaderMetadataAreAvailable() throws Exception {
        Context context=ApplicationProvider.getApplicationContext();
        JSONArray bibliography=PhLibraryBook01.bibliography(context);
        assertEquals(1159,bibliography.length());
        assertTrue(bibliography.getJSONObject(0).getString("text").length()>30);
        assertTrue(PhLibraryBook01.sourceEdition().contains("Rarity v0.3.4"));
        assertEquals(715,PhLibraryBook01.count(context));
    }
}
