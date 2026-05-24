package com.zomba.cal.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ZombaLightColors = lightColorScheme(
    primary = Color(0xFF0F766E),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFA4F4E8),
    onPrimaryContainer = Color(0xFF00201D),
    secondary = Color(0xFFB45309),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDDBA),
    onSecondaryContainer = Color(0xFF2B1700),
    background = Color(0xFFF7F4ED),
    onBackground = Color(0xFF1C1B18),
    surface = Color(0xFFFFFBFF),
    onSurface = Color(0xFF1C1B18),
    surfaceVariant = Color(0xFFE7E2D4),
    onSurfaceVariant = Color(0xFF4A473D)
)

private val ZombaDarkColors = darkColorScheme(
    primary = Color(0xFF82D8CB),
    onPrimary = Color(0xFF003733),
    primaryContainer = Color(0xFF00504A),
    onPrimaryContainer = Color(0xFFA4F4E8),
    secondary = Color(0xFFF4B66D),
    onSecondary = Color(0xFF442B00),
    secondaryContainer = Color(0xFF633F00),
    onSecondaryContainer = Color(0xFFFFDDBA),
    background = Color(0xFF141411),
    onBackground = Color(0xFFE6E2D9),
    surface = Color(0xFF141411),
    onSurface = Color(0xFFE6E2D9),
    surfaceVariant = Color(0xFF4A473D),
    onSurfaceVariant = Color(0xFFCBC6B8)
)

@Composable
fun ZombaCalTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) ZombaDarkColors else ZombaLightColors,
        typography = Typography(),
        content = content
    )
}
