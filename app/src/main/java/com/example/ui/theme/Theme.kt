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

private val GraphiteMintColorScheme = darkColorScheme(
    primary = MintGlowPrimary,
    onPrimary = MintGlowOnPrimary,
    primaryContainer = MintGlowSecondary,
    onPrimaryContainer = MintGlowOnSecondary,
    secondary = MintGlowSecondary,
    onSecondary = MintGlowOnSecondary,
    background = DeepGraphiteBg,
    surface = DeepGraphiteSurface,
    onBackground = GraphiteTextPrimary,
    onSurface = GraphiteTextPrimary,
    surfaceVariant = DeepGraphiteElevated,
    onSurfaceVariant = GraphiteTextSecondary,
    outline = DeepGraphiteOutline
)

private val DarkColorScheme = GraphiteMintColorScheme
private val LightColorScheme = GraphiteMintColorScheme


@Composable
fun TikSaverTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent branding
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

