package com.example.logger.data.remote.dto

data class StandupEntryResponseDto(
    val id: Long,
    val standupDate: String,
    val yesterdayWork: String,
    val todayPlan: String,
    val blockers: String?,
    val teamMemberId: Long,
    val teamId: Long,
    val createdAt: String = "",
    val updatedAt: String = ""
)

