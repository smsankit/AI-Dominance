package com.example.logger.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.logger.presentation.dashboard.DashboardScreen
import com.example.logger.presentation.home.HomeRoute
import com.example.logger.presentation.home.HomeViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.logger.presentation.splash.SplashScreen
import com.example.logger.presentation.submitstandup.SubmitConfirmScreen
import com.example.logger.presentation.submitstandup.SubmitStandupScreen
import com.example.logger.presentation.submitstandup.SubmitStandupViewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.logger.presentation.history.HistoryScreen
import com.example.logger.presentation.settings.SettingsScreen
import com.example.logger.presentation.roster.RosterScreen
import androidx.compose.runtime.collectAsState
import com.example.logger.presentation.missing.MissingScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    // Render Splash outside of RootScaffold to prevent chrome flash
    NavHost(
        navController = navController,
        startDestination = Destinations.SPLASH
    ) {
        composable(Destinations.SPLASH) {
            SplashScreen(
                onGetStarted = {
                    navController.navigate(Destinations.DASHBOARD) {
                        popUpTo(Destinations.SPLASH) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onConfigureSettings = {
                    navController.navigate(Destinations.DASHBOARD) {
                        popUpTo(Destinations.SPLASH) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        // Wrap the rest inside RootScaffold
        composable(Destinations.DASHBOARD) {
            RootScaffold(navController = navController) { padding ->
                Box(Modifier.padding(padding)) {
                    DashboardScreen(
                        onNavigateSubmit = {
                            navController.navigate(Destinations.SUBMIT_STANDUP) {
                                popUpTo(Destinations.DASHBOARD) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onNavigateHistory = {
                            navController.navigate(Destinations.HISTORY) {
                                popUpTo(Destinations.DASHBOARD) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onNavigateSettings = { /* TODO: add settings destination when available */ },
                        onNavigateMissing = {
                            navController.navigate(Destinations.MISSING) {
                                popUpTo(Destinations.DASHBOARD) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
        composable(Destinations.HOME) {
            RootScaffold(navController = navController) { padding ->
                Box(Modifier.padding(padding)) {
                    val vm: HomeViewModel = hiltViewModel()
                    HomeRoute(viewModel = vm)
                }
            }
        }
        composable(Destinations.SUBMIT_STANDUP) {
            RootScaffold(navController = navController) { padding ->
                Box(Modifier.padding(padding)) {
                    val vm: SubmitStandupViewModel = hiltViewModel()
                    SubmitStandupScreen(
                        viewModel = vm,
                        onSubmitted = { ts ->
                            navController.navigate(Destinations.submitConfirm(ts)) {
                                // Remove Submit from back stack so back from Success doesn’t return to Submit
                                popUpTo(Destinations.DASHBOARD) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onCancel = { navController.popBackStack() },
                        onNavigateHome = { navController.navigate(Destinations.DASHBOARD) },
                        onNavigateSubmit = { /* stay on current submit screen */ },
                        onNavigateHistory = { navController.navigate(Destinations.HISTORY) },
                        onNavigateSettings = { /* TODO: add settings destination when available */ }
                    )
                }
            }
        }
        composable(
            route = Destinations.SUBMIT_CONFIRM,
            arguments = listOf(navArgument(Destinations.ARG_TS) { type = NavType.StringType })
        ) { backStackEntry ->
            RootScaffold(navController = navController) { padding ->
                val ts = backStackEntry.arguments?.getString(Destinations.ARG_TS) ?: ""
                SubmitConfirmScreen(
                    timestamp = ts,
                    onGoDashboard = {
                        navController.navigate(Destinations.DASHBOARD) {
                            // Clear Success from back stack by popping to Dashboard
                            popUpTo(Destinations.DASHBOARD) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onGoHistory = {
                        navController.navigate(Destinations.HISTORY) {
                            popUpTo(Destinations.DASHBOARD) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
        composable(Destinations.HISTORY) {
            RootScaffold(navController = navController) { padding ->
                Box(Modifier.padding(padding)) {
                    HistoryScreen(onNavigateBack = { navController.popBackStack() })
                }
            }
        }
        composable(Destinations.SETTINGS) {
            RootScaffold(navController = navController) { padding ->
                Box(Modifier.padding(padding)) {
                    SettingsScreen(onNavigateRoster = { navController.navigate(Destinations.ROSTER) })
                }
            }
        }
        composable(Destinations.ROSTER) {
            RootScaffold(navController = navController) { padding ->
                Box(Modifier.padding(padding)) {
                    val homeVm: HomeViewModel = hiltViewModel()
                    val state = homeVm.uiState.collectAsState()
                    RosterScreen(
                        members = state.value.roster,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
        // Missing standups screen route
        composable(Destinations.MISSING) {
            // Missing should not show FAB or bottom bar according to requirements.
            // Render without RootScaffold to hide chrome.
            val homeVm: HomeViewModel = hiltViewModel()
            val state = homeVm.uiState.collectAsState()
            MissingScreen(
                roster = state.value.roster,
                submittedNames = state.value.submissions.map { it.name },
                onNavigateBack = { navController.popBackStack() },
                onSendReminder = { /* TODO: hook reminder send; for now no-op */ }
            )
        }
    }
}
