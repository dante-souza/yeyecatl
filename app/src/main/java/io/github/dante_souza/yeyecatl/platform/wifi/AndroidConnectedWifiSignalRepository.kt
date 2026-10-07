package io.github.dante_souza.yeyecatl.platform.wifi

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiManager
import android.os.Handler
import android.os.Looper
import io.github.dante_souza.yeyecatl.domain.wifi.ObservedSsid
import io.github.dante_souza.yeyecatl.domain.wifi.WifiConnectedSignalAccumulator
import io.github.dante_souza.yeyecatl.domain.wifi.WifiConnectedSignalHistory
import io.github.dante_souza.yeyecatl.domain.wifi.WifiConnectedSignalSample
import io.github.dante_souza.yeyecatl.domain.wifi.WifiConnectedSignalState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AndroidConnectedWifiSignalRepository(
    context: Context,
    private val sampleIntervalMillis: Long = DEFAULT_SAMPLE_INTERVAL_MILLIS
) {
    private val appContext = context.applicationContext
    private val wifiManager = appContext.getSystemService(WifiManager::class.java)
    private val handler = Handler(Looper.getMainLooper())
    private val state = MutableStateFlow<WifiConnectedSignalState>(WifiConnectedSignalState.Idle)

    private var history = WifiConnectedSignalHistory()
    private var running = false

    private val sampleRunnable = object : Runnable {
        override fun run() {
            if (!running) {
                return
            }
            sampleNow()
            handler.postDelayed(this, sampleIntervalMillis)
        }
    }

    fun observeState(): StateFlow<WifiConnectedSignalState> = state

    fun start() {
        if (running) {
            return
        }
        running = true
        handler.removeCallbacks(sampleRunnable)
        handler.post(sampleRunnable)
    }

    fun stop() {
        running = false
        handler.removeCallbacks(sampleRunnable)
    }

    @SuppressLint("MissingPermission")
    @Suppress("DEPRECATION")
    private fun sampleNow() {
        val manager = wifiManager
        if (manager == null) {
            state.value = WifiConnectedSignalState.Unavailable(
                reason = "Wi-Fi hardware is unavailable.",
                history = history
            )
            return
        }

        val info = runCatching { manager.connectionInfo }.getOrNull()
        if (info == null || info.networkId < 0) {
            state.value = WifiConnectedSignalState.Disconnected(history)
            return
        }

        val bssid = info.bssid
            ?.takeUnless { it.isBlank() || it == REDACTED_BSSID }
        if (bssid == null) {
            state.value = WifiConnectedSignalState.Unavailable(
                reason = "Connected Wi-Fi identity is unavailable to the app.",
                history = history
            )
            return
        }

        val rssiDbm = info.rssi
        if (rssiDbm <= INVALID_RSSI_FLOOR || rssiDbm > 0) {
            state.value = WifiConnectedSignalState.Unavailable(
                reason = "Android did not provide a usable connected-link RSSI.",
                history = history
            )
            return
        }

        val ssidText = info.ssid
            ?.removeSurrounding(""")
            ?.takeUnless { it.isBlank() || it == UNKNOWN_SSID }
        val sample = WifiConnectedSignalSample(
            bssid = bssid,
            ssid = ObservedSsid(
                displayText = ssidText,
                rawBytes = null,
                isHidden = ssidText == null
            ),
            rssiDbm = rssiDbm,
            frequencyMhz = info.frequency.takeIf { it > 0 },
            observedAtMillis = System.currentTimeMillis()
        )
        history = WifiConnectedSignalAccumulator.append(history, sample)
        state.value = WifiConnectedSignalState.Connected(
            sample = sample,
            history = history
        )
    }

    private companion object {
        const val DEFAULT_SAMPLE_INTERVAL_MILLIS = 1_000L
        const val REDACTED_BSSID = "02:00:00:00:00:00"
        const val UNKNOWN_SSID = "<unknown ssid>"
        const val INVALID_RSSI_FLOOR = -127
    }
}
