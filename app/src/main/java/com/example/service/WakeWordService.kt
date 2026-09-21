package com.example.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.local.JarvisDatabase
import com.example.data.speech.JarvisSpeechEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*

class WakeWordService : Service(), RecognitionListener, TextToSpeech.OnInitListener {

    companion object {
        const val CHANNEL_ID = "jarvis_wakeword_channel"
        const val NOTIFICATION_ID = 3000
        const val ACTION_START = "com.example.service.ACTION_START_WAKEWORD"
        const val ACTION_STOP = "com.example.service.ACTION_STOP_WAKEWORD"
        const val ACTION_WAKE_WORD_TRIGGERED = "com.example.ACTION_WAKE_WORD_TRIGGERED"

        fun startService(context: Context) {
            val intent = Intent(context, WakeWordService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, WakeWordService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var isServiceRunning = false
    private val mainHandler = Handler(Looper.getMainLooper())
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())

    private var userTitle = "Sir"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        initTts()
        loadUserProfileTitle()
    }

    private fun loadUserProfileTitle() {
        serviceScope.launch {
            try {
                val db = JarvisDatabase.getInstance(applicationContext)
                val profile = db.jarvisDao().getUserProfileSync()
                if (profile != null) {
                    userTitle = profile.preferredTitle.ifBlank { "Sir" }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun initTts() {
        tts = TextToSpeech(this, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.UK
            tts?.setPitch(0.95f)
            tts?.setSpeechRate(1.02f)
            isTtsReady = true
        }
    }

    private fun speakResponse(text: String, onComplete: (() -> Unit)? = null) {
        if (!isTtsReady) {
            onComplete?.invoke()
            return
        }
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "WAKE_WORD_RESPONSE_" + System.currentTimeMillis())
        mainHandler.postDelayed({
            onComplete?.invoke()
        }, 1500L)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_STOP) {
            stopListening()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            isServiceRunning = false
            return START_NOT_STICKY
        }

        startForegroundNotification()
        isServiceRunning = true
        startListening()
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "J.A.R.V.I.S. Background Wake-Word Matrix",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitors background microphone acoustic triggers for 'Jarvis'"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun startForegroundNotification() {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("J.A.R.V.I.S. Acoustic Matrix Online")
            .setContentText("Listening for 'Jarvis' wake-word in real-time...")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                )
            } catch (e: Exception) {
                startForeground(NOTIFICATION_ID, notification)
            }
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun startListening() {
        mainHandler.post {
            if (!isServiceRunning) return@post
            try {
                if (speechRecognizer == null) {
                    if (SpeechRecognizer.isRecognitionAvailable(this)) {
                        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
                            setRecognitionListener(this@WakeWordService)
                        }
                    }
                }
                speechRecognizer?.cancel()

                val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                    putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
                }

                speechRecognizer?.startListening(recognizerIntent)
            } catch (e: Exception) {
                e.printStackTrace()
                scheduleRestartListening(2000L)
            }
        }
    }

    private fun stopListening() {
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
                speechRecognizer?.cancel()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun scheduleRestartListening(delayMs: Long = 800L) {
        if (!isServiceRunning) return
        mainHandler.postDelayed({
            if (isServiceRunning) {
                startListening()
            }
        }, delayMs)
    }

    private fun handleWakeWordTriggered(triggerWord: String) {
        stopListening()

        val greetingText = "Yes $userTitle."

        // 1. Speak Voice Confirmation ("Yes Sir" or "Yes Mr. Stark")
        speakResponse(greetingText) {
            scheduleRestartListening(1000L)
        }

        // 2. Pop up tiny floating Arc Reactor overlay in the corner of the screen
        FloatingArcReactorManager.show(
            context = applicationContext,
            salutation = userTitle,
            statusMessage = "'$triggerWord' Acoustic Trigger Detected!"
        )

        // 3. Send Broadcast Intent to app (if open or backgrounded)
        val broadcastIntent = Intent(ACTION_WAKE_WORD_TRIGGERED).apply {
            putExtra("WAKE_WORD", triggerWord)
            putExtra("SALUTATION", userTitle)
            setPackage(packageName)
        }
        sendBroadcast(broadcastIntent)
    }

    // --- SpeechRecognizer Callbacks ---
    override fun onReadyForSpeech(params: Bundle?) {}
    override fun onBeginningOfSpeech() {}
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() {}

    override fun onError(error: Int) {
        scheduleRestartListening(1200L)
    }

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            val text = matches[0].lowercase().trim()
            if (isJarvisWakeWord(text)) {
                handleWakeWordTriggered(matches[0])
                return
            }
        }
        scheduleRestartListening(500L)
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            val text = matches[0].lowercase().trim()
            if (isJarvisWakeWord(text)) {
                handleWakeWordTriggered(matches[0])
            }
        }
    }

    private fun isJarvisWakeWord(text: String): Boolean {
        return text == "jarvis" ||
                text == "hey jarvis" ||
                text == "hi jarvis" ||
                text == "ok jarvis" ||
                text == "okay jarvis" ||
                text == "wake up jarvis" ||
                text == "stark" ||
                text.contains("hey jarvis") ||
                text.contains("ok jarvis") ||
                (text.startsWith("jarvis") && text.length <= 15)
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
