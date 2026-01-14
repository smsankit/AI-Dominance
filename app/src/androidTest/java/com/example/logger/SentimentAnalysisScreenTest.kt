package com.example.logger

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import com.example.logger.presentation.sentiment.SentimentAnalysisScreen

class SentimentAnalysisScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun showsTitleAndBackButton_andInvokesNavigateBackOnClick() {
        var backClicked = false
        composeRule.setContent {
            SentimentAnalysisScreen(pos = 3, neu = 1, neg = 1, onNavigateBack = { backClicked = true })
        }

        composeRule.onNodeWithText("Sentiment Analysis").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Back").assertIsDisplayed().performClick()
        assert(backClicked)
    }

    @Test
    fun showsPieChartWithContentDescription() {
        composeRule.setContent {
            SentimentAnalysisScreen(pos = 2, neu = 2, neg = 1, onNavigateBack = {})
        }

        composeRule.onNode(hasContentDescription("Sentiment pie chart")).assertIsDisplayed()
    }

    @Test
    fun legendDisplaysCorrectCountsAndPercentages_forTypicalDistribution() {
        // pos=3, neu=1, neg=1 => total=5 => 60%, 20%, 20%
        composeRule.setContent {
            SentimentAnalysisScreen(pos = 3, neu = 1, neg = 1, onNavigateBack = {})
        }

        composeRule.onNodeWithText("Overall sentiment distribution").assertIsDisplayed()
        composeRule.onNodeWithText("Positive: 3 (60%)").assertIsDisplayed()
        composeRule.onNodeWithText("Neutral: 1 (20%)").assertIsDisplayed()
        composeRule.onNodeWithText("Negative: 1 (20%)").assertIsDisplayed()
    }

    @Test
    fun legendDisplaysZeroPercentages_whenAllCountsZero() {
        composeRule.setContent {
            SentimentAnalysisScreen(pos = 0, neu = 0, neg = 0, onNavigateBack = {})
        }

        composeRule.onNodeWithText("Positive: 0 (0%)").assertIsDisplayed()
        composeRule.onNodeWithText("Neutral: 0 (0%)").assertIsDisplayed()
        composeRule.onNodeWithText("Negative: 0 (0%)").assertIsDisplayed()
    }
}
