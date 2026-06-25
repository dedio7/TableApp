package com.dedio.dailypulse.inspiration

import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri

@Composable
fun DiscoveryWidget(
    index: Int,
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
) {
    val context = LocalContext.current
    val strings = com.dedio.dailypulse.ui.i18n.LocalStrings.current
    val language = if (strings.settingsTitle == "Settings") "EN" else "IT"

    val typeIndex = index % 3
    val categoryLabel = when(typeIndex) {
        0 -> if(language == "IT") "FILM DEL GIORNO" else "MOVIE OF THE DAY"
        1 -> if(language == "IT") "DISCO DEL GIORNO" else "ALBUM OF THE DAY"
        else -> if(language == "IT") "SERIE TV DEL GIORNO" else "TV SERIES OF THE DAY"
    }
    
    val item = MotivationRepository.rememberDailyMedia(index, language)

    Column(
        modifier = modifier
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .fillMaxWidth()
            .clickable { 
                item.wikiUrl?.let { url ->
                    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                    context.startActivity(intent)
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Icon Placeholder (Immagini rimosse per stabilità)
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(textColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when(typeIndex) {
                        0 -> "🎬"
                        1 -> "💿"
                        else -> "📺"
                    },
                    fontSize = 28.sp
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f, fill = false)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(textColor.copy(alpha = 0.1f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = categoryLabel,
                        color = textColor.copy(alpha = 0.4f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = item.title,
                    color = textColor.copy(alpha = 0.9f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    maxLines = 1
                )
                
                Text(
                    text = item.info,
                    color = textColor.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Light,
                    maxLines = 1
                )
            }
        }
    }
}
