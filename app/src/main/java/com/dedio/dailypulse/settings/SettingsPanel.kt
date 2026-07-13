package com.dedio.dailypulse.settings

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dedio.dailypulse.background.Atmosphere
import com.dedio.dailypulse.clock.ClockType
import com.dedio.dailypulse.news.DEFAULT_RSS_SOURCES
import com.dedio.dailypulse.ui.i18n.LocalStrings
import com.dedio.dailypulse.weather.WeatherLocation
import com.dedio.dailypulse.weather.WeatherRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

private val AccentBlue = Color(0xFF4FC3F7)
private val PanelBg = Color(0xFF101626)
private val SurfaceBg = Color(0xFF1E294A)
private val TextPrimary = Color(0xFFEEEEEE)
private val TextSecondary = Color(0xFF90A4AE)
private val DividerColor = Color(0xFF263254)

private val CLOCK_COLOR_PRESETS: List<Pair<String, Color>> = listOf(
    "Bianco" to Color(0xFFFFFFFF), "Arancione" to Color(0xFFFF9100), "Azzurro" to Color(0xFF00B0FF),
    "Verde" to Color(0xFF00E676), "Giallo" to Color(0xFFFFEA00), "Rosa" to Color(0xFFFF4081),
    "Viola" to Color(0xFFD500F9), "Rosso" to Color(0xFFFF1744), "Teal" to Color(0xFF1DE9B6),
    "Ambra" to Color(0xFFFFC400), "Indaco" to Color(0xFF651FFF), "Lime" to Color(0xFFC6FF00),
    "Ciano" to Color(0xFF00E5FF), "Deep Orange" to Color(0xFFFF3D00), "Grigio" to Color(0xFFCFD8DC),
    "Oro" to Color(0xFFFFD700),
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsPanel(
    visible: Boolean,
    onDismiss: () -> Unit,
    onNewsRefresh: () -> Unit = {},
) {
    val context = LocalContext.current
    val appSettings = remember { AppSettings(context) }
    val scope = rememberCoroutineScope()
    val strings = LocalStrings.current

    val clockType by appSettings.clockType.collectAsStateWithLifecycle(initialValue = "FLIP")
    val clockColor by appSettings.clockColor.collectAsStateWithLifecycle(initialValue = 0xFFE8E8E8L)
    val showSeconds by appSettings.showSeconds.collectAsStateWithLifecycle(initialValue = true)
    val neonModeEnabled by appSettings.neonModeEnabled.collectAsStateWithLifecycle(initialValue = false)
    val currentCity by appSettings.weatherCity.collectAsStateWithLifecycle(initialValue = "Roma")
    val binaryMode by appSettings.binaryClockMode.collectAsStateWithLifecycle(initialValue = "BINARY")
    val nightShiftEnabled by appSettings.nightShiftEnabled.collectAsStateWithLifecycle(initialValue = false)
    val antiBurnInEnabled by appSettings.antiBurnInEnabled.collectAsStateWithLifecycle(initialValue = true)
    val appLanguage by appSettings.appLanguage.collectAsStateWithLifecycle(initialValue = "IT")
    val dateFormat by appSettings.dateFormat.collectAsStateWithLifecycle(initialValue = "IT")
    val atmosphereName by appSettings.atmosphereName.collectAsStateWithLifecycle(initialValue = "DEEP_SPACE")
    val appOrientation by appSettings.appOrientation.collectAsStateWithLifecycle(initialValue = "AUTO")
    
    val nightStart by appSettings.nightModeStart.collectAsStateWithLifecycle(initialValue = 22)
    val nightEnd by appSettings.nightModeEnd.collectAsStateWithLifecycle(initialValue = 7)
    val nightBrightness by appSettings.nightModeBrightness.collectAsStateWithLifecycle(initialValue = 0.3f)

    val weatherEnabled by appSettings.weatherEnabled.collectAsStateWithLifecycle(initialValue = true)
    val weatherUseGps by appSettings.weatherUseGps.collectAsStateWithLifecycle(initialValue = false)
    val batteryEnabled by appSettings.batteryEnabled.collectAsStateWithLifecycle(initialValue = true)
    val mediaEnabled by appSettings.mediaEnabled.collectAsStateWithLifecycle(initialValue = true)
    val calendarEnabled by appSettings.calendarEnabled.collectAsStateWithLifecycle(initialValue = true)
    val timerEnabled by appSettings.timerEnabled.collectAsStateWithLifecycle(initialValue = true)
    val worldClockEnabled by appSettings.worldClockEnabled.collectAsStateWithLifecycle(initialValue = false)
    val worldClockCities by appSettings.worldClockCities.collectAsStateWithLifecycle(initialValue = emptySet())
    val statsEnabled by appSettings.statsEnabled.collectAsStateWithLifecycle(initialValue = false)
    val sunriseModeEnabled by appSettings.sunriseModeEnabled.collectAsStateWithLifecycle(initialValue = false)
    
    val onThisDayEnabled by appSettings.onThisDayEnabled.collectAsStateWithLifecycle(initialValue = false)
    val visualNewsEnabled by appSettings.visualNewsEnabled.collectAsStateWithLifecycle(initialValue = false)
    val countdownEnabled by appSettings.countdownEnabled.collectAsStateWithLifecycle(initialValue = false)
    val marketTickerEnabled by appSettings.marketTickerEnabled.collectAsStateWithLifecycle(initialValue = false)
    
    val inspirationEnabled by appSettings.inspirationEnabled.collectAsStateWithLifecycle(initialValue = false)
    val discoveryEnabled by appSettings.discoveryEnabled.collectAsStateWithLifecycle(initialValue = false)
    
    val newsEnabled by appSettings.newsEnabled.collectAsStateWithLifecycle(initialValue = true)
    val enabledNewsSources by appSettings.newsSources.collectAsStateWithLifecycle(initialValue = emptySet())

    val widgetOrder by appSettings.widgetOrder.collectAsStateWithLifecycle(initialValue = listOf("WORLD_CLOCK", "STATS", "WEATHER", "CALENDAR", "MEDIA", "TIMER", "INSPIRATION", "DISCOVERY", "ON_THIS_DAY", "VISUAL_NEWS", "COUNTDOWN", "MARKET"))

    val weatherRepo = remember { WeatherRepository() }
    var cityQuery by remember { mutableStateOf("") }
    val searchResults = remember { mutableStateListOf<WeatherLocation>() }

    val backDispatcher = androidx.activity.compose.LocalOnBackPressedDispatcherOwner.current
    if (backDispatcher != null) {
        BackHandler(enabled = visible) {
            onDismiss()
        }
    }

    LaunchedEffect(cityQuery) {
        if (cityQuery.length >= 2) {
            delay(600.milliseconds)
            val results = weatherRepo.searchCity(cityQuery, appLanguage)
            searchResults.clear()
            searchResults.addAll(results)
        } else {
            searchResults.clear()
        }
    }

    AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut()) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.55f)).clickable { onDismiss() })
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow,
            )
        ) + fadeIn(),
        exit = slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) + fadeOut()
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(400.dp)
                    .background(PanelBg)
                    .padding(vertical = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = strings.settingsTitle.uppercase(), color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    IconButton(onClick = onDismiss) { Text("✕", color = TextSecondary, fontSize = 20.sp) }
                }
                
                HorizontalDivider(color = DividerColor, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // ─── 1. GENERAL & SYSTEM ───
                SettingGroup(title = strings.generalSystemSection) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(AccentBlue.copy(alpha = 0.15f))
                            .border(1.dp, AccentBlue.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .clickable {
                                try {
                                    val intent = Intent(Settings.ACTION_DREAM_SETTINGS)
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = strings.setupScreensaver.uppercase(), color = AccentBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    SettingLabel(label = strings.languageLabel, horizontalPadding = 0.dp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("IT" to "ITALIANO", "EN" to "ENGLISH").forEach { (code, label) ->
                            val isSelected = appLanguage == code
                            Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(if (isSelected) AccentBlue.copy(alpha = 0.2f) else SurfaceBg).border(1.dp, if (isSelected) AccentBlue else DividerColor, RoundedCornerShape(8.dp)).clickable { scope.launch { appSettings.setAppLanguage(code) } }.padding(8.dp), contentAlignment = Alignment.Center) {
                                Text(text = label, color = if (isSelected) AccentBlue else TextSecondary, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    SettingLabel(label = strings.dateFormatLabel, horizontalPadding = 0.dp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("IT" to (if(appLanguage == "IT") "GIORNO/MESE" else "DAY/MONTH"), "EN" to (if(appLanguage == "IT") "MESE/GIORNO" else "MONTH/DAY")).forEach { (code, label) ->
                            val isSelected = dateFormat == code
                            Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(if (isSelected) AccentBlue.copy(alpha = 0.2f) else SurfaceBg).border(1.dp, if (isSelected) AccentBlue else DividerColor, RoundedCornerShape(8.dp)).clickable { scope.launch { appSettings.setDateFormat(code) } }.padding(8.dp), contentAlignment = Alignment.Center) {
                                Text(text = label, color = if (isSelected) AccentBlue else TextSecondary, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    SettingLabel(label = strings.screenOrientationLabel, horizontalPadding = 0.dp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("AUTO" to strings.orientationAuto, "PORTRAIT" to strings.orientationPortrait, "LANDSCAPE" to strings.orientationLandscape).forEach { (code, label) ->
                            val isSelected = appOrientation == code
                            Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(if (isSelected) AccentBlue.copy(alpha = 0.2f) else SurfaceBg).border(1.dp, if (isSelected) AccentBlue else DividerColor, RoundedCornerShape(8.dp)).clickable { scope.launch { appSettings.setAppOrientation(code) } }.padding(8.dp), contentAlignment = Alignment.Center) {
                                Text(text = label, color = if (isSelected) AccentBlue else TextSecondary, fontSize = 9.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }

                // ─── 2. CLOCK & APPEARANCE ───
                SettingGroup(title = strings.clockAppearanceSection) {
                    SettingLabel(label = strings.clockTypeLabel, horizontalPadding = 0.dp)
                    ClockTypeSelector(selected = clockType, onSelect = { scope.launch { appSettings.setClockType(it) } }, horizontalPadding = 0.dp)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    SettingLabel(label = strings.clockColorLabel, horizontalPadding = 0.dp)
                    ColorPicker(colors = CLOCK_COLOR_PRESETS, selectedColor = Color(clockColor.toInt()), onColorSelected = { scope.launch { appSettings.setClockColor(it.toArgb().toLong() and 0xFFFFFFFFL) } }, horizontalPadding = 0.dp)
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    SettingSwitch(label = strings.neonModeLabel, checked = neonModeEnabled, onCheckedChange = { scope.launch { appSettings.setNeonModeEnabled(it) } }, horizontalPadding = 0.dp)
                    SettingSwitch(label = strings.showSecondsLabel, checked = showSeconds, onCheckedChange = { scope.launch { appSettings.setShowSeconds(it) } }, horizontalPadding = 0.dp)

                    if (clockType == "BINARY") {
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = DividerColor.copy(alpha = 0.5f), thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = strings.binarySettingsLabel.uppercase(), color = AccentBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("BINARY" to (if(appLanguage == "IT") "Binario" else "Binary"), "BCD" to "BCD").forEach { (code, label) ->
                                val isSelected = binaryMode == code
                                Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(if (isSelected) AccentBlue.copy(alpha = 0.2f) else SurfaceBg).border(1.dp, if (isSelected) AccentBlue else DividerColor, RoundedCornerShape(8.dp)).clickable { scope.launch { appSettings.setBinaryClockMode(code) } }.padding(8.dp), contentAlignment = Alignment.Center) {
                                    Text(text = label, color = if (isSelected) AccentBlue else TextSecondary, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    SettingLabel(label = strings.backgroundSection, horizontalPadding = 0.dp)
                    AtmosphereGallery(selected = atmosphereName, onSelect = { scope.launch { appSettings.setAtmosphereName(it) } }, horizontalPadding = 0.dp)
                }

                // ─── 3. WIDGETS & FEATURES ───
                SettingGroup(title = strings.widgetsFeaturesSection) {
                    SettingLabel(label = if(appLanguage == "IT") "GESTIONE WIDGET" else "WIDGET MANAGEMENT", horizontalPadding = 0.dp)
                    WidgetManagerList(
                        currentOrder = widgetOrder,
                        onOrderChange = { scope.launch { appSettings.setWidgetOrder(it) } },
                        onToggle = { id, enabled ->
                            scope.launch {
                                when(id) {
                                    "WORLD_CLOCK" -> appSettings.setWorldClockEnabled(enabled)
                                    "STATS" -> appSettings.setStatsEnabled(enabled)
                                    "WEATHER" -> appSettings.setWeatherEnabled(enabled)
                                    "CALENDAR" -> appSettings.setCalendarEnabled(enabled)
                                    "MEDIA" -> appSettings.setMediaEnabled(enabled)
                                    "TIMER" -> appSettings.setTimerEnabled(enabled)
                                    "INSPIRATION" -> appSettings.setInspirationEnabled(enabled)
                                    "DISCOVERY" -> appSettings.setDiscoveryEnabled(enabled)
                                    "ON_THIS_DAY" -> appSettings.setOnThisDayEnabled(enabled)
                                    "VISUAL_NEWS" -> appSettings.setVisualNewsEnabled(enabled)
                                    "COUNTDOWN" -> appSettings.setCountdownEnabled(enabled)
                                    "MARKET" -> appSettings.setMarketTickerEnabled(enabled)
                                }
                            }
                        },
                        states = mapOf(
                            "WORLD_CLOCK" to worldClockEnabled,
                            "STATS" to statsEnabled,
                            "WEATHER" to weatherEnabled,
                            "CALENDAR" to calendarEnabled,
                            "MEDIA" to mediaEnabled,
                            "TIMER" to timerEnabled,
                            "INSPIRATION" to inspirationEnabled,
                            "DISCOVERY" to discoveryEnabled,
                            "ON_THIS_DAY" to onThisDayEnabled,
                            "VISUAL_NEWS" to visualNewsEnabled,
                            "COUNTDOWN" to countdownEnabled,
                            "MARKET" to marketTickerEnabled
                        ),
                        appLanguage = appLanguage
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (weatherEnabled) {
                        SettingLabel(label = strings.weatherEnabledLabel.uppercase(), horizontalPadding = 0.dp)
                        SettingSwitch(label = strings.gpsLabel, checked = weatherUseGps, onCheckedChange = { scope.launch { appSettings.setWeatherUseGps(it) } }, horizontalPadding = 0.dp)
                        
                        if (!weatherUseGps) {
                            var localCityQuery by remember { mutableStateOf(currentCity) }
                            OutlinedTextField(
                                value = localCityQuery, 
                                onValueChange = { 
                                    localCityQuery = it
                                    cityQuery = it 
                                }, 
                                placeholder = { Text(strings.weatherSearchPlaceholder, color = TextSecondary, fontSize = 12.sp) }, 
                                label = { Text(strings.weatherCurrentCity.format(currentCity), fontSize = 11.sp) },
                                singleLine = true, 
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedBorderColor = AccentBlue, unfocusedBorderColor = DividerColor, cursorColor = AccentBlue), 
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            )
                            searchResults.forEach { loc ->
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp).clip(RoundedCornerShape(8.dp)).background(SurfaceBg).clickable { scope.launch { appSettings.setWeatherLocation(loc.latitude, loc.longitude, loc.cityName); cityQuery = ""; searchResults.clear() } }.padding(8.dp)) {
                                    Text("•", color = AccentBlue, fontWeight = FontWeight.Bold); Spacer(Modifier.width(8.dp)); Text(text = loc.cityName, color = TextPrimary, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                    
                    if (countdownEnabled) {
                        Spacer(modifier = Modifier.height(12.dp))
                        SettingLabel(label = if(appLanguage == "IT") "CONFIGURA COUNTDOWN" else "COUNTDOWN CONFIG", horizontalPadding = 0.dp)
                        val cDate by appSettings.countdownDate.collectAsStateWithLifecycle(initialValue = "2026-12-25")
                        val cLabel by appSettings.countdownLabel.collectAsStateWithLifecycle(initialValue = "Christmas")
                        var tempDate by remember { mutableStateOf(cDate) }
                        var tempLabel by remember { mutableStateOf(cLabel) }
                        
                        OutlinedTextField(
                            value = tempLabel,
                            onValueChange = { tempLabel = it },
                            label = { Text("Label", fontSize = 10.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedBorderColor = AccentBlue)
                        )
                        OutlinedTextField(
                            value = tempDate,
                            onValueChange = { tempDate = it },
                            label = { Text("Date (YYYY-MM-DD)", fontSize = 10.sp) },
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedBorderColor = AccentBlue)
                        )
                        Button(
                            onClick = { scope.launch { appSettings.setCountdownTarget(tempDate, tempLabel) } },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("SAVE TARGET", fontSize = 11.sp)
                        }
                    }

                    if (worldClockEnabled) {
                        Spacer(modifier = Modifier.height(12.dp))
                        SettingLabel(label = strings.worldClockLabel.uppercase(), horizontalPadding = 0.dp)
                        FlowRow(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val allTimeZones = listOf(
                                "Pacific/Midway", "Pacific/Honolulu", "America/Anchorage", "America/Los_Angeles",
                                "America/Denver", "America/Chicago", "America/New_York", "America/Halifax",
                                "America/Sao_Paulo", "Atlantic/South_Georgia", "Atlantic/Azores", "Europe/London",
                                "Europe/Rome", "Africa/Cairo", "Europe/Moscow", "Asia/Dubai",
                                "Asia/Karachi", "Asia/Dhaka", "Asia/Bangkok", "Asia/Singapore",
                                "Asia/Tokyo", "Australia/Sydney", "Pacific/Guadalcanal", "Pacific/Auckland"
                            )
                            allTimeZones.forEach { tz ->
                                val isSelected = tz in worldClockCities
                                val cityName = tz.substringAfterLast('/').replace('_', ' ')
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) AccentBlue.copy(alpha = 0.2f) else SurfaceBg)
                                        .border(1.dp, if (isSelected) AccentBlue else DividerColor, RoundedCornerShape(6.dp))
                                        .clickable { 
                                            scope.launch { 
                                                val current = worldClockCities.toMutableSet()
                                                if (isSelected) current.remove(tz) else current.add(tz)
                                                appSettings.setWorldClockCities(current)
                                            } 
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(text = cityName, color = if (isSelected) AccentBlue else TextPrimary, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = DividerColor.copy(alpha = 0.5f), thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    SettingSwitch(label = strings.newsEnabledLabel, checked = newsEnabled, onCheckedChange = { scope.launch { appSettings.setNewsEnabled(it) } }, horizontalPadding = 0.dp)
                    if (newsEnabled) {
                        FlowRow(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            DEFAULT_RSS_SOURCES.forEach { source ->
                                val isSelected = source.name in enabledNewsSources
                                Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(if (isSelected) AccentBlue.copy(alpha = 0.2f) else SurfaceBg).border(1.dp, if (isSelected) AccentBlue else DividerColor, RoundedCornerShape(6.dp)).clickable { scope.launch { val current = enabledNewsSources.toMutableSet(); if (isSelected) current.remove(source.name) else current.add(source.name); appSettings.setNewsSources(current) } }.padding(horizontal = 8.dp, vertical = 4.dp)) {
                                    Text(text = source.name, color = if (isSelected) AccentBlue else TextPrimary, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                }
                            }
                        }
                        Button(
                            onClick = { onNewsRefresh(); onDismiss() },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue.copy(alpha = 0.2f), contentColor = AccentBlue)
                        ) {
                            Text(strings.newsRefreshButton.uppercase(), fontSize = 12.sp, fontWeight = FontWeight.Black)
                        }
                    }
                    
                    SettingSwitch(label = strings.batteryEnabledLabel, checked = batteryEnabled, onCheckedChange = { scope.launch { appSettings.setBatteryEnabled(it) } }, horizontalPadding = 0.dp)
                }

                // ─── 4. DISPLAY OPTIONS ───
                SettingGroup(title = strings.displayOptionsSection) {
                    SettingSwitch(label = strings.nightShiftLabel, checked = nightShiftEnabled, onCheckedChange = { scope.launch { appSettings.setNightShiftEnabled(it) } }, horizontalPadding = 0.dp)
                    if (nightShiftEnabled) {
                        Column(modifier = Modifier.padding(top = 4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(strings.nightStartLabel, color = TextSecondary, fontSize = 11.sp, modifier = Modifier.weight(1f))
                                Slider(value = nightStart.toFloat(), onValueChange = { scope.launch { appSettings.setNightModeStart(it.toInt()) } }, valueRange = 0f..23f, steps = 23, modifier = Modifier.weight(2f))
                                Text("$nightStart:00", color = TextPrimary, fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(strings.nightEndLabel, color = TextSecondary, fontSize = 11.sp, modifier = Modifier.weight(1f))
                                Slider(value = nightEnd.toFloat(), onValueChange = { scope.launch { appSettings.setNightModeEnd(it.toInt()) } }, valueRange = 0f..23f, steps = 23, modifier = Modifier.weight(2f))
                                Text("$nightEnd:00", color = TextPrimary, fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(strings.nightBrightnessLabel, color = TextSecondary, fontSize = 11.sp, modifier = Modifier.weight(1f))
                                Slider(value = nightBrightness, onValueChange = { scope.launch { appSettings.setNightModeBrightness(it) } }, valueRange = 0.05f..0.8f, modifier = Modifier.weight(2f))
                            }
                        }
                    }
                    SettingSwitch(label = strings.antiBurnInLabel, checked = antiBurnInEnabled, onCheckedChange = { scope.launch { appSettings.setAntiBurnInEnabled(it) } }, horizontalPadding = 0.dp)
                    SettingSwitch(label = strings.sunriseModeLabel, checked = sunriseModeEnabled, onCheckedChange = { scope.launch { appSettings.setSunriseModeEnabled(it) } }, horizontalPadding = 0.dp)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "DailyPulse Version 1.3.3 (Code 32)",
                    color = TextSecondary.copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun WidgetManagerList(
    currentOrder: List<String>,
    onOrderChange: (List<String>) -> Unit,
    onToggle: (String, Boolean) -> Unit,
    states: Map<String, Boolean>,
    appLanguage: String
) {
    val widgetNames = mapOf(
        "WORLD_CLOCK" to (if (appLanguage == "IT") "Orologio Mondiale" else "World Clock"),
        "STATS" to (if (appLanguage == "IT") "Statistiche Sistema" else "System Stats"),
        "WEATHER" to (if (appLanguage == "IT") "Meteo" else "Weather"),
        "CALENDAR" to (if (appLanguage == "IT") "Calendario" else "Calendar"),
        "MEDIA" to (if (appLanguage == "IT") "Media Player" else "Media Player"),
        "TIMER" to (if (appLanguage == "IT") "Timer / Pomodoro" else "Timer / Pomodoro"),
        "INSPIRATION" to (if (appLanguage == "IT") "Citazione del Giorno" else "Quote of the Day"),
        "DISCOVERY" to (if (appLanguage == "IT") "Discovery" else "Discovery"),
        "ON_THIS_DAY" to (if (appLanguage == "IT") "Accadde Oggi" else "On This Day"),
        "VISUAL_NEWS" to (if (appLanguage == "IT") "News Visive" else "Visual News"),
        "COUNTDOWN" to (if (appLanguage == "IT") "Countdown Evento" else "Event Countdown"),
        "MARKET" to (if (appLanguage == "IT") "Ticker Mercati" else "Market Ticker")
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        currentOrder.forEachIndexed { index, id ->
            val isEnabled = states[id] ?: false
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isEnabled) SurfaceBg else SurfaceBg.copy(alpha = 0.4f))
                    .border(1.dp, if (isEnabled) DividerColor else DividerColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Toggle Switch
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { onToggle(id, it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = AccentBlue,
                        uncheckedThumbColor = TextSecondary,
                        uncheckedTrackColor = DividerColor
                    ),
                    modifier = Modifier.scale(0.6f)
                )
                
                // 2. Label
                Text(
                    text = widgetNames[id] ?: id,
                    color = if (isEnabled) TextPrimary else TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = if (isEnabled) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.weight(1f).padding(start = 4.dp)
                )
                
                // 3. Reorder Arrows
                Row {
                    if (index > 0) {
                        IconButton(
                            modifier = Modifier.size(28.dp),
                            onClick = {
                                val newList = currentOrder.toMutableList()
                                val item = newList.removeAt(index)
                                newList.add(index - 1, item)
                                onOrderChange(newList)
                            }
                        ) {
                            Text("▲", color = if (isEnabled) AccentBlue else TextSecondary.copy(alpha = 0.3f), fontSize = 12.sp)
                        }
                    }
                    if (index < currentOrder.size - 1) {
                        IconButton(
                            modifier = Modifier.size(28.dp),
                            onClick = {
                                val newList = currentOrder.toMutableList()
                                val item = newList.removeAt(index)
                                newList.add(index + 1, item)
                                onOrderChange(newList)
                            }
                        ) {
                            Text("▼", color = if (isEnabled) AccentBlue else TextSecondary.copy(alpha = 0.3f), fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceBg.copy(alpha = 0.4f))
            .border(1.dp, DividerColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = title.uppercase(),
            color = AccentBlue,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        content()
    }
}

@Composable
private fun AtmosphereGallery(selected: String, onSelect: (String) -> Unit, horizontalPadding: androidx.compose.ui.unit.Dp = 20.dp) {
    val atmospheres = Atmosphere.entries
    Column(modifier = Modifier.padding(horizontal = horizontalPadding), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        atmospheres.asSequence().chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { atm ->
                    val isSelected = selected == atm.name
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Brush.linearGradient(atm.colors))
                            .border(if (isSelected) 2.dp else 0.dp, Color.White, RoundedCornerShape(10.dp))
                            .clickable { onSelect(atm.name) }
                            .padding(6.dp),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        Text(
                            text = atm.displayName.uppercase(),
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingLabel(label: String, horizontalPadding: androidx.compose.ui.unit.Dp = 20.dp) {
    Text(text = label, color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(horizontal = horizontalPadding, vertical = 4.dp))
}

@Composable
private fun SettingSwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, horizontalPadding: androidx.compose.ui.unit.Dp = 20.dp) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = horizontalPadding, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = AccentBlue, uncheckedThumbColor = TextSecondary, uncheckedTrackColor = DividerColor), modifier = Modifier.scale(0.7f))
    }
}

@Composable
private fun ColorPicker(colors: List<Pair<String, Color>>, selectedColor: Color, onColorSelected: (Color) -> Unit, horizontalPadding: androidx.compose.ui.unit.Dp = 20.dp) {
    val rows = colors.chunked(8)
    Column(modifier = Modifier.padding(horizontal = horizontalPadding), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { rowColors ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowColors.forEach { (_, color) ->
                    val isSelected = selectedColor.toArgb() == color.toArgb()
                    Box(modifier = Modifier.size(if (isSelected) 28.dp else 24.dp).clip(CircleShape).background(color).border(if (isSelected) 2.dp else 1.dp, if (isSelected) Color.White else Color.White.copy(alpha = 0.3f), CircleShape).clickable { onColorSelected(color) }, contentAlignment = Alignment.Center) {
                        if (isSelected) { Text("✓", color = if (color.luminance() > 0.5f) Color.Black else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClockTypeSelector(selected: String, onSelect: (String) -> Unit, horizontalPadding: androidx.compose.ui.unit.Dp = 20.dp) {
    val types = ClockType.entries
    val rows = types.chunked(3)
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = horizontalPadding), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        rows.forEach { rowTypes ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                rowTypes.forEach { clockType ->
                    val isSelected = selected == clockType.name
                    Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(if (isSelected) AccentBlue.copy(alpha = 0.2f) else SurfaceBg).border(1.dp, if (isSelected) AccentBlue else DividerColor, RoundedCornerShape(8.dp)).clickable { onSelect(clockType.name) }.padding(vertical = 8.dp, horizontal = 4.dp), contentAlignment = Alignment.Center) {
                        Text(text = clockType.displayName.uppercase(), color = if (isSelected) AccentBlue else TextPrimary, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, maxLines = 1)
                    }
                }
            }
        }
    }
}
