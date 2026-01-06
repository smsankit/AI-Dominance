package com.example.logger.domain.repository

import com.example.logger.core.network.NetworkResult
import com.example.logger.domain.model.TeamMemberData
import kotlinx.coroutines.flow.Flow

interface TeamRepository {
    fun getTeamMembers(teamId: Long, page: Int, size: Int): Flow<NetworkResult<List<TeamMemberData>>>
}