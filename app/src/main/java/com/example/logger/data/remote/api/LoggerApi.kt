package com.example.logger.data.remote.api

import com.example.logger.data.remote.dto.PaginatedStandupEntriesDto
import com.example.logger.data.remote.dto.SampleDto
import com.example.logger.data.remote.dto.StandupEntryResponseDto
import com.example.logger.data.remote.dto.StandupResponseDto
import com.example.logger.data.remote.dto.SubmitStandupEntryRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface LoggerApi {
    @GET("sample")
    suspend fun getSamples(): List<SampleDto>

    @GET("standup/today")
    suspend fun getStandup(): StandupResponseDto

    @GET("standup-entries")
    suspend fun getStandupEntries(
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
        @Query("teamId") teamId: Long? = null,
        @Query("teamMemberId") teamMemberId: Long? = null,
        @Query("standupDate") standupDate: String? = null
    ): PaginatedStandupEntriesDto

    @POST("standup-entries")
    suspend fun submitStandupEntry(@Body body: SubmitStandupEntryRequestDto): StandupEntryResponseDto
}
