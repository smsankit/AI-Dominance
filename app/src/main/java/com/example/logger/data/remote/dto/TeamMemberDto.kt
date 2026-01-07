package com.example.logger.data.remote.dto

import com.google.gson.annotations.SerializedName

// DTO for a single team member
data class TeamMemberDto(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String
)

// DTO for the paged response
data class TeamMembersResponseDto(
    @SerializedName("data") val items: List<TeamMemberDto>,
    @SerializedName("meta") val meta: MetaDto
)

data class MetaDto(
    @SerializedName("page") val page: Int,
    @SerializedName("size") val size: Int,
    @SerializedName("total") val total: Int
)

