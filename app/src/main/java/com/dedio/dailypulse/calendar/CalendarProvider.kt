package com.dedio.dailypulse.calendar

import android.Manifest
import android.content.ContentUris
import android.content.ContentValues
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
fun rememberCalendarEvents(selectedDate: Calendar = Calendar.getInstance()): CalendarState {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val strings = LocalStrings.current

    var hasPermission by remember { 
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    // Observer to re-check permission when app is resumed
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasPermission = 
                    ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var state by remember { mutableStateOf(CalendarState(hasPermission = hasPermission)) }

    LaunchedEffect(hasPermission, strings, selectedDate) {
        if (hasPermission) {
            val emptyTitle = if (isToday(selectedDate)) strings.noEventsToday else strings.noEventsLabel
            state = CalendarState(events = fetchCalendarEvents(context, selectedDate, emptyTitle, strings.todayLabel), hasPermission = true)
        } else {
            state = CalendarState(events = emptyList(), hasPermission = false)
        }
    }

    return state
}

private fun isToday(date: Calendar): Boolean {
    val today = Calendar.getInstance()
    return date.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            date.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
}

fun fetchCalendarEvents(context: Context, date: Calendar, emptyTitle: String, emptyTime: String): List<CalendarEvent> {
    val result = mutableListOf<CalendarEvent>()
    
    val startOfDay = (date.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    
    val endOfDay = (date.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }.timeInMillis

    val projection = arrayOf(
        CalendarContract.Instances.TITLE,
        CalendarContract.Instances.BEGIN,
        CalendarContract.Instances.DISPLAY_COLOR
    )

    val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
    ContentUris.appendId(builder, startOfDay)
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
                val title = it.getString(0) ?: "No Title"
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
        result.take(10)
    }
}

/**
 * Checks if there is at least one event for the given month.
 * Returns a set of days (1-31) that have events.
 */
fun getDaysWithEvents(context: Context, month: Calendar): Set<Int> {
    val daysWithEvents = mutableSetOf<Int>()
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
        return daysWithEvents
    }

    val startOfMonth = (month.clone() as Calendar).apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }.timeInMillis

    val endOfMonth = (month.clone() as Calendar).apply {
        set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
    }.timeInMillis

    val projection = arrayOf(CalendarContract.Instances.BEGIN)
    val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
    ContentUris.appendId(builder, startOfMonth)
    ContentUris.appendId(builder, endOfMonth)

    try {
        val cursor = context.contentResolver.query(
            builder.build(),
            projection,
            null,
            null,
            null
        )

        cursor?.use {
            while (it.moveToNext()) {
                val begin = it.getLong(0)
                val cal = Calendar.getInstance().apply { timeInMillis = begin }
                daysWithEvents.add(cal.get(Calendar.DAY_OF_MONTH))
            }
        }
    } catch (e: Exception) {
        // Ignore
    }
    return daysWithEvents
}

fun addCalendarEvent(context: Context, title: String, startTime: Long, endTime: Long): Boolean {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
        return false
    }

    return try {
        // Try to find a valid calendar ID first
        val calendarId = getPrimaryCalendarId(context) ?: 1
        
        val values = ContentValues().apply {
            put(CalendarContract.Events.DTSTART, startTime)
            put(CalendarContract.Events.DTEND, endTime)
            put(CalendarContract.Events.TITLE, title)
            put(CalendarContract.Events.DESCRIPTION, "Added from DailyPulse")
            put(CalendarContract.Events.CALENDAR_ID, calendarId)
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
        }
        val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
        uri != null
    } catch (e: Exception) {
        false
    }
}

private fun getPrimaryCalendarId(context: Context): Long? {
    val projection = arrayOf(CalendarContract.Calendars._ID, CalendarContract.Calendars.IS_PRIMARY)
    val selection = "${CalendarContract.Calendars.VISIBLE} = 1"
    
    return try {
        context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            selection,
            null,
            null
        )?.use { cursor ->
            var primaryId: Long? = null
            while (cursor.moveToNext()) {
                val id = cursor.getLong(0)
                val isPrimary = cursor.getInt(1) == 1
                if (isPrimary) return id // Found primary
                if (primaryId == null) primaryId = id // Fallback to first visible
            }
            primaryId
        }
    } catch (e: Exception) {
        null
    }
}
