package com.dedio.dailypulse

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.service.dreams.DreamService
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.dedio.dailypulse.theme.DailyPulseTheme
import com.dedio.dailypulse.ui.main.MainScreen

/**
 * Service that allows DailyPulse to be used as an Android Daydream (Screensaver).
 */
class DailyPulseDreamService : DreamService(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    override val viewModelStore = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateScreenBrightness(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        // Configure DreamService
        isInteractive = true
        isFullscreen = true
        
        // Initial check for brightness
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val currentStatus = registerReceiver(batteryReceiver, filter)
        updateScreenBrightness(currentStatus)

        val composeView = ComposeView(this).apply {
            setContent {
                DailyPulseTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        MainScreen()
                    }
                }
            }
        }

        // Set up the necessary owners for Compose to work in a Service
        composeView.setViewTreeLifecycleOwner(this)
        composeView.setViewTreeViewModelStoreOwner(this)
        composeView.setViewTreeSavedStateRegistryOwner(this)

        setContentView(composeView)
    }

    private fun updateScreenBrightness(intent: Intent?) {
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val plugged = intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
        
        val isCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING) ||
                (status == BatteryManager.BATTERY_STATUS_FULL) ||
                (plugged == BatteryManager.BATTERY_PLUGGED_AC) ||
                (plugged == BatteryManager.BATTERY_PLUGGED_USB) ||
                (plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS)

        // Keep screen bright if charging, allow dimming if on battery
        isScreenBright = isCharging
    }

    override fun onDreamingStarted() {
        super.onDreamingStarted()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun onDreamingStopped() {
        super.onDreamingStopped()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        try {
            unregisterReceiver(batteryReceiver)
        } catch (e: Exception) {
            // Receiver might not be registered
        }
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModelStore.clear()
    }
}
