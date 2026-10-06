package io.github.dante_souza.yeyecatl.domain.wifi

data class WifiSignalSample(
    val bssid: String,
    val ssid: ObservedSsid,
    val rssiDbm: Int,
    val frequencyMhz: Int?,
    val observedAtMillis: Long
)

data class WifiTemporalObservationHistory(
    val samplesByBssid: Map<String, List<WifiSignalSample>> = emptyMap()
) {
    fun samplesFor(bssid: String): List<WifiSignalSample> =
        samplesByBssid[bssid].orEmpty()
}

object WifiTemporalObservationAccumulator {
    fun append(
        current: WifiTemporalObservationHistory,
        snapshot: WifiScanSnapshot
    ): WifiTemporalObservationHistory {
        if (snapshot.freshness != WifiScanFreshness.Fresh) {
            return current
        }

        val samples = snapshot.observations.mapNotNull { observation ->
            val bssid = observation.bssid ?: return@mapNotNull null
            val rssiDbm = observation.rssiDbm ?: return@mapNotNull null

            WifiSignalSample(
                bssid = bssid,
                ssid = observation.ssid,
                rssiDbm = rssiDbm,
                frequencyMhz = observation.frequencyMhz,
                observedAtMillis = snapshot.receivedAtMillis
            )
        }

        if (samples.isEmpty()) {
            return current
        }

        val updated = current.samplesByBssid.toMutableMap()
        samples.groupBy { it.bssid }.forEach { (bssid, newSamples) ->
            updated[bssid] = current.samplesFor(bssid) + newSamples
        }

        return WifiTemporalObservationHistory(samplesByBssid = updated)
    }
}
