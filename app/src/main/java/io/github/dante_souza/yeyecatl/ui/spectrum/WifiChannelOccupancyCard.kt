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
import io.github.dante_souza.yeyecatl.domain.wifi.WifiScanObservation

@Composable
fun WifiChannelOccupancyCard(
    observations: List<WifiScanObservation>,
    band: WifiBand,
    modifier: Modifier = Modifier
) {
    val overview = remember(observations, band) {
        WifiChannelOccupancyAnalyzer.analyze(observations, band)
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
                text = "Latest-scan primary-channel counts and geometric RF-footprint overlap for ${band.label()}.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OverviewRow("Access points", overview.observedAccessPointCount.toString())
            OverviewRow("Primary channels", overview.mappedPrimaryChannelCount.toString())
            OverviewRow("Overlapping AP pairs", overview.overlappingPairCount.toString())

            if (overview.observedAccessPointCount == 0) {
                Text(
                    text = "No ${band.label()} access points are present in the latest scan.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            } else {
                Text(
                    text = "Most occupied primary channels",
                    style = MaterialTheme.typography.labelLarge,
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
                        val strongest = channel.strongestRssiDbm?.let { " · strongest $it dBm" }.orEmpty()
                        val accessPointLabel = if (channel.accessPointCount == 1) "AP" else "APs"
                        Text(
                            text = "ch ${channel.channel} · ${channel.accessPointCount} $accessPointLabel$strongest",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Text(
                    text = "Largest geometric overlaps",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 6.dp)
                )

                if (overview.overlappingPairs.isEmpty()) {
                    Text(
                        text = "No positive RF-footprint intersections are present in this band.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    overview.overlappingPairs.take(5).forEach { pair ->
                        val geometryNote = if (pair.estimatedFromPartialGeometry) {
                            " · partial geometry"
                        } else {
                            ""
                        }
                        Text(
                            text = "${pair.firstLabel} ↔ ${pair.secondLabel} · " +
                                "${pair.overlapBandwidthMhz} MHz$geometryNote",
                            style = MaterialTheme.typography.bodyMedium
                        )
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
                text = "Occupancy here means observed AP count, not airtime/channel-utilization measurement. No interference score or channel recommendation is produced.",
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

private fun WifiBand.label(): String =
    when (this) {
        WifiBand.Ghz2_4 -> "2.4 GHz"
        WifiBand.Ghz5 -> "5 GHz"
        WifiBand.Ghz6 -> "6 GHz"
        WifiBand.Ghz60 -> "60 GHz"
        WifiBand.Unknown -> "Unknown"
    }
