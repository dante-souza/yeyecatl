package io.github.dante_souza.yeyecatl.ui.history

import io.github.dante_souza.yeyecatl.domain.wifi.ObservedSsid
import io.github.dante_souza.yeyecatl.domain.wifi.WifiConnectedSignalSample
import org.junit.Assert.assertEquals
import org.junit.Test

class WifiConnectedSignalProjectionTest {
    @Test
    fun fixedRangeCoversVeryStrongAndWeakHouseholdSignals() {
        assertEquals(-100, WifiConnectedSignalProjection.fixedRange.minRssiDbm)
        assertEquals(-20, WifiConnectedSignalProjection.fixedRange.maxRssiDbm)
    }

    @Test
    fun autoRangeQuantizesRecentSignalAndKeepsThirtyDbMinimumSpan() {
        val range = WifiConnectedSignalProjection.autoTargetRange(
            samples = listOf(
                sample(-67, 50_000L),
                sample(-61, 55_000L)
            ),
            nowMillis = 60_000L
        )

        assertEquals(-80, range.minRssiDbm)
        assertEquals(-50, range.maxRssiDbm)
    }

    @Test
    fun autoRangeHandlesVeryStrongSignalWithoutClipping() {
        val range = WifiConnectedSignalProjection.autoTargetRange(
            samples = listOf(
                sample(-23, 50_000L),
                sample(-21, 55_000L)
            ),
            nowMillis = 60_000L
        )

        assertEquals(-50, range.minRssiDbm)
        assertEquals(-20, range.maxRssiDbm)
    }

    @Test
    fun autoRangeIgnoresOldSamplesOutsideRecentWindow() {
        val range = WifiConnectedSignalProjection.autoTargetRange(
            samples = listOf(
                sample(-90, 1_000L),
                sample(-55, 119_000L)
            ),
            nowMillis = 120_000L
        )

        assertEquals(-70, range.minRssiDbm)
        assertEquals(-40, range.maxRssiDbm)
    }

    @Test
    fun hysteresisExpandsImmediatelyButRequiresTenDbMarginToShrink() {
        val current = WifiConnectedRssiRange(-80, -30)

        assertEquals(
            WifiConnectedRssiRange(-90, -30),
            WifiConnectedSignalProjection.stabilizeAutoRange(
                current = current,
                target = WifiConnectedRssiRange(-90, -40)
            )
        )
        assertEquals(
            current,
            WifiConnectedSignalProjection.stabilizeAutoRange(
                current = current,
                target = WifiConnectedRssiRange(-75, -30)
            )
        )
        assertEquals(
            WifiConnectedRssiRange(-70, -30),
            WifiConnectedSignalProjection.stabilizeAutoRange(
                current = current,
                target = WifiConnectedRssiRange(-70, -30)
            )
        )
        assertEquals(
            WifiConnectedRssiRange(-70, -40),
            WifiConnectedSignalProjection.stabilizeAutoRange(
                current = current,
                target = WifiConnectedRssiRange(-70, -40)
            )
        )
    }

    @Test
    fun sessionChangeBreaksLineEvenWhenSamplesAreCloseInTime() {
        val segments = WifiConnectedSignalProjection.sessionSegments(
            samples = listOf(
                sample(-50, 1_000L, sessionId = 1L),
                sample(-51, 2_000L, sessionId = 1L),
                sample(-52, 2_500L, sessionId = 2L),
                sample(-53, 3_000L, sessionId = 2L)
            ),
            maxGapMillis = 3_000L
        )

        assertEquals(2, segments.size)
        assertEquals(listOf(1L, 1L), segments[0].map { it.connectionSessionId })
        assertEquals(listOf(2L, 2L), segments[1].map { it.connectionSessionId })
    }

    private fun sample(
        rssiDbm: Int,
        observedAtMillis: Long,
        sessionId: Long = 1L
    ): WifiConnectedSignalSample =
        WifiConnectedSignalSample(
            bssid = "00:00:00:00:00:01",
            ssid = ObservedSsid("lab", null, false),
            rssiDbm = rssiDbm,
            frequencyMhz = 2412,
            observedAtMillis = observedAtMillis,
            connectionSessionId = sessionId
        )
}
