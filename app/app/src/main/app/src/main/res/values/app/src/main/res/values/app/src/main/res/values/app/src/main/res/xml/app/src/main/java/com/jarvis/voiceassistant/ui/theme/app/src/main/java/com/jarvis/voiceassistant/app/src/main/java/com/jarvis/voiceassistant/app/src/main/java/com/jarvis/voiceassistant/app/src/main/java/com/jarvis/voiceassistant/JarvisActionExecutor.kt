package com.jarvis.voiceassistant

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import android.view.KeyEvent

data class ExecutionResult(
    val success: Boolean,
    val spokenResponse: String,
    val isHindi: Boolean = false
)

object JarvisActionExecutor {

    fun execute(
        context: Context,
        action: JarvisAction,
        onRequestCallConfirmation: (String) -> Unit
    ): ExecutionResult {
        return when (action) {
            is JarvisAction.OpenApp -> handleOpenApp(context, action.appTarget)
            is JarvisAction.WebSearch -> handleWebSearch(context, action.query)
            is JarvisAction.MakeCall -> {
                onRequestCallConfirmation(action.target)
                ExecutionResult(true, "Confirm calling ${action.target}?")
            }
            is JarvisAction.SystemControl -> handleSystemControl(context, action.control)
            is JarvisAction.MediaControl -> handleMediaControl(context, action.command)
            is JarvisAction.Unknown -> {
                handleWebSearch(context, action.rawText)
                ExecutionResult(true, "Searching the web for you.")
            }
        }
    }

    private fun handleOpenApp(context: Context, target: AppTarget): ExecutionResult {
        return try {
            when (target) {
                AppTarget.SETTINGS -> {
                    val intent = Intent(Settings.ACTION_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                    context.startActivity(intent)
                    ExecutionResult(true, "Opening Settings.")
                }
                AppTarget.CAMERA -> {
                    val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                    context.startActivity(intent)
                    ExecutionResult(true, "Opening Camera.")
                }
                else -> {
                    val launchIntent = target.packageName?.let { context.packageManager.getLaunchIntentForPackage(it) }
                    if (launchIntent != null) {
                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(launchIntent)
                        ExecutionResult(true, "Opening ${target.displayName}.")
                    } else {
                        ExecutionResult(false, "${target.displayName} is not installed.")
                    }
                }
            }
        } catch (e: Exception) {
            ExecutionResult(false, "Could not open: ${e.localizedMessage}")
        }
    }

    private fun handleWebSearch(context: Context, query: String): ExecutionResult {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=${Uri.encode(query)}")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            ExecutionResult(true, "Searching Google for $query.")
        } catch (e: Exception) {
            ExecutionResult(false, "Search failed.")
        }
    }

    fun initiateDialer(context: Context, target: String): ExecutionResult {
        return try {
            val isNumber = target.replace(Regex("[^0-9+]"), "").length >= 3
            val dialUri = if (isNumber) Uri.parse("tel:${Uri.encode(target)}") else Uri.parse("tel:")
            val intent = Intent(Intent.ACTION_DIAL, dialUri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            ExecutionResult(true, "Opening dialer for $target.")
        } catch (e: Exception) {
            ExecutionResult(false, "Failed to open phone dialer.")
        }
    }

    private fun handleSystemControl(context: Context, control: ControlType): ExecutionResult {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        return when (control) {
            ControlType.VOLUME_UP -> {
                audioManager?.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
                ExecutionResult(true, "Volume raised.")
            }
            ControlType.VOLUME_DOWN -> {
                audioManager?.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
                ExecutionResult(true, "Volume decreased.")
            }
            ControlType.OPEN_WIFI_SETTINGS -> {
                context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
                ExecutionResult(true, "Opening Wi-Fi settings.")
            }
            ControlType.OPEN_BLUETOOTH_SETTINGS -> {
                context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
                ExecutionResult(true, "Opening Bluetooth settings.")
            }
            ControlType.OPEN_NOTIFICATIONS -> {
                if (JarvisAccessibilityService.isServiceRunning()) {
                    val ok = JarvisAccessibilityService.openNotifications()
                    if (ok) ExecutionResult(true, "Opening notifications.") else ExecutionResult(false, "Permission blocked.")
                } else {
                    ExecutionResult(false, "Please enable JARVIS in Accessibility Settings.")
                }
            }
        }
    }

    private fun handleMediaControl(context: Context, action: MediaAction): ExecutionResult {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            ?: return ExecutionResult(false, "Audio service unavailable.")
        val keyCode = when (action) {
            MediaAction.PLAY_PAUSE -> KeyEvent.KEYCODE_HEADSETHOOK
            MediaAction.NEXT_TRACK -> KeyEvent.KEYCODE_MEDIA_NEXT
            MediaAction.PREVIOUS_TRACK -> KeyEvent.KEYCODE_MEDIA_PREVIOUS
        }
        return try {
            audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
            audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
            ExecutionResult(true, "Media command executed.")
        } catch (e: Exception) {
            ExecutionResult(false, "Media control failed.")
        }
    }
}
