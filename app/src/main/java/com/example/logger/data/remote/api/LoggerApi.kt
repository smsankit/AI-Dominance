package com.example.logger.data.remote.api

import com.example.logger.data.remote.dto.SampleDto
import com.example.logger.data.remote.dto.StandupResponseDto
import com.example.logger.data.remote.dto.SubmitStandupRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface LoggerApi {
    @GET("sample")
    suspend fun getSamples(): List<SampleDto>

    @GET("standup/today")
    suspend fun getStandup(): StandupResponseDto

    @POST("standup")
    suspend fun submitStandup(@Body body: SubmitStandupRequestDto)
}
