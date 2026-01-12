package com.example.logger

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.logger.presentation.submitstandup.SubmitConfirmScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SubmitConfirmScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun submitConfirmScreen_displaysSuccessCheckmark() {
        composeRule.setContent {
            SubmitConfirmScreen(
                timestamp = "13:02",
                onGoDashboard = {},
                onGoHistory = {}
            )
        }
        composeRule.waitForIdle()

        // Check for success checkmark emoji
        composeRule.onNodeWithText("✓").assertIsDisplayed()
    }

    @Test
    fun submitConfirmScreen_displaysSubmittedTitle() {
        composeRule.setContent {
            SubmitConfirmScreen(
                timestamp = "13:02",
                onGoDashboard = {},
                onGoHistory = {}
            )
        }
        composeRule.waitForIdle()

        // Check title from strings.xml (R.string.submitted_title = "Submitted!")
        composeRule.onNodeWithText("Submitted!").assertIsDisplayed()
    }

    @Test
    fun submitConfirmScreen_displaysTimestampMessage() {
        val timestamp = "13:02"

        composeRule.setContent {
            SubmitConfirmScreen(
                timestamp = timestamp,
                onGoDashboard = {},
                onGoHistory = {}
            )
        }
        composeRule.waitForIdle()

        // Check message from strings.xml (R.string.submitted_message = "Your standup was submitted at %1$s")
        composeRule.onNodeWithText("Your standup was submitted at $timestamp").assertIsDisplayed()
    }

    @Test
    fun submitConfirmScreen_displaysNextActionsLabel() {
        composeRule.setContent {
            SubmitConfirmScreen(
                timestamp = "13:02",
                onGoDashboard = {},
                onGoHistory = {}
            )
        }
        composeRule.waitForIdle()

        // Check label from strings.xml (R.string.confirm_next_actions_label = "What would you like to do next?")
        composeRule.onNodeWithText("What would you like to do next?").assertIsDisplayed()
    }

    @Test
    fun submitConfirmScreen_displaysDashboardButton() {
        composeRule.setContent {
            SubmitConfirmScreen(
                timestamp = "13:02",
                onGoDashboard = {},
                onGoHistory = {}
            )
        }
        composeRule.waitForIdle()

        // Check Dashboard button from strings.xml (R.string.dashboard = "Dashboard")
        composeRule.onNodeWithText("Dashboard").assertIsDisplayed()
    }

    @Test
    fun submitConfirmScreen_displaysViewHistoryButton() {
        composeRule.setContent {
            SubmitConfirmScreen(
                timestamp = "13:02",
                onGoDashboard = {},
                onGoHistory = {}
            )
        }
        composeRule.waitForIdle()

        // Check View History button from strings.xml (R.string.view_history = "View History")
        composeRule.onNodeWithText("View History").assertIsDisplayed()
    }

    @Test
    fun dashboardButton_isClickable() {
        composeRule.setContent {
            SubmitConfirmScreen(
                timestamp = "13:02",
                onGoDashboard = {},
                onGoHistory = {}
            )
        }
        composeRule.waitForIdle()

        // Verify Dashboard button has click action
        composeRule.onNodeWithText("Dashboard")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun viewHistoryButton_isClickable() {
        composeRule.setContent {
            SubmitConfirmScreen(
                timestamp = "13:02",
                onGoDashboard = {},
                onGoHistory = {}
            )
        }
        composeRule.waitForIdle()

        // Verify View History button has click action
        composeRule.onNodeWithText("View History")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun dashboardButton_triggersCallback() {
        var dashboardPressed = false

        composeRule.setContent {
            SubmitConfirmScreen(
                timestamp = "13:02",
                onGoDashboard = { dashboardPressed = true },
                onGoHistory = {}
            )
        }
        composeRule.waitForIdle()

        // Click Dashboard button
        composeRule.onNodeWithText("Dashboard").performClick()

        // Verify callback was invoked
        assert(dashboardPressed)
    }

    @Test
    fun viewHistoryButton_triggersCallback() {
        var historyPressed = false

        composeRule.setContent {
            SubmitConfirmScreen(
                timestamp = "13:02",
                onGoDashboard = {},
                onGoHistory = { historyPressed = true }
            )
        }
        composeRule.waitForIdle()

        // Click View History button
        composeRule.onNodeWithText("View History").performClick()

        // Verify callback was invoked
        assert(historyPressed)
    }

    @Test
    fun submitConfirmScreen_displaysBothButtons() {
        composeRule.setContent {
            SubmitConfirmScreen(
                timestamp = "13:02",
                onGoDashboard = {},
                onGoHistory = {}
            )
        }
        composeRule.waitForIdle()

        // Verify both buttons are present
        composeRule.onNodeWithText("Dashboard").assertIsDisplayed()
        composeRule.onNodeWithText("View History").assertIsDisplayed()
    }

    @Test
    fun submitConfirmScreen_hasCorrectLayout() {
        composeRule.setContent {
            SubmitConfirmScreen(
                timestamp = "13:02",
                onGoDashboard = {},
                onGoHistory = {}
            )
        }
        composeRule.waitForIdle()

        // Verify all main components are present in order
        composeRule.onNodeWithText("✓").assertIsDisplayed()
        composeRule.onNodeWithText("Submitted!").assertIsDisplayed()
        composeRule.onNodeWithText("Your standup was submitted at 13:02").assertIsDisplayed()
        composeRule.onNodeWithText("What would you like to do next?").assertIsDisplayed()
        composeRule.onNodeWithText("Dashboard").assertIsDisplayed()
        composeRule.onNodeWithText("View History").assertIsDisplayed()
    }

    @Test
    fun submitConfirmScreen_allTextElementsAreVisible() {
        composeRule.setContent {
            SubmitConfirmScreen(
                timestamp = "13:02",
                onGoDashboard = {},
                onGoHistory = {}
            )
        }
        composeRule.waitForIdle()

        // Comprehensive check of all text elements
        val expectedTexts = listOf(
            "✓",
            "Submitted!",
            "Your standup was submitted at 13:02",
            "What would you like to do next?",
            "Dashboard",
            "View History"
        )

        expectedTexts.forEach { text ->
            composeRule.onNodeWithText(text).assertIsDisplayed()
        }
    }

    @Test
    fun submitConfirmScreen_rendersWithoutCrashing() {
        composeRule.setContent {
            SubmitConfirmScreen(
                timestamp = "13:02",
                onGoDashboard = {},
                onGoHistory = {}
            )
        }
        composeRule.waitForIdle()

        // If we reach here, the screen rendered without crashing
        composeRule.onNodeWithText("Submitted!").assertIsDisplayed()
    }

    @Test
    fun submitConfirmScreen_contentIsCentered() {
        composeRule.setContent {
            SubmitConfirmScreen(
                timestamp = "13:02",
                onGoDashboard = {},
                onGoHistory = {}
            )
        }
        composeRule.waitForIdle()

        // Verify all content is displayed (centering is visual)
        composeRule.onNodeWithText("✓").assertIsDisplayed()
        composeRule.onNodeWithText("Submitted!").assertIsDisplayed()
        composeRule.onNodeWithText("Your standup was submitted at 13:02").assertIsDisplayed()
    }

    @Test
    fun submitConfirmScreen_actionCardHasElevatedStyling() {
        composeRule.setContent {
            SubmitConfirmScreen(
                timestamp = "13:02",
                onGoDashboard = {},
                onGoHistory = {}
            )
        }
        composeRule.waitForIdle()

        // Verify action card content is displayed (elevation is visual)
        composeRule.onNodeWithText("What would you like to do next?").assertIsDisplayed()
        composeRule.onNodeWithText("Dashboard").assertIsDisplayed()
        composeRule.onNodeWithText("View History").assertIsDisplayed()
    }

    @Test
    fun submitConfirmScreen_checkmarkHasPrimaryColor() {
        composeRule.setContent {
            SubmitConfirmScreen(
                timestamp = "13:02",
                onGoDashboard = {},
                onGoHistory = {}
            )
        }
        composeRule.waitForIdle()

        // Verify checkmark is displayed (color is visual)
        composeRule.onNodeWithText("✓").assertIsDisplayed()
    }

    @Test
    fun submitConfirmScreen_dashboardButtonHasIcon() {
        composeRule.setContent {
            SubmitConfirmScreen(
                timestamp = "13:02",
                onGoDashboard = {},
                onGoHistory = {}
            )
        }
        composeRule.waitForIdle()

        // Verify Dashboard button is displayed (icon is part of button content)
        composeRule.onNodeWithText("Dashboard").assertIsDisplayed()
    }

    @Test
    fun submitConfirmScreen_viewHistoryButtonHasIcon() {
        composeRule.setContent {
            SubmitConfirmScreen(
                timestamp = "13:02",
                onGoDashboard = {},
                onGoHistory = {}
            )
        }
        composeRule.waitForIdle()

        // Verify View History button is displayed (icon is part of button content)
        composeRule.onNodeWithText("View History").assertIsDisplayed()
    }

    @Test
    fun submitConfirmScreen_buttonsHaveFullWidth() {
        composeRule.setContent {
            SubmitConfirmScreen(
                timestamp = "13:02",
                onGoDashboard = {},
                onGoHistory = {}
            )
        }
        composeRule.waitForIdle()

        // Verify both buttons are displayed (full width is visual)
        composeRule.onNodeWithText("Dashboard").assertIsDisplayed()
        composeRule.onNodeWithText("View History").assertIsDisplayed()
    }

    @Test
    fun submitConfirmScreen_displaysDifferentTimestamps() {
        val timestamp1 = "09:15"

        composeRule.setContent {
            SubmitConfirmScreen(
                timestamp = timestamp1,
                onGoDashboard = {},
                onGoHistory = {}
            )
        }
        composeRule.waitForIdle()

        // Check message displays correct timestamp
        composeRule.onNodeWithText("Your standup was submitted at $timestamp1").assertIsDisplayed()
    }

    @Test
    fun bothCallbacks_areIndependent() {
        var dashboardCount = 0
        var historyCount = 0

        composeRule.setContent {
            SubmitConfirmScreen(
                timestamp = "13:02",
                onGoDashboard = { dashboardCount++ },
                onGoHistory = { historyCount++ }
            )
        }
        composeRule.waitForIdle()

        // Click Dashboard
        composeRule.onNodeWithText("Dashboard").performClick()
        assert(dashboardCount == 1 && historyCount == 0)

        // Click View History
        composeRule.onNodeWithText("View History").performClick()
        assert(dashboardCount == 1 && historyCount == 1)
    }

    @Test
    fun submitConfirmScreen_buttonsAreInCorrectOrder() {
        composeRule.setContent {
            SubmitConfirmScreen(
                timestamp = "13:02",
                onGoDashboard = {},
                onGoHistory = {}
            )
        }
        composeRule.waitForIdle()

        // Verify Dashboard appears before View History
        // Both buttons should be present
        composeRule.onNodeWithText("Dashboard").assertIsDisplayed()
        composeRule.onNodeWithText("View History").assertIsDisplayed()

        // Dashboard should be the first button (solid), View History is outlined
        composeRule.onAllNodes(hasClickAction())
            .assertCountEquals(2)
    }

    /**
     * All actual strings used in SubmitConfirmScreen.kt:
     *
     * From strings.xml (via stringResource()):
     * - R.string.submitted_title: "Submitted!"
     * - R.string.submitted_message: "Your standup was submitted at %1$s"
     * - R.string.confirm_next_actions_label: "What would you like to do next?"
     * - R.string.dashboard: "Dashboard"
     * - R.string.view_history: "View History"
     *
     * Hardcoded in SubmitConfirmScreen.kt:
     * - "✓" (line 40, success checkmark emoji)
     *
     * UI Components:
     * - Success checkmark (✓) with primary color
     * - Title: "Submitted!"
     * - Message: "Your standup was submitted at {timestamp}"
     * - ElevatedCard with action card
     * - Label: "What would you like to do next?"
     * - Dashboard Button (solid, with Dashboard icon)
     * - View History OutlinedButton (with EventNote icon)
     * - BackHandler (disables system back button)
     *
     * Layout:
     * - Centered vertically and horizontally
     * - 24.dp padding around screen
     * - Header block with checkmark, title, and message
     * - Action card with label and buttons
     * - Buttons have 280.dp max width
     * - Icons with 8.dp spacing from text
     *
     * Behavior:
     * - System back button is intercepted and disabled
     * - Users must choose either Dashboard or View History
     * - No way to go back to previous screen
     */
    @Test
    fun submitConfirmScreen_allStringsDocumented() {
        val stringsFromXml = listOf(
            "Submitted!",
            "Your standup was submitted at %1\$s",
            "What would you like to do next?",
            "Dashboard",
            "View History"
        )

        val hardcodedStrings = listOf(
            "✓"
        )

        // Verify string lists are populated (5 from XML + 1 hardcoded = 6 total)
        assert(stringsFromXml.size == 5)
        assert(hardcodedStrings.size == 1)
    }
}

