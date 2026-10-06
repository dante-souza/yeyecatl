package io.github.dante_souza.yeyecatl.platform.wifi

import io.github.dante_souza.yeyecatl.domain.wifi.WifiChannelWidth
import io.github.dante_souza.yeyecatl.domain.wifi.WifiStandard
import org.junit.Assert.assertEquals
import org.junit.Test

class AndroidWifiRadioMetadataMapperTest {
    @Test
    fun mapsAndroidChannelWidths() {
        listOf(
            0 to WifiChannelWidth.Mhz20,
            1 to WifiChannelWidth.Mhz40,
            2 to WifiChannelWidth.Mhz80,
            3 to WifiChannelWidth.Mhz160,
            4 to WifiChannelWidth.Mhz80Plus80,
            5 to WifiChannelWidth.Mhz320,
            -1 to WifiChannelWidth.Unknown,
            99 to WifiChannelWidth.Unknown
        ).forEach { (raw, width) ->
            assertEquals(width, AndroidWifiRadioMetadataMapper.channelWidth(36, raw))
        }
    }

    @Test
    fun threeHundredTwentyMhzWidthIsUnknownBeforeAndroidThirteen() {
        assertEquals(WifiChannelWidth.Unknown, AndroidWifiRadioMetadataMapper.channelWidth(32, 5))
    }

    @Test
    fun mapsAndroidWifiStandards() {
        listOf(
            1 to WifiStandard.Legacy,
            4 to WifiStandard.Ieee80211n,
            5 to WifiStandard.Ieee80211ac,
            6 to WifiStandard.Ieee80211ax,
            7 to WifiStandard.Ieee80211ad,
            8 to WifiStandard.Ieee80211be,
            0 to WifiStandard.Unknown,
            99 to WifiStandard.Unknown
        ).forEach { (raw, standard) ->
            assertEquals(standard, AndroidWifiRadioMetadataMapper.wifiStandard(36, raw))
        }
    }

    @Test
    fun wifiStandardIsUnknownBeforeAndroidExposesIt() {
        assertEquals(WifiStandard.Unknown, AndroidWifiRadioMetadataMapper.wifiStandard(29, 6))
        assertEquals(WifiStandard.Unknown, AndroidWifiRadioMetadataMapper.wifiStandard(36, null))
    }

    @Test
    fun wifiSevenIsUnknownBeforeAndroidThirteen() {
        assertEquals(WifiStandard.Unknown, AndroidWifiRadioMetadataMapper.wifiStandard(30, 8))
    }
}
