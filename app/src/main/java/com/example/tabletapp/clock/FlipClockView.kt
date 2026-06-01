package com.example.tabletapp.clock

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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.util.Calendar

/**
 * A split-flap display style clock composable.
 *
 * Shows hours and minutes in large split panels, each digit having a top and bottom
 * half with a dividing line. Dark panels with rounded corners give a 3D appearance
 * with subtle shadow/gradient effects and a blinking colon separator.
 */
@Composable
fun FlipClock(modifier: Modifier = Modifier, textColor: Color = Color.White) {
    val currentHour = remember { mutableIntStateOf(0) }
    val currentMinute = remember { mutableIntStateOf(0) }
    val currentSecond = remember { mutableIntStateOf(0) }
    val tick = remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        while (true) {
            val cal = Calendar.getInstance()
            currentHour.intValue = cal.get(Calendar.HOUR_OF_DAY)
            currentMinute.intValue = cal.get(Calendar.MINUTE)
            currentSecond.intValue = cal.get(Calendar.SECOND)
            tick.longValue = System.currentTimeMillis()
            delay(1000L)
        }
    }

    val hour = currentHour.intValue
    val minute = currentMinute.intValue
    val second = currentSecond.intValue
    val showColon = (tick.longValue / 1000L) % 2 == 0L

    val panelColor = Color(0xFF1A1A2E)
    val panelTopColor = Color(0xFF22223A)
    val panelBottomColor = Color(0xFF151528)
    val accentOrange = Color(0xFFE8722A)
    val dividerColor = Color(0xFF0D0D1A)
    val shadowColor = Color(0x66000000)

    val digits = listOf(
        hour / 10, hour % 10,
        -1, // colon
        minute / 10, minute % 10
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Calculate panel dimensions
        val totalDigits = 4
        val colonWidth = canvasWidth * 0.06f
        val totalSpacing = canvasWidth * 0.03f * 5 // spacing between elements
        val availableWidth = canvasWidth - colonWidth - totalSpacing - canvasWidth * 0.1f
        val panelWidth = availableWidth / totalDigits
        val panelHeight = (canvasHeight * 0.55f).coerceAtMost(panelWidth * 1.6f)
        val cornerRad = panelWidth * 0.08f
        val spacing = canvasWidth * 0.03f

        val totalWidth = panelWidth * 4 + colonWidth + spacing * 4
        val startX = (canvasWidth - totalWidth) / 2f
        val startY = (canvasHeight - panelHeight) / 2f

        val textPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.argb(
                (textColor.alpha * 255).toInt(),
                (textColor.red * 255).toInt(),
                (textColor.green * 255).toInt(),
                (textColor.blue * 255).toInt()
            )
            textSize = panelHeight * 0.78f
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
            isFakeBoldText = true
        }

        val secondsPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.argb(
                180,
                (accentOrange.red * 255).toInt(),
                (accentOrange.green * 255).toInt(),
                (accentOrange.blue * 255).toInt()
            )
            textSize = panelHeight * 0.18f
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }

        var xPos = startX

        for (item in digits) {
            if (item == -1) {
                // Draw colon
                if (showColon) {
                    val colonCenterX = xPos + colonWidth / 2f
                    val dotRadius = panelWidth * 0.06f
                    val dotY1 = startY + panelHeight * 0.33f
                    val dotY2 = startY + panelHeight * 0.67f

                    // Glow
                    drawCircle(
                        color = accentOrange.copy(alpha = 0.3f),
                        radius = dotRadius * 2.5f,
                        center = Offset(colonCenterX, dotY1)
                    )
                    drawCircle(
                        color = accentOrange.copy(alpha = 0.3f),
                        radius = dotRadius * 2.5f,
                        center = Offset(colonCenterX, dotY2)
                    )
                    drawCircle(
                        color = accentOrange,
                        radius = dotRadius,
                        center = Offset(colonCenterX, dotY1)
                    )
                    drawCircle(
                        color = accentOrange,
                        radius = dotRadius,
                        center = Offset(colonCenterX, dotY2)
                    )
                }
                xPos += colonWidth + spacing
            } else {
                // Draw flip panel
                drawFlipPanel(
                    digit = item,
                    x = xPos,
                    y = startY,
                    width = panelWidth,
                    height = panelHeight,
                    cornerRadius = cornerRad,
                    panelTopColor = panelTopColor,
                    panelBottomColor = panelBottomColor,
                    panelColor = panelColor,
                    accentOrange = accentOrange,
                    dividerColor = dividerColor,
                    shadowColor = shadowColor,
                    textPaint = textPaint
                )
                xPos += panelWidth + spacing
            }
        }

        // Draw seconds below
        val secondsText = String.format("%02d", second)
        drawContext.canvas.nativeCanvas.drawText(
            secondsText,
            canvasWidth / 2f,
            startY + panelHeight + panelHeight * 0.22f,
            secondsPaint
        )
    }
}

