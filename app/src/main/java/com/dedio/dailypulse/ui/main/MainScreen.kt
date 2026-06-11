package com.dedio.dailypulse.ui.main

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.view.WindowManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dedio.dailypulse.background.AmbientBackground
import com.dedio.dailypulse.background.BackgroundConfig
import com.dedio.dailypulse.battery.BatteryWidget
import com.dedio.dailypulse.calendar.CalendarWidget
import com.dedio.dailypulse.clock.ClockDisplay
import com.dedio.dailypulse.clock.ClockType
import com.dedio.dailypulse.clock.DateWidget
import com.dedio.dailypulse.media.MediaWidget
import com.dedio.dailypulse.news.NewsTicker
import com.dedio.dailypulse.settings.AppSettings
import com.dedio.dailypulse.settings.SettingsPanel
import com.dedio.dailypulse.ui.i18n.ProvideLocalization
import com.dedio.dailypulse.weather.WeatherWidget
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.minutes

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val appSettings = remember { AppSettings(context) }
    val scope = rememberCoroutineScope()

    // --- Screen On Logic ---
    DisposableEffect(context) {
        val activity = context as? Activity
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
                val plugged = intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
                
                val isCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING) ||
                        (status == BatteryManager.BATTERY_STATUS_FULL) ||
                        (plugged == BatteryManager.BATTERY_PLUGGED_AC) ||
                        (plugged == BatteryManager.BATTERY_PLUGGED_USB) ||
                        (plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS)

                activity?.window?.let { window ->
                    if (isCharging) {
                        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    } else {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    }
                }
            }
        }
        
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(receiver, filter)
        
        onDispose {
            context.unregisterReceiver(receiver)
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // ── Background settings ───────────────────────────────────────────────────
    val bgPrimary by appSettings.bgPrimaryColor.collectAsStateWithLifecycle(initialValue = 0xFF0D0D0DL)
    val bgSecondary by appSettings.bgSecondaryColor.collectAsStateWithLifecycle(initialValue = 0xFF1A1A2EL)
    val bgUseGradient by appSettings.bgUseGradient.collectAsStateWithLifecycle(initialValue = false)

    // ── Clock settings ────────────────────────────────────────────────────────
    val clockTypeName by appSettings.clockType.collectAsStateWithLifecycle(initialValue = "FLIP")
    val clockColorLong by appSettings.clockColor.collectAsStateWithLifecycle(initialValue = 0xFFEEEEEEE)
    val showSeconds by appSettings.showSeconds.collectAsStateWithLifecycle(initialValue = true)
    val binaryModeName by appSettings.binaryClockMode.collectAsStateWithLifecycle(initialValue = "BINARY")
    val binaryThemeName by appSettings.binaryClockTheme.collectAsStateWithLifecycle(initialValue = "DEFAULT")

    // ── News settings ─────────────────────────────────────────────────────────
    val newsEnabled by appSettings.newsEnabled.collectAsStateWithLifecycle(initialValue = true)
    val newsRefreshMinutes by appSettings.newsRefreshMinutes.collectAsStateWithLifecycle(initialValue = 30)
    val newsSources by appSettings.newsSources.collectAsStateWithLifecycle(initialValue = emptySet())

    // ── Weather settings ──────────────────────────────────────────────────────
    val weatherEnabled by appSettings.weatherEnabled.collectAsStateWithLifecycle(initialValue = true)
    val weatherLat by appSettings.weatherLatitude.collectAsStateWithLifecycle(initialValue = 41.9028)
    val weatherLon by appSettings.weatherLongitude.collectAsStateWithLifecycle(initialValue = 12.4964)
    val weatherCity by appSettings.weatherCity.collectAsStateWithLifecycle(initialValue = "Roma")

    // ── Widget visibility ─────────────────────────────────────────────────────
    val batteryEnabled by appSettings.batteryEnabled.collectAsStateWithLifecycle(initialValue = true)
    val mediaEnabled by appSettings.mediaEnabled.collectAsStateWithLifecycle(initialValue = true)
    val calendarEnabled by appSettings.calendarEnabled.collectAsStateWithLifecycle(initialValue = true)

    val anyWidgetEnabled = weatherEnabled || calendarEnabled || mediaEnabled

    // ── Date format ───────────────────────────────────────────────────────────
    val dateFormat by appSettings.dateFormat.collectAsStateWithLifecycle(initialValue = "IT")
    val appLanguage by appSettings.appLanguage.collectAsStateWithLifecycle(initialValue = "IT")
    val nightShiftEnabled by appSettings.nightShiftEnabled.collectAsStateWithLifecycle(initialValue = false)
    val antiBurnInEnabled by appSettings.antiBurnInEnabled.collectAsStateWithLifecycle(initialValue = true)

    val config = remember(bgPrimary, bgSecondary, bgUseGradient) {
        BackgroundConfig(
            primaryColor = Color(bgPrimary.toInt()),
            secondaryColor = Color(bgSecondary.toInt()),
            useGradient = bgUseGradient,
        )
    }

    val clockType = remember(clockTypeName) {
        try { ClockType.valueOf(clockTypeName) } catch (_: Exception) { ClockType.FLIP }
    }
    val clockColor = Color(clockColorLong.toInt())

    // Settings panel & news refresh state
    var settingsOpen by remember { mutableStateOf(value = false) }
    var newsRefreshTrigger by remember { mutableIntStateOf(0) }
    val gearRotation by animateFloatAsState(
        targetValue = if (settingsOpen) 90f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "gearRotation",
    )
    
    // --- Burn-in Protection State ---
    var burnInOffset by remember { mutableStateOf(Offset.Zero) }
    LaunchedEffect(antiBurnInEnabled) {
        if (antiBurnInEnabled) {
            while (true) {
                delay(1.minutes) // Shift every minute
                burnInOffset = Offset(
                    x = (-3..3).random().toFloat(),
                    y = (-3..3).random().toFloat()
                )
            }
        } else {
            burnInOffset = Offset.Zero
        }
    }

    // --- Night Shift State ---
    val isNightTime = remember {
        val calendar = java.util.Calendar.getInstance()
        val hour = calendar[java.util.Calendar.HOUR_OF_DAY]
        hour in 22..23 || hour in 0..6
    }
    val applyNightShift = nightShiftEnabled && isNightTime

    val textMeasurer = rememberTextMeasurer()

    ProvideLocalization(appLanguage) {
        AmbientBackground(config = config, modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .then(
                        if (antiBurnInEnabled) {
                            Modifier.layout { measurable, constraints ->
                                val placeable = measurable.measure(constraints)
                                layout(placeable.width, placeable.height) {
                                    placeable.placeRelative(
                                        burnInOffset.x.toInt(),
                                        burnInOffset.y.toInt()
                                    )
                                }
                            }
                        } else Modifier
                    )
            ) {
                // 1. Battery widget — Top-left (Moved from top-right to prevent overlap)
                if (batteryEnabled) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(top = 10.dp, start = 12.dp)
                    ) {
                        BatteryWidget()
                    }
                }

                // 2. Settings gear button — top-right corner
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 8.dp, end = 8.dp)
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                        .border(1.2.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                        .clickable { settingsOpen = !settingsOpen }
                        .rotate(gearRotation),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(28.dp)) {
                        val style = TextStyle(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White // Colore bianco pieno per risalto
                        )
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
                            top = 64.dp,
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
                            .weight(if (anyWidgetEnabled) 2f else 1f)
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top,
                    ) {
                        // Date widget above the clock - centered and clear of icons
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 70.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            DateWidget(
                                modifier = Modifier.wrapContentWidth(),
                                textColor = clockColor,
                                dateFormat = dateFormat,
                                isFullScreen = !anyWidgetEnabled,
                            )
                        }

                        // Clock — takes remaining vertical space
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(bottom = if (!anyWidgetEnabled) 16.dp else 0.dp)
                                .pointerInput(Unit) {
                                    var totalDrag = 0f
                                    detectHorizontalDragGestures(
                                        onDragEnd = {
                                            if (kotlin.math.abs(totalDrag) > 60f) {
                                                val allTypes = ClockType.entries
                                                // Find current index based on the TYPE NAME to avoid reference issues
                                                val currentIndex = allTypes.indexOfFirst { it.name == clockTypeName }
                                                if (currentIndex != -1) {
                                                    val nextIndex = if (totalDrag < 0) {
                                                        (currentIndex + 1) % allTypes.size
                                                    } else {
                                                        (currentIndex - 1 + allTypes.size) % allTypes.size
                                                    }
                                                    scope.launch {
                                                        appSettings.setClockType(allTypes[nextIndex].name)
                                                    }
                                                }
                                            }
                                            totalDrag = 0f
                                        },
                                        onHorizontalDrag = { change, dragAmount ->
                                            change.consume()
                                            totalDrag += dragAmount
                                        }
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            ClockDisplay(
                                clockType = clockType,
                                modifier = Modifier.fillMaxSize(),
                                textColor = clockColor,
                                showSeconds = showSeconds,
                                binaryMode = binaryModeName,
                                binaryTheme = binaryThemeName,
                                language = appLanguage,
                                isFullScreen = !anyWidgetEnabled,
                            )
                        }
                    }

                    // ── Right: Weather + Calendar + Media ─────────────────────────
                    if (anyWidgetEnabled) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState())
                                .padding(vertical = 4.dp), // Add vertical breathing room
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(
                                12.dp, // Reduced spacing for better fit
                                Alignment.Top // Align to top for better visibility in scroll
                            )
                        ) {
                            if (weatherEnabled) {
                                WeatherWidget(
                                    modifier = Modifier.fillMaxWidth(),
                                    latitude = weatherLat,
                                    longitude = weatherLon,
                                    cityName = weatherCity
                                )
                            }

                            if (calendarEnabled) {
                                CalendarWidget(modifier = Modifier.fillMaxWidth())
                            }

                            if (mediaEnabled) {
                                MediaWidget(modifier = Modifier.fillMaxWidth())
                            }
                        }
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
                            refreshIntervalMs = newsRefreshMinutes * 60_000L,
                            enabledSources = newsSources,
                            language = appLanguage
                        )
                    }
                }
            }

            // 5. Settings overlay (on top of everything)
            SettingsPanel(
                visible = settingsOpen,
                onDismiss = { settingsOpen = false }
            ) {
                newsRefreshTrigger++
            }

            // 6. Night Shift Overlay
            if (applyNightShift) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFE8722A).copy(alpha = 0.15f)) // Warm amber overlay
                        .background(Color.Black.copy(alpha = 0.10f)) // Additional dimming
                )
            }
        }
    }
}
