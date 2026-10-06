package io.github.dante_souza.yeyecatl.platform.wifi

import io.github.dante_souza.yeyecatl.domain.wifi.WifiScanBlockReason

object WifiScanReadinessPolicy {
    fun blockReason(readiness: WifiPlatformReadiness): WifiScanBlockReason? =
        when {
            readiness.hardware != WifiHardwareStatus.Available -> WifiScanBlockReason.WifiHardwareUnavailable
            readiness.wifiPower != WifiPowerStatus.Enabled -> WifiScanBlockReason.WifiUnavailable
            readiness.locationServices != LocationServicesStatus.Enabled -> WifiScanBlockReason.LocationServicesUnavailable
            !readiness.discoveryPermission.allowsDiscovery -> WifiScanBlockReason.PermissionUnavailable
            else -> null
        }
}
