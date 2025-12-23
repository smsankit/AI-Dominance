package com.example.logger.core.exception

sealed class AppException(message: String? = null, cause: Throwable? = null) : Exception(message, cause) {
    class Network(message: String? = null, cause: Throwable? = null) : AppException(message, cause)
    class Server(message: String? = null, cause: Throwable? = null) : AppException(message, cause)
    class Parsing(message: String? = null, cause: Throwable? = null) : AppException(message, cause)
    class Unknown(message: String? = null, cause: Throwable? = null) : AppException(message, cause)
}

