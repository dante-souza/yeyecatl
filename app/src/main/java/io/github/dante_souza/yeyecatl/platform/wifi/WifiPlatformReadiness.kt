package io.github.dante_souza.yeyecatl.platform.wifi

enum class WifiHardwareStatus {
    Available,
    Unavailable
}

enum class WifiPowerStatus {
    Enabled,
    Disabled,
    Unknown
}

enum class LocationServicesStatus {
    Enabled,
    Disabled,
    Unknown
}

enum class ScannerImplementationStatus {
    Implemented,
    NotImplemented
}

data class WifiPlatformReadiness(
    val hardware: WifiHardwareStatus,
    val wifiPower: WifiPowerStatus,
    val locationServices: LocationServicesStatus,
    val discoveryPermission: WifiDiscoveryPermissionStatus,
    val scanner: ScannerImplementationStatus = ScannerImplementationStatus.NotImplemented
) {
    val isDiscoveryAllowed: Boolean
        get() = hardware == WifiHardwareStatus.Available &&
            wifiPower == WifiPowerStatus.Enabled &&
            locationServices == LocationServicesStatus.Enabled &&
            discoveryPermission.allowsDiscovery
}

interface WifiPlatformReadinessProvider {
    fun currentReadiness(
        permissionRequestAttempted: Boolean,
        shouldShowPermissionRationale: Boolean
    ): WifiPlatformReadiness

    fun discoveryRuntimePermissionNames(): List<String>
}
