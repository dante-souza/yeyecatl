package io.github.dante_souza.yeyecatl.domain.wifi

object WifiPollingIntervalPolicy {
    const val DEFAULT_INTERVAL_MILLIS: Long = 5_000L

    val supportedIntervalsMillis: List<Long> = listOf(
        1_000L,
        2_000L,
        5_000L,
        10_000L,
        30_000L
    )

    fun sanitize(intervalMillis: Long?): Long =
        intervalMillis
            ?.takeIf(supportedIntervalsMillis::contains)
            ?: DEFAULT_INTERVAL_MILLIS
}
