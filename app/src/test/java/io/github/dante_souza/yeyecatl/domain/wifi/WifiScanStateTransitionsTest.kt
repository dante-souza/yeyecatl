package io.github.dante_souza.yeyecatl.domain.wifi

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class WifiScanStateTransitionsTest {
    @Test
    fun readyStateCanMoveToScanRequested() {
        val state = WifiScanStateTransitions.request(WifiScanState.Idle)

        assertTrue(state is WifiScanState.ScanRequested)
        assertEquals(null, state.latestSnapshot)
    }

    @Test
    fun scanRequestedCanMoveToFreshResults() {
        val requested = WifiScanStateTransitions.request(WifiScanState.Idle)
        val state = WifiScanStateTransitions.resultsAvailable(
            current = requested,
            observations = listOf(observation("one")),
            freshness = WifiScanFreshness.Fresh,
            source = WifiScanResultSource.ApplicationRequest,
            resultsUpdated = true,
            receivedAtMillis = 10L
        )

        val results = state as WifiScanState.Results
        assertEquals(WifiScanFreshness.Fresh, results.snapshot.freshness)
        assertEquals(1, results.snapshot.observations.size)
        assertEquals(true, results.snapshot.resultsUpdated)
    }

    @Test
    fun scanRequestedCanMoveToRequestRejected() {
        val requested = WifiScanStateTransitions.request(WifiScanState.Idle)
        val rejected = WifiScanStateTransitions.requestRejected(requested)

        assertTrue(rejected is WifiScanState.RequestRejected)
    }

    @Test
    fun requestRejectedPreservesExistingResults() {
        val existing = existingResults()
        val rejected = WifiScanStateTransitions.requestRejected(existing)

        assertSame(existing.snapshot, rejected.latestSnapshot)
    }

    @Test
    fun unsuccessfulEmptyUpdatePreservesCachedResults() {
        val existing = existingResults()
        val state = WifiScanStateTransitions.resultsAvailable(
            current = existing,
            observations = emptyList(),
            freshness = WifiScanFreshness.Cached,
            source = WifiScanResultSource.ApplicationRequest,
            resultsUpdated = false,
            receivedAtMillis = 20L
        )

        val results = state as WifiScanState.Results
        assertEquals(1, results.snapshot.observations.size)
        assertEquals(WifiScanFreshness.Cached, results.snapshot.freshness)
        assertEquals(false, results.snapshot.resultsUpdated)
    }

    @Test
    fun freshEmptyResultsAreRepresentedIntentionally() {
        val state = WifiScanStateTransitions.resultsAvailable(
            current = WifiScanState.Idle,
            observations = emptyList(),
            freshness = WifiScanFreshness.Fresh,
            source = WifiScanResultSource.ApplicationRequest,
            resultsUpdated = true,
            receivedAtMillis = 30L
        )

        val results = state as WifiScanState.Results
        assertEquals(0, results.snapshot.observations.size)
        assertEquals(WifiScanFreshness.Fresh, results.snapshot.freshness)
    }

    @Test
    fun readinessBlocksPreserveExistingResults() {
        val existing = existingResults()
        val blocked = WifiScanStateTransitions.blocked(
            existing,
            WifiScanBlockReason.PermissionUnavailable
        )

        assertSame(existing.snapshot, blocked.latestSnapshot)
    }

    private fun existingResults(): WifiScanState.Results =
        WifiScanState.Results(
            WifiScanSnapshot(
                observations = listOf(observation("existing")),
                freshness = WifiScanFreshness.Fresh,
                source = WifiScanResultSource.ApplicationRequest,
                resultsUpdated = true,
                receivedAtMillis = 1L
            )
        )

    private fun observation(ssid: String): WifiScanObservation =
        WifiScanObservation(
            ssid = ObservedSsid(
                displayText = ssid,
                rawBytes = null,
                isHidden = false
            ),
            bssid = "00:11:22:33:44:55",
            rssiDbm = -50,
            frequencyMhz = 2412,
            capabilities = "[ESS]",
            platformTimestampMicros = 100L
        )
}
