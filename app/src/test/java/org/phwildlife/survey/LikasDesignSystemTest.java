package org.phwildlife.survey;
import org.junit.Test;
import static org.junit.Assert.*;

/** No Android dependencies: style measurements stay stable across activities. */
public final class LikasDesignSystemTest {
    @Test public void spacingHasAConsistentFourDpRhythm(){
        assertEquals(4,LikasDesignSystem.SPACE_XS);
        assertEquals(8,LikasDesignSystem.SPACE_SM);
        assertEquals(12,LikasDesignSystem.SPACE_MD);
        assertEquals(16,LikasDesignSystem.SPACE_LG);
        assertEquals(24,LikasDesignSystem.SPACE_XL);
        assertEquals(32,LikasDesignSystem.SPACE_XXL);
        assertEquals(16,LikasDesignSystem.SCREEN_GUTTER);
    }
    @Test public void responsiveCardsStayLegibleOnNarrowOrLargeType(){
        assertTrue(LikasDesignSystem.useSingleColumn(320,1f));
        assertTrue(LikasDesignSystem.useSingleColumn(360,1f));
        assertFalse(LikasDesignSystem.useSingleColumn(390,1f));
        assertTrue(LikasDesignSystem.useSingleColumn(412,1.3f));
        assertTrue(LikasDesignSystem.useSingleColumn(480,2f));
        assertFalse(LikasDesignSystem.useSingleColumn(480,1f));
        assertEquals(LikasDesignSystem.useSingleColumn(360,1.0f),
            FieldLayoutPolicy.stackPortalPair(360,1.0f));
    }
    @Test public void touchAndTextTokensRespectFieldUse(){
        assertTrue(LikasDesignSystem.MIN_TOUCH_DP>=48);
        assertTrue(LikasDesignSystem.TEXT_PAGE>LikasDesignSystem.TEXT_SECTION);
        assertTrue(LikasDesignSystem.TEXT_SECTION>LikasDesignSystem.TEXT_CAPTION);
        assertTrue(LikasDesignSystem.RADIUS_CARD>=LikasDesignSystem.RADIUS_BUTTON);
    }
}
