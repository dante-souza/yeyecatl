package io.github.dante_souza.yeyecatl.domain.wifi

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class WifiObservationDetailTest {
    @Test
    fun resolvesSelectedBssidWithRfSpectrumAndRetainedHistory() {
        val selected = observation(
            ssid = "mesh",
            bssid = "00:11:22:33:44:55",
            rssiDbm = -48,
            frequencyMhz = 5180,
            channelWidth = WifiChannelWidth.Mhz80,
            centerFrequency0Mhz = 5210,
            wifiStandard = WifiStandard.Ieee80211ac
        )
        val other = observation(
            ssid = "mesh",
            bssid = "00:11:22:33:44:66",
            rssiDbm = -60,
            frequencyMhz = 5220
        )
        val history = WifiTemporalObservationHistory(
            samplesByBssid = mapOf(
                "00:11:22:33:44:55" to listOf(
                    sample(selected, -52, 1_000L),
                    sample(selected, -48, 3_500L)
                ),
                "00:11:22:33:44:66" to listOf(
                    sample(other, -60, 2_000L)
                )
            )
        )

        val detail = WifiObservationDetailResolver.resolve(
            selection = WifiObservationSelection("00:11:22:33:44:55"),
            snapshot = snapshot(selected, other),
            history = history
        )

        assertNotNull(detail)
        requireNotNull(detail)
        assertEquals("00:11:22:33:44:55", detail.selection.bssid)
        assertEquals(selected, detail.latestObservation)
        assertEquals(WifiBand.Ghz5, detail.rf.band)
        assertEquals(36, detail.rf.primaryChannel)
        assertEquals(5180, detail.rf.primaryFrequencyMhz)
        assertEquals(WifiChannelWidth.Mhz80, detail.rf.channelWidth)
        assertEquals(5210, detail.rf.centerFrequency0Mhz)
        assertEquals(WifiStandard.Ieee80211ac, detail.rf.wifiStandard)
        assertEquals(WifiSpectrumCompleteness.Complete, detail.spectrum.completeness)
        assertEquals(1, detail.spectrum.segments.size)
        assertEquals(5170, detail.spectrum.segments.single().lowerFrequencyMhz)
        assertEquals(5250, detail.spectrum.segments.single().upperFrequencyMhz)
        assertEquals(2, detail.retainedSignalSampleCount)
        assertEquals(1_000L, detail.firstRetainedSampleAtMillis)
        assertEquals(3_500L, detail.lastRetainedSampleAtMillis)
        assertEquals(2_500L, detail.retainedHistorySpanMillis)
        assertEquals(5_000L, detail.latestSnapshotReceivedAtMillis)
    }

    @Test
    fun sameSsidDoesNotOverrideBssidIdentity() {
        val first = observation(
            ssid = "mesh",
            bssid = "00:00:00:00:00:01",
            rssiDbm = -40,
            frequencyMhz = 2412
        )
        val second = observation(
            ssid = "mesh",
            bssid = "00:00:00:00:00:02",
            rssiDbm = -70,
            frequencyMhz = 2462
        )

        val detail = WifiObservationDetailResolver.resolve(
            selection = WifiObservationSelection("00:00:00:00:00:02"),
            snapshot = snapshot(first, second),
            history = WifiTemporalObservationHistory()
        )

        assertEquals(second, detail?.latestObservation)
        assertEquals(11, detail?.rf?.primaryChannel)
    }

    @Test
    fun returnsNullWhenSelectedBssidIsNotInLatestSnapshot() {
        val detail = WifiObservationDetailResolver.resolve(
            selection = WifiObservationSelection("00:00:00:00:00:99"),
            snapshot = snapshot(
                observation(
                    ssid = "other",
                    bssid = "00:00:00:00:00:01",
                    rssiDbm = -45,
                    frequencyMhz = 2412
                )
            ),
            history = WifiTemporalObservationHistory()
        )

        assertNull(detail)
    }

    @Test
    fun keepsPreviousDetailWhenSelectedBssidIsMissingFromLatestSnapshot() {
        val selected = observation(
            ssid = "mesh",
            bssid = "00:00:00:00:00:01",
            rssiDbm = -42,
            frequencyMhz = 2412
        )
        val firstSnapshot = WifiScanSnapshot(
            observations = listOf(selected),
            freshness = WifiScanFreshness.Fresh,
            source = WifiScanResultSource.ApplicationRequest,
            resultsUpdated = true,
            receivedAtMillis = 5_000L
        )
        val firstHistory = WifiTemporalObservationHistory(
            samplesByBssid = mapOf(
                "00:00:00:00:00:01" to listOf(
                    sample(selected, -42, 5_000L)
                )
            )
        )
        val initial = WifiObservationDetailResolver.resolve(
            selection = WifiObservationSelection("00:00:00:00:00:01"),
            snapshot = firstSnapshot,
            history = firstHistory
        )
        requireNotNull(initial)

        val missingSnapshot = WifiScanSnapshot(
            observations = listOf(
                observation(
                    ssid = "other",
                    bssid = "00:00:00:00:00:02",
                    rssiDbm = -60,
                    frequencyMhz = 2437
                )
            ),
            freshness = WifiScanFreshness.Fresh,
            source = WifiScanResultSource.ApplicationRequest,
            resultsUpdated = true,
            receivedAtMillis = 35_000L
        )
        val retained = WifiObservationDetailResolver.resolve(
            selection = WifiObservationSelection("00:00:00:00:00:01"),
            snapshot = missingSnapshot,
            history = firstHistory,
            previousDetail = initial
        )

        requireNotNull(retained)
        assertEquals(selected, retained.latestObservation)
        assertEquals(false, retained.observedInLatestSnapshot)
        assertEquals(5_000L, retained.lastSeenAtMillis)
        assertEquals(35_000L, retained.latestSnapshotReceivedAtMillis)
    }

    @Test
    fun selectedBssidReturningToScanRefreshesStickyDetail() {
        val selected = observation(
            ssid = "mesh",
            bssid = "00:00:00:00:00:01",
            rssiDbm = -50,
            frequencyMhz = 2412
        )
        val initial = WifiObservationDetailResolver.resolve(
            selection = WifiObservationSelection("00:00:00:00:00:01"),
            snapshot = WifiScanSnapshot(
                observations = listOf(selected),
                freshness = WifiScanFreshness.Fresh,
                source = WifiScanResultSource.ApplicationRequest,
                resultsUpdated = true,
                receivedAtMillis = 5_000L
            ),
            history = WifiTemporalObservationHistory()
        )
        requireNotNull(initial)

        val missing = WifiObservationDetailResolver.resolve(
            selection = initial.selection,
            snapshot = WifiScanSnapshot(
                observations = emptyList(),
                freshness = WifiScanFreshness.Fresh,
                source = WifiScanResultSource.ApplicationRequest,
                resultsUpdated = true,
                receivedAtMillis = 35_000L
            ),
            history = WifiTemporalObservationHistory(),
            previousDetail = initial
        )
        requireNotNull(missing)

        val returnedObservation = selected.copy(rssiDbm = -44)
        val returned = WifiObservationDetailResolver.resolve(
            selection = initial.selection,
            snapshot = WifiScanSnapshot(
                observations = listOf(returnedObservation),
                freshness = WifiScanFreshness.Fresh,
                source = WifiScanResultSource.ApplicationRequest,
                resultsUpdated = true,
                receivedAtMillis = 65_000L
            ),
            history = WifiTemporalObservationHistory(),
            previousDetail = missing
        )

        requireNotNull(returned)
        assertEquals(true, returned.observedInLatestSnapshot)
        assertEquals(-44, returned.latestObservation.rssiDbm)
        assertEquals(65_000L, returned.lastSeenAtMillis)
    }

    @Test
    fun blankSelectionIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            WifiObservationSelection("   ")
        }
    }

    private fun snapshot(vararg observations: WifiScanObservation): WifiScanSnapshot =
        WifiScanSnapshot(
            observations = observations.toList(),
            freshness = WifiScanFreshness.Fresh,
            source = WifiScanResultSource.ApplicationRequest,
            resultsUpdated = true,
            receivedAtMillis = 5_000L
        )

    private fun sample(
        observation: WifiScanObservation,
        rssiDbm: Int,
        observedAtMillis: Long
    ): WifiSignalSample =
        WifiSignalSample(
            bssid = requireNotNull(observation.bssid),
            ssid = observation.ssid,
            rssiDbm = rssiDbm,
            frequencyMhz = observation.frequencyMhz,
            observedAtMillis = observedAtMillis
        )

    private fun observation(
        ssid: String,
        bssid: String,
        rssiDbm: Int,
        frequencyMhz: Int,
        channelWidth: WifiChannelWidth = WifiChannelWidth.Mhz20,
        centerFrequency0Mhz: Int? = null,
        wifiStandard: WifiStandard = WifiStandard.Unknown
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
            channelWidth = channelWidth,
            centerFrequency0Mhz = centerFrequency0Mhz,
            wifiStandard = wifiStandard,
            capabilities = "[ESS]",
            platformTimestampMicros = 1L
        )
}
