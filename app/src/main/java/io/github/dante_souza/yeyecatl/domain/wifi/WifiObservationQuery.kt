package io.github.dante_souza.yeyecatl.domain.wifi

data class WifiObservationQuery(
    val band: WifiBand? = null,
    val text: String = "",
    val sort: WifiObservationSort = WifiObservationSort.PlatformOrder
)

enum class WifiObservationSort {
    PlatformOrder,
    StrongestFirst,
    WeakestFirst,
    SsidAscending,
    ChannelAscending
}

object WifiObservationQueryEngine {
    fun apply(
        observations: List<WifiScanObservation>,
        query: WifiObservationQuery
    ): List<WifiScanObservation> {
        val normalizedText = query.text.trim().lowercase()

        val filtered = observations.filter { observation ->
            matchesBand(observation, query.band) &&
                matchesText(observation, normalizedText)
        }

        return when (query.sort) {
            WifiObservationSort.PlatformOrder -> filtered
            WifiObservationSort.StrongestFirst ->
                filtered.sortedWith(strongestComparator())
            WifiObservationSort.WeakestFirst ->
                filtered.sortedWith(weakestComparator())
            WifiObservationSort.SsidAscending ->
                filtered.sortedWith(ssidComparator())
            WifiObservationSort.ChannelAscending ->
                filtered.sortedWith(channelComparator())
        }
    }

    private fun matchesBand(
        observation: WifiScanObservation,
        band: WifiBand?
    ): Boolean =
        band == null || WifiRfInterpreter.bandForFrequency(observation.frequencyMhz) == band

    private fun matchesText(
        observation: WifiScanObservation,
        normalizedText: String
    ): Boolean {
        if (normalizedText.isEmpty()) {
            return true
        }

        val ssid = observation.ssid.displayText?.lowercase().orEmpty()
        val bssid = observation.bssid?.lowercase().orEmpty()
        return normalizedText in ssid || normalizedText in bssid
    }

    private fun strongestComparator(): Comparator<WifiScanObservation> =
        compareBy<WifiScanObservation> { it.rssiDbm == null }
            .thenByDescending { it.rssiDbm ?: Int.MIN_VALUE }
            .thenBy { it.ssid.displayText?.lowercase().orEmpty() }
            .thenBy { it.bssid.orEmpty() }

    private fun weakestComparator(): Comparator<WifiScanObservation> =
        compareBy<WifiScanObservation> { it.rssiDbm == null }
            .thenBy { it.rssiDbm ?: Int.MAX_VALUE }
            .thenBy { it.ssid.displayText?.lowercase().orEmpty() }
            .thenBy { it.bssid.orEmpty() }

    private fun ssidComparator(): Comparator<WifiScanObservation> =
        compareBy<WifiScanObservation> { it.ssid.displayText == null }
            .thenBy { it.ssid.displayText?.lowercase().orEmpty() }
            .thenBy { it.bssid.orEmpty() }

    private fun channelComparator(): Comparator<WifiScanObservation> =
        compareBy<WifiScanObservation> {
            WifiRfInterpreter.channelForFrequency(it.frequencyMhz) == null
        }
            .thenBy {
                WifiRfInterpreter.channelForFrequency(it.frequencyMhz) ?: Int.MAX_VALUE
            }
            .thenBy { it.frequencyMhz ?: Int.MAX_VALUE }
            .thenBy { it.bssid.orEmpty() }
}
