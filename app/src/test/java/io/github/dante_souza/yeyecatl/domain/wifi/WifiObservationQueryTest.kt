package io.github.dante_souza.yeyecatl.domain.wifi

import org.junit.Assert.assertEquals
import org.junit.Test

class WifiObservationQueryTest {
    @Test
    fun filtersCurrentSnapshotByBandWithoutChangingInput() {
        val observations = listOf(
            observation("two-four", "00:00:00:00:00:01", -50, 2412),
            observation("five", "00:00:00:00:00:02", -60, 5180),
            observation("six", "00:00:00:00:00:03", -70, 5955)
        )

        val selected = WifiObservationQueryEngine.apply(
            observations = observations,
            query = WifiObservationQuery(
                band = WifiBand.Ghz5
            )
        )

        assertEquals(listOf("five"), selected.map { it.ssid.displayText })
        assertEquals(
            listOf("two-four", "five", "six"),
            observations.map { it.ssid.displayText }
        )
    }

    @Test
    fun textFilterMatchesSsidCaseInsensitively() {
        val observations = listOf(
            observation("Lab-WiFi", "00:00:00:00:00:01", -40, 2412),
            observation("guest", "00:00:00:00:00:02", -50, 2412)
        )

        val selected = WifiObservationQueryEngine.apply(
            observations = observations,
            query = WifiObservationQuery(text = "lab-wifi")
        )

        assertEquals(listOf("Lab-WiFi"), selected.map { it.ssid.displayText })
    }

    @Test
    fun textFilterAlsoMatchesBssid() {
        val observations = listOf(
            observation("one", "AA:BB:CC:00:00:01", -40, 2412),
            observation("two", "AA:BB:DD:00:00:02", -50, 2412)
        )

        val selected = WifiObservationQueryEngine.apply(
            observations = observations,
            query = WifiObservationQuery(text = "cc:00")
        )

        assertEquals(listOf("AA:BB:CC:00:00:01"), selected.map { it.bssid })
    }

    @Test
    fun strongestSortPlacesUnknownRssiLastAndUsesBssidAsTieBreaker() {
        val observations = listOf(
            observation("unknown", "00:00:00:00:00:04", null, 2412),
            observation("same", "00:00:00:00:00:02", -40, 2412),
            observation("same", "00:00:00:00:00:01", -40, 2412),
            observation("weak", "00:00:00:00:00:03", -80, 2412)
        )

        val selected = WifiObservationQueryEngine.apply(
            observations = observations,
            query = WifiObservationQuery(
                sort = WifiObservationSort.StrongestFirst
            )
        )

        assertEquals(
            listOf(
                "00:00:00:00:00:01",
                "00:00:00:00:00:02",
                "00:00:00:00:00:03",
                "00:00:00:00:00:04"
            ),
            selected.map { it.bssid }
        )
    }

    @Test
    fun weakestSortPlacesUnknownRssiLast() {
        val observations = listOf(
            observation("strong", "00:00:00:00:00:01", -35, 2412),
            observation("unknown", "00:00:00:00:00:02", null, 2412),
            observation("weak", "00:00:00:00:00:03", -88, 2412)
        )

        val selected = WifiObservationQueryEngine.apply(
            observations = observations,
            query = WifiObservationQuery(
                sort = WifiObservationSort.WeakestFirst
            )
        )

        assertEquals(listOf(-88, -35, null), selected.map { it.rssiDbm })
    }

    @Test
    fun ssidSortIsCaseInsensitiveAndPlacesUnavailableNamesLast() {
        val observations = listOf(
            observation(null, "00:00:00:00:00:04", -50, 2412),
            observation("zulu", "00:00:00:00:00:03", -50, 2412),
            observation("Alpha", "00:00:00:00:00:02", -50, 2412),
            observation("alpha", "00:00:00:00:00:01", -50, 2412)
        )

        val selected = WifiObservationQueryEngine.apply(
            observations = observations,
            query = WifiObservationQuery(
                sort = WifiObservationSort.SsidAscending
            )
        )

        assertEquals(
            listOf(
                "00:00:00:00:00:01",
                "00:00:00:00:00:02",
                "00:00:00:00:00:03",
                "00:00:00:00:00:04"
            ),
            selected.map { it.bssid }
        )
    }

    @Test
    fun channelSortUsesInterpretedPrimaryChannelAndPlacesUnknownLast() {
        val observations = listOf(
            observation("unknown", "00:00:00:00:00:04", -50, null),
            observation("ch11", "00:00:00:00:00:03", -50, 2462),
            observation("ch1", "00:00:00:00:00:01", -50, 2412),
            observation("ch6", "00:00:00:00:00:02", -50, 2437)
        )

        val selected = WifiObservationQueryEngine.apply(
            observations = observations,
            query = WifiObservationQuery(
                sort = WifiObservationSort.ChannelAscending
            )
        )

        assertEquals(
            listOf("ch1", "ch6", "ch11", "unknown"),
            selected.map { it.ssid.displayText }
        )
    }

    private fun observation(
        ssid: String?,
        bssid: String,
        rssiDbm: Int?,
        frequencyMhz: Int?
    ): WifiScanObservation =
        WifiScanObservation(
            ssid = ObservedSsid(
                displayText = ssid,
                rawBytes = null,
                isHidden = ssid == null
            ),
            bssid = bssid,
            rssiDbm = rssiDbm,
            frequencyMhz = frequencyMhz,
            capabilities = "[ESS]",
            platformTimestampMicros = 1L
        )
}
