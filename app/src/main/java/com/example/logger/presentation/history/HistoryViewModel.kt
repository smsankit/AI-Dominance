package com.example.logger.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.logger.core.network.NetworkResult
import com.example.logger.domain.model.Standup
import com.example.logger.domain.usecase.GetTodayStandupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class HistoryUiState(
    val selectedDate: Date = Date(),
    val submissions: List<Standup> = emptyList()
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getTodayStandup: GetTodayStandupUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState

    private val dateKeyFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    init { refresh() }

    fun onPrevDate() {
        val cal = Calendar.getInstance().apply { time = _uiState.value.selectedDate }
        cal.add(Calendar.DAY_OF_MONTH, -1)
        _uiState.value = _uiState.value.copy(selectedDate = cal.time)
        refresh()
    }

    fun onNextDate() {
        val cal = Calendar.getInstance().apply { time = _uiState.value.selectedDate }
        cal.add(Calendar.DAY_OF_MONTH, 1)
        val next = cal.time
        val today = Date()
        if (dateKeyFmt.format(next) <= dateKeyFmt.format(today)) {
            _uiState.value = _uiState.value.copy(selectedDate = next)
            refresh()
        }
    }

    fun onPickDate(date: Date) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
        refresh()
    }

    private fun refresh() {
        // Source data from the same source as dashboard (today's standups) for now
        viewModelScope.launch {
            getTodayStandup().collect { res ->
                if (res is NetworkResult.Success) {
                    _uiState.value = _uiState.value.copy(submissions = res.data.submissions)
                } else {
                    _uiState.value = _uiState.value.copy(submissions = emptyList())
                }
            }
        }
    }
}
