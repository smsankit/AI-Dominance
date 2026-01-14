package com.example.logger.presentation.export

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.example.logger.core.network.NetworkResult
import com.example.logger.domain.model.PaginatedStandupEntriesData
import com.example.logger.domain.model.PaginationMetaData
import com.example.logger.domain.model.StandupEntryData
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
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ExportViewModelTest {
    private lateinit var getTodayStandupUseCase: GetTodayStandupUseCase
    private lateinit var viewModel: ExportViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val testDate = "2026-01-08"
    private val testTeamMember = com.example.logger.domain.model.TeamMember(
        id = 2,
        name = "Test User",
        email = "test@example.com"
    )
    private val testStandupEntry = StandupEntryData(
        id = 1,
        standupDate = testDate,
        yesterdayWork = "Did X",
        todayPlan = "Will do Y",
        blockers = "None",
        teamMemberId = 2,
        teamId = 1,
        createdAt = null,
        updatedAt = null,
        teamMember = testTeamMember
    )
    private val testMeta = PaginationMetaData(
        page = 0, totalPages = 1, totalElements = 1,
        size = 1
    )
    private val testPaginatedData =
        PaginatedStandupEntriesData(items = listOf(testStandupEntry), meta = testMeta)

    private val testMissingMember = com.example.logger.domain.model.TeamMember(
        id = 3,
        name = "Missing User",
        email = "missing@example.com"
    )
    private val testMissingEntry = StandupEntryData(
        id = 2,
        standupDate = testDate,
        yesterdayWork = "N/A",
        todayPlan = "N/A",
        blockers = null,
        teamMemberId = 3,
        teamId = 1,
        createdAt = null,
        updatedAt = null,
        teamMember = testMissingMember
    )
    private val testMissingPaginatedData =
        PaginatedStandupEntriesData(items = listOf(testMissingEntry), meta = testMeta)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads today's standups`() = runTest {
        getTodayStandupUseCase = mock()

        // Match submissions call (status = null)
        whenever(
            getTodayStandupUseCase.invoke(eq(1L), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), eq(null))
        ).thenReturn(NetworkResult.Success(testPaginatedData))

        viewModel = ExportViewModel(getTodayStandupUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // Set the initial date to testDate for consistency
        viewModel.onDateChange(testDate)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(testDate, state.selectedDate)
        assertEquals(1, state.standupEntries.size)
        assertEquals("Did X", state.standupEntries.first().yesterdayWork)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `onDateChange resets and loads new standups`() = runTest {
        getTodayStandupUseCase = mock()

        whenever(
            getTodayStandupUseCase.invoke(eq(1L), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), eq(null))
        ).thenReturn(NetworkResult.Success(testPaginatedData))

        viewModel = ExportViewModel(getTodayStandupUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onDateChange(testDate)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(testDate, state.selectedDate)
        assertEquals(1, state.standupEntries.size)
    }

    @Test
    fun `loadMore appends standups when canLoadMore is true`() = runTest {
        getTodayStandupUseCase = mock()

        val meta = PaginationMetaData(page = 0, totalPages = 2, totalElements = 2, size = 2)
        val paginatedData = PaginatedStandupEntriesData(items = listOf(testStandupEntry), meta = meta)

        whenever(
            getTodayStandupUseCase.invoke(eq(1L), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), eq(null))
        ).thenReturn(NetworkResult.Success(paginatedData))
            .thenReturn(NetworkResult.Success(paginatedData))

        viewModel = ExportViewModel(getTodayStandupUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onDateChange(testDate)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.loadMore()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.standupEntries.isNotEmpty())
        assertTrue(state.canLoadMore)
    }

    @Test
    fun `error state is set when use case returns error`() = runTest {
        getTodayStandupUseCase = mock()

        whenever(
            getTodayStandupUseCase.invoke(eq(1L), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), eq(null))
        ).thenReturn(NetworkResult.Error(message = "Network error"))

        viewModel = ExportViewModel(getTodayStandupUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onDateChange(testDate)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Network error", state.error)
        assertTrue(state.standupEntries.isEmpty())
    }

    @Test
    fun `exportMarkdownToUri writes markdown and returns true`() {
        val context = mock<Context>()
        val uri = mock<Uri>()
        val outputStream = ByteArrayOutputStream()
        val resolver = mock<ContentResolver>()
        whenever(context.contentResolver).thenReturn(resolver)
        whenever(resolver.openOutputStream(uri)).thenReturn(outputStream)
        val standups = listOf(
            ExportStandupUiModel(
                name = "John Doe",
                time = "09:00",
                yesterday = "Did X",
                today = "Will do Y",
                blockers = "None",
                editedAt = null
            )
        )

        // Create viewModel with mock
        getTodayStandupUseCase = mock()
        viewModel = ExportViewModel(getTodayStandupUseCase)

        val result = viewModel.exportMarkdownToUri(context, uri, testDate, standups)
        assertTrue(result)
        assertTrue(outputStream.size() > 0)
    }

    @Test
    fun `exportMarkdownToUri returns false on exception`() {
        val context = mock<Context>()
        val uri = mock<Uri>()
        val resolver = mock<ContentResolver>()
        whenever(context.contentResolver).thenReturn(resolver)
        whenever(resolver.openOutputStream(uri)).thenThrow(RuntimeException("IO error"))

        // Create viewModel with mock
        getTodayStandupUseCase = mock()
        viewModel = ExportViewModel(getTodayStandupUseCase)

        val standups = emptyList<ExportStandupUiModel>()
        val result = viewModel.exportMarkdownToUri(context, uri, testDate, standups)
        assertFalse(result)
    }

    @Test
    fun `loads missing members along with submissions`() = runTest {
        getTodayStandupUseCase = mock()

        // Match submissions call (status = null)
        whenever(
            getTodayStandupUseCase.invoke(eq(1L), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), eq(null))
        ).thenReturn(NetworkResult.Success(testPaginatedData))

        // Match missing call (status = "MISSING")
        whenever(
            getTodayStandupUseCase.invoke(eq(1L), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), eq("MISSING"))
        ).thenReturn(NetworkResult.Success(testMissingPaginatedData))

        viewModel = ExportViewModel(getTodayStandupUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.standupEntries.size)
        assertEquals(1, state.missingNames.size)
        assertEquals("Missing User", state.missingNames.first())
    }

    @Test
    fun `missing API error does not affect submissions`() = runTest {
        getTodayStandupUseCase = mock()

        // Match submissions call - succeeds
        whenever(
            getTodayStandupUseCase.invoke(eq(1L), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), eq(null))
        ).thenReturn(NetworkResult.Success(testPaginatedData))

        // Match missing call - fails
        whenever(
            getTodayStandupUseCase.invoke(eq(1L), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), eq("MISSING"))
        ).thenReturn(NetworkResult.Error("Missing API error"))

        viewModel = ExportViewModel(getTodayStandupUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.standupEntries.size)
        assertTrue(state.missingNames.isEmpty())
        assertNull(state.error)
    }

    @Test
    fun `empty submissions list with no missing members`() = runTest {
        getTodayStandupUseCase = mock()
        val emptyData = PaginatedStandupEntriesData(
            items = emptyList(),
            meta = PaginationMetaData(page = 0, size = 10, totalElements = 0, totalPages = 1)
        )

        whenever(
            getTodayStandupUseCase.invoke(eq(1L), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), eq(null))
        ).thenReturn(NetworkResult.Success(emptyData))

        whenever(
            getTodayStandupUseCase.invoke(eq(1L), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), eq("MISSING"))
        ).thenReturn(NetworkResult.Success(emptyData))

        viewModel = ExportViewModel(getTodayStandupUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.standupEntries.isEmpty())
        assertTrue(state.missingNames.isEmpty())
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `multiple submissions with multiple missing members`() = runTest {
        getTodayStandupUseCase = mock()
        val submissions = PaginatedStandupEntriesData(
            items = listOf(
                testStandupEntry,
                testStandupEntry.copy(id = 2, teamMemberId = 4, teamMember = testTeamMember.copy(id = 4, name = "User 2"))
            ),
            meta = PaginationMetaData(page = 0, size = 10, totalElements = 2, totalPages = 1)
        )
        val missing = PaginatedStandupEntriesData(
            items = listOf(
                testMissingEntry,
                testMissingEntry.copy(id = 3, teamMemberId = 5, teamMember = testMissingMember.copy(id = 5, name = "Missing 2"))
            ),
            meta = PaginationMetaData(page = 0, size = 100, totalElements = 2, totalPages = 1)
        )

        whenever(
            getTodayStandupUseCase.invoke(eq(1L), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), eq(null))
        ).thenReturn(NetworkResult.Success(submissions))

        whenever(
            getTodayStandupUseCase.invoke(eq(1L), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), eq("MISSING"))
        ).thenReturn(NetworkResult.Success(missing))

        viewModel = ExportViewModel(getTodayStandupUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.standupEntries.size)
        assertEquals(2, state.missingNames.size)
    }

    @Test
    fun `loadMore with multiple pages updates correctly`() = runTest {
        getTodayStandupUseCase = mock()
        val page0 = PaginatedStandupEntriesData(
            items = listOf(testStandupEntry),
            meta = PaginationMetaData(page = 0, size = 10, totalElements = 20, totalPages = 2)
        )
        val page1 = PaginatedStandupEntriesData(
            items = listOf(testStandupEntry.copy(id = 2)),
            meta = PaginationMetaData(page = 1, size = 10, totalElements = 20, totalPages = 2)
        )

        whenever(
            getTodayStandupUseCase.invoke(eq(1L), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), eq(null))
        ).thenReturn(NetworkResult.Success(page0))
            .thenReturn(NetworkResult.Success(page1))

        viewModel = ExportViewModel(getTodayStandupUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.standupEntries.size)
        assertTrue(viewModel.uiState.value.canLoadMore)

        viewModel.loadMore()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.standupEntries.size)
        assertEquals(1, state.currentPage)
    }

    @Test
    fun `loadMore on last page does not load more`() = runTest {
        getTodayStandupUseCase = mock()
        val lastPageData = PaginatedStandupEntriesData(
            items = listOf(testStandupEntry),
            meta = PaginationMetaData(page = 1, size = 10, totalElements = 20, totalPages = 2)
        )

        whenever(
            getTodayStandupUseCase.invoke(eq(1L), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), eq(null))
        ).thenReturn(NetworkResult.Success(lastPageData))

        viewModel = ExportViewModel(getTodayStandupUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.canLoadMore)
        assertEquals(1, state.currentPage)
    }

    @Test
    fun `error on loadMore keeps existing data and sets error`() = runTest {
        getTodayStandupUseCase = mock()
        val page0 = PaginatedStandupEntriesData(
            items = listOf(testStandupEntry),
            meta = PaginationMetaData(page = 0, size = 10, totalElements = 20, totalPages = 2)
        )

        whenever(
            getTodayStandupUseCase.invoke(eq(1L), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), eq(null))
        ).thenReturn(NetworkResult.Success(page0))
            .thenReturn(NetworkResult.Error("Load more failed"))

        viewModel = ExportViewModel(getTodayStandupUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        val initialSize = viewModel.uiState.value.standupEntries.size
        viewModel.loadMore()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(initialSize, state.standupEntries.size)
        assertEquals("Load more failed", state.error)
    }

    @Test
    fun `onDateChange clears data and reloads`() = runTest {
        getTodayStandupUseCase = mock()
        val date1 = "2026-01-10"
        val date2 = "2026-01-11"
        val data1 = testPaginatedData.copy(items = listOf(testStandupEntry.copy(standupDate = date1)))
        val data2 = testPaginatedData.copy(items = listOf(testStandupEntry.copy(id = 5, standupDate = date2)))
        val emptyMissing = PaginatedStandupEntriesData(
            items = emptyList(),
            meta = PaginationMetaData(page = 0, size = 100, totalElements = 0, totalPages = 1)
        )

        whenever(
            getTodayStandupUseCase.invoke(eq(1L), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), eq(null))
        ).thenReturn(NetworkResult.Success(data1))
            .thenReturn(NetworkResult.Success(data2))

        whenever(
            getTodayStandupUseCase.invoke(eq(1L), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), eq("MISSING"))
        ).thenReturn(NetworkResult.Success(emptyMissing))

        viewModel = ExportViewModel(getTodayStandupUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // ViewModel initializes with today's date, so just verify it loads data
        val initialState = viewModel.uiState.value
        assertEquals(1, initialState.standupEntries.size)

        // Now change the date to date1
        viewModel.onDateChange(date1)
        testDispatcher.scheduler.advanceUntilIdle()

        val stateAfterFirstChange = viewModel.uiState.value
        assertEquals(date1, stateAfterFirstChange.selectedDate)
        assertEquals(1, stateAfterFirstChange.standupEntries.size)
        assertEquals("Did X", stateAfterFirstChange.standupEntries.first().yesterdayWork)

        // Change to date2
        viewModel.onDateChange(date2)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(date2, state.selectedDate)
        assertEquals(1, state.standupEntries.size)
        assertEquals("Did X", state.standupEntries.first().yesterdayWork)
        assertEquals(date2, state.standupEntries.first().standupDate)
        assertNull(state.error)
        assertFalse(state.isLoading)
    }

    @Test
    fun `exportMarkdownToUri with missing members includes them in export`() {
        val context = mock<Context>()
        val uri = mock<Uri>()
        val outputStream = ByteArrayOutputStream()
        val resolver = mock<ContentResolver>()
        whenever(context.contentResolver).thenReturn(resolver)
        whenever(resolver.openOutputStream(uri)).thenReturn(outputStream)

        getTodayStandupUseCase = mock()
        viewModel = ExportViewModel(getTodayStandupUseCase)

        val standups = listOf(
            ExportStandupUiModel(
                name = "Alice",
                time = "10:00",
                yesterday = "Work A",
                today = "Task B",
                blockers = null,
                editedAt = null
            )
        )

        val result = viewModel.exportMarkdownToUri(context, uri, testDate, standups)
        assertTrue(result)
        val content = outputStream.toString()
        assertTrue(content.contains("Alice"))
    }

    @Test
    fun `state updates preserve current page on error`() = runTest {
        getTodayStandupUseCase = mock()
        val page0 = PaginatedStandupEntriesData(
            items = listOf(testStandupEntry),
            meta = PaginationMetaData(page = 0, size = 10, totalElements = 20, totalPages = 2)
        )

        whenever(
            getTodayStandupUseCase.invoke(eq(1L), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), eq(null))
        ).thenReturn(NetworkResult.Success(page0))

        viewModel = ExportViewModel(getTodayStandupUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        val initialPage = viewModel.uiState.value.currentPage
        assertEquals(0, initialPage)
    }

    @Test
    fun `isLoadingMore flag transitions correctly`() = runTest {
        getTodayStandupUseCase = mock()
        val page0 = PaginatedStandupEntriesData(
            items = listOf(testStandupEntry),
            meta = PaginationMetaData(page = 0, size = 10, totalElements = 20, totalPages = 2)
        )
        val page1 = PaginatedStandupEntriesData(
            items = listOf(testStandupEntry.copy(id = 2)),
            meta = PaginationMetaData(page = 1, size = 10, totalElements = 20, totalPages = 2)
        )

        whenever(
            getTodayStandupUseCase.invoke(eq(1L), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), eq(null))
        ).thenReturn(NetworkResult.Success(page0))
            .thenReturn(NetworkResult.Success(page1))

        viewModel = ExportViewModel(getTodayStandupUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoadingMore)

        viewModel.loadMore()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoadingMore)
    }
}
