package com.example.logger.domain.model

data class Standup(
    val id: String,
    val name: String,
    val yesterday: String,
    val today: String,
    val blockers: String?,
    val time: String,
    val editedAt: String?
)

data class StandupDay(
    val date: String,
    val roster: List<String>,
    val submissions: List<Standup>
)

