package org.phwildlife.survey;

/** Measurable rules for compact and large-type Android screens. */
public final class FieldLayoutPolicy {
    private FieldLayoutPolicy(){}
    // Portal rows must also respond to width, not only Android's large-text setting.
    public static boolean stackPortalPair(int availableWidthDp,float fontScale){
        return LikasDesignSystem.useSingleColumn(availableWidthDp,fontScale);
    }
    public static boolean stackSurveyTiles(int availableWidthDp,float fontScale){
        return stackPortalPair(availableWidthDp,fontScale);
    }
    public static boolean stackSpeciesEntry(int availableWidthDp,float fontScale){
        return availableWidthDp<350||fontScale>=1.55f;
    }
    public static int actionHeightDp(float fontScale){
        float scale=Float.isFinite(fontScale)?Math.max(1f,fontScale):1f;
        return Math.max(48,Math.min(96,Math.round(50f*scale)));
    }
    public static int saveHeightDp(float fontScale){
        return Math.max(56,Math.min(110,Math.round(56f*Math.max(1f,fontScale))));
    }
}
