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
        // A compact "List" shortcut keeps species and count in the first viewport.
        // Stack only when there is truly insufficient horizontal space.
        return availableWidthDp<300||(fontScale>=2.4f&&availableWidthDp<360);
    }
    public static int actionHeightDp(float fontScale){
        float scale=Float.isFinite(fontScale)?Math.max(1f,fontScale):1f;
        return Math.max(48,Math.min(96,Math.round(50f*scale)));
    }
    /**
     * Keep secondary survey actions side-by-side when actual screen width can
     * fit both labels. Other portal pairs may still stack at this width.
     * At very large fonts on narrow phones, preserve two full-width targets.
     */
    public static boolean stackRecorderDock(int widthDp,float fontScale){
        if(widthDp<320)return true;
        float scale=Float.isFinite(fontScale)?Math.max(1f,fontScale):1f;
        return scale>=2.4f&&widthDp<360;
    }
    public static int saveHeightDp(float fontScale){
        float scale=Float.isFinite(fontScale)?Math.max(1f,fontScale):1f;
        // A single large target remains reachable without consuming the form.
        return Math.max(56,Math.min(80,Math.round(50f*scale)));
    }
}
