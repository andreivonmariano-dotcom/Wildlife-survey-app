package org.phwildlife.survey;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.os.PowerManager;
import android.os.SystemClock;
import android.view.View;

import java.util.Calendar;

/**
 * Offline, vector-drawn rainforest scene for the LIKAS welcome hero.
 *
 * Independent planes: clouds, ridges, mid-canopy, fog, water and near leaves.
 * No large bitmaps, no raster-upscaling artifacts, no UI interception.
 * Frame scheduling follows display vsync, with battery/reduced-motion fallback.
 */
public final class RainforestBackdropView extends View {
    private static final float W = 360f, H = 242f;
    private static final float TAU = (float) (Math.PI * 2.0);
    private static final int SKY_TOP = 0, SKY_BOTTOM = 1, FAR = 2, NEAR = 3,
        CANOPY = 4, LEAVES = 5, WATER = 6, SPARKLE = 7, CLOUD = 8;
    // morning, noon, sunset, night. Same objects in every lighting state.
    private static final int[][] COLORS = {
        {0xFFF5C697,0xFFB6C9D3,0xFF7B9B9D,0xFF476E68,0xFF3F795F,0xFF224C3D,0xFF477E88,0xFFDFEBC7,0xFFF8DFB7},
        {0xFF77C8EF,0xFFE1E9C8,0xFF86B3A5,0xFF3F8575,0xFF267754,0xFF124D37,0xFF338B98,0xFFDBFFF2,0xFFF5FAF0},
        {0xFFF8A372,0xFFF3C4A0,0xFF806B83,0xFF665571,0xFF375E53,0xFF173D39,0xFF3D6077,0xFFFFCFA9,0xFFFFDCCB},
        {0xFF081D39,0xFF234363,0xFF304D67,0xFF1B3D4A,0xFF173C40,0xFF0A292D,0xFF204456,0xFF82C2C3,0xFF59788D}
    };

    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private final int[] colors = new int[9];
    private final float[] currentLight = new float[4];
    private final PowerManager powerManager;
    private LinearGradient sky;
    private boolean ambientEnabled = true;
    private long clockCheckAt;
    private int lastMinute = -1;
    private long motionEpochMs = SystemClock.uptimeMillis();
    private long elapsedAtPauseMs;
    private boolean pauseRecorded;

    public RainforestBackdropView(Context context) {
        super(context);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        setFocusable(false);
        powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        updateClock();
    }

    /**
     * Explicitly pause from Activity.onPause(). Detachment also ends drawing.
     * Re-enable only when the home surface is foregrounded.
     */
    public void setAmbientEnabled(boolean enabled) {
        if (ambientEnabled == enabled) return;
        ambientEnabled = enabled;
        if (!enabled) {
            elapsedAtPauseMs = SystemClock.uptimeMillis() - motionEpochMs;
            pauseRecorded = true;
        } else {
            if (pauseRecorded) motionEpochMs = SystemClock.uptimeMillis() - elapsedAtPauseMs;
            pauseRecorded = false;
            updateClock();
        }
        invalidate();
    }

    private boolean canAnimate() {
        return ambientEnabled && isShown() && getWindowVisibility() == VISIBLE
            && ValueAnimator.areAnimatorsEnabled()
            && (powerManager == null || !powerManager.isPowerSaveMode());
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        updateClock();
        invalidate();
    }

    @Override protected void onDetachedFromWindow() {
        // No Handler or scheduled Runnable survives removal of the welcome hero.
        super.onDetachedFromWindow();
    }

    @Override public void onVisibilityAggregated(boolean isVisible) {
        super.onVisibilityAggregated(isVisible);
        if (isVisible) invalidate();
    }

