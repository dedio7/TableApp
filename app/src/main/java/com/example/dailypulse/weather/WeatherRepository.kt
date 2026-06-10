package com.example.dailypulse.weather

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Repository for fetching weather data from the Open-Meteo API.
 * Uses only standard Android SDK classes (no external libraries).
 */
class WeatherRepository {

    companion object {
        private const val TAG = "WeatherRepository"
        private const val WEATHER_BASE_URL = "https://api.open-meteo.com/v1/forecast"
        private const val GEOCODING_BASE_URL = "https://geocoding-api.open-meteo.com/v1/search"
        private const val CONNECT_TIMEOUT_MS = 10_000
        private const val READ_TIMEOUT_MS = 15_000
    }

    /**
     * Fetches current weather data for the given coordinates.
     *
     * @param latitude Geographic latitude
     * @param longitude Geographic longitude
     * @return WeatherData if successful, null on failure
     */
    suspend fun fetchWeather(latitude: Double, longitude: Double): WeatherData? {
        return withContext(Dispatchers.IO) {
            try {
                val urlString = buildString {
                    append(WEATHER_BASE_URL)
                    append("?latitude=").append(latitude)
                    append("&longitude=").append(longitude)
                    append("&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m,apparent_temperature,is_day")
                    append("&timezone=auto")
                }

                val responseBody = performGetRequest(urlString)
                if (responseBody == null) {
                    Log.e(TAG, "Empty response from weather API")
                    return@withContext null
                }

                parseWeatherResponse(responseBody)
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching weather data", e)
                null
            }
        }
    }

    /**
     * Searches for cities matching the given query string.
     *
     * @param query City name to search for
     * @return List of matching WeatherLocation entries
     */
    suspend fun searchCity(query: String): List<WeatherLocation> {
        return withContext(Dispatchers.IO) {
            try {
                val encodedQuery = URLEncoder.encode(query, "UTF-8")
                val urlString = buildString {
                    append(GEOCODING_BASE_URL)
                    append("?name=").append(encodedQuery)
                    append("&count=5")
                    append("&language=it")
                }

                val responseBody = performGetRequest(urlString)
                if (responseBody == null) {
                    Log.e(TAG, "Empty response from geocoding API")
                    return@withContext emptyList()
                }

                parseGeocodingResponse(responseBody)
            } catch (e: Exception) {
                Log.e(TAG, "Error searching city", e)
                emptyList()
            }
        }
    }

    /**
     * Performs an HTTP GET request and returns the response body as a String.
     */
    private fun performGetRequest(urlString: String): String? {
        var connection: HttpURLConnection? = null
        try {
            val url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = CONNECT_TIMEOUT_MS
            connection.readTimeout = READ_TIMEOUT_MS
            connection.setRequestProperty("Accept", "application/json")

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                Log.e(TAG, "HTTP error: $responseCode for URL: $urlString")
                return null
            }

            val reader = BufferedReader(InputStreamReader(connection.inputStream, "UTF-8"))
            val response = StringBuilder()
            var line: String? = reader.readLine()
            while (line != null) {
                response.append(line)
                line = reader.readLine()
            }
            reader.close()

            return response.toString()
        } catch (e: Exception) {
            Log.e(TAG, "Network request failed for URL: $urlString", e)
            return null
        } finally {
            connection?.disconnect()
        }
    }

    /**
     * Parses the Open-Meteo weather API JSON response into a WeatherData object.
     */
    private fun parseWeatherResponse(jsonString: String): WeatherData? {
        return try {
            val json = JSONObject(jsonString)
            val current = json.getJSONObject("current")

            val temperature = current.getDouble("temperature_2m")
            val humidity = current.getInt("relative_humidity_2m")
            val weatherCode = current.getInt("weather_code")
            val windSpeed = current.getDouble("wind_speed_10m")
            val feelsLike = current.getDouble("apparent_temperature")
            val isDay = current.getInt("is_day") == 1

            val (description, iconEmoji) = getWeatherDescription(weatherCode, isDay)

            WeatherData(
                temperature = temperature,
                weatherCode = weatherCode,
                humidity = humidity,
                windSpeed = windSpeed,
                feelsLike = feelsLike,
                description = description,
                iconEmoji = iconEmoji,
                isDay = isDay
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing weather JSON response", e)
            null
        }
    }

    /**
     * Parses the Open-Meteo geocoding API JSON response into a list of WeatherLocation.
     */
    private fun parseGeocodingResponse(jsonString: String): List<WeatherLocation> {
        return try {
            val json = JSONObject(jsonString)

            if (!json.has("results")) {
                return emptyList()
            }

            val results = json.getJSONArray("results")
            val locations = mutableListOf<WeatherLocation>()

            for (i in 0 until results.length()) {
                val item = results.getJSONObject(i)
                val latitude = item.getDouble("latitude")
                val longitude = item.getDouble("longitude")

                // Build a descriptive city name with admin area and country
                val name = item.optString("name", "")
                val admin1 = item.optString("admin1", "")
                val country = item.optString("country", "")

                val cityName = buildString {
                    append(name)
                    if (admin1.isNotEmpty() && admin1 != name) {
                        append(", ").append(admin1)
                    }
                    if (country.isNotEmpty()) {
                        append(", ").append(country)
                    }
                }

                locations.add(
                    WeatherLocation(
                        latitude = latitude,
                        longitude = longitude,
                        cityName = cityName
                    )
                )
            }

            locations
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing geocoding JSON response", e)
            emptyList()
        }
    }
}
