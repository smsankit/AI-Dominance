package com.example.logger.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ErrorResponseDto(
    @SerializedName("errorMessage")
    val errorMessage: String?,
    @SerializedName("errorCode")
    val errorCode: String?,
    @SerializedName("detailedMessageList")
    val detailedMessageList: List<String>?
)

