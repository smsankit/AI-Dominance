package com.example.logger.data.remote.dto

data class SubmitStandupEntryRequestDto(
    val standupDate: String,
    val yesterdayWork: String,
    val todayPlan: String,
    val blockers: String?,
    val teamMemberId: Long,
    val teamId: Long
)

