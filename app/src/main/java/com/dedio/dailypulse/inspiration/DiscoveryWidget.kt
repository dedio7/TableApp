package com.dedio.dailypulse.inspiration

import android.content.Intent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.dedio.dailypulse.settings.AppSettings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DiscoveryWidget(
    index: Int,
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
) {
    val context = LocalContext.current
    val appSettings = remember { AppSettings(context) }
    val repository = remember { DiscoveryRepository() }
    val scope = rememberCoroutineScope()
    val strings = com.dedio.dailypulse.ui.i18n.LocalStrings.current
    val language = if (strings.settingsTitle == "Settings") "EN" else "IT"

    val typeIndex = index % 3
    val categoryLabel = when(typeIndex) {
        0 -> if(language == "IT") "FILM DEL GIORNO" else "MOVIE OF THE DAY"
        1 -> if(language == "IT") "DISCO DEL GIORNO" else "ALBUM OF THE DAY"
        else -> if(language == "IT") "SERIE TV DEL GIORNO" else "TV SERIES OF THE DAY"
    }

    var mediaItem by remember { mutableStateOf<MediaItem?>(null) }
    var isFetching by remember { mutableStateOf(false) }

    val fetchMedia = suspend {
        isFetching = true
        val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val newItem = repository.fetchDailyMedia(typeIndex, language)
        
        if (newItem != null) {
            mediaItem = newItem
            val json = JSONObject().apply {
                put("t", newItem.title)
                put("i", newItem.info)
                put("u", newItem.imageUrl ?: "")
                put("w", newItem.wikiUrl ?: "")
            }.toString()
            appSettings.setDiscoveryMedia(typeIndex, json, today)
        } else if (mediaItem == null) {
            // Updated fallback to show it's a fallback but not always the same title
            mediaItem = MediaItem(
                if(typeIndex == 0) "Interstellar" else if(typeIndex == 1) "Abbey Road" else "The Crown", 
                "Apple RSS (Error)", 
                null, 
                "https://wikipedia.org"
            )
        }
        isFetching = false
    }
    
    LaunchedEffect(typeIndex, language) {
        val cachedJson = when(typeIndex) {
            0 -> appSettings.discoveryMovieJson.first()
            1 -> appSettings.discoveryAlbumJson.first()
            else -> appSettings.discoverySeriesJson.first()
        }
        val lastDate = appSettings.discoveryLastDate.first()
        val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())

        if (cachedJson != null && lastDate == today) {
            try {
                val json = JSONObject(cachedJson)
                mediaItem = MediaItem(
                    json.getString("t"), 
                    json.getString("i"), 
                    json.optString("u", "").ifBlank { null },
                    json.optString("w"),
                )
            } catch (_: Exception) { fetchMedia() }
        } else { fetchMedia() }
    }

    Box(
        modifier = modifier.fillMaxWidth().clickable { 
            mediaItem?.wikiUrl?.let { url ->
                try { context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri())) } catch (_: Exception) {}
            }
        }
    ) {
        Crossfade(targetState = mediaItem, animationSpec = tween(1000), label = "discoveryFade") { item ->
            if (item != null) DiscoveryContent(item, categoryLabel, typeIndex, textColor)
        }
    }
}

@Composable
private fun DiscoveryContent(
    item: MediaItem,
    categoryLabel: String,
    typeIndex: Int,
    textColor: Color
) {
    Row(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp).fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // High Quality Image with Coil
        if (item.imageUrl != null) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.05f))
            )
        } else {
            // Fallback Emoji
            Box(
                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(12.dp)).background(textColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = when(typeIndex) { 0 -> "🎬"; 1 -> "💿"; else -> "📺" }, fontSize = 28.sp)
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f, fill = false)) {
            Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(textColor.copy(alpha = 0.1f)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                Text(text = categoryLabel, color = textColor.copy(alpha = 0.4f), fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.2.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = item.title, color = textColor.copy(alpha = 0.9f), fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(text = item.info, color = textColor.copy(alpha = 0.5f), fontSize = 12.sp, fontWeight = FontWeight.Light, maxLines = 1)
        }
    }
}
