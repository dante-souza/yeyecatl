package io.github.dante_souza.yeyecatl.domain.wifi

import org.junit.Assert.assertEquals
import org.junit.Test

class WifiPollingIntervalPolicyTest {
    @Test
    fun exposesAnalyzerStyleIntervalsWithFiveSecondDefault() {
        assertEquals(
            listOf(1_000L, 2_000L, 5_000L, 10_000L, 30_000L),
            WifiPollingIntervalPolicy.supportedIntervalsMillis
        )
        assertEquals(5_000L, WifiPollingIntervalPolicy.DEFAULT_INTERVAL_MILLIS)
    }

    @Test
    fun acceptsSupportedPersistedInterval() {
        assertEquals(10_000L, WifiPollingIntervalPolicy.sanitize(10_000L))
    }

    @Test
    fun fallsBackForMissingOrUnknownPersistedInterval() {
        assertEquals(5_000L, WifiPollingIntervalPolicy.sanitize(null))
        assertEquals(5_000L, WifiPollingIntervalPolicy.sanitize(7_000L))
    }
}
