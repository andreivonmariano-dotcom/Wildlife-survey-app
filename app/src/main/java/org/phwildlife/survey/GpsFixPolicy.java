package org.phwildlife.survey;

/** Pure-Java acceptance criteria for an actual, recent GNSS survey-start fix. */
public final class GpsFixPolicy {
    private GpsFixPolicy() {}
    public static final float MAX_ACCURACY_EXCLUSIVE_M = 10f;
    public static final long MAX_FIX_AGE_MS = 10_000L;

    public static boolean acceptable(double latitude, double longitude,
                                     float accuracyMeters, long fixTimeMs,
                                     long nowMs, boolean mockLocation) {
        return !mockLocation
            && Double.isFinite(latitude) && Double.isFinite(longitude)
            && latitude >= -90d && latitude <= 90d
            && longitude >= -180d && longitude <= 180d
            && Float.isFinite(accuracyMeters)
            && accuracyMeters > 0f && accuracyMeters < MAX_ACCURACY_EXCLUSIVE_M
            && fixTimeMs > 0 && nowMs >= fixTimeMs
            && nowMs - fixTimeMs <= MAX_FIX_AGE_MS;
    }
}
