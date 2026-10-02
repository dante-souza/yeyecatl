package io.github.dante_souza.yeyecatl

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class YeyecatlAppTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun placeholderIdentifiesApplication() {
        composeRule.onNodeWithText("Yeyecatl").assertIsDisplayed()
        composeRule.onNodeWithText("Scan Wi-Fi").assertIsDisplayed()
    }
}
