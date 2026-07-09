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
    PRISMATIC_WAVE("Prismatic Wave", listOf(Color(0xFF0A0A14), Color(0xFF9C27B0), Color(0xFF3F51B5), Color(0xFF00BCD4), Color(0xFF4CAF50), Color(0xFFFFEB3B), Color(0xFFFF9800))),
    DEEP_SPACE("Deep Space", listOf(Color(0xFF020617), Color(0xFF0F172A), Color(0xFF1E1B4B), Color(0xFF1e293b))),
    MIDNIGHT_SILK("Midnight Silk", listOf(Color(0xFF0D0D0D), Color(0xFF1A1A1A), Color(0xFF262626), Color(0xFF333333))),
    VIOLET_DREAM("Violet Dream", listOf(Color(0xFF240046), Color(0xFF5A189A), Color(0xFF9D4EDD), Color(0xFFC77DFF))),
    NEON_PULSE("Neon Pulse", listOf(Color(0xFF001233), Color(0xFF2D0036), Color(0xFF00F5D4), Color(0xFF7000FF))),
    OCEAN_DEPTHS("Ocean Depths", listOf(Color(0xFF082F49), Color(0xFF075985), Color(0xFF0C4A6E), Color(0xFF38BDF8))),
    FOREST_MIST("Forest Mist", listOf(Color(0xFF0B120B), Color(0xFF1B3022), Color(0xFF4A7C59), Color(0xFF2D6A4F))),
    SOLAR_FLARE("Solar Flare", listOf(Color(0xFF2B0000), Color(0xFF5E1914), Color(0xFFD4A017), Color(0xFFFF5400))),
    GOLDEN_HOUR("Golden Hour", listOf(Color(0xFF450A0A), Color(0xFF78350F), Color(0xFF92400E), Color(0xFFB45309))),
    NORDIC_AURORA("Nordic Aurora", listOf(Color(0xFF064E3B), Color(0xFF0F172A), Color(0xFF4C1D95), Color(0xFF10b981))),
    FROZEN_TUNDRA("Frozen Tundra", listOf(Color(0xFF0A1931), Color(0xFF185ADB), Color(0xFFEFEFEF), Color(0xFF93C5FD))),
    GEMINI_ANIMATED("Gemini (Animated)", listOf(
        Color(0xFF020617), // Deep Navy Base
        Color(0xFF4285F4), // Google Blue
        Color(0xFF9B72CB), // Purple
        Color(0xFFD96570), // Soft Red
        Color(0xFF1AA7EC), // Electric Blue
        Color(0xFF00F5D4), // Teal/Green
        Color(0xFFF4B400)  // Golden Yellow
    ))
}

data class BackgroundConfig(
    val atmosphere: Atmosphere = Atmosphere.DEEP_SPACE,
    val isMusicPlaying: Boolean = false,
)

@Composable
fun AmbientBackground(
    config: BackgroundConfig,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ambientFlow")
    
    // Music Pulse - Dynamic for all themes
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (config.isMusicPlaying) 1.12f else 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (config.isMusicPlaying) 1500 else 4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // A list of time values for each possible blob (up to 6)
    // We use different durations to make movement feel organic
    val durations = listOf(35000, 42000, 55000, 48000, 62000, 39000)
    val timePhases = durations.mapIndexed { index, duration ->
        infiniteTransition.animateFloat(
            initialValue = 0f, targetValue = 360f,
            animationSpec = infiniteRepeatable(tween(duration, easing = LinearEasing), RepeatMode.Restart),
            label = "t$index"
        )
    }

    val atmosphere = config.atmosphere
    val colors = atmosphere.colors
    val baseColor = colors[0]
    val blobColors = colors.drop(1)
    
    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            
            // 1. Solid Base
            drawRect(baseColor)

            // 2. Dynamic Rendering based on theme
            if (atmosphere == Atmosphere.GEMINI_ANIMATED) {
                // High-performance Mesh Gradient Simulation for Gemini
                renderGeminiStyle(w, h, timePhases, blobColors, pulse)
            } else {
                // Standard organic blob system
                blobColors.forEachIndexed { index, color ->
                    if (index < timePhases.size) {
                        val phase = timePhases[index].value
                        
                        // Assign semi-random positions and orbits based on index
                        val centerX = when (index % 4) {
                            0 -> 0.3f; 1 -> 0.7f; 2 -> 0.4f; else -> 0.6f
                        }
                        val centerY = when (index % 3) {
                            0 -> 0.4f; 1 -> 0.6f; else -> 0.5f
                        }
                        val orbitRadius = 0.2f + (index * 0.05f)
                        val blobRadiusScale = 0.8f + (index * 0.1f)
                        
                        // Alpha gets softer for extra layers to avoid over-saturation
                        val alphaScale = if (index > 2) 0.35f else 0.5f
                        val finalColor = color.copy(alpha = alphaScale * pulse)

                        drawMovingBlob(
                            w, h, 
                            phase, 
                            centerX, centerY, 
                            orbitRadius, 
                            finalColor, 
                            blobRadiusScale,
                            index
                        )
                    }
                }
            }
        }
        content()
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.renderGeminiStyle(
    w: Float, h: Float, 
    timePhases: List<State<Float>>, 
    colors: List<Color>,
    pulse: Float
) {
    // Gemini style uses overlapping large mesh gradients that move more fluidly
    colors.forEachIndexed { index, color ->
        val phase = timePhases[index % timePhases.size].value
        val angle = Math.toRadians(phase.toDouble())
        
        // Much more dynamic and unpredictable movement patterns
        val speedMult = 1.0 + (index * 0.15)
        val xShift = (cos(angle * 0.7 * speedMult) * 0.5 + sin(angle * 0.4) * 0.2).toFloat()
        val yShift = (sin(angle * 0.5 * speedMult) * 0.4 + cos(angle * 0.3) * 0.1).toFloat()
        
        val center = Offset(
            x = (w * (0.5f + xShift)),
            y = (h * (0.5f + yShift))
        )
        
        // Varying radii to create depth
        val radiusBase = if (index % 2 == 0) 1.4f else 1.0f
        val radius = w * (radiusBase + (index * 0.15f))
        
        // Layer blending: higher indices (new colors) are more subtle
        val alphaBase = when(index) {
            0 -> 0.55f
            1 -> 0.45f
            2 -> 0.40f
            else -> 0.30f
        }

        drawCircle(
            brush = Brush.radialGradient(
                0.0f to color.copy(alpha = alphaBase * pulse),
                0.5f to color.copy(alpha = (alphaBase * 0.4f) * pulse),
                1.0f to Color.Transparent,
                center = center,
                radius = radius
            ),
            center = center,
            radius = radius
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMovingBlob(
    w: Float, h: Float, 
    phase: Float, 
    centerX: Float, centerY: Float, 
    orbitRadius: Float,
    color: Color,
    blobRadiusScale: Float,
    index: Int
) {
    val angle = Math.toRadians(phase.toDouble())
    
    // Create organic movement using a mix of frequencies
    val xOffset = cos(angle).toFloat()
    val yOffset = sin(angle * (0.7 + (index * 0.1))).toFloat()

    val offset = Offset(
        x = (w * centerX) + (w * orbitRadius * xOffset),
        y = (h * centerY) + (h * orbitRadius * yOffset)
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
