package io.github.dante_souza.yeyecatl.ui.networks

import io.github.dante_souza.yeyecatl.domain.wifi.ObservedSsid
import io.github.dante_souza.yeyecatl.domain.wifi.WifiChannelWidth
import io.github.dante_souza.yeyecatl.domain.wifi.WifiScanObservation
import io.github.dante_souza.yeyecatl.domain.wifi.WifiStandard
import org.junit.Assert.assertEquals
import org.junit.Test

class WifiObservationPresentationTest {
    @Test
    fun presentsOnlyNonRedundantRfSummaryInformation() {
        val presentation = WifiObservationPresenter.present(
            observation(
                ssid = "whanganui",
                bssid = "00:11:22:33:44:55",
                rssiDbm = -42,
                frequencyMhz = 2412,
                width = WifiChannelWidth.Mhz20,
                standard = WifiStandard.Ieee80211n,
                capabilities = "[WPA2-PSK-CCMP][ESS]"
            )
        )

        assertEquals("whanganui", presentation.ssidText)
        assertEquals("00:11:22:33:44:55", presentation.bssidText)
        assertEquals("-42 dBm", presentation.rssiText)
        assertEquals(
            "2.4 GHz • Ch 1 • 20 MHz • 802.11n",
            presentation.radioSummary
        )
        assertEquals(
            "Capabilities  [WPA2-PSK-CCMP][ESS]",
            presentation.capabilitiesSummary
        )
    }

    @Test
    fun omitsUnknownWifiStandardInsteadOfShowingPlaceholder() {
        val presentation = WifiObservationPresenter.present(
            observation(
                ssid = "Quarter",
                bssid = "84:0b:bb:43:68:e8",
                rssiDbm = -60,
                frequencyMhz = 2462,
                width = WifiChannelWidth.Mhz20,
                standard = WifiStandard.Unknown,
                capabilities = "[WPA2-PSK-CCMP][RSN-PSK-CCMP][ESS][WPS]"
            )
        )

        assertEquals("2.4 GHz • Ch 11 • 20 MHz", presentation.radioSummary)
    }

    @Test
    fun keepsHiddenAndUnavailableValuesExplicit() {
        val presentation = WifiObservationPresenter.present(
            WifiScanObservation(
                ssid = ObservedSsid(
                    displayText = null,
                    rawBytes = null,
                    isHidden = true
                ),
                bssid = null,
                rssiDbm = null,
                frequencyMhz = null,
                capabilities = null,
                platformTimestampMicros = null
            )
        )

        assertEquals("Hidden network", presentation.ssidText)
        assertEquals("Unavailable", presentation.bssidText)
        assertEquals("RSSI ?", presentation.rssiText)
        assertEquals(
            "Band unknown • Ch ? • Width ?",
            presentation.radioSummary
        )
        assertEquals("Capabilities  not reported", presentation.capabilitiesSummary)
    }

    private fun observation(
        ssid: String,
        bssid: String,
        rssiDbm: Int,
        frequencyMhz: Int,
        width: WifiChannelWidth,
        standard: WifiStandard,
        capabilities: String
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
            wifiStandard = standard,
            capabilities = capabilities,
            platformTimestampMicros = 1L
        )
}
