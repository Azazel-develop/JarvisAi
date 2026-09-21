package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.data.model.LanguageRegistry
import com.example.ui.JarvisViewModel
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val profile by viewModel.userProfile.collectAsState()
    val isCoreLocked by viewModel.isCoreLocked.collectAsState()
    val isBiometricSecurityEnabled by viewModel.isBiometricSecurityEnabled.collectAsState()
    val biometricStatusMessage by viewModel.biometricStatusMessage.collectAsState()
    val biometricStatus = remember { viewModel.biometricAuthManager.checkBiometricStatus() }

    val currentLangCode = profile?.voiceLanguage ?: "EN_GB"
    val currentLang = remember(currentLangCode) { LanguageRegistry.findLanguageByCode(currentLangCode) }
    val isWakeWordRunning by viewModel.isWakeWordServiceRunning.collectAsState()
    val hasOverlayPermission = remember(context) { com.example.service.FloatingArcReactorManager.canDrawOverlays(context) }

    var selectedPersona by remember(profile) { mutableStateOf(profile?.voicePersona ?: "JARVIS_BRITISH") }
    var voicePitch by remember(profile) { mutableStateOf(profile?.voicePitch ?: 0.95f) }
    var voiceRate by remember(profile) { mutableStateOf(profile?.voiceRate ?: 1.02f) }
    var apiKeyInput by remember(profile) { mutableStateOf(profile?.customApiKey ?: "") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(GalaxyVoid)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- HEADER BANNER ---
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = VoidSurface),
                border = BorderStroke(1.dp, CosmicCyan),
                modifier = Modifier.fillMaxWidth().testTag("settings_header_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(CosmicCyan.copy(alpha = 0.2f))
                                    .border(1.dp, CosmicCyan, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = CosmicCyan,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "SYSTEM SETTINGS & WAKE-WORD",
                                    color = TextGlow,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "Neural Matrix • Acoustic Triggers • Arc Reactor",
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        // Active Language Badge
                        Surface(
                            color = VoidSurfaceVariant,
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, VioletNeon)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(text = currentLang.flagEmoji, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = currentLang.code,
                                    color = TextGlow,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- BACKGROUND WAKE-WORD & FLOATING ARC REACTOR CARD ---
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = VoidSurface),
                border = BorderStroke(1.5.dp, CosmicCyan),
                modifier = Modifier.fillMaxWidth().testTag("wakeword_service_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(CosmicCyan.copy(alpha = 0.2f))
                                    .border(1.dp, CosmicCyan, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = "Wake Word",
                                    tint = CosmicCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "ALWAYS-ON WAKE-WORD MATRIX",
                                    color = TextGlow,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "Detects 'Jarvis' even outside the app",
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Switch(
                            checked = isWakeWordRunning,
                            onCheckedChange = { enable ->
                                viewModel.toggleWakeWordService(context, enable)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = GalaxyVoid,
                                checkedTrackColor = CosmicCyan,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = VoidSurfaceVariant
                            ),
                            modifier = Modifier.testTag("wakeword_service_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "• Runs a real-time background listener service.\n• When 'Jarvis' is spoken, J.A.R.V.I.S. responds \"Yes ${profile?.preferredTitle ?: "Sir"}\" and a tiny glowing Arc Reactor appears in the corner of your screen.",
                        color = TextMuted,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Test Arc Reactor Overlay Button
                        Button(
                            onClick = {
                                viewModel.testFloatingArcReactorOverlay(context)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CosmicCyan),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("test_arc_reactor_overlay_button")
                        ) {
                            Icon(imageVector = Icons.Default.Visibility, contentDescription = null, tint = GalaxyVoid, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "TEST ARC REACTOR OVERLAY",
                                color = GalaxyVoid,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        // Request Draw Over Apps Permission Button
                        if (!hasOverlayPermission) {
                            OutlinedButton(
                                onClick = {
                                    com.example.service.FloatingArcReactorManager.requestOverlayPermission(context)
                                },
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, VioletNeon),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .testTag("grant_overlay_permission_button")
                            ) {
                                Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, tint = VioletNeon, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "GRANT OVERLAY PERMISSION",
                                    color = VioletNeon,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- LANGUAGE SELECTION MATRIX ---
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = VoidSurface),
                border = BorderStroke(1.dp, VioletNeon),
                modifier = Modifier.fillMaxWidth().testTag("language_settings_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Language, contentDescription = "Language", tint = VioletNeon, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("GLOBAL LANGUAGE MATRIX", color = TextGlow, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        Text(
                            text = "${currentLang.flagEmoji} ${currentLang.nativeName}",
                            color = CosmicCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Select primary language for Text-to-Speech (TTS), Voice Recognition, and Gemini Neural Responses:",
                        color = TextMuted,
                        fontSize = 10.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Language Grid (12 Languages)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        LanguageRegistry.ALL_LANGUAGES.chunked(2).forEach { rowLangs ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                rowLangs.forEach { lang ->
                                    val isSelected = lang.code == currentLangCode
                                    Surface(
                                        color = if (isSelected) VioletNeon.copy(alpha = 0.25f) else VoidSurfaceVariant,
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(
                                            1.dp,
                                            if (isSelected) VioletNeon else VoidBorder
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                viewModel.setLanguage(lang.code)
                                                Toast.makeText(context, "Language switched to ${lang.displayName}", Toast.LENGTH_SHORT).show()
                                            }
                                            .testTag("lang_select_${lang.code}")
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                        ) {
                                            Text(text = lang.flagEmoji, fontSize = 16.sp)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = lang.displayName,
                                                    color = if (isSelected) TextGlow else TextMuted,
                                                    fontSize = 10.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                                Text(
                                                    text = lang.nativeName,
                                                    color = if (isSelected) CosmicCyan else TextMuted.copy(alpha = 0.7f),
                                                    fontSize = 9.sp
                                                )
                                            }
                                        }
                                    }
                                }
                                if (rowLangs.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Test Language Voice Button
                    Button(
                        onClick = {
                            viewModel.speechEngine.speak(currentLang.greetingConfirmation)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CosmicCyan),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .testTag("test_language_voice_button")
                    ) {
                        Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null, tint = GalaxyVoid, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "TEST VOICE SYNTHESIS (${currentLang.code})",
                            color = GalaxyVoid,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        // --- VOICE DIRECTIVES & COMMAND GUIDE CARD ---
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = VoidSurface),
                border = BorderStroke(1.dp, VoidBorder),
                modifier = Modifier.fillMaxWidth().testTag("voice_commands_guide_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Mic, contentDescription = "Mic Directives", tint = StatusGold, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("VOICE & CHAT LANGUAGE DIRECTIVES", color = TextGlow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val commandExamples = listOf(
                        "🗣️ Voice: \"Switch language to Spanish\"",
                        "🗣️ Voice: \"Change language to French\"",
                        "🗣️ Voice: \"Speak in German\"",
                        "🗣️ Voice: \"Set language to Japanese\"",
                        "💬 Chat Command: /language spanish",
                        "💬 Chat Command: /lang fr",
                        "💬 Chat Command: /language list"
                    )

                    commandExamples.forEach { ex ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 3.dp)
                        ) {
                            Text(text = ex, color = CosmicCyan, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }

        // --- SPEECH SYNTHESIS & VOICE PERSONAS ---
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = VoidSurface),
                border = BorderStroke(1.dp, VoidBorder),
                modifier = Modifier.fillMaxWidth().testTag("voice_personas_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.RecordVoiceOver, contentDescription = "Voice", tint = VioletNeon, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SPEECH SYNTHESIS & PERSONA", color = TextGlow, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Voice Personas Chips
                    val personas = listOf(
                        "JARVIS_BRITISH" to "Classic British",
                        "FRIDAY_TACTICAL" to "FRIDAY Tactical",
                        "CYBER_SYNTH" to "Cyber Synth",
                        "ANIME_COMPANION" to "Anime Companion",
                        "DEEP_COMMANDER" to "Deep Commander"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        personas.take(3).forEach { (key, label) ->
                            val isSelected = selectedPersona == key
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedPersona = key
                                    viewModel.updateUserProfile(
                                        userName = profile?.userName ?: "Tony Stark",
                                        userEmail = profile?.userEmail ?: "tony@starkindustries.com",
                                        preferredTitle = profile?.preferredTitle ?: "Sir",
                                        voicePitch = voicePitch,
                                        voiceRate = voiceRate
                                    )
                                },
                                label = { Text(label, fontSize = 9.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = VioletNeon,
                                    selectedLabelColor = TextGlow
                                ),
                                modifier = Modifier.testTag("persona_chip_$key")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Pitch Slider
                    Text(text = "Voice Pitch: ${String.format("%.2f", voicePitch)}x", color = TextMuted, fontSize = 10.sp)
                    Slider(
                        value = voicePitch,
                        onValueChange = {
                            voicePitch = it
                            viewModel.speechEngine.updatePitchAndRate(voicePitch, voiceRate)
                        },
                        onValueChangeFinished = {
                            viewModel.updateUserProfile(
                                userName = profile?.userName ?: "Tony Stark",
                                userEmail = profile?.userEmail ?: "tony@starkindustries.com",
                                preferredTitle = profile?.preferredTitle ?: "Sir",
                                voicePitch = voicePitch,
                                voiceRate = voiceRate
                            )
                        },
                        valueRange = 0.5f..1.5f,
                        colors = SliderDefaults.colors(thumbColor = CosmicCyan, activeTrackColor = CosmicCyan)
                    )

                    // Tempo / Rate Slider
                    Text(text = "Speaking Tempo / Rate: ${String.format("%.2f", voiceRate)}x", color = TextMuted, fontSize = 10.sp)
                    Slider(
                        value = voiceRate,
                        onValueChange = {
                            voiceRate = it
                            viewModel.speechEngine.updatePitchAndRate(voicePitch, voiceRate)
                        },
                        onValueChangeFinished = {
                            viewModel.updateUserProfile(
                                userName = profile?.userName ?: "Tony Stark",
                                userEmail = profile?.userEmail ?: "tony@starkindustries.com",
                                preferredTitle = profile?.preferredTitle ?: "Sir",
                                voicePitch = voicePitch,
                                voiceRate = voiceRate
                            )
                        },
                        valueRange = 0.7f..1.6f,
                        colors = SliderDefaults.colors(thumbColor = VioletNeon, activeTrackColor = VioletNeon)
                    )
                }
            }
        }

        // --- BIOMETRIC SECURITY VAULT ---
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = VoidSurface),
                border = BorderStroke(1.dp, VoidBorder),
                modifier = Modifier.fillMaxWidth().testTag("biometric_vault_settings_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Shield, contentDescription = "Biometric", tint = VioletNeon, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("BIOMETRIC CORE VAULT", color = TextGlow, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        Switch(
                            checked = isBiometricSecurityEnabled,
                            onCheckedChange = { viewModel.toggleBiometricSecurity(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = TextGlow,
                                checkedTrackColor = VioletNeon
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Status: $biometricStatusMessage (Hardware: ${biometricStatus.name})",
                        color = if (biometricStatus.name == "AVAILABLE") StatusGreen else StatusGold,
                        fontSize = 10.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val activity = context as? FragmentActivity
                                if (activity != null) {
                                    viewModel.authenticateWithBiometrics(activity)
                                } else {
                                    Toast.makeText(context, "FragmentActivity not available", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CosmicCyan),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("settings_test_biometric_button")
                        ) {
                            Icon(imageVector = Icons.Default.Fingerprint, contentDescription = null, tint = GalaxyVoid, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("SCAN BIOMETRICS", color = GalaxyVoid, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                        }

                        Button(
                            onClick = { viewModel.lockCoreSystem() },
                            colors = ButtonDefaults.buttonColors(containerColor = StatusRed),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("settings_lock_core_button")
                        ) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = TextGlow, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("LOCK MATRIX", color = TextGlow, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- CUSTOM GEMINI API KEY & DEV MODE ---
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = VoidSurface),
                border = BorderStroke(1.dp, VoidBorder),
                modifier = Modifier.fillMaxWidth().testTag("api_key_settings_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Key, contentDescription = "API Key", tint = StatusGold, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("CUSTOM GEMINI API KEY", color = TextGlow, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Optionally inject your personal Gemini API key or use system default environment credentials:", color = TextMuted, fontSize = 10.sp)

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        singleLine = true,
                        placeholder = { Text("AIzaSy...", color = TextMuted, fontSize = 10.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CosmicCyan,
                            unfocusedBorderColor = VoidBorder,
                            focusedTextColor = TextGlow,
                            unfocusedTextColor = TextGlow
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("custom_api_key_field")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            viewModel.updateUserProfile(
                                userName = profile?.userName ?: "Tony Stark",
                                userEmail = profile?.userEmail ?: "tony@starkindustries.com",
                                preferredTitle = profile?.preferredTitle ?: "Sir",
                                voicePitch = voicePitch,
                                voiceRate = voiceRate
                            )
                            Toast.makeText(context, "Settings & API Credentials Saved!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VioletNeon),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(42.dp).testTag("save_settings_button")
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null, tint = TextGlow, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SAVE ALL SYSTEM CONFIGURATIONS", color = TextGlow, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}
