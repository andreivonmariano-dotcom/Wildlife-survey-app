package org.phwildlife.survey;
import org.junit.Test;
import static org.junit.Assert.*;
public final class OccurrenceEvidencePolicyTest {
 @Test public void localEvidenceIsNotOccurrenceProbability(){
   assertTrue(OccurrenceEvidencePolicy.title(2).contains("Province"));
   assertTrue(OccurrenceEvidencePolicy.title(1).contains("Island"));
   assertTrue(OccurrenceEvidencePolicy.title(0).contains("unassessed"));
   assertFalse(OccurrenceEvidencePolicy.details(0).contains("unlikely"));
   assertTrue(OccurrenceEvidencePolicy.probabilityStatus().contains("Not calculated"));
 }
}
