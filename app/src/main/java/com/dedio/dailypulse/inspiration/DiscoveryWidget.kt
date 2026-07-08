package com.dedio.dailypulse.inspiration

import android.content.Intent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@Composable
fun DiscoveryWidget(
    index: Int,
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
) {
    val context = LocalContext.current
    val repository = remember { DiscoveryRepository() }
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

    LaunchedEffect(typeIndex, language) {
        isFetching = true
        mediaItem = repository.fetchDailyMedia(typeIndex, language)
        isFetching = false
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp) // Spazio aumentato considerevolmente
            .clickable { 
                mediaItem?.wikiUrl?.let { url ->
                    try { context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri())) } catch (_: Exception) {}
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (isFetching && mediaItem == null) {
            CircularProgressIndicator(color = textColor.copy(alpha = 0.3f), modifier = Modifier.size(32.dp), strokeWidth = 3.dp)
        } else {
            Crossfade(targetState = mediaItem, animationSpec = tween(1000), label = "discoveryFade") { item ->
                if (item != null) {
                    DiscoveryContent(item, categoryLabel, typeIndex, textColor)
                } else if (!isFetching) {
                    Text("Service Unavailable", color = textColor.copy(alpha = 0.5f), fontSize = 12.sp)
                }
            }
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
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Copertina più grande per maggiore impatto
        if (item.imageUrl != null) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.05f))
            )
        } else {
            Box(
                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)).background(textColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = when(typeIndex) { 0 -> "🎬"; 1 -> "💿"; else -> "📺" }, fontSize = 40.sp)
            }
        }
        
        Spacer(modifier = Modifier.width(20.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            // Label categoria
            Text(
                text = categoryLabel, 
                color = textColor.copy(alpha = 0.5f), 
                fontSize = 11.sp, 
                fontWeight = FontWeight.Black, 
                letterSpacing = 1.2.sp
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Titolo (su 2 righe)
            Text(
                text = item.title, 
                color = textColor.copy(alpha = 1f), 
                fontSize = 18.sp, 
                fontWeight = FontWeight.Bold, 
                maxLines = 2,
                lineHeight = 22.sp,
                overflow = TextOverflow.Ellipsis
            )
            
            // Artista / Regista (su 1 riga o 2 se necessario)
            if (item.artist.isNotEmpty()) {
                Text(
                    text = item.artist, 
                    color = textColor.copy(alpha = 0.9f), 
                    fontSize = 15.sp, 
                    fontWeight = FontWeight.SemiBold, 
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Anno / Categoria
            Text(
                text = item.info, 
                color = textColor.copy(alpha = 0.6f), 
                fontSize = 13.sp, 
                fontWeight = FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
