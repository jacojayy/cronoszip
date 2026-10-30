package com.cronoszip.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val CronosColorScheme = darkColorScheme(
    primary = CronosBlue,
    onPrimary = TextPrimary,
    primaryContainer = CronosBlueDeep,
    onPrimaryContainer = ColorTokens.onPrimaryContainer,
    secondary = CronosCyan,
    onSecondary = Ink,
    secondaryContainer = NightBlue,
    onSecondaryContainer = CronosBlueSoft,
    tertiary = CronosCyan,
    onTertiary = Ink,
    background = Ink,
    onBackground = TextPrimary,
    surface = NightBlue,
    onSurface = TextPrimary,
    surfaceVariant = ColorTokens.surfaceVariant,
    onSurfaceVariant = TextSecondary,
    surfaceTint = CronosBlue,
    outline = ColorTokens.outline,
    outlineVariant = ColorTokens.outlineVariant,
    error = Danger,
    onError = Ink,
    errorContainer = ColorTokens.errorContainer,
    onErrorContainer = ColorTokens.onErrorContainer,
    scrim = Ink,
)

private object ColorTokens {
    val onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFFD9E8FF)
    val surfaceVariant = androidx.compose.ui.graphics.Color(0xFF102238)
    val outline = androidx.compose.ui.graphics.Color(0xFF3A5478)
    val outlineVariant = androidx.compose.ui.graphics.Color(0xFF1C3350)
    val errorContainer = androidx.compose.ui.graphics.Color(0xFF4A1420)
    val onErrorContainer = androidx.compose.ui.graphics.Color(0xFFFFD9DD)
}

private val CronosShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp),
)

@Composable
fun CronosZipTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CronosColorScheme,
        typography = CronosTypography,
        shapes = CronosShapes,
        content = content,
    )
}
