package io.github.dante_souza.yeyecatl.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.dante_souza.yeyecatl.domain.wifi.WifiScanBlockReason
import io.github.dante_souza.yeyecatl.domain.wifi.WifiScanFreshness
import io.github.dante_souza.yeyecatl.domain.wifi.WifiScanObservation
import io.github.dante_souza.yeyecatl.domain.wifi.WifiScanResultSource
import io.github.dante_souza.yeyecatl.domain.wifi.WifiScanSnapshot
import io.github.dante_souza.yeyecatl.domain.wifi.WifiScanState
import io.github.dante_souza.yeyecatl.platform.wifi.LocationServicesStatus
import io.github.dante_souza.yeyecatl.platform.wifi.PermissionGrantState
import io.github.dante_souza.yeyecatl.platform.wifi.PermissionRequirement
import io.github.dante_souza.yeyecatl.platform.wifi.ScannerImplementationStatus
import io.github.dante_souza.yeyecatl.platform.wifi.WifiDiscoveryPermission
import io.github.dante_souza.yeyecatl.platform.wifi.WifiDiscoveryPermissionStatus
import io.github.dante_souza.yeyecatl.platform.wifi.WifiHardwareStatus
import io.github.dante_souza.yeyecatl.platform.wifi.WifiPlatformReadiness
import io.github.dante_souza.yeyecatl.platform.wifi.WifiPowerStatus

@Composable
fun YeyecatlApp(
    readiness: WifiPlatformReadiness = previewReadiness(),
    scanState: WifiScanState = WifiScanState.Idle,
    onRequestScan: () -> Unit = {},
    onRequestDiscoveryPermission: () -> Unit = {}
) {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            YeyecatlReadinessScreen(
                readiness = readiness,
                scanState = scanState,
                onRequestScan = onRequestScan,
                onRequestDiscoveryPermission = onRequestDiscoveryPermission
            )
        }
    }
}

@Composable
fun YeyecatlReadinessScreen(
    readiness: WifiPlatformReadiness,
    scanState: WifiScanState,
    onRequestScan: () -> Unit,
    onRequestDiscoveryPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Yeyecatl",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Android Wi-Fi platform readiness",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp)
        )
        ReadinessRow("Wi-Fi hardware", readiness.hardware.label())
        ReadinessRow("Wi-Fi state", readiness.wifiPower.label())
        ReadinessRow("Location services", readiness.locationServices.label())
        ReadinessRow("Scan permission", readiness.discoveryPermission.label())
        ReadinessRow("Discovery allowed", readiness.isDiscoveryAllowed.yesNo())
        ReadinessRow("Scanner", readiness.scanner.label())

        if (readiness.discoveryPermission.canRequestFromApp) {
            Button(
                onClick = onRequestDiscoveryPermission,
                modifier = Modifier.padding(top = 24.dp)
            ) {
                Text("Grant scan permission")
            }
        }

        Button(
            onClick = onRequestScan,
            enabled = readiness.isDiscoveryAllowed,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Scan Wi-Fi")
        }

        ReadinessRow("Scan state", scanState.label())
        scanState.message()?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        ScanResults(scanState.latestSnapshot)

        Text(
            text = "Phase 1C: acquisition only. RF/channel analysis is intentionally not implemented yet.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
private fun ScanResults(snapshot: WifiScanSnapshot?) {
    if (snapshot == null) {
        ReadinessRow("Observed networks", "No scan results yet")
        return
    }

    ReadinessRow("Observed networks", snapshot.observations.size.toString())
    ReadinessRow("Freshness", snapshot.freshness.label())
    snapshot.observations.forEach { observation ->
        Text(
            text = observation.rowText(),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun ReadinessRow(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
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

private fun WifiScanObservation.rowText(): String {
    val ssidText = ssid.displayText ?: if (ssid.isHidden) {
        "<hidden>"
    } else {
        "<unavailable>"
    }
    return listOf(
        ssidText,
        bssid ?: "<no BSSID>",
        rssiDbm?.toString() ?: "?",
        frequencyMhz?.toString() ?: "?"
    ).joinToString(separator = "  ")
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
                WifiScanObservation(
                    ssid = io.github.dante_souza.yeyecatl.domain.wifi.ObservedSsid(
                        displayText = "whanganui",
                        rawBytes = null,
                        isHidden = false
                    ),
                    bssid = "xx:xx:xx:xx:xx:xx",
                    rssiDbm = -42,
                    frequencyMhz = 2412,
                    capabilities = "[WPA2-PSK-CCMP][ESS]",
                    platformTimestampMicros = 1234L
                )
            ),
            freshness = WifiScanFreshness.Fresh,
            source = WifiScanResultSource.ApplicationRequest,
            resultsUpdated = true,
            receivedAtMillis = 0L
        )
    )

@Preview(showBackground = true)
@Composable
private fun YeyecatlPlaceholderPreview() {
    YeyecatlApp(scanState = previewScanState())
}
