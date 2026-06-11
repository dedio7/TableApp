package com.dedio.dailypulse.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dedio.dailypulse.ui.i18n.LocalStrings

/**
 * A minimalist and stylish battery widget.
 * Matches the glassmorphism aesthetic of the app with clean lines and subtle indicators.
 */
@Composable
fun BatteryWidget(
    modifier: Modifier = Modifier,
    textColor: Color = Color.White
) {
    val context = LocalContext.current
    val strings = LocalStrings.current

    var batteryLevel by remember { mutableFloatStateOf(100f) }
    var isCharging by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                if (intent == null) return
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)

                if (level >= 0 && scale > 0) {
                    batteryLevel = (level.toFloat() / scale.toFloat()) * 100f
                }

                isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL ||
                        plugged == BatteryManager.BATTERY_PLUGGED_AC ||
                        plugged == BatteryManager.BATTERY_PLUGGED_USB ||
                        plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS
            }
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(receiver, filter)
        onDispose { context.unregisterReceiver(receiver) }
    }

    val levelPercent = batteryLevel
    val charging = isCharging

    // Minimalist colors
    val statusColor = when {
        charging -> Color(0xFF00E676) // Subtle Emerald
        levelPercent > 20f -> textColor.copy(alpha = 0.9f) // Clean White
        else -> Color(0xFFFF5252) // Soft Red
    }

    val animatedColor by animateColorAsState(targetValue = statusColor, animationSpec = tween(800), label = "color")

    // Very subtle breathing for charging
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val subtleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.25f))
            .border(0.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Horizontal Battery - Ultra Thin & Clean
            Canvas(modifier = Modifier.size(width = 28.dp, height = 14.dp)) {
                val sw = 1.dp.toPx()
                val bodyW = size.width - 3.dp.toPx()
                val bodyH = size.height
                val corner = 2.dp.toPx()

                // Outline
                drawRoundRect(
                    color = textColor.copy(alpha = 0.3f),
                    size = Size(bodyW, bodyH),
                    cornerRadius = CornerRadius(corner),
                    style = Stroke(width = sw)
                )

                // Tip
                drawRoundRect(
                    color = textColor.copy(alpha = 0.3f),
                    topLeft = Offset(bodyW + 1.dp.toPx(), bodyH * 0.3f),
                    size = Size(2.dp.toPx(), bodyH * 0.4f),
                    cornerRadius = CornerRadius(1.dp.toPx())
                )

                // Fill
                val padding = sw + 1.5.dp.toPx()
                val fillW = (bodyW - padding * 2) * (levelPercent / 100f)
                if (fillW > 0) {
                    drawRoundRect(
                        color = if (charging) animatedColor.copy(alpha = subtleAlpha) else animatedColor,
                        topLeft = Offset(padding, padding),
                        size = Size(fillW, bodyH - padding * 2),
                        cornerRadius = CornerRadius(1.dp.toPx())
                    )
                }
            }

            // Typography - Matching the DateWidget style
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${levelPercent.toInt()}%",
                    color = textColor.copy(alpha = 0.9f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (-0.5).sp
                )
                
                Spacer(modifier = Modifier.width(6.dp))
                
                Text(
                    text = "•",
                    color = textColor.copy(alpha = 0.2f),
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = (if (charging) strings.chargingLabel else strings.batteryLabel).uppercase(),
                    color = if (charging) animatedColor.copy(alpha = subtleAlpha) else textColor.copy(alpha = 0.4f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
