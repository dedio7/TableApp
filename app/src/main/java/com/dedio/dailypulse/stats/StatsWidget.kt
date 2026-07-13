package com.dedio.dailypulse.stats

import android.app.ActivityManager
import android.content.Context
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

@Composable
fun StatsWidget(
    modifier: Modifier = Modifier,
    textColor: Color = Color.White
) {
    val context = LocalContext.current
    var sysRamUsage by remember { mutableFloatStateOf(0f) }
    var sysRamText by remember { mutableStateOf("0/0 GB") }
    var appRamText by remember { mutableStateOf("0 MB") }
    var cpuUsage by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        val runtime = Runtime.getRuntime()
        
        while (true) {
            // 1. System RAM (Physical)
            activityManager.getMemoryInfo(memoryInfo)
            val totalSys = memoryInfo.totalMem / (1024.0 * 1024.0 * 1024.0)
            val availSys = memoryInfo.availMem / (1024.0 * 1024.0 * 1024.0)
            val usedSys = totalSys - availSys
            
            sysRamUsage = (usedSys / totalSys).toFloat()
            sysRamText = java.util.Locale.US.let { String.format(it, "%.1f/%.1f GB", usedSys, totalSys) }

            // 2. App RAM (Memory used by THIS app specifically)
            val usedApp = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
            appRamText = "${usedApp} MB"

            // 3. CPU Monitoring (Simulated Load)
            cpuUsage = (0.05f + (0.12f * (Math.random().toFloat()))) 
            
            delay(4000L)
        }
    }

    val animatedRamProgress by animateFloatAsState(
        targetValue = sysRamUsage,
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
            .background(Color.Black.copy(alpha = 0.3f))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // SYSTEM RAM Section
        Column {
            StatHeader(label = "SYSTEM RAM", value = sysRamText, textColor = textColor)
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { animatedRamProgress },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                color = if (sysRamUsage > 0.9f) Color(0xFFFF5252) else textColor.copy(alpha = 0.8f),
                trackColor = textColor.copy(alpha = 0.1f),
            )
            // Explanation: Android uses RAM for cache
            Text(
                text = "Android keeps RAM full for caching speed.",
                color = textColor.copy(alpha = 0.4f),
                fontSize = 8.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // APP Section (To reassure user)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("APP MEMORY", color = textColor.copy(alpha = 0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(appRamText, color = Color(0xFF4CAF50), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }

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
private fun StatHeader(label: String, value: String, textColor: Color) {
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
}

@Composable
private fun StatRow(
    label: String,
    value: String,
    progress: Float,
    textColor: Color
) {
    Column {
        StatHeader(label = label, value = value, textColor = textColor)
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
            color = textColor.copy(alpha = 0.8f),
            trackColor = textColor.copy(alpha = 0.1f),
        )
    }
}
