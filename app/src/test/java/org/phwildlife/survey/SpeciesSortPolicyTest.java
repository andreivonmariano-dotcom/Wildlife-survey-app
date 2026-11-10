package org.phwildlife.survey;
import static org.junit.Assert.*;
import org.junit.Test;
public final class SpeciesSortPolicyTest{
    private int compare(int sort,String nameA,String sciA,String familyA,Integer scoreA,
            String nameB,String sciB,String familyB,Integer scoreB){
        return SpeciesSortPolicy.compare(sort,nameA,sciA,familyA,scoreA,
            nameB,sciB,familyB,scoreB);
    }
    @Test public void genusAndFamilyOrdering(){
        assertEquals("platymantis",SpeciesSortPolicy.genus("Platymantis cornutus"));
        assertEquals("ophiophagus",SpeciesSortPolicy.genus("Ophiophagus"));
        assertTrue(compare(2,"B","Agamia agami","Z",null,
            "A","Bubo bubo","A",null)<0);
        assertTrue(compare(1,"A","Bubo bubo","Z",null,
            "B","Agamia agami","A",null)>0);
    }
    @Test public void alphabeticAndNumericalOrdering(){
        assertTrue(compare(0,"A","A a","F",60,
            "Z","Z z","F",90)<0);
        assertTrue(compare(3,"A","A a","F",60,
            "Z","Z z","F",90)>0);
        assertTrue(compare(4,"A","A a","F",60,
            "Z","Z z","F",90)<0);
    }
    @Test public void missingScoresAreAlwaysLast(){
        assertTrue(compare(3,"A","A a","F",null,
            "Z","Z z","F",60)>0);
        assertTrue(compare(4,"A","A a","F",null,
            "Z","Z z","F",60)>0);
    }
    @Test public void filtersRetainUnknownInsteadOfCallingAbsence(){
        assertTrue(SpeciesSortPolicy.keep(0,"Insufficient Data"));
        assertFalse(SpeciesSortPolicy.keep(1,"Insufficient Data"));
        assertTrue(SpeciesSortPolicy.keep(3,"Insufficient Data"));
        assertTrue(SpeciesSortPolicy.keep(2,"Unlikely"));
        assertFalse(SpeciesSortPolicy.keep(2,"Likely"));
    }
}
