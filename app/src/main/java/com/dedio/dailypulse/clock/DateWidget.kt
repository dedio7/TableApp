package com.dedio.dailypulse.clock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.Calendar

private val GIORNI_IT = listOf(
    "Domenica", "Lunedì", "Martedì", "Mercoledì",
    "Giovedì", "Venerdì", "Sabato"
)
private val MESI_IT = listOf(
    "Gennaio", "Febbraio", "Marzo", "Aprile", "Maggio", "Giugno",
    "Luglio", "Agosto", "Settembre", "Ottobre", "Novembre", "Dicembre"
)

/**
 * Displays the current date in a single compact line.
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
            delay(60_000L)
            tick.longValue = System.currentTimeMillis()
        }
    }

    val cal = Calendar.getInstance()
    val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1
    val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
    val month = cal.get(Calendar.MONTH)
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

    // More compact scales for Tablet
    val scale = if (isFullScreen) 1.4f else 1.0f

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = dayName.uppercase(),
            color = textColor.copy(alpha = 0.6f),
            fontSize = (12 * scale).sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (2 * scale).sp
        )
        
        Spacer(modifier = Modifier.width((12 * scale).dp))
        
        Text(
            text = "•",
            color = textColor.copy(alpha = 0.3f),
            fontSize = (14 * scale).sp
        )

        Spacer(modifier = Modifier.width((12 * scale).dp))

        Text(
            text = dateLine,
            color = textColor.copy(alpha = 0.9f),
            fontSize = (14 * scale).sp,
            fontWeight = FontWeight.Light,
            letterSpacing = (0.5 * scale).sp
        )
    }
}
