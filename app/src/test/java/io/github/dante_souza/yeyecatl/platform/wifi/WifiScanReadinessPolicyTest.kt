package io.github.dante_souza.yeyecatl.platform.wifi

import io.github.dante_souza.yeyecatl.domain.wifi.WifiScanBlockReason
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WifiScanReadinessPolicyTest {
    @Test
    fun readyPlatformDoesNotBlockScanRequests() {
        assertNull(WifiScanReadinessPolicy.blockReason(readiness()))
    }

    @Test
    fun missingPermissionBlocksScanRequests() {
        val reason = WifiScanReadinessPolicy.blockReason(
            readiness(permissionGrantState = PermissionGrantState.NotGranted)
        )

        assertEquals(WifiScanBlockReason.PermissionUnavailable, reason)
    }

    @Test
    fun disabledWifiBlocksScanRequests() {
        val reason = WifiScanReadinessPolicy.blockReason(
            readiness(wifiPower = WifiPowerStatus.Disabled)
        )

        assertEquals(WifiScanBlockReason.WifiUnavailable, reason)
    }

    @Test
    fun disabledLocationServicesBlocksScanRequests() {
        val reason = WifiScanReadinessPolicy.blockReason(
            readiness(locationServices = LocationServicesStatus.Disabled)
        )

        assertEquals(WifiScanBlockReason.LocationServicesUnavailable, reason)
    }

    @Test
    fun unavailableHardwareBlocksScanRequests() {
        val reason = WifiScanReadinessPolicy.blockReason(
            readiness(hardware = WifiHardwareStatus.Unavailable)
        )

        assertEquals(WifiScanBlockReason.WifiHardwareUnavailable, reason)
    }

    private fun readiness(
        hardware: WifiHardwareStatus = WifiHardwareStatus.Available,
        wifiPower: WifiPowerStatus = WifiPowerStatus.Enabled,
        locationServices: LocationServicesStatus = LocationServicesStatus.Enabled,
        permissionGrantState: PermissionGrantState = PermissionGrantState.Available
    ): WifiPlatformReadiness =
        WifiPlatformReadiness(
            hardware = hardware,
            wifiPower = wifiPower,
            locationServices = locationServices,
            discoveryPermission = WifiDiscoveryPermissionStatus(
                permission = WifiDiscoveryPermission.FineLocation,
                requirement = PermissionRequirement.Required,
                grantState = permissionGrantState
            ),
            scanner = ScannerImplementationStatus.Implemented
        )
}
