package com.example.logger.domain.usecase

import com.example.logger.core.datastore.PreferencesManager
import com.example.logger.core.network.NetworkResult
import com.example.logger.domain.model.TeamMemberData
import com.example.logger.domain.repository.TeamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetTeamMembersUseCase @Inject constructor(
    private val repository: TeamRepository,
    private val preferencesManager: PreferencesManager
) {
    operator fun invoke(teamId: Long, page: Int, size: Int, isApiCallRequired : Boolean = false): Flow<NetworkResult<List<TeamMemberData>>> = flow {
        // Try cache first
        val cached = preferencesManager.getTeamMembers().first()
        if (cached.isNotEmpty() && !isApiCallRequired) {
            emit(NetworkResult.Success(cached))
            return@flow
        }
        // Call API through repository
        repository.getTeamMembers(teamId, page, size).collect { result ->
            when (result) {
                is NetworkResult.Success<*> -> {
                    preferencesManager.saveTeamMembers(result.data as List<TeamMemberData>)
                    emit(NetworkResult.Success(result.data))
                }
                is NetworkResult.Error -> emit(result)
            }
        }
    }
}