package org.phwildlife.survey;

/**
 * Source evidence, never numerical habitat suitability or species absence.
 * Missing occurrence records always mean unassessed, not unlikely.
 */
public final class OccurrenceEvidencePolicy {
    public static final int NATIONAL=0, ISLAND=1, PROVINCE=2;
    private OccurrenceEvidencePolicy(){}
    public static String title(int evidence){
        if(evidence==PROVINCE)return "Province listed in source";
        if(evidence==ISLAND)return "Island listed in source";
        return "National candidate · local evidence unassessed";
    }
    public static String details(int evidence){
        if(evidence==PROVINCE)return "A cited checklist lists this province. Not a site observation.";
        if(evidence==ISLAND)return "A cited checklist lists this island. Not a site observation.";
        return "No local entry in the installed evidence. Absence is not established.";
    }
    public static String probabilityStatus(){return "Not calculated · habitat model not validated";}
}
