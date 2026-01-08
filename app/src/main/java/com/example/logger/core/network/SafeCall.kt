package com.example.logger.core.network

import com.example.logger.core.exception.AppException
import com.example.logger.data.remote.dto.ErrorResponseDto
import com.google.gson.Gson
import retrofit2.HttpException
import java.io.IOException

suspend inline fun <T> safeApiCall(crossinline block: suspend () -> T): T {
    try {
        return block()
    } catch (e: IOException) {
        throw AppException.Network(message = e.message, cause = e)
    } catch (e: HttpException) {
        val errorMessage = try {
            val errorBody = e.response()?.errorBody()?.string()
            if (!errorBody.isNullOrEmpty()) {
                val errorResponse = Gson().fromJson(errorBody, ErrorResponseDto::class.java)
                errorResponse.errorMessage ?: "HTTP ${e.code()}: ${e.message()}"
            } else {
                "HTTP ${e.code()}: ${e.message()}"
            }
        } catch (_: Exception) {
            "HTTP ${e.code()}: ${e.message()}"
        }
        throw AppException.Server(message = errorMessage, cause = e)
    } catch (e: AppException) {
        throw e
    } catch (e: Throwable) {
        throw AppException.Unknown(message = e.message, cause = e)
    }
}

suspend inline fun <T> safeNetworkCall(crossinline block: suspend () -> T): NetworkResult<T> = try {
    NetworkResult.Success(safeApiCall { block() })
} catch (e: AppException) {
    NetworkResult.Error(message = e.message, throwable = e)
} catch (e: Throwable) {
    NetworkResult.Error(message = e.message, throwable = e)
}
