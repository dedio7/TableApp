package com.dedio.dailypulse.weather

/**
 * Data class representing current weather conditions and forecasts.
 */
data class WeatherData(
    val temperature: Double,
    val weatherCode: Int,
    val humidity: Int,
    val windSpeed: Double,
    val feelsLike: Double,
    val description: String,
    val iconEmoji: String,
    val isDay: Boolean,
    val hourly: List<HourlyForecast> = emptyList(),
    val daily: List<DailyForecast> = emptyList()
)

/**
 * Forecast for a specific hour.
 */
data class HourlyForecast(
    val time: String, // format "HH:mm"
    val temperature: Double,
    val weatherCode: Int,
    val iconEmoji: String
)

/**
 * Forecast for a specific day.
 */
data class DailyForecast(
    val date: String, // format "dd/MM" or "EEE"
    val maxTemp: Double,
    val minTemp: Double,
    val weatherCode: Int,
    val iconEmoji: String
)

/**
 * Data class representing a geographic location for weather lookup.
 */
data class WeatherLocation(
    val latitude: Double,
    val longitude: Double,
    val cityName: String
)

/**
 * Maps WMO weather codes to descriptions and emoji icons based on language.
 *
 * @param code WMO weather interpretation code
 * @param isDay true if it's daytime, false for nighttime
 * @param language "IT" or "EN"
 * @return Pair of (Description, emoji icon)
 */
fun getWeatherDescription(code: Int, isDay: Boolean, language: String = "IT"): Pair<String, String> {
    val isEn = language == "EN"
    
    return when (code) {
        0 -> {
            if (isDay) Pair(if (isEn) "Clear sky" else "Sereno", "☀️")
            else Pair(if (isEn) "Clear sky" else "Sereno", "🌙")
        }
        1 -> {
            if (isDay) Pair(if (isEn) "Mainly clear" else "Prevalentemente sereno", "🌤️")
            else Pair(if (isEn) "Mainly clear" else "Prevalentemente sereno", "🌙")
        }
        2 -> {
            if (isDay) Pair(if (isEn) "Partly cloudy" else "Parzialmente nuvoloso", "⛅")
            else Pair(if (isEn) "Partly cloudy" else "Parzialmente nuvoloso", "☁️")
        }
        3 -> Pair(if (isEn) "Overcast" else "Coperto", "☁️")

        45 -> Pair(if (isEn) "Fog" else "Nebbia", "🌫️")
        48 -> Pair(if (isEn) "Depositing rime fog" else "Nebbia con brina", "🌫️")

        51 -> Pair(if (isEn) "Light drizzle" else "Pioviggine leggera", "🌦️")
        53 -> Pair(if (isEn) "Moderate drizzle" else "Pioviggine moderata", "🌦️")
        55 -> Pair(if (isEn) "Dense drizzle" else "Pioviggine intensa", "🌧️")
        56 -> Pair(if (isEn) "Light freezing drizzle" else "Pioviggine gelata leggera", "🌧️")
        57 -> Pair(if (isEn) "Dense freezing drizzle" else "Pioviggine gelata intensa", "🌧️")

        61 -> Pair(if (isEn) "Slight rain" else "Pioggia leggera", "🌦️")
        63 -> Pair(if (isEn) "Moderate rain" else "Pioggia moderata", "🌧️")
        65 -> Pair(if (isEn) "Heavy rain" else "Pioggia intensa", "🌧️")
        66 -> Pair(if (isEn) "Light freezing rain" else "Pioggia gelata leggera", "🌧️")
        67 -> Pair(if (isEn) "Heavy freezing rain" else "Pioggia gelata intensa", "🌧️")

        71 -> Pair(if (isEn) "Slight snow fall" else "Neve leggera", "🌨️")
        73 -> Pair(if (isEn) "Moderate snow fall" else "Neve moderata", "🌨️")
        75 -> Pair(if (isEn) "Heavy snow fall" else "Neve intensa", "❄️")
        77 -> Pair(if (isEn) "Snow grains" else "Granelli di neve", "❄️")

        80 -> Pair(if (isEn) "Slight rain showers" else "Rovesci leggeri", "🌦️")
        81 -> Pair(if (isEn) "Moderate rain showers" else "Rovesci moderati", "🌧️")
        82 -> Pair(if (isEn) "Violent rain showers" else "Rovesci violenti", "⛈️")

        85 -> Pair(if (isEn) "Slight snow showers" else "Neve a rovesci leggera", "🌨️")
        86 -> Pair(if (isEn) "Heavy snow showers" else "Neve a rovesci intensa", "❄️")

        95 -> Pair(if (isEn) "Thunderstorm" else "Temporale", "⛈️")
        96 -> Pair(if (isEn) "Thunderstorm with slight hail" else "Temporale con grandine leggera", "⛈️")
        99 -> Pair(if (isEn) "Thunderstorm with heavy hail" else "Temporale con grandine intensa", "⛈️")

        else -> Pair(if (isEn) "Unknown conditions" else "Condizioni sconosciute", "❓")
    }
}
