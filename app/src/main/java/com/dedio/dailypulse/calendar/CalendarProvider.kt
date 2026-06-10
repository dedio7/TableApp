package com.dedio.dailypulse.calendar

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.dedio.dailypulse.ui.i18n.LocalStrings
import java.util.*

data class CalendarState(
    val events: List<CalendarEvent> = emptyList(),
    val hasPermission: Boolean = true
)

@Composable
fun rememberCalendarEvents(): CalendarState {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val strings = LocalStrings.current

    var hasPermission by remember { 
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED)
    }
    
    // Observer to re-check permission when app is resumed
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var state by remember { mutableStateOf(CalendarState(hasPermission = hasPermission)) }

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            state = CalendarState(events = fetchCalendarEvents(context, strings.noEventsToday, strings.todayLabel), hasPermission = true)
        } else {
            state = CalendarState(events = emptyList(), hasPermission = false)
        }
    }

    return state
}

private fun fetchCalendarEvents(context: Context, emptyTitle: String, emptyTime: String): List<CalendarEvent> {
    val result = mutableListOf<CalendarEvent>()
    
    val now = Calendar.getInstance().timeInMillis
    val endOfDay = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
    }.timeInMillis

    val projection = arrayOf(
        CalendarContract.Instances.TITLE,
        CalendarContract.Instances.BEGIN,
        CalendarContract.Instances.DISPLAY_COLOR
    )

    val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
    ContentUris.appendId(builder, now)
    ContentUris.appendId(builder, endOfDay)

    try {
        val cursor = context.contentResolver.query(
            builder.build(),
            projection,
            null,
            null,
            CalendarContract.Instances.BEGIN + " ASC"
        )

        cursor?.use {
            while (it.moveToNext()) {
                val title = it.getString(0)
                val begin = it.getLong(1)
                val colorInt = it.getInt(2)
                
                val cal = Calendar.getInstance().apply { timeInMillis = begin }
                val timeStr = String.format("%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
                
                result.add(CalendarEvent(
                    title = title,
                    time = timeStr,
                    color = if (colorInt != 0) androidx.compose.ui.graphics.Color(colorInt) else androidx.compose.ui.graphics.Color(0xFF4FC3F7)
                ))
            }
        }
    } catch (e: SecurityException) {
        // Permission not granted
    }

    return if (result.isEmpty()) {
        listOf(CalendarEvent(emptyTitle, emptyTime, androidx.compose.ui.graphics.Color.Gray))
    } else {
        result.take(3) // Reduced from 4 to save vertical space
    }
}
