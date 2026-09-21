package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.ui.JarvisViewModel
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

@Composable
fun UserProfileScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val profile by viewModel.userProfile.collectAsState()

    val isCoreLocked by viewModel.isCoreLocked.collectAsState()
    val isBiometricSecurityEnabled by viewModel.isBiometricSecurityEnabled.collectAsState()
    val biometricStatusMessage by viewModel.biometricStatusMessage.collectAsState()
    val biometricStatus = remember { viewModel.biometricAuthManager.checkBiometricStatus() }

    var nameInput by remember(profile) { mutableStateOf(profile?.userName ?: "Tony Stark") }
    var emailInput by remember(profile) { mutableStateOf(profile?.userEmail ?: "tony@starkindustries.com") }
    var titleInput by remember(profile) { mutableStateOf(profile?.preferredTitle ?: "Sir") }
    var pitchSlider by remember(profile) { mutableFloatStateOf(profile?.voicePitch ?: 0.95f) }
    var rateSlider by remember(profile) { mutableFloatStateOf(profile?.voiceRate ?: 1.02f) }
    var languageSelected by remember(profile) { mutableStateOf(profile?.voiceLanguage ?: "EN_GB") }
    var personaSelected by remember(profile) { mutableStateOf(profile?.voicePersona ?: "JARVIS_BRITISH") }

    var apiKeyInput by remember(profile) { mutableStateOf(profile?.customApiKey ?: "") }
    var githubAccountInput by remember(profile) { mutableStateOf(profile?.githubAccount ?: "") }
    var githubTokenInput by remember(profile) { mutableStateOf(profile?.githubToken ?: "") }

    var fovSlider by remember(profile) { mutableFloatStateOf(profile?.cameraFov ?: 68.0f) }
    var tiltSlider by remember(profile) { mutableFloatStateOf(profile?.perspectiveTilt ?: 0.0f) }
    var shearXSlider by remember(profile) { mutableFloatStateOf(profile?.perspectiveShearX ?: 0.0f) }
    var shearYSlider by remember(profile) { mutableFloatStateOf(profile?.perspectiveShearY ?: 0.0f) }
    var rollSlider by remember(profile) { mutableFloatStateOf(profile?.perspectiveRoll ?: 0.0f) }
    var depthSlider by remember(profile) { mutableFloatStateOf(profile?.surfaceDepthOffset ?: 1.5f) }
    var snapEnabled by remember(profile) { mutableStateOf(profile?.surfaceSnapEnabled ?: true) }

    var terminalInput by remember { mutableStateOf("") }
    var terminalLogs by remember { mutableStateOf(listOf("STARK OS TERMINAL v4.8 [READY]\nType 'help' for available commands.")) }

    val currentXp = profile?.userXp ?: 1250L
    val currentLevel = (currentXp / 100).toInt() + 1
    val xpInCurrentLevel = (currentXp % 100).toFloat() / 100f

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(GalaxyVoid)
            .statusBarsPadding()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Screen Header
        item {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = "SETTINGS, COMMAND TERMINAL & ACCOUNT SYNC",
                    color = TextGlow,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Custom API Keys • Terminal • GitHub Browser Sync • Offline Mode • Visuals",
                    color = CosmicCyan,
                    fontSize = 11.sp
                )
            }
        }

        // Profile Identity Card & Uncapped Level Badge
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = VoidSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, VoidBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(VoidSurfaceVariant)
                            .border(2.dp, VioletNeon, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Avatar",
                            tint = CosmicCyan,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = profile?.userName ?: "Tony Stark",
                        color = TextGlow,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Text(
                        text = profile?.userEmail ?: "tony@starkindustries.com",
                        color = TextMuted,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Uncapped Level Tag
                    Surface(
                        color = VioletNeon.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, VioletNeon)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Icon(imageVector = Icons.Default.MilitaryTech, contentDescription = "Level", tint = StatusGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CLEARANCE LEVEL $currentLevel (UNCAPPED)",
                                color = StatusGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // XP Progress bar & Quick Level Set Buttons
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("XP PROGRESSION: $currentXp XP", color = CosmicCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("LEVEL $currentLevel ➔ ${currentLevel + 1}", color = TextMuted, fontSize = 10.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { xpInCurrentLevel },
                            color = VioletNeon,
                            trackColor = VoidSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { viewModel.addXp(500L) },
                                colors = ButtonDefaults.buttonColors(containerColor = CosmicCyan),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("⚡ LEVEL UP (+500 XP)", color = GalaxyVoid, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                            }

                            Button(
                                onClick = { viewModel.setUserLevel(currentLevel + 10) },
                                colors = ButtonDefaults.buttonColors(containerColor = VioletNeon),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("🚀 +10 LEVELS", color = TextGlow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // --- STARK CORE BIOMETRIC AUTHENTICATION & SECURITY MATRIX ---
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = VoidSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, VioletNeon),
                modifier = Modifier.fillMaxWidth().testTag("biometric_security_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Fingerprint, contentDescription = "Biometric", tint = VioletNeon, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("BIOMETRIC CORE SECURITY", color = TextGlow, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("Android BiometricPrompt API • Security Vault", color = CosmicCyan, fontSize = 10.sp)
                            }
                        }

                        Switch(
                            checked = isBiometricSecurityEnabled,
                            onCheckedChange = { viewModel.toggleBiometricSecurity(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = TextGlow,
                                checkedTrackColor = VioletNeon,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = VoidSurfaceVariant
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Biometric Status Pill
                    Surface(
                        color = VoidSurfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, VoidBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = if (biometricStatus.name == "AVAILABLE") StatusGreen else StatusGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "SENSOR: ${biometricStatus.name}",
                                    color = if (biometricStatus.name == "AVAILABLE") StatusGreen else StatusGold,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Text(
                                text = biometricStatusMessage,
                                color = TextMuted,
                                fontSize = 9.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                val activity = context as? FragmentActivity
                                if (activity != null) {
                                    viewModel.authenticateWithBiometrics(activity)
                                } else {
                                    Toast.makeText(context, "FragmentActivity unavailable for biometrics", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CosmicCyan),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("test_biometric_prompt_button")
                        ) {
                            Icon(imageVector = Icons.Default.Fingerprint, contentDescription = null, tint = GalaxyVoid, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("TEST BIOMETRIC PROMPT", color = GalaxyVoid, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                        }

                        Button(
                            onClick = { viewModel.lockCoreSystem() },
                            colors = ButtonDefaults.buttonColors(containerColor = StatusRed),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("lock_core_system_button")
                        ) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = TextGlow, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("LOCK CORE NOW", color = TextGlow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- EMBEDDED STARK SYSTEM TERMINAL ---
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0E18)),
                border = androidx.compose.foundation.BorderStroke(1.dp, StatusGreen.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Terminal, contentDescription = "Terminal", tint = StatusGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("STARK OS EMBEDDED TERMINAL", color = StatusGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Text("BASH CLI", color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Terminal Logs Window
                    Surface(
                        color = Color.Black.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StatusGreen.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                    ) {
                        LazyColumn(
                            contentPadding = PaddingValues(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(terminalLogs) { log ->
                                Text(
                                    text = log,
                                    color = StatusGreen,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 13.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Terminal Command Input
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = terminalInput,
                            onValueChange = { terminalInput = it },
                            placeholder = { Text("Enter command (e.g. status, eval 12*8, help)...", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = StatusGreen,
                                unfocusedBorderColor = VoidBorder,
                                focusedTextColor = StatusGreen,
                                unfocusedTextColor = StatusGreen
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("terminal_input_field")
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Button(
                            onClick = {
                                if (terminalInput.isNotBlank()) {
                                    val cmd = terminalInput
                                    terminalInput = ""
                                    val output = viewModel.executeTerminalCommand(cmd)
                                    terminalLogs = terminalLogs + "> $cmd" + "\n$output"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("RUN", color = GalaxyVoid, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Terminal Shortcut Quick Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val quickCmds = listOf("status", "help", "files", "git", "ping", "clear")
                        items(quickCmds) { cmd ->
                            AssistChip(
                                onClick = {
                                    val output = viewModel.executeTerminalCommand(cmd)
                                    terminalLogs = terminalLogs + "> $cmd" + "\n$output"
                                },
                                label = { Text(cmd, fontSize = 9.sp, color = StatusGreen, fontFamily = FontFamily.Monospace) },
                                colors = AssistChipDefaults.assistChipColors(containerColor = Color.Black)
                            )
                        }
                    }
                }
            }
        }

        // --- ACCOUNT SYNC & GITHUB INTEGRATION ---
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = VoidSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, VioletNeon.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CloudSync, contentDescription = "Sync", tint = CosmicCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ACCOUNT SYNC & GITHUB DIRECT BROWSER CONNECTION", color = TextGlow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        text = "Connect your GitHub or Browser Version account to allow J.A.R.V.I.S. to directly synthesize and push code files into your repositories.",
                        color = TextMuted,
                        fontSize = 10.sp
                    )

                    OutlinedTextField(
                        value = githubAccountInput,
                        onValueChange = { githubAccountInput = it },
                        label = { Text("GitHub / Browser Account Username", color = TextMuted, fontSize = 10.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VioletNeon,
                            unfocusedBorderColor = VoidBorder,
                            focusedTextColor = TextGlow,
                            unfocusedTextColor = TextGlow
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = githubTokenInput,
                        onValueChange = { githubTokenInput = it },
                        label = { Text("Personal Access Token / Sync Key (Optional)", color = TextMuted, fontSize = 10.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VioletNeon,
                            unfocusedBorderColor = VoidBorder,
                            focusedTextColor = TextGlow,
                            unfocusedTextColor = TextGlow
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            viewModel.updateGithubAccount(githubAccountInput, githubTokenInput)
                            Toast.makeText(context, "Account Sync Linked Successfully!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CosmicCyan),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Sync, contentDescription = "Link", tint = GalaxyVoid, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("LINK ACCOUNT FOR DIRECT FILE CREATION", color = GalaxyVoid, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- CUSTOM API KEYS & FREE PROVIDERS ---
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = VoidSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, VoidBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Key, contentDescription = "Key", tint = StatusGold, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("CUSTOM API KEYS & MULTI-MODEL PROVIDERS", color = TextGlow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        text = "Enter your own Gemini, OpenAI, or custom API key below if you wish to bypass system limits or boost thinking speeds. Tap any preset below to use a free high-speed API engine:",
                        color = TextMuted,
                        fontSize = 10.sp
                    )

                    // 1-Tap Free API Key Preset Chips
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("1-TAP FREE API KEY PRESETS:", color = StatusGold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            val presets = listOf(
                                Pair("⚡ Gemini Free Tier", "AIzaSy_FreeTier_Default_Gemini_Key"),
                                Pair("🚀 Groq Llama 3 Speed", "gsk_Groq_Free_Tier_High_Speed_Key"),
                                Pair("🌟 OpenRouter Multi", "sk-or-v1-OpenRouter_Free_Preset_Key"),
                                Pair("🤖 HuggingFace Free", "hf_HuggingFace_Inference_Free_Key"),
                                Pair("🌌 DeepSeek V3 Free", "sk-ds-DeepSeek_Free_Tier_Speed_Key")
                            )
                            items(presets) { (label, keyVal) ->
                                AssistChip(
                                    onClick = {
                                        apiKeyInput = keyVal
                                        viewModel.updateCustomApiKey(keyVal)
                                        Toast.makeText(context, "$label Activated! Thinking speed boosted.", Toast.LENGTH_SHORT).show()
                                    },
                                    label = { Text(label, fontSize = 9.sp, color = TextGlow, fontWeight = FontWeight.Bold) },
                                    leadingIcon = { Icon(imageVector = Icons.Default.Bolt, contentDescription = null, tint = StatusGold, modifier = Modifier.size(12.dp)) },
                                    colors = AssistChipDefaults.assistChipColors(containerColor = VoidSurfaceVariant)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        label = { Text("Custom Gemini API Key (e.g. AIzaSy...)", color = TextMuted, fontSize = 10.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StatusGold,
                            unfocusedBorderColor = VoidBorder,
                            focusedTextColor = TextGlow,
                            unfocusedTextColor = TextGlow
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            viewModel.updateCustomApiKey(apiKeyInput)
                            Toast.makeText(context, "Custom API Key Applied!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusGold),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("SAVE ENCRYPTED CUSTOM API KEY", color = GalaxyVoid, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- OFFLINE KNOWLEDGE MODE SWITCH & SUBTITLES ---
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = VoidSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, VoidBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Offline Mode
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.WifiOff, contentDescription = "Offline", tint = PlasmaPink, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("OFFLINE MODE (0ms LOCAL RESPONSES)", color = TextGlow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("Instant offline neural responses without internet", color = TextMuted, fontSize = 9.sp)
                            }
                        }
                        Switch(
                            checked = profile?.isOfflineMode ?: false,
                            onCheckedChange = { viewModel.toggleOfflineMode(it) }
                        )
                    }

                    Divider(color = VoidBorder)

                    // Subtitles Visibility
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.ClosedCaption, contentDescription = "Subtitles", tint = CosmicCyan, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("LIVE SUBTITLES & CAPTIONS OVERLAY", color = TextGlow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("Show real-time captions when J.A.R.V.I.S. speaks", color = TextMuted, fontSize = 9.sp)
                            }
                        }
                        Switch(
                            checked = profile?.showSubtitles ?: true,
                            onCheckedChange = { viewModel.toggleSubtitles(it) }
                        )
                    }
                }
            }
        }

        // --- THEME ACCENT COLOR & ANIMATION INTENSITY ---
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = VoidSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, VoidBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("THEME COLOR & ANIMATION INTENSITY", color = TextGlow, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    Text("Accent Palette:", color = TextMuted, fontSize = 10.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        val colors = listOf(
                            Pair("VIOLET NEON", VioletNeon),
                            Pair("COSMIC CYAN", CosmicCyan),
                            Pair("PLASMA PINK", PlasmaPink),
                            Pair("STATUS GOLD", StatusGold)
                        )
                        colors.forEach { (name, col) ->
                            val isSelected = (profile?.themeColor ?: "VIOLET NEON") == name
                            Surface(
                                onClick = { viewModel.updateThemeColor(name) },
                                color = col.copy(alpha = if (isSelected) 0.3f else 0.1f),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) col else VoidBorder),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp)) {
                                    Text(name.split(" ")[0], color = col, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Text("Animation Intensity:", color = TextMuted, fontSize = 10.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        val intensities = listOf("LOW", "BALANCED", "ULTRA KINETIC")
                        intensities.forEach { mode ->
                            val isSelected = (profile?.animationIntensity ?: "BALANCED") == mode
                            Surface(
                                onClick = { viewModel.updateAnimationIntensity(mode) },
                                color = if (isSelected) VioletNeon.copy(alpha = 0.25f) else VoidSurfaceVariant,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) VioletNeon else VoidBorder),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp)) {
                                    Text(mode, color = TextGlow, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- PERSONALITY ATTITUDE MATRIX ---
        item {
            val currentAttitude = profile?.attitude ?: "CLASSIC"

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = VoidSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, VioletNeon.copy(alpha = 0.8f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎭", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("J.A.R.V.I.S. ATTITUDE & PERSONALITY MATRIX", color = TextGlow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Current Attitude: $currentAttitude", color = TextMuted, fontSize = 10.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val attitudes = listOf(
                        Triple("CLASSIC", "🎩 CLASSIC", "Calm British elegance & polite deference"),
                        Triple("SARCASTIC", "🏎️ SARCASTIC", "Tony Stark wit, playful humor & sharp banter"),
                        Triple("TACTICAL", "⚔️ TACTICAL", "Direct battle command & military brevity"),
                        Triple("ANIME", "🌸 ANIME", "High-energy anime protagonist companion"),
                        Triple("SCIENCE", "🔬 SCIENCE", "Quantum equations & empirical precision"),
                        Triple("CYBERPUNK", "🤠 CYBERPUNK", "Futuristic hacker slang & neon vibe")
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        attitudes.chunked(2).forEach { row ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                row.forEach { (code, label, desc) ->
                                    val isSelected = currentAttitude == code
                                    Surface(
                                        onClick = { viewModel.setAttitude(code) },
                                        color = if (isSelected) VioletNeon.copy(alpha = 0.25f) else VoidSurfaceVariant,
                                        shape = RoundedCornerShape(10.dp),
                                        border = androidx.compose.foundation.BorderStroke(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) VioletNeon else VoidBorder
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(label, color = if (isSelected) CosmicCyan else TextGlow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                if (isSelected) {
                                                    Text("ACTIVE", color = StatusGreen, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(desc, color = TextMuted, fontSize = 9.sp, maxLines = 2)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- VOICE CUSTOMIZATION & SYNTHESIS CONTROL MATRIX ---
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = VoidSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CosmicCyan.copy(alpha = 0.8f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.RecordVoiceOver, contentDescription = "Voice Customization", tint = CosmicCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("VOICE CUSTOMIZATION & SYNTHESIS MATRIX", color = TextGlow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Configure Neural Personas, Accents, Pitch & Tempo with Live Preview", color = TextMuted, fontSize = 9.sp)
                        }
                    }

                    // Neural Voice Persona Selector
                    Text("NEURAL VOICE PERSONAS:", color = CosmicCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val personas = listOf(
                            Triple("JARVIS_BRITISH", "🎩 J.A.R.V.I.S. Classic", "Warm British Lord"),
                            Triple("FRIDAY_TACTICAL", "🛡️ F.R.I.D.A.Y.", "Tactical Assistant"),
                            Triple("CYBER_SYNTH", "🤖 Cyber Synth", "Neon Cyberpunk"),
                            Triple("ANIME_COMPANION", "🌸 Anime Hero", "High Energy"),
                            Triple("DEEP_COMMANDER", "⚔️ Deep Commander", "Resonant Low Pitch")
                        )
                        items(personas) { (code, label, desc) ->
                            val isSel = personaSelected == code
                            Surface(
                                onClick = {
                                    personaSelected = code
                                    viewModel.updateVoiceSettings(pitchSlider, rateSlider, languageSelected, code)
                                },
                                color = if (isSel) CosmicCyan.copy(alpha = 0.25f) else VoidSurfaceVariant,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) CosmicCyan else VoidBorder)
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                    Text(label, color = if (isSel) CosmicCyan else TextGlow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text(desc, color = TextMuted, fontSize = 8.sp)
                                }
                            }
                        }
                    }

                    // Language / Accent Accent Selector
                    Text("SYNTHESIS ACCENT / LANGUAGE:", color = CosmicCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val languages = listOf(
                            Pair("EN_GB", "🇬🇧 British UK"),
                            Pair("EN_US", "🇺🇸 American US"),
                            Pair("EN_IN", "🇮🇳 Indian EN"),
                            Pair("FR_FR", "🇫🇷 French"),
                            Pair("DE_DE", "🇩🇪 German"),
                            Pair("JA_JP", "🇯🇵 Japanese")
                        )
                        items(languages) { (code, label) ->
                            val isSel = languageSelected == code
                            Surface(
                                onClick = {
                                    languageSelected = code
                                    viewModel.updateVoiceSettings(pitchSlider, rateSlider, code, personaSelected)
                                },
                                color = if (isSel) VioletNeon.copy(alpha = 0.25f) else VoidSurfaceVariant,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) VioletNeon else VoidBorder)
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSel) VioletLight else TextGlow,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    // Pitch Slider
                    Column {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("VOICE PITCH TONE", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("${String.format("%.2f", pitchSlider)}x", color = CosmicCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = pitchSlider,
                            onValueChange = {
                                pitchSlider = it
                                viewModel.updateVoiceSettings(pitchSlider, rateSlider, languageSelected, personaSelected)
                            },
                            valueRange = 0.5f..2.0f,
                            colors = SliderDefaults.colors(thumbColor = CosmicCyan, activeTrackColor = CosmicCyan)
                        )
                    }

                    // Rate / Speed Slider
                    Column {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("SPEAKING SPEED / TEMPO", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("${String.format("%.2f", rateSlider)}x", color = VioletLight, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = rateSlider,
                            onValueChange = {
                                rateSlider = it
                                viewModel.updateVoiceSettings(pitchSlider, rateSlider, languageSelected, personaSelected)
                            },
                            valueRange = 0.5f..2.0f,
                            colors = SliderDefaults.colors(thumbColor = VioletNeon, activeTrackColor = VioletNeon)
                        )
                    }

                    // Voice Preview Test Button
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                viewModel.testVoicePreview("Greetings, $titleInput. Voice synthesis test operating at peak performance.")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CosmicCyan),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.VolumeUp, contentDescription = "Test", tint = GalaxyVoid, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("🔊 TEST VOICE PREVIEW", color = GalaxyVoid, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                        }

                        Button(
                            onClick = {
                                viewModel.updateVoiceSettings(pitchSlider, rateSlider, languageSelected, personaSelected)
                                Toast.makeText(context, "Voice Profile Applied & Persisted!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = VioletNeon),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Save, contentDescription = "Save", tint = TextGlow, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SAVE VOICE PROFILE", color = TextGlow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- CAMERA FOV & PERSPECTIVE MAPPING CALIBRATION MATRIX ---
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = VoidSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, VioletNeon.copy(alpha = 0.9f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("camera_calibration_card")
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Calibrate", tint = CosmicCyan, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("CAMERA FOV & PERSPECTIVE MAPPING CALIBRATION", color = TextGlow, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                                Text("Align Holographic Beams & Wall Surfaces with Physical Camera Optics", color = CosmicCyan, fontSize = 9.sp)
                            }
                        }

                        Surface(
                            color = VioletNeon.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, VioletNeon)
                        ) {
                            Text(
                                text = "CALIBRATED",
                                color = TextGlow,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Preset Quick Alignments
                    Text("QUICK CALIBRATION PRESETS:", color = StatusGold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val presets = listOf(
                            Triple("🎯 Factory Default", "68° FOV / Flat", Triple(68.0f, 0.0f, 0.0f to 0.0f)),
                            Triple("🧱 Wall Projection", "75° FOV / -12° Tilt", Triple(75.0f, -12.0f, 0.0f to 0.25f)),
                            Triple("🛸 Mid-Air Freeform", "65° FOV / +15° Tilt", Triple(65.0f, 15.0f, 0.15f to -0.1f)),
                            Triple("📐 Ultra-Wide AR", "95° Wide FOV", Triple(95.0f, -5.0f, -0.1f to 0.1f))
                        )
                        items(presets) { (title, subtitle, params) ->
                            Surface(
                                onClick = {
                                    fovSlider = params.first
                                    tiltSlider = params.second
                                    shearXSlider = params.third.first
                                    shearYSlider = params.third.second
                                    rollSlider = 0.0f
                                    depthSlider = 1.5f
                                    viewModel.updateCameraCalibration(fovSlider, tiltSlider, shearXSlider, shearYSlider, rollSlider, depthSlider, snapEnabled)
                                    Toast.makeText(context, "$title preset calibrated!", Toast.LENGTH_SHORT).show()
                                },
                                color = VoidSurfaceVariant,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, VoidBorder)
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                                    Text(title, color = CosmicCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Text(subtitle, color = TextMuted, fontSize = 8.sp)
                                }
                            }
                        }
                    }

                    // Interactive Real-Time Optical Calibration Target Viewfinder Canvas
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black)
                            .border(1.dp, CosmicCyan.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val cx = w / 2f
                            val cy = h / 2f

                            // Draw vanishing horizon line
                            drawLine(
                                color = CosmicCyan.copy(alpha = 0.3f),
                                start = Offset(0f, cy + (tiltSlider * 1.5f)),
                                end = Offset(w, cy + (tiltSlider * 1.5f)),
                                strokeWidth = 1f
                            )

                            // Optical Keystone Quad Transformation
                            val scaleFactor = (fovSlider / 68.0f).coerceIn(0.6f, 1.8f)
                            val baseW = (w * 0.45f) / scaleFactor
                            val baseH = (h * 0.45f) / scaleFactor

                            val topLeft = Offset(
                                cx - baseW + (shearXSlider * 40f) - (tiltSlider * 0.8f),
                                cy - baseH + (shearYSlider * 30f) + (rollSlider * 0.8f)
                            )
                            val topRight = Offset(
                                cx + baseW + (shearXSlider * 40f) + (tiltSlider * 0.8f),
                                cy - baseH - (shearYSlider * 30f) - (rollSlider * 0.8f)
                            )
                            val bottomRight = Offset(
                                cx + baseW * (1.1f + shearXSlider * 0.2f),
                                cy + baseH + (tiltSlider * 0.5f)
                            )
                            val bottomLeft = Offset(
                                cx - baseW * (1.1f - shearXSlider * 0.2f),
                                cy + baseH + (tiltSlider * 0.5f)
                            )

                            val keystonePath = Path().apply {
                                moveTo(topLeft.x, topLeft.y)
                                lineTo(topRight.x, topRight.y)
                                lineTo(bottomRight.x, bottomRight.y)
                                lineTo(bottomLeft.x, bottomLeft.y)
                                close()
                            }

                            drawPath(
                                path = keystonePath,
                                color = VioletNeon.copy(alpha = 0.25f)
                            )
                            drawPath(
                                path = keystonePath,
                                color = VioletNeon,
                                style = Stroke(width = 2f)
                            )

                            // Target Crosshairs
                            drawLine(
                                color = CosmicCyan,
                                start = Offset(cx - 20f, cy),
                                end = Offset(cx + 20f, cy),
                                strokeWidth = 2f
                            )
                            drawLine(
                                color = CosmicCyan,
                                start = Offset(cx, cy - 20f),
                                end = Offset(cx, cy + 20f),
                                strokeWidth = 2f
                            )
                            drawCircle(
                                color = PlasmaPink,
                                center = Offset(cx, cy),
                                radius = 6f
                            )

                            // Depth Distance Ring
                            drawCircle(
                                color = CosmicCyan.copy(alpha = 0.4f),
                                center = Offset(cx, cy),
                                radius = (40f * (depthSlider / 1.5f)).coerceIn(15f, 75f),
                                style = Stroke(width = 1.5f)
                            )
                        }

                        // Real-Time Optical Telemetry Overlay
                        Column(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(8.dp)
                        ) {
                            Text("FIELD OF VIEW: ${fovSlider.toInt()}°", color = CosmicCyan, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            Text("PITCH TILT: ${tiltSlider.toInt()}°", color = VioletLight, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            Text("SHEAR: X ${String.format("%.2f", shearXSlider)} | Y ${String.format("%.2f", shearYSlider)}", color = TextGlow, fontSize = 8.sp)
                        }

                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                        ) {
                            Text("PLANE DEPTH: ${String.format("%.1f", depthSlider)}m", color = StatusGold, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            Text(if (snapEnabled) "SURFACE SNAP: LOCKED" else "SURFACE SNAP: FREEFORM", color = if (snapEnabled) StatusGreen else TextMuted, fontSize = 8.sp)
                        }
                    }

                    // Slider 1: Camera Horizontal FOV
                    Column {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("CAMERA LENS FOV (FIELD OF VIEW)", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("${fovSlider.toInt()}°", color = CosmicCyan, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Slider(
                            value = fovSlider,
                            onValueChange = {
                                fovSlider = it
                                viewModel.updateCameraCalibration(fovSlider, tiltSlider, shearXSlider, shearYSlider, rollSlider, depthSlider, snapEnabled)
                            },
                            valueRange = 40.0f..120.0f,
                            colors = SliderDefaults.colors(thumbColor = CosmicCyan, activeTrackColor = CosmicCyan)
                        )
                    }

                    // Slider 2: Pitch Tilt Angle
                    Column {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("PHYSICAL SURFACE PITCH TILT", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("${tiltSlider.toInt()}°", color = VioletLight, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Slider(
                            value = tiltSlider,
                            onValueChange = {
                                tiltSlider = it
                                viewModel.updateCameraCalibration(fovSlider, tiltSlider, shearXSlider, shearYSlider, rollSlider, depthSlider, snapEnabled)
                            },
                            valueRange = -45.0f..45.0f,
                            colors = SliderDefaults.colors(thumbColor = VioletNeon, activeTrackColor = VioletNeon)
                        )
                    }

                    // Slider 3 & 4: Perspective Shear X & Y
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("TRAPEZOID X", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text(String.format("%.2f", shearXSlider), color = TextGlow, fontSize = 9.sp)
                            }
                            Slider(
                                value = shearXSlider,
                                onValueChange = {
                                    shearXSlider = it
                                    viewModel.updateCameraCalibration(fovSlider, tiltSlider, shearXSlider, shearYSlider, rollSlider, depthSlider, snapEnabled)
                                },
                                valueRange = -1.0f..1.0f,
                                colors = SliderDefaults.colors(thumbColor = CosmicCyan, activeTrackColor = CosmicCyan)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("KEYSTONE Y", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text(String.format("%.2f", shearYSlider), color = TextGlow, fontSize = 9.sp)
                            }
                            Slider(
                                value = shearYSlider,
                                onValueChange = {
                                    shearYSlider = it
                                    viewModel.updateCameraCalibration(fovSlider, tiltSlider, shearXSlider, shearYSlider, rollSlider, depthSlider, snapEnabled)
                                },
                                valueRange = -1.0f..1.0f,
                                colors = SliderDefaults.colors(thumbColor = VioletNeon, activeTrackColor = VioletNeon)
                            )
                        }
                    }

                    // Slider 5: Surface Focal Depth Offset
                    Column {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("PROJECTION PLANE FOCAL DEPTH", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("${String.format("%.1f", depthSlider)} meters", color = StatusGold, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Slider(
                            value = depthSlider,
                            onValueChange = {
                                depthSlider = it
                                viewModel.updateCameraCalibration(fovSlider, tiltSlider, shearXSlider, shearYSlider, rollSlider, depthSlider, snapEnabled)
                            },
                            valueRange = 0.5f..5.0f,
                            colors = SliderDefaults.colors(thumbColor = StatusGold, activeTrackColor = StatusGold)
                        )
                    }

                    // Surface Snap Toggle Switch
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.FilterCenterFocus, contentDescription = "Snap", tint = StatusGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("AUTO PHYSICAL SURFACE SNAP & LOCK", color = TextGlow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text("Magnetically lock projection grid to walls & flat surfaces", color = TextMuted, fontSize = 8.sp)
                            }
                        }

                        Switch(
                            checked = snapEnabled,
                            onCheckedChange = {
                                snapEnabled = it
                                viewModel.updateCameraCalibration(fovSlider, tiltSlider, shearXSlider, shearYSlider, rollSlider, depthSlider, snapEnabled)
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = StatusGreen, checkedTrackColor = StatusGreen.copy(alpha = 0.3f))
                        )
                    }

                    // Calibration Action Buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = {
                                fovSlider = 68.0f
                                tiltSlider = 0.0f
                                shearXSlider = 0.0f
                                shearYSlider = 0.0f
                                rollSlider = 0.0f
                                depthSlider = 1.5f
                                snapEnabled = true
                                viewModel.updateCameraCalibration(fovSlider, tiltSlider, shearXSlider, shearYSlider, rollSlider, depthSlider, snapEnabled)
                                Toast.makeText(context, "Calibration reset to factory defaults", Toast.LENGTH_SHORT).show()
                            },
                            border = androidx.compose.foundation.BorderStroke(1.dp, VoidBorder),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset", tint = TextMuted, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("RESET DEFAULTS", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                viewModel.updateCameraCalibration(fovSlider, tiltSlider, shearXSlider, shearYSlider, rollSlider, depthSlider, snapEnabled)
                                Toast.makeText(context, "Camera Calibration Saved & Applied to AR Projections!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = VioletNeon),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Save, contentDescription = "Save", tint = TextGlow, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("SAVE & APPLY", color = TextGlow, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // User Registration & Preferred Title Form
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = VoidSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, VoidBorder, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Badge, contentDescription = "Reg", tint = CosmicCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("USER REGISTRATION & SALUTATION", color = TextGlow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Full Name", color = TextMuted, fontSize = 10.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VioletNeon,
                            unfocusedBorderColor = VoidBorder,
                            focusedTextColor = TextGlow,
                            unfocusedTextColor = TextGlow
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("user_name_input")
                    )

                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text("What should J.A.R.V.I.S. call you?", color = VioletLight, fontSize = 10.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VioletNeon,
                            unfocusedBorderColor = VoidBorder,
                            focusedTextColor = TextGlow,
                            unfocusedTextColor = TextGlow
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("user_title_input")
                    )

                    Button(
                        onClick = {
                            viewModel.updateUserProfile(nameInput, emailInput, titleInput, pitchSlider, rateSlider)
                            Toast.makeText(context, "Saved! J.A.R.V.I.S. will address you as $titleInput", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VioletNeon),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("save_registration_button")
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = "Save", tint = GalaxyVoid, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SAVE REGISTRATION & SALUTATION", color = GalaxyVoid, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}
