package io.github.dante_souza.yeyecatl

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import io.github.dante_souza.yeyecatl.domain.wifi.WifiForegroundScanCadence
import io.github.dante_souza.yeyecatl.platform.wifi.AndroidWifiScanCadenceScheduler
import io.github.dante_souza.yeyecatl.platform.wifi.AndroidWifiScanRepository
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
    private val wifiScanRepository by lazy {
        AndroidWifiScanRepository(this, wifiReadinessProvider)
    }
    private val wifiScanCadence by lazy {
        WifiForegroundScanCadence(
            requestScan = ::requestScan,
            scheduler = AndroidWifiScanCadenceScheduler()
        )
    }

    private var permissionRequestAttempted by mutableStateOf(false)
    private var readiness by mutableStateOf(initialReadiness())
    private var dynamicScanEnabled by mutableStateOf(false)

    private val discoveryPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        permissionRequestAttempted = true
        refreshReadiness()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        refreshReadiness()
        setContent {
            val scanState by wifiScanRepository.observeScanState().collectAsState()
            val temporalHistory by wifiScanRepository.observeTemporalHistory().collectAsState()
            YeyecatlApp(
                readiness = readiness,
                scanState = scanState,
                temporalHistory = temporalHistory,
                dynamicScanEnabled = dynamicScanEnabled,
                onRequestScan = ::requestScan,
                onToggleDynamicScan = ::toggleDynamicScan,
                onRequestDiscoveryPermission = ::requestDiscoveryPermission
            )
        }
    }

    override fun onStart() {
        super.onStart()
        wifiScanRepository.start()
        wifiScanCadence.enterForeground()
    }

    override fun onResume() {
        super.onResume()
        refreshReadiness()
    }

    override fun onStop() {
        wifiScanCadence.leaveForeground()
        wifiScanRepository.stop()
        super.onStop()
    }

    private fun requestDiscoveryPermission() {
        val permissionNames = wifiReadinessProvider.discoveryRuntimePermissionNames()
        if (permissionNames.isEmpty()) {
            return
        }

        discoveryPermissionLauncher.launch(permissionNames.toTypedArray())
    }

    private fun requestScan() {
        refreshReadiness()
        wifiScanRepository.requestScan()
    }

    private fun toggleDynamicScan() {
        dynamicScanEnabled = !dynamicScanEnabled
        wifiScanCadence.setEnabled(dynamicScanEnabled)
    }

    private fun refreshReadiness() {
        val shouldShowPermissionRationale = wifiReadinessProvider
            .discoveryRuntimePermissionNames()
            .any(::shouldShowRequestPermissionRationale)

        readiness = wifiReadinessProvider.currentReadiness(
            permissionRequestAttempted = permissionRequestAttempted,
            shouldShowPermissionRationale = shouldShowPermissionRationale
        )
        if (!readiness.isDiscoveryAllowed && dynamicScanEnabled) {
            dynamicScanEnabled = false
            wifiScanCadence.setEnabled(false)
        }
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
