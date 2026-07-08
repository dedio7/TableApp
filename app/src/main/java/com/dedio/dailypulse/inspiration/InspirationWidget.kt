package com.dedio.dailypulse.inspiration

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dedio.dailypulse.settings.AppSettings
import com.dedio.dailypulse.ui.i18n.LocalStrings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun InspirationWidget(
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    isSmallHeight: Boolean = false
) {
    val context = LocalContext.current
    val appSettings = remember { AppSettings(context) }
    val repository = remember { InspirationRepository() }
    val scope = rememberCoroutineScope()
    val strings = LocalStrings.current
    val language = if (strings.settingsTitle == "Settings") "EN" else "IT"
    
    var displayedQuote by remember { mutableStateOf<Quote?>(null) }
    var isFetching by remember { mutableStateOf(false) }

    val refreshQuote = suspend {
        isFetching = true
        val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val newQuote = repository.fetchQuote(language)
        
        if (newQuote != null) {
            appSettings.setLastQuote(newQuote.text, newQuote.author, language, today)
            displayedQuote = newQuote
        } else if (displayedQuote == null) {
            displayedQuote = Quote("La vita è ciò che accade mentre fai altri progetti.", "John Lennon")
        }
        isFetching = false
    }

    LaunchedEffect(language) {
        val savedText = appSettings.lastQuoteText.first()
        val savedAuthor = appSettings.lastQuoteAuthor.first()
        val savedLang = appSettings.lastQuoteLang.first()
        val lastDate = appSettings.lastQuoteDate.first()
        val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())

        if (savedText != null && savedAuthor != null && savedLang == language) {
            displayedQuote = Quote(savedText, savedAuthor)
            if (lastDate != today) refreshQuote()
        } else {
            refreshQuote()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable { scope.launch { refreshQuote() } }
    ) {
        Crossfade(targetState = displayedQuote, animationSpec = tween(1500), label = "quoteFade") { quote ->
            if (quote != null) {
                QuoteContent(quote, textColor, isSmallHeight)
            }
        }
    }
}

@Composable
private fun QuoteContent(
    quote: Quote,
    textColor: Color,
    isSmallHeight: Boolean
) {
    val quoteFontSize = if (isSmallHeight) 14.sp else 18.sp
    val authorFontSize = if (isSmallHeight) 11.sp else 13.sp

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = if (isSmallHeight) 4.dp else 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "“${quote.text}”", color = textColor.copy(alpha = 0.85f), fontSize = quoteFontSize, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Light, textAlign = TextAlign.Center, lineHeight = if (isSmallHeight) 18.sp else 26.sp)
        Spacer(modifier = Modifier.height(if (isSmallHeight) 2.dp else 8.dp))
        Text(text = "— ${quote.author}", color = textColor.copy(alpha = 0.5f), fontSize = authorFontSize, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, textAlign = TextAlign.Center)
    }
}

data class Quote(val text: String, val author: String)
