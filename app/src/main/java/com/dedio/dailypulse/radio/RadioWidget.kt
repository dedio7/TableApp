package com.dedio.dailypulse.radio

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.PlaybackException
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.media3.ui.AspectRatioFrameLayout

data class RadioStation(
    val name: String,
    val streamUrl: String,
    val icon: String,
)

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
    val context = LocalContext.current
    
    // Manage ExoPlayer state
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
        }
    }

    var currentStation by remember { mutableStateOf<RadioStation?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorState by remember { mutableStateOf<String?>(null) }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsLoadingChanged(loading: Boolean) {
                isLoading = loading
            }
            override fun onPlaybackStateChanged(state: Int) {
                isLoading = state == Player.STATE_BUFFERING
                isPlaying = state == Player.STATE_READY && exoPlayer.playWhenReady
                if (state == Player.STATE_ENDED) {
                    isPlaying = false
                }
            }
            override fun onPlayerError(error: PlaybackException) {
                android.util.Log.e("RadioWidget", "ExoPlayer error", error)
                errorState = "ERRORE DI RETE"
                isLoading = false
                isPlaying = false
                currentStation = null
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    fun stopRadio() {
        exoPlayer.stop()
        isPlaying = false
        isLoading = false
        currentStation = null
        errorState = null
    }

    fun playStation(station: RadioStation) {
        if (currentStation == station && isPlaying) {
            stopRadio()
            return
        }

        errorState = null
        isLoading = true
        currentStation = station
        
        try {
            val mediaItem = MediaItem.fromUri(station.streamUrl)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.play()
        } catch (e: Exception) {
            android.util.Log.e("RadioWidget", "Error starting playback", e)
            errorState = "ERRORE AVVIO"
            isLoading = false
            currentStation = null
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.Black.copy(alpha = 0.25f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "WEB RADIO & LIVE TV",
                color = textColor.copy(alpha = 0.5f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            
            if (currentStation != null || errorState != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color(0xFFFF5252),
                            modifier = Modifier.size(10.dp),
                            strokeWidth = 1.5.dp
                        )
                    }
                    Text(
                        text = errorState ?: if (isLoading) "CARICAMENTO..." else "LIVE: ${currentStation?.name}",
                        color = if (errorState != null) Color.Yellow else Color(0xFFFF5252),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Embedded video player for Radio Rock TV stream
        if (currentStation?.name == "Radio Rock" && isPlaying) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black)
            ) {
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = exoPlayer
                            useController = false
                            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        }
                    },
                    update = { playerView ->
                        playerView.player = exoPlayer
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(PRESET_RADIOS) { station ->
                val isActive = currentStation == station
                val bgColor by animateColorAsState(
                    targetValue = if (isActive) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.04f),
                    label = "radioBg"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(72.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(bgColor)
                        .clickable { playStation(station) }
                        .padding(vertical = 10.dp)
                ) {
                    Text(text = station.icon, fontSize = 24.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = station.name,
                        color = if (isActive) Color.White else textColor.copy(alpha = 0.7f),
                        fontSize = 9.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
