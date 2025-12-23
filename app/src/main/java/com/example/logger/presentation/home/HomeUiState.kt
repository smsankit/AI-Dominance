package com.example.logger.presentation.home

import com.example.logger.domain.model.Standup

data class HomeUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val date: String = "",
    val roster: List<String> = emptyList(),
    val submissions: List<Standup> = emptyList(),
    val pending: List<String> = emptyList(),
    val lastUpdated: String = ""
)
