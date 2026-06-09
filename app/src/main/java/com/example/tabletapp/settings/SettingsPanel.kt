package com.example.tabletapp.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.example.tabletapp.background.PRESET_COLORS
import com.example.tabletapp.clock.ClockType
import com.example.tabletapp.weather.WeatherLocation
import com.example.tabletapp.weather.WeatherRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Accent color used across the settings panel
private val AccentBlue = Color(0xFF4FC3F7)
private val PanelBg = Color(0xF0101626)  // near-opaque dark navy
private val SurfaceBg = Color(0xFF1A2240)
private val TextPrimary = Color(0xFFEEEEEE)
private val TextSecondary = Color(0xFF90A4AE)
private val DividerColor = Color(0xFF263254)

/**
 * Clock color presets for the picker.
 */
private val CLOCK_COLOR_PRESETS: List<Pair<String, Color>> = listOf(
    "Bianco" to Color(0xFFEEEEEE),
    "Arancione" to Color(0xFFE8722A),
    "Azzurro" to Color(0xFF4FC3F7),
    "Verde" to Color(0xFF66BB6A),
    "Giallo" to Color(0xFFFFD54F),
    "Rosa" to Color(0xFFF48FB1),
    "Viola" to Color(0xFFCE93D8),
    "Rosso" to Color(0xFFEF5350),
)

/**
 * Full-screen settings overlay panel that slides in from the right.
 *
 * @param visible Whether the panel should be visible.
 * @param onDismiss Called when the user wants to close the panel.
 * @param onNewsRefresh Called when the user taps the manual news refresh button.
 */
