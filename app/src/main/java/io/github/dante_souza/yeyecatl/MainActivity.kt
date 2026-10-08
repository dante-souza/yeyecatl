package io.github.dante_souza.yeyecatl

import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import io.github.dante_souza.yeyecatl.domain.wifi.WifiForegroundScanCadence
import io.github.dante_souza.yeyecatl.domain.wifi.WifiPollingIntervalPolicy
import io.github.dante_souza.yeyecatl.platform.wifi.AndroidWifiScanCadenceScheduler
import io.github.dante_souza.yeyecatl.platform.wifi.AndroidWifiScanRepository
import io.github.dante_souza.yeyecatl.platform.wifi.AndroidConnectedWifiSignalRepository
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
    private val connectedWifiSignalRepository by lazy {
        AndroidConnectedWifiSignalRepository(this)
    }
    private val wifiScanCadence by lazy {
        WifiForegroundScanCadence(
            requestScan = ::requestDynamicScan,
            scheduler = AndroidWifiScanCadenceScheduler(),
            intervalMillis = dynamicScanIntervalMillis
        )
    }
    private val settings by lazy {
        getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE)
    }

    private var permissionRequestAttempted by mutableStateOf(false)
    private var readiness by mutableStateOf(initialReadiness())
    private var dynamicScanEnabled by mutableStateOf(false)
    private var dynamicScanRequestCount by mutableIntStateOf(0)
    private var dynamicScanFreshHistoryBaseline by mutableIntStateOf(0)
    private var dynamicScanIntervalMillis by mutableLongStateOf(
        WifiPollingIntervalPolicy.DEFAULT_INTERVAL_MILLIS
    )
    private var initialScanRequested = false

    private val discoveryPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        permissionRequestAttempted = true
        refreshReadiness()
        maybeRequestInitialScan()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashStartedAt = SystemClock.elapsedRealtime()
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition {
            SystemClock.elapsedRealtime() - splashStartedAt < MIN_SPLASH_VISIBLE_MILLIS
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        initialScanRequested = savedInstanceState
            ?.getBoolean(KEY_INITIAL_SCAN_REQUESTED)
            ?: false
        dynamicScanIntervalMillis = WifiPollingIntervalPolicy.sanitize(
            runCatching {
                settings.getLong(
                    KEY_POLLING_INTERVAL_MILLIS,
                    WifiPollingIntervalPolicy.DEFAULT_INTERVAL_MILLIS
                )
            }.getOrNull()
        )
        refreshReadiness()
        setContent {
            val scanState by wifiScanRepository.observeScanState().collectAsState()
            val temporalHistory by wifiScanRepository.observeTemporalHistory().collectAsState()
            val connectedSignalState by connectedWifiSignalRepository.observeState().collectAsState()
            YeyecatlApp(
                readiness = readiness,
                scanState = scanState,
                temporalHistory = temporalHistory,
                connectedSignalState = connectedSignalState,
                dynamicScanEnabled = dynamicScanEnabled,
                dynamicScanRequestCount = dynamicScanRequestCount,
                dynamicScanFreshUpdateCount =
                    (temporalHistory.freshSnapshotCount - dynamicScanFreshHistoryBaseline)
                        .coerceAtLeast(0),
                dynamicScanIntervalMillis = dynamicScanIntervalMillis,
                onDynamicScanIntervalSelected = ::selectDynamicScanInterval,
                onRequestScan = ::requestScan,
                onToggleDynamicScan = ::toggleDynamicScan,
                onRequestDiscoveryPermission = ::requestDiscoveryPermission
            )
        }
    }

    override fun onStart() {
        super.onStart()
        wifiScanRepository.start()
        connectedWifiSignalRepository.start()
        wifiScanCadence.enterForeground()
        refreshReadiness()
        maybeRequestInitialScan()
    }

    override fun onResume() {
        super.onResume()
        refreshReadiness()
        maybeRequestInitialScan()
    }

    override fun onStop() {
        wifiScanCadence.leaveForeground()
        connectedWifiSignalRepository.stop()
        wifiScanRepository.stop()
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(KEY_INITIAL_SCAN_REQUESTED, initialScanRequested)
        super.onSaveInstanceState(outState)
    }

    private fun maybeRequestInitialScan() {
        if (initialScanRequested || !readiness.isDiscoveryAllowed) {
            return
        }

        initialScanRequested = true
        requestScan()
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

    private fun requestDynamicScan() {
        dynamicScanRequestCount += 1
        requestScan()
    }

    private fun selectDynamicScanInterval(intervalMillis: Long) {
        val selectedIntervalMillis = WifiPollingIntervalPolicy.sanitize(intervalMillis)
        if (selectedIntervalMillis == dynamicScanIntervalMillis) {
            return
        }

        dynamicScanIntervalMillis = selectedIntervalMillis
        wifiScanCadence.setIntervalMillis(selectedIntervalMillis)
        settings.edit()
            .putLong(KEY_POLLING_INTERVAL_MILLIS, selectedIntervalMillis)
            .apply()
    }

    private fun toggleDynamicScan() {
        val nextEnabled = !dynamicScanEnabled
        if (nextEnabled) {
            dynamicScanRequestCount = 0
            dynamicScanFreshHistoryBaseline =
                wifiScanRepository.observeTemporalHistory().value.freshSnapshotCount
        }
        dynamicScanEnabled = nextEnabled
        wifiScanCadence.setEnabled(nextEnabled)
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

    private companion object {
        const val PREFERENCES_NAME = "yeyecatl_settings"
        const val KEY_INITIAL_SCAN_REQUESTED = "initial_scan_requested"
        const val KEY_POLLING_INTERVAL_MILLIS = "polling_interval_millis"
        const val MIN_SPLASH_VISIBLE_MILLIS = 550L
    }
}
