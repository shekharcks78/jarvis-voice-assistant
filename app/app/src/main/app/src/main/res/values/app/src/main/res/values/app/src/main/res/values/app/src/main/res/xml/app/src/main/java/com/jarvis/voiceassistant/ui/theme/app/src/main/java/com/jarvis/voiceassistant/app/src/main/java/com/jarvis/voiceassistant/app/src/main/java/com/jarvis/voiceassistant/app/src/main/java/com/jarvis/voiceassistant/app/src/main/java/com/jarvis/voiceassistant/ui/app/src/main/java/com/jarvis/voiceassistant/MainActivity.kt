package com.jarvis.voiceassistant

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.jarvis.voiceassistant.ui.CommandHistoryItem
import com.jarvis.voiceassistant.ui.JarvisScreen
import com.jarvis.voiceassistant.ui.theme.JarvisTheme

class MainActivity : ComponentActivity() {

    private lateinit var voiceManager: JarvisVoiceManager
    private var isListening by mutableStateOf(false)
    private var statusText by mutableStateOf("Ready for command")
    private var transcriptText by mutableStateOf("")
    private var pendingCallTarget by mutableStateOf<String?>(null)
    private val commandHistory = mutableStateListOf<CommandHistoryItem>()

    private val recordAudioLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) voiceManager.startListening()
        else statusText = "Microphone access denied."
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        voiceManager = JarvisVoiceManager(
            context = this,
            onListeningStateChanged = { listening ->
                isListening = listening
                if (listening) statusText = "Listening..."
            },
            onResult = { result ->
                transcriptText = result
                processCommand(result)
            },
            onError = { err -> statusText = err }
        )

        setContent {
            JarvisTheme {
                JarvisScreen(
                    isListening = isListening,
                    statusText = statusText,
                    transcriptText = transcriptText,
                    commandHistory = commandHistory,
                    pendingCallTarget = pendingCallTarget,
                    onMicClick = { handleMicClick() },
                    onConfirmCall = { target ->
                        pendingCallTarget = null
                        val dialResult = JarvisActionExecutor.initiateDialer(this, target)
                        voiceManager.speak(dialResult.spokenResponse)
                        commandHistory.add(0, CommandHistoryItem("Call $target", dialResult.spokenResponse, dialResult.success))
                    },
                    onDismissCall = {
                        pendingCallTarget = null
                        voiceManager.speak("Call cancelled.")
                    },
                    onOpenAccessibilitySettings = {
                        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    }
                )
            }
        }
    }

    private fun handleMicClick() {
        if (isListening) {
            voiceManager.stopListening()
            return
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            voiceManager.startListening()
        } else {
            recordAudioLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun processCommand(input: String) {
        val action = JarvisCommandParser.parse(input)
        val result = JarvisActionExecutor.execute(
            context = this,
            action = action,
            onRequestCallConfirmation = { target -> pendingCallTarget = target }
        )

        statusText = result.spokenResponse
        voiceManager.speak(result.spokenResponse, result.isHindi)
        commandHistory.add(0, CommandHistoryItem(input, result.spokenResponse, result.success))
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceManager.destroy()
    }
}
