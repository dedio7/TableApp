package com.dedio.dailypulse.settings

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
    "Oro" to Color(0xFFFFD700)
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsPanel(
    visible: Boolean,
    onDismiss: () -> Unit,
    onNewsRefresh: () -> Unit = {}
) {
    val context = LocalContext.current
    val appSettings = remember { AppSettings(context) }
    val scope = rememberCoroutineScope()
    val strings = LocalStrings.current

    val clockType by appSettings.clockType.collectAsStateWithLifecycle(initialValue = "FLIP")
    val clockColor by appSettings.clockColor.collectAsStateWithLifecycle(initialValue = 0xFFE8E8E8L)
    val showSeconds by appSettings.showSeconds.collectAsStateWithLifecycle(initialValue = true)
    val neonModeEnabled by appSettings.neonModeEnabled.collectAsStateWithLifecycle(initialValue = false)
    val newsEnabled by appSettings.newsEnabled.collectAsStateWithLifecycle(initialValue = true)
    val currentCity by appSettings.weatherCity.collectAsStateWithLifecycle(initialValue = "Roma")
    val binaryMode by appSettings.binaryClockMode.collectAsStateWithLifecycle(initialValue = "BINARY")
    val nightShiftEnabled by appSettings.nightShiftEnabled.collectAsStateWithLifecycle(initialValue = false)
    val antiBurnInEnabled by appSettings.antiBurnInEnabled.collectAsStateWithLifecycle(initialValue = true)
    val inspirationEnabled by appSettings.inspirationEnabled.collectAsStateWithLifecycle(initialValue = false)
    val sunriseModeEnabled by appSettings.sunriseModeEnabled.collectAsStateWithLifecycle(initialValue = false)
    val enabledNewsSources by appSettings.newsSources.collectAsStateWithLifecycle(initialValue = emptySet())
    val appLanguage by appSettings.appLanguage.collectAsStateWithLifecycle(initialValue = "IT")
    val dateFormat by appSettings.dateFormat.collectAsStateWithLifecycle(initialValue = "IT")
    val atmosphereName by appSettings.atmosphereName.collectAsStateWithLifecycle(initialValue = "DEEP_SPACE")
    
    val nightStart by appSettings.nightModeStart.collectAsStateWithLifecycle(initialValue = 22)
    val nightEnd by appSettings.nightModeEnd.collectAsStateWithLifecycle(initialValue = 7)
    val nightBrightness by appSettings.nightModeBrightness.collectAsStateWithLifecycle(initialValue = 0.3f)

    val weatherEnabled by appSettings.weatherEnabled.collectAsStateWithLifecycle(initialValue = true)
    val weatherUseGps by appSettings.weatherUseGps.collectAsStateWithLifecycle(initialValue = false)
    val batteryEnabled by appSettings.batteryEnabled.collectAsStateWithLifecycle(initialValue = true)
    val mediaEnabled by appSettings.mediaEnabled.collectAsStateWithLifecycle(initialValue = true)
    val calendarEnabled by appSettings.calendarEnabled.collectAsStateWithLifecycle(initialValue = true)
    val timerEnabled by appSettings.timerEnabled.collectAsStateWithLifecycle(initialValue = true)

    val weatherRepo = remember { WeatherRepository() }
    var cityQuery by remember { mutableStateOf("") }
    val searchResults = remember { mutableStateListOf<WeatherLocation>() }

    LaunchedEffect(cityQuery) {
        if (cityQuery.length >= 2) {
            delay(600L)
            val results = weatherRepo.searchCity(cityQuery, appLanguage)
            searchResults.clear()
            searchResults.addAll(results)
        } else {
            searchResults.clear()
        }
    }

    AnimatedVisibility(visible = visible, enter = androidx.compose.animation.fadeIn(), exit = androidx.compose.animation.fadeOut()) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.55f)).clickable { onDismiss() })
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        ) + androidx.compose.animation.fadeIn(),
        exit = slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) + androidx.compose.animation.fadeOut()
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

                // ─── 0. SETUP & SYSTEM (SISTEMA) - NOW AT TOP ───
                SectionHeader(title = "SETUP")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AccentBlue.copy(alpha = 0.15f))
                        .border(1.dp, AccentBlue.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .clickable {
                            val intent = Intent(Settings.ACTION_DREAM_SETTINGS)
                            context.startActivity(intent)
                        }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = strings.setupScreensaver.uppercase(),
                        color = AccentBlue,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                SettingLabel(label = strings.languageLabel)
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf("IT" to "ITALIANO", "EN" to "ENGLISH").forEach { (code, label) ->
                        val isSelected = appLanguage == code
                        Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(if (isSelected) AccentBlue.copy(alpha = 0.2f) else SurfaceBg).border(1.1.dp, if (isSelected) AccentBlue else DividerColor, RoundedCornerShape(10.dp)).clickable { scope.launch { appSettings.setAppLanguage(code) } }.padding(12.dp), contentAlignment = Alignment.Center) {
                            Text(text = label, color = if (isSelected) AccentBlue else TextSecondary, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                SettingLabel(label = strings.dateFormatLabel)
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf("IT" to "IT", "EN" to "EN").forEach { (code, label) ->
                        val isSelected = dateFormat == code
                        Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(if (isSelected) AccentBlue.copy(alpha = 0.2f) else SurfaceBg).border(1.1.dp, if (isSelected) AccentBlue else DividerColor, RoundedCornerShape(10.dp)).clickable { scope.launch { appSettings.setDateFormat(code) } }.padding(12.dp), contentAlignment = Alignment.Center) {
                            Text(text = label, color = if (isSelected) AccentBlue else TextSecondary, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = DividerColor, thickness = 1.dp)

                // ─── 1. DISPLAY (SCHERMO) ───
                SectionHeader(title = strings.clockSection.uppercase())
                SettingLabel(label = strings.clockTypeLabel)
                ClockTypeSelector(selected = clockType, onSelect = { scope.launch { appSettings.setClockType(it) } })
                
                Spacer(modifier = Modifier.height(12.dp))
                SettingLabel(label = strings.clockColorLabel)
                ColorPicker(colors = CLOCK_COLOR_PRESETS, selectedColor = Color(clockColor.toInt()), onColorSelected = { scope.launch { appSettings.setClockColor(it.toArgb().toLong() and 0xFFFFFFFFL) } })
                
                Spacer(modifier = Modifier.height(12.dp))
                SettingSwitch(label = strings.neonModeLabel, checked = neonModeEnabled, onCheckedChange = { scope.launch { appSettings.setNeonModeEnabled(it) } })
                SettingSwitch(label = strings.showSecondsLabel, checked = showSeconds, onCheckedChange = { scope.launch { appSettings.setShowSeconds(it) } })

                if (clockType == "BINARY") {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = strings.binarySettingsLabel.uppercase(), color = AccentBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 20.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    SettingLabel(label = strings.displayMode)
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("BINARY" to (if(appLanguage == "IT") "Binario" else "Binary"), "BCD" to "BCD").forEach { (code, label) ->
                            val isSelected = binaryMode == code
                            Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(if (isSelected) AccentBlue.copy(alpha = 0.2f) else SurfaceBg).border(1.5.dp, if (isSelected) AccentBlue else DividerColor, RoundedCornerShape(8.dp)).clickable { scope.launch { appSettings.setBinaryClockMode(code) } }.padding(8.dp), contentAlignment = Alignment.Center) {
                                Text(text = label, color = if (isSelected) AccentBlue else TextSecondary, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                SettingLabel(label = strings.backgroundSection)
                AtmosphereGallery(selected = atmosphereName, onSelect = { scope.launch { appSettings.setAtmosphereName(it) } })
                
                Spacer(modifier = Modifier.height(8.dp))
                SettingSwitch(label = strings.nightShiftLabel, checked = nightShiftEnabled, onCheckedChange = { scope.launch { appSettings.setNightShiftEnabled(it) } })
                if (nightShiftEnabled) {
                    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(strings.nightStartLabel, color = TextSecondary, fontSize = 12.sp, modifier = Modifier.weight(1f))
                            Slider(value = nightStart.toFloat(), onValueChange = { scope.launch { appSettings.setNightModeStart(it.toInt()) } }, valueRange = 0f..23f, steps = 23, modifier = Modifier.weight(2f))
                            Text("$nightStart:00", color = TextPrimary, fontSize = 12.sp, modifier = Modifier.padding(start = 8.dp))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(strings.nightEndLabel, color = TextSecondary, fontSize = 12.sp, modifier = Modifier.weight(1f))
                            Slider(value = nightEnd.toFloat(), onValueChange = { scope.launch { appSettings.setNightModeEnd(it.toInt()) } }, valueRange = 0f..23f, steps = 23, modifier = Modifier.weight(2f))
                            Text("$nightEnd:00", color = TextPrimary, fontSize = 12.sp, modifier = Modifier.padding(start = 8.dp))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(strings.nightBrightnessLabel, color = TextSecondary, fontSize = 12.sp, modifier = Modifier.weight(1f))
                            Slider(value = nightBrightness, onValueChange = { scope.launch { appSettings.setNightModeBrightness(it) } }, valueRange = 0.05f..0.8f, modifier = Modifier.weight(2f))
                        }
                    }
                }
                SettingSwitch(label = strings.antiBurnInLabel, checked = antiBurnInEnabled, onCheckedChange = { scope.launch { appSettings.setAntiBurnInEnabled(it) } })

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = DividerColor, thickness = 1.dp)

                // ─── 2. WIDGETS ───
                SectionHeader(title = strings.widgetSection.uppercase())
                SettingSwitch(label = strings.weatherEnabledLabel, checked = weatherEnabled, onCheckedChange = { scope.launch { appSettings.setWeatherEnabled(it) } })
                if (weatherEnabled) {
                    SettingSwitch(label = (if(appLanguage == "IT") "Usa GPS" else "Use GPS"), checked = weatherUseGps, onCheckedChange = { scope.launch { appSettings.setWeatherUseGps(it) } })
                    
                    if (!weatherUseGps) {
                        SettingLabel(label = strings.weatherCurrentCity.format(currentCity))
                        OutlinedTextField(value = cityQuery, onValueChange = { cityQuery = it }, placeholder = { Text(strings.weatherSearchPlaceholder, color = TextSecondary, fontSize = 14.sp) }, singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedBorderColor = AccentBlue, unfocusedBorderColor = DividerColor, cursorColor = AccentBlue), modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp))
                        searchResults.forEach { loc ->
                            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 3.dp).clip(RoundedCornerShape(8.dp)).background(SurfaceBg).border(1.dp, DividerColor, RoundedCornerShape(8.dp)).clickable { scope.launch { appSettings.setWeatherLocation(loc.latitude, loc.longitude, loc.cityName); cityQuery = ""; searchResults.clear() } }.padding(12.dp)) {
                                Text("•", color = AccentBlue, fontWeight = FontWeight.Bold); Spacer(Modifier.width(8.dp)); Text(text = loc.cityName, color = TextPrimary, fontSize = 14.sp)
                            }
                        }
                    }
                }
                SettingSwitch(label = strings.batteryEnabledLabel, checked = batteryEnabled, onCheckedChange = { scope.launch { appSettings.setBatteryEnabled(it) } })
                SettingSwitch(label = strings.mediaEnabledLabel, checked = mediaEnabled, onCheckedChange = { scope.launch { appSettings.setMediaEnabled(it) } })
                SettingSwitch(label = strings.pomodoroLabel, checked = timerEnabled, onCheckedChange = { scope.launch { appSettings.setTimerEnabled(it) } })
                SettingSwitch(label = strings.calendarEnabledLabel, checked = calendarEnabled, onCheckedChange = { scope.launch { appSettings.setCalendarEnabled(it) } })

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = DividerColor, thickness = 1.dp)

                // ─── 3. CONTENT (CONTENUTI) ───
                SectionHeader(title = (if(appLanguage == "IT") "Contenuti" else "Content").uppercase())
                SettingSwitch(label = strings.newsEnabledLabel, checked = newsEnabled, onCheckedChange = { scope.launch { appSettings.setNewsEnabled(it) } })
                if (newsEnabled) {
                    FlowRow(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        DEFAULT_RSS_SOURCES.forEach { source ->
                            val isSelected = source.name in enabledNewsSources
                            Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(if (isSelected) AccentBlue.copy(alpha = 0.2f) else SurfaceBg).border(1.1.dp, if (isSelected) AccentBlue else DividerColor, RoundedCornerShape(8.dp)).clickable { scope.launch { val current = enabledNewsSources.toMutableSet(); if (isSelected) current.remove(source.name) else current.add(source.name); appSettings.setNewsSources(current) } }.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                Text(text = source.name, color = if (isSelected) AccentBlue else TextPrimary, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).clip(RoundedCornerShape(12.dp)).background(AccentBlue.copy(alpha = 0.15f)).border(1.dp, AccentBlue.copy(alpha = 0.4f), RoundedCornerShape(12.dp)).clickable { onNewsRefresh(); onDismiss() }.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Text(text = strings.newsRefreshButton.uppercase(), color = AccentBlue, fontSize = 14.sp, fontWeight = FontWeight.Black)
                    }
                }
                SettingSwitch(label = strings.inspirationEnabledLabel, checked = inspirationEnabled, onCheckedChange = { scope.launch { appSettings.setInspirationEnabled(it) } })
                SettingSwitch(label = strings.sunriseModeLabel, checked = sunriseModeEnabled, onCheckedChange = { scope.launch { appSettings.setSunriseModeEnabled(it) } })
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun AtmosphereGallery(selected: String, onSelect: (String) -> Unit) {
    val atmospheres = Atmosphere.entries
    Column(modifier = Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        atmospheres.toList().chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { atm ->
                    val isSelected = selected == atm.name
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.linearGradient(atm.colors))
                            .border(if (isSelected) 2.dp else 0.dp, Color.White, RoundedCornerShape(12.dp))
                            .clickable { onSelect(atm.name) }
                            .padding(8.dp),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        Text(
                            text = atm.displayName.uppercase(),
                            color = Color.White,
                            fontSize = 11.sp,
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
private fun SectionHeader(title: String) {
    Text(text = title, color = AccentBlue, fontSize = 13.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp, modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp))
}

@Composable
private fun SettingLabel(label: String) {
    Text(text = label, color = TextSecondary, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp))
}

@Composable
private fun SettingSwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Switch(checked = checked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = AccentBlue, uncheckedThumbColor = TextSecondary, uncheckedTrackColor = DividerColor))
    }
}

