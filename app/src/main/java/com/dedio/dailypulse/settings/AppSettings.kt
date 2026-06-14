package com.dedio.dailypulse.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "daily_pulse_settings")

class AppSettings(private val context: Context) {

    companion object {
        val CLOCK_TYPE = stringPreferencesKey("clock_type")
        val CLOCK_COLOR = longPreferencesKey("clock_color")
        val SHOW_SECONDS = booleanPreferencesKey("show_seconds")
        val BINARY_CLOCK_MODE = stringPreferencesKey("binary_clock_mode")
        val BINARY_CLOCK_THEME = stringPreferencesKey("binary_clock_theme")

        val WEATHER_ENABLED = booleanPreferencesKey("weather_enabled")
        val WEATHER_LATITUDE = doublePreferencesKey("weather_latitude")
        val WEATHER_LONGITUDE = doublePreferencesKey("weather_longitude")
        val WEATHER_CITY = stringPreferencesKey("weather_city")
        val WEATHER_USE_GPS = booleanPreferencesKey("weather_use_gps")

        val NEWS_ENABLED = booleanPreferencesKey("news_enabled")
        val NEWS_REFRESH_MINUTES = intPreferencesKey("news_refresh_minutes")
        val NEWS_SOURCES_SET = stringSetPreferencesKey("news_sources_set")

        val BATTERY_ENABLED = booleanPreferencesKey("battery_enabled")
        val MEDIA_ENABLED = booleanPreferencesKey("media_enabled")
        val CALENDAR_ENABLED = booleanPreferencesKey("calendar_enabled")
        val TIMER_ENABLED = booleanPreferencesKey("timer_enabled")

        val ATMOSPHERE_NAME = stringPreferencesKey("atmosphere_name")

        val DATE_FORMAT = stringPreferencesKey("date_format")
        val APP_LANGUAGE = stringPreferencesKey("app_language")
        val NIGHT_SHIFT_ENABLED = booleanPreferencesKey("night_shift_enabled")
        val ANTI_BURN_IN_ENABLED = booleanPreferencesKey("anti_burn_in_enabled")
        
        val INSPIRATION_ENABLED = booleanPreferencesKey("inspiration_enabled")
        val SUNRISE_MODE_ENABLED = booleanPreferencesKey("sunrise_mode_enabled")

        val LAST_WEATHER_JSON = stringPreferencesKey("last_weather_json")
    }

    val clockType: Flow<String> = context.dataStore.data.map { it[CLOCK_TYPE] ?: "FLIP" }
    suspend fun setClockType(type: String) { context.dataStore.edit { it[CLOCK_TYPE] = type } }

    val clockColor: Flow<Long> = context.dataStore.data.map { it[CLOCK_COLOR] ?: 0xFFE8E8E8L }
    suspend fun setClockColor(color: Long) { context.dataStore.edit { it[CLOCK_COLOR] = color } }

    val showSeconds: Flow<Boolean> = context.dataStore.data.map { it[SHOW_SECONDS] ?: true }
    suspend fun setShowSeconds(show: Boolean) { context.dataStore.edit { it[SHOW_SECONDS] = show } }

    val binaryClockMode: Flow<String> = context.dataStore.data.map { it[BINARY_CLOCK_MODE] ?: "BINARY" }
    suspend fun setBinaryClockMode(mode: String) { context.dataStore.edit { it[BINARY_CLOCK_MODE] = mode } }

    val binaryClockTheme: Flow<String> = context.dataStore.data.map { it[BINARY_CLOCK_THEME] ?: "DEFAULT" }
    suspend fun setBinaryClockTheme(theme: String) { context.dataStore.edit { it[BINARY_CLOCK_THEME] = theme } }

    val weatherEnabled: Flow<Boolean> = context.dataStore.data.map { it[WEATHER_ENABLED] ?: true }
    suspend fun setWeatherEnabled(enabled: Boolean) { context.dataStore.edit { it[WEATHER_ENABLED] = enabled } }

    val weatherLatitude: Flow<Double> = context.dataStore.data.map { it[WEATHER_LATITUDE] ?: 41.9028 }
    val weatherLongitude: Flow<Double> = context.dataStore.data.map { it[WEATHER_LONGITUDE] ?: 12.4964 }
    val weatherCity: Flow<String> = context.dataStore.data.map { it[WEATHER_CITY] ?: "Roma" }
    suspend fun setWeatherLocation(lat: Double, lon: Double, city: String) {
        context.dataStore.edit { it[WEATHER_LATITUDE] = lat; it[WEATHER_LONGITUDE] = lon; it[WEATHER_CITY] = city }
    }

