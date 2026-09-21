package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.telemetry.SystemTelemetryData
import com.example.ui.theme.*

@Composable
fun TelemetryWidgetRow(
    telemetry: SystemTelemetryData,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TelemetryCard(
            title = "BATTERY",
            value = "${telemetry.batteryPercent}%",
            progress = telemetry.batteryPercent / 100f,
            icon = Icons.Default.BatteryChargingFull,
            color = StatusGreen,
            modifier = Modifier.weight(1f)
        )
        TelemetryCard(
            title = "RAM USED",
            value = "${telemetry.ramUsedMb}MB",
            progress = telemetry.ramUsedMb.toFloat() / telemetry.ramTotalMb,
            icon = Icons.Default.Memory,
            color = CosmicCyan,
            modifier = Modifier.weight(1f)
        )
        TelemetryCard(
            title = "CPU LOAD",
            value = "${telemetry.cpuUsagePercent}%",
            progress = telemetry.cpuUsagePercent / 100f,
            icon = Icons.Default.Speed,
            color = VioletNeon,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun TelemetryCard(
    title: String,
    value: String,
    progress: Float,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(VoidSurface.copy(alpha = 0.85f))
            .border(1.dp, VoidBorder, RoundedCornerShape(12.dp))
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
                    tint = color,
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
                color = color,
                trackColor = VoidSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
            )
        }
    }
}
