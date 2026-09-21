package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun HolographicPulseOrb(
    modifier: Modifier = Modifier,
    isSpeaking: Boolean = false,
    isListening: Boolean = false,
    speechAmplitude: Float = 0f,
    userTitle: String = "Sir",
    onDismiss: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "PulseOrb3D")

    // Rotation angles for 3D orbital rings & spinning Arc Reactor armor
    val spinFastAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SpinFast"
    )

    val counterSpinAngle by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "CounterSpin"
    )

    val ringAngleX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "AngleX"
    )

    val ringAngleY by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "AngleY"
    )

    val corePulse by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "CorePulse"
    )

    val vibrationPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(250, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "VibrationPhase"
    )

    val floatingPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "FloatingPhase"
    )

    val laserBeamAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LaserAlpha"
    )

    val dashEffect = remember { PathEffect.dashPathEffect(floatArrayOf(15f, 10f, 5f, 10f), 0f) }

    val orbParticles = remember {
        List(30) {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                radius = Random.nextFloat() * 3f + 1.5f,
                speedX = (Random.nextFloat() - 0.5f) * 0.002f,
                speedY = (Random.nextFloat() - 0.5f) * 0.002f,
                alpha = Random.nextFloat() * 0.8f + 0.2f,
                color = if (Random.nextBoolean()) CosmicCyan else VioletLight
            )
        }
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = VoidSurface.copy(alpha = 0.95f)),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    colors = listOf(VioletNeon, CosmicCyan, PlasmaPink)
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .testTag("hologram_pulse_orb_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // HUD Header Bar
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
                            .background(VioletNeon.copy(alpha = 0.25f))
                            .border(1.dp, VioletNeon, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sensors,
                            contentDescription = "Holo Emitter",
                            tint = CosmicCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = "HOLOGRAPHIC PULSE ORB",
                            color = TextGlow,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "3D Quantum Arc Projection • $userTitle",
                            color = CosmicCyan,
                            fontSize = 9.sp
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(VoidSurfaceVariant)
                        .testTag("close_hologram_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss Hologram",
                        tint = TextGlow,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3D Holographic Canvas Projection Box
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(GalaxyVoid.copy(alpha = 0.8f))
                    .border(1.dp, VioletNeon.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Dynamic 3D Floating + Kinetic Vibration Offsets
                    val floatY = sin(floatingPhase) * 10f
                    val floatX = cos(floatingPhase * 0.7f) * 6f
                    val vibeX = if (isSpeaking || isListening) sin(vibrationPhase * 12f) * 5f else sin(vibrationPhase * 6f) * 2f
                    val vibeY = if (isSpeaking || isListening) cos(vibrationPhase * 15f) * 5f else cos(vibrationPhase * 8f) * 2f

                    val center = Offset(w / 2f + floatX + vibeX, h / 2f - 15f + floatY + vibeY)
                    val baseCenter = Offset(w / 2f, h - 25f)

                    // 1. Light Emitter Base Cone Beam
                    val beamPath = Path().apply {
                        moveTo(baseCenter.x - 45f, baseCenter.y)
                        lineTo(center.x - 85f, center.y + 20f)
                        lineTo(center.x + 85f, center.y + 20f)
                        lineTo(baseCenter.x + 45f, baseCenter.y)
                        close()
                    }

                    drawPath(
                        path = beamPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                VioletNeon.copy(alpha = 0.05f),
                                CosmicCyan.copy(alpha = 0.35f * laserBeamAlpha),
                                VioletNeon.copy(alpha = 0.7f * laserBeamAlpha)
                            )
                        )
                    )

                    // 2. Base Pedestal Ring Emitter
                    drawOval(
                        color = CosmicCyan.copy(alpha = 0.8f),
                        topLeft = Offset(baseCenter.x - 55f, baseCenter.y - 12f),
                        size = Size(110f, 24f),
                        style = Stroke(width = 2.5f)
                    )
                    drawOval(
                        color = VioletNeon.copy(alpha = 0.5f),
                        topLeft = Offset(baseCenter.x - 40f, baseCenter.y - 8f),
                        size = Size(80f, 16f),
                        style = Stroke(width = 1.5f)
                    )

                    // 3. Floating Orb Particles
                    orbParticles.forEach { p ->
                        p.x = (p.x + p.speedX).let { if (it < 0) 1f else if (it > 1) 0f else it }
                        p.y = (p.y + p.speedY).let { if (it < 0) 1f else if (it > 1) 0f else it }
                        val px = p.x * w
                        val py = p.y * h
                        drawCircle(
                            color = p.color.copy(alpha = p.alpha * 0.9f),
                            radius = p.radius,
                            center = Offset(px, py)
                        )
                    }

                    // 4. 3D Elliptical Orbital Rings
                    val ampBonus = if (isSpeaking || isListening) (speechAmplitude * 25f) else 0f
                    val orbRadius = 70f * corePulse + ampBonus

                    // Fast Rotating Arc Reactor Outer Armor Ring
                    rotate(spinFastAngle, center) {
                        for (i in 0 until 10) {
                            val angleRad = (i * 36f) * (Math.PI / 180f)
                            val rOuter = orbRadius * 1.45f
                            val rInner = orbRadius * 1.25f
                            val sx = center.x + cos(angleRad).toFloat() * rInner
                            val sy = center.y + sin(angleRad).toFloat() * rInner
                            val ex = center.x + cos(angleRad).toFloat() * rOuter
                            val ey = center.y + sin(angleRad).toFloat() * rOuter

                            drawLine(
                                color = CosmicCyan,
                                start = Offset(sx, sy),
                                end = Offset(ex, ey),
                                strokeWidth = 3.5f
                            )
                        }
                    }

                    // Counter Rotating Inner Gear Armor
                    rotate(counterSpinAngle, center) {
                        drawCircle(
                            color = VioletNeon.copy(alpha = 0.8f),
                            center = center,
                            radius = orbRadius * 1.15f,
                            style = Stroke(width = 2f, pathEffect = dashEffect)
                        )
                    }

                    // Orbital Ring 1 (Horizontal Inclined)
                    rotate(ringAngleX, center) {
                        drawOval(
                            color = CosmicCyan.copy(alpha = 0.85f),
                            topLeft = Offset(center.x - orbRadius * 1.3f, center.y - orbRadius * 0.45f),
                            size = Size(orbRadius * 2.6f, orbRadius * 0.9f),
                            style = Stroke(width = 2.5f, pathEffect = dashEffect)
                        )
                    }

                    // Orbital Ring 2 (Vertical Cross Inclined)
                    rotate(ringAngleY, center) {
                        drawOval(
                            color = VioletNeon.copy(alpha = 0.9f),
                            topLeft = Offset(center.x - orbRadius * 0.5f, center.y - orbRadius * 1.3f),
                            size = Size(orbRadius * 1.0f, orbRadius * 2.6f),
                            style = Stroke(width = 2.5f, pathEffect = dashEffect)
                        )
                    }

                    // Orbital Ring 3 (45 deg Diagonal)
                    rotate(ringAngleX * 0.7f + 45f, center) {
                        drawOval(
                            color = PlasmaPink.copy(alpha = 0.7f),
                            topLeft = Offset(center.x - orbRadius * 1.1f, center.y - orbRadius * 0.6f),
                            size = Size(orbRadius * 2.2f, orbRadius * 1.2f),
                            style = Stroke(width = 1.8f)
                        )
                    }

                    // 5. Core Arc Reactor Energy Ball
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White,
                                if (isListening) StatusGreen else CosmicCyan,
                                VioletNeon.copy(alpha = 0.85f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = orbRadius * 0.65f
                        ),
                        center = center,
                        radius = orbRadius * 0.65f
                    )

                    // 6. Audio Waveform Spectrum Spokes (16 Radial Frequency Rays)
                    val spokes = 16
                    for (i in 0 until spokes) {
                        val angleRad = (i * 360f / spokes) * (Math.PI / 180f)
                        val rayLen = if (isSpeaking || isListening) {
                            12f + (sin(i.toDouble() * 1.2 + ringAngleX * 0.1).toFloat() * 18f) + (speechAmplitude * 30f)
                        } else {
                            6f + sin(i.toDouble() * 0.8 + ringAngleX * 0.05).toFloat() * 6f
                        }
                        val rStart = orbRadius * 0.65f
                        val rEnd = rStart + rayLen

                        val sx = center.x + cos(angleRad).toFloat() * rStart
                        val sy = center.y + sin(angleRad).toFloat() * rStart
                        val ex = center.x + cos(angleRad).toFloat() * rEnd
                        val ey = center.y + sin(angleRad).toFloat() * rEnd

                        drawLine(
                            color = if (isListening) StatusGreen else CosmicCyan,
                            start = Offset(sx, sy),
                            end = Offset(ex, ey),
                            strokeWidth = 2.5f
                        )
                    }
                }

                // Overlay Callout Floating Telemetry Cards
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(8.dp)
                ) {
                    Surface(
                        color = VoidSurface.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CosmicCyan.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "ARC TEMP: 302.4 K",
                            color = CosmicCyan,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }

                    Surface(
                        color = VoidSurface.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, VioletNeon.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "SYNC: HARMONIC 432Hz",
                            color = VioletLight,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer Status & Quick Command
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Active",
                        tint = StatusGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Orb Field Online • Responding to 'Project Holo'",
                        color = StatusGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VioletNeon),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("CLOSE ORB", color = VioletNeon, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
