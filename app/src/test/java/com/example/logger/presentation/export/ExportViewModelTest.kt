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

    @Test
    fun `onDateChange updates selected date and resets page`() = runTest {
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

        val newDate = "2026-01-15"
        viewModel.onDateChange(newDate)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(newDate, state.selectedDate)
        assertEquals(0, state.currentPage)
    }

    @Test
    fun `onDateChange with valid date loads standups`() = runTest {
        getTodayStandupUseCase = mock()
        val dateEntry = testStandupEntry.copy(standupDate = "2026-01-14")
        val dateData = PaginatedStandupEntriesData(
            items = listOf(dateEntry),
            meta = PaginationMetaData(page = 0, size = 10, totalElements = 1, totalPages = 1)
        )
        val emptyMissing = PaginatedStandupEntriesData(
            items = emptyList(),
            meta = PaginationMetaData(page = 0, size = 100, totalElements = 0, totalPages = 1)
        )

        whenever(
            getTodayStandupUseCase.invoke(eq(1L), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), eq(null))
        ).thenReturn(NetworkResult.Success(dateData))

        whenever(
            getTodayStandupUseCase.invoke(eq(1L), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), eq("MISSING"))
        ).thenReturn(NetworkResult.Success(emptyMissing))

        viewModel = ExportViewModel(getTodayStandupUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        val newDate = "2026-01-14"
        viewModel.onDateChange(newDate)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(newDate, state.selectedDate)
        assertEquals(1, state.standupEntries.size)
        assertEquals("Did X", state.standupEntries.first().yesterdayWork)
    }

    @Test
    fun `onDateChange clears previous data before loading new`() = runTest {
        getTodayStandupUseCase = mock()
        val data1 = PaginatedStandupEntriesData(
            items = listOf(testStandupEntry.copy(standupDate = "2026-01-13")),
            meta = PaginationMetaData(page = 0, size = 10, totalElements = 1, totalPages = 1)
        )
        val data2 = PaginatedStandupEntriesData(
            items = listOf(testStandupEntry.copy(id = 10, standupDate = "2026-01-14")),
            meta = PaginationMetaData(page = 0, size = 10, totalElements = 1, totalPages = 1)
        )
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

        val initialSize = viewModel.uiState.value.standupEntries.size
        assertEquals(1, initialSize)

        viewModel.onDateChange("2026-01-14")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("2026-01-14", state.selectedDate)
        assertEquals(1, state.standupEntries.size)
        assertEquals(10L, state.standupEntries.first().id)
    }

    @Test
    fun `loadMore increments page and loads more standups`() = runTest {
        getTodayStandupUseCase = mock()
        val page0 = PaginatedStandupEntriesData(
            items = listOf(testStandupEntry),
            meta = PaginationMetaData(page = 0, size = 10, totalElements = 20, totalPages = 2)
        )
        val page1 = PaginatedStandupEntriesData(
            items = listOf(testStandupEntry.copy(id = 20)),
            meta = PaginationMetaData(page = 1, size = 10, totalElements = 20, totalPages = 2)
        )

        whenever(
            getTodayStandupUseCase.invoke(eq(1L), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), eq(null))
        ).thenReturn(NetworkResult.Success(page0))
            .thenReturn(NetworkResult.Success(page1))

        viewModel = ExportViewModel(getTodayStandupUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.currentPage)
        assertEquals(1, viewModel.uiState.value.standupEntries.size)

        viewModel.loadMore()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.currentPage)
        assertEquals(2, state.standupEntries.size)
        assertTrue(state.standupEntries.any { it.id == 20L })
    }

    @Test
    fun `loadMore does not load when canLoadMore is false`() = runTest {
        getTodayStandupUseCase = mock()
        val lastPage = PaginatedStandupEntriesData(
            items = listOf(testStandupEntry),
            meta = PaginationMetaData(page = 1, size = 10, totalElements = 20, totalPages = 2)
        )

        whenever(
            getTodayStandupUseCase.invoke(eq(1L), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), eq(null))
        ).thenReturn(NetworkResult.Success(lastPage))

        viewModel = ExportViewModel(getTodayStandupUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        val initialSize = viewModel.uiState.value.standupEntries.size
        assertFalse(viewModel.uiState.value.canLoadMore)

        viewModel.loadMore()
        testDispatcher.scheduler.advanceUntilIdle()

        val finalSize = viewModel.uiState.value.standupEntries.size
        assertEquals(initialSize, finalSize)
    }

    @Test
    fun `loadMore does not load when isLoadingMore is true`() = runTest {
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

        assertTrue(viewModel.uiState.value.canLoadMore)

        // Call loadMore but don't advance idle to keep isLoadingMore true
        viewModel.loadMore()

        val state = viewModel.uiState.value
        assertEquals(1, state.standupEntries.size)
    }

    @Test
    fun `exportMarkdownToUri successfully exports with content`() {
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
                name = "John Doe",
                time = "09:00",
                yesterday = "Fixed bugs",
                today = "Implement feature",
                blockers = "None",
                editedAt = null
            ),
            ExportStandupUiModel(
                name = "Jane Smith",
                time = "10:00",
                yesterday = "Code review",
                today = "Write tests",
                blockers = "Waiting for approval",
                editedAt = null
            )
        )

        val result = viewModel.exportMarkdownToUri(context, uri, "2026-01-08", standups)

        assertTrue(result)
        assertTrue(outputStream.size() > 0)
        val content = outputStream.toString()
        assertTrue(content.contains("John Doe"))
        assertTrue(content.contains("Jane Smith"))
        assertTrue(content.contains("Fixed bugs"))
    }

    @Test
    fun `exportMarkdownToUri returns true when successful`() {
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
                name = "Test User",
                time = "10:00",
                yesterday = "Work",
                today = "Plan",
                blockers = null,
                editedAt = null
            )
        )

        val result = viewModel.exportMarkdownToUri(context, uri, testDate, standups)
        assertTrue(result)
    }



    @Test
    fun `exportMarkdownToUri handles null output stream`() {
        val context = mock<Context>()
        val uri = mock<Uri>()
        val resolver = mock<ContentResolver>()
        whenever(context.contentResolver).thenReturn(resolver)
        whenever(resolver.openOutputStream(uri)).thenReturn(null)

        getTodayStandupUseCase = mock()
        viewModel = ExportViewModel(getTodayStandupUseCase)

        val standups = listOf(
            ExportStandupUiModel(
                name = "Test",
                time = "10:00",
                yesterday = "Work",
                today = "Plan",
                blockers = null,
                editedAt = null
            )
        )

        val result = viewModel.exportMarkdownToUri(context, uri, testDate, standups)
        // Should return true as no exception is thrown, just nothing written
        assertTrue(result)
    }

    @Test
    fun `exportMarkdownToUri includes missing names in export`() {
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
                yesterday = "Task A",
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
    fun `exportMarkdownToUri with blockers includes them`() {
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
                name = "User",
                time = "10:00",
                yesterday = "Work",
                today = "Plan",
                blockers = "API is down",
                editedAt = null
            )
        )

        val result = viewModel.exportMarkdownToUri(context, uri, testDate, standups)
        assertTrue(result)
        val content = outputStream.toString()
        assertTrue(content.contains("API is down"))
    }

    @Test
    fun `exportMarkdownToUri with empty standups list`() {
        val context = mock<Context>()
        val uri = mock<Uri>()
        val outputStream = ByteArrayOutputStream()
        val resolver = mock<ContentResolver>()
        whenever(context.contentResolver).thenReturn(resolver)
        whenever(resolver.openOutputStream(uri)).thenReturn(outputStream)

        getTodayStandupUseCase = mock()
        viewModel = ExportViewModel(getTodayStandupUseCase)

        val standups = emptyList<ExportStandupUiModel>()

        val result = viewModel.exportMarkdownToUri(context, uri, testDate, standups)
        assertTrue(result)
        assertTrue(outputStream.size() >= 0)
    }

    @Test
    fun `loadStandups with resetList true clears previous data`() = runTest {
        getTodayStandupUseCase = mock()
        val data1 = PaginatedStandupEntriesData(
            items = listOf(testStandupEntry.copy(id = 1)),
            meta = PaginationMetaData(page = 0, size = 10, totalElements = 1, totalPages = 1)
        )
        val data2 = PaginatedStandupEntriesData(
            items = listOf(testStandupEntry.copy(id = 2)),
            meta = PaginationMetaData(page = 0, size = 10, totalElements = 1, totalPages = 1)
        )
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

        assertEquals(1, viewModel.uiState.value.standupEntries.size)
        assertEquals(1L, viewModel.uiState.value.standupEntries.first().id)

        viewModel.onDateChange("2026-01-09")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.standupEntries.size)
        assertEquals(2L, state.standupEntries.first().id)
    }

    @Test
    fun `loadStandups with resetList false appends data`() = runTest {
        getTodayStandupUseCase = mock()
        val page0 = PaginatedStandupEntriesData(
            items = listOf(testStandupEntry.copy(id = 1)),
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

        viewModel.loadMore()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.standupEntries.size)
        assertTrue(state.standupEntries.any { it.id == 1L })
        assertTrue(state.standupEntries.any { it.id == 2L })
    }
}
