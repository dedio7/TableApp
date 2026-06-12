package com.dedio.dailypulse.calendar

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dedio.dailypulse.ui.i18n.LocalStrings
import java.util.*

@Composable
fun CalendarDetailsPanel(
    visible: Boolean,
    onDismiss: () -> Unit
) {
    val strings = LocalStrings.current
    val context = LocalContext.current
    
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var currentMonth by remember { mutableStateOf(Calendar.getInstance()) }
    var refreshTrigger by remember { mutableIntStateOf(0) }
    val calendarState = rememberCalendarEvents(selectedDate)
    
    // Fetch days with events for the current month view
    val daysWithEvents = remember(currentMonth, calendarState, refreshTrigger) {
        getDaysWithEvents(context, currentMonth)
    }
    
    var showAddEventDialog by remember { mutableStateOf(false) }

    if (!visible) return

    Popup(
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .width(600.dp)
                    .fillMaxHeight(0.9f)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFF1A1A2E))
                    .clickable(enabled = false) { }
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.calendarMonthlyView,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(onClick = { showAddEventDialog = true }) {
                        Text("＋", fontSize = 20.sp, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Month Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        currentMonth = (currentMonth.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
                    }) {
                        Text("◀", color = Color.White)
                    }
                    
                    val monthName = currentMonth.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
                    val year = currentMonth.get(Calendar.YEAR)
                    Text(
                        text = "$monthName $year",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    IconButton(onClick = {
                        currentMonth = (currentMonth.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
                    }) {
                        Text("▶", color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Monthly Grid
                CalendarGrid(
                    currentMonth = currentMonth,
                    selectedDate = selectedDate,
                    daysWithEvents = daysWithEvents,
                    onDateSelected = { selectedDate = it }
                )

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(24.dp))

                // Day Events
                Text(
                    text = if (isToday(selectedDate)) strings.todayLabel.uppercase() else 
                        "${selectedDate.get(Calendar.DAY_OF_MONTH)} ${selectedDate.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())}".uppercase(),
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(calendarState.events) { event ->
                        CalendarEventRow(event)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Close Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(text = strings.close, color = Color.White)
                }
            }
        }
        
        if (showAddEventDialog) {
            AddEventDialog(
                selectedDate = selectedDate,
                onDismiss = { showAddEventDialog = false },
                onEventAdded = { title, start, end ->
                    val success = addCalendarEvent(context, title, start, end)
                    if (success) {
                        showAddEventDialog = false
                        // Force refresh of events by cloning selected date and incrementing trigger
                        selectedDate = (selectedDate.clone() as Calendar)
                        refreshTrigger++
                        Toast.makeText(context, "Evento salvato", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Errore nel salvataggio", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }
}

@Composable
fun CalendarGrid(
    currentMonth: Calendar,
    selectedDate: Calendar,
    daysWithEvents: Set<Int>,
    onDateSelected: (Calendar) -> Unit
) {
    val daysInMonth = currentMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfMonth = (currentMonth.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
    val startOffset = (firstDayOfMonth.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7

    val days = (1..daysInMonth).toList()
    val totalGridItems = days.size + startOffset

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.height(240.dp)
    ) {
        val weekDays = listOf("Lun", "Mar", "Mer", "Gio", "Ven", "Sab", "Dom")
        items(weekDays) { day ->
            Text(
                text = day,
                color = Color.White.copy(alpha = 0.3f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        items((0 until totalGridItems).toList()) { index ->
            if (index < startOffset) {
                Spacer(modifier = Modifier.size(40.dp))
            } else {
                val day = days[index - startOffset]
                val date = (currentMonth.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, day) }
                val isSelected = date.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR) &&
                                 date.get(Calendar.DAY_OF_YEAR) == selectedDate.get(Calendar.DAY_OF_YEAR)
                val isToday = isToday(date)
                val hasEvents = daysWithEvents.contains(day)

                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color(0xFF4FC3F7) else if (isToday) Color.White.copy(alpha = 0.1f) else Color.Transparent)
                        .clickable { onDateSelected(date) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = day.toString(),
                            color = if (isSelected) Color.Black else Color.White,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
                        )
                        if (hasEvents) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) Color.Black.copy(alpha = 0.5f) else Color(0xFF4FC3F7))
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarEventRow(event: CalendarEvent) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(4.dp, 20.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(event.color)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = event.title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(text = event.time, color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventDialog(
    selectedDate: Calendar,
    onDismiss: () -> Unit,
    onEventAdded: (String, Long, Long) -> Unit
) {
    val strings = LocalStrings.current
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var hour by remember { mutableIntStateOf(12) }
    var minute by remember { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.calendarAddEvent) },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(strings.calendarEventTitle) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Orario: ", color = Color.White)
                    TimeValuePicker(value = hour, range = 0..23, onValueChange = { hour = it })
                    Text(" : ", color = Color.White)
                    TimeValuePicker(value = minute, range = 0..59, onValueChange = { minute = it })
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (title.isNotBlank()) {
                    val start = (selectedDate.clone() as Calendar).apply {
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, minute)
                    }.timeInMillis
                    val end = start + 3600000 // 1 hour duration
                    onEventAdded(title, start, end)
                } else {
                    Toast.makeText(context, "Inserisci un titolo", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text(strings.calendarSave)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.calendarCancel)
            }
        }
    )
}

@Composable
fun TimeValuePicker(value: Int, range: IntRange, onValueChange: (Int) -> Unit) {
    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
            .clickable { 
                val next = if (value >= range.last) range.first else value + 1
                onValueChange(next)
            }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = String.format("%02d", value), color = Color.White)
    }
}

private fun isToday(date: Calendar): Boolean {
    val today = Calendar.getInstance()
    return date.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            date.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
}
