package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val KifaruDarkColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.Black,
    primaryContainer = EmeraldDark,
    secondary = GoldAccent,
    onSecondary = Color.Black,
    background = DarkCanvas,
    onBackground = TextPrimaryDark,
    surface = SurfaceCard,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceCardBorder,
    onSurfaceVariant = TextSecondaryDark
)

private val KifaruLightColorScheme = lightColorScheme(
    primary = EmeraldDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2F1),
    secondary = GoldAccent,
    onSecondary = Color.Black,
    background = LightCanvas,
    onBackground = Color(0xFF101C19),
    surface = LightSurfaceCard,
    onSurface = Color(0xFF101C19),
    surfaceVariant = Color(0xFFD3E3DF),
    onSurfaceVariant = Color(0xFF2E4D45)
)

@Composable
fun KifaruPayTheme(
    darkTheme: Boolean = true, // Default to sleek fintech dark theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) KifaruDarkColorScheme else KifaruLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

