package com.dedio.dailypulse.background

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin

/**
 * Defines a generative atmosphere with its core color palette.
 */
enum class Atmosphere(val displayName: String, val colors: List<Color>) {
    DEEP_SPACE("Deep Space", listOf(Color(0xFF020617), Color(0xFF0F172A), Color(0xFF1E1B4B))),
    MORNING_MIST("Morning Mist", listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0), Color(0xFFBFDBFE))),
    GOLDEN_HOUR("Golden Hour", listOf(Color(0xFF450A0A), Color(0xFF78350F), Color(0xFF92400E))),
    NORDIC_AURORA("Nordic Aurora", listOf(Color(0xFF064E3B), Color(0xFF0F172A), Color(0xFF4C1D95))),
    MIDNIGHT_SILK("Midnight Silk", listOf(Color(0xFF0D0D0D), Color(0xFF1A1A1A), Color(0xFF262626))),
    OCEAN_DEPTHS("Ocean Depths", listOf(Color(0xFF082F49), Color(0xFF075985), Color(0xFF0C4A6E)))
}

data class BackgroundConfig(
    val atmosphere: Atmosphere = Atmosphere.DEEP_SPACE,
    val isMusicPlaying: Boolean = false
)

@Composable
fun AmbientBackground(
    config: BackgroundConfig,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "silkFlow")
    
    // Music Pulse - Slow and gentle to avoid UI choke on old devices
    val musicPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (config.isMusicPlaying) 1.08f else 1f, // Reduced amplitude
        animationSpec = infiniteRepeatable(
            animation = tween(if (config.isMusicPlaying) 1200 else 4000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "musicPulse"
    )

    val phase1 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(50000, easing = LinearEasing), RepeatMode.Restart),
        label = "p1"
    )
    val phase2 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(45000, easing = LinearEasing), RepeatMode.Restart),
        label = "p2"
    )

    val colors = config.atmosphere.colors
    
    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            
            // 1. Base
            drawRect(colors[0])

            // 2. Layer 1 - Simplified for performance
            val angle1 = Math.toRadians(phase1.toDouble())
            val center1 = Offset(
                x = (w * 0.5f) + (w * 0.15f * cos(angle1)).toFloat(),
                y = (h * 0.5f) + (h * 0.15f * sin(angle1)).toFloat()
            )
            drawCircle(
                brush = Brush.radialGradient(
                    0.0f to colors[1].copy(alpha = 0.5f * musicPulse),
                    1.0f to Color.Transparent,
                    center = center1,
                    radius = w * 0.9f
                ),
                center = center1,
                radius = w * 0.9f
            )

            // 3. Layer 2
            val angle2 = Math.toRadians(phase2.toDouble() + 180.0)
            val center2 = Offset(
                x = (w * 0.5f) + (w * 0.2f * cos(angle2)).toFloat(),
                y = (h * 0.5f) + (h * 0.2f * sin(angle2)).toFloat()
            )
            drawCircle(
                brush = Brush.radialGradient(
                    0.0f to colors[2].copy(alpha = 0.3f),
                    1.0f to Color.Transparent,
                    center = center2,
                    radius = w * 0.8f
                ),
                center = center2,
                radius = w * 0.8f
            )
        }
        
        content()
    }
}
