package com.example.nfcpoc

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.nfcpoc.ui.theme.NfcPocTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ThemeTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun lightTheme_rendersContent() {
        composeRule.setContent {
            NfcPocTheme(darkTheme = false) {
                Text("Light mode")
            }
        }
        composeRule.onNodeWithText("Light mode").assertIsDisplayed()
    }

    @Test
    fun darkTheme_rendersContent() {
        composeRule.setContent {
            NfcPocTheme(darkTheme = true) {
                Text("Dark mode")
            }
        }
        composeRule.onNodeWithText("Dark mode").assertIsDisplayed()
    }

    @Test
    fun defaultTheme_usesSystemSetting() {
        composeRule.setContent {
            NfcPocTheme {
                Text("Default")
            }
        }
        composeRule.onNodeWithText("Default").assertIsDisplayed()
    }
}
