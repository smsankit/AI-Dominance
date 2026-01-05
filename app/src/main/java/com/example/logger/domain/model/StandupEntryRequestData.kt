package com.example.logger.domain.model

data class StandupEntryRequestData(
    val standupDate: String,
    val yesterdayWork: String,
    val todayPlan: String,
    val blockers: String?,
    val teamMemberId: Long,
    val teamId: Long
)

