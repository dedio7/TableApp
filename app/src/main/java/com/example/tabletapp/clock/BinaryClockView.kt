package com.example.tabletapp.clock

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import kotlinx.coroutines.delay
import java.util.Calendar

/**
 * Binary clock displaying hours, minutes and seconds as vertical columns of dots.
 *
 * Each column represents a value in binary (6 bits, MSB at top).
 * Lit dot = 1, dim dot = 0. Labels H / M / S appear below each column.
 */
@Composable
fun BinaryClock(
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    showSeconds: Boolean = true
) {
    val hour = remember { mutableIntStateOf(0) }
    val minute = remember { mutableIntStateOf(0) }
    val second = remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            val cal = Calendar.getInstance()
            hour.intValue = cal.get(Calendar.HOUR_OF_DAY)
            minute.intValue = cal.get(Calendar.MINUTE)
            second.intValue = cal.get(Calendar.SECOND)
            delay(1000L)
        }
    }

    val litColor = textColor
    val dimColor = textColor.copy(alpha = 0.08f)
    val accentColor = Color(0xFFE8722A)

    Canvas(modifier = modifier.fillMaxSize()) {
        val cw = size.width
        val ch = size.height

        val cols = if (showSeconds) 3 else 2
        val bits = 6  // rows
        val colSpacing = cw * 0.06f
        val dotSize = ((cw - colSpacing * (cols + 1)) / cols).coerceAtMost(ch * 0.10f)
        val dotGap = dotSize * 0.45f
        val cellSize = dotSize + dotGap
        val colW = dotSize
        val totalH = bits * cellSize
        val labelH = dotSize * 0.9f

        val startY = (ch - totalH - labelH - dotGap) / 2f
        val totalW = cols * colW + colSpacing * (cols - 1).coerceAtLeast(1)
        val startX = (cw - totalW) / 2f

        val values = if (showSeconds)
            listOf(hour.intValue, minute.intValue, second.intValue)
        else
            listOf(hour.intValue, minute.intValue)

        val labelPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.argb(
                (textColor.alpha * 160).toInt(),
                (textColor.red * 255).toInt(),
                (textColor.green * 255).toInt(),
                (textColor.blue * 255).toInt()
            )
            textSize = dotSize * 0.75f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }

        val labels = listOf("H", "M", "S")

        for ((colIdx, value) in values.withIndex()) {
            val cx = startX + colIdx * (colW + colSpacing) + colW / 2f
            val colColor = if (colIdx == 0) textColor
            else if (colIdx == 1) textColor.copy(alpha = 0.85f)
            else accentColor

            for (bit in 0 until bits) {
                val bitIndex = bits - 1 - bit          // MSB at top
                val isLit = (value shr bitIndex) and 1 == 1
                val dotY = startY + bit * cellSize

                // Outer glow for lit dots
                if (isLit) {
                    drawCircle(
                        color = colColor.copy(alpha = 0.12f),
                        radius = dotSize * 0.85f,
                        center = Offset(cx, dotY + dotSize / 2f)
                    )
                }

                drawRoundRect(
                    color = if (isLit) colColor else dimColor,
                    topLeft = Offset(cx - dotSize / 2f, dotY),
                    size = Size(dotSize, dotSize),
                    cornerRadius = CornerRadius(dotSize * 0.28f)
                )
            }

            // Column label
            drawContext.canvas.nativeCanvas.drawText(
                labels[colIdx],
                cx,
                startY + bits * cellSize + labelH,
                labelPaint
            )
        }
    }
}
