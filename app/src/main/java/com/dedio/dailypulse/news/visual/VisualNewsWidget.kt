package com.dedio.dailypulse.news.visual

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.dedio.dailypulse.network.NetworkClient
import com.dedio.dailypulse.network.NetworkResult
import kotlinx.coroutines.delay
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import kotlin.time.Duration.Companion.minutes

@Composable
fun VisualNewsWidget(
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    language: String = "IT"
) {
    var newsItems by remember { mutableStateOf<List<VisualNews>>(emptyList()) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var detailsOpen by remember { mutableStateOf(false) }
    
    LaunchedEffect(language) {
        while(true) {
            val items = fetchLatestVisualNews(language)
            if (items.isNotEmpty()) {
                newsItems = items
                currentIndex = 0
            }
            delay(15.minutes)
        }
    }

    // Auto-advance Carousel every 8 seconds
    LaunchedEffect(newsItems) {
        if (newsItems.isNotEmpty()) {
            while (true) {
                delay(8000L)
                currentIndex = (currentIndex + 1) % newsItems.size
            }
        }
    }

    val currentItem = newsItems.getOrNull(currentIndex)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.Black.copy(alpha = 0.25f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
            .clickable(enabled = currentItem != null) { detailsOpen = true }
    ) {
        Crossfade(targetState = currentItem, label = "newsFade") { item ->
            if (item != null) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (item.imageUrl != null) {
                        AsyncImage(
                            model = item.imageUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().customAlpha(0.55f)
                        )
                        // Dark premium gradient overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.6f),
                                            Color.Black.copy(alpha = 0.9f)
                                        ),
                                        startY = 0f
                                    )
                                )
                        )
                    } else {
                        // Premium abstract gradient fallback
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF0F172A),
                                            Color(0xFF1E1B4B),
                                            Color(0xFF311042)
                                        )
                                    )
                                )
                        )
                    }
                    
                    // Main Content Overlay
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                            .padding(end = 60.dp) // Leave space for dots
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFFF5252).copy(alpha = 0.15f))
                                    .border(1.dp, Color(0xFFFF5252).copy(alpha = 0.8f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = (if (language == "IT") "ULTIM'ORA" else "BREAKING").uppercase(),
                                    color = Color(0xFFFF5252),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                            }
                            Text(
                                text = item.source.uppercase(),
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "•",
                                color = Color.White.copy(alpha = 0.3f),
                                fontSize = 10.sp
                            )
                            Text(
                                text = getRelativeTimeAgo(item.pubDate, language),
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = item.title,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 20.sp
                        )
                    }

                    // Pager/Carousel Indicators (Dots)
                    if (newsItems.size > 1) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(bottom = 16.dp, end = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            newsItems.forEachIndexed { index, _ ->
                                val active = index == currentIndex
                                Box(
                                    modifier = Modifier
                                        .size(if (active) 6.dp else 4.dp)
                                        .clip(CircleShape)
                                        .background(color = if (active) Color.White else Color.White.copy(alpha = 0.3f))
                                )
                            }
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (language == "IT") "Caricamento notizie..." else "Loading news...",
                        color = textColor.copy(alpha = 0.3f),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }

    currentItem?.let {
        VisualNewsDetailsPanel(
            visible = detailsOpen,
            item = it,
            language = language,
            onDismiss = { detailsOpen = false }
        )
    }
}

private suspend fun fetchLatestVisualNews(language: String): List<VisualNews> {
    val sources = if (language == "IT") {
        listOf(
            "SkyTG24" to "https://tg24.sky.it/rss/tg24_mondo.xml",
            "ANSA" to "https://www.ansa.it/sito/ansait_rss.xml"
        )
    } else {
        listOf(
            "BBC News" to "https://feeds.bbci.co.uk/news/world/rss.xml",
            "CNN" to "http://rss.cnn.com/rss/edition.rss"
        )
    }

    val combinedNews = mutableListOf<VisualNews>()
    for ((name, url) in sources) {
        try {
            val result = NetworkClient.performGet(url)
            if (result is NetworkResult.Success) {
                val parsed = parseVisualNewsRss(result.data, name)
                combinedNews.addAll(parsed)
            }
        } catch (_: Exception) {}
    }
    
    // Sort by simple heuristic (source sequence interleaving) and limit to 8
    return combinedNews.take(8)
}

