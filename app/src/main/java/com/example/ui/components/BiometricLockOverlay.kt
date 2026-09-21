package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.ui.JarvisViewModel
import com.example.ui.theme.*

@Composable
fun BiometricLockOverlay(
    isLocked: Boolean,
    statusMessage: String,
    onAuthenticateBiometric: (FragmentActivity) -> Unit,
    onAuthenticatePasscode: (String) -> Boolean,
    modifier: Modifier = Modifier
) {
    if (!isLocked) return

    val context = LocalContext.current
    var showPasscodeModal by remember { mutableStateOf(false) }
    var passcodeText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val infiniteTransition = rememberInfiniteTransition(label = "BiometricPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GalaxyVoid.copy(alpha = 0.96f))
            .clickable(enabled = true, onClick = {}) // Block touch pass-through when locked
            .testTag("biometric_lock_overlay"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Animated Holographic Biometric Pulse Ring
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(130.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                VioletNeon.copy(alpha = 0.35f),
                                CosmicCyan.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    )
                    .border(2.dp, Brush.sweepGradient(listOf(CosmicCyan, VioletNeon, StatusGold, CosmicCyan)), CircleShape)
            ) {
                IconButton(
                    onClick = {
                        val activity = context as? FragmentActivity
                        if (activity != null) {
                            onAuthenticateBiometric(activity)
                        } else {
                            Toast.makeText(context, "FragmentActivity not available, enter Passcode", Toast.LENGTH_SHORT).show()
                            showPasscodeModal = true
                        }
                    },
                    modifier = Modifier.size(90.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Biometric Scan",
                        tint = CosmicCyan,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "⚡ J.A.R.V.I.S. CORE MATRIX SECURED",
                color = TextGlow,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "LEVEL 5 STARK BIOMETRIC CLEARANCE REQUIRED",
                color = VioletNeon,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Status message pill
            Surface(
                color = VoidSurfaceVariant,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, VoidBorder)
            ) {
                Text(
                    text = statusMessage,
                    color = StatusGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Primary Scan Button
            Button(
                onClick = {
                    val activity = context as? FragmentActivity
                    if (activity != null) {
                        onAuthenticateBiometric(activity)
                    } else {
                        Toast.makeText(context, "Opening Passcode Input...", Toast.LENGTH_SHORT).show()
                        showPasscodeModal = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = VioletNeon),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(48.dp)
                    .testTag("scan_biometric_button")
            ) {
                Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = TextGlow)
                Spacer(modifier = Modifier.width(8.dp))
                Text("SCAN FINGERPRINT / BIOMETRICS", color = TextGlow, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Passcode Fallback Button
            OutlinedButton(
                onClick = { showPasscodeModal = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CosmicCyan),
                border = androidx.compose.foundation.BorderStroke(1.dp, CosmicCyan),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(44.dp)
                    .testTag("passcode_fallback_button")
            ) {
                Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = CosmicCyan, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("ENTER STARK PASSCODE (PIN)", color = CosmicCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    // PASSCODE INPUT DIALOG MODAL
    if (showPasscodeModal) {
        AlertDialog(
            onDismissRequest = { showPasscodeModal = false },
            containerColor = VoidSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = "Lock", tint = VioletNeon)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("STARK CORE PASSCODE", color = TextGlow, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text("Enter Security Passcode (Default: 3000):", color = TextMuted, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = passcodeText,
                        onValueChange = {
                            passcodeText = it
                            errorMessage = null
                        },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        label = { Text("Passcode PIN", color = TextMuted, fontSize = 10.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CosmicCyan,
                            unfocusedBorderColor = VoidBorder,
                            focusedTextColor = TextGlow,
                            unfocusedTextColor = TextGlow
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("passcode_input_field")
                    )

                    errorMessage?.let { err ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(err, color = StatusRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val success = onAuthenticatePasscode(passcodeText)
                        if (success) {
                            showPasscodeModal = false
                            passcodeText = ""
                            Toast.makeText(context, "Passcode Verified! Access Granted.", Toast.LENGTH_SHORT).show()
                        } else {
                            errorMessage = "Invalid Passcode. Hint: 3000"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicCyan)
                ) {
                    Text("VERIFY", color = GalaxyVoid, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasscodeModal = false }) {
                    Text("CANCEL", color = TextMuted, fontSize = 11.sp)
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BiometricLockOverlayPreview() {
    JarvisTheme {
        BiometricLockOverlay(
            isLocked = true,
            statusMessage = "Biometric Verification Required",
            onAuthenticateBiometric = {},
            onAuthenticatePasscode = { true }
        )
    }
}
