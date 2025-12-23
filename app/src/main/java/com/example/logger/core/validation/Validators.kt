package com.example.logger.core.validation

interface Validator<T> {
    fun validate(value: T): Boolean
}

object NonEmptyStringValidator : Validator<String> {
    override fun validate(value: String): Boolean = value.isNotBlank()
}

