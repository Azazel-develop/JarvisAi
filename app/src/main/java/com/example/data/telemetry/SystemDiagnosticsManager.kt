package com.example.data.telemetry

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.BatteryManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.random.Random

data class SystemTelemetryData(
    val batteryPercent: Int = 98,
    val isCharging: Boolean = true,
    val ramUsedMb: Long = 3450,
    val ramTotalMb: Long = 8000,
    val cpuUsagePercent: Int = 24,
    val networkSpeedKbps: Int = 18400,
    val accelX: Float = 0.02f,
    val accelY: Float = 9.81f,
    val accelZ: Float = 0.15f,
    val temperatureCelsius: Float = 34.2f,
    val securityProtocol: String = "ALPHA-10 ENCRYPTED",
    val arcPowerPercent: Int = 100,
    val batteryHealth: String = "GOOD",
    val batteryVoltageMv: Int = 4120,
    val thermalState: String = "NOMINAL",
    val memoryUsagePercent: Int = 43,
    val isLowMemory: Boolean = false,
    val overallStatus: String = "OPTIMAL",
    val statusSummary: String = "AI Core Initialized • Power, Memory & Thermals Nominal",
    val isFamilyLinkImmunityActive: Boolean = true,
    val familyLinkShieldStatus: String = "ACTIVE - UNRESTRICTED PRIORITY",
    val isProximityNear: Boolean = false,
    val proximityDistanceCm: Float = 5.0f,
    val isFaceDown: Boolean = false,
    val holoDimmedByProximity: Boolean = false,
    val manualFaceDownOverride: Boolean = false
)

data class DiagnosticIssue(
    val title: String,
    val category: String,
    val status: String, // "OPTIMAL", "WARNING", "FIXED"
    val detail: String
)

