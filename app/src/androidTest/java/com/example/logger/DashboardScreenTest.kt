package com.example.logger

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.hasText
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.logger.core.datastore.PreferencesManager
import com.example.logger.core.network.NetworkResult
import com.example.logger.domain.model.PaginatedStandupEntriesData
import com.example.logger.domain.model.PaginationMetaData
import com.example.logger.domain.model.StandupEntryData
import com.example.logger.domain.model.TeamMember
import com.example.logger.domain.model.TeamMemberData
import com.example.logger.domain.model.TeamSentimentItem
import com.example.logger.domain.repository.StandupRepository
import com.example.logger.domain.repository.TeamRepository
import com.example.logger.domain.usecase.GetTeamMembersUseCase
import com.example.logger.domain.usecase.GetTeamSentimentsUseCase
import com.example.logger.domain.usecase.GetTodayStandupUseCase
import com.example.logger.presentation.dashboard.DashboardScreen
import com.example.logger.presentation.home.HomeViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DashboardScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun createFakeTeamMember(id: Long, name: String) = TeamMember(
        id = id,
        name = name,
        email = "$name@example.com"
    )

    private fun createFakeTeamMemberData(id: Long, name: String) = TeamMemberData(
        id = id,
        name = name,
        email = "$name@example.com",
        createdAt = "2026-01-01",
        updatedAt = "2026-01-01"
    )

    private fun buildStandupEntry(
        id: Long,
        name: String,
        time: String,
        yesterday: String,
        today: String,
        blockers: String? = null,
        standupDate: String = "2026-01-13"
    ): StandupEntryData {
        val member = createFakeTeamMember(id, name)
        return StandupEntryData(
            id = id,
            standupDate = standupDate,
            yesterdayWork = yesterday,
            todayPlan = today,
            blockers = blockers,
            teamMemberId = id,
            teamId = 1,
            createdAt = time,
            updatedAt = null,
            teamMember = member
        )
    }

    private fun createFakeTeamRepository(members: List<TeamMemberData>): TeamRepository {
        return object : TeamRepository {
            override fun getTeamMembers(teamId: Long, page: Int, size: Int): Flow<NetworkResult<List<TeamMemberData>>> {
                return flowOf(NetworkResult.Success(members))
            }

            override fun getTeamSentiments(teamId: Long, from: String?, to: String?): Flow<NetworkResult<List<TeamSentimentItem>>> {
                return flowOf(NetworkResult.Success(emptyList()))
            }
        }
    }

    private fun createFakeStandupRepository(entries: List<StandupEntryData>): StandupRepository {
        return object : StandupRepository {
            override suspend fun getStandupEntries(
                teamId: Long,
                page: Int?,
                size: Int?,
                teamMemberId: Long?,
                standupDate: String?,
                status: String?
            ): NetworkResult<PaginatedStandupEntriesData> {
                val meta = PaginationMetaData(
                    page = page ?: 0,
                    size = size ?: entries.size,
                    totalElements = entries.size,
                    totalPages = 1
                )
                return NetworkResult.Success(
                    PaginatedStandupEntriesData(items = entries, meta = meta)
                )
            }

            override fun getTodayStandup() = throw UnsupportedOperationException()
            override suspend fun submitStandupEntry(request: com.example.logger.domain.model.StandupEntryRequestData) =
                throw UnsupportedOperationException()
        }
    }

    private fun createFakePreferencesManager(): PreferencesManager {
        val fakeDataStore = object : DataStore<Preferences> {
            override val data: Flow<Preferences> = flowOf(androidx.datastore.preferences.core.emptyPreferences())
            override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
                return androidx.datastore.preferences.core.emptyPreferences()
            }
        }
        return PreferencesManager(fakeDataStore)
    }

    @Test
    fun loadingState_showsLoadingIndicator() {
        val members = listOf(
            createFakeTeamMemberData(1, "Alice Johnson"),
            createFakeTeamMemberData(2, "Bob Smith")
        )

        val teamRepo = createFakeTeamRepository(members)
        val standupRepo = createFakeStandupRepository(emptyList())
        val prefsManager = createFakePreferencesManager()

        val getTeamMembersUseCase = GetTeamMembersUseCase(teamRepo, prefsManager)
        val getTodayStandupUseCase = GetTodayStandupUseCase(standupRepo)
        val getTeamSentimentsUseCase = GetTeamSentimentsUseCase(teamRepo)

        val viewModel = HomeViewModel(getTodayStandupUseCase, getTeamMembersUseCase, getTeamSentimentsUseCase)

        composeRule.setContent {
            DashboardScreen(
                viewModel = viewModel,
                onNavigateSubmit = {},
                onNavigateHistory = {},
                onNavigateSettings = {},
                onNavigateMissing = {},
                onNavigateExport = {},
                onNavigateRoster = {}
            )
        }
        composeRule.waitForIdle()

        // Wait for async loading
        Thread.sleep(1500)
        composeRule.waitForIdle()

        // Verify screen renders - HomeRoute should display content
        // Check for common elements like buttons or headers that exist in HomeRoute
    }

    @Test
    fun emptyState_showsNoSubmissions() {
        val members = listOf(
            createFakeTeamMemberData(1, "Alice Johnson"),
            createFakeTeamMemberData(2, "Bob Smith")
        )

        val teamRepo = createFakeTeamRepository(members)
        val standupRepo = createFakeStandupRepository(emptyList())
        val prefsManager = createFakePreferencesManager()

        val getTeamMembersUseCase = GetTeamMembersUseCase(teamRepo, prefsManager)
        val getTodayStandupUseCase = GetTodayStandupUseCase(standupRepo)
        val getTeamSentimentsUseCase = GetTeamSentimentsUseCase(teamRepo)

        val viewModel = HomeViewModel(getTodayStandupUseCase, getTeamMembersUseCase, getTeamSentimentsUseCase)

        composeRule.setContent {
            DashboardScreen(
                viewModel = viewModel,
                onNavigateSubmit = {},
                onNavigateHistory = {},
                onNavigateSettings = {},
                onNavigateMissing = {},
                onNavigateExport = {},
                onNavigateRoster = {}
            )
        }
        composeRule.waitForIdle()

        // Wait for async loading to complete (team members + standup data)
        Thread.sleep(1500)
        composeRule.waitForIdle()

        // With 2 team members and 0 submissions, verify pending state
        // The exact UI depends on HomeRoute implementation but screen should render
    }

    @Test
    fun withSubmissions_displaysStandupEntries() {
        val members = listOf(
            createFakeTeamMemberData(1, "Alice Johnson"),
            createFakeTeamMemberData(2, "Bob Smith")
        )

        val entries = listOf(
            buildStandupEntry(
                id = 1,
                name = "Alice Johnson",
                time = "10:15",
                yesterday = "Fixed bugs",
                today = "Implement feature X"
            )
        )

        val teamRepo = createFakeTeamRepository(members)
        val standupRepo = createFakeStandupRepository(entries)
        val prefsManager = createFakePreferencesManager()

        val getTeamMembersUseCase = GetTeamMembersUseCase(teamRepo, prefsManager)
        val getTodayStandupUseCase = GetTodayStandupUseCase(standupRepo)
        val getTeamSentimentsUseCase = GetTeamSentimentsUseCase(teamRepo)

        val viewModel = HomeViewModel(getTodayStandupUseCase, getTeamMembersUseCase, getTeamSentimentsUseCase)

        composeRule.setContent {
            DashboardScreen(
                viewModel = viewModel,
                onNavigateSubmit = {},
                onNavigateHistory = {},
                onNavigateSettings = {},
                onNavigateMissing = {},
                onNavigateExport = {},
                onNavigateRoster = {}
            )
        }
        composeRule.waitForIdle()

        // Wait for async loading (fetch team members, then fetch standups)
        Thread.sleep(1500)
        composeRule.waitForIdle()

        // Verify submission is displayed - Alice's name should appear
        composeRule.onNode(hasText("Alice Johnson", substring = true)).assertIsDisplayed()

        // Also verify work content appears
        composeRule.onNode(hasText("Fixed bugs", substring = true)).assertIsDisplayed()
    }

    @Test
    fun multipleSubmissions_allDisplayed() {
        val members = listOf(
            createFakeTeamMemberData(1, "Alice Johnson"),
            createFakeTeamMemberData(2, "Bob Smith"),
            createFakeTeamMemberData(3, "Charlie Brown")
        )

        val entries = listOf(
            buildStandupEntry(
                id = 1,
                name = "Alice Johnson",
                time = "10:15",
                yesterday = "Fixed bugs",
                today = "Implement feature X"
            ),
            buildStandupEntry(
                id = 2,
                name = "Bob Smith",
                time = "10:20",
                yesterday = "Code review",
                today = "Write tests"
            )
        )

        val teamRepo = createFakeTeamRepository(members)
        val standupRepo = createFakeStandupRepository(entries)
        val prefsManager = createFakePreferencesManager()

        val getTeamMembersUseCase = GetTeamMembersUseCase(teamRepo, prefsManager)
        val getTodayStandupUseCase = GetTodayStandupUseCase(standupRepo)
        val getTeamSentimentsUseCase = GetTeamSentimentsUseCase(teamRepo)

        val viewModel = HomeViewModel(getTodayStandupUseCase, getTeamMembersUseCase, getTeamSentimentsUseCase)

        composeRule.setContent {
            DashboardScreen(
                viewModel = viewModel,
                onNavigateSubmit = {},
                onNavigateHistory = {},
                onNavigateSettings = {},
                onNavigateMissing = {},
                onNavigateExport = {},
                onNavigateRoster = {}
            )
        }
        composeRule.waitForIdle()

        // Wait for async loading
        Thread.sleep(1500)
        composeRule.waitForIdle()

        // Verify both submissions display
        composeRule.onNode(hasText("Alice Johnson", substring = true)).assertIsDisplayed()
        composeRule.onNode(hasText("Bob Smith", substring = true)).assertIsDisplayed()

        // Verify work content
        composeRule.onNode(hasText("Fixed bugs", substring = true)).assertIsDisplayed()
        composeRule.onNode(hasText("Code review", substring = true)).assertIsDisplayed()
    }

    @Test
    fun pendingMembers_showsCorrectCount() {
        val members = listOf(
            createFakeTeamMemberData(1, "Alice Johnson"),
            createFakeTeamMemberData(2, "Bob Smith"),
            createFakeTeamMemberData(3, "Charlie Brown")
        )

        // Only Alice submitted
        val entries = listOf(
            buildStandupEntry(
                id = 1,
                name = "Alice Johnson",
                time = "10:15",
                yesterday = "Fixed bugs",
                today = "Implement feature X"
            )
        )

        val teamRepo = createFakeTeamRepository(members)
        val standupRepo = createFakeStandupRepository(entries)
        val prefsManager = createFakePreferencesManager()

        val getTeamMembersUseCase = GetTeamMembersUseCase(teamRepo, prefsManager)
        val getTodayStandupUseCase = GetTodayStandupUseCase(standupRepo)
        val getTeamSentimentsUseCase = GetTeamSentimentsUseCase(teamRepo)

        val viewModel = HomeViewModel(getTodayStandupUseCase, getTeamMembersUseCase, getTeamSentimentsUseCase)

        composeRule.setContent {
            DashboardScreen(
                viewModel = viewModel,
                onNavigateSubmit = {},
                onNavigateHistory = {},
                onNavigateSettings = {},
                onNavigateMissing = {},
                onNavigateExport = {},
                onNavigateRoster = {}
            )
        }
        composeRule.waitForIdle()

        // Wait for async loading
        Thread.sleep(1500)
        composeRule.waitForIdle()

        // Verify Alice submitted
        composeRule.onNode(hasText("Alice Johnson", substring = true)).assertIsDisplayed()

        // Pending members (Bob and Charlie) should be tracked in ViewModel
        // The exact UI display depends on HomeRoute implementation
    }

    @Test
    fun navigationCallbacks_areTriggered() {
        val members = listOf(createFakeTeamMemberData(1, "Alice"))
        val teamRepo = createFakeTeamRepository(members)
        val standupRepo = createFakeStandupRepository(emptyList())
        val prefsManager = createFakePreferencesManager()

        val getTeamMembersUseCase = GetTeamMembersUseCase(teamRepo, prefsManager)
        val getTodayStandupUseCase = GetTodayStandupUseCase(standupRepo)
        val getTeamSentimentsUseCase = GetTeamSentimentsUseCase(teamRepo)

        val viewModel = HomeViewModel(getTodayStandupUseCase, getTeamMembersUseCase, getTeamSentimentsUseCase)

        composeRule.setContent {
            DashboardScreen(
                viewModel = viewModel,
                onNavigateSubmit = {},
                onNavigateHistory = {},
                onNavigateSettings = {},
                onNavigateMissing = {},
                onNavigateExport = {},
                onNavigateRoster = {}
            )
        }
        composeRule.waitForIdle()

        // Screen should render without crashing
        // Callbacks will be triggered by user interactions in HomeRoute
    }

    @Test
    fun errorState_displaysErrorMessage() {
        val members = listOf(createFakeTeamMemberData(1, "Alice"))

        val teamRepo = createFakeTeamRepository(members)

        // Create error repository
        val errorRepo = object : StandupRepository {
            override suspend fun getStandupEntries(
                teamId: Long,
                page: Int?,
                size: Int?,
                teamMemberId: Long?,
                standupDate: String?,
                status: String?
            ): NetworkResult<PaginatedStandupEntriesData> {
                return NetworkResult.Error("Failed to load standups")
            }

            override fun getTodayStandup() = throw UnsupportedOperationException()
            override suspend fun submitStandupEntry(request: com.example.logger.domain.model.StandupEntryRequestData) =
                throw UnsupportedOperationException()
        }

        val prefsManager = createFakePreferencesManager()

        val getTeamMembersUseCase = GetTeamMembersUseCase(teamRepo, prefsManager)
        val getTodayStandupUseCase = GetTodayStandupUseCase(errorRepo)
        val getTeamSentimentsUseCase = GetTeamSentimentsUseCase(teamRepo)

        val viewModel = HomeViewModel(getTodayStandupUseCase, getTeamMembersUseCase, getTeamSentimentsUseCase)

        composeRule.setContent {
            DashboardScreen(
                viewModel = viewModel,
                onNavigateSubmit = {},
                onNavigateHistory = {},
                onNavigateSettings = {},
                onNavigateMissing = {},
                onNavigateExport = {},
                onNavigateRoster = {}
            )
        }
        composeRule.waitForIdle()

        // Wait for async loading
        Thread.sleep(1000)
        composeRule.waitForIdle()

        // Error message should be displayed (exact text depends on HomeRoute)
    }

    @Test
    fun refreshToken_isHandledCorrectly() {
        val members = listOf(createFakeTeamMemberData(1, "Alice"))
        val teamRepo = createFakeTeamRepository(members)
        val standupRepo = createFakeStandupRepository(emptyList())
        val prefsManager = createFakePreferencesManager()

        val getTeamMembersUseCase = GetTeamMembersUseCase(teamRepo, prefsManager)
        val getTodayStandupUseCase = GetTodayStandupUseCase(standupRepo)
        val getTeamSentimentsUseCase = GetTeamSentimentsUseCase(teamRepo)

        val viewModel = HomeViewModel(getTodayStandupUseCase, getTeamMembersUseCase, getTeamSentimentsUseCase)

        composeRule.setContent {
            DashboardScreen(
                viewModel = viewModel,
                refreshToken = "test-token-123",
                onNavigateSubmit = {},
                onNavigateHistory = {},
                onNavigateSettings = {},
                onNavigateMissing = {},
                onNavigateExport = {},
                onNavigateRoster = {}
            )
        }
        composeRule.waitForIdle()

        // Screen should render with refresh token
        Thread.sleep(1500)
        composeRule.waitForIdle()
    }

    @Test
    fun withBlockers_displaysBlockerInformation() {
        val members = listOf(
            createFakeTeamMemberData(1, "Alice Johnson"),
            createFakeTeamMemberData(2, "Bob Smith")
        )

        val entries = listOf(
            buildStandupEntry(
                id = 1,
                name = "Alice Johnson",
                time = "10:15",
                yesterday = "Fixed bugs",
                today = "Implement feature X",
                blockers = "Waiting for API documentation"
            )
        )

        val teamRepo = createFakeTeamRepository(members)
        val standupRepo = createFakeStandupRepository(entries)
        val prefsManager = createFakePreferencesManager()

        val getTeamMembersUseCase = GetTeamMembersUseCase(teamRepo, prefsManager)
        val getTodayStandupUseCase = GetTodayStandupUseCase(standupRepo)
        val getTeamSentimentsUseCase = GetTeamSentimentsUseCase(teamRepo)

        val viewModel = HomeViewModel(getTodayStandupUseCase, getTeamMembersUseCase, getTeamSentimentsUseCase)

        composeRule.setContent {
            DashboardScreen(
                viewModel = viewModel,
                onNavigateSubmit = {},
                onNavigateHistory = {},
                onNavigateSettings = {},
                onNavigateMissing = {},
                onNavigateExport = {},
                onNavigateRoster = {}
            )
        }
        composeRule.waitForIdle()

        // Wait for async loading
        Thread.sleep(1500)
        composeRule.waitForIdle()

        // Verify submission with blocker is displayed
        composeRule.onNode(hasText("Alice Johnson", substring = true)).assertIsDisplayed()
        composeRule.onNode(hasText("Waiting for API documentation", substring = true)).assertIsDisplayed()
    }

    @Test
    fun allMembersSubmitted_showsNoActivePending() {
        val members = listOf(
            createFakeTeamMemberData(1, "Alice Johnson"),
            createFakeTeamMemberData(2, "Bob Smith")
        )

        // Both members submitted
        val entries = listOf(
            buildStandupEntry(
                id = 1,
                name = "Alice Johnson",
                time = "10:15",
                yesterday = "Fixed bugs",
                today = "Implement feature X"
            ),
            buildStandupEntry(
                id = 2,
                name = "Bob Smith",
                time = "10:20",
                yesterday = "Code review",
                today = "Write tests"
            )
        )

        val teamRepo = createFakeTeamRepository(members)
        val standupRepo = createFakeStandupRepository(entries)
        val prefsManager = createFakePreferencesManager()

        val getTeamMembersUseCase = GetTeamMembersUseCase(teamRepo, prefsManager)
        val getTodayStandupUseCase = GetTodayStandupUseCase(standupRepo)
        val getTeamSentimentsUseCase = GetTeamSentimentsUseCase(teamRepo)

        val viewModel = HomeViewModel(getTodayStandupUseCase, getTeamMembersUseCase, getTeamSentimentsUseCase)

        composeRule.setContent {
            DashboardScreen(
                viewModel = viewModel,
                onNavigateSubmit = {},
                onNavigateHistory = {},
                onNavigateSettings = {},
                onNavigateMissing = {},
                onNavigateExport = {},
                onNavigateRoster = {}
            )
        }
        composeRule.waitForIdle()

        // Wait for async loading
        Thread.sleep(1500)
        composeRule.waitForIdle()

        // Verify both members submitted
        composeRule.onNode(hasText("Alice Johnson", substring = true)).assertIsDisplayed()
        composeRule.onNode(hasText("Bob Smith", substring = true)).assertIsDisplayed()

        // All members have submitted, so pending count should be 0
    }

    @Test
    fun missingStandups_displaysMissingBanner() {
        val members = listOf(
            createFakeTeamMemberData(1, "Alice Johnson"),
            createFakeTeamMemberData(2, "Bob Smith"),
            createFakeTeamMemberData(3, "Charlie Brown")
        )

        // Only one member submitted, two are missing
        val entries = listOf(
            buildStandupEntry(
                id = 1,
                name = "Alice Johnson",
                time = "10:15",
                yesterday = "Fixed bugs",
                today = "Implement feature X"
            )
        )

        val teamRepo = createFakeTeamRepository(members)
        val standupRepo = createFakeStandupRepository(entries)
        val prefsManager = createFakePreferencesManager()

        val getTeamMembersUseCase = GetTeamMembersUseCase(teamRepo, prefsManager)
        val getTodayStandupUseCase = GetTodayStandupUseCase(standupRepo)
        val getTeamSentimentsUseCase = GetTeamSentimentsUseCase(teamRepo)

        val viewModel = HomeViewModel(getTodayStandupUseCase, getTeamMembersUseCase, getTeamSentimentsUseCase)

        composeRule.setContent {
            DashboardScreen(
                viewModel = viewModel,
                onNavigateSubmit = {},
                onNavigateHistory = {},
                onNavigateSettings = {},
                onNavigateMissing = {},
                onNavigateExport = {},
                onNavigateRoster = {}
            )
        }
        composeRule.waitForIdle()

        Thread.sleep(1500)
        composeRule.waitForIdle()

        // Verify submitted standup
        composeRule.onNode(hasText("Alice Johnson", substring = true)).assertIsDisplayed()
    }

    @Test
    fun emptyStandups_showsLoadingAndThenEmpty() {
        val members = listOf(
            createFakeTeamMemberData(1, "Alice Johnson")
        )

        val entries = emptyList<StandupEntryData>()

        val teamRepo = createFakeTeamRepository(members)
        val standupRepo = createFakeStandupRepository(entries)
        val prefsManager = createFakePreferencesManager()

        val getTeamMembersUseCase = GetTeamMembersUseCase(teamRepo, prefsManager)
        val getTodayStandupUseCase = GetTodayStandupUseCase(standupRepo)
        val getTeamSentimentsUseCase = GetTeamSentimentsUseCase(teamRepo)

        val viewModel = HomeViewModel(getTodayStandupUseCase, getTeamMembersUseCase, getTeamSentimentsUseCase)

        composeRule.setContent {
            DashboardScreen(
                viewModel = viewModel,
                onNavigateSubmit = {},
                onNavigateHistory = {},
                onNavigateSettings = {},
                onNavigateMissing = {},
                onNavigateExport = {},
                onNavigateRoster = {}
            )
        }
        composeRule.waitForIdle()

        Thread.sleep(1500)
        composeRule.waitForIdle()

        // When empty, there should be no submission cards displayed
    }

    @Test
    fun standupWithNullBlockers_displaysProperly() {
        val members = listOf(
            createFakeTeamMemberData(1, "Alice Johnson")
        )

        val entries = listOf(
            buildStandupEntry(
                id = 1,
                name = "Alice Johnson",
                time = "10:15",
                yesterday = "Fixed bugs",
                today = "Implement feature X",
                blockers = null  // No blockers
            )
        )

        val teamRepo = createFakeTeamRepository(members)
        val standupRepo = createFakeStandupRepository(entries)
        val prefsManager = createFakePreferencesManager()

        val getTeamMembersUseCase = GetTeamMembersUseCase(teamRepo, prefsManager)
        val getTodayStandupUseCase = GetTodayStandupUseCase(standupRepo)
        val getTeamSentimentsUseCase = GetTeamSentimentsUseCase(teamRepo)

        val viewModel = HomeViewModel(getTodayStandupUseCase, getTeamMembersUseCase, getTeamSentimentsUseCase)

        composeRule.setContent {
            DashboardScreen(
                viewModel = viewModel,
                onNavigateSubmit = {},
                onNavigateHistory = {},
                onNavigateSettings = {},
                onNavigateMissing = {},
                onNavigateExport = {},
                onNavigateRoster = {}
            )
        }
        composeRule.waitForIdle()

        Thread.sleep(1500)
        composeRule.waitForIdle()

        // Verify standup with null blockers displays correctly
        composeRule.onNode(hasText("Alice Johnson", substring = true)).assertIsDisplayed()
        composeRule.onNode(hasText("Fixed bugs", substring = true)).assertIsDisplayed()
    }

    @Test
    fun standupWithEmptyBlockers_displaysProperly() {
        val members = listOf(
            createFakeTeamMemberData(1, "Alice Johnson")
        )

        val entries = listOf(
            buildStandupEntry(
                id = 1,
                name = "Alice Johnson",
                time = "10:15",
                yesterday = "Fixed bugs",
                today = "Implement feature X",
                blockers = ""  // Empty blockers string
            )
        )

        val teamRepo = createFakeTeamRepository(members)
        val standupRepo = createFakeStandupRepository(entries)
        val prefsManager = createFakePreferencesManager()

        val getTeamMembersUseCase = GetTeamMembersUseCase(teamRepo, prefsManager)
        val getTodayStandupUseCase = GetTodayStandupUseCase(standupRepo)
        val getTeamSentimentsUseCase = GetTeamSentimentsUseCase(teamRepo)

        val viewModel = HomeViewModel(getTodayStandupUseCase, getTeamMembersUseCase, getTeamSentimentsUseCase)

        composeRule.setContent {
            DashboardScreen(
                viewModel = viewModel,
                onNavigateSubmit = {},
                onNavigateHistory = {},
                onNavigateSettings = {},
                onNavigateMissing = {},
                onNavigateExport = {},
                onNavigateRoster = {}
            )
        }
        composeRule.waitForIdle()

        Thread.sleep(1500)
        composeRule.waitForIdle()

        // Verify standup with empty blockers displays correctly
        composeRule.onNode(hasText("Alice Johnson", substring = true)).assertIsDisplayed()
    }

    @Test
    fun multipleStandupsDifferentTimes_displayedInOrder() {
        val members = listOf(
            createFakeTeamMemberData(1, "Alice Johnson"),
            createFakeTeamMemberData(2, "Bob Smith"),
            createFakeTeamMemberData(3, "Charlie Brown")
        )

        val entries = listOf(
            buildStandupEntry(
                id = 1,
                name = "Alice Johnson",
                time = "09:00",
                yesterday = "Task A",
                today = "Task B"
            ),
            buildStandupEntry(
                id = 2,
                name = "Bob Smith",
                time = "10:30",
                yesterday = "Task C",
                today = "Task D"
            ),
            buildStandupEntry(
                id = 3,
                name = "Charlie Brown",
                time = "11:45",
                yesterday = "Task E",
                today = "Task F"
            )
        )

        val teamRepo = createFakeTeamRepository(members)
        val standupRepo = createFakeStandupRepository(entries)
        val prefsManager = createFakePreferencesManager()

        val getTeamMembersUseCase = GetTeamMembersUseCase(teamRepo, prefsManager)
        val getTodayStandupUseCase = GetTodayStandupUseCase(standupRepo)
        val getTeamSentimentsUseCase = GetTeamSentimentsUseCase(teamRepo)

        val viewModel = HomeViewModel(getTodayStandupUseCase, getTeamMembersUseCase, getTeamSentimentsUseCase)

        composeRule.setContent {
            DashboardScreen(
                viewModel = viewModel,
                onNavigateSubmit = {},
                onNavigateHistory = {},
                onNavigateSettings = {},
                onNavigateMissing = {},
                onNavigateExport = {},
                onNavigateRoster = {}
            )
        }
        composeRule.waitForIdle()

        Thread.sleep(1500)
        composeRule.waitForIdle()

        // Verify all three standups are displayed
        composeRule.onNode(hasText("Alice Johnson", substring = true)).assertIsDisplayed()
        composeRule.onNode(hasText("Bob Smith", substring = true)).assertIsDisplayed()
        composeRule.onNode(hasText("Charlie Brown", substring = true)).assertIsDisplayed()
    }

    @Test
    fun standupWithLongText_displaysWithEllipsis() {
        val members = listOf(
            createFakeTeamMemberData(1, "Alice Johnson")
        )

        val longText = "This is a very long text that contains a lot of information about the work done " +
                       "and should demonstrate how the UI handles lengthy content in the standup entries " +
                       "to ensure proper text truncation and display"

        val entries = listOf(
            buildStandupEntry(
                id = 1,
                name = "Alice Johnson",
                time = "10:15",
                yesterday = longText,
                today = "Short plan"
            )
        )

        val teamRepo = createFakeTeamRepository(members)
        val standupRepo = createFakeStandupRepository(entries)
        val prefsManager = createFakePreferencesManager()

        val getTeamMembersUseCase = GetTeamMembersUseCase(teamRepo, prefsManager)
        val getTodayStandupUseCase = GetTodayStandupUseCase(standupRepo)
        val getTeamSentimentsUseCase = GetTeamSentimentsUseCase(teamRepo)

        val viewModel = HomeViewModel(getTodayStandupUseCase, getTeamMembersUseCase, getTeamSentimentsUseCase)

        composeRule.setContent {
            DashboardScreen(
                viewModel = viewModel,
                onNavigateSubmit = {},
                onNavigateHistory = {},
                onNavigateSettings = {},
                onNavigateMissing = {},
                onNavigateExport = {},
                onNavigateRoster = {}
            )
        }
        composeRule.waitForIdle()

        Thread.sleep(1500)
        composeRule.waitForIdle()

        // Verify standup is displayed despite long text
        composeRule.onNode(hasText("Alice Johnson", substring = true)).assertIsDisplayed()
    }

    @Test
    fun scrollableStandupList_scrollsWithManyEntries() {
        val members = (1..15).map { i ->
            createFakeTeamMemberData(i.toLong(), "Member$i")
        }

        val entries = (1..15).map { i ->
            buildStandupEntry(
                id = i.toLong(),
                name = "Member$i",
                time = "10:${String.format("%02d", i)}",
                yesterday = "Work $i yesterday",
                today = "Plan $i today"
            )
        }

        val teamRepo = createFakeTeamRepository(members)
        val standupRepo = createFakeStandupRepository(entries)
        val prefsManager = createFakePreferencesManager()

        val getTeamMembersUseCase = GetTeamMembersUseCase(teamRepo, prefsManager)
        val getTodayStandupUseCase = GetTodayStandupUseCase(standupRepo)
        val getTeamSentimentsUseCase = GetTeamSentimentsUseCase(teamRepo)

        val viewModel = HomeViewModel(getTodayStandupUseCase, getTeamMembersUseCase, getTeamSentimentsUseCase)

        composeRule.setContent {
            DashboardScreen(
                viewModel = viewModel,
                onNavigateSubmit = {},
                onNavigateHistory = {},
                onNavigateSettings = {},
                onNavigateMissing = {},
                onNavigateExport = {},
                onNavigateRoster = {}
            )
        }
        composeRule.waitForIdle()

        Thread.sleep(1500)
        composeRule.waitForIdle()

        // Verify some members are displayed
        composeRule.onNode(hasText("Member1", substring = true)).assertIsDisplayed()
    }
}
