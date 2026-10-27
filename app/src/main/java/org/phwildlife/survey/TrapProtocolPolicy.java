package org.phwildlife.survey;

/** Conservative minimum records for a completed device-based survey. */
public final class TrapProtocolPolicy {
    private TrapProtocolPolicy(){}
    public static boolean supported(String method){
        return "camera_trap_check".equals(method)||"drift_fence_check".equals(method)||
            "live_trap_check".equals(method);
    }
    public static boolean needsCaptureDetails(String method){
        return "drift_fence_check".equals(method)||"live_trap_check".equals(method);
    }
    public static boolean canComplete(int deployments,int uncheckedDevices,int openDevices,
            int missingCaptureDetails){
        // A scheduled check can be complete without retrieving installed traps.
        // Open devices remain visible in the deployment inventory.
        return deployments>0&&uncheckedDevices==0&&missingCaptureDetails==0;
    }
}
