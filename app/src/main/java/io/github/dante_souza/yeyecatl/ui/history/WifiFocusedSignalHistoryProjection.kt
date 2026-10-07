package io.github.dante_souza.yeyecatl.ui.history

import io.github.dante_souza.yeyecatl.domain.wifi.WifiSignalSample
import kotlin.math.ceil
import kotlin.math.floor

data class WifiFocusedSignalHistorySummary(
    val sampleCount: Int,
    val latestRssiDbm: Int,
    val strongestRssiDbm: Int,
    val weakestRssiDbm: Int
) {
    val rangeDb: Int
        get() = strongestRssiDbm - weakestRssiDbm
}

object WifiFocusedSignalHistoryProjection {
    const val DEFAULT_MAX_POINTS: Int = 60
    private const val SINGLE_POINT_WINDOW_MILLIS = 30_000L
    private const val RSSI_GRID_STEP_DB = 10
    private const val MIN_RSSI_SPAN_DB = 20

    fun points(
        samples: List<WifiSignalSample>,
        maxPoints: Int = DEFAULT_MAX_POINTS
    ): List<WifiSignalHistoryPoint> {
        require(maxPoints > 0) {
            "maxPoints must be greater than zero"
        }

        return samples
            .sortedBy { it.observedAtMillis }
            .takeLast(maxPoints)
            .map {
                WifiSignalHistoryPoint(
                    observedAtMillis = it.observedAtMillis,
                    rssiDbm = it.rssiDbm
                )
            }
    }

    fun summary(samples: List<WifiSignalSample>): WifiFocusedSignalHistorySummary? {
        if (samples.isEmpty()) {
            return null
        }

        val latest = samples.maxBy { it.observedAtMillis }

        return WifiFocusedSignalHistorySummary(
            sampleCount = samples.size,
            latestRssiDbm = latest.rssiDbm,
            strongestRssiDbm = samples.maxOf { it.rssiDbm },
            weakestRssiDbm = samples.minOf { it.rssiDbm }
        )
    }

    fun viewport(points: List<WifiSignalHistoryPoint>): WifiSignalHistoryViewport? {
        if (points.isEmpty()) {
            return null
        }

        val observedMinTime = points.minOf { it.observedAtMillis }
        val observedMaxTime = points.maxOf { it.observedAtMillis }
        val observedWeakestRssi = points.minOf { it.rssiDbm }
        val observedStrongestRssi = points.maxOf { it.rssiDbm }

        var minRssi = floorToRssiGrid(observedWeakestRssi)
        var maxRssi = ceilToRssiGrid(observedStrongestRssi)

        while (maxRssi - minRssi < MIN_RSSI_SPAN_DB) {
            val upperHeadroom = maxRssi - observedStrongestRssi
            val lowerHeadroom = observedWeakestRssi - minRssi

            if (upperHeadroom <= lowerHeadroom) {
                maxRssi += RSSI_GRID_STEP_DB
            } else {
                minRssi -= RSSI_GRID_STEP_DB
            }
        }

        return WifiSignalHistoryViewport(
            minTimeMillis = if (observedMinTime == observedMaxTime) {
                observedMaxTime - SINGLE_POINT_WINDOW_MILLIS
            } else {
                observedMinTime
            },
            maxTimeMillis = observedMaxTime,
            minRssiDbm = minRssi,
            maxRssiDbm = maxRssi
        )
    }

    private fun ceilToRssiGrid(rssiDbm: Int): Int =
        ceil(rssiDbm.toDouble() / RSSI_GRID_STEP_DB)
            .toInt() * RSSI_GRID_STEP_DB

    private fun floorToRssiGrid(rssiDbm: Int): Int =
        floor(rssiDbm.toDouble() / RSSI_GRID_STEP_DB)
            .toInt() * RSSI_GRID_STEP_DB
}
