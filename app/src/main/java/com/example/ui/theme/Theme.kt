package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val IslamicDarkColorScheme = darkColorScheme(
    primary = IslamicGold,
    onPrimary = DarkBackground,
    primaryContainer = IslamicGoldDark,
    onPrimaryContainer = IslamicGoldLight,
    secondary = IslamicGoldLight,
    onSecondary = DarkBackground,
    background = DarkBackground,
    onBackground = TextPrimaryDark,
    surface = DarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkSurfaceCard,
    onSurfaceVariant = TextSecondaryDark,
    outline = IslamicGoldBorder
)

private val IslamicLightColorScheme = lightColorScheme(
    primary = IslamicGoldDark,
    onPrimary = TextPrimaryDark,
    primaryContainer = IslamicGoldLight,
    onPrimaryContainer = DarkBackground,
    secondary = IslamicGold,
    onSecondary = TextPrimaryDark,
    background = LightBackground,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightSurfaceCard,
    onSurfaceVariant = TextSecondaryLight,
    outline = IslamicGoldBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Defaults to luxury dark mode
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) IslamicDarkColorScheme else IslamicLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
