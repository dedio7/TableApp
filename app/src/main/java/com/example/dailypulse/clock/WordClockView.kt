package com.example.dailypulse.clock

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.Calendar

@Composable
fun WordClock(
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    language: String = "IT"
) {
    val hour = remember { mutableIntStateOf(0) }
    val minute = remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            val cal = Calendar.getInstance()
            hour.intValue = cal.get(Calendar.HOUR_OF_DAY)
            minute.intValue = cal.get(Calendar.MINUTE)
            delay(5_000L)
        }
    }

    if (language == "EN") {
        EnglishWordClock(modifier, textColor, hour.intValue, minute.intValue)
    } else {
        ItalianWordClock(modifier, textColor, hour.intValue, minute.intValue)
    }
}

@Composable
private fun ItalianWordClock(modifier: Modifier, textColor: Color, h: Int, m: Int) {
    val dimColor = textColor.copy(alpha = 0.12f)
    val accentColor = Color(0xFFE8722A)

    val m5 = (m / 5) * 5
    val remainder = m % 5
    val displayHour = if (m5 >= 35) (h + 1) % 24 else h
    val h12 = displayHour % 12

    val lit = mutableSetOf<String>()
    if (h12 == 1) {
        lit += "È"; lit += "L'UNA"
    } else {
        lit += "SONO"; lit += "LE"
        lit += italianHourWord(h12)
    }

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

    WordClockGrid(modifier, textColor, dimColor, accentColor, grid, lit, remainder)
}

@Composable
private fun EnglishWordClock(modifier: Modifier, textColor: Color, h: Int, m: Int) {
    val dimColor = textColor.copy(alpha = 0.12f)
    val accentColor = Color(0xFFE8722A)

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

    WordClockGrid(modifier, textColor, dimColor, accentColor, grid, lit, remainder)
}

@Composable
private fun WordClockGrid(
    modifier: Modifier,
    textColor: Color,
    dimColor: Color,
    accentColor: Color,
    grid: List<List<Pair<String, String>>>,
    lit: Set<String>,
    remainder: Int
) {
    Box(
        modifier = modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        val annotated = buildAnnotatedString {
            for ((rowIdx, row) in grid.withIndex()) {
                for ((wordIdx, pair) in row.withIndex()) {
                    val (display, id) = pair
                    val isLit = id in lit
                    withStyle(SpanStyle(color = if (isLit) textColor else dimColor, fontWeight = if (isLit) FontWeight.Bold else FontWeight.Light, fontSize = 22.sp, letterSpacing = 1.5.sp)) {
                        append(display)
                    }
                    if (wordIdx < row.lastIndex) append("  ")
                }
                if (rowIdx < grid.lastIndex) append("\n")
            }
            if (remainder > 0) {
                append("\n")
                for (i in 1..4) {
                    withStyle(SpanStyle(color = if (i <= remainder) accentColor else dimColor, fontSize = 12.sp)) { append("●") }
                    if (i < 4) append(" ")
                }
            }
        }
        Text(text = annotated, textAlign = TextAlign.Center, lineHeight = 32.sp)
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
