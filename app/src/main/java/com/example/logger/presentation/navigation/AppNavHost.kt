package com.example.logger.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.logger.presentation.dashboard.DashboardScreen
import com.example.logger.presentation.home.HomeRoute
import com.example.logger.presentation.home.HomeViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.logger.presentation.splash.SplashScreen
import com.example.logger.presentation.submitstandup.SubmitStandupScreen
import com.example.logger.presentation.submitstandup.SubmitStandupViewModel

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Destinations.SPLASH
    ) {
        composable(Destinations.SPLASH) {
            SplashScreen(
                onGetStarted = { navController.navigate(Destinations.DASHBOARD) },
                onConfigureSettings = { navController.navigate(Destinations.DASHBOARD) }
            )
        }
        composable(Destinations.DASHBOARD) {
            DashboardScreen(
                onNavigateSubmit = { navController.navigate(Destinations.SUBMIT_STANDUP) },
                onNavigateHistory = { /* TODO: add history destination when available */ },
                onNavigateSettings = { /* TODO: add settings destination when available */ }
            )
        }
        composable(Destinations.HOME) {
            val vm: HomeViewModel = hiltViewModel()
            HomeRoute(viewModel = vm)
        }
        composable(Destinations.SUBMIT_STANDUP) {
            val vm: SubmitStandupViewModel = hiltViewModel()
            SubmitStandupScreen(
                viewModel = vm,
                onSubmitted = { navController.navigate(Destinations.DASHBOARD) },
                onCancel = { navController.popBackStack() }
            )
        }
    }
}
