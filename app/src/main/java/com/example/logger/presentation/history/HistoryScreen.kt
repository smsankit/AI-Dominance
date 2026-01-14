package com.example.logger.presentation.history

import android.app.DatePickerDialog
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.logger.R
import com.example.logger.core.util.DateFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit,
    onNavigateMissing: (List<String>) -> Unit = {},
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val vm: HistoryViewModel = viewModel
    val state by vm.uiState.collectAsState()
    val context = LocalContext.current

    // Debug: Log state changes
    SideEffect {
        Log.d("HistoryScreen", "===== STATE UPDATE =====")
        Log.d("HistoryScreen", "isLoading: ${state.isLoading}")
        Log.d("HistoryScreen", "submissions count: ${state.submissions.size}")
        Log.d("HistoryScreen", "missingNames count: ${state.missingNames.size}")
        Log.d("HistoryScreen", "missingNames: ${state.missingNames}")
        Log.d("HistoryScreen", "Banner condition: !isLoading (${!state.isLoading}) && missingNames.isNotEmpty() (${state.missingNames.isNotEmpty()}) = ${!state.isLoading && state.missingNames.isNotEmpty()}")
        Log.d("HistoryScreen", "=====================")
    }

    val inputFormat = remember { DateFormatter.getInputDateFormat() }
    val displayFormat = remember { DateFormatter.getDisplayDateFormat() }
    // Calculate yesterday's date for max date validation (IST timezone)
    val yesterdayMillis = remember {
        Calendar.getInstance().apply {
            timeZone = DateFormatter.istTimeZone
            add(Calendar.DAY_OF_MONTH, -1)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }.timeInMillis
    }
    // Yesterday key for next button validation
    val yesterdayKey = remember {
        DateFormatter.getCompactDateFormat().format(
            Calendar.getInstance().apply {
                timeZone = DateFormatter.istTimeZone
                add(Calendar.DAY_OF_MONTH, -1)
            }.time
        )
    }

    fun openDatePicker() {
        val cal = Calendar.getInstance().apply { time = state.selectedDate }
        DatePickerDialog(
            context,
            { _, y, m, d ->
                val picked = Calendar.getInstance().apply {
                    set(y, m, d)
                }.time
                vm.onPickDate(picked)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).apply {
            // Set max date to yesterday (exclude today)
            datePicker.maxDate = yesterdayMillis
        }.show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.history),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { innerPadding ->

        val listState = rememberLazyListState()

        // Detect when user reaches the end of the list for infinite scrolling
        LaunchedEffect(listState, state.canLoadMore) {
            snapshotFlow {
                val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
                val totalItems = listState.layoutInfo.totalItemsCount
                lastVisibleItem?.index == totalItems - 1 && totalItems > 0
            }
            .collect { isAtEnd ->
                if (isAtEnd && state.canLoadMore && !state.isLoadingMore) {
                    vm.loadMore()
                }
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            /** -------- Header (UNCHANGED) -------- */
            item {
                Column {

                    Text(
                        text = stringResource(R.string.history_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            /** -------- TOP DATE PANEL (UPDATED ONLY) -------- */
            item {
                ElevatedCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        // Row 1: Date strip with calendar icon
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    MaterialTheme.shapes.small
                                )
                                .clickable { openDatePicker() }
                                .padding(horizontal = 12.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = inputFormat.format(state.selectedDate),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Outlined.CalendarMonth,
                                contentDescription = "Pick date"
                            )
                        }

                        // Row 2: Previous / Date / Next
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            CircleArrowButton(
                                icon = Icons.AutoMirrored.Outlined.ArrowBack,
                                enabled = true,
                                onClick = { vm.onPrevDate() }
                            )

                            Text(
                                text = displayFormat.format(state.selectedDate),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            CircleArrowButton(
                                icon = Icons.Outlined.ArrowForward,
                                enabled = DateFormatter.getCompactDateFormat().format(state.selectedDate) != yesterdayKey,
                                onClick = { vm.onNextDate() }
                            )
                        }
                    }
                }
            }

            /** -------- MISSING STANDUP CARD -------- */
            if (!state.isLoading && state.missingNames.isNotEmpty()) {
                item {
                    Log.d("HistoryScreen", "Rendering missing banner - missingNames: ${state.missingNames}")
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateMissing(state.missingNames) },
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${state.missingNames.size} ${if (state.missingNames.size == 1) "team member" else "team members"} missing standup",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Icon(
                                imageVector = Icons.Outlined.ArrowForward,
                                contentDescription = "View missing",
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            /** -------- LIST & EMPTY STATE -------- */
            val rows = state.submissions

            if (state.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (state.error != null) {
                item {
                    val errorMessage = state.error ?: "An error occurred"
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("⚠️", style = MaterialTheme.typography.displaySmall)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Error Loading History",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.W600
                            )
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (rows.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("📅", style = MaterialTheme.typography.displaySmall)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.history_empty_title),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.W600
                            )
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.history_empty_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(rows) { s ->
                    val hasBlocker =
                        (s.blockers ?: "").trim().isNotEmpty() &&
                                (s.blockers ?: "").lowercase() != "none"

                    // Time is already formatted in IST by the mapper (HH:mm format)
                    val submittedTime = s.createdAt ?: "--:--"

                    ElevatedCard(elevation = CardDefaults.elevatedCardElevation(4.dp)) {
                        Row(modifier = Modifier.height(IntrinsicSize.Min)) {

                            if (hasBlocker) {
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .fillMaxHeight()
                                        .background(MaterialTheme.colorScheme.error)
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "TM",
                                                style = MaterialTheme.typography.labelLarge
                                            )
                                        }
                                    }

                                    Spacer(Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = s.teamMember.name,
                                                style = MaterialTheme.typography.titleMedium
                                            )
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
                                            text = "Submitted at $submittedTime",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(Modifier.height(12.dp))

                                Text(
                                    text = "YESTERDAY",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(text = s.yesterdayWork ?: "N/A")

                                Spacer(Modifier.height(8.dp))

                                Text(
                                    text = "TODAY",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(text = s.todayPlan ?: "N/A")

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
        }
    }
}

@Composable
private fun CircleArrowButton(
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        tonalElevation = 2.dp,
        color = if (enabled)
            MaterialTheme.colorScheme.surface
        else
            MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .size(40.dp)
            .clickable(enabled = enabled, onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled)
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
