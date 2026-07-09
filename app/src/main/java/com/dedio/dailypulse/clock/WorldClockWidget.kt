package com.dedio.dailypulse.clock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import kotlin.time.Duration.Companion.seconds

@Composable
fun WorldClockWidget(
    timeZones: Set<String>,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = System.currentTimeMillis()
            delay(1.seconds)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black.copy(alpha = 0.2f))
            .padding(12.dp)
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(timeZones.toList()) { tzId ->
                WorldClockItem(tzId, currentTime, textColor)
            }
        }
    }
}

@Composable
private fun WorldClockItem(tzId: String, currentTime: Long, textColor: Color) {
    val timeZone = remember(tzId) { TimeZone.getTimeZone(tzId) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val dayFormat = remember { SimpleDateFormat("EEE", Locale.getDefault()) }
    
    timeFormat.timeZone = timeZone
    dayFormat.timeZone = timeZone

    val date = remember(currentTime) { Date(currentTime) }
    val timeStr = timeFormat.format(date)
    val dayStr = dayFormat.format(date)
    
    // Extract city name from ID (e.g., "Europe/London" -> "London")
    val cityName = remember(tzId) {
        tzId.substringAfterLast('/').replace('_', ' ')
    }
    
    // Extract region/continent (e.g., "Europe/London" -> "EUROPE")
    val regionName = remember(tzId) {
        tzId.substringBefore('/', "WORLD").uppercase()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .widthIn(min = 80.dp)
    ) {
        Text(
            text = regionName,
            color = textColor.copy(alpha = 0.4f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
        Text(
            text = cityName,
            color = textColor.copy(alpha = 0.9f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
        Text(
            text = timeStr,
            color = textColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.Light
        )
        Text(
            text = dayStr.uppercase(),
            color = textColor.copy(alpha = 0.5f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
