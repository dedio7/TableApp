package com.example.dailypulse.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.dailypulse.ui.i18n.LocalStrings

@Composable
fun BatteryWidget(
    modifier: Modifier = Modifier,
    textColor: Color = Color.White
) {
    val context = LocalContext.current
    val strings = LocalStrings.current

    val batteryLevel = remember { mutableFloatStateOf(100f) }
    val isCharging = remember { mutableStateOf(false) }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                if (intent == null) return
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)

                if (level >= 0 && scale > 0) {
                    batteryLevel.floatValue = (level.toFloat() / scale.toFloat()) * 100f
                }

                isCharging.value = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL ||
                        plugged == BatteryManager.BATTERY_PLUGGED_AC ||
                        plugged == BatteryManager.BATTERY_PLUGGED_USB ||
                        plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS
            }
        }

        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(receiver, filter)

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    val levelPercent = batteryLevel.floatValue
    val charging = isCharging.value

    val fillColor = when {
        levelPercent > 50f -> Color(0xFF4CAF50)
        levelPercent > 20f -> Color(0xFFFFC107)
        else -> Color(0xFFF44336)
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Battery icon drawn with Canvas
        Canvas(modifier = Modifier.size(width = 44.dp, height = 22.dp)) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            val bodyLeft = 0f
            val bodyTop = 0f
            val tipWidth = canvasWidth * 0.08f
            val bodyWidth = canvasWidth - tipWidth - 2f
            val bodyHeight = canvasHeight
            val cornerRadius = 3.dp.toPx()

            // Battery body outline
            drawRoundRect(
                color = textColor.copy(alpha = 0.8f),
                topLeft = Offset(bodyLeft, bodyTop),
                size = Size(bodyWidth, bodyHeight),
                cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Battery tip (positive terminal)
            val tipHeight = bodyHeight * 0.4f
            val tipLeft = bodyLeft + bodyWidth
            val tipTop = (bodyHeight - tipHeight) / 2f
            drawRoundRect(
                color = textColor.copy(alpha = 0.8f),
                topLeft = Offset(tipLeft, tipTop),
                size = Size(tipWidth, tipHeight),
                cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
            )

            // Fill level
            val strokeWidth = 1.5.dp.toPx()
            val fillPadding = strokeWidth + 1.5.dp.toPx()
            val maxFillWidth = bodyWidth - fillPadding * 2f
            val fillWidth = maxFillWidth * (levelPercent / 100f).coerceIn(0f, 1f)
            val fillHeight = bodyHeight - fillPadding * 2f

            if (fillWidth > 0f) {
                drawRoundRect(
                    color = fillColor,
                    topLeft = Offset(bodyLeft + fillPadding, bodyTop + fillPadding),
                    size = Size(fillWidth, fillHeight),
                    cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
                )
            }

            // Lightning bolt overlay when charging
            if (charging) {
                val boltPath = Path().apply {
                    val cx = bodyLeft + bodyWidth / 2f
                    val cy = bodyHeight / 2f
                    val boltW = bodyWidth * 0.22f
                    val boltH = bodyHeight * 0.7f

                    moveTo(cx + boltW * 0.1f, cy - boltH / 2f)
                    lineTo(cx - boltW * 0.5f, cy + boltH * 0.05f)
                    lineTo(cx - boltW * 0.05f, cy + boltH * 0.05f)
                    lineTo(cx - boltW * 0.1f, cy + boltH / 2f)
                    lineTo(cx + boltW * 0.5f, cy - boltH * 0.05f)
                    lineTo(cx + boltW * 0.05f, cy - boltH * 0.05f)
                    close()
                }
                drawPath(
                    path = boltPath,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Text info
        Column(
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${levelPercent.toInt()}%",
                color = textColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (charging) strings.chargingLabel else strings.batteryLabel,
                color = textColor.copy(alpha = 0.7f),
                fontSize = 10.sp
            )
        }
    }
}
