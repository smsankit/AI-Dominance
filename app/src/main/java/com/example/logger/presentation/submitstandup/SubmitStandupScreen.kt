package com.example.logger.presentation.submitstandup

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.logger.R
import kotlinx.coroutines.flow.collect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmitStandupScreen(
    viewModel: SubmitStandupViewModel,
    onSubmitted: () -> Unit,
    onCancel: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { evt ->
            when (evt) {
                is SubmitStandupUiEvent.ApiError -> snackbarHostState.showSnackbar(evt.message)
                SubmitStandupUiEvent.Submitted -> { /* no-op here, navigation handled by callback */ }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.submit_standup_title), color = MaterialTheme.colorScheme.onPrimary) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(imageVector = Icons.Outlined.ArrowBack, contentDescription = stringResource(R.string.back), tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Page header
            Column {
                Text(text = stringResource(R.string.submit_standup_title), style = MaterialTheme.typography.headlineMedium)
                Text(text = stringResource(R.string.submit_standup_subtitle), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Validation banner
            if (state.error != null) {
                ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(
                        text = when (state.error) {
                            "required_fields_missing" -> stringResource(R.string.required_fields_missing)
                            else -> stringResource(R.string.unknown_error)
                        },
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            // Form card styled per wireframe (tinted surface with shadow)
            ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Name dropdown (roster)
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                        OutlinedTextField(
                            value = state.name,
                            onValueChange = { viewModel.onNameChange(it) },
                            label = { Text(stringResource(R.string.name_label)) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            readOnly = true,
                            isError = state.nameError,
                            supportingText = {
                                if (state.nameError) Text(stringResource(R.string.required_fields_missing))
                            }
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            state.roster.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        viewModel.onNameChange(option)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = state.yesterday,
                        onValueChange = viewModel::onYesterdayChange,
                        label = { Text(stringResource(R.string.yesterday_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        isError = state.yesterdayError,
                        supportingText = {
                            if (state.yesterdayError) Text(stringResource(R.string.required_fields_missing))
                        }
                    )

                    OutlinedTextField(
                        value = state.today,
                        onValueChange = viewModel::onTodayChange,
                        label = { Text(stringResource(R.string.today_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        isError = state.todayError,
                        supportingText = {
                            if (state.todayError) Text(stringResource(R.string.required_fields_missing))
                        }
                    )

                    OutlinedTextField(
                        value = state.blockers,
                        onValueChange = viewModel::onBlockersChange,
                        label = { Text(stringResource(R.string.blockers_label_optional)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )

                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { viewModel.submit { onSubmitted() } }, enabled = !state.isSubmitting, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.submit))
                        }
                        OutlinedButton(onClick = onCancel, enabled = !state.isSubmitting, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.cancel))
                        }
                    }

                    if (state.isSubmitting) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        }
    }
}
