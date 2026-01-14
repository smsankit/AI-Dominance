package com.example.logger.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.logger.core.network.NetworkResult
import com.example.logger.core.util.DateFormatter
import com.example.logger.data.mapper.toSummary
import com.example.logger.domain.model.Standup
import com.example.logger.domain.usecase.GetTeamMembersUseCase
import com.example.logger.domain.usecase.GetTeamSentimentsUseCase
import com.example.logger.domain.usecase.GetTodayStandupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTodayStandup: GetTodayStandupUseCase,
    private val getTeamMembers: GetTeamMembersUseCase,
    private val getTeamSentiments: GetTeamSentimentsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState


    // Map to store team member id to name mapping
    private val teamMembersMap = mutableMapOf<Long, String>()

    init {
        fetchTeamMembers()
        loadTeamSentiments()
    }

    private fun fetchTeamMembers() {
        viewModelScope.launch {
            getTeamMembers(1, 0, 100, true).collect { result ->
                if (result is NetworkResult.Success) {
                    val members = result.data
                    // Store in map for easy lookup
                    teamMembersMap.clear()
                    members.forEach { member ->
                        teamMembersMap[member.id] = member.name
                    }
                    _uiState.update { it.copy(roster = members.map { m -> m.name }) }
                    // Load standup data after team members are fetched
                    load()
                }
            }
        }
    }

    fun load(resetList: Boolean = true, fetchAllStandups: Boolean = false) {
        viewModelScope.launch {
            // Only show full loading if team members haven't been loaded yet
            val showFullLoading = teamMembersMap.isEmpty()

            if (resetList) {
                _uiState.update {
                    it.copy(
                        isLoading = showFullLoading,
                        error = null,
                        currentPage = 0
                    )
                }
            } else {
                _uiState.update { it.copy(isLoadingMore = true, error = null) }
            }

            // Get today's date in yyyy-MM-dd format (IST)
            val todayDate = DateFormatter.getCurrentDateString()
            val currentState = _uiState.value

            // If fetchAllStandups is true, use a large size to get all in one call
            val pageSize = if (fetchAllStandups) 100 else currentState.pageSize
            val page = if (fetchAllStandups) 0 else currentState.currentPage

            val result = getTodayStandup(
                teamId = 1, // Using hardcoded teamId for now
                page = page,
                size = pageSize,
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

                    // Map StandupEntryData to Standup model for UI
                    // createdAt and updatedAt are already formatted to time strings (HH:mm) in the mapper
                    // Use teamMember.name from API response if available, fallback to teamMembersMap or ID
                    val mappedSubmissions = updatedEntries.map { entry ->
                        val memberName = entry.teamMember?.name
                            ?: teamMembersMap[entry.teamMemberId]
                            ?: "Team Member #${entry.teamMemberId}"
                        val time = entry.createdAt ?: "--:--"

                        Standup(
                            id = entry.id.toString(),
                            name = memberName,
                            yesterday = entry.yesterdayWork!!,
                            today = entry.todayPlan!!,
                            blockers = entry.blockers,
                            time = time,
                            editedAt = entry.updatedAt
                        )
                    }

                    // Calculate pending members based on total count, not loaded items
                    // This ensures correct count during pagination
                    val totalSubmitted = data.meta.totalElements
                    val totalTeamMembers = teamMembersMap.size
                    val pendingCount = totalTeamMembers - totalSubmitted

                    // For the pending list, calculate based on ALL loaded entries across pagination
                    // This gives us an accurate list of who hasn't submitted based on what we've loaded so far
                    val submittedMemberIds = updatedEntries.map { it.teamMemberId }.toSet()
                    val pendingMembers = teamMembersMap.filterKeys { it !in submittedMemberIds }.values.toList()

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            error = null,
                            date = todayDate,
                            submissions = mappedSubmissions,
                            pending = pendingMembers, // Names list (from loaded data)
                            pendingCount = pendingCount, // Accurate count (from totalElements)
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

    private fun loadTeamSentiments() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSentimentLoading = true, sentimentError = null) }

            // Compute 30-day range in IST: from = today - 30 days, to = today
            val toDate = DateFormatter.getCurrentDateString()
            val fromDate = DateFormatter.getDateDaysAgoString(30)

            getTeamSentiments(
                teamId = 1, // Using hardcoded teamId for now
                from = fromDate,
                to = toDate
            ).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        val summary = result.data.toSummary()
                        _uiState.update {
                            it.copy(
                                sentimentSummary = summary,
                                isSentimentLoading = false,
                                sentimentError = null
                            )
                        }
                    }
                    is NetworkResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isSentimentLoading = false,
                                sentimentError = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    private fun nowTime(): String = DateFormatter.getCurrentTimeString()
}
