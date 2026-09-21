package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.JarvisViewModel
import com.example.ui.components.ArcReactorButton
import com.example.ui.components.CameraHologramOverlay
import com.example.ui.components.HolographicPulseOrb
import com.example.ui.components.HolographicVisualizer
import com.example.ui.components.SystemStatusSummaryCard
import com.example.ui.components.TelemetryWidgetRow
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    viewModel: JarvisViewModel,
    onNavigateToChat: () -> Unit,
    onNavigateToCode: () -> Unit,
    onNavigateToDiagnostics: () -> Unit
) {
    val telemetry by viewModel.telemetry.collectAsState()
    val isSpeaking by viewModel.speechEngine.isSpeaking.collectAsState()
    val isListening by viewModel.speechEngine.isListening.collectAsState()
    val speechAmplitude by viewModel.speechEngine.speechAmplitude.collectAsState()
    val isHologramProjected by viewModel.isHologramProjected.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()

    val userTitle = userProfile?.preferredTitle ?: "Sir"
    val userName = userProfile?.userName ?: "Tony Stark"
    val preferredDisplayName = if (userName.isNotBlank() && userName != "Tony Stark") userName else userTitle

    LaunchedEffect(Unit) {
        viewModel.speakWelcomeGreeting()
    }

    val lastJarvisMessage = remember(chatMessages) {
        chatMessages.lastOrNull { it.sender == "JARVIS" }?.text
            ?: "At your service, $userTitle. Say 'Jarvis', tap the Arc Reactor, or say 'Project Holo'."
    }

    val quickPrompts = listOf(
        "🔮 Project Holo",
        "Set attitude to Sarcastic",
        "Set attitude to Anime",
        "Set level to 50",
        "Jarvis, run system diagnostics",
        "Write a Python web scraper"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GalaxyVoid)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 90.dp)
    ) {
        // Hero Graphic Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.violet_galaxy_hud_1784740147854),
                contentDescription = "Violet Galaxy HUD Header",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                GalaxyVoid.copy(alpha = 0.3f),
                                GalaxyVoid
                            )
                        )
                    )
            )

            // Header Top Info & Proximity Stealth Indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Clearance",
                        tint = CosmicCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "STARK IND. ALPHA-10",
                        color = CosmicCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Interactive Proximity Sensor Status Chip
                    Surface(
                        color = if (telemetry.holoDimmedByProximity) StatusGold.copy(alpha = 0.25f) else CosmicCyan.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (telemetry.holoDimmedByProximity) StatusGold else CosmicCyan),
                        modifier = Modifier.clickable { viewModel.toggleManualFaceDownOverride() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sensors,
                                contentDescription = "Proximity",
                                tint = if (telemetry.holoDimmedByProximity) StatusGold else CosmicCyan,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (telemetry.holoDimmedByProximity) "DIMMED (FACE-DOWN)" else "PROXIMITY: ACTIVE",
                                color = if (telemetry.holoDimmedByProximity) StatusGold else CosmicCyan,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Surface(
                        color = StatusGreen.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StatusGreen)
                    ) {
                        Text(
                            text = "● ONLINE",
                            color = StatusGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // PROXIMITY STEALTH MODE ALERT BANNER
        if (telemetry.holoDimmedByProximity) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = VoidSurfaceVariant.copy(alpha = 0.9f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, VioletNeon),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VisibilityOff,
                            contentDescription = "Proximity Dimmed",
                            tint = VioletNeon,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "⚡ PROXIMITY STEALTH MODE • FACE-DOWN DETECTED",
                                color = TextGlow,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Holographic projections dimmed for privacy & battery optimization (${"%.1f".format(telemetry.proximityDistanceCm)}cm / Accel Z: ${"%.1f".format(telemetry.accelZ)})",
                                color = TextMuted,
                                fontSize = 9.sp
                            )
                        }
                    }
                    TextButton(onClick = { viewModel.toggleManualFaceDownOverride() }) {
                        Text("RESTORE", color = CosmicCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isHologramProjected) {
                Box(modifier = Modifier.graphicsLayer { alpha = if (telemetry.holoDimmedByProximity) 0.1f else 1.0f }) {
                    CameraHologramOverlay(
                        isSpeaking = isSpeaking,
                        isListening = isListening,
                        speechAmplitude = speechAmplitude,
                        userTitle = userTitle,
                        latestMessage = lastJarvisMessage,
                        isProcessing = isProcessing,
                        onSendQuery = { query -> viewModel.sendMessage(query) },
                        onDismiss = { viewModel.toggleHologramProjection(false) }
                    )
                }
            } else {
                // Holographic Visualizer & Arc Reactor Voice Controller
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(VoidSurface.copy(alpha = 0.6f))
                        .border(1.dp, VoidBorder, RoundedCornerShape(20.dp))
                ) {
                    HolographicVisualizer(
                        modifier = Modifier.fillMaxSize(),
                        isSpeaking = isSpeaking,
                        isListening = isListening,
                        speechAmplitude = speechAmplitude,
                        isDimmed = telemetry.holoDimmedByProximity
                    )

                    ArcReactorButton(
                        isListening = isListening,
                        isSpeaking = isSpeaking,
                        onClick = { viewModel.toggleVoiceListening() }
                    )
                }
            }

            // AI Initialized - System Status Summary Card
            SystemStatusSummaryCard(
                telemetry = telemetry,
                onRefresh = { viewModel.runDiagnostics() }
            )

            // Real-Time System Telemetry Card Row
            TelemetryWidgetRow(telemetry = telemetry)

            // Live JARVIS Status Feed Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = VoidSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, VoidBorder, RoundedCornerShape(16.dp))
                    .testTag("jarvis_status_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "JARVIS Brain",
                                tint = VioletNeon,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "J.A.R.V.I.S. NEURAL RESPONSE",
                                color = TextGlow,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (isProcessing) {
                            CircularProgressIndicator(
                                color = CosmicCyan,
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }

                    Text(
                        text = lastJarvisMessage,
                        color = TextMuted,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )

                    Button(
                        onClick = onNavigateToChat,
                        colors = ButtonDefaults.buttonColors(containerColor = VoidSurfaceVariant),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .align(Alignment.End)
                            .testTag("open_neural_chat_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = "Chat",
                            tint = CosmicCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "OPEN NEURAL CHAT",
                            color = TextGlow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Quick Speech / Command Chips
            Text(
                text = "VOICE COMMAND DIRECTIVES",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(quickPrompts) { prompt ->
                    SuggestionChip(
                        onClick = {
                            if (prompt.contains("Project Holo")) {
                                viewModel.toggleHologramProjection(true)
                            } else if (prompt.contains("diagnostics")) {
                                onNavigateToDiagnostics()
                            } else {
                                viewModel.sendMessage(prompt)
                                onNavigateToChat()
                            }
                        },
                        label = {
                            Text(
                                text = prompt,
                                color = TextGlow,
                                fontSize = 11.sp
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = VoidSurface
                        ),
                        border = SuggestionChipDefaults.suggestionChipBorder(
                            enabled = true,
                            borderColor = VoidBorder
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }
    }
}
