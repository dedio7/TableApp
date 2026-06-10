package com.example.dailypulse.clock

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
 *
 * Each digit sits inside a dark rounded "tube" with an amber/orange glow effect
 * simulated by layered circles. The colon is rendered as two glowing dots.
 */
@Composable
fun NixieClock(
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

    // Nixie amber color — warm bright orange
    val nixieAmber = Color(0xFFFF8C00)
    val nixieGlow1 = nixieAmber.copy(alpha = 0.35f)
    val nixieGlow2 = nixieAmber.copy(alpha = 0.12f)
    val nixieGlow3 = nixieAmber.copy(alpha = 0.05f)
    val tubeColor = Color(0xFF0A0A14)
    val tubeRim = Color(0xFF252540)
    val secAmber = nixieAmber.copy(alpha = 0.65f)

    Canvas(modifier = modifier.fillMaxSize()) {
        val cw = size.width
        val ch = size.height

        val colonW = cw * 0.045f
        val spacing = cw * 0.022f
        val totalMainW = cw - colonW - spacing * 4
        val tubeW = totalMainW / 4f
        val tubeH = (ch * 0.62f).coerceAtMost(tubeW * 1.85f)
        val cornerR = tubeW * 0.18f
        val startY = if (showSeconds) ch * 0.18f else (ch - tubeH) / 2f
        val startX = (cw - (tubeW * 4 + colonW + spacing * 4)) / 2f

        val digits = listOf(
            hour.intValue / 10,
            hour.intValue % 10,
            -1, // colon
            minute.intValue / 10,
            minute.intValue % 10
        )

        val textPaint = Paint().apply {
            color = nixieAmber.toArgb()
            textSize = tubeH * 0.72f
            typeface = Typeface.create("serif", Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        val glowPaint = Paint().apply {
            color = nixieGlow1.toArgb()
            textSize = tubeH * 0.82f
            typeface = Typeface.create("serif", Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

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

                // Glow layers (simulate bloom around digit)
                val cx = xPos + tubeW / 2f
                val ty = startY + tubeH * 0.75f
                drawContext.canvas.nativeCanvas.drawText(digitStr, cx, ty, glowPaint.apply {
                    color = nixieGlow3.toArgb()
                    textSize = tubeH * 0.90f
                })
                drawContext.canvas.nativeCanvas.drawText(digitStr, cx, ty, glowPaint.apply {
                    color = nixieGlow2.toArgb()
                    textSize = tubeH * 0.80f
                })
                drawContext.canvas.nativeCanvas.drawText(digitStr, cx, ty, textPaint.apply {
                    color = nixieAmber.copy(alpha = 0.55f).toArgb()
                    textSize = tubeH * 0.74f
                })
                // Sharp digit on top
                drawContext.canvas.nativeCanvas.drawText(digitStr, cx, ty, textPaint.apply {
                    color = nixieAmber.toArgb()
                    textSize = tubeH * 0.72f
                })

                // Tube reflection (top highlight)
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.025f),
                    topLeft = Offset(xPos + tubeW * 0.1f, startY + tubeH * 0.03f),
                    size = Size(tubeW * 0.8f, tubeH * 0.25f),
                    cornerRadius = CornerRadius(cornerR * 0.8f)
                )

                xPos += tubeW + spacing
            }
        }

        // Seconds — small below main display
        if (showSeconds) {
            val s0 = second.intValue / 10
            val s1 = second.intValue % 10
            val secTubeW = tubeW * 0.46f
            val secTubeH = tubeH * 0.38f
            val secCorner = secTubeW * 0.18f
            val secY = startY + tubeH + tubeH * 0.10f
            val secX = (cw - secTubeW * 2 - spacing) / 2f

            val secPaint = Paint().apply {
                color = secAmber.toArgb()
                textSize = secTubeH * 0.72f
                typeface = Typeface.create("serif", Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }

            for ((i, d) in listOf(s0, s1).withIndex()) {
                val sx = secX + i * (secTubeW + spacing)
                drawRoundRect(tubeColor, Offset(sx, secY), Size(secTubeW, secTubeH), CornerRadius(secCorner))
                drawRoundRect(tubeRim, Offset(sx, secY), Size(secTubeW, secTubeH), CornerRadius(secCorner),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(1f))
                drawContext.canvas.nativeCanvas.drawText(
                    d.toString(), sx + secTubeW / 2f, secY + secTubeH * 0.76f, secPaint
                )
            }
        }
    }
}
