package com.dedio.dailypulse.media

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dedio.dailypulse.ui.i18n.LocalStrings

@Composable
fun MediaWidget(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val mediaInfo = rememberMediaController()
    val strings = LocalStrings.current

    if (!mediaInfo.hasPermission) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Red.copy(alpha = 0.15f))
                .clickable {
                    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                    context.startActivity(intent)
                }
                .padding(12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                strings.spotifyEnableAccess,
                color = Color.White,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
        }
        return
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Album Art
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            if (mediaInfo.artwork != null) {
                Image(
                    bitmap = mediaInfo.artwork,
                    contentDescription = "Album Art",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text("🎵", fontSize = 24.sp)
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                mediaInfo.title.ifEmpty { strings.spotifyNoTrack },
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                mediaInfo.artist.ifEmpty { strings.spotifyWaiting },
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        val spotifyGreen = Color(0xFF1DB954)

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Previous Button (Custom Draw)
            Canvas(
                modifier = Modifier
                    .size(24.dp)
                    .clickable { mediaInfo.onPrevious() }
            ) {
                val w = size.width
                val h = size.height
                val path = Path().apply {
                    moveTo(w * 0.8f, h * 0.2f)
                    lineTo(w * 0.25f, h * 0.5f)
                    lineTo(w * 0.8f, h * 0.8f)
                    close()
                }
                drawPath(path, Color.White)
                drawRect(Color.White, Offset(w * 0.15f, h * 0.2f), Size(w * 0.1f, h * 0.6f))
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Play/Pause Button (Custom Draw)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable { mediaInfo.onPlayPause() },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(16.dp)) {
                    val w = size.width
                    val h = size.height
                    if (mediaInfo.isPlaying) {
                        // Pause Bars
                        val barW = w * 0.35f
                        drawRect(spotifyGreen, Offset(0f, 0f), Size(barW, h))
                        drawRect(spotifyGreen, Offset(w - barW, 0f), Size(barW, h))
                    } else {
                        // Play Triangle
                        val path = Path().apply {
                            moveTo(0f, 0f)
                            lineTo(w, h / 2f)
                            lineTo(0f, h)
                            close()
                        }
                        drawPath(path, spotifyGreen)
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Next Button (Custom Draw)
            Canvas(
                modifier = Modifier
                    .size(24.dp)
                    .clickable { mediaInfo.onNext() }
            ) {
                val w = size.width
                val h = size.height
                val path = Path().apply {
                    moveTo(w * 0.2f, h * 0.2f)
                    lineTo(w * 0.75f, h * 0.5f)
                    lineTo(w * 0.2f, h * 0.8f)
                    close()
                }
                drawPath(path, Color.White)
                drawRect(Color.White, Offset(w * 0.75f, h * 0.2f), Size(w * 0.1f, h * 0.6f))
            }
        }
    }
}
