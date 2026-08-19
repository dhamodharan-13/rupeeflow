package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppleDarkColorScheme = darkColorScheme(
    primary = AppleBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1C2838),
    onPrimaryContainer = Color(0xFF90CAFF),
    secondary = AppleIndigo,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF262547),
    onSecondaryContainer = Color(0xFFC5C3FF),
    tertiary = AppleTeal,
    background = iOSDarkBackground,
    onBackground = iOSDarkTextPrimary,
    surface = iOSDarkSurface,
    onSurface = iOSDarkTextPrimary,
    surfaceVariant = iOSDarkSurfaceVariant,
    onSurfaceVariant = iOSDarkTextSecondary,
    surfaceContainer = iOSDarkSurfaceVariant,
    surfaceContainerHigh = iOSDarkSurfaceHigh,
    surfaceContainerLow = iOSDarkSurface,
    outline = iOSDarkOutline,
    outlineVariant = iOSDarkTextTertiary,
    error = AppleRed,
    onError = Color.White,
    errorContainer = Color(0xFF381515),
    onErrorContainer = Color(0xFFFFB4AB)
)

private val AppleLightColorScheme = lightColorScheme(
    primary = AppleBlueLight,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD7ECFF),
    onPrimaryContainer = Color(0xFF00325B),
    secondary = AppleIndigo,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE2E0FF),
    onSecondaryContainer = Color(0xFF18124C),
    tertiary = Color(0xFF0284C7),
    background = iOSLightBackground,
    onBackground = iOSLightTextPrimary,
    surface = iOSLightSurface,
    onSurface = iOSLightTextPrimary,
    surfaceVariant = iOSLightSurfaceVariant,
    onSurfaceVariant = iOSLightTextSecondary,
    surfaceContainer = iOSLightSurface,
    surfaceContainerHigh = iOSLightSurfaceVariant,
    surfaceContainerLow = iOSLightSurface,
    outline = iOSLightOutline,
    outlineVariant = Color(0xFFE5E5EA),
    error = AppleRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set to false to showcase the curated Apple Card Stack styling
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) AppleDarkColorScheme else AppleLightColorScheme
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
