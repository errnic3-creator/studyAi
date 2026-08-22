package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class DayVelocity(
    val dayLabel: String,
    val hours: Float,
    val isToday: Boolean = false
)

@Composable
fun VelocityBarChart(
    velocities: List<DayVelocity>,
    targetHours: Float = 3.5f,
    modifier: Modifier = Modifier
) {
    val maxHours = (velocities.maxOfOrNull { it.hours } ?: 4f).coerceAtLeast(targetHours + 1f)

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("velocity_chart_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Weekly Study Velocity",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ElegantDarkTextPrimary
                        )
                    )
                    Text(
                        text = "Daily focus hours vs target (${targetHours}h)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = LavenderOnPrimary
                ) {
                    Text(
                        text = "Avg: ${String.format("%.1f", velocities.map { it.hours }.average())}h/day",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = LavenderPrimary,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Bars Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                velocities.forEach { item ->
                    val barHeightFraction = (item.hours / maxHours).coerceIn(0.06f, 1f)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (item.hours > 0) String.format("%.1f", item.hours) else "-",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                color = if (item.isToday) LavenderPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Box(
                            modifier = Modifier
                                .width(22.dp)
                                .fillMaxHeight(barHeightFraction)
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                .background(
                                    when {
                                        item.isToday -> LavenderPrimary
                                        item.hours >= targetHours -> Emerald500
                                        item.hours > 0 -> MauveSecondary
                                        else -> ElegantDarkOutline.copy(alpha = 0.5f)
                                    }
                                )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = item.dayLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (item.isToday) FontWeight.Bold else FontWeight.Normal,
                                color = if (item.isToday) LavenderPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    }
}
