package com.example.logger.domain.usecase

import com.example.logger.core.network.NetworkResult
import com.example.logger.domain.model.PaginatedStandupEntriesData
import com.example.logger.domain.repository.StandupRepository
import javax.inject.Inject

class GetTodayStandupUseCase @Inject constructor(
    private val repository: StandupRepository
) {
    suspend operator fun invoke(
        page: Int? = null,
        size: Int? = null,
        teamId: Long? = null,
        teamMemberId: Long? = null,
        standupDate: String? = null
    ): NetworkResult<PaginatedStandupEntriesData> {
        return repository.getStandupEntries(
            page = page,
            size = size,
            teamId = teamId,
            teamMemberId = teamMemberId,
            standupDate = standupDate
        )
    }
}

