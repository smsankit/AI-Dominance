package com.example.logger.data.remote.api

import com.example.logger.data.remote.dto.TeamMembersResponseDto
import com.example.logger.data.remote.dto.TeamSentimentItemDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TeamApiService {
    @GET("teams/{teamId}/members")
    suspend fun getTeamMembers(
        @Path("teamId") teamId: Long,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): TeamMembersResponseDto

    @GET("teams/{teamId}/sentiments")
    suspend fun getTeamSentiments(
        @Path("teamId") teamId: Long,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null
    ): List<TeamSentimentItemDto>
}

