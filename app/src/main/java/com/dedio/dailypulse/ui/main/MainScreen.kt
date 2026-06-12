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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dedio.dailypulse.background.AmbientBackground
import com.dedio.dailypulse.background.BackgroundConfig
import com.dedio.dailypulse.battery.BatteryWidget
import com.dedio.dailypulse.calendar.CalendarWidget
import com.dedio.dailypulse.clock.ClockDisplay
import com.dedio.dailypulse.clock.ClockType
import com.dedio.dailypulse.clock.DateWidget
import com.dedio.dailypulse.inspiration.InspirationWidget
import com.dedio.dailypulse.media.MediaWidget
import com.dedio.dailypulse.news.NewsTicker
import com.dedio.dailypulse.settings.AppSettings
import com.dedio.dailypulse.settings.SettingsPanel
import com.dedio.dailypulse.sunrise.SunriseManager
import com.dedio.dailypulse.ui.i18n.ProvideLocalization
import com.dedio.dailypulse.weather.WeatherWidget

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val appSettings = remember { AppSettings(context) }
    val viewModel: MainScreenViewModel = viewModel { MainScreenViewModel(appSettings) }

    // --- Screen On Logic ---
    LaunchedEffect(Unit) {
        val activity = context as? Activity
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        
        // Initial check
        val statusIntent = context.registerReceiver(null, intentFilter)
        statusIntent?.let { intent ->
            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
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
            // We don't necessarily want to clear it on dispose if the app is still visible, 
            // but for a Service (DreamService) it might be different.
        }
    }

    // ── Settings ──────────────────────────────────────────────────────────
    val bgPrimary by viewModel.bgPrimary.collectAsStateWithLifecycle()
    val bgSecondary by viewModel.bgSecondary.collectAsStateWithLifecycle()
    val bgUseGradient by viewModel.bgUseGradient.collectAsStateWithLifecycle()

    val clockTypeName by viewModel.clockTypeName.collectAsStateWithLifecycle()
    val clockColorLong by viewModel.clockColorLong.collectAsStateWithLifecycle()
    val showSeconds by viewModel.showSeconds.collectAsStateWithLifecycle()
    val binaryModeName by viewModel.binaryModeName.collectAsStateWithLifecycle()
    val binaryThemeName by viewModel.binaryThemeName.collectAsStateWithLifecycle()

    val newsEnabled by viewModel.newsEnabled.collectAsStateWithLifecycle()
    val newsRefreshMinutes by viewModel.newsRefreshMinutes.collectAsStateWithLifecycle()
    val newsSources by viewModel.newsSources.collectAsStateWithLifecycle()

    val weatherEnabled by viewModel.weatherEnabled.collectAsStateWithLifecycle()
    val weatherLat by viewModel.weatherLat.collectAsStateWithLifecycle()
    val weatherLon by viewModel.weatherLon.collectAsStateWithLifecycle()
    val weatherCity by viewModel.weatherCity.collectAsStateWithLifecycle()

    val batteryEnabled by viewModel.batteryEnabled.collectAsStateWithLifecycle()
    val mediaEnabled by viewModel.mediaEnabled.collectAsStateWithLifecycle()
    val calendarEnabled by viewModel.calendarEnabled.collectAsStateWithLifecycle()

    val dateFormat by viewModel.dateFormat.collectAsStateWithLifecycle()
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val antiBurnInEnabled by viewModel.antiBurnInEnabled.collectAsStateWithLifecycle()
    
    val inspirationEnabled by viewModel.inspirationEnabled.collectAsStateWithLifecycle()
    val sunriseModeEnabled by viewModel.sunriseModeEnabled.collectAsStateWithLifecycle()

    val burnInOffset by viewModel.burnInOffset.collectAsStateWithLifecycle()
    val applyNightShift by viewModel.applyNightShift.collectAsStateWithLifecycle()

    val anyWidgetEnabled = weatherEnabled || calendarEnabled || mediaEnabled

    val sunriseManager = remember { SunriseManager(context) }
    val sunriseProgress = if (sunriseModeEnabled) sunriseManager.rememberSunriseProgress() else 0f

    val config = remember(bgPrimary, bgSecondary, bgUseGradient, sunriseProgress) {
        if (sunriseProgress > 0.05f) {
            val sunriseColors = SunriseManager.getSunriseColors(sunriseProgress)
            BackgroundConfig(
                primaryColor = sunriseColors.first,
                secondaryColor = sunriseColors.second,
                useGradient = true,
            )
        } else {
            BackgroundConfig(
                primaryColor = Color(bgPrimary.toInt()),
                secondaryColor = Color(bgSecondary.toInt()),
                useGradient = bgUseGradient,
            )
        }
    }

    val clockType = remember(clockTypeName) {
        try { ClockType.valueOf(clockTypeName) } catch (_: Exception) { ClockType.FLIP }
    }
    val clockColor = Color(clockColorLong.toInt())

    var settingsOpen by remember { mutableStateOf(value = false) }
    var newsRefreshTrigger by remember { mutableIntStateOf(0) }
    val gearRotation by animateFloatAsState(
        targetValue = if (settingsOpen) 90f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "gearRotation",
    )
    
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
                // 1. Battery widget
                if (batteryEnabled) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(top = 10.dp, start = 12.dp)
                    ) {
                        BatteryWidget()
                    }
                }

                // 2. Settings gear button
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
                            color = Color.White
                        )
                        val measured = textMeasurer.measure(AnnotatedString("⚙"), style)
                        drawText(
                            textLayoutResult = measured,
                            topLeft = Offset(
                                (size.width / 2f) - (measured.size.width / 2f),
                                (size.height / 2f) - (measured.size.height / 2f)
                            )
                        )
                    }
                }

                // 3. Main content
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
                    Column(
                        modifier = Modifier
                            .weight(if (anyWidgetEnabled) 2f else 1f)
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top,
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 70.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            DateWidget(
                                modifier = Modifier.wrapContentWidth(),
                                textColor = clockColor,
                                isFullScreen = !anyWidgetEnabled,
                            )
                        }

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
                                                val currentIndex = allTypes.indexOfFirst { it.name == clockTypeName }
                                                if (currentIndex != -1) {
                                                    val nextIndex = if (totalDrag < 0) {
                                                        (currentIndex + 1) % allTypes.size
                                                    } else {
                                                        (currentIndex - 1 + allTypes.size) % allTypes.size
                                                    }
                                                    viewModel.setClockType(allTypes[nextIndex].name)
                                                }
                                            }
                                            totalDrag = 0f
                                        }
                                    ) { change, dragAmount ->
                                        change.consume()
                                        totalDrag += dragAmount
                                    }
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

                        if (inspirationEnabled) {
                            InspirationWidget(textColor = clockColor)
                        }
                    }

                    if (anyWidgetEnabled) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState())
                                .padding(vertical = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(
                                12.dp,
                                Alignment.Top
                            )
                        ) {
                            if (weatherEnabled) {
                                WeatherWidget(
                                    modifier = Modifier.fillMaxWidth(),
                                    latitude = weatherLat,
                                    longitude = weatherLon,
                                    cityName = weatherCity,
                                    language = appLanguage
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

            SettingsPanel(
                visible = settingsOpen,
                onDismiss = { settingsOpen = false }
            ) {
                newsRefreshTrigger++
            }

            if (applyNightShift) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFE8722A).copy(alpha = 0.15f))
                        .background(Color.Black.copy(alpha = 0.10f))
                )
            }
        }
    }
}
