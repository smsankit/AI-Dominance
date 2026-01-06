package com.example.logger.domain.model

data class PaginatedStandupEntriesData(
    val items: List<StandupEntryData>,
    val meta: PaginationMetaData
)

data class PaginationMetaData(
    val page: Int,
    val size: Int,
    val totalElements: Int,
    val totalPages: Int
)

