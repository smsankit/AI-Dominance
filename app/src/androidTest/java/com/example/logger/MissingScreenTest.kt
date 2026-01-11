package com.example.logger

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.logger.presentation.missing.MissingScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MissingScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun emptyState_showsNoMissingMembersMessage() {
        composeRule.setContent {
            MissingScreen(
                pendingMembers = emptyList(),
                onNavigateBack = {},
                onSubmitStandup = {}
            )
        }
        composeRule.waitForIdle()

        // Check for empty state UI
        composeRule.onNodeWithText("🎉").assertIsDisplayed()
        composeRule.onNodeWithText("All caught up!").assertIsDisplayed()
        composeRule.onNodeWithText("Everyone has submitted today").assertIsDisplayed()

        // Verify count shows 0
        composeRule.onNodeWithText("0 team members have not submitted").assertIsDisplayed()
    }

    @Test
    fun singleMember_showsCorrectCountAndMemberCard() {
        val memberName = "Alice Johnson"

        composeRule.setContent {
            MissingScreen(
                pendingMembers = listOf(memberName),
                onNavigateBack = {},
                onSubmitStandup = {}
            )
        }

        composeRule.waitForIdle()

        // Check count uses singular form
        composeRule.onNodeWithText("1 team member have not submitted").assertIsDisplayed()

        // Check member card is displayed
        composeRule.onNodeWithText(memberName).assertIsDisplayed()
        composeRule.onNodeWithText("Not submitted yet").assertIsDisplayed()

        // Check initials are displayed
        composeRule.onNodeWithText("AJ").assertIsDisplayed()

        // Check submit button exists
        composeRule.onNodeWithText("Submit").assertIsDisplayed()
    }

    @Test
    fun multipleMembers_showsCorrectPluralCountAndAllCards() {
        val members = listOf(
            "Alice Johnson",
            "Bob Smith",
            "Charlie Brown"
        )

        composeRule.setContent {
            MissingScreen(
                pendingMembers = members,
                onNavigateBack = {},
                onSubmitStandup = {}
            )
        }

        composeRule.waitForIdle()

        // Check count uses plural form
        composeRule.onNodeWithText("3 team members have not submitted").assertIsDisplayed()

        // Check all member cards are displayed
        composeRule.onNodeWithText("Alice Johnson").assertIsDisplayed()
        composeRule.onNodeWithText("Bob Smith").assertIsDisplayed()
        composeRule.onNodeWithText("Charlie Brown").assertIsDisplayed()

        // Check initials for each member
        composeRule.onNodeWithText("AJ").assertIsDisplayed()
        composeRule.onNodeWithText("BS").assertIsDisplayed()
        composeRule.onNodeWithText("CB").assertIsDisplayed()

        // Verify all have submit buttons
        composeRule.onAllNodesWithText("Submit").assertCountEquals(3)
    }

    @Test
    fun topBar_showsCorrectTitleAndBackButton() {
        composeRule.setContent {
            MissingScreen(
                pendingMembers = listOf("John Doe"),
                onNavigateBack = {},
                onSubmitStandup = {}
            )
        }

        composeRule.waitForIdle()

        // Check title
        composeRule.onNodeWithText("Missing Standup").assertIsDisplayed()

        // Check back button exists
        composeRule.onNodeWithContentDescription("Back").assertIsDisplayed()
    }

    @Test
    fun submitButton_existsForEachMember() {
        val members = listOf("Alice Johnson", "Bob Smith")

        composeRule.setContent {
            MissingScreen(
                pendingMembers = members,
                onNavigateBack = {},
                onSubmitStandup = {}
            )
        }

        composeRule.waitForIdle()

        // Verify each member has a submit button
        composeRule.onAllNodesWithText("Submit")
            .assertCountEquals(2)
            .onFirst()
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun memberCard_displaysInitialsCorrectly_twoWords() {
        composeRule.setContent {
            MissingScreen(
                pendingMembers = listOf("John Doe"),
                onNavigateBack = {},
                onSubmitStandup = {}
            )
        }
        composeRule.waitForIdle()
        // Check initials are correctly generated for two-word name
        composeRule.onNodeWithText("JD").assertIsDisplayed()
    }

    @Test
    fun memberCard_displaysInitialsCorrectly_singleWord() {
        composeRule.setContent {
            MissingScreen(
                pendingMembers = listOf("Alice"),
                onNavigateBack = {},
                onSubmitStandup = {}
            )
        }
        composeRule.waitForIdle()
        // Check initials are correctly generated for single-word name
        composeRule.onNodeWithText("A").assertIsDisplayed()
    }

    @Test
    fun memberCard_displaysInitialsCorrectly_threeWords() {
        composeRule.setContent {
            MissingScreen(
                pendingMembers = listOf("Bob Smith Jr"),
                onNavigateBack = {},
                onSubmitStandup = {}
            )
        }
        composeRule.waitForIdle()
        // Check initials are correctly generated for three-word name (takes first two)
        composeRule.onNodeWithText("BS").assertIsDisplayed()
    }

    @Test
    fun memberCard_displaysInitialsCorrectly_hyphenatedName() {
        composeRule.setContent {
            MissingScreen(
                pendingMembers = listOf("Mary-Jane Watson"),
                onNavigateBack = {},
                onSubmitStandup = {}
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
            MissingScreen(
                pendingMembers = members,
                onNavigateBack = {},
                onSubmitStandup = {}
            )
        }

        composeRule.waitForIdle()

        // Check count is correct
        composeRule.onNodeWithText("20 team members have not submitted").assertIsDisplayed()

        // First member should be visible
        composeRule.onNodeWithText("Member 1").assertIsDisplayed()

        // Verify list is scrollable by performing scroll gestures
        // Get any scrollable node (the LazyColumn) and perform scroll
        composeRule.onAllNodes(hasScrollAction())
            .onFirst()
            .performScrollToIndex(5)

        composeRule.waitForIdle()

        // After scrolling, Member 6 should be visible
        composeRule.onNodeWithText("Member 6").assertIsDisplayed()
    }

    @Test
    fun emptyState_noSubmitButtonsShown() {
        composeRule.setContent {
            MissingScreen(
                pendingMembers = emptyList(),
                onNavigateBack = {},
                onSubmitStandup = {}
            )
        }

        composeRule.waitForIdle()

        // Verify no submit buttons exist in empty state
        composeRule.onAllNodesWithText("Submit").assertCountEquals(0)
    }

    @Test
    fun memberCard_showsCorrectSubtitleText() {
        composeRule.setContent {
            MissingScreen(
                pendingMembers = listOf("John Doe"),
                onNavigateBack = {},
                onSubmitStandup = {}
            )
        }

        composeRule.waitForIdle()

        // Check subtitle text is displayed
        composeRule.onNodeWithText("Not submitted yet").assertIsDisplayed()
    }

    @Test
    fun backButton_hasCorrectContentDescription() {
        composeRule.setContent {
            MissingScreen(
                pendingMembers = listOf("Alice"),
                onNavigateBack = {},
                onSubmitStandup = {}
            )
        }

        composeRule.waitForIdle()

        // Verify back button has accessibility label
        composeRule.onNodeWithContentDescription("Back")
            .assertIsDisplayed()
            .assertHasClickAction()
    }
}