    private void updateClock() {
        Calendar now = Calendar.getInstance(); // Phone's local time zone.
        int minute = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
        if (minute == lastMinute) return;
        lastMinute = minute;
        float[] weights = RainforestMotion.lightWeights(minute);
        System.arraycopy(weights, 0, currentLight, 0, weights.length);
        for (int k = 0; k < colors.length; k++) {
            float red = 0, green = 0, blue = 0;
            for (int i = 0; i < 4; i++) {
                red += Color.red(COLORS[i][k]) * weights[i];
                green += Color.green(COLORS[i][k]) * weights[i];
                blue += Color.blue(COLORS[i][k]) * weights[i];
            }
            colors[k] = Color.rgb(Math.round(red), Math.round(green), Math.round(blue));
        }
        sky = new LinearGradient(0, 0, 0, H, colors[SKY_TOP], colors[SKY_BOTTOM], Shader.TileMode.CLAMP);
    }

    private void color(int argb) {
        // Keep Paint allocated and configured. Resetting it for every leaf
        // creates avoidable UI-thread work on high-refresh-rate devices.
        p.setStyle(Paint.Style.FILL);
        p.setColor(argb);
    }

    private void rect(Canvas c, int argb, float l, float t, float r, float b) {
        color(argb);
        c.drawRect(l, t, r, b, p);
    }

    private void oval(Canvas c, int argb, float l, float t, float r, float b) {
        color(argb);
        c.drawOval(l, t, r, b, p);
    }

    private void circle(Canvas c, int argb, float x, float y, float radius) {
        color(argb);
        c.drawCircle(x, y, radius, p);
    }

    private static int alpha(int c, int a) {
        return (c & 0xFFFFFF) | (Math.max(0, Math.min(255, a)) << 24);
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getWidth() == 0 || getHeight() == 0) return;

        long uptime = SystemClock.uptimeMillis();
        if (uptime >= clockCheckAt) {
            updateClock();
            clockCheckAt = uptime + 5000L;
        }
        boolean moving = canAnimate();
        float time = (moving ? uptime - motionEpochMs : (pauseRecorded ? elapsedAtPauseMs : 0L)) / 1000f;

