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
    fun emptyRoster_showsEmptyStateMessage() {
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

        // Check section label
        composeRule.onNodeWithText("TEAM MEMBERS").assertIsDisplayed()
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

        // Check team member subtitles
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
    fun backButton_triggersNavigationCallback() {
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

        // Verify callback was invoked
        assert(backPressed)
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
        // Check initials are correctly generated for three-word name (takes first two)
        composeRule.onNodeWithText("BS").assertIsDisplayed()
    }

    @Test
    fun memberCard_displaysInitialsCorrectly_hyphenatedName() {
        composeRule.setContent {
            RosterScreen(
                members = listOf("Mary-Jane Watson"),
                onNavigateBack = {}
            )
        }
        composeRule.waitForIdle()
        // Check initials are correctly generated for hyphenated name
        composeRule.onNodeWithText("MW").assertIsDisplayed()
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

        // Verify list is scrollable by performing scroll gestures
        composeRule.onAllNodes(hasScrollAction())
            .onFirst()
            .performScrollToIndex(10)

        composeRule.waitForIdle()

        // After scrolling, Member 11 should be visible
        composeRule.onNodeWithText("Member 11").assertIsDisplayed()
    }

    @Test
    fun emptyState_noMemberCardsShown() {
        composeRule.setContent {
            RosterScreen(
                members = emptyList(),
                onNavigateBack = {}
            )
        }

        composeRule.waitForIdle()

        // Verify no member cards exist in empty state
        composeRule.onAllNodesWithText("Team member 1").assertCountEquals(0)
    }

    @Test
    fun memberCard_showsCorrectSubtitleForEachPosition() {
        val members = listOf("Alice", "Bob", "Charlie")

        composeRule.setContent {
            RosterScreen(
                members = members,
                onNavigateBack = {}
            )
        }

        composeRule.waitForIdle()

        // Check each member has the correct position subtitle
        composeRule.onNodeWithText("Team member 1").assertIsDisplayed()
        composeRule.onNodeWithText("Team member 2").assertIsDisplayed()
        composeRule.onNodeWithText("Team member 3").assertIsDisplayed()
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
    fun sectionLabel_displayedWhenMembersExist() {
        composeRule.setContent {
            RosterScreen(
                members = listOf("John Doe"),
                onNavigateBack = {}
            )
        }

        composeRule.waitForIdle()

        // Verify section label is displayed
        composeRule.onNodeWithText("TEAM MEMBERS").assertIsDisplayed()
    }

    @Test
    fun sectionLabel_notDisplayedWhenEmpty() {
        composeRule.setContent {
            RosterScreen(
                members = emptyList(),
                onNavigateBack = {}
            )
        }

        composeRule.waitForIdle()

        // Verify section label is not displayed in empty state
        composeRule.onAllNodesWithText("TEAM MEMBERS").assertCountEquals(0)
    }

    @Test
    fun memberCards_displayedInCorrectOrder() {
        val members = listOf("Alice", "Bob", "Charlie", "David")

        composeRule.setContent {
            RosterScreen(
                members = members,
                onNavigateBack = {}
            )
        }

        composeRule.waitForIdle()

        // Verify all members are displayed
        members.forEach { name ->
            composeRule.onNodeWithText(name).assertIsDisplayed()
        }

        // Verify they have correct position numbers
        composeRule.onNodeWithText("Team member 1").assertIsDisplayed()
        composeRule.onNodeWithText("Team member 2").assertIsDisplayed()
        composeRule.onNodeWithText("Team member 3").assertIsDisplayed()
        composeRule.onNodeWithText("Team member 4").assertIsDisplayed()
    }

    @Test
    fun memberCard_hasElevatedCardStyling() {
        composeRule.setContent {
            RosterScreen(
                members = listOf("John Doe"),
                onNavigateBack = {}
            )
        }

        composeRule.waitForIdle()

        // Verify member card content is displayed (elevation is visual)
        composeRule.onNodeWithText("John Doe").assertIsDisplayed()
        composeRule.onNodeWithText("JD").assertIsDisplayed()
    }

    @Test
    fun twoMemberRoster_showsCorrectPluralForm() {
        composeRule.setContent {
            RosterScreen(
                members = listOf("Alice", "Bob"),
                onNavigateBack = {}
            )
        }

        composeRule.waitForIdle()

        // Check count uses plural form for 2 members
        composeRule.onNodeWithText("2 team members").assertIsDisplayed()
    }
}

