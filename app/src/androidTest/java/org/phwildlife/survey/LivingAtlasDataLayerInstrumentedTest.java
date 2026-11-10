package org.phwildlife.survey;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.core.app.ApplicationProvider;
import android.content.Context;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public final class LivingAtlasDataLayerInstrumentedTest {
    @Test public void opensExistingOfflineBookAndKeepsProvisionalName() {
        Context context = ApplicationProvider.getApplicationContext();
        LivingAtlasDataLayer.Snapshot atlas = LivingAtlasDataLayer.open(context);
        assertEquals(715,atlas.taxa().size());
        assertEquals(1159,atlas.bibliographyCount);
        assertNotNull(atlas.findScientificName("Pithecophaga jefferyi"));
        assertNotNull(atlas.findScientificName("Oreophryne cf. nana"));
        assertNull(atlas.findScientificName("Aphantophryne nana"));
        assertEquals("book01@1.82:pithecophaga_jefferyi",
            atlas.findScientificName("Pithecophaga jefferyi").editionScopedId);
        assertFalse(atlas.search("eagle", 10).isEmpty());
    }

    @Test public void unreviewedLinksRemainUnavailableToPublicAtlas() {
        LivingAtlasDataLayer.Snapshot atlas = LivingAtlasDataLayer.open(
            ApplicationProvider.getApplicationContext());
        assertTrue(atlas.pendingHabitatEdgeCount > 0);
        assertTrue(atlas.approvedHabitatEdges("Pithecophaga jefferyi").isEmpty());
        assertTrue(atlas.habitats().size() > 0);
    }
}
