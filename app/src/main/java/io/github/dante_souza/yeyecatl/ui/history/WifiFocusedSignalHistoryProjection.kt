package io.github.dante_souza.yeyecatl.ui.history

import io.github.dante_souza.yeyecatl.domain.wifi.WifiSignalSample

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

        val observedMin = points.minOf { it.observedAtMillis }
        val observedMax = points.maxOf { it.observedAtMillis }

        return WifiSignalHistoryViewport(
            minTimeMillis = if (observedMin == observedMax) {
                observedMax - SINGLE_POINT_WINDOW_MILLIS
            } else {
                observedMin
            },
            maxTimeMillis = observedMax
        )
    }
}
