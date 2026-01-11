package com.example.logger.presentation.submitstandup

import app.cash.turbine.test
import com.example.logger.core.datastore.PreferencesManager
import com.example.logger.core.network.NetworkResult
import com.example.logger.domain.model.TeamMemberData
import com.example.logger.domain.usecase.GetTeamMembersUseCase
import com.example.logger.domain.usecase.SubmitStandupUseCase
import com.example.logger.presentation.submitstandup.mapper.SubmitStandupUiMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)

class SubmitStandupViewModelTest {
    private lateinit var viewModel: SubmitStandupViewModel
    private lateinit var submitUseCase: SubmitStandupUseCase
    private lateinit var uiMapper: SubmitStandupUiMapper
    private lateinit var getTeamMembersUseCase: GetTeamMembersUseCase
    private lateinit var preferencesManager: PreferencesManager
    private val testDispatcher = UnconfinedTestDispatcher()
    // private val testScope = TestScope(testDispatcher)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        submitUseCase = mock()
        uiMapper = mock()
        getTeamMembersUseCase = mock()
        preferencesManager = mock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads team members and sets roster`() = runTest(testDispatcher) {
        val teamMembers = listOf(
            TeamMemberData(1, "Alice", "alice@email.com", "", ""),
            TeamMemberData(2, "Bob", "bob@email.com", "", "")
        )
        whenever(getTeamMembersUseCase.invoke(any(), any(), any(), any())).thenReturn(flowOf(NetworkResult.Success(teamMembers)))
        whenever(preferencesManager.getTeamMembers()).thenReturn(flowOf(teamMembers))
        viewModel = SubmitStandupViewModel(submitUseCase, uiMapper, getTeamMembersUseCase, preferencesManager)
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals(listOf("Alice", "Bob"), state.roster)
        assertEquals("Alice", state.name)
    }

    @Test
    fun `submit with blank fields sets error flags`() = runTest(testDispatcher) {
        whenever(getTeamMembersUseCase.invoke(any(), any(), any(), any())).thenReturn(flowOf(NetworkResult.Success(emptyList())))
        whenever(preferencesManager.getTeamMembers()).thenReturn(flowOf(emptyList()))
        viewModel = SubmitStandupViewModel(submitUseCase, uiMapper, getTeamMembersUseCase, preferencesManager)
        advanceUntilIdle()
        viewModel.submit { }
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertTrue(state.nameError)
        assertTrue(state.yesterdayError)
        assertTrue(state.todayError)
        assertNull(state.error)
    }

    @Test
    fun `submit success resets fields and emits Submitted event`() = runTest(testDispatcher) {
        val teamMembers = listOf(TeamMemberData(1, "Alice", "alice@email.com", "", ""))
        whenever(getTeamMembersUseCase.invoke(any(), any(), any(), any())).thenReturn(flowOf(NetworkResult.Success(teamMembers)))
        whenever(preferencesManager.getTeamMembers()).thenReturn(flowOf(teamMembers))
        whenever(uiMapper.toRequest(any(), any(), any(), any())).thenReturn(mock())
        val standupEntryData = com.example.logger.domain.model.StandupEntryData(
            id = 1L,
            standupDate = "2024-01-01",
            yesterdayWork = "Did stuff",
            todayPlan = "Do more stuff",
            blockers = "None",
            teamMemberId = 1L,
            teamId = 1L,
            createdAt = null,
            updatedAt = null,
            teamMember = com.example.logger.domain.model.TeamMember(
                id = 1L,
                name = "Alice",
                email = "alice@example.com"
            )
        )
        whenever(submitUseCase.invoke(any())).thenReturn(NetworkResult.Success(standupEntryData))
        viewModel = SubmitStandupViewModel(submitUseCase, uiMapper, getTeamMembersUseCase, preferencesManager)
        advanceUntilIdle()
        viewModel.onNameChange("Alice")
        viewModel.onYesterdayChange("Did stuff")
        viewModel.onTodayChange("Do more stuff")
        viewModel.onBlockersChange("None")
        viewModel.events.test {
            viewModel.submit { }
            advanceUntilIdle()
            assertEquals(false, viewModel.uiState.value.isSubmitting)
            assertEquals("", viewModel.uiState.value.yesterday)
            assertEquals("", viewModel.uiState.value.today)
            assertEquals("", viewModel.uiState.value.blockers)
            assertEquals("Alice", viewModel.uiState.value.name)
            assertEquals(SubmitStandupUiEvent.Submitted, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `submit error sets error and emits ApiError event`() = runTest(testDispatcher) {
        val teamMembers = listOf(TeamMemberData(1, "Alice", "alice@email.com", "", ""))
        whenever(getTeamMembersUseCase.invoke(any(), any(), any(), any())).thenReturn(flowOf(NetworkResult.Success(teamMembers)))
        whenever(preferencesManager.getTeamMembers()).thenReturn(flowOf(teamMembers))
        whenever(uiMapper.toRequest(any(), any(), any(), any())).thenReturn(mock())
        whenever(submitUseCase.invoke(any())).thenReturn(NetworkResult.Error("fail"))
        viewModel = SubmitStandupViewModel(submitUseCase, uiMapper, getTeamMembersUseCase, preferencesManager)
        advanceUntilIdle()
        viewModel.onNameChange("Alice")
        viewModel.onYesterdayChange("Did stuff")
        viewModel.onTodayChange("Do more stuff")
        viewModel.onBlockersChange("None")
        viewModel.events.test {
            viewModel.submit { }
            advanceUntilIdle()
            assertEquals(false, viewModel.uiState.value.isSubmitting)
            assertEquals("fail", viewModel.uiState.value.error)
            assertEquals(SubmitStandupUiEvent.ApiError("fail"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
