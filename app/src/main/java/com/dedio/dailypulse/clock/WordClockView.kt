package com.dedio.dailypulse.clock

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.Calendar

@Composable
fun WordClock(
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    language: String = "IT",
    isFullScreen: Boolean = false,
) {
    val hour = remember { mutableIntStateOf(0) }
    val minute = remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            val cal = Calendar.getInstance()
            hour.intValue = cal.get(Calendar.HOUR_OF_DAY)
            minute.intValue = cal.get(Calendar.MINUTE)
            delay(1000L) 
        }
    }

    if (language == "EN") {
        EnglishWordClock(modifier, textColor, hour.intValue, minute.intValue, isFullScreen)
    } else {
        ItalianWordClock(modifier, textColor, hour.intValue, minute.intValue, isFullScreen)
    }
}

@Composable
private fun ItalianWordClock(modifier: Modifier, textColor: Color, h: Int, m: Int, isFullScreen: Boolean) {
    val m5 = (m / 5) * 5
    val remainder = m % 5
    val displayHour = if (m5 >= 35) (h + 1) % 24 else h
    val h12 = displayHour % 12

    val lit = mutableSetOf<String>()
    lit += "SONO"; lit += "LE"
    
    if (h12 == 1) {
        lit.remove("SONO"); lit.remove("LE")
        lit += "È"; lit += "L'UNA"
    } else if (h12 == 0) {
        lit += "DODICI"
    } else {
        lit += italianHourWord(h12)
    }

    if (m5 > 0) {
        when (m5) {
            5 -> { lit += "E"; lit += "CINQUE_MIN" }
            10 -> { lit += "E"; lit += "DIECI_MIN" }
            15 -> { lit += "E"; lit += "UN"; lit += "QUARTO" }
            20 -> { lit += "E"; lit += "VENTI" }
            25 -> { lit += "E"; lit += "VENTICINQUE" }
            30 -> { lit += "E"; lit += "MEZZA" }
            35 -> { lit += "MENO"; lit += "VENTICINQUE" }
            40 -> { lit += "MENO"; lit += "VENTI" }
            45 -> { lit += "MENO"; lit += "UN"; lit += "QUARTO" }
            50 -> { lit += "MENO"; lit += "DIECI_MIN" }
            55 -> { lit += "MENO"; lit += "CINQUE_MIN" }
        }
    }

    val grid: List<List<Pair<String, String>>> = listOf(
        listOf("SONO" to "SONO", "LE" to "LE", "È" to "È", "L'UNA" to "L'UNA"),
        listOf("DUE" to "DUE", "TRE" to "TRE", "QUATTRO" to "QUATTRO"),
        listOf("CINQUE" to "CINQUE", "SEI" to "SEI", "SETTE" to "SETTE"),
        listOf("OTTO" to "OTTO", "NOVE" to "NOVE", "DIECI" to "DIECI"),
        listOf("UNDICI" to "UNDICI", "DODICI" to "DODICI"),
        listOf("E" to "E", "MENO" to "MENO", "MEZZA" to "MEZZA"),
        listOf("UN" to "UN", "QUARTO" to "QUARTO", "VENTI" to "VENTI"),
        listOf("VENTICINQUE" to "VENTICINQUE", "CINQUE" to "CINQUE_MIN", "DIECI" to "DIECI_MIN")
    )

    WordClockGrid(modifier, textColor, grid, lit, remainder, isFullScreen)
}

