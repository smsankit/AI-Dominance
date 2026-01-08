package com.example.logger.presentation.export

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.example.logger.core.network.NetworkResult
import com.example.logger.core.util.DateFormatter
import com.example.logger.domain.model.PaginatedStandupEntriesData
import com.example.logger.domain.model.PaginationMetaData
import com.example.logger.domain.model.StandupEntryData
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
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalCoroutinesApi::class)
class ExportViewModelTest {
    private lateinit var getTodayStandupUseCase: GetTodayStandupUseCase
    private lateinit var viewModel: ExportViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val testDate = "2026-01-08"
    private val testStandupEntry = StandupEntryData(
        id = 1,
        standupDate = testDate,
        yesterdayWork = "Did X",
        todayPlan = "Will do Y",
        blockers = "None",
        teamMemberId = 2,
        teamId = 1,
        createdAt = null,
        updatedAt = null
    )
    private val testMeta = PaginationMetaData(
        page = 0, totalPages = 1, totalElements = 1,
        size = 1
    )
    private val testPaginatedData =
        PaginatedStandupEntriesData(items = listOf(testStandupEntry), meta = testMeta)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getTodayStandupUseCase = mock()
        // Use nullable matchers for nullable params (page, size, teamMemberId, standupDate)
        runBlocking {
            whenever(
                getTodayStandupUseCase.invoke(
                    eq(1L), anyOrNull<Int>(), anyOrNull<Int>(), anyOrNull<Long>(), anyOrNull<String>()
                )
            ).thenReturn(NetworkResult.Success(testPaginatedData))
        }
        viewModel = ExportViewModel(getTodayStandupUseCase)
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads today's standups`() {
        val state = viewModel.uiState.value
        assertEquals(DateFormatter.getCurrentDateString(), state.selectedDate)
        assertEquals(1, state.standupEntries.size)
        assertEquals("Did X", state.standupEntries.first().yesterdayWork)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `onDateChange resets and loads new standups`() {
        runBlocking {
            whenever(
                getTodayStandupUseCase.invoke(
                    eq(1L), anyOrNull<Int>(), anyOrNull<Int>(), anyOrNull<Long>(), anyOrNull<String>()
                )
            ).thenReturn(NetworkResult.Success(testPaginatedData))
        }
        viewModel.onDateChange(testDate)
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals(testDate, state.selectedDate)
        assertEquals(1, state.standupEntries.size)
    }

    @Test
    fun `loadMore appends standups when canLoadMore is true`() {
        val meta = PaginationMetaData(page = 0, totalPages = 2, totalElements = 2, size = 2)
        val paginatedData = PaginatedStandupEntriesData(items = listOf(testStandupEntry), meta = meta)
        runBlocking {
            whenever(
                getTodayStandupUseCase.invoke(
                    eq(1L), anyOrNull<Int>(), anyOrNull<Int>(), anyOrNull<Long>(), anyOrNull<String>()
                )
            ).thenReturn(NetworkResult.Success(paginatedData))
        }
        viewModel.onDateChange(testDate)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.loadMore()
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value
        assertTrue(state.standupEntries.isNotEmpty())
        assertTrue(state.canLoadMore)
    }

    @Test
    fun `error state is set when use case returns error`() {
        runBlocking {
            whenever(
                getTodayStandupUseCase.invoke(
                    eq(1L), anyOrNull<Int>(), anyOrNull<Int>(), anyOrNull<Long>(), anyOrNull<String>()
                )
            ).thenReturn(NetworkResult.Error(message = "Network error"))
        }
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
        val standups = emptyList<ExportStandupUiModel>()
        val result = viewModel.exportMarkdownToUri(context, uri, testDate, standups)
        assertFalse(result)
    }
}
