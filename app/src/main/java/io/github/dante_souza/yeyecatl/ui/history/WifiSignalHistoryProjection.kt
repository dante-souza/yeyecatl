package io.github.dante_souza.yeyecatl.ui.history

import io.github.dante_souza.yeyecatl.domain.wifi.WifiBand
import io.github.dante_souza.yeyecatl.domain.wifi.WifiRfInterpreter
import io.github.dante_souza.yeyecatl.domain.wifi.WifiScanObservation
import io.github.dante_souza.yeyecatl.domain.wifi.WifiTemporalObservationHistory

data class WifiSignalHistoryPoint(
    val observedAtMillis: Long,
    val rssiDbm: Int
)

data class WifiSignalHistoryVisualSeries(
    val label: String,
    val bssid: String,
    val colorKey: String,
    val points: List<WifiSignalHistoryPoint>
)

data class WifiSignalHistoryViewport(
    val minTimeMillis: Long,
    val maxTimeMillis: Long,
    val minRssiDbm: Int = -90,
    val maxRssiDbm: Int = -30
)

object WifiSignalHistoryProjection {
    const val DEFAULT_MAX_POINTS_PER_SERIES: Int = 30
    private const val SINGLE_POINT_WINDOW_MILLIS = 30_000L

    fun series(
        history: WifiTemporalObservationHistory,
        observations: List<WifiScanObservation>,
        band: WifiBand,
        maxPointsPerSeries: Int = DEFAULT_MAX_POINTS_PER_SERIES
    ): List<WifiSignalHistoryVisualSeries> {
        require(maxPointsPerSeries > 0) {
            "maxPointsPerSeries must be greater than zero"
        }

        return observations
            .distinctBy { it.bssid }
            .mapNotNull { observation ->
                val bssid = observation.bssid ?: return@mapNotNull null
                val points = history.samplesFor(bssid)
                    .asSequence()
                    .filter { WifiRfInterpreter.bandForFrequency(it.frequencyMhz) == band }
                    .sortedBy { it.observedAtMillis }
                    .map {
                        WifiSignalHistoryPoint(
                            observedAtMillis = it.observedAtMillis,
                            rssiDbm = it.rssiDbm
                        )
                    }
                    .toList()
                    .takeLast(maxPointsPerSeries)

                if (points.isEmpty()) {
                    return@mapNotNull null
                }

                WifiSignalHistoryVisualSeries(
                    label = observation.ssid.displayText ?: if (observation.ssid.isHidden) {
                        "<hidden>"
                    } else {
                        "<unavailable>"
                    },
                    bssid = bssid,
                    colorKey = bssid,
                    points = points
                )
            }
    }

    fun viewport(series: List<WifiSignalHistoryVisualSeries>): WifiSignalHistoryViewport? {
        val points = series.flatMap { it.points }
        if (points.isEmpty()) {
            return null
        }

        val observedMin = points.minOf { it.observedAtMillis }
        val observedMax = points.maxOf { it.observedAtMillis }
        val minTime = if (observedMin == observedMax) {
            observedMax - SINGLE_POINT_WINDOW_MILLIS
        } else {
            observedMin
        }

        return WifiSignalHistoryViewport(
            minTimeMillis = minTime,
            maxTimeMillis = observedMax
        )
    }

    fun timeToX(
        observedAtMillis: Long,
        viewport: WifiSignalHistoryViewport,
        widthPx: Float
    ): Float {
        val clamped = observedAtMillis.coerceIn(
            viewport.minTimeMillis,
            viewport.maxTimeMillis
        )
        val span = viewport.maxTimeMillis - viewport.minTimeMillis
        return ((clamped - viewport.minTimeMillis).toFloat() / span.toFloat()) * widthPx
    }

    fun rssiToY(
        rssiDbm: Int,
        viewport: WifiSignalHistoryViewport,
        heightPx: Float
    ): Float {
        val clamped = rssiDbm.coerceIn(viewport.minRssiDbm, viewport.maxRssiDbm)
        val span = viewport.maxRssiDbm - viewport.minRssiDbm
        val normalized = (clamped - viewport.minRssiDbm).toFloat() / span.toFloat()
        return heightPx * (1f - normalized)
    }
}
