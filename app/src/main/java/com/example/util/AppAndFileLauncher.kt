package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast

object AppAndFileLauncher {

    data class LaunchResult(
        val success: Boolean,
        val appName: String,
        val message: String
    )

    fun openAppOrAction(context: Context, query: String): LaunchResult {
        val lower = query.lowercase().trim()

        return try {
            when {
                // Direct HTTP / HTTPS / WSS URLs or Web Domains
                lower.startsWith("http://") || lower.startsWith("https://") || lower.startsWith("wss://") -> {
                    val cleanUrl = if (lower.startsWith("wss://")) lower.replace("wss://", "https://") else query
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(cleanUrl)).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                    context.startActivity(intent)
                    LaunchResult(true, "Web Browser", "Navigated directly to $cleanUrl")
                }

                // Explicit Website Domains (e.g. github.com, modrinth.com)
                lower.endsWith(".com") || lower.endsWith(".org") || lower.endsWith(".net") || lower.endsWith(".io") || lower.endsWith(".gg") || lower.endsWith(".dev") -> {
                    val url = if (!lower.startsWith("http")) "https://$query" else query
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                    context.startActivity(intent)
                    LaunchResult(true, "Web Browser", "Navigated directly to $url")
                }

                // Modrinth
                lower.contains("modrinth") -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://modrinth.com/plugins")).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                    context.startActivity(intent)
                    LaunchResult(true, "Modrinth", "Opened Modrinth Plugin Portal")
                }

                // CurseForge
                lower.contains("curseforge") || lower.contains("curse forge") -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.curseforge.com/minecraft/mc-mods")).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                    context.startActivity(intent)
                    LaunchResult(true, "CurseForge", "Opened CurseForge Minecraft Mods & Plugins")
                }

                // PlanetMinecraft
                lower.contains("planetminecraft") || lower.contains("planet minecraft") -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.planetminecraft.com/resources/mods/")).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                    context.startActivity(intent)
                    LaunchResult(true, "PlanetMinecraft", "Opened PlanetMinecraft Resource Repository")
                }

                // Eaglercraft
                lower.contains("eaglercraft") || lower.contains("eagler") -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://eaglercraft.com")).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                    context.startActivity(intent)
                    LaunchResult(true, "Eaglercraft", "Opened Eaglercraft Web Client Matrix")
                }

                // GitHub
                lower.contains("github") -> {
                    val intent = context.packageManager.getLaunchIntentForPackage("com.github.android")
                        ?: Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com"))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    LaunchResult(true, "GitHub", "Launched GitHub")
                }

                // Discord
                lower.contains("discord") -> {
                    val intent = context.packageManager.getLaunchIntentForPackage("com.discord")
                        ?: Intent(Intent.ACTION_VIEW, Uri.parse("https://discord.com"))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    LaunchResult(true, "Discord", "Launched Discord")
                }

                // Settings
                lower.contains("setting") -> {
                    val intent = Intent(Settings.ACTION_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                    context.startActivity(intent)
                    LaunchResult(true, "Settings", "Opened Android System Settings")
                }

                // Camera
                lower.contains("camera") -> {
                    val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                    context.startActivity(intent)
                    LaunchResult(true, "Camera", "Opened System Camera")
                }

                // Clock / Alarm
                lower.contains("clock") || lower.contains("alarm") || lower.contains("timer") -> {
                    val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                    context.startActivity(intent)
                    LaunchResult(true, "Clock", "Opened System Clock & Alarms")
                }

                // YouTube
                lower.contains("youtube") -> {
                    val intent = context.packageManager.getLaunchIntentForPackage("com.google.android.youtube")
                        ?: Intent(Intent.ACTION_VIEW, Uri.parse("https://youtube.com"))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    LaunchResult(true, "YouTube", "Launched YouTube App")
                }

                // Chrome / Web Browser
                lower.contains("chrome") || lower.contains("browser") || lower.contains("google") || lower.contains("web") -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com")).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                    context.startActivity(intent)
                    LaunchResult(true, "Browser", "Opened Chrome / Web Browser")
                }

                // Gmail / Mail
                lower.contains("gmail") || lower.contains("email") || lower.contains("mail") -> {
                    val intent = context.packageManager.getLaunchIntentForPackage("com.google.android.gm")
                        ?: Intent(Intent.ACTION_VIEW, Uri.parse("mailto:"))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    LaunchResult(true, "Gmail", "Opened Gmail")
                }

                // WhatsApp
                lower.contains("whatsapp") -> {
                    val intent = context.packageManager.getLaunchIntentForPackage("com.whatsapp")
                        ?: Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com"))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    LaunchResult(true, "WhatsApp", "Opened WhatsApp")
                }

                // Maps
                lower.contains("map") || lower.contains("navigation") || lower.contains("location") -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=maps")).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                    context.startActivity(intent)
                    LaunchResult(true, "Google Maps", "Opened Google Maps")
                }

                // Calculator
                lower.contains("calculator") || lower.contains("calc") -> {
                    val intent = context.packageManager.getLaunchIntentForPackage("com.google.android.calculator")
                        ?: Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_APP_CALCULATOR) }
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    LaunchResult(true, "Calculator", "Opened Calculator")
                }

                // Files / Storage
                lower.contains("file") || lower.contains("folder") || lower.contains("download") -> {
                    val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                        type = "*/*"
                        addCategory(Intent.CATEGORY_OPENABLE)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(Intent.createChooser(intent, "Open File Explorer").apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                    LaunchResult(true, "File Manager", "Opened Device Files & Storage Explorer")
                }

                // Generic Package Search on Device
                else -> {
                    val pm = context.packageManager
                    val installedApps = pm.getInstalledApplications(0)
                    val targetApp = installedApps.firstOrNull { app ->
                        val label = pm.getApplicationLabel(app).toString().lowercase()
                        label.contains(lower) || app.packageName.lowercase().contains(lower)
                    }

                    if (targetApp != null) {
                        val launchIntent = pm.getLaunchIntentForPackage(targetApp.packageName)
                        if (launchIntent != null) {
                            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(launchIntent)
                            val name = pm.getApplicationLabel(targetApp).toString()
                            LaunchResult(true, name, "Launched $name (${targetApp.packageName})")
                        } else {
                            LaunchResult(false, query, "Could not obtain launch intent for target app")
                        }
                    } else {
                        // Fallback open web search or browser for app
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + Uri.encode(query)))
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                        LaunchResult(true, "Web Browser Search", "Opened Web Query for '$query'")
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LaunchResult(false, query, "Unable to launch app or action: ${e.message}")
        }
    }

    fun openTextFileContent(context: Context, fileName: String, content: String) {
        try {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_TEXT, content)
                putExtra(Intent.EXTRA_SUBJECT, "J.A.R.V.I.S. File: $fileName")
                type = "text/plain"
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val shareIntent = Intent.createChooser(sendIntent, "Open or Share $fileName").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(shareIntent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "File Action: $fileName preview ready", Toast.LENGTH_SHORT).show()
        }
    }
}
