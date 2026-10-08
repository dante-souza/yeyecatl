package io.github.dante_souza.yeyecatl.domain.wifi

/**
 * Intervals are *request cadences*, not guarantees of fresh Android scan results.
 *
 * Standard mode defaults to 30 seconds to avoid sustained rapid requests under
 * Android foreground Wi-Fi scan throttling. Faster periods are explicit lab
 * experiments, useful when the test device has throttling disabled.
 */
object WifiPollingIntervalPolicy {
    const val DEFAULT_INTERVAL_MILLIS: Long = 30_000L
    const val EXPERIMENTAL_INTERVAL_MILLIS: Long = 5_000L

    val supportedIntervalsMillis: List<Long> = listOf(
        5_000L,
        10_000L,
        30_000L,
        45_000L,
        60_000L,
        120_000L
    )

    fun isExperimental(intervalMillis: Long): Boolean = intervalMillis < DEFAULT_INTERVAL_MILLIS

    fun sanitize(intervalMillis: Long?): Long =
        intervalMillis
            ?.takeIf(supportedIntervalsMillis::contains)
            ?: DEFAULT_INTERVAL_MILLIS
}
