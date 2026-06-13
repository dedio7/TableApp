package com.dedio.dailypulse.calendar

import android.Manifest
import android.accounts.Account
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
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

/**
 * Hook to manage calendar events with an optional refreshKey to force re-fetches.
 */
@Composable
fun rememberCalendarEvents(
    selectedDate: Calendar = Calendar.getInstance(),
    refreshKey: Int = 0
): CalendarState {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val strings = LocalStrings.current

    var hasPermission by remember { 
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED
        )
    }
    
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

    // Re-fetch when permission, selected date, or the explicit refreshKey changes
    LaunchedEffect(hasPermission, strings, selectedDate, refreshKey) {
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
        CalendarContract.Instances.EVENT_ID,
        CalendarContract.Instances.TITLE,
        CalendarContract.Instances.BEGIN,
        CalendarContract.Instances.END,
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
                val id = it.getLong(0)
                val title = it.getString(1) ?: "No Title"
                val begin = it.getLong(2)
                val end = it.getLong(3)
                val colorInt = it.getInt(4)
                
                val cal = Calendar.getInstance().apply { timeInMillis = begin }
                val timeStr = String.format("%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
                
                result.add(CalendarEvent(
                    id = id,
                    title = title,
                    time = timeStr,
                    color = if (colorInt != 0) androidx.compose.ui.graphics.Color(colorInt) else androidx.compose.ui.graphics.Color(0xFF4FC3F7),
                    startTime = begin,
                    endTime = end
                ))
            }
        }
    } catch (e: SecurityException) { }

    return if (result.isEmpty()) {
        listOf(CalendarEvent(title = emptyTitle, time = emptyTime, color = androidx.compose.ui.graphics.Color.Gray))
    } else {
        result.take(10)
    }
}

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
        val cursor = context.contentResolver.query(builder.build(), projection, null, null, null)
        cursor?.use {
            while (it.moveToNext()) {
                val begin = it.getLong(0)
                val cal = Calendar.getInstance().apply { timeInMillis = begin }
                daysWithEvents.add(cal.get(Calendar.DAY_OF_MONTH))
            }
        }
    } catch (e: Exception) { }
    return daysWithEvents
}

fun addCalendarEvent(context: Context, title: String, startTime: Long, endTime: Long): Boolean {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) return false

    return try {
        val calendarInfo = getGoogleCalendarInfo(context) ?: return false
        
        val values = ContentValues().apply {
            put(CalendarContract.Events.DTSTART, startTime)
            put(CalendarContract.Events.DTEND, endTime)
            put(CalendarContract.Events.TITLE, title)
            put(CalendarContract.Events.CALENDAR_ID, calendarInfo.id)
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
        }
        val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
        
        if (uri != null) {
            triggerSync(calendarInfo.accountName)
            true
        } else false
    } catch (e: Exception) { false }
}

fun updateCalendarEvent(context: Context, eventId: Long, title: String, startTime: Long, endTime: Long): Boolean {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) return false

    return try {
        val calendarInfo = getGoogleCalendarInfo(context)
        val values = ContentValues().apply {
            put(CalendarContract.Events.TITLE, title)
            put(CalendarContract.Events.DTSTART, startTime)
            put(CalendarContract.Events.DTEND, endTime)
        }
        val updateUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId)
        val rows = context.contentResolver.update(updateUri, values, null, null)
        if (rows > 0) {
            calendarInfo?.let { triggerSync(it.accountName) }
            true
        } else false
    } catch (e: Exception) { false }
}

fun deleteCalendarEvent(context: Context, eventId: Long): Boolean {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) return false

    return try {
        val calendarInfo = getGoogleCalendarInfo(context)
        val deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId)
        val rows = context.contentResolver.delete(deleteUri, null, null)
        if (rows > 0) {
            calendarInfo?.let { triggerSync(it.accountName) }
            true
        } else false
    } catch (e: Exception) { false }
}

private data class CalendarInfo(val id: Long, val accountName: String)

private fun getGoogleCalendarInfo(context: Context): CalendarInfo? {
    val projection = arrayOf(
        CalendarContract.Calendars._ID,
        CalendarContract.Calendars.ACCOUNT_NAME,
        CalendarContract.Calendars.ACCOUNT_TYPE,
        CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL,
        CalendarContract.Calendars.IS_PRIMARY
    )
    
    return try {
        context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            null,
            null,
            null
        )?.use { cursor ->
            var fallback: CalendarInfo? = null
            while (cursor.moveToNext()) {
                val id = cursor.getLong(0)
                val accountName = cursor.getString(1)
                val accountType = cursor.getString(2)
                val accessLevel = cursor.getInt(3)
                val isPrimary = cursor.getInt(4) == 1
                
                if (accessLevel >= CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR) {
                    val info = CalendarInfo(id, accountName)
                    if (accountType == "com.google" && isPrimary) return info
                    if (accountType == "com.google" && fallback == null) fallback = info
                    if (fallback == null) fallback = info
                }
            }
            fallback
        }
    } catch (e: Exception) { null }
}

private fun triggerSync(accountName: String) {
    val account = Account(accountName, "com.google")
    val bundle = Bundle().apply {
        putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true)
        putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true)
    }
    ContentResolver.requestSync(account, CalendarContract.AUTHORITY, bundle)
}
