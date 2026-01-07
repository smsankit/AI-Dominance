package com.example.logger.data.repository

import com.example.logger.core.network.NetworkResult
import com.example.logger.core.network.safeNetworkCall
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
    override fun getTeamMembers(teamId: Long, page: Int, size: Int): Flow<NetworkResult<List<TeamMemberData>>> =
        flow {
            // Hardcode page=0 and size=100 to always fetch all team members
            val result = safeNetworkCall { api.getTeamMembers(teamId, 0, 100) }
            when (result) {
                is NetworkResult.Success -> {
                    val members = result.data.items.map { teamMemberDtoMapper.map(it) }
                    emit(NetworkResult.Success(members))
                }
                is NetworkResult.Error -> emit(result)
            }
        }
}