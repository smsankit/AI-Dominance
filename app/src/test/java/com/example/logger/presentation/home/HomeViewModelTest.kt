package com.example.logger.presentation.home

import com.example.logger.core.network.NetworkResult
import com.example.logger.core.util.DateFormatter
import com.example.logger.domain.model.PaginatedStandupEntriesData
import com.example.logger.domain.model.PaginationMetaData
import com.example.logger.domain.model.StandupEntryData
import com.example.logger.domain.model.TeamMember
import com.example.logger.domain.model.TeamMemberData
import com.example.logger.domain.usecase.GetTeamMembersUseCase
import com.example.logger.domain.usecase.GetTodayStandupUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private lateinit var getTodayStandupUseCase: GetTodayStandupUseCase
    private lateinit var getTeamMembersUseCase: GetTeamMembersUseCase
    private lateinit var viewModel: HomeViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val memberAlice = TeamMemberData(id = 1, name = "Alice", email = "a@a.com", createdAt = "", updatedAt = "" )
    private val memberBob = TeamMemberData(id = 2, name = "Bob", email = "b@b.com", createdAt = "", updatedAt = "" )
    private val teamMembers = listOf(memberAlice, memberBob)

    private fun entry(id: Long, memberId: Long, createdAt: String? = "09:00", updatedAt: String? = null): StandupEntryData =
        StandupEntryData(
            id = id,
            standupDate = DateFormatter.getCurrentDateString(),
            yesterdayWork = "Y$id",
            todayPlan = "T$id",
            blockers = null,
            teamMemberId = memberId,
            teamId = 1,
            createdAt = createdAt,
            updatedAt = updatedAt,
            teamMember = TeamMember(
                id = memberId,
                name = if (memberId == memberAlice.id) "Alice" else "Bob",
                email = if (memberId == memberAlice.id) "a@a.com" else "b@b.com"
            )
        )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getTodayStandupUseCase = mock()
        getTeamMembersUseCase = mock()
        // Team members flow success by default
        whenever(getTeamMembersUseCase.invoke(eq(1L), eq(0), eq(100), eq(true))).thenReturn(flowOf(NetworkResult.Success(teamMembers)))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads team members, roster, then standups and pending`() {
        val page = PaginatedStandupEntriesData(
            items = listOf(entry(1, memberAlice.id)),
            meta = PaginationMetaData(page = 0, size = 10, totalElements = 1, totalPages = 1)
        )
        runBlocking {
            whenever(
                getTodayStandupUseCase.invoke(eq(1L), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
            ).thenReturn(NetworkResult.Success(page))
        }
        viewModel = HomeViewModel(getTodayStandupUseCase, getTeamMembersUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        // roster mapped
        assertEquals(listOf("Alice", "Bob"), state.roster)
        // submissions mapped from entries to Standup
        assertEquals(1, state.submissions.size)
        assertEquals("Alice", state.submissions.first().name)
        assertEquals("09:00", state.submissions.first().time)
        // pending names from members not submitted (Bob)
        assertEquals(listOf("Bob"), state.pending)
        // pendingCount from roster.size - totalElements
        assertEquals(1, state.pendingCount)
        assertEquals(DateFormatter.getCurrentDateString(), state.date)
        assertFalse(state.isLoading)
        assertFalse(state.isLoadingMore)
        assertFalse(state.canLoadMore)
    }

    @Test
    fun `loadMore appends and updates pagination`() {
        val page0 = PaginatedStandupEntriesData(
            items = listOf(entry(1, memberAlice.id)),
            meta = PaginationMetaData(page = 0, size = 10, totalElements = 2, totalPages = 2)
        )
        val page1 = PaginatedStandupEntriesData(
            items = listOf(entry(2, memberBob.id, createdAt = "10:00")),
            meta = PaginationMetaData(page = 1, size = 10, totalElements = 2, totalPages = 2)
        )
        runBlocking {
            whenever(
                getTodayStandupUseCase.invoke(eq(1L), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
            ).thenReturn(NetworkResult.Success(page0))
        }
        viewModel = HomeViewModel(getTodayStandupUseCase, getTeamMembersUseCase)
        testDispatcher.scheduler.advanceUntilIdle()
        // canLoadMore true after first load
        assertTrue(viewModel.uiState.value.canLoadMore)

        // Next call returns page1
        runBlocking {
            whenever(
                getTodayStandupUseCase.invoke(eq(1L), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
            ).thenReturn(NetworkResult.Success(page1))
        }
        viewModel.loadMore()
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals(2, state.submissions.size)
        assertEquals(1, state.currentPage)
        assertFalse(state.canLoadMore)
        // verify mapped second entry
        assertEquals("Bob", state.submissions.last().name)
        assertEquals("10:00", state.submissions.last().time)
    }

    @Test
    fun `error during load sets error and flags`() {
        runBlocking {
            whenever(
                getTodayStandupUseCase.invoke(eq(1L), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
            ).thenReturn(NetworkResult.Error(message = "fail"))
        }
        viewModel = HomeViewModel(getTodayStandupUseCase, getTeamMembersUseCase)
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals("fail", state.error)
        assertFalse(state.isLoading)
        assertFalse(state.isLoadingMore)
        // submissions remain empty on error
        assertTrue(state.submissions.isEmpty())
    }
}

