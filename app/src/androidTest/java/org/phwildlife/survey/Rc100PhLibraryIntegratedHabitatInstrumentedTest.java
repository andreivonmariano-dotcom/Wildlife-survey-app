package org.phwildlife.survey;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Full source-integrated profile verification after document-first editing. */
@RunWith(AndroidJUnit4.class)
public final class Rc100PhLibraryIntegratedHabitatInstrumentedTest {
    @Test public void integratedNarrativeAndBibliographyArePreserved() throws Exception {
        Context context=ApplicationProvider.getApplicationContext();
        assertTrue(PhLibraryBook01.availabilityMessage(context),PhLibraryBook01.ready(context));
        assertTrue(PhLibraryBook01.availabilityMessage(context).contains("Text v1.82"));
        List<JSONObject> entries=PhLibraryBook01.entries(context);
        assertEquals(715,entries.size());
        Map<String,JSONObject> indexed=new HashMap<>();
        for(JSONObject r:entries){
            assertNull("Duplicate scientific name",indexed.put(r.getString("s"),r));
            String account=r.getString("p");
            assertFalse(account.contains("\n"));
            assertFalse(account.toLowerCase(java.util.Locale.ROOT).contains("associated with"));
            JSONArray refs=r.getJSONArray("r");
            for(int j=0;j<refs.length();j++)
                assertTrue("Missing reference "+r.getString("s"),
                    account.contains(refs.getJSONObject(j).getString("short")));
        }
        assertTrue(indexed.get("Aviceda jerdoni").getString("p")
            .contains("shaded canopy gaps and streamside trees"));
        assertTrue(indexed.get("Ceyx mindanensis").getString("p").length()>500);
        assertTrue(indexed.get("Gonyosoma oxycephalum").getString("p")
            .contains("On Samar, records include Mount Huraw and Eastern Samar"));
        assertTrue(indexed.get("Gonyosoma oxycephalum").getString("p").length()>500);
        assertTrue(indexed.get("Acerodon jubatus").getString("p").length()>500);
        assertTrue(indexed.get("Fejervarya vittigera").getString("p").length()>500);
        assertEquals("Unranked",indexed.get("Oreophryne cf. nana").getString("t"));
    }

    @Test public void reviewedEditsMatchVersionedDocumentOverlay() throws Exception {
        Context c=ApplicationProvider.getApplicationContext();
        StringBuilder buf=new StringBuilder();
        try(java.io.InputStream is=c.getAssets().open("phlibrary_habitat_integration_v180.json");
            java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream()){
            byte[] b=new byte[8192];int n;
            while((n=is.read(b))!=-1)out.write(b,0,n);
            buf.append(out.toString("UTF-8"));
        }
        JSONObject pack=new JSONObject(buf.toString());
        assertEquals(714,pack.getInt("speciesCount"));
        assertEquals(100,pack.getInt("reviewedSpecies"));
        assertEquals(47,pack.getInt("integratedSpecies"));
        JSONArray changes=pack.getJSONArray("records");
        assertEquals(48,changes.length());
        Set<String> species=new HashSet<>();
        for(int i=0;i<changes.length();i++)species.add(changes.getJSONObject(i).getString("s"));
        assertEquals(47,species.size());
    }
}