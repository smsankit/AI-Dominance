package com.example.logger.presentation.submitstandup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.logger.core.datastore.PreferencesManager
import com.example.logger.core.network.NetworkResult
import com.example.logger.domain.usecase.GetTeamMembersUseCase
import com.example.logger.domain.usecase.GetTodayStandupUseCase
import com.example.logger.domain.usecase.SubmitStandupUseCase
import com.example.logger.presentation.submitstandup.mapper.SubmitStandupUiMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
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
    private val getTodayStandupUseCase: GetTodayStandupUseCase,
    private val preferencesManager: PreferencesManager,
    private val uiMapper: SubmitStandupUiMapper,
    private val getTeamMembersUseCase: GetTeamMembersUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(SubmitStandupUiState())
    val uiState: StateFlow<SubmitStandupUiState> = _uiState
    private val _events = Channel<SubmitStandupUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        // Fetch team members from use case and set roster
        viewModelScope.launch {
            getTeamMembersUseCase(1, 1, 20).collect { members ->
                val names = members.map { it.name }
                _uiState.value = _uiState.value.copy(
                    roster = names,
                    name = if (_uiState.value.name.isBlank() && names.isNotEmpty()) names.first() else _uiState.value.name
                )
            }
        }
        // Merge API roster if present
        viewModelScope.launch {
            getTodayStandupUseCase().collect { res ->
                if (res is NetworkResult.Success) {
                    val merged = (_uiState.value.roster + res.data.roster).distinct()
                    _uiState.value = _uiState.value.copy(roster = merged)
                    if (_uiState.value.name.isBlank() && merged.isNotEmpty()) {
                        _uiState.value = _uiState.value.copy(name = merged.first())
                    }
                }
            }
        }
        // Merge cached team members from preferences if available
        viewModelScope.launch {
            preferencesManager.getTeamMembers().collectLatest { members ->
                if (members.isNotEmpty()) {
                    val names = members.map { it.name }
                    val merged = (_uiState.value.roster + names).distinct()
                    _uiState.value = _uiState.value.copy(roster = merged)
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

        viewModelScope.launch {
            // Resolve team member id from cached preferences by name
            val sNow = _uiState.value
            val list = preferencesManager.getTeamMembers().first()
            val teamMemberId = list.firstOrNull { it.name == sNow.name }?.id ?: 0
            val standupDate = try { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) } catch (_: Throwable) { "" }
            val request = uiMapper.toRequest(
                state = sNow,
                standupDate = standupDate,
                teamMemberId = teamMemberId,
                teamId = 1L
            )
            val result = submitUseCase(request)
            when (result) {
                is NetworkResult.Success -> {
                    val ts = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                    _uiState.value = _uiState.value.copy(isSubmitting = false, submittedAt = ts)
                    _events.send(SubmitStandupUiEvent.Submitted)
                    onSuccess(ts)
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(isSubmitting = false, error = result.message ?: "Submission failed")
                    _events.send(SubmitStandupUiEvent.ApiError(_uiState.value.error ?: "Submission failed"))
                }
            }
        }
    }
}
