package com.dedio.dailypulse.clock

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import kotlinx.coroutines.delay
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.sin

/**
 * Analog clock with hour, minute and second hands drawn on Canvas.
 * Features tick marks, gradient hands and a subtle glow effect on the second hand.
 */
@Composable
fun AnalogClock(
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    showSeconds: Boolean = true,
    isNeon: Boolean = false,
    isFullScreen: Boolean = false,
) {
    val hour = remember { mutableIntStateOf(0) }
    val minute = remember { mutableIntStateOf(0) }
    val second = remember { mutableIntStateOf(0) }

    LaunchedEffect(showSeconds) {
        while (true) {
            val cal = Calendar.getInstance()
            hour.intValue = cal[Calendar.HOUR_OF_DAY]
            minute.intValue = cal[Calendar.MINUTE]
            second.intValue = cal[Calendar.SECOND]
            delay(if (showSeconds) 1000L else 10_000L)
        }
    }

    val h = hour.intValue
    val m = minute.intValue
    val s = second.intValue

    // Derived colors
    val faceColor = Color(0xFF0D0D1A)
    val rimColor = textColor.copy(alpha = 0.15f)
    val tickColor = textColor.copy(alpha = 0.4f)
    val majorTickColor = if (isNeon) Color(0xFF00E5FF) else textColor.copy(alpha = 0.7f)
    val hourHandColor = if (isNeon) Color(0xFF00E5FF) else textColor
    val minuteHandColor = if (isNeon) Color(0xFF00E5FF).copy(alpha = 0.85f) else textColor.copy(alpha = 0.9f)
    val secondHandColor = if (isNeon) Color(0xFFFF4081) else Color(0xFFE8722A)
    val centerDotColor = if (isNeon) Color(0xFF00E5FF) else textColor
    val accentOrange = if (isNeon) Color(0xFFFF4081) else Color(0xFFE8722A)

    Canvas(modifier = modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val radius = minOf(cx, cy) * (if (isFullScreen) 0.95f else 0.88f)

        // ── Clock face ──────────────────────────────────────────────────────
        drawCircle(color = faceColor, radius = radius, center = Offset(cx, cy))
        drawCircle(color = rimColor, radius = radius, center = Offset(cx, cy), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f))

        // ── Tick marks ──────────────────────────────────────────────────────
        for (i in 0 until 60) {
            val angle = Math.toRadians((i * 6.0) - 90)
            val isMajor = i % 5 == 0
            val inner = if (isMajor) radius * 0.82f else radius * 0.90f
            val outer = radius * 0.97f
            val strokeW = if (isMajor) 2.5f else 1f
            
            drawLine(
                color = if (isMajor) majorTickColor else tickColor,
                start = Offset(cx + inner * cos(angle).toFloat(), cy + inner * sin(angle).toFloat()),
                end = Offset(cx + outer * cos(angle).toFloat(), cy + outer * sin(angle).toFloat()),
                strokeWidth = strokeW,
            )
        }

        // ── Hour hand ───────────────────────────────────────────────────────
        val hourAngle = Math.toRadians((h % 12 + m / 60.0) * 30.0 - 90)
        val hourLen = radius * 0.52f
        val hourW = radius * 0.055f
        
        drawLine(
            color = hourHandColor,
            start = Offset(cx, cy),
            end = Offset(cx + hourLen * cos(hourAngle).toFloat(), cy + hourLen * sin(hourAngle).toFloat()),
            strokeWidth = hourW,
            cap = StrokeCap.Round
        )

        // ── Minute hand ─────────────────────────────────────────────────────
        val minAngle = Math.toRadians((m + s / 60.0) * 6.0 - 90)
        val minLen = radius * 0.76f
        val minW = radius * 0.032f
        
        drawLine(
            color = minuteHandColor,
            start = Offset(cx, cy),
            end = Offset(cx + minLen * cos(minAngle).toFloat(), cy + minLen * sin(minAngle).toFloat()),
            strokeWidth = minW,
            cap = StrokeCap.Round
        )

        // ── Second hand ─────────────────────────────────────────────────────
        if (showSeconds) {
            val secAngle = Math.toRadians(s * 6.0 - 90)
            val secLen = radius * 0.82f
            val secTailLen = radius * 0.18f
            val secW = radius * 0.018f
            
            // Tail
            drawLine(
                color = secondHandColor.copy(alpha = 0.6f),
                start = Offset(cx, cy),
                end = Offset(cx - secTailLen * cos(secAngle).toFloat(), cy - secTailLen * sin(secAngle).toFloat()),
                strokeWidth = secW,
                cap = StrokeCap.Round
            )
            // Main
            drawLine(
                color = secondHandColor,
                start = Offset(cx, cy),
                end = Offset(cx + secLen * cos(secAngle).toFloat(), cy + secLen * sin(secAngle).toFloat()),
                strokeWidth = secW,
                cap = StrokeCap.Round
            )
        }

        // ── Center dot ──────────────────────────────────────────────────────
        drawCircle(color = accentOrange, radius = radius * 0.045f, center = Offset(cx, cy))
        drawCircle(color = centerDotColor, radius = radius * 0.02f, center = Offset(cx, cy))
    }
}
