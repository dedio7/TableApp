package com.dedio.dailypulse.clock

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.delay
import java.util.Calendar

/**
 * Nixie tube style clock.
 * Optimized to reduce object allocations during drawing.
 */
@Composable
fun NixieClock(
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    showSeconds: Boolean = true,
    isFullScreen: Boolean = false,
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

    // Nixie colors
    val nixieAmber = Color(0xFFFF8C00)
    val nixieGlow1 = nixieAmber.copy(alpha = 0.35f)
    val nixieGlow2 = nixieAmber.copy(alpha = 0.12f)
    val nixieGlow3 = nixieAmber.copy(alpha = 0.05f)
    val tubeColor = Color(0xFF0A0A14)
    val tubeRim = Color(0xFF252540)
    val secAmber = nixieAmber.copy(alpha = 0.65f)

    // Pre-allocated Paint objects
    val textPaint = remember {
        Paint().apply {
            typeface = Typeface.create("serif", Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
    }
    val glowPaint = remember {
        Paint().apply {
            typeface = Typeface.create("serif", Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val cw = size.width
        val ch = size.height

        val colonW = cw * 0.045f
        val spacing = cw * 0.022f
        val totalMainW = cw - colonW - spacing * 4
        val tubeW = (totalMainW / 4f).coerceAtMost(ch * 0.35f)
        val tubeH = (ch * (if (isFullScreen) 0.58f else 0.48f)).coerceAtMost(tubeW * 1.85f)
        val cornerR = tubeW * 0.18f
        val startY = if (showSeconds) (if (isFullScreen) ch * 0.10f else ch * 0.18f) else (ch - tubeH) / 2f
        val startX = (cw - (tubeW * 4 + colonW + spacing * 4)) / 2f

        val digits = listOf(
            hour.intValue / 10,
            hour.intValue % 10,
            -1, // colon
            minute.intValue / 10,
            minute.intValue % 10
        )

        var xPos = startX

        for (item in digits) {
            if (item == -1) {
                // Colon dots
                val cx = xPos + colonW / 2f
                val dot1Y = startY + tubeH * 0.33f
                val dot2Y = startY + tubeH * 0.67f
                val dotR = tubeW * 0.07f
                if (showColon) {
                    for (dot in listOf(dot1Y, dot2Y)) {
                        drawCircle(nixieGlow3, dotR * 8f, Offset(cx, dot))
                        drawCircle(nixieGlow2, dotR * 4f, Offset(cx, dot))
                        drawCircle(nixieGlow1, dotR * 2f, Offset(cx, dot))
                        drawCircle(nixieAmber, dotR, Offset(cx, dot))
                    }
                }
                xPos += colonW + spacing
            } else {
                val digitStr = item.toString()

                // Tube background
                drawRoundRect(
                    color = tubeColor,
                    topLeft = Offset(xPos, startY),
                    size = Size(tubeW, tubeH),
                    cornerRadius = CornerRadius(cornerR)
                )
                // Tube rim
                drawRoundRect(
                    color = tubeRim,
                    topLeft = Offset(xPos, startY),
                    size = Size(tubeW, tubeH),
                    cornerRadius = CornerRadius(cornerR),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
                )

                // Glow layers
                val cx = xPos + tubeW / 2f
                val ty = startY + tubeH * 0.75f
                
                // Draw with cached paints
                glowPaint.color = nixieGlow3.toArgb()
                glowPaint.textSize = tubeH * 0.90f
                drawContext.canvas.nativeCanvas.drawText(digitStr, cx, ty, glowPaint)
                
                glowPaint.color = nixieGlow2.toArgb()
                glowPaint.textSize = tubeH * 0.80f
                drawContext.canvas.nativeCanvas.drawText(digitStr, cx, ty, glowPaint)
                
                textPaint.color = nixieAmber.copy(alpha = 0.55f).toArgb()
                textPaint.textSize = tubeH * 0.74f
                drawContext.canvas.nativeCanvas.drawText(digitStr, cx, ty, textPaint)
                
                textPaint.color = nixieAmber.toArgb()
                textPaint.textSize = tubeH * 0.72f
                drawContext.canvas.nativeCanvas.drawText(digitStr, cx, ty, textPaint)

                // Tube reflection
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.025f),
                    topLeft = Offset(xPos + tubeW * 0.1f, startY + tubeH * 0.03f),
                    size = Size(tubeW * 0.8f, tubeH * 0.25f),
                    cornerRadius = CornerRadius(cornerR * 0.8f)
                )

                xPos += tubeW + spacing
            }
        }

        // Seconds
        if (showSeconds) {
            val s0 = second.intValue / 10
            val s1 = second.intValue % 10
            val secTubeW = tubeW * 0.46f
            val secTubeH = tubeH * 0.38f
            val secCorner = secTubeW * 0.18f
            val secY = startY + tubeH + tubeH * 0.10f
            val secX = (cw - secTubeW * 2 - spacing) / 2f

            textPaint.color = secAmber.toArgb()
            textPaint.textSize = secTubeH * 0.72f

            for ((i, d) in listOf(s0, s1).withIndex()) {
                val sx = secX + i * (secTubeW + spacing)
                drawRoundRect(tubeColor, Offset(sx, secY), Size(secTubeW, secTubeH), CornerRadius(secCorner))
                drawRoundRect(tubeRim, Offset(sx, secY), Size(secTubeW, secTubeH), CornerRadius(secCorner),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(1f))
                drawContext.canvas.nativeCanvas.drawText(
                    d.toString(), sx + secTubeW / 2f, secY + secTubeH * 0.76f, textPaint
                )
            }
        }
    }
}
