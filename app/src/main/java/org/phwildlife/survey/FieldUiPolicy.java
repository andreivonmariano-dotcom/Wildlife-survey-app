package org.phwildlife.survey;

/** Screen sections derived from the selected survey profile and method. */
public final class FieldUiPolicy {
    private FieldUiPolicy(){}

    public static boolean birdDetails(String profile){
        return "Birds".equals(profile);
    }
    public static boolean herpetofaunaDetails(String profile){
        return "Herpetofauna".equals(profile);
    }
    public static boolean mammalDetails(String profile){
        return "Mammals".equals(profile);
    }
    public static boolean callIndex(String profile,String group){
        return herpetofaunaDetails(profile)&&"Amphibians".equals(group);
    }
    public static boolean listeningNoise(String profile,String group,String method){
        return callIndex(profile,group)&&"amphibian_calls".equals(method);
    }
    public static boolean routeSetup(String method){
        return "point_count".equals(method)||"line_transect".equals(method)
            ||"ves_transect".equals(method)||"spotlight".equals(method);
    }
    public static boolean pointStationSetup(String method){
        return "point_count".equals(method);
    }
}
