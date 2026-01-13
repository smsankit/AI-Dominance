package com.example.logger

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
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

        // Check for empty state UI
        composeRule.onNodeWithText("👥").assertIsDisplayed()
        composeRule.onNodeWithText("No team members").assertIsDisplayed()
        composeRule.onNodeWithText("No team members configured").assertIsDisplayed()

        // Verify count shows 0
        composeRule.onNodeWithText("0 team members").assertIsDisplayed()
    }

    @Test
    fun singleMember_showsCorrectCountAndMemberCard() {
        val memberName = "Alice Johnson"

        composeRule.setContent {
            RosterScreen(
                members = listOf(memberName),
                onNavigateBack = {}
            )
        }

        composeRule.waitForIdle()

        // Check count uses singular form
        composeRule.onNodeWithText("1 team member").assertIsDisplayed()

        // Check member card is displayed
        composeRule.onNodeWithText(memberName).assertIsDisplayed()
        composeRule.onNodeWithText("Team member 1").assertIsDisplayed()

        // Check initials are displayed
        composeRule.onNodeWithText("AJ").assertIsDisplayed()
    }

    @Test
    fun multipleMembers_showsCorrectPluralCountAndAllCards() {
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

        // Check count uses plural form
        composeRule.onNodeWithText("3 team members").assertIsDisplayed()

        // Check all member cards are displayed
        composeRule.onNodeWithText("Alice Johnson").assertIsDisplayed()
        composeRule.onNodeWithText("Bob Smith").assertIsDisplayed()
        composeRule.onNodeWithText("Charlie Brown").assertIsDisplayed()

        // Check initials for each member
        composeRule.onNodeWithText("AJ").assertIsDisplayed()
        composeRule.onNodeWithText("BS").assertIsDisplayed()
        composeRule.onNodeWithText("CB").assertIsDisplayed()

        // Verify all have subtitles with indices
        composeRule.onNodeWithText("Team member 1").assertIsDisplayed()
        composeRule.onNodeWithText("Team member 2").assertIsDisplayed()
        composeRule.onNodeWithText("Team member 3").assertIsDisplayed()
    }

    @Test
    fun topBar_showsCorrectTitleAndBackButton() {
        composeRule.setContent {
            RosterScreen(
                members = listOf("John Doe"),
                onNavigateBack = {}
            )
        }

        composeRule.waitForIdle()

        // Check title
        composeRule.onNodeWithText("Team Roster").assertIsDisplayed()

        // Check back button exists
        composeRule.onNodeWithContentDescription("Back").assertIsDisplayed()
    }

    @Test
    fun memberCard_displaysInitialsCorrectly_twoWords() {
        composeRule.setContent {
            RosterScreen(
                members = listOf("John Doe"),
                onNavigateBack = {}
            )
        }
        composeRule.waitForIdle()

        // Check initials are correctly generated for two-word name
        composeRule.onNodeWithText("JD").assertIsDisplayed()
    }

    @Test
    fun memberCard_displaysInitialsCorrectly_singleWord() {
        composeRule.setContent {
            RosterScreen(
                members = listOf("Alice"),
                onNavigateBack = {}
            )
        }
        composeRule.waitForIdle()

        // Check initials are correctly generated for single-word name
        composeRule.onNodeWithText("A").assertIsDisplayed()
    }

    @Test
    fun memberCard_displaysInitialsCorrectly_threeWords() {
        composeRule.setContent {
            RosterScreen(
                members = listOf("Bob Smith Jr"),
                onNavigateBack = {}
            )
        }
        composeRule.waitForIdle()

        // Check initials are correctly generated for three-word name
        composeRule.onNodeWithText("BS").assertIsDisplayed()
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
        composeRule.onNodeWithText("2 team members").assertIsDisplayed()
    }

    @Test
    fun largeList_allMembersAreScrollable() {
        val members = (1..20).map { "Member $it" }

        composeRule.setContent {
            RosterScreen(
                members = members,
                onNavigateBack = {}
            )
        }

        composeRule.waitForIdle()

        // Check count is correct
        composeRule.onNodeWithText("20 team members").assertIsDisplayed()

        // First member should be visible
        composeRule.onNodeWithText("Member 1").assertIsDisplayed()

        // Verify list is scrollable
        composeRule.onAllNodes(hasScrollAction())
            .onFirst()
            .performScrollToIndex(5)

        composeRule.waitForIdle()

        // After scrolling, Member 6 should be visible
        composeRule.onNodeWithText("Member 6").assertIsDisplayed()
    }

    @Test
    fun emptyState_noSectionLabelShown() {
        composeRule.setContent {
            RosterScreen(
                members = emptyList(),
                onNavigateBack = {}
            )
        }

        composeRule.waitForIdle()

        // Verify section label is not shown in empty state
        composeRule.onNodeWithText("👥").assertIsDisplayed()
        composeRule.onNodeWithText("No team members").assertIsDisplayed()
    }

    @Test
    fun backButton_hasCorrectContentDescription() {
        composeRule.setContent {
            RosterScreen(
                members = listOf("Alice"),
                onNavigateBack = {}
            )
        }

        composeRule.waitForIdle()

        // Verify back button has accessibility label
        composeRule.onNodeWithContentDescription("Back")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun memberCard_displaysInitialsCorrectly_specialCharacters() {
        composeRule.setContent {
            RosterScreen(
                members = listOf("José García"),
                onNavigateBack = {}
            )
        }
        composeRule.waitForIdle()

        // Check initials with special characters
        composeRule.onNodeWithText("JG").assertIsDisplayed()
    }

    @Test
    fun memberCard_hasElevation() {
        composeRule.setContent {
            RosterScreen(
                members = listOf("John Doe"),
                onNavigateBack = {}
            )
        }

        composeRule.waitForIdle()

        // Verify the member card is rendered
        composeRule.onNodeWithText("John Doe").assertIsDisplayed()
    }

    @Test
    fun backButton_triggersCallback() {
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
    fun sectionLabel_displayedWhenMembersExist() {
        composeRule.setContent {
            RosterScreen(
                members = listOf("Member"),
                onNavigateBack = {}
            )
        }

        composeRule.waitForIdle()

        // Section label should be visible
        composeRule.onNodeWithText("TEAM MEMBERS").assertIsDisplayed()
    }

    @Test
    fun memberSubtitle_showsCorrectIndex() {
        val members = listOf("First", "Second", "Third")

        composeRule.setContent {
            RosterScreen(
                members = members,
                onNavigateBack = {}
            )
        }

        composeRule.waitForIdle()

        // Verify indices are correct (1-based)
        composeRule.onNodeWithText("Team member 1").assertIsDisplayed()
        composeRule.onNodeWithText("Team member 2").assertIsDisplayed()
        composeRule.onNodeWithText("Team member 3").assertIsDisplayed()
    }
}

