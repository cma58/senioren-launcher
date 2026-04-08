package com.seniorenlauncher.ui.screens

import android.Manifest
import android.app.DatePickerDialog
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.CalendarContract
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.SoundEffectConstants
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.seniorenlauncher.ui.components.ScreenHeader
import com.seniorenlauncher.ui.components.TimeStepper
import com.seniorenlauncher.ui.components.vibrateFeedback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

data class CalendarEventInfo(
    val id: Long,
    val eventId: Long,
    val title: String,
    val startTime: Long,
    val endTime: Long,
    val description: String?,
    val isAllDay: Boolean
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val localeNl = remember { Locale("nl", "BE") }
    
    var canDrawOverlays by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                canDrawOverlays = Settings.canDrawOverlays(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var hasPermission by remember { 
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { perms ->
        hasPermission = perms.values.all { it }
    }

    var events by remember { mutableStateOf<List<CalendarEventInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) tts?.language = localeNl
        }
        onDispose { tts?.stop(); tts?.shutdown() }
    }

    fun refreshEvents() {
        if (!hasPermission) return
        isLoading = true
        scope.launch(Dispatchers.IO) {
            try {
                val fetched = fetchCalendarInstances(context.contentResolver)
                withContext(Dispatchers.Main) {
                    events = fetched
                    isLoading = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { isLoading = false }
            }
        }
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission) refreshEvents()
    }

    val groupedEvents = remember(events) { groupEventsByDay(events, localeNl) }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(horizontal = 12.dp, vertical = 8.dp)) {
        ScreenHeader(title = "Agenda", onBack = onBack)
        
        if (!canDrawOverlays) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clickable {
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
                    context.startActivity(intent)
                },
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFED7D7)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, null, tint = Color(0xFFC53030), modifier = Modifier.size(32.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Zet 'Verschijnen bovenop' AAN voor wekkers.", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC53030))
                }
            }
        }

        if (!hasPermission) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Button(
                    onClick = { launcher.launch(arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR)) },
                    modifier = Modifier.fillMaxWidth().height(120.dp).padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("GEEF TOESTEMMING VOOR AGENDA", fontSize = 20.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                }
            }
        } else {
            if (isLoading && events.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (events.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Geen afspraken in de komende 3 maanden", fontSize = 20.sp, textAlign = TextAlign.Center)
                }
            } else {
                LazyColumn(
                    Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 100.dp, top = 8.dp)
                ) {
                    groupedEvents.forEach { (dayLabel, dayEvents) ->
                        item {
                            Text(text = dayLabel, fontSize = 24.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 8.dp, top = 16.dp, bottom = 8.dp))
                        }
                        items(dayEvents, key = { it.id }) { event ->
                            var showDeleteConfirm by remember { mutableStateOf(false) }
                            EventCard(event = event, onClick = {
                                val dayPrefix = when {
                                    isToday(event.startTime) -> "Vandaag"
                                    isTomorrow(event.startTime) -> "Morgen"
                                    else -> SimpleDateFormat("EEEE d MMMM", localeNl).format(Date(event.startTime))
                                }
                                val timeMsg = if (event.isAllDay) "de hele dag" else "om " + SimpleDateFormat("HH:mm", localeNl).format(Date(event.startTime))
                                val msg = "$dayPrefix, $timeMsg. ${event.title}"
                                tts?.speak(msg, TextToSpeech.QUEUE_FLUSH, null, "EventID")
                            }, onLongClick = { showDeleteConfirm = true })

                            if (showDeleteConfirm) {
                                AlertDialog(
                                    onDismissRequest = { showDeleteConfirm = false },
                                    title = { Text("Verwijderen?") },
                                    text = { Text("Wilt u '${event.title}' verwijderen?") },
                                    confirmButton = {
                                        Button(onClick = {
                                            scope.launch(Dispatchers.IO) {
                                                deleteEvent(context.contentResolver, event.eventId)
                                                refreshEvents()
                                            }
                                            showDeleteConfirm = false
                                        }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("VERWIJDER") }
                                    },
                                    dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("ANNULEREN") } }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (hasPermission) {
        Box(Modifier.fillMaxSize()) {
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp).fillMaxWidth().height(80.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(12.dp))
                Text("NIEUWE AFSPRAAK", fontSize = 22.sp, fontWeight = FontWeight.Black)
            }
        }
    }

    if (showAddDialog) {
        SeniorFriendlyAddEventDialog(
            onDismiss = { showAddDialog = false },
            onSave = { title, time ->
                scope.launch(Dispatchers.IO) {
                    val calId = fetchPrimaryCalendarId(context.contentResolver)
                    if (calId != null) {
                        insertCalendarEvent(context, context.contentResolver, calId, title, time)
                        refreshEvents()
                    }
                }
                showAddDialog = false
            }
        )
    }
}

