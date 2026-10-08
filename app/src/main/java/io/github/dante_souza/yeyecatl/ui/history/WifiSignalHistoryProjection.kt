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
    const val DEFAULT_ROLLING_WINDOW_MILLIS = 120_000L
    const val MIN_LINE_GAP_THRESHOLD_MILLIS = 15_000L
    private const val SINGLE_POINT_WINDOW_MILLIS = 30_000L
    private const val MIN_STALE_HOLD_MILLIS = 15_000L
    private const val MAX_STALE_HOLD_MILLIS = 90_000L
    private const val MIN_STALE_GRACE_MILLIS = 10_000L
    private const val MAX_STALE_GRACE_MILLIS = 30_000L

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

    fun rollingViewport(
        nowMillis: Long,
        windowMillis: Long = DEFAULT_ROLLING_WINDOW_MILLIS
    ): WifiSignalHistoryViewport {
        require(windowMillis > 0L) { "windowMillis must be greater than zero" }
        return WifiSignalHistoryViewport(
            minTimeMillis = nowMillis - windowMillis,
            maxTimeMillis = nowMillis
        )
    }

    fun visiblePoints(
        points: List<WifiSignalHistoryPoint>,
        viewport: WifiSignalHistoryViewport
    ): List<WifiSignalHistoryPoint> =
        points.filter {
            it.observedAtMillis in viewport.minTimeMillis..viewport.maxTimeMillis
        }

    fun contiguousSegments(
        points: List<WifiSignalHistoryPoint>,
        maxGapMillis: Long
    ): List<List<WifiSignalHistoryPoint>> {
        require(maxGapMillis > 0L) { "maxGapMillis must be greater than zero" }
        if (points.isEmpty()) {
            return emptyList()
        }

        val sorted = points.sortedBy { it.observedAtMillis }
        val segments = mutableListOf<MutableList<WifiSignalHistoryPoint>>()
        var current = mutableListOf(sorted.first())
        segments += current

        sorted.drop(1).forEach { point ->
            val previous = current.last()
            if (point.observedAtMillis - previous.observedAtMillis > maxGapMillis) {
                current = mutableListOf(point)
                segments += current
            } else {
                current += point
            }
        }

        return segments
    }

    fun staleHoldMillis(pollingIntervalMillis: Long): Long {
        require(pollingIntervalMillis > 0L) { "pollingIntervalMillis must be greater than zero" }
        val graceMillis = (pollingIntervalMillis / 2L)
            .coerceIn(MIN_STALE_GRACE_MILLIS, MAX_STALE_GRACE_MILLIS)
        return (pollingIntervalMillis + graceMillis)
            .coerceIn(MIN_STALE_HOLD_MILLIS, MAX_STALE_HOLD_MILLIS)
    }

    fun foregroundBssids(
        series: List<WifiSignalHistoryVisualSeries>,
        maxForegroundSeries: Int
    ): Set<String> {
        require(maxForegroundSeries > 0) { "maxForegroundSeries must be greater than zero" }
        return series
            .sortedWith(
                compareByDescending<WifiSignalHistoryVisualSeries> {
                    it.points.lastOrNull()?.rssiDbm ?: Int.MIN_VALUE
                }.thenBy { it.bssid }
            )
            .take(maxForegroundSeries)
            .mapTo(linkedSetOf()) { it.bssid }
    }

    // Presentation-only endpoint. No sample is appended to temporal history.
    // The hold expires rather than suggesting indefinitely fresh RF data.
    fun heldEndpoint(
        points: List<WifiSignalHistoryPoint>,
        nowMillis: Long,
        maxHoldMillis: Long
    ): WifiSignalHistoryPoint? {
        require(maxHoldMillis > 0L)
        val latest = points.maxByOrNull { it.observedAtMillis } ?: return null
        val age = nowMillis - latest.observedAtMillis
        if (age < 0L || age >= maxHoldMillis) return null
        return WifiSignalHistoryPoint(
            observedAtMillis = nowMillis,
            rssiDbm = latest.rssiDbm
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
