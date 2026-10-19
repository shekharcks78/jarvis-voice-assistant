package com.jarvis.voiceassistant.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val JarvisBackground = Color(0xFF040814)
val JarvisSurface = Color(0xFF0C1427)
val JarvisCyan = Color(0xFF00E5FF)
val JarvisDeepBlue = Color(0xFF0288D1)
val JarvisTextPrimary = Color(0xFFE1F5FE)
val JarvisError = Color(0xFFFF5252)
val JarvisSuccess = Color(0xFF00E676)

private val DarkColorScheme = darkColorScheme(
    primary = JarvisCyan,
    secondary = JarvisDeepBlue,
    background = JarvisBackground,
    surface = JarvisSurface,
    onPrimary = Color.Black,
    onBackground = JarvisTextPrimary,
    onSurface = JarvisTextPrimary,
    error = JarvisError
)

@Composable
fun JarvisTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
