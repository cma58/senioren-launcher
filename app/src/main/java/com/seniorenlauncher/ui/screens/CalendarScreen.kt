package com.seniorenlauncher.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seniorenlauncher.LauncherApp
import com.seniorenlauncher.data.model.CalendarEvent
import com.seniorenlauncher.ui.components.ScreenHeader
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalendarScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val dao = LauncherApp.instance.database.calendarDao()
    
    // We tonen events van vandaag en de toekomst
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }
    val startTimestamp = calendar.timeInMillis
    val endTimestamp = startTimestamp + (1000L * 60 * 60 * 24 * 365) // 1 jaar vooruit
    
    val events by dao.getEventsInRange(startTimestamp, endTimestamp).collectAsState(initial = emptyList())
    
    var showAddDialog by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(horizontal = 12.dp, vertical = 8.dp)) {
        ScreenHeader(title = "Agenda", onBack = onBack)
        
        if (events.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📅", fontSize = 80.sp)
                    Spacer(Modifier.height(16.dp))
                    Text("Geen afspraken gepland", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp, top = 8.dp)
            ) {
                val groupedEvents = events.groupBy { 
                    SimpleDateFormat("EEEE d MMMM", Locale.getDefault()).format(Date(it.dateTime))
                }
                
                groupedEvents.forEach { (date, dayEvents) ->
                    item {
                        Text(
                            text = date.uppercase(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                        )
                    }
                    items(dayEvents) { event ->
                        EventCard(event)
                    }
                }
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        LargeFloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(20.dp)
        ) {
            Icon(Icons.Default.Add, "Nieuwe afspraak", modifier = Modifier.size(40.dp))
        }
    }

    if (showAddDialog) {
        AddEventDialog(
            onDismiss = { showAddDialog = false },
            onSave = { title, time ->
                scope.launch {
                    dao.insert(CalendarEvent(title = title, dateTime = time))
                }
                showAddDialog = false
            }
        )
    }
}

@Composable
fun EventCard(event: CalendarEvent) {
    val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(event.dateTime))
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(60.dp)) {
                Text(timeStr, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.width(12.dp))
            VerticalDivider(modifier = Modifier.height(40.dp), thickness = 2.dp, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            Spacer(Modifier.width(12.dp))
            Text(event.title, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun AddEventDialog(onDismiss: () -> Unit, onSave: (String, Long) -> Unit) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    val calendar = remember { Calendar.getInstance() }
    
    var dateLabel by remember { 
        mutableStateOf(SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).format(calendar.time)) 
    }
    var timeLabel by remember { 
        mutableStateOf(SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.time)) 
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nieuwe Afspraak", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Wat gaat u doen?") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Button(
                    onClick = {
                        DatePickerDialog(context, { _, y, m, d ->
                            calendar.set(Calendar.YEAR, y)
                            calendar.set(Calendar.MONTH, m)
                            calendar.set(Calendar.DAY_OF_MONTH, d)
                            dateLabel = SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).format(calendar.time)
                        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.filledTonalButtonColors()
                ) {
                    Icon(Icons.Default.CalendarMonth, null)
                    Spacer(Modifier.width(8.dp))
                    Text(dateLabel)
                }

                Button(
                    onClick = {
                        TimePickerDialog(context, { _, h, m ->
                            calendar.set(Calendar.HOUR_OF_DAY, h)
                            calendar.set(Calendar.MINUTE, m)
                            timeLabel = SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.time)
                        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.filledTonalButtonColors()
                ) {
                    Icon(Icons.Default.Event, null)
                    Spacer(Modifier.width(8.dp))
                    Text(timeLabel)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank()) onSave(title, calendar.timeInMillis) },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Opslaan", fontSize = 18.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuleren", fontSize = 18.sp)
            }
        }
    )
}
