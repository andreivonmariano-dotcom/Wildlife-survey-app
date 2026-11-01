package org.phwildlife.survey;

import java.util.ArrayList;

/** Source-independent policy for a single, user-selected shortcut per survey method. */
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
    /** Reopen only tools already implemented for this method. No fake call recorder. */
    public static Kind[] choices(String profile,String method){
        ArrayList<Kind> result=new ArrayList<>();
        result.add(Kind.NONE);
        if(profile==null||method==null)return result.toArray(new Kind[0]);
        if("Birds".equals(profile)||"Herpetofauna".equals(profile))
            result.add(Kind.CALLS);
        if("Birds".equals(profile)||"Herpetofauna".equals(profile)||
                "Mammals".equals(profile))result.add(Kind.PHOTO);
        if(TrapProtocolPolicy.supported(method))result.add(Kind.DEVICES);
        if(FieldProtocolPolicy.supported(method))result.add(Kind.METHOD_EVIDENCE);
        return result.toArray(new Kind[0]);
    }
    /** Reject stale preferences from changed methods or withdrawn capture routes. */
    public static Kind resolve(String profile,String method,String preference){
        Kind selected=forSurvey(profile,method);
        if(preference==null||preference.isEmpty())return selected;
        try{
            Kind saved=Kind.valueOf(preference);
            for(Kind available:choices(profile,method))
                if(saved==available)return saved;
        }catch(IllegalArgumentException ignored){}
        return selected;
    }
    public static String preferenceKey(String profile,String method){
        return (profile==null?"":profile)+"|"+(method==null?"":method);
    }
    public static String label(Kind action){
        switch(action){
            case CALLS:return "Record / review calls";
            case PHOTO:return "Attach observation photo";
            case DEVICES:return "Device checks & captures";
            case METHOD_EVIDENCE:return "Record method evidence";
            default:return "No quick action";
        }
    }
}
