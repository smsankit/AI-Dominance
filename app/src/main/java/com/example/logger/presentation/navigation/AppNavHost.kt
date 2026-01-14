package com.example.logger.presentation.navigation

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
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
import com.example.logger.presentation.missing.MissingScreen
import com.example.logger.presentation.sentiment.SentimentAnalysisScreen

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
        composable(
            route = Destinations.DASHBOARD,
            arguments = listOf(
                navArgument(Destinations.ARG_REFRESH) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val refreshToken = backStackEntry.arguments?.getString(Destinations.ARG_REFRESH)
            RootScaffold(navController = navController) { padding ->
                Box(Modifier.padding(padding)) {
                    DashboardScreen(
                        refreshToken = refreshToken,
                        onNavigateSubmit = {
                            navController.navigate(Destinations.submitStandup()) {
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
                        },
                        onNavigateExport = { navController.navigate(Destinations.EXPORT) },
                        onNavigateRoster = { navController.navigate(Destinations.ROSTER) },
                        onNavigateSentiment = { pos, neu, neg, total ->
                            navController.navigate(Destinations.sentimentRoute(pos, neu, neg, total))
                        }
                    )
                }
            }
        }
        composable(
            route = Destinations.HOME,
            arguments = listOf(
                navArgument(Destinations.ARG_REFRESH) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val refreshToken = backStackEntry.arguments?.getString(Destinations.ARG_REFRESH)
            RootScaffold(navController = navController) { padding ->
                Box(Modifier.padding(padding)) {
                    val vm: HomeViewModel = hiltViewModel()
                    HomeRoute(
                        viewModel = vm,
                        refreshToken = refreshToken,
                        onSubmit = {
                            navController.navigate(Destinations.submitStandup()) {
                                popUpTo(Destinations.HOME) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onNavigateExport = { navController.navigate(Destinations.EXPORT) },
                        onViewRoster = { navController.navigate(Destinations.ROSTER) },
                        onNavigateToSentimentAnalysis = {
                            val summary = vm.uiState.value.sentimentSummary
                            val route = Destinations.sentimentRoute(
                                pos = summary?.positive ?: 0,
                                neu = summary?.neutral ?: 0,
                                neg = summary?.negative ?: 0,
                                total = summary?.total ?: 0
                            )
                            navController.navigate(route)
                        }
                    )
                }
            }
        }
        composable(
            route = Destinations.SUBMIT_STANDUP,
            arguments = listOf(
                navArgument(Destinations.ARG_MEMBER_NAME) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val memberName = backStackEntry.arguments?.getString(Destinations.ARG_MEMBER_NAME)
            RootScaffold(navController = navController) { padding ->
                Box(Modifier.padding(padding)) {
                    val vm: SubmitStandupViewModel = hiltViewModel()

                    // Set the pre-selected member name if provided
                    if (memberName != null) {
                        vm.setPreSelectedMember(memberName)
                    }

                    SubmitStandupScreen(
                        viewModel = vm,
                        onSubmitted = { ts ->
                            Log.e("AppNavHost", "Navigating to SubmitConfirm with ts=$ts")
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
            // Submit success screen should not show bottom bar or back navigation
            // Render without RootScaffold to hide chrome
            val ts = backStackEntry.arguments?.getString(Destinations.ARG_TS) ?: ""
            SubmitConfirmScreen(
                timestamp = ts,
                onGoDashboard = {
                    navController.navigate(Destinations.dashboard(refresh = true)) {
                        popUpTo(Destinations.DASHBOARD_BASE) { inclusive = false }
                        launchSingleTop = true
                        restoreState = false
                    }
                },
                onGoHistory = {
                    navController.navigate(Destinations.HISTORY) {
                        popUpTo(Destinations.DASHBOARD_BASE) { inclusive = false }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
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
        composable(Destinations.ROSTER) { backStackEntry ->
            RootScaffold(navController = navController) { padding ->
                Box(Modifier.padding(padding)) {
                    // Get ViewModel from DASHBOARD backstack entry to avoid unnecessary API calls
                    val dashboardEntry = remember(backStackEntry) {
                        navController.getBackStackEntry(Destinations.DASHBOARD)
                    }
                    val homeVm: HomeViewModel = hiltViewModel(dashboardEntry)
                    val state = homeVm.uiState.collectAsState()
                    RosterScreen(
                        members = state.value.roster,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
        // Missing standups screen route
        composable(Destinations.MISSING) { backStackEntry ->
            // Missing should not show FAB or bottom bar according to requirements.
            // Render without RootScaffold to hide chrome.
            // Get ViewModel from DASHBOARD backstack entry to avoid unnecessary API calls
            val dashboardEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Destinations.DASHBOARD_BASE)
            }
            val homeVm: HomeViewModel = hiltViewModel(dashboardEntry)
            val state = homeVm.uiState.collectAsState()

            // Load ALL standups to accurately identify which specific members haven't submitted
            // Using fetchAllStandups=true to get complete list based on totalElements
            LaunchedEffect(Unit) {
                homeVm.load(resetList = true, fetchAllStandups = true)
            }

            // Use actual pending members list from state which is calculated based on teamMemberId
            MissingScreen(
                pendingMembers = state.value.pending,
                onNavigateBack = { navController.popBackStack() },
                onSubmitStandup = { memberName ->
                    navController.navigate(Destinations.submitStandup(memberName)) {
                        popUpTo(Destinations.DASHBOARD_BASE) { saveState = true }
                        launchSingleTop = true
                        restoreState = false
                    }
                }
            )
        }
        composable(Destinations.EXPORT) {
            // Export should not show bottom bar or FAB. Render without RootScaffold.
            com.example.logger.presentation.export.ExportScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        // Sentiment analysis screen
        composable(
            route = Destinations.SENTIMENT,
            arguments = listOf(
                navArgument(Destinations.ARG_POS) { type = NavType.IntType },
                navArgument(Destinations.ARG_NEU) { type = NavType.IntType },
                navArgument(Destinations.ARG_NEG) { type = NavType.IntType },
                navArgument(Destinations.ARG_TOTAL) { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val pos = backStackEntry.arguments?.getInt(Destinations.ARG_POS) ?: 0
            val neu = backStackEntry.arguments?.getInt(Destinations.ARG_NEU) ?: 0
            val neg = backStackEntry.arguments?.getInt(Destinations.ARG_NEG) ?: 0
            val total = backStackEntry.arguments?.getInt(Destinations.ARG_TOTAL) ?: 0
            // Render without RootScaffold to hide chrome (bottom bar), like Missing screen
            SentimentAnalysisScreen(
                pos = pos,
                neu = neu,
                neg = neg,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
