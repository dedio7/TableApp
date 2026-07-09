package com.dedio.dailypulse

import android.os.BatteryManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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

class DailyPulseDreamService : DreamService(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    override val viewModelStore = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private var batteryReceiver: BroadcastReceiver? = null

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        isInteractive = true // Enable interactivity to allow discovery gestures
        isFullscreen = true
        
        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@DailyPulseDreamService)
            setViewTreeViewModelStoreOwner(this@DailyPulseDreamService)
            setViewTreeSavedStateRegistryOwner(this@DailyPulseDreamService)
        }

        composeView.setContent {
            DailyPulseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    MainScreen(isDreamMode = true)
                }
            }
        }

        setContentView(composeView)

        // Brightness management
        if (batteryReceiver == null) {
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            batteryReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
                    isScreenBright = (status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL)
                }
            }
            registerReceiver(batteryReceiver, filter)
        }
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
        batteryReceiver?.let { 
            try {
                unregisterReceiver(it)
            } catch (_: IllegalArgumentException) {
                // Receiver not registered
            }
        }
        batteryReceiver = null
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        viewModelStore.clear()
    }
}
