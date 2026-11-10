package org.phwildlife.survey;
import org.junit.Test;
import static org.junit.Assert.*;
public final class ForestMotionPolicyTest {
    @Test public void boundsStayInPanorama(){
        for(int n=0;n<=960;n++){
            float fraction=ForestMotionPolicy.pan(n/10f);
            assertTrue(fraction>=.129f);
            assertTrue(fraction<=.871f);
        }
    }
    @Test public void endlessLoopIsContinuous(){
        float beginning=ForestMotionPolicy.pan(0f);
        assertEquals(beginning,ForestMotionPolicy.pan(96f),.000001f);
        assertEquals(ForestMotionPolicy.pan(.01f),ForestMotionPolicy.pan(96.01f),.000001f);
        assertEquals(.87f,ForestMotionPolicy.pan(24f),.000001f);
        assertEquals(.13f,ForestMotionPolicy.pan(72f),.000001f);
    }
    @Test public void frameToFrameMovementIsSmall(){
        float previous=ForestMotionPolicy.pan(0f);
        for(int n=1;n<=2880;n++){
            float next=ForestMotionPolicy.pan(n/30f);
            assertTrue(Math.abs(next-previous)<.001f);
            previous=next;
        }
    }
    @Test(expected=IllegalArgumentException.class)
    public void invalidTimestampIsRejected(){ForestMotionPolicy.pan(Float.NaN);}
}