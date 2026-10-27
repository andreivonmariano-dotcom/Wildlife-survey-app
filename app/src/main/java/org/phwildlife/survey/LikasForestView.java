package org.phwildlife.survey;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

/**
 * Lightweight, offline, clock-aware ambient background for the LIKAS welcome screen.
 * The view is decorative only. Place interactive controls in a sibling view above it.
 */
public final class LikasForestView extends View {
    private static final long FRAME_INTERVAL_MS = 100L;
    private static final float TAU = (float) (Math.PI * 2.0);
    private static final String DAY_PATH = "likas_forest/forest_day.webp";
    private static final String NIGHT_PATH = "likas_forest/forest_night.webp";

    private final Paint bitmapPaint = new Paint();
    private final Paint effectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Rect source = new Rect();
    private final Rect destination = new Rect();
    private final RectF pixel = new RectF();
    private final Bitmap day;
    private final Bitmap night;
    private final long startedAt = SystemClock.uptimeMillis();

    private boolean motionEnabled = true;
    private float nightMix = 0f;
    private float targetNightMix = 0f;
    private long lastClockSample = 0L;
    private long lastFrame = 0L;
    private float sourceLeft = 0f;
    private float sourceSpan = 1f;

    public LikasForestView(Context context) {
        this(context, null);
    }

    public LikasForestView(Context context, AttributeSet attrs) {
        super(context, attrs);
        bitmapPaint.setAntiAlias(false);
        bitmapPaint.setFilterBitmap(false);
        day = load(context, DAY_PATH);
        night = load(context, NIGHT_PATH);
        nightMix = targetNightMix = clockNightFraction();
        setClickable(false);
        setFocusable(false);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    private static Bitmap load(Context context, String path) {
        try (InputStream stream = context.getAssets().open(path)) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;
            return BitmapFactory.decodeStream(stream, null, options);
        } catch (IOException exception) {
            return null;
        }
    }

    public void setMotionEnabled(boolean enabled) {
        motionEnabled = enabled;
        invalidate();
    }

    private static float ease(float value) {
        float t = Math.max(0f, Math.min(1f, value));
        return t * t * (3f - 2f * t);
    }

