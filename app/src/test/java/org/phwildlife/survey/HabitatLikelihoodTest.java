package org.phwildlife.survey;

import org.junit.Test;
import static org.junit.Assert.*;

public final class HabitatLikelihoodTest {
    private HabitatLikelihood.Result rate(int geo,String site,Double elevation,Double err,
            String habitats,boolean strict,Double min,Double max,boolean reviewed){
        return HabitatLikelihood.assess(geo,site,elevation,err,habitats,strict,min,max,reviewed);
    }
    @Test public void provinceCitedIsPreliminaryLikely(){
        assertEquals("Likely",rate(2,"",null,null,"",false,null,null,false).label);
    }
    @Test public void islandNeedsAdditionalPositiveHabitatOrElevation(){
        assertEquals("Insufficient Data",rate(1,"",null,null,"",false,null,null,false).label);
        assertEquals("Likely",rate(1,"montane",850d,30d,"montane|mossy",false,
            500d,1400d,true).label);
    }
    @Test public void noRegionDoesNotSilentlyMeanAbsence(){
        assertEquals("Insufficient Data",rate(0,"",null,null,"",false,null,null,false).label);
        assertEquals("Insufficient Data",rate(0,"lowland",650d,40d,"mossy",false,
            1000d,2000d,true).label);
    }
    @Test public void unlikelyRequiresTwoReviewedConflicts(){
        HabitatLikelihood.Result result=rate(0,"grassland",100d,15d,
            "mossy|montane",true,1000d,2200d,true);
        assertEquals("Unlikely",result.label);
        assertTrue(result.habitatConflict);
        assertTrue(result.elevationConflict);
    }
    @Test public void oneConflictNeverClaimsUnlikely(){
        assertEquals("Insufficient Data",rate(0,"grassland",1250d,15d,
            "mossy",true,1000d,2000d,true).label);
        assertEquals("Insufficient Data",rate(0,"mossy",100d,15d,
            "mossy",true,1000d,2000d,true).label);
    }
    @Test public void lackOfReviewedProfileDoesNotPenalize(){
        assertEquals("Likely",rate(2,"urban",20d,5d,"mossy",true,
            1000d,2000d,false).label);
    }
    @Test public void altitudeUncertaintyNearThresholdIsNeutral(){
        assertEquals("Insufficient Data",rate(0,"",945d,45d,"",false,
            1000d,1700d,true).label);
    }
    @Test public void indexIsWeightedAndBounded(){
        assertEquals(Integer.valueOf(60),rate(2,"",null,null,
            "",false,null,null,false).scorePercent);
        assertEquals(Integer.valueOf(45),rate(1,"",null,null,
            "",false,null,null,false).scorePercent);
        assertEquals(Integer.valueOf(100),rate(2,"montane",850d,25d,
            "montane",true,500d,1500d,true).scorePercent);
        assertEquals(Integer.valueOf(0),rate(0,"grassland",50d,25d,
            "mossy",true,1000d,2400d,true).scorePercent);
        assertNull(rate(0,"",null,null,"",false,null,null,false).scorePercent);
    }
    @Test public void unreviewedHabitatDoesNotChangeIndex(){
        assertEquals(Integer.valueOf(60),rate(2,"urban",50d,10d,
            "mossy",true,1000d,2200d,false).scorePercent);
    }
    @Test public void labelsAreNotProbabilities(){
        String label=rate(2,"lowland",200d,10d,"lowland",false,0d,1500d,true).label;
        assertEquals("Likely",label);
        assertFalse(label.contains("%"));
    }
}