    val newsEnabled: Flow<Boolean> = context.dataStore.data.map { it[NEWS_ENABLED] ?: true }
    suspend fun setNewsEnabled(enabled: Boolean) { context.dataStore.edit { it[NEWS_ENABLED] = enabled } }

    val newsRefreshMinutes: Flow<Int> = context.dataStore.data.map { it[NEWS_REFRESH_MINUTES] ?: 30 }
    suspend fun setNewsRefreshMinutes(min: Int) { context.dataStore.edit { it[NEWS_REFRESH_MINUTES] = min } }

    val newsSources: Flow<Set<String>> = context.dataStore.data.map { it[NEWS_SOURCES_SET] ?: emptySet() }
    suspend fun setNewsSources(sources: Set<String>) { context.dataStore.edit { it[NEWS_SOURCES_SET] = sources } }

    val batteryEnabled: Flow<Boolean> = context.dataStore.data.map { it[BATTERY_ENABLED] ?: true }
    suspend fun setBatteryEnabled(enabled: Boolean) { context.dataStore.edit { it[BATTERY_ENABLED] = enabled } }

    val mediaEnabled: Flow<Boolean> = context.dataStore.data.map { it[MEDIA_ENABLED] ?: true }
    suspend fun setMediaEnabled(enabled: Boolean) { context.dataStore.edit { it[MEDIA_ENABLED] = enabled } }

    val calendarEnabled: Flow<Boolean> = context.dataStore.data.map { it[CALENDAR_ENABLED] ?: true }
    suspend fun setCalendarEnabled(enabled: Boolean) { context.dataStore.edit { it[CALENDAR_ENABLED] = enabled } }

    val timerEnabled: Flow<Boolean> = context.dataStore.data.map { it[TIMER_ENABLED] ?: true }
    suspend fun setTimerEnabled(enabled: Boolean) { context.dataStore.edit { it[TIMER_ENABLED] = enabled } }

    val atmosphereName: Flow<String> = context.dataStore.data.map { it[ATMOSPHERE_NAME] ?: "DEEP_SPACE" }
    suspend fun setAtmosphereName(name: String) { context.dataStore.edit { it[ATMOSPHERE_NAME] = name } }

    val dateFormat: Flow<String> = context.dataStore.data.map { it[DATE_FORMAT] ?: "IT" }
    suspend fun setDateFormat(f: String) { context.dataStore.edit { it[DATE_FORMAT] = f } }

    val appLanguage: Flow<String> = context.dataStore.data.map { it[APP_LANGUAGE] ?: "IT" }
    suspend fun setAppLanguage(lang: String) { context.dataStore.edit { it[APP_LANGUAGE] = lang } }

    val nightShiftEnabled: Flow<Boolean> = context.dataStore.data.map { it[NIGHT_SHIFT_ENABLED] ?: false }
    suspend fun setNightShiftEnabled(e: Boolean) { context.dataStore.edit { it[NIGHT_SHIFT_ENABLED] = e } }

    val antiBurnInEnabled: Flow<Boolean> = context.dataStore.data.map { it[ANTI_BURN_IN_ENABLED] ?: true }
    suspend fun setAntiBurnInEnabled(e: Boolean) { context.dataStore.edit { it[ANTI_BURN_IN_ENABLED] = e } }
    
    val inspirationEnabled: Flow<Boolean> = context.dataStore.data.map { it[INSPIRATION_ENABLED] ?: false }
    suspend fun setInspirationEnabled(enabled: Boolean) { context.dataStore.edit { it[INSPIRATION_ENABLED] = enabled } }

    val sunriseModeEnabled: Flow<Boolean> = context.dataStore.data.map { it[SUNRISE_MODE_ENABLED] ?: false }
    suspend fun setSunriseModeEnabled(enabled: Boolean) { context.dataStore.edit { it[SUNRISE_MODE_ENABLED] = enabled } }

    val lastWeatherJson: Flow<String?> = context.dataStore.data.map { it[LAST_WEATHER_JSON] }
    suspend fun setLastWeatherJson(json: String) { context.dataStore.edit { it[LAST_WEATHER_JSON] = json } }
}
