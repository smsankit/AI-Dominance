package com.example.logger.data.mapper

import com.example.logger.data.remote.dto.StandupDto
import com.example.logger.data.remote.dto.StandupResponseDto
import com.example.logger.domain.model.Standup
import com.example.logger.domain.model.StandupDay

fun StandupDto.toDomain() = Standup(
    id = id,
    name = name,
    yesterday = yesterday,
    today = today,
    blockers = blockers,
    time = time,
    editedAt = editedAt
)

fun StandupResponseDto.toDomain() = StandupDay(
    date = date,
    roster = roster,
    submissions = submissions.map { it.toDomain() }
)

