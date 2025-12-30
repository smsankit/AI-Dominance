package com.example.logger.domain.usecase

import com.example.logger.core.network.NetworkResult
import com.example.logger.domain.repository.StandupRepository
import javax.inject.Inject

class SubmitStandupUseCase @Inject constructor(
    private val repository: StandupRepository
) {
    suspend operator fun invoke(
        name: String,
        yesterday: String,
        today: String,
        blockers: String?
    ): NetworkResult<Unit> = repository.submitStandup(name, yesterday, today, blockers)
}

