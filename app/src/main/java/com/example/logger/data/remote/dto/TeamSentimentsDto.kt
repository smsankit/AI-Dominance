package com.example.logger.data.remote.dto

import com.google.gson.annotations.SerializedName

data class TeamSentimentItemDto(
    @SerializedName("id")
    val id: Long,
    @SerializedName("standupEntryId")
    val standupEntryId: Long,
    @SerializedName("value")
    val value: Int, // -1: Negative, 0: Neutral, 1: Positive
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String
)

