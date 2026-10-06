package io.github.dante_souza.yeyecatl.domain.wifi

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WifiForegroundScanCadenceTest {
    @Test
    fun enablingCadenceInForegroundRequestsImmediatelyAndSchedulesNextScan() {
        val scheduler = FakeScheduler()
        var requests = 0
        val cadence = WifiForegroundScanCadence(
            requestScan = { requests += 1 },
            scheduler = scheduler
        )

        cadence.enterForeground()
        cadence.setEnabled(true)

        assertTrue(cadence.isEnabled)
        assertEquals(1, requests)
        assertEquals(WifiForegroundScanCadence.DEFAULT_INTERVAL_MILLIS, scheduler.pendingDelayMillis)
    }

    @Test
    fun scheduledTickRequestsAgainAndReschedules() {
        val scheduler = FakeScheduler()
        var requests = 0
        val cadence = WifiForegroundScanCadence(
            requestScan = { requests += 1 },
            scheduler = scheduler
        )

        cadence.enterForeground()
        cadence.setEnabled(true)
        scheduler.runPending()

        assertEquals(2, requests)
        assertEquals(WifiForegroundScanCadence.DEFAULT_INTERVAL_MILLIS, scheduler.pendingDelayMillis)
    }

    @Test
    fun enablingCadenceWhileBackgroundedWaitsForForeground() {
        val scheduler = FakeScheduler()
        var requests = 0
        val cadence = WifiForegroundScanCadence(
            requestScan = { requests += 1 },
            scheduler = scheduler
        )

        cadence.setEnabled(true)

        assertEquals(0, requests)
        assertEquals(null, scheduler.pendingDelayMillis)

        cadence.enterForeground()

        assertEquals(1, requests)
        assertEquals(WifiForegroundScanCadence.DEFAULT_INTERVAL_MILLIS, scheduler.pendingDelayMillis)
    }

    @Test
    fun leavingForegroundCancelsPendingScanButPreservesEnabledPreference() {
        val scheduler = FakeScheduler()
        var requests = 0
        val cadence = WifiForegroundScanCadence(
            requestScan = { requests += 1 },
            scheduler = scheduler
        )

        cadence.enterForeground()
        cadence.setEnabled(true)
        cadence.leaveForeground()
        scheduler.runPending()

        assertTrue(cadence.isEnabled)
        assertEquals(1, requests)
        assertEquals(null, scheduler.pendingDelayMillis)
    }

    @Test
    fun reenteringForegroundResumesEnabledCadenceWithImmediateScan() {
        val scheduler = FakeScheduler()
        var requests = 0
        val cadence = WifiForegroundScanCadence(
            requestScan = { requests += 1 },
            scheduler = scheduler
        )

        cadence.enterForeground()
        cadence.setEnabled(true)
        cadence.leaveForeground()
        cadence.enterForeground()

        assertEquals(2, requests)
        assertEquals(WifiForegroundScanCadence.DEFAULT_INTERVAL_MILLIS, scheduler.pendingDelayMillis)
    }

    @Test
    fun disablingCadenceCancelsPendingScan() {
        val scheduler = FakeScheduler()
        var requests = 0
        val cadence = WifiForegroundScanCadence(
            requestScan = { requests += 1 },
            scheduler = scheduler
        )

        cadence.enterForeground()
        cadence.setEnabled(true)
        cadence.setEnabled(false)
        scheduler.runPending()

        assertFalse(cadence.isEnabled)
        assertEquals(1, requests)
        assertEquals(null, scheduler.pendingDelayMillis)
    }

    @Test(expected = IllegalArgumentException::class)
    fun cadenceRejectsNonPositiveIntervals() {
        WifiForegroundScanCadence(
            requestScan = {},
            scheduler = FakeScheduler(),
            intervalMillis = 0L
        )
    }

    private class FakeScheduler : WifiScanCadenceScheduler {
        private var pending: Pending? = null

        val pendingDelayMillis: Long?
            get() = pending
                ?.takeUnless { it.cancelled }
                ?.delayMillis

        override fun schedule(
            delayMillis: Long,
            action: () -> Unit
        ): WifiScanCadenceTask {
            val next = Pending(delayMillis = delayMillis, action = action)
            pending = next
            return WifiScanCadenceTask {
                next.cancelled = true
                if (pending === next) {
                    pending = null
                }
            }
        }

        fun runPending() {
            val next = pending ?: return
            pending = null
            if (!next.cancelled) {
                next.action()
            }
        }

        private data class Pending(
            val delayMillis: Long,
            val action: () -> Unit,
            var cancelled: Boolean = false
        )
    }
}
