package com.dedio.dailypulse

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dedio.dailypulse.settings.AppSettings
import com.dedio.dailypulse.theme.DailyPulseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val appSettings = AppSettings(this)

        enableEdgeToEdge()
        setContent {
            val orientation by appSettings.appOrientation.collectAsStateWithLifecycle(initialValue = "AUTO")
            
            // Force orientation whenever the setting changes
            LaunchedEffect(orientation) {
                requestedOrientation = when (orientation) {
                    "PORTRAIT" -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    "LANDSCAPE" -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    else -> ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
                }
            }

            DailyPulseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavigation()
                }
            }
        }
    }
}
