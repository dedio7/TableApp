package com.example.tabletapp.clock

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.tabletapp.clock.enhanced.BinaryClockTheme
import com.example.tabletapp.clock.enhanced.ClockConfig
import com.example.tabletapp.clock.enhanced.ClockMode
import com.example.tabletapp.clock.enhanced.EnhancedBinaryClock

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
    binaryMode: String = "BINARY",
    binaryTheme: String = "DEFAULT"
) {
    when (clockType) {
        ClockType.FLIP      -> FlipClock(modifier = modifier, textColor = textColor)
        ClockType.DIGITAL   -> DigitalClock(modifier = modifier, textColor = textColor, showSeconds = showSeconds)
        ClockType.ANALOG    -> AnalogClock(modifier = modifier, textColor = textColor, showSeconds = showSeconds)
        ClockType.NIXIE     -> NixieClock(modifier = modifier, textColor = textColor, showSeconds = showSeconds)
        ClockType.PIXEL     -> PixelClock(modifier = modifier, textColor = textColor, showSeconds = showSeconds)
        ClockType.BINARY    -> {
            val mode = if (binaryMode == "BCD") ClockMode.BCD else ClockMode.BINARY
            val theme = if (binaryTheme == "ACCENT") {
                BinaryClockTheme(
                    litColor = Color(0xFF00E5FF),
                    accentColor = Color(0xFFFF4081)
                )
            } else {
                BinaryClockTheme(litColor = textColor)
            }
            EnhancedBinaryClock(
                modifier = modifier,
                config = ClockConfig(mode = mode, showSeconds = showSeconds, theme = theme)
            )
        }
        ClockType.WORD_CLOCK -> WordClock(modifier = modifier, textColor = textColor)
    }
}
