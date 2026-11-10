package org.phwildlife.survey;
import org.junit.Test;
import static org.junit.Assert.*;
public final class AcousticViewportTest {
    @Test public void frequenciesAreIncreasingTowardTop(){
        assertEquals(17000d,AcousticViewport.frequencyAt(0f),1d);
        assertEquals(85d,AcousticViewport.frequencyAt(1f),1d);
        assertTrue(AcousticViewport.frequencyAt(.6f)<AcousticViewport.frequencyAt(.2f));
    }
    @Test public void seeksRemainInsideWindow(){
        assertEquals(3200L,AcousticViewport.timeAt(.2f,10000,2000,8000));
        assertEquals(8000L,AcousticViewport.timeAt(2f,10000,2000,8000));
    }
    @Test public void zoomBoundsNeverEscapeTheClip(){
        long[] w=AcousticViewport.window(60000,58000,8);
        assertTrue(w[0]>=0);
        assertTrue(w[1]<=60000);
        assertEquals(7500L,w[1]-w[0]);
    }
}