        int save = canvas.save();
        canvas.scale(getWidth() / W, getHeight() / H);
        sky(canvas);
        starsAndSun(canvas, time);
        clouds(canvas, time);
        distantMountains(canvas, time);
        midCanopy(canvas, time);
        movingMist(canvas, time);
        riverAndWaterfall(canvas, time);
        nearLeaves(canvas, time);
        fireflies(canvas, time);
        canvas.restoreToCount(save);
        if (moving) postInvalidateOnAnimation();
    }

    private void sky(Canvas c) {
        color(Color.WHITE);
        p.setShader(sky);
        c.drawRect(0, 0, W, H, p);
        p.setShader(null);
    }

    private void starsAndSun(Canvas c, float time) {
        float[] w = currentLight;
        float night = w[3], day = w[0] + w[1];
        if (day > 0.02f) {
            circle(c, alpha(0xFFFFF5C3, Math.round(180 * day)), 279, 46, 17);
            circle(c, alpha(0xFFFFF9D6, Math.round(240 * day)), 279, 46, 10);
        }
        if (night > 0.02f) {
            circle(c, alpha(0xFFF2F4D4, Math.round(230 * night)), 285, 49, 9);
            // Deterministic star coordinates. No per-frame random flicker.
            for (int i = 0; i < 22; i++) {
                float x = 11 + (i * 113 % 340);
                float y = 9 + (i * 47 % 89);
                int a = (int) (night * (110 + 70 * (0.5f +
                    0.5f * RainforestMotion.wave(time, 9f + i % 5, i))));
                circle(c, alpha(0xFFE7EED9, a), x, y, i % 4 == 0 ? 1f : 0.55f);
            }
        }
    }

    // Cloud plane moves horizontally independently of ridges and foreground foliage.
    private void clouds(Canvas c, float t) {
        float shift = 12f * RainforestMotion.wave(t, 64f, .65f);
        float shift2 = 8f * RainforestMotion.wave(t, 87f, 2f);
        drawCloud(c, 42 + shift, 49, .9f);
        drawCloud(c, 202 + shift2, 78, 1.25f);
        drawCloud(c, 307 - shift * .4f, 21, .53f);
    }

    private void drawCloud(Canvas c, float x, float y, float scale) {
        int pale = alpha(colors[CLOUD], lastMinute >= 1200 || lastMinute < 350 ? 55 : 140);
        c.save();
        c.translate(x, y);
        c.scale(scale, scale);
        oval(c, pale, -24, 0, 24, 9);
        oval(c, pale, -12, -6, 15, 9);
        oval(c, pale, -2, -11, 15, 8);
        c.restore();
    }

    private void mountain(Canvas c, int shade, float dx, float top, float roughness) {
        c.save();
        c.translate(dx, 0f);
        path.reset();
        path.moveTo(-28, 176);
        for (int x = -28; x <= 386; x += 8) {
            float peak1 = (float) Math.exp(-Math.pow((x - 112f) / 66f, 2));
            float peak2 = (float) Math.exp(-Math.pow((x - 274f) / 80f, 2));
            float edge = (float) Math.sin(x * .075f) * roughness +
                (float) Math.sin(x * .19f) * roughness * .3f;
            float y = top - peak1 * 53f - peak2 * 39f + edge;
            path.lineTo(x, y);
        }
        path.lineTo(388, 205);
        path.lineTo(-28, 205);
        path.close();
        color(shade);
        c.drawPath(path, p);
        c.restore();
    }

    // Far and near ridges have different travel periods and amplitudes.
    private void distantMountains(Canvas c, float t) {
        mountain(c, alpha(colors[FAR], 172),
            1.8f * RainforestMotion.wave(t, 104f, 0f), 145, 2f);
        mountain(c, colors[NEAR],
            2.7f * RainforestMotion.wave(t, 78f, 1.7f), 178, 3.7f);
    }

    private void midCanopy(Canvas c, float t) {
        c.save();
        c.translate(3.4f * RainforestMotion.wave(t, 47f, 0f), 0);
        for (int i = 0; i < 29; i++) {
            float x = -14 + i * 14.3f;
            float y = 150 + 13 * (float) Math.sin(i * 1.55f);
            float size = 11 + (i * 17 % 19);
            oval(c, colors[CANOPY], x-size, y-size, x+size, y+14);
            if (i % 3 == 0) oval(c, alpha(colors[SPARKLE], 30),
                x-size*.6f, y-size*.7f, x+3, y-size*.15f);
        }
        // Tree silhouettes on separate mid plane.
        for (int i = 0; i < 10; i++) {
            float x = i * 43f - 14f;
            rect(c, colors[LEAVES], x, 151, x + 2.5f, 204);
            oval(c, colors[CANOPY], x - 12, 131 - (i%3)*9, x + 16, 174);
        }
        c.restore();
    }

    private void movingMist(Canvas c, float t) {
        float dx = 14f * RainforestMotion.wave(t, 93f, .8f);
        oval(c, alpha(colors[CLOUD], 35), -90 + dx, 136, 200 + dx, 151);
        oval(c, alpha(colors[CLOUD], 28), 95 - dx * .4f, 157, 460 - dx * .4f, 168);
    }

    private void riverAndWaterfall(Canvas c, float t) {
        rect(c, colors[WATER], 0, 184, W, H);
        // Distant waterfall has a slower independent vertical current.
        float foam = RainforestMotion.wave(t, 2.7f, 0f);
        rect(c, alpha(colors[SPARKLE], 180), 207, 149, 239, 200);
        rect(c, alpha(colors[CLOUD], 125), 211, 150, 217, 193);
        rect(c, alpha(colors[CLOUD], 135), 225, 153, 230, 196);
        rect(c, alpha(colors[WATER], 125), 218, 151, 223, 193);
        for (int i = 0; i < 7; i++) {
            float y = 151 + ((i * 11f + t * (13f + i % 3)) % 45f);
            rect(c, alpha(colors[SPARKLE], 70), 209 + i * 4, y,
                211 + i * 4, y + 4f);
        }
        oval(c, alpha(colors[SPARKLE], 145), 196, 190 + foam, 247, 200 + foam);
        // River reflections are independent of waterfall flow.
        for (int i = 0; i < 35; i++) {
            float y = 197 + (i * 19 % 41);
            float x = (i * 73 + (t * (4 + i%4))) % 396 - 18;
            float width = 4 + i % 5 * 3;
            rect(c, alpha(colors[SPARKLE], 55 + i % 3 * 24),
                x, y, x + width, y + (i%3==0 ? 2f : 1f));
        }
        // Foreground riverbanks and stone forms.
        oval(c, colors[LEAVES], -42, 194, 95, 270);
        oval(c, colors[CANOPY], 265, 202, 420, 277);
        oval(c, alpha(colors[FAR], 230), 39, 212, 72, 227);
        oval(c, alpha(colors[FAR], 230), 287, 196, 322, 213);
    }

    // Foreground fronds translate and sway independently, never as a flattened plate.
    private void nearLeaves(Canvas c, float t) {
        int deep = colors[LEAVES];
        float leftSway = 3.3f * RainforestMotion.wave(t, 6.7f, .4f);
        float rightSway = 4.6f * RainforestMotion.wave(t, 8.4f, 1.2f);
        drawFrond(c, 5, 3, 85 + leftSway, 80, deep, t, 0f);
        drawFrond(c, 355, 4, 271 + rightSway, 91, deep, t, 2f);
        drawFrond(c, -7, 184, 56 + leftSway, 123, deep, t, .8f);
        drawFrond(c, 372, 166, 306 + rightSway, 107, deep, t, 2.7f);
        // Vines and little near-leaf clusters.
        color(alpha(deep, 230));
        p.setStrokeWidth(3);
        c.drawLine(31, 0, 30 + leftSway, 38, p);
        c.drawLine(331, 0, 335 + rightSway, 38, p);
        oval(c, deep, 16 + leftSway, 18, 39 + leftSway, 38);
        oval(c, deep, 322 + rightSway, 24, 349 + rightSway, 46);
    }

    private void drawFrond(Canvas c, float ox, float oy, float tx, float ty,
                           int shade, float time, float seed) {
        color(alpha(shade, 230));
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(3.4f);
        c.drawLine(ox, oy, tx, ty, p);
        p.setStyle(Paint.Style.FILL);
        for (int j = 1; j <= 9; j++) {
            float f = j / 10f;
            float sway = 2 * RainforestMotion.wave(time, 5.8f + j%4, seed + j*.4f);
            float x = ox + (tx-ox)*f + sway;
            float y = oy + (ty-oy)*f;
            float r = 7f + (j%3)*2.5f;
            oval(c, shade, x-r*1.4f, y-r*.65f, x+r*.4f, y+r*.8f);
            oval(c, alpha(colors[CANOPY], 230), x-r*.25f, y-r*.5f,
                x+r*1.4f, y+r*.7f);
        }
    }

    private void fireflies(Canvas c, float t) {
        float[] weights = currentLight;
        float strength = weights[3] + weights[2]*.15f;
        if (strength <= .01f) return;
        for (int i = 0; i < 11; i++) {
            float x = 17 + i*31 + 4 * RainforestMotion.wave(t, 5f + i, i);
            float y = 130 + (i*29 % 98) + 3 * RainforestMotion.wave(t, 7f + i%4, i*.5f);
            float pulse = .5f + .5f * RainforestMotion.wave(t, 2.4f + i%5, i);
            circle(c, alpha(0xFFF4E8A0, Math.round(strength*(50 + 90*pulse))),
                x,y,2.5f);
            circle(c, alpha(0xFFFFEF9E, Math.round(strength*(125 + 95*pulse))),
                x,y,.8f);
        }
    }
}