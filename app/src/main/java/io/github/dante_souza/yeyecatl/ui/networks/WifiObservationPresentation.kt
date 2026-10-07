package io.github.dante_souza.yeyecatl.ui.networks

import io.github.dante_souza.yeyecatl.domain.wifi.WifiBand
import io.github.dante_souza.yeyecatl.domain.wifi.WifiChannelWidth
import io.github.dante_souza.yeyecatl.domain.wifi.WifiRfInterpreter
import io.github.dante_souza.yeyecatl.domain.wifi.WifiScanObservation
import io.github.dante_souza.yeyecatl.domain.wifi.WifiStandard

data class WifiObservationPresentation(
    val ssidText: String,
    val bssidText: String,
    val rssiText: String,
    val radioSummary: String,
    val capabilitiesSummary: String
)

object WifiObservationPresenter {
    fun present(observation: WifiScanObservation): WifiObservationPresentation {
        val rf = WifiRfInterpreter.interpret(observation)

        return WifiObservationPresentation(
            ssidText = observation.ssid.displayText ?: if (observation.ssid.isHidden) {
                "Hidden network"
            } else {
                "SSID unavailable"
            },
            bssidText = observation.bssid ?: "Unavailable",
            rssiText = observation.rssiDbm?.let { "$it dBm" } ?: "RSSI ?",
            radioSummary = buildList {
                add(rf.band.displayLabel())
                add(rf.primaryChannel?.let { "Ch $it" } ?: "Ch ?")
                add(rf.channelWidth.displayLabel())
                rf.wifiStandard.displayLabelOrNull()?.let(::add)
            }.joinToString(separator = " • "),
            capabilitiesSummary = observation.capabilities
                ?.let { "Capabilities  $it" }
                ?: "Capabilities  not reported"
        )
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

    private fun WifiStandard.displayLabelOrNull(): String? =
        when (this) {
            WifiStandard.Legacy -> "Legacy"
            WifiStandard.Ieee80211n -> "802.11n"
            WifiStandard.Ieee80211ac -> "802.11ac"
            WifiStandard.Ieee80211ax -> "802.11ax"
            WifiStandard.Ieee80211ad -> "802.11ad"
            WifiStandard.Ieee80211be -> "802.11be"
            WifiStandard.Unknown -> null
        }
}


fun sameSsidBssidCounts(
    observations: List<WifiScanObservation>
): Map<String, Int> =
    observations
        .mapNotNull { observation ->
            val ssid = observation.ssid.displayText ?: return@mapNotNull null
            val bssid = observation.bssid ?: return@mapNotNull null
            ssid to bssid
        }
        .groupBy(
            keySelector = { it.first },
            valueTransform = { it.second }
        )
        .mapValues { (_, bssids) -> bssids.distinct().size }
