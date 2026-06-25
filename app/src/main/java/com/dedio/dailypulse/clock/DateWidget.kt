package com.dedio.dailypulse.clock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
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
import com.dedio.dailypulse.ui.i18n.LocalStrings
import kotlinx.coroutines.delay
import java.util.Calendar
import kotlin.time.Duration.Companion.minutes

private val GIORNI_IT = listOf("Domenica", "Lunedì", "Martedì", "Mercoledì", "Giovedì", "Venerdì", "Sabato")
private val MESI_IT = listOf("Gennaio", "Febbraio", "Marzo", "Aprile", "Maggio", "Giugno", "Luglio", "Agosto", "Settembre", "Ottobre", "Novembre", "Dicembre")

private val DAYS_EN = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
private val MONTHS_EN = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")

@Composable
fun DateWidget(
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    dateFormat: String = "IT",
    isFullScreen: Boolean = false,
) {
    val tick = remember { mutableLongStateOf(System.currentTimeMillis()) }
    val strings = LocalStrings.current
    val isLanguageEn = strings.settingsTitle == "Settings"

    LaunchedEffect(Unit) {
        while (true) {
            delay(1.minutes)
            tick.longValue = System.currentTimeMillis()
        }
    }

    val cal = Calendar.getInstance()
    val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1
    val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
    val month = cal.get(Calendar.MONTH)

    val dateLine = if (dateFormat == "EN") {
        val m = if (isLanguageEn) MONTHS_EN[month] else MESI_IT[month]
        "$m $dayOfMonth"
    } else {
        val m = if (isLanguageEn) MONTHS_EN[month] else MESI_IT[month]
        "$dayOfMonth $m"
    }
    
    val dayName = if (isLanguageEn) DAYS_EN[dayOfWeek] else GIORNI_IT[dayOfWeek]

    val scale = if (isFullScreen) 1.1f else 0.8f

    // Optimized layout: more compact and elegant without the year
    Row(
        modifier = modifier.wrapContentWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = dayName.uppercase(),
            color = textColor.copy(alpha = 0.5f),
            fontSize = (11 * scale).sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (1.5 * scale).sp
        )
        Spacer(modifier = Modifier.width((6 * scale).dp))
        Text(text = "•", color = textColor.copy(alpha = 0.2f), fontSize = (11 * scale).sp)
        Spacer(modifier = Modifier.width((6 * scale).dp))
        Text(
            text = dateLine.uppercase(),
            color = textColor.copy(alpha = 0.8f),
            fontSize = (13 * scale).sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = (1 * scale).sp
        )
    }
}
