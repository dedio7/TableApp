package com.dedio.dailypulse.ui.main

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dedio.dailypulse.background.Atmosphere
import com.dedio.dailypulse.settings.AppSettings
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.time.Duration.Companion.minutes

class MainScreenViewModel(private val appSettings: AppSettings) : ViewModel() {

    // --- Settings StateFlows ---
    val atmosphereName = appSettings.atmosphereName.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "DEEP_SPACE")
    
    val atmosphere = atmosphereName.map { name ->
        try { Atmosphere.valueOf(name) } catch (_: Exception) { Atmosphere.DEEP_SPACE }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Atmosphere.DEEP_SPACE)

    val clockTypeName = appSettings.clockType.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "FLIP")
    val clockColorLong = appSettings.clockColor.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0xFFEEEEEEE)
    val showSeconds = appSettings.showSeconds.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initialValue = true)
    val neonModeEnabled = appSettings.neonModeEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initialValue = false)
    val binaryModeName = appSettings.binaryClockMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "BINARY")

    val newsEnabled = appSettings.newsEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val newsRefreshMinutes = appSettings.newsRefreshMinutes.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 30)
    val newsSources = appSettings.newsSources.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val weatherEnabled = appSettings.weatherEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val weatherLat = appSettings.weatherLatitude.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 41.9028)
    val weatherLon = appSettings.weatherLongitude.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 12.4964)
    val weatherCity = appSettings.weatherCity.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Roma")

    val batteryEnabled = appSettings.batteryEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val mediaEnabled = appSettings.mediaEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val calendarEnabled = appSettings.calendarEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val timerEnabled = appSettings.timerEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val dateFormat = appSettings.dateFormat.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "IT")
    val appLanguage = appSettings.appLanguage.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "IT")
    val antiBurnInEnabled = appSettings.antiBurnInEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    
    val inspirationEnabled = appSettings.inspirationEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val sunriseModeEnabled = appSettings.sunriseModeEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val nightModeStart = appSettings.nightModeStart.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 22)
    val nightModeEnd = appSettings.nightModeEnd.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 7)
    val nightBrightness = appSettings.nightModeBrightness.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.3f)

    // --- Burn-in Protection ---
    private val _burnInOffset = MutableStateFlow(Offset.Zero)
    val burnInOffset: StateFlow<Offset> = _burnInOffset.asStateFlow()

    init {
        viewModelScope.launch {
            appSettings.antiBurnInEnabled.collectLatest { enabled ->
                if (enabled) {
                    while (true) {
                        delay(1.minutes)
                        _burnInOffset.value = Offset(
                            x = (-3..3).random().toFloat(),
                            y = (-3..3).random().toFloat(),
                        )
                    }
                } else {
                    _burnInOffset.value = Offset.Zero
                }
            }
        }
    }

    // --- Night Shift ---
    val isNightTime = combine(nightModeStart, nightModeEnd) { start, end ->
        val calendar = Calendar.getInstance()
        val hour = calendar[Calendar.HOUR_OF_DAY]
        if (start < end) {
            hour in (start until end)
        } else {
            hour !in (end until start)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initialValue = false)

    val applyNightShift = combine(appSettings.nightShiftEnabled, isNightTime) { enabled, night ->
        enabled && night
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setClockType(type: String) {
        viewModelScope.launch { appSettings.setClockType(type) }
    }
}
