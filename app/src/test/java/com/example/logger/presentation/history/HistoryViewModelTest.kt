package com.example.logger.presentation.history

import com.example.logger.core.network.NetworkResult
import com.example.logger.core.util.DateFormatter
import com.example.logger.domain.model.PaginatedStandupEntriesData
import com.example.logger.domain.model.PaginationMetaData
import com.example.logger.domain.model.StandupEntryData
import com.example.logger.domain.model.TeamMember
import com.example.logger.domain.usecase.GetTodayStandupUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {
    private lateinit var getTodayStandupUseCase: GetTodayStandupUseCase
    private lateinit var viewModel: HistoryViewModel
    private val testDispatcher = StandardTestDispatcher()

    private fun yesterday(): Date {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_MONTH, -1)
        return cal.time
    }

    private val testDateStr = DateFormatter.getApiDateFormat().format(yesterday())

    private fun standup(id: Long, dateStr: String): StandupEntryData = StandupEntryData(
        id = id,
        standupDate = dateStr,
        yesterdayWork = "Y$id",
        todayPlan = "T$id",
        blockers = null,
        teamMemberId = 1,
        teamId = 1,
        createdAt = null,
        updatedAt = null,
        teamMember = TeamMember(
            id = 1,
            name = "Test User",
            email = "test@example.com"
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getTodayStandupUseCase = mock()
        // Default stub: one item, page 0/1
        val first = PaginatedStandupEntriesData(
            items = listOf(standup(1, testDateStr)),
            meta = PaginationMetaData(page = 0, size = 10, totalElements = 1, totalPages = 1)
        )
        runBlocking {
            whenever(
                getTodayStandupUseCase.invoke(eq(1L), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
            ).thenReturn(NetworkResult.Success(first))
        }
        viewModel = HistoryViewModel(getTodayStandupUseCase)
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads yesterday submissions successfully`() {
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals(1, state.submissions.size)
        assertEquals(0, state.currentPage)
        assertFalse(state.canLoadMore)
    }

    @Test
    fun `onPrevDate moves back a day and refreshes list`() {
        val second = PaginatedStandupEntriesData(
            items = listOf(standup(2, testDateStr), standup(3, testDateStr)),
            meta = PaginationMetaData(page = 0, size = 10, totalElements = 2, totalPages = 1)
        )
        runBlocking {
            whenever(
                getTodayStandupUseCase.invoke(eq(1L), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
            ).thenReturn(NetworkResult.Success(second))
        }
        val prevSelected = viewModel.uiState.value.selectedDate
        viewModel.onPrevDate()
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value
        assertTrue(state.selectedDate.before(prevSelected))
        assertEquals(2, state.submissions.size)
        assertEquals(0, state.currentPage)
    }

    @Test
    fun `onNextDate does not go beyond yesterday`() {
        val before = viewModel.uiState.value.selectedDate // default is yesterday
        viewModel.onNextDate()
        testDispatcher.scheduler.advanceUntilIdle()
        val after = viewModel.uiState.value.selectedDate
        // Should remain unchanged because next would be today
        assertEquals(before, after)
    }

    @Test
    fun `onNextDate from older date advances up to yesterday`() {
        // go one more day back
        viewModel.onPrevDate()
        val before = viewModel.uiState.value.selectedDate
        val newData = PaginatedStandupEntriesData(
            items = listOf(standup(4, testDateStr)),
            meta = PaginationMetaData(page = 0, size = 10, totalElements = 1, totalPages = 1)
        )
        runBlocking {
            whenever(
                getTodayStandupUseCase.invoke(eq(1L), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
            ).thenReturn(NetworkResult.Success(newData))
        }
        viewModel.onNextDate()
        testDispatcher.scheduler.advanceUntilIdle()
        val after = viewModel.uiState.value.selectedDate
        assertTrue(after.after(before))
        assertEquals(1, viewModel.uiState.value.submissions.size)
    }

    @Test
    fun `onPickDate sets date and refreshes`() {
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -3) }
        val pick = cal.time
        val pickData = PaginatedStandupEntriesData(
            items = listOf(standup(5, DateFormatter.getApiDateFormat().format(pick))),
            meta = PaginationMetaData(page = 0, size = 10, totalElements = 1, totalPages = 1)
        )
        runBlocking {
            whenever(
                getTodayStandupUseCase.invoke(eq(1L), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
            ).thenReturn(NetworkResult.Success(pickData))
        }
        viewModel.onPickDate(pick)
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals(pick, state.selectedDate)
        assertEquals(1, state.submissions.size)
    }

    @Test
    fun `loadMore appends items when canLoadMore`() {
        // First make state canLoadMore by returning totalPages=2
        val page0 = PaginatedStandupEntriesData(
            items = listOf(standup(10, testDateStr)),
            meta = PaginationMetaData(page = 0, size = 10, totalElements = 2, totalPages = 2)
        )
        val page1 = PaginatedStandupEntriesData(
            items = listOf(standup(11, testDateStr)),
            meta = PaginationMetaData(page = 1, size = 10, totalElements = 2, totalPages = 2)
        )
        runBlocking {
            whenever(
                getTodayStandupUseCase.invoke(eq(1L), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
            ).thenReturn(NetworkResult.Success(page0))
        }
        // trigger refresh to set canLoadMore = true
        viewModel.onPickDate(yesterday())
        testDispatcher.scheduler.advanceUntilIdle()
        // next call returns page1
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
    }

    @Test
    fun `error on refresh sets error and clears list`() {
        runBlocking {
            whenever(
                getTodayStandupUseCase.invoke(eq(1L), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
            ).thenReturn(NetworkResult.Error(message = "boom"))
        }
        viewModel.onPickDate(yesterday())
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals("boom", state.error)
        assertTrue(state.submissions.isEmpty())
        assertFalse(state.isLoading)
        assertFalse(state.isLoadingMore)
    }

    @Test
    fun `error on loadMore keeps existing list and sets error`() {
        // prepare list with canLoadMore true
        val page0 = PaginatedStandupEntriesData(
            items = listOf(standup(20, testDateStr)),
            meta = PaginationMetaData(page = 0, size = 10, totalElements = 2, totalPages = 2)
        )
        runBlocking {
            whenever(
                getTodayStandupUseCase.invoke(eq(1L), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
            ).thenReturn(NetworkResult.Success(page0))
        }
        viewModel.onPickDate(yesterday())
        testDispatcher.scheduler.advanceUntilIdle()
        // now error on loadMore
        runBlocking {
            whenever(
                getTodayStandupUseCase.invoke(eq(1L), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
            ).thenReturn(NetworkResult.Error(message = "err"))
        }
        val before = viewModel.uiState.value.submissions
        viewModel.loadMore()
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals(before.size, state.submissions.size)
        assertEquals("err", state.error)
        assertFalse(state.isLoading)
        assertFalse(state.isLoadingMore)
    }
}

