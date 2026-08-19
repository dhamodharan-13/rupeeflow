package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.DaySpending
import java.util.Locale

@Composable
fun BarTrendChart(
    dailyData: List<DaySpending>,
    modifier: Modifier = Modifier,
    title: String = "Spending Trend"
) {
    if (dailyData.isEmpty()) return

    val maxAmount = remember(dailyData) {
        val max = dailyData.maxOfOrNull { it.amount } ?: 0.0
        if (max <= 0.0) 100.0 else max
    }

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(dailyData) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animationSpec = tween(600))
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("bar_trend_chart"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Canvas Bar chart
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                val count = dailyData.size
                if (count == 0) return@Canvas

                val availableWidth = size.width
                val barWidth = (availableWidth / count) * 0.55f
                val spacing = availableWidth / count
                val chartHeight = size.height

                for (i in 0 until count) {
                    val item = dailyData[i]
                    val xCenter = (i * spacing) + (spacing / 2f)
                    val xLeft = xCenter - (barWidth / 2f)

                    // Draw track background
                    drawRoundRect(
                        color = trackColor.copy(alpha = 0.5f),
                        topLeft = Offset(xLeft, 0f),
                        size = Size(barWidth, chartHeight),
                        cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                    )

                    // Draw filled bar
                    if (item.amount > 0) {
                        val barHeight = ((item.amount / maxAmount) * chartHeight * animProgress.value).toFloat()
                        val yTop = chartHeight - barHeight

                        drawRoundRect(
                            color = primaryColor,
                            topLeft = Offset(xLeft, yTop),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Labels row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // If weekly (7 items), show all 7 labels; if monthly (30 items), show subset
                if (dailyData.size <= 7) {
                    dailyData.forEach { day ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = day.dayLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (day.amount > 0) {
                                Text(
                                    text = String.format(Locale.getDefault(), "₹%.0f", day.amount),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                } else {
                    // Monthly sampling
                    val sampleIndices = listOf(0, dailyData.size / 4, dailyData.size / 2, (3 * dailyData.size) / 4, dailyData.size - 1)
                    sampleIndices.forEach { idx ->
                        if (idx in dailyData.indices) {
                            val day = dailyData[idx]
                            Text(
                                text = "Day ${day.dayLabel}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
