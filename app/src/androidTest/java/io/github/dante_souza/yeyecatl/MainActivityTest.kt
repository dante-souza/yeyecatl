package io.github.dante_souza.yeyecatl

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class MainActivityTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appShellIdentifiesFieldAnalyzer() {
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Yeyecatl").assertIsDisplayed()
        composeRule.onNodeWithText("Wi-Fi field analyzer").assertIsDisplayed()
        composeRule.onNodeWithText("Scanner").assertIsDisplayed()
        composeRule.onNodeWithText("Observation").assertIsDisplayed()
        composeRule.onNodeWithText("Scan Wi-Fi").assertIsDisplayed()
    }
}
