package com.example.logger.presentation.export

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.logger.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import android.app.DatePickerDialog
import androidx.compose.ui.platform.LocalContext
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    date: String = LocalDate.now().format(DateTimeFormatter.ISO_DATE),
    standups: List<ExportStandupUiModel> = emptyList(),
    onDateChange: (String) -> Unit = {},
    onExport: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val calendar = remember(date) {
        val localDate = LocalDate.parse(date)
        Calendar.getInstance().apply {
            set(Calendar.YEAR, localDate.year)
            set(Calendar.MONTH, localDate.monthValue - 1)
            set(Calendar.DAY_OF_MONTH, localDate.dayOfMonth)
        }
    }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(date) }
    OutlinedTextField(
        value = selectedDate,
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
    if (selectedDate != date) {
        LaunchedEffect(selectedDate) {
            onDateChange(selectedDate)
        }
    }
    if (showDatePicker) {
        val dialog = remember { mutableStateOf<DatePickerDialog?>(null) }
        DisposableEffect(showDatePicker) {
            if (showDatePicker) {
                dialog.value = DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        val picked = LocalDate.of(year, month + 1, dayOfMonth)
                        selectedDate = picked.format(DateTimeFormatter.ISO_DATE)
                        showDatePicker = false
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
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
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                        value = selectedDate,
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
                    if (selectedDate != date) {
                        LaunchedEffect(selectedDate) {
                            onDateChange(selectedDate)
                        }
                    }
                    if (showDatePicker) {
                        val dialog = remember { mutableStateOf<DatePickerDialog?>(null) }
                        DisposableEffect(showDatePicker) {
                            if (showDatePicker) {
                                dialog.value = DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        val picked = LocalDate.of(year, month + 1, dayOfMonth)
                                        selectedDate = picked.format(DateTimeFormatter.ISO_DATE)
                                        showDatePicker = false
                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
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
                        text = stringResource(R.string.export_file_label, "standup-$date.md"),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF666666)
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { onExport(date) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = standups.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EA), contentColor = Color.White)
                    ) {
                        Icon(Icons.Outlined.Download, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.export_button))
                    }
                }
            }
            if (standups.isEmpty()) {
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
                            text = standupsToMarkdown(date, standups),
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
    ExportScreen(
        date = "2025-12-30",
        standups = listOf(
            ExportStandupUiModel(
                name = "Alex Johnson",
                time = "09:10",
                yesterday = "Reviewed PRs",
                today = "Finalize API spec",
                blockers = "None",
                editedAt = null
            ),
            ExportStandupUiModel(
                name = "Priya Verma",
                time = "09:25",
                yesterday = "Auth flow fixes",
                today = "Add MFA",
                blockers = "Waiting on UX",
                editedAt = null
            )
        )
    )
}
