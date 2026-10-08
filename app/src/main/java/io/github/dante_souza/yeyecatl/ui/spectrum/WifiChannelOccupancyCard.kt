package io.github.dante_souza.yeyecatl.ui.spectrum

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.dante_souza.yeyecatl.domain.wifi.WifiBand
import io.github.dante_souza.yeyecatl.domain.wifi.WifiChannelOccupancyAnalyzer
import io.github.dante_souza.yeyecatl.domain.wifi.WifiRfInterpreter
import io.github.dante_souza.yeyecatl.domain.wifi.WifiScanObservation
import java.util.Locale

@Composable
fun WifiChannelOccupancyCard(
    observations: List<WifiScanObservation>,
    band: WifiBand,
    selectedBssid: String? = null,
    modifier: Modifier = Modifier
) {
    val overview = remember(observations, band) {
        WifiChannelOccupancyAnalyzer.analyze(observations, band)
    }
    val selectedObservation = remember(observations, band, selectedBssid) {
        observations.firstOrNull { observation ->
            observation.bssid == selectedBssid &&
                WifiRfInterpreter.interpret(observation).band == band
        }
    }
    val selectedRf = selectedObservation?.let(WifiRfInterpreter::interpret)
    val selectedOverlapCount = selectedBssid?.let { bssid ->
        overview.overlappingPairs.count { pair ->
            pair.firstBssid == bssid || pair.secondBssid == bssid
        }
    } ?: 0
    val displayedOverlapPairs = if (selectedBssid == null) {
        overview.overlappingPairs
    } else {
        val (selectedPairs, otherPairs) = overview.overlappingPairs.partition { pair ->
            pair.firstBssid == selectedBssid || pair.secondBssid == selectedBssid
        }
        selectedPairs + otherPairs
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Channel occupancy + overlap",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${band.label()} · latest scan",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OverviewRow("Access points", overview.observedAccessPointCount.toString())
            OverviewRow("Primary channels", overview.mappedPrimaryChannelCount.toString())
            OverviewRow("Overlap pairs", overview.overlappingPairCount.toString())
            OverviewRow(
                "Avg. overlap neighbors / AP",
                String.format(
                    Locale.US,
                    "%.1f",
                    overview.averageOverlappingNeighborsPerAccessPoint
                )
            )

            if (selectedObservation != null && selectedRf != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "Selected BSSID",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${selectedObservation.ssid.displayText ?: "<hidden>"} · " +
                                "ch ${selectedRf.primaryChannel ?: "?"} · " +
                                "${selectedObservation.rssiDbm?.let { "$it dBm" } ?: "RSSI unavailable"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "$selectedOverlapCount geometric overlap neighbor(s) in this band",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            if (overview.observedAccessPointCount == 0) {
                Text(
                    text = "No ${band.label()} access points are present in the latest scan.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            } else {
                Text(
                    text = "Most occupied",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 6.dp)
                )

                if (overview.channels.isEmpty()) {
                    Text(
                        text = "No primary channels could be mapped from this scan.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    overview.channels.take(6).forEach { channel ->
                        val strongest = channel.strongestRssiDbm
                            ?.let { " · strongest $it dBm" }
                            .orEmpty()
                        val accessPointLabel =
                            if (channel.accessPointCount == 1) "AP" else "APs"
                        Text(
                            text = "ch ${channel.channel} · ${channel.accessPointCount} " +
                                "$accessPointLabel$strongest",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Text(
                    text = "Largest overlaps",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 6.dp)
                )

                if (overview.overlappingPairs.isEmpty()) {
                    Text(
                        text = "No positive RF-footprint intersections are present in this band.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    displayedOverlapPairs.take(5).forEach { pair ->
                        val includesSelection = selectedBssid != null &&
                            (pair.firstBssid == selectedBssid || pair.secondBssid == selectedBssid)
                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Text(
                                text = "${endpointLabel(pair.firstLabel, pair.firstBssid)} vs " +
                                    endpointLabel(pair.secondLabel, pair.secondBssid),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (includesSelection) {
                                    FontWeight.SemiBold
                                } else {
                                    FontWeight.Medium
                                },
                                color = if (includesSelection) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                            Text(
                                text = "${pair.overlapBandwidthMhz} MHz overlap" +
                                    if (pair.estimatedFromPartialGeometry) {
                                        " · partial geometry"
                                    } else {
                                        ""
                                    },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (overview.unmappedPrimaryChannelAccessPointCount > 0) {
                Text(
                    text = "${overview.unmappedPrimaryChannelAccessPointCount} AP(s) could not be mapped to a primary channel.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (overview.nonCompleteGeometryAccessPointCount > 0) {
                Text(
                    text = "${overview.nonCompleteGeometryAccessPointCount} AP(s) use partial or unavailable geometry; overlap may be conservative.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "Occupancy means observed AP count, not airtime utilization. " +
                    "Overlap is geometric RF-footprint intersection; no interference score " +
                    "or channel recommendation is produced.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun OverviewRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun endpointLabel(
    label: String,
    bssid: String?
): String {
    val suffix = bssid
        ?.split(":")
        ?.takeLast(2)
        ?.takeIf { it.size == 2 }
        ?.joinToString(":")
        ?: return label

    return "$label [$suffix]"
}

private fun WifiBand.label(): String =
    when (this) {
        WifiBand.Ghz2_4 -> "2.4 GHz"
        WifiBand.Ghz5 -> "5 GHz"
        WifiBand.Ghz6 -> "6 GHz"
        WifiBand.Ghz60 -> "60 GHz"
        WifiBand.Unknown -> "Unknown"
    }
