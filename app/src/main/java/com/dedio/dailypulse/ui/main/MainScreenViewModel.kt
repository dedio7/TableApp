package com.dedio.dailypulse.ui.main

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dedio.dailypulse.settings.AppSettings
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.time.Duration.Companion.minutes

class MainScreenViewModel(private val appSettings: AppSettings) : ViewModel() {

    // --- Settings StateFlows ---
    val bgPrimary = appSettings.bgPrimaryColor.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0xFF0D0D0DL)
    val bgSecondary = appSettings.bgSecondaryColor.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0xFF1A1A2EL)
    val bgUseGradient = appSettings.bgUseGradient.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val clockTypeName = appSettings.clockType.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "FLIP")
    val clockColorLong = appSettings.clockColor.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0xFFEEEEEEE)
    val showSeconds = appSettings.showSeconds.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val binaryModeName = appSettings.binaryClockMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "BINARY")
    val binaryThemeName = appSettings.binaryClockTheme.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "DEFAULT")

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

    val dateFormat = appSettings.dateFormat.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "IT")
    val appLanguage = appSettings.appLanguage.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "IT")
    val nightShiftEnabled = appSettings.nightShiftEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val antiBurnInEnabled = appSettings.antiBurnInEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    
    val inspirationEnabled = appSettings.inspirationEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val sunriseModeEnabled = appSettings.sunriseModeEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // --- Burn-in Protection ---
    private val _burnInOffset = MutableStateFlow(Offset.Zero)
    val burnInOffset: StateFlow<Offset> = _burnInOffset.asStateFlow()

    init {
        viewModelScope.launch {
            antiBurnInEnabled.collectLatest { enabled ->
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
    val isNightTime = flow {
        while (true) {
            val calendar = Calendar.getInstance()
            val hour = calendar[Calendar.HOUR_OF_DAY]
            emit((hour in 22..23) || (hour in 0..6))
            delay(10.minutes)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val applyNightShift = combine(nightShiftEnabled, isNightTime) { enabled, night ->
        enabled && night
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setClockType(type: String) {
        viewModelScope.launch { appSettings.setClockType(type) }
    }
}
