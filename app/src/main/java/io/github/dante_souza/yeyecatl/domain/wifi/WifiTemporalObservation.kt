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
    const val DEFAULT_MAX_SAMPLES_PER_BSSID: Int = 120

    fun append(
        current: WifiTemporalObservationHistory,
        snapshot: WifiScanSnapshot,
        maxSamplesPerBssid: Int = DEFAULT_MAX_SAMPLES_PER_BSSID
    ): WifiTemporalObservationHistory {
        require(maxSamplesPerBssid > 0) {
            "maxSamplesPerBssid must be greater than zero"
        }
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
            updated[bssid] = (current.samplesFor(bssid) + newSamples)
                .takeLast(maxSamplesPerBssid)
        }

        return WifiTemporalObservationHistory(samplesByBssid = updated)
    }
}
