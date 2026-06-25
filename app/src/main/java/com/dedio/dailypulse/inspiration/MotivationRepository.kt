package com.dedio.dailypulse.inspiration

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.util.Calendar

/**
 * Repository locale per Citazioni e Discovery.
 * Utilizza liste curate interne per garantire stabilità totale senza dipendere da API esterne.
 */
object MotivationRepository {

    @Composable
    fun rememberDailyQuote(language: String, offset: Int): Quote {
        val dayOfYear = Calendar.getInstance()[Calendar.DAY_OF_YEAR]
        return remember(language, dayOfYear, offset) {
            getStaticQuote(language, dayOfYear + offset)
        }
    }

    @Composable
    fun rememberDailyMedia(index: Int, language: String): MediaItem {
        val dayOfYear = Calendar.getInstance()[Calendar.DAY_OF_YEAR]
        val typeIndex = index % 3
        return remember(typeIndex, dayOfYear, language) {
            getStaticMedia(typeIndex, dayOfYear, language)
        }
    }

    private fun getStaticQuote(lang: String, index: Int): Quote {
        val quotes = if (lang == "IT") ITALIAN_QUOTES else ENGLISH_QUOTES
        return quotes[kotlin.math.abs(index) % quotes.size]
    }

    private fun getStaticMedia(typeIndex: Int, dayIndex: Int, lang: String): MediaItem {
        val list = when(typeIndex) {
            0 -> MOVIES
            1 -> ALBUMS
            else -> TV_SERIES
        }
        val item = list[kotlin.math.abs(dayIndex) % list.size]
        // Localizzazione URL Wikipedia se necessario
        val wikiLang = if (lang == "IT") "it" else "en"
        val localizedWiki = item.wikiUrl?.replace(Regex("https://(it|en)\\."), "https://$wikiLang.")
        return item.copy(wikiUrl = localizedWiki)
    }
}

data class MediaItem(
    val title: String, 
    val info: String, 
    val imageUrl: String? = null, 
    val wikiUrl: String? = null,
)

// --- Liste Curate (Dati Locali Offline) ---

private val ITALIAN_QUOTES = listOf(
    Quote("La semplicità è l'ultima sofisticazione.", "Leonardo da Vinci"),
    Quote("Il segreto per andare avanti è iniziare.", "Mark Twain"),
    Quote("Sii il cambiamento che vuoi vedere nel mondo.", "Mahatma Gandhi"),
    Quote("L'unica costante nella vita è il cambiamento.", "Eraclito"),
    Quote("Il viaggio di mille miglia inizia con un solo passo.", "Lao Tzu"),
    Quote("Non si può possedere ciò che non si comprende.", "Goethe"),
    Quote("La vita è quello che succede mentre sei impegnato a fare altri progetti.", "John Lennon"),
    Quote("Sbagli il 100% dei tiri che non fai mai.", "Wayne Gretzky"),
    Quote("La mente è come un paracadute. Funziona solo se si apre.", "Albert Einstein"),
    Quote("Vivi come se dovessi morire domani. Impara come se dovessi vivere per sempre.", "Mahatma Gandhi"),
)

private val ENGLISH_QUOTES = listOf(
    Quote("Simplicity is the ultimate sophistication.", "Leonardo da Vinci"),
    Quote("The best way to predict your future is to create it.", "Abraham Lincoln"),
    Quote("The only way to do great work is to love what you do.", "Steve Jobs"),
    Quote("Life is what happens while you're busy making other plans.", "John Lennon"),
    Quote("To be yourself in a world that is constantly trying to make you something else is the greatest accomplishment.", "Ralph Waldo Emerson"),
)

private val MOVIES = listOf(
    MediaItem("Interstellar", "Christopher Nolan, 2014", null, "https://it.wikipedia.org/wiki/Interstellar"),
    MediaItem("Inception", "Christopher Nolan, 2010", null, "https://it.wikipedia.org/wiki/Inception"),
    MediaItem("Il Gladiatore", "Ridley Scott, 2000", null, "https://it.wikipedia.org/wiki/Il_gladiatore"),
    MediaItem("Pulp Fiction", "Quentin Tarantino, 1994", null, "https://it.wikipedia.org/wiki/Pulp_Fiction"),
    MediaItem("The Dark Knight", "Christopher Nolan, 2008", null, "https://it.wikipedia.org/wiki/Il_cavaliere_oscuro"),
)

private val ALBUMS = listOf(
    MediaItem("The Dark Side of the Moon", "Pink Floyd, 1973", null, "https://it.wikipedia.org/wiki/The_Dark_Side_of_the_Moon"),
    MediaItem("Random Access Memories", "Daft Punk, 2013", null, "https://it.wikipedia.org/wiki/Random_Access_Memories"),
    MediaItem("Thriller", "Michael Jackson, 1982", null, "https://it.wikipedia.org/wiki/Thriller_(album_Michael_Jackson)"),
    MediaItem("Back to Black", "Amy Winehouse, 2006", null, "https://it.wikipedia.org/wiki/Back_to_Black"),
    MediaItem("Abbey Road", "The Beatles, 1969", null, "https://it.wikipedia.org/wiki/Abbey_Road"),
)

private val TV_SERIES = listOf(
    MediaItem("Breaking Bad", "Vince Gilligan, 2008", null, "https://it.wikipedia.org/wiki/Breaking_Bad"),
    MediaItem("Stranger Things", "The Duffer Brothers, 2016", null, "https://it.wikipedia.org/wiki/Stranger_Things"),
    MediaItem("The Crown", "Peter Morgan, 2016", null, "https://it.wikipedia.org/wiki/The_Crown_(serie_televisiva)"),
    MediaItem("Chernobyl", "Craig Mazin, 2019", null, "https://it.wikipedia.org/wiki/Chernobyl_(miniserie_televisiva)"),
    MediaItem("Better Call Saul", "Vince Gilligan, 2015", null, "https://it.wikipedia.org/wiki/Better_Call_Saul"),
)
