package io.github.dante_souza.yeyecatl.ui.history

import io.github.dante_souza.yeyecatl.domain.wifi.ObservedSsid
import io.github.dante_souza.yeyecatl.domain.wifi.WifiBand
import io.github.dante_souza.yeyecatl.domain.wifi.WifiScanObservation
import io.github.dante_souza.yeyecatl.domain.wifi.WifiSignalSample
import io.github.dante_souza.yeyecatl.domain.wifi.WifiTemporalObservationHistory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WifiSignalHistoryProjectionTest {
    @Test
    fun projectsOnlySelectedBssidsAndSelectedBand() {
        val history = WifiTemporalObservationHistory(
            samplesByBssid = mapOf(
                "00:00:00:00:00:01" to listOf(
                    sample("mesh", "00:00:00:00:00:01", -40, 2412, 1_000L),
                    sample("mesh", "00:00:00:00:00:01", -45, 5180, 2_000L),
                    sample("mesh", "00:00:00:00:00:01", -42, 2412, 3_000L)
                ),
                "00:00:00:00:00:02" to listOf(
                    sample("other", "00:00:00:00:00:02", -60, 2412, 3_000L)
                )
            )
        )

        val series = WifiSignalHistoryProjection.series(
            history = history,
            observations = listOf(observation("mesh", "00:00:00:00:00:01", -42, 2412)),
            band = WifiBand.Ghz2_4
        )

        assertEquals(1, series.size)
        assertEquals("00:00:00:00:00:01", series.single().bssid)
        assertEquals(listOf(1_000L, 3_000L), series.single().points.map { it.observedAtMillis })
        assertEquals(listOf(-40, -42), series.single().points.map { it.rssiDbm })
    }

    @Test
    fun projectionKeepsOnlyMostRecentVisualPointsPerBssid() {
        val bssid = "00:00:00:00:00:01"
        val history = WifiTemporalObservationHistory(
            samplesByBssid = mapOf(
                bssid to (1L..5L).map { second ->
                    sample(
                        ssid = "lab",
                        bssid = bssid,
                        rssiDbm = -40 - second.toInt(),
                        frequencyMhz = 2412,
                        observedAtMillis = second * 1_000L
                    )
                }
            )
        )

        val series = WifiSignalHistoryProjection.series(
            history = history,
            observations = listOf(observation("lab", bssid, -45, 2412)),
            band = WifiBand.Ghz2_4,
            maxPointsPerSeries = 3
        )

        assertEquals(
            listOf(3_000L, 4_000L, 5_000L),
            series.single().points.map { it.observedAtMillis }
        )
    }

    @Test
    fun sameSsidAcrossDifferentBssidsRemainsSeparateSeries() {
        val history = WifiTemporalObservationHistory(
            samplesByBssid = mapOf(
                "00:00:00:00:00:01" to listOf(
                    sample("mesh", "00:00:00:00:00:01", -40, 2412, 1_000L)
                ),
                "00:00:00:00:00:02" to listOf(
                    sample("mesh", "00:00:00:00:00:02", -55, 2412, 1_000L)
                )
            )
        )

        val series = WifiSignalHistoryProjection.series(
            history = history,
            observations = listOf(
                observation("mesh", "00:00:00:00:00:01", -40, 2412),
                observation("mesh", "00:00:00:00:00:02", -55, 2412)
            ),
            band = WifiBand.Ghz2_4
        )

        assertEquals(2, series.size)
        assertEquals(
            setOf("00:00:00:00:00:01", "00:00:00:00:00:02"),
            series.map { it.bssid }.toSet()
        )
    }

    @Test
    fun viewportUsesObservedTimeRangeAndStableRssiBounds() {
        val series = listOf(
            WifiSignalHistoryVisualSeries(
                label = "lab",
                bssid = "00:00:00:00:00:01",
                colorKey = "00:00:00:00:00:01",
                points = listOf(
                    WifiSignalHistoryPoint(1_000L, -80),
                    WifiSignalHistoryPoint(4_000L, -40)
                )
            )
        )

        val viewport = WifiSignalHistoryProjection.viewport(series)!!

        assertEquals(1_000L, viewport.minTimeMillis)
        assertEquals(4_000L, viewport.maxTimeMillis)
        assertEquals(-90, viewport.minRssiDbm)
        assertEquals(-30, viewport.maxRssiDbm)
    }

    @Test
    fun singleTimestampGetsDisplaySpanWithoutInventingSamples() {
        val series = listOf(
            WifiSignalHistoryVisualSeries(
                label = "lab",
                bssid = "00:00:00:00:00:01",
                colorKey = "00:00:00:00:00:01",
                points = listOf(WifiSignalHistoryPoint(5_000L, -50))
            )
        )

        val viewport = WifiSignalHistoryProjection.viewport(series)!!

        assertTrue(viewport.minTimeMillis < viewport.maxTimeMillis)
        assertEquals(5_000L, viewport.maxTimeMillis)
        assertEquals(1, series.single().points.size)
    }

    @Test
    fun rollingViewportUsesFixedTwoMinuteWindow() {
        val viewport = WifiSignalHistoryProjection.rollingViewport(
            nowMillis = 200_000L
        )

        assertEquals(80_000L, viewport.minTimeMillis)
        assertEquals(200_000L, viewport.maxTimeMillis)
    }

    @Test
    fun visiblePointsDropsSamplesOutsideRollingWindow() {
        val viewport = WifiSignalHistoryProjection.rollingViewport(
            nowMillis = 200_000L
        )
        val points = listOf(
            WifiSignalHistoryPoint(70_000L, -80),
            WifiSignalHistoryPoint(80_000L, -70),
            WifiSignalHistoryPoint(150_000L, -60),
            WifiSignalHistoryPoint(200_000L, -50)
        )

        assertEquals(
            listOf(80_000L, 150_000L, 200_000L),
            WifiSignalHistoryProjection.visiblePoints(points, viewport)
                .map { it.observedAtMillis }
        )
    }

    @Test
    fun longObservationGapBreaksHistoryLine() {
        val points = listOf(
            WifiSignalHistoryPoint(1_000L, -60),
            WifiSignalHistoryPoint(5_000L, -61),
            WifiSignalHistoryPoint(30_000L, -62),
            WifiSignalHistoryPoint(34_000L, -63)
        )

        val segments = WifiSignalHistoryProjection.contiguousSegments(
            points = points,
            maxGapMillis = 15_000L
        )

        assertEquals(2, segments.size)
        assertEquals(listOf(1_000L, 5_000L), segments[0].map { it.observedAtMillis })
        assertEquals(listOf(30_000L, 34_000L), segments[1].map { it.observedAtMillis })
    }

    @Test
    fun timeProjectionMapsRangeAndClamps() {
        val viewport = WifiSignalHistoryViewport(
            minTimeMillis = 1_000L,
            maxTimeMillis = 5_000L
        )

        assertEquals(0f, WifiSignalHistoryProjection.timeToX(1_000L, viewport, 100f))
        assertEquals(50f, WifiSignalHistoryProjection.timeToX(3_000L, viewport, 100f))
        assertEquals(100f, WifiSignalHistoryProjection.timeToX(5_000L, viewport, 100f))
        assertEquals(0f, WifiSignalHistoryProjection.timeToX(0L, viewport, 100f))
        assertEquals(100f, WifiSignalHistoryProjection.timeToX(6_000L, viewport, 100f))
    }

    @Test
    fun rssiProjectionPlacesStrongerSignalHigher() {
        val viewport = WifiSignalHistoryViewport(
            minTimeMillis = 1_000L,
            maxTimeMillis = 5_000L
        )

        val strong = WifiSignalHistoryProjection.rssiToY(-40, viewport, 100f)
        val weak = WifiSignalHistoryProjection.rssiToY(-80, viewport, 100f)

        assertTrue(strong < weak)
        assertEquals(0f, WifiSignalHistoryProjection.rssiToY(-20, viewport, 100f))
        assertEquals(100f, WifiSignalHistoryProjection.rssiToY(-100, viewport, 100f))
    }

    private fun sample(
        ssid: String,
        bssid: String,
        rssiDbm: Int,
        frequencyMhz: Int,
        observedAtMillis: Long
    ): WifiSignalSample =
        WifiSignalSample(
            bssid = bssid,
            ssid = ObservedSsid(ssid, null, false),
            rssiDbm = rssiDbm,
            frequencyMhz = frequencyMhz,
            observedAtMillis = observedAtMillis
        )

    private fun observation(
        ssid: String,
        bssid: String,
        rssiDbm: Int,
        frequencyMhz: Int
    ): WifiScanObservation =
        WifiScanObservation(
            ssid = ObservedSsid(ssid, null, false),
            bssid = bssid,
            rssiDbm = rssiDbm,
            frequencyMhz = frequencyMhz,
            capabilities = "[ESS]",
            platformTimestampMicros = 1L
        )
}
