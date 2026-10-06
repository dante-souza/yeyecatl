package io.github.dante_souza.yeyecatl.platform.wifi

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WifiDiscoveryPermissionPolicyTest {
    @Test
    fun api22DoesNotRequireRuntimePermission() {
        val status = WifiDiscoveryPermissionPolicy.evaluate(
            apiLevel = 22,
            isGranted = false,
            permissionRequestAttempted = false,
            shouldShowRationale = false
        )

        assertEquals(PermissionRequirement.NotRequired, status.requirement)
        assertEquals(PermissionGrantState.NotRequired, status.grantState)
        assertTrue(status.allowsDiscovery)
    }

    @Test
    fun api23RequiresRuntimePermission() {
        val status = WifiDiscoveryPermissionPolicy.evaluate(
            apiLevel = 23,
            isGranted = false,
            permissionRequestAttempted = false,
            shouldShowRationale = false
        )

        assertEquals(PermissionRequirement.Required, status.requirement)
        assertEquals(WifiDiscoveryPermission.FineLocation, status.permission)
        assertEquals(PermissionGrantState.NotGranted, status.grantState)
        assertFalse(status.allowsDiscovery)
    }

    @Test
    fun api29RequiresFineLocationRuntimePermissionForPhaseOneBaseline() {
        val status = WifiDiscoveryPermissionPolicy.evaluate(
            apiLevel = 29,
            isGranted = true,
            permissionRequestAttempted = false,
            shouldShowRationale = false
        )

        assertEquals(PermissionRequirement.Required, status.requirement)
        assertEquals(WifiDiscoveryPermission.FineLocation, status.permission)
        assertEquals(PermissionGrantState.Available, status.grantState)
        assertTrue(status.allowsDiscovery)
    }

    @Test
    fun deniedPermissionCanRequireSettingsAfterAndroidStopsShowingRationale() {
        val status = WifiDiscoveryPermissionPolicy.evaluate(
            apiLevel = 36,
            isGranted = false,
            permissionRequestAttempted = true,
            shouldShowRationale = false
        )

        assertEquals(PermissionGrantState.RequiresSettingsAction, status.grantState)
        assertFalse(status.canRequestFromApp)
        assertFalse(status.allowsDiscovery)
    }
}
