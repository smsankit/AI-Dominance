package com.example.logger.data.repository

import com.example.logger.data.mapper.TeamMemberDtoMapper
import com.example.logger.data.remote.api.TeamApiService
import com.example.logger.domain.model.TeamMemberData
import com.example.logger.domain.repository.TeamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class TeamRepositoryImpl @Inject constructor(
    private val api: TeamApiService,
    private val teamMemberDtoMapper: TeamMemberDtoMapper
) : TeamRepository {
    override fun getTeamMembers(teamId: Long, page: Int, size: Int): Flow<List<TeamMemberData>> =
        flow {
            val response = api.getTeamMembers(teamId, page, size)
            val members = response.items.map { teamMemberDtoMapper.map(it) }
            emit(members)
        }
}