    private static float clockNightFraction() {
        Calendar clock = Calendar.getInstance();
        float hour = clock.get(Calendar.HOUR_OF_DAY)
                + clock.get(Calendar.MINUTE) / 60f;
        if (hour < 5.25f || hour >= 19.25f) return 1f;
        if (hour < 6.50f) return 1f - ease((hour - 5.25f) / 1.25f);
        if (hour < 17.75f) return 0f;
        return ease((hour - 17.75f) / 1.50f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        final int width = getWidth();
        final int height = getHeight();
        if (width <= 0 || height <= 0) return;

        final long now = SystemClock.uptimeMillis();
        if (now - lastClockSample > 30000L || lastClockSample == 0L) {
            targetNightMix = clockNightFraction();
            lastClockSample = now;
        }
        float elapsed = lastFrame == 0L ? 0f : Math.min(500f, now - lastFrame);
        lastFrame = now;
        nightMix += (targetNightMix - nightMix) * Math.min(1f, elapsed / 1800f);

        float seconds = motionEnabled ? (now - startedAt) / 1000f : 0f;
        canvas.drawColor(Color.rgb(8, 28, 31));
        Bitmap primary = day != null ? day : night;
        if (primary != null) {
            drawPlate(canvas, primary, 255, seconds);
            if (night != null && day != null) {
                drawPlate(canvas, night, Math.round(255f * nightMix), seconds);
            }
            if (motionEnabled) drawAmbientEffects(canvas, seconds, width, height);
        }

        if (motionEnabled && isShown() && ValueAnimator.areAnimatorsEnabled())
            postInvalidateDelayed(FRAME_INTERVAL_MS);
    }

    private void drawPlate(Canvas canvas, Bitmap plate, int alpha, float seconds) {
        float viewRatio = getWidth() / (float) getHeight();
        float span = Math.max(1f, Math.min(plate.getWidth(),
                plate.getHeight() * viewRatio));
        float maxLeft = Math.max(0f, plate.getWidth() - span);
        float range = Math.min(17f, maxLeft * 0.43f);
        float center = plate.getWidth() * 0.55f
                + (float) Math.sin(seconds * TAU / 34f) * range;
        float left = Math.max(0f, Math.min(maxLeft, center - span / 2f));
        sourceLeft = left;
        sourceSpan = span;

        source.set(Math.round(left), 0, Math.round(left + span), plate.getHeight());
        destination.set(0, 0, getWidth(), getHeight());
        bitmapPaint.setAlpha(alpha);
        canvas.drawBitmap(plate, source, destination, bitmapPaint);
    }

    private float screenX(float worldX) {
        return (worldX - sourceLeft) * getWidth() / sourceSpan;
    }

    private void drawAmbientEffects(Canvas canvas, float t, int w, int h) {
        // Gentle leaf movement at the edges without shifting the entire scene.
        for (int i = 0; i < 4; i++) {
            float x = i % 2 == 0 ? w * 0.025f : w * 0.975f;
            float y = h * (0.14f + 0.17f * (i / 2));
            float angle = (float) Math.sin(t * 0.65f + i) * 5f;
            canvas.save();
            canvas.rotate(angle, x, y);
            effectPaint.setColor(Color.argb(120, 24, 85, 47));
            pixel.set(x - w * 0.012f, y - h * 0.018f,
                    x + w * 0.012f, y + h * 0.018f);
            canvas.drawOval(pixel, effectPaint);
            canvas.restore();
        }

        // Clouds move over the exact supplied day/night forest artwork.
        for (int i = 0; i < 3; i++) {
            float wrap = w + w * 0.3f;
            float x = ((w * (0.16f + i * 0.39f) + t * (2.2f + i)) % wrap);
            float y = h * (0.12f + i * 0.065f);
            effectPaint.setColor(Color.argb(Math.round(17f * (1f - nightMix)),
                    239, 251, 249));
            pixel.set(x, y, x + w * 0.075f, y + Math.max(2f, h * 0.003f));
            canvas.drawRect(pixel, effectPaint);
        }

        // Short repeating fall highlights and moving surface ripples.
        effectPaint.setStrokeWidth(Math.max(2f, w * 0.005f));
        for (int i = 0; i < 2; i++) {
            float x = screenX(i == 0 ? 129f : 151f);
            if (x > 0 && x < w) {
                float offset = (t * 18f + i * 23f) % (h * 0.12f);
                effectPaint.setColor(Color.argb(95, 214, 249, 255));
                canvas.drawLine(x, h * 0.49f + offset,
                        x, h * 0.505f + offset, effectPaint);
            }
        }
        for (int i = 0; i < 7; i++) {
            float x = screenX(109f + i * 8f
                    + (float) Math.sin(t * 0.7f + i) * 0.8f);
            if (x > 0 && x < w) {
                float y = h * (0.67f + i % 3 * 0.018f);
                effectPaint.setColor(Color.argb(50 + (i % 3) * 18,
                        195, 244, 248));
                canvas.drawLine(x, y, x + w * 0.025f, y, effectPaint);
            }
        }

        // Night fireflies complement the original waterfall and foliage.
        for (int i = 0; i < 10; i++) {
            float baseX = (i % 2 == 0 ? 0.08f : 0.90f)
                    + (i / 2) * (i % 2 == 0 ? 0.025f : -0.02f);
            float x = w * baseX + (float) Math.sin(t * 0.3f + i * 2f) * w * 0.018f;
            float y = h * (0.19f + (i % 5) * 0.135f)
                    + (float) Math.cos(t * 0.48f + i) * h * 0.009f;
            float pulse = 0.5f + 0.5f * (float) Math.sin(t * 1.25f + i * 1.7f);
            int alpha = Math.round((30f + 115f * nightMix) * pulse);
            effectPaint.setColor(Color.argb(alpha, 235, 254, 145));
            float diameter = Math.max(2f, w * 0.006f);
            pixel.set(x, y, x + diameter, y + diameter);
            canvas.drawRect(pixel, effectPaint);
        }
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == VISIBLE && motionEnabled && ValueAnimator.areAnimatorsEnabled()) {
            lastFrame = 0L;
            postInvalidateDelayed(FRAME_INTERVAL_MS);
        }
    }
}
