package io.github.dante_souza.yeyecatl.domain.wifi

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class WifiTemporalObservationTest {
    @Test
    fun recordsFreshRssiSamplesUsingSnapshotReceiptTime() {
        val snapshot = snapshot(
            receivedAtMillis = 1_000L,
            observations = listOf(
                observation(
                    ssid = "lab",
                    bssid = "00:00:00:00:00:01",
                    rssiDbm = -47,
                    frequencyMhz = 2412
                )
            )
        )

        val history = WifiTemporalObservationAccumulator.append(
            current = WifiTemporalObservationHistory(),
            snapshot = snapshot
        )

        assertEquals(1, history.freshSnapshotCount)
        assertEquals(
            listOf(
                WifiSignalSample(
                    bssid = "00:00:00:00:00:01",
                    ssid = snapshot.observations.single().ssid,
                    rssiDbm = -47,
                    frequencyMhz = 2412,
                    observedAtMillis = 1_000L
                )
            ),
            history.samplesFor("00:00:00:00:00:01")
        )
    }

    @Test
    fun groupsRepeatedFreshObservationsByBssid() {
        val first = snapshot(
            receivedAtMillis = 1_000L,
            observations = listOf(observation("lab", "00:00:00:00:00:01", -55))
        )
        val second = snapshot(
            receivedAtMillis = 2_000L,
            observations = listOf(observation("lab", "00:00:00:00:00:01", -48))
        )

        val afterFirst = WifiTemporalObservationAccumulator.append(
            current = WifiTemporalObservationHistory(),
            snapshot = first
        )
        val afterSecond = WifiTemporalObservationAccumulator.append(
            current = afterFirst,
            snapshot = second
        )

        assertEquals(2, afterSecond.freshSnapshotCount)
        assertEquals(
            listOf(-55, -48),
            afterSecond.samplesFor("00:00:00:00:00:01").map { it.rssiDbm }
        )
        assertEquals(
            listOf(1_000L, 2_000L),
            afterSecond.samplesFor("00:00:00:00:00:01").map { it.observedAtMillis }
        )
    }

    @Test
    fun keepsSameSsidAcrossDistinctBssidsSeparate() {
        val snapshot = snapshot(
            observations = listOf(
                observation("mesh", "00:00:00:00:00:01", -41),
                observation("mesh", "00:00:00:00:00:02", -63)
            )
        )

        val history = WifiTemporalObservationAccumulator.append(
            current = WifiTemporalObservationHistory(),
            snapshot = snapshot
        )

        assertEquals(
            setOf("00:00:00:00:00:01", "00:00:00:00:00:02"),
            history.samplesByBssid.keys
        )
        assertEquals(-41, history.samplesFor("00:00:00:00:00:01").single().rssiDbm)
        assertEquals(-63, history.samplesFor("00:00:00:00:00:02").single().rssiDbm)
    }

    @Test
    fun ignoresObservationsWithoutStableBssidOrRssi() {
        val snapshot = snapshot(
            observations = listOf(
                observation("no-bssid", null, -42),
                observation("no-rssi", "00:00:00:00:00:02", null),
                observation("usable", "00:00:00:00:00:03", -60)
            )
        )

        val history = WifiTemporalObservationAccumulator.append(
            current = WifiTemporalObservationHistory(),
            snapshot = snapshot
        )

        assertEquals(setOf("00:00:00:00:00:03"), history.samplesByBssid.keys)
    }

    @Test
    fun keepsOnlyMostRecentSamplesPerBssidWithinConfiguredLimit() {
        var history = WifiTemporalObservationHistory()

        (1L..4L).forEach { second ->
            history = WifiTemporalObservationAccumulator.append(
                current = history,
                snapshot = snapshot(
                    receivedAtMillis = second * 1_000L,
                    observations = listOf(
                        observation(
                            ssid = "lab",
                            bssid = "00:00:00:00:00:01",
                            rssiDbm = -40 - second.toInt()
                        )
                    )
                ),
                maxSamplesPerBssid = 3
            )
        }

        assertEquals(
            listOf(2_000L, 3_000L, 4_000L),
            history.samplesFor("00:00:00:00:00:01").map { it.observedAtMillis }
        )
    }

    @Test
    fun cachedSnapshotsDoNotCreateTemporalSamples() {
        val current = WifiTemporalObservationHistory(
            samplesByBssid = mapOf(
                "00:00:00:00:00:01" to listOf(
                    WifiSignalSample(
                        bssid = "00:00:00:00:00:01",
                        ssid = ssid("lab"),
                        rssiDbm = -50,
                        frequencyMhz = 2412,
                        observedAtMillis = 1_000L
                    )
                )
            )
        )
        val cached = snapshot(
            freshness = WifiScanFreshness.Cached,
            receivedAtMillis = 2_000L,
            observations = listOf(observation("lab", "00:00:00:00:00:01", -50))
        )

        val result = WifiTemporalObservationAccumulator.append(current, cached)

        assertSame(current, result)
        assertEquals(current.freshSnapshotCount, result.freshSnapshotCount)
    }

    @Test
    fun unknownFreshnessDoesNotCreateTemporalSamples() {
        val current = WifiTemporalObservationHistory()
        val unknown = snapshot(
            freshness = WifiScanFreshness.Unknown,
            observations = listOf(observation("lab", "00:00:00:00:00:01", -50))
        )

        val result = WifiTemporalObservationAccumulator.append(current, unknown)

        assertSame(current, result)
        assertEquals(current.freshSnapshotCount, result.freshSnapshotCount)
    }

    private fun snapshot(
        freshness: WifiScanFreshness = WifiScanFreshness.Fresh,
        receivedAtMillis: Long = 1_000L,
        observations: List<WifiScanObservation>
    ): WifiScanSnapshot =
        WifiScanSnapshot(
            observations = observations,
            freshness = freshness,
            source = WifiScanResultSource.ApplicationRequest,
            resultsUpdated = freshness == WifiScanFreshness.Fresh,
            receivedAtMillis = receivedAtMillis
        )

    private fun observation(
        ssid: String,
        bssid: String?,
        rssiDbm: Int?,
        frequencyMhz: Int? = 2412
    ): WifiScanObservation =
        WifiScanObservation(
            ssid = ssid(ssid),
            bssid = bssid,
            rssiDbm = rssiDbm,
            frequencyMhz = frequencyMhz,
            capabilities = "[ESS]",
            platformTimestampMicros = 1L
        )

    private fun ssid(value: String): ObservedSsid =
        ObservedSsid(
            displayText = value,
            rawBytes = null,
            isHidden = false
        )
}
