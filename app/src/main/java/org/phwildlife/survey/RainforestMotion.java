package org.phwildlife.survey;

/** Pure, deterministic ambient-motion math. No Android dependencies or location permissions. */
public final class RainforestMotion {
    private RainforestMotion() {}

    public static float clamp(float v) {
        return Math.max(0f, Math.min(1f, v));
    }

    public static float smooth(float v) {
        float t = clamp(v);
        return t * t * (3f - 2f * t);
    }

    private static float fraction(int minute, int from, int to) {
        return smooth((minute - from) / (float) (to - from));
    }

    /**
     * Local clock minute in [0,1439]. Weights: morning, day, sunset, night.
     * Same scenery geometry is used in all four states.
     */
    public static float[] lightWeights(int localMinute) {
        if (localMinute < 0 || localMinute >= 1440)
            throw new IllegalArgumentException("Local minute must be 0..1439");
        float[] w = new float[4];
        if (localMinute < 300) {
            w[3] = 1f;
        } else if (localMinute < 396) {
            float f = fraction(localMinute, 300, 396);
            w[3] = 1f - f;
            w[0] = f;
        } else if (localMinute < 540) {
            float f = fraction(localMinute, 396, 540);
            w[0] = 1f - f;
            w[1] = f;
        } else if (localMinute < 960) {
            w[1] = 1f;
        } else if (localMinute < 1080) {
            float f = fraction(localMinute, 960, 1080);
            w[1] = 1f - f;
            w[2] = f;
        } else if (localMinute < 1200) {
            float f = fraction(localMinute, 1080, 1200);
            w[2] = 1f - f;
            w[3] = f;
        } else {
            w[3] = 1f;
        }
        return w;
    }

    public static float wave(float seconds, float periodSeconds, float phase) {
        if (!(periodSeconds > 0)) throw new IllegalArgumentException("periodSeconds");
        return (float) Math.sin(seconds * (2f * Math.PI / periodSeconds) + phase);
    }
}