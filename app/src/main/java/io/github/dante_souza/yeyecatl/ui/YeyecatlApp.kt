package io.github.dante_souza.yeyecatl.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.dante_souza.yeyecatl.R
import io.github.dante_souza.yeyecatl.domain.wifi.ObservedSsid
import io.github.dante_souza.yeyecatl.domain.wifi.WifiBand
import io.github.dante_souza.yeyecatl.domain.wifi.WifiChannelWidth
import io.github.dante_souza.yeyecatl.domain.wifi.WifiObservationDetail
import io.github.dante_souza.yeyecatl.domain.wifi.WifiObservationDetailResolver
import io.github.dante_souza.yeyecatl.domain.wifi.WifiObservationQuery
import io.github.dante_souza.yeyecatl.domain.wifi.WifiObservationSelection
import io.github.dante_souza.yeyecatl.domain.wifi.WifiObservationQueryEngine
import io.github.dante_souza.yeyecatl.domain.wifi.WifiObservationSort
import io.github.dante_souza.yeyecatl.domain.wifi.WifiScanBlockReason
import io.github.dante_souza.yeyecatl.domain.wifi.WifiScanFreshness
import io.github.dante_souza.yeyecatl.domain.wifi.WifiScanObservation
import io.github.dante_souza.yeyecatl.domain.wifi.WifiScanResultSource
import io.github.dante_souza.yeyecatl.domain.wifi.WifiScanSnapshot
import io.github.dante_souza.yeyecatl.domain.wifi.WifiScanState
import io.github.dante_souza.yeyecatl.domain.wifi.WifiSignalRanker
import io.github.dante_souza.yeyecatl.domain.wifi.WifiSignalScope
import io.github.dante_souza.yeyecatl.domain.wifi.WifiRfInterpreter
import io.github.dante_souza.yeyecatl.domain.wifi.WifiStandard
import io.github.dante_souza.yeyecatl.domain.wifi.WifiTemporalObservationHistory
import io.github.dante_souza.yeyecatl.platform.wifi.LocationServicesStatus
import io.github.dante_souza.yeyecatl.platform.wifi.PermissionGrantState
import io.github.dante_souza.yeyecatl.platform.wifi.PermissionRequirement
import io.github.dante_souza.yeyecatl.platform.wifi.ScannerImplementationStatus
import io.github.dante_souza.yeyecatl.platform.wifi.WifiDiscoveryPermission
import io.github.dante_souza.yeyecatl.platform.wifi.WifiDiscoveryPermissionStatus
import io.github.dante_souza.yeyecatl.platform.wifi.WifiHardwareStatus
import io.github.dante_souza.yeyecatl.platform.wifi.WifiPlatformReadiness
import io.github.dante_souza.yeyecatl.platform.wifi.WifiPowerStatus
import io.github.dante_souza.yeyecatl.ui.history.WifiSignalHistoryChart
import io.github.dante_souza.yeyecatl.ui.networks.WifiObservationDetailCard
import io.github.dante_souza.yeyecatl.ui.networks.WifiObservationListItem
import io.github.dante_souza.yeyecatl.ui.networks.WifiObservationQueryControls
import io.github.dante_souza.yeyecatl.ui.networks.sameSsidBssidCounts
import io.github.dante_souza.yeyecatl.ui.signal.WifiSignalRankingCard
import io.github.dante_souza.yeyecatl.ui.spectrum.WifiChannelOccupancyCard
import io.github.dante_souza.yeyecatl.ui.spectrum.WifiSpectrumChart
import io.github.dante_souza.yeyecatl.ui.theme.YeyecatlTheme

@Composable
fun YeyecatlApp(
    readiness: WifiPlatformReadiness = previewReadiness(),
    scanState: WifiScanState = WifiScanState.Idle,
    temporalHistory: WifiTemporalObservationHistory = WifiTemporalObservationHistory(),
    dynamicScanEnabled: Boolean = false,
    onRequestScan: () -> Unit = {},
    onToggleDynamicScan: () -> Unit = {},
    onRequestDiscoveryPermission: () -> Unit = {}
) {
    YeyecatlTheme {
        Scaffold(
            topBar = { YeyecatlTopBar() },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                color = MaterialTheme.colorScheme.background
            ) {
                YeyecatlReadinessScreen(
                    readiness = readiness,
                    scanState = scanState,
                    temporalHistory = temporalHistory,
                    dynamicScanEnabled = dynamicScanEnabled,
                    onRequestScan = onRequestScan,
                    onToggleDynamicScan = onToggleDynamicScan,
                    onRequestDiscoveryPermission = onRequestDiscoveryPermission
                )
            }
        }
    }
}

