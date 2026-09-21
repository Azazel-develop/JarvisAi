package com.example.ui.screens

import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.local.ChatMessage
import com.example.ui.JarvisViewModel
import com.example.ui.components.CodeBlockView
import com.example.ui.components.CameraHologramOverlay
import com.example.ui.components.HolographicPulseOrb
import com.example.ui.components.SystemStatusSummaryCard
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun NeuralChatScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val messages by viewModel.chatMessages.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val streamingText by viewModel.streamingMessage.collectAsState()
    val thinkingProgress by viewModel.thinkingProgress.collectAsState()
    val thinkingStage by viewModel.thinkingStage.collectAsState()
    val thinkingSources by viewModel.thinkingSources.collectAsState()
    val thinkingSteps by viewModel.thinkingSteps.collectAsState()
    val quantumDepth by viewModel.quantumDepth.collectAsState()
    val isSpeaking by viewModel.speechEngine.isSpeaking.collectAsState()
    val isListening by viewModel.speechEngine.isListening.collectAsState()
    val speechAmplitude by viewModel.speechEngine.speechAmplitude.collectAsState()
    val recognizedText by viewModel.speechEngine.recognizedText.collectAsState()
    val isHologramProjected by viewModel.isHologramProjected.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val telemetry by viewModel.telemetry.collectAsState()

    val userTitle = userProfile?.preferredTitle ?: "Sir"

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Speech-to-text Permission Launcher
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.toggleVoiceListening()
        } else {
            Toast.makeText(context, "Microphone permission required for speech-to-text", Toast.LENGTH_SHORT).show()
        }
    }

    fun requestSpeechAndToggle() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            viewModel.toggleVoiceListening()
        } else {
            audioPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
        }
    }

    // Smooth glassmorphism border brush
    val glassBorderBrush = Brush.linearGradient(
        colors = listOf(
            VioletNeon.copy(alpha = 0.7f),
            CosmicCyan.copy(alpha = 0.4f),
            PlasmaPink.copy(alpha = 0.5f)
        )
    )

    val pulsingTransition = rememberInfiniteTransition(label = "HudPulse")
    val pulseAlpha by pulsingTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    // Auto-scroll on new message or stream update
    LaunchedEffect(messages.size, streamingText, isHologramProjected) {
        val totalItems = messages.size + (if (!streamingText.isNullOrEmpty() || isProcessing) 1 else 0)
        if (totalItems > 0) {
            listState.animateScrollToItem(totalItems - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GalaxyVoid)
    ) {
        // Glassmorphism Violet HUD Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .background(VoidSurface.copy(alpha = 0.85f))
                .border(width = 1.dp, brush = glassBorderBrush, shape = RoundedCornerShape(0.dp))
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(VioletNeon.copy(alpha = 0.2f))
                            .border(1.5.dp, VioletNeon, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Neural Core",
                            tint = CosmicCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "J.A.R.V.I.S. NEURAL HUD",
                                color = TextGlow,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isProcessing || !streamingText.isNullOrEmpty()) StatusGold else StatusGreen)
                            )
                        }

                        Text(
                            text = when {
                                isSpeaking -> "Vocalizing Output to $userTitle..."
                                isListening -> "Listening to Voice Command..."
                                !streamingText.isNullOrEmpty() -> "Streaming Real-Time Intelligence..."
                                else -> "Gemini 2.5 Flash Stream Engine • Active"
                            },
                            color = CosmicCyan,
                            fontSize = 10.sp
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = { viewModel.toggleHologramProjection() },
                        modifier = Modifier.testTag("project_holo_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sensors,
                            contentDescription = "Project Holo",
                            tint = if (isHologramProjected) CosmicCyan else VioletNeon
                        )
                    }

                    IconButton(
                        onClick = {
                            if (isSpeaking) viewModel.speechEngine.stopSpeaking()
                        },
                        modifier = Modifier.testTag("mute_speech_button")
                    ) {
                        Icon(
                            imageVector = if (isSpeaking) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "Mute Vocal",
                            tint = if (isSpeaking) PlasmaPink else TextMuted
                        )
                    }

                    IconButton(
                        onClick = { viewModel.clearChatHistory() },
                        modifier = Modifier.testTag("clear_chat_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear Chat",
                            tint = TextMuted
                        )
                    }
                }
            }
        }

        // Voice Active Waveform Bar (When listening or speaking)
        if (isListening || isSpeaking) {
            Surface(
                color = VioletNeon.copy(alpha = 0.15f),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, VioletNeon.copy(alpha = pulseAlpha))
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 12.dp)
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.Mic else Icons.Default.VolumeDown,
                        contentDescription = "Audio Wave",
                        tint = VioletNeon,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isListening) {
                            if (recognizedText.isNotBlank()) "LIVE SPEECH: \"$recognizedText\"" else "SPEECH RECOGNITION ACTIVE - SPEAK NOW, $userTitle..."
                        } else "VOCALIZING RESPONSE SYNTHESIS...",
                        color = VioletLight,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Chat Messages List (Optimized zero-lag LazyColumn with explicit keys)
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // Animated Hologram Pulse Orb & AR Projection
            if (isHologramProjected) {
                item(key = "hologram_pulse_orb_item") {
                    CameraHologramOverlay(
                        isSpeaking = isSpeaking,
                        isListening = isListening,
                        speechAmplitude = speechAmplitude,
                        userTitle = userTitle,
                        latestMessage = messages.lastOrNull()?.text ?: streamingText,
                        isProcessing = isProcessing,
                        cameraFov = userProfile?.cameraFov ?: 68.0f,
                        perspectiveTilt = userProfile?.perspectiveTilt ?: 0.0f,
                        perspectiveShearX = userProfile?.perspectiveShearX ?: 0.0f,
                        perspectiveShearY = userProfile?.perspectiveShearY ?: 0.0f,
                        perspectiveRoll = userProfile?.perspectiveRoll ?: 0.0f,
                        surfaceDepthOffset = userProfile?.surfaceDepthOffset ?: 1.5f,
                        surfaceSnapEnabled = userProfile?.surfaceSnapEnabled ?: true,
                        onSendQuery = { query: String -> viewModel.sendMessage(query) },
                        onDismiss = { viewModel.toggleHologramProjection(false) }
                    )
                }
            }

            if (messages.isEmpty() && streamingText.isNullOrEmpty() && !isProcessing && !isHologramProjected) {
                item {
                    EmptyChatState(
                        userTitle = userTitle,
                        telemetry = telemetry,
                        onRefresh = { viewModel.runDiagnostics() },
                        onPromptClick = { prompt ->
                            viewModel.sendMessage(prompt)
                        }
                    )
                }
            } else {
                items(
                    items = messages,
                    key = { it.id }
                ) { msg ->
                    ChatMessageItem(
                        message = msg,
                        userTitle = userTitle,
                        onSpeakMessage = { viewModel.speakMessage(msg.text) },
                        onDeleteMessage = { viewModel.deleteMessage(msg.id) },
                        onSaveFile = { name, code ->
                            viewModel.saveGeneratedFile(name, msg.language ?: "Kotlin", code)
                        }
                    )
                }
            }

            // Real-Time Streaming Message Card & Thinking Progression Bar
            if (!streamingText.isNullOrEmpty()) {
                item {
                    StreamingMessageItem(
                        streamingText = streamingText!!,
                        userTitle = userTitle,
                        onSaveFile = { name, code ->
                            viewModel.saveGeneratedFile(name, "Kotlin", code)
                        }
                    )
                }
            } else if (isProcessing) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(VoidSurface.copy(alpha = 0.95f))
                            .border(1.dp, VioletNeon.copy(alpha = pulseAlpha), RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        // Header Title & Quantum Depth Indicator
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    color = CosmicCyan,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "QUANTUM THINKING MATRIX ACTIVE",
                                        color = TextGlow,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = if (thinkingStage.isNotBlank()) thinkingStage else "Reasoning & Analyzing Input...",
                                        color = CosmicCyan,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            // Depth level tag
                            Surface(
                                color = VioletNeon.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, VioletNeon)
                            ) {
                                Text(
                                    text = "DEPTH $quantumDepth/5",
                                    color = VioletLight,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Glowing Animated Progress Bar
                        LinearProgressIndicator(
                            progress = { thinkingProgress.coerceIn(0.05f, 1.0f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = CosmicCyan,
                            trackColor = VoidSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Chain of Thought Live Reasoning Trace Steps
                        Text(
                            text = "CHAIN-OF-THOUGHT REASONING TRACE:",
                            color = TextMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            thinkingSteps.forEach { step ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = StatusGreen,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = step,
                                        color = TextGlow,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        // Knowledge Matrix Sources Badges
                        if (thinkingSources.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "KNOWLEDGE MATRIX SOURCES RETRIEVED:",
                                color = VioletLight,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(thinkingSources) { source ->
                                    Surface(
                                        color = CosmicCyan.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, CosmicCyan.copy(alpha = 0.4f))
                                    ) {
                                        Text(
                                            text = "🌐 $source",
                                            color = TextGlow,
                                            fontSize = 9.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quick Query Chips Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val suggestions = listOf(
                "🔮 Project Holo",
                "⚡ Run Core Diagnostics",
                "💻 Write Kotlin Flow Pipeline",
                "🌌 Solve Physics Equation",
                "📁 Create Custom Script File"
            )
            items(suggestions) { suggestion ->
                Surface(
                    color = VoidSurfaceVariant.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VioletNeon.copy(alpha = 0.4f)),
                    modifier = Modifier.clickable {
                        if (suggestion.contains("Project Holo")) {
                            viewModel.toggleHologramProjection(true)
                        } else {
                            viewModel.sendMessage(suggestion.substringAfter(" "))
                        }
                    }
                ) {
                    Text(
                        text = suggestion,
                        color = CosmicCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        // Glassmorphism Violet Input Bar
        Surface(
            color = VoidSurface.copy(alpha = 0.9f),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, glassBorderBrush),
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                IconButton(
                    onClick = { requestSpeechAndToggle() },
                    modifier = Modifier.testTag("chat_voice_input_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Input",
                        tint = if (isListening) StatusGreen else CosmicCyan
                    )
                }

                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text("Ask J.A.R.V.I.S. anything, $userTitle...", color = TextMuted, fontSize = 13.sp)
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = TextGlow,
                        unfocusedTextColor = TextGlow
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_textfield")
                )

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            viewModel.sendMessage(inputText)
                            inputText = ""
                        }
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(VioletNeon)
                        .testTag("send_message_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Message",
                        tint = GalaxyVoid
                    )
                }
            }
        }
    }
}

@Composable
fun StreamingMessageItem(
    streamingText: String,
    userTitle: String,
    onSaveFile: (String, String) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "CursorBlink")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "CursorAlpha"
    )

    Row(
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(VoidSurfaceVariant)
                .border(1.dp, VioletNeon, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "JARVIS Streaming",
                tint = CosmicCyan,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.widthIn(max = 340.dp)) {
            Text(
                text = "J.A.R.V.I.S. (STREAMING)",
                color = VioletLight,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 2.dp)
            )

            Card(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = 4.dp,
                    bottomEnd = 16.dp
                ),
                colors = CardDefaults.cardColors(containerColor = VoidSurface.copy(alpha = 0.9f)),
                modifier = Modifier.border(1.dp, VioletNeon, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = streamingText + if (cursorAlpha > 0.5f) " ▌" else "  ",
                        color = TextGlow,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    userTitle: String,
    onSpeakMessage: () -> Unit,
    onDeleteMessage: () -> Unit,
    onSaveFile: (String, String) -> Unit
) {
    val isUser = message.sender == "USER"

    Row(
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (!isUser) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(VoidSurfaceVariant)
                    .border(1.dp, VioletNeon, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "JARVIS Avatar",
                    tint = CosmicCyan,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 340.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isUser) userTitle else "J.A.R.V.I.S.",
                    color = if (isUser) CosmicCyan else VioletLight,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 2.dp)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!isUser) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Replay Speech",
                            tint = CosmicCyan,
                            modifier = Modifier
                                .size(14.dp)
                                .clickable { onSpeakMessage() }
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Message",
                        tint = TextMuted.copy(alpha = 0.7f),
                        modifier = Modifier
                            .size(13.dp)
                            .clickable { onDeleteMessage() }
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isUser) VoidSurfaceVariant else VoidSurface.copy(alpha = 0.85f)
                ),
                modifier = Modifier.border(
                    width = 1.dp,
                    color = if (isUser) VioletNeon.copy(alpha = 0.5f) else VoidBorder,
                    shape = RoundedCornerShape(16.dp)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = message.text,
                        color = TextGlow,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )

                    if (!message.codeSnippet.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        CodeBlockView(
                            code = message.codeSnippet,
                            language = message.language ?: "Kotlin",
                            onSaveFile = onSaveFile
                        )
                    }
                }
            }
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(VoidSurfaceVariant)
                    .border(1.dp, CosmicCyan, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User Avatar",
                    tint = CosmicCyan,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyChatState(
    userTitle: String,
    telemetry: com.example.data.telemetry.SystemTelemetryData,
    onRefresh: () -> Unit,
    onPromptClick: (String) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        SystemStatusSummaryCard(
            telemetry = telemetry,
            onRefresh = onRefresh
        )

        // Multi Search Engine Matrix & Auto Speed Router Badge Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = VoidSurface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, VoidBorder, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Multi Search Engine",
                            tint = CosmicCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "MULTI FREE SEARCH ENGINE MATRIX",
                            color = TextGlow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Surface(
                        color = StatusGreen.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StatusGreen.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "⚡ AUTO SPEED ROUTER",
                            color = StatusGreen,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = "Concurrently searching Wikipedia • DuckDuckGo • ArXiv Papers • Wikidata • Google Web Matrix for high-speed complex answers.",
                    color = TextMuted,
                    fontSize = 10.sp,
                    lineHeight = 13.sp
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AssistChip(
                        onClick = { onPromptClick("Search quantum physics wave equations") },
                        label = { Text("🔍 Search Physics", fontSize = 10.sp, color = CosmicCyan) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = GalaxyVoid)
                    )
                    AssistChip(
                        onClick = { onPromptClick("Solve complex calculus integrals step by step") },
                        label = { Text("🔍 Math Solver", fontSize = 10.sp, color = CosmicCyan) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = GalaxyVoid)
                    )
                    AssistChip(
                        onClick = { onPromptClick("Open YouTube") },
                        label = { Text("🚀 Open App", fontSize = 10.sp, color = VioletNeon) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = GalaxyVoid)
                    )
                }
            }
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(VoidSurface)
                .border(1.5.dp, VioletNeon, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "JARVIS Active",
                tint = CosmicCyan,
                modifier = Modifier.size(30.dp)
            )
        }

        Text(
            text = "ONLINE & READY FOR YOU, $userTitle",
            color = TextGlow,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Text(
            text = "Ask me complex math, full algorithm code, system telemetry, file generation, open apps, or server management.",
            color = TextMuted,
            fontSize = 11.sp
        )
    }
}
