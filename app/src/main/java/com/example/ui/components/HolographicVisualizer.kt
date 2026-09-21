package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class Particle(
    var x: Float,
    var y: Float,
    var radius: Float,
    var speedX: Float,
    var speedY: Float,
    var alpha: Float,
    val color: Color
)

@Composable
fun HolographicVisualizer(
    modifier: Modifier = Modifier,
    isSpeaking: Boolean = false,
    isListening: Boolean = false,
    speechAmplitude: Float = 0f,
    isDimmed: Boolean = false
) {
    val targetAlpha by animateFloatAsState(
        targetValue = if (isDimmed) 0.08f else 1.0f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "ProximityDimAlpha"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "HologramTransition")
    
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Rotation"
    )

    val reverseRotationAngle by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ReverseRotation"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Pulse"
    )

    val dashEffectOuter = remember { PathEffect.dashPathEffect(floatArrayOf(20f, 15f, 5f, 15f), 0f) }
    val dashEffectInner = remember { PathEffect.dashPathEffect(floatArrayOf(40f, 20f), 0f) }

    val particles = remember {
        List(25) {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                radius = Random.nextFloat() * 3.5f + 1.5f,
                speedX = (Random.nextFloat() - 0.5f) * 0.001f,
                speedY = (Random.nextFloat() - 0.5f) * 0.001f,
                alpha = Random.nextFloat() * 0.8f + 0.2f,
                color = if (Random.nextBoolean()) CosmicCyan else VioletLight
            )
        }
    }

    Canvas(modifier = modifier.graphicsLayer { alpha = targetAlpha }) {
        val width = size.width
        val height = size.height
        val center = Offset(width / 2f, height / 2f)
        val maxRadius = minOf(width, height) / 2.2f

        // Draw Cosmic Background Glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    VioletNeon.copy(alpha = 0.25f),
                    GalaxyDarkPurple.copy(alpha = 0.1f),
                    Color.Transparent
                ),
                center = center,
                radius = maxRadius * 1.5f
            ),
            center = center,
            radius = maxRadius * 1.5f
        )

        // Draw Moving Galactic Particle Matrix
        particles.forEach { p ->
            p.x = (p.x + p.speedX).let { if (it < 0) 1f else if (it > 1) 0f else it }
            p.y = (p.y + p.speedY).let { if (it < 0) 1f else if (it > 1) 0f else it }
            val px = p.x * width
            val py = p.y * height
            drawCircle(
                color = p.color.copy(alpha = p.alpha * 0.8f),
                radius = p.radius,
                center = Offset(px, py)
            )
        }

        // Concentric Holographic Ring 1 - Dashed Outer HUD Ring
        rotate(rotationAngle, center) {
            drawCircle(
                color = CosmicCyan.copy(alpha = 0.4f),
                radius = maxRadius * pulseScale,
                center = center,
                style = Stroke(
                    width = 2f,
                    pathEffect = dashEffectOuter
                )
            )
        }

        // Concentric Holographic Ring 2 - Reverse Ring
        rotate(reverseRotationAngle, center) {
            drawCircle(
                color = VioletNeon.copy(alpha = 0.5f),
                radius = maxRadius * 0.82f,
                center = center,
                style = Stroke(
                    width = 3f,
                    pathEffect = dashEffectInner
                )
            )
        }

        // Inner Arc Reactor Center Ring
        val ampBonus = if (isSpeaking || isListening) (speechAmplitude * 0.3f) else 0f
        val coreRadius = maxRadius * (0.45f + ampBonus)

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    if (isListening) StatusGreen.copy(alpha = 0.8f) else ArcReactorBlue.copy(alpha = 0.8f),
                    VioletNeon.copy(alpha = 0.4f),
                    Color.Transparent
                ),
                center = center,
                radius = coreRadius
            ),
            center = center,
            radius = coreRadius
        )

        // Audio Frequency Wave Bars (32 circular radiating sound bars)
        val numBars = 32
        for (i in 0 until numBars) {
            val angle = (i * 360f / numBars) * (Math.PI / 180f)
            val barLen = if (isSpeaking || isListening) {
                15f + (sin(i.toDouble() * 0.8 + rotationAngle * 0.1).toFloat() * 25f) + (speechAmplitude * 40f)
            } else {
                8f + sin(i.toDouble() * 0.5 + rotationAngle * 0.05).toFloat() * 6f
            }

            val startRadius = maxRadius * 0.85f
            val endRadius = startRadius + barLen

            val startX = center.x + cos(angle).toFloat() * startRadius
            val startY = center.y + sin(angle).toFloat() * startRadius
            val endX = center.x + cos(angle).toFloat() * endRadius
            val endY = center.y + sin(angle).toFloat() * endRadius

            drawLine(
                color = if (isListening) StatusGreen else if (isSpeaking) PlasmaPink else CosmicCyan,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 3f
            )
        }
    }
}
