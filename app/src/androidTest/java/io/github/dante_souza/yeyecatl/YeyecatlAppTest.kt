package io.github.dante_souza.yeyecatl

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.dante_souza.yeyecatl.domain.wifi.ObservedSsid
import io.github.dante_souza.yeyecatl.domain.wifi.WifiChannelWidth
import io.github.dante_souza.yeyecatl.domain.wifi.WifiScanFreshness
import io.github.dante_souza.yeyecatl.domain.wifi.WifiScanObservation
import io.github.dante_souza.yeyecatl.domain.wifi.WifiScanResultSource
import io.github.dante_souza.yeyecatl.domain.wifi.WifiScanSnapshot
import io.github.dante_souza.yeyecatl.domain.wifi.WifiScanState
import io.github.dante_souza.yeyecatl.ui.YeyecatlApp
import org.junit.Rule
import org.junit.Test

class YeyecatlAppTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun spectrumBandSelectorHandlesEmptyState() {
        composeRule.setContent {
            YeyecatlApp(scanState = resultsState(emptyList()))
        }

        composeRule.onNodeWithText("2.4 GHz").assertIsDisplayed()
        composeRule.onNodeWithText("5 GHz").assertIsDisplayed()
        composeRule.onNodeWithText("6 GHz").assertIsDisplayed()
        composeRule.onNodeWithText("No 2.4 GHz access points observed in the latest scan.")
            .assertIsDisplayed()

        composeRule.onNodeWithText("6 GHz").performClick()
        composeRule.onNodeWithText("No 6 GHz access points observed in the latest scan.")
            .assertIsDisplayed()
    }

    @Test
    fun diagnosticListRemainsAvailableWithSyntheticObservation() {
        composeRule.setContent {
            YeyecatlApp(scanState = resultsState(listOf(observation())))
        }

        composeRule.onNodeWithText("Observed networks").assertIsDisplayed()
        composeRule.onNodeWithText("Freshness").assertIsDisplayed()
        composeRule.onNodeWithText("SSID: whanganui", substring = true).assertIsDisplayed()
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
