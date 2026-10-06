package io.github.dante_souza.yeyecatl

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import io.github.dante_souza.yeyecatl.domain.wifi.ObservedSsid
import io.github.dante_souza.yeyecatl.domain.wifi.WifiChannelWidth
import io.github.dante_souza.yeyecatl.domain.wifi.WifiScanFreshness
import io.github.dante_souza.yeyecatl.domain.wifi.WifiScanObservation
import io.github.dante_souza.yeyecatl.domain.wifi.WifiScanResultSource
import io.github.dante_souza.yeyecatl.domain.wifi.WifiScanSnapshot
import io.github.dante_souza.yeyecatl.domain.wifi.WifiScanState
import io.github.dante_souza.yeyecatl.ui.YeyecatlApp
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
class YeyecatlAppTest {
    @Test
    fun appShellIdentifiesFieldAnalyzer() = runComposeUiTest {
        setContent {
            YeyecatlApp()
        }

        onNodeWithText("Yeyecatl").assertIsDisplayed()
        onNodeWithText("Wi-Fi field analyzer").assertIsDisplayed()
        onNodeWithText("Scanner").assertIsDisplayed()
        onNodeWithText("Observation").assertIsDisplayed()
        onNodeWithText("Scan Wi-Fi").assertIsDisplayed()
    }

    @Test
    fun spectrumBandSelectorHandlesEmptyState() = runComposeUiTest {
        setContent {
            YeyecatlApp(scanState = resultsState(emptyList()))
        }

        onNodeWithText("2.4 GHz").assertIsDisplayed()
        onNodeWithText("5 GHz").assertIsDisplayed()
        onNodeWithText("6 GHz").assertIsDisplayed()
        onNodeWithText("No 2.4 GHz access points observed in the latest scan.")
            .assertIsDisplayed()

        onNodeWithText("6 GHz").performClick()
        onNodeWithText("No 6 GHz access points observed in the latest scan.")
            .assertIsDisplayed()
    }

    @Test
    fun diagnosticListRemainsAvailableWithSyntheticObservation() = runComposeUiTest {
        setContent {
            YeyecatlApp(scanState = resultsState(listOf(observation())))
        }

        onNodeWithText("Observed networks").assertIsDisplayed()
        onNodeWithText("Freshness").assertIsDisplayed()
        onNodeWithText("SSID: whanganui", substring = true).assertIsDisplayed()
    }

    private fun resultsState(observations: List<WifiScanObservation>): WifiScanState =
        WifiScanState.Results(
            WifiScanSnapshot(
                observations = observations,
                freshness = WifiScanFreshness.Fresh,
                source = WifiScanResultSource.ApplicationRequest,
                resultsUpdated = true,
                receivedAtMillis = 0L
            )
        )

    private fun observation(): WifiScanObservation =
        WifiScanObservation(
            ssid = ObservedSsid(
                displayText = "whanganui",
                rawBytes = null,
                isHidden = false
            ),
            bssid = "00:00:00:00:00:01",
            rssiDbm = -42,
            frequencyMhz = 2412,
            channelWidth = WifiChannelWidth.Mhz20,
            capabilities = "[ESS]",
            platformTimestampMicros = 1L
        )
}
