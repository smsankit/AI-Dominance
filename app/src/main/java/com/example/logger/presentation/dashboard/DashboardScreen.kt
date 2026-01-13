package com.example.logger.presentation.dashboard

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.example.logger.R
import com.example.logger.presentation.home.HomeRoute
import com.example.logger.presentation.home.HomeViewModel
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun DashboardScreen(
    refreshToken: String? = null,
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateSubmit: () -> Unit = {},
    onNavigateHistory: () -> Unit = {},
    onNavigateSettings: () -> Unit = {},
    onNavigateMissing: () -> Unit = {},
    onNavigateExport: () -> Unit = {},
    onNavigateRoster: () -> Unit = {},
    onNavigateSentiment: (pos: Int, neu: Int, neg: Int, total: Int) -> Unit = { _, _, _, _ -> },
) {
    // Body-only Home content, RootScaffold provides FAB and bottom bar
    val vm: HomeViewModel = viewModel
    HomeRoute(
        viewModel = vm,
        refreshToken = refreshToken,
        onViewMissing = onNavigateMissing,
        onSubmit = onNavigateSubmit,
        onExport = onNavigateExport,
        onNavigateExport = onNavigateExport,
        onViewRoster = onNavigateRoster,
        onNavigateToSentimentAnalysis = {
            val summary = vm.uiState.value.sentimentSummary
            onNavigateSentiment(
                summary?.positive ?: 0,
                summary?.neutral ?: 0,
                summary?.negative ?: 0,
                summary?.total ?: 0
            )
        }
    )
}
