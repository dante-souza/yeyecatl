package io.github.dante_souza.yeyecatl.domain.wifi

import kotlin.math.max

data class WifiPrimaryChannelOccupancy(
    val channel: Int,
    val primaryFrequencyMhz: Int,
    val accessPointCount: Int,
    val strongestRssiDbm: Int?
)

data class WifiGeometricOverlapPair(
    val firstLabel: String,
    val firstBssid: String?,
    val firstRssiDbm: Int?,
    val secondLabel: String,
    val secondBssid: String?,
    val secondRssiDbm: Int?,
    val overlapBandwidthMhz: Int,
    val estimatedFromPartialGeometry: Boolean
)

data class WifiChannelOccupancyOverview(
    val band: WifiBand,
    val observedAccessPointCount: Int,
    val unmappedPrimaryChannelAccessPointCount: Int,
    val nonCompleteGeometryAccessPointCount: Int,
    val channels: List<WifiPrimaryChannelOccupancy>,
    val overlappingPairs: List<WifiGeometricOverlapPair>
) {
    val mappedPrimaryChannelCount: Int
        get() = channels.size

    val overlappingPairCount: Int
        get() = overlappingPairs.size
}

object WifiChannelOccupancyAnalyzer {
    fun analyze(
        observations: List<WifiScanObservation>,
        band: WifiBand
    ): WifiChannelOccupancyOverview {
        val interpreted = observations.mapNotNull { observation ->
            val rf = WifiRfInterpreter.interpret(observation)
            if (rf.band != band) {
                return@mapNotNull null
            }

            InterpretedObservation(
                observation = observation,
                rf = rf,
                footprint = WifiSpectrumGeometry.footprint(rf)
            )
        }

        val channels = interpreted
            .mapNotNull { item ->
                val channel = item.rf.primaryChannel ?: return@mapNotNull null
                val frequency = item.rf.primaryFrequencyMhz ?: return@mapNotNull null
                Triple(channel, frequency, item.observation.rssiDbm)
            }
            .groupBy { (channel, frequency, _) -> channel to frequency }
            .map { (key, entries) ->
                WifiPrimaryChannelOccupancy(
                    channel = key.first,
                    primaryFrequencyMhz = key.second,
                    accessPointCount = entries.size,
                    strongestRssiDbm = entries.mapNotNull { it.third }.maxOrNull()
                )
            }
            .sortedWith(
                compareByDescending<WifiPrimaryChannelOccupancy> { it.accessPointCount }
                    .thenByDescending { it.strongestRssiDbm ?: Int.MIN_VALUE }
                    .thenBy { it.channel }
            )

        val overlappingPairs = buildList {
            interpreted.forEachIndexed { firstIndex, first ->
                for (secondIndex in firstIndex + 1 until interpreted.size) {
                    val second = interpreted[secondIndex]
                    val overlap = WifiSpectrumGeometry.overlap(
                        first = first.footprint,
                        second = second.footprint
                    )
                    if (!overlap.overlaps) {
                        continue
                    }

                    add(
                        WifiGeometricOverlapPair(
                            firstLabel = first.observation.displayLabel(),
                            firstBssid = first.observation.bssid,
                            firstRssiDbm = first.observation.rssiDbm,
                            secondLabel = second.observation.displayLabel(),
                            secondBssid = second.observation.bssid,
                            secondRssiDbm = second.observation.rssiDbm,
                            overlapBandwidthMhz = overlap.overlapBandwidthMhz,
                            estimatedFromPartialGeometry =
                                first.footprint.completeness != WifiSpectrumCompleteness.Complete ||
                                    second.footprint.completeness !=
                                    WifiSpectrumCompleteness.Complete
                        )
                    )
                }
            }
        }.sortedWith(
            compareByDescending<WifiGeometricOverlapPair> { it.overlapBandwidthMhz }
                .thenByDescending {
                    max(
                        it.firstRssiDbm ?: Int.MIN_VALUE,
                        it.secondRssiDbm ?: Int.MIN_VALUE
                    )
                }
                .thenBy { it.firstLabel }
                .thenBy { it.secondLabel }
        )

        return WifiChannelOccupancyOverview(
            band = band,
            observedAccessPointCount = interpreted.size,
            unmappedPrimaryChannelAccessPointCount = interpreted.count {
                it.rf.primaryChannel == null || it.rf.primaryFrequencyMhz == null
            },
            nonCompleteGeometryAccessPointCount = interpreted.count {
                it.footprint.completeness != WifiSpectrumCompleteness.Complete
            },
            channels = channels,
            overlappingPairs = overlappingPairs
        )
    }

    private data class InterpretedObservation(
        val observation: WifiScanObservation,
        val rf: WifiRfCharacteristics,
        val footprint: WifiSpectrumFootprint
    )

    private fun WifiScanObservation.displayLabel(): String =
        ssid.displayText ?: if (ssid.isHidden) {
            "<hidden>"
        } else {
            "<unavailable>"
        }
}
