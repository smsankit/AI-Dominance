package com.example.logger.data.repository

import com.example.logger.core.network.NetworkResult
import com.example.logger.data.mapper.toDomain
import com.example.logger.data.remote.api.LoggerApi
import com.example.logger.domain.model.Sample
import com.example.logger.domain.repository.SampleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SampleRepositoryImpl @Inject constructor(
    private val api: LoggerApi
) : SampleRepository {
    override fun getSamples(): Flow<NetworkResult<List<Sample>>> = flow {
        try {
            val dto = api.getSamples()
            emit(NetworkResult.Success(dto.map { it.toDomain() }))
        } catch (e: Exception) {
            emit(NetworkResult.Error(message = e.message, throwable = e))
        }
    }
}

