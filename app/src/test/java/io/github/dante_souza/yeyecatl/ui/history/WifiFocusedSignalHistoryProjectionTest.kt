package io.github.dante_souza.yeyecatl.ui.history

import io.github.dante_souza.yeyecatl.domain.wifi.ObservedSsid
import io.github.dante_souza.yeyecatl.domain.wifi.WifiSignalSample
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class WifiFocusedSignalHistoryProjectionTest {
    @Test
    fun projectsChronologicalPointsAndTruthfulRetainedSummary() {
        val samples = listOf(
            sample(rssiDbm = -61, observedAtMillis = 3_000L),
            sample(rssiDbm = -70, observedAtMillis = 1_000L),
            sample(rssiDbm = -55, observedAtMillis = 2_000L)
        )

        val points = WifiFocusedSignalHistoryProjection.points(samples)
        val summary = WifiFocusedSignalHistoryProjection.summary(samples)
        val viewport = WifiFocusedSignalHistoryProjection.viewport(points)

        assertEquals(listOf(1_000L, 2_000L, 3_000L), points.map { it.observedAtMillis })
        assertEquals(listOf(-70, -55, -61), points.map { it.rssiDbm })
        requireNotNull(summary)
        assertEquals(3, summary.sampleCount)
        assertEquals(-61, summary.latestRssiDbm)
        assertEquals(-55, summary.strongestRssiDbm)
        assertEquals(-70, summary.weakestRssiDbm)
        assertEquals(15, summary.rangeDb)
        requireNotNull(viewport)
        assertEquals(1_000L, viewport.minTimeMillis)
        assertEquals(3_000L, viewport.maxTimeMillis)
    }

    @Test
    fun focusedViewportAdaptsToStrongJ8SignalWithoutGlobalMinus30Clamp() {
        val points = WifiFocusedSignalHistoryProjection.points(
            samples = listOf(
                sample(rssiDbm = -22, observedAtMillis = 1_000L),
                sample(rssiDbm = -41, observedAtMillis = 2_000L),
                sample(rssiDbm = -33, observedAtMillis = 3_000L)
            )
        )

        val viewport = WifiFocusedSignalHistoryProjection.viewport(points)

        requireNotNull(viewport)
        assertEquals(-50, viewport.minRssiDbm)
        assertEquals(-20, viewport.maxRssiDbm)
    }

    @Test
    fun focusedViewportKeepsAtLeastTwentyDbForNearlyFlatHistory() {
        val points = WifiFocusedSignalHistoryProjection.points(
            samples = listOf(
                sample(rssiDbm = -35, observedAtMillis = 1_000L),
                sample(rssiDbm = -33, observedAtMillis = 2_000L)
            )
        )

        val viewport = WifiFocusedSignalHistoryProjection.viewport(points)

        requireNotNull(viewport)
        assertEquals(20, viewport.maxRssiDbm - viewport.minRssiDbm)
        assertEquals(-40, viewport.minRssiDbm)
        assertEquals(-20, viewport.maxRssiDbm)
    }

    @Test
    fun projectionKeepsNewestPointsWhenDisplayLimitIsSmallerThanRetainedHistory() {
        val samples = (1L..5L).map { index ->
            sample(
                rssiDbm = -40 - index.toInt(),
                observedAtMillis = index * 1_000L
            )
        }

        val points = WifiFocusedSignalHistoryProjection.points(
            samples = samples,
            maxPoints = 3
        )

        assertEquals(listOf(3_000L, 4_000L, 5_000L), points.map { it.observedAtMillis })
    }

    @Test
    fun emptyHistoryHasNoSummaryOrViewport() {
        val points = WifiFocusedSignalHistoryProjection.points(emptyList())

        assertEquals(emptyList<WifiSignalHistoryPoint>(), points)
        assertNull(WifiFocusedSignalHistoryProjection.summary(emptyList()))
        assertNull(WifiFocusedSignalHistoryProjection.viewport(points))
    }

    @Test
    fun nonPositiveDisplayLimitIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            WifiFocusedSignalHistoryProjection.points(
                samples = listOf(sample(-50, 1_000L)),
                maxPoints = 0
            )
        }
    }

    private fun sample(
        rssiDbm: Int,
        observedAtMillis: Long
    ): WifiSignalSample =
        WifiSignalSample(
            bssid = "00:11:22:33:44:55",
            ssid = ObservedSsid(
                displayText = "mesh",
                rawBytes = null,
                isHidden = false
            ),
            rssiDbm = rssiDbm,
            frequencyMhz = 5180,
            observedAtMillis = observedAtMillis
        )
}
