package com.dedio.dailypulse.weather

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dedio.dailypulse.ui.i18n.LocalStrings
import com.dedio.dailypulse.ui.i18n.Strings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.minutes

/**
 * A compact, elegant weather widget that displays current weather conditions.
 * Auto-refreshes every 30 minutes. Shows loading shimmer and error state with retry.
 */
@Composable
fun WeatherWidget(
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    latitude: Double = 41.9028,
    longitude: Double = 12.4964,
    cityName: String = "Roma",
) {
    val repository = remember { WeatherRepository() }
    val strings = LocalStrings.current
    var weatherData by remember { mutableStateOf<WeatherData?>(null) }
    var isLoading by remember { mutableStateOf(value = true) }
    var hasError by remember { mutableStateOf(value = false) }
    var refreshTrigger by remember { mutableLongStateOf(0L) }
    var detailsOpen by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Re-fetch when coordinates change
    LaunchedEffect(latitude, longitude) {
        refreshTrigger = System.currentTimeMillis()
    }

    // Auto-refresh logic
    LaunchedEffect(refreshTrigger) {
        isLoading = true
        hasError = false

        val result = repository.fetchWeather(latitude, longitude)
        if (result != null) {
            weatherData = result
            hasError = false
        } else {
            hasError = true
        }
        isLoading = false

        delay(30.minutes)
        refreshTrigger = System.currentTimeMillis()
    }

    val cardBackground = Color.Black.copy(alpha = 0.35f)
    val secondaryTextColor = textColor.copy(alpha = 0.7f)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(cardBackground)
            .clickable(enabled = weatherData != null) { detailsOpen = true }
            .padding(12.dp)
    ) {
        when {
            isLoading -> WeatherShimmer(textColor = textColor)
            hasError -> {
                WeatherError(
                    textColor = textColor,
                    strings = strings,
                    onRetry = {
                        coroutineScope.launch {
                            refreshTrigger = System.currentTimeMillis()
                        }
                    }
                )
            }
            weatherData != null -> {
                WeatherContent(
                    data = weatherData!!,
                    cityName = cityName,
                    textColor = textColor,
                    secondaryTextColor = secondaryTextColor
                )
            }
        }
    }

    // Expansion Panel
    weatherData?.let { data ->
        WeatherDetailsPanel(
            visible = detailsOpen,
            data = data,
            cityName = cityName,
            onDismiss = { detailsOpen = false }
        )
    }
}

@Composable
private fun WeatherContent(
    data: WeatherData,
    cityName: String,
    textColor: Color,
    secondaryTextColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = cityName.uppercase(),
            color = secondaryTextColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = data.iconEmoji,
                fontSize = 32.sp,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "${data.temperature.roundToInt()}°",
                color = textColor,
                fontSize = 38.sp,
                fontWeight = FontWeight.Light
            )
        }

        Text(
            text = data.description,
            color = textColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("💧", fontSize = 12.sp)
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "${data.humidity}%",
                    color = secondaryTextColor,
                    fontSize = 11.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("💨", fontSize = 12.sp)
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "${data.windSpeed.roundToInt()} km/h",
                    color = secondaryTextColor,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun WeatherShimmer(textColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerProgress = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerProgress"
    )

    val shimmerColor = textColor.copy(alpha = 0.1f)
    val shimmerHighlight = textColor.copy(alpha = 0.25f)

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(shimmerColor, shimmerHighlight, shimmerColor),
        start = Offset((shimmerProgress.value * 300f) - 100f, 0f),
        end = Offset((shimmerProgress.value * 300f) + 100f, 0f)
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Box(modifier = Modifier.width(50.dp).height(12.dp).clip(RoundedCornerShape(4.dp)).background(shimmerBrush))
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(shimmerBrush))
            Spacer(modifier = Modifier.width(12.dp))
            Box(modifier = Modifier.width(40.dp).height(32.dp).clip(RoundedCornerShape(4.dp)).background(shimmerBrush))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(modifier = Modifier.width(100.dp).height(14.dp).clip(RoundedCornerShape(4.dp)).background(shimmerBrush))
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.width(40.dp).height(11.dp).clip(RoundedCornerShape(4.dp)).background(shimmerBrush))
            Spacer(modifier = Modifier.width(16.dp))
            Box(modifier = Modifier.width(60.dp).height(11.dp).clip(RoundedCornerShape(4.dp)).background(shimmerBrush))
        }
    }
}

@Composable
private fun WeatherError(
    textColor: Color,
    strings: Strings,
    onRetry: () -> Unit,
) {
    val secondaryTextColor = textColor.copy(alpha = 0.7f)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
    ) {
        Text(text = "⚠️", fontSize = 36.sp, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = strings.weatherNotAvailable, color = textColor, fontSize = 15.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = strings.weatherCheckConnection, color = secondaryTextColor, fontSize = 13.sp, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = strings.weatherRetry,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.15f)).clickable { onRetry() }.padding(horizontal = 24.dp, vertical = 10.dp),
        )
    }
}
