package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.telemetry.DiagnosticActionLog
import com.example.ui.JarvisViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun DiagnosticOverlayHud(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    val logs by viewModel.diagnosticLogs.collectAsState()
    val servers by viewModel.eaglercraftServers.collectAsState()

    var isExpanded by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("ALL") }
    var isAutoScrollEnabled by remember { mutableStateOf(true) }

    val filteredLogs = remember(logs, selectedFilter) {
        if (selectedFilter == "ALL") logs
        else logs.filter { it.tag.equals(selectedFilter, ignoreCase = true) }
    }

    val listState = rememberLazyListState()

    // Auto-scroll to top when new log arrives
    LaunchedEffect(logs.size, isAutoScrollEnabled) {
        if (isAutoScrollEnabled && logs.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    val latestLog = logs.firstOrNull()

    Box(modifier = modifier) {
        if (!isExpanded) {
            // Minimized Floating Diagnostic Pill HUD
            Surface(
                onClick = { isExpanded = true },
                color = GalaxyVoid.copy(alpha = 0.92f),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    Brush.horizontalGradient(listOf(CosmicCyan, VioletNeon, StatusGreen))
                ),
                tonalElevation = 8.dp,
                modifier = Modifier
                    .wrapContentSize()
                    .testTag("diagnostic_overlay_pill")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                color = when (latestLog?.status) {
                                    "ONLINE", "SUCCESS" -> StatusGreen
                                    "PROCESSING", "ACTIVE" -> StatusGold
                                    "ERROR" -> StatusRed
                                    else -> CosmicCyan
                                },
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "⚡ DIAGNOSTICS & AI LOGS",
                                color = TextGlow,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = VioletNeon.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = "${logs.size} RECS",
                                    color = VioletLight,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        if (latestLog != null) {
                            Text(
                                text = "[${latestLog.tag}] ${latestLog.title}",
                                color = CosmicCyan,
                                fontSize = 9.sp,
                                maxLines = 1
                            )
                        } else {
                            Text(
                                text = "All systems operational • WSS active",
                                color = TextMuted,
                                fontSize = 9.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.OpenInFull,
                        contentDescription = "Expand HUD",
                        tint = CosmicCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        } else {
            // Expanded Rolling Log Terminal Overlay Sheet
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = VoidSurface.copy(alpha = 0.96f)),
                modifier = Modifier
                    .fillMaxWidth(0.96f)
                    .fillMaxHeight(0.70f)
                    .border(2.dp, Brush.horizontalGradient(listOf(CosmicCyan, VioletNeon, StatusGreen)), RoundedCornerShape(20.dp))
                    .testTag("diagnostic_overlay_expanded")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                ) {
                    // Header Bar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = "Terminal",
                                tint = StatusGreen,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "J.A.R.V.I.S. REAL-TIME PROCESS DIAGNOSTICS",
                                    color = TextGlow,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "Live rolling log of AI streams, file modifications & WSS sockets",
                                    color = CosmicCyan,
                                    fontSize = 9.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = { isExpanded = false },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TextGlow,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Server WSS Quick Status Bar
                    Surface(
                        color = GalaxyVoid,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, VoidBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("WSS SERVERS:", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${servers.count { it.status == "ONLINE" }}/${servers.size} ONLINE",
                                    color = StatusGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = {
                                    servers.forEach { srv ->
                                        viewModel.testWebSocketConnection(srv.wssUrl, srv.serverName)
                                    }
                                    Toast.makeText(context, "Initiated WSS WebSocket Handshake Pings!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.testTag("test_all_wss_button")
                            ) {
                                Icon(imageVector = Icons.Default.NetworkCheck, contentDescription = "Ping", tint = GalaxyVoid, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("PING ALL WSS", color = GalaxyVoid, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Tag Filter Strip
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("ALL", "AI ACTION", "FILE MOD", "SERVER WSS", "SYSTEM").forEach { tag ->
                            FilterChip(
                                selected = selectedFilter == tag,
                                onClick = { selectedFilter = tag },
                                label = { Text(tag, fontSize = 9.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = VioletNeon,
                                    containerColor = VoidSurfaceVariant,
                                    selectedLabelColor = TextGlow,
                                    labelColor = TextMuted
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Rolling Log Stream
                    Surface(
                        color = GalaxyVoid,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, VoidBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        if (filteredLogs.isEmpty()) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = "No diagnostic events recorded for [$selectedFilter]",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        } else {
                            LazyColumn(
                                state = listState,
                                contentPadding = PaddingValues(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(filteredLogs, key = { it.id }) { item ->
                                    DiagnosticLogCard(log = item)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Action Controls Footer
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    val fullLogText = logs.joinToString("\n") { "[${it.timestamp}] [${it.tag}] (${it.status}) ${it.title} - ${it.details}" }
                                    clipboardManager.setText(AnnotatedString(fullLogText))
                                    Toast.makeText(context, "Copied diagnostic logs to clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = CosmicCyan, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("COPY LOGS", color = CosmicCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { viewModel.clearDiagnosticLogs() },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Clear", tint = StatusRed, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("CLEAR", color = StatusRed, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        TextButton(
                            onClick = { isAutoScrollEnabled = !isAutoScrollEnabled }
                        ) {
                            Text(
                                text = if (isAutoScrollEnabled) "AUTOSCROLL: ON" else "AUTOSCROLL: OFF",
                                color = if (isAutoScrollEnabled) StatusGreen else TextMuted,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DiagnosticLogCard(log: DiagnosticActionLog) {
    var isExpanded by remember { mutableStateOf(false) }

    val tagColor = when (log.tag) {
        "AI ACTION" -> PlasmaPink
        "FILE MOD" -> CosmicCyan
        "SERVER WSS" -> StatusGreen
        else -> VioletLight
    }

    val statusColor = when (log.status) {
        "ONLINE", "SUCCESS" -> StatusGreen
        "PROCESSING", "ACTIVE" -> StatusGold
        "ERROR" -> StatusRed
        else -> CosmicCyan
    }

    Card(
        onClick = { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(containerColor = VoidSurfaceVariant),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, VoidBorder, RoundedCornerShape(8.dp))
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = tagColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = log.tag,
                            color = tagColor,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = log.timestamp,
                        color = TextMuted,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Surface(
                    color = statusColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = log.status,
                        color = statusColor,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = log.title,
                color = TextGlow,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            if (isExpanded || log.details.length < 80) {
                Text(
                    text = log.details,
                    color = CosmicCyan,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(top = 4.dp)
                )
            } else {
                Text(
                    text = log.details.take(80) + "...",
                    color = TextMuted,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}
