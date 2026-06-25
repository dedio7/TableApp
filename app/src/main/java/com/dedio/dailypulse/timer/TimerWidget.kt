package com.dedio.dailypulse.timer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.time.Duration.Companion.seconds

@Composable
fun TimerWidget(
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
) {
    var timeLeftSeconds by remember { mutableIntStateOf(0) }
    var initialSeconds by remember { mutableIntStateOf(0) }
    var isRunning by remember { mutableStateOf(value = false) }

    LaunchedEffect(isRunning, timeLeftSeconds) {
        if ((isRunning) && timeLeftSeconds > 0) {
            delay(1.seconds)
            timeLeftSeconds--
            if (timeLeftSeconds == 0) isRunning = false
        }
    }

    val progress = if (initialSeconds > 0) timeLeftSeconds.toFloat() / initialSeconds else 0f

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (timeLeftSeconds == 0 && !isRunning) {
            // Preset selection
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(5, 10, 15).forEach { mins ->
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                            .clickable {
                                initialSeconds = mins * 60
                                timeLeftSeconds = mins * 60
                                isRunning = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${mins}m", color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // Active Timer
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(Color.White.copy(alpha = 0.1f), style = Stroke(2.dp.toPx()))
                        drawArc(
                            color = Color(0xFF4FC3F7),
                            startAngle = -90f,
                            sweepAngle = 360f * progress,
                            useCenter = false,
                            style = Stroke(2.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    Text(
                        text = if (isRunning) "Ⅱ" else "▶", 
                        color = textColor, 
                        fontSize = 10.sp, 
                        modifier = Modifier.clickable { isRunning = !isRunning }
                    )
                }
                
                Text(
                    text = formatTime(timeLeftSeconds),
                    color = textColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.clickable { 
                        isRunning = false
                        timeLeftSeconds = 0 
                    }
                )
            }
        }
    }
}

private fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", m, s)
}
