package com.jarvis.voiceassistant

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import java.util.Locale

class JarvisVoiceManager(
    private val context: Context,
    private val onListeningStateChanged: (Boolean) -> Unit,
    private val onResult: (String) -> Unit,
    private val onError: (String) -> Unit
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsInitialized = false

    init {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale.US
                textToSpeech?.setPitch(0.95f)
                textToSpeech?.setSpeechRate(1.05f)
                isTtsInitialized = true
            }
        }
    }

    fun speak(text: String, isHindi: Boolean = false) {
        if (!isTtsInitialized || textToSpeech == null) return
        textToSpeech?.language = if (isHindi) Locale("hi", "IN") else Locale.US
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "JARVIS_REPLY")
    }

    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError("Speech recognition not available")
            return
        }

        stopListening()

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    onListeningStateChanged(true)
                }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    onListeningStateChanged(false)
                }
                override fun onError(error: Int) {
                    onListeningStateChanged(false)
                    val msg = when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Try again."
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Listening timed out."
                        else -> "Speech recognition error ($error)"
                    }
                    onError(msg)
                }
                override fun onResults(results: Bundle?) {
                    onListeningStateChanged(false)
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        onResult(matches[0])
                    } else {
                        onError("Could not understand voice.")
                    }
                }
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        speechRecognizer?.startListening(intent)
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
        } catch (_: Exception) {
        } finally {
            speechRecognizer = null
            onListeningStateChanged(false)
        }
    }

    fun destroy() {
        stopListening()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
    }
}
