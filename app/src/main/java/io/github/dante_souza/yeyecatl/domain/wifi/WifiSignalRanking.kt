package io.github.dante_souza.yeyecatl.domain.wifi

enum class WifiSignalScope {
    All,
    Strongest,
    Weakest
}

data class WifiSignalRanking(
    val strongest: List<WifiScanObservation>,
    val weakest: List<WifiScanObservation>
)

object WifiSignalRanker {
    const val DEFAULT_LIMIT: Int = 5

    fun rank(
        observations: List<WifiScanObservation>,
        limit: Int = DEFAULT_LIMIT
    ): WifiSignalRanking {
        require(limit > 0) { "limit must be greater than zero" }

        val measurable = observations.filter { it.rssiDbm != null }

        return WifiSignalRanking(
            strongest = measurable
                .sortedWith(
                    compareByDescending<WifiScanObservation> { it.rssiDbm }
                        .thenBy { it.bssid.orEmpty() }
                )
                .take(limit),
            weakest = measurable
                .sortedWith(
                    compareBy<WifiScanObservation> { it.rssiDbm }
                        .thenBy { it.bssid.orEmpty() }
                )
                .take(limit)
        )
    }

    fun select(
        observations: List<WifiScanObservation>,
        scope: WifiSignalScope,
        limit: Int = DEFAULT_LIMIT
    ): List<WifiScanObservation> =
        when (scope) {
            WifiSignalScope.All -> observations
            WifiSignalScope.Strongest -> rank(observations, limit).strongest
            WifiSignalScope.Weakest -> rank(observations, limit).weakest
        }
}
