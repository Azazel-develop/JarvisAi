package com.example

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.JarvisViewModel
import com.example.ui.components.BiometricLockOverlay
import com.example.ui.components.DiagnosticOverlayHud
import com.example.ui.screens.*
import com.example.ui.theme.*

sealed class NavItem(val route: String, val label: String, val icon: ImageVector) {
    object Home : NavItem("home", "HUD", Icons.Default.Home)
    object Chat : NavItem("chat", "Brain", Icons.Default.Terminal)
    object Anime : NavItem("anime", "Anime Studio", Icons.Default.Movie)
    object Telemetry : NavItem("telemetry", "Telemetry", Icons.Default.Speed)
    object Code : NavItem("code", "Code & Files", Icons.Default.Code)
    object Tools : NavItem("tools", "Utilities", Icons.Default.Build)
    object Settings : NavItem("settings", "Settings", Icons.Default.Settings)
    object Profile : NavItem("profile", "Clearance", Icons.Default.Person)
}

class MainActivity : FragmentActivity() {

    private val viewModel: JarvisViewModel by viewModels()

    private val wakeWordReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: android.content.Context?, intent: android.content.Intent?) {
            if (intent?.action == com.example.service.WakeWordService.ACTION_WAKE_WORD_TRIGGERED) {
                val salutation = intent.getStringExtra("SALUTATION") ?: "Sir"
                viewModel.navigateTo("chat")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val filter = android.content.IntentFilter(com.example.service.WakeWordService.ACTION_WAKE_WORD_TRIGGERED)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(wakeWordReceiver, filter, android.content.Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(wakeWordReceiver, filter)
        }

        setContent {
            JarvisTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: NavItem.Home.route

                val popupAlert by viewModel.popupAlert.collectAsState()
                val isCoreLocked by viewModel.isCoreLocked.collectAsState()
                val biometricStatusMessage by viewModel.biometricStatusMessage.collectAsState()

                val audioPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted) {
                        viewModel.startAmbientListening()
                    }
                }

                LaunchedEffect(Unit) {
                    val hasMicPermission = ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        android.Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED

                    if (hasMicPermission) {
                        viewModel.startAmbientListening()
                    } else {
                        audioPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                    }
                }

                // Handle ViewModel navigation events (e.g., voice or chat directives to open screens)
                LaunchedEffect(Unit) {
                    viewModel.navigationEvent.collect { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }

                // Display popup alert if wake word is detected
                LaunchedEffect(popupAlert) {
                    popupAlert?.let { alertMsg ->
                        Toast.makeText(this@MainActivity, alertMsg, Toast.LENGTH_LONG).show()
                        viewModel.dismissPopupAlert()
                    }
                }

                val navItems = listOf(
                    NavItem.Home,
                    NavItem.Chat,
                    NavItem.Anime,
                    NavItem.Telemetry,
                    NavItem.Code,
                    NavItem.Tools,
                    NavItem.Settings,
                    NavItem.Profile
                )

                Scaffold(
                    bottomBar = {
                        NavigationBar(
                            containerColor = VoidSurface,
                            contentColor = CosmicCyan,
                            windowInsets = WindowInsets.navigationBars,
                            modifier = Modifier
                                .border(
                                    width = 1.dp,
                                    color = VoidBorder,
                                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                                )
                                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        ) {
                            navItems.forEach { item ->
                                val selected = currentRoute == item.route
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = {
                                        if (currentRoute != item.route) {
                                            navController.navigate(item.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = item.label,
                                            tint = if (selected) VioletNeon else TextMuted
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = item.label,
                                            color = if (selected) TextGlow else TextMuted,
                                            fontSize = 9.sp,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = VoidSurfaceVariant
                                    ),
                                    modifier = Modifier.testTag("nav_${item.route}")
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = NavItem.Home.route,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            composable(NavItem.Home.route) {
                                HomeScreen(
                                    viewModel = viewModel,
                                    onNavigateToChat = { navController.navigate(NavItem.Chat.route) },
                                    onNavigateToCode = { navController.navigate(NavItem.Code.route) },
                                    onNavigateToDiagnostics = { navController.navigate(NavItem.Telemetry.route) }
                                )
                            }

                            composable(NavItem.Chat.route) {
                                NeuralChatScreen(viewModel = viewModel)
                            }

                            composable(NavItem.Anime.route) {
                                AnimeAnimationStudioScreen(viewModel = viewModel)
                            }

                            composable(NavItem.Telemetry.route) {
                                TelemetryDiagnosticsScreen(viewModel = viewModel)
                            }

                            composable(NavItem.Code.route) {
                                CodeAndFileScreen(viewModel = viewModel)
                            }

                            composable(NavItem.Tools.route) {
                                ToolsIntegrationScreen(viewModel = viewModel)
                            }

                            composable(NavItem.Settings.route) {
                                SettingsScreen(viewModel = viewModel)
                            }

                            composable(NavItem.Profile.route) {
                                UserProfileScreen(viewModel = viewModel)
                            }
                        }

                        // Floating Diagnostic Action & Server WSS Overlay HUD
                        DiagnosticOverlayHud(
                            viewModel = viewModel,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 10.dp, end = 10.dp)
                        )

                        // Biometric Core Security Lock Overlay
                        BiometricLockOverlay(
                            isLocked = isCoreLocked,
                            statusMessage = biometricStatusMessage,
                            onAuthenticateBiometric = { activity ->
                                viewModel.authenticateWithBiometrics(activity)
                            },
                            onAuthenticatePasscode = { pin ->
                                viewModel.authenticateWithPasscode(pin)
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(wakeWordReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
