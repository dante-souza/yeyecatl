package io.github.dante_souza.yeyecatl.domain.wifi

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WifiPollingIntervalPolicyTest {
    @Test
    fun usesThirtySecondDefaultWithExplicitExperimentalOptions() {
        assertEquals(
            listOf(5_000L, 10_000L, 30_000L, 45_000L, 60_000L, 120_000L),
            WifiPollingIntervalPolicy.supportedIntervalsMillis
        )
        assertEquals(30_000L, WifiPollingIntervalPolicy.DEFAULT_INTERVAL_MILLIS)
        assertTrue(WifiPollingIntervalPolicy.isExperimental(5_000L))
        assertTrue(WifiPollingIntervalPolicy.isExperimental(10_000L))
        assertFalse(WifiPollingIntervalPolicy.isExperimental(30_000L))
    }

    @Test
    fun acceptsSupportedPersistedIntervals() {
        assertEquals(5_000L, WifiPollingIntervalPolicy.sanitize(5_000L))
        assertEquals(45_000L, WifiPollingIntervalPolicy.sanitize(45_000L))
        assertEquals(120_000L, WifiPollingIntervalPolicy.sanitize(120_000L))
    }

    @Test
    fun migratesUnsupportedLegacyIntervalsToStandardDefault() {
        assertEquals(30_000L, WifiPollingIntervalPolicy.sanitize(null))
        assertEquals(30_000L, WifiPollingIntervalPolicy.sanitize(1_000L))
        assertEquals(30_000L, WifiPollingIntervalPolicy.sanitize(2_000L))
        assertEquals(30_000L, WifiPollingIntervalPolicy.sanitize(7_000L))
    }
}
