package com.example.data.telemetry

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DiagnosticActionLog(
    val id: Long = System.currentTimeMillis(),
    val timestamp: String = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date()),
    val tag: String, // "AI ACTION", "FILE MOD", "SERVER WSS", "SYSTEM"
    val title: String,
    val details: String,
    val status: String = "SUCCESS" // "SUCCESS", "PROCESSING", "ONLINE", "ERROR", "ACTIVE"
)
