package com.example.tabletapp.ui.main

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.example.tabletapp.background.AmbientBackground
import com.example.tabletapp.background.BackgroundConfig
import com.example.tabletapp.battery.BatteryWidget
import com.example.tabletapp.clock.ClockDisplay
import com.example.tabletapp.clock.ClockType
import com.example.tabletapp.clock.DateWidget
import com.example.tabletapp.news.NewsTicker
import com.example.tabletapp.settings.AppSettings
import com.example.tabletapp.settings.SettingsPanel
import com.example.tabletapp.weather.WeatherWidget

@Composable
fun MainScreen(
    onItemClick: (NavKey) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val appSettings = remember { AppSettings(context) }

    // ── Background settings ───────────────────────────────────────────────────
    val bgPrimary by appSettings.bgPrimaryColor.collectAsStateWithLifecycle(initialValue = 0xFF0D1B2A)
    val bgSecondary by appSettings.bgSecondaryColor.collectAsStateWithLifecycle(initialValue = 0xFF151528)
    val bgUseGradient by appSettings.bgUseGradient.collectAsStateWithLifecycle(initialValue = false)

    // ── Clock settings ────────────────────────────────────────────────────────
    val clockTypeName by appSettings.clockType.collectAsStateWithLifecycle(initialValue = "FLIP")
    val clockColorLong by appSettings.clockColor.collectAsStateWithLifecycle(initialValue = 0xFFEEEEEEL)
    val showSeconds by appSettings.showSeconds.collectAsStateWithLifecycle(initialValue = true)
    val binaryModeName by appSettings.binaryClockMode.collectAsStateWithLifecycle(initialValue = "BINARY")
    val binaryThemeName by appSettings.binaryClockTheme.collectAsStateWithLifecycle(initialValue = "DEFAULT")

    // ── News settings ─────────────────────────────────────────────────────────
    val newsEnabled by appSettings.newsEnabled.collectAsStateWithLifecycle(initialValue = true)
    val newsRefreshMinutes by appSettings.newsRefreshMinutes.collectAsStateWithLifecycle(initialValue = 30)

    // ── Weather settings ──────────────────────────────────────────────────────
    val weatherLat by appSettings.weatherLatitude.collectAsStateWithLifecycle(initialValue = 41.9028)
    val weatherLon by appSettings.weatherLongitude.collectAsStateWithLifecycle(initialValue = 12.4964)
    val weatherCity by appSettings.weatherCity.collectAsStateWithLifecycle(initialValue = "Roma")

    // ── Date format ───────────────────────────────────────────────────────────
    val dateFormat by appSettings.dateFormat.collectAsStateWithLifecycle(initialValue = "IT")

    val config = remember(bgPrimary, bgSecondary, bgUseGradient) {
        BackgroundConfig(
            primaryColor = Color(bgPrimary),
            secondaryColor = Color(bgSecondary),
            useGradient = bgUseGradient
        )
    }

    val clockType = remember(clockTypeName) {
        try { ClockType.valueOf(clockTypeName) } catch (_: Exception) { ClockType.FLIP }
    }
    val clockColor = Color(clockColorLong)

    // Settings panel & news refresh state
    var settingsOpen by remember { mutableStateOf(false) }
    var newsRefreshTrigger by remember { mutableIntStateOf(0) }
    val gearRotation by animateFloatAsState(
        targetValue = if (settingsOpen) 90f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "gearRotation"
    )
    val textMeasurer = rememberTextMeasurer()

    AmbientBackground(config = config, modifier = Modifier.fillMaxSize()) {
        Box(modifier = modifier.fillMaxSize()) {

            // 1. Battery widget — top-right (shifted left to clear gear button)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 52.dp)
            ) {
                BatteryWidget()
            }

            // 2. Settings gear button — top-right corner
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 8.dp)
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                    .clickable { settingsOpen = !settingsOpen }
                    .rotate(gearRotation),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(28.dp)) {
                    val style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Normal)
                    val measured = textMeasurer.measure(AnnotatedString("⚙"), style)
                    drawText(
                        textLayoutResult = measured,
                        topLeft = Offset(
                            size.width / 2f - measured.size.width / 2f,
                            size.height / 2f - measured.size.height / 2f
                        )
                    )
                }
            }

            // 3. Main content — Clock column (left) + Weather column (right)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(
                        top = 44.dp,
                        bottom = if (newsEnabled) 60.dp else 16.dp,
                        start = 16.dp,
                        end = 16.dp
                    ),
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ── Left: Clock + Date ────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Date widget above the clock
                    DateWidget(
                        modifier = Modifier.fillMaxWidth(),
                        textColor = clockColor,
                        dateFormat = dateFormat
                    )

                    // Clock — takes remaining vertical space
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        ClockDisplay(
                            clockType = clockType,
                            modifier = Modifier.fillMaxSize(),
                            textColor = clockColor,
                            showSeconds = showSeconds,
                            binaryMode = binaryModeName,
                            binaryTheme = binaryThemeName
                        )
                    }
                }

                // ── Right: Weather widget ─────────────────────────────────────
                Box(
                    modifier = Modifier
                        .weight(0.8f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    WeatherWidget(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        latitude = weatherLat,
                        longitude = weatherLon,
                        cityName = weatherCity
                    )
                }
            }

            // 4. News Ticker — pinned to bottom
            if (newsEnabled) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                ) {
                    NewsTicker(
                        modifier = Modifier.fillMaxWidth(),
                        refreshTrigger = newsRefreshTrigger,
                        refreshIntervalMs = newsRefreshMinutes * 60_000L
                    )
                }
            }
        }

        // 5. Settings overlay (on top of everything)
        SettingsPanel(
            visible = settingsOpen,
            onDismiss = { settingsOpen = false },
            onNewsRefresh = { newsRefreshTrigger++ }
        )
    }
}