private fun parseVisualNewsRss(xmlData: String, defaultSource: String): List<VisualNews> {
    val items = mutableListOf<VisualNews>()
    try {
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = false
        val parser = factory.newPullParser()
        parser.setInput(StringReader(xmlData))

        var insideItem = false
        var title = ""
        var description = ""
        var link = ""
        var pubDate = ""
        var imageUrl: String? = null
        var currentTag = ""

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    currentTag = parser.name ?: ""
                    if (currentTag.equals("item", ignoreCase = true)) {
                        insideItem = true
                        title = ""
                        description = ""
                        link = ""
                        pubDate = ""
                        imageUrl = null
                    } else if (insideItem) {
                        if (currentTag.equals("enclosure", ignoreCase = true)) {
                            val type = parser.getAttributeValue(null, "type") ?: ""
                            if (type.startsWith("image/", ignoreCase = true) || type.isEmpty()) {
                                val urlAttr = parser.getAttributeValue(null, "url")
                                if (!urlAttr.isNullOrEmpty()) imageUrl = urlAttr
                            }
                        } else if (currentTag.equals("media:content", ignoreCase = true) || 
                                   currentTag.equals("media:thumbnail", ignoreCase = true)) {
                            val urlAttr = parser.getAttributeValue(null, "url")
                            if (!urlAttr.isNullOrEmpty()) imageUrl = urlAttr
                        }
                    }
                }
                XmlPullParser.TEXT -> {
                    if (insideItem) {
                        val text = parser.text ?: ""
                        when {
                            currentTag.equals("title", ignoreCase = true) -> title += text
                            currentTag.equals("description", ignoreCase = true) -> description += text
                            currentTag.equals("link", ignoreCase = true) -> link += text
                            currentTag.equals("pubDate", ignoreCase = true) -> pubDate += text
                            currentTag.equals("thumb", ignoreCase = true) || 
                            currentTag.equals("thumb_intermedia", ignoreCase = true) -> {
                                if (imageUrl == null && text.trim().isNotEmpty()) {
                                    imageUrl = text.trim()
                                }
                            }
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    val endTag = parser.name ?: ""
                    if (endTag.equals("item", ignoreCase = true) && insideItem) {
                        insideItem = false
                        val cleanTitle = stripHtml(title).trim()
                        val cleanDesc = stripHtml(description).trim()
                        
                        // Extract image from HTML description if tags didn't provide one
                        var finalImgUrl = imageUrl
                        if (finalImgUrl.isNullOrEmpty()) {
                            finalImgUrl = extractImageFromHtml(description)
                        }
                        
                        if (cleanTitle.isNotEmpty() && cleanTitle.length > 5) {
                            items.add(
                                VisualNews(
                                    title = cleanTitle,
                                    description = cleanDesc,
                                    imageUrl = finalImgUrl,
                                    pubDate = pubDate.trim(),
                                    source = defaultSource,
                                    link = link.trim()
                                )
                            )
                        }
                    }
                    currentTag = ""
                }
            }
            eventType = parser.next()
        }
    } catch (e: Exception) {
        android.util.Log.e("VisualNewsParser", "Error parsing feed", e)
    }
    return items
}

private fun extractImageFromHtml(html: String): String? {
    try {
        val regex = Regex("""<img[^>]+src=["'](https?://[^"']+)["']""", RegexOption.IGNORE_CASE)
        val match = regex.find(html)
        if (match != null) {
            return match.groupValues[1]
        }
    } catch (_: Exception) {}
    return null
}

private fun stripHtml(html: String): String {
    var cleaned = html
    cleaned = Regex("<!\\[CDATA\\[|]]>").replace(cleaned, "")
    cleaned = Regex("<[^>]*>").replace(cleaned, "")
    cleaned = cleaned
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&#039;", "'")
        .replace("&apos;", "'")
        .replace("&nbsp;", " ")
    return cleaned.replace(Regex("\\s+"), " ")
}

private fun getRelativeTimeAgo(pubDateString: String, language: String): String {
    if (pubDateString.isEmpty()) return ""
    val dateFormats = listOf(
        java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", java.util.Locale.ENGLISH),
        java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", java.util.Locale.ENGLISH),
        java.text.SimpleDateFormat("dd MMM yyyy HH:mm:ss Z", java.util.Locale.ENGLISH),
        java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", java.util.Locale.ENGLISH),
        java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.ENGLISH),
        java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm Z", java.util.Locale.ENGLISH),
    )
    
    var parsedDate: java.util.Date? = null
    for (format in dateFormats) {
        try {
            parsedDate = format.parse(pubDateString)
            if (parsedDate != null) break
        } catch (_: Exception) {}
    }
    
    if (parsedDate == null) return pubDateString
    
    val diffMs = System.currentTimeMillis() - parsedDate.time
    if (diffMs < 0) return if (language == "IT") "poco fa" else "just now"
    
    val diffMins = diffMs / (60 * 1000L)
    if (diffMins < 60) {
        return if (language == "IT") "$diffMins min fa" else "${diffMins}m ago"
    }
    
    val diffHours = diffMins / 60
    if (diffHours < 24) {
        return if (language == "IT") "$diffHours ore fa" else "${diffHours}h ago"
    }
    
    val diffDays = diffHours / 24
    return if (language == "IT") "$diffDays gg fa" else "${diffDays}d ago"
}

data class VisualNews(
    val title: String,
    val description: String,
    val imageUrl: String?,
    val pubDate: String,
    val source: String,
    val link: String
)

private fun Modifier.customAlpha(alpha: Float): Modifier = this.then(
    Modifier.drawWithContent {
        drawContent()
        drawRect(Color.Black.copy(alpha = 1f - alpha), blendMode = androidx.compose.ui.graphics.BlendMode.DstIn)
    }
)
