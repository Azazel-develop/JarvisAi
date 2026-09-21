package com.example.data.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.*

import com.example.data.model.LanguageRegistry

class JarvisSpeechEngine(private val context: Context) : TextToSpeech.OnInitListener, RecognitionListener {

    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var isTtsReady = false

    private val mainHandler = Handler(Looper.getMainLooper())

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening

    private val _speechAmplitude = MutableStateFlow(0f)
    val speechAmplitude: StateFlow<Float> = _speechAmplitude

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText

    private val _wakeWordDetected = MutableStateFlow(false)
    val wakeWordDetected: StateFlow<Boolean> = _wakeWordDetected

    var isAlwaysOnWakeWordEnabled: Boolean = true
    var onSpeechRecognizedCallback: ((String) -> Unit)? = null
    var onWakeWordOrHoloTriggered: ((String) -> Unit)? = null

    init {
        mainHandler.post {
            try {
                tts = TextToSpeech(context, this)
                initSpeechRecognizer()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun initSpeechRecognizer() {
        try {
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                speechRecognizer?.destroy()
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(this@JarvisSpeechEngine)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.UK) ?: TextToSpeech.LANG_MISSING_DATA
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.US)
            }

            // Select highest quality natural human voice if available
            try {
                val availableVoices = tts?.voices
                if (!availableVoices.isNullOrEmpty()) {
                    val naturalVoice = availableVoices.firstOrNull { voice ->
                        (voice.locale == Locale.UK || voice.locale == Locale.US) &&
                                !voice.isNetworkConnectionRequired &&
                                (voice.name.lowercase().contains("en-gb") || voice.name.lowercase().contains("male") || voice.name.lowercase().contains("natural"))
                    } ?: availableVoices.firstOrNull { it.locale == Locale.UK || it.locale == Locale.US }
                    
                    if (naturalVoice != null) {
                        tts?.voice = naturalVoice
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            tts?.setPitch(0.95f) // Warm, calm, articulate British AI voice tone
            tts?.setSpeechRate(1.02f) // Fluent natural human speaking tempo
            
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }
                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    _speechAmplitude.value = 0f
                    scheduleAlwaysOnListeningRestart()
                }
                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    _speechAmplitude.value = 0f
                    scheduleAlwaysOnListeningRestart()
                }
            })
            isTtsReady = true
        }
    }

    fun updatePitchAndRate(pitch: Float, rate: Float) {
        tts?.setPitch(pitch.coerceIn(0.5f, 2.0f))
        tts?.setSpeechRate(rate.coerceIn(0.5f, 2.0f))
    }

    fun applyVoiceSettings(pitch: Float, rate: Float, languageCode: String = "EN_GB", persona: String = "JARVIS_BRITISH") {
        if (!isTtsReady) return
        try {
            val jarvisLang = LanguageRegistry.findLanguageByCode(languageCode)
            val targetLocale = jarvisLang.locale
            tts?.setLanguage(targetLocale)

            val personaPitch = when (persona.uppercase()) {
                "FRIDAY_TACTICAL" -> pitch * 1.15f
                "CYBER_SYNTH" -> pitch * 0.85f
                "ANIME_COMPANION" -> pitch * 1.35f
                "DEEP_COMMANDER" -> pitch * 0.70f
                else -> pitch // JARVIS_BRITISH
            }

            val personaRate = when (persona.uppercase()) {
                "ANIME_COMPANION" -> rate * 1.15f
                "DEEP_COMMANDER" -> rate * 0.90f
                else -> rate
            }

            tts?.setPitch(personaPitch.coerceIn(0.5f, 2.0f))
            tts?.setSpeechRate(personaRate.coerceIn(0.5f, 2.0f))

            val availableVoices = tts?.voices
            if (!availableVoices.isNullOrEmpty()) {
                val matchedVoice = availableVoices.firstOrNull { voice ->
                    voice.locale.language == targetLocale.language &&
                            !voice.isNetworkConnectionRequired &&
                            (if (persona.uppercase().contains("FRIDAY") || persona.uppercase().contains("ANIME")) 
                                voice.name.lowercase().contains("female") 
                             else 
                                voice.name.lowercase().contains("male") || voice.name.lowercase().contains("natural"))
                } ?: availableVoices.firstOrNull { it.locale.language == targetLocale.language }

                if (matchedVoice != null) {
                    tts?.voice = matchedVoice
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun speak(text: String) {
        if (!isTtsReady) return
        stopListening()

        // Humanize text & pronounce math / technical terms naturally
        val textWithoutCode = text.replace(Regex("```[a-zA-Z]*[\\s\\S]*?```"), " Executable code block synthesized. ")
        
        val humanizedText = textWithoutCode
            .replace("^", " to the power of ")
            .replace("√", " square root of ")
            .replace("∫", " integral of ")
            .replace("π", " pi ")
            .replace("±", " plus or minus ")
            .replace("!=", " is not equal to ")
            .replace("==", " equals ")
            .replace(">=", " is greater than or equal to ")
            .replace("<=", " is less than or equal to ")
            .replace(Regex("[#*`_\\[\\]()]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
        
        if (humanizedText.isBlank()) return
        
        _isSpeaking.value = true
        tts?.speak(humanizedText, TextToSpeech.QUEUE_FLUSH, null, "JARVIS_UTTERANCE_" + System.currentTimeMillis())
    }

    fun stopSpeaking() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun startListening() {
        mainHandler.post {
            if (_isSpeaking.value) return@post
            try {
                if (speechRecognizer == null) {
                    initSpeechRecognizer()
                }
                speechRecognizer?.cancel()

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 150L)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 300L)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 300L)
                    putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
                }
                _isListening.value = true
                _recognizedText.value = ""
                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                e.printStackTrace()
                _isListening.value = false
            }
        }
    }

    fun stopListening() {
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            _isListening.value = false
            _speechAmplitude.value = 0f
        }
    }

    fun resetWakeWordFlag() {
        _wakeWordDetected.value = false
    }

    // SpeechRecognizer Callbacks
    override fun onReadyForSpeech(params: Bundle?) {
        _isListening.value = true
    }

    override fun onBeginningOfSpeech() {}

    override fun onRmsChanged(rmsdB: Float) {
        // Map dB (usually -2 to 10) to 0.0 .. 1.0 amplitude for visualizer
        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
        _speechAmplitude.value = normalized
    }

    override fun onBufferReceived(buffer: ByteArray?) {}

    private fun scheduleAlwaysOnListeningRestart() {
        if (isAlwaysOnWakeWordEnabled && !_isSpeaking.value) {
            mainHandler.postDelayed({
                try {
                    if (isAlwaysOnWakeWordEnabled && !_isSpeaking.value && !_isListening.value) {
                        startListening()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, 200)
        }
    }

    override fun onEndOfSpeech() {
        _isListening.value = false
        scheduleAlwaysOnListeningRestart()
    }

    override fun onError(error: Int) {
        _isListening.value = false
        _speechAmplitude.value = 0f
        if (error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT || error == SpeechRecognizer.ERROR_NO_MATCH) {
            _recognizedText.value = "Ambient Listening (Say 'Jarvis' or 'Project Holo')..."
        } else {
            _recognizedText.value = "Voice matrix resetting..."
        }

        if (error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY || error == SpeechRecognizer.ERROR_CLIENT) {
            mainHandler.post {
                try {
                    speechRecognizer?.cancel()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        scheduleAlwaysOnListeningRestart()
    }

    override fun onResults(results: Bundle?) {
        _isListening.value = false
        _speechAmplitude.value = 0f
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            val text = matches[0]
            _recognizedText.value = text
            val lower = text.lowercase().trim()

            var isPureWakeWordOrTrigger = false

            if (lower == "jarvis" || lower == "hi jarvis" || lower == "hey jarvis" || lower == "wake up" || lower == "stark" || lower == "ok jarvis" || lower == "okay jarvis") {
                _wakeWordDetected.value = true
                onWakeWordOrHoloTriggered?.invoke("WAKE")
                isPureWakeWordOrTrigger = true
            } else if (lower == "holo" || lower == "project holo" || lower == "hologram") {
                _wakeWordDetected.value = true
                onWakeWordOrHoloTriggered?.invoke("HOLO")
                isPureWakeWordOrTrigger = true
            } else if (lower == "keyboard" || lower == "project keyboard" || lower == "holo keyboard" || lower == "hologram keyboard") {
                _wakeWordDetected.value = true
                onWakeWordOrHoloTriggered?.invoke("KEYBOARD")
                isPureWakeWordOrTrigger = true
            } else {
                if (lower.contains("jarvis") || lower.contains("stark") || lower.contains("holo")) {
                    _wakeWordDetected.value = true
                }
            }

            if (!isPureWakeWordOrTrigger) {
                onSpeechRecognizedCallback?.invoke(text)
            }
        }
        scheduleAlwaysOnListeningRestart()
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            val partialText = matches[0]
            _recognizedText.value = partialText
            val lower = partialText.lowercase().trim()

            if (lower == "jarvis" || lower == "hey jarvis" || lower == "hi jarvis" || lower == "wake up") {
                _wakeWordDetected.value = true
                onWakeWordOrHoloTriggered?.invoke("WAKE")
            } else if (lower == "holo" || lower == "project holo") {
                _wakeWordDetected.value = true
                onWakeWordOrHoloTriggered?.invoke("HOLO")
            } else if (lower == "keyboard" || lower == "project keyboard") {
                _wakeWordDetected.value = true
                onWakeWordOrHoloTriggered?.invoke("KEYBOARD")
            }
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}

    fun destroy() {
        mainHandler.post {
            try {
                tts?.stop()
                tts?.shutdown()
                speechRecognizer?.destroy()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
