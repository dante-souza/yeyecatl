package io.github.dante_souza.yeyecatl.ui.networks

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.dante_souza.yeyecatl.domain.wifi.WifiBand
import io.github.dante_souza.yeyecatl.domain.wifi.WifiObservationSort

@Composable
fun WifiObservationQueryControls(
    text: String,
    selectedBand: WifiBand?,
    selectedSort: WifiObservationSort,
    onTextChange: (String) -> Unit,
    onBandSelected: (WifiBand?) -> Unit,
    onSortSelected: (WifiObservationSort) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            label = { Text("Filter SSID or BSSID") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = "Nearby network text filter"
                }
        )

        QueryChipRow(
            title = "Band filter"
        ) {
            bandOptions.forEach { option ->
                FilterChip(
                    selected = selectedBand == option.band,
                    onClick = { onBandSelected(option.band) },
                    label = { Text(option.label) }
                )
            }
        }

        QueryChipRow(
            title = "Sort"
        ) {
            WifiObservationSort.entries.forEach { sort ->
                FilterChip(
                    selected = selectedSort == sort,
                    onClick = { onSortSelected(sort) },
                    label = { Text(sort.label()) }
                )
            }
        }
    }
}

@Composable
private fun QueryChipRow(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            content()
        }
    }
}

private data class BandOption(
    val band: WifiBand?,
    val label: String
)

private val bandOptions = listOf(
    BandOption(null, "All bands"),
    BandOption(WifiBand.Ghz2_4, "2.4 only"),
    BandOption(WifiBand.Ghz5, "5 only"),
    BandOption(WifiBand.Ghz6, "6 only")
)

private fun WifiObservationSort.label(): String =
    when (this) {
        WifiObservationSort.PlatformOrder -> "Scan order"
        WifiObservationSort.StrongestFirst -> "Signal strongest"
        WifiObservationSort.WeakestFirst -> "Signal weakest"
        WifiObservationSort.SsidAscending -> "SSID A-Z"
        WifiObservationSort.ChannelAscending -> "Channel"
    }