@Composable
fun SeniorFriendlyAddEventDialog(onDismiss: () -> Unit, onSave: (String, Long) -> Unit) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    val calendar = remember { Calendar.getInstance().apply { add(Calendar.MINUTE, 10); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) } }
    
    var selectedDate by remember { mutableStateOf(calendar.timeInMillis) }
    var hour by remember { mutableIntStateOf(calendar.get(Calendar.HOUR_OF_DAY)) }
    var minute by remember { mutableIntStateOf(calendar.get(Calendar.MINUTE)) }
    
    val dateLabel = SimpleDateFormat("EEEE d MMMM yyyy", Locale("nl", "BE")).format(Date(selectedDate)).uppercase()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                Modifier.padding(24.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text("AFSPRAAK MAKEN", fontSize = 32.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)

                Column(Modifier.fillMaxWidth()) {
                    Text("1. WAT GAAT U DOEN?", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = title, 
                        onValueChange = { title = it }, 
                        placeholder = { Text("Bijv. Kapper of Dokter", fontSize = 20.sp) },
                        modifier = Modifier.fillMaxWidth(), 
                        textStyle = LocalTextStyle.current.copy(fontSize = 26.sp, fontWeight = FontWeight.Bold), 
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = Color.LightGray)
                    )
                }

                Column(Modifier.fillMaxWidth()) {
                    Text("2. WELKE DAG?", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val c = Calendar.getInstance().apply { timeInMillis = selectedDate }
                            val picker = DatePickerDialog(context, { _, y, m, d ->
                                val newCal = Calendar.getInstance().apply { 
                                    set(Calendar.YEAR, y)
                                    set(Calendar.MONTH, m)
                                    set(Calendar.DAY_OF_MONTH, d)
                                }
                                selectedDate = newCal.timeInMillis
                            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
                            picker.show()
                        },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer, contentColor = Color.Black),
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.tertiary)
                    ) {
                        Icon(Icons.Default.CalendarMonth, null, modifier = Modifier.size(36.dp))
                        Spacer(Modifier.width(16.dp))
                        Text(dateLabel, fontSize = 22.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                    }
                }

                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("3. HOE LAAT?", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Spacer(Modifier.height(16.dp))
                    Row(
                        Modifier.fillMaxWidth(), 
                        horizontalArrangement = Arrangement.SpaceEvenly, 
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TimeStepper(
                            value = hour, 
                            range = 0..23, 
                            label = "UUR", 
                            onValueChange = { hour = it }, 
                            fontSize = 64.sp, 
                            buttonSize = 80.dp,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                        Text(":", fontSize = 64.sp, fontWeight = FontWeight.Black)
                        TimeStepper(
                            value = minute, 
                            range = 0..59, 
                            label = "MIN", 
                            onValueChange = { minute = it }, 
                            fontSize = 64.sp, 
                            buttonSize = 80.dp,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(
                        onClick = onDismiss, 
                        modifier = Modifier.weight(1f).height(90.dp), 
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(2.dp, Color.Gray)
                    ) { 
                        Text("STOP", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Gray) 
                    }
                    Button(
                        onClick = {
                            val finalCal = Calendar.getInstance().apply { 
                                timeInMillis = selectedDate
                                set(Calendar.HOUR_OF_DAY, hour)
                                set(Calendar.MINUTE, minute)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            if (title.isNotBlank()) onSave(title, finalCal.timeInMillis)
                        }, 
                        modifier = Modifier.weight(1.5f).height(90.dp), 
                        shape = RoundedCornerShape(20.dp), 
                        enabled = title.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) { 
                        Text("OPSLAAN", fontSize = 26.sp, fontWeight = FontWeight.Black) 
                    }
                }
            }
        }
    }
}

private suspend fun insertCalendarEvent(context: Context, contentResolver: ContentResolver, calId: Long, title: String, startTime: Long) {
    try {
        val values = ContentValues().apply {
            put(CalendarContract.Events.DTSTART, startTime)
            put(CalendarContract.Events.DTEND, startTime + 3600000)
            put(CalendarContract.Events.TITLE, title)
            put(CalendarContract.Events.CALENDAR_ID, calId)
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            put(CalendarContract.Events.HAS_ALARM, 1)
        }
        val uri = contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
        val eventID = uri?.lastPathSegment?.toLongOrNull()
        if (eventID != null) {
            val reminderValues = ContentValues().apply { put(CalendarContract.Reminders.EVENT_ID, eventID); put(CalendarContract.Reminders.MINUTES, 15); put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT) }
            contentResolver.insert(CalendarContract.Reminders.CONTENT_URI, reminderValues)
            withContext(Dispatchers.Main) { Toast.makeText(context, "Afspraak opgeslagen!", Toast.LENGTH_SHORT).show() }
        }
    } catch (e: Exception) { Log.e("Calendar", "Insert error", e) }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EventCard(event: CalendarEventInfo, onClick: () -> Unit, onLongClick: () -> Unit) {
    val isPast = !event.isAllDay && event.endTime < System.currentTimeMillis()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 90.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongClick() }
                )
            }, 
        shape = RoundedCornerShape(20.dp), 
        colors = CardDefaults.cardColors(containerColor = if (isPast) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = if (event.isAllDay) "Hele dag" else SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(event.startTime)), fontSize = 24.sp, fontWeight = FontWeight.Black, color = if (isPast) Color.Gray else MaterialTheme.colorScheme.primary, modifier = Modifier.width(70.dp))
            Spacer(Modifier.width(12.dp))
            Box(Modifier.width(2.dp).height(40.dp).background(if (isPast) Color.Gray else MaterialTheme.colorScheme.primary))
            Spacer(Modifier.width(20.dp))
            Text(text = event.title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = if (isPast) Color.Gray else Color.Black, modifier = Modifier.weight(1f))
        }
    }
}

