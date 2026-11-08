package org.phwildlife.survey;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.Assert.*;

/** Read-only species article and unchanged rarity acceptance for RC99. */
@RunWith(AndroidJUnit4.class)
public final class Rc99PhLibraryHabitatInstrumentedTest {
    @Test public void habitatCuesKeepTheOriginal714TaxaAndRarity() throws Exception {
        Context context=ApplicationProvider.getApplicationContext();
        assertTrue(PhLibraryBook01.availabilityMessage(context),PhLibraryBook01.ready(context));
        List<JSONObject> rows=PhLibraryBook01.entries(context);
        assertEquals(714,rows.size());
        Map<String,Integer> tiers=new HashMap<>();
        Map<String,JSONObject> indexed=new HashMap<>();
        for(JSONObject row:rows){
            String tier=row.getString("t");
            tiers.put(tier,tiers.containsKey(tier)?tiers.get(tier)+1:1);
            assertNull("Duplicate species "+row.getString("s"),
                indexed.put(row.getString("s"),row));
            assertTrue(row.getString("p").indexOf('\n')<0);
        }
        assertEquals(Integer.valueOf(117),tiers.get("Common"));
        assertEquals(Integer.valueOf(343),tiers.get("Uncommon"));
        assertEquals(Integer.valueOf(214),tiers.get("Rare"));
        assertEquals(Integer.valueOf(37),tiers.get("Exceptionally rare"));
        assertEquals(Integer.valueOf(3),tiers.get("Legendary"));
        assertTrue(indexed.get("Aviceda jerdoni").getString("p")
            .contains("Shaded canopy gaps and streamside trees"));
        assertTrue(indexed.get("Pelophryne lighti").getString("p")
            .contains("Low ferns and shrub leaves form recorded perches"));
        assertTrue(indexed.get("Draco mindanensis").getString("p")
            .contains("commonly around 5–10 m"));
        assertTrue(indexed.get("Acerodon jubatus").getString("p")
            .contains("most recorded foraging locations of Golden-capped Flying Foxes in closed forest"));
        assertTrue(indexed.get("Acerodon jubatus").getJSONArray("r").length()>0);
    }
}
