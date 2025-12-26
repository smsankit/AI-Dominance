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
        composable(Destinations.DASHBOARD) { DashboardScreen() }
        composable(Destinations.HOME) {
            val vm: HomeViewModel = hiltViewModel()
            HomeRoute(viewModel = vm)
        }
    }
}
