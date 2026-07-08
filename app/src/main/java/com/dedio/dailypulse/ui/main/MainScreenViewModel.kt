package com.dedio.dailypulse.ui.main

import android.app.Application
import android.content.Context
import android.location.Geocoder
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dedio.dailypulse.background.Atmosphere
import com.dedio.dailypulse.settings.AppSettings
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.time.Duration.Companion.minutes

class MainScreenViewModel(
    application: Application,
    private val appSettings: AppSettings
) : AndroidViewModel(application) {

    private val context: Context get() = getApplication()

    // --- Settings StateFlows ---
    val atmosphereName = appSettings.atmosphereName.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "DEEP_SPACE")
    
    val atmosphere = atmosphereName.map { name ->
        try { Atmosphere.valueOf(name) } catch (_: Exception) { Atmosphere.DEEP_SPACE }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Atmosphere.DEEP_SPACE)

    val clockTypeName = appSettings.clockType.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "FLIP")
    val clockColorLong = appSettings.clockColor.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0xFFEEEEEE)
    val showSeconds = appSettings.showSeconds.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initialValue = true)
    val neonModeEnabled = appSettings.neonModeEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
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
    val discoveryEnabled = appSettings.discoveryEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val sunriseModeEnabled = appSettings.sunriseModeEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val nightModeStart = appSettings.nightModeStart.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 22)
    val nightModeEnd = appSettings.nightModeEnd.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 7)
    val nightBrightness = appSettings.nightModeBrightness.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.3f)

    // --- Burn-in Protection ---
    private val _burnInOffset = MutableStateFlow(Offset.Zero)
    val burnInOffset: StateFlow<Offset> = _burnInOffset.asStateFlow()

    // --- Night Shift Logic ---
    val isNightTime = combine(nightModeStart, nightModeEnd) { start, end ->
        val calendar = Calendar.getInstance()
        val hour = calendar[Calendar.HOUR_OF_DAY]
        if (start < end) {
            hour in (start until end)
        } else {
            hour !in (end until start)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val applyNightShift = combine(appSettings.nightShiftEnabled, isNightTime) { enabled, night ->
        enabled && night
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // --- Dynamic Location Tracking ---
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    init {
        // 1. Burn-in Protection Loop
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

        // 2. Periodic location refresh if GPS is enabled
        viewModelScope.launch {
            while (true) {
                refreshLocationIfGpsEnabled()
                delay(60.minutes)
            }
        }
    }

    @android.annotation.SuppressLint("MissingPermission")
    private fun refreshLocationIfGpsEnabled() {
        viewModelScope.launch {
            val useGps = appSettings.weatherUseGps.firstOrNull() ?: false
            if (!useGps) return@launch

            try {
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                    .addOnSuccessListener { location ->
                        location?.let {
                            updateLocationSettings(it.latitude, it.longitude)
                        }
                    }
            } catch (_: Exception) {
                // Silently ignore location errors
            }
        }
    }

    private fun updateLocationSettings(lat: Double, lon: Double) {
        viewModelScope.launch {
            // Reverse geocode to get city name
            val geocoder = Geocoder(context, java.util.Locale.getDefault())
            val addresses = try {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(lat, lon, 1)
            } catch (_: Exception) {
                null
            }

            val cityName = addresses?.firstOrNull()?.let { address ->
                address.locality ?: address.subAdminArea ?: address.adminArea ?: "Unknown"
            } ?: "Unknown"

            // Only update if significantly changed or city name updated
            val currentCity = appSettings.weatherCity.firstOrNull()
            if (cityName != "Unknown" && (cityName != currentCity)) {
                appSettings.setWeatherLocation(lat, lon, cityName)
            }
        }
    }

    fun setClockType(type: String) {
        viewModelScope.launch { appSettings.setClockType(type) }
    }
}
