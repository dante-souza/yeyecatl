package io.github.dante_souza.yeyecatl.domain.wifi

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WifiChannelOccupancyAnalyzerTest {
    @Test
    fun countsPrimaryChannelsWithinSelectedBand() {
        val overview = WifiChannelOccupancyAnalyzer.analyze(
            observations = listOf(
                observation("a", "00:00:00:00:00:01", -40, 2412),
                observation("b", "00:00:00:00:00:02", -65, 2412),
                observation("c", "00:00:00:00:00:03", -55, 2437),
                observation("five", "00:00:00:00:00:04", -50, 5180)
            ),
            band = WifiBand.Ghz2_4
        )

        assertEquals(3, overview.observedAccessPointCount)
        assertEquals(2, overview.mappedPrimaryChannelCount)
        assertEquals(1, overview.channels.first().channel)
        assertEquals(2, overview.channels.first().accessPointCount)
        assertEquals(-40, overview.channels.first().strongestRssiDbm)
    }

    @Test
    fun reportsPositiveGeometricOverlapPairs() {
        val overview = WifiChannelOccupancyAnalyzer.analyze(
            observations = listOf(
                observation("one", "00:00:00:00:00:01", -40, 2412),
                observation("three", "00:00:00:00:00:02", -60, 2422),
                observation("eleven", "00:00:00:00:00:03", -70, 2462)
            ),
            band = WifiBand.Ghz2_4
        )

        assertEquals(1, overview.overlappingPairCount)
        assertEquals(2.0 / 3.0, overview.averageOverlappingNeighborsPerAccessPoint, 0.0001)
        assertEquals(10, overview.overlappingPairs.single().overlapBandwidthMhz)
        assertFalse(overview.overlappingPairs.single().estimatedFromPartialGeometry)
    }

    @Test
    fun marksOverlapDerivedFromPartialGeometry() {
        val overview = WifiChannelOccupancyAnalyzer.analyze(
            observations = listOf(
                observation(
                    ssid = "wide-missing-center",
                    bssid = "00:00:00:00:00:01",
                    rssiDbm = -45,
                    frequencyMhz = 5180,
                    width = WifiChannelWidth.Mhz80
                ),
                observation(
                    ssid = "twenty",
                    bssid = "00:00:00:00:00:02",
                    rssiDbm = -55,
                    frequencyMhz = 5180
                )
            ),
            band = WifiBand.Ghz5
        )

        assertEquals(1, overview.overlappingPairCount)
        assertTrue(overview.overlappingPairs.single().estimatedFromPartialGeometry)
        assertEquals(1, overview.nonCompleteGeometryAccessPointCount)
    }

    @Test
    fun doesNotMixBandsIntoOverlapAnalysis() {
        val overview = WifiChannelOccupancyAnalyzer.analyze(
            observations = listOf(
                observation("two-four", "00:00:00:00:00:01", -40, 2412),
                observation("five", "00:00:00:00:00:02", -45, 5180)
            ),
            band = WifiBand.Ghz2_4
        )

        assertEquals(1, overview.observedAccessPointCount)
        assertEquals(0, overview.overlappingPairCount)
        assertEquals(0.0, overview.averageOverlappingNeighborsPerAccessPoint, 0.0)
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
