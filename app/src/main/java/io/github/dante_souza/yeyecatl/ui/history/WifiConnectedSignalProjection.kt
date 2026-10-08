package io.github.dante_souza.yeyecatl.ui.history

import io.github.dante_souza.yeyecatl.domain.wifi.WifiConnectedSignalSample
import kotlin.math.max
import kotlin.math.min

enum class WifiConnectedSignalScaleMode {
    Fixed,
    Auto
}

data class WifiConnectedRssiRange(
    val minRssiDbm: Int,
    val maxRssiDbm: Int
) {
    init {
        require(minRssiDbm < maxRssiDbm) { "minRssiDbm must be below maxRssiDbm" }
    }
}

object WifiConnectedSignalProjection {
    val fixedRange = WifiConnectedRssiRange(
        minRssiDbm = -100,
        maxRssiDbm = -20
    )

    fun autoTargetRange(
        samples: List<WifiConnectedSignalSample>,
        nowMillis: Long,
        recentWindowMillis: Long = DEFAULT_AUTO_WINDOW_MILLIS
    ): WifiConnectedRssiRange {
        require(recentWindowMillis > 0L) { "recentWindowMillis must be greater than zero" }

        val cutoff = nowMillis - recentWindowMillis
        val recent = samples.filter { it.observedAtMillis in cutoff..nowMillis }
        if (recent.isEmpty()) {
            return fixedRange
        }

        val observedMin = recent.minOf { it.rssiDbm }
        val observedMax = recent.maxOf { it.rssiDbm }

        var minRssi = floorToTen(observedMin - AUTO_PADDING_DB)
            .coerceAtLeast(AUTO_MIN_RSSI_DBM)
        var maxRssi = ceilToTen(observedMax + AUTO_PADDING_DB)
            .coerceAtMost(AUTO_MAX_RSSI_DBM)

        if (maxRssi - minRssi < AUTO_MIN_SPAN_DB) {
            val midpoint = (minRssi + maxRssi) / 2
            minRssi = floorToTen(midpoint - AUTO_MIN_SPAN_DB / 2)
            maxRssi = minRssi + AUTO_MIN_SPAN_DB

            if (minRssi < AUTO_MIN_RSSI_DBM) {
                val shift = AUTO_MIN_RSSI_DBM - minRssi
                minRssi += shift
                maxRssi += shift
            }
            if (maxRssi > AUTO_MAX_RSSI_DBM) {
                val shift = maxRssi - AUTO_MAX_RSSI_DBM
                minRssi -= shift
                maxRssi -= shift
            }
        }

        return WifiConnectedRssiRange(
            minRssiDbm = minRssi.coerceAtLeast(AUTO_MIN_RSSI_DBM),
            maxRssiDbm = maxRssi.coerceAtMost(AUTO_MAX_RSSI_DBM)
        )
    }

    fun stabilizeAutoRange(
        current: WifiConnectedRssiRange?,
        target: WifiConnectedRssiRange
    ): WifiConnectedRssiRange {
        if (current == null) {
            return target
        }

        val needsExpansion =
            target.minRssiDbm < current.minRssiDbm ||
                target.maxRssiDbm > current.maxRssiDbm
        if (needsExpansion) {
            return WifiConnectedRssiRange(
                minRssiDbm = min(current.minRssiDbm, target.minRssiDbm),
                maxRssiDbm = max(current.maxRssiDbm, target.maxRssiDbm)
            )
        }

        val canShrink =
            target.minRssiDbm >= current.minRssiDbm + AUTO_HYSTERESIS_DB ||
                target.maxRssiDbm <= current.maxRssiDbm - AUTO_HYSTERESIS_DB

        return if (canShrink) target else current
    }

    fun sessionSegments(
        samples: List<WifiConnectedSignalSample>,
        maxGapMillis: Long
    ): List<List<WifiConnectedSignalSample>> {
        require(maxGapMillis > 0L) { "maxGapMillis must be greater than zero" }
        if (samples.isEmpty()) {
            return emptyList()
        }

        val sorted = samples.sortedBy { it.observedAtMillis }
        val segments = mutableListOf<MutableList<WifiConnectedSignalSample>>()
        var current = mutableListOf(sorted.first())
        segments += current

        sorted.drop(1).forEach { sample ->
            val previous = current.last()
            val sessionChanged = sample.connectionSessionId != previous.connectionSessionId
            val gapTooLarge = sample.observedAtMillis - previous.observedAtMillis > maxGapMillis
            if (sessionChanged || gapTooLarge) {
                current = mutableListOf(sample)
                segments += current
            } else {
                current += sample
            }
        }

        return segments
    }

    fun ticks(range: WifiConnectedRssiRange): List<Int> {
        val span = range.maxRssiDbm - range.minRssiDbm
        val step = if (span <= 50) 10 else 20
        val first = ceilToStep(range.minRssiDbm, step)
        return generateSequence(first) { it + step }
            .takeWhile { it <= range.maxRssiDbm }
            .toList()
    }

    private fun floorToTen(value: Int): Int = Math.floorDiv(value, 10) * 10

    private fun ceilToTen(value: Int): Int = -Math.floorDiv(-value, 10) * 10

    private fun ceilToStep(value: Int, step: Int): Int =
        -Math.floorDiv(-value, step) * step

    private const val DEFAULT_AUTO_WINDOW_MILLIS = 60_000L
    private const val AUTO_PADDING_DB = 5
    private const val AUTO_MIN_SPAN_DB = 30
    private const val AUTO_HYSTERESIS_DB = 10
    private const val AUTO_MIN_RSSI_DBM = -100
    private const val AUTO_MAX_RSSI_DBM = -20
}
