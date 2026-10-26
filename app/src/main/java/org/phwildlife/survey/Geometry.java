package org.phwildlife.survey;
public final class Geometry {
    private Geometry() {}
    public static double normalize(double a) { return (a % 360 + 360) % 360; }
    public static double perpendicular(double horizontal, double targetAz, double transectAz) {
        return horizontal * Math.abs(Math.sin(Math.toRadians(normalize(targetAz - transectAz))));
    }
    // Android TYPE_ROTATION_VECTOR -> ENU world coordinates. Back-facing camera points along device -Z.
    public static float[] cameraAxisHeadingAndPitch(float[] r) {
        double east=-r[2], north=-r[5], up=-r[8];
        double planar=Math.hypot(east,north);
        if(planar < 0.15) return new float[] { Float.NaN, (float)Math.toDegrees(Math.atan2(up,planar)) };
        return new float[] {(float)normalize(Math.toDegrees(Math.atan2(east,north))), (float)Math.toDegrees(Math.atan2(up,planar)) };
    }
}
