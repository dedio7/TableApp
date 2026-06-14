package com.dedio.dailypulse.ui.main

import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.view.WindowManager
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dedio.dailypulse.background.AmbientBackground
import com.dedio.dailypulse.background.BackgroundConfig
import com.dedio.dailypulse.battery.rememberBatteryState
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
import com.dedio.dailypulse.timer.TimerWidget
import com.dedio.dailypulse.ui.i18n.ProvideLocalization
import com.dedio.dailypulse.weather.WeatherWidget
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val appSettings = remember { AppSettings(context) }
    val viewModel: MainScreenViewModel = viewModel { MainScreenViewModel(appSettings) }
    val batteryInfo = rememberBatteryState()
    val mediaInfo = com.dedio.dailypulse.media.rememberMediaController()

    // --- UX Idle Logic ---
    var isIdle by remember { mutableStateOf(false) }
    var lastInteraction by remember { mutableLongStateOf(System.currentTimeMillis()) }
    
    LaunchedEffect(lastInteraction) {
        isIdle = false
        delay(8.seconds)
        isIdle = true
    }

    val controlsAlpha by animateFloatAsState(targetValue = if (isIdle) 0f else 1f, animationSpec = tween(1500), label = "fade")
    val batteryGlowAlpha by rememberInfiniteTransition(label = "glow").animateFloat(
        initialValue = 0.3f, targetValue = 0.8f, 
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse), label = "alpha"
    )

    // --- Screen On Logic ---
    LaunchedEffect(Unit) {
        val activity = context as? Activity
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val statusIntent = context.registerReceiver(null, intentFilter)
        statusIntent?.let { intent ->
            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
            activity?.window?.let { window ->
                if (isCharging) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                else window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    // ── Settings ──────────────────────────────────────────────────────────
    val atmosphere by viewModel.atmosphere.collectAsStateWithLifecycle()
    val clockTypeName by viewModel.clockTypeName.collectAsStateWithLifecycle()
    val clockColorLong by viewModel.clockColorLong.collectAsStateWithLifecycle()
    val showSeconds by viewModel.showSeconds.collectAsStateWithLifecycle()
    val binaryModeName by viewModel.binaryModeName.collectAsStateWithLifecycle()
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
    val timerEnabled by viewModel.timerEnabled.collectAsStateWithLifecycle()
    val dateFormat by viewModel.dateFormat.collectAsStateWithLifecycle()
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val antiBurnInEnabled by viewModel.antiBurnInEnabled.collectAsStateWithLifecycle()
    val inspirationEnabled by viewModel.inspirationEnabled.collectAsStateWithLifecycle()
    val sunriseModeEnabled by viewModel.sunriseModeEnabled.collectAsStateWithLifecycle()
    val burnInOffset by viewModel.burnInOffset.collectAsStateWithLifecycle()
    val applyNightShift by viewModel.applyNightShift.collectAsStateWithLifecycle()

    val anyWidgetEnabled = weatherEnabled || calendarEnabled || mediaEnabled || timerEnabled

    val config = remember(atmosphere, mediaInfo.isPlaying) {
        BackgroundConfig(atmosphere = atmosphere, isMusicPlaying = mediaInfo.isPlaying)
    }

    val clockType = remember(clockTypeName) {
        try { ClockType.valueOf(clockTypeName) } catch (_: Exception) { ClockType.FLIP }
    }
    val clockColor = Color(clockColorLong.toInt())

    var settingsOpen by remember { mutableStateOf(value = false) }
    var newsRefreshTrigger by remember { mutableIntStateOf(0) }
    val gearRotation by animateFloatAsState(targetValue = if (settingsOpen) 90f else 0f, animationSpec = tween(300), label = "gear")
    
    ProvideLocalization(appLanguage) {
        AmbientBackground(config = config, modifier = Modifier.fillMaxSize()) {
            BoxWithConstraints(
                modifier = modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        // Efficient touch detection to avoid main thread choke
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                if (event.changes.any { it.pressed }) {
                                    lastInteraction = System.currentTimeMillis()
                                }
                            }
                        }
                    }
                    .then(
                        if (antiBurnInEnabled) {
                            Modifier.layout { measurable, constraints ->
                                val placeable = measurable.measure(constraints)
                                layout(placeable.width, placeable.height) {
                                    placeable.placeRelative(burnInOffset.x.toInt(), burnInOffset.y.toInt())
                                }
                            }
                        } else Modifier
                    )
            ) {
                val screenHeight = maxHeight
                val isSmallHeight = screenHeight < 500.dp
                val isPortrait = maxWidth < maxHeight

                // 1. Unified Energy Ring & Settings (Top-Right)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = if (isPortrait) 32.dp else 10.dp, end = 12.dp) // More padding for mobile status bars
                        .alpha(controlsAlpha)
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (isSmallHeight || isPortrait) 40.dp else 46.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                            .clickable(enabled = !isIdle) { 
                                lastInteraction = System.currentTimeMillis()
                                settingsOpen = !settingsOpen 
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (batteryEnabled) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val sw = 3.dp.toPx()
                                val r = (size.minDimension - sw) / 2f
                                drawCircle(Color.White.copy(alpha = 0.05f), radius = r, style = Stroke(width = sw))
                                val sweep = (batteryInfo.level / 100f) * 360f
                                val ringColor = if (batteryInfo.level < 20 && !batteryInfo.isCharging) Color(0xFFFF5252) else Color(0xFF00E5FF).copy(alpha = 0.8f)
                                drawArc(color = if (batteryInfo.isCharging) Color.White.copy(alpha = batteryGlowAlpha) else ringColor, startAngle = -90f, sweepAngle = sweep, useCenter = false, style = Stroke(width = sw, cap = StrokeCap.Round))
                                drawArc(color = (if (batteryInfo.isCharging) Color.White else ringColor).copy(alpha = 0.2f), startAngle = -90f, sweepAngle = sweep, useCenter = false, style = Stroke(width = sw * 2.5f, cap = StrokeCap.Round))
                            }
                        }
                        Text("⚙", color = Color.White.copy(alpha = 0.9f), fontSize = if (isSmallHeight || isPortrait) 18.sp else 22.sp, modifier = Modifier.rotate(gearRotation))
                    }
                }

                // 2. Main content
                if (isPortrait) {
                    // --- PORTRAIT MOBILE LAYOUT ---
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp)
                            .padding(top = 80.dp, bottom = if (newsEnabled) 80.dp else 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        DateWidget(textColor = clockColor, dateFormat = dateFormat, isFullScreen = !anyWidgetEnabled)
                        
                        Spacer(modifier = Modifier.height(24.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp) // Large fixed height for clock on mobile
                                .pointerInput(Unit) {
                                    var totalDrag = 0f
                                    detectHorizontalDragGestures(onDragEnd = {
                                        if (kotlin.math.abs(totalDrag) > 60f) {
                                            val allTypes = ClockType.entries
                                            val currentIndex = allTypes.indexOfFirst { it.name == clockTypeName }
                                            if (currentIndex != -1) {
                                                val nextIndex = if (totalDrag < 0) (currentIndex + 1) % allTypes.size else (currentIndex - 1 + allTypes.size) % allTypes.size
                                                viewModel.setClockType(allTypes[nextIndex].name)
                                            }
                                        }
                                        totalDrag = 0f
                                    }) { change, dragAmount -> change.consume(); totalDrag += dragAmount; lastInteraction = System.currentTimeMillis() }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Crossfade(targetState = clockType, label = "clockFade", animationSpec = tween(500)) { targetType ->
                                ClockDisplay(clockType = targetType, modifier = Modifier.fillMaxSize(), textColor = clockColor, showSeconds = showSeconds, binaryMode = binaryModeName, language = appLanguage, isFullScreen = !anyWidgetEnabled)
                            }
                        }

                        if (inspirationEnabled) {
                            InspirationWidget(textColor = clockColor)
                            Spacer(modifier = Modifier.height(32.dp))
                        }

                        if (anyWidgetEnabled) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (weatherEnabled) WeatherWidget(modifier = Modifier.fillMaxWidth(), latitude = weatherLat, longitude = weatherLon, cityName = weatherCity, language = appLanguage)
                                if (calendarEnabled) CalendarWidget(modifier = Modifier.fillMaxWidth())
                                if (mediaEnabled) MediaWidget(modifier = Modifier.fillMaxWidth())
                                if (timerEnabled) TimerWidget(modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }
                } else {
                    // --- LANDSCAPE TABLET LAYOUT ---
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .padding(
                                top = if (isSmallHeight) 48.dp else 64.dp,
                                bottom = if (newsEnabled) 60.dp else 16.dp,
                                start = 24.dp,
                                end = 24.dp
                            ),
                        horizontalArrangement = Arrangement.spacedBy(if (isSmallHeight) 12.dp else 32.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(if (anyWidgetEnabled) 2.2f else 1f).fillMaxHeight(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
                                DateWidget(modifier = Modifier.wrapContentWidth(), textColor = clockColor, dateFormat = dateFormat, isFullScreen = !anyWidgetEnabled)
                            }

                            Spacer(modifier = Modifier.height(if (isSmallHeight) 8.dp else 16.dp))

                            Box(
                                modifier = Modifier.weight(1f).fillMaxWidth().pointerInput(Unit) {
                                    var totalDrag = 0f
                                    detectHorizontalDragGestures(onDragEnd = {
                                        if (kotlin.math.abs(totalDrag) > 60f) {
                                            val allTypes = ClockType.entries
                                            val currentIndex = allTypes.indexOfFirst { it.name == clockTypeName }
                                            if (currentIndex != -1) {
                                                val nextIndex = if (totalDrag < 0) (currentIndex + 1) % allTypes.size else (currentIndex - 1 + allTypes.size) % allTypes.size
                                                viewModel.setClockType(allTypes[nextIndex].name)
                                            }
                                        }
                                        totalDrag = 0f
                                    }) { change, dragAmount -> change.consume(); totalDrag += dragAmount; lastInteraction = System.currentTimeMillis() }
                                },
                            contentAlignment = Alignment.Center
                            ) {
                                Crossfade(targetState = clockType, label = "clockFade", animationSpec = tween(500)) { targetType ->
                                    ClockDisplay(clockType = targetType, modifier = Modifier.fillMaxSize(), textColor = clockColor, showSeconds = showSeconds, binaryMode = binaryModeName, language = appLanguage, isFullScreen = !anyWidgetEnabled)
                                }
                            }

                            if (inspirationEnabled) {
                                InspirationWidget(textColor = clockColor)
                            }
                        }

                        if (anyWidgetEnabled) {
                            Column(
                                modifier = Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()).padding(vertical = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top)
                            ) {
                                if (weatherEnabled) WeatherWidget(modifier = Modifier.fillMaxWidth(), latitude = weatherLat, longitude = weatherLon, cityName = weatherCity, language = appLanguage)
                                if (calendarEnabled) CalendarWidget(modifier = Modifier.fillMaxWidth())
                                if (mediaEnabled) MediaWidget(modifier = Modifier.fillMaxWidth())
                                if (timerEnabled) TimerWidget(modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }
                }

                if (newsEnabled) {
                    Box(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()) {
                        NewsTicker(modifier = Modifier.fillMaxWidth(), refreshTrigger = newsRefreshTrigger, refreshIntervalMs = newsRefreshMinutes * 60_000L, enabledSources = newsSources, language = appLanguage)
                    }
                }
            }

            SettingsPanel(visible = settingsOpen, onDismiss = { settingsOpen = false }) { newsRefreshTrigger++ }

            if (applyNightShift) {
                Box(modifier = Modifier.fillMaxSize().background(Color(0xFFE8722A).copy(alpha = 0.12f)).background(Color.Black.copy(alpha = 0.08f)))
            }
        }
    }
}
