package com.example.logger

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.logger.presentation.home.HomeUiState
import com.example.logger.presentation.home.HomeScreen
import com.example.logger.domain.model.Standup
import com.example.logger.domain.model.SentimentSummary
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loadingState_showsProgressIndicator() {
        composeTestRule.setContent {
            HomeScreen(
                state = HomeUiState(isLoading = true),
                onRetry = {},
                onViewMissing = {},
                onSubmit = {},
                onExport = {},
                onLoadMore = {},
                onViewRoster = {},
            )
        }
        composeTestRule.onNodeWithContentDescription("Progress indicator", useUnmergedTree = true).assertExists()
    }

    @Test
    fun errorState_showsErrorMessage_andRetryButton() {
        val errorMsg = "Network error. Please try again."
        composeTestRule.setContent {
            HomeScreen(
                state = HomeUiState(isLoading = false, error = errorMsg),
                onRetry = {},
                onViewMissing = {},
                onSubmit = {},
                onExport = {},
                onLoadMore = {},
                onViewRoster = {},
            )
        }
        composeTestRule.onNodeWithText(errorMsg).assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun emptyState_showsNoStandupsMessage_andSubmitButton() {
        composeTestRule.setContent {
            HomeScreen(
                state = HomeUiState(
                    isLoading = false,
                    error = null,
                    submissions = emptyList(),
                    roster = listOf("Alex", "Priya"),
                    pending = listOf("Alex", "Priya")
                ),
                onRetry = {},
                onViewMissing = {},
                onSubmit = {},
                onExport = {},
                onLoadMore = {},
                onViewRoster = {},
            )
        }
        composeTestRule.onNodeWithText("No Standups Yet").assertIsDisplayed()
        composeTestRule.onNodeWithText("Submit First Standup").assertIsDisplayed()
    }

    @Test
    fun dataState_showsStandupList_andExportButton() {
        val submissions = listOf(
            Standup(id = "1", name = "Alex Johnson", yesterday = "Reviewed PRs", today = "Finalize API spec", blockers = null, time = "09:10", editedAt = null),
            Standup(id = "2", name = "Priya Verma", yesterday = "Auth flow fixes", today = "Add MFA", blockers = "Waiting on UX", time = "09:25", editedAt = null)
        )
        val roster = listOf("Alex Johnson", "Priya Verma", "Miguel Santos")
        composeTestRule.setContent {
            HomeScreen(
                state = HomeUiState(
                    isLoading = false,
                    error = null,
                    submissions = submissions,
                    roster = roster,
                    pending = listOf("Miguel Santos"),
                    lastUpdated = "10:45"
                ),
                onRetry = {},
                onViewMissing = {},
                onSubmit = {},
                onExport = {},
                onLoadMore = {},
                onViewRoster = {},
            )
        }
        composeTestRule.onNodeWithText("Today's Standups").assertIsDisplayed()
        composeTestRule.onNodeWithText("Export").assertIsDisplayed()
        composeTestRule.onNodeWithText("Alex Johnson").assertIsDisplayed()
        composeTestRule.onNodeWithText("Priya Verma").assertIsDisplayed()
    }

    // TeamMoodCard coverage below

    @Test
    fun teamMood_loading_showsArrow_andInvokesNavigationOnClick() {
        var clicked = false
        composeTestRule.setContent {
            HomeScreen(
                state = HomeUiState(
                    isLoading = false,
                    sentimentSummary = null,
                    isSentimentLoading = true,
                    sentimentError = null
                ),
                onRetry = {},
                onViewMissing = {},
                onSubmit = {},
                onExport = {},
                onLoadMore = {},
                onViewRoster = {},
                onNavigateToSentimentAnalysis = { clicked = true }
            )
        }
        composeTestRule.onNodeWithContentDescription("View Sentiment Analysis").assertIsDisplayed().performClick()
        assert(clicked)
    }

    @Test
    fun teamMood_error_showsUnableToLoadMessage() {
        composeTestRule.setContent {
            HomeScreen(
                state = HomeUiState(
                    isLoading = false,
                    sentimentSummary = null,
                    isSentimentLoading = false,
                    sentimentError = "boom"
                ),
                onRetry = {},
                onViewMissing = {},
                onSubmit = {},
                onExport = {},
                onLoadMore = {},
                onViewRoster = {},
            )
        }
        composeTestRule.onNodeWithText("Unable to load team mood").assertIsDisplayed()
    }

    @Test
    fun teamMood_noDataToday_showsMessage_andNoArrow() {
        composeTestRule.setContent {
            HomeScreen(
                state = HomeUiState(
                    isLoading = false,
                    sentimentSummary = SentimentSummary(positive = 0, neutral = 0, negative = 0, total = 0),
                    isSentimentLoading = false,
                    sentimentError = null
                ),
                onRetry = {},
                onViewMissing = {},
                onSubmit = {},
                onExport = {},
                onLoadMore = {},
                onViewRoster = {},
                onNavigateToSentimentAnalysis = {}
            )
        }
        composeTestRule.onNodeWithText("No sentiment data today").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("View Sentiment Analysis").assertDoesNotExist()
    }

    @Test
    fun teamMood_positiveSummary_showsTexts_andArrowWorks() {
        var clicked = false
        composeTestRule.setContent {
            HomeScreen(
                state = HomeUiState(
                    isLoading = false,
                    sentimentSummary = SentimentSummary(positive = 5, neutral = 2, negative = 1, total = 8),
                    isSentimentLoading = false,
                    sentimentError = null
                ),
                onRetry = {},
                onViewMissing = {},
                onSubmit = {},
                onExport = {},
                onLoadMore = {},
                onViewRoster = {},
                onNavigateToSentimentAnalysis = { clicked = true }
            )
        }
        composeTestRule.onNodeWithText("Team Mood: Positive 🙂").assertIsDisplayed()
        composeTestRule.onNodeWithText("Based on 8 submissions. Team morale is good!").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("View Sentiment Analysis").assertIsDisplayed().performClick()
        assert(clicked)
    }
}
