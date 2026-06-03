package com.example.tabletapp.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.example.tabletapp.background.AmbientBackground
import com.example.tabletapp.background.BackgroundConfig
import com.example.tabletapp.battery.BatteryWidget
import com.example.tabletapp.clock.FlipClock
import com.example.tabletapp.news.NewsTicker
import com.example.tabletapp.settings.AppSettings
import com.example.tabletapp.weather.WeatherWidget

@Composable
fun MainScreen(
  onItemClick: (NavKey) -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val appSettings = remember { AppSettings(context) }

  val bgPrimary by appSettings.bgPrimaryColor.collectAsStateWithLifecycle(initialValue = 0xFF0D1B2A)
  val bgSecondary by appSettings.bgSecondaryColor.collectAsStateWithLifecycle(initialValue = 0xFF151528)
  val bgUseGradient by appSettings.bgUseGradient.collectAsStateWithLifecycle(initialValue = true)

  val config = remember(bgPrimary, bgSecondary, bgUseGradient) {
    BackgroundConfig(
      primaryColor = Color(bgPrimary),
      secondaryColor = Color(bgSecondary),
      useGradient = bgUseGradient
    )
  }

  AmbientBackground(config = config, modifier = Modifier.fillMaxSize()) {
    Box(
      modifier = modifier.fillMaxSize()
    ) {
      // 1. Battery Widget in top-right corner
      Box(
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(top = 8.dp, end = 8.dp)
      ) {
        BatteryWidget()
      }

      // 2. Main Content Layout (Clock on left, compact Weather on right)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .fillMaxHeight()
          .padding(top = 40.dp, bottom = 64.dp, start = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(32.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Large Clock Panel (Left)
        Box(
          modifier = Modifier
            .weight(2f)
            .fillMaxHeight(),
          contentAlignment = Alignment.Center
        ) {
          FlipClock()
        }

        // Smaller, Compact Weather Widget Panel (Right)
        Box(
          modifier = Modifier
            .weight(0.8f)
            .fillMaxHeight(),
          contentAlignment = Alignment.Center
        ) {
          WeatherWidget(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 16.dp)
          )
        }
      }

      // 3. News Ticker at the absolute bottom
      Box(
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .fillMaxWidth()
      ) {
        NewsTicker(
          modifier = Modifier.fillMaxWidth()
        )
      }
    }
  }
}
