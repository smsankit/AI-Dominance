package com.example.logger.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.logger.presentation.navigation.Destinations
import com.example.logger.presentation.home.HomeRoute
import com.example.logger.presentation.home.HomeViewModel

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Destinations.HOME
    ) {
        composable(Destinations.HOME) {
            val vm: HomeViewModel = hiltViewModel()
            HomeRoute(viewModel = vm)
        }
    }
}

