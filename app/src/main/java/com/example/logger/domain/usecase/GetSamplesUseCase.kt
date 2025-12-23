package com.example.logger.domain.usecase

import com.example.logger.core.network.NetworkResult
import com.example.logger.domain.model.Sample
import com.example.logger.domain.repository.SampleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSamplesUseCase @Inject constructor(
    private val repository: SampleRepository
) {
    operator fun invoke(): Flow<NetworkResult<List<Sample>>> = repository.getSamples()
}

