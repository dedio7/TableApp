package com.example.dailypulse.clock

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
import kotlinx.coroutines.delay
import java.util.Calendar

/**
 * Pixel / dot-matrix clock.
 *
 * Each digit is rendered as a 3×5 grid of square dots (lit or dim).
 * Uses a retro green-phosphor palette by default, tinted by [textColor].
 */
@Composable
fun PixelClock(
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

    val dimColor = textColor.copy(alpha = 0.07f)
    val accentColor = Color(0xFFE8722A)

    Canvas(modifier = modifier.fillMaxSize()) {
        val cw = size.width
        val ch = size.height

        // Each digit is 3 cols × 5 rows of dots
        // Layout: 4 main digits + 2-dot colon + optional 2 second digits
        val colonDots = 2  // width in dot-columns
        val spacingDots = 1
        val totalDotCols = 3 * 4 + colonDots + spacingDots * 5
        
        // Ulteriore riduzione della dimensione dei punti per farlo stare meglio nello schermo
        val dotSize = (cw / (totalDotCols + 8f)).coerceAtMost(ch / 13f)
        val gap = dotSize * 0.35f
        val cellSize = dotSize + gap

        val gridH = 5 * cellSize
        val startY = if (showSeconds) (ch - gridH * 2.2f) / 2f else (ch - gridH) / 2f
        val totalW = totalDotCols * cellSize
        val startX = (cw - totalW) / 2f

        fun drawDigit(digit: Int, ox: Float, oy: Float, color: Color, offColor: Color) {
            val pattern = PIXEL_FONT[digit.coerceIn(0, 9)]
            for (row in 0 until 5) {
                for (col in 0 until 3) {
                    val on = (pattern[row] shr (2 - col)) and 1 == 1
                    drawRoundRect(
                        color = if (on) color else offColor,
                        topLeft = Offset(ox + col * cellSize, oy + row * cellSize),
                        size = Size(dotSize, dotSize),
                        cornerRadius = CornerRadius(dotSize * 0.3f)
                    )
                }
            }
        }

        // h0, h1, colon, m0, m1
        val h0 = hour.intValue / 10
        val h1 = hour.intValue % 10
        val m0 = minute.intValue / 10
        val m1 = minute.intValue % 10

        var xCursor = startX

        drawDigit(h0, xCursor, startY, textColor, dimColor)
        xCursor += 3 * cellSize + spacingDots * cellSize

        drawDigit(h1, xCursor, startY, textColor, dimColor)
        xCursor += 3 * cellSize + spacingDots * cellSize

        // Colon — two dots in the middle
        val colonX = xCursor + gap / 2f
        val dot1Y = startY + 1 * cellSize + gap / 2
        val dot2Y = startY + 3 * cellSize + gap / 2
        val blinkOn = (System.currentTimeMillis() / 500L) % 2 == 0L
        val colonColor = if (blinkOn) accentColor else dimColor
        drawRoundRect(colonColor, Offset(colonX, dot1Y), Size(dotSize, dotSize), CornerRadius(dotSize * 0.3f))
        drawRoundRect(colonColor, Offset(colonX, dot2Y), Size(dotSize, dotSize), CornerRadius(dotSize * 0.3f))
        xCursor += colonDots * cellSize + spacingDots * cellSize

        drawDigit(m0, xCursor, startY, textColor, dimColor)
        xCursor += 3 * cellSize + spacingDots * cellSize

        drawDigit(m1, xCursor, startY, textColor, dimColor)

        // Seconds — smaller, below
        if (showSeconds) {
            val s0 = second.intValue / 10
            val s1 = second.intValue % 10
            val secDot = dotSize * 0.55f
            val secGap = gap * 0.55f
            val secCell = secDot + secGap
            val secGridW = (3 * 2 + 2) * secCell
            val secX = (cw - secGridW) / 2f
            val secY = startY + gridH + gridH * 0.10f
            val secColor = accentColor.copy(alpha = 0.75f)
            val secDim = dimColor.copy(alpha = 0.04f)

            fun drawSmallDigit(digit: Int, ox: Float, oy: Float) {
                val pattern = PIXEL_FONT[digit.coerceIn(0, 9)]
                for (row in 0 until 5) {
                    for (col in 0 until 3) {
                        val on = (pattern[row] shr (2 - col)) and 1 == 1
                        drawRoundRect(
                            color = if (on) secColor else secDim,
                            topLeft = Offset(ox + col * secCell, oy + row * secCell),
                            size = Size(secDot, secDot),
                            cornerRadius = CornerRadius(secDot * 0.3f)
                        )
                    }
                }
            }
            drawSmallDigit(s0, secX, secY)
            drawSmallDigit(s1, secX + 3 * secCell + 2 * secCell, secY)
        }
    }
}

/**
 * 3-wide × 5-tall pixel font, one IntArray per digit.
 * Each Int is a row bitmask, MSB = leftmost column.
 * Bit 2 = col 0, Bit 1 = col 1, Bit 0 = col 2.
 */
private val PIXEL_FONT: Array<IntArray> = arrayOf(
    intArrayOf(0b111, 0b101, 0b101, 0b101, 0b111), // 0
    intArrayOf(0b010, 0b110, 0b010, 0b010, 0b111), // 1
    intArrayOf(0b111, 0b001, 0b111, 0b100, 0b111), // 2
    intArrayOf(0b111, 0b001, 0b111, 0b001, 0b111), // 3
    intArrayOf(0b101, 0b101, 0b111, 0b001, 0b001), // 4
    intArrayOf(0b111, 0b100, 0b111, 0b001, 0b111), // 5
    intArrayOf(0b111, 0b100, 0b111, 0b101, 0b111), // 6
    intArrayOf(0b111, 0b001, 0b011, 0b010, 0b010), // 7
    intArrayOf(0b111, 0b101, 0b111, 0b101, 0b111), // 8
    intArrayOf(0b111, 0b101, 0b111, 0b001, 0b111)  // 9
)
