package com.example.logger

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.logger.presentation.history.HistoryScreen
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class HistoryScreenTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()
// TODO   val composeRule = createAndroidComposeRule<dagger.hilt.android.testing.HiltTestActivity>()

    @Test
    fun history_displaysSubtitle_andDatePickerIcon() {
        composeRule.setContent {
            HistoryScreen(onNavigateBack = {})
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("View past submissions").assertIsDisplayed()
        composeRule.onNodeWithText("Pick date").assertIsDisplayed()
    }

    @Test
    fun history_emptyState_showsEmptyTitle_andSubtitle() {
        composeRule.setContent {
            HistoryScreen(onNavigateBack = {})
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("No standups").assertIsDisplayed()
        composeRule.onNodeWithText("No submissions for this date").assertIsDisplayed()
    }
}
