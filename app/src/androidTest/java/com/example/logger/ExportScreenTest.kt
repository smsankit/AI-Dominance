package com.example.logger

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.hasText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.logger.core.network.NetworkResult
import com.example.logger.domain.model.PaginatedStandupEntriesData
import com.example.logger.domain.model.PaginationMetaData
import com.example.logger.domain.model.StandupEntryData
import com.example.logger.domain.model.TeamMember
import com.example.logger.presentation.export.ExportScreen
import com.example.logger.presentation.export.ExportViewModel
import com.example.logger.domain.usecase.GetTodayStandupUseCase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExportScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun fakeTeamMember(id: Long, name: String) = TeamMember(id = id, name = name, email = "$name@example.com")

    private fun buildEntry(name: String, time: String, yesterday: String, today: String, blockers: String? = null, editedAt: String? = null): StandupEntryData {
        val member = fakeTeamMember(1, name)
        return StandupEntryData(
            id = 1,
            standupDate = "2026-01-10",
            yesterdayWork = yesterday,
            todayPlan = today,
            blockers = blockers,
            teamMemberId = 1,
            teamId = 1,
            createdAt = time,
            updatedAt = editedAt,
            teamMember = member
        )
    }

    private fun successUseCase(items: List<StandupEntryData>): GetTodayStandupUseCase {
        val fakeRepo = object : com.example.logger.domain.repository.StandupRepository {
            override fun getTodayStandup() = throw UnsupportedOperationException()
            override suspend fun submitStandupEntry(request: com.example.logger.domain.model.StandupEntryRequestData) = throw UnsupportedOperationException()
            override suspend fun getStandupEntries(teamId: Long, page: Int?, size: Int?, teamMemberId: Long?, standupDate: String?): com.example.logger.core.network.NetworkResult<PaginatedStandupEntriesData> {
                val meta = PaginationMetaData(page = page ?: 0, size = size ?: items.size, totalElements = items.size, totalPages = 1)
                return com.example.logger.core.network.NetworkResult.Success(PaginatedStandupEntriesData(items = items, meta = meta))
            }
        }
        return GetTodayStandupUseCase(repository = fakeRepo)
    }

    private fun errorUseCase(message: String = "An error occurred"): GetTodayStandupUseCase {
        val fakeRepo = object : com.example.logger.domain.repository.StandupRepository {
            override fun getTodayStandup() = throw UnsupportedOperationException()
            override suspend fun submitStandupEntry(request: com.example.logger.domain.model.StandupEntryRequestData) = throw UnsupportedOperationException()
            override suspend fun getStandupEntries(teamId: Long, page: Int?, size: Int?, teamMemberId: Long?, standupDate: String?): com.example.logger.core.network.NetworkResult<PaginatedStandupEntriesData> {
                return com.example.logger.core.network.NetworkResult.Error(message)
            }
        }
        return GetTodayStandupUseCase(repository = fakeRepo)
    }

    @Test
    fun emptyState_showsNoDataMessage_andExportDisabled() {
        val vm = ExportViewModel(getTodayStandup = successUseCase(emptyList()))
        composeRule.setContent {
            ExportScreen(viewModel = vm)
        }
        // Wait for recomposition
        composeRule.waitForIdle()

        // Expect the no data texts (matching strings in ExportScreen)
        composeRule.onNodeWithText("No data to export").assertIsDisplayed()
        composeRule.onNodeWithText("No submissions found for this date").assertIsDisplayed()
    }

    @Test
    fun errorState_showsErrorUI() {
        val vm = ExportViewModel(getTodayStandup = errorUseCase("Failed to load"))
        composeRule.setContent {
            ExportScreen(viewModel = vm)
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Error Loading Data").assertIsDisplayed()
        composeRule.onNodeWithText("Failed to load").assertIsDisplayed()
    }

    @Test
    fun happyPath_showsPreviewMarkdown_andCount() {
        val entries = listOf(
            buildEntry(name = "Alice", time = "10:15", yesterday = "Fixed bugs", today = "Implement feature X", blockers = null),
            buildEntry(name = "Bob", time = "10:20", yesterday = "Code review", today = "Write tests", blockers = "Env issue")
        )
        val vm = ExportViewModel(getTodayStandup = successUseCase(entries))
        composeRule.setContent {
            ExportScreen(viewModel = vm)
        }
        composeRule.waitForIdle()

        // Preview title
        composeRule.onNodeWithText("Preview").assertIsDisplayed()
        // Exact count label to avoid ambiguous matches
        composeRule.onNodeWithText("2 standup(s)").assertIsDisplayed()

        // Markdown content contains names and headers
        composeRule.onNode(hasText("# Team Standups", substring = true)).assertIsDisplayed()
        composeRule.onNode(hasText("Alice", substring = true)).assertIsDisplayed()
        composeRule.onNode(hasText("Bob", substring = true)).assertIsDisplayed()
    }

}
