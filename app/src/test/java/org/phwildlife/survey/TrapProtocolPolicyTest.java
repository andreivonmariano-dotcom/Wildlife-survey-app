package org.phwildlife.survey;
import static org.junit.Assert.*;
import org.junit.Test;
public final class TrapProtocolPolicyTest {
    @Test public void onlyTheThreeDeviceMethodsAreSupported(){
        assertTrue(TrapProtocolPolicy.supported("camera_trap_check"));
        assertTrue(TrapProtocolPolicy.supported("drift_fence_check"));
        assertTrue(TrapProtocolPolicy.supported("live_trap_check"));
        assertFalse(TrapProtocolPolicy.supported("bat_acoustic"));
    }
    @Test public void completeNeedsInspectionsAndRetrieval(){
        assertFalse(TrapProtocolPolicy.canComplete(0,0,0,0));
        assertFalse(TrapProtocolPolicy.canComplete(3,1,0,0));
        assertFalse(TrapProtocolPolicy.canComplete(3,0,1,0));
        assertFalse(TrapProtocolPolicy.canComplete(3,0,0,1));
        assertTrue(TrapProtocolPolicy.canComplete(3,0,0,0));
    }
}
