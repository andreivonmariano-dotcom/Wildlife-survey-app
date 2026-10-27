package org.phwildlife.survey;

/** Plain-data guard for attaching a manual call label to existing evidence. */
public final class CallLinkPolicy {
    private CallLinkPolicy(){}
    public static boolean sameSurveyAndGroup(String clipSurvey,String linkedSurvey,
            String clipGroup,String linkedGroup){
        return clipSurvey!=null&&clipSurvey.equals(linkedSurvey)&&clipGroup!=null&&
            clipGroup.equals(linkedGroup);
    }
    public static boolean usableLabelRange(long startMs,long endMs,long durationMs){
        return startMs>=0&&endMs>startMs&&endMs<=durationMs;
    }
}
