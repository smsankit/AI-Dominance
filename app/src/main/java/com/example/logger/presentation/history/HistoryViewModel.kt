package com.example.logger.presentation.history

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.logger.core.network.NetworkResult
import com.example.logger.core.util.DateFormatter
import com.example.logger.domain.model.StandupEntryData
import com.example.logger.domain.usecase.GetTodayStandupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

data class HistoryUiState(
    val selectedDate: Date = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_MONTH, -1)
    }.time, // Start from yesterday
    val submissions: List<StandupEntryData> = emptyList(),
    val missingNames: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 0,
    val pageSize: Int = 10,
    val totalItems: Int = 0,
    val totalPages: Int = 0,
    val canLoadMore: Boolean = false
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getTodayStandup: GetTodayStandupUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState

    // Helper to get yesterday's date for comparison
    private fun getYesterday(): Date = Calendar.getInstance().apply {
        timeZone = DateFormatter.istTimeZone
        add(Calendar.DAY_OF_MONTH, -1)
    }.time

    init { refresh() }

    fun onPrevDate() {
        val cal = Calendar.getInstance().apply {
            timeZone = DateFormatter.istTimeZone
            time = _uiState.value.selectedDate
        }
        cal.add(Calendar.DAY_OF_MONTH, -1)
        _uiState.value = _uiState.value.copy(selectedDate = cal.time, currentPage = 0)
        refresh(resetList = true)
    }

    fun onNextDate() {
        val cal = Calendar.getInstance().apply {
            timeZone = DateFormatter.istTimeZone
            time = _uiState.value.selectedDate
        }
        cal.add(Calendar.DAY_OF_MONTH, 1)
        val next = cal.time
        val yesterday = getYesterday()
        val dateKeyFmt = DateFormatter.getApiDateFormat()
        // Only allow navigation up to yesterday (not today)
        if (dateKeyFmt.format(next) <= dateKeyFmt.format(yesterday)) {
            _uiState.value = _uiState.value.copy(selectedDate = next, currentPage = 0)
            refresh(resetList = true)
        }
    }

    fun onPickDate(date: Date) {
        _uiState.value = _uiState.value.copy(selectedDate = date, currentPage = 0)
        refresh(resetList = true)
    }

    fun loadMore() {
        val currentState = _uiState.value
        if (currentState.canLoadMore && !currentState.isLoadingMore) {
            _uiState.value = currentState.copy(currentPage = currentState.currentPage + 1)
            refresh(resetList = false)
        }
    }

    private fun refresh(resetList: Boolean = true) {
        viewModelScope.launch {
            if (resetList) {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    error = null,
                    submissions = emptyList(),
                    missingNames = emptyList()
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoadingMore = true, error = null)
            }

            val currentState = _uiState.value
            val dateStr = DateFormatter.getApiDateFormat().format(currentState.selectedDate)

            // Call submissions API (sequential, not async)
            Log.d("HistoryViewModel", "Calling Submissions API for date: $dateStr")
            val result = getTodayStandup(
                teamId = 1,
                page = currentState.currentPage,
                size = currentState.pageSize,
                standupDate = dateStr,
                status = null
            )

            // Call missing API (sequential, not async) only on first page load
            val missingResult = if (resetList) {
                Log.d("HistoryViewModel", "Calling MISSING API for date: $dateStr")
                getTodayStandup(
                    teamId = 1,
                    page = 0,
                    size = 100,
                    standupDate = dateStr,
                    status = "MISSING"
                )
            } else {
                null
            }


            Log.d("HistoryViewModel", "===== API RESULTS =====")
            Log.d("HistoryViewModel", "Submissions API result: ${result is NetworkResult.Success}")
            Log.d("HistoryViewModel", "Missing API result: ${missingResult is NetworkResult.Success}")
            if (missingResult is NetworkResult.Error) {
                Log.d("HistoryViewModel", "Missing API ERROR - Message: ${missingResult.message}, Code: ${missingResult.code}")
            }
            if (missingResult is NetworkResult.Success) {
                Log.d("HistoryViewModel", "Missing API SUCCESS - Items: ${missingResult.data.items.size}")
            }
            Log.d("HistoryViewModel", "========================")

            when (result) {
                is NetworkResult.Success -> {
                    val data = result.data
                    // Submissions list for display
                    val updatedSubmissions = if (resetList) {
                        data.items
                    } else {
                        currentState.submissions + data.items
                    }

                    // Extract missing member names from MISSING API
                    val missingMemberNames = if (resetList && missingResult is NetworkResult.Success) {
                        missingResult.data.items.map { it.teamMember.name }
                    } else if (resetList) {
                        emptyList()
                    } else {
                        currentState.missingNames
                    }

                    Log.d("HistoryViewModel", "Setting state - submissions: ${updatedSubmissions.size}, missingNames: ${missingMemberNames.size} = $missingMemberNames")
                    _uiState.value = _uiState.value.copy(
                        submissions = updatedSubmissions,
                        missingNames = missingMemberNames,
                        isLoading = false,
                        isLoadingMore = false,
                        error = null,
                        currentPage = data.meta.page,
                        totalItems = data.meta.totalElements,
                        totalPages = data.meta.totalPages,
                        canLoadMore = data.meta.page < data.meta.totalPages - 1
                    )
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        submissions = if (resetList) emptyList() else currentState.submissions,
                        missingNames = if (resetList) emptyList() else currentState.missingNames,
                        isLoading = false,
                        isLoadingMore = false,
                        error = result.message ?: "An error occurred"
                    )
                }
            }
        }
    }
}
