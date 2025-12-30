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
    onNavigateSubmit: () -> Unit = {},
    onNavigateHistory: () -> Unit = {},
    onNavigateSettings: () -> Unit = {},
    onNavigateMissing: () -> Unit = {},
    onNavigateExport: () -> Unit = {},
) {
    // Body-only Home content, RootScaffold provides FAB and bottom bar
    val vm: HomeViewModel = hiltViewModel()
    HomeRoute(
        viewModel = vm,
        onViewMissing = onNavigateMissing,
        onSubmit = onNavigateSubmit,
        onExport = onNavigateExport,
        onNavigateExport = onNavigateExport
    )
}
