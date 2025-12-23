package com.example.logger.domain.repository

import com.example.logger.core.network.NetworkResult
import com.example.logger.domain.model.StandupDay
import kotlinx.coroutines.flow.Flow

interface StandupRepository {
    fun getTodayStandup(): Flow<NetworkResult<StandupDay>>
}

