package com.dedio.dailypulse.inspiration

import com.dedio.dailypulse.network.NetworkClient
import com.dedio.dailypulse.network.NetworkResult
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

class InspirationRepository() {

    companion object {
        private const val ZEN_QUOTES_URL = "https://zenquotes.io/api/random"
        private const val TRANSLATE_URL = "https://api.mymemory.translated.net/get"
    }

    suspend fun fetchQuote(language: String): Quote? {
        val result = NetworkClient.performGet(ZEN_QUOTES_URL)
        
        return when (result) {
            is NetworkResult.Success -> {
                try {
                    val array = JSONArray(result.data)
                    val obj = array.getJSONObject(0)
                    val englishText = obj.getString("q")
                    val author = obj.getString("a")
                    
                    if (language == "IT") {
                        // Translation is optional, if it fails we just return the English quote
                        val translatedText = translateText(englishText)
                        Quote(translatedText ?: englishText, author)
                    } else {
                        Quote(englishText, author)
                    }
                } catch (e: Exception) {
                    null
                }
            }
            is NetworkResult.Error -> {
                null
            }
        }
    }

    private suspend fun translateText(text: String): String? {
        return try {
            val encodedText = URLEncoder.encode(text, "UTF-8")
            val urlString = "$TRANSLATE_URL?q=$encodedText&langpair=en|it"
            val result = NetworkClient.performGet(urlString)
            
            if (result is NetworkResult.Success) {
                val json = JSONObject(result.data)
                val responseData = json.getJSONObject("responseData")
                responseData.getString("translatedText")
            } else null
        } catch (e: Exception) { 
            null // Silent failure for translation
        }
    }
}
