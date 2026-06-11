package com.dedio.dailypulse.sunrise

import android.app.AlarmManager
import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import kotlinx.coroutines.delay
import java.util.*

/**
 * Manages the Sunrise Mode logic: detects next alarm and calculates color transitions.
 */
class SunriseManager(private val context: Context) {

    /**
     * Checks for the next scheduled alarm and returns the progress towards it
     * (0.0 far away, 1.0 at alarm time). Transition starts 30 mins before alarm.
     */
    @Composable
    fun rememberSunriseProgress(): Float {
        var progress by remember { mutableFloatStateOf(0f) }
        
        LaunchedEffect(Unit) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            
            while (true) {
                val nextAlarm = alarmManager.nextAlarmClock
                if (nextAlarm != null) {
                    val triggerTime = nextAlarm.triggerTime
                    val currentTime = System.currentTimeMillis()
                    val timeUntilAlarm = triggerTime - currentTime
                    
                    // Sunrise starts 30 minutes (1,800,000 ms) before alarm
                    val sunriseDuration = 30 * 60 * 1000L
                    
                    progress = when {
                        timeUntilAlarm <= 0 -> 0f // Reset after alarm
                        timeUntilAlarm > sunriseDuration -> 0f
                        else -> 1f - (timeUntilAlarm.toFloat() / sunriseDuration)
                    }
                } else {
                    progress = 0f
                }
                
                delay(60_000L) // Check every minute
            }
        }
        
        return progress
    }

    companion object {
        // Sunrise colors: Night Deep Blue -> Early Dawn -> Full Sunrise Gold
        private val NightPrimary = Color(0xFF0D1B2A)
        private val DawnPrimary = Color(0xFFE8722A) // Warm Orange
        private val SunrisePrimary = Color(0xFFFFB300) // Golden Yellow

        private val NightSecondary = Color(0xFF151528)
        private val DawnSecondary = Color(0xFF4A148C) // Deep Purple
        private val SunriseSecondary = Color(0xFFFF7043) // Soft Red/Coral

        /**
         * Calculates the sunrise color based on progress (0.0 to 1.0)
         */
        fun getSunriseColors(progress: Float): Pair<Color, Color> {
            return if (progress <= 0.5f) {
                // First phase: Night to Dawn
                val p = progress * 2f
                lerp(NightPrimary, DawnPrimary, p) to lerp(NightSecondary, DawnSecondary, p)
            } else {
                // Second phase: Dawn to Full Sunrise
                val p = (progress - 0.5f) * 2f
                lerp(DawnPrimary, SunrisePrimary, p) to lerp(DawnSecondary, SunriseSecondary, p)
            }
        }
    }
}
