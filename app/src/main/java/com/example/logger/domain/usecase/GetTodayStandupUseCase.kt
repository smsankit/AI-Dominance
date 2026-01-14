package com.example.logger.domain.usecase

import com.example.logger.core.network.NetworkResult
import com.example.logger.domain.model.PaginatedStandupEntriesData
import com.example.logger.domain.repository.StandupRepository
import javax.inject.Inject

class GetTodayStandupUseCase @Inject constructor(
    private val repository: StandupRepository
) {
    suspend operator fun invoke(
        teamId: Long,
        page: Int? = null,
        size: Int? = null,
        teamMemberId: Long? = null,
        standupDate: String? = null,
        status: String? = null
    ): NetworkResult<PaginatedStandupEntriesData> {
        return repository.getStandupEntries(
            teamId = teamId,
            page = page,
            size = size,
            teamMemberId = teamMemberId,
            standupDate = standupDate,
            status = status
        )
    }
}

