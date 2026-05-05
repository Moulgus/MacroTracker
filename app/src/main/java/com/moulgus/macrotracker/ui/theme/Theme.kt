package com.moulgus.macrotracker.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.graphics.Color

private val ColorOnTealDark = Color(0xFF00201B)
private val DarkColorScheme = darkColorScheme(
    primary = AppTeal,
    onPrimary = ColorOnTealDark,

    primaryContainer = AppTealDarker,
    onPrimaryContainer = TextLight,

    secondary = AppTealLight,
    onSecondary = AppTealDarker,

    background = DarkBackground,
    onBackground = TextLight,

    surface = DarkSurface,
    onSurface = TextLight,

    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFC8D0D4),

    outline = DarkOutline,

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

private val LightColorScheme = lightColorScheme(
    primary = AppTealDark,
    onPrimary = Color.White,

    primaryContainer = AppTealLight,
    onPrimaryContainer = AppTealDarker,

    secondary = AppTeal,
    onSecondary = TextDark,

    background = LightBackground,
    onBackground = TextDark,

    surface = LightSurface,
    onSurface = TextDark,

    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF4A5555),

    outline = LightOutline,

    error = Color(0xFFBA1A1A),
    onError = Color.White
)

@Composable
fun MacroTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()

            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}