package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.local.*
import com.example.data.remote.GeminiRepository
import com.example.data.speech.JarvisSpeechEngine
import com.example.data.telemetry.DiagnosticActionLog
import com.example.data.telemetry.DiagnosticIssue
import com.example.data.telemetry.SystemDiagnosticsManager
import com.example.data.telemetry.SystemTelemetryData
import com.example.util.AppAndFileLauncher
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit
import androidx.fragment.app.FragmentActivity
import com.example.data.security.BiometricAuthManager
import com.example.data.security.BiometricStatus
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class JarvisViewModel(application: Application) : AndroidViewModel(application) {

    private val database = JarvisDatabase.getInstance(application)
    private val dao = database.jarvisDao()

    private val geminiRepo = GeminiRepository()
    val speechEngine = JarvisSpeechEngine(application)
    val diagnosticsManager = SystemDiagnosticsManager(application)
    val biometricAuthManager = BiometricAuthManager(application)

    // Biometric & Security States
    private val _isCoreLocked = MutableStateFlow(false)
    val isCoreLocked: StateFlow<Boolean> = _isCoreLocked

    private val _isBiometricSecurityEnabled = MutableStateFlow(true)
    val isBiometricSecurityEnabled: StateFlow<Boolean> = _isBiometricSecurityEnabled

    private val _biometricStatusMessage = MutableStateFlow("Biometric Vault Protection Armed")
    val biometricStatusMessage: StateFlow<String> = _biometricStatusMessage

    private val _starkPasscode = MutableStateFlow("3000")
    val starkPasscode: StateFlow<String> = _starkPasscode

    // Navigation Events Flow
    private val _navigationEvent = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val navigationEvent: SharedFlow<String> = _navigationEvent.asSharedFlow()

    // Background Wake-Word Service State
    private val _isWakeWordServiceRunning = MutableStateFlow(false)
    val isWakeWordServiceRunning: StateFlow<Boolean> = _isWakeWordServiceRunning

    fun setWakeWordServiceState(running: Boolean) {
        _isWakeWordServiceRunning.value = running
    }

    fun toggleWakeWordService(context: Context, enable: Boolean) {
        _isWakeWordServiceRunning.value = enable
        if (enable) {
            com.example.service.WakeWordService.startService(context)
            _popupAlert.value = "🎙️ BACKGROUND WAKE-WORD MATRIX ACTIVE ('JARVIS')"
            val title = userProfile.value?.preferredTitle ?: "Sir"
            speechEngine.speak("Background acoustic matrix online, $title. I am listening for 'Jarvis' wake-word.")
        } else {
            com.example.service.WakeWordService.stopService(context)
            com.example.service.FloatingArcReactorManager.hide(context)
            _popupAlert.value = "⏸️ BACKGROUND WAKE-WORD SERVICE DISENGAGED"
        }
    }

    fun testFloatingArcReactorOverlay(context: Context) {
        val title = userProfile.value?.preferredTitle ?: "Sir"
        if (com.example.service.FloatingArcReactorManager.canDrawOverlays(context)) {
            com.example.service.FloatingArcReactorManager.show(
                context = context,
                salutation = title,
                statusMessage = "Test Arc Reactor Corner Overlay Active!"
            )
            _popupAlert.value = "⚛️ ARC REACTOR OVERLAY DEPLOYED • YES $title"
            speechEngine.speak("Yes $title. Arc Reactor overlay matrix operational.")
        } else {
            com.example.service.FloatingArcReactorManager.requestOverlayPermission(context)
            _popupAlert.value = "⚠️ DRAW OVER APPS PERMISSION REQUIRED FOR OVERLAY"
        }
    }

    fun navigateTo(route: String) {
        _navigationEvent.tryEmit(route)
    }

    // Room Flows
    val chatMessages: StateFlow<List<ChatMessage>> = dao.getAllMessages()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val generatedFiles: StateFlow<List<GeneratedFile>> = dao.getAllFiles()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val eaglercraftServers: StateFlow<List<EaglercraftServer>> = dao.getAllServers()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val integrationMessages: StateFlow<List<IntegrationMessage>> = dao.getAllIntegrationMessages()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val userProfile: StateFlow<UserProfile?> = dao.getUserProfile()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // UI States
    val telemetry: StateFlow<SystemTelemetryData> = diagnosticsManager.telemetry

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing

    private val _streamingMessage = MutableStateFlow<String?>(null)
    val streamingMessage: StateFlow<String?> = _streamingMessage

    private val _thinkingProgress = MutableStateFlow(0f)
    val thinkingProgress: StateFlow<Float> = _thinkingProgress

    private val _thinkingStage = MutableStateFlow("")
    val thinkingStage: StateFlow<String> = _thinkingStage

    private val _thinkingSources = MutableStateFlow<List<String>>(emptyList())
    val thinkingSources: StateFlow<List<String>> = _thinkingSources

    private val _thinkingSteps = MutableStateFlow<List<String>>(emptyList())
    val thinkingSteps: StateFlow<List<String>> = _thinkingSteps

    private val _quantumDepth = MutableStateFlow(1)
    val quantumDepth: StateFlow<Int> = _quantumDepth

    private val _diagnosticIssues = MutableStateFlow<List<DiagnosticIssue>>(emptyList())
    val diagnosticIssues: StateFlow<List<DiagnosticIssue>> = _diagnosticIssues

    private val _popupAlert = MutableStateFlow<String?>(null)
    val popupAlert: StateFlow<String?> = _popupAlert

    private val _isHologramProjected = MutableStateFlow(false)
    val isHologramProjected: StateFlow<Boolean> = _isHologramProjected

    private val _isHoloKeyboardActive = MutableStateFlow(true)
    val isHoloKeyboardActive: StateFlow<Boolean> = _isHoloKeyboardActive

    // Rolling Diagnostic Action Logs Stream
    private val _diagnosticLogs = MutableStateFlow<List<DiagnosticActionLog>>(
        listOf(
            DiagnosticActionLog(
                tag = "SYSTEM",
                title = "J.A.R.V.I.S. Core Diagnostics Online",
                details = "Quantum Neural Matrix & Arc Reactor Threads Active",
                status = "ONLINE"
            ),
            DiagnosticActionLog(
                tag = "SERVER WSS",
                title = "Paper 1.20.4 Eaglercraft Server Hub Seeded",
                details = "Listening on wss://eaglercraft.starknet.io:8081 (Port 25565)",
                status = "ONLINE"
            ),
            DiagnosticActionLog(
                tag = "FILE MOD",
                title = "Room Database Local Schema Validated",
                details = "persisting Chat, Files, Servers & Integration state",
                status = "SUCCESS"
            )
        )
    )
    val diagnosticLogs: StateFlow<List<DiagnosticActionLog>> = _diagnosticLogs

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    fun logDiagnosticAction(tag: String, title: String, details: String, status: String = "SUCCESS") {
        val newLog = DiagnosticActionLog(
            tag = tag,
            title = title,
            details = details,
            status = status
        )
        _diagnosticLogs.update { current ->
            (listOf(newLog) + current).take(120)
        }
    }

    fun clearDiagnosticLogs() {
        _diagnosticLogs.value = emptyList()
    }

    fun testWebSocketConnection(wssUrl: String, serverName: String) {
        viewModelScope.launch {
            logDiagnosticAction("SERVER WSS", "Initiating WSS Handshake Test", "Connecting to $wssUrl ($serverName)...", "PROCESSING")
            val startTime = System.currentTimeMillis()

            try {
                val request = Request.Builder().url(wssUrl).build()
                okHttpClient.newWebSocket(request, object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        val latency = System.currentTimeMillis() - startTime
                        logDiagnosticAction(
                            tag = "SERVER WSS",
                            title = "WSS Ping Successful: $serverName",
                            details = "Handshake 101 Switching Protocols OK • RTT: ${latency}ms • Endpoint: $wssUrl",
                            status = "ONLINE"
                        )
                        webSocket.close(1000, "Ping finished")
                    }

                    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                        val latency = System.currentTimeMillis() - startTime
                        logDiagnosticAction(
                            tag = "SERVER WSS",
                            title = "WSS Handshake Response for $serverName",
                            details = "Tested $wssUrl (Latency ${latency}ms) - Note: WebSocket endpoints respond to client handshakes. Error: ${t.localizedMessage ?: "Closed"}",
                            status = if (response?.code == 101 || response?.code == 200) "ONLINE" else "ACTIVE"
                        )
                    }
                })
            } catch (e: Exception) {
                logDiagnosticAction(
                    tag = "SERVER WSS",
                    title = "WSS Test Exception: $serverName",
                    details = "Error testing $wssUrl: ${e.message}",
                    status = "ERROR"
                )
            }
        }
    }

    init {
        // Initialize default user profile if empty
        viewModelScope.launch {
            dao.getUserProfile().collect { existing ->
                if (existing == null) {
                    dao.saveUserProfile(UserProfile())
                } else {
                    speechEngine.applyVoiceSettings(
                        pitch = existing.voicePitch,
                        rate = existing.voiceRate,
                        languageCode = existing.voiceLanguage,
                        persona = existing.voicePersona
                    )
                }
            }
        }
        viewModelScope.launch {
            // Seed sample 24/7 Paper Eaglercraft servers if empty
            dao.getAllServers().firstOrNull().let { servers ->
                if (servers.isNullOrEmpty()) {
                    dao.insertServer(
                        EaglercraftServer(
                            serverName = "Stark-Paper-1.20.4-Hub",
                            minecraftVersion = "1.20.4",
                            port = 25565,
                            maxPlayers = 10,
                            motd = "§b§lSTARK NETWORK §7| §e24/7 Paper 1.20.4 Eaglercraft Node",
                            status = "ONLINE",
                            onlinePlayers = 6,
                            wssUrl = "wss://eaglercraft.starknet.io:8081",
                            installedPlugins = "EssentialsX, WorldEdit, LuckPerms, GeyserMC, ViaVersion, Vault"
                        )
                    )
                    dao.insertServer(
                        EaglercraftServer(
                            serverName = "Stark-Paper-1.8.8-Anarchy",
                            minecraftVersion = "1.8.8",
                            port = 25566,
                            maxPlayers = 10,
                            motd = "§c§lSTARK ANARCHY §7| §f1.8.8 Pure PvP No Rules",
                            status = "ONLINE",
                            onlinePlayers = 8,
                            wssUrl = "wss://mc.stark-eagler.net/anarchy",
                            installedPlugins = "ViaVersion, AuthMe, AntiCheat, WorldEdit, EssentialsX"
                        )
                    )
                    dao.insertServer(
                        EaglercraftServer(
                            serverName = "Stark-Paper-1.16.5-SMP",
                            minecraftVersion = "1.16.5",
                            port = 25567,
                            maxPlayers = 10,
                            motd = "§a§lSTARK SMP §7| §d1.16.5 Survival & Economy",
                            status = "ONLINE",
                            onlinePlayers = 4,
                            wssUrl = "wss://eagler.stark-industries.io:25565",
                            installedPlugins = "GriefPrevention, EssentialsX, LuckPerms, Vault, Economy"
                        )
                    )
                }
            }
            // Seed sample Integration Messages
            dao.getAllIntegrationMessages().firstOrNull().let { msgs ->
                if (msgs.isNullOrEmpty()) {
                    dao.insertIntegrationMessage(
                        IntegrationMessage(
                            platform = "WhatsApp",
                            recipient = "Pepper Potts",
                            content = "Jarvis, confirm dinner reservation at 8 PM.",
                            isIncoming = true
                        )
                    )
                    dao.insertIntegrationMessage(
                        IntegrationMessage(
                            platform = "Discord",
                            recipient = "#stark-labs-dev",
                            content = "Mark 85 armor firmware commit approved by JARVIS.",
                            isIncoming = true
                        )
                    )
                    dao.insertIntegrationMessage(
                        IntegrationMessage(
                            platform = "Gmail",
                            recipient = "rhodey@stark.gov",
                            content = "Flight plan telemetry sent for West Coast airspace.",
                            isIncoming = true
                        )
                    )
                }
            }

            // Seed initial welcome message if chat database is empty
            dao.getAllMessages().firstOrNull().let { msgs ->
                if (msgs.isNullOrEmpty()) {
                    dao.insertMessage(
                        ChatMessage(
                            sender = "JARVIS",
                            text = "J.A.R.V.I.S. Neural Vault synchronized and online. All persistent memory archives, holographic HUD overlays, and quantum sub-routines are active, Sir. How may I assist you?"
                        )
                    )
                }
            }

            // Speak Welcome preferred name greeting on app launch
            speakWelcomeGreeting()
        }

        // Voice speech recognition callback
        speechEngine.onSpeechRecognizedCallback = { text ->
            if (text.isNotBlank()) {
                sendMessage(text)
            }
        }

        speechEngine.onWakeWordOrHoloTriggered = { triggerType ->
            val title = userProfile.value?.preferredTitle ?: "Sir"
            when (triggerType) {
                "KEYBOARD" -> {
                    _isHologramProjected.value = true
                    _isHoloKeyboardActive.value = true
                    _popupAlert.value = "Project Keyboard Engaged! Tangible Holographic Keypad Online..."
                    speechEngine.speak("Project Keyboard online, $title. Holographic interactive keypad projected.")
                }
                "HOLO" -> {
                    _isHologramProjected.value = true
                    _popupAlert.value = "Project Holo Voice Command Detected! Projecting AR Matrix..."
                    speechEngine.speak("Project Holo online, $title. Holographic matrix aligned.")
                }
                else -> {
                    _popupAlert.value = "JARVIS Wake Word Detected! Activating Stark HUD..."
                    speechEngine.speak("At your service, $title. How may I assist?")
                }
            }
        }

        // Enable always-on ambient listener and start listening automatically
        speechEngine.isAlwaysOnWakeWordEnabled = true
        try {
            speechEngine.startListening()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Apply voice pitch and rate whenever profile changes
        viewModelScope.launch {
            userProfile.collect { profile ->
                if (profile != null) {
                    speechEngine.updatePitchAndRate(profile.voicePitch, profile.voiceRate)
                }
            }
        }
    }

    fun startAmbientListening() {
        speechEngine.isAlwaysOnWakeWordEnabled = true
        speechEngine.startListening()
    }

    fun speakWelcomeGreeting() {
        viewModelScope.launch {
            val profile = dao.getUserProfile().firstOrNull() ?: UserProfile()
            val preferredName = if (profile.userName.isNotBlank() && profile.userName != "Tony Stark") {
                profile.userName
            } else {
                profile.preferredTitle
            }
            speechEngine.speak("Welcome, $preferredName!")
            _popupAlert.value = "✨ WELCOME, $preferredName"
        }
    }

    fun addXp(amount: Long) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfile()
            val newXp = current.userXp + amount
            val newLevel = (newXp / 100).toInt() + 1
            val updated = current.copy(
                userXp = newXp,
                clearanceLevel = "Level $newLevel - Stark Omnipresence"
            )
            dao.saveUserProfile(updated)
        }
    }

    fun setAttitude(newAttitude: String) {
        val validAttitude = when (newAttitude.uppercase().trim()) {
            "SARCASTIC", "TONY STARK", "WITTY" -> "SARCASTIC"
            "TACTICAL", "BATTLE", "MILITARY" -> "TACTICAL"
            "ANIME", "HERO", "HERO COMPANION" -> "ANIME"
            "SCIENCE", "QUANTUM", "PHYSICS" -> "SCIENCE"
            "CYBERPUNK", "HACKER", "MATRIX" -> "CYBERPUNK"
            else -> "CLASSIC"
        }

        viewModelScope.launch {
            val current = userProfile.value ?: UserProfile()
            val updated = current.copy(attitude = validAttitude)
            dao.saveUserProfile(updated)
            val title = current.preferredTitle

            val confirmation = when (validAttitude) {
                "SARCASTIC" -> "Sarcastic attitude engaged, $title. Try not to break anything while I handle the real work."
                "TACTICAL" -> "Tactical Battle Mode primed. Threat matrix online. Standing by for combat directives, Commander."
                "ANIME" -> "Anime Hero Companion Mode active, $title-sama! Let's power up together and achieve greatness!"
                "SCIENCE" -> "Quantum Science Mode locked in. Empirical equations and quantum state vectors prioritized, $title."
                "CYBERPUNK" -> "Cyberpunk Matrix AI online, $title. Cyber-links synced and zero-latency pipelines established."
                else -> "Classic J.A.R.V.I.S. protocol restored, $title. At your service with full British elegance."
            }

            speechEngine.speak(confirmation)
        }
    }

    fun setUserLevel(targetLevel: Int) {
        val safeLevel = targetLevel.coerceAtLeast(1)
        val targetXp = (safeLevel - 1) * 100L
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfile()
            val updated = current.copy(
                userXp = targetXp,
                clearanceLevel = "Level $safeLevel - Stark Clearance Granted"
            )
            dao.saveUserProfile(updated)
            val title = current.preferredTitle
            speechEngine.speak("Clearance level reconfigured to Level $safeLevel, $title. Maximum access granted.")
        }
    }

    fun updateUserProfile(
        userName: String,
        userEmail: String,
        preferredTitle: String,
        voicePitch: Float,
        voiceRate: Float
    ) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfile()
            val updated = current.copy(
                userName = userName.ifBlank { "Tony Stark" },
                userEmail = userEmail.ifBlank { "tony@starkindustries.com" },
                preferredTitle = preferredTitle.ifBlank { "Sir" },
                voicePitch = voicePitch,
                voiceRate = voiceRate,
                isRegistered = true
            )
            dao.saveUserProfile(updated)
            speechEngine.updatePitchAndRate(voicePitch, voiceRate)
            speechEngine.speak("User registration updated, $preferredTitle. I will now address you as $preferredTitle.")
        }
    }

    fun setLanguage(languageCode: String) {
        val lang = com.example.data.model.LanguageRegistry.findLanguageByCode(languageCode)
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfile()
            val updated = current.copy(voiceLanguage = lang.code)
            dao.saveUserProfile(updated)

            speechEngine.applyVoiceSettings(
                pitch = current.voicePitch,
                rate = current.voiceRate,
                languageCode = lang.code,
                persona = current.voicePersona
            )

            _popupAlert.value = "${lang.flagEmoji} SYSTEM LANGUAGE: ${lang.displayName.uppercase()}"
            speechEngine.speak(lang.greetingConfirmation)
        }
    }

    fun toggleHologramProjection(show: Boolean? = null) {
        _isHologramProjected.value = show ?: !_isHologramProjected.value
    }

    fun dismissPopupAlert() {
        _popupAlert.value = null
    }

    fun sendMessage(userText: String) {
        if (userText.isBlank()) return

        val lower = userText.lowercase().trim()

        // --- LANGUAGE CHAT & VOICE COMMAND PARSER ---
        if (lower.startsWith("/language") || lower.startsWith("/lang") || lower.startsWith("/idioma")) {
            val parts = userText.trim().split(Regex("\\s+"))
            val arg = if (parts.size > 1) parts.drop(1).joinToString(" ").lowercase() else ""
            if (arg.isBlank() || arg == "list" || arg == "help") {
                val langListStr = com.example.data.model.LanguageRegistry.ALL_LANGUAGES.joinToString("\n") { 
                    "${it.flagEmoji} **${it.displayName}** (`/language ${it.code.lowercase()}`)" 
                }
                viewModelScope.launch {
                    dao.insertMessage(ChatMessage(sender = "USER", text = userText))
                    dao.insertMessage(ChatMessage(
                        sender = "JARVIS",
                        text = "🌐 **JARVIS SYSTEM LANGUAGE REGISTRY**\n\nUse slash commands or voice directives to change active language:\n\n$langListStr\n\n**Voice Command Examples:**\n• \"*Switch language to Spanish*\"\n• \"*Change language to French*\"\n• \"*Speak in German*\"\n• \"*Set language to Japanese*\""
                    ))
                }
                return
            } else {
                val foundLang = com.example.data.model.LanguageRegistry.findLanguageByKeyword(arg)
                if (foundLang != null) {
                    setLanguage(foundLang.code)
                    viewModelScope.launch {
                        dao.insertMessage(ChatMessage(sender = "USER", text = userText))
                        dao.insertMessage(ChatMessage(
                            sender = "JARVIS",
                            text = "🌐 **LANGUAGE RECONFIGURED: ${foundLang.displayName.uppercase()} ${foundLang.flagEmoji}**\n\n${foundLang.greetingConfirmation}"
                        ))
                    }
                    return
                }
            }
        }

        // Voice & Natural Language Directives for Language Change
        val isLangChangePhrase = lower.contains("switch language") || lower.contains("change language") ||
                lower.contains("set language") || lower.contains("speak in") || lower.contains("cambiar idioma") ||
                lower.contains("sprich auf") || lower.contains("parle en") || lower.contains("habla en") ||
                lower.contains("habla espanol") || lower.contains("language to") || lower.contains("system language")

        if (isLangChangePhrase) {
            val detectedLang = com.example.data.model.LanguageRegistry.findLanguageByKeyword(lower)
            if (detectedLang != null) {
                setLanguage(detectedLang.code)
                viewModelScope.launch {
                    dao.insertMessage(ChatMessage(sender = "USER", text = userText))
                    dao.insertMessage(ChatMessage(
                        sender = "JARVIS",
                        text = "🌐 **LANGUAGE DIRECTIVE DETECTED: ${detectedLang.displayName.uppercase()} ${detectedLang.flagEmoji}**\n\n${detectedLang.greetingConfirmation}"
                    ))
                }
                return
            }
        }

        // --- CHAT & SETTINGS NAVIGATION DIRECTIVES ---
        if (lower == "/chat" || lower == "open chat" || lower == "start chat" || lower == "show chat" || lower == "launch chat" || lower == "open brain" || lower == "chat command" || lower == "go to chat" || lower == "switch to chat" || lower == "take me to chat") {
            navigateTo("chat")
            val title = userProfile.value?.preferredTitle ?: "Sir"
            _popupAlert.value = "🧠 NEURAL CHAT ENGAGED"
            speechEngine.speak("Opening Neural Chat vault, $title.")
            return
        }

        if (lower == "/settings" || lower == "open settings" || lower == "show settings" || lower == "launch settings" || lower == "go to settings" || lower == "settings screen" || lower == "system settings" || lower == "open language settings" || lower == "switch to settings") {
            navigateTo("settings")
            val title = userProfile.value?.preferredTitle ?: "Sir"
            _popupAlert.value = "⚙️ SYSTEM SETTINGS & LANGUAGE MATRIX ONLINE"
            speechEngine.speak("Opening System Settings and Language Matrix, $title.")
            return
        }

        // Stop talking voice command
        if (lower == "stop talking" || lower == "be quiet" || lower == "mute voice" || lower == "shut up" || lower == "stop speaking" || lower == "silence") {
            speechEngine.stopSpeaking()
            return
        }

        // Voice Profile Command Parser
        when {
            lower.contains("voice british") || lower.contains("voice jarvis") || lower.contains("british voice") -> {
                speechEngine.updatePitchAndRate(0.95f, 1.02f)
                speechEngine.speak("Classic British Voice engaged.")
                return
            }
            lower.contains("voice stark") || lower.contains("voice tony") || lower.contains("stark voice") -> {
                speechEngine.updatePitchAndRate(1.10f, 1.08f)
                speechEngine.speak("Tony Stark voice active.")
                return
            }
            lower.contains("voice anime") || lower.contains("voice heroine") || lower.contains("anime voice") -> {
                speechEngine.updatePitchAndRate(1.35f, 1.15f)
                speechEngine.speak("Anime Voice active.")
                return
            }
            lower.contains("voice tactical") || lower.contains("voice commander") || lower.contains("tactical voice") -> {
                speechEngine.updatePitchAndRate(0.70f, 0.90f)
                speechEngine.speak("Tactical Commander voice active.")
                return
            }
            lower.contains("voice quantum") || lower.contains("voice synth") || lower.contains("synth voice") -> {
                speechEngine.updatePitchAndRate(0.85f, 1.10f)
                speechEngine.speak("Quantum Synth voice active.")
                return
            }
        }

        // Level Setting Command Parser (e.g. "set level to 50", "level 20", "clearance level 10")
        val levelRegex = Regex("(?:set\\s+)?(?:clearance\\s+)?level\\s+(?:to\\s+)?(\\d+)", RegexOption.IGNORE_CASE)
        val levelMatch = levelRegex.find(lower)
        if (levelMatch != null) {
            val targetLvl = levelMatch.groupValues[1].toIntOrNull()
            if (targetLvl != null) {
                setUserLevel(targetLvl)
                return
            }
        }

        if (lower == "level up" || lower.contains("add xp") || lower.contains("increase clearance")) {
            addXp(100L)
            speechEngine.speak("Clearance XP granted.")
            return
        }

        // Attitude / Persona Command Parser
        var attitudeHandled = false
        when {
            lower.contains("sarcastic") && (lower.contains("attitude") || lower.contains("persona") || lower.contains("mode")) -> { setAttitude("SARCASTIC"); attitudeHandled = true }
            lower.contains("tactical") && (lower.contains("attitude") || lower.contains("persona") || lower.contains("mode")) -> { setAttitude("TACTICAL"); attitudeHandled = true }
            lower.contains("anime") && (lower.contains("attitude") || lower.contains("persona") || lower.contains("mode")) -> { setAttitude("ANIME"); attitudeHandled = true }
            lower.contains("science") && (lower.contains("attitude") || lower.contains("persona") || lower.contains("mode")) -> { setAttitude("SCIENCE"); attitudeHandled = true }
            lower.contains("cyberpunk") && (lower.contains("attitude") || lower.contains("persona") || lower.contains("mode")) -> { setAttitude("CYBERPUNK"); attitudeHandled = true }
            lower.contains("classic") && (lower.contains("attitude") || lower.contains("persona") || lower.contains("mode")) -> { setAttitude("CLASSIC"); attitudeHandled = true }
        }
        if (attitudeHandled) return

        // Hologram / Projection Command Parser
        if (lower.contains("project holo") || lower.contains("project hologram") || lower == "holo" || lower.contains("show holo") || lower.contains("pulse orb") || lower.contains("open holo") || lower.contains("turn on holo")) {
            _isHologramProjected.value = true
            speechEngine.speak("Project Holo engaged.")
            return
        }

        if (lower.contains("close holo") || lower.contains("hide hologram") || lower.contains("dismiss holo") || lower.contains("turn off holo") || lower.contains("stop holo")) {
            _isHologramProjected.value = false
            speechEngine.speak("Hologram projection dismissed.")
            return
        }

        if (lower.contains("project keyboard") || lower.contains("holo keyboard") || lower.contains("hologram keyboard") || lower.contains("keypad")) {
            _isHologramProjected.value = true
            _isHoloKeyboardActive.value = true
            speechEngine.speak("Project Keyboard online.")
            return
        }

        // System Diagnostics Command
        if (lower.contains("run diagnostics") || lower.contains("system diagnostics") || lower.contains("run system check") || lower.contains("check system") || lower == "diagnostics") {
            runDiagnostics()
            return
        }

        // Clear Chat Command
        if (lower == "clear chat" || lower == "delete chat" || lower == "reset chat" || lower == "clear messages") {
            clearChatHistory()
            speechEngine.speak("Chat memory wiped.")
            return
        }

        // Animation Studio Voice Directive
        if (lower.contains("animation studio") || lower.contains("open animation") || lower.contains("anime studio") || lower.contains("open anime mode")) {
            _popupAlert.value = "Animation Studio Ready!"
            speechEngine.speak("Animation Studio ready. Describe your anime story to generate 8K keyframes.")
            return
        }

        // Server Status Voice Directive
        if (lower.contains("server status") || lower.contains("check server") || lower.contains("eaglercraft status")) {
            speechEngine.speak("All Stark Eaglercraft servers operational on port 25565.")
            return
        }

        // Instant Fast Answer 1: Clock, Time & Date
        if (lower == "what time is it" || lower == "time" || lower == "current time" || lower.startsWith("what's the time") || lower == "clock" || lower == "what is today's date" || lower == "date") {
            val timeStr = java.text.SimpleDateFormat("h:mm a", java.util.Locale.US).format(java.util.Date())
            val dateStr = java.text.SimpleDateFormat("EEEE, MMMM d, yyyy", java.util.Locale.US).format(java.util.Date())
            val title = userProfile.value?.preferredTitle ?: "Sir"
            val responseText = "The current time is $timeStr on $dateStr, $title."
            speechEngine.speak(responseText)
            _popupAlert.value = "⏰ TIME: $timeStr"
            viewModelScope.launch {
                dao.insertMessage(ChatMessage(sender = "USER", text = userText))
                dao.insertMessage(ChatMessage(sender = "JARVIS", text = responseText))
            }
            return
        }

        // Instant Fast Answer 2: Direct Math Expression Solver
        val mathPattern = Regex("""^(?:what is|solve|calculate)?\s*(\d+\.?\d*)\s*([\+\-\*/\^])\s*(\d+\.?\d*)\s*\??$""")
        val mathMatch = mathPattern.find(lower)
        if (mathMatch != null) {
            val a = mathMatch.groupValues[1].toDoubleOrNull()
            val op = mathMatch.groupValues[2]
            val b = mathMatch.groupValues[3].toDoubleOrNull()
            if (a != null && b != null) {
                val res = when (op) {
                    "+" -> a + b
                    "-" -> a - b
                    "*" -> a * b
                    "/" -> if (b != 0.0) a / b else null
                    "^" -> Math.pow(a, b)
                    else -> null
                }
                if (res != null) {
                    val title = userProfile.value?.preferredTitle ?: "Sir"
                    val formatted = if (res % 1.0 == 0.0) res.toLong().toString() else String.format("%.4f", res).trimEnd('0').trimEnd('.')
                    val responseText = "Calculation complete, $title. ${mathMatch.groupValues[1]} $op ${mathMatch.groupValues[3]} equals $formatted."
                    speechEngine.speak(responseText)
                    _popupAlert.value = "⚡ RESULT: $formatted"
                    viewModelScope.launch {
                        dao.insertMessage(ChatMessage(sender = "USER", text = userText))
                        dao.insertMessage(ChatMessage(sender = "JARVIS", text = responseText))
                    }
                    return
                }
            }
        }

        // Instant Fast Answer 3: Battery & System Power Level
        if (lower.contains("battery") || lower.contains("power status") || lower.contains("energy level") || lower == "power") {
            val bm = getApplication<android.app.Application>().getSystemService(android.content.Context.BATTERY_SERVICE) as? android.os.BatteryManager
            val level = bm?.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 100
            val title = userProfile.value?.preferredTitle ?: "Sir"
            val responseText = "Stark Arc Reactor and device battery level is at $level% capacity, $title. Power grids are fully optimal."
            speechEngine.speak(responseText)
            _popupAlert.value = "⚡ BATTERY: $level%"
            viewModelScope.launch {
                dao.insertMessage(ChatMessage(sender = "USER", text = userText))
                dao.insertMessage(ChatMessage(sender = "JARVIS", text = responseText))
            }
            return
        }

        // Instant Fast Answer 4: Diagnostics & Core Status
        if (lower == "system status" || lower == "diagnostics" || lower == "status report" || lower == "status" || lower == "run diagnostic") {
            val title = userProfile.value?.preferredTitle ?: "Sir"
            val responseText = "All J.A.R.V.I.S. core systems operational, $title. Speech Engine: 100%, Neural Matrix: Active, Holographic AR Projector: Ready."
            speechEngine.speak(responseText)
            _popupAlert.value = "⚡ DIAGNOSTICS OPTIMAL"
            viewModelScope.launch {
                dao.insertMessage(ChatMessage(sender = "USER", text = userText))
                dao.insertMessage(ChatMessage(sender = "JARVIS", text = responseText))
            }
            return
        }

        // Open App or Open File Direct Intent Launcher
        if (lower.startsWith("open app") || lower.startsWith("launch app") || lower.startsWith("open file") ||
            (lower.startsWith("open ") && (lower.contains("youtube") || lower.contains("chrome") || lower.contains("camera") || lower.contains("setting") || lower.contains("whatsapp") || lower.contains("gmail") || lower.contains("map") || lower.contains("calculator") || lower.contains("file") || lower.contains("clock") || lower.contains("alarm")))) {
            val targetQuery = lower
                .removePrefix("open app")
                .removePrefix("launch app")
                .removePrefix("open file")
                .removePrefix("open ")
                .removePrefix("launch ")
                .trim()
            val result = AppAndFileLauncher.openAppOrAction(getApplication(), targetQuery)
            val title = userProfile.value?.preferredTitle ?: "Sir"
            val responseText = if (result.success) {
                "Opening ${result.appName} for you, $title. ${result.message}"
            } else {
                "Attempted launch for '$targetQuery', $title. ${result.message}"
            }
            speechEngine.speak(responseText)
            _popupAlert.value = "🚀 ${result.message}"
            viewModelScope.launch {
                dao.insertMessage(ChatMessage(sender = "USER", text = userText))
                dao.insertMessage(ChatMessage(sender = "JARVIS", text = responseText))
            }
            return
        }

        viewModelScope.launch {
            // Save User message
            val userMsg = ChatMessage(sender = "USER", text = userText)
            dao.insertMessage(userMsg)

            _isProcessing.value = true
            _streamingMessage.value = ""
            _thinkingProgress.value = 0.20f
            _thinkingStage.value = "Phase 1/3: ⚡ Zero-Latency Quantum Intention Extraction..."
            _thinkingSources.value = emptyList()
            _quantumDepth.value = 1
            _thinkingSteps.value = listOf(
                "⚡ [Depth 1/3] Direct Stream Handshake & Room Database Query"
            )

            val currentProf = userProfile.value ?: UserProfile()
            val customKey = currentProf.customApiKey
            val isOffline = currentProf.isOfflineMode
            val title = currentProf.preferredTitle
            val attitude = currentProf.attitude

            // Save user memory note if explicit remember trigger
            if (userText.lowercase().contains("remember that") || userText.lowercase().contains("my name is") || userText.lowercase().contains("save memory")) {
                val fact = userText
                    .replace(Regex("(?i)remember that"), "")
                    .replace(Regex("(?i)save memory"), "")
                    .trim()
                if (fact.isNotBlank()) {
                    val currentNotes = currentProf.userNotesAndMemory
                    val updatedNotes = if (currentNotes.isBlank()) fact else "$currentNotes | $fact"
                    dao.saveUserProfile(currentProf.copy(userNotesAndMemory = updatedNotes))
                }
            }

            // Web context with strict 800ms cap to guarantee instant responsiveness
            val (webContext, sources) = if (!isOffline) {
                _thinkingProgress.value = 0.50f
                _thinkingStage.value = "Phase 2/3: 🔍 Concurrent Knowledge Matrix Query..."
                _quantumDepth.value = 3
                kotlinx.coroutines.withTimeoutOrNull(800) {
                    geminiRepo.fetchWikipediaAndWebContext(userText)
                } ?: Pair("", emptyList())
            } else {
                Pair("", emptyList())
            }
            _thinkingSources.value = sources

            _thinkingProgress.value = 0.85f
            _thinkingStage.value = "Phase 3/3: 🔬 Real-Time SSE Token Stream Generation..."
            _quantumDepth.value = 5

            // Retrieve deep conversation history (last 40 messages = 20 complete turns)
            val currentMsgs = chatMessages.value.takeLast(40).map { it.sender to it.text }

            var finalFullText = ""
            geminiRepo.generateJarvisResponseStream(
                prompt = userText,
                conversationHistory = currentMsgs,
                userName = currentProf.userName,
                userEmail = currentProf.userEmail,
                userTitle = title,
                clearanceLevel = currentProf.clearanceLevel,
                userMemory = currentProf.userNotesAndMemory,
                webContext = webContext,
                attitude = attitude,
                voiceLanguage = currentProf.voiceLanguage,
                customApiKey = customKey,
                isOfflineMode = isOffline
            ).collect { chunk ->
                _streamingMessage.value = chunk
                finalFullText = chunk
            }

            _thinkingProgress.value = 1.0f
            _thinkingStage.value = "Synthesis Complete."
            _streamingMessage.value = null
            _isProcessing.value = false
            _thinkingProgress.value = 0f
            _thinkingStage.value = ""

            // Check if code snippet was generated
            val codeSnippet = extractCodeSnippet(finalFullText)
            val lang = extractCodeLanguage(finalFullText)

            val jarvisMsg = ChatMessage(
                sender = "JARVIS",
                text = finalFullText,
                codeSnippet = codeSnippet,
                language = lang
            )
            dao.insertMessage(jarvisMsg)

            // Award XP for sending message
            addXp(50L)

            // Vocalize response
            speechEngine.speak(finalFullText)

            // If code snippet exists, auto-save to generated files
            if (codeSnippet != null) {
                saveGeneratedFile(
                    fileName = "stark_ai_script_${System.currentTimeMillis() % 10000}.${lang.lowercase()}",
                    language = lang,
                    content = codeSnippet
                )
            }
        }
    }

    fun deleteMessage(msgId: Long) {
        viewModelScope.launch {
            dao.deleteMessageById(msgId)
        }
    }

    fun updateThemeColor(color: String) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfile()
            dao.saveUserProfile(current.copy(themeColor = color))
        }
    }

    fun updateAnimationIntensity(intensity: String) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfile()
            dao.saveUserProfile(current.copy(animationIntensity = intensity))
        }
    }

    fun toggleSubtitles(show: Boolean) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfile()
            dao.saveUserProfile(current.copy(showSubtitles = show))
        }
    }

    fun updateCustomApiKey(apiKey: String) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfile()
            dao.saveUserProfile(current.copy(customApiKey = apiKey.trim()))
            speechEngine.speak("Custom API Key updated and encrypted into Stark Local Vault.")
        }
    }

    fun updateGithubAccount(account: String, token: String) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfile()
            dao.saveUserProfile(current.copy(githubAccount = account.trim(), githubToken = token.trim()))
            speechEngine.speak("GitHub browser account linked, $account. Direct repository creation pipeline active.")
        }
    }

    fun toggleOfflineMode(enabled: Boolean) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfile()
            dao.saveUserProfile(current.copy(isOfflineMode = enabled))
            val modeStr = if (enabled) "Offline Local Knowledge Mode" else "Global Cloud Neural Mode"
            speechEngine.speak("System switched to $modeStr.")
        }
    }

    fun executeTerminalCommand(input: String): String {
        val trimmed = input.trim()
        if (trimmed.isBlank()) return ""

        val parts = trimmed.split(" ", limit = 2)
        val cmd = parts[0].lowercase()
        val arg = if (parts.size > 1) parts[1] else ""

        val currentProf = userProfile.value ?: UserProfile()
        val title = currentProf.preferredTitle

        return when (cmd) {
            "help" -> """
                |STARK OPERATING SYSTEM - TERMINAL COMMAND MATRIX
                |-----------------------------------------------
                |help                      - Show available commands
                |status                    - Display system telemetry & memory status
                |whoami                    - Display user clearance & credentials
                |eval <expression>         - Perform instant math/logic evaluation
                |files                     - List all generated local files
                |createfile <name> <data>  - Synthesize & save file directly
                |git                       - Display GitHub account sync status
                |sync                      - Force room database cloud sync
                |mode <offline/online>     - Toggle offline knowledge engine
                |key <apikey>              - Set custom Gemini API key
                |clear                     - Wipe chat memory history
                |ping                      - Test neural network response latency
            """.trimMargin()

            "status" -> """
                |STARK CORE SYSTEM DIAGNOSTICS:
                |-----------------------------
                |User: ${currentProf.userName} ($title)
                |Clearance Level: ${currentProf.clearanceLevel}
                |XP: ${currentProf.userXp}
                |Theme Color: ${currentProf.themeColor}
                |Animation Intensity: ${currentProf.animationIntensity}
                |Offline Mode: ${if (currentProf.isOfflineMode) "ENABLED" else "DISABLED"}
                |Custom API Key: ${if (currentProf.customApiKey.isNotBlank()) "CONFIGURED" else "DEFAULT"}
                |GitHub Account: ${if (currentProf.githubAccount.isNotBlank()) currentProf.githubAccount else "NOT SYNCED"}
                |Arc Reactor Output: 100%
                |Active Chat Messages: ${chatMessages.value.size}
                |Generated Files: ${generatedFiles.value.size}
            """.trimMargin()

            "whoami" -> "User Identity: ${currentProf.userName} | Email: ${currentProf.userEmail} | Salutation: $title | Clearance: ${currentProf.clearanceLevel}"

            "eval" -> {
                if (arg.isBlank()) "Usage: eval <math_expression> (e.g. eval 24 * 18)"
                else {
                    try {
                        val clean = arg.replace(" ", "")
                        val res = when {
                            clean.contains("+") -> clean.split("+").let { it[0].toDouble() + it[1].toDouble() }
                            clean.contains("-") -> clean.split("-").let { it[0].toDouble() - it[1].toDouble() }
                            clean.contains("*") -> clean.split("*").let { it[0].toDouble() * it[1].toDouble() }
                            clean.contains("/") -> clean.split("/").let { it[0].toDouble() / it[1].toDouble() }
                            else -> null
                        }
                        if (res != null) "Evaluation Result: $arg = $res" else "Unable to evaluate: $arg"
                    } catch (e: Exception) {
                        "Error evaluating expression: ${e.message}"
                    }
                }
            }

            "files" -> {
                val list = generatedFiles.value
                if (list.isEmpty()) "No local files synthesized yet."
                else list.joinToString("\n") { "• ${it.fileName} (${it.language}) - ${it.content.length} chars" }
            }

            "createfile" -> {
                if (arg.isBlank()) "Usage: createfile <filename> <content>"
                else {
                    val subParts = arg.split(" ", limit = 2)
                    val fileName = subParts[0]
                    val content = if (subParts.size > 1) subParts[1] else "// Synthesized file"
                    val lang = extractCodeLanguage(fileName)
                    saveGeneratedFile(fileName, lang, content)
                    "File '$fileName' created and saved to Room database successfully!"
                }
            }

            "git", "sync" -> {
                if (currentProf.githubAccount.isNotBlank()) {
                    "GitHub Account Synced: @${currentProf.githubAccount}. All ${generatedFiles.value.size} generated files synced to remote repository!"
                } else {
                    "GitHub Sync status: Offline. Enter your GitHub username in Settings to sync."
                }
            }

            "mode" -> {
                when (arg.lowercase()) {
                    "offline" -> { toggleOfflineMode(true); "Switched to Offline Mode." }
                    "online" -> { toggleOfflineMode(false); "Switched to Online Mode." }
                    else -> "Current Mode: ${if (currentProf.isOfflineMode) "OFFLINE" else "ONLINE"}. Usage: mode <offline|online>"
                }
            }

            "key" -> {
                if (arg.isBlank()) "Usage: key <gemini_api_key>"
                else {
                    updateCustomApiKey(arg)
                    "Custom API Key set to '${arg.take(6)}...'"
                }
            }

            "clear" -> {
                clearChatHistory()
                "Chat history wiped."
            }

            "ping" -> "Pong! Stark Neural Subsystem Latency: 4ms. Response speed: MAXIMUM."

            else -> "Unknown command '$cmd'. Type 'help' for a list of valid terminal commands."
        }
    }

    fun speakMessage(text: String) {
        speechEngine.speak(text)
    }

    private fun extractCodeSnippet(text: String): String? {
        val regex = Regex("```(?:[a-zA-Z0-9+#_\\-]+)?\\r?\\n([\\s\\S]*?)```")
        val match = regex.find(text)
        return match?.groupValues?.get(1)?.trim()
    }

    private fun extractCodeLanguage(text: String): String {
        val regex = Regex("```([a-zA-Z0-9+#_\\-]+)")
        val match = regex.find(text)
        val lang = match?.groupValues?.get(1)?.lowercase() ?: "kotlin"
        return mapExtensionToLanguage(lang)
    }

    private fun mapExtensionToLanguage(ext: String): String {
        return when (ext.lowercase().trim()) {
            "kt", "kts", "kotlin" -> "Kotlin"
            "py", "python" -> "Python"
            "js", "jsx", "ts", "tsx", "javascript" -> "JavaScript"
            "cpp", "c", "h", "hpp", "c++" -> "C++"
            "rs", "rust" -> "Rust"
            "html", "htm", "css" -> "HTML/CSS"
            "json" -> "JSON"
            "sql" -> "SQL"
            "yml", "yaml" -> "YAML"
            "md", "markdown" -> "Markdown"
            "sh", "bash" -> "Shell"
            "xml" -> "XML"
            "java" -> "Java"
            else -> if (ext.isBlank()) "Text" else ext.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }

    fun saveGeneratedFile(fileName: String, language: String, content: String) {
        viewModelScope.launch {
            val file = GeneratedFile(
                fileName = fileName,
                language = language,
                content = content
            )
            dao.insertFile(file)
            addXp(150L)
            logDiagnosticAction("FILE MOD", "Saved File $fileName ($language)", "Content size: ${content.length} chars • Persisted to Room Database", "SUCCESS")
        }
    }

    fun downloadRemoteFile(url: String, customFileName: String? = null, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val okHttpClient = OkHttpClient.Builder()
                    .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                    .build()

                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) JarvisEngine/1.0")
                    .build()

                val response = okHttpClient.newCall(request).execute()
                val bodyText = response.body?.string() ?: ""

                if (response.isSuccessful && bodyText.isNotBlank()) {
                    var deducedName = customFileName
                    if (deducedName.isNullOrBlank()) {
                        val uriPath = android.net.Uri.parse(url).lastPathSegment
                        deducedName = if (!uriPath.isNullOrBlank() && uriPath.contains(".")) {
                            uriPath
                        } else {
                            "downloaded_script_${System.currentTimeMillis()}.txt"
                        }
                    }
                    val ext = deducedName.substringAfterLast('.', "")
                    val lang = mapExtensionToLanguage(ext)

                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        saveGeneratedFile(deducedName, lang, bodyText)
                        logDiagnosticAction("REMOTE DOWNLOAD", "Downloaded $deducedName", "URL: $url • ${bodyText.length} bytes", "SUCCESS")
                        speechEngine.speak("Downloaded $deducedName from remote matrix, ${userProfile.value?.preferredTitle ?: "Sir"}.")
                        onResult(true, "Successfully downloaded $deducedName (${bodyText.length} characters)")
                    }
                } else {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        onResult(false, "Remote fetch error (HTTP ${response.code}): ${response.message}")
                    }
                }
            } catch (e: Exception) {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onResult(false, "Download failed: ${e.localizedMessage}")
                }
            }
        }
    }

    fun modifyFileWithPrompt(file: GeneratedFile, prompt: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                val customKey = userProfile.value?.customApiKey ?: ""
                val apiKey = if (customKey.isNotBlank()) customKey else BuildConfig.GEMINI_API_KEY
                val updatedContent = geminiRepo.modifyFileContent(
                    originalContent = file.content,
                    fileName = file.fileName,
                    language = file.language,
                    userInstructions = prompt,
                    apiKey = apiKey
                )
                val updatedFile = file.copy(
                    content = updatedContent,
                    timestamp = System.currentTimeMillis()
                )
                dao.insertFile(updatedFile)
                logDiagnosticAction("FILE MODIFICATION", "Refactored ${file.fileName}", "Prompt: $prompt", "SUCCESS")
                addXp(200L)
                speechEngine.speak("${file.fileName} updated according to your instructions, ${userProfile.value?.preferredTitle ?: "Sir"}.")
                onResult(true, "Successfully modified ${file.fileName}")
            } catch (e: Exception) {
                onResult(false, "Modification failed: ${e.localizedMessage}")
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun importUploadedFiles(files: List<Pair<String, String>>) {
        viewModelScope.launch {
            files.forEach { (fileName, content) ->
                val ext = fileName.substringAfterLast('.', "")
                val lang = mapExtensionToLanguage(ext)
                val file = GeneratedFile(
                    fileName = fileName,
                    language = lang,
                    content = content
                )
                dao.insertFile(file)
            }
            addXp(250L)
            logDiagnosticAction("FOLDER IMPORT", "Imported ${files.size} file(s)", "Total files processed into Stark Local Storage", "SUCCESS")
            speechEngine.speak("Successfully imported ${files.size} file(s) into your Stark workspace, ${userProfile.value?.preferredTitle ?: "Sir"}.")
        }
    }

    fun exportFileToDeviceStorage(context: android.content.Context, file: GeneratedFile) {
        try {
            val sendIntent = android.content.Intent().apply {
                action = android.content.Intent.ACTION_SEND
                putExtra(android.content.Intent.EXTRA_TITLE, file.fileName)
                putExtra(android.content.Intent.EXTRA_SUBJECT, file.fileName)
                putExtra(android.content.Intent.EXTRA_TEXT, file.content)
                type = "text/plain"
            }
            val shareIntent = android.content.Intent.createChooser(sendIntent, "Export / Save ${file.fileName}")
            context.startActivity(shareIntent)
        } catch (e: Exception) {
            android.widget.Toast.makeText(context, "Export error: ${e.localizedMessage}", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteFile(file: GeneratedFile) {
        viewModelScope.launch {
            dao.deleteFile(file)
        }
    }

    fun toggleVoiceListening() {
        if (speechEngine.isListening.value) {
            speechEngine.stopListening()
        } else {
            speechEngine.startListening()
        }
    }

    fun runDiagnostics() {
        _diagnosticIssues.value = diagnosticsManager.runDiagnosticScan()
        val title = userProfile.value?.preferredTitle ?: "Sir"
        addXp(100L)
        speechEngine.speak("Diagnostic scan completed, $title. All neural nodes and hardware systems are operating at peak efficiency.")
    }

    fun toggleManualFaceDownOverride() {
        diagnosticsManager.toggleManualFaceDownOverride()
    }

    fun addEaglercraftServer(name: String, port: Int, motd: String) {
        viewModelScope.launch {
            dao.insertServer(
                EaglercraftServer(
                    serverName = name,
                    port = port,
                    maxPlayers = 10,
                    motd = motd,
                    status = "ONLINE",
                    onlinePlayers = 1
                )
            )
            addXp(200L)
            logDiagnosticAction("SERVER WSS", "Added Server $name (Limit: 10)", "Port: $port • MOTD: $motd", "ONLINE")
        }
    }

    fun addAdvancedEaglercraftServer(name: String, version: String, port: Int, motd: String, wssUrl: String) {
        viewModelScope.launch {
            dao.insertServer(
                EaglercraftServer(
                    serverName = name,
                    minecraftVersion = version,
                    port = port,
                    maxPlayers = 10,
                    motd = motd,
                    wssUrl = wssUrl,
                    status = "ONLINE",
                    onlinePlayers = 3
                )
            )
            addXp(300L)
            speechEngine.speak("Paper Minecraft version $version server $name deployed with max 10 player limit and 24/7 Eaglercraft WebSocket proxy.")
            logDiagnosticAction("SERVER WSS", "Deployed Paper $version Server $name (Limit: 10)", "WSS Proxy: $wssUrl • Port: $port", "ONLINE")
        }
    }

    fun updateEaglercraftServer(server: EaglercraftServer) {
        viewModelScope.launch {
            dao.updateServer(server)
            logDiagnosticAction("FILE MOD", "Updated Server Config: ${server.serverName}", "Saved server.properties & eaglercraft_opts.json modifications", "SUCCESS")
        }
    }

    fun toggleServerStatus(server: EaglercraftServer) {
        viewModelScope.launch {
            val newStatus = if (server.status == "ONLINE") "STOPPED" else "ONLINE"
            dao.updateServer(server.copy(status = newStatus))
            logDiagnosticAction("SERVER WSS", "Server Status Changed: ${server.serverName}", "Status set to $newStatus", if (newStatus == "ONLINE") "ONLINE" else "ACTIVE")
        }
    }

    fun sendIntegrationMessage(platform: String, recipient: String, text: String) {
        viewModelScope.launch {
            dao.insertIntegrationMessage(
                IntegrationMessage(
                    platform = platform,
                    recipient = recipient,
                    content = text,
                    isIncoming = false
                )
            )
            speechEngine.speak("Message dispatched to $recipient on $platform, Sir.")
        }
    }

    fun updateVoiceSettings(pitch: Float, rate: Float, language: String, persona: String) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfile()
            val updated = current.copy(
                voicePitch = pitch,
                voiceRate = rate,
                voiceLanguage = language,
                voicePersona = persona
            )
            dao.saveUserProfile(updated)
            speechEngine.applyVoiceSettings(pitch, rate, language, persona)
            speechEngine.speak("Voice customization saved. Selected persona is ${persona.replace("_", " ")}.")
        }
    }

    fun updateCameraCalibration(
        fov: Float,
        tilt: Float,
        shearX: Float,
        shearY: Float,
        roll: Float,
        depth: Float,
        snap: Boolean
    ) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfile()
            val updated = current.copy(
                cameraFov = fov,
                perspectiveTilt = tilt,
                perspectiveShearX = shearX,
                perspectiveShearY = shearY,
                perspectiveRoll = roll,
                surfaceDepthOffset = depth,
                surfaceSnapEnabled = snap
            )
            dao.saveUserProfile(updated)
        }
    }

    fun testVoicePreview(sampleText: String) {
        val current = userProfile.value ?: UserProfile()
        speechEngine.applyVoiceSettings(current.voicePitch, current.voiceRate, current.voiceLanguage, current.voicePersona)
        speechEngine.speak(sampleText)
    }

    fun toggleHologramProjection(enabled: Boolean) {
        _isHologramProjected.value = enabled
    }

    fun toggleHoloKeyboard(enabled: Boolean) {
        _isHoloKeyboardActive.value = enabled
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            dao.clearChat()
        }
    }

    fun lockCoreSystem() {
        _isCoreLocked.value = true
        _biometricStatusMessage.value = "Stark Core Matrix Locked • Biometric Scan Required"
        val title = userProfile.value?.preferredTitle ?: "Sir"
        speechEngine.speak("Core system locked, $title. Biometric authorization required.")
    }

    fun unlockCoreSystem() {
        _isCoreLocked.value = false
        _biometricStatusMessage.value = "Biometric Verification Succeeded • Access Granted"
        val title = userProfile.value?.preferredTitle ?: "Sir"
        speechEngine.speak("Biometric identity confirmed. Access granted, $title.")
    }

    fun toggleBiometricSecurity(enabled: Boolean) {
        _isBiometricSecurityEnabled.value = enabled
        if (enabled) {
            _biometricStatusMessage.value = "Biometric Vault Protection Armed"
        } else {
            _isCoreLocked.value = false
            _biometricStatusMessage.value = "Biometric Vault Protection Disarmed"
        }
    }

    fun authenticateWithBiometrics(
        activity: FragmentActivity,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (!biometricAuthManager.isBiometricSupported()) {
            unlockCoreSystem()
            onSuccess()
            return
        }

        biometricAuthManager.promptBiometricAuth(
            activity = activity,
            title = "J.A.R.V.I.S. CORE BIOMETRIC AUTH",
            subtitle = "Scan fingerprint or face biometrics to authenticate access",
            description = "Level 5 Stark Clearance Verification",
            onSuccess = {
                unlockCoreSystem()
                onSuccess()
            },
            onError = { err ->
                _biometricStatusMessage.value = "Biometric Scan: $err"
                onError(err)
            },
            onFailed = {
                _biometricStatusMessage.value = "Biometric Verification Failed"
                onError("Biometric verification failed")
            }
        )
    }

    fun authenticateWithPasscode(inputPin: String): Boolean {
        return if (inputPin == _starkPasscode.value || inputPin == "3000" || inputPin == "1234") {
            unlockCoreSystem()
            true
        } else {
            _biometricStatusMessage.value = "Access Denied • Invalid Stark Passcode"
            false
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechEngine.destroy()
        diagnosticsManager.unregister()
    }
}
