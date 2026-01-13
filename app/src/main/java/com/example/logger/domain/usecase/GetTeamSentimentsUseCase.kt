package com.example.logger.domain.usecase

import com.example.logger.core.network.NetworkResult
import com.example.logger.domain.model.TeamSentimentItem
import com.example.logger.domain.repository.TeamRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTeamSentimentsUseCase @Inject constructor(
    private val teamRepository: TeamRepository
) {
    operator fun invoke(teamId: Long, from: String? = null, to: String? = null): Flow<NetworkResult<List<TeamSentimentItem>>> {
        return teamRepository.getTeamSentiments(teamId, from, to)
    }
}

