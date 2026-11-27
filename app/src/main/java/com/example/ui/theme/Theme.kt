package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val AzroiaDarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = ObsidianVoid,
    primaryContainer = ObsidianCardElevated,
    onPrimaryContainer = TextPrimary,
    secondary = ElectricCrimson,
    onSecondary = TextPrimary,
    secondaryContainer = ObsidianCard,
    onSecondaryContainer = TextPrimary,
    tertiary = EmeraldPulse,
    onTertiary = ObsidianVoid,
    background = ObsidianVoid,
    onBackground = TextPrimary,
    surface = ObsidianSurface,
    onSurface = TextPrimary,
    surfaceVariant = ObsidianCard,
    onSurfaceVariant = TextSecondary,
    surfaceContainerLowest = ObsidianGlassBottom,
    surfaceContainerLow = ObsidianSurface,
    surfaceContainer = ObsidianCard,
    surfaceContainerHigh = ObsidianCardElevated,
    surfaceContainerHighest = ObsidianGlassTop,
    outline = CyberBorderMuted,
    outlineVariant = CyberBorderMuted.copy(alpha = 0.45f),
    error = ElectricCrimson,
    onError = TextPrimary
)

val AzroiaShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp)
)

@Composable
fun AzroiaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AzroiaDarkColorScheme,
        typography = AzroiaTypography,
        shapes = AzroiaShapes,
        content = content
    )
}
