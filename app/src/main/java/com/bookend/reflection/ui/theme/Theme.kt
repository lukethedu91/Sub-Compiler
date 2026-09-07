package com.bookend.reflection.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = SunriseDeep,
    onPrimary = Color.White,
    primaryContainer = SunriseLight,
    onPrimaryContainer = SunriseInk,
    secondary = Dusk,
    onSecondary = Color.White,
    secondaryContainer = DuskLight,
    onSecondaryContainer = DuskInk,
    tertiary = Dusk,
    onTertiary = Color.White,
    background = Paper,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = PaperMid,
    onSurfaceVariant = InkSoft,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = PaperLow,
    surfaceContainer = PaperMid,
    surfaceContainerHigh = PaperHigh,
    surfaceContainerHighest = PaperHigh,
    outline = InkFaint,
    outlineVariant = PaperEdge,
)

private val DarkColors = darkColorScheme(
    primary = Sunrise,
    onPrimary = SunriseInk,
    primaryContainer = Color(0xFF5C4300),
    onPrimaryContainer = SunriseLight,
    secondary = DuskBright,
    onSecondary = Color(0xFF222C57),
    secondaryContainer = DuskDeep,
    onSecondaryContainer = DuskLight,
    tertiary = DuskBright,
    onTertiary = Color(0xFF222C57),
    background = Night,
    onBackground = Chalk,
    surface = Night,
    onSurface = Chalk,
    surfaceVariant = NightHigh,
    onSurfaceVariant = ChalkSoft,
    surfaceContainerLowest = Color(0xFF0E0D0C),
    surfaceContainerLow = NightLow,
    surfaceContainer = NightMid,
    surfaceContainerHigh = NightHigh,
    surfaceContainerHighest = NightEdge,
    outline = ChalkFaint,
    outlineVariant = NightEdge,
)

/**
 * Bookend keeps its own palette by default: the sunrise and dusk accents carry
 * the meaning of the two halves of the day, which a dynamic palette would lose.
 */
@Composable
fun BookendTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> DarkColors
        else -> LightColors
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = BookendTypography,
        shapes = BookendShapes,
        content = content,
    )
}
