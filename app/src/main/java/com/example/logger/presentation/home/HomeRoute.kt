package com.example.logger.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.PersonOff
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.logger.domain.model.Standup
import com.example.logger.ui.theme.LoggerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeRoute(
    viewModel: HomeViewModel,
    onViewMissing: () -> Unit = {},
    onSubmit: () -> Unit = {},
    onExport: () -> Unit = {},
    onNavigateExport: () -> Unit = {}, // Add this line
) {
    val state by viewModel.uiState.collectAsState()
    Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { Text("Dashboard") },
                actions = {
                    IconButton(onClick = { /* TODO: navigate to settings */ }) {
                        Icon(Icons.Outlined.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6200EA),
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            HomeScreen(
                state = state,
                onRetry = { viewModel.load() },
                onViewMissing = onViewMissing,
                onSubmit = onSubmit,
                onExport = onNavigateExport, // Use the new navigation lambda
            )
        }
    }
}

@Composable
private fun HomeScreen(
    state: HomeUiState,
    onRetry: () -> Unit,
    onViewMissing: () -> Unit,
    onSubmit: () -> Unit,
    onExport: () -> Unit,
) {
    when {
        state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        state.error != null -> Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = state.error,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
            Button(onClick = onRetry) { Text("Retry") }
        }
        else -> Box(Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    HeroStatCard(
                        submitted = state.submissions.size,
                        total = state.roster.size,
                        lastUpdated = state.lastUpdated
                    )
                }

                if (state.pending.isNotEmpty()) {
                    item {
                        PendingBar(count = state.pending.size, onViewMissing = onViewMissing)
                    }
                }

                item {
                    RowHeader(onExport = onExport)
                }

                if (state.submissions.isEmpty()) {
                    item {
                        EmptyState()
                    }
                } else {
                    items(state.submissions) { s ->
                        SubmissionCard(s)
                    }
                }

                item { Spacer(Modifier.height(96.dp)) }
            }
        }
    }
}

@Composable
private fun HeroStatCard(submitted: Int, total: Int, lastUpdated: String) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF6200EA), Color(0xFF7C3AED))
                    )
                )
                .padding(24.dp)
        ) {
            Column(Modifier.fillMaxWidth()) {
                Text(
                    text = "$submitted/$total",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = "Standups submitted today",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Last updated $lastUpdated",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun PendingBar(count: Int, onViewMissing: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            androidx.compose.foundation.layout.Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Outlined.PersonOff,
                    contentDescription = null,
                    tint = Color(0xFFE65100)
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "$count team member${if (count == 1) "" else "s"} missing",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFE65100)
                    )
                    Text(
                        text = "Haven't submitted standup yet",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF666666)
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onViewMissing,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE65100),
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Outlined.NotificationsActive, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("View Missing Standups")
            }
        }
    }
}

@Composable
private fun RowHeader(onExport: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp)
    ) {
        Text(
            text = "Today's Standups",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.align(Alignment.CenterStart)
        )
        Button(onClick = onExport, modifier = Modifier.align(Alignment.CenterEnd)) {
            Icon(Icons.Outlined.Download, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Export")
        }
    }
}

@Composable
private fun SubmissionCard(s: Standup) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Column(Modifier.fillMaxWidth()) {
                RowHeaderAvatar(name = s.name, time = s.time)
            }
            Spacer(Modifier.height(8.dp))
            LabeledSection(label = "YESTERDAY", text = s.yesterday)
            Spacer(Modifier.height(8.dp))
            LabeledSection(label = "TODAY", text = s.today)
            if (!s.blockers.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                LabeledSection(label = "BLOCKERS", text = s.blockers ?: "", highlight = true)
            }
        }
    }
}

@Composable
private fun RowHeaderAvatar(name: String, time: String) {
    androidx.compose.foundation.layout.Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        val initials = name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("")
        Box(
            modifier = Modifier
                .clip(MaterialTheme.shapes.small)
                .background(Color(0xFFEDE7F6))
                .height(40.dp)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(initials, color = Color(0xFF5E35B1), fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.titleMedium)
            Text("Submitted at $time", style = MaterialTheme.typography.bodySmall, color = Color(0xFF666666))
        }
    }
}

@Composable
private fun LabeledSection(label: String, text: String, highlight: Boolean = false) {
    Text(label, style = MaterialTheme.typography.labelSmall, color = Color(0xFF666666))
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = if (highlight) Color(0xFFD32F2F) else MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("📭", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(8.dp))
        Text("No standups yet", style = MaterialTheme.typography.titleMedium, color = Color(0xFF666666))
        Text("Be the first to submit today", style = MaterialTheme.typography.bodySmall, color = Color(0xFF999999))
    }
}

// Previews
@Preview(showBackground = true, name = "Dashboard - Loading")
@Composable
private fun PreviewDashboardLoading() {
    LoggerTheme {
        HomeScreen(
            state = HomeUiState(isLoading = true),
            onRetry = {},
            onViewMissing = {},
            onSubmit = {},
            onExport = {},
        )
    }
}

@Preview(showBackground = true, name = "Dashboard - Error")
@Composable
private fun PreviewDashboardError() {
    LoggerTheme {
        HomeScreen(
            state = HomeUiState(isLoading = false, error = "Network error. Please try again."),
            onRetry = {},
            onViewMissing = {},
            onSubmit = {},
            onExport = {},
        )
    }
}

@Preview(showBackground = true, name = "Dashboard - Data")
@Composable
private fun PreviewDashboardData() {
    LoggerTheme {
        val submissions = listOf(
            Standup(id = "s1", name = "Alex Johnson", yesterday = "Reviewed PRs", today = "Finalize API spec", blockers = null, time = "09:10", editedAt = null),
            Standup(id = "s2", name = "Priya Verma", yesterday = "Auth flow fixes", today = "Add MFA", blockers = "Waiting on UX", time = "09:25", editedAt = null),
            Standup(id = "me", name = "You", yesterday = "Feature A tests", today = "Implement Feature B", blockers = "", time = "09:55", editedAt = "10:30"),
        )
        val roster = listOf("Alex Johnson", "Priya Verma", "Miguel Santos", "Sarah Kim", "You")
        val pending = roster.filterNot { name -> submissions.any { it.name == name } }
        HomeScreen(
            state = HomeUiState(
                isLoading = false,
                error = null,
                date = "2025-12-22",
                roster = roster,
                submissions = submissions,
                pending = pending,
                lastUpdated = "10:45"
            ),
            onRetry = {},
            onViewMissing = {},
            onSubmit = {},
            onExport = {},
        )
    }
}
