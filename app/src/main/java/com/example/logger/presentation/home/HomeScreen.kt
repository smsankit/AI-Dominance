package com.example.logger.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.PersonOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.logger.domain.model.Standup
import com.example.logger.presentation.home.components.TeamMoodCard
import com.example.logger.ui.theme.LoggerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeRoute(
    viewModel: HomeViewModel,
    refreshToken: String? = null,
    onViewMissing: () -> Unit = {},
    onSubmit: () -> Unit = {},
    onExport: () -> Unit = {},
    onNavigateExport: () -> Unit = {},
    onViewRoster: () -> Unit = {},
    onNavigateToSentimentAnalysis: (() -> Unit)? = null,
) {
    val state by viewModel.uiState.collectAsState()

    // Refresh data when refresh token changes (triggered by navigation with timestamp)
    // Using refreshToken instead of boolean ensures this triggers every time we want to refresh
    // On first load, ViewModel's init block handles the data loading via fetchTeamMembers() -> load()
    LaunchedEffect(refreshToken) {
        if (refreshToken != null) {
            viewModel.load()
        }
    }


    Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { Text("Dashboard") },
//                actions = {
//                    IconButton(onClick = { /* TODO: navigate to settings */ }) {
//                        Icon(Icons.Outlined.Settings, contentDescription = "Settings")
//                    }
//                },
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
                onExport = onNavigateExport,
                onLoadMore = { viewModel.loadMore() },
                onViewRoster = onViewRoster,
                onNavigateToSentimentAnalysis = onNavigateToSentimentAnalysis
            )
        }
    }
}

@Composable
fun HomeScreen(
    state: HomeUiState,
    onRetry: () -> Unit,
    onViewMissing: () -> Unit,
    onSubmit: () -> Unit,
    onExport: () -> Unit,
    onLoadMore: () -> Unit,
    onViewRoster: () -> Unit,
    onNavigateToSentimentAnalysis: (() -> Unit)? = null,
) {
    when {
        state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier.semantics { contentDescription = "Progress indicator" })
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
        else -> {
            val listState = rememberLazyListState()

            // Detect when user reaches the end of the list
            // Use canLoadMore as a key to restart effect when pagination state changes
            LaunchedEffect(listState, state.canLoadMore) {
                snapshotFlow {
                    val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
                    val totalItems = listState.layoutInfo.totalItemsCount
                    lastVisibleItem?.index == totalItems - 1 && totalItems > 0
                }
                .collect { isAtEnd ->
                    if (isAtEnd && state.canLoadMore && !state.isLoadingMore) {
                        onLoadMore()
                    }
                }
            }

            Box(Modifier.fillMaxSize()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        HeroStatCard(
                            submitted = state.totalEntries, // Use totalEntries from API, not submissions.size
                            total = state.roster.size,
                            lastUpdated = state.lastUpdated
                        )
                    }

                    item {
                        TeamMoodCard(
                            sentimentSummary = state.sentimentSummary,
                            isLoading = state.isSentimentLoading,
                            error = state.sentimentError,
                            onNavigateToSentimentAnalysis = onNavigateToSentimentAnalysis
                        )
                    }

                    if (state.pendingCount > 0) {
                        item {
                            PendingBar(count = state.pendingCount, onViewMissing = onViewMissing)
                        }
                    }

                    if (state.submissions.isEmpty()) {
                        item {
                            EmptyState(onSubmit = onSubmit, onViewRoster = onViewRoster)
                        }
                    } else {
                        item {
                            RowHeader(onExport = onExport)
                        }

                        items(state.submissions) { s ->
                            SubmissionCard(s)
                        }
                    }

                    // Loading indicator at the bottom when loading more
                    if (state.isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }

                    item { Spacer(Modifier.height(96.dp)) }
                }
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
    val hasBlocker = (s.blockers ?: "").trim().isNotEmpty() &&
                     (s.blockers ?: "").lowercase() != "none"

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        androidx.compose.foundation.layout.Row(modifier = Modifier.height(androidx.compose.foundation.layout.IntrinsicSize.Min)) {
            // Red vertical bar for blockers
            if (hasBlocker) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.error)
                )
            }

            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Column(Modifier.fillMaxWidth()) {
                    RowHeaderAvatar(name = s.name, time = s.time, hasBlocker = hasBlocker)
                }
                Spacer(Modifier.height(12.dp))
                LabeledSection(label = "YESTERDAY", text = s.yesterday)
                Spacer(Modifier.height(8.dp))
                LabeledSection(label = "TODAY", text = s.today)
                if (hasBlocker) {
                    Spacer(Modifier.height(8.dp))
                    ElevatedCard(
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "⚠ BLOCKERS",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = s.blockers ?: "",
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RowHeaderAvatar(name: String, time: String, hasBlocker: Boolean = false) {
    androidx.compose.foundation.layout.Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        val initials = name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("")

        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
                Text(name, style = MaterialTheme.typography.titleMedium)

                if (hasBlocker) {
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(
                            text = "⚠ HAS BLOCKER",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(
                                horizontal = 8.dp,
                                vertical = 2.dp
                            )
                        )
                    }
                }
            }

            Text(
                "Submitted at $time",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
private fun EmptyState(onSubmit: () -> Unit, onViewRoster: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 64.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "📭",
            style = MaterialTheme.typography.displayLarge,
            fontSize = 80.sp



        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = "No Standups Yet",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Start by submitting your first daily standup or add team members.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF666666),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(32.dp))
        Column(
            modifier = Modifier.fillMaxWidth(0.85f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Submit First Standup")
            }

        }
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
            onLoadMore = {},
            onViewRoster = {},
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
            onLoadMore = {},
            onViewRoster = {},
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
            onLoadMore = {},
            onViewRoster = {},
        )
    }
}
