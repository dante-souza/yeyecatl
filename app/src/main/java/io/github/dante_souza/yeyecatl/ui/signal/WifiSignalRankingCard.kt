package io.github.dante_souza.yeyecatl.ui.signal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.dante_souza.yeyecatl.domain.wifi.WifiScanObservation
import io.github.dante_souza.yeyecatl.domain.wifi.WifiSignalRanker

@Composable
fun WifiSignalRankingCard(
    observations: List<WifiScanObservation>,
    modifier: Modifier = Modifier
) {
    val ranking = WifiSignalRanker.rank(observations)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Signal ranking",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Latest scan snapshot, ranked by RSSI.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (ranking.strongest.isEmpty()) {
                Text(
                    text = "No RSSI measurements available.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                SignalRankingGroup(
                    title = "Strongest signals",
                    observations = ranking.strongest
                )
                SignalRankingGroup(
                    title = "Weakest signals",
                    observations = ranking.weakest
                )
            }
        }
    }
}

@Composable
private fun SignalRankingGroup(
    title: String,
    observations: List<WifiScanObservation>
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        observations.forEachIndexed { index, observation ->
            SignalRankingRow(
                rank = index + 1,
                observation = observation
            )
        }
    }
}

@Composable
private fun SignalRankingRow(
    rank: Int,
    observation: WifiScanObservation
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "#$rank",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = observation.signalDisplayName(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = observation.bssid ?: "<no BSSID>",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = "${observation.rssiDbm} dBm",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun WifiScanObservation.signalDisplayName(): String =
    ssid.displayText ?: if (ssid.isHidden) {
        "<hidden>"
    } else {
        "<unavailable>"
    }
