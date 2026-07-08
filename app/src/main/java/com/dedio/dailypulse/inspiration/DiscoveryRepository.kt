package com.dedio.dailypulse.inspiration

import com.dedio.dailypulse.network.NetworkClient
import com.dedio.dailypulse.network.NetworkResult
import android.util.Log
import org.json.JSONObject
import java.util.Calendar

class DiscoveryRepository() {

    companion object {
        private const val TAG = "DiscoveryRepo"
        private fun movieUrl(storefront: String) = "https://itunes.apple.com/$storefront/rss/topmovies/limit=50/json"
        private fun albumUrl(storefront: String) = "https://itunes.apple.com/$storefront/rss/topalbums/limit=50/json"
        private fun tvUrl(storefront: String) = "https://itunes.apple.com/$storefront/rss/toptvseasons/limit=50/json"
    }

    suspend fun fetchDailyMedia(type: Int, language: String): MediaItem? {
        val storefront = if (language == "IT") "it" else "us"
        var urlString = when(type) {
            0 -> movieUrl(storefront)
            1 -> albumUrl(storefront)
            else -> tvUrl(storefront)
        }
        
        Log.d(TAG, "Fetching type $type from: $urlString")
        var result = NetworkClient.performGet(urlString)

        if (type == 2 && storefront == "it" && (result is NetworkResult.Success)) {
            val json = JSONObject(result.data)
            if (!json.getJSONObject("feed").has("entry")) {
                urlString = tvUrl("us")
                result = NetworkClient.performGet(urlString)
            }
        }
        
        return when (result) {
            is NetworkResult.Success -> {
                try {
                    val root = JSONObject(result.data)
                    val feed = root.getJSONObject("feed")
                    
                    if (!feed.has("entry")) return null
                    
                    val entries = feed.getJSONArray("entry")
                    if (entries.length() == 0) return null

                    val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
                    val item = entries.getJSONObject(dayOfYear % entries.length())
                    
                    val title = item.optJSONObject("im:name")?.optString("label") ?: "Unknown Title"
                    val artist = item.optJSONObject("im:artist")?.optString("label") ?: ""
                    val category = item.optJSONObject("category")?.optJSONObject("attributes")?.optString("label")
                    
                    val rawDate = item.optJSONObject("im:releaseDate")?.optString("label")
                    val year = if (rawDate != null && rawDate.length >= 4) rawDate.substring(0, 4) else null

                    val infoExtra = buildString {
                        if (year != null) append(year)
                        if (!category.isNullOrBlank()) {
                            if (isNotEmpty()) append(" • ")
                            append(category)
                        }
                    }
                    
                    val images = item.optJSONArray("im:image")
                    val artwork = if (images != null && images.length() > 0) {
                        images.getJSONObject(images.length() - 1).optString("label")
                    } else null
                    
                    val highResImage = artwork?.replace(Regex("/\\d+x\\d+"), "/600x600")
                    
                    MediaItem(
                        title = title,
                        artist = artist,
                        info = infoExtra,
                        imageUrl = highResImage,
                        wikiUrl = generateWikiUrl(title, artist, language, type)
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Parsing error: ${e.message}")
                    null
                }
            }
            is NetworkResult.Error -> null
        }
    }

    private fun generateWikiUrl(title: String, artist: String, language: String, type: Int): String {
        val wikiLang = if (language == "IT") "it" else "en"
        
        // Pulizia titoli per Wikipedia
        fun String.clean(): String = this
            .replace(Regex(",\\s*Stagione\\s*\\d+", RegexOption.IGNORE_CASE), "") 
            .replace(Regex(",\\s*Season\\s*\\d+", RegexOption.IGNORE_CASE), "")
            .replace(Regex(":\\s*Season\\s*\\d+", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\(.*?\\)"), "")
            .replace(Regex("[:,!\\-']"), "")
            .trim()
            .replace(" ", "_")

        val cleanTitle = title.clean()
        val cleanArtist = artist.clean()
        
        return when (type) {
            1 -> "https://$wikiLang.wikipedia.org/wiki/${cleanTitle}_(${cleanArtist}_album)"
            0 -> "https://$wikiLang.wikipedia.org/wiki/${cleanTitle}_(film)"
            else -> "https://$wikiLang.wikipedia.org/wiki/${cleanTitle}_(serie_televisiva)"
        }
    }
}

data class MediaItem(
    val title: String,
    val artist: String,
    val info: String, // Year/Category
    val imageUrl: String? = null,
    val wikiUrl: String? = null,
)
