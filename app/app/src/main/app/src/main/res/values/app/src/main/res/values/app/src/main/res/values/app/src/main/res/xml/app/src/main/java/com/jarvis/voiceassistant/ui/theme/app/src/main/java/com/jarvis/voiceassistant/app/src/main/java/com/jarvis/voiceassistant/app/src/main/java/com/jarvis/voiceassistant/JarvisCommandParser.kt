package com.jarvis.voiceassistant

sealed class JarvisAction {
    data class OpenApp(val appTarget: AppTarget) : JarvisAction()
    data class MakeCall(val target: String) : JarvisAction()
    data class WebSearch(val query: String) : JarvisAction()
    data class SystemControl(val control: ControlType) : JarvisAction()
    data class MediaControl(val command: MediaAction) : JarvisAction()
    data class Unknown(val rawText: String) : JarvisAction()
}

enum class AppTarget(val displayName: String, val packageName: String?) {
    YOUTUBE("YouTube", "com.google.android.youtube"),
    WHATSAPP("WhatsApp", "com.whatsapp"),
    CHROME("Google Chrome", "com.android.chrome"),
    SETTINGS("Settings", null),
    CAMERA("Camera", null)
}

enum class ControlType {
    VOLUME_UP,
    VOLUME_DOWN,
    OPEN_WIFI_SETTINGS,
    OPEN_BLUETOOTH_SETTINGS,
    OPEN_NOTIFICATIONS
}

enum class MediaAction {
    PLAY_PAUSE,
    NEXT_TRACK,
    PREVIOUS_TRACK
}

object JarvisCommandParser {
    fun parse(rawInput: String): JarvisAction {
        val text = rawInput.trim().lowercase()
        val query = text.replace(Regex("^(hey\\s+)?jarvis\\s*"), "").trim()

        return when {
            query.startsWith("open ") || query.startsWith("kholo ") -> {
                val targetName = query.replace(Regex("^(open|kholo)\\s+"), "").trim()
                when {
                    "youtube" in targetName -> JarvisAction.OpenApp(AppTarget.YOUTUBE)
                    "whatsapp" in targetName -> JarvisAction.OpenApp(AppTarget.WHATSAPP)
                    "chrome" in targetName || "browser" in targetName -> JarvisAction.OpenApp(AppTarget.CHROME)
                    "setting" in targetName -> JarvisAction.OpenApp(AppTarget.SETTINGS)
                    "camera" in targetName -> JarvisAction.OpenApp(AppTarget.CAMERA)
                    else -> JarvisAction.WebSearch(rawInput)
                }
            }
            query.startsWith("search ") || query.startsWith("google ") || query.startsWith("find ") -> {
                val searchQuery = query.replace(Regex("^(search|google|find)\\s+"), "")
                    .replace(Regex("\\s+on google$"), "").trim()
                JarvisAction.WebSearch(searchQuery)
            }
            query.startsWith("call ") || query.startsWith("phone lagao ") -> {
                val target = query.replace(Regex("^(call|phone lagao)\\s+"), "").trim()
                JarvisAction.MakeCall(target)
            }
            "volume up" in query || "awaaz badhao" in query -> JarvisAction.SystemControl(ControlType.VOLUME_UP)
            "volume down" in query || "awaaz kam karo" in query -> JarvisAction.SystemControl(ControlType.VOLUME_DOWN)
            "wi-fi" in query || "wifi" in query -> JarvisAction.SystemControl(ControlType.OPEN_WIFI_SETTINGS)
            "bluetooth" in query -> JarvisAction.SystemControl(ControlType.OPEN_BLUETOOTH_SETTINGS)
            "notification" in query -> JarvisAction.SystemControl(ControlType.OPEN_NOTIFICATIONS)
            "play" in query || "pause" in query -> JarvisAction.MediaControl(MediaAction.PLAY_PAUSE)
            "next" in query -> JarvisAction.MediaControl(MediaAction.NEXT_TRACK)
            "previous" in query -> JarvisAction.MediaControl(MediaAction.PREVIOUS_TRACK)
            else -> JarvisAction.Unknown(rawInput)
        }
    }
}
