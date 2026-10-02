package io.github.dante_souza.yeyecatl.platform.wifi

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.os.Build
import androidx.core.content.ContextCompat

class AndroidWifiPlatformReadinessProvider(
    context: Context
) : WifiPlatformReadinessProvider {
    private val appContext = context.applicationContext

    override fun currentReadiness(
        permissionRequestAttempted: Boolean,
        shouldShowPermissionRationale: Boolean
    ): WifiPlatformReadiness {
        val permissionStatus = WifiDiscoveryPermissionPolicy.evaluate(
            apiLevel = Build.VERSION.SDK_INT,
            isGranted = isFineLocationGranted(),
            permissionRequestAttempted = permissionRequestAttempted,
            shouldShowRationale = shouldShowPermissionRationale
        )

        return WifiPlatformReadiness(
            hardware = wifiHardwareStatus(),
            wifiPower = wifiPowerStatus(),
            locationServices = locationServicesStatus(),
            discoveryPermission = permissionStatus
        )
    }

    override fun discoveryRuntimePermissionName(): String? =
        if (WifiDiscoveryPermissionPolicy.requirementFor(Build.VERSION.SDK_INT) == PermissionRequirement.Required) {
            Manifest.permission.ACCESS_FINE_LOCATION
        } else {
            null
        }

    private fun wifiHardwareStatus(): WifiHardwareStatus =
        if (appContext.packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI)) {
            WifiHardwareStatus.Available
        } else {
            WifiHardwareStatus.Unavailable
        }

    private fun wifiPowerStatus(): WifiPowerStatus {
        val wifiManager = appContext.getSystemService(WifiManager::class.java)
            ?: return WifiPowerStatus.Unknown

        return if (wifiManager.isWifiEnabled) {
            WifiPowerStatus.Enabled
        } else {
            WifiPowerStatus.Disabled
        }
    }

    private fun locationServicesStatus(): LocationServicesStatus {
        val locationManager = appContext.getSystemService(LocationManager::class.java)
            ?: return LocationServicesStatus.Unknown

        return if (locationManager.isLocationEnabled) {
            LocationServicesStatus.Enabled
        } else {
            LocationServicesStatus.Disabled
        }
    }

    private fun isFineLocationGranted(): Boolean =
        ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
}
