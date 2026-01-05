package com.example.logger.domain.usecase

import com.example.logger.core.network.NetworkResult
import com.example.logger.domain.model.StandupEntryData
import com.example.logger.domain.model.StandupEntryRequestData
import com.example.logger.domain.repository.StandupRepository
import javax.inject.Inject

class SubmitStandupUseCase @Inject constructor(
    private val repository: StandupRepository
) {
    suspend operator fun invoke(request: StandupEntryRequestData): NetworkResult<StandupEntryData> =
        repository.submitStandupEntry(request)
}
