package io.github.dante_souza.yeyecatl.ui.networks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.dante_souza.yeyecatl.domain.wifi.WifiBand
import io.github.dante_souza.yeyecatl.domain.wifi.WifiChannelWidth
import io.github.dante_souza.yeyecatl.domain.wifi.WifiObservationDetail
import io.github.dante_souza.yeyecatl.domain.wifi.WifiSpectrumCompleteness
import io.github.dante_souza.yeyecatl.domain.wifi.WifiStandard
import io.github.dante_souza.yeyecatl.ui.history.WifiFocusedSignalHistoryChart
import io.github.dante_souza.yeyecatl.ui.history.WifiFocusedSignalHistoryProjection

@Composable
fun WifiObservationDetailCard(
    detail: WifiObservationDetail,
    sameSsidBssidCount: Int = 0,
    onClearSelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    val observation = detail.latestObservation
    val temporalSummary = WifiFocusedSignalHistoryProjection.summary(detail.retainedSignalSamples)
    val timeSinceLastSeenMillis = (detail.latestSnapshotReceivedAtMillis - detail.lastSeenAtMillis)
        .coerceAtLeast(0L)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 3.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Selected access point",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = observation.ssid.displayText ?: if (observation.ssid.isHidden) {
                            "Hidden network"
                        } else {
                            "SSID unavailable"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(onClick = onClearSelection) {
                    Text("Clear selection")
                }
            }

            DetailRow(
                label = "Latest scan",
                value = if (detail.observedInLatestSnapshot) "Seen" else "Not seen"
            )
            DetailRow(
                label = "Last seen",
                value = if (detail.observedInLatestSnapshot) {
                    "now"
                } else {
                    elapsedLabel(timeSinceLastSeenMillis)
                }
            )

            DetailRow(
                label = "BSSID",
                value = detail.selection.bssid,
                monospace = true
            )
            if (sameSsidBssidCount > 1) {
                DetailRow(
                    label = "Same SSID",
                    value = "$sameSsidBssidCount BSSIDs in latest scan"
                )
            }
            DetailRow(
                label = "Observed RSSI",
                value = observation.rssiDbm?.let { "$it dBm" } ?: "Unavailable"
            )
            DetailRow(
                label = "Band",
                value = detail.rf.band.displayLabel()
            )
            DetailRow(
                label = "Channel",
                value = detail.rf.primaryChannel?.toString() ?: "Unavailable"
            )
            DetailRow(
                label = "Primary frequency",
                value = detail.rf.primaryFrequencyMhz?.let { "$it MHz" } ?: "Unavailable"
            )
            DetailRow(
                label = "Channel width",
                value = detail.rf.channelWidth.displayLabel()
            )
            DetailRow(
                label = "Center frequency 0",
                value = detail.rf.centerFrequency0Mhz?.let { "$it MHz" } ?: "Unavailable"
            )
            DetailRow(
                label = "Center frequency 1",
                value = detail.rf.centerFrequency1Mhz?.let { "$it MHz" } ?: "Unavailable"
            )
            DetailRow(
                label = "Wi-Fi standard",
                value = detail.rf.wifiStandard.displayLabel()
            )
            DetailRow(
                label = "Spectrum span",
                value = detail.spectrum.segments
                    .joinToString(separator = " + ") {
                        "${it.lowerFrequencyMhz}–${it.upperFrequencyMhz} MHz"
                    }
                    .ifEmpty { "Unavailable" }
            )
            DetailRow(
                label = "Spectrum geometry",
                value = detail.spectrum.completeness.displayLabel()
            )

            if (detail.retainedSignalSampleCount == 0) {
                Text(
                    text = "No retained RSSI samples yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                DetailRow(
                    label = "Retained RSSI samples",
                    value = detail.retainedSignalSampleCount.toString()
                )
                DetailRow(
                    label = "Retained history span",
                    value = detail.retainedHistorySpanMillis
                        ?.let { "${it / 1_000.0} s" }
                        ?: "Unavailable"
                )
                temporalSummary?.let { summary ->
                    DetailRow(
                        label = "Latest retained RSSI",
                        value = "${summary.latestRssiDbm} dBm"
                    )
                    DetailRow(
                        label = "Strongest retained RSSI",
                        value = "${summary.strongestRssiDbm} dBm"
                    )
                    DetailRow(
                        label = "Weakest retained RSSI",
                        value = "${summary.weakestRssiDbm} dBm"
                    )
                    DetailRow(
                        label = "Retained RSSI range",
                        value = "${summary.rangeDb} dB"
                    )
                }

                Text(
                    text = "Focused RSSI history",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 6.dp)
                )
                Text(
                    text = "Exact BSSID · bounded in-memory history · up to 60 newest retained samples shown.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                WifiFocusedSignalHistoryChart(
                    samples = detail.retainedSignalSamples,
                    bssid = detail.selection.bssid,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

        }
    }
}

private fun elapsedLabel(deltaMillis: Long): String {
    val seconds = deltaMillis / 1_000L
    return when {
        seconds < 60L -> "${seconds}s ago"
        seconds < 3_600L -> "${seconds / 60L}m ${seconds % 60L}s ago"
        else -> "${seconds / 3_600L}h ${(seconds % 3_600L) / 60L}m ago"
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    monospace: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.9f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = if (monospace) FontFamily.Monospace else FontFamily.Default,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1.1f)
        )
    }
}

private fun WifiBand.displayLabel(): String =
    when (this) {
        WifiBand.Ghz2_4 -> "2.4 GHz"
        WifiBand.Ghz5 -> "5 GHz"
        WifiBand.Ghz6 -> "6 GHz"
        WifiBand.Ghz60 -> "60 GHz"
        WifiBand.Unknown -> "Unknown"
    }

private fun WifiChannelWidth.displayLabel(): String =
    when (this) {
        WifiChannelWidth.Mhz20 -> "20 MHz"
        WifiChannelWidth.Mhz40 -> "40 MHz"
        WifiChannelWidth.Mhz80 -> "80 MHz"
        WifiChannelWidth.Mhz160 -> "160 MHz"
        WifiChannelWidth.Mhz80Plus80 -> "80+80 MHz"
        WifiChannelWidth.Mhz320 -> "320 MHz"
        WifiChannelWidth.Unknown -> "Unknown"
    }

private fun WifiStandard.displayLabel(): String =
    when (this) {
        WifiStandard.Legacy -> "Legacy"
        WifiStandard.Ieee80211n -> "802.11n"
        WifiStandard.Ieee80211ac -> "802.11ac"
        WifiStandard.Ieee80211ax -> "802.11ax"
        WifiStandard.Ieee80211ad -> "802.11ad"
        WifiStandard.Ieee80211be -> "802.11be"
        WifiStandard.Unknown -> "Unknown"
    }

private fun WifiSpectrumCompleteness.displayLabel(): String =
    when (this) {
        WifiSpectrumCompleteness.Complete -> "Complete"
        WifiSpectrumCompleteness.Partial -> "Partial"
        WifiSpectrumCompleteness.Unavailable -> "Unavailable"
        WifiSpectrumCompleteness.Inconsistent -> "Inconsistent"
    }
