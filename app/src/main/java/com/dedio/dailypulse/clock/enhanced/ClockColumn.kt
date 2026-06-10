package com.dedio.dailypulse.clock.enhanced

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ClockColumnComponent(
    column: ClockColumn,
    config: ClockConfig,
    showValue: Boolean,
    dotSizePx: Float,
    dotGapPx: Float
) {
    val baseColor = when (column.colorType) {
        ColorType.NORMAL -> config.theme.litColor
        ColorType.DIMMED -> config.theme.litColor.copy(alpha = 0.85f)
        ColorType.ACCENT -> config.theme.accentColor
    }

    val density = LocalDensity.current
    val dotSizeDp = with(density) { dotSizePx.toDp() }
    val dotGapDp = with(density) { dotGapPx.toDp() }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxHeight()
    ) {
        // Bits
        for (bit in 0 until column.bits) {
            val bitIndex = column.bits - 1 - bit
            val isLit = (column.value shr bitIndex) and 1 == 1
            
            ClockBit(
                isLit = isLit,
                baseColor = baseColor,
                config = config,
                sizePx = dotSizePx
            )
            
            if (bit < column.bits - 1) {
                Spacer(modifier = Modifier.height(dotGapDp))
            }
        }

        Spacer(modifier = Modifier.height(dotSizeDp * 0.5f))

        // Label / Value
        Text(
            text = if (showValue) column.value.toString() else column.label,
            color = config.theme.litColor.copy(alpha = 0.7f),
            fontSize = with(density) { (dotSizePx * 0.5f).toSp() },
            fontWeight = FontWeight.Bold
        )
    }
}
