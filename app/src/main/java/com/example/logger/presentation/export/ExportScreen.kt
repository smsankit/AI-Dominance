package com.example.logger.presentation.export

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.logger.R
import com.example.logger.domain.model.StandupEntryData
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    modifier: Modifier = Modifier,
    viewModel: ExportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showDatePicker by remember { mutableStateOf(false) }

    // Convert StandupEntryData to ExportStandupUiModel
    val standups = remember(uiState.standupEntries) {
        uiState.standupEntries.map { entry ->
            ExportStandupUiModel(
                name = "Team Member #${entry.teamMemberId}",
                time = try {
                    val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    val date = dateTimeFormat.parse(entry.createdAt)
                    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    date?.let { timeFormat.format(it) } ?: entry.createdAt
                } catch (e: Exception) {
                    entry.createdAt.substringAfter("T").substringBefore(".")
                },
                yesterday = entry.yesterdayWork,
                today = entry.todayPlan,
                blockers = entry.blockers,
                editedAt = null
            )
        }
    }

    Column(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Toolbar
        TopAppBar(
            title = { Text(stringResource(R.string.export_standups_title)) },
            navigationIcon = {
                IconButton(onClick = { /* TODO: handle back */ }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF6200EA),
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White
            )
        )
        Spacer(Modifier.height(8.dp))
        Column(Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = stringResource(R.string.export_standups_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF666666),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.export_select_date),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = uiState.selectedDate,
                        onValueChange = {}, // Disable manual editing
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Outlined.CalendarToday, contentDescription = "Select date")
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFE7E0EC),
                            unfocusedContainerColor = Color(0xFFE7E0EC),
                            focusedIndicatorColor = Color(0xFF6200EA),
                            unfocusedIndicatorColor = Color(0xFFE7E0EC)
                        )
                    )
                    if (showDatePicker) {
                        val dialog = remember { mutableStateOf<DatePickerDialog?>(null) }
                        DisposableEffect(showDatePicker) {
                            if (showDatePicker) {
                                val cal = Calendar.getInstance().apply {
                                    // Try to parse selectedDate simply: yyyy-MM-dd
                                    val parts = uiState.selectedDate.split("-")
                                    if (parts.size == 3) {
                                        set(Calendar.YEAR, parts[0].toIntOrNull() ?: this[Calendar.YEAR])
                                        set(Calendar.MONTH, (parts[1].toIntOrNull()?.minus(1)) ?: this[Calendar.MONTH])
                                        set(Calendar.DAY_OF_MONTH, parts[2].toIntOrNull() ?: this[Calendar.DAY_OF_MONTH])
                                    }
                                }
                                dialog.value = DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        val newDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
                                        viewModel.onDateChange(newDate)
                                        showDatePicker = false
                                    },
                                    cal[Calendar.YEAR],
                                    cal[Calendar.MONTH],
                                    cal[Calendar.DAY_OF_MONTH]
                                ).apply {
                                    datePicker.maxDate = System.currentTimeMillis()
                                    setOnCancelListener { showDatePicker = false }
                                    show()
                                }
                            }
                            onDispose {
                                dialog.value?.dismiss()
                                dialog.value = null
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.export_file_label, "standup-${uiState.selectedDate}.md"),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF666666)
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { /* TODO: Implement export functionality */ },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = standups.isNotEmpty() && !uiState.isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EA), contentColor = Color.White)
                    ) {
                        Icon(Icons.Outlined.Download, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.export_button))
                    }
                }
            }

            // Loading state
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.error != null) {
                // Error state
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("⚠️", style = MaterialTheme.typography.headlineLarge)
                    Spacer(Modifier.height(8.dp))
                    Text("Error Loading Data", style = MaterialTheme.typography.titleMedium, color = Color(0xFF666666))
                    Text(uiState.error ?: "Unknown error", style = MaterialTheme.typography.bodySmall, color = Color(0xFF999999))
                }
            } else if (standups.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("📄", style = MaterialTheme.typography.headlineLarge)
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.export_no_data), style = MaterialTheme.typography.titleMedium, color = Color(0xFF666666))
                    Text(stringResource(R.string.export_no_data_sub), style = MaterialTheme.typography.bodySmall, color = Color(0xFF999999))
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(R.string.export_preview), fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text(stringResource(R.string.export_count, standups.size), color = Color(0xFF666666))
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = standupsToMarkdown(uiState.selectedDate, standups),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF333333),
                            modifier = Modifier.background(Color(0xFFF5F5F5)).padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}

data class ExportStandupUiModel(
    val name: String,
    val time: String,
    val yesterday: String,
    val today: String,
    val blockers: String?,
    val editedAt: String?
)

fun standupsToMarkdown(date: String, standups: List<ExportStandupUiModel>): String {
    if (standups.isEmpty()) return "# Team Standups - $date\n\nNo standups for this date.\n"
    return buildString {
        appendLine("# Team Standups - $date\n")
        for (s in standups) {
            appendLine("## ${s.name} (${s.time})")
            appendLine("- Yesterday: ${s.yesterday}")
            appendLine("- Today: ${s.today}")
            appendLine("- Blockers: ${s.blockers?.ifBlank { "None" } ?: "None"}")
            if (!s.editedAt.isNullOrBlank()) appendLine("- Edited: ${s.editedAt}")
            appendLine()
        }
    }
}

@Preview(showBackground = true, name = "Export Standup")
@Composable
fun PreviewExportScreen() {
    // Preview without ViewModel - just show the UI structure
    MaterialTheme {
        Column(Modifier.fillMaxSize()) {
            Text("Export Screen Preview")
        }
    }
}