private fun DrawScope.drawFlipPanel(
    digit: Int,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    cornerRadius: Float,
    panelTopColor: Color,
    panelBottomColor: Color,
    panelColor: Color,
    accentOrange: Color,
    dividerColor: Color,
    shadowColor: Color,
    textPaint: android.graphics.Paint
) {
    val halfH = height / 2f
    val digitStr = digit.toString()

    // Outer shadow
    drawRoundRect(
        color = shadowColor,
        topLeft = Offset(x + 3.dp.toPx(), y + 5.dp.toPx()),
        size = Size(width, height),
        cornerRadius = CornerRadius(cornerRadius)
    )

    // Side accent (orange edge)
    drawRoundRect(
        color = accentOrange.copy(alpha = 0.25f),
        topLeft = Offset(x - 1.5f, y),
        size = Size(width + 3f, height),
        cornerRadius = CornerRadius(cornerRadius)
    )

    // --- TOP HALF ---
    val topPath = Path().apply {
        addRoundRect(
            RoundRect(
                rect = Rect(x, y, x + width, y + halfH),
                topLeft = androidx.compose.ui.geometry.CornerRadius(cornerRadius),
                topRight = androidx.compose.ui.geometry.CornerRadius(cornerRadius),
                bottomLeft = androidx.compose.ui.geometry.CornerRadius(0f),
                bottomRight = androidx.compose.ui.geometry.CornerRadius(0f)
            )
        )
    }

    clipPath(topPath) {
        // Top panel background with gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(panelTopColor, panelColor),
                startY = y,
                endY = y + halfH
            ),
            topLeft = Offset(x, y),
            size = Size(width, halfH)
        )

        // Shine highlight at top
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.White.copy(alpha = 0.06f), Color.Transparent),
                startY = y,
                endY = y + halfH * 0.4f
            ),
            topLeft = Offset(x, y),
            size = Size(width, halfH * 0.4f)
        )

        // Draw digit text (positioned so top half is visible)
        drawContext.canvas.nativeCanvas.drawText(
            digitStr,
            x + width / 2f,
            y + halfH + textPaint.textSize * 0.34f,
            textPaint
        )
    }

    // --- BOTTOM HALF ---
    val bottomPath = Path().apply {
        addRoundRect(
            RoundRect(
                rect = Rect(x, y + halfH, x + width, y + height),
                topLeft = androidx.compose.ui.geometry.CornerRadius(0f),
                topRight = androidx.compose.ui.geometry.CornerRadius(0f),
                bottomLeft = androidx.compose.ui.geometry.CornerRadius(cornerRadius),
                bottomRight = androidx.compose.ui.geometry.CornerRadius(cornerRadius)
            )
        )
    }

    clipPath(bottomPath) {
        // Bottom panel background with gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(panelColor, panelBottomColor),
                startY = y + halfH,
                endY = y + height
            ),
            topLeft = Offset(x, y + halfH),
            size = Size(width, halfH)
        )

        // Draw digit text (positioned so bottom half is visible)
        drawContext.canvas.nativeCanvas.drawText(
            digitStr,
            x + width / 2f,
            y + halfH + textPaint.textSize * 0.34f,
            textPaint
        )
    }

    // Dividing line
    drawRect(
        color = dividerColor,
        topLeft = Offset(x, y + halfH - 1.5f),
        size = Size(width, 3f)
    )

    // Subtle notch on sides of divider
    val notchSize = 6f
    drawCircle(
        color = dividerColor,
        radius = notchSize,
        center = Offset(x, y + halfH)
    )
    drawCircle(
        color = dividerColor,
        radius = notchSize,
        center = Offset(x + width, y + halfH)
    )
}
