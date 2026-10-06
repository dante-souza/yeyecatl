package io.github.dante_souza.yeyecatl.domain.wifi

import kotlin.math.max
import kotlin.math.min

enum class WifiSpectrumCompleteness {
    Complete,
    Partial,
    Unavailable,
    Inconsistent
}

data class WifiSpectrumSegment(
    val centerFrequencyMhz: Int,
    val widthMhz: Int
) {
    val lowerFrequencyMhz: Int = centerFrequencyMhz - (widthMhz / 2)
    val upperFrequencyMhz: Int = centerFrequencyMhz + (widthMhz / 2)

    fun contains(frequencyMhz: Int): Boolean =
        frequencyMhz >= lowerFrequencyMhz && frequencyMhz <= upperFrequencyMhz
}

data class WifiSpectrumFootprint(
    val segments: List<WifiSpectrumSegment>,
    val totalNominalBandwidthMhz: Int,
    val completeness: WifiSpectrumCompleteness
)

data class WifiSpectrumOverlapSegment(
    val lowerFrequencyMhz: Int,
    val upperFrequencyMhz: Int
) {
    val widthMhz: Int = upperFrequencyMhz - lowerFrequencyMhz
}

data class WifiSpectrumOverlap(
    val overlappingSegments: List<WifiSpectrumOverlapSegment>
) {
    val overlapBandwidthMhz: Int = overlappingSegments.sumOf { it.widthMhz }
    val overlaps: Boolean = overlapBandwidthMhz > 0
}

object WifiSpectrumGeometry {
    fun footprint(rf: WifiRfCharacteristics): WifiSpectrumFootprint {
        val primary = rf.primaryFrequencyMhz?.takeIf { it > 0 }
            ?: return unavailable()

        return when (rf.channelWidth) {
            WifiChannelWidth.Mhz20 -> completeOrInconsistent(primary, listOf(segment(primary, 20)))
            WifiChannelWidth.Mhz40 -> centered(primary, rf.centerFrequency0Mhz, 40)
            WifiChannelWidth.Mhz80 -> centered(primary, rf.centerFrequency0Mhz, 80)
            WifiChannelWidth.Mhz160 -> centered(primary, rf.centerFrequency0Mhz, 160)
            WifiChannelWidth.Mhz320 -> centered(primary, rf.centerFrequency0Mhz, 320)
            WifiChannelWidth.Mhz80Plus80 -> eightyPlusEighty(primary, rf)
            WifiChannelWidth.Unknown -> WifiSpectrumFootprint(
                segments = emptyList(),
                totalNominalBandwidthMhz = 0,
                completeness = WifiSpectrumCompleteness.Partial
            )
        }
    }

    fun overlap(
        first: WifiSpectrumFootprint,
        second: WifiSpectrumFootprint
    ): WifiSpectrumOverlap {
        val overlaps = first.segments.flatMap { left ->
            second.segments.mapNotNull { right ->
                val lower = max(left.lowerFrequencyMhz, right.lowerFrequencyMhz)
                val upper = min(left.upperFrequencyMhz, right.upperFrequencyMhz)
                if (upper > lower) {
                    WifiSpectrumOverlapSegment(lower, upper)
                } else {
                    null
                }
            }
        }

        return WifiSpectrumOverlap(overlaps)
    }

    private fun centered(
        primaryFrequencyMhz: Int,
        centerFrequencyMhz: Int?,
        widthMhz: Int
    ): WifiSpectrumFootprint {
        val center = centerFrequencyMhz?.takeIf { it > 0 }
            ?: return partialPrimary(primaryFrequencyMhz)

        return completeOrInconsistent(
            primaryFrequencyMhz = primaryFrequencyMhz,
            segments = listOf(segment(center, widthMhz))
        )
    }

    private fun eightyPlusEighty(
        primaryFrequencyMhz: Int,
        rf: WifiRfCharacteristics
    ): WifiSpectrumFootprint {
        val center0 = rf.centerFrequency0Mhz?.takeIf { it > 0 }
            ?: return partialPrimary(primaryFrequencyMhz)
        val center1 = rf.centerFrequency1Mhz?.takeIf { it > 0 }
            ?: return partialPrimary(primaryFrequencyMhz)

        if (center0 == center1) {
            return WifiSpectrumFootprint(
                segments = emptyList(),
                totalNominalBandwidthMhz = 0,
                completeness = WifiSpectrumCompleteness.Inconsistent
            )
        }

        return completeOrInconsistent(
            primaryFrequencyMhz = primaryFrequencyMhz,
            segments = listOf(segment(center0, 80), segment(center1, 80))
        )
    }

    private fun completeOrInconsistent(
        primaryFrequencyMhz: Int,
        segments: List<WifiSpectrumSegment>
    ): WifiSpectrumFootprint {
        val complete = segments.any { it.contains(primaryFrequencyMhz) } &&
            segments.all { it.widthMhz > 0 && it.lowerFrequencyMhz < it.centerFrequencyMhz &&
                it.centerFrequencyMhz < it.upperFrequencyMhz }

        return WifiSpectrumFootprint(
            segments = segments,
            totalNominalBandwidthMhz = segments.sumOf { it.widthMhz },
            completeness = if (complete) {
                WifiSpectrumCompleteness.Complete
            } else {
                WifiSpectrumCompleteness.Inconsistent
            }
        )
    }

    private fun partialPrimary(primaryFrequencyMhz: Int): WifiSpectrumFootprint =
        WifiSpectrumFootprint(
            segments = listOf(segment(primaryFrequencyMhz, 20)),
            totalNominalBandwidthMhz = 20,
            completeness = WifiSpectrumCompleteness.Partial
        )

    private fun unavailable(): WifiSpectrumFootprint =
        WifiSpectrumFootprint(
            segments = emptyList(),
            totalNominalBandwidthMhz = 0,
            completeness = WifiSpectrumCompleteness.Unavailable
        )

    private fun segment(centerFrequencyMhz: Int, widthMhz: Int): WifiSpectrumSegment =
        WifiSpectrumSegment(
            centerFrequencyMhz = centerFrequencyMhz,
            widthMhz = widthMhz
        )
}
