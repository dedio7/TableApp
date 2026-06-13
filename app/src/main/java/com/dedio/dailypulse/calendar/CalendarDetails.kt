package com.dedio.dailypulse.calendar

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.dedio.dailypulse.ui.i18n.LocalStrings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    val scope = rememberCoroutineScope()
    
    val calendarState = rememberCalendarEvents(selectedDate, refreshTrigger)
    val daysWithEvents = remember(currentMonth, calendarState, refreshTrigger) {
        getDaysWithEvents(context, currentMonth)
    }
    
    var editingEvent by remember { mutableStateOf<CalendarEvent?>(null) }
    var showEventDialog by remember { mutableStateOf(false) }

    if (!visible) return

    Popup(
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.82f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            val isSmallHeight = maxHeight < 520.dp
            val mainScrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .widthIn(max = if (isSmallHeight) 850.dp else 600.dp)
                    .fillMaxWidth(0.94f)
                    .fillMaxHeight(0.92f)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFF161B33))
                    .clickable(enabled = false) { }
                    .padding(if (isSmallHeight) 16.dp else 24.dp)
                    // Ensure the whole dialog can scroll if content overflows the height
                    .verticalScroll(mainScrollState)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.settingsTitle,
                        color = Color.White,
                        fontSize = if (isSmallHeight) 18.sp else 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(onClick = { 
                        editingEvent = null
                        showEventDialog = true 
                    }) {
                        Text("＋", fontSize = 22.sp, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(if (isSmallHeight) 8.dp else 16.dp))

                if (isSmallHeight) {
                    Row(
                        modifier = Modifier.height(300.dp), // Fixed height to enable internal scrolling
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Left: Calendar
                        Column(modifier = Modifier.weight(1.2f)) {
                            MonthSelector(currentMonth, onMonthChange = { currentMonth = it }, isSmall = true)
                            Spacer(modifier = Modifier.height(8.dp))
                            CalendarGrid(currentMonth, selectedDate, daysWithEvents, onDateSelected = { selectedDate = it }, isSmall = true)
                        }

                        // Right: Events (using LazyColumn for better touch handling)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = getSelectedDateTitle(selectedDate, strings),
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(bottom = 8.dp)
                            ) {
                                items(calendarState.events) { event ->
                                    CalendarEventRow(event, onClick = {
                                        if (event.id != -1L) {
                                            editingEvent = event
                                            showEventDialog = true
                                        }
                                    })
                                }
                            }
                        }
                    }
                } else {
                    MonthSelector(currentMonth, onMonthChange = { currentMonth = it })
                    Spacer(modifier = Modifier.height(16.dp))
                    CalendarGrid(currentMonth, selectedDate, daysWithEvents, onDateSelected = { selectedDate = it })
                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = getSelectedDateTitle(selectedDate, strings),
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Fixed height for list in vertical mode to keep Close button visible
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 200.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        items(calendarState.events) { event ->
                            CalendarEventRow(event, onClick = {
                                if (event.id != -1L) {
                                    editingEvent = event
                                    showEventDialog = true
                                }
                            })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Close Button - Pushed to bottom
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
        
        if (showEventDialog) {
            EventDialog(
                selectedDate = selectedDate,
                event = editingEvent,
                onDismiss = { showEventDialog = false },
                onEventSaved = { title, start, end ->
                    val success = if (editingEvent == null) {
                        addCalendarEvent(context, title, start, end)
                    } else {
                        updateCalendarEvent(context, editingEvent!!.id, title, start, end)
                    }
                    if (success) {
                        scope.launch { delay(200); refreshTrigger++; showEventDialog = false }
                        Toast.makeText(context, "Evento salvato", Toast.LENGTH_SHORT).show()
                    }
                },
                onEventDeleted = { id ->
                    if (deleteCalendarEvent(context, id)) {
                        scope.launch { delay(200); refreshTrigger++; showEventDialog = false }
                        Toast.makeText(context, "Evento eliminato", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }
}

@Composable
fun MonthSelector(currentMonth: Calendar, onMonthChange: (Calendar) -> Unit, isSmall: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = {
            onMonthChange((currentMonth.clone() as Calendar).apply { add(Calendar.MONTH, -1) })
        }) {
            Text("◀", color = Color.White, fontSize = if (isSmall) 16.sp else 20.sp)
        }
        
        val monthName = currentMonth.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
        val year = currentMonth.get(Calendar.YEAR)
        Text(
            text = "$monthName $year",
            color = Color.White,
            fontSize = if (isSmall) 16.sp else 18.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        IconButton(onClick = {
            onMonthChange((currentMonth.clone() as Calendar).apply { add(Calendar.MONTH, 1) })
        }) {
            Text("▶", color = Color.White, fontSize = if (isSmall) 16.sp else 20.sp)
        }
    }
}

@Composable
fun CalendarGrid(
    currentMonth: Calendar,
    selectedDate: Calendar,
    daysWithEvents: Set<Int>,
    onDateSelected: (Calendar) -> Unit,
    isSmall: Boolean = false
) {
    val daysInMonth = currentMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfMonth = (currentMonth.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
    val startOffset = (firstDayOfMonth.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7

    val days = (1..daysInMonth).toList()
    val totalGridItems = days.size + startOffset

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.height(if (isSmall) 160.dp else 240.dp),
        userScrollEnabled = false // Prevent grid from fighting with main scroll
    ) {
        val weekDays = listOf("Lun", "Mar", "Mer", "Gio", "Ven", "Sab", "Dom")
        items(weekDays) { day ->
            Text(
                text = day,
                color = Color.White.copy(alpha = 0.3f),
                fontSize = if (isSmall) 10.sp else 11.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }

        items((0 until totalGridItems).toList()) { index ->
            if (index < startOffset) {
                Spacer(modifier = Modifier.size(if (isSmall) 24.dp else 40.dp))
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
                        .padding(if (isSmall) 1.dp else 4.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color(0xFF4FC3F7) else if (isToday) Color.White.copy(alpha = 0.1f) else Color.Transparent)
                        .clickable { onDateSelected(date) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = day.toString(),
                            color = if (isSelected) Color.Black else Color.White,
                            fontSize = if (isSmall) 11.sp else 14.sp,
                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
                        )
                        if (hasEvents) {
                            Box(
                                modifier = Modifier
                                    .size(if (isSmall) 3.dp else 4.dp)
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
fun CalendarEventRow(event: CalendarEvent, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .clickable { onClick() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(4.dp, 16.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(event.color)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = event.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(text = event.time, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDialog(
    selectedDate: Calendar,
    event: CalendarEvent?,
    onDismiss: () -> Unit,
    onEventSaved: (String, Long, Long) -> Unit,
    onEventDeleted: (Long) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(event?.title ?: "") }
    val initialStart = event?.startTime ?: (selectedDate.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, 12); set(Calendar.MINUTE, 0)
    }.timeInMillis
    val startCal = Calendar.getInstance().apply { timeInMillis = initialStart }
    var hour by remember { mutableIntStateOf(startCal.get(Calendar.HOUR_OF_DAY)) }
    var minute by remember { mutableIntStateOf(startCal.get(Calendar.MINUTE)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (event == null) "Aggiungi Evento" else "Modifica Evento", color = Color.White) },
        containerColor = Color(0xFF1A1A2E),
        text = {
            Column {
                OutlinedTextField(
                    value = title, onValueChange = { title = it },
                    label = { Text("Titolo Evento") }, singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF4FC3F7), unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Orario: ", color = Color.White)
                    TimeValuePicker(value = hour, range = 0..23, onValueChange = { hour = it })
                    Text(" : ", color = Color.White)
                    TimeValuePicker(value = minute, range = 0..59, onValueChange = { minute = it })
                }
                if (event != null) {
                    Spacer(modifier = Modifier.height(24.dp))
                    TextButton(onClick = { onEventDeleted(event.id) }, colors = ButtonDefaults.textButtonColors(contentColor = Color.Red.copy(alpha = 0.8f))) {
                        Text("Elimina Evento")
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (title.isNotBlank()) {
                    val start = (selectedDate.clone() as Calendar).apply { set(Calendar.HOUR_OF_DAY, hour); set(Calendar.MINUTE, minute) }.timeInMillis
                    onEventSaved(title, start, start + 3600000)
                } else Toast.makeText(context, "Inserisci un titolo", Toast.LENGTH_SHORT).show()
            }) { Text("Salva") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annulla") } }
    )
}

@Composable
fun TimeValuePicker(value: Int, range: IntRange, onValueChange: (Int) -> Unit) {
    Box(
        modifier = Modifier.padding(horizontal = 4.dp).border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
            .clickable { val next = if (value >= range.last) range.first else value + 1; onValueChange(next) }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) { Text(text = String.format("%02d", value), color = Color.White) }
}

private fun isToday(date: Calendar): Boolean {
    val today = Calendar.getInstance()
    return date.get(Calendar.YEAR) == today.get(Calendar.YEAR) && date.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
}

private fun getSelectedDateTitle(date: Calendar, strings: com.dedio.dailypulse.ui.i18n.Strings): String {
    return if (isToday(date)) strings.todayLabel.uppercase() 
    else "${date.get(Calendar.DAY_OF_MONTH)} ${date.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())}".uppercase()
}
