package com.dedio.dailypulse.media

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.view.KeyEvent
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.dedio.dailypulse.ui.i18n.LocalStrings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class MediaInfo(
    val title: String = "Nessun brano",
    val artist: String = "In attesa di Spotify...",
    val artwork: ImageBitmap? = null,
    val isPlaying: Boolean = false,
    val hasPermission: Boolean = true,
    val lyrics: String? = null,
    val syncedLyrics: List<LyricLine>? = null,
    val playbackPosition: Long = 0,
    val duration: Long = 0,
    val onPlayPause: () -> Unit = {},
    val onNext: () -> Unit = {},
    val onPrevious: () -> Unit = {},
)

data class LyricLine(val timeMs: Long, val text: String)

@Composable
fun rememberMediaController(): MediaInfo {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val strings = LocalStrings.current
    
    fun checkPermission(): Boolean {
        return try {
            val listeners = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
            listeners?.contains(context.packageName) == true
        } catch (_: Exception) {
            false
        }
    }

    var hasPermission by remember { mutableStateOf(checkPermission()) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasPermission = checkPermission()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    
    var mediaInfo by remember { 
        mutableStateOf(
            MediaInfo(
                title = strings.spotifyNoTrack,
                artist = strings.spotifyWaiting,
                hasPermission = hasPermission
            )
        )
    }

    // --- Lyrics Fetching Logic ---
    var lastLyricsKey by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    fun fetchLyrics(title: String, artist: String) {
        val key = "$title-$artist"
        if (key == lastLyricsKey || title == strings.spotifyNoTrack) return
        lastLyricsKey = key

        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val encodedTitle = java.net.URLEncoder.encode(title, "UTF-8")
                val encodedArtist = java.net.URLEncoder.encode(artist, "UTF-8")
                val url = "https://lrclib.net/api/get?artist_name=$encodedArtist&track_name=$encodedTitle"
                
                val result = com.dedio.dailypulse.network.NetworkClient.performGet(url)
                if (result is com.dedio.dailypulse.network.NetworkResult.Success) {
                    val json = org.json.JSONObject(result.data)
                    val plain = json.optString("plainLyrics", "")
                    val synced = json.optString("syncedLyrics", "")
                    
                    val lyricLines = if (synced.isNotEmpty()) {
                        parseLrc(synced)
                    } else null
                    
                    mediaInfo = mediaInfo.copy(lyrics = plain.ifEmpty { null }, syncedLyrics = lyricLines)
                } else {
                    mediaInfo = mediaInfo.copy(lyrics = null, syncedLyrics = null)
                }
            } catch (_: Exception) {
                mediaInfo = mediaInfo.copy(lyrics = null, syncedLyrics = null)
            }
        }
    }

    LaunchedEffect(hasPermission, strings) {
        if (!mediaInfo.isPlaying) {
            mediaInfo = mediaInfo.copy(
                hasPermission = hasPermission,
                title = strings.spotifyNoTrack,
                artist = if (mediaInfo.artist.contains("Spotify")) strings.spotifyWaiting else strings.spotifyForcePlay
            )
        } else {
            mediaInfo = mediaInfo.copy(hasPermission = hasPermission)
        }
    }

    var refreshTrigger by remember { mutableIntStateOf(0) }

    DisposableEffect(context, hasPermission, strings, refreshTrigger) {
        if (!hasPermission) return@DisposableEffect onDispose {}

        val sessionManager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
        var currentController: MediaController? = null

        val callback = object : MediaController.Callback() {
            override fun onMetadataChanged(metadata: MediaMetadata?) {
                metadata?.let {
                    val bitmap = it.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART) 
                        ?: it.getBitmap(MediaMetadata.METADATA_KEY_ART)
                    
                    val title = it.getString(MediaMetadata.METADATA_KEY_TITLE) ?: "Sconosciuto"
                    val artist = it.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: "Artista Sconosciuto"
                    val duration = it.getLong(MediaMetadata.METADATA_KEY_DURATION)

                    mediaInfo = mediaInfo.copy(
                        title = title,
                        artist = artist,
                        artwork = bitmap?.asImageBitmap(),
                        duration = duration
                    )
                    fetchLyrics(title, artist)
                }
            }

            override fun onPlaybackStateChanged(state: PlaybackState?) {
                state?.let {
                    mediaInfo = mediaInfo.copy(
                        isPlaying = it.state == PlaybackState.STATE_PLAYING,
                        playbackPosition = it.position
                    )
                }
            }
        }

        fun updateController() {
            val componentName = ComponentName(context, SpotifyNotificationService::class.java)
            val controllers = try { 
                sessionManager.getActiveSessions(componentName) 
            } catch (_: SecurityException) {
                try { sessionManager.getActiveSessions(null) } catch (_: Exception) { emptyList() }
            }

            val controller = controllers.firstOrNull { it.packageName.contains("spotify") } 
                ?: controllers.firstOrNull { it.playbackState?.state == PlaybackState.STATE_PLAYING }
                ?: controllers.firstOrNull()
            
            if (controller != null) {
                if (controller.sessionToken != currentController?.sessionToken) {
                    currentController?.unregisterCallback(callback)
                    currentController = controller
                    controller.registerCallback(callback)
                }

                val meta = controller.metadata
                val pbState = controller.playbackState
                val bitmap = meta?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART) 
                        ?: meta?.getBitmap(MediaMetadata.METADATA_KEY_ART)

                mediaInfo = mediaInfo.copy(
                    title = meta?.getString(MediaMetadata.METADATA_KEY_TITLE) ?: "In riproduzione...",
                    artist = meta?.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: "Artista...",
                    artwork = bitmap?.asImageBitmap(),
                    isPlaying = pbState?.state == PlaybackState.STATE_PLAYING,
                    playbackPosition = pbState?.position ?: 0L,
                    duration = meta?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0L,
                    onPlayPause = {
                        val activeState = controller.playbackState?.state
                        if (activeState == PlaybackState.STATE_PLAYING) controller.transportControls.pause()
                        else controller.transportControls.play()
                    },
                    onNext = { controller.transportControls.skipToNext() },
                    onPrevious = { controller.transportControls.skipToPrevious() },
                )
                meta?.let { fetchLyrics(it.getString(MediaMetadata.METADATA_KEY_TITLE) ?: "", it.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: "") }
            } else {
                mediaInfo = mediaInfo.copy(
                    title = strings.spotifyNoTrack,
                    artist = strings.spotifyForcePlay,
                    artwork = null,
                    isPlaying = false,
                    onPlayPause = {
                        try {
                            val mediaIntent = Intent(Intent.ACTION_MEDIA_BUTTON).apply {
                                putExtra(Intent.EXTRA_KEY_EVENT, KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY))
                            }
                            context.sendOrderedBroadcast(mediaIntent, null)
                            val mediaIntentUp = Intent(Intent.ACTION_MEDIA_BUTTON).apply {
                                putExtra(Intent.EXTRA_KEY_EVENT, KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY))
                            }
                            context.sendOrderedBroadcast(mediaIntentUp, null)
                            val launchIntent = context.packageManager.getLaunchIntentForPackage("com.spotify.music")
                            launchIntent?.let { context.startActivity(it) }
                        } catch (_: Exception) {}
                    }
                )
            }
        }

        updateController()
        onDispose { currentController?.unregisterCallback(callback) }
    }

    // Modern Coroutine-based loop instead of Timer to prevent thread-safety issues
    LaunchedEffect(hasPermission) {
        if (!hasPermission) return@LaunchedEffect
        
        // Forza un re-bind del servizio quando l'app è attiva per risolvere problemi di sincronizzazione del sistema
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            try {
                NotificationListenerService.requestRebind(ComponentName(context, SpotifyNotificationService::class.java))
            } catch (_: Exception) {}
        }

        while (isActive) {
            delay(5000L)
            refreshTrigger++ // Trigger a check for new media sessions
        }
    }

    return mediaInfo
}

private fun parseLrc(lrcContent: String): List<LyricLine> {
    val lines = mutableListOf<LyricLine>()
    val regex = Regex("\\[(\\d+):(\\d+)\\.(\\d+)\\](.*)")
    
    lrcContent.lines().forEach { line ->
        val match = regex.find(line)
        if (match != null) {
            val min = match.groupValues[1].toLong()
            val sec = match.groupValues[2].toLong()
            val ms = match.groupValues[3].toLong() * 10
            
            val totalMs = (min * 60 * 1000) + (sec * 1000) + ms
            val text = match.groupValues[4].trim()
            if (text.isNotEmpty()) {
                lines.add(LyricLine(totalMs, text))
            }
        }
    }
    return lines.sortedBy { it.timeMs }
}
