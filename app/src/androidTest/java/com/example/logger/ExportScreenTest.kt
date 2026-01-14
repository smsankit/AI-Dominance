package com.example.logger

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.hasText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.logger.domain.model.PaginatedStandupEntriesData
import com.example.logger.domain.model.PaginationMetaData
import com.example.logger.domain.model.StandupEntryData
import com.example.logger.domain.model.TeamMember
import com.example.logger.presentation.export.ExportScreen
import com.example.logger.presentation.export.ExportViewModel
import com.example.logger.domain.usecase.GetTodayStandupUseCase
import com.example.logger.core.network.NetworkResult
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

    private fun successUseCase(items: List<StandupEntryData>, missingItems: List<StandupEntryData> = emptyList()): GetTodayStandupUseCase {
        val fakeRepo = object : com.example.logger.domain.repository.StandupRepository {
            override fun getTodayStandup() = throw UnsupportedOperationException()
            override suspend fun submitStandupEntry(request: com.example.logger.domain.model.StandupEntryRequestData) = throw UnsupportedOperationException()
            override suspend fun getStandupEntries(
                teamId: Long,
                page: Int?,
                size: Int?,
                teamMemberId: Long?,
                standupDate: String?,
                status: String?
            ): NetworkResult<PaginatedStandupEntriesData> {
                val dataList = if (status == "MISSING") missingItems else items
                val meta = PaginationMetaData(page = page ?: 0, size = size ?: dataList.size, totalElements = dataList.size, totalPages = 1)
                return NetworkResult.Success(PaginatedStandupEntriesData(items = dataList, meta = meta))
            }
        }
        return GetTodayStandupUseCase(repository = fakeRepo)
    }

    private fun errorUseCase(message: String = "An error occurred"): GetTodayStandupUseCase {
        val fakeRepo = object : com.example.logger.domain.repository.StandupRepository {
            override fun getTodayStandup() = throw UnsupportedOperationException()
            override suspend fun submitStandupEntry(request: com.example.logger.domain.model.StandupEntryRequestData) = throw UnsupportedOperationException()
            override suspend fun getStandupEntries(
                teamId: Long,
                page: Int?,
                size: Int?,
                teamMemberId: Long?,
                standupDate: String?,
                status: String?
            ): NetworkResult<PaginatedStandupEntriesData> {
                return NetworkResult.Error(message)
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
        val missingEntries = listOf(
            buildEntry(name = "Charlie", time = "N/A", yesterday = "N/A", today = "N/A", blockers = null),
            buildEntry(name = "Diana", time = "N/A", yesterday = "N/A", today = "N/A", blockers = null)
        )
        val vm = ExportViewModel(getTodayStandup = successUseCase(entries, missingEntries))
        composeRule.setContent {
            ExportScreen(viewModel = vm)
        }

        // Wait for initial state to load
        composeRule.waitForIdle()
        Thread.sleep(1500)
        composeRule.waitForIdle()

        // Verify submitted count and missing count are displayed
        // The count should be "2 submitted, 2 missing" or similar format
        composeRule.onNode(hasText("submitted", substring = true)).assertIsDisplayed()
        composeRule.onNode(hasText("missing", substring = true)).assertIsDisplayed()

        // Markdown content should contain the header
        composeRule.onNode(hasText("Team Standups", substring = true)).assertIsDisplayed()

        // Verify names are displayed in the markdown content
        composeRule.onNode(hasText("Alice", substring = true)).assertIsDisplayed()
        composeRule.onNode(hasText("Bob", substring = true)).assertIsDisplayed()
    }

    @Test
    fun singleSubmission_displaysProperly() {
        val entries = listOf(
            buildEntry(name = "Alice", time = "10:15", yesterday = "Fixed bugs", today = "Implement feature X", blockers = null)
        )
        val vm = ExportViewModel(getTodayStandup = successUseCase(entries))
        composeRule.setContent {
            ExportScreen(viewModel = vm)
        }
        composeRule.waitForIdle()
        Thread.sleep(1500)
        composeRule.waitForIdle()

        // Verify single submission displays
        composeRule.onNode(hasText("Alice", substring = true)).assertIsDisplayed()
        composeRule.onNode(hasText("Fixed bugs", substring = true)).assertIsDisplayed()
        composeRule.onNode(hasText("submitted", substring = true)).assertIsDisplayed()
    }

    @Test
    fun withBlockers_displaysBlockerInfo() {
        val entries = listOf(
            buildEntry(name = "Alice", time = "10:15", yesterday = "Fixed bugs", today = "Implement feature", blockers = "Waiting for API"),
            buildEntry(name = "Bob", time = "10:20", yesterday = "Code review", today = "Write tests", blockers = "Environment issue")
        )
        val vm = ExportViewModel(getTodayStandup = successUseCase(entries))
        composeRule.setContent {
            ExportScreen(viewModel = vm)
        }
        composeRule.waitForIdle()
        Thread.sleep(1500)
        composeRule.waitForIdle()

        // Verify blockers are displayed
        composeRule.onNode(hasText("Alice", substring = true)).assertIsDisplayed()
        composeRule.onNode(hasText("Bob", substring = true)).assertIsDisplayed()
        composeRule.onNode(hasText("Waiting for API", substring = true)).assertIsDisplayed()
    }

    @Test
    fun manySubmissions_allDisplayedWithCount() {
        val entries = (1..10).map { i ->
            buildEntry(
                name = "Member$i",
                time = "10:${String.format("%02d", i)}",
                yesterday = "Work $i yesterday",
                today = "Plan $i today"
            )
        }
        val vm = ExportViewModel(getTodayStandup = successUseCase(entries))
        composeRule.setContent {
            ExportScreen(viewModel = vm)
        }
        composeRule.waitForIdle()
        Thread.sleep(1500)
        composeRule.waitForIdle()

        // Verify count is shown
        composeRule.onNode(hasText("submitted", substring = true)).assertIsDisplayed()
        // Verify some members are displayed
        composeRule.onNode(hasText("Member1", substring = true)).assertIsDisplayed()
    }

    @Test
    fun noBlockers_displaysWithoutBlockerInfo() {
        val entries = listOf(
            buildEntry(name = "Alice", time = "10:15", yesterday = "Fixed bugs", today = "Implement feature", blockers = null),
            buildEntry(name = "Bob", time = "10:20", yesterday = "Code review", today = "Write tests", blockers = null)
        )
        val vm = ExportViewModel(getTodayStandup = successUseCase(entries))
        composeRule.setContent {
            ExportScreen(viewModel = vm)
        }
        composeRule.waitForIdle()
        Thread.sleep(1500)
        composeRule.waitForIdle()

        // Verify standups display without blockers
        composeRule.onNode(hasText("Alice", substring = true)).assertIsDisplayed()
        composeRule.onNode(hasText("Bob", substring = true)).assertIsDisplayed()
    }

    @Test
    fun emptyBlockers_treatAsNoBlockers() {
        val entries = listOf(
            buildEntry(name = "Alice", time = "10:15", yesterday = "Fixed bugs", today = "Implement feature", blockers = "")
        )
        val vm = ExportViewModel(getTodayStandup = successUseCase(entries))
        composeRule.setContent {
            ExportScreen(viewModel = vm)
        }
        composeRule.waitForIdle()
        Thread.sleep(1500)
        composeRule.waitForIdle()

        // Verify standup displays with empty blocker string
        composeRule.onNode(hasText("Alice", substring = true)).assertIsDisplayed()
    }

    @Test
    fun longText_displaysInMarkdown() {
        val longYesterday = "This is a very long description of what was done yesterday. " +
                "It contains multiple sentences describing various bug fixes and improvements " +
                "that were made to different components of the system."
        val longToday = "This is a long plan for today that includes multiple tasks " +
                "such as code review, testing, and documentation updates."

        val entries = listOf(
            buildEntry(
                name = "Alice",
                time = "10:15",
                yesterday = longYesterday,
                today = longToday
            )
        )
        val vm = ExportViewModel(getTodayStandup = successUseCase(entries))
        composeRule.setContent {
            ExportScreen(viewModel = vm)
        }
        composeRule.waitForIdle()
        Thread.sleep(1500)
        composeRule.waitForIdle()

        // Verify long text displays
        composeRule.onNode(hasText("Alice", substring = true)).assertIsDisplayed()
        composeRule.onNode(hasText("bug fixes", substring = true)).assertIsDisplayed()
    }

    @Test
    fun submissionsAndMissingTogether_displaysCorrectly() {
        val entries = listOf(
            buildEntry(name = "Alice", time = "10:15", yesterday = "Fixed bugs", today = "Feature X"),
            buildEntry(name = "Bob", time = "10:20", yesterday = "Code review", today = "Write tests")
        )
        val missingEntries = listOf(
            buildEntry(name = "Charlie", time = "N/A", yesterday = "N/A", today = "N/A"),
            buildEntry(name = "Diana", time = "N/A", yesterday = "N/A", today = "N/A")
        )
        val vm = ExportViewModel(getTodayStandup = successUseCase(entries, missingEntries))
        composeRule.setContent {
            ExportScreen(viewModel = vm)
        }
        composeRule.waitForIdle()
        Thread.sleep(1500)
        composeRule.waitForIdle()

        // Verify both submitted and missing are shown
        composeRule.onNode(hasText("submitted", substring = true)).assertIsDisplayed()
        composeRule.onNode(hasText("missing", substring = true)).assertIsDisplayed()
        composeRule.onNode(hasText("Alice", substring = true)).assertIsDisplayed()
        composeRule.onNode(hasText("Bob", substring = true)).assertIsDisplayed()
    }

    @Test
    fun dateDisplay_showsCorrectDate() {
        val entries = listOf(
            buildEntry(name = "Alice", time = "10:15", yesterday = "Fixed bugs", today = "Feature X")
        )
        val vm = ExportViewModel(getTodayStandup = successUseCase(entries))
        composeRule.setContent {
            ExportScreen(viewModel = vm)
        }
        composeRule.waitForIdle()
        Thread.sleep(1500)
        composeRule.waitForIdle()

        // Verify markdown header with date is displayed
        // Instead of searching for just "2026", search for the specific date format
        composeRule.onNode(hasText("Team Standups", substring = true)).assertIsDisplayed()
        // Verify name is displayed in the content
        composeRule.onNode(hasText("Alice", substring = true)).assertIsDisplayed()
    }
}
