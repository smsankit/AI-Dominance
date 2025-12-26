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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.graphics.Shape
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun DashboardScreen(
    onNavigateSubmit: () -> Unit = {},
    onNavigateHistory: () -> Unit = {},
    onNavigateSettings: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(DashboardTab.Home) }

    Scaffold(
        floatingActionButton = {
            // FAB styled like wireframe: purple, white icon, shadow, rounded 16dp
            FloatingActionButton(
                onClick = { onNavigateSubmit() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Outlined.EditNote, contentDescription = stringResource(R.string.submit))
            }
        },
        bottomBar = {
            NavigationBar {
                DashboardTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = {
                            selectedTab = tab
                            when (tab) {
                                DashboardTab.Submit -> onNavigateSubmit()
                                DashboardTab.History -> onNavigateHistory()
                                DashboardTab.Settings -> onNavigateSettings()
                                else -> {}
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.labelResId?.let { stringResource(it) } ?: tab.label) },
                        label = { Text(tab.labelResId?.let { stringResource(it) } ?: tab.label) }
                    )
                }
            }
        }
    ) { _ ->
        when (selectedTab) {
            DashboardTab.Home -> {
                val vm: HomeViewModel = hiltViewModel()
                HomeRoute(viewModel = vm)
            }
            // Other tabs navigate away, show minimal content if still on this screen
            DashboardTab.Submit -> BlankTab(label = stringResource(R.string.submit))
            DashboardTab.History -> BlankTab(label = stringResource(R.string.history))
            DashboardTab.Settings -> BlankTab(label = stringResource(R.string.settings))
        }
    }
}

private enum class DashboardTab(val label: String, val icon: ImageVector, val labelResId: Int? = null) {
    Home(label = "Home", icon = Icons.Outlined.Dashboard, labelResId = R.string.home),
    Submit(label = "Submit", icon = Icons.Outlined.EditNote, labelResId = R.string.submit),
    History(label = "History", icon = Icons.Outlined.EventNote, labelResId = R.string.history),
    Settings(label = "Settings", icon = Icons.Outlined.Settings, labelResId = R.string.settings);

    companion object { val entries = values().toList() }
}

@Composable
private fun BlankTab(label: String) {
    Text(text = label, style = MaterialTheme.typography.titleLarge)
}
