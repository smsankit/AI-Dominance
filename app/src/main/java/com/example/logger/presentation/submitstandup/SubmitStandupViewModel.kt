package com.example.logger.presentation.submitstandup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.logger.core.network.NetworkResult
import com.example.logger.domain.usecase.GetTodayStandupUseCase
import com.example.logger.domain.usecase.SubmitStandupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class SubmitStandupUiState(
    val name: String = "",
    val yesterday: String = "",
    val today: String = "",
    val blockers: String = "",
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val submittedAt: String? = null,
    val roster: List<String> = emptyList(),
    val nameError: Boolean = false,
    val yesterdayError: Boolean = false,
    val todayError: Boolean = false
)

sealed interface SubmitStandupUiEvent {
    data class ApiError(val message: String): SubmitStandupUiEvent
    object Submitted: SubmitStandupUiEvent
}

@HiltViewModel
class SubmitStandupViewModel @Inject constructor(
    private val submitUseCase: SubmitStandupUseCase,
    private val getTodayStandupUseCase: GetTodayStandupUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(SubmitStandupUiState())
    val uiState: StateFlow<SubmitStandupUiState> = _uiState
    private val _events = Channel<SubmitStandupUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        // Hardcoded roster per wireframe
        val defaultRoster = listOf("Alex Johnson","Priya Verma","Miguel Santos","Sarah Kim","You")
        _uiState.value = _uiState.value.copy(
            roster = defaultRoster,
            name = if (_uiState.value.name.isBlank()) "You" else _uiState.value.name
        )
        // Optionally merge API roster if present, but keep hardcoded as source of truth for now
        viewModelScope.launch {
            getTodayStandupUseCase().collect { res ->
                if (res is NetworkResult.Success) {
                    // Merge unique names preserving order: hardcoded first, then any new ones
                    val merged = (defaultRoster + res.data.roster).distinct()
                    _uiState.value = _uiState.value.copy(roster = merged)
                    if (_uiState.value.name.isBlank() && merged.isNotEmpty()) {
                        _uiState.value = _uiState.value.copy(name = if (merged.contains("You")) "You" else merged.first())
                    }
                }
            }
        }
    }

    fun onNameChange(v: String) { _uiState.value = _uiState.value.copy(name = v, nameError = false) }
    fun onYesterdayChange(v: String) { _uiState.value = _uiState.value.copy(yesterday = v, yesterdayError = false) }
    fun onTodayChange(v: String) { _uiState.value = _uiState.value.copy(today = v, todayError = false) }
    fun onBlockersChange(v: String) { _uiState.value = _uiState.value.copy(blockers = v) }

    fun submit(onSuccess: (String) -> Unit) {
        val s = _uiState.value
        val nameErr = s.name.isBlank()
        val yErr = s.yesterday.isBlank()
        val tErr = s.today.isBlank()
        if (nameErr || yErr || tErr) {
            _uiState.value = s.copy(
                nameError = nameErr,
                yesterdayError = yErr,
                todayError = tErr,
                error = null
            )
            return
        }
        _uiState.value = s.copy(isSubmitting = true, error = null)
        // No API call for now: simulate success and navigate to confirm with timestamp
        val ts = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        _uiState.value = _uiState.value.copy(isSubmitting = false, submittedAt = ts)
        viewModelScope.launch { _events.send(SubmitStandupUiEvent.Submitted) }
        onSuccess(ts)
    }
}
