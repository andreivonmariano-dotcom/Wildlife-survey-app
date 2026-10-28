package org.phwildlife.survey;

/** One context-sensitive capture shortcut per field protocol. */
public final class FieldQuickActions {
    private FieldQuickActions(){}
    public enum Kind { NONE, CALLS, PHOTO, DEVICES, METHOD_EVIDENCE }
    public static Kind forSurvey(String profile,String method){
        if(profile==null||method==null)return Kind.NONE;
        if("amphibian_calls".equals(method))return Kind.CALLS;
        if(TrapProtocolPolicy.supported(method))return Kind.DEVICES;
        if(FieldProtocolPolicy.supported(method))return Kind.METHOD_EVIDENCE;
        if("Birds".equals(profile))return Kind.CALLS;
        if("Herpetofauna".equals(profile))return Kind.PHOTO;
        if("Mammals".equals(profile) &&
           ("line_transect".equals(method)||"opportunistic".equals(method)))
            return Kind.PHOTO;
        return Kind.NONE;
    }
    public static String label(Kind action){
        switch(action){
            case CALLS:return "Record / review calls";
            case PHOTO:return "Attach observation photo";
            case DEVICES:return "Device checks & captures";
            case METHOD_EVIDENCE:return "Record method evidence";
            default:return "";
        }
    }
}
