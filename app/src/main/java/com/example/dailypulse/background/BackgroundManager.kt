package com.example.dailypulse.background

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin

/**
 * Configuration for the ambient background appearance.
 */
data class BackgroundConfig(
    val primaryColor: Color,
    val secondaryColor: Color? = null,
    val useGradient: Boolean = false
)

/**
 * Preset dark colors for the screensaver background.
 */
val PRESET_COLORS: List<Pair<String, Color>> = listOf(
    "Nero" to Color(0xFF0D0D0D),
    "Blu Notte" to Color(0xFF0D1B2A),
    "Verde Scuro" to Color(0xFF1B2D1B),
    "Rosso Scuro" to Color(0xFF2D1B1B),
    "Viola" to Color(0xFF1B1B2D),
    "Grigio Antracite" to Color(0xFF1A1A2E),
    "Blu Oceano" to Color(0xFF16213E),
    "Verde Smeraldo" to Color(0xFF0A2E36),
    "Bordeaux" to Color(0xFF2D0A0A),
    "Ambra" to Color(0xFF2D2A0A)
)

/**
 * Full-screen ambient background that wraps content.
 *
 * Supports solid color or animated gradient fills with a subtle
 * shimmer/pulse effect to make the background feel alive.
 */
@Composable
fun AmbientBackground(
    config: BackgroundConfig,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ambientBg")

    // Slowly rotating gradient angle (full cycle over 20 seconds)
    val gradientAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gradientAngle"
    )

    // Subtle brightness pulse (cycles every 8 seconds)
    val brightnessPulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "brightnessPulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                if (config.useGradient && config.secondaryColor != null) {
                    // Animated diagonal gradient
                    val angleRad = Math.toRadians(gradientAngle.toDouble())
                    val centerX = size.width / 2f
                    val centerY = size.height / 2f
                    val radius = maxOf(size.width, size.height) / 2f

                    val startX = centerX + radius * cos(angleRad).toFloat()
                    val startY = centerY + radius * sin(angleRad).toFloat()
                    val endX = centerX - radius * cos(angleRad).toFloat()
                    val endY = centerY - radius * sin(angleRad).toFloat()

                    // Apply subtle brightness modulation to colors
                    val pulsedPrimary = modulateBrightness(config.primaryColor, brightnessPulse)
                    val pulsedSecondary = modulateBrightness(config.secondaryColor, brightnessPulse)

                    val brush = Brush.linearGradient(
                        colors = listOf(pulsedPrimary, pulsedSecondary),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY)
                    )
                    drawRect(brush = brush)
                } else {
                    // Solid color with subtle brightness pulse
                    val pulsedColor = modulateBrightness(config.primaryColor, brightnessPulse)
                    drawRect(color = pulsedColor)
                }
            }
    ) {
        content()
    }
}

/**
 * Modulates the brightness of a color by a given factor.
 * Factor > 1 brightens, factor < 1 darkens. Alpha is preserved.
 */
private fun modulateBrightness(color: Color, factor: Float): Color {
    return Color(
        red = (color.red * factor).coerceIn(0f, 1f),
        green = (color.green * factor).coerceIn(0f, 1f),
        blue = (color.blue * factor).coerceIn(0f, 1f),
        alpha = color.alpha
    )
}
