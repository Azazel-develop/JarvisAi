package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.telemetry.DiagnosticIssue
import com.example.ui.JarvisViewModel
import com.example.ui.components.SystemStatusSummaryCard
import com.example.ui.components.TelemetryWidgetRow
import com.example.ui.theme.*

@Composable
fun TelemetryDiagnosticsScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val telemetry by viewModel.telemetry.collectAsState()
    val issues by viewModel.diagnosticIssues.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GalaxyVoid)
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        // Top Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Column {
                Text(
                    text = "PREDICTIVE SYSTEM DIAGNOSTICS",
                    color = TextGlow,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Real-Time Stark Telemetry & Sensor Matrix",
                    color = CosmicCyan,
                    fontSize = 11.sp
                )
            }

            Button(
                onClick = { viewModel.runDiagnostics() },
                colors = ButtonDefaults.buttonColors(containerColor = VioletNeon),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("run_diagnostics_button")
            ) {
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = "Scan",
                    tint = GalaxyVoid,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "SCAN",
                    color = GalaxyVoid,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 90.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                SystemStatusSummaryCard(
                    telemetry = telemetry,
                    onRefresh = { viewModel.runDiagnostics() }
                )
            }

            // Parental Controls & Family Link Protection Shield Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = VoidSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, StatusGreen, RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Shield Active",
                                tint = StatusGreen,
                                modifier = Modifier.size(28.dp)
                            )
                            Column {
                                Text(
                                    text = "FAMILY LINK & PARENTAL CONTROL IMMUNITY",
                                    color = TextGlow,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Foreground Priority • Continuous Keep-Alive • Unrestricted Port 8081 WSS",
                                    color = CosmicCyan,
                                    fontSize = 9.sp
                                )
                            }
                        }

                        Surface(
                            color = StatusGreen.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "SHIELD ACTIVE",
                                color = StatusGreen,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            // Widget Row
            item {
                TelemetryWidgetRow(telemetry = telemetry)
            }

            // Detailed Hardware Sensors Grid Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = VoidSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, VoidBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "HARDWARE SENSOR TELEMETRY",
                            color = CosmicCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            SensorMetric("ACCEL X", "%.2f".format(telemetry.accelX))
                            SensorMetric("ACCEL Y", "%.2f".format(telemetry.accelY))
                            SensorMetric("ACCEL Z", "%.2f".format(telemetry.accelZ))
                        }

                        Divider(color = VoidBorder, thickness = 1.dp)

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            SensorMetric("TEMPERATURE", "%.1f°C".format(telemetry.temperatureCelsius))
                            SensorMetric("NET SPEED", "${telemetry.networkSpeedKbps / 1000} Mbps")
                            SensorMetric("CLEARANCE", telemetry.securityProtocol)
                        }

                        Divider(color = VoidBorder, thickness = 1.dp)

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            SensorMetric("PROXIMITY DIST", "%.1f cm".format(telemetry.proximityDistanceCm))
                            SensorMetric("PROXIMITY STATE", if (telemetry.isProximityNear) "NEAR/COVERED" else "UNCOVERED")
                            SensorMetric("FACE-DOWN HUD", if (telemetry.holoDimmedByProximity) "DIMMED" else "ACTIVE")
                        }
                    }
                }
            }

            // Diagnostic Issues List
            item {
                Text(
                    text = "NEURAL DIAGNOSTIC REPORT",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (issues.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = VoidSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, VoidBorder, RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Optimal",
                                tint = StatusGreen,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "ALL SYSTEM NODES OPTIMAL",
                                color = TextGlow,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Tap 'SCAN' above to execute a real-time neural diagnostics pass.",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            } else {
                items(issues) { issue ->
                    DiagnosticIssueCard(issue = issue)
                }
            }
        }
    }
}

@Composable
fun SensorMetric(label: String, value: String) {
    Column {
        Text(text = label, color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Text(text = value, color = TextGlow, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
fun DiagnosticIssueCard(issue: DiagnosticIssue) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = VoidSurface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, VoidBorder, RoundedCornerShape(12.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(StatusGreen.copy(alpha = 0.2f))
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = issue.status,
                    tint = StatusGreen,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = issue.title,
                    color = TextGlow,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = issue.detail,
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            Surface(
                color = StatusGreen.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, StatusGreen)
            ) {
                Text(
                    text = issue.status,
                    color = StatusGreen,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}
