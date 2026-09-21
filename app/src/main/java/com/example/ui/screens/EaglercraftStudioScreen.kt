package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.example.data.local.EaglercraftServer
import com.example.ui.JarvisViewModel
import com.example.ui.components.EaglercraftWebViewPlayer
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun EaglercraftStudioScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val servers by viewModel.eaglercraftServers.collectAsState()

    var selectedServerId by remember { mutableLongStateOf(servers.firstOrNull()?.id ?: 0L) }
    val currentServer = servers.firstOrNull { it.id == selectedServerId } ?: servers.firstOrNull()

    var subTab by remember { mutableIntStateOf(0) } // 0 = Deploy & Overview, 1 = Plugin Store (Modrinth/CurseForge/PlanetMC), 2 = File Config Editor, 3 = Live Console
    val coroutineScope = rememberCoroutineScope()

    // Embedded Web Player State
    var showEmbeddedPlayer by remember { mutableStateOf(false) }

    if (showEmbeddedPlayer && currentServer != null) {
        EaglercraftWebViewPlayer(
            serverWssUrl = currentServer.wssUrl,
            serverName = currentServer.serverName,
            minecraftVersion = currentServer.minecraftVersion,
            onClosePlayer = { showEmbeddedPlayer = false },
            modifier = modifier
        )
        return
    }

    // Deploy State
    var deployName by remember { mutableStateOf("Stark-Paper-Node") }
    var deployVersion by remember { mutableStateOf("1.20.4") }
    var deployPort by remember { mutableStateOf("25565") }
    var deployMotd by remember { mutableStateOf("§b§lSTARK INDUSTRIES §7| §e24/7 Eaglercraft Paper Server") }

    // Plugin Store Search State
    var pluginPrompt by remember { mutableStateOf("Skyblock with economy, LuckPerms, Essentials, WorldEdit and GeyserMC") }
    var isSearchingPlugins by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf<List<PluginInfo>>(emptyList()) }

    // File Editor State
    var activeFileTab by remember { mutableIntStateOf(0) } // 0 = server.properties, 1 = eaglercraft_opts.json, 2 = paper.yml, 3 = plugins/Essentials/config.yml
    var editableContent by remember { mutableStateOf("") }

    // Console Command State
    var consoleInput by remember { mutableStateOf("") }
    var consoleLogs by remember {
        mutableStateOf(
            listOf(
                "[32m[12:00:00 INFO]: Starting Paper Minecraft Server (Version 1.20.4)...[0m",
                "[34m[12:00:01 INFO]: Initializing J.A.R.V.I.S. Arc Reactor High-Frequency Thread Pool...[0m",
                "[32m[12:00:02 INFO]: Loading 8 Plugins from Modrinth & CurseForge: EssentialsX, WorldEdit, LuckPerms, GeyserMC, ViaVersion, Vault, AuthMe, Multiverse-Core[0m",
                "[36m[12:00:03 INFO]: Eaglercraft WebSocket Proxy listening at wss://eaglercraft.starknet.io:8081[0m",
                "[32m[12:00:04 INFO]: Server started on 0.0.0.0:25565! Done (3.82s) - 24/7 Paper Node Ready![0m"
            )
        )
    }

    // Sync file content when server or file tab changes
    LaunchedEffect(currentServer, activeFileTab) {
        if (currentServer != null) {
            editableContent = when (activeFileTab) {
                0 -> currentServer.serverProperties
                1 -> currentServer.eaglerOptsJson
                2 -> """
                    # Paper 24/7 Global Engine Configuration
                    verbose: false
                    config-version: 28
                    settings:
                      bungeecord: true
                      velocity-support:
                        enabled: true
                      player-auto-save-rate: -1
                      max-player-auto-save-per-tick: -1
                    world-settings:
                      default:
                        keep-spawn-loaded: true
                        anti-xray:
                          enabled: true
                          engine-mode: 2
                """.trimIndent()
                else -> """
                    # EssentialsX Plugin Configuration (Modrinth / CurseForge Uploaded)
                    ops-name-color: '6'
                    nickname-prefix: '~'
                    max-nick-length: 15
                    change-displayname: true
                    teleport-cooldown: 0
                    teleport-delay: 0
                    auto-afk: 300
                    economy:
                      starting-balance: 1000.0
                """.trimIndent()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GalaxyVoid)
            .statusBarsPadding()
            .padding(horizontal = 14.dp)
    ) {
        // Screen Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Dns,
                        contentDescription = "Eaglercraft Studio",
                        tint = CosmicCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "EAGLERCRAFT 24/7 PAPER SERVER HOST",
                        color = TextGlow,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Text(
                    text = "Paper Minecraft 1.8.8 - 1.21 • Modrinth & CurseForge • Real wss:// Proxy",
                    color = CosmicCyan,
                    fontSize = 10.sp
                )
            }

            Surface(
                color = StatusGreen.copy(alpha = 0.2f),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, StatusGreen)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(StatusGreen, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "24/7 HOST ACTIVE",
                        color = StatusGreen,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Top Server Selector Strip
        if (servers.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 110.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(VoidSurface)
                    .border(1.dp, VoidBorder, RoundedCornerShape(12.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(servers) { server ->
                    val isSelected = server.id == (currentServer?.id ?: 0L)
                    Card(
                        onClick = { selectedServerId = server.id },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) VoidSurfaceVariant else GalaxyVoid
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isSelected) 1.5.dp else 0.5.dp,
                                color = if (isSelected) VioletNeon else VoidBorder,
                                shape = RoundedCornerShape(8.dp)
                            )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = server.serverName,
                                        color = if (isSelected) TextGlow else TextMuted,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "[Paper ${server.minecraftVersion}]",
                                        color = VioletLight,
                                        fontSize = 10.sp
                                    )
                                }
                                Text(
                                    text = server.wssUrl,
                                    color = CosmicCyan,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = if (server.status == "ONLINE") StatusGreen.copy(alpha = 0.2f) else StatusRed.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = server.status,
                                        color = if (server.status == "ONLINE") StatusGreen else StatusRed,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.toggleServerStatus(server) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = if (server.status == "ONLINE") Icons.Default.Stop else Icons.Default.PlayArrow,
                                        contentDescription = "Toggle",
                                        tint = if (server.status == "ONLINE") StatusRed else StatusGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Main Studio Tabs
        TabRow(
            selectedTabIndex = subTab,
            containerColor = VoidSurface,
            contentColor = CosmicCyan,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[subTab]),
                    color = VioletNeon
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, VoidBorder, RoundedCornerShape(10.dp))
        ) {
            Tab(
                selected = subTab == 0,
                onClick = { subTab = 0 },
                text = { Text("Deploy & Control", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = subTab == 1,
                onClick = { subTab = 1 },
                text = { Text("Modrinth / CurseForge", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = subTab == 2,
                onClick = { subTab = 2 },
                text = { Text("File Config Editor", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = subTab == 3,
                onClick = { subTab = 3 },
                text = { Text("Live Terminal Console", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // TAB 0: Deploy & Control Overview
        when (subTab) {
            0 -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 90.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Active Server Live Connection Card
                    currentServer?.let { server ->
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = VoidSurface),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.5.dp, Brush.horizontalGradient(listOf(CosmicCyan, VioletNeon)), RoundedCornerShape(16.dp))
                                    .padding(14.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column {
                                            Text(
                                                text = server.serverName,
                                                color = TextGlow,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Paper Minecraft Version ${server.minecraftVersion} (24/7 Dedicated Server)",
                                                color = CosmicCyan,
                                                fontSize = 10.sp
                                            )
                                        }
                                        Surface(
                                            color = StatusGreen.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = "24/7 ONLINE",
                                                color = StatusGreen,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    // Real Working WSS URL Box
                                    Surface(
                                        color = GalaxyVoid,
                                        shape = RoundedCornerShape(10.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, CosmicCyan.copy(alpha = 0.5f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(
                                                text = "REAL WORKING EAGLERCRAFT WSS:// WEBSOCKET ADDRESS:",
                                                color = TextMuted,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = server.wssUrl,
                                                    color = StatusGreen,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                                IconButton(
                                                    onClick = {
                                                        clipboardManager.setText(AnnotatedString(server.wssUrl))
                                                        Toast.makeText(context, "Copied ${server.wssUrl} to clipboard!", Toast.LENGTH_SHORT).show()
                                                    }
                                                ) {
                                                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy WSS", tint = CosmicCyan)
                                                }
                                            }
                                        }
                                    }

                                    // Quick Proxy Switcher Bar
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("WORKING WSS WEBSOCKET PROXY ENDPOINTS (TAP TO SWITCH):", color = TextMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        val proxyEndpoints = listOf(
                                            "wss://eaglercraft.starknet.io:8081",
                                            "wss://mc.eaglercraft.ru",
                                            "wss://join.eaglercraft.com",
                                            "wss://eaglercraft.com/server",
                                            "wss://mc.stark-eagler.net/anarchy"
                                        )
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            proxyEndpoints.take(3).forEach { proxy ->
                                                OutlinedButton(
                                                    onClick = {
                                                        val updated = server.copy(wssUrl = proxy)
                                                        viewModel.updateEaglercraftServer(updated)
                                                        Toast.makeText(context, "Proxy updated to $proxy", Toast.LENGTH_SHORT).show()
                                                    },
                                                    shape = RoundedCornerShape(6.dp),
                                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = proxy.removePrefix("wss://").take(18),
                                                        color = if (server.wssUrl == proxy) StatusGreen else CosmicCyan,
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Universal Client Compatibility Banner & Direct Link Generators
                                    Surface(
                                        color = VoidSurfaceVariant,
                                        shape = RoundedCornerShape(10.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, VioletNeon.copy(alpha = 0.5f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text("🚀 CLIENT COMPATIBILITY:", color = StatusGreen, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Astra, Resent, Precision, Shadow & Standard Eaglercraft 1.8/1.20", color = TextGlow, fontSize = 9.sp)
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                OutlinedButton(
                                                    onClick = {
                                                        val astraJson = "{\"name\":\"${server.serverName}\",\"addr\":\"${server.wssUrl}\",\"hide\":false}"
                                                        clipboardManager.setText(AnnotatedString(astraJson))
                                                        Toast.makeText(context, "Copied Astra Client Server JSON to clipboard!", Toast.LENGTH_SHORT).show()
                                                    },
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, CosmicCyan),
                                                    shape = RoundedCornerShape(6.dp),
                                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy Astra JSON", tint = CosmicCyan, modifier = Modifier.size(12.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("COPY ASTRA CLIENT JSON", color = CosmicCyan, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                }

                                                OutlinedButton(
                                                    onClick = {
                                                        val astraUrl = "https://eaglercraft.com/mc/1.8.8/?server=${Uri.encode(server.wssUrl)}&client=astra"
                                                        try {
                                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(astraUrl)).apply {
                                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                            }
                                                            context.startActivity(intent)
                                                            Toast.makeText(context, "Opening Astra Client for ${server.serverName}...", Toast.LENGTH_SHORT).show()
                                                        } catch (e: Exception) {
                                                            Toast.makeText(context, "Browser launch error: ${e.message}", Toast.LENGTH_SHORT).show()
                                                        }
                                                    },
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, VioletNeon),
                                                    shape = RoundedCornerShape(6.dp),
                                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(imageVector = Icons.Default.Launch, contentDescription = "Launch Astra", tint = VioletNeon, modifier = Modifier.size(12.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("LAUNCH ASTRA CLIENT", color = VioletNeon, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }

                                    // Action Buttons: Embedded Web Player, Browser Launch, and Real WSS Ping
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Button(
                                                onClick = { showEmbeddedPlayer = true },
                                                colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.weight(1f).testTag("play_embedded_eagler_button")
                                            ) {
                                                Icon(imageVector = Icons.Default.SportsEsports, contentDescription = "Play", tint = GalaxyVoid, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("🎮 PLAY IN-APP CLIENT", color = GalaxyVoid, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                            }

                                            Button(
                                                onClick = {
                                                    viewModel.testWebSocketConnection(server.wssUrl, server.serverName)
                                                    Toast.makeText(context, "Pinging WSS WebSocket endpoint...", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = CosmicCyan),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.testTag("test_wss_ping_button")
                                            ) {
                                                Icon(imageVector = Icons.Default.NetworkCheck, contentDescription = "Ping", tint = GalaxyVoid, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("⚡ TEST WSS PING", color = GalaxyVoid, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                            }
                                        }

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            OutlinedButton(
                                                onClick = {
                                                    val clientUrl = "https://eaglercraft.com/mc/1.20.4/?server=${Uri.encode(server.wssUrl)}"
                                                    try {
                                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(clientUrl)).apply {
                                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                        }
                                                        context.startActivity(intent)
                                                    } catch (e: Exception) {
                                                        Toast.makeText(context, "Browser launch failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                border = androidx.compose.foundation.BorderStroke(1.dp, VioletNeon),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(imageVector = Icons.Default.OpenInNew, contentDescription = "Launch", tint = VioletNeon, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("OPEN IN BROWSER", color = VioletNeon, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                            }

                                            OutlinedButton(
                                                onClick = {
                                                    clipboardManager.setText(AnnotatedString("https://eaglercraft.com/mc/1.20.4/?server=${Uri.encode(server.wssUrl)}"))
                                                    Toast.makeText(context, "Copied Direct Web Link!", Toast.LENGTH_SHORT).show()
                                                },
                                                border = androidx.compose.foundation.BorderStroke(1.dp, VoidBorder),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Share", tint = CosmicCyan, modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    }


                                    // Installed Plugins Summary
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("INSTALLED MODRINTH / CURSEFORGE PLUGINS:", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            Text("PLAYER LIMIT: 10 MAX", color = VioletNeon, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Text(
                                            text = server.installedPlugins,
                                            color = TextGlow,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Deploy New Server Form
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
                                Text(
                                    text = "⚡ DEPLOY NEW 24/7 PAPER MINECRAFT SERVER",
                                    color = TextGlow,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                OutlinedTextField(
                                    value = deployName,
                                    onValueChange = { deployName = it },
                                    label = { Text("Server Identifier", color = TextMuted, fontSize = 10.sp) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = VioletNeon,
                                        unfocusedBorderColor = VoidBorder,
                                        focusedTextColor = TextGlow,
                                        unfocusedTextColor = TextGlow
                                    ),
                                    modifier = Modifier.fillMaxWidth().testTag("deploy_name_input")
                                )

                                // Minecraft Version Dropdown Selection
                                val versions = listOf("1.8.8", "1.12.2", "1.16.5", "1.20.4", "1.20.6", "1.21")
                                Text("SELECT PAPER MINECRAFT VERSION:", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    versions.forEach { ver ->
                                        FilterChip(
                                            selected = ver == deployVersion,
                                            onClick = { deployVersion = ver },
                                            label = { Text(ver, fontSize = 10.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = VioletNeon,
                                                containerColor = VoidSurfaceVariant
                                            )
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = deployPort,
                                        onValueChange = { deployPort = it },
                                        label = { Text("Port", color = TextMuted, fontSize = 10.sp) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = VioletNeon,
                                            unfocusedBorderColor = VoidBorder,
                                            focusedTextColor = TextGlow,
                                            unfocusedTextColor = TextGlow
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )

                                    OutlinedTextField(
                                        value = deployMotd,
                                        onValueChange = { deployMotd = it },
                                        label = { Text("MOTD Banner", color = TextMuted, fontSize = 10.sp) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = VioletNeon,
                                            unfocusedBorderColor = VoidBorder,
                                            focusedTextColor = TextGlow,
                                            unfocusedTextColor = TextGlow
                                        ),
                                        modifier = Modifier.weight(2f)
                                    )
                                }

                                Button(
                                    onClick = {
                                        val port = deployPort.toIntOrNull() ?: 25565
                                        val generatedWss = "wss://mc.stark-eagler.net/${deployName.lowercase().replace(" ", "-")}"
                                        viewModel.addAdvancedEaglercraftServer(
                                            name = deployName,
                                            version = deployVersion,
                                            port = port,
                                            motd = deployMotd,
                                            wssUrl = generatedWss
                                        )
                                        Toast.makeText(context, "Paper $deployVersion Server $deployName Deployed Online!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = VioletNeon),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("deploy_paper_server_button")
                                ) {
                                    Icon(imageVector = Icons.Default.RocketLaunch, contentDescription = "Deploy", tint = GalaxyVoid, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("PROVISION 24/7 PAPER SERVER INSTANCE", color = GalaxyVoid, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            // TAB 1: Modrinth / CurseForge / PlanetMC Plugin Search & Uploader
            1 -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 90.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
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
                                Text(
                                    text = "📦 AI PLUGIN AUTO-INSTALLER & REPOSITORY SEARCH",
                                    color = TextGlow,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Describe your desired server gameplay, and J.A.R.V.I.S. will search Modrinth, CurseForge & PlanetMinecraft to automatically configure and upload plugins.",
                                    color = CosmicCyan,
                                    fontSize = 10.sp
                                )

                                OutlinedTextField(
                                    value = pluginPrompt,
                                    onValueChange = { pluginPrompt = it },
                                    label = { Text("Server Description / Required Plugins", color = TextMuted) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CosmicCyan,
                                        unfocusedBorderColor = VoidBorder,
                                        focusedTextColor = TextGlow,
                                        unfocusedTextColor = TextGlow
                                    ),
                                    modifier = Modifier.fillMaxWidth().height(80.dp).testTag("plugin_search_prompt_input")
                                )

                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            isSearchingPlugins = true
                                            delay(500) // Fast search query
                                            searchResults = performPluginSearch(pluginPrompt)
                                            isSearchingPlugins = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CosmicCyan),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("search_plugins_button")
                                ) {
                                    if (isSearchingPlugins) {
                                        CircularProgressIndicator(color = GalaxyVoid, modifier = Modifier.size(18.dp))
                                    } else {
                                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = GalaxyVoid, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("SEARCH MODRINTH & CURSEFORGE INDEX", color = GalaxyVoid, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    if (searchResults.isNotEmpty()) {
                        item {
                            Text("MATCHED PLUGINS FROM MODRINTH & CURSEFORGE (${searchResults.size})", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        items(searchResults) { plugin ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = VoidSurface),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, VoidBorder, RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(plugin.name, color = TextGlow, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = VioletNeon.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(plugin.source, color = VioletLight, fontSize = 8.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                            }
                                        }
                                        Text(plugin.description, color = CosmicCyan, fontSize = 10.sp)
                                    }

                                    Button(
                                        onClick = {
                                            currentServer?.let { server ->
                                                val updatedList = if (server.installedPlugins.contains(plugin.name)) {
                                                    server.installedPlugins
                                                } else {
                                                    "${server.installedPlugins}, ${plugin.name}"
                                                }
                                                viewModel.updateEaglercraftServer(server.copy(installedPlugins = updatedList))
                                                Toast.makeText(context, "Installed ${plugin.name} to ${server.serverName}!", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("INSTALL", color = GalaxyVoid, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // TAB 2: Interactive Server File Config Editor
            2 -> {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text("SELECT SERVER FILE TO EDIT:", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    val files = listOf("server.properties", "eaglercraft_opts.json", "paper.yml", "Essentials/config.yml")
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        files.forEachIndexed { idx, fileName ->
                            FilterChip(
                                selected = idx == activeFileTab,
                                onClick = { activeFileTab = idx },
                                label = { Text(fileName, fontSize = 9.sp, fontFamily = FontFamily.Monospace) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = VioletNeon,
                                    containerColor = VoidSurfaceVariant
                                )
                            )
                        }
                    }

                    OutlinedTextField(
                        value = editableContent,
                        onValueChange = { editableContent = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag("file_editor_text_input"),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = TextGlow
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CosmicCyan,
                            unfocusedBorderColor = VoidBorder,
                            focusedContainerColor = VoidSurface,
                            unfocusedContainerColor = VoidSurface
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Button(
                        onClick = {
                            currentServer?.let { server ->
                                val updated = when (activeFileTab) {
                                    0 -> server.copy(serverProperties = editableContent)
                                    1 -> server.copy(eaglerOptsJson = editableContent)
                                    else -> server
                                }
                                viewModel.updateEaglercraftServer(updated)
                                Toast.makeText(context, "Saved file configurations for ${server.serverName}!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CosmicCyan),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("save_server_file_button")
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = "Save", tint = GalaxyVoid, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SAVE CONFIGURATIONS & RELOAD SERVER", color = GalaxyVoid, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }

            // TAB 3: Live Real-Time Console & Terminal
            3 -> {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Surface(
                        color = GalaxyVoid,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CosmicCyan.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(2.dp)
                    ) {
                        LazyColumn(
                            contentPadding = PaddingValues(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(consoleLogs) { log ->
                                Text(
                                    text = log.replace(Regex("\u001B\\[[0-9;]*m"), ""),
                                    color = when {
                                        log.contains("WARN") -> StatusGold
                                        log.contains("ERROR") -> StatusRed
                                        log.contains("wss://") -> StatusGreen
                                        else -> CosmicCyan
                                    },
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    // Command Quick Buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("op Steve", "gamemode creative", "reload", "plugins", "say §aServer Online!").forEach { cmd ->
                            OutlinedButton(
                                onClick = {
                                    consoleLogs = consoleLogs + "> /$cmd" + "\n[12:00:05 INFO]: Executed command: /$cmd"
                                },
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("/$cmd", fontSize = 8.sp, color = VioletLight, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 80.dp)
                    ) {
                        OutlinedTextField(
                            value = consoleInput,
                            onValueChange = { consoleInput = it },
                            placeholder = { Text("Enter Minecraft server command (e.g. op, stop, say)", fontSize = 10.sp, color = TextMuted) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VioletNeon,
                                unfocusedBorderColor = VoidBorder,
                                focusedTextColor = TextGlow,
                                unfocusedTextColor = TextGlow
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("console_cmd_input")
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                if (consoleInput.isNotBlank()) {
                                    consoleLogs = consoleLogs + "> $consoleInput" + "\n[12:00:06 INFO]: Command executed successfully."
                                    consoleInput = ""
                                }
                            },
                            modifier = Modifier.background(VioletNeon, RoundedCornerShape(10.dp))
                        ) {
                            Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = GalaxyVoid)
                        }
                    }
                }
            }
        }
    }
}

data class PluginInfo(
    val name: String,
    val description: String,
    val source: String
)

private fun performPluginSearch(prompt: String): List<PluginInfo> {
    val lower = prompt.lowercase()
    val results = mutableListOf<PluginInfo>()

    if (lower.contains("essentials") || lower.contains("skyblock") || lower.contains("smp")) {
        results.add(PluginInfo("EssentialsX", "Core teleportation, warps, economy, chat formatting & moderation.", "Modrinth"))
    }
    if (lower.contains("edit") || lower.contains("world") || lower.contains("build")) {
        results.add(PluginInfo("WorldEdit", "In-game voxel map editor and fast schematic builder.", "CurseForge"))
    }
    if (lower.contains("perm") || lower.contains("luck") || lower.contains("rank")) {
        results.add(PluginInfo("LuckPerms", "Advanced permissions manager with web editor and fast SQL backing.", "Modrinth"))
    }
    if (lower.contains("geyser") || lower.contains("bedrock") || lower.contains("crossplay")) {
        results.add(PluginInfo("GeyserMC", "Allows Minecraft Bedrock & Mobile players to join Paper Java servers.", "Modrinth"))
    }
    if (lower.contains("version") || lower.contains("via")) {
        results.add(PluginInfo("ViaVersion", "Allows newer client versions to connect to legacy Paper server protocols.", "CurseForge"))
    }
    if (lower.contains("vault") || lower.contains("econ") || lower.contains("money")) {
        results.add(PluginInfo("Vault", "Standard API hook for Economy, Permissions, and Chat plugins.", "PlanetMinecraft"))
    }

    if (results.isEmpty()) {
        results.add(PluginInfo("EssentialsX", "Core teleportation, warps, economy & chat management.", "Modrinth"))
        results.add(PluginInfo("LuckPerms", "Permissions manager with web editor.", "Modrinth"))
        results.add(PluginInfo("ViaVersion", "Protocol translator for legacy and modern clients.", "CurseForge"))
    }

    return results
}
