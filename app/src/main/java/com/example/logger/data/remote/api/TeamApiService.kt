package com.example.logger.data.remote.api

import com.example.logger.data.remote.dto.TeamMembersResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TeamApiService {
    @GET("standuploggerservices/teams/{teamId}/members")
    suspend fun getTeamMembers(
        @Path("teamId") teamId: Long,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): TeamMembersResponseDto
}

