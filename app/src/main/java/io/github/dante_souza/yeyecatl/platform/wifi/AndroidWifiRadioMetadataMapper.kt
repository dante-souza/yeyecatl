package io.github.dante_souza.yeyecatl.platform.wifi

import io.github.dante_souza.yeyecatl.domain.wifi.WifiChannelWidth
import io.github.dante_souza.yeyecatl.domain.wifi.WifiStandard

object AndroidWifiRadioMetadataMapper {
    fun channelWidth(apiLevel: Int, rawChannelWidth: Int): WifiChannelWidth =
        when (rawChannelWidth) {
            0 -> WifiChannelWidth.Mhz20
            1 -> WifiChannelWidth.Mhz40
            2 -> WifiChannelWidth.Mhz80
            3 -> WifiChannelWidth.Mhz160
            4 -> WifiChannelWidth.Mhz80Plus80
            5 -> if (apiLevel >= 33) WifiChannelWidth.Mhz320 else WifiChannelWidth.Unknown
            else -> WifiChannelWidth.Unknown
        }

    fun wifiStandard(apiLevel: Int, rawWifiStandard: Int?): WifiStandard {
        if (apiLevel < 30 || rawWifiStandard == null) {
            return WifiStandard.Unknown
        }

        return when (rawWifiStandard) {
            1 -> WifiStandard.Legacy
            4 -> WifiStandard.Ieee80211n
            5 -> WifiStandard.Ieee80211ac
            6 -> WifiStandard.Ieee80211ax
            7 -> WifiStandard.Ieee80211ad
            8 -> if (apiLevel >= 33) WifiStandard.Ieee80211be else WifiStandard.Unknown
            else -> WifiStandard.Unknown
        }
    }
}
