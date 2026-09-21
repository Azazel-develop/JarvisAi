package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String, // "USER" or "JARVIS"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val codeSnippet: String? = null,
    val language: String? = null
)

@Entity(tableName = "generated_files")
data class GeneratedFile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fileName: String,
    val language: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isCloudSynced: Boolean = true
)

@Entity(tableName = "eaglercraft_servers")
data class EaglercraftServer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val serverName: String,
    val port: Int,
    val maxPlayers: Int = 10,
    val motd: String = "J.A.R.V.I.S. High Performance Eaglercraft Node",
    val status: String = "ONLINE", // "ONLINE", "STOPPED", "BUILDING"
    val onlinePlayers: Int = 3,
    val minecraftVersion: String = "1.20.4", // "1.8.8", "1.12.2", "1.16.5", "1.20.4", "1.20.6", "1.21"
    val wssUrl: String = "wss://eaglercraft.starknet.io:8081",
    val installedPlugins: String = "EssentialsX, WorldEdit, LuckPerms, GeyserMC, ViaVersion, Vault",
    val serverProperties: String = "pvp=true\nonline-mode=false\ngamemode=survival\ndifficulty=normal\nmax-players=10\nallow-flight=true\nmotd=§b§lSTARK INDUSTRIES §7| §e24/7 Paper Eaglercraft Node",
    val eaglerOptsJson: String = "{\n  \"server_name\": \"Stark Paper Eagler Server\",\n  \"version\": \"1.20.4\",\n  \"wss_proxy\": \"wss://eaglercraft.starknet.io:8081\",\n  \"max_players\": 10\n}"
)

@Entity(tableName = "integration_messages")
data class IntegrationMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val platform: String, // "WhatsApp", "Discord", "Gmail", "Outlook"
    val recipient: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isIncoming: Boolean = true
)

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val userName: String = "Tony Stark",
    val userEmail: String = "tony@starkindustries.com",
    val preferredTitle: String = "Sir", // e.g. "Sir", "Boss", "Mr. Stark", "Ma'am", "Commander", "Doctor"
    val clearanceLevel: String = "Level 10 - Alpha",
    val userXp: Long = 1250L, // Infinite uncapped XP
    val isRegistered: Boolean = true,
    val arcReactorPower: Int = 100,
    val voicePitch: Float = 0.95f,
    val voiceRate: Float = 1.02f,
    val voiceLanguage: String = "EN_GB", // "EN_GB", "EN_US", "EN_IN", "FR_FR", "DE_DE", "JA_JP"
    val voicePersona: String = "JARVIS_BRITISH", // "JARVIS_BRITISH", "FRIDAY_TACTICAL", "CYBER_SYNTH", "ANIME_COMPANION", "DEEP_COMMANDER"
    val isCloudSynced: Boolean = true,
    val wakeWordEnabled: Boolean = true,
    val attitude: String = "CLASSIC", // "CLASSIC", "SARCASTIC", "TACTICAL", "ANIME", "SCIENCE", "CYBERPUNK"
    val themeColor: String = "CYAN", // "CYAN", "GOLD", "GREEN", "PINK", "PURPLE"
    val animationIntensity: String = "ULTRA", // "LOW", "NORMAL", "ULTRA"
    val showSubtitles: Boolean = true,
    val customApiKey: String = "",
    val githubAccount: String = "",
    val githubToken: String = "",
    val isOfflineMode: Boolean = false,
    val cameraFov: Float = 68.0f,
    val perspectiveTilt: Float = 0.0f,
    val perspectiveShearX: Float = 0.0f,
    val perspectiveShearY: Float = 0.0f,
    val perspectiveRoll: Float = 0.0f,
    val surfaceDepthOffset: Float = 1.5f,
    val surfaceSnapEnabled: Boolean = true,
    val userNotesAndMemory: String = ""
)
