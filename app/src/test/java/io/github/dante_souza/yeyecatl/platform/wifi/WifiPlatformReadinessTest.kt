package io.github.dante_souza.yeyecatl.platform.wifi

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WifiPlatformReadinessTest {
    @Test
    fun discoveryIsAllowedWhenAllPlatformPreconditionsAreReady() {
        val readiness = readiness(
            hardware = WifiHardwareStatus.Available,
            wifiPower = WifiPowerStatus.Enabled,
            locationServices = LocationServicesStatus.Enabled,
            permissionGrantState = PermissionGrantState.Available
        )

        assertTrue(readiness.isDiscoveryAllowed)
    }

    @Test
    fun discoveryIsBlockedWhenPermissionIsMissing() {
        val readiness = readiness(permissionGrantState = PermissionGrantState.NotGranted)

        assertFalse(readiness.isDiscoveryAllowed)
    }

    @Test
    fun discoveryIsBlockedWhenWifiHardwareIsUnavailable() {
        val readiness = readiness(hardware = WifiHardwareStatus.Unavailable)

        assertFalse(readiness.isDiscoveryAllowed)
    }

    @Test
    fun discoveryIsBlockedWhenWifiIsDisabled() {
        val readiness = readiness(wifiPower = WifiPowerStatus.Disabled)

        assertFalse(readiness.isDiscoveryAllowed)
    }

    @Test
    fun discoveryIsBlockedWhenLocationServicesAreDisabled() {
        val readiness = readiness(locationServices = LocationServicesStatus.Disabled)

        assertFalse(readiness.isDiscoveryAllowed)
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
            )
        )
}
