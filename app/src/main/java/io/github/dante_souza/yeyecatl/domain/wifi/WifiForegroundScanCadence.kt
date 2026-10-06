package io.github.dante_souza.yeyecatl.domain.wifi

fun interface WifiScanCadenceTask {
    fun cancel()
}

fun interface WifiScanCadenceScheduler {
    fun schedule(
        delayMillis: Long,
        action: () -> Unit
    ): WifiScanCadenceTask
}

class WifiForegroundScanCadence(
    private val requestScan: () -> Unit,
    private val scheduler: WifiScanCadenceScheduler,
    val intervalMillis: Long = DEFAULT_INTERVAL_MILLIS
) {
    private var foreground = false
    private var scheduledTask: WifiScanCadenceTask? = null

    var isEnabled: Boolean = false
        private set

    init {
        require(intervalMillis > 0L) { "intervalMillis must be greater than zero" }
    }

    fun setEnabled(enabled: Boolean) {
        if (isEnabled == enabled) {
            return
        }

        isEnabled = enabled
        if (enabled) {
            startIfEligible()
        } else {
            cancelScheduledTask()
        }
    }

    fun enterForeground() {
        if (foreground) {
            return
        }

        foreground = true
        startIfEligible()
    }

    fun leaveForeground() {
        if (!foreground) {
            return
        }

        foreground = false
        cancelScheduledTask()
    }

    private fun startIfEligible() {
        if (!isEnabled || !foreground || scheduledTask != null) {
            return
        }

        requestScan()
        if (isEnabled && foreground) {
            scheduleNext()
        }
    }

    private fun scheduleNext() {
        scheduledTask = scheduler.schedule(intervalMillis) {
            scheduledTask = null
            if (isEnabled && foreground) {
                requestScan()
                if (isEnabled && foreground) {
                    scheduleNext()
                }
            }
        }
    }

    private fun cancelScheduledTask() {
        scheduledTask?.cancel()
        scheduledTask = null
    }

    companion object {
        const val DEFAULT_INTERVAL_MILLIS: Long = 30_000L
    }
}
