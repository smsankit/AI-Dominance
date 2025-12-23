package com.example.logger.data.remote.api

import com.example.logger.data.remote.dto.SampleDto
import com.example.logger.data.remote.dto.StandupResponseDto
import retrofit2.http.GET

interface LoggerApi {
    @GET("sample")
    suspend fun getSamples(): List<SampleDto>

    @GET("standup/today")
    suspend fun getStandup(): StandupResponseDto
}
