package com.example.dailypulse.settings

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dailypulse.background.PRESET_COLORS
import com.example.dailypulse.clock.ClockType
import com.example.dailypulse.news.DEFAULT_RSS_SOURCES
import com.example.dailypulse.ui.i18n.LocalStrings
import com.example.dailypulse.weather.WeatherLocation
import com.example.dailypulse.weather.WeatherRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val AccentBlue = Color(0xFF4FC3F7)
private val PanelBg = Color(0xF0101626)
private val SurfaceBg = Color(0xFF1A2240)
private val TextPrimary = Color(0xFFEEEEEE)
private val TextSecondary = Color(0xFF90A4AE)
private val DividerColor = Color(0xFF263254)

private val CLOCK_COLOR_PRESETS: List<Pair<String, Color>> = listOf(
    "Bianco" to Color(0xFFFFFFFF),
    "Arancione" to Color(0xFFFF9100),
    "Azzurro" to Color(0xFF00B0FF),
    "Verde" to Color(0xFF00E676),
    "Giallo" to Color(0xFFFFEA00),
    "Rosa" to Color(0xFFFF4081),
    "Viola" to Color(0xFFD500F9),
    "Rosso" to Color(0xFFFF1744),
    "Teal" to Color(0xFF1DE9B6),
    "Ambra" to Color(0xFFFFC400),
    "Indaco" to Color(0xFF651FFF),
    "Lime" to Color(0xFFC6FF00),
    "Ciano" to Color(0xFF00E5FF),
    "Deep Orange" to Color(0xFFFF3D00),
    "Grigio" to Color(0xFFCFD8DC),
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
    val bgPrimary by appSettings.bgPrimaryColor.collectAsStateWithLifecycle(initialValue = 0xFF0D1B2AL)
    val bgSecondary by appSettings.bgSecondaryColor.collectAsStateWithLifecycle(initialValue = 0xFF151528L)
    val bgUseGradient by appSettings.bgUseGradient.collectAsStateWithLifecycle(initialValue = false)
    val newsEnabled by appSettings.newsEnabled.collectAsStateWithLifecycle(initialValue = true)
    val newsRefreshMinutes by appSettings.newsRefreshMinutes.collectAsStateWithLifecycle(initialValue = 30)
    val dateFormat by appSettings.dateFormat.collectAsStateWithLifecycle(initialValue = "IT")
    val currentCity by appSettings.weatherCity.collectAsStateWithLifecycle(initialValue = "Roma")
    val binaryMode by appSettings.binaryClockMode.collectAsStateWithLifecycle(initialValue = "BINARY")
    val binaryTheme by appSettings.binaryClockTheme.collectAsStateWithLifecycle(initialValue = "DEFAULT")
    val nightShiftEnabled by appSettings.nightShiftEnabled.collectAsStateWithLifecycle(initialValue = false)
    val antiBurnInEnabled by appSettings.antiBurnInEnabled.collectAsStateWithLifecycle(initialValue = true)
    val enabledNewsSources by appSettings.newsSources.collectAsStateWithLifecycle(initialValue = emptySet())
    val appLanguage by appSettings.appLanguage.collectAsStateWithLifecycle(initialValue = "IT")

    val weatherRepo = remember { WeatherRepository() }
    var cityQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    val searchResults = remember { mutableStateListOf<WeatherLocation>() }

    LaunchedEffect(cityQuery) {
        if (cityQuery.length >= 2) {
            delay(600)
            isSearching = true
            val results = weatherRepo.searchCity(cityQuery)
            searchResults.clear()
            searchResults.addAll(results)
            isSearching = false
        } else {
            searchResults.clear()
        }
    }

    AnimatedVisibility(visible = visible, enter = androidx.compose.animation.fadeIn(), exit = androidx.compose.animation.fadeOut()) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.55f)).clickable { onDismiss() })
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(initialOffsetX = { it }, animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium)),
        exit = slideOutHorizontally(targetOffsetX = { it }, animationSpec = spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMedium))
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
                    Text(text = "⚙  ${strings.settingsTitle}", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) { Text("✕", color = TextSecondary, fontSize = 20.sp) }
                }
                HorizontalDivider(color = DividerColor, thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                SectionHeader(title = "🕐  ${strings.clockSection}")
                SettingLabel(label = strings.clockTypeLabel)
                ClockTypeSelector(selected = clockType, onSelect = { scope.launch { appSettings.setClockType(it) } })
                Spacer(modifier = Modifier.height(12.dp))

                SettingLabel(label = strings.clockColorLabel)
                ColorPicker(colors = CLOCK_COLOR_PRESETS, selectedColor = Color(clockColor), onColorSelected = { scope.launch { appSettings.setClockColor(it.toArgb().toLong() and 0xFFFFFFFFL) } })
                Spacer(modifier = Modifier.height(12.dp))

                SettingSwitch(label = strings.showSecondsLabel, checked = showSeconds, onCheckedChange = { scope.launch { appSettings.setShowSeconds(it) } })

                if (clockType == "BINARY") {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = strings.binaryClockSettings, color = AccentBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 20.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    SettingLabel(label = strings.displayMode)
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("BINARY" to (if(appLanguage == "IT") "Binario" else "Binary"), "BCD" to "BCD").forEach { (code, label) ->
                            val isSelected = binaryMode == code
                            Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(if (isSelected) AccentBlue.copy(alpha = 0.2f) else SurfaceBg).border(if (isSelected) 1.5.dp else 1.dp, if (isSelected) AccentBlue else DividerColor, RoundedCornerShape(8.dp)).clickable { scope.launch { appSettings.setBinaryClockMode(code) } }.padding(8.dp), contentAlignment = Alignment.Center) {
                                Text(text = label, color = if (isSelected) AccentBlue else TextSecondary, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    SettingLabel(label = strings.colorTheme)
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("DEFAULT" to (if(appLanguage == "IT") "Standard" else "Default"), "ACCENT" to "Neon").forEach { (code, label) ->
                            val isSelected = binaryTheme == code
                            Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(if (isSelected) AccentBlue.copy(alpha = 0.2f) else SurfaceBg).border(if (isSelected) 1.5.dp else 1.dp, if (isSelected) AccentBlue else DividerColor, RoundedCornerShape(8.dp)).clickable { scope.launch { appSettings.setBinaryClockTheme(code) } }.padding(8.dp), contentAlignment = Alignment.Center) {
                                Text(text = label, color = if (isSelected) AccentBlue else TextSecondary, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = DividerColor, thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                SectionHeader(title = "🎨  ${strings.backgroundSection}")
                SettingLabel(label = strings.primaryColorLabel)
                ColorPicker(colors = PRESET_COLORS, selectedColor = Color(bgPrimary), onColorSelected = { scope.launch { appSettings.setBackgroundColors(it.toArgb().toLong() and 0xFFFFFFFFL, bgSecondary, bgUseGradient) } })
                Spacer(modifier = Modifier.height(12.dp))
                SettingSwitch(label = strings.useGradientLabel, checked = bgUseGradient, onCheckedChange = { scope.launch { appSettings.setBackgroundColors(bgPrimary, bgSecondary, it) } })
                if (bgUseGradient) {
                    Spacer(modifier = Modifier.height(8.dp))
                    SettingLabel(label = strings.secondaryColorLabel)
                    ColorPicker(colors = PRESET_COLORS, selectedColor = Color(bgSecondary), onColorSelected = { scope.launch { appSettings.setBackgroundColors(bgPrimary, it.toArgb().toLong() and 0xFFFFFFFFL, bgUseGradient) } })
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = DividerColor, thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                SectionHeader(title = "📰  ${strings.newsSection}")
                SettingSwitch(label = strings.newsEnabledLabel, checked = newsEnabled, onCheckedChange = { scope.launch { appSettings.setNewsEnabled(it) } })
                if (newsEnabled) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        SettingLabel(label = strings.selectedSourcesLabel)
                    }
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = strings.selectAll, color = AccentBlue, fontSize = 11.sp, modifier = Modifier.clickable { scope.launch { appSettings.setNewsSources(DEFAULT_RSS_SOURCES.map { it.name }.toSet()) } })
                        Text(text = "|", color = DividerColor, fontSize = 11.sp)
                        Text(text = strings.deselectAll, color = TextSecondary, fontSize = 11.sp, modifier = Modifier.clickable { scope.launch { appSettings.setNewsSources(emptySet()) } })
                    }
                    FlowRow(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        DEFAULT_RSS_SOURCES.forEach { source ->
                            val isSelected = source.name in enabledNewsSources
                            Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(if (isSelected) AccentBlue.copy(alpha = 0.2f) else SurfaceBg).border(1.dp, if (isSelected) AccentBlue else DividerColor, RoundedCornerShape(8.dp)).clickable { scope.launch { val current = enabledNewsSources.toMutableSet(); if (isSelected) current.remove(source.name) else current.add(source.name); appSettings.setNewsSources(current) } }.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                Text(text = source.name, color = if (isSelected) AccentBlue else TextPrimary, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    SettingLabel(label = strings.newsRefreshLabel.format(newsRefreshMinutes))
                    var sliderValue by remember(newsRefreshMinutes) { mutableFloatStateOf(newsRefreshMinutes.toFloat()) }
                    Slider(value = sliderValue, onValueChange = { sliderValue = it }, onValueChangeFinished = { scope.launch { appSettings.setNewsRefreshMinutes(sliderValue.toInt()) } }, valueRange = 5f..120f, steps = 22, colors = SliderDefaults.colors(thumbColor = AccentBlue, activeTrackColor = AccentBlue, inactiveTrackColor = DividerColor), modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).clip(RoundedCornerShape(12.dp)).background(AccentBlue.copy(alpha = 0.15f)).border(1.dp, AccentBlue.copy(alpha = 0.4f), RoundedCornerShape(12.dp)).clickable { onNewsRefresh(); onDismiss() }.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Text("🔄", fontSize = 20.sp); Spacer(modifier = Modifier.width(10.dp)); Text(text = strings.newsRefreshButton, color = AccentBlue, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = DividerColor, thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                SectionHeader(title = "🌤  ${strings.weatherSection}")
                SettingLabel(label = strings.weatherCurrentCity.format(currentCity))
                OutlinedTextField(value = cityQuery, onValueChange = { cityQuery = it }, placeholder = { Text(strings.weatherSearchPlaceholder, color = TextSecondary, fontSize = 14.sp) }, singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedBorderColor = AccentBlue, unfocusedBorderColor = DividerColor, cursorColor = AccentBlue), modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp))
                if (isSearching) { Text(text = strings.weatherSearching, color = TextSecondary, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) }
                searchResults.forEach { loc ->
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 3.dp).clip(RoundedCornerShape(8.dp)).background(SurfaceBg).border(1.dp, DividerColor, RoundedCornerShape(8.dp)).clickable { scope.launch { appSettings.setWeatherLocation(loc.latitude, loc.longitude, loc.cityName); cityQuery = ""; searchResults.clear() } }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("📍", fontSize = 16.sp); Spacer(Modifier.width(8.dp)); Text(text = loc.cityName, color = TextPrimary, fontSize = 14.sp, modifier = Modifier.weight(1f))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = DividerColor, thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                SectionHeader(title = "🌍  ${strings.generalSection}")
                SettingLabel(label = strings.languageLabel)
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf("IT" to "Italiano 🇮🇹", "EN" to "English 🇬🇧").forEach { (code, label) ->
                        val isSelected = appLanguage == code
                        Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(if (isSelected) AccentBlue.copy(alpha = 0.2f) else SurfaceBg).border(if (isSelected) 1.5.dp else 1.dp, if (isSelected) AccentBlue else DividerColor, RoundedCornerShape(10.dp)).clickable { scope.launch { appSettings.setAppLanguage(code) } }.padding(12.dp), contentAlignment = Alignment.Center) {
                            Text(text = label, color = if (isSelected) AccentBlue else TextSecondary, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                SettingSwitch(label = strings.nightShiftLabel, checked = nightShiftEnabled, onCheckedChange = { scope.launch { appSettings.setNightShiftEnabled(it) } })
                SettingSwitch(label = strings.antiBurnInLabel, checked = antiBurnInEnabled, onCheckedChange = { scope.launch { appSettings.setAntiBurnInEnabled(it) } })
                Spacer(modifier = Modifier.height(12.dp))
                SettingLabel(label = strings.dateFormatLabel)
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf("IT" to "Italiano (gg/mm/aaaa)", "EN" to "English (mm/dd/yyyy)").forEach { (code, label) ->
                        val isSelected = dateFormat == code
                        Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(if (isSelected) AccentBlue.copy(alpha = 0.2f) else SurfaceBg).border(if (isSelected) 1.5.dp else 1.dp, if (isSelected) AccentBlue else DividerColor, RoundedCornerShape(10.dp)).clickable { scope.launch { appSettings.setDateFormat(code) } }.padding(12.dp), contentAlignment = Alignment.Center) {
                            Text(text = label, color = if (isSelected) AccentBlue else TextSecondary, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(text = title, color = AccentBlue, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp))
}

@Composable
private fun SettingLabel(label: String) {
    Text(text = label, color = TextSecondary, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp))
}

@Composable
private fun SettingSwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, color = TextPrimary, fontSize = 15.sp)
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
                    Box(modifier = Modifier.size(if (isSelected) 40.dp else 36.dp).clip(CircleShape).background(color).border(if (isSelected) 3.dp else 1.dp, if (isSelected) Color.White else Color.White.copy(alpha = 0.3f), CircleShape).clickable { onColorSelected(color) }, contentAlignment = Alignment.Center) {
                        if (isSelected) { Text("✓", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
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
                    Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(if (isSelected) AccentBlue.copy(alpha = 0.2f) else SurfaceBg).border(if (isSelected) 1.5.dp else 1.dp, if (isSelected) AccentBlue else DividerColor, RoundedCornerShape(10.dp)).clickable { onSelect(clockType.name) }.padding(vertical = 10.dp, horizontal = 6.dp), contentAlignment = Alignment.Center) {
                        Text(text = clockType.displayName, color = if (isSelected) AccentBlue else TextPrimary, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, maxLines = 1)
                    }
                }
            }
        }
    }
}