@Composable
private fun YeyecatlTopBar() {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.yeyecatl_launcher_icon),
                contentDescription = null,
                modifier = Modifier.size(44.dp)
            )
            Column {
                Text(
                    text = "Yeyecatl",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Wi-Fi field analyzer",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun YeyecatlReadinessScreen(
    readiness: WifiPlatformReadiness,
    scanState: WifiScanState,
    temporalHistory: WifiTemporalObservationHistory,
    dynamicScanEnabled: Boolean,
    onRequestScan: () -> Unit,
    onToggleDynamicScan: () -> Unit,
    onRequestDiscoveryPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Scanner",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Android Wi-Fi platform readiness and nearby spectrum observation.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp)
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            shape = MaterialTheme.shapes.large,
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                ReadinessRow("Wi-Fi hardware", readiness.hardware.label())
                ReadinessRow("Wi-Fi state", readiness.wifiPower.label())
                ReadinessRow("Location services", readiness.locationServices.label())
                ReadinessRow("Scan permission", readiness.discoveryPermission.label())
                ReadinessRow("Discovery allowed", readiness.isDiscoveryAllowed.yesNo())
                ReadinessRow("Scanner", readiness.scanner.label())
            }
        }

        if (readiness.discoveryPermission.canRequestFromApp) {
            OutlinedButton(
                onClick = onRequestDiscoveryPermission,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
            ) {
                Text("Grant scan permission")
            }
        }

        Button(
            onClick = onRequestScan,
            enabled = readiness.isDiscoveryAllowed && !dynamicScanEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        ) {
            Text("Scan Wi-Fi")
        }

        OutlinedButton(
            onClick = onToggleDynamicScan,
            enabled = readiness.isDiscoveryAllowed,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text(if (dynamicScanEnabled) "Stop dynamic scan" else "Start dynamic scan")
        }

        if (dynamicScanEnabled) {
            Text(
                text = "Foreground cadence: every 30 seconds. Android may reject individual scan requests.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Text(
            text = "Observation",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 28.dp)
        )
        ReadinessRow("Scan state", scanState.label())
        ReadinessRow("Dynamic scan", if (dynamicScanEnabled) "Running" else "Stopped")
        ReadinessRow("Tracked BSSIDs", temporalHistory.samplesByBssid.size.toString())
        ReadinessRow(
            "Signal samples",
            temporalHistory.samplesByBssid.values.sumOf { it.size }.toString()
        )
        scanState.message()?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        ScanResults(
            snapshot = scanState.latestSnapshot,
            temporalHistory = temporalHistory
        )

        Text(
            text = "Spectrum geometry is observational; interference scoring is not enabled.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)
        )
    }
}

