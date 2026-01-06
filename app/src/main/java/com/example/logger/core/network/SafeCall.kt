package com.example.logger.core.network

import com.example.logger.core.exception.AppException
import retrofit2.HttpException
import java.io.IOException

suspend inline fun <T> safeApiCall(crossinline block: suspend () -> T): T {
    try {
        return block()
    } catch (e: IOException) {
        throw AppException.Network(message = e.message, cause = e)
    } catch (e: HttpException) {
        throw AppException.Server(message = e.message(), cause = e)
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
