package com.example.tabletapp.clock.enhanced

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ClockShowcase() {
    var mode by remember { mutableStateOf(ClockMode.BINARY) }
    var showSeconds by remember { mutableStateOf(true) }
    var useAccentTheme by remember { mutableStateOf(false) }

    val theme = if (useAccentTheme) {
        BinaryClockTheme(
            litColor = Color(0xFF00E5FF), // Cyan
            accentColor = Color(0xFFFF4081) // Pink
        )
    } else {
        BinaryClockTheme() // Default
    }

    val config = ClockConfig(mode = mode, showSeconds = showSeconds, theme = theme)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Enhanced Binary Clock",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        // The Clock
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            EnhancedBinaryClock(config = config)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Controls
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Mode", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                    Button(onClick = {
                        mode = if (mode == ClockMode.BINARY) ClockMode.BCD else ClockMode.BINARY
                    }) {
                        Text(if (mode == ClockMode.BINARY) "Binary" else "BCD")
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Seconds", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                    Switch(checked = showSeconds, onCheckedChange = { showSeconds = it })
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Theme", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                    IconButton(onClick = { useAccentTheme = !useAccentTheme }) {
                        val color = if (useAccentTheme) Color(0xFF00E5FF) else Color.White
                        Box(modifier = Modifier.size(24.dp).background(color))
                    }
                }
            }
        }
        
        Text(
            "Tip: Tap a column to see its decimal value",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