private fun fetchCalendarInstances(contentResolver: ContentResolver): List<CalendarEventInfo> {
    val eventList = mutableListOf<CalendarEventInfo>()
    val now = System.currentTimeMillis()
    val end = now + (90L * 24 * 60 * 60 * 1000)
    val uriBuilder = CalendarContract.Instances.CONTENT_URI.buildUpon()
    ContentUris.appendId(uriBuilder, now); ContentUris.appendId(uriBuilder, end)
    val projection = arrayOf(CalendarContract.Instances._ID, CalendarContract.Instances.EVENT_ID, CalendarContract.Instances.TITLE, CalendarContract.Instances.BEGIN, CalendarContract.Instances.END, CalendarContract.Instances.DESCRIPTION, CalendarContract.Instances.ALL_DAY)
    try {
        contentResolver.query(uriBuilder.build(), projection, null, null, CalendarContract.Instances.BEGIN + " ASC")?.use { cursor ->
            val idIdx = cursor.getColumnIndexOrThrow(CalendarContract.Instances._ID)
            val eventIdIdx = cursor.getColumnIndexOrThrow(CalendarContract.Instances.EVENT_ID)
            val titleIdx = cursor.getColumnIndexOrThrow(CalendarContract.Instances.TITLE)
            val beginIdx = cursor.getColumnIndexOrThrow(CalendarContract.Instances.BEGIN)
            val endIdx = cursor.getColumnIndexOrThrow(CalendarContract.Instances.END)
            val allDayIdx = cursor.getColumnIndexOrThrow(CalendarContract.Instances.ALL_DAY)
            while (cursor.moveToNext()) {
                eventList.add(CalendarEventInfo(cursor.getLong(idIdx), cursor.getLong(eventIdIdx), cursor.getString(titleIdx) ?: "(Geen titel)", cursor.getLong(beginIdx), cursor.getLong(endIdx), null, cursor.getInt(allDayIdx) != 0))
            }
        }
    } catch (e: Exception) { Log.e("Calendar", "Query error", e) }
    return eventList
}

private fun fetchPrimaryCalendarId(contentResolver: ContentResolver): Long? {
    try {
        contentResolver.query(CalendarContract.Calendars.CONTENT_URI, arrayOf(CalendarContract.Calendars._ID, CalendarContract.Calendars.IS_PRIMARY), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idIdx = cursor.getColumnIndex(CalendarContract.Calendars._ID)
                val priIdx = cursor.getColumnIndex(CalendarContract.Calendars.IS_PRIMARY)
                var firstId: Long? = null
                do {
                    val id = cursor.getLong(idIdx)
                    if (firstId == null) firstId = id
                    if (priIdx != -1 && cursor.getInt(priIdx) == 1) return id
                } while (cursor.moveToNext())
                return firstId
            }
        }
    } catch (e: Exception) { }
    return null
}

private fun deleteEvent(contentResolver: ContentResolver, eventId: Long) {
    contentResolver.delete(ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId), null, null)
}

private fun groupEventsByDay(events: List<CalendarEventInfo>, locale: Locale): Map<String, List<CalendarEventInfo>> {
    return events.groupBy { event ->
        when {
            isToday(event.startTime) -> "VANDAAG"
            isTomorrow(event.startTime) -> "MORGEN"
            else -> SimpleDateFormat("EEEE d MMMM", locale).format(Date(event.startTime)).uppercase()
        }
    }
}

private fun isToday(time: Long): Boolean {
    val c1 = Calendar.getInstance(); val c2 = Calendar.getInstance().apply { timeInMillis = time }
    return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
}

private fun isTomorrow(time: Long): Boolean {
    val c1 = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }; val c2 = Calendar.getInstance().apply { timeInMillis = time }
    return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
}