@Composable
fun SettingsPanel(
    visible: Boolean,
    onDismiss: () -> Unit,
    onNewsRefresh: () -> Unit = {}
) {
    val context = LocalContext.current
    val appSettings = remember { AppSettings(context) }
    val scope = rememberCoroutineScope()

    // Collect current settings
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

    // Weather search state
    val weatherRepo = remember { WeatherRepository() }
    var cityQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    val searchResults = remember { mutableStateListOf<WeatherLocation>() }

    // Debounced search
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

    // Scrim: dim the background when panel is open
    AnimatedVisibility(
        visible = visible,
        enter = androidx.compose.animation.fadeIn(),
        exit = androidx.compose.animation.fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
                .clickable { onDismiss() }
        )
    }

    // Sliding panel from the right
    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(
            initialOffsetX = { fullWidth -> fullWidth },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ),
        exit = slideOutHorizontally(
            targetOffsetX = { fullWidth -> fullWidth },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.CenterEnd
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(400.dp)
                    .background(PanelBg)
                    .padding(vertical = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "⚙  Impostazioni",
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Text("✕", color = TextSecondary, fontSize = 20.sp)
                    }
                }

                HorizontalDivider(color = DividerColor, thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                // ── SEZIONE OROLOGIO ──────────────────────────────────────────
                SectionHeader(title = "🕐  Orologio")

                // Tipo di orologio
                SettingLabel(label = "Tipo di orologio")
                ClockTypeSelector(
                    selected = clockType,
                    onSelect = { type -> scope.launch { appSettings.setClockType(type) } }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Colore cifre orologio
                SettingLabel(label = "Colore cifre")
                ColorPicker(
                    colors = CLOCK_COLOR_PRESETS,
                    selectedColor = Color(clockColor),
                    onColorSelected = { color ->
                        scope.launch { appSettings.setClockColor(color.toArgb().toLong() and 0xFFFFFFFFL) }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Mostra secondi
                SettingSwitch(
                    label = "Mostra secondi",
                    checked = showSeconds,
                    onCheckedChange = { scope.launch { appSettings.setShowSeconds(it) } }
                )

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = DividerColor, thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                // ── SEZIONE SFONDO ────────────────────────────────────────────
                SectionHeader(title = "🎨  Sfondo")

                SettingLabel(label = "Colore primario")
                ColorPicker(
                    colors = PRESET_COLORS,
                    selectedColor = Color(bgPrimary),
                    onColorSelected = { color ->
                        scope.launch {
                            appSettings.setBackgroundColors(
                                primary = color.toArgb().toLong() and 0xFFFFFFFFL,
                                secondary = bgSecondary,
                                useGradient = bgUseGradient
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Gradient switch
                SettingSwitch(
                    label = "Usa gradiente animato",
                    checked = bgUseGradient,
                    onCheckedChange = { useGrad ->
                        scope.launch {
                            appSettings.setBackgroundColors(
                                primary = bgPrimary,
                                secondary = bgSecondary,
                                useGradient = useGrad
                            )
                        }
                    }
                )

                if (bgUseGradient) {
                    Spacer(modifier = Modifier.height(8.dp))
                    SettingLabel(label = "Colore secondario gradiente")
                    ColorPicker(
                        colors = PRESET_COLORS,
                        selectedColor = Color(bgSecondary),
                        onColorSelected = { color ->
                            scope.launch {
                                appSettings.setBackgroundColors(
                                    primary = bgPrimary,
                                    secondary = color.toArgb().toLong() and 0xFFFFFFFFL,
                                    useGradient = bgUseGradient
                                )
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = DividerColor, thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                // ── SEZIONE NOTIZIE ───────────────────────────────────────────
                SectionHeader(title = "📰  Notizie")

                SettingSwitch(
                    label = "Ticker notizie abilitato",
                    checked = newsEnabled,
                    onCheckedChange = { scope.launch { appSettings.setNewsEnabled(it) } }
                )

                if (newsEnabled) {
                    Spacer(modifier = Modifier.height(12.dp))
                    SettingLabel(label = "Aggiornamento automatico: ogni ${newsRefreshMinutes} minuti")
                    var sliderValue by remember(newsRefreshMinutes) {
                        mutableFloatStateOf(newsRefreshMinutes.toFloat())
                    }
                    Slider(
                        value = sliderValue,
                        onValueChange = { sliderValue = it },
                        onValueChangeFinished = {
                            scope.launch { appSettings.setNewsRefreshMinutes(sliderValue.toInt()) }
                        },
                        valueRange = 5f..120f,
                        steps = 22,
                        colors = SliderDefaults.colors(
                            thumbColor = AccentBlue,
                            activeTrackColor = AccentBlue,
                            inactiveTrackColor = DividerColor
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Pulsante aggiornamento manuale notizie
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(AccentBlue.copy(alpha = 0.15f))
                            .border(1.dp, AccentBlue.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .clickable {
                                onNewsRefresh()
                                onDismiss()
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("🔄", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Aggiorna notizie ora",
                            color = AccentBlue,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = DividerColor, thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                // ── SEZIONE METEO ─────────────────────────────────────────────
                SectionHeader(title = "🌤  Meteo")

                SettingLabel(label = "Città attuale: $currentCity")

                OutlinedTextField(
                    value = cityQuery,
                    onValueChange = { cityQuery = it },
                    placeholder = { Text("Cerca città…", color = TextSecondary, fontSize = 14.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = DividerColor,
                        cursorColor = AccentBlue
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                )

                if (isSearching) {
                    Text(
                        text = "Ricerca in corso…",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                }

                searchResults.forEach { loc ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 3.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceBg)
                            .border(1.dp, DividerColor, RoundedCornerShape(8.dp))
                            .clickable {
                                scope.launch {
                                    appSettings.setWeatherLocation(loc.latitude, loc.longitude, loc.cityName)
                                    cityQuery = ""
                                    searchResults.clear()
                                }
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📍", fontSize = 16.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = loc.cityName,
                            color = TextPrimary,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = DividerColor, thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                // ── SEZIONE GENERALE ──────────────────────────────────────────
                SectionHeader(title = "🌍  Generale")

                SettingLabel(label = "Formato data")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf("IT" to "Italiano (gg/mm/aaaa)", "EN" to "Inglese (mm/dd/yyyy)").forEach { (code, label) ->
                        val isSelected = dateFormat == code
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) AccentBlue.copy(alpha = 0.2f) else SurfaceBg)
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) AccentBlue else DividerColor,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { scope.launch { appSettings.setDateFormat(code) } }
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) AccentBlue else TextSecondary,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// ─── Helper composables ────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        color = AccentBlue,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
    )
}

@Composable
private fun SettingLabel(label: String) {
    Text(
        text = label,
        color = TextSecondary,
        fontSize = 13.sp,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
    )
}

@Composable
private fun SettingSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextPrimary, fontSize = 15.sp)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AccentBlue,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = DividerColor
            )
        )
    }
}

/**
 * Grid of color circles for selection.
 */
@Composable
private fun ColorPicker(
    colors: List<Pair<String, Color>>,
    selectedColor: Color,
    onColorSelected: (Color) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Show up to 5 per row, wrap manually
        val rows = colors.chunked(5)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            rows.forEach { rowColors ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    rowColors.forEach { (name, color) ->
                        val isSelected = selectedColor.toArgb() == color.toArgb()
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 40.dp else 36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.3f),
                                    shape = CircleShape
                                )
                                .clickable { onColorSelected(color) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Text("✓", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Chip-based selector for clock type.
 */
@Composable
private fun ClockTypeSelector(
    selected: String,
    onSelect: (String) -> Unit
) {
    val types = ClockType.values()
    val rows = types.toList().chunked(3)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        rows.forEach { rowTypes ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowTypes.forEach { clockType ->
                    val isSelected = selected == clockType.name
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) AccentBlue.copy(alpha = 0.2f) else SurfaceBg)
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) AccentBlue else DividerColor,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onSelect(clockType.name) }
                            .padding(vertical = 10.dp, horizontal = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = clockType.displayName,
                            color = if (isSelected) AccentBlue else TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
