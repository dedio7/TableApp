package com.example.dailypulse.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AmbientColorScheme = darkColorScheme(
    primary = AmberGlow,
    secondary = CoolCyan,
    tertiary = SoftGreen,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
)

@Composable
fun DailyPulseTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = AmbientColorScheme,
        typography = Typography,
        content = content
    )
}
