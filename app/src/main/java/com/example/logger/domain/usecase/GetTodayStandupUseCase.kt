package com.example.logger.domain.usecase

import com.example.logger.core.network.NetworkResult
import com.example.logger.domain.model.StandupDay
import com.example.logger.domain.repository.StandupRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTodayStandupUseCase @Inject constructor(
    private val repository: StandupRepository
) {
    operator fun invoke(): Flow<NetworkResult<StandupDay>> = repository.getTodayStandup()
}

