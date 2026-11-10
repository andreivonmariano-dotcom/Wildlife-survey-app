package org.phwildlife.survey;
import static org.junit.Assert.*;
import org.junit.Test;
public final class GpsElevationPolicyTest{
 @Test public void mslAndUncertaintyAreRequired(){
    assertTrue(GpsElevation.validForHabitat(true,true,35d));
    assertTrue(GpsElevation.validForHabitat(true,true,50d));
    assertFalse(GpsElevation.validForHabitat(true,false,5d));
    assertFalse(GpsElevation.validForHabitat(true,true,null));
    assertFalse(GpsElevation.validForHabitat(false,true,5d));
    assertFalse(GpsElevation.validForHabitat(true,true,51d));
    assertFalse(GpsElevation.validForHabitat(true,true,Double.NaN));
 }
 @Test public void unknownVerticalErrorNeverPasses(){
    assertFalse(GpsElevation.validForHabitat(true,true,-1d));
    assertFalse(GpsElevation.validForHabitat(true,true,Double.POSITIVE_INFINITY));
 }
}
