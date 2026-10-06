package io.github.dante_souza.yeyecatl.domain.wifi

import org.junit.Assert.assertEquals
import org.junit.Test

class WifiSignalRankingTest {
    @Test
    fun ranksStrongestAndWeakestByRssi() {
        val observations = listOf(
            observation("middle", "00:00:00:00:00:03", -65),
            observation("strongest", "00:00:00:00:00:01", -38),
            observation("weakest", "00:00:00:00:00:05", -91),
            observation("strong", "00:00:00:00:00:02", -51),
            observation("weak", "00:00:00:00:00:04", -78)
        )

        val ranking = WifiSignalRanker.rank(observations, limit = 3)

        assertEquals(listOf(-38, -51, -65), ranking.strongest.map { it.rssiDbm })
        assertEquals(listOf(-91, -78, -65), ranking.weakest.map { it.rssiDbm })
    }

    @Test
    fun ignoresObservationsWithoutRssi() {
        val observations = listOf(
            observation("known", "00:00:00:00:00:01", -42),
            observation("unknown", "00:00:00:00:00:02", null)
        )

        val ranking = WifiSignalRanker.rank(observations)

        assertEquals(listOf("00:00:00:00:00:01"), ranking.strongest.map { it.bssid })
        assertEquals(listOf("00:00:00:00:00:01"), ranking.weakest.map { it.bssid })
    }

    @Test
    fun preservesDistinctBssidsAdvertisingTheSameSsid() {
        val observations = listOf(
            observation("mesh", "00:00:00:00:00:01", -40),
            observation("mesh", "00:00:00:00:00:02", -55),
            observation("other", "00:00:00:00:00:03", -70)
        )

        val ranking = WifiSignalRanker.rank(observations)

        assertEquals(
            listOf("00:00:00:00:00:01", "00:00:00:00:00:02", "00:00:00:00:00:03"),
            ranking.strongest.map { it.bssid }
        )
    }

    @Test
    fun defaultLimitShowsAtMostFiveObservationsPerSide() {
        val observations = (1..8).map { index ->
            observation(
                ssid = "network-$index",
                bssid = "00:00:00:00:00:${index.toString().padStart(2, '0')}",
                rssiDbm = -30 - index
            )
        }

        val ranking = WifiSignalRanker.rank(observations)

        assertEquals(5, ranking.strongest.size)
        assertEquals(5, ranking.weakest.size)
    }

    private fun observation(
        ssid: String,
        bssid: String,
        rssiDbm: Int?
    ): WifiScanObservation =
        WifiScanObservation(
            ssid = ObservedSsid(
                displayText = ssid,
                rawBytes = null,
                isHidden = false
            ),
            bssid = bssid,
            rssiDbm = rssiDbm,
            frequencyMhz = 2412,
            capabilities = "[ESS]",
            platformTimestampMicros = 1L
        )
}
