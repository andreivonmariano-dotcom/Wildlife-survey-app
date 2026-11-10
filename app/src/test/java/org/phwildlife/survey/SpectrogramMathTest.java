package org.phwildlife.survey;

import org.junit.Test;
import static org.junit.Assert.*;

public final class SpectrogramMathTest {
    @Test public void silenceHasDarkBands(){
        float[] bands=SpectrogramMath.analyze(new short[1024],0);
        assertEquals(64,bands.length);
        for(float b:bands)assertEquals(0f,b,0.001f);
    }
    @Test public void pureToneHasVisibleEnergyNearFrequency(){
        short[] samples=new short[1024];
        for(int i=0;i<samples.length;i++)
            samples[i]=(short)(Math.sin(2*Math.PI*2000*i/44100d)*23000);
        float[] bands=SpectrogramMath.analyze(samples,0);
        float peak=0f;
        int index=-1;
        for(int i=0;i<bands.length;i++)if(bands[i]>peak){peak=bands[i];index=i;}
        assertTrue(peak>.1f);
        assertTrue(index>=20 && index<=53);
    }
    @Test(expected=IllegalArgumentException.class)
    public void partialFrameRejected(){SpectrogramMath.analyze(new short[10],0);}
}
