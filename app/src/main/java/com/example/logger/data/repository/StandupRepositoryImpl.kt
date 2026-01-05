package com.example.logger.data.repository

import com.example.logger.core.network.NetworkResult
import com.example.logger.data.mapper.toDomain
import com.example.logger.data.remote.api.LoggerApi
import com.example.logger.data.remote.dto.SubmitStandupEntryRequestDto
import com.example.logger.domain.model.StandupDay
import com.example.logger.domain.model.StandupEntryData
import com.example.logger.domain.model.StandupEntryRequestData
import com.example.logger.domain.repository.StandupRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StandupRepositoryImpl @Inject constructor(
    private val api: LoggerApi
) : StandupRepository {
    override fun getTodayStandup(): Flow<NetworkResult<StandupDay>> = flow {
        try {
            val dto = api.getStandup()
            emit(NetworkResult.Success(dto.toDomain()))
        } catch (e: Exception) {
            emit(NetworkResult.Error(message = e.message, throwable = e))
        }
    }

    override suspend fun submitStandupEntry(request: StandupEntryRequestData): NetworkResult<StandupEntryData> {
        return try {
            val response = api.submitStandupEntry(
                SubmitStandupEntryRequestDto(
                    standupDate = request.standupDate,
                    yesterdayWork = request.yesterdayWork,
                    todayPlan = request.todayPlan,
                    blockers = request.blockers,
                    teamMemberId = request.teamMemberId,
                    teamId = request.teamId
                )
            )
            NetworkResult.Success(response.toDomain())
        } catch (e: Exception) {
            NetworkResult.Error(message = e.message, throwable = e)
        }
    }
}
