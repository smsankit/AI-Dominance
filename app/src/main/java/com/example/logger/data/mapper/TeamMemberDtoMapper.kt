package com.example.logger.data.mapper

import com.example.logger.data.remote.dto.TeamMemberDto
import com.example.logger.domain.model.TeamMemberData
import javax.inject.Inject

class TeamMemberDtoMapper @Inject constructor() {
    fun map(dto: TeamMemberDto): TeamMemberData = TeamMemberData(
        id = dto.id,
        name = dto.name,
        email = dto.email,
        createdAt = dto.createdAt,
        updatedAt = dto.updatedAt
    )
}

