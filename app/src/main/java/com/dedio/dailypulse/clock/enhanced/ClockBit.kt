package com.dedio.dailypulse.clock.enhanced

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
        drawRoundRect(
            color = color,
            size = Size(sizePx, sizePx),
            cornerRadius = CornerRadius(sizePx * config.theme.dotCornerRadiusRatio)
        )
    }
}