@Composable
private fun ScanResults(
    snapshot: WifiScanSnapshot?,
    temporalHistory: WifiTemporalObservationHistory
) {
    if (snapshot == null) {
        ReadinessRow("Observed networks", "No scan results yet")
        return
    }

    var selectedBandName by rememberSaveable { mutableStateOf(WifiBand.Ghz2_4.name) }
    var selectedSignalScopeName by rememberSaveable { mutableStateOf(WifiSignalScope.All.name) }
    var observationFilterText by rememberSaveable { mutableStateOf("") }
    var observationBandName by rememberSaveable { mutableStateOf(ALL_BANDS_KEY) }
    var observationSortName by rememberSaveable {
        mutableStateOf(WifiObservationSort.PlatformOrder.name)
    }
    var selectedBssid by rememberSaveable { mutableStateOf<String?>(null) }
    var focusedDetail by remember { mutableStateOf<WifiObservationDetail?>(null) }
    val focusedSelectionBringIntoViewRequester = remember {
        BringIntoViewRequester()
    }

    val selectedBand = WifiBand.valueOf(selectedBandName)
    val selectedSignalScope = WifiSignalScope.valueOf(selectedSignalScopeName)
    val observationBand = observationBandName
        .takeUnless { it == ALL_BANDS_KEY }
        ?.let(WifiBand::valueOf)
    val observationSort = WifiObservationSort.valueOf(observationSortName)

    val spectrumObservations = spectrumObservations(
        observations = snapshot.observations,
        band = selectedBand,
        scope = selectedSignalScope
    )
    val visibleObservations = WifiObservationQueryEngine.apply(
        observations = snapshot.observations,
        query = WifiObservationQuery(
            band = observationBand,
            text = observationFilterText,
            sort = observationSort
        )
    )
    val sameSsidCounts = sameSsidBssidCounts(snapshot.observations)

    LaunchedEffect(selectedBssid, snapshot.receivedAtMillis, temporalHistory) {
        focusedDetail = WifiObservationDetailResolver.resolve(
            selection = selectedBssid?.let(::WifiObservationSelection),
            snapshot = snapshot,
            history = temporalHistory,
            previousDetail = focusedDetail
        )
    }

    val selectedDetail = focusedDetail
        ?.takeIf { it.selection.bssid == selectedBssid }

    LaunchedEffect(selectedDetail?.selection?.bssid) {
        if (selectedDetail != null) {
            focusedSelectionBringIntoViewRequester.bringIntoView()
        }
    }

    BandSelector(
        selectedBand = selectedBand,
        onSelected = { selectedBandName = it.name }
    )
    SignalScopeSelector(
        selectedScope = selectedSignalScope,
        onSelected = { selectedSignalScopeName = it.name }
    )
    WifiSpectrumChart(
        observations = spectrumObservations,
        band = selectedBand,
        selectedBssid = selectedBssid,
        modifier = Modifier.padding(top = 16.dp)
    )

    WifiChannelOccupancyCard(
        observations = snapshot.observations,
        band = selectedBand,
        selectedBssid = selectedBssid,
        modifier = Modifier.padding(top = 20.dp)
    )

    Text(
        text = "Signal history",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 24.dp)
    )
    Text(
        text = "History follows the selected band and All / Strongest 5 / Weakest 5 filter.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp)
    )
    WifiSignalHistoryChart(
        history = temporalHistory,
        observations = spectrumObservations,
        band = selectedBand,
        selectedBssid = selectedBssid,
        modifier = Modifier.padding(top = 8.dp)
    )

    WifiSignalRankingCard(
        observations = snapshot.observations,
        modifier = Modifier.padding(top = 20.dp)
    )

    Text(
        text = "Nearby networks",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 24.dp)
    )
    Text(
        text = "Filter and sort only the latest scan list. Selecting a BSSID synchronizes the cross-view highlight.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp)
    )
    WifiObservationQueryControls(
        text = observationFilterText,
        selectedBand = observationBand,
        selectedSort = observationSort,
        onTextChange = { observationFilterText = it },
        onBandSelected = {
            observationBandName = it?.name ?: ALL_BANDS_KEY
        },
        onSortSelected = {
            observationSortName = it.name
        },
        modifier = Modifier.padding(top = 12.dp)
    )
    Text(
        text = "Showing ${visibleObservations.size} of ${snapshot.observations.size}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp)
    )

    ReadinessRow("Observed networks", snapshot.observations.size.toString())
    ReadinessRow("Visible networks", visibleObservations.size.toString())
    ReadinessRow("Freshness", snapshot.freshness.label())

    if (selectedDetail != null) {
        Text(
            text = "Focused selection",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(top = 14.dp)
                .bringIntoViewRequester(focusedSelectionBringIntoViewRequester)
        )
        WifiObservationDetailCard(
            detail = selectedDetail,
            onClearSelection = { selectedBssid = null },
            modifier = Modifier.padding(top = 6.dp, bottom = 4.dp)
        )
    }

    if (visibleObservations.isEmpty()) {
        Text(
            text = "No networks match the current filters.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
    } else {
        visibleObservations.forEachIndexed { index, observation ->
            val observationKey = observation.bssid
                ?: "${observation.ssid.displayText}:${observation.frequencyMhz}:$index"
            key(observationKey) {
                val isSelected = observation.bssid != null &&
                    observation.bssid == selectedBssid
                WifiObservationListItem(
                    observation = observation,
                    sameSsidBssidCount = observation.ssid.displayText
                        ?.let { sameSsidCounts[it] }
                        ?: 0,
                    selected = isSelected,
                    onSelect = observation.bssid?.let { bssid ->
                        {
                            selectedBssid = bssid
                            when (val observationBand =
                                WifiRfInterpreter.interpret(observation).band
                            ) {
                                WifiBand.Ghz2_4,
                                WifiBand.Ghz5,
                                WifiBand.Ghz6 -> {
                                    selectedBandName = observationBand.name
                                    selectedSignalScopeName = WifiSignalScope.All.name
                                }
                                WifiBand.Ghz60,
                                WifiBand.Unknown -> Unit
                            }
                        }
                    },
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
        }
    }

}

@Composable
private fun BandSelector(
    selectedBand: WifiBand,
    onSelected: (WifiBand) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf(WifiBand.Ghz2_4, WifiBand.Ghz5, WifiBand.Ghz6).forEach { band ->
            if (band == selectedBand) {
                Button(onClick = { onSelected(band) }) {
                    Text(band.label())
                }
            } else {
                OutlinedButton(onClick = { onSelected(band) }) {
                    Text(band.label())
                }
            }
        }
    }
}

@Composable
private fun SignalScopeSelector(
    selectedScope: WifiSignalScope,
    onSelected: (WifiSignalScope) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
    ) {
        Text(
            text = "Spectrum filter",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            WifiSignalScope.entries.forEach { scope ->
                val buttonModifier = Modifier.weight(1f)
                val contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                if (scope == selectedScope) {
                    Button(
                        onClick = { onSelected(scope) },
                        modifier = buttonModifier,
                        contentPadding = contentPadding
                    ) {
                        Text(
                            text = scope.label(),
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = { onSelected(scope) },
                        modifier = buttonModifier,
                        contentPadding = contentPadding
                    ) {
                        Text(
                            text = scope.label(),
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

private fun spectrumObservations(
    observations: List<WifiScanObservation>,
    band: WifiBand,
    scope: WifiSignalScope
): List<WifiScanObservation> {
    val observationsInBand = observations.filter {
        WifiRfInterpreter.interpret(it).band == band
    }

    return if (scope == WifiSignalScope.All) {
        observationsInBand
    } else {
        WifiSignalRanker.select(
            observations = observationsInBand,
            scope = scope
        )
    }
}

private fun WifiSignalScope.label(): String =
    when (this) {
        WifiSignalScope.All -> "All"
        WifiSignalScope.Strongest -> "Strongest 5"
        WifiSignalScope.Weakest -> "Weakest 5"
    }

@Composable
private fun ReadinessRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun WifiHardwareStatus.label(): String =
    when (this) {
        WifiHardwareStatus.Available -> "Available"
        WifiHardwareStatus.Unavailable -> "Unavailable"
    }

private fun WifiPowerStatus.label(): String =
    when (this) {
        WifiPowerStatus.Enabled -> "Enabled"
        WifiPowerStatus.Disabled -> "Disabled"
        WifiPowerStatus.Unknown -> "Unknown"
    }

private fun LocationServicesStatus.label(): String =
    when (this) {
        LocationServicesStatus.Enabled -> "Enabled"
        LocationServicesStatus.Disabled -> "Disabled"
        LocationServicesStatus.Unknown -> "Unknown"
    }

private fun WifiDiscoveryPermissionStatus.label(): String =
    when (grantState) {
        PermissionGrantState.Available -> "Granted"
        PermissionGrantState.NotGranted -> "Not granted"
        PermissionGrantState.RequiresSettingsAction -> "Requires app settings"
        PermissionGrantState.NotRequired -> "Not required"
    }

private fun ScannerImplementationStatus.label(): String =
    when (this) {
        ScannerImplementationStatus.Implemented -> "Implemented"
        ScannerImplementationStatus.NotImplemented -> "Not implemented"
    }

private fun WifiScanState.label(): String =
    when (this) {
        WifiScanState.Idle -> "Idle"
        is WifiScanState.ScanRequested -> "Scan requested"
        is WifiScanState.Results -> "Results available"
        is WifiScanState.RequestRejected -> "Request rejected"
        is WifiScanState.Blocked -> reason.label()
        is WifiScanState.Error -> "Error"
    }

private fun WifiScanState.message(): String? =
    when (this) {
        is WifiScanState.RequestRejected -> message
        is WifiScanState.Error -> message
        else -> null
    }

private fun WifiScanBlockReason.label(): String =
    when (this) {
        WifiScanBlockReason.WifiHardwareUnavailable -> "Wi-Fi hardware unavailable"
        WifiScanBlockReason.WifiUnavailable -> "Wi-Fi unavailable"
        WifiScanBlockReason.LocationServicesUnavailable -> "Location Services unavailable"
        WifiScanBlockReason.PermissionUnavailable -> "Scan permission unavailable"
    }

private fun WifiScanFreshness.label(): String =
    when (this) {
        WifiScanFreshness.Fresh -> "Fresh"
        WifiScanFreshness.Cached -> "Cached or previous"
        WifiScanFreshness.Unknown -> "Unknown"
    }

private fun io.github.dante_souza.yeyecatl.domain.wifi.WifiBand.label(): String =
    when (this) {
        io.github.dante_souza.yeyecatl.domain.wifi.WifiBand.Ghz2_4 -> "2.4 GHz"
        io.github.dante_souza.yeyecatl.domain.wifi.WifiBand.Ghz5 -> "5 GHz"
        io.github.dante_souza.yeyecatl.domain.wifi.WifiBand.Ghz6 -> "6 GHz"
        io.github.dante_souza.yeyecatl.domain.wifi.WifiBand.Ghz60 -> "60 GHz"
        io.github.dante_souza.yeyecatl.domain.wifi.WifiBand.Unknown -> "Unknown"
    }

private fun Boolean.yesNo(): String =
    if (this) {
        "Yes"
    } else {
        "No"
    }

private fun previewReadiness(): WifiPlatformReadiness =
    WifiPlatformReadiness(
        hardware = WifiHardwareStatus.Available,
        wifiPower = WifiPowerStatus.Enabled,
        locationServices = LocationServicesStatus.Enabled,
        discoveryPermission = WifiDiscoveryPermissionStatus(
            permission = WifiDiscoveryPermission.FineLocation,
            requirement = PermissionRequirement.Required,
            grantState = PermissionGrantState.Available
        )
    )

private fun previewScanState(): WifiScanState =
    WifiScanState.Results(
        WifiScanSnapshot(
            observations = listOf(
                previewObservation("whanganui", "00:11:22:33:44:01", -42, 2412),
                previewObservation("Te Moana", "00:11:22:33:44:06", -61, 2437),
                previewObservation("IoT", "00:11:22:33:44:11", -73, 2462),
                previewObservation(
                    ssid = "Punga-5",
                    bssid = "00:11:22:33:55:36",
                    rssiDbm = -48,
                    frequencyMhz = 5180,
                    width = WifiChannelWidth.Mhz80,
                    center0 = 5210,
                    standard = WifiStandard.Ieee80211ac
                ),
                previewObservation(
                    ssid = "Lab-160",
                    bssid = "00:11:22:33:55:64",
                    rssiDbm = -66,
                    frequencyMhz = 5180,
                    width = WifiChannelWidth.Mhz160,
                    center0 = 5250,
                    standard = WifiStandard.Ieee80211ax
                ),
                previewObservation(
                    ssid = "Backhaul",
                    bssid = "00:11:22:33:55:80",
                    rssiDbm = -70,
                    frequencyMhz = 5180,
                    width = WifiChannelWidth.Mhz80Plus80,
                    center0 = 5210,
                    center1 = 5530,
                    standard = WifiStandard.Ieee80211ac
                ),
                previewObservation(
                    ssid = "Rua-6",
                    bssid = "00:11:22:33:66:01",
                    rssiDbm = -55,
                    frequencyMhz = 5955,
                    width = WifiChannelWidth.Mhz80,
                    center0 = 5985,
                    standard = WifiStandard.Ieee80211ax
                ),
                previewObservation(
                    ssid = "Rua-320",
                    bssid = "00:11:22:33:66:02",
                    rssiDbm = -67,
                    frequencyMhz = 5975,
                    width = WifiChannelWidth.Mhz320,
                    center0 = 6105,
                    standard = WifiStandard.Ieee80211be
                )
            ),
            freshness = WifiScanFreshness.Fresh,
            source = WifiScanResultSource.ApplicationRequest,
            resultsUpdated = true,
            receivedAtMillis = 0L
        )
    )

private fun previewObservation(
    ssid: String,
    bssid: String,
    rssiDbm: Int,
    frequencyMhz: Int,
    width: WifiChannelWidth = WifiChannelWidth.Mhz20,
    center0: Int? = null,
    center1: Int? = null,
    standard: WifiStandard = WifiStandard.Ieee80211n
): WifiScanObservation =
    WifiScanObservation(
        ssid = ObservedSsid(
            displayText = ssid,
            rawBytes = null,
            isHidden = false
        ),
        bssid = bssid,
        rssiDbm = rssiDbm,
        frequencyMhz = frequencyMhz,
        channelWidth = width,
        centerFrequency0Mhz = center0,
        centerFrequency1Mhz = center1,
        wifiStandard = standard,
        capabilities = "[ESS]",
        platformTimestampMicros = 1234L
    )

@Preview(showBackground = true)
@Composable
private fun YeyecatlPlaceholderPreview() {
    YeyecatlApp(scanState = previewScanState())
}

private const val ALL_BANDS_KEY = "all"
