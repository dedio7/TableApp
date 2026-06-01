package com.example.tabletapp.weather

/**
 * Data class representing current weather conditions.
 */
data class WeatherData(
    val temperature: Double,
    val weatherCode: Int,
    val humidity: Int,
    val windSpeed: Double,
    val feelsLike: Double,
    val description: String,
    val iconEmoji: String,
    val isDay: Boolean
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
 * Maps WMO weather codes to Italian descriptions and emoji icons.
 *
 * @param code WMO weather interpretation code
 * @param isDay true if it's daytime, false for nighttime
 * @return Pair of (Italian description, emoji icon)
 */
fun getWeatherDescription(code: Int, isDay: Boolean): Pair<String, String> {
    return when (code) {
        0 -> {
            if (isDay) Pair("Sereno", "☀️")
            else Pair("Sereno", "🌙")
        }
        1 -> {
            if (isDay) Pair("Prevalentemente sereno", "🌤️")
            else Pair("Prevalentemente sereno", "🌙")
        }
        2 -> {
            if (isDay) Pair("Parzialmente nuvoloso", "⛅")
            else Pair("Parzialmente nuvoloso", "☁️")
        }
        3 -> Pair("Coperto", "☁️")

        45 -> Pair("Nebbia", "🌫️")
        48 -> Pair("Nebbia con brina", "🌫️")

        51 -> Pair("Pioviggine leggera", "🌦️")
        53 -> Pair("Pioviggine moderata", "🌦️")
        55 -> Pair("Pioviggine intensa", "🌧️")
        56 -> Pair("Pioviggine gelata leggera", "🌧️")
        57 -> Pair("Pioviggine gelata intensa", "🌧️")

        61 -> Pair("Pioggia leggera", "🌦️")
        63 -> Pair("Pioggia moderata", "🌧️")
        65 -> Pair("Pioggia intensa", "🌧️")
        66 -> Pair("Pioggia gelata leggera", "🌧️")
        67 -> Pair("Pioggia gelata intensa", "🌧️")

        71 -> Pair("Neve leggera", "🌨️")
        73 -> Pair("Neve moderata", "🌨️")
        75 -> Pair("Neve intensa", "❄️")
        77 -> Pair("Granelli di neve", "❄️")

        80 -> Pair("Rovesci leggeri", "🌦️")
        81 -> Pair("Rovesci moderati", "🌧️")
        82 -> Pair("Rovesci violenti", "⛈️")

        85 -> Pair("Neve a rovesci leggera", "🌨️")
        86 -> Pair("Neve a rovesci intensa", "❄️")

        95 -> Pair("Temporale", "⛈️")
        96 -> Pair("Temporale con grandine leggera", "⛈️")
        99 -> Pair("Temporale con grandine intensa", "⛈️")

        else -> Pair("Condizioni sconosciute", "❓")
    }
}
