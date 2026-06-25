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
    COSMIC_FLOW("Cosmic Flow", listOf(Color(0xFF030712), Color(0xFF4285F4), Color(0xFF9B72CB), Color(0xFFD96570), Color(0xFF1AA7EC))),
    DEEP_SPACE("Deep Space", listOf(Color(0xFF020617), Color(0xFF0F172A), Color(0xFF1E1B4B))),
    MORNING_MIST("Morning Mist", listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0), Color(0xFFBFDBFE))),
    GOLDEN_HOUR("Golden Hour", listOf(Color(0xFF450A0A), Color(0xFF78350F), Color(0xFF92400E))),
    NORDIC_AURORA("Nordic Aurora", listOf(Color(0xFF064E3B), Color(0xFF0F172A), Color(0xFF4C1D95))),
    MIDNIGHT_SILK("Midnight Silk", listOf(Color(0xFF0D0D0D), Color(0xFF1A1A1A), Color(0xFF262626))),
    OCEAN_DEPTHS("Ocean Depths", listOf(Color(0xFF082F49), Color(0xFF075985), Color(0xFF0C4A6E))),
    NEON_PULSE("Neon Pulse", listOf(Color(0xFF2D0036), Color(0xFF001233), Color(0xFF00F5D4))),
    FOREST_MIST("Forest Mist", listOf(Color(0xFF0B120B), Color(0xFF1B3022), Color(0xFF4A7C59))),
    SOLAR_FLARE("Solar Flare", listOf(Color(0xFF2B0000), Color(0xFF5E1914), Color(0xFFD4A017))),
    FROZEN_TUNDRA("Frozen Tundra", listOf(Color(0xFF0A1931), Color(0xFF185ADB), Color(0xFFEFEFEF))),
    VIOLET_DREAM("Violet Dream", listOf(Color(0xFF240046), Color(0xFF5A189A), Color(0xFF9D4EDD)))
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
    if (config.atmosphere == Atmosphere.COSMIC_FLOW) {
        CosmicFlowBackground(config.isMusicPlaying, modifier, content)
    } else {
        StandardAmbientBackground(config, modifier, content)
    }
}

@Composable
private fun CosmicFlowBackground(
    isMusicPlaying: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cosmicFlow")
    
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isMusicPlaying) 1.15f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Different movement speeds for each "blob"
    val t1 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(35000, easing = LinearEasing), RepeatMode.Restart),
        label = "t1"
    )
    val t2 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(42000, easing = LinearEasing), RepeatMode.Restart),
        label = "t2"
    )
    val t3 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(55000, easing = LinearEasing), RepeatMode.Restart),
        label = "t3"
    )
    val t4 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(48000, easing = LinearEasing), RepeatMode.Restart),
        label = "t4"
    )

    val colors = Atmosphere.COSMIC_FLOW.colors
    
    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            
            // Base background
            drawRect(colors[0])

            // Blob 1: Blue (Google Blue)
            drawBlob(w, h, t1, 0.35f, 0.45f, 0.25f, colors[1].copy(alpha = 0.6f * pulse), 1.1f)
            
            // Blob 2: Deep Purple
            drawBlob(w, h, t2, 0.65f, 0.55f, 0.30f, colors[2].copy(alpha = 0.5f), 1.2f)
            
            // Blob 3: Magenta/Pink
            drawBlob(w, h, t3, 0.45f, 0.65f, 0.35f, colors[3].copy(alpha = 0.45f * pulse), 0.9f)
            
            // Blob 4: Cyan/Azure
            drawBlob(w, h, t4, 0.75f, 0.35f, 0.40f, colors[4].copy(alpha = 0.4f), 1.0f)
        }
        content()
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBlob(
    w: Float, h: Float, 
    phase: Float, 
    centerX: Float, centerY: Float, 
    orbitRadius: Float,
    color: Color,
    blobRadiusScale: Float
) {
    val angle = Math.toRadians(phase.toDouble())
    val offset = Offset(
        x = (w * centerX) + (w * orbitRadius * cos(angle)).toFloat(),
        y = (h * centerY) + (h * orbitRadius * sin(angle * 0.8)).toFloat() // Slight non-circular motion
    )
    drawCircle(
        brush = Brush.radialGradient(
            0.0f to color,
            1.0f to Color.Transparent,
            center = offset,
            radius = w * blobRadiusScale
        ),
        center = offset,
        radius = w * blobRadiusScale
    )
}

@Composable
private fun StandardAmbientBackground(
    config: BackgroundConfig,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "silkFlow")
    
    val musicPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (config.isMusicPlaying) 1.08f else 1f,
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
            
            drawRect(colors[0])

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
