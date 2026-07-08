package com.dedio.dailypulse.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.URL

/**
 * Unified network client with a unique User-Agent and deep connectivity diagnostics.
 */
object NetworkClient {
    private const val TAG = "NetworkClient"
    private const val USER_AGENT = "DailyPulse-Android/1.3.2 (dedio7@gmail.com) Chrome/120.0.0.0"

    suspend fun performGet(urlString: String): NetworkResult {
        return withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                // Pre-flight check: Can we resolve any address?
                if (!isGoogleReachable()) {
                    return@withContext NetworkResult.Error("Device Internet Unreachable")
                }

                val url = URL(urlString)
                connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                connection.setRequestProperty("User-Agent", USER_AGENT)
                connection.setRequestProperty("Accept", "application/json")

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream, "UTF-8"))
                    val result = StringBuilder()
                    var line: String? = reader.readLine()
                    while (line != null) {
                        result.append(line)
                        line = reader.readLine()
                    }
                    reader.close()
                    NetworkResult.Success(result.toString())
                } else {
                    Log.e(TAG, "HTTP Error: $responseCode for $urlString")
                    NetworkResult.Error("HTTP $responseCode", responseCode)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Connection Exception: ${e.message}")
                NetworkResult.Error(e.message ?: "Network Failure")
            } finally {
                connection?.disconnect()
            }
        }
    }

    /**
     * Checks if Google DNS is reachable to distinguish between server error and device internet error.
     */
    private fun isGoogleReachable(): Boolean {
        return try {
            val timeoutMs = 2000
            val address = InetAddress.getByName("8.8.8.8")
            address.isReachable(timeoutMs)
        } catch (e: Exception) {
            false
        }
    }
}

sealed class NetworkResult {
    data class Success(val data: String) : NetworkResult()
    data class Error(val message: String, val code: Int? = null) : NetworkResult()
}
