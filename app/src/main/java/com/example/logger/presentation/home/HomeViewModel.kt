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

    fun load() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            // Get today's date in yyyy-MM-dd format
            val todayDate = dateFormatter.format(Date())

            val result = getTodayStandup(
                page = 0,
                size = 20,
                standupDate = todayDate
            )

            when (result) {
                is NetworkResult.Success -> {
                    val data = result.data
                    // For now, we'll need to handle the roster differently
                    // as the new API returns entries, not the full roster
                    val submittedIds = data.items.map { it.teamMemberId }.toSet()

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = null,
                            date = todayDate,
                            roster = emptyList(), // Will need to fetch roster separately if needed
                            submissions = emptyList(), // Converting StandupEntryData to Standup if needed
                            pending = emptyList(),
                            lastUpdated = nowTime(),
                            standupEntries = data.items,
                            totalEntries = data.meta.total
                        )
                    }
                }

                is NetworkResult.Error -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = result.message ?: "Unknown error"
                    )
                }
            }
        }
    }

    private fun nowTime(): String = try {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    } catch (_: Throwable) {
        "--:--"
    }
}
