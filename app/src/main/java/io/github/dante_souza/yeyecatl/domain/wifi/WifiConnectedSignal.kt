package io.github.dante_souza.yeyecatl.domain.wifi

data class WifiConnectedSignalSample(
    val bssid: String,
    val ssid: ObservedSsid,
    val rssiDbm: Int,
    val frequencyMhz: Int?,
    val observedAtMillis: Long,
    val connectionSessionId: Long = 0L
)

data class WifiConnectedSignalHistory(
    val samples: List<WifiConnectedSignalSample> = emptyList()
) {
    val latest: WifiConnectedSignalSample?
        get() = samples.lastOrNull()
}

sealed interface WifiConnectedSignalState {
    val history: WifiConnectedSignalHistory

    data object Idle : WifiConnectedSignalState {
        override val history: WifiConnectedSignalHistory = WifiConnectedSignalHistory()
    }

    data class Connected(
        val sample: WifiConnectedSignalSample,
        override val history: WifiConnectedSignalHistory
    ) : WifiConnectedSignalState

    data class Disconnected(
        override val history: WifiConnectedSignalHistory
    ) : WifiConnectedSignalState

    data class Unavailable(
        val reason: String,
        override val history: WifiConnectedSignalHistory
    ) : WifiConnectedSignalState
}

object WifiConnectedSignalAccumulator {
    const val DEFAULT_MAX_SAMPLES: Int = 240

    fun append(
        current: WifiConnectedSignalHistory,
        sample: WifiConnectedSignalSample,
        maxSamples: Int = DEFAULT_MAX_SAMPLES
    ): WifiConnectedSignalHistory {
        require(maxSamples > 0) { "maxSamples must be greater than zero" }

        val sameBssidSamples = if (current.latest?.bssid == sample.bssid) {
            current.samples
        } else {
            emptyList()
        }

        return WifiConnectedSignalHistory(
            samples = (sameBssidSamples + sample).takeLast(maxSamples)
        )
    }
}
