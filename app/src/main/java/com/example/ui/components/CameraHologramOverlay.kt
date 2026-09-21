package com.example.ui.components

import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.theme.*

@Composable
fun CameraHologramOverlay(
    isSpeaking: Boolean = false,
    isListening: Boolean = false,
    speechAmplitude: Float = 0f,
    userTitle: String = "Sir",
    latestMessage: String? = null,
    isProcessing: Boolean = false,
    cameraFov: Float = 68.0f,
    perspectiveTilt: Float = 0.0f,
    perspectiveShearX: Float = 0.0f,
    perspectiveShearY: Float = 0.0f,
    perspectiveRoll: Float = 0.0f,
    surfaceDepthOffset: Float = 1.5f,
    surfaceSnapEnabled: Boolean = true,
    onSendQuery: (String) -> Unit = {},
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    var projectionMode by remember { mutableStateOf("AIR") } // "AIR" or "WALL"
    var projectionDistance by remember { mutableFloatStateOf(1.5f) } // 0.8m, 1.5m, 2.5m
    var isKeyboardExpanded by remember { mutableStateOf(true) }
    var keyboardMode by remember { mutableStateOf("ALPHA") } // "ALPHA", "MATH"
    var typedText by remember { mutableStateOf("") }

    // Hologram Touch Gesture States (Pinch-to-zoom & Pan)
    var holoScale by remember { mutableFloatStateOf(1.0f) }
    var holoOffsetX by remember { mutableFloatStateOf(0.0f) }
    var holoOffsetY by remember { mutableFloatStateOf(0.0f) }

    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "Camera permission required for Holographic AR Projection", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(hasCameraPermission, lensFacing, previewViewRef) {
        val pv = previewViewRef ?: return@LaunchedEffect
        if (!hasCameraPermission) return@LaunchedEffect

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(pv.surfaceProvider)
                }

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(lensFacing)
                    .build()

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.95f)),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(listOf(VioletNeon, CosmicCyan, PlasmaPink)),
                shape = RoundedCornerShape(24.dp)
            )
            .testTag("camera_hologram_ar_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // AR Header Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(CosmicCyan.copy(alpha = 0.3f))
                            .border(1.dp, CosmicCyan, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "AR Feed",
                            tint = CosmicCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = if (projectionMode == "AIR") "MID-AIR QUANTUM HOLOGRAM" else "WALL SURFACE PROJECTION ANCHOR",
                            color = TextGlow,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Physical Surface Matrix • Depth: ${String.format("%.1fm", projectionDistance)} • $userTitle",
                            color = CosmicCyan,
                            fontSize = 9.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                                CameraSelector.LENS_FACING_FRONT
                            } else {
                                CameraSelector.LENS_FACING_BACK
                            }
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlipCameraAndroid,
                            contentDescription = "Switch Camera",
                            tint = CosmicCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(VoidSurfaceVariant)
                            .testTag("close_ar_hologram_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close AR Hologram",
                            tint = TextGlow,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // AR Mode Selector Bar (MID-AIR vs WALL SURFACE)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(VoidSurface)
                    .border(1.dp, VoidBorder, RoundedCornerShape(10.dp))
                    .padding(2.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { projectionMode = "AIR" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (projectionMode == "AIR") VioletNeon else Color.Transparent,
                        contentColor = TextGlow
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("🛸 MID-AIR PROJECTION", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(4.dp))

                Button(
                    onClick = { projectionMode = "WALL" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (projectionMode == "WALL") CosmicCyan else Color.Transparent,
                        contentColor = if (projectionMode == "WALL") Color.Black else TextGlow
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("🧱 WALL SURFACE ANCHOR", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Live AR Camera Box with Holographic Pulse Orb & Tangible Touch Keypad
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.Black)
                    .border(1.dp, if (projectionMode == "AIR") VioletNeon else CosmicCyan, RoundedCornerShape(18.dp))
            ) {
                if (hasCameraPermission) {
                    AndroidView(
                        factory = { ctx ->
                            PreviewView(ctx).apply {
                                scaleType = PreviewView.ScaleType.FILL_CENTER
                                previewViewRef = this
                            }
                        },
                        update = {},
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "No Camera",
                            tint = CosmicCyan,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "TAP TO GRANT CAMERA PERMISSION FOR AR PROJECTION",
                            color = TextGlow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA) },
                            colors = ButtonDefaults.buttonColors(containerColor = VioletNeon)
                        ) {
                            Text("Grant Permission", fontSize = 11.sp)
                        }
                    }
                }

                // Interactive Hologram Touch Gesture Container (Pinch-to-Zoom & Pan)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                holoScale = (holoScale * zoom).coerceIn(0.4f, 3.5f)
                                holoOffsetX += pan.x
                                holoOffsetY += pan.y
                            }
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = holoScale,
                                scaleY = holoScale,
                                translationX = holoOffsetX,
                                translationY = holoOffsetY
                            )
                    ) {
                        // AR Projection Spatial Laser Beam & Wall Grid Canvas with Optical Calibration Matrix
                        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val cx = w / 2f
                            val cy = h / 2f + (perspectiveTilt * 2f)

                            val fovFactor = (cameraFov / 68.0f).coerceIn(0.5f, 2.0f)
                            val depthFactor = (surfaceDepthOffset / 1.5f).coerceIn(0.4f, 3.0f)

                            if (projectionMode == "WALL") {
                                val gridStep = (40f / fovFactor).coerceAtLeast(20f)
                                for (x in 0..w.toInt() step gridStep.toInt()) {
                                    val xShift = (x - cx) * (perspectiveShearX * 0.15f)
                                    drawLine(
                                        color = CosmicCyan.copy(alpha = 0.20f),
                                        start = androidx.compose.ui.geometry.Offset(x.toFloat() + xShift, 0f),
                                        end = androidx.compose.ui.geometry.Offset(x.toFloat() - xShift, h),
                                        strokeWidth = 1f
                                    )
                                }
                                for (y in 0..h.toInt() step gridStep.toInt()) {
                                    val yShift = (y - cy) * (perspectiveShearY * 0.15f)
                                    drawLine(
                                        color = CosmicCyan.copy(alpha = 0.20f),
                                        start = androidx.compose.ui.geometry.Offset(0f, y.toFloat() + yShift),
                                        end = androidx.compose.ui.geometry.Offset(w, y.toFloat() - yShift),
                                        strokeWidth = 1f
                                    )
                                }

                                // Calibrated Surface Anchor Rect
                                val frameW = (180f * depthFactor) / fovFactor
                                val frameH = (180f * depthFactor) / fovFactor
                                drawRect(
                                    color = CosmicCyan.copy(alpha = 0.6f),
                                    topLeft = androidx.compose.ui.geometry.Offset(cx - frameW / 2 + (perspectiveShearX * 30f), cy - frameH / 2 + (perspectiveShearY * 30f)),
                                    size = androidx.compose.ui.geometry.Size(frameW, frameH),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                                )
                            } else {
                                // Mid-Air Calibrated Laser Projection Rays
                                val rayLeftX = cx - (cx * fovFactor) + (perspectiveShearX * 50f)
                                val rayRightX = cx + (cx * fovFactor) + (perspectiveShearX * 50f)

                                drawLine(
                                    color = VioletNeon.copy(alpha = 0.8f),
                                    start = androidx.compose.ui.geometry.Offset(0f, h),
                                    end = androidx.compose.ui.geometry.Offset(cx + (perspectiveShearX * 30f), cy),
                                    strokeWidth = 2.5f
                                )
                                drawLine(
                                    color = VioletNeon.copy(alpha = 0.8f),
                                    start = androidx.compose.ui.geometry.Offset(w, h),
                                    end = androidx.compose.ui.geometry.Offset(cx + (perspectiveShearX * 30f), cy),
                                    strokeWidth = 2.5f
                                )
                                drawLine(
                                    color = CosmicCyan.copy(alpha = 0.5f),
                                    start = androidx.compose.ui.geometry.Offset(cx, h),
                                    end = androidx.compose.ui.geometry.Offset(cx + (perspectiveShearX * 30f), cy),
                                    strokeWidth = 1.5f
                                )
                            }
                        }

                        // Holographic Orb background pulse
                        HolographicPulseOrb(
                            isSpeaking = isSpeaking,
                            isListening = isListening,
                            speechAmplitude = speechAmplitude,
                            userTitle = userTitle,
                            onDismiss = onDismiss,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp)
                        )

                        // Floating Holographic Response Chat Stream Overlay
                        if (!latestMessage.isNullOrBlank() || isProcessing) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .fillMaxWidth(0.92f)
                                    .padding(top = 10.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Black.copy(alpha = 0.85f))
                                    .border(1.dp, VioletNeon, RoundedCornerShape(12.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = if (isProcessing) "⚡ J.A.R.V.I.S. QUANTUM THINKING..." else "💬 $latestMessage",
                                    color = TextGlow,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 3
                                )
                            }
                        }
                    }
                }

                // Interactive HUD Gesture Scale & Position Badge with Reset Chip
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.75f),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CosmicCyan.copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = "MAG: ${String.format("%.2f", holoScale)}x | PAN: (${holoOffsetX.toInt()}, ${holoOffsetY.toInt()})",
                            color = CosmicCyan,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (holoScale != 1.0f || holoOffsetX != 0.0f || holoOffsetY != 0.0f) {
                        Surface(
                            onClick = {
                                holoScale = 1.0f
                                holoOffsetX = 0.0f
                                holoOffsetY = 0.0f
                            },
                            color = PlasmaPink.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PlasmaPink)
                        ) {
                            Text(
                                text = "RESET POS",
                                color = TextGlow,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Tangible Interactive Holographic Touch Keyboard Overlaid on Camera Feed
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(6.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.Black.copy(alpha = 0.88f))
                        .border(1.5.dp, CosmicCyan.copy(alpha = 0.8f), RoundedCornerShape(14.dp))
                        .padding(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Holographic Input Display Box
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(VoidSurface)
                            .border(1.dp, CosmicCyan.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (typedText.isEmpty()) "Holo Touch: Type question or command..." else "$typedText▌",
                            color = if (typedText.isEmpty()) TextMuted else CosmicCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (typedText.isNotEmpty()) {
                                Surface(
                                    onClick = { typedText = "" },
                                    color = PlasmaPink.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text("CLEAR", color = TextGlow, fontSize = 8.sp, modifier = Modifier.padding(4.dp))
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                            }

                            Surface(
                                onClick = { isKeyboardExpanded = !isKeyboardExpanded },
                                color = VoidSurfaceVariant,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = if (isKeyboardExpanded) "▼ HIDE" else "▲ HOLO KEYBOARD",
                                    color = TextGlow,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    if (isKeyboardExpanded) {
                        Spacer(modifier = Modifier.height(4.dp))

                        // Keyboard Keys Layout
                        val rows = if (keyboardMode == "ALPHA") {
                            listOf(
                                listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"),
                                listOf("A", "S", "D", "F", "G", "H", "J", "K", "L"),
                                listOf("Z", "X", "C", "V", "B", "N", "M", "⌫")
                            )
                        } else {
                            listOf(
                                listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
                                listOf("+", "-", "*", "/", "=", "√", "π", "^", "∫"),
                                listOf("(", ")", "[", "]", "{", "}", "#", "$", "⌫")
                            )
                        }

                        for (row in rows) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 1.dp)
                            ) {
                                for (key in row) {
                                    Surface(
                                        onClick = {
                                            if (key == "⌫") {
                                                if (typedText.isNotEmpty()) typedText = typedText.dropLast(1)
                                            } else {
                                                typedText += key
                                            }
                                        },
                                        color = if (key == "⌫") PlasmaPink.copy(alpha = 0.3f) else VoidSurfaceVariant.copy(alpha = 0.9f),
                                        shape = RoundedCornerShape(6.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, CosmicCyan.copy(alpha = 0.4f)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(28.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = key,
                                                color = TextGlow,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Bottom Action Row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 2.dp)
                        ) {
                            // Mode Switcher
                            Surface(
                                onClick = { keyboardMode = if (keyboardMode == "ALPHA") "MATH" else "ALPHA" },
                                color = VioletNeon.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, VioletNeon),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 8.dp)) {
                                    Text(
                                        text = if (keyboardMode == "ALPHA") "∑ 123 MATH" else "ABC TEXT",
                                        color = TextGlow,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // SPACE Key
                            Surface(
                                onClick = { typedText += " " },
                                color = VoidSurfaceVariant,
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, VoidBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(30.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("SPACE", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // TRANSMIT / SEND Query Button
                            Surface(
                                onClick = {
                                    if (typedText.isNotBlank()) {
                                        onSendQuery(typedText)
                                        Toast.makeText(context, "Query Transmitted via Holo Matrix!", Toast.LENGTH_SHORT).show()
                                        typedText = ""
                                    }
                                },
                                color = CosmicCyan,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp)) {
                                    Text("TRANSMIT 🚀", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer AR Hud info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (projectionMode == "AIR") "• MID-AIR VOLUMETRIC LIGHT BEAM" else "• WALL SURFACE PLANE LOCKED",
                    color = StatusGreen,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "DEPTH: ",
                        color = TextMuted,
                        fontSize = 9.sp
                    )
                    Text(
                        text = "${String.format("%.1f", projectionDistance)} METERS",
                        color = CosmicCyan,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
