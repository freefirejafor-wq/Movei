package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = CinemaGold,
    onPrimary = CinemaNavy,
    primaryContainer = CinemaAmberContainer,
    onPrimaryContainer = CinemaGold,
    secondary = CinemaGoldDark,
    onSecondary = Color.White,
    background = CinemaNavy,
    onBackground = CinemaTextLight,
    surface = CinemaNavySurface,
    onSurface = CinemaTextLight,
    surfaceVariant = CinemaNavyCard,
    onSurfaceVariant = CinemaTextMuted
)

private val LightColorScheme = lightColorScheme(
    primary = CinemaGoldDark,
    onPrimary = Color.White,
    primaryContainer = CinemaGold,
    onPrimaryContainer = CinemaNavy,
    secondary = CinemaNavySurface,
    onSecondary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = CinemaNavy,
    surface = Color.White,
    onSurface = CinemaNavy,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569)
)

@Composable
fun MovieNestBDTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent cinema identity
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> DarkColorScheme // Cinema apps default elegantly to dark theme for immersive viewing
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
