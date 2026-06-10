package com.example.dailypulse.weather

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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.dailypulse.ui.i18n.LocalStrings
import com.example.dailypulse.ui.i18n.Strings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.minutes

/**
 * A compact, elegant weather widget that displays current weather conditions.
 * Auto-refreshes every 30 minutes. Shows loading shimmer and error state with retry.
 *
 * @param modifier Modifier for layout customization
 * @param textColor Primary text color for the widget
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
    val weatherData = remember { mutableStateOf<WeatherData?>(null) }
    val isLoading = remember { mutableStateOf(value = true) }
    val hasError = remember { mutableStateOf(value = false) }
    val refreshTrigger = remember { mutableLongStateOf(0L) }

    val coroutineScope = rememberCoroutineScope()

    // Re-fetch when coordinates change
    LaunchedEffect(latitude, longitude) {
        refreshTrigger.longValue = System.currentTimeMillis()
    }

    // Auto-refresh every 30 minutes
    LaunchedEffect(refreshTrigger.longValue) {
        isLoading.value = true
        hasError.value = false

        val result = repository.fetchWeather(latitude, longitude)
        if (result != null) {
            weatherData.value = result
            hasError.value = false
        } else {
            hasError.value = true
        }
        isLoading.value = false

        // Wait 30 minutes then refresh
        delay(30.minutes)
        refreshTrigger.longValue = System.currentTimeMillis()
    }

    val cardBackground = Color.Black.copy(alpha = 0.35f)
    val secondaryTextColor = textColor.copy(alpha = 0.7f)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(cardBackground)
            .padding(12.dp) // Reduced padding from 20.dp
    ) {
        when {
            isLoading.value -> {
                WeatherShimmer(textColor = textColor)
            }
            hasError.value -> {
                WeatherError(
                    textColor = textColor,
                    strings = strings,
                    onRetry = {
                        coroutineScope.launch {
                            refreshTrigger.longValue = System.currentTimeMillis()
                        }
                    }
                )
            }
            weatherData.value != null -> {
                val data = weatherData.value!!
                WeatherContent(
                    data = data,
                    cityName = cityName,
                    textColor = textColor,
                    secondaryTextColor = secondaryTextColor
                )
            }
        }
    }
}

/**
 * Main weather content layout showing all weather information.
 */
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
        // City name - small and elegant
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

        // Main info: Emoji and Temperature in a Row
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

        // Description
        Text(
            text = data.description,
            color = textColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Humidity and wind speed row - more compact
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

/**
 * Loading shimmer effect displayed while weather data is being fetched.
 */
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // City name placeholder
        Box(
            modifier = Modifier
                .width(50.dp)
                .height(12.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(shimmerBrush)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Emoji & Temp Row placeholder
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(shimmerBrush)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(32.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(shimmerBrush)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Description placeholder
        Box(
            modifier = Modifier
                .width(100.dp)
                .height(14.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(shimmerBrush)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Bottom row placeholder
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(11.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(shimmerBrush)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(11.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(shimmerBrush)
            )
        }
    }
}

/**
 * Error state with retry option.
 */
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
    ) {
        Text(
            text = "⚠️",
            fontSize = 36.sp,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = strings.weatherNotAvailable,
            color = textColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = strings.weatherCheckConnection,
            color = secondaryTextColor,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = strings.weatherRetry,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.15f))
                .clickable { onRetry() }
                .padding(horizontal = 24.dp, vertical = 10.dp),
        )
    }
}
