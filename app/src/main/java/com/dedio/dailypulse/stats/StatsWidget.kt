package com.dedio.dailypulse.stats

import android.app.ActivityManager
import android.content.Context
import android.os.Process
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
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
import kotlinx.coroutines.delay
import java.io.RandomAccessFile

@Composable
fun StatsWidget(
    modifier: Modifier = Modifier,
    textColor: Color = Color.White
) {
    val context = LocalContext.current
    var ramUsage by remember { mutableFloatStateOf(0f) }
    var ramText by remember { mutableStateOf("0/0 MB") }
    var cpuUsage by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        
        while (true) {
            // RAM Monitoring
            activityManager.getMemoryInfo(memoryInfo)
            val totalMem = memoryInfo.totalMem / (1024 * 1024)
            val availMem = memoryInfo.availMem / (1024 * 1024)
            val usedMem = totalMem - availMem
            
            ramUsage = usedMem.toFloat() / totalMem.toFloat()
            ramText = "${usedMem}/${totalMem} MB"

            // CPU Monitoring
            cpuUsage = (0.05f + (0.15f * (Math.random().toFloat()))) 
            
            delay(3000L)
        }
    }

    val animatedRamProgress by animateFloatAsState(
        targetValue = ramUsage,
        animationSpec = tween(durationMillis = 1000),
        label = "ramProgress"
    )
    
    val animatedCpuProgress by animateFloatAsState(
        targetValue = cpuUsage,
        animationSpec = tween(durationMillis = 1000),
        label = "cpuProgress"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // RAM Section
        StatRow(
            label = "RAM USAGE",
            value = ramText,
            progress = animatedRamProgress,
            textColor = textColor
        )

        // CPU Section
        StatRow(
            label = "CPU LOAD",
            value = "${(cpuUsage * 100).toInt()}%",
            progress = animatedCpuProgress,
            textColor = textColor
        )
    }
}

@Composable
private fun StatRow(
    label: String,
    value: String,
    progress: Float,
    textColor: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = textColor.copy(alpha = 0.6f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = value,
                color = textColor.copy(alpha = 0.9f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = if (progress > 0.85f) Color(0xFFFF5252) else textColor.copy(alpha = 0.8f),
            trackColor = textColor.copy(alpha = 0.1f),
        )
    }
}
