package com.dedio.dailypulse.clock.enhanced

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun ClockBit(
    isLit: Boolean,
    baseColor: Color,
    config: ClockConfig,
    sizePx: Float
) {
    val color by animateColorAsState(
        targetValue = if (isLit) baseColor else config.theme.dimColor,
        animationSpec = if (isLit) spring(dampingRatio = 0.8f, stiffness = 400f) else tween(durationMillis = 500),
        label = "bitColor"
    )

    val scale by animateFloatAsState(
        targetValue = if (isLit) 1f else 0.85f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f),
        label = "bitScale"
    )

    val glowAlpha by animateFloatAsState(
        targetValue = if (isLit) 0.6f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "bitGlow"
    )

    val sizeDp = with(LocalDensity.current) { sizePx.toDp() }

    Canvas(modifier = Modifier.size(sizeDp)) {
        val scaledSize = sizePx * scale
        val topLeft = Offset((sizePx - scaledSize) / 2f, (sizePx - scaledSize) / 2f)
        val cornerRadius = CornerRadius(scaledSize * config.theme.dotCornerRadiusRatio)

        // Glow Layer
        if (glowAlpha > 0f) {
            drawRoundRect(
                color = baseColor.copy(alpha = glowAlpha * 0.3f),
                topLeft = topLeft - Offset(scaledSize * 0.1f, scaledSize * 0.1f),
                size = Size(scaledSize * 1.2f, scaledSize * 1.2f),
                cornerRadius = CornerRadius(scaledSize * 1.2f * config.theme.dotCornerRadiusRatio)
            )
        }

        // Main Body
        drawRoundRect(
            color = color,
            topLeft = topLeft,
            size = Size(scaledSize, scaledSize),
            cornerRadius = cornerRadius
        )

        // Subtle highlight for "glassy" look when lit
        if (isLit) {
            drawRoundRect(
                color = Color.White.copy(alpha = 0.15f),
                topLeft = topLeft + Offset(scaledSize * 0.15f, scaledSize * 0.1f),
                size = Size(scaledSize * 0.4f, scaledSize * 0.15f),
                cornerRadius = CornerRadius(scaledSize * 0.05f)
            )
        }
    }
}
