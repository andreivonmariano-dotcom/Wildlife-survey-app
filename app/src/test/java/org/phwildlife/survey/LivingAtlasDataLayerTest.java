package org.phwildlife.survey;

import org.junit.Test;
import static org.junit.Assert.*;

public final class LivingAtlasDataLayerTest {
    @Test public void idsAreExplicitlyScopedToBookEdition() {
        assertEquals("book01@1.82:pithecophaga_jefferyi",
            LivingAtlasDataLayer.editionScopedId(" Pithecophaga   jefferyi "));
        assertEquals("book01@1.82:oreophryne_cf._nana",
            LivingAtlasDataLayer.editionScopedId("Oreophryne cf. nana"));
        assertEquals("", LivingAtlasDataLayer.editionScopedId(" "));
    }

    @Test public void provisionalOrUnreviewedEdgesNeverBecomeVerifiedAtlasFacts() {
        assertFalse(LivingAtlasDataLayer.approvedHabitatLink(
            "source_excerpt_checked_crosswalk_pending_specialist_review",
            "approved","approved",false));
        assertFalse(LivingAtlasDataLayer.approvedHabitatLink(
            "approved","literature_synthesis_pending_specialist_review",
            "approved",false));
        assertFalse(LivingAtlasDataLayer.approvedHabitatLink(
            "approved","approved","provisional_physiognomic_crosswalk",false));
        assertFalse(LivingAtlasDataLayer.approvedHabitatLink(
            "approved","approved","approved",true));
        assertTrue(LivingAtlasDataLayer.approvedHabitatLink(
            "approved","approved","approved",false));
    }
}
