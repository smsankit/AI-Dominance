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
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
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
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads yesterday submissions successfully`() = runTest {
        getTodayStandupUseCase = mock()
        val first = PaginatedStandupEntriesData(
            items = listOf(standup(1, testDateStr)),
            meta = PaginationMetaData(page = 0, size = 10, totalElements = 1, totalPages = 1)
        )
        val missing = PaginatedStandupEntriesData(
            items = emptyList(),
            meta = PaginationMetaData(page = 0, size = 100, totalElements = 0, totalPages = 1)
        )

        whenever(
            getTodayStandupUseCase.invoke(
                eq(1L),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                eq(null)
            )
        ).thenReturn(NetworkResult.Success(first))

        whenever(
            getTodayStandupUseCase.invoke(
                eq(1L),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                eq("MISSING")
            )
        ).thenReturn(NetworkResult.Success(missing))

        viewModel = HistoryViewModel(getTodayStandupUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals(1, state.submissions.size)
        assertEquals(0, state.currentPage)
        assertFalse(state.canLoadMore)
    }

    @Test
    fun `onPrevDate moves back a day and refreshes list`() = runTest {
        getTodayStandupUseCase = mock()
        val first = PaginatedStandupEntriesData(
            items = listOf(standup(1, testDateStr)),
            meta = PaginationMetaData(page = 0, size = 10, totalElements = 1, totalPages = 1)
        )
        val second = PaginatedStandupEntriesData(
            items = listOf(standup(2, testDateStr), standup(3, testDateStr)),
            meta = PaginationMetaData(page = 0, size = 10, totalElements = 2, totalPages = 1)
        )
        val missing = PaginatedStandupEntriesData(
            items = emptyList(),
            meta = PaginationMetaData(page = 0, size = 100, totalElements = 0, totalPages = 1)
        )

        whenever(
            getTodayStandupUseCase.invoke(
                eq(1L),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                eq(null)
            )
        ).thenReturn(NetworkResult.Success(first))
            .thenReturn(NetworkResult.Success(second))

        whenever(
            getTodayStandupUseCase.invoke(
                eq(1L),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                eq("MISSING")
            )
        ).thenReturn(NetworkResult.Success(missing))

        viewModel = HistoryViewModel(getTodayStandupUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        val prevSelected = viewModel.uiState.value.selectedDate
        viewModel.onPrevDate()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.selectedDate.before(prevSelected))
        assertEquals(2, state.submissions.size)
        assertEquals(0, state.currentPage)
    }

    @Test
    fun `onNextDate does not go beyond yesterday`() = runTest {
        getTodayStandupUseCase = mock()
        val first = PaginatedStandupEntriesData(
            items = listOf(standup(1, testDateStr)),
            meta = PaginationMetaData(page = 0, size = 10, totalElements = 1, totalPages = 1)
        )
        val missing = PaginatedStandupEntriesData(
            items = emptyList(),
            meta = PaginationMetaData(page = 0, size = 100, totalElements = 0, totalPages = 1)
        )

        whenever(
            getTodayStandupUseCase.invoke(
                eq(1L),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                eq(null)
            )
        ).thenReturn(NetworkResult.Success(first))

        whenever(
            getTodayStandupUseCase.invoke(
                eq(1L),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                eq("MISSING")
            )
        ).thenReturn(NetworkResult.Success(missing))

        viewModel = HistoryViewModel(getTodayStandupUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        val before = viewModel.uiState.value.selectedDate
        viewModel.onNextDate()
        testDispatcher.scheduler.advanceUntilIdle()

        val after = viewModel.uiState.value.selectedDate
        assertEquals(before, after)
    }

    @Test
    fun `onNextDate from older date advances up to yesterday`() = runTest {
        getTodayStandupUseCase = mock()
        val first = PaginatedStandupEntriesData(
            items = listOf(standup(1, testDateStr)),
            meta = PaginationMetaData(page = 0, size = 10, totalElements = 1, totalPages = 1)
        )
        val newData = PaginatedStandupEntriesData(
            items = listOf(standup(4, testDateStr)),
            meta = PaginationMetaData(page = 0, size = 10, totalElements = 1, totalPages = 1)
        )
        val missing = PaginatedStandupEntriesData(
            items = emptyList(),
            meta = PaginationMetaData(page = 0, size = 100, totalElements = 0, totalPages = 1)
        )

        whenever(
            getTodayStandupUseCase.invoke(
                eq(1L),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                eq(null)
            )
        ).thenReturn(NetworkResult.Success(first))
            .thenReturn(NetworkResult.Success(first))
            .thenReturn(NetworkResult.Success(newData))

        whenever(
            getTodayStandupUseCase.invoke(
                eq(1L),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                eq("MISSING")
            )
        ).thenReturn(NetworkResult.Success(missing))

        viewModel = HistoryViewModel(getTodayStandupUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onPrevDate()
        testDispatcher.scheduler.advanceUntilIdle()
        val before = viewModel.uiState.value.selectedDate

        viewModel.onNextDate()
        testDispatcher.scheduler.advanceUntilIdle()

        val after = viewModel.uiState.value.selectedDate
        assertTrue(after.after(before))
        assertEquals(1, viewModel.uiState.value.submissions.size)
    }

    @Test
    fun `onPickDate sets date and refreshes`() = runTest {
        getTodayStandupUseCase = mock()
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -3) }
        val pick = cal.time
        val pickData = PaginatedStandupEntriesData(
            items = listOf(standup(5, DateFormatter.getApiDateFormat().format(pick))),
            meta = PaginationMetaData(page = 0, size = 10, totalElements = 1, totalPages = 1)
        )
        val missing = PaginatedStandupEntriesData(
            items = emptyList(),
            meta = PaginationMetaData(page = 0, size = 100, totalElements = 0, totalPages = 1)
        )

        whenever(
            getTodayStandupUseCase.invoke(
                eq(1L),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                eq(null)
            )
        ).thenReturn(NetworkResult.Success(pickData))

        whenever(
            getTodayStandupUseCase.invoke(
                eq(1L),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                eq("MISSING")
            )
        ).thenReturn(NetworkResult.Success(missing))

        viewModel = HistoryViewModel(getTodayStandupUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onPickDate(pick)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(pick, state.selectedDate)
        assertEquals(1, state.submissions.size)
    }

    @Test
    fun `loadMore appends items when canLoadMore`() = runTest {
        getTodayStandupUseCase = mock()
        val page0 = PaginatedStandupEntriesData(
            items = listOf(standup(10, testDateStr)),
            meta = PaginationMetaData(page = 0, size = 10, totalElements = 2, totalPages = 2)
        )
        val page1 = PaginatedStandupEntriesData(
            items = listOf(standup(11, testDateStr)),
            meta = PaginationMetaData(page = 1, size = 10, totalElements = 2, totalPages = 2)
        )
        val missing = PaginatedStandupEntriesData(
            items = emptyList(),
            meta = PaginationMetaData(page = 0, size = 100, totalElements = 0, totalPages = 1)
        )

        whenever(
            getTodayStandupUseCase.invoke(
                eq(1L),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                eq(null)
            )
        ).thenReturn(NetworkResult.Success(page0))
            .thenReturn(NetworkResult.Success(page1))

        whenever(
            getTodayStandupUseCase.invoke(
                eq(1L),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                eq("MISSING")
            )
        ).thenReturn(NetworkResult.Success(missing))

        viewModel = HistoryViewModel(getTodayStandupUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.loadMore()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.submissions.size)
        assertEquals(1, state.currentPage)
        assertFalse(state.canLoadMore)
    }

    @Test
    fun `error on refresh sets error and clears list`() = runTest {
        getTodayStandupUseCase = mock()

        whenever(
            getTodayStandupUseCase.invoke(
                eq(1L),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                eq(null)
            )
        ).thenReturn(NetworkResult.Error(message = "boom"))

        viewModel = HistoryViewModel(getTodayStandupUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("boom", state.error)
        assertTrue(state.submissions.isEmpty())
        assertFalse(state.isLoading)
        assertFalse(state.isLoadingMore)
    }

    @Test
    fun `error on loadMore keeps existing list and sets error`() = runTest {
        getTodayStandupUseCase = mock()
        val page0 = PaginatedStandupEntriesData(
            items = listOf(standup(20, testDateStr)),
            meta = PaginationMetaData(page = 0, size = 10, totalElements = 2, totalPages = 2)
        )
        val missing = PaginatedStandupEntriesData(
            items = emptyList(),
            meta = PaginationMetaData(page = 0, size = 100, totalElements = 0, totalPages = 1)
        )

        whenever(
            getTodayStandupUseCase.invoke(
                eq(1L),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                eq(null)
            )
        ).thenReturn(NetworkResult.Success(page0))
            .thenReturn(NetworkResult.Error(message = "err"))

        whenever(
            getTodayStandupUseCase.invoke(
                eq(1L),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                eq("MISSING")
            )
        ).thenReturn(NetworkResult.Success(missing))

        viewModel = HistoryViewModel(getTodayStandupUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        val before = viewModel.uiState.value.submissions
        viewModel.loadMore()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(before.size, state.submissions.size)
        assertEquals("err", state.error)
        assertFalse(state.isLoading)
        assertFalse(state.isLoadingMore)
    }

    @Test
    fun `multiple page loads with pagination metadata`() = runTest {
        getTodayStandupUseCase = mock()
        val page0 = PaginatedStandupEntriesData(
            items = listOf(standup(1, testDateStr)),
            meta = PaginationMetaData(page = 0, size = 10, totalElements = 30, totalPages = 3)
        )
        val page1 = PaginatedStandupEntriesData(
            items = listOf(standup(2, testDateStr)),
            meta = PaginationMetaData(page = 1, size = 10, totalElements = 30, totalPages = 3)
        )
        val page2 = PaginatedStandupEntriesData(
            items = listOf(standup(3, testDateStr)),
            meta = PaginationMetaData(page = 2, size = 10, totalElements = 30, totalPages = 3)
        )
        val missing = PaginatedStandupEntriesData(
            items = emptyList(),
            meta = PaginationMetaData(page = 0, size = 100, totalElements = 0, totalPages = 1)
        )

        whenever(
            getTodayStandupUseCase.invoke(
                eq(1L),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                eq(null)
            )
        ).thenReturn(NetworkResult.Success(page0))
            .thenReturn(NetworkResult.Success(page1))
            .thenReturn(NetworkResult.Success(page2))

        whenever(
            getTodayStandupUseCase.invoke(
                eq(1L),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                eq("MISSING")
            )
        ).thenReturn(NetworkResult.Success(missing))

        viewModel = HistoryViewModel(getTodayStandupUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.currentPage)
        assertTrue(viewModel.uiState.value.canLoadMore)

        viewModel.loadMore()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.currentPage)
        assertTrue(viewModel.uiState.value.canLoadMore)

        viewModel.loadMore()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.currentPage)
        assertFalse(viewModel.uiState.value.canLoadMore)
    }

    @Test
    fun `missing data with multiple team members`() = runTest {
        getTodayStandupUseCase = mock()
        val submissions = PaginatedStandupEntriesData(
            items = listOf(standup(1, testDateStr)),
            meta = PaginationMetaData(page = 0, size = 10, totalElements = 1, totalPages = 1)
        )
        val missingMembers = PaginatedStandupEntriesData(
            items = listOf(
                standup(2, testDateStr).copy(
                    teamMember = TeamMember(
                        2,
                        "Alice",
                        "alice@example.com"
                    )
                ),
                standup(3, testDateStr).copy(teamMember = TeamMember(3, "Bob", "bob@example.com")),
                standup(4, testDateStr).copy(
                    teamMember = TeamMember(
                        4,
                        "Charlie",
                        "charlie@example.com"
                    )
                )
            ),
            meta = PaginationMetaData(page = 0, size = 100, totalElements = 3, totalPages = 1)
        )

        whenever(
            getTodayStandupUseCase.invoke(
                eq(1L),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                eq(null)
            )
        ).thenReturn(NetworkResult.Success(submissions))

        whenever(
            getTodayStandupUseCase.invoke(
                eq(1L),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                eq("MISSING")
            )
        ).thenReturn(NetworkResult.Success(missingMembers))

        viewModel = HistoryViewModel(getTodayStandupUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.submissions.size)
        assertEquals(3, state.missingNames.size)
        assertTrue(state.missingNames.contains("Alice"))
        assertTrue(state.missingNames.contains("Bob"))
        assertTrue(state.missingNames.contains("Charlie"))
    }

    @Test
    fun `state reset on date change clears error`() = runTest {
        getTodayStandupUseCase = mock()
        val errorData = PaginatedStandupEntriesData(
            items = emptyList(),
            meta = PaginationMetaData(page = 0, size = 10, totalElements = 0, totalPages = 1)
        )
        val successData = PaginatedStandupEntriesData(
            items = listOf(standup(1, testDateStr)),
            meta = PaginationMetaData(page = 0, size = 10, totalElements = 1, totalPages = 1)
        )
        val missing = PaginatedStandupEntriesData(
            items = emptyList(),
            meta = PaginationMetaData(page = 0, size = 100, totalElements = 0, totalPages = 1)
        )

        whenever(
            getTodayStandupUseCase.invoke(
                eq(1L),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                eq(null)
            )
        ).thenReturn(NetworkResult.Error("first error"))
            .thenReturn(NetworkResult.Success(successData))

        whenever(
            getTodayStandupUseCase.invoke(
                eq(1L),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                eq("MISSING")
            )
        ).thenReturn(NetworkResult.Success(missing))

        viewModel = HistoryViewModel(getTodayStandupUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        val errorState = viewModel.uiState.value
        assertEquals("first error", errorState.error)

        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -5) }
        viewModel.onPickDate(cal.time)
        testDispatcher.scheduler.advanceUntilIdle()

        val successState = viewModel.uiState.value
        assertNull(successState.error)
        assertEquals(1, successState.submissions.size)
    }

    @Test
    fun `canLoadMore is false when on last page`() = runTest {
        getTodayStandupUseCase = mock()
        val lastPage = PaginatedStandupEntriesData(
            items = listOf(standup(1, testDateStr)),
            meta = PaginationMetaData(page = 2, size = 10, totalElements = 30, totalPages = 3)
        )
        val missing = PaginatedStandupEntriesData(
            items = emptyList(),
            meta = PaginationMetaData(page = 0, size = 100, totalElements = 0, totalPages = 1)
        )

        whenever(
            getTodayStandupUseCase.invoke(
                eq(1L),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                eq(null)
            )
        ).thenReturn(NetworkResult.Success(lastPage))

        whenever(
            getTodayStandupUseCase.invoke(
                eq(1L),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                eq("MISSING")
            )
        ).thenReturn(NetworkResult.Success(missing))

        viewModel = HistoryViewModel(getTodayStandupUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.currentPage)
        assertFalse(state.canLoadMore)
    }

    @Test
    fun `error on missing API does not affect submissions`() = runTest {
        getTodayStandupUseCase = mock()
        val submissions = PaginatedStandupEntriesData(
            items = listOf(standup(1, testDateStr), standup(2, testDateStr)),
            meta = PaginationMetaData(page = 0, size = 10, totalElements = 2, totalPages = 1)
        )

        whenever(
            getTodayStandupUseCase.invoke(
                eq(1L),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                eq(null)
            )
        ).thenReturn(NetworkResult.Success(submissions))

        whenever(
            getTodayStandupUseCase.invoke(
                eq(1L),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                eq("MISSING")
            )
        ).thenReturn(NetworkResult.Error("Missing API failed"))

        viewModel = HistoryViewModel(getTodayStandupUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.submissions.size)
        assertTrue(state.missingNames.isEmpty())
        assertNull(state.error)
    }

    @Test
    fun `isLoading flag is false after data loads`() = runTest {
        getTodayStandupUseCase = mock()
        val data = PaginatedStandupEntriesData(
            items = listOf(standup(1, testDateStr)),
            meta = PaginationMetaData(page = 0, size = 10, totalElements = 1, totalPages = 1)
        )
        val missing = PaginatedStandupEntriesData(
            items = emptyList(),
            meta = PaginationMetaData(page = 0, size = 100, totalElements = 0, totalPages = 1)
        )

        whenever(
            getTodayStandupUseCase.invoke(
                eq(1L),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                eq(null)
            )
        ).thenReturn(NetworkResult.Success(data))

        whenever(
            getTodayStandupUseCase.invoke(
                eq(1L),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                eq("MISSING")
            )
        ).thenReturn(NetworkResult.Success(missing))

        viewModel = HistoryViewModel(getTodayStandupUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isLoadingMore)
    }

    @Test
    fun `isLoadingMore flag is false after loadMore completes`() = runTest {
        getTodayStandupUseCase = mock()
        val page0 = PaginatedStandupEntriesData(
            items = listOf(standup(1, testDateStr)),
            meta = PaginationMetaData(page = 0, size = 10, totalElements = 20, totalPages = 2)
        )
        val page1 = PaginatedStandupEntriesData(
            items = listOf(standup(2, testDateStr)),
            meta = PaginationMetaData(page = 1, size = 10, totalElements = 20, totalPages = 2)
        )
        val missing = PaginatedStandupEntriesData(
            items = emptyList(),
            meta = PaginationMetaData(page = 0, size = 100, totalElements = 0, totalPages = 1)
        )

        whenever(
            getTodayStandupUseCase.invoke(
                eq(1L),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                eq(null)
            )
        ).thenReturn(NetworkResult.Success(page0))
            .thenReturn(NetworkResult.Success(page1))

        whenever(
            getTodayStandupUseCase.invoke(
                eq(1L),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                eq("MISSING")
            )
        ).thenReturn(NetworkResult.Success(missing))

        viewModel = HistoryViewModel(getTodayStandupUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.loadMore()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoadingMore)
    }

    @Test
    fun `totalItems and totalPages from metadata are stored correctly`() = runTest {
        getTodayStandupUseCase = mock()
        val data = PaginatedStandupEntriesData(
            items = listOf(standup(1, testDateStr)),
            meta = PaginationMetaData(page = 0, size = 10, totalElements = 47, totalPages = 5)
        )
        val missing = PaginatedStandupEntriesData(
            items = emptyList(),
            meta = PaginationMetaData(page = 0, size = 100, totalElements = 0, totalPages = 1)
        )

        whenever(
            getTodayStandupUseCase.invoke(
                eq(1L),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                eq(null)
            )
        ).thenReturn(NetworkResult.Success(data))

        whenever(
            getTodayStandupUseCase.invoke(
                eq(1L),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                eq("MISSING")
            )
        ).thenReturn(NetworkResult.Success(missing))

        viewModel = HistoryViewModel(getTodayStandupUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(47, state.totalItems)
        assertEquals(5, state.totalPages)
    }

    @Test
    fun `empty submissions with missing members shows correct state`() = runTest {
        getTodayStandupUseCase = mock()
        val emptySubmissions = PaginatedStandupEntriesData(
            items = emptyList(),
            meta = PaginationMetaData(page = 0, size = 10, totalElements = 0, totalPages = 1)
        )
        val missingMembers = PaginatedStandupEntriesData(
            items = listOf(
                standup(1, testDateStr).copy(
                    teamMember = TeamMember(
                        1,
                        "Alice",
                        "alice@example.com"
                    )
                ),
                standup(2, testDateStr).copy(teamMember = TeamMember(2, "Bob", "bob@example.com"))
            ),
            meta = PaginationMetaData(page = 0, size = 100, totalElements = 2, totalPages = 1)
        )

        whenever(
            getTodayStandupUseCase.invoke(
                eq(1L),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                eq(null)
            )
        ).thenReturn(NetworkResult.Success(emptySubmissions))

        whenever(
            getTodayStandupUseCase.invoke(
                eq(1L),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                eq("MISSING")
            )
        ).thenReturn(NetworkResult.Success(missingMembers))

        viewModel = HistoryViewModel(getTodayStandupUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.submissions.isEmpty())
        assertEquals(2, state.missingNames.size)
        assertNull(state.error)
    }

    @Test
    fun `loadMore on second page with missing members`() = runTest {
        getTodayStandupUseCase = mock()
        val page0 = PaginatedStandupEntriesData(
            items = listOf(standup(1, testDateStr)),
            meta = PaginationMetaData(page = 0, size = 10, totalElements = 20, totalPages = 2)
        )
        val page1 = PaginatedStandupEntriesData(
            items = listOf(standup(2, testDateStr)),
            meta = PaginationMetaData(page = 1, size = 10, totalElements = 20, totalPages = 2)
        )
        val missing = PaginatedStandupEntriesData(
            items = listOf(standup(3, testDateStr).copy(teamMember = TeamMember(3, "Charlie", "charlie@example.com"))),
            meta = PaginationMetaData(page = 0, size = 100, totalElements = 1, totalPages = 1)
        )

        whenever(
            getTodayStandupUseCase.invoke(eq(1L), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), eq(null))
        ).thenReturn(NetworkResult.Success(page0)).thenReturn(NetworkResult.Success(page1))

        whenever(
            getTodayStandupUseCase.invoke(eq(1L), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), eq("MISSING"))
        ).thenReturn(NetworkResult.Success(missing))

        viewModel = HistoryViewModel(getTodayStandupUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.loadMore()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.submissions.size)
        assertEquals(1, state.missingNames.size)
        assertEquals("Charlie", state.missingNames.first())
    }

    @Test
    fun `onPickDate with specific date loads correctly`() = runTest {
        getTodayStandupUseCase = mock()
        val targetDate = "2026-01-05"
        val data = PaginatedStandupEntriesData(
            items = listOf(standup(1, targetDate)),
            meta = PaginationMetaData(page = 0, size = 10, totalElements = 1, totalPages = 1)
        )
        val missing = PaginatedStandupEntriesData(
            items = emptyList(),
            meta = PaginationMetaData(page = 0, size = 100, totalElements = 0, totalPages = 1)
        )

        whenever(
            getTodayStandupUseCase.invoke(eq(1L), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), eq(null))
        ).thenReturn(NetworkResult.Success(data))

        whenever(
            getTodayStandupUseCase.invoke(eq(1L), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), eq("MISSING"))
        ).thenReturn(NetworkResult.Success(missing))

        viewModel = HistoryViewModel(getTodayStandupUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        val cal = Calendar.getInstance()
        cal.set(2026, 0, 5) // January 5, 2026
        viewModel.onPickDate(cal.time)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.submissions.size)
        assertTrue(state.submissions.first().standupDate == targetDate)
    }

    @Test
    fun `rapid date changes maintain consistency`() = runTest {
        getTodayStandupUseCase = mock()
        val data1 = PaginatedStandupEntriesData(
            items = listOf(standup(1, "2026-01-10")),
            meta = PaginationMetaData(page = 0, size = 10, totalElements = 1, totalPages = 1)
        )
        val data2 = PaginatedStandupEntriesData(
            items = listOf(standup(2, "2026-01-09")),
            meta = PaginationMetaData(page = 0, size = 10, totalElements = 1, totalPages = 1)
        )
        val missing = PaginatedStandupEntriesData(
            items = emptyList(),
            meta = PaginationMetaData(page = 0, size = 100, totalElements = 0, totalPages = 1)
        )

        whenever(
            getTodayStandupUseCase.invoke(eq(1L), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), eq(null))
        ).thenReturn(NetworkResult.Success(data1)).thenReturn(NetworkResult.Success(data2))

        whenever(
            getTodayStandupUseCase.invoke(eq(1L), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), eq("MISSING"))
        ).thenReturn(NetworkResult.Success(missing))

        viewModel = HistoryViewModel(getTodayStandupUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onPrevDate()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.submissions.size)
        assertEquals("2026-01-09", state.submissions.first().standupDate)
    }

    @Test
    fun `currentPage resets on new date`() = runTest {
        getTodayStandupUseCase = mock()
        val data = PaginatedStandupEntriesData(
            items = listOf(standup(1, testDateStr)),
            meta = PaginationMetaData(page = 0, size = 10, totalElements = 1, totalPages = 1)
        )
        val missing = PaginatedStandupEntriesData(
            items = emptyList(),
            meta = PaginationMetaData(page = 0, size = 100, totalElements = 0, totalPages = 1)
        )

        whenever(
            getTodayStandupUseCase.invoke(eq(1L), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), eq(null))
        ).thenReturn(NetworkResult.Success(data))

        whenever(
            getTodayStandupUseCase.invoke(eq(1L), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), eq("MISSING"))
        ).thenReturn(NetworkResult.Success(missing))

        viewModel = HistoryViewModel(getTodayStandupUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        val initialState = viewModel.uiState.value
        assertEquals(0, initialState.currentPage)
    }

    @Test
    fun `error state persists until next successful load`() = runTest {
        getTodayStandupUseCase = mock()
        val successData = PaginatedStandupEntriesData(
            items = listOf(standup(1, testDateStr)),
            meta = PaginationMetaData(page = 0, size = 10, totalElements = 1, totalPages = 1)
        )
        val missing = PaginatedStandupEntriesData(
            items = emptyList(),
            meta = PaginationMetaData(page = 0, size = 100, totalElements = 0, totalPages = 1)
        )

        whenever(
            getTodayStandupUseCase.invoke(eq(1L), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), eq(null))
        ).thenReturn(NetworkResult.Error("Error 1"))
            .thenReturn(NetworkResult.Success(successData))

        whenever(
            getTodayStandupUseCase.invoke(eq(1L), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), eq("MISSING"))
        ).thenReturn(NetworkResult.Success(missing))

        viewModel = HistoryViewModel(getTodayStandupUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        val errorState = viewModel.uiState.value
        assertEquals("Error 1", errorState.error)

        viewModel.onPrevDate()
        testDispatcher.scheduler.advanceUntilIdle()

        val successState = viewModel.uiState.value
        assertNull(successState.error)
    }

    @Test
    fun `canLoadMore updates correctly with pagination`() = runTest {
        getTodayStandupUseCase = mock()
        val page0 = PaginatedStandupEntriesData(
            items = listOf(standup(1, testDateStr)),
            meta = PaginationMetaData(page = 0, size = 10, totalElements = 20, totalPages = 2)
        )
        val page1 = PaginatedStandupEntriesData(
            items = listOf(standup(2, testDateStr)),
            meta = PaginationMetaData(page = 1, size = 10, totalElements = 20, totalPages = 2)
        )
        val missing = PaginatedStandupEntriesData(
            items = emptyList(),
            meta = PaginationMetaData(page = 0, size = 100, totalElements = 0, totalPages = 1)
        )

        whenever(
            getTodayStandupUseCase.invoke(eq(1L), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), eq(null))
        ).thenReturn(NetworkResult.Success(page0)).thenReturn(NetworkResult.Success(page1))

        whenever(
            getTodayStandupUseCase.invoke(eq(1L), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), eq("MISSING"))
        ).thenReturn(NetworkResult.Success(missing))

        viewModel = HistoryViewModel(getTodayStandupUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.canLoadMore)

        viewModel.loadMore()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.canLoadMore)
    }

    @Test
    fun `submissions and missing both empty state`() = runTest {
        getTodayStandupUseCase = mock()
        val emptyData = PaginatedStandupEntriesData(
            items = emptyList(),
            meta = PaginationMetaData(page = 0, size = 10, totalElements = 0, totalPages = 1)
        )
        val emptyMissing = PaginatedStandupEntriesData(
            items = emptyList(),
            meta = PaginationMetaData(page = 0, size = 100, totalElements = 0, totalPages = 1)
        )

        whenever(
            getTodayStandupUseCase.invoke(eq(1L), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), eq(null))
        ).thenReturn(NetworkResult.Success(emptyData))

        whenever(
            getTodayStandupUseCase.invoke(eq(1L), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), eq("MISSING"))
        ).thenReturn(NetworkResult.Success(emptyMissing))

        viewModel = HistoryViewModel(getTodayStandupUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.submissions.isEmpty())
        assertTrue(state.missingNames.isEmpty())
        assertFalse(state.canLoadMore)
        assertNull(state.error)
    }

    @Test
    fun `large dataset pagination works correctly`() = runTest {
        getTodayStandupUseCase = mock()
        val items = (1..10).map { standup(it.toLong(), testDateStr) }
        val page0 = PaginatedStandupEntriesData(
            items = items,
            meta = PaginationMetaData(page = 0, size = 10, totalElements = 100, totalPages = 10)
        )
        val missing = PaginatedStandupEntriesData(
            items = emptyList(),
            meta = PaginationMetaData(page = 0, size = 100, totalElements = 0, totalPages = 1)
        )

        whenever(
            getTodayStandupUseCase.invoke(eq(1L), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), eq(null))
        ).thenReturn(NetworkResult.Success(page0))

        whenever(
            getTodayStandupUseCase.invoke(eq(1L), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), eq("MISSING"))
        ).thenReturn(NetworkResult.Success(missing))

        viewModel = HistoryViewModel(getTodayStandupUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(10, state.submissions.size)
        assertTrue(state.canLoadMore)
        assertEquals(0, state.currentPage)
    }
}
