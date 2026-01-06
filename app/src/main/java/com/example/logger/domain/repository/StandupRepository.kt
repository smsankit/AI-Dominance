package com.example.logger.domain.repository

import com.example.logger.core.network.NetworkResult
import com.example.logger.domain.model.PaginatedStandupEntriesData
import com.example.logger.domain.model.StandupDay
import com.example.logger.domain.model.StandupEntryData
import com.example.logger.domain.model.StandupEntryRequestData
import kotlinx.coroutines.flow.Flow

interface StandupRepository {
    fun getTodayStandup(): Flow<NetworkResult<StandupDay>>
    suspend fun submitStandupEntry(request: StandupEntryRequestData): NetworkResult<StandupEntryData>
    suspend fun getStandupEntries(
        teamId: Long,
        page: Int? = null,
        size: Int? = null,
        teamMemberId: Long? = null,
        standupDate: String? = null
    ): NetworkResult<PaginatedStandupEntriesData>
}
