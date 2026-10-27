package org.phwildlife.survey;

/** Pure-Java camera travel curve for the offline rainforest illustration. */
public final class ForestMotionPolicy {
    private ForestMotionPolicy(){}
    public static final float LOOP_SECONDS=96f;
    public static float pan(float seconds){
        if(!Float.isFinite(seconds))throw new IllegalArgumentException("Finite animation time required");
        // Range .13..87: crop stays inside a long panoramic image.
        return .5f+.37f*(float)Math.sin(seconds*Math.PI*2/LOOP_SECONDS);
    }
}