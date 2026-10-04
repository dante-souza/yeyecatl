package io.github.dante_souza.yeyecatl

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
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
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class YeyecatlAppTest {
    @get:Rule
    val composeRule = createEmptyComposeRule()

    @Test
    fun appShellIdentifiesFieldAnalyzer() = withTestContent(
        content = { YeyecatlApp() }
    ) {
        composeRule.onNodeWithText("Yeyecatl").assertIsDisplayed()
        composeRule.onNodeWithText("Wi-Fi field analyzer").assertIsDisplayed()
        composeRule.onNodeWithText("Scanner").assertIsDisplayed()
        composeRule.onNodeWithText("Observation").assertIsDisplayed()
        composeRule.onNodeWithText("Scan Wi-Fi").assertIsDisplayed()
    }

    @Test
    fun spectrumBandSelectorHandlesEmptyState() = withTestContent(
        content = { YeyecatlApp(scanState = resultsState(emptyList())) }
    ) {
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
    fun diagnosticListRemainsAvailableWithSyntheticObservation() = withTestContent(
        content = { YeyecatlApp(scanState = resultsState(listOf(observation()))) }
    ) {
        composeRule.onNodeWithText("Observed networks").assertIsDisplayed()
        composeRule.onNodeWithText("Freshness").assertIsDisplayed()
        composeRule.onNodeWithText("SSID: whanganui", substring = true).assertIsDisplayed()
    }

    private fun withTestContent(
        content: @Composable () -> Unit,
        assertions: () -> Unit
    ) {
        val scenario = ActivityScenario.launch(ComponentActivity::class.java)
        try {
            scenario.onActivity { activity ->
                activity.setContent {
                    content()
                }
            }
            composeRule.waitForIdle()
            assertions()
        } finally {
            scenario.close()
        }
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
