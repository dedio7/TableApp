package com.dedio.dailypulse.radio

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri

data class RadioStation(
    val name: String,
    val streamUrl: String,
    val icon: String,
)

// Updated with HTTPS links where possible and verified stream formats
val PRESET_RADIOS = listOf(
    RadioStation("Radio Rock", "https://rr.fluid.stream/RadioRockTV/livestream/playlist.m3u8", "🤘"),
    RadioStation("Virgin Radio", "https://icecast.unitedradio.it/Virgin.mp3", "🎸"),
    RadioStation("Radio 105", "https://icecast.unitedradio.it/Radio105.mp3", "🎧"),
    RadioStation("RTL 102.5", "https://streamingv2.shoutcast.com/rtl-1025", "🎵"),
    RadioStation("Rai Radio 1", "https://icestreaming.rai.it/1.mp3", "🇮🇹"),
)

@Composable
fun RadioWidget(
    modifier: Modifier = Modifier,
    textColor: Color = Color.White
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var currentStation by remember { mutableStateOf<RadioStation?>(null) }
    var isPlaying by remember { mutableStateOf(value = false) }
    var isLoading by remember { mutableStateOf(value = false) }
    var errorState by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    fun stopRadio() {
        try {
            mediaPlayer?.stop()
        } catch (_: Exception) {}
        mediaPlayer?.release()
        mediaPlayer = null
        isPlaying = false
        isLoading = false
        currentStation = null
        errorState = null
    }

    fun playStation(station: RadioStation) {
        if ((currentStation == station) && isPlaying) {
            stopRadio()
            return
        }

        stopRadio()
        currentStation = station
        isLoading = true
        errorState = null
        
        try {
            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                // Inclusion of User-Agent header to bypass potential server blocks
                val uri = station.streamUrl.toUri()
                val headers = mapOf("User-Agent" to "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36")
                setDataSource(context, uri, headers)
                
                setOnPreparedListener {
                    isLoading = false
                    isPlaying = true
                    errorState = null
                    it.start()
                }
                setOnErrorListener { _, what, extra ->
                    android.util.Log.e("RadioWidget", "Error: $what, $extra")
                    isLoading = false
                    isPlaying = false
                    currentStation = null
                    errorState = "ERRORE CONNESSIONE"
                    true
                }
                prepareAsync()
            }
            mediaPlayer = player
        } catch (e: Exception) {
            android.util.Log.e("RadioWidget", "Exception: ${e.message}")
            isLoading = false
            currentStation = null
            errorState = "ERRORE AVVIO"
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Black.copy(alpha = 0.3f))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "WEB RADIO RAPIDA",
                color = textColor.copy(alpha = 0.5f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            
            if (isPlaying || isLoading || errorState != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color(0xFFFF5252),
                            modifier = Modifier.size(12.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = errorState ?: if (isLoading) "CONNETTO..." else "LIVE: ${currentStation?.name}",
                        color = if (errorState != null) Color.Yellow else Color(0xFFFF5252),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(PRESET_RADIOS) { station ->
                val isActive = currentStation == station
                val bgColor by animateColorAsState(
                    targetValue = if (isActive) Color.White.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
                    label = "radioBg"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(bgColor)
                        .clickable { playStation(station) }
                        .padding(vertical = 10.dp)
                ) {
                    Text(text = station.icon, fontSize = 28.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = station.name,
                        color = if (isActive) Color.White else textColor.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
