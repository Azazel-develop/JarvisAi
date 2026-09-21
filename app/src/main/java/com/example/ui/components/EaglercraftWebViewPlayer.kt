package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.*

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun EaglercraftWebViewPlayer(
    serverWssUrl: String,
    serverName: String,
    minecraftVersion: String,
    onClosePlayer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var loadingProgress by remember { mutableIntStateOf(0) }
    var activeClientUrl by remember {
        mutableStateOf(
            if (minecraftVersion.contains("1.8")) {
                "https://eaglercraft.com/mc/1.8.8/?server=${Uri.encode(serverWssUrl)}"
            } else {
                "https://eaglercraft.com/mc/1.20.4/?server=${Uri.encode(serverWssUrl)}"
            }
        )
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = VoidSurface),
        modifier = modifier
            .fillMaxSize()
            .border(1.5.dp, CosmicCyan, RoundedCornerShape(16.dp))
            .padding(8.dp)
            .testTag("eaglercraft_webview_player")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Player Top Control Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(StatusGreen, shape = androidx.compose.foundation.shape.CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "EAGLERCRAFT WEB PLAYER - $serverName",
                                color = TextGlow,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "WSS Proxy: $serverWssUrl",
                            color = CosmicCyan,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Open External Browser Option
                        IconButton(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(activeClientUrl)).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Browser launch failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Icon(imageVector = Icons.Default.OpenInBrowser, contentDescription = "Browser", tint = CosmicCyan)
                        }

                        // Close Player Button
                        IconButton(onClick = onClosePlayer) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = StatusRed)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Client Selector Preset Chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val clientPresets = listOf(
                        "🚀 Astra Client" to "https://eaglercraft.com/mc/1.8.8/?server=${Uri.encode(serverWssUrl)}&client=astra",
                        "⚡ Eagler 1.20" to "https://eaglercraft.com/mc/1.20.4/?server=${Uri.encode(serverWssUrl)}",
                        "⚔️ Eagler 1.8.8" to "https://eaglercraft.com/mc/1.8.8/?server=${Uri.encode(serverWssUrl)}",
                        "🔥 Resent Client" to "https://eaglercraft.com/mc/1.8.8/?server=${Uri.encode(serverWssUrl)}&client=resent",
                        "🎯 Precision" to "https://eaglercraft.com/mc/1.8.8/?server=${Uri.encode(serverWssUrl)}&client=precision"
                    )

                    clientPresets.forEach { (label, url) ->
                        val isSelected = activeClientUrl == url
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                activeClientUrl = url
                                isLoading = true
                                Toast.makeText(context, "Switching client to $label...", Toast.LENGTH_SHORT).show()
                            },
                            label = { Text(label, fontSize = 8.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VioletNeon,
                                containerColor = VoidSurfaceVariant,
                                selectedLabelColor = TextGlow,
                                labelColor = TextMuted
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(26.dp)
                        )
                    }
                }
            }

            if (isLoading) {
                LinearProgressIndicator(
                    progress = { loadingProgress / 100f },
                    color = VioletNeon,
                    trackColor = GalaxyVoid,
                    modifier = Modifier.fillMaxWidth().height(4.dp)
                )
            }

            // Embedded HTML5 Eaglercraft Web Client Player
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
                        tag = activeClientUrl
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            allowFileAccess = true
                            allowContentAccess = true
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            mediaPlaybackRequiresUserGesture = false
                            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            setSupportZoom(false)
                            builtInZoomControls = false
                            userAgentString = "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
                        }
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                isLoading = false
                            }
                        }
                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                loadingProgress = newProgress
                                if (newProgress >= 100) isLoading = false
                            }
                            override fun onPermissionRequest(request: android.webkit.PermissionRequest?) {
                                request?.grant(request.resources)
                            }
                        }
                        loadUrl(activeClientUrl)
                    }
                },
                update = { webView ->
                    val currentTag = webView.tag as? String
                    if (currentTag != activeClientUrl) {
                        webView.tag = activeClientUrl
                        webView.loadUrl(activeClientUrl)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
            )
        }
    }
}
