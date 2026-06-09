package com.example.tabletapp.clock

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import kotlinx.coroutines.delay
import java.util.Calendar

/**
 * 7-segment digital clock display.
 *
 * Draws each digit as a classic 7-segment LCD display with dim "ghost" segments.
 * A blinking colon separates hours from minutes; optional seconds shown below.
 */
@Composable
fun DigitalClock(
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    showSeconds: Boolean = true
) {
    val hour = remember { mutableIntStateOf(0) }
    val minute = remember { mutableIntStateOf(0) }
    val second = remember { mutableIntStateOf(0) }
    val tick = remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        while (true) {
            val cal = Calendar.getInstance()
            hour.intValue = cal.get(Calendar.HOUR_OF_DAY)
            minute.intValue = cal.get(Calendar.MINUTE)
            second.intValue = cal.get(Calendar.SECOND)
            tick.longValue = System.currentTimeMillis()
            delay(1000L)
        }
    }

    val showColon = (tick.longValue / 500L) % 2 == 0L
    val dimColor = textColor.copy(alpha = 0.06f)
    val segColor = textColor
    val accentColor = Color(0xFFE8722A)

    Canvas(modifier = modifier.fillMaxSize()) {
        val cw = size.width
        val ch = size.height

        // Layout: 4 big digit panels + 1 colon + optional 2 small second panels
        val colonW = cw * 0.05f
        val spacing = cw * 0.025f
        val totalMainW = cw - colonW - spacing * 4
        val digitW = totalMainW / 4f
        // Riduzione della dimensione: da 0.60f a 0.50f e rapporto digitW ridotto
        val digitH = (ch * 0.50f).coerceAtMost(digitW * 1.5f)
        val startY = if (showSeconds) (ch - digitH * 1.6f) / 2f else (ch - digitH) / 2f
        val startX = (cw - (digitW * 4 + colonW + spacing * 4)) / 2f

        val h0 = hour.intValue / 10
        val h1 = hour.intValue % 10
        val m0 = minute.intValue / 10
        val m1 = minute.intValue % 10

        // Draw the 4 main digits
        drawSevenSegDigit(h0, startX, startY, digitW, digitH, segColor, dimColor)
        drawSevenSegDigit(h1, startX + digitW + spacing, startY, digitW, digitH, segColor, dimColor)

        // Colon
        if (showColon) {
            val cx = startX + digitW * 2 + spacing * 2 + colonW / 2f
            val dotR = digitH * 0.06f
            val dot1Y = startY + digitH * 0.30f
            val dot2Y = startY + digitH * 0.70f
            // Glow
            drawCircle(accentColor.copy(alpha = 0.25f), dotR * 3f, Offset(cx, dot1Y))
            drawCircle(accentColor.copy(alpha = 0.25f), dotR * 3f, Offset(cx, dot2Y))
            drawCircle(accentColor, dotR, Offset(cx, dot1Y))
            drawCircle(accentColor, dotR, Offset(cx, dot2Y))
        }

        val afterColonX = startX + digitW * 2 + spacing * 3 + colonW
        drawSevenSegDigit(m0, afterColonX, startY, digitW, digitH, segColor, dimColor)
        drawSevenSegDigit(m1, afterColonX + digitW + spacing, startY, digitW, digitH, segColor, dimColor)

        // Seconds — small, below
        if (showSeconds) {
            val s0 = second.intValue / 10
            val s1 = second.intValue % 10
            val secDigitW = digitW * 0.50f
            val secDigitH = digitH * 0.45f
            val secY = startY + digitH + digitH * 0.12f
            val secX = (cw - secDigitW * 2 - spacing) / 2f
            drawSevenSegDigit(s0, secX, secY, secDigitW, secDigitH, accentColor.copy(alpha = 0.75f), dimColor)
            drawSevenSegDigit(s1, secX + secDigitW + spacing, secY, secDigitW, secDigitH, accentColor.copy(alpha = 0.75f), dimColor)
        }
    }
}

// Segment patterns indexed by digit 0-9: [a, b, c, d, e, f, g]
private val SEG = arrayOf(
    intArrayOf(1, 1, 1, 1, 1, 1, 0), // 0
    intArrayOf(0, 1, 1, 0, 0, 0, 0), // 1
    intArrayOf(1, 1, 0, 1, 1, 0, 1), // 2
    intArrayOf(1, 1, 1, 1, 0, 0, 1), // 3
    intArrayOf(0, 1, 1, 0, 0, 1, 1), // 4
    intArrayOf(1, 0, 1, 1, 0, 1, 1), // 5
    intArrayOf(1, 0, 1, 1, 1, 1, 1), // 6
    intArrayOf(1, 1, 1, 0, 0, 0, 0), // 7
    intArrayOf(1, 1, 1, 1, 1, 1, 1), // 8
    intArrayOf(1, 1, 1, 1, 0, 1, 1)  // 9
)

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSevenSegDigit(
    digit: Int,
    x: Float, y: Float,
    w: Float, h: Float,
    onColor: Color,
    offColor: Color
) {
    if (digit < 0 || digit > 9) return
    val pat = SEG[digit]
    val t = w * 0.13f   // segment thickness
    val hw = h / 2f
    val gap = t * 0.25f

    fun seg(on: Boolean, start: Offset, end: Offset) {
        drawLine(
            color = if (on) onColor else offColor,
            start = start,
            end = end,
            strokeWidth = t,
            cap = StrokeCap.Round
        )
    }

    // a — top horizontal
    seg(pat[0] == 1, Offset(x + t + gap, y + t / 2), Offset(x + w - t - gap, y + t / 2))
    // b — top-right vertical
    seg(pat[1] == 1, Offset(x + w - t / 2, y + t + gap), Offset(x + w - t / 2, y + hw - t / 2 - gap))
    // c — bottom-right vertical
    seg(pat[2] == 1, Offset(x + w - t / 2, y + hw + t / 2 + gap), Offset(x + w - t / 2, y + h - t - gap))
    // d — bottom horizontal
    seg(pat[3] == 1, Offset(x + t + gap, y + h - t / 2), Offset(x + w - t - gap, y + h - t / 2))
    // e — bottom-left vertical
    seg(pat[4] == 1, Offset(x + t / 2, y + hw + t / 2 + gap), Offset(x + t / 2, y + h - t - gap))
    // f — top-left vertical
    seg(pat[5] == 1, Offset(x + t / 2, y + t + gap), Offset(x + t / 2, y + hw - t / 2 - gap))
    // g — middle horizontal
    seg(pat[6] == 1, Offset(x + t + gap, y + hw), Offset(x + w - t - gap, y + hw))
}
