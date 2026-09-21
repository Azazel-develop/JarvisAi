package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.telemetry.SystemTelemetryData
import com.example.ui.theme.*

@Composable
fun SystemStatusSummaryCard(
    telemetry: SystemTelemetryData,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alphaPulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val thermalColor = when (telemetry.thermalState) {
        "CRITICAL" -> PlasmaPink
        "WARM" -> Color(0xFFFFB300)
        "MODERATE" -> CosmicCyan
        else -> StatusGreen
    }

    val overallColor = when (telemetry.overallStatus) {
        "CRITICAL" -> PlasmaPink
        "WARNING" -> Color(0xFFFFB300)
        else -> StatusGreen
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = VoidSurface),
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.dp,
                Brush.linearGradient(listOf(CosmicCyan.copy(alpha = 0.6f), VioletNeon.copy(alpha = 0.4f))),
                RoundedCornerShape(20.dp)
            )
            .testTag("system_status_summary_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row: AI Initialized & Status Tag
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(overallColor.copy(alpha = alphaPulse))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "AI INITIALIZED • SYSTEM STATUS",
                            color = TextGlow,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = telemetry.statusSummary,
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                Surface(
                    color = overallColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, overallColor.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "● ${telemetry.overallStatus}",
                        color = overallColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(color = VoidBorder, thickness = 1.dp)

            // 3 Hardware Metrics Grid
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Battery Metric
                StatusMetricBlock(
                    title = "BATTERY",
                    value = "${telemetry.batteryPercent}%",
                    subtitle = if (telemetry.isCharging) "Charging • ${telemetry.batteryHealth}" else "Discharging • ${telemetry.batteryHealth}",
                    progress = telemetry.batteryPercent / 100f,
                    icon = Icons.Default.BatteryChargingFull,
                    accentColor = StatusGreen,
                    modifier = Modifier.weight(1f)
                )

                // Memory Metric
                StatusMetricBlock(
                    title = "MEMORY (RAM)",
                    value = "${telemetry.memoryUsagePercent}%",
                    subtitle = "${telemetry.ramUsedMb} MB / ${telemetry.ramTotalMb} MB",
                    progress = telemetry.memoryUsagePercent / 100f,
                    icon = Icons.Default.Memory,
                    accentColor = CosmicCyan,
                    modifier = Modifier.weight(1f)
                )

                // Thermal Metric
                StatusMetricBlock(
                    title = "THERMAL",
                    value = "%.1f°C".format(telemetry.temperatureCelsius),
                    subtitle = "State: ${telemetry.thermalState}",
                    progress = (telemetry.temperatureCelsius / 60f).coerceIn(0f, 1f),
                    icon = Icons.Default.Thermostat,
                    accentColor = thermalColor,
                    modifier = Modifier.weight(1f)
                )
            }

            // Bottom Scan / Refresh Action
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(VoidSurfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Shield",
                        tint = CosmicCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ALPHA-10 REAL-TIME HARDWARE MONITORING",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("system_status_refresh_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Telemetry",
                        tint = CosmicCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusMetricBlock(
    title: String,
    value: String,
    subtitle: String,
    progress: Float,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(GalaxyVoid.copy(alpha = 0.6f))
            .border(1.dp, VoidBorder, RoundedCornerShape(14.dp))
            .padding(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    color = TextMuted,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                )
            }

            Text(
                text = value,
                color = TextGlow,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold
            )

            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                color = accentColor,
                trackColor = VoidSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
            )

            Text(
                text = subtitle,
                color = TextMuted,
                fontSize = 8.5.sp,
                lineHeight = 11.sp,
                maxLines = 1
            )
        }
    }
}
