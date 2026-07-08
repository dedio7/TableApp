package com.dedio.dailypulse.weather

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.dedio.dailypulse.ui.i18n.LocalStrings
import kotlin.math.roundToInt

@Composable
fun WeatherDetailsPanel(
    visible: Boolean,
    data: WeatherData,
    cityName: String,
    onDismiss: () -> Unit
) {
    val strings = LocalStrings.current

    if (!visible) return

    Popup(
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            val isSmallHeight = this.maxHeight < 500.dp
            val panelPadding = if (isSmallHeight) 16.dp else 24.dp
            val sectionSpacing = if (isSmallHeight) 12.dp else 24.dp

            Column(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.9f)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFF1A1A2E))
                    .clickable(enabled = false) { } // Prevent clicks through to background
                    .padding(panelPadding)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = cityName,
                            color = Color.White,
                            fontSize = if (isSmallHeight) 20.sp else 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = data.description,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = if (isSmallHeight) 13.sp else 15.sp
                        )
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = data.iconEmoji,
                            fontSize = if (isSmallHeight) 32.sp else 40.sp,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Text(
                            text = "${data.temperature.roundToInt()}°",
                            color = Color.White,
                            fontSize = if (isSmallHeight) 36.sp else 48.sp,
                            fontWeight = FontWeight.Light
                        )
                    }
                }

                Spacer(modifier = Modifier.height(sectionSpacing))

                // Additional Details (Humidity, Wind, Feels Like)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DetailItem(
                        icon = "🌡️",
                        value = "${data.feelsLike.roundToInt()}°",
                        label = strings.weatherFeelsLike,
                        isSmall = isSmallHeight
                    )
                    DetailItem(
                        icon = "💧",
                        value = "${data.humidity}%",
                        label = strings.weatherHumidity,
                        isSmall = isSmallHeight
                    )
                    DetailItem(
                        icon = "💨",
                        value = "${data.windSpeed.roundToInt()} km/h",
                        label = strings.weatherWind,
                        isSmall = isSmallHeight
                    )
                }

                Spacer(modifier = Modifier.height(sectionSpacing))

                // Hourly Forecast
                Text(
                    text = strings.weatherHourly.uppercase(),
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(if (isSmallHeight) 12.dp else 16.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(data.hourly) { hour ->
                        HourlyItem(hour, isSmallHeight)
                    }
                }

                Spacer(modifier = Modifier.height(sectionSpacing))
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(sectionSpacing))

                // Daily Forecast
                Text(
                    text = strings.weatherDaily.uppercase(),
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(if (isSmallHeight) 6.dp else 8.dp)
                ) {
                    data.daily.forEach { day ->
                        DailyItem(day, isSmallHeight)
                    }
                }

                Spacer(modifier = Modifier.height(if (isSmallHeight) 8.dp else 16.dp))

                // Close Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .clickable { onDismiss() }
                        .padding(vertical = if (isSmallHeight) 10.dp else 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = strings.close,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = if (isSmallHeight) 13.sp else 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailItem(icon: String, value: String, label: String, isSmall: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .padding(vertical = if (isSmall) 8.dp else 12.dp)
            .width(if (isSmall) 82.dp else 100.dp)
    ) {
        Text(text = icon, fontSize = if (isSmall) 16.sp else 20.sp)
        Text(
            text = value,
            color = Color.White,
            fontSize = if (isSmall) 13.sp else 15.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.5f),
            fontSize = if (isSmall) 10.sp else 11.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun HourlyItem(hour: HourlyForecast, isSmall: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .padding(vertical = if (isSmall) 8.dp else 12.dp, horizontal = if (isSmall) 10.dp else 12.dp)
    ) {
        Text(
            text = hour.time,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = if (isSmall) 11.sp else 12.sp
        )
        Text(
            text = hour.iconEmoji,
            fontSize = if (isSmall) 20.sp else 24.sp,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        Text(
            text = "${hour.temperature.roundToInt()}°",
            color = Color.White,
            fontSize = if (isSmall) 14.sp else 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun DailyItem(day: DailyForecast, isSmall: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.03f))
            .padding(horizontal = 16.dp, vertical = if (isSmall) 8.dp else 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = day.date,
            color = Color.White,
            fontSize = if (isSmall) 13.sp else 15.sp,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = day.iconEmoji,
            fontSize = if (isSmall) 18.sp else 20.sp,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )

        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "${day.maxTemp.roundToInt()}°",
                color = Color.White,
                fontSize = if (isSmall) 13.sp else 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "${day.minTemp.roundToInt()}°",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = if (isSmall) 13.sp else 15.sp
            )
        }
    }
}
