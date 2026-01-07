package com.example.logger.presentation.export

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.logger.core.network.NetworkResult
import com.example.logger.domain.model.StandupEntryData
import com.example.logger.domain.usecase.GetTodayStandupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

data class ExportUiState(
    val selectedDate: String = "",
    val standupEntries: List<StandupEntryData> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 0,
    val pageSize: Int = 100,
    val totalEntries: Int = 0,
    val totalPages: Int = 0,
    val canLoadMore: Boolean = false
)

@HiltViewModel
class ExportViewModel @Inject constructor(
    private val getTodayStandup: GetTodayStandupUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(ExportUiState())
    val uiState: StateFlow<ExportUiState> = _uiState

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    init {
        // Initialize with today's date
        val today = dateFormatter.format(Calendar.getInstance().time)
        _uiState.value = _uiState.value.copy(selectedDate = today)
        loadStandups(today)
    }

    fun onDateChange(date: String) {
        _uiState.value = _uiState.value.copy(selectedDate = date, currentPage = 0)
        loadStandups(date, resetList = true)
    }

    fun loadMore() {
        val currentState = _uiState.value
        if (currentState.canLoadMore && !currentState.isLoadingMore) {
            _uiState.value = currentState.copy(currentPage = currentState.currentPage + 1)
            loadStandups(currentState.selectedDate, resetList = false)
        }
    }

    private fun loadStandups(date: String, resetList: Boolean = true) {
        viewModelScope.launch {
            if (resetList) {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            } else {
                _uiState.value = _uiState.value.copy(isLoadingMore = true, error = null)
            }

            val currentState = _uiState.value
            val result = getTodayStandup(
                teamId = 1, // Using hardcoded teamId for now
                page = currentState.currentPage,
                size = currentState.pageSize,
                standupDate = date
            )

            when (result) {
                is NetworkResult.Success -> {
                    val data = result.data
                    val updatedEntries = if (resetList) {
                        data.items
                    } else {
                        currentState.standupEntries + data.items
                    }

                    _uiState.value = _uiState.value.copy(
                        standupEntries = updatedEntries,
                        isLoading = false,
                        isLoadingMore = false,
                        error = null,
                        currentPage = data.meta.page,
                        totalEntries = data.meta.totalElements,
                        totalPages = data.meta.totalPages,
                        canLoadMore = data.meta.page < data.meta.totalPages - 1
                    )
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        standupEntries = if (resetList) emptyList() else currentState.standupEntries,
                        isLoading = false,
                        isLoadingMore = false,
                        error = result.message ?: "An error occurred"
                    )
                }
            }
        }
    }

    fun exportMarkdownFile(context: Context, date: String, standups: List<ExportStandupUiModel>): Boolean {
        val markdown = standupsToMarkdown(date, standups)
        val fileName = "standup-$date.md"
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, fileName)
        return try {
            FileOutputStream(file).use { it.write(markdown.toByteArray()) }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun exportMarkdownToUri(context: Context, uri: Uri, date: String, standups: List<ExportStandupUiModel>): Boolean {
        return try {
            val markdown = standupsToMarkdown(date, standups)
            context.contentResolver.openOutputStream(uri)?.use { it.write(markdown.toByteArray()) }
            true
        } catch (e: Exception) {
            false
        }
    }
}
