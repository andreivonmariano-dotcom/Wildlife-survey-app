package org.phwildlife.survey;
import org.junit.Test;
import static org.junit.Assert.*;
public final class SurveyMathTest {
    @Test public void twoKmSpacing250HasNinePoints(){
        assertEquals(9,SurveyMath.stations(2000,250));
        assertEquals(0,SurveyMath.chainage(0,250),0.001);
        assertEquals(2000,SurveyMath.chainage(8,250),0.001);
    }
    @Test public void spacingOptions(){
        assertEquals(21,SurveyMath.stations(2000,100));
        assertEquals(9,SurveyMath.stations(2000,250));
        assertEquals(5,SurveyMath.stations(2000,500));
    }
    @Test public void remainderIsNotExtraStation(){
        assertEquals(9,SurveyMath.stations(2100,250));
        assertEquals(2000,SurveyMath.chainage(8,250),0.001);
    }
    @Test public void lengthEqualSpacing(){
        assertEquals(2,SurveyMath.stations(250,250));
    }
    @Test(expected=IllegalArgumentException.class)
    public void rejectZeroSpacing(){SurveyMath.stations(1000,0);}
    @Test(expected=IllegalArgumentException.class)
    public void rejectNaN(){SurveyMath.stations(Double.NaN,250);}
    @Test(expected=IllegalArgumentException.class)
    public void rejectOverflow(){SurveyMath.stations(1e20,1);}
}
