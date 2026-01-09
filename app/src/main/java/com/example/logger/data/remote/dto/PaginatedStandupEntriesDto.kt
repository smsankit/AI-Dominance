package com.example.logger.data.remote.dto

data class PaginatedStandupEntriesDto(
    val data: List<StandupEntryDto>,
    val meta: PaginationMetaDto
)

data class StandupEntryDto(
    val id: Long,
    val standupDate: String,
    val yesterdayWork: String,
    val todayPlan: String,
    val blockers: String?,
    val teamMemberId: Long,
    val teamId: Long,
    val createdAt: String?,
    val updatedAt: String?,
    val teamMember: TeamMemberDto
)

data class PaginationMetaDto(
    val page: Int,
    val size: Int,
    val totalElements: Int,
    val totalPages: Int
)

