package com.dedio.dailypulse.news.visual

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import kotlin.time.Duration.Companion.minutes

@Composable
fun VisualNewsWidget(
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    language: String = "IT"
) {
    var newsItem by remember { mutableStateOf<VisualNews?>(null) }
    var detailsOpen by remember { mutableStateOf(false) }
    
    LaunchedEffect(language) {
        while(true) {
            newsItem = fetchLatestVisualNews(language)
            delay(15.minutes)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable(enabled = newsItem != null) { detailsOpen = true }
    ) {
        Crossfade(targetState = newsItem, label = "newsFade") { item ->
            if (item != null) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().customAlpha(0.6f)
                    )
                    
                    // Gradient overlay for text readability
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                                    startY = 100f
                                )
                            )
                    )
                    
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = (if (language == "IT") "ULTIMA ORA" else "BREAKING NEWS").uppercase(),
                            color = Color(0xFFFF5252),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.title,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 20.sp
                        )
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Loading news...", color = textColor.copy(alpha = 0.3f), fontSize = 12.sp)
                }
            }
        }
    }

    newsItem?.let {
        VisualNewsDetailsPanel(
            visible = detailsOpen,
            item = it,
            language = language,
            onDismiss = { detailsOpen = false }
        )
    }
}

private suspend fun fetchLatestVisualNews(language: String): VisualNews? {
    val url = if (language == "IT") "https://www.ansa.it/sito/ansait_rss.xml" 
              else "http://feeds.bbci.co.uk/news/rss.xml"
              
    val result = NetworkClient.performGet(url)
    if (result is NetworkResult.Success) {
        val data = result.data
        val title = data.substringAfter("<title><![CDATA[").substringBefore("]]></title>")
            .ifEmpty { data.substringAfter("<title>").substringBefore("</title>") }
            
        var imageUrl = data.substringAfter("<media:thumbnail url=\"").substringBefore("\"")
        if (imageUrl.length > 500 || !imageUrl.contains("http")) imageUrl = "" 
        
        if (imageUrl.isEmpty()) {
            imageUrl = data.substringAfter("url=\"").substringBefore("\"")
            if (!imageUrl.endsWith(".jpg") && !imageUrl.contains(".png")) imageUrl = ""
        }
        
        return if (title.length > 5) VisualNews(title, imageUrl.ifEmpty { null }) else null
    }
    return null
}

data class VisualNews(val title: String, val imageUrl: String?)

private fun Modifier.customAlpha(alpha: Float): Modifier = this.then(
    Modifier.drawWithContent {
        drawContent()
        drawRect(Color.Black.copy(alpha = 1f - alpha), blendMode = androidx.compose.ui.graphics.BlendMode.DstIn)
    }
)
