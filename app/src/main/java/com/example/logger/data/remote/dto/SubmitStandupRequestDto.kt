package com.example.logger.data.remote.dto

data class SubmitStandupRequestDto(
    val name: String,
    val yesterday: String,
    val today: String,
    val blockers: String?
)
