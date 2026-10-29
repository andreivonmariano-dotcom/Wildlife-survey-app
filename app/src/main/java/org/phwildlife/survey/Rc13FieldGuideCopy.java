package org.phwildlife.survey;

/** Editorial shortening for the on-screen field guide. Original evidence assets are intact. */
public final class Rc13FieldGuideCopy {
    private Rc13FieldGuideCopy(){}
    private static final String[] EDITORIAL_TAILS={
        "; habitat is context, not diagnostic proof",
        "; obtain clear images rather than identifying from a brief flush.",
        ". Female identification needs specialist comparison.",
        ". An island name is not confirmation at an individual point.",
        ". Records from other islands do not establish this species.",
        ". Historical island records do not establish a current occupied site.",
        ". Show the assessment year, not an undated category.",
        "; store original audio rather than guessing a species from an unreviewed recording.",
        "; a published record is not proof of local station presence",
        "; this is not a site occurrence claim.",
        ". This is a published range/habitat summary, not proof of occurrence at a LIKAS station.",
        "; survey-site occupancy is not established.",
        ". Voice and colors vary by sex and light, so record photography and exact detection type."
    };
    public static String fieldText(String value){
        if(value==null)return "";
        String shown=value.trim();
        for(String tail:EDITORIAL_TAILS){
            int at=shown.indexOf(tail);
            if(at>=0)shown=shown.substring(0,at).trim();
        }
        return shown;
    }
}
