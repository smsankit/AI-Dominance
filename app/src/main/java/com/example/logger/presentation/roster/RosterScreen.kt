package com.example.logger.presentation.roster

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.logger.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RosterScreen(
    members: List<String>,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.roster_title), color = MaterialTheme.colorScheme.onPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.back), tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Page header per wireframe with member count
            item {
                Column {
                    val pluralSuffix = if (members.size == 1) "" else "s"
                    Text(text = stringResource(R.string.roster_members_count, members.size, pluralSuffix), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            if (members.isEmpty()) {
                item {
                    Column(modifier = Modifier.fillMaxWidth().padding(48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("👥", style = MaterialTheme.typography.displaySmall)
                        Spacer(Modifier.height(8.dp))
                        Text(stringResource(R.string.roster_empty_title), style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(R.string.roster_empty_subtitle), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                item {
                    Text(text = stringResource(R.string.roster_section_label), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }

                items(members) { name ->
                    val idx = members.indexOf(name)
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            val initials = name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("")
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer) {
                                Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                                    Text(initials, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(name, style = MaterialTheme.typography.titleMedium)
                                Text(stringResource(R.string.roster_member_subtitle, idx + 1), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}
