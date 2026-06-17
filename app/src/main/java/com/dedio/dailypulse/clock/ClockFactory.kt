package com.dedio.dailypulse.clock

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.dedio.dailypulse.clock.enhanced.BinaryClockTheme
import com.dedio.dailypulse.clock.enhanced.ClockConfig
import com.dedio.dailypulse.clock.enhanced.ClockMode
import com.dedio.dailypulse.clock.enhanced.EnhancedBinaryClock

/**
 * Clock dispatcher — renders the correct clock composable for the given [ClockType].
 *
 * All clocks share the same [modifier], [textColor] and [showSeconds] parameters
 * so that MainScreen can configure them uniformly from AppSettings.
 */
@Composable
fun ClockDisplay(
    clockType: ClockType,
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    showSeconds: Boolean = true,
    isNeon: Boolean = false,
    binaryMode: String = "BINARY",
    binaryTheme: String = "DEFAULT",
    language: String = "IT",
    isFullScreen: Boolean = false,
) {
    when (clockType) {
        ClockType.FLIP      -> FlipClock(modifier = modifier, textColor = textColor, isNeon = isNeon, isFullScreen = isFullScreen)
        ClockType.DIGITAL   -> DigitalClock(modifier = modifier, textColor = textColor, showSeconds = showSeconds, isNeon = isNeon, isFullScreen = isFullScreen)
        ClockType.ANALOG    -> AnalogClock(modifier = modifier, textColor = textColor, showSeconds = showSeconds, isNeon = isNeon, isFullScreen = isFullScreen)
        ClockType.NIXIE     -> NixieClock(modifier = modifier, textColor = textColor, showSeconds = showSeconds, isNeon = isNeon, isFullScreen = isFullScreen)
        ClockType.PIXEL     -> PixelClock(modifier = modifier, textColor = textColor, showSeconds = showSeconds, isNeon = isNeon, isFullScreen = isFullScreen)
        ClockType.BINARY    -> {
            val mode = if (binaryMode == "BCD") ClockMode.BCD else ClockMode.BINARY
            val theme = if ((binaryTheme == "ACCENT") || isNeon) {
                BinaryClockTheme(
                    litColor = if (isNeon) Color(0xFF00E5FF) else Color(0xFF00E5FF),
                    accentColor = if (isNeon) Color(0xFFFF4081) else Color(0xFFFF4081),
                    glowAlpha = if (isNeon) 0.35f else 0.12f,
                )
            } else {
                BinaryClockTheme(litColor = textColor)
            }
            EnhancedBinaryClock(
                modifier = modifier,
                config = ClockConfig(mode = mode, showSeconds = showSeconds, theme = theme),
                isFullScreen = isFullScreen
            )
        }
        ClockType.WORD_CLOCK -> WordClock(modifier = modifier, textColor = textColor, language = language, isNeon = isNeon, isFullScreen = isFullScreen)
    }
}
