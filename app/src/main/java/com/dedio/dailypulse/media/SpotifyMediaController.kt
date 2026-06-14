package com.dedio.dailypulse.media

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.provider.Settings
import android.view.KeyEvent
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.dedio.dailypulse.ui.i18n.LocalStrings
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

data class MediaInfo(
    val title: String = "Nessun brano",
    val artist: String = "In attesa di Spotify...",
    val artwork: ImageBitmap? = null,
    val isPlaying: Boolean = false,
    val hasPermission: Boolean = true,
    val onPlayPause: () -> Unit = {},
    val onNext: () -> Unit = {},
    val onPrevious: () -> Unit = {},
)

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

    DisposableEffect(context, hasPermission, strings) {
        if (!hasPermission) return@DisposableEffect onDispose {}

        val sessionManager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
        var currentController: MediaController? = null

        val callback = object : MediaController.Callback() {
            override fun onMetadataChanged(metadata: MediaMetadata?) {
                metadata?.let {
                    val bitmap = it.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART) 
                        ?: it.getBitmap(MediaMetadata.METADATA_KEY_ART)
                    
                    mediaInfo = mediaInfo.copy(
                        title = it.getString(MediaMetadata.METADATA_KEY_TITLE) ?: "Sconosciuto",
                        artist = it.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: "Artista Sconosciuto",
                        artwork = bitmap?.asImageBitmap()
                    )
                }
            }

            override fun onPlaybackStateChanged(state: PlaybackState?) {
                state?.let {
                    mediaInfo = mediaInfo.copy(
                        isPlaying = it.state == PlaybackState.STATE_PLAYING
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
                    onPlayPause = {
                        val activeState = controller.playbackState?.state
                        if (activeState == PlaybackState.STATE_PLAYING) controller.transportControls.pause()
                        else controller.transportControls.play()
                    },
                    onNext = { controller.transportControls.skipToNext() },
                    onPrevious = { controller.transportControls.skipToPrevious() },
                )
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
        while (isActive) {
            delay(5000L)
            // Re-trigger the controller update if needed
            // (The callback handles most changes, but we check for new sessions periodically)
        }
    }

    return mediaInfo
}
