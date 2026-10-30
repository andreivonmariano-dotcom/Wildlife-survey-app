package org.phwildlife.survey;

/** Shared read-only outcome for current and legacy surveys; never rewrites stored values. */
public final class SurveyOutcomeSql {
    private SurveyOutcomeSql(){}
    public static final String RESOLVED =
        "CASE WHEN s.outcome IS NOT NULL THEN s.outcome "+
        "WHEN s.ended IS NULL THEN 'active' "+
        "WHEN COALESCE(s.complete,0)=1 THEN 'complete' ELSE 'incomplete' END";
    /** Derived analytical filter; surveys table must have the alias s. */
    public static final String COMPLETE = "("+RESOLVED+")='complete'";
}
