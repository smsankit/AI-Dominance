package com.example.logger.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.logger.R

private enum class RootTab(val route: String, val labelRes: Int, val icon: ImageVector) {
    Home(Destinations.DASHBOARD_BASE, R.string.home, Icons.Outlined.Dashboard),
    Submit(Destinations.SUBMIT_STANDUP_BASE, R.string.submit, Icons.Outlined.EditNote),
    History(Destinations.HISTORY, R.string.history, Icons.AutoMirrored.Outlined.EventNote),
    Settings("settings", R.string.settings, Icons.Outlined.Settings)
}

private fun mapRouteToTab(route: String?): RootTab = when {
    route == Destinations.DASHBOARD_BASE || route?.startsWith(Destinations.DASHBOARD_BASE) == true || route == Destinations.HOME -> RootTab.Home
    route == Destinations.SUBMIT_STANDUP_BASE || route?.startsWith(Destinations.SUBMIT_STANDUP_BASE) == true || (route?.startsWith(Destinations.SUBMIT_CONFIRM) == true) -> RootTab.Submit
    route == Destinations.HISTORY -> RootTab.History
    route == Destinations.SETTINGS -> RootTab.Settings
    else -> RootTab.Home
}

@Composable
fun RootScaffold(
    navController: NavHostController,
    content: @Composable (PaddingValues) -> Unit
) {
    val tabs = remember { RootTab.entries }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val selectedTab = mapRouteToTab(currentRoute)
    val isChromeVisible = currentRoute != Destinations.SPLASH && currentRoute != Destinations.ROSTER && currentRoute != Destinations.MISSING

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        floatingActionButton = {
            if (isChromeVisible && selectedTab == RootTab.Home) {
                FloatingActionButton(
                    onClick = {
                        navController.navigate(Destinations.submitStandup()) {
                            popUpTo(Destinations.DASHBOARD_BASE) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.Outlined.EditNote, contentDescription = stringResource(R.string.submit))
                }
            }
        },
        bottomBar = {
            if (isChromeVisible) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = {
                                when (tab) {
                                    RootTab.Home -> {
                                        val current = navController.currentBackStackEntry?.destination?.route
                                        // Always navigate to dashboard with refresh=true when clicking the tab
                                        // This ensures data is fresh after standup submission or coming from History
                                        navController.navigate(Destinations.dashboard(refresh = true)) {
                                            popUpTo(Destinations.DASHBOARD_BASE) { inclusive = true }
                                            launchSingleTop = true
                                            restoreState = false // Don't restore state to ensure refresh happens
                                        }
                                    }
                                    RootTab.Submit -> {
                                        // Avoid restoreState to guarantee fresh screen after coming from History or Success
                                        navController.navigate(Destinations.submitStandup()) {
                                            popUpTo(Destinations.DASHBOARD_BASE) { saveState = false }
                                            launchSingleTop = true
                                            restoreState = false
                                        }
                                    }
                                    RootTab.History -> {
                                        navController.navigate(Destinations.HISTORY) {
                                            popUpTo(Destinations.DASHBOARD_BASE) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                    RootTab.Settings -> {
                                        navController.navigate(Destinations.SETTINGS) {
                                            popUpTo(Destinations.DASHBOARD_BASE) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = stringResource(tab.labelRes)) },
                            label = { Text(stringResource(tab.labelRes)) }
                        )
                    }
                }
            }
        }
    ) { padding -> content(padding) }
}
