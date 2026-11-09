package org.phwildlife.survey;
import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.Arrays;
import static org.junit.Assert.*;
@RunWith(AndroidJUnit4.class)
public final class Rc110DriveOnlyPhotoResetInstrumentedTest {
    @Test public void nativePackContainsNoSpeciesPhotos() throws Exception {
        Context app=ApplicationProvider.getApplicationContext();
        String[] entries=app.getAssets().list("offline_photos");
        assertNotNull(entries);
        for(String entry:entries){
            assertFalse(entry,entry.startsWith("species_"));
            assertFalse(entry,entry.startsWith("inat_"));
        }
        assertEquals(0,app.getAssets().list("offline_owner_photo_manifest_v1.json").length);
    }
}
