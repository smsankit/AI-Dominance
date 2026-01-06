package com.example.logger.presentation.export

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.logger.core.network.NetworkResult
import com.example.logger.domain.model.StandupEntryData
import com.example.logger.domain.usecase.GetTodayStandupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

data class ExportUiState(
    val selectedDate: String = "",
    val standupEntries: List<StandupEntryData> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val totalEntries: Int = 0
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
        _uiState.value = _uiState.value.copy(selectedDate = date)
        loadStandups(date)
    }

    private fun loadStandups(date: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = getTodayStandup(
                page = 0,
                size = 100, // Get all entries for export
                standupDate = date
            )

            when (result) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        standupEntries = result.data.items,
                        isLoading = false,
                        error = null,
                        totalEntries = result.data.meta.total
                    )
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        standupEntries = emptyList(),
                        isLoading = false,
                        error = result.message ?: "An error occurred"
                    )
                }
            }
        }
    }
}

