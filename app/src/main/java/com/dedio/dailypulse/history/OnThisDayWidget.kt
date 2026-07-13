package com.dedio.dailypulse.history

import android.util.Log
import androidx.compose.animation.Crossfade
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dedio.dailypulse.network.NetworkClient
import com.dedio.dailypulse.network.NetworkResult
import org.json.JSONObject
import java.util.Calendar

@Composable
fun OnThisDayWidget(
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    language: String = "IT"
) {
    var event by remember { mutableStateOf<HistoryEvent?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var detailsOpen by remember { mutableStateOf(false) }

    LaunchedEffect(language) {
        isLoading = true
        event = fetchOnThisDay(language)
        isLoading = false
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black.copy(alpha = 0.3f))
            .clickable(enabled = event != null) { detailsOpen = true }
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Crossfade(targetState = isLoading to event, label = "historyFade") { (loading, data) ->
            if (loading) {
                CircularProgressIndicator(color = textColor.copy(alpha = 0.5f), modifier = Modifier.size(24.dp))
            } else if (data != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = (if (language == "IT") "ACCADDE OGGI" else "ON THIS DAY").uppercase(),
                        color = textColor.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${data.year}",
                        color = textColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = data.text,
                        color = textColor.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            } else {
                Text(
                    text = if (language == "IT") "Nessun evento oggi" else "No events today",
                    color = textColor.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
            }
        }
    }

    event?.let {
        HistoryDetailsPanel(
            visible = detailsOpen,
            event = it,
            onDismiss = { detailsOpen = false }
        )
    }
}

private suspend fun fetchOnThisDay(language: String): HistoryEvent? {
    val lang = if (language == "IT") "it" else "en"
    val cal = Calendar.getInstance()
    val month = String.format("%02d", cal.get(Calendar.MONTH) + 1)
    val day = String.format("%02d", cal.get(Calendar.DAY_OF_MONTH))
    
    val url = "https://$lang.wikipedia.org/api/rest_v1/feed/onthisday/events/$month/$day"
    
    return try {
        val result = NetworkClient.performGet(url)
        if (result is NetworkResult.Success) {
            val json = JSONObject(result.data)
            val events = json.getJSONArray("events")
            if (events.length() > 0) {
                // Select a "random" but consistent event for the day
                val index = Calendar.getInstance().get(Calendar.DAY_OF_YEAR) % events.length()
                val obj = events.getJSONObject(index)
                HistoryEvent(
                    year = obj.optInt("year"),
                    text = obj.optString("text")
                )
            } else null
        } else null
    } catch (e: Exception) {
        Log.e("HistoryRepo", "Error: ${e.message}")
        null
    }
}

data class HistoryEvent(val year: Int, val text: String)
