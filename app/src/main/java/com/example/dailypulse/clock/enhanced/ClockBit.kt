package com.example.dailypulse.clock.enhanced

import androidx.compose.animation.animateColorAsState
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
        animationSpec = tween(durationMillis = 400),
        label = "bitColor"
    )

    val sizeDp = with(LocalDensity.current) { sizePx.toDp() }

    Canvas(modifier = Modifier.size(sizeDp)) {
        if (isLit) {
            drawCircle(
                color = baseColor.copy(alpha = config.theme.glowAlpha),
                radius = sizePx * 0.8f,
                center = Offset(sizePx / 2f, sizePx / 2f)
            )
        }

        drawRoundRect(
            color = color,
            size = Size(sizePx, sizePx),
            cornerRadius = CornerRadius(sizePx * config.theme.dotCornerRadiusRatio)
        )
    }
}
