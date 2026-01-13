package com.example.logger.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.logger.domain.model.SentimentSummary

@Composable
fun TeamMoodCard(
    sentimentSummary: SentimentSummary?,
    isLoading: Boolean,
    error: String?,
    modifier: Modifier = Modifier,
    onNavigateToSentimentAnalysis: (() -> Unit)? = null,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        val applyClickable: (Modifier) -> Modifier = { base ->
            if (onNavigateToSentimentAnalysis != null) base.clickable(onClick = onNavigateToSentimentAnalysis) else base
        }
        when {
            isLoading -> {
                Box(
                    modifier = applyClickable(
                        Modifier
                            .fillMaxWidth()
                            .drawBehind {
                                drawLine(
                                    color = Color(0xFF4CAF50),
                                    start = Offset(0f, 0f),
                                    end = Offset(0f, size.height),
                                    strokeWidth = 4.dp.toPx()
                                )
                            }
                            .background(Color(0xFFE8F5E9))
                            .padding(16.dp)
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(color = Color(0xFF2E7D32))
                        if (onNavigateToSentimentAnalysis != null) {
                            IconButton(onClick = onNavigateToSentimentAnalysis) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "View Sentiment Analysis",
                                    tint = Color(0xFF2E7D32)
                                )
                            }
                        }
                    }
                }
            }
            error != null && sentimentSummary == null -> {
                Box(
                    modifier = applyClickable(
                        Modifier
                            .fillMaxWidth()
                            .drawBehind {
                                drawLine(
                                    color = Color(0xFFF44336),
                                    start = Offset(0f, 0f),
                                    end = Offset(0f, size.height),
                                    strokeWidth = 4.dp.toPx()
                                )
                            }
                            .background(Color(0xFFFFEBEE))
                            .padding(16.dp)
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Unable to load team mood",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFC62828)
                        )
//                        if (onNavigateToSentimentAnalysis != null) {
//                            IconButton(onClick = onNavigateToSentimentAnalysis) {
//                                Icon(
//                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
//                                    contentDescription = "View Sentiment Analysis",
//                                    tint = Color(0xFFC62828)
//                                )
//                            }
//                        }
                    }
                }
            }
            sentimentSummary != null && sentimentSummary.total == 0 -> {
                Box(
                    modifier = applyClickable(
                        Modifier
                            .fillMaxWidth()
                            .drawBehind {
                                drawLine(
                                    color = Color(0xFFFFC107),
                                    start = Offset(0f, 0f),
                                    end = Offset(0f, size.height),
                                    strokeWidth = 4.dp.toPx()
                                )
                            }
                            .background(Color(0xFFFFF8E1))
                            .padding(16.dp)
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(text = "😐", fontSize = 24.sp)
                            Text(
                                text = "No sentiment data today",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF7C5800)
                            )
                        }
//                        if (onNavigateToSentimentAnalysis != null) {
//                            IconButton(onClick = onNavigateToSentimentAnalysis) {
//                                Icon(
//                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
//                                    contentDescription = "View Sentiment Analysis",
//                                    tint = Color(0xFF7C5800)
//                                )
//                            }
//                        }
                    }
                }
            }
            sentimentSummary != null -> {
                // Use dominant (largest) sentiment to determine mood and styling
                val positive = sentimentSummary.positive
                val neutral = sentimentSummary.neutral
                val negative = sentimentSummary.negative

                val dominant = when {
                    positive > negative && positive > neutral -> "POSITIVE"
                    negative > positive && negative > neutral -> "NEGATIVE"
                    neutral > positive && neutral > negative -> "NEUTRAL"
                    else -> "NEUTRAL" // tie case -> neutral/balanced
                }

                val moodText = when (dominant) {
                    "POSITIVE" -> "Positive 🙂"
                    "NEUTRAL" -> "Neutral 😐"
                    else -> "Needs Attention 😟" // NEGATIVE
                }

                val backgroundColor = when (dominant) {
                    "POSITIVE" -> Color(0xFFE8F5E9) // Light green
                    "NEUTRAL" -> Color(0xFFFFF8E1) // Light yellow
                    else -> Color(0xFFFFEBEE)       // Light red
                }

                val borderColor = when (dominant) {
                    "POSITIVE" -> Color(0xFF4CAF50) // Green
                    "NEUTRAL" -> Color(0xFFFFC107) // Amber
                    else -> Color(0xFFF44336)       // Red
                }

                val textColor = when (dominant) {
                    "POSITIVE" -> Color(0xFF2E7D32) // Dark green
                    "NEUTRAL" -> Color(0xFF7C5800) // Dark amber
                    else -> Color(0xFFC62828)       // Dark red
                }

                val subtitleColor = when (dominant) {
                    "POSITIVE" -> Color(0xFF1B5E20) // Darker green
                    "NEUTRAL" -> Color(0xFF5D4037) // Dark brown
                    else -> Color(0xFFB71C1C)       // Darker red
                }

                val insightText = when (dominant) {
                    "POSITIVE" -> "Team morale is good!"
                    "NEUTRAL" -> "Team sentiment is neutral."
                    else -> "Consider team check-in."
                }

                Box(
                    modifier = applyClickable(
                        Modifier
                            .fillMaxWidth()
                            .drawBehind {
                                drawLine(
                                    color = borderColor,
                                    start = Offset(0f, 0f),
                                    end = Offset(0f, size.height),
                                    strokeWidth = 4.dp.toPx()
                                )
                            }
                            .background(backgroundColor)
                            .padding(16.dp)
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Team Mood: $moodText",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                ),
                                color = textColor
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Based on ${sentimentSummary.total} submission${if (sentimentSummary.total != 1) "s" else ""}. $insightText",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 13.sp
                                ),
                                color = subtitleColor
                            )
                        }

                        if (onNavigateToSentimentAnalysis != null) {
                            IconButton(
                                onClick = onNavigateToSentimentAnalysis,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "View Sentiment Analysis",
                                    tint = textColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
            else -> {
                Box(
                    modifier = applyClickable(
                        Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF5F5F5))
                            .padding(16.dp)
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "No sentiment data available",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
//                        if (onNavigateToSentimentAnalysis != null) {
//                            IconButton(onClick = onNavigateToSentimentAnalysis) {
//                                Icon(
//                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
//                                    contentDescription = "View Sentiment Analysis",
//                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
//                                )
//                            }
//                        }
                    }
                }
            }
        }
    }
}
