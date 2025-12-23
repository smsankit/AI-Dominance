package com.example.logger.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.logger.core.network.NetworkResult
import com.example.logger.domain.usecase.GetTodayStandupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTodayStandup: GetTodayStandupUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            getTodayStandup().collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        val day = result.data
                        val submittedNames = day.submissions.map { it.name }.toSet()
                        val pending = day.roster.filterNot { it in submittedNames }
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = null,
                                date = day.date,
                                roster = day.roster,
                                submissions = day.submissions,
                                pending = pending,
                                lastUpdated = nowTime()
                            )
                        }
                    }
                    is NetworkResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.message ?: "Unknown error") }
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