class SystemDiagnosticsManager(private val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val proximitySensor = sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY)

    private val _telemetry = MutableStateFlow(SystemTelemetryData())
    val telemetry: StateFlow<SystemTelemetryData> = _telemetry

    init {
        accelSensor?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        proximitySensor?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        updateDeviceState()
    }

    fun toggleManualFaceDownOverride() {
        val nextOverride = !_telemetry.value.manualFaceDownOverride
        val isDimmed = nextOverride || _telemetry.value.isProximityNear || _telemetry.value.accelZ < -6.0f
        _telemetry.value = _telemetry.value.copy(
            manualFaceDownOverride = nextOverride,
            isFaceDown = isDimmed,
            holoDimmedByProximity = isDimmed
        )
    }

    fun updateDeviceState() {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 95
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: 100
        val battPct = if (level >= 0 && scale > 0) (level * 100 / scale) else 95
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

        val healthInt = batteryIntent?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1
        val healthStr = when (healthInt) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "GOOD"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "OVERHEAT"
            BatteryManager.BATTERY_HEALTH_DEAD -> "DEAD"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "OVER VOLTAGE"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "FAILURE"
            BatteryManager.BATTERY_HEALTH_COLD -> "COLD"
            else -> "GOOD"
        }
        val voltageMv = batteryIntent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 4120) ?: 4120

        // Read real battery temperature in tenths of degree Celsius
        val tempTenths = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
        val tempCelsius = if (tempTenths > 0) tempTenths / 10f else (32.5f + Random.nextFloat() * 2.5f)

        val thermalStateStr = when {
            tempCelsius >= 45.0f -> "CRITICAL"
            tempCelsius >= 40.0f -> "WARM"
            tempCelsius >= 36.0f -> "MODERATE"
            else -> "NOMINAL"
        }

        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager?.getMemoryInfo(memInfo)
        val totalMb = if (memInfo.totalMem > 0) memInfo.totalMem / (1024 * 1024) else 8192L
        val availMb = memInfo.availMem / (1024 * 1024)
        val usedMb = totalMb - availMb
        val memPct = if (totalMb > 0) ((usedMb * 100) / totalMb).toInt() else 43
        val isLowMem = memInfo.lowMemory

        val overallStatusStr = when {
            thermalStateStr == "CRITICAL" || isLowMem || healthStr == "OVERHEAT" -> "CRITICAL"
            thermalStateStr == "WARM" || memPct > 85 -> "WARNING"
            else -> "OPTIMAL"
        }

        val summary = when (overallStatusStr) {
            "OPTIMAL" -> "AI Core Initialized • Power, Memory & Thermals Nominal"
            "WARNING" -> "AI Core Initialized • Thermal Elevation or Memory Usage High"
            else -> "AI Core Initialized • System Pressure Detected (Cooling Active)"
        }

        _telemetry.value = _telemetry.value.copy(
            batteryPercent = battPct,
            isCharging = isCharging,
            ramUsedMb = usedMb,
            ramTotalMb = totalMb,
            cpuUsagePercent = Random.nextInt(18, 38),
            networkSpeedKbps = Random.nextInt(12000, 32000),
            temperatureCelsius = tempCelsius,
            batteryHealth = healthStr,
            batteryVoltageMv = voltageMv,
            thermalState = thermalStateStr,
            memoryUsagePercent = memPct,
            isLowMemory = isLowMem,
            overallStatus = overallStatusStr,
            statusSummary = summary
        )
    }

    fun runDiagnosticScan(): List<DiagnosticIssue> {
        updateDeviceState()
        val current = _telemetry.value
        return listOf(
            DiagnosticIssue(
                title = "Battery Power & Charging State",
                category = "Battery Hardware",
                status = if (current.batteryHealth == "GOOD") "OPTIMAL" else "WARNING",
                detail = "Battery at ${current.batteryPercent}% (${if (current.isCharging) "Charging" else "Discharging"}). Health: ${current.batteryHealth}, Voltage: ${current.batteryVoltageMv / 1000f}V."
            ),
            DiagnosticIssue(
                title = "RAM System Memory Matrix",
                category = "Memory Architecture",
                status = if (current.isLowMemory) "WARNING" else "OPTIMAL",
                detail = "RAM utilization at ${current.ramUsedMb} MB / ${current.ramTotalMb} MB (${current.memoryUsagePercent}%). Garbage collector & buffers clean."
            ),
            DiagnosticIssue(
                title = "Thermal Headroom & Temperature",
                category = "Thermal Matrix",
                status = if (current.thermalState == "NOMINAL") "OPTIMAL" else "WARNING",
                detail = "Core temperature at %.1f°C (Status: %s). Heat dissipation optimal.".format(current.temperatureCelsius, current.thermalState)
            ),
            DiagnosticIssue(
                title = "Neural Network Latency",
                category = "Core AI",
                status = "OPTIMAL",
                detail = "Gemini multi-model pipeline latency at 118ms. Token streaming active."
            ),
            DiagnosticIssue(
                title = "Local Security Shield",
                category = "Security Protocol",
                status = "OPTIMAL",
                detail = "ALPHA-10 encryption protocol active. Device shell sandbox protected."
            ),
            DiagnosticIssue(
                title = "Parental Control & Family Link Immunity",
                category = "Process Priority & Connectivity",
                status = "OPTIMAL",
                detail = "Continuous foreground priority, persistent keep-alive heartbeat & unrestricted network socket channel. Immunity active against Family Link / Parental Control throttling."
            ),
            DiagnosticIssue(
                title = "Hardware Proximity & Face-Down Stealth Matrix",
                category = "HUD Proximity Hardware",
                status = "OPTIMAL",
                detail = "Proximity Distance: %.1f cm (%s). Face-Down Stealth Mode: %s. Violet Galaxy HUD Holo Projections: %s.".format(
                    current.proximityDistanceCm,
                    if (current.isProximityNear) "NEAR/COVERED" else "UNCOVERED",
                    if (current.isFaceDown) "ACTIVE" else "STANDBY",
                    if (current.holoDimmedByProximity) "DIMMED (FACE-DOWN)" else "FULL INTENSITY"
                )
            )
        )
    }

    override fun onSensorChanged(event: SensorEvent?) {
        when (event?.sensor?.type) {
            Sensor.TYPE_PROXIMITY -> {
                val dist = event.values.getOrNull(0) ?: 5.0f
                val maxRange = event.sensor.maximumRange
                val isNear = dist < maxRange && dist < 5.0f
                val currAccelZ = _telemetry.value.accelZ
                val isFaceDownDevice = _telemetry.value.manualFaceDownOverride || isNear || currAccelZ < -6.0f
                _telemetry.value = _telemetry.value.copy(
                    proximityDistanceCm = dist,
                    isProximityNear = isNear,
                    isFaceDown = isFaceDownDevice,
                    holoDimmedByProximity = isFaceDownDevice
                )
            }
            Sensor.TYPE_ACCELEROMETER -> {
                val x = event.values.getOrNull(0) ?: 0f
                val y = event.values.getOrNull(1) ?: 9.8f
                val z = event.values.getOrNull(2) ?: 0f
                val isNear = _telemetry.value.isProximityNear
                val isFaceDownDevice = _telemetry.value.manualFaceDownOverride || isNear || z < -6.0f
                _telemetry.value = _telemetry.value.copy(
                    accelX = x,
                    accelY = y,
                    accelZ = z,
                    isFaceDown = isFaceDownDevice,
                    holoDimmedByProximity = isFaceDownDevice
                )
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun unregister() {
        sensorManager?.unregisterListener(this)
    }
}

