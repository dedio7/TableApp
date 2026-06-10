package com.dedio.dailypulse.clock

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.Locale

private val GIORNI_IT = listOf(
    "Domenica", "Lunedì", "Martedì", "Mercoledì",
    "Giovedì", "Venerdì", "Sabato"
)
private val MESI_IT = listOf(
    "Gennaio", "Febbraio", "Marzo", "Aprile", "Maggio", "Giugno",
    "Luglio", "Agosto", "Settembre", "Ottobre", "Novembre", "Dicembre"
)

/**
 * Displays the current date in a clean, elegant layout.
 * Shows day of week, day number and month/year.
 *
 * @param modifier   Layout modifier.
 * @param textColor  Primary text color.
 * @param dateFormat "IT" for Italian format, "EN" for English format.
 */
@Composable
fun DateWidget(
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    dateFormat: String = "IT",
    isFullScreen: Boolean = false,
) {
    val tick = remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000L) // refresh every minute
            tick.longValue = System.currentTimeMillis()
        }
    }

    val cal = Calendar.getInstance()
    val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1        // 0=Sun
    val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
    val month = cal.get(Calendar.MONTH)                       // 0=Jan
    val year = cal.get(Calendar.YEAR)

    val dayName: String
    val dateLine: String

    if (dateFormat == "EN") {
        val days = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
        val months = listOf("January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December")
        dayName = days[dayOfWeek]
        dateLine = "${months[month]} $dayOfMonth, $year"
    } else {
        dayName = GIORNI_IT[dayOfWeek]
        dateLine = "$dayOfMonth ${MESI_IT[month]} $year"
    }

    val baseSize = if (isFullScreen) 1.8f else 1f

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Day of week — small, spaced
        Text(
            text = dayName.uppercase(),
            color = textColor.copy(alpha = 0.55f),
            fontSize = (11 * baseSize).sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = (3 * baseSize).sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height((2 * baseSize).dp))
        // Date line — more prominent
        Text(
            text = dateLine,
            color = textColor.copy(alpha = 0.85f),
            fontSize = (15 * baseSize).sp,
            fontWeight = FontWeight.Light,
            letterSpacing = (0.5 * baseSize).sp,
            textAlign = TextAlign.Center
        )
    }
}
