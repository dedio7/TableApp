package com.example.tabletapp.clock

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

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
    showSeconds: Boolean = true
) {
    when (clockType) {
        ClockType.FLIP      -> FlipClock(modifier = modifier, textColor = textColor)
        ClockType.DIGITAL   -> DigitalClock(modifier = modifier, textColor = textColor, showSeconds = showSeconds)
        ClockType.ANALOG    -> AnalogClock(modifier = modifier, textColor = textColor, showSeconds = showSeconds)
        ClockType.NIXIE     -> NixieClock(modifier = modifier, textColor = textColor, showSeconds = showSeconds)
        ClockType.PIXEL     -> PixelClock(modifier = modifier, textColor = textColor, showSeconds = showSeconds)
        ClockType.BINARY    -> BinaryClock(modifier = modifier, textColor = textColor, showSeconds = showSeconds)
        ClockType.WORD_CLOCK -> WordClock(modifier = modifier, textColor = textColor)
    }
}
