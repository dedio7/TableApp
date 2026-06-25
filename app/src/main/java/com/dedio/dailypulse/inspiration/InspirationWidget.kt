package com.dedio.dailypulse.inspiration

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
    isSmallHeight: Boolean = false,
) {
    val strings = LocalStrings.current
    val language = if (strings.settingsTitle == "Settings") "EN" else "IT"
    
    // Use the Repository for fetching (Online + Offline fallback)
    // Removed manualOffset to keep the quote fixed for the day as requested.
    val quote = MotivationRepository.rememberDailyQuote(language, 0)

    val quoteFontSize = if (isSmallHeight) 14.sp else 16.sp
    val authorFontSize = if (isSmallHeight) 10.sp else 12.sp
    val verticalPadding = if (isSmallHeight) 2.dp else 8.dp
    val spacing = if (isSmallHeight) 1.dp else 4.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = verticalPadding),
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

