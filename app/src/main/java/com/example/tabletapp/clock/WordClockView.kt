package com.example.tabletapp.clock

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

/**
 * Italian word clock ("Orologio a Parole").
 *
 * A grid of Italian words where active words (lit in [textColor]) spell
 * the current time to the nearest 5-minute interval.
 *
 * Inactive words are shown dim. The bottom row shows remaining minutes 1-4
 * as single-letter dots for precision.
 */
@Composable
fun WordClock(
    modifier: Modifier = Modifier,
    textColor: Color = Color.White
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

    val h = hour.intValue
    val m = minute.intValue
    val dimColor = textColor.copy(alpha = 0.12f)
    val accentColor = Color(0xFFE8722A)

    // Calculate display time (round down to nearest 5 min)
    val m5 = (m / 5) * 5
    val remainder = m % 5
    // For "MENO" phrases, we advance the hour
    val displayHour = if (m5 >= 35) (h + 1) % 24 else h
    val h12 = displayHour % 12

    // Words that should be LIT
    val lit = mutableSetOf<String>()

    // "È L'UNA" for 1:00, "SONO LE X" for others
    if (h12 == 1) {
        lit += "È"; lit += "L'UNA"
    } else {
        lit += "SONO"; lit += "LE"
        lit += hourWord(h12)
    }

    // Minute phrase
    when (m5) {
        0 -> { /* nothing extra */ }
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

    // Word grid — each entry: (displayText, wordId)
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        val annotated = buildAnnotatedString {
            for ((rowIdx, row) in grid.withIndex()) {
                for ((wordIdx, pair) in row.withIndex()) {
                    val (display, id) = pair
                    val isLit = id in lit
                    withStyle(
                        SpanStyle(
                            color = if (isLit) textColor else dimColor,
                            fontWeight = if (isLit) FontWeight.Bold else FontWeight.Light,
                            fontSize = 28.sp,
                            letterSpacing = 2.sp
                        )
                    ) {
                        append(display)
                    }
                    if (wordIdx < row.lastIndex) append("  ")
                }
                if (rowIdx < grid.lastIndex) append("\n")
            }

            // Minute dots (remaining 1-4 min)
            if (remainder > 0) {
                append("\n\n")
                for (i in 1..4) {
                    withStyle(SpanStyle(
                        color = if (i <= remainder) accentColor else dimColor,
                        fontSize = 14.sp
                    )) {
                        append("●")
                    }
                    if (i < 4) append(" ")
                }
            }
        }

        Text(
            text = annotated,
            textAlign = TextAlign.Center,
            lineHeight = 40.sp
        )
    }
}

private fun hourWord(h12: Int): String = when (h12) {
    0 -> "DODICI"
    1 -> "L'UNA"
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
