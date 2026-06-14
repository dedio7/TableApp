package com.dedio.dailypulse.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.animation.Crossfade
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dedio.dailypulse.clock.ClockType

/**
 * An adaptive and stylish battery widget.
 * Changes icon style based on the active clock to ensure visual coherence.
 */
@Composable
fun BatteryWidget(
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    clockType: ClockType = ClockType.FLIP,
    accentColor: Color = Color.White // New parameter to match background
) {
    val context = LocalContext.current

    var batteryLevel by remember { mutableFloatStateOf(100f) }
    var isCharging by remember { mutableStateOf(false) }

    fun updateBatteryState(intent: Intent?) {
        if (intent == null) return
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)

        if (level >= 0 && scale > 0) {
            batteryLevel = (level.toFloat() / scale.toFloat()) * 100f
        }

        isCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL ||
                plugged == BatteryManager.BATTERY_PLUGGED_AC ||
                plugged == BatteryManager.BATTERY_PLUGGED_USB ||
                plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS)
    }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                updateBatteryState(intent)
            }
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val stickyIntent = context.registerReceiver(receiver, filter)
        updateBatteryState(stickyIntent)
        onDispose { 
            try { context.unregisterReceiver(receiver) } catch (_: Exception) { }
        }
    }

    val levelPercent = batteryLevel
    val charging = isCharging

    // Dynamic color logic: derive status color from background accent or level
    val statusColor = when {
        charging -> {
            // If charging, use a very bright/white version of the accent color for a 'glow' effect
            accentColor.copy(alpha = 0.9f)
        }
        levelPercent > 20f -> textColor.copy(alpha = 0.8f)
        else -> Color(0xFFFF5252) // Keep critical red for safety
    }

    val animatedColor by animateColorAsState(targetValue = statusColor, animationSpec = tween(800), label = "color")

    Box(
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.25f))
            .border(0.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Crossfade(targetState = clockType, label = "batteryStyle", animationSpec = tween(500)) { type ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                when (type) {
                    ClockType.DIGITAL, ClockType.PIXEL, ClockType.BINARY -> {
                        TechBatteryIcon(levelPercent, animatedColor)
                    }
                    ClockType.ANALOG, ClockType.NIXIE -> {
                        VintageBatteryIcon(levelPercent, animatedColor, charging)
                    }
                    else -> {
                        MinimalBatteryIcon(levelPercent, animatedColor)
                    }
                }

                Text(
                    text = "${levelPercent.toInt()}%",
                    color = textColor.copy(alpha = 0.9f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (-0.5).sp
                )
            }
        }
    }
}

@Composable
private fun TechBatteryIcon(level: Float, color: Color) {
    Canvas(modifier = Modifier.size(width = 22.dp, height = 10.dp)) {
        val segments = 4
        val gap = 2.dp.toPx()
        val segW = (size.width - (segments - 1) * gap) / segments
        
        for (i in 0 until segments) {
            val isFilled = (level / 100f) > (i.toFloat() / segments)
            drawRect(
                color = if (isFilled) color else color.copy(alpha = 0.15f),
                topLeft = Offset(i * (segW + gap), 0f),
                size = Size(segW, size.height)
            )
        }
    }
}

@Composable
private fun VintageBatteryIcon(level: Float, color: Color, charging: Boolean) {
    Canvas(modifier = Modifier.size(18.dp)) {
        val sw = 1.5.dp.toPx()
        val r = size.minDimension / 2
        drawCircle(color = color.copy(alpha = 0.1f), radius = r, style = Stroke(width = sw))
        val angle = (level / 100f) * 360f - 90f
        val rad = Math.toRadians(angle.toDouble())
        val dotR = 2.5.dp.toPx()
        drawCircle(
            color = color,
            radius = dotR,
            center = Offset(
                (size.width / 2) + (r * Math.cos(rad)).toFloat(),
                (size.height / 2) + (r * Math.sin(rad)).toFloat()
            )
        )
        if (charging) {
            drawCircle(color = color.copy(alpha = 0.3f), radius = dotR * 1.8f, center = center)
        }
    }
}

@Composable
private fun MinimalBatteryIcon(level: Float, color: Color) {
    Box(modifier = Modifier.size(width = 16.dp, height = 12.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val h = size.height * (level / 100f)
            drawLine(
                color = color,
                start = Offset(size.width / 2, size.height),
                end = Offset(size.width / 2, size.height - h),
                strokeWidth = 2.5.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}
