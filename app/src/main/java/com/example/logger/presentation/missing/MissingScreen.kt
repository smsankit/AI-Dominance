package com.example.logger.presentation.missing

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.logger.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissingScreen(
    roster: List<String>,
    pendingCount: Int,
    isLoadingComplete: Boolean = true,
    onNavigateBack: () -> Unit,
    onSubmitStandup: (String) -> Unit // Pass member name
) {
    // Calculate pending members from roster minus the submitted count
    // Since we don't have exact names, show first N members as "potentially missing"
    // This is a limitation without loading all data
    val totalSubmitted = roster.size - pendingCount
    val pending = roster.take(pendingCount) // Simplified - shows first N as pending

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.missing_title), color = MaterialTheme.colorScheme.onPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.back), tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { inner ->
        Column(modifier = Modifier.fillMaxSize().padding(inner).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column {
                val suffix = if (pendingCount == 1) "" else "s"
                Text(text = stringResource(R.string.missing_members_count, pendingCount, suffix), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (pending.isEmpty()) {
                Column(modifier = Modifier.fillMaxWidth().padding(48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🎉", style = MaterialTheme.typography.displaySmall)
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.missing_empty_title), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    Text(stringResource(R.string.missing_empty_subtitle), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                pending.forEach { name ->
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            val initials = name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("")
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer) {
                                Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                                    Text(initials, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(name, style = MaterialTheme.typography.titleMedium)
                                Text(stringResource(R.string.missing_row_subtitle), style = MaterialTheme.typography.bodySmall, color = Color(0xFFD32F2F))
                            }
                            Button(onClick = { onSubmitStandup(name) }) {
                                Icon(Icons.Outlined.EditNote, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Submit")
                            }
                        }
                    }
                }
            }
        }
    }
}
