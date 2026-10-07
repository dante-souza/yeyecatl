package io.github.dante_souza.yeyecatl.domain.wifi

data class WifiObservationSelection(
    val bssid: String
) {
    init {
        require(bssid.isNotBlank()) {
            "Selected BSSID must not be blank"
        }
    }
}

data class WifiObservationDetail(
    val selection: WifiObservationSelection,
    val latestObservation: WifiScanObservation,
    val rf: WifiRfCharacteristics,
    val spectrum: WifiSpectrumFootprint,
    val retainedSignalSamples: List<WifiSignalSample>,
    val latestSnapshotReceivedAtMillis: Long
) {
    val retainedSignalSampleCount: Int
        get() = retainedSignalSamples.size

    val firstRetainedSampleAtMillis: Long?
        get() = retainedSignalSamples.firstOrNull()?.observedAtMillis

    val lastRetainedSampleAtMillis: Long?
        get() = retainedSignalSamples.lastOrNull()?.observedAtMillis

    val retainedHistorySpanMillis: Long?
        get() {
            val first = firstRetainedSampleAtMillis ?: return null
            val last = lastRetainedSampleAtMillis ?: return null
            return (last - first).coerceAtLeast(0L)
        }
}

object WifiObservationDetailResolver {
    fun resolve(
        selection: WifiObservationSelection?,
        snapshot: WifiScanSnapshot?,
        history: WifiTemporalObservationHistory
    ): WifiObservationDetail? {
        val selected = selection ?: return null
        val currentSnapshot = snapshot ?: return null
        val observation = currentSnapshot.observations.firstOrNull {
            it.bssid == selected.bssid
        } ?: return null
        val rf = WifiRfInterpreter.interpret(observation)

        return WifiObservationDetail(
            selection = selected,
            latestObservation = observation,
            rf = rf,
            spectrum = WifiSpectrumGeometry.footprint(rf),
            retainedSignalSamples = history.samplesFor(selected.bssid),
            latestSnapshotReceivedAtMillis = currentSnapshot.receivedAtMillis
        )
    }
}
