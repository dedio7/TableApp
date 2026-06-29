package com.dedio.dailypulse.inspiration

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

class InspirationRepository {

    companion object {
        private const val TAG = "InspirationRepo"
        // Using a reliable public API that supports multiple languages or random quotes
        // We'll use ZenQuotes for English and a specialized Italian source or translation logic fallback
        private const val ZEN_QUOTES_URL = "https://zenquotes.io/api/today"
        // Specialized Italian Quote API (Open Source aggregator)
        private const val IT_QUOTES_URL = "https://pensieri.cloud/api/v1/random"
    }

    suspend fun fetchDailyQuote(language: String): Quote? {
        return withContext(Dispatchers.IO) {
            try {
                if (language == "IT") {
                    fetchItalianQuote()
                } else {
                    fetchEnglishQuote()
                }
            } catch (e: Exception) {
                Log.e(TAG, "General error fetching quote", e)
                null
            }
        }
    }

    private fun fetchItalianQuote(): Quote? {
        val response = performGetRequest(IT_QUOTES_URL) ?: return null
        return try {
            val json = JSONObject(response)
            val text = json.getString("testo")
            val author = json.optString("autore", "Anonimo")
            Quote(text, author)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing Italian quote", e)
            null
        }
    }

    private fun fetchEnglishQuote(): Quote? {
        val response = performGetRequest(ZEN_QUOTES_URL) ?: return null
        return try {
            val jsonArray = JSONArray(response)
            val firstObj = jsonArray.getJSONObject(0)
            val text = firstObj.getString("q")
            val author = firstObj.getString("a")
            Quote(text, author)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing English quote", e)
            null
        }
    }

    private fun performGetRequest(urlString: String): String? {
        var connection: HttpURLConnection? = null
        return try {
            val url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val result = StringBuilder()
                var line: String? = reader.readLine()
                while (line != null) {
                    result.append(line)
                    line = reader.readLine()
                }
                reader.close()
                result.toString()
            } else {
                Log.e(TAG, "HTTP Error: $responseCode")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Connection error", e)
            null
        } finally {
            connection?.disconnect()
        }
    }
}
