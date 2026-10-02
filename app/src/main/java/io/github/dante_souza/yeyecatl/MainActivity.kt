package io.github.dante_souza.yeyecatl

import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.dante_souza.yeyecatl.platform.wifi.AndroidWifiPlatformReadinessProvider
import io.github.dante_souza.yeyecatl.platform.wifi.LocationServicesStatus
import io.github.dante_souza.yeyecatl.platform.wifi.PermissionGrantState
import io.github.dante_souza.yeyecatl.platform.wifi.PermissionRequirement
import io.github.dante_souza.yeyecatl.platform.wifi.ScannerImplementationStatus
import io.github.dante_souza.yeyecatl.platform.wifi.WifiDiscoveryPermissionStatus
import io.github.dante_souza.yeyecatl.platform.wifi.WifiHardwareStatus
import io.github.dante_souza.yeyecatl.platform.wifi.WifiPlatformReadiness
import io.github.dante_souza.yeyecatl.platform.wifi.WifiPowerStatus
import io.github.dante_souza.yeyecatl.ui.YeyecatlApp

class MainActivity : ComponentActivity() {
    private val wifiReadinessProvider by lazy {
        AndroidWifiPlatformReadinessProvider(this)
    }

    private var permissionRequestAttempted by mutableStateOf(false)
    private var readiness by mutableStateOf(initialReadiness())

    private val discoveryPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        permissionRequestAttempted = true
        refreshReadiness()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        refreshReadiness()
        setContent {
            YeyecatlApp(
                readiness = readiness,
                onRequestDiscoveryPermission = ::requestDiscoveryPermission
            )
        }
    }

    override fun onResume() {
        super.onResume()
        refreshReadiness()
    }

    private fun requestDiscoveryPermission() {
        val permissionName = wifiReadinessProvider.discoveryRuntimePermissionName()
            ?: return

        discoveryPermissionLauncher.launch(permissionName)
    }

    private fun refreshReadiness() {
        val permissionName = wifiReadinessProvider.discoveryRuntimePermissionName()
        val shouldShowPermissionRationale = permissionName?.let {
            shouldShowRequestPermissionRationale(it)
        } ?: false

        readiness = wifiReadinessProvider.currentReadiness(
            permissionRequestAttempted = permissionRequestAttempted,
            shouldShowPermissionRationale = shouldShowPermissionRationale
        )
    }

    private fun initialReadiness(): WifiPlatformReadiness =
        WifiPlatformReadiness(
            hardware = WifiHardwareStatus.Unavailable,
            wifiPower = WifiPowerStatus.Unknown,
            locationServices = LocationServicesStatus.Unknown,
            discoveryPermission = WifiDiscoveryPermissionStatus(
                permission = null,
                requirement = PermissionRequirement.Required,
                grantState = PermissionGrantState.NotGranted
            ),
            scanner = ScannerImplementationStatus.NotImplemented
        )
}
