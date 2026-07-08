package com.dedio.dailypulse.inspiration

import com.dedio.dailypulse.network.NetworkClient
import com.dedio.dailypulse.network.NetworkResult
import android.util.Log
import org.json.JSONObject
import java.util.Calendar

class DiscoveryRepository() {

    companion object {
        private const val TAG = "DiscoveryRepo"
        // Modern Apple RSS API v2 - Stable and High Quality
        private fun movieUrl(storefront: String) = "https://rss.applemarketingtools.com/api/v2/$storefront/movies/top-movies/20/movies.json"
        private fun albumUrl(storefront: String) = "https://rss.applemarketingtools.com/api/v2/$storefront/music/most-played/20/albums.json"
        // Correct TV Shows endpoint for API v2
        private fun tvUrl(storefront: String) = "https://rss.applemarketingtools.com/api/v2/$storefront/tv-shows/top-tv-seasons/20/tv-seasons.json"
    }

    suspend fun fetchDailyMedia(type: Int, language: String): MediaItem? {
        val storefront = if (language == "IT") "it" else "us"
        val urlString = when(type) {
            0 -> movieUrl(storefront)
            1 -> albumUrl(storefront)
            else -> tvUrl(storefront)
        }
        
        Log.d(TAG, "Fetching type $type from: $urlString")
        val result = NetworkClient.performGet(urlString)
        
        return when (result) {
            is NetworkResult.Success -> {
                try {
                    val root = JSONObject(result.data)
                    val feed = root.optJSONObject("feed") ?: run {
                        Log.e(TAG, "Missing 'feed' object in response")
                        return null
                    }
                    val results = feed.optJSONArray("results") ?: run {
                        Log.e(TAG, "Missing 'results' array in response")
                        return null
                    }
                    
                    if (results.length() == 0) {
                        Log.w(TAG, "Empty 'results' array for type $type")
                        return null
                    }

                    val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
                    val item = results.getJSONObject(dayOfYear % results.length())
                    
                    // API v2 keys are unified: "name", "artistName", "artworkUrl100"
                    val title = item.optString("name", "DailyPulse Selection")
                    val artist = item.optString("artistName", "Apple Discovery")
                    val artwork = item.optString("artworkUrl100", "")
                    
                    Log.d(TAG, "Found item: $title by $artist")

                    // Transform to high resolution (600x600)
                    val highResImage = if (artwork.isNotEmpty()) {
                        artwork.replace("100x100", "600x600")
                               .replace("200x200", "600x600")
                               .replace("bb.jpg", "600x600bb.jpg")
                    } else null
                    
                    MediaItem(
                        title = title,
                        info = artist,
                        imageUrl = highResImage,
                        wikiUrl = generateWikiUrl(title, language)
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Parsing error for type $type: ${e.message}")
                    null
                }
            }
            is NetworkResult.Error -> {
                Log.e(TAG, "Network error for type $type: ${result.message} (Code: ${result.code})")
                null
            }
        }
    }

    private fun generateWikiUrl(title: String, language: String): String {
        val wikiLang = if (language == "IT") "it" else "en"
        val cleanTitle = title
            .replace(Regex(",\\s*Stagione\\s*\\d+", RegexOption.IGNORE_CASE), "") // IT
            .replace(Regex(",\\s*Season\\s*\\d+", RegexOption.IGNORE_CASE), "")   // EN
            .replace(Regex(":\\s*Season\\s*\\d+", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\(.*?\\)"), "")
            .replace(Regex("[:,!\\-']"), "")
            .trim()
            .replace(" ", "_")

        return "https://$wikiLang.wikipedia.org/wiki/$cleanTitle"
    }
}

data class MediaItem(
    val title: String,
    val info: String,
    val imageUrl: String? = null,
    val wikiUrl: String? = null,
)