@Composable
private fun ColorPicker(colors: List<Pair<String, Color>>, selectedColor: Color, onColorSelected: (Color) -> Unit) {
    val rows = colors.chunked(5)
    Column(modifier = Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { rowColors ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowColors.forEach { (_, color) ->
                    val isSelected = selectedColor.toArgb() == color.toArgb()
                    Box(modifier = Modifier.size(if (isSelected) 42.dp else 36.dp).clip(CircleShape).background(color).border(if (isSelected) 3.dp else 1.5.dp, if (isSelected) Color.White else Color.White.copy(alpha = 0.5f), CircleShape).clickable { onColorSelected(color) }, contentAlignment = Alignment.Center) {
                        if (isSelected) { Text("✓", color = if (color.luminance() > 0.5f) Color.Black else Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClockTypeSelector(selected: String, onSelect: (String) -> Unit) {
    val types = ClockType.values()
    val rows = types.toList().chunked(3)
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { rowTypes ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowTypes.forEach { clockType ->
                    val isSelected = selected == clockType.name
                    Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(if (isSelected) AccentBlue.copy(alpha = 0.2f) else SurfaceBg).border(1.1.dp, if (isSelected) AccentBlue else DividerColor, RoundedCornerShape(10.dp)).clickable { onSelect(clockType.name) }.padding(vertical = 10.dp, horizontal = 6.dp), contentAlignment = Alignment.Center) {
                        Text(text = clockType.displayName.uppercase(), color = if (isSelected) AccentBlue else TextPrimary, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, maxLines = 1)
                    }
                }
            }
        }
    }
}
