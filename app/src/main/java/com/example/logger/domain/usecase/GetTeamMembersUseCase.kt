package com.example.logger.domain.usecase

import com.example.logger.core.datastore.PreferencesManager
import com.example.logger.domain.model.TeamMemberData
import com.example.logger.domain.repository.TeamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetTeamMembersUseCase @Inject constructor(
    private val repository: TeamRepository,
    private val preferencesManager: PreferencesManager
) {
    operator fun invoke(teamId: Long, page: Int, size: Int): Flow<List<TeamMemberData>> = flow {
        val cached = preferencesManager.getTeamMembers().first()
        if (cached.isNotEmpty()) {
            emit(cached)
        } else {
            val apiResult = repository.getTeamMembers(teamId, page, size).first()
            preferencesManager.saveTeamMembers(apiResult)
            emit(apiResult)
        }
    }
}