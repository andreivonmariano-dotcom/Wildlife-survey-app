package org.phwildlife.survey;

/**
 * Presentation-only method focus. This never changes effort, protocol
 * eligibility, required metadata, taxonomy or observation persistence.
 */
public final class RecorderMethodUiPolicy {
    private RecorderMethodUiPolicy(){}

    public enum Action { NONE, AUDIO, FIELD_EFFORT, DEVICES }
    public static boolean firstDetectionProminent(String profile,String method){
        return "Birds".equals(profile)&&
            ("point_count".equals(method)||"line_transect".equals(method));
    }
    public static boolean listeningProminent(String profile,String method){
        return "Herpetofauna".equals(profile)&&"amphibian_calls".equals(method);
    }
    public static boolean microhabitatProminent(String profile,String method){
        return "Herpetofauna".equals(profile)&&!"amphibian_calls".equals(method);
    }
    public static boolean deviceIdProminent(String profile,String method){
        return "Mammals".equals(profile)&&(
            "camera_trap_check".equals(method)||"live_trap_check".equals(method));
    }
    public static Action action(String profile,String method){
        if(listeningProminent(profile,method))return Action.AUDIO;
        if("camera_trap_check".equals(method)||"live_trap_check".equals(method)
                ||"drift_fence_check".equals(method))return Action.DEVICES;
        if("area_search".equals(method)||"track_sign".equals(method)
                ||"spotlight".equals(method)||"coverboard_search".equals(method))
            return Action.FIELD_EFFORT;
        return Action.NONE;
    }
    /**
     * A visible inline action replaces the identical advanced quick action.
     * Other user-selected shortcuts remain available under Field tools.
     */
    public static FieldQuickActions.Kind inlineQuickKind(String profile,String method){
        switch(action(profile,method)){
            case AUDIO:return FieldQuickActions.Kind.CALLS;
            case FIELD_EFFORT:return FieldQuickActions.Kind.METHOD_EVIDENCE;
            case DEVICES:return FieldQuickActions.Kind.DEVICES;
            default:return FieldQuickActions.Kind.NONE;
        }
    }
    public static String actionLabel(String profile,String method){
        switch(action(profile,method)){
            case AUDIO:return "Record / review calls";
            case FIELD_EFFORT:return "Record method effort";
            case DEVICES:return "Device checks & captures";
            default:return "";
        }
    }
    public static String focus(String profile,String method){
        if("Birds".equals(profile)){
            if("point_count".equals(method))
                return "Point count · radial distance and first detection";
            if("line_transect".equals(method))
                return "Line transect · measured distance and bearing";
            if("area_search".equals(method))
                return "Area search · search effort and repeat detections";
            if("opportunistic".equals(method))
                return "Incidental bird record · no standardized absence claim";
            return "Bird survey · method-specific panel not configured";
        }
        if("Herpetofauna".equals(profile)){
            if("amphibian_calls".equals(method))
                return "Amphibian listening · call index and background noise";
            if("ves_transect".equals(method))
                return "Transect VES · microhabitat and search effort";
            if("visual_encounter".equals(method))
                return "Visual encounter · microhabitat and search effort";
            if("coverboard_search".equals(method))
                return "Cover-board check · unit inspections and substrate";
            if("drift_fence_check".equals(method))
                return "Drift-fence check · traps and capture audit";
            if("opportunistic".equals(method))
                return "Incidental herpetofauna record · effort not standardized";
            return "Herpetofauna survey · method-specific panel not configured";
        }
        if("Mammals".equals(profile)){
            if("camera_trap_check".equals(method))
                return "Camera trap · device ID, operation and detections";
            if("live_trap_check".equals(method))
                return "Live trap · device ID, captures and releases";
            if("track_sign".equals(method))
                return "Track and sign · evidence, substrate and effort";
            if("spotlight".equals(method))
                return "Spotlight · effort, range and repeat detections";
            if("line_transect".equals(method))
                return "Mammal transect · distance and encounter evidence";
            if("bat_acoustic".equals(method))
                return "Bat acoustic · basic detection log only";
            if("opportunistic".equals(method))
                return "Incidental mammal record · no standardized absence claim";
            return "Mammal survey · method-specific panel not configured";
        }
        return "Field observation · review survey method and effort";
    }
}
