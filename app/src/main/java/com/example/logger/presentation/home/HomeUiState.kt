package com.example.logger.presentation.home

import com.example.logger.domain.model.Standup
import com.example.logger.domain.model.StandupEntryData

data class HomeUiState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val date: String = "",
    val roster: List<String> = emptyList(),
    val submissions: List<Standup> = emptyList(),
    val pending: List<String> = emptyList(),
    val lastUpdated: String = "",
    val standupEntries: List<StandupEntryData> = emptyList(),
    val currentPage: Int = 0,
    val pageSize: Int = 20,
    val totalEntries: Int = 0,
    val totalPages: Int = 0,
    val canLoadMore: Boolean = false
)
