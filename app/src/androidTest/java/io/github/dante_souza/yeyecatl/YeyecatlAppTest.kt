package io.github.dante_souza.yeyecatl

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
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
import io.github.dante_souza.yeyecatl.domain.wifi.WifiSignalSample
import io.github.dante_souza.yeyecatl.domain.wifi.WifiTemporalObservationHistory
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
    fun dynamicScanControlTogglesInHostState() = runComposeUiTest {
        setContent {
            var enabled by remember { mutableStateOf(false) }
            YeyecatlApp(
                dynamicScanEnabled = enabled,
                onToggleDynamicScan = { enabled = !enabled }
            )
        }

        onNodeWithText("Start dynamic scan").assertIsDisplayed()
        onNodeWithText("Start dynamic scan").performClick()
        onNodeWithText("Stop dynamic scan").assertIsDisplayed()
        onNodeWithText("Foreground cadence: every 30 seconds.", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun temporalCountersReportTrackedBssidsAndSamples() = runComposeUiTest {
        val firstSsid = ObservedSsid("mesh", null, false)
        val secondSsid = ObservedSsid("other", null, false)
        val history = WifiTemporalObservationHistory(
            samplesByBssid = mapOf(
                "00:00:00:00:00:01" to listOf(
                    WifiSignalSample(
                        bssid = "00:00:00:00:00:01",
                        ssid = firstSsid,
                        rssiDbm = -40,
                        frequencyMhz = 2412,
                        observedAtMillis = 1_000L
                    ),
                    WifiSignalSample(
                        bssid = "00:00:00:00:00:01",
                        ssid = firstSsid,
                        rssiDbm = -42,
                        frequencyMhz = 2412,
                        observedAtMillis = 2_000L
                    )
                ),
                "00:00:00:00:00:02" to listOf(
                    WifiSignalSample(
                        bssid = "00:00:00:00:00:02",
                        ssid = secondSsid,
                        rssiDbm = -65,
                        frequencyMhz = 2437,
                        observedAtMillis = 2_000L
                    )
                )
            )
        )

        setContent {
            YeyecatlApp(temporalHistory = history)
        }

        onNodeWithText("Tracked BSSIDs").assertIsDisplayed()
        onNodeWithText("2").assertIsDisplayed()
        onNodeWithText("Signal samples").assertIsDisplayed()
        onNodeWithText("3").assertIsDisplayed()
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
    fun spectrumSignalScopeFiltersLatestSnapshotWithoutChangingDiagnostics() = runComposeUiTest {
        val observations = (1..7).map { index ->
            observation(
                ssid = "network-$index",
                bssid = "00:00:00:00:00:${index.toString().padStart(2, '0')}",
                rssiDbm = -30 - index
            )
        }

        setContent {
            YeyecatlApp(scanState = resultsState(observations))
        }

        onNodeWithText("Spectrum filter").assertIsDisplayed()
        onNodeWithText("All").assertIsDisplayed()
        onNodeWithText("Strongest 5").assertIsDisplayed()
        onNodeWithText("Weakest 5").assertIsDisplayed()
        onNodeWithContentDescription(
            "2.4 GHz Wi-Fi spectrum chart with 7 observed access points"
        ).assertIsDisplayed()

        onNodeWithText("Strongest 5").performClick()
        onNodeWithContentDescription(
            "2.4 GHz Wi-Fi spectrum chart with 5 observed access points"
        ).assertIsDisplayed()

        onNodeWithText("Weakest 5").performClick()
        onNodeWithContentDescription(
            "2.4 GHz Wi-Fi spectrum chart with 5 observed access points"
        ).assertIsDisplayed()

        onNodeWithText("Observed networks").assertIsDisplayed()
        onNodeWithText("7").assertIsDisplayed()
        onNodeWithText("SSID: network-1", substring = true).assertIsDisplayed()
        onNodeWithText("SSID: network-7", substring = true).assertIsDisplayed()
    }

    @Test
    fun staticSignalRankingIsShownForLatestSnapshot() = runComposeUiTest {
        setContent {
            YeyecatlApp(
                scanState = resultsState(
                    listOf(
                        observation("strong", "00:00:00:00:00:01", -40),
                        observation("middle", "00:00:00:00:00:02", -65),
                        observation("weak", "00:00:00:00:00:03", -90)
                    )
                )
            )
        }

        onNodeWithText("Signal ranking").assertIsDisplayed()
        onNodeWithText("Strongest signals").assertIsDisplayed()
        onNodeWithText("Weakest signals").assertIsDisplayed()
        onNodeWithText("strong").assertIsDisplayed()
        onNodeWithText("weak").assertIsDisplayed()
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
        observation("whanganui", "00:00:00:00:00:01", -42)

    private fun observation(
        ssid: String,
        bssid: String,
        rssiDbm: Int
    ): WifiScanObservation =
        WifiScanObservation(
            ssid = ObservedSsid(
                displayText = ssid,
                rawBytes = null,
                isHidden = false
            ),
            bssid = bssid,
            rssiDbm = rssiDbm,
            frequencyMhz = 2412,
            channelWidth = WifiChannelWidth.Mhz20,
            capabilities = "[ESS]",
            platformTimestampMicros = 1L
        )
}
