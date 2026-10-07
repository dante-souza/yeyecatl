package io.github.dante_souza.yeyecatl.ui.networks

import io.github.dante_souza.yeyecatl.domain.wifi.ObservedSsid
import io.github.dante_souza.yeyecatl.domain.wifi.WifiChannelWidth
import io.github.dante_souza.yeyecatl.domain.wifi.WifiScanObservation
import io.github.dante_souza.yeyecatl.domain.wifi.WifiStandard
import org.junit.Assert.assertEquals
import org.junit.Test

class WifiObservationPresentationTest {
    @Test
    fun presentsNamedNetworkWithReadableRfHierarchy() {
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
            "2.4 GHz • Ch 1 • 2412 MHz • 20 MHz • 802.11n",
            presentation.radioSummary
        )
        assertEquals(
            "Center — • RF 2402–2422 MHz • Geometry complete",
            presentation.geometrySummary
        )
        assertEquals(
            "Capabilities  [WPA2-PSK-CCMP][ESS]",
            presentation.capabilitiesSummary
        )
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
            "Band unknown • Ch ? • Frequency ? • Width ? • Standard ?",
            presentation.radioSummary
        )
        assertEquals(
            "Center — • RF unavailable • Geometry unavailable",
            presentation.geometrySummary
        )
        assertEquals("Capabilities  not reported", presentation.capabilitiesSummary)
    }

    @Test
    fun presentsSplitChannelGeometryWithoutFlatteningSegments() {
        val presentation = WifiObservationPresenter.present(
            observation(
                ssid = "Backhaul",
                bssid = "00:11:22:33:55:80",
                rssiDbm = -70,
                frequencyMhz = 5180,
                width = WifiChannelWidth.Mhz80Plus80,
                center0Mhz = 5210,
                center1Mhz = 5530,
                standard = WifiStandard.Ieee80211ac,
                capabilities = "[ESS]"
            )
        )

        assertEquals(
            "Centers 5210 / 5530 MHz • RF 5170–5250 MHz / 5490–5570 MHz • Geometry complete",
            presentation.geometrySummary
        )
    }

    private fun observation(
        ssid: String,
        bssid: String,
        rssiDbm: Int,
        frequencyMhz: Int,
        width: WifiChannelWidth,
        center0Mhz: Int? = null,
        center1Mhz: Int? = null,
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
            centerFrequency0Mhz = center0Mhz,
            centerFrequency1Mhz = center1Mhz,
            wifiStandard = standard,
            capabilities = capabilities,
            platformTimestampMicros = 1L
        )
}
