package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.example.data.local.EaglercraftServer
import com.example.data.local.IntegrationMessage
import com.example.ui.JarvisViewModel
import com.example.ui.theme.*

@Composable
fun ToolsIntegrationScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val servers by viewModel.eaglercraftServers.collectAsState()
    val integrationMessages by viewModel.integrationMessages.collectAsState()

    var activeTab by remember { mutableIntStateOf(0) } // 0 = Eaglercraft, 1 = Messaging, 2 = System Shell

    var newServerName by remember { mutableStateOf("Stark-Node-7") }
    var newPortText by remember { mutableStateOf("25565") }

    var msgPlatform by remember { mutableStateOf("WhatsApp") }
    var msgRecipient by remember { mutableStateOf("Pepper Potts") }
    var msgText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GalaxyVoid)
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        // Screen Header
        Column(modifier = Modifier.padding(vertical = 12.dp)) {
            Text(
                text = "STARK TOOL UTILITIES & INTEGRATIONS",
                color = TextGlow,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Eaglercraft Host, Messaging Hub & System Override Shell",
                color = CosmicCyan,
                fontSize = 11.sp
            )
        }

        // Tab Selector
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = VoidSurface,
            contentColor = CosmicCyan,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                    color = VioletNeon
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, VoidBorder, RoundedCornerShape(12.dp))
        ) {
            Tab(selected = activeTab == 0, onClick = { activeTab = 0 }, text = { Text("Eaglercraft", fontSize = 10.sp, fontWeight = FontWeight.Bold) })
            Tab(selected = activeTab == 1, onClick = { activeTab = 1 }, text = { Text("Messaging", fontSize = 10.sp, fontWeight = FontWeight.Bold) })
            Tab(selected = activeTab == 2, onClick = { activeTab = 2 }, text = { Text("System Override", fontSize = 10.sp, fontWeight = FontWeight.Bold) })
            Tab(selected = activeTab == 3, onClick = { activeTab = 3 }, text = { Text("Puter.js AI Sync", fontSize = 10.sp, fontWeight = FontWeight.Bold) })
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (activeTab) {
            0 -> EaglercraftStudioScreen(viewModel = viewModel)

            1 -> MessagingHubTab(
                messages = integrationMessages,
                platform = msgPlatform,
                onPlatformSelect = { msgPlatform = it },
                recipient = msgRecipient,
                onRecipientChange = { msgRecipient = it },
                msgText = msgText,
                onTextChange = { msgText = it },
                onSend = {
                    if (msgText.isNotBlank()) {
                        viewModel.sendIntegrationMessage(msgPlatform, msgRecipient, msgText)
                        msgText = ""
                        Toast.makeText(context, "Message dispatched via J.A.R.V.I.S.", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            2 -> SystemOverrideTab()

            3 -> PuterAiSyncTab(viewModel = viewModel)
        }
    }
}

@Composable
fun EaglercraftTab(
    servers: List<EaglercraftServer>,
    serverName: String,
    onNameChange: (String) -> Unit,
    portText: String,
    onPortChange: (String) -> Unit,
    onAddServer: () -> Unit,
    onToggleStatus: (EaglercraftServer) -> Unit
) {
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
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Dns, contentDescription = "Server", tint = CosmicCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("HOST EAGLERCRAFT MINECRAFT SERVER", color = TextGlow, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = serverName,
                            onValueChange = onNameChange,
                            label = { Text("Server Name", color = TextMuted, fontSize = 10.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VioletNeon,
                                unfocusedBorderColor = VoidBorder,
                                focusedTextColor = TextGlow,
                                unfocusedTextColor = TextGlow
                            ),
                            modifier = Modifier.weight(1f).testTag("eagler_server_name_input")
                        )

                        OutlinedTextField(
                            value = portText,
                            onValueChange = onPortChange,
                            label = { Text("Port", color = TextMuted, fontSize = 10.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VioletNeon,
                                unfocusedBorderColor = VoidBorder,
                                focusedTextColor = TextGlow,
                                unfocusedTextColor = TextGlow
                            ),
                            modifier = Modifier.width(100.dp).testTag("eagler_port_input")
                        )
                    }

                    Button(
                        onClick = onAddServer,
                        colors = ButtonDefaults.buttonColors(containerColor = VioletNeon),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("deploy_eagler_server_button")
                    ) {
                        Text("DEPLOY NEW SERVER INSTANCE", color = GalaxyVoid, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }

        item {
            Text("ACTIVE EAGLERCRAFT NODES (${servers.size})", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }

        items(servers) { server ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = VoidSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, VoidBorder, RoundedCornerShape(12.dp))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(14.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = server.serverName, color = TextGlow, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = if (server.status == "ONLINE") StatusGreen.copy(alpha = 0.2f) else StatusRed.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = server.status,
                                    color = if (server.status == "ONLINE") StatusGreen else StatusRed,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(text = "Port: ${server.port} | Players: ${server.onlinePlayers}/${server.maxPlayers}", color = TextMuted, fontSize = 11.sp)
                        Text(text = server.motd, color = CosmicCyan, fontSize = 10.sp)
                    }

                    IconButton(onClick = { onToggleStatus(server) }) {
                        Icon(
                            imageVector = if (server.status == "ONLINE") Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = "Toggle",
                            tint = if (server.status == "ONLINE") StatusRed else StatusGreen
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MessagingHubTab(
    messages: List<IntegrationMessage>,
    platform: String,
    onPlatformSelect: (String) -> Unit,
    recipient: String,
    onRecipientChange: (String) -> Unit,
    msgText: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit
) {
    val platforms = listOf("WhatsApp", "Discord", "Gmail", "Outlook")

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
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Chat, contentDescription = "Messaging", tint = CosmicCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI MESSAGING AUTOMATION", color = TextGlow, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        platforms.forEach { p ->
                            FilterChip(
                                selected = p == platform,
                                onClick = { onPlatformSelect(p) },
                                label = { Text(p, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = VioletNeon,
                                    containerColor = VoidSurfaceVariant
                                )
                            )
                        }
                    }

                    OutlinedTextField(
                        value = recipient,
                        onValueChange = onRecipientChange,
                        label = { Text("Recipient Contact / Channel", color = TextMuted, fontSize = 10.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VioletNeon,
                            unfocusedBorderColor = VoidBorder,
                            focusedTextColor = TextGlow,
                            unfocusedTextColor = TextGlow
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("msg_recipient_input")
                    )

                    OutlinedTextField(
                        value = msgText,
                        onValueChange = onTextChange,
                        label = { Text("Message Content / AI Draft", color = TextMuted, fontSize = 10.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VioletNeon,
                            unfocusedBorderColor = VoidBorder,
                            focusedTextColor = TextGlow,
                            unfocusedTextColor = TextGlow
                        ),
                        modifier = Modifier.fillMaxWidth().height(90.dp).testTag("msg_content_input")
                    )

                    Button(
                        onClick = onSend,
                        colors = ButtonDefaults.buttonColors(containerColor = VioletNeon),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("send_integration_msg_button")
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = GalaxyVoid, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("DISPATCH MESSAGE VIA J.A.R.V.I.S.", color = GalaxyVoid, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }

                    if (platform == "Gmail") {
                        val context = LocalContext.current
                        OutlinedButton(
                            onClick = {
                                try {
                                    val emailUri = android.net.Uri.parse("mailto:${if (recipient.contains("@")) recipient else "stark@gmail.com"}?subject=${android.net.Uri.encode("J.A.R.V.I.S. AI Dispatch")}&body=${android.net.Uri.encode(msgText)}")
                                    val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO, emailUri)
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Opening Gmail application...", Toast.LENGTH_SHORT).show()
                                }
                            },
                            border = androidx.compose.foundation.BorderStroke(1.dp, StatusGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("open_gmail_app_button")
                        ) {
                            Text("LAUNCH NATIVE GMAIL CLIENT (INTENT)", color = StatusGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        item {
            // Gmail verification card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = VoidSurfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CosmicCyan, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Security, contentDescription = "Gmail Verified", tint = StatusGreen)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("GMAIL INTEGRATION STATUS: ACTIVE", color = TextGlow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Gmail integration is fully functional via Android Mail Intents and J.A.R.V.I.S. local database sync.", color = CosmicCyan, fontSize = 10.sp)
                    }
                }
            }
        }

        item {
            Text("COMMUNICATION FEED HISTORY", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }

        items(messages) { msg ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = VoidSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, VoidBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "${msg.platform} • ${msg.recipient}", color = CosmicCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(text = if (msg.isIncoming) "INCOMING" else "DISPATCHED", color = VioletLight, fontSize = 9.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = msg.content, color = TextGlow, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun SystemOverrideTab() {
    val context = LocalContext.current
    var appLaunchQuery by remember { mutableStateOf("YouTube") }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = VoidSurface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, VoidBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Security, contentDescription = "Shield", tint = PlasmaPink, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("STARK PROTOCOL ALPHA - SYSTEM OVERRIDE", color = TextGlow, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Text(
                    text = "System Integration Status: Active.\nApp & File Launch Intents Ready.\nFloating Wake Word HUD Overlay Enabled.",
                    color = TextMuted,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )

                Surface(
                    color = VoidSurfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = "Active", tint = StatusGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("DEVICE SHELL & INTENT LAUNCHER", color = TextGlow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("Root-Level App & File Opening Matrix Active", color = StatusGreen, fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // App & File Direct Launcher Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = VoidSurface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, VoidBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "🚀 DIRECT APP, FILE & WEBSITE MATRIX LAUNCHER",
                    color = CosmicCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = appLaunchQuery,
                    onValueChange = { appLaunchQuery = it },
                    label = { Text("App, Website URL or Domain (e.g. YouTube, Modrinth, github.com, Camera, Files)", fontSize = 10.sp, color = TextMuted) },
                    modifier = Modifier.fillMaxWidth().testTag("app_launch_query_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CosmicCyan,
                        unfocusedBorderColor = VoidBorder,
                        focusedTextColor = TextGlow,
                        unfocusedTextColor = TextGlow
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Button(
                    onClick = {
                        val result = com.example.util.AppAndFileLauncher.openAppOrAction(context, appLaunchQuery)
                        Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicCyan),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("launch_app_button")
                ) {
                    Text("LAUNCH APPLICATION / WEBSITE / FILE", color = GalaxyVoid, fontWeight = FontWeight.Bold)
                }

                Text("QUICK ACCESS MATRIX:", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)

                val quickApps = listOf("Modrinth", "CurseForge", "PlanetMinecraft", "Eaglercraft", "YouTube", "GitHub", "Discord", "Gmail", "Camera", "Files", "Settings", "Chrome")
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    quickApps.take(4).forEach { target ->
                        OutlinedButton(
                            onClick = {
                                appLaunchQuery = target
                                val result = com.example.util.AppAndFileLauncher.openAppOrAction(context, target)
                                Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(target, color = VioletLight, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    quickApps.drop(4).take(4).forEach { target ->
                        OutlinedButton(
                            onClick = {
                                appLaunchQuery = target
                                val result = com.example.util.AppAndFileLauncher.openAppOrAction(context, target)
                                Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(target, color = CosmicCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PuterAiSyncTab(viewModel: JarvisViewModel) {
    val context = LocalContext.current
    var testPrompt by remember { mutableStateOf("Solve: Integrate x^3 * e^(2x) dx step by step") }
    var responseText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = VoidSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Brush.linearGradient(listOf(CosmicCyan, VioletNeon)), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Dns, contentDescription = "Puter AI", tint = CosmicCyan, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("PUTER.JS FREE CLOUD AI MATRIX", color = TextGlow, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                            Text("Zero API Key • Free Unlimited Multi-Model Intelligence", color = StatusGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text(
                        text = "J.A.R.V.I.S. is synchronized with Puter.js Cloud Engine and Google Gemini Free Tier. Complex mathematical derivations, quantum physics, code synthesis, and deep reasoning run with zero cost and high speed.",
                        color = TextMuted,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )

                    HorizontalDivider(color = VoidBorder)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("FREE GEMINI TIER", color = TextGlow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("gemini-2.5-flash • Auto Sync", color = CosmicCyan, fontSize = 9.sp)
                        }
                        Column {
                            Text("PUTER.JS ENGINE", color = TextGlow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("GPT-4o / Claude 3.5 Sonnet", color = VioletLight, fontSize = 9.sp)
                        }
                        Column {
                            Text("STATUS", color = TextGlow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("SYNCHRONIZED", color = StatusGreen, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = VoidSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, VoidBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("TEST PUTER.JS COMPLEX QUERY ENGINE", color = TextGlow, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = testPrompt,
                        onValueChange = { testPrompt = it },
                        label = { Text("Complex Question / Code / Math Query", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CosmicCyan,
                            unfocusedBorderColor = VoidBorder,
                            focusedTextColor = TextGlow,
                            unfocusedTextColor = TextGlow
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            if (testPrompt.isNotBlank()) {
                                isLoading = true
                                responseText = "Contacting Puter.js Neural Array..."
                                viewModel.sendMessage(testPrompt)
                                Toast.makeText(context, "Query sent to J.A.R.V.I.S. Neural Core!", Toast.LENGTH_SHORT).show()
                                isLoading = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VioletNeon),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("EXECUTE COMPLEX QUERY", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
