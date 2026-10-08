package io.github.dante_souza.yeyecatl.domain.wifi

import org.junit.Assert.assertEquals
import org.junit.Test

class WifiConnectedSignalTest {
    @Test
    fun accumulatorKeepsSameBssidAndBoundsHistory() {
        var history = WifiConnectedSignalHistory()
        history = WifiConnectedSignalAccumulator.append(
            history,
            sample("00:00:00:00:00:01", -50, 1_000L),
            maxSamples = 2
        )
        history = WifiConnectedSignalAccumulator.append(
            history,
            sample("00:00:00:00:00:01", -51, 2_000L),
            maxSamples = 2
        )
        history = WifiConnectedSignalAccumulator.append(
            history,
            sample("00:00:00:00:00:01", -52, 3_000L),
            maxSamples = 2
        )

        assertEquals(listOf(2_000L, 3_000L), history.samples.map { it.observedAtMillis })
        assertEquals(listOf(-51, -52), history.samples.map { it.rssiDbm })
    }

    @Test
    fun accumulatorResetsWhenConnectedBssidChanges() {
        val first = WifiConnectedSignalAccumulator.append(
            WifiConnectedSignalHistory(),
            sample("00:00:00:00:00:01", -50, 1_000L)
        )
        val roamed = WifiConnectedSignalAccumulator.append(
            first,
            sample("00:00:00:00:00:02", -60, 2_000L)
        )

        assertEquals(1, roamed.samples.size)
        assertEquals("00:00:00:00:00:02", roamed.latest?.bssid)
        assertEquals(2_000L, roamed.latest?.observedAtMillis)
    }

    private fun sample(
        bssid: String,
        rssiDbm: Int,
        observedAtMillis: Long
    ): WifiConnectedSignalSample =
        WifiConnectedSignalSample(
            bssid = bssid,
            ssid = ObservedSsid("lab", null, false),
            rssiDbm = rssiDbm,
            frequencyMhz = 2412,
            observedAtMillis = observedAtMillis
        )
}
