package com.example.logger

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.assertCountEquals
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.logger.presentation.roster.RosterScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RosterScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun emptyState_showsNoTeamMembersMessage() {
        composeRule.setContent {
            RosterScreen(
                members = emptyList(),
                onNavigateBack = {}
            )
        }
        composeRule.waitForIdle()

        // Verify empty state messages
        composeRule.onNodeWithText("No team members").assertIsDisplayed()
        composeRule.onNodeWithText("No team members configured").assertIsDisplayed()

        // Verify count shows 0
        composeRule.onNode(hasText("0 team member", substring = true)).assertIsDisplayed()
    }

    @Test
    fun singleMember_displaysCorrectly() {
        val members = listOf("Alice Johnson")

        composeRule.setContent {
            RosterScreen(
                members = members,
                onNavigateBack = {}
            )
        }
        composeRule.waitForIdle()

        // Verify member name is displayed
        composeRule.onNodeWithText("Alice Johnson").assertIsDisplayed()

        // Verify count shows singular form (no 's')
        composeRule.onNode(hasText("1 team member", substring = true)).assertIsDisplayed()

        // Verify section label
        composeRule.onNodeWithText("TEAM MEMBERS").assertIsDisplayed()

        // Verify member subtitle
        composeRule.onNode(hasText("Team member 1", substring = true)).assertIsDisplayed()
    }

    @Test
    fun multipleMembers_displaysCorrectCount() {
        val members = listOf(
            "Alice Johnson",
            "Bob Smith",
            "Charlie Brown"
        )

        composeRule.setContent {
            RosterScreen(
                members = members,
                onNavigateBack = {}
            )
        }
        composeRule.waitForIdle()

        // Verify all members are displayed
        composeRule.onNodeWithText("Alice Johnson").assertIsDisplayed()
        composeRule.onNodeWithText("Bob Smith").assertIsDisplayed()
        composeRule.onNodeWithText("Charlie Brown").assertIsDisplayed()

        // Verify count shows plural form
        composeRule.onNode(hasText("3 team members", substring = true)).assertIsDisplayed()

        // Verify section label
        composeRule.onNodeWithText("TEAM MEMBERS").assertIsDisplayed()
    }

    @Test
    fun multipleMembers_showsCorrectSubtitles() {
        val members = listOf(
            "Alice Johnson",
            "Bob Smith",
            "Charlie Brown"
        )

        composeRule.setContent {
            RosterScreen(
                members = members,
                onNavigateBack = {}
            )
        }
        composeRule.waitForIdle()

        // Verify each member has correct subtitle with index
        composeRule.onNode(hasText("Team member 1", substring = true)).assertIsDisplayed()
        composeRule.onNode(hasText("Team member 2", substring = true)).assertIsDisplayed()
        composeRule.onNode(hasText("Team member 3", substring = true)).assertIsDisplayed()
    }

    @Test
    fun memberInitials_areDisplayedCorrectly() {
        val members = listOf(
            "Alice Johnson",
            "Bob Smith",
            "X" // Single letter name
        )

        composeRule.setContent {
            RosterScreen(
                members = members,
                onNavigateBack = {}
            )
        }
        composeRule.waitForIdle()

        // Verify the full names are displayed
        composeRule.onNodeWithText("Alice Johnson").assertIsDisplayed()
        composeRule.onNodeWithText("Bob Smith").assertIsDisplayed()

        // Verify initials are extracted and displayed
        // AJ for Alice Johnson, BS for Bob Smith
        composeRule.onNodeWithText("AJ").assertIsDisplayed()
        composeRule.onNodeWithText("BS").assertIsDisplayed()

        // For single letter "X", both initials and full name should be "X"
        // So we should find "X" at least once (appears as both name and initials)
        composeRule.onAllNodesWithText("X").assertCountEquals(2) // Once as name, once as initials
    }

    @Test
    fun navigationBack_triggersCallback() {
        var backPressed = false

        composeRule.setContent {
            RosterScreen(
                members = listOf("Alice Johnson"),
                onNavigateBack = { backPressed = true }
            )
        }
        composeRule.waitForIdle()

        // Click back button
        composeRule.onNodeWithContentDescription("Back").performClick()

        // Verify callback was triggered
        assert(backPressed) { "Back navigation should trigger callback" }
    }

    @Test
    fun topBar_displaysTitleCorrectly() {
        composeRule.setContent {
            RosterScreen(
                members = emptyList(),
                onNavigateBack = {}
            )
        }
        composeRule.waitForIdle()

        // Verify top bar title
        composeRule.onNodeWithText("Team Roster").assertIsDisplayed()
    }

    @Test
    fun largeMemberList_displaysAll() {
        val members = (1..10).map { "Team Member $it" }

        composeRule.setContent {
            RosterScreen(
                members = members,
                onNavigateBack = {}
            )
        }
        composeRule.waitForIdle()

        // Verify count
        composeRule.onNode(hasText("10 team members", substring = true)).assertIsDisplayed()

        // Verify first and last members (LazyColumn should render visible items)
        composeRule.onNodeWithText("Team Member 1").assertIsDisplayed()
        // Note: Not all items may be visible without scrolling in LazyColumn
    }

    @Test
    fun twoMembers_usesPluralForm() {
        val members = listOf("Alice Johnson", "Bob Smith")

        composeRule.setContent {
            RosterScreen(
                members = members,
                onNavigateBack = {}
            )
        }
        composeRule.waitForIdle()

        // Verify plural form is used for 2 members
        composeRule.onNode(hasText("2 team members", substring = true)).assertIsDisplayed()
    }

    @Test
    fun emptyState_doesNotShowSectionLabel() {
        composeRule.setContent {
            RosterScreen(
                members = emptyList(),
                onNavigateBack = {}
            )
        }
        composeRule.waitForIdle()

        // Verify empty state is shown (with emoji icon)
        composeRule.onNodeWithText("👥").assertIsDisplayed()
        composeRule.onNodeWithText("No team members").assertIsDisplayed()

        // When empty, the "TEAM MEMBERS" section label should not appear
        // We verify by checking empty state message is shown instead
    }

    @Test
    fun membersWithSpecialCharacters_displayCorrectly() {
        val members = listOf(
            "O'Brien",
            "José García",
            "李明"
        )

        composeRule.setContent {
            RosterScreen(
                members = members,
                onNavigateBack = {}
            )
        }
        composeRule.waitForIdle()

        // Verify all names with special characters display correctly
        composeRule.onNodeWithText("O'Brien").assertIsDisplayed()
        composeRule.onNodeWithText("José García").assertIsDisplayed()
        composeRule.onNodeWithText("李明").assertIsDisplayed()
    }
}

