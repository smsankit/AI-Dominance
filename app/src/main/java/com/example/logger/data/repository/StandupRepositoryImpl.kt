package com.example.logger.data.repository

import com.example.logger.core.network.NetworkResult
import com.example.logger.data.mapper.toDomain
import com.example.logger.data.remote.api.LoggerApi
import com.example.logger.data.remote.dto.SubmitStandupRequestDto
import com.example.logger.domain.model.StandupDay
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

    override suspend fun submitStandup(
        name: String,
        yesterday: String,
        today: String,
        blockers: String?
    ): NetworkResult<Unit> {
        return try {
            api.submitStandup(
                SubmitStandupRequestDto(
                    name = name,
                    yesterday = yesterday,
                    today = today,
                    blockers = blockers
                )
            )
            NetworkResult.Success(Unit)
        } catch (e: Exception) {
            NetworkResult.Error(message = e.message, throwable = e)
        }
    }
}
