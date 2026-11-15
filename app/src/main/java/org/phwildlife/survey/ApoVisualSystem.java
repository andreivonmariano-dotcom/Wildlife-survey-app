package org.phwildlife.survey;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

/**
 * Shared semantic palette and dimensions for APO's field and reading surfaces.
 * The original asset artwork, survey database and reader typography remain unchanged.
 */
public final class ApoVisualSystem {
    private ApoVisualSystem() {}

    public static final int FIELD_BACKGROUND = Color.rgb(247,248,242);
    public static final int READING_BACKGROUND = Color.rgb(245,242,233);
    public static final int SURFACE = Color.WHITE;
    public static final int READING_SURFACE = Color.rgb(254,252,245);
    public static final int INK = Color.rgb(19,51,38);
    public static final int MUTED = Color.rgb(81,106,87);
    public static final int FOREST = Color.rgb(33,83,53);
    public static final int FOREST_DEEP = Color.rgb(10,42,29);
    public static final int POND = Color.rgb(46,105,65);
    public static final int LEAF_PALE = Color.rgb(237,246,231);
    public static final int LEAF_MID = Color.rgb(228,239,220);
    public static final int BORDER = Color.rgb(217,228,210);
    public static final int INPUT_BORDER = Color.rgb(117,143,123);
    public static final int READING_BORDER = Color.rgb(208,216,202);
    public static final int ACCENT = Color.rgb(205,230,160);
    public static final int CAUTION = Color.rgb(145,85,20);
    public static final int CAMERA = Color.rgb(40,88,137);

    public static int dp(Context c,float value) {
        return Math.round(c.getResources().getDisplayMetrics().density*value);
    }

    public static boolean largeText(Context c) {
        return c.getResources().getConfiguration().fontScale>=1.6f;
    }

    public static GradientDrawable surface(Context c,int fill,int stroke,int radiusDp) {
        GradientDrawable result=new GradientDrawable();
        result.setColor(fill);
        result.setCornerRadius(dp(c,radiusDp));
        if(stroke!=Color.TRANSPARENT)result.setStroke(dp(c,1),stroke);
        return result;
    }
}
