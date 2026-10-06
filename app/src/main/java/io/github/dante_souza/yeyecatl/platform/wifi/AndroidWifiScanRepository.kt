package io.github.dante_souza.yeyecatl.platform.wifi

import android.annotation.SuppressLint
import android.content.pm.ApplicationInfo
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import io.github.dante_souza.yeyecatl.domain.wifi.RawWifiScanObservation
import io.github.dante_souza.yeyecatl.domain.wifi.WifiRfInterpreter
import io.github.dante_souza.yeyecatl.domain.wifi.WifiScanBlockReason
import io.github.dante_souza.yeyecatl.domain.wifi.WifiScanFreshness
import io.github.dante_souza.yeyecatl.domain.wifi.WifiScanObservation
import io.github.dante_souza.yeyecatl.domain.wifi.WifiScanObservationMapper
import io.github.dante_souza.yeyecatl.domain.wifi.WifiScanRepository
import io.github.dante_souza.yeyecatl.domain.wifi.WifiScanResultSource
import io.github.dante_souza.yeyecatl.domain.wifi.WifiScanState
import io.github.dante_souza.yeyecatl.domain.wifi.WifiScanStateTransitions
import io.github.dante_souza.yeyecatl.domain.wifi.WifiTemporalObservationAccumulator
import io.github.dante_souza.yeyecatl.domain.wifi.WifiTemporalObservationHistory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AndroidWifiScanRepository(
    context: Context,
    private val readinessProvider: WifiPlatformReadinessProvider
) : WifiScanRepository {
    private val appContext = context.applicationContext
    private val wifiManager = appContext.getSystemService(WifiManager::class.java)
    private val state = MutableStateFlow<WifiScanState>(WifiScanState.Idle)
    private val temporalHistory = MutableStateFlow(WifiTemporalObservationHistory())
    private var receiverRegistered = false
    private var requestInFlight = false

    private val scanResultsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
                handleScanResultsAvailable(intent)
            }
        }
    }

    override fun observeScanState(): StateFlow<WifiScanState> = state

    override fun observeTemporalHistory(): StateFlow<WifiTemporalObservationHistory> = temporalHistory

    override fun start() {
        if (receiverRegistered) {
            return
        }

        ContextCompat.registerReceiver(
            appContext,
            scanResultsReceiver,
            IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        receiverRegistered = true
        devLog("scan receiver registered")
    }

    override fun stop() {
        if (!receiverRegistered) {
            return
        }

        runCatching {
            appContext.unregisterReceiver(scanResultsReceiver)
        }
        receiverRegistered = false
        requestInFlight = false
        devLog("scan receiver unregistered")
    }

    @SuppressLint("MissingPermission")
    @Suppress("DEPRECATION")
    override fun requestScan() {
        start()

        val readiness = readinessProvider.currentReadiness(
            permissionRequestAttempted = false,
            shouldShowPermissionRationale = false
        )
        val blockReason = WifiScanReadinessPolicy.blockReason(readiness)
        if (blockReason != null) {
            devLog("scan request blocked reason=$blockReason")
            state.value = WifiScanStateTransitions.blocked(state.value, blockReason)
            return
        }

        val manager = wifiManager
        if (manager == null) {
            devLog("scan request blocked reason=${WifiScanBlockReason.WifiHardwareUnavailable}")
            state.value = WifiScanStateTransitions.blocked(
                state.value,
                WifiScanBlockReason.WifiHardwareUnavailable
            )
            return
        }

        state.value = WifiScanStateTransitions.request(state.value)
        devLog("scan request attempted")

        try {
            val accepted = manager.startScan()
            if (accepted) {
                requestInFlight = true
                devLog("scan request accepted")
            } else {
                requestInFlight = false
                devLog("scan request rejected")
                state.value = WifiScanStateTransitions.requestRejected(state.value)
            }
        } catch (_: SecurityException) {
            requestInFlight = false
            devLog("scan request security failure")
            state.value = WifiScanStateTransitions.blocked(
                state.value,
                WifiScanBlockReason.PermissionUnavailable
            )
        } catch (_: RuntimeException) {
            requestInFlight = false
            devLog("scan request runtime failure")
            state.value = WifiScanStateTransitions.error(
                state.value,
                "Android failed to request a Wi-Fi scan."
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun handleScanResultsAvailable(intent: Intent) {
        val source = if (requestInFlight) {
            WifiScanResultSource.ApplicationRequest
        } else {
            WifiScanResultSource.PassiveAvailability
        }
        requestInFlight = false

        val resultsUpdated = if (intent.hasExtra(WifiManager.EXTRA_RESULTS_UPDATED)) {
            intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
        } else {
            null
        }
        val freshness = when (resultsUpdated) {
            true -> WifiScanFreshness.Fresh
            false -> WifiScanFreshness.Cached
            null -> WifiScanFreshness.Unknown
        }
        devLog(
            "scan broadcast source=$source resultsUpdated=$resultsUpdated freshness=$freshness"
        )

        try {
            val observations = wifiManager
                ?.scanResults
                .orEmpty()
                .map(::mapScanResult)
            devLog(
                "scan results mapped count=${observations.size} " +
                    "bands=${observations.map { WifiRfInterpreter.bandForFrequency(it.frequencyMhz) }.toSet()} " +
                    "widths=${observations.map { it.channelWidth }.toSet()}"
            )

            val nextState = WifiScanStateTransitions.resultsAvailable(
                current = state.value,
                observations = observations,
                freshness = freshness,
                source = source,
                resultsUpdated = resultsUpdated,
                receivedAtMillis = System.currentTimeMillis()
            )
            state.value = nextState
            nextState.latestSnapshot?.let { snapshot ->
                temporalHistory.value = WifiTemporalObservationAccumulator.append(
                    current = temporalHistory.value,
                    snapshot = snapshot
                )
            }
        } catch (_: SecurityException) {
            devLog("scan results security failure")
            state.value = WifiScanStateTransitions.blocked(
                state.value,
                WifiScanBlockReason.PermissionUnavailable
            )
        } catch (_: RuntimeException) {
            devLog("scan results runtime failure")
            state.value = WifiScanStateTransitions.error(
                state.value,
                "Android failed to read Wi-Fi scan results."
            )
        }
    }

    @Suppress("DEPRECATION")
    private fun mapScanResult(scanResult: ScanResult): WifiScanObservation =
        WifiScanObservationMapper.fromRaw(
            RawWifiScanObservation(
                ssidDisplayText = scanResult.SSID,
                ssidRawBytes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    scanResult.wifiSsid?.bytes
                } else {
                    null
                },
                bssid = scanResult.BSSID,
                rssiDbm = scanResult.level,
                frequencyMhz = scanResult.frequency,
                channelWidth = AndroidWifiRadioMetadataMapper.channelWidth(
                    apiLevel = Build.VERSION.SDK_INT,
                    rawChannelWidth = scanResult.channelWidth
                ),
                centerFrequency0Mhz = scanResult.centerFreq0,
                centerFrequency1Mhz = scanResult.centerFreq1,
                wifiStandard = AndroidWifiRadioMetadataMapper.wifiStandard(
                    apiLevel = Build.VERSION.SDK_INT,
                    rawWifiStandard = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        scanResult.wifiStandard
                    } else {
                        null
                    }
                ),
                capabilities = scanResult.capabilities,
                platformTimestampMicros = scanResult.timestamp
            )
        )

    private fun devLog(message: String) {
        if ((appContext.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            Log.d(LOG_TAG, message)
        }
    }

    private companion object {
        const val LOG_TAG = "YeyecatlWifiScan"
    }

}
