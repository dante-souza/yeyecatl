package io.github.dante_souza.yeyecatl.domain.wifi

enum class WifiScanFreshness {
    Fresh,
    Cached,
    Unknown
}

enum class WifiScanResultSource {
    ApplicationRequest,
    PassiveAvailability
}

enum class WifiScanBlockReason {
    WifiHardwareUnavailable,
    WifiUnavailable,
    LocationServicesUnavailable,
    PermissionUnavailable
}

data class WifiScanSnapshot(
    val observations: List<WifiScanObservation>,
    val freshness: WifiScanFreshness,
    val source: WifiScanResultSource,
    val resultsUpdated: Boolean?,
    val receivedAtMillis: Long
)

sealed interface WifiScanState {
    val latestSnapshot: WifiScanSnapshot?

    data object Idle : WifiScanState {
        override val latestSnapshot: WifiScanSnapshot? = null
    }

    data class ScanRequested(
        override val latestSnapshot: WifiScanSnapshot?
    ) : WifiScanState

    data class Results(
        val snapshot: WifiScanSnapshot
    ) : WifiScanState {
        override val latestSnapshot: WifiScanSnapshot = snapshot
    }

    data class RequestRejected(
        val message: String,
        override val latestSnapshot: WifiScanSnapshot?
    ) : WifiScanState

    data class Blocked(
        val reason: WifiScanBlockReason,
        override val latestSnapshot: WifiScanSnapshot?
    ) : WifiScanState

    data class Error(
        val message: String,
        override val latestSnapshot: WifiScanSnapshot?
    ) : WifiScanState
}

object WifiScanStateTransitions {
    fun request(current: WifiScanState): WifiScanState =
        WifiScanState.ScanRequested(current.latestSnapshot)

    fun requestRejected(current: WifiScanState): WifiScanState =
        WifiScanState.RequestRejected(
            message = "Android did not accept the scan request. Existing results may be cached.",
            latestSnapshot = current.latestSnapshot
        )

    fun blocked(current: WifiScanState, reason: WifiScanBlockReason): WifiScanState =
        WifiScanState.Blocked(
            reason = reason,
            latestSnapshot = current.latestSnapshot
        )

    fun error(current: WifiScanState, message: String): WifiScanState =
        WifiScanState.Error(
            message = message,
            latestSnapshot = current.latestSnapshot
        )

    fun resultsAvailable(
        current: WifiScanState,
        observations: List<WifiScanObservation>,
        freshness: WifiScanFreshness,
        source: WifiScanResultSource,
        resultsUpdated: Boolean?,
        receivedAtMillis: Long
    ): WifiScanState {
        val previous = current.latestSnapshot
        if (freshness == WifiScanFreshness.Cached && observations.isEmpty() && previous != null) {
            return WifiScanState.Results(
                previous.copy(
                    freshness = WifiScanFreshness.Cached,
                    source = source,
                    resultsUpdated = resultsUpdated,
                    receivedAtMillis = receivedAtMillis
                )
            )
        }

        return WifiScanState.Results(
            WifiScanSnapshot(
                observations = observations,
                freshness = freshness,
                source = source,
                resultsUpdated = resultsUpdated,
                receivedAtMillis = receivedAtMillis
            )
        )
    }
}