@Composable
private fun EnglishWordClock(modifier: Modifier, textColor: Color, h: Int, m: Int, isFullScreen: Boolean) {
    val m5 = (m / 5) * 5
    val remainder = m % 5
    val displayHour = if (m5 > 30) (h + 1) % 24 else h
    val h12 = displayHour % 12

    val lit = mutableSetOf<String>()
    lit += "IT"; lit += "IS"

    when (m5) {
        0 -> { lit += "OCLOCK" }
        5 -> { lit += "FIVE_MIN"; lit += "PAST" }
        10 -> { lit += "TEN_MIN"; lit += "PAST" }
        15 -> { lit += "QUARTER"; lit += "PAST" }
        20 -> { lit += "TWENTY"; lit += "PAST" }
        25 -> { lit += "TWENTY"; lit += "FIVE_MIN"; lit += "PAST" }
        30 -> { lit += "HALF"; lit += "PAST" }
        35 -> { lit += "TWENTY"; lit += "FIVE_MIN"; lit += "TO" }
        40 -> { lit += "TWENTY"; lit += "TO" }
        45 -> { lit += "QUARTER"; lit += "TO" }
        50 -> { lit += "TEN_MIN"; lit += "TO" }
        55 -> { lit += "FIVE_MIN"; lit += "TO" }
    }
    lit += englishHourWord(h12)

    val grid: List<List<Pair<String, String>>> = listOf(
        listOf("IT" to "IT", "IS" to "IS", "HALF" to "HALF", "TEN" to "TEN_MIN"),
        listOf("QUARTER" to "QUARTER", "TWENTY" to "TWENTY"),
        listOf("FIVE" to "FIVE_MIN", "PAST" to "PAST", "TO" to "TO"),
        listOf("ONE" to "ONE", "TWO" to "TWO", "THREE" to "THREE"),
        listOf("FOUR" to "FOUR", "FIVE" to "FIVE", "SIX" to "SIX"),
        listOf("SEVEN" to "SEVEN", "EIGHT" to "EIGHT", "NINE" to "NINE"),
        listOf("TEN" to "TEN", "ELEVEN" to "ELEVEN", "TWELVE" to "TWELVE"),
        listOf("O'CLOCK" to "OCLOCK")
    )

    WordClockGrid(modifier, textColor, grid, lit, remainder, isFullScreen)
}

@Composable
private fun WordClockGrid(
    modifier: Modifier,
    textColor: Color,
    grid: List<List<Pair<String, String>>>,
    lit: Set<String>,
    remainder: Int,
    isFullScreen: Boolean,
) {
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT

    // Blower scales for portrait to fit words horizontally
    val baseScale = if (isFullScreen) 1.5f else 1.15f
    val scale = if (isPortrait) baseScale * 0.75f else baseScale
    
    val dimColor = textColor.copy(alpha = 0.12f)
    val accentColor = Color(0xFFE8722A)

    Column(
        modifier = modifier.fillMaxSize().padding(if (isPortrait) 8.dp else 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        grid.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                row.forEach { pair ->
                    val (display, id) = pair
                    val isLit = id in lit
                    Text(
                        text = display,
                        color = if (isLit) textColor else dimColor,
                        fontWeight = if (isLit) FontWeight.Bold else FontWeight.Light,
                        fontSize = (19 * scale).sp,
                        letterSpacing = (if (isPortrait) 1.0 * scale else 2.0 * scale).sp,
                        modifier = Modifier.padding(
                            horizontal = (if (isPortrait) 6 * scale else 10 * scale).dp, 
                            vertical = (if (isPortrait) 3 * scale else 4 * scale).dp
                        )
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height((if (isPortrait) 16 * scale else 24 * scale).dp))
        
        // Precision dots
        Row(horizontalArrangement = Arrangement.spacedBy((if (isPortrait) 8 * scale else 12 * scale).dp)) {
            for (i in 1..4) {
                Text(
                    text = "●",
                    color = if (i <= remainder) accentColor else dimColor,
                    fontSize = (12 * scale).sp
                )
            }
        }
    }
}

private fun italianHourWord(h12: Int): String = when (h12) {
    0 -> "DODICI"
    2 -> "DUE"
    3 -> "TRE"
    4 -> "QUATTRO"
    5 -> "CINQUE"
    6 -> "SEI"
    7 -> "SETTE"
    8 -> "OTTO"
    9 -> "NOVE"
    10 -> "DIECI"
    11 -> "UNDICI"
    else -> "DODICI"
}

private fun englishHourWord(h12: Int): String = when (h12) {
    0 -> "TWELVE"
    1 -> "ONE"
    2 -> "TWO"
    3 -> "THREE"
    4 -> "FOUR"
    5 -> "FIVE"
    6 -> "SIX"
    7 -> "SEVEN"
    8 -> "EIGHT"
    9 -> "NINE"
    10 -> "TEN"
    11 -> "ELEVEN"
    else -> "TWELVE"
}
