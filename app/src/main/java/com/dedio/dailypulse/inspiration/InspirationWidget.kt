package com.dedio.dailypulse.inspiration

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dedio.dailypulse.ui.i18n.LocalStrings
import java.util.*

@Composable
fun InspirationWidget(
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    isSmallHeight: Boolean = false
) {
    val strings = LocalStrings.current
    val language = if (strings.settingsTitle == "Settings") "EN" else "IT"
    
    val quote = remember(language) {
        getQuoteForToday(language)
    }

    val quoteFontSize = if (isSmallHeight) 14.sp else 18.sp
    val authorFontSize = if (isSmallHeight) 11.sp else 13.sp
    val verticalPadding = if (isSmallHeight) 4.dp else 16.dp
    val spacing = if (isSmallHeight) 2.dp else 8.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = verticalPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "“${quote.text}”",
            color = textColor.copy(alpha = 0.85f),
            fontSize = quoteFontSize,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Light,
            textAlign = TextAlign.Center,
            lineHeight = if (isSmallHeight) 18.sp else 26.sp
        )
        
        Spacer(modifier = Modifier.height(spacing))
        
        Text(
            text = "— ${quote.author}",
            color = textColor.copy(alpha = 0.5f),
            fontSize = authorFontSize,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center
        )
    }
}

data class Quote(val text: String, val author: String)

private fun getQuoteForToday(language: String): Quote {
    val quotes = if (language == "EN") ENGLISH_QUOTES else ITALIAN_QUOTES
    val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
    return quotes[dayOfYear % quotes.size]
}

private val ITALIAN_QUOTES = listOf(
    Quote("La vita è per il 10% cosa ti accade e per il 90% come reagisci.", "Charles R. Swindoll"),
    Quote("L'unico modo per fare un ottimo lavoro è amare quello che fai.", "Steve Jobs"),
    Quote("Non conta quanto vai piano, l'importante è che non ti fermi.", "Confucio"),
    Quote("Sia che tu pensi di farcela o di non farcela, avrai comunque ragione.", "Henry Ford"),
    Quote("Il segreto per andare avanti è iniziare.", "Mark Twain"),
    Quote("La felicità non è qualcosa di pronto. Viene dalle tue azioni.", "Dalai Lama"),
    Quote("Ogni cosa che puoi immaginare, la natura l'ha già creata.", "Albert Einstein"),
    Quote("Il miglior momento per piantare un albero era 20 anni fa. Il secondo miglior momento è ora.", "Proverbio Cinese"),
    Quote("Non smettere mai di imparare, perché la vita non smette mai di insegnare.", "Anonimo"),
    Quote("Sii il cambiamento che vuoi vedere nel mondo.", "Mahatma Gandhi")
)

private val ENGLISH_QUOTES = listOf(
    Quote("Life is 10% what happens to us and 90% how we react to it.", "Charles R. Swindoll"),
    Quote("The only way to do great work is to love what you do.", "Steve Jobs"),
    Quote("It does not matter how slowly you go as long as you do not stop.", "Confucius"),
    Quote("Whether you think you can or you think you can't, you're right.", "Henry Ford"),
    Quote("The secret of getting ahead is getting started.", "Mark Twain"),
    Quote("Happiness is not something ready made. It comes from your own actions.", "Dalai Lama"),
    Quote("Everything you can imagine, nature has already created.", "Albert Einstein"),
    Quote("The best time to plant a tree was 20 years ago. The second best time is now.", "Chinese Proverb"),
    Quote("Never stop learning, because life never stops teaching.", "Anonymous"),
    Quote("Be the change that you wish to see in the world.", "Mahatma Gandhi")
)
