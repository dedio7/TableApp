package com.dedio.dailypulse.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

/**
 * Global state-holder for battery information, shared across components.
 */
data class BatteryInfo(
    val level: Int = 100,
    val isCharging: Boolean = false
)

@Composable
fun rememberBatteryState(): BatteryInfo {
    val context = LocalContext.current
    var state by remember { mutableStateOf(BatteryInfo()) }

    fun update(intent: Intent?) {
        intent?.let {
            val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
            val status = it.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val plugged = it.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
            
            val levelPercent = if (level >= 0 && scale > 0) (level * 100 / scale) else 100
            val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL ||
                    plugged != 0
            
            state = BatteryInfo(levelPercent, charging)
        }
    }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) { update(intent) }
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val initial = context.registerReceiver(receiver, filter)
        update(initial)
        onDispose { try { context.unregisterReceiver(receiver) } catch (_: Exception) {} }
    }

    return state
}
