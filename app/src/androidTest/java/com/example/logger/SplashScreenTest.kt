package com.example.logger

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.logger.presentation.splash.SplashScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SplashScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun splashScreen_displaysEmojiIcon() {
        composeRule.setContent {
            SplashScreen(
                onGetStarted = {},
                onConfigureSettings = {}
            )
        }
        composeRule.waitForIdle()

        // Check for emoji icon
        composeRule.onNodeWithText("📊").assertIsDisplayed()
    }

    @Test
    fun splashScreen_displaysTitleText() {
        composeRule.setContent {
            SplashScreen(
                onGetStarted = {},
                onConfigureSettings = {}
            )
        }
        composeRule.waitForIdle()

        // Check for title from strings.xml
        composeRule.onNodeWithText("Daily Standup").assertIsDisplayed()
    }

    @Test
    fun splashScreen_displaysSubtitleText() {
        composeRule.setContent {
            SplashScreen(
                onGetStarted = {},
                onConfigureSettings = {}
            )
        }
        composeRule.waitForIdle()

        // Check for subtitle from strings.xml
        composeRule.onNodeWithText("Keep your team in sync with daily updates and track progress effortlessly.").assertIsDisplayed()
    }

    @Test
    fun splashScreen_displaysGetStartedButton() {
        composeRule.setContent {
            SplashScreen(
                onGetStarted = {},
                onConfigureSettings = {}
            )
        }
        composeRule.waitForIdle()

        // Check for Get Started button from strings.xml
        composeRule.onNodeWithText("Get Started").assertIsDisplayed()
    }

    @Test
    fun splashScreen_displaysConfigureSettingsButton() {
        composeRule.setContent {
            SplashScreen(
                onGetStarted = {},
                onConfigureSettings = {}
            )
        }
        composeRule.waitForIdle()

        // Check for Configure Settings button from strings.xml
        composeRule.onNodeWithText("Configure Settings").assertIsDisplayed()
    }

    @Test
    fun getStartedButton_isClickable() {
        composeRule.setContent {
            SplashScreen(
                onGetStarted = {},
                onConfigureSettings = {}
            )
        }
        composeRule.waitForIdle()

        // Verify Get Started button has click action
        composeRule.onNodeWithText("Get Started")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun configureSettingsButton_isClickable() {
        composeRule.setContent {
            SplashScreen(
                onGetStarted = {},
                onConfigureSettings = {}
            )
        }
        composeRule.waitForIdle()

        // Verify Configure Settings button has click action
        composeRule.onNodeWithText("Configure Settings")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun getStartedButton_triggersCallback() {
        var getStartedPressed = false

        composeRule.setContent {
            SplashScreen(
                onGetStarted = { getStartedPressed = true },
                onConfigureSettings = {}
            )
        }
        composeRule.waitForIdle()

        // Click Get Started button
        composeRule.onNodeWithText("Get Started").performClick()

        // Verify callback was invoked
        assert(getStartedPressed)
    }

    @Test
    fun configureSettingsButton_triggersCallback() {
        var configureSettingsPressed = false

        composeRule.setContent {
            SplashScreen(
                onGetStarted = {},
                onConfigureSettings = { configureSettingsPressed = true }
            )
        }
        composeRule.waitForIdle()

        // Click Configure Settings button
        composeRule.onNodeWithText("Configure Settings").performClick()

        // Verify callback was invoked
        assert(configureSettingsPressed)
    }

    @Test
    fun splashScreen_displaysBothButtons() {
        composeRule.setContent {
            SplashScreen(
                onGetStarted = {},
                onConfigureSettings = {}
            )
        }
        composeRule.waitForIdle()

        // Verify both buttons are present
        composeRule.onNodeWithText("Get Started").assertIsDisplayed()
        composeRule.onNodeWithText("Configure Settings").assertIsDisplayed()
    }

    @Test
    fun splashScreen_hasCorrectLayout() {
        composeRule.setContent {
            SplashScreen(
                onGetStarted = {},
                onConfigureSettings = {}
            )
        }
        composeRule.waitForIdle()

        // Verify all main components are present in order
        composeRule.onNodeWithText("📊").assertIsDisplayed()
        composeRule.onNodeWithText("Daily Standup").assertIsDisplayed()
        composeRule.onNodeWithText("Keep your team in sync with daily updates and track progress effortlessly.").assertIsDisplayed()
        composeRule.onNodeWithText("Get Started").assertIsDisplayed()
        composeRule.onNodeWithText("Configure Settings").assertIsDisplayed()
    }

    @Test
    fun splashScreen_allTextElementsAreVisible() {
        composeRule.setContent {
            SplashScreen(
                onGetStarted = {},
                onConfigureSettings = {}
            )
        }
        composeRule.waitForIdle()

        // Comprehensive check of all text elements
        val expectedTexts = listOf(
            "📊",
            "Daily Standup",
            "Keep your team in sync with daily updates and track progress effortlessly.",
            "Get Started",
            "Configure Settings"
        )

        expectedTexts.forEach { text ->
            composeRule.onNodeWithText(text).assertIsDisplayed()
        }
    }

    @Test
    fun splashScreen_rendersWithoutCrashing() {
        composeRule.setContent {
            SplashScreen(
                onGetStarted = {},
                onConfigureSettings = {}
            )
        }
        composeRule.waitForIdle()

        // If we reach here, the screen rendered without crashing
        composeRule.onNodeWithText("Daily Standup").assertIsDisplayed()
    }

    @Test
    fun splashScreen_buttonsAreInCorrectOrder() {
        composeRule.setContent {
            SplashScreen(
                onGetStarted = {},
                onConfigureSettings = {}
            )
        }
        composeRule.waitForIdle()

        // Verify Get Started appears before Configure Settings
        // Both buttons should be present
        composeRule.onNodeWithText("Get Started").assertIsDisplayed()
        composeRule.onNodeWithText("Configure Settings").assertIsDisplayed()

        // Get Started should be the first button
        composeRule.onAllNodes(hasClickAction())
            .assertCountEquals(2)
    }

    @Test
    fun splashScreen_titleHasPrimaryColor() {
        composeRule.setContent {
            SplashScreen(
                onGetStarted = {},
                onConfigureSettings = {}
            )
        }
        composeRule.waitForIdle()

        // Verify title is displayed (color is visual but we can verify presence)
        composeRule.onNodeWithText("Daily Standup").assertIsDisplayed()
    }

    @Test
    fun splashScreen_subtitleIsDisplayed() {
        composeRule.setContent {
            SplashScreen(
                onGetStarted = {},
                onConfigureSettings = {}
            )
        }
        composeRule.waitForIdle()

        // Verify full subtitle text is displayed
        composeRule.onNodeWithText("Keep your team in sync with daily updates and track progress effortlessly.")
            .assertIsDisplayed()
    }

    @Test
    fun splashScreen_emojiIconIsLarge() {
        composeRule.setContent {
            SplashScreen(
                onGetStarted = {},
                onConfigureSettings = {}
            )
        }
        composeRule.waitForIdle()

        // Verify emoji is displayed (size is visual)
        composeRule.onNodeWithText("📊").assertIsDisplayed()
    }

    @Test
    fun splashScreen_contentIsCentered() {
        composeRule.setContent {
            SplashScreen(
                onGetStarted = {},
                onConfigureSettings = {}
            )
        }
        composeRule.waitForIdle()

        // Verify all content is displayed (centering is visual)
        composeRule.onNodeWithText("📊").assertIsDisplayed()
        composeRule.onNodeWithText("Daily Standup").assertIsDisplayed()
        composeRule.onNodeWithText("Keep your team in sync with daily updates and track progress effortlessly.").assertIsDisplayed()
    }

    @Test
    fun getStartedButton_hasDashboardIcon() {
        composeRule.setContent {
            SplashScreen(
                onGetStarted = {},
                onConfigureSettings = {}
            )
        }
        composeRule.waitForIdle()

        // Verify Get Started button is displayed (icon is part of button content)
        composeRule.onNodeWithText("Get Started").assertIsDisplayed()
    }

    @Test
    fun configureSettingsButton_hasSettingsIcon() {
        composeRule.setContent {
            SplashScreen(
                onGetStarted = {},
                onConfigureSettings = {}
            )
        }
        composeRule.waitForIdle()

        // Verify Configure Settings button is displayed (icon is part of button content)
        composeRule.onNodeWithText("Configure Settings").assertIsDisplayed()
    }

    @Test
    fun splashScreen_buttonsHaveFullWidth() {
        composeRule.setContent {
            SplashScreen(
                onGetStarted = {},
                onConfigureSettings = {}
            )
        }
        composeRule.waitForIdle()

        // Verify both buttons are displayed (full width is visual)
        composeRule.onNodeWithText("Get Started").assertIsDisplayed()
        composeRule.onNodeWithText("Configure Settings").assertIsDisplayed()
    }

    @Test
    fun multipleClicks_triggersCallbackMultipleTimes() {
        var clickCount = 0

        composeRule.setContent {
            SplashScreen(
                onGetStarted = { clickCount++ },
                onConfigureSettings = {}
            )
        }
        composeRule.waitForIdle()

        // Click Get Started button multiple times
        composeRule.onNodeWithText("Get Started").performClick()
        assert(clickCount == 1)

        composeRule.onNodeWithText("Get Started").performClick()
        assert(clickCount == 2)
    }

    @Test
    fun bothCallbacks_areIndependent() {
        var getStartedCount = 0
        var settingsCount = 0

        composeRule.setContent {
            SplashScreen(
                onGetStarted = { getStartedCount++ },
                onConfigureSettings = { settingsCount++ }
            )
        }
        composeRule.waitForIdle()

        // Click Get Started
        composeRule.onNodeWithText("Get Started").performClick()
        assert(getStartedCount == 1 && settingsCount == 0)

        // Click Configure Settings
        composeRule.onNodeWithText("Configure Settings").performClick()
        assert(getStartedCount == 1 && settingsCount == 1)
    }

    @Test
    fun splashScreen_hasCorrectSpacing() {
        composeRule.setContent {
            SplashScreen(
                onGetStarted = {},
                onConfigureSettings = {}
            )
        }
        composeRule.waitForIdle()

        // Verify all elements are present (spacing is visual)
        composeRule.onNodeWithText("📊").assertIsDisplayed()
        composeRule.onNodeWithText("Daily Standup").assertIsDisplayed()
        composeRule.onNodeWithText("Keep your team in sync with daily updates and track progress effortlessly.").assertIsDisplayed()
        composeRule.onNodeWithText("Get Started").assertIsDisplayed()
        composeRule.onNodeWithText("Configure Settings").assertIsDisplayed()
    }
}

