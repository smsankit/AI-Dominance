package com.example.logger.data.repository

import com.example.logger.core.network.NetworkResult
import com.example.logger.core.network.safeNetworkCall
import com.example.logger.data.mapper.toDomain
import com.example.logger.data.remote.api.LoggerApi
import com.example.logger.data.remote.dto.SubmitStandupEntryRequestDto
import com.example.logger.domain.model.PaginatedStandupEntriesData
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
            val result = safeNetworkCall { api.getStandup() }
            when (result) {
                is NetworkResult.Success -> emit(NetworkResult.Success(result.data.toDomain()))
                is NetworkResult.Error -> emit(NetworkResult.Error(message = result.message, throwable = result.throwable, code = result.code))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(message = e.message, throwable = e))
        }
    }

    override suspend fun submitStandupEntry(request: StandupEntryRequestData): NetworkResult<StandupEntryData> {
        return try {
            when (val result = safeNetworkCall {
                api.submitStandupEntry(
                    SubmitStandupEntryRequestDto(
                        standupDate = request.standupDate,
                        yesterdayWork = request.yesterdayWork,
                        todayPlan = request.todayPlan,
                        blockers = request.blockers,
                        teamMemberId = request.teamMemberId,
                        teamId = request.teamId
                    )
                )
            }) {
                is NetworkResult.Success -> NetworkResult.Success(result.data.toDomain())
                is NetworkResult.Error -> NetworkResult.Error(message = result.message, throwable = result.throwable, code = result.code)
            }
        } catch (e: Exception) {
            NetworkResult.Error(message = e.message, throwable = e)
        }
    }

    override suspend fun getStandupEntries(
        teamId: Long,
        page: Int?,
        size: Int?,
        teamMemberId: Long?,
        standupDate: String?,
        status: String?
    ): NetworkResult<PaginatedStandupEntriesData> {
        return try {
            when (val result = safeNetworkCall {
                api.getStandupEntries(
                    teamId = teamId,
                    page = page,
                    size = size,
                    teamMemberId = teamMemberId,
                    standupDate = standupDate,
                    status = status
                )
            }) {
                is NetworkResult.Success -> NetworkResult.Success(result.data.toDomain())
                is NetworkResult.Error -> NetworkResult.Error(message = result.message, throwable = result.throwable, code = result.code)
            }
        } catch (e: Exception) {
            NetworkResult.Error(message = e.message, throwable = e)
        }
    }
}
