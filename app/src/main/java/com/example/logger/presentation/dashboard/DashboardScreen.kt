package com.example.logger.presentation.dashboard

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.example.logger.presentation.home.HomeRoute
import com.example.logger.ui.theme.LoggerTheme
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.logger.presentation.home.HomeViewModel

@Composable
fun DashboardScreen() {
    var selectedTab by remember { mutableStateOf(DashboardTab.Home) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                DashboardTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { padding ->
        when (selectedTab) {
            DashboardTab.Home -> {
                val vm: HomeViewModel = hiltViewModel()
                HomeRoute(viewModel = vm)
            }
            DashboardTab.Submit -> BlankTab(label = "Submit")
            DashboardTab.History -> BlankTab(label = "History")
            DashboardTab.Settings -> BlankTab(label = "Settings")
        }
    }
}

private enum class DashboardTab(val label: String, val icon: ImageVector) {
    Home("Home", Icons.Outlined.Dashboard),
    Submit("Submit", Icons.Outlined.EditNote),
    History("History", Icons.Outlined.EventNote),
    Settings("Setting", Icons.Outlined.Settings);

    companion object {
        val entries = values().toList()
    }
}

@Composable
private fun BlankTab(label: String) {
    Text(
        text = "$label",
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier
    )
}

@Preview(showBackground = true)
@Composable
private fun DashboardPreview() {
    LoggerTheme { DashboardScreen() }
}

