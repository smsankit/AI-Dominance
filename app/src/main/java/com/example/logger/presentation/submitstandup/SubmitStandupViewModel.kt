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
        viewModelScope.launch {
            getTodayStandupUseCase().collect { res ->
                if (res is NetworkResult.Success) {
                    _uiState.value = _uiState.value.copy(roster = res.data.roster)
                    if (_uiState.value.name.isBlank() && res.data.roster.isNotEmpty()) {
                        _uiState.value = _uiState.value.copy(name = res.data.roster.last())
                    }
                }
            }
        }
    }

    fun onNameChange(v: String) { _uiState.value = _uiState.value.copy(name = v, nameError = false) }
    fun onYesterdayChange(v: String) { _uiState.value = _uiState.value.copy(yesterday = v, yesterdayError = false) }
    fun onTodayChange(v: String) { _uiState.value = _uiState.value.copy(today = v, todayError = false) }
    fun onBlockersChange(v: String) { _uiState.value = _uiState.value.copy(blockers = v) }

    fun submit(onSuccess: () -> Unit) {
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
        viewModelScope.launch {
            when (val res = submitUseCase(s.name, s.yesterday, s.today, s.blockers.ifBlank { null })) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(isSubmitting = false, submittedAt = System.currentTimeMillis().toString())
                    _events.send(SubmitStandupUiEvent.Submitted)
                    onSuccess()
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(isSubmitting = false)
                    _events.send(SubmitStandupUiEvent.ApiError(res.message ?: "unknown_error"))
                }
            }
        }
    }
}
