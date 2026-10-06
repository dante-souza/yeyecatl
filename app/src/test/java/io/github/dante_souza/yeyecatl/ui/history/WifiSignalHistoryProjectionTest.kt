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
