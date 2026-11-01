package org.phwildlife.survey;

/** Stable Android-independent visual measurements for LIKAS field and reference screens.
 * All dimensions are dp, all text sizes are sp. No user data or biological claims live here. */
public final class LikasDesignSystem {
    private LikasDesignSystem(){}

    // 4dp rhythm. A component may use a half step only for an icon optical adjustment.
    public static final int SPACE_XS=4, SPACE_SM=8, SPACE_MD=12;
    public static final int SPACE_LG=16, SPACE_XL=24, SPACE_XXL=32;

    public static final int SCREEN_GUTTER=16;
    public static final int RADIUS_INPUT=14, RADIUS_BUTTON=16;
    public static final int RADIUS_CARD=19, RADIUS_HERO=26;

    public static final int TEXT_PAGE=23, TEXT_SECTION=17, TEXT_CARD=16;
    public static final int TEXT_BODY=15, TEXT_CAPTION=13;
    public static final int TEXT_CONTROL=14;

    public static final int MIN_TOUCH_DP=48;

    /** On narrow phones, favor readable full-width cards over a nominal two-column grid. */
    public static boolean useSingleColumn(int screenWidthDp,float fontScale){
        return screenWidthDp<390||fontScale>=1.30f;
    }
}
