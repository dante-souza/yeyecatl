package io.github.dante_souza.yeyecatl.ui.networks

import io.github.dante_souza.yeyecatl.domain.wifi.WifiBand
import io.github.dante_souza.yeyecatl.domain.wifi.WifiChannelWidth
import io.github.dante_souza.yeyecatl.domain.wifi.WifiRfInterpreter
import io.github.dante_souza.yeyecatl.domain.wifi.WifiScanObservation
import io.github.dante_souza.yeyecatl.domain.wifi.WifiSpectrumCompleteness
import io.github.dante_souza.yeyecatl.domain.wifi.WifiSpectrumGeometry
import io.github.dante_souza.yeyecatl.domain.wifi.WifiStandard

data class WifiObservationPresentation(
    val ssidText: String,
    val bssidText: String,
    val rssiText: String,
    val radioSummary: String,
    val geometrySummary: String,
    val capabilitiesSummary: String
)

object WifiObservationPresenter {
    fun present(observation: WifiScanObservation): WifiObservationPresentation {
        val rf = WifiRfInterpreter.interpret(observation)
        val footprint = WifiSpectrumGeometry.footprint(rf)

        return WifiObservationPresentation(
            ssidText = observation.ssid.displayText ?: if (observation.ssid.isHidden) {
                "Hidden network"
            } else {
                "SSID unavailable"
            },
            bssidText = observation.bssid ?: "Unavailable",
            rssiText = observation.rssiDbm?.let { "$it dBm" } ?: "RSSI ?",
            radioSummary = listOf(
                rf.band.displayLabel(),
                rf.primaryChannel?.let { "Ch $it" } ?: "Ch ?",
                rf.primaryFrequencyMhz?.let { "$it MHz" } ?: "Frequency ?",
                rf.channelWidth.displayLabel(),
                rf.wifiStandard.displayLabel()
            ).joinToString(separator = " • "),
            geometrySummary = listOf(
                centerSummary(rf.centerFrequency0Mhz, rf.centerFrequency1Mhz),
                footprint.segments
                    .takeIf { it.isNotEmpty() }
                    ?.joinToString(
                        prefix = "RF ",
                        separator = " / "
                    ) { segment ->
                        "${segment.lowerFrequencyMhz}–${segment.upperFrequencyMhz} MHz"
                    }
                    ?: "RF unavailable",
                "Geometry ${footprint.completeness.displayLabel()}"
            ).joinToString(separator = " • "),
            capabilitiesSummary = observation.capabilities
                ?.let { "Capabilities  $it" }
                ?: "Capabilities  not reported"
        )
    }

    private fun centerSummary(center0Mhz: Int?, center1Mhz: Int?): String =
        when {
            center0Mhz != null && center1Mhz != null ->
                "Centers $center0Mhz / $center1Mhz MHz"
            center0Mhz != null ->
                "Center $center0Mhz MHz"
            center1Mhz != null ->
                "Center $center1Mhz MHz"
            else ->
                "Center —"
        }

    private fun WifiBand.displayLabel(): String =
        when (this) {
            WifiBand.Ghz2_4 -> "2.4 GHz"
            WifiBand.Ghz5 -> "5 GHz"
            WifiBand.Ghz6 -> "6 GHz"
            WifiBand.Ghz60 -> "60 GHz"
            WifiBand.Unknown -> "Band unknown"
        }

    private fun WifiChannelWidth.displayLabel(): String =
        when (this) {
            WifiChannelWidth.Mhz20 -> "20 MHz"
            WifiChannelWidth.Mhz40 -> "40 MHz"
            WifiChannelWidth.Mhz80 -> "80 MHz"
            WifiChannelWidth.Mhz160 -> "160 MHz"
            WifiChannelWidth.Mhz80Plus80 -> "80+80 MHz"
            WifiChannelWidth.Mhz320 -> "320 MHz"
            WifiChannelWidth.Unknown -> "Width ?"
        }

    private fun WifiStandard.displayLabel(): String =
        when (this) {
            WifiStandard.Legacy -> "Legacy"
            WifiStandard.Ieee80211n -> "802.11n"
            WifiStandard.Ieee80211ac -> "802.11ac"
            WifiStandard.Ieee80211ax -> "802.11ax"
            WifiStandard.Ieee80211ad -> "802.11ad"
            WifiStandard.Ieee80211be -> "802.11be"
            WifiStandard.Unknown -> "Standard ?"
        }

    private fun WifiSpectrumCompleteness.displayLabel(): String =
        when (this) {
            WifiSpectrumCompleteness.Complete -> "complete"
            WifiSpectrumCompleteness.Partial -> "partial"
            WifiSpectrumCompleteness.Unavailable -> "unavailable"
            WifiSpectrumCompleteness.Inconsistent -> "inconsistent"
        }
}
