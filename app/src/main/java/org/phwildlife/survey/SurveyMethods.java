package org.phwildlife.survey;

/** Method catalog. Labels are literature-based; advanced modules marked basic logging. */
public final class SurveyMethods {
    private SurveyMethods(){}
    public static final class Method {
        public final String code,label,description;
        public final boolean basicLog;
        public Method(String code,String label,String description,boolean basicLog) {
            this.code=code;this.label=label;this.description=description;this.basicLog=basicLog;
        }
    }
    public static Method[] forProfile(String profile) {
        if("Birds".equals(profile))return new Method[]{
            new Method("point_count","Point count","Stationary timed count. Record radial distance for distance sampling.",false),
            new Method("line_transect","Line transect","Walk a measured route. Record horizontal distance and bearing.",false),
            new Method("area_search","Area search","Search a defined area and retain area, time and observer effort.",true),
            new Method("opportunistic","Opportunistic","Record incidental encounters. Not a standardized absence survey.",false)
        };
        if("Herpetofauna".equals(profile))return new Method[]{
            new Method("visual_encounter","Visual encounter (VES)","Active timed search for reptiles and amphibians.",false),
            new Method("ves_transect","Transect VES","Systematic searches along a defined terrestrial or riparian route.",false),
            new Method("amphibian_calls","Amphibian calling","Timed listening for amphibian calls at a station.",true),
            new Method("coverboard_search","Cover-board search","Inspect predefined cover objects and record inspection effort.",true),
            new Method("drift_fence_check","Pitfall / drift fence check","Record detections during trap inspections. Trap-deployment data are not yet captured.",true),
            new Method("opportunistic","Opportunistic","Record incidental reptiles and amphibians.",false)
        };
        return new Method[]{
            new Method("line_transect","Line transect","Record visual encounters along a measured route.",false),
            new Method("track_sign","Track and sign survey","Record identifiable tracks, scat and other field signs.",true),
            new Method("spotlight","Spotlight survey","Nocturnal searches along a defined route or area.",true),
            new Method("camera_trap_check","Camera-trap check","Log identified detections. Deployment and camera-days tracking is planned.",true),
            new Method("bat_acoustic","Bat acoustic survey","Log acoustic detections. Recorder effort and call metadata are planned.",true),
            new Method("live_trap_check","Live-trap check","Log sightings or captures. Trap-night and capture fields are planned.",true),
            new Method("opportunistic","Opportunistic","Record incidental mammal observations.",false)
        };
    }
    public static Method lookup(String profile,String code) {
        for(Method m:forProfile(profile))if(m.code.equals(code))return m;
        return forProfile(profile)[0];
    }
    public static String[] displayNames(String profile) {
        Method[] a=forProfile(profile);String[] v=new String[a.length];
        for(int i=0;i<a.length;i++)v[i]=a[i].label+(a[i].basicLog?" · basic log":"");
        return v;
    }
    public static String codeForIndex(String profile,int index) {
        Method[] a=forProfile(profile);return a[Math.max(0,Math.min(index,a.length-1))].code;
    }
    public static boolean isBirdLine(String profile,String code) {
        return "Birds".equals(profile)&&"line_transect".equals(code);
    }
    public static boolean needsRouteLength(String method) {
        return method.equals("line_transect")||method.equals("ves_transect")
            ||method.equals("visual_encounter")||method.equals("area_search")
            ||method.equals("track_sign")||method.equals("spotlight");
    }
    /** Only protocols where an observation/search/listening interval is meaningful. */
    public static boolean timedInterval(String method){
        return method.equals("point_count")||method.equals("line_transect")
          ||method.equals("ves_transect")||method.equals("visual_encounter")
          ||method.equals("amphibian_calls")||method.equals("area_search")
          ||method.equals("track_sign")||method.equals("spotlight")
          ||method.equals("bat_acoustic")||method.equals("coverboard_search");
    }
    public static boolean structured(String method) {
        return !method.equals("opportunistic");
    }
}
