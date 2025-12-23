package com.example.logger.domain.repository

import com.example.logger.core.network.NetworkResult
import com.example.logger.domain.model.Sample
import kotlinx.coroutines.flow.Flow

interface SampleRepository {
    fun getSamples(): Flow<NetworkResult<List<Sample>>>
}

