package com.example.logger.presentation.submitstandup.mapper

import com.example.logger.domain.model.StandupEntryRequestData
import com.example.logger.presentation.submitstandup.SubmitStandupUiState
import javax.inject.Inject

class SubmitStandupUiMapper @Inject constructor() {
    fun toRequest(
        state: SubmitStandupUiState,
        standupDate: String,
        teamMemberId: Long,
        teamId: Long
    ): StandupEntryRequestData = StandupEntryRequestData(
        standupDate = standupDate,
        yesterdayWork = state.yesterday,
        todayPlan = state.today,
        blockers = state.blockers.ifBlank { null },
        teamMemberId = teamMemberId,
        teamId = teamId
    )
}

