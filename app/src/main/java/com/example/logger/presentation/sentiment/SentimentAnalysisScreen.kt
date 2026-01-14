package com.example.logger.presentation.sentiment

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SentimentAnalysisScreen(
    pos: Int,
    neu: Int,
    neg: Int,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sentiment Analysis", color = MaterialTheme.colorScheme.onPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Overall sentiment distribution",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    PieChart(
                        positive = pos,
                        neutral = neu,
                        negative = neg,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .semantics { contentDescription = "Sentiment pie chart" }
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LegendRow(pos = pos, neu = neu, neg = neg)
                }
            }
        }
    }
}

@Composable
private fun PieChart(
    positive: Int,
    neutral: Int,
    negative: Int,
    modifier: Modifier = Modifier
) {
    val total = (positive + neutral + negative).coerceAtLeast(1)
    val posAngle = 360f * (positive / total.toFloat())
    val neuAngle = 360f * (neutral / total.toFloat())
    val negAngle = 360f * (negative / total.toFloat())

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val diameter = size.minDimension
            val left = (size.width - diameter) / 2f
            val top = (size.height - diameter) / 2f
            val chartSize = Size(diameter, diameter)
            var startAngle = -90f

            // Positive - Green
            if (posAngle > 0f) {
                drawArc(
                    color = Color(0xFF4CAF50),
                    startAngle = startAngle,
                    sweepAngle = posAngle,
                    useCenter = true,
                    topLeft = Offset(left, top),
                    size = chartSize
                )
            }
            startAngle += posAngle

            // Neutral - Amber
            if (neuAngle > 0f) {
                drawArc(
                    color = Color(0xFFFFC107),
                    startAngle = startAngle,
                    sweepAngle = neuAngle,
                    useCenter = true,
                    topLeft = Offset(left, top),
                    size = chartSize
                )
            }
            startAngle += neuAngle

            // Negative - Red
            if (negAngle > 0f) {
                drawArc(
                    color = Color(0xFFF44336),
                    startAngle = startAngle,
                    sweepAngle = negAngle,
                    useCenter = true,
                    topLeft = Offset(left, top),
                    size = chartSize
                )
            }
        }
    }
}

@Composable
private fun LegendRow(pos: Int, neu: Int, neg: Int) {
    val total = (pos + neu + neg).coerceAtLeast(1)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendItem(color = Color(0xFF4CAF50), label = "Positive", count = pos, total = total)
        LegendItem(color = Color(0xFFFFC107), label = "Neutral", count = neu, total = total)
        LegendItem(color = Color(0xFFF44336), label = "Negative", count = neg, total = total)
    }
}

@Composable
private fun LegendItem(color: Color, label: String, count: Int, total: Int) {
    val percentage = ((count / total.toFloat()) * 100).toInt()
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.size(6.dp))
        Text(
            text = "$label: $count (${percentage}%)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
