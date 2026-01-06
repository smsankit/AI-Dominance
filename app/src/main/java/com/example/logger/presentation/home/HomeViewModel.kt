package com.example.logger.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.logger.core.network.NetworkResult
import com.example.logger.domain.usecase.GetTeamMembersUseCase
import com.example.logger.domain.usecase.GetTodayStandupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTodayStandup: GetTodayStandupUseCase,
    private val getTeamMembers: GetTeamMembersUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    init {
        load()
        fetchTeamMembers()
    }

    private fun fetchTeamMembers() {
        viewModelScope.launch {
            getTeamMembers(1, 1, 20).collect { /* No-op or update state if needed */ }
        }
    }

    fun load(resetList: Boolean = true) {
        viewModelScope.launch {
            if (resetList) {
                _uiState.update { it.copy(isLoading = true, error = null, currentPage = 0) }
            } else {
                _uiState.update { it.copy(isLoadingMore = true, error = null) }
            }

            // Get today's date in yyyy-MM-dd format
            val todayDate = dateFormatter.format(Date())
            val currentState = _uiState.value

            val result = getTodayStandup(
                teamId = 1, // Using hardcoded teamId for now
                page = currentState.currentPage,
                size = currentState.pageSize,
                standupDate = todayDate
            )

            when (result) {
                is NetworkResult.Success -> {
                    val data = result.data
                    val updatedEntries = if (resetList) {
                        data.items
                    } else {
                        currentState.standupEntries + data.items
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            error = null,
                            date = todayDate,
                            roster = emptyList(),
                            submissions = emptyList(),
                            pending = emptyList(),
                            lastUpdated = nowTime(),
                            standupEntries = updatedEntries,
                            currentPage = data.meta.page,
                            totalEntries = data.meta.totalElements,
                            totalPages = data.meta.totalPages,
                            canLoadMore = data.meta.page < data.meta.totalPages - 1
                        )
                    }
                }

                is NetworkResult.Error -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        error = result.message ?: "Unknown error"
                    )
                }
            }
        }
    }

    fun loadMore() {
        val currentState = _uiState.value
        if (currentState.canLoadMore && !currentState.isLoadingMore) {
            _uiState.update { it.copy(currentPage = currentState.currentPage + 1) }
            load(resetList = false)
        }
    }

    private fun nowTime(): String = try {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    } catch (_: Throwable) {
        "--:--"
    }
}
