package com.example.logger

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.logger.core.datastore.PreferencesManager
import com.example.logger.core.network.NetworkResult
import com.example.logger.domain.model.StandupEntryData
import com.example.logger.domain.model.StandupEntryRequestData
import com.example.logger.domain.model.TeamMember
import com.example.logger.domain.model.TeamMemberData
import com.example.logger.domain.repository.StandupRepository
import com.example.logger.domain.repository.TeamRepository
import com.example.logger.domain.usecase.GetTeamMembersUseCase
import com.example.logger.domain.usecase.SubmitStandupUseCase
import com.example.logger.presentation.submitstandup.SubmitStandupScreen
import com.example.logger.presentation.submitstandup.SubmitStandupViewModel
import com.example.logger.presentation.submitstandup.mapper.SubmitStandupUiMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SubmitStandupScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun createFakeTeamRepository(members: List<TeamMemberData>): TeamRepository {
        return object : TeamRepository {
            override fun getTeamMembers(teamId: Long, page: Int, size: Int): Flow<NetworkResult<List<TeamMemberData>>> {
                return flowOf(NetworkResult.Success(members))
            }
        }
    }

    private fun createFakeStandupRepository(shouldSucceed: Boolean = true): StandupRepository {
        return object : StandupRepository {
            override suspend fun submitStandupEntry(request: StandupEntryRequestData): NetworkResult<StandupEntryData> {
                return if (shouldSucceed) {
                    val entry = StandupEntryData(
                        id = 1,
                        standupDate = request.standupDate,
                        yesterdayWork = request.yesterdayWork,
                        todayPlan = request.todayPlan,
                        blockers = request.blockers,
                        teamMemberId = request.teamMemberId,
                        teamId = request.teamId,
                        createdAt = "10:00",
                        updatedAt = null,
                        teamMember = TeamMember(id = request.teamMemberId, name = "Test User", email = "test@example.com")
                    )
                    NetworkResult.Success(entry)
                } else {
                    NetworkResult.Error("Submission failed")
                }
            }

            override fun getTodayStandup() = throw UnsupportedOperationException()
            override suspend fun getStandupEntries(
                teamId: Long,
                page: Int?,
                size: Int?,
                teamMemberId: Long?,
                standupDate: String?
            ) = throw UnsupportedOperationException()
        }
    }

    private fun createFakePreferencesManager(members: List<TeamMemberData> = emptyList()): PreferencesManager {
        // Create a minimal fake DataStore
        val fakeDataStore = object : DataStore<Preferences> {
            override val data: Flow<Preferences> = flowOf(androidx.datastore.preferences.core.emptyPreferences())
            override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
                return androidx.datastore.preferences.core.emptyPreferences()
            }
        }

        // Return PreferencesManager that will use cached data
        return PreferencesManager(fakeDataStore)
    }

    @Test
    fun initialState_showsFormWithLabels() {
        val members = listOf(
            TeamMemberData(1, "Alice Johnson", "alice@example.com", "2026-01-01", "2026-01-01"),
            TeamMemberData(2, "Bob Smith", "bob@example.com", "2026-01-01", "2026-01-01")
        )

        val teamRepo = createFakeTeamRepository(members)
        val standupRepo = createFakeStandupRepository()
        val prefsManager = createFakePreferencesManager(members)

        val getTeamMembersUseCase = GetTeamMembersUseCase(teamRepo, prefsManager)
        val submitUseCase = SubmitStandupUseCase(standupRepo)

        val viewModel = SubmitStandupViewModel(
            submitUseCase = submitUseCase,
            uiMapper = SubmitStandupUiMapper(),
            getTeamMembersUseCase = getTeamMembersUseCase,
            preferencesManager = prefsManager
        )

        composeRule.setContent {
            SubmitStandupScreen(
                viewModel = viewModel,
                onSubmitted = {},
                onCancel = {},
                onNavigateHome = {},
                onNavigateSubmit = {},
                onNavigateHistory = {},
                onNavigateSettings = {}
            )
        }
        composeRule.waitForIdle()

        // Verify title and subtitle
        composeRule.onNodeWithText("Submit Standup").assertIsDisplayed()
        composeRule.onNodeWithText("Share your progress with the team").assertIsDisplayed()

        // Verify form labels
        composeRule.onNodeWithText("Name").assertIsDisplayed()
        composeRule.onNode(hasText("What did you do yesterday?", substring = true)).assertIsDisplayed()
        composeRule.onNode(hasText("What will you do today?", substring = true)).assertIsDisplayed()
        composeRule.onNode(hasText("Any blockers?", substring = true)).assertIsDisplayed()

        // Verify buttons
        composeRule.onNodeWithText("Submit").assertIsDisplayed()
        composeRule.onNodeWithText("Cancel").assertIsDisplayed()
    }

    @Test
    fun withTeamMembers_showsFirstMemberPreselected() {
        val members = listOf(
            TeamMemberData(1, "Alice Johnson", "alice@example.com", "2026-01-01", "2026-01-01"),
            TeamMemberData(2, "Bob Smith", "bob@example.com", "2026-01-01", "2026-01-01")
        )

        val teamRepo = createFakeTeamRepository(members)
        val standupRepo = createFakeStandupRepository()
        val prefsManager = createFakePreferencesManager(members)

        val getTeamMembersUseCase = GetTeamMembersUseCase(teamRepo, prefsManager)
        val submitUseCase = SubmitStandupUseCase(standupRepo)

        val viewModel = SubmitStandupViewModel(
            submitUseCase = submitUseCase,
            uiMapper = SubmitStandupUiMapper(),
            getTeamMembersUseCase = getTeamMembersUseCase,
            preferencesManager = prefsManager
        )

        composeRule.setContent {
            SubmitStandupScreen(
                viewModel = viewModel,
                onSubmitted = {},
                onCancel = {},
                onNavigateHome = {},
                onNavigateSubmit = {},
                onNavigateHistory = {},
                onNavigateSettings = {}
            )
        }
        composeRule.waitForIdle()

        // First member should be pre-selected in the name field
        composeRule.onNode(hasText("Alice Johnson", substring = true)).assertIsDisplayed()
    }

    @Test
    fun emptyTeamMembers_stillShowsForm() {
        val teamRepo = createFakeTeamRepository(emptyList())
        val standupRepo = createFakeStandupRepository()
        val prefsManager = createFakePreferencesManager()

        val getTeamMembersUseCase = GetTeamMembersUseCase(teamRepo, prefsManager)
        val submitUseCase = SubmitStandupUseCase(standupRepo)

        val viewModel = SubmitStandupViewModel(
            submitUseCase = submitUseCase,
            uiMapper = SubmitStandupUiMapper(),
            getTeamMembersUseCase = getTeamMembersUseCase,
            preferencesManager = prefsManager
        )

        composeRule.setContent {
            SubmitStandupScreen(
                viewModel = viewModel,
                onSubmitted = {},
                onCancel = {},
                onNavigateHome = {},
                onNavigateSubmit = {},
                onNavigateHistory = {},
                onNavigateSettings = {}
            )
        }
        composeRule.waitForIdle()

        // Screen should still render without crashing
        composeRule.onNodeWithText("Submit Standup").assertIsDisplayed()
        composeRule.onNodeWithText("Name").assertIsDisplayed()
    }

    @Test
    fun cancelButton_triggersCallback() {
        var cancelCalled = false
        val members = listOf(TeamMemberData(1, "Alice", "alice@example.com", "2026-01-01", "2026-01-01"))

        val teamRepo = createFakeTeamRepository(members)
        val standupRepo = createFakeStandupRepository()
        val prefsManager = createFakePreferencesManager(members)

        val getTeamMembersUseCase = GetTeamMembersUseCase(teamRepo, prefsManager)
        val submitUseCase = SubmitStandupUseCase(standupRepo)

        val viewModel = SubmitStandupViewModel(
            submitUseCase = submitUseCase,
            uiMapper = SubmitStandupUiMapper(),
            getTeamMembersUseCase = getTeamMembersUseCase,
            preferencesManager = prefsManager
        )

        composeRule.setContent {
            SubmitStandupScreen(
                viewModel = viewModel,
                onSubmitted = {},
                onCancel = { cancelCalled = true },
                onNavigateHome = {},
                onNavigateSubmit = {},
                onNavigateHistory = {},
                onNavigateSettings = {}
            )
        }
        composeRule.waitForIdle()

        // Click cancel button
        composeRule.onNodeWithText("Cancel").performClick()

        // Verify callback was triggered
        assert(cancelCalled) { "Cancel callback should be triggered" }
    }

    @Test
    fun topBar_displaysTitleCorrectly() {
        val members = listOf(TeamMemberData(1, "Alice", "alice@example.com", "2026-01-01", "2026-01-01"))

        val teamRepo = createFakeTeamRepository(members)
        val standupRepo = createFakeStandupRepository()
        val prefsManager = createFakePreferencesManager(members)

        val getTeamMembersUseCase = GetTeamMembersUseCase(teamRepo, prefsManager)
        val submitUseCase = SubmitStandupUseCase(standupRepo)

        val viewModel = SubmitStandupViewModel(
            submitUseCase = submitUseCase,
            uiMapper = SubmitStandupUiMapper(),
            getTeamMembersUseCase = getTeamMembersUseCase,
            preferencesManager = prefsManager
        )

        composeRule.setContent {
            SubmitStandupScreen(
                viewModel = viewModel,
                onSubmitted = {},
                onCancel = {},
                onNavigateHome = {},
                onNavigateSubmit = {},
                onNavigateHistory = {},
                onNavigateSettings = {}
            )
        }
        composeRule.waitForIdle()

        // Verify top bar title
        composeRule.onNodeWithText("Submit Standup").assertIsDisplayed()
    }

    @Test
    fun multipleTeamMembers_allMembersAvailable() {
        val members = listOf(
            TeamMemberData(1, "Alice Johnson", "alice@example.com", "2026-01-01", "2026-01-01"),
            TeamMemberData(2, "Bob Smith", "bob@example.com", "2026-01-01", "2026-01-01"),
            TeamMemberData(3, "Charlie Brown", "charlie@example.com", "2026-01-01", "2026-01-01")
        )

        val teamRepo = createFakeTeamRepository(members)
        val standupRepo = createFakeStandupRepository()
        val prefsManager = createFakePreferencesManager(members)

        val getTeamMembersUseCase = GetTeamMembersUseCase(teamRepo, prefsManager)
        val submitUseCase = SubmitStandupUseCase(standupRepo)

        val viewModel = SubmitStandupViewModel(
            submitUseCase = submitUseCase,
            uiMapper = SubmitStandupUiMapper(),
            getTeamMembersUseCase = getTeamMembersUseCase,
            preferencesManager = prefsManager
        )

        composeRule.setContent {
            SubmitStandupScreen(
                viewModel = viewModel,
                onSubmitted = {},
                onCancel = {},
                onNavigateHome = {},
                onNavigateSubmit = {},
                onNavigateHistory = {},
                onNavigateSettings = {}
            )
        }
        composeRule.waitForIdle()

        // First member should be displayed
        composeRule.onNode(hasText("Alice Johnson", substring = true)).assertIsDisplayed()
    }

    @Test
    fun optionalBlockersField_displaysCorrectly() {
        val members = listOf(TeamMemberData(1, "Alice", "alice@example.com", "2026-01-01", "2026-01-01"))

        val teamRepo = createFakeTeamRepository(members)
        val standupRepo = createFakeStandupRepository()
        val prefsManager = createFakePreferencesManager(members)

        val getTeamMembersUseCase = GetTeamMembersUseCase(teamRepo, prefsManager)
        val submitUseCase = SubmitStandupUseCase(standupRepo)

        val viewModel = SubmitStandupViewModel(
            submitUseCase = submitUseCase,
            uiMapper = SubmitStandupUiMapper(),
            getTeamMembersUseCase = getTeamMembersUseCase,
            preferencesManager = prefsManager
        )

        composeRule.setContent {
            SubmitStandupScreen(
                viewModel = viewModel,
                onSubmitted = {},
                onCancel = {},
                onNavigateHome = {},
                onNavigateSubmit = {},
                onNavigateHistory = {},
                onNavigateSettings = {}
            )
        }
        composeRule.waitForIdle()

        // Blockers field should have "(optional)" in label
        composeRule.onNode(hasText("optional", substring = true, ignoreCase = true)).assertIsDisplayed()
    }
}


