package com.example.tabletapp.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension property on Context for DataStore singleton
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tablet_app_settings")

/**
 * Centralized preferences manager using DataStore.
 * All app settings are stored and retrieved through this class.
 */
class AppSettings(private val context: Context) {

    companion object {
        // Clock settings
        val CLOCK_TYPE = stringPreferencesKey("clock_type")
        val CLOCK_COLOR = longPreferencesKey("clock_color")
        val SHOW_SECONDS = booleanPreferencesKey("show_seconds")
        val BINARY_CLOCK_MODE = stringPreferencesKey("binary_clock_mode") // "BINARY" or "BCD"
        val BINARY_CLOCK_THEME = stringPreferencesKey("binary_clock_theme") // "DEFAULT" or "ACCENT"

        // Weather settings
        val WEATHER_LATITUDE = doublePreferencesKey("weather_latitude")
        val WEATHER_LONGITUDE = doublePreferencesKey("weather_longitude")
        val WEATHER_CITY = stringPreferencesKey("weather_city")
        val WEATHER_USE_GPS = booleanPreferencesKey("weather_use_gps")

        // News settings
        val NEWS_ENABLED = booleanPreferencesKey("news_enabled")
        val NEWS_SOURCES = stringPreferencesKey("news_sources") // comma-separated URLs
        val NEWS_REFRESH_MINUTES = intPreferencesKey("news_refresh_minutes")

        // Background settings
        val BG_PRIMARY_COLOR = longPreferencesKey("bg_primary_color")
        val BG_SECONDARY_COLOR = longPreferencesKey("bg_secondary_color")
        val BG_USE_GRADIENT = booleanPreferencesKey("bg_use_gradient")

        // General settings
        val DATE_FORMAT = stringPreferencesKey("date_format") // "IT" or "EN"
        val BRIGHTNESS = intPreferencesKey("brightness")
        val NIGHT_SHIFT_ENABLED = booleanPreferencesKey("night_shift_enabled")
        val ANTI_BURN_IN_ENABLED = booleanPreferencesKey("anti_burn_in_enabled")
    }

    // --- Clock settings ---

    val clockType: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[CLOCK_TYPE] ?: "FLIP"
    }

    suspend fun setClockType(type: String) {
        context.dataStore.edit { prefs ->
            prefs[CLOCK_TYPE] = type
        }
    }

    val clockColor: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[CLOCK_COLOR] ?: 0xFFE8E8E8
    }

    suspend fun setClockColor(color: Long) {
        context.dataStore.edit { prefs ->
            prefs[CLOCK_COLOR] = color
        }
    }

    val showSeconds: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[SHOW_SECONDS] ?: true
    }

    suspend fun setShowSeconds(show: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[SHOW_SECONDS] = show
        }
    }

    val binaryClockMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[BINARY_CLOCK_MODE] ?: "BINARY"
    }

    suspend fun setBinaryClockMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[BINARY_CLOCK_MODE] = mode
        }
    }

    val binaryClockTheme: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[BINARY_CLOCK_THEME] ?: "DEFAULT"
    }

    suspend fun setBinaryClockTheme(theme: String) {
        context.dataStore.edit { prefs ->
            prefs[BINARY_CLOCK_THEME] = theme
        }
    }

    // --- Weather settings ---

    val weatherLatitude: Flow<Double> = context.dataStore.data.map { prefs ->
        prefs[WEATHER_LATITUDE] ?: 41.9028 // Rome default
    }

    val weatherLongitude: Flow<Double> = context.dataStore.data.map { prefs ->
        prefs[WEATHER_LONGITUDE] ?: 12.4964 // Rome default
    }

    val weatherCity: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[WEATHER_CITY] ?: "Roma"
    }

    val weatherUseGps: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[WEATHER_USE_GPS] ?: false
    }

    suspend fun setWeatherLocation(lat: Double, lon: Double, city: String) {
        context.dataStore.edit { prefs ->
            prefs[WEATHER_LATITUDE] = lat
            prefs[WEATHER_LONGITUDE] = lon
            prefs[WEATHER_CITY] = city
        }
    }

    suspend fun setWeatherUseGps(useGps: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[WEATHER_USE_GPS] = useGps
        }
    }

    // --- News settings ---

    val newsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[NEWS_ENABLED] ?: true
    }

    suspend fun setNewsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[NEWS_ENABLED] = enabled
        }
    }

    val newsRefreshMinutes: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[NEWS_REFRESH_MINUTES] ?: 30
    }

    suspend fun setNewsRefreshMinutes(minutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[NEWS_REFRESH_MINUTES] = minutes
        }
    }

    // --- Background settings ---

    val bgPrimaryColor: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[BG_PRIMARY_COLOR] ?: 0xFF0D0D0D
    }

    val bgSecondaryColor: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[BG_SECONDARY_COLOR] ?: 0xFF1A1A2E
    }

    val bgUseGradient: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[BG_USE_GRADIENT] ?: false
    }

    suspend fun setBackgroundColors(primary: Long, secondary: Long, useGradient: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[BG_PRIMARY_COLOR] = primary
            prefs[BG_SECONDARY_COLOR] = secondary
            prefs[BG_USE_GRADIENT] = useGradient
        }
    }

    // --- General settings ---

    val dateFormat: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[DATE_FORMAT] ?: "IT"
    }

    suspend fun setDateFormat(format: String) {
        context.dataStore.edit { prefs ->
            prefs[DATE_FORMAT] = format
        }
    }

    val nightShiftEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[NIGHT_SHIFT_ENABLED] ?: false
    }

    suspend fun setNightShiftEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[NIGHT_SHIFT_ENABLED] = enabled
        }
    }

    val antiBurnInEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[ANTI_BURN_IN_ENABLED] ?: true
    }

    suspend fun setAntiBurnInEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[ANTI_BURN_IN_ENABLED] = enabled
        }
    }
}
