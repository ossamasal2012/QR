package com.ossama.qrvision.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.ossama.qrvision.data.AppSettings

private val Emerald = Color(0xFF00DCA4)
private val DeepDark = Color(0xFF080D0D)
private val DarkSurface = Color(0xFF111A18)
private val LightBackground = Color(0xFFF4F8F6)

private val DarkScheme = darkColorScheme(
    primary = Emerald,
    onPrimary = Color(0xFF002117),
    secondary = Color(0xFF85E8C9),
    background = DeepDark,
    surface = DarkSurface,
    surfaceVariant = Color(0xFF1A2622),
    onBackground = Color(0xFFE7F2EE),
    onSurface = Color(0xFFE7F2EE),
    onSurfaceVariant = Color(0xFFB1C5BE),
    outline = Color(0xFF40524B)
)

private val LightScheme = lightColorScheme(
    primary = Color(0xFF007F5C),
    onPrimary = Color.White,
    secondary = Color(0xFF126B54),
    background = LightBackground,
    surface = Color.White,
    surfaceVariant = Color(0xFFE3EFEB),
    onBackground = Color(0xFF12221C),
    onSurface = Color(0xFF12221C),
    onSurfaceVariant = Color(0xFF40564D),
    outline = Color(0xFF9AAEA5)
)

@Composable
fun QrVisionTheme(settings: AppSettings, content: @Composable () -> Unit) {
    val dark = when (settings.themeMode) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }
    MaterialTheme(colorScheme = if (dark) DarkScheme else LightScheme, content = content)
}
