package com.example.logger.data.mapper

import com.example.logger.core.util.DateFormatter
import com.example.logger.data.remote.dto.PaginatedStandupEntriesDto
import com.example.logger.data.remote.dto.PaginationMetaDto
import com.example.logger.data.remote.dto.StandupDto
import com.example.logger.data.remote.dto.StandupEntryDto
import com.example.logger.data.remote.dto.StandupEntryResponseDto
import com.example.logger.data.remote.dto.StandupResponseDto
import com.example.logger.domain.model.PaginatedStandupEntriesData
import com.example.logger.domain.model.PaginationMetaData
import com.example.logger.domain.model.Standup
import com.example.logger.domain.model.StandupDay
import com.example.logger.domain.model.StandupEntryData

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

fun StandupEntryResponseDto.toDomain() = StandupEntryData(
    id = id,
    standupDate = standupDate,
    yesterdayWork = yesterdayWork,
    todayPlan = todayPlan,
    blockers = blockers,
    teamMemberId = teamMemberId,
    teamId = teamId,
    createdAt = DateFormatter.parseToTimeString(createdAt),
    updatedAt = DateFormatter.parseToTimeString(updatedAt)
)

fun StandupEntryDto.toDomain() = StandupEntryData(
    id = id,
    standupDate = standupDate,
    yesterdayWork = yesterdayWork,
    todayPlan = todayPlan,
    blockers = blockers,
    teamMemberId = teamMemberId,
    teamId = teamId,
    createdAt = DateFormatter.parseToTimeString(createdAt),
    updatedAt = DateFormatter.parseToTimeString(updatedAt)
)

fun PaginationMetaDto.toDomain() = PaginationMetaData(
    page = page,
    size = size,
    totalElements = totalElements,
    totalPages = totalPages
)

fun PaginatedStandupEntriesDto.toDomain() = PaginatedStandupEntriesData(
    items = data.map { it.toDomain() },
    meta = meta.toDomain()
)
