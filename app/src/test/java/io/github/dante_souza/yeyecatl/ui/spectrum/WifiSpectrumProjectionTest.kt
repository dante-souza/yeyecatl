package io.github.dante_souza.yeyecatl.ui.spectrum

import io.github.dante_souza.yeyecatl.domain.wifi.ObservedSsid
import io.github.dante_souza.yeyecatl.domain.wifi.WifiBand
import io.github.dante_souza.yeyecatl.domain.wifi.WifiChannelWidth
import io.github.dante_souza.yeyecatl.domain.wifi.WifiScanObservation
import io.github.dante_souza.yeyecatl.domain.wifi.WifiSpectrumCompleteness
import io.github.dante_souza.yeyecatl.domain.wifi.WifiStandard
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WifiSpectrumProjectionTest {
    @Test
    fun frequencyProjectionMapsMinMaxMidpointAndClamps() {
        val viewport = WifiSpectrumViewport(
            band = WifiBand.Ghz5,
            minFrequencyMhz = 5000,
            maxFrequencyMhz = 6000,
            frequencyTicks = emptyList()
        )

        assertEquals(0f, WifiSpectrumProjection.frequencyToX(5000, viewport, 100f))
        assertEquals(100f, WifiSpectrumProjection.frequencyToX(6000, viewport, 100f))
        assertEquals(50f, WifiSpectrumProjection.frequencyToX(5500, viewport, 100f))
        assertEquals(0f, WifiSpectrumProjection.frequencyToX(4900, viewport, 100f))
        assertEquals(100f, WifiSpectrumProjection.frequencyToX(6100, viewport, 100f))
        assertTrue(
            WifiSpectrumProjection.frequencyToX(5200, viewport, 100f) <
                WifiSpectrumProjection.frequencyToX(5300, viewport, 100f)
        )
    }

    @Test
    fun rssiProjectionPlacesStrongerSignalsHigherAndClamps() {
        val viewport = WifiSpectrumViewports.forBand(WifiBand.Ghz2_4)

        assertEquals(0f, WifiSpectrumProjection.rssiToY(-30, viewport, 120f))
        assertEquals(120f, WifiSpectrumProjection.rssiToY(-100, viewport, 120f))
        assertEquals(0f, WifiSpectrumProjection.rssiToY(-10, viewport, 120f))
        assertEquals(120f, WifiSpectrumProjection.rssiToY(-110, viewport, 120f))
        assertTrue(
            WifiSpectrumProjection.rssiToY(-40, viewport, 120f) <
                WifiSpectrumProjection.rssiToY(-80, viewport, 120f)
        )
    }

    @Test
    fun viewportsAreBandSpecific() {
        assertEquals(WifiBand.Ghz2_4, WifiSpectrumViewports.forBand(WifiBand.Ghz2_4).band)
        assertEquals(WifiBand.Ghz5, WifiSpectrumViewports.forBand(WifiBand.Ghz5).band)
        assertEquals(WifiBand.Ghz6, WifiSpectrumViewports.forBand(WifiBand.Ghz6).band)
        assertTrue(WifiSpectrumViewports.forBand(WifiBand.Ghz6).minFrequencyMhz <= 5935)
        assertTrue(WifiSpectrumViewports.forBand(WifiBand.Ghz6).maxFrequencyMhz >= 7115)
    }

    @Test
    fun visualObservationsRenderWeakerSignalsFirst() {
        val projections = WifiSpectrumProjection.visualObservations(
            observations = listOf(
                observation("strong", "00:00:00:00:00:01", -40, 2412),
                observation("weak", "00:00:00:00:00:02", -80, 2437)
            ),
            band = WifiBand.Ghz2_4
        )

        assertEquals("weak", projections[0].label)
        assertEquals("strong", projections[1].label)
    }

    @Test
    fun projectionPreservesDomainSegmentCounts() {
        listOf(
            observation("20", "00:00:00:00:00:20", -50, 5180),
            observation("80", "00:00:00:00:00:80", -51, 5180, WifiChannelWidth.Mhz80, 5210),
            observation("160", "00:00:00:00:01:60", -52, 5180, WifiChannelWidth.Mhz160, 5250),
            observation("320", "00:00:00:00:03:20", -53, 5975, WifiChannelWidth.Mhz320, 6105)
        ).forEach { scanObservation ->
            val projection = WifiSpectrumProjection.visualObservations(
                listOf(scanObservation),
                WifiBand.Ghz5
            ).ifEmpty {
                WifiSpectrumProjection.visualObservations(listOf(scanObservation), WifiBand.Ghz6)
            }.single()

            assertEquals(WifiSpectrumCompleteness.Complete, projection.footprint.completeness)
            assertEquals(1, WifiSpectrumProjection.drawableSegments(projection))
        }

        val eightyPlusEighty = WifiSpectrumProjection.visualObservations(
            listOf(
                observation(
                    ssid = "80+80",
                    bssid = "00:00:00:00:88:80",
                    rssiDbm = -60,
                    frequencyMhz = 5180,
                    width = WifiChannelWidth.Mhz80Plus80,
                    center0 = 5210,
                    center1 = 5530
                )
            ),
            WifiBand.Ghz5
        ).single()

        assertEquals(2, WifiSpectrumProjection.drawableSegments(eightyPlusEighty))
    }

    @Test
    fun unknownFootprintDoesNotFabricateDrawableSegments() {
        val projection = WifiSpectrumProjection.visualObservations(
            listOf(
                observation(
                    ssid = "unknown",
                    bssid = "00:00:00:00:00:99",
                    rssiDbm = -60,
                    frequencyMhz = 5180,
                    width = WifiChannelWidth.Unknown
                )
            ),
            WifiBand.Ghz5
        ).single()

        assertEquals(WifiSpectrumCompleteness.Partial, projection.footprint.completeness)
        assertEquals(0, WifiSpectrumProjection.drawableSegments(projection))
    }

    @Test
    fun duplicateSsidsWithDifferentBssidsRemainSeparate() {
        val projections = WifiSpectrumProjection.visualObservations(
            observations = listOf(
                observation("whanganui", "00:00:00:00:00:01", -43, 2412),
                observation("whanganui", "00:00:00:00:00:02", -65, 2437)
            ),
            band = WifiBand.Ghz2_4
        )

        assertEquals(2, projections.size)
        assertEquals(2, projections.map { it.bssid }.toSet().size)
    }

    private fun observation(
        ssid: String,
        bssid: String,
        rssiDbm: Int,
        frequencyMhz: Int,
        width: WifiChannelWidth = WifiChannelWidth.Mhz20,
        center0: Int? = null,
        center1: Int? = null
    ): WifiScanObservation =
        WifiScanObservation(
            ssid = ObservedSsid(
                displayText = ssid,
                rawBytes = null,
                isHidden = false
            ),
            bssid = bssid,
            rssiDbm = rssiDbm,
            frequencyMhz = frequencyMhz,
            channelWidth = width,
            centerFrequency0Mhz = center0,
            centerFrequency1Mhz = center1,
            wifiStandard = WifiStandard.Unknown,
            capabilities = "[ESS]",
            platformTimestampMicros = 1L
        )
}
