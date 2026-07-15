package com.dedio.dailypulse.media

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun LyricsWidget(
    mediaInfo: MediaInfo,
    modifier: Modifier = Modifier,
    textColor: Color = Color.White
) {
    val listState = rememberLazyListState()
    
    // Determine current active line for synced lyrics
    val activeLineIndex = remember(mediaInfo.playbackPosition, mediaInfo.syncedLyrics) {
        mediaInfo.syncedLyrics?.indexOfLast { it.timeMs <= mediaInfo.playbackPosition } ?: -1
    }

    // Auto-scroll to active line
    LaunchedEffect(activeLineIndex) {
        if (activeLineIndex >= 0) {
            listState.animateScrollToItem(activeLineIndex, scrollOffset = -100)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Black.copy(alpha = 0.4f))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            mediaInfo.syncedLyrics != null -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 40.dp)
                ) {
                    itemsIndexed(mediaInfo.syncedLyrics) { index, line ->
                        val isActive = index == activeLineIndex
                        val alpha by animateFloatAsState(
                            targetValue = if (isActive) 1f else 0.4f,
                            animationSpec = tween(500),
                            label = "lyricAlpha"
                        )
                        val fontSize = if (isActive) 18.sp else 15.sp
                        val fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium

                        Text(
                            text = line.text,
                            color = textColor,
                            fontSize = fontSize,
                            fontWeight = fontWeight,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.alpha(alpha).fillMaxWidth()
                        )
                    }
                }
            }
            mediaInfo.lyrics != null -> {
                // Static lyrics fallback
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            text = mediaInfo.lyrics,
                            color = textColor.copy(alpha = 0.8f),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            else -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "TESTI NON DISPONIBILI",
                        color = textColor.copy(alpha = 0.3f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Spotify non sta inviando dati o il brano non è riconosciuto",
                        color = textColor.copy(alpha = 0.2f),
                        fontSize = 9.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
