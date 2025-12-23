package com.example.logger.data.remote.dto

data class StandupDto(
    val id: String,
    val name: String,
    val yesterday: String,
    val today: String,
    val blockers: String?,
    val time: String,
    val editedAt: String?
)

data class StandupResponseDto(
    val date: String,
    val roster: List<String>,
    val submissions: List<StandupDto>
)

