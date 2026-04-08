package com.seniorenlauncher.ui.screens

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.seniorenlauncher.data.model.AlarmEntry
import com.seniorenlauncher.ui.components.ScreenHeader
import com.seniorenlauncher.ui.components.TimeStepper
import java.util.Locale

@Composable
fun AlarmScreen(onBack: () -> Unit, viewModel: AlarmViewModel = viewModel()) {
    val alarms by viewModel.alarms.collectAsState()
    var showDialogFor by remember { mutableStateOf<AlarmEntry?>(null) }
    var isNewAlarm by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // --- Taak 1: Exact Alarm Permissie Banner ---
    val alarmManager = remember { context.getSystemService(Context.ALARM_SERVICE) as AlarmManager }
    var canScheduleExact by remember { 
        mutableStateOf(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) alarmManager.canScheduleExactAlarms() else true) 
    }

    // Refresh check als de gebruiker terugkomt van instellingen
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) alarmManager.canScheduleExactAlarms() else true
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp)) {
            ScreenHeader("Wekker", onBack)
            
            if (!canScheduleExact) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                }
                                context.startActivity(intent)
                            }
                        },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE082))
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, null, tint = Color(0xFFE65100))
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Toestemming nodig voor nauwkeurige wekkers. Tik hier.",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100)
                        )
                    }
                }
            }

            if (alarms.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Geen wekkers ingesteld", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(alarms) { alarm ->
                        AlarmItem(
                            alarm = alarm,
                            onToggle = { viewModel.toggleAlarm(alarm) },
                            onDelete = { viewModel.deleteAlarm(alarm) },
                            onEdit = {
                                showDialogFor = alarm
                                isNewAlarm = false
                            }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                showDialogFor = AlarmEntry(hour = 8, minute = 0, label = "")
                isNewAlarm = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .size(72.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Wekker toevoegen", modifier = Modifier.size(36.dp))
        }
    }

    if (showDialogFor != null) {
        AlarmEditDialog(
            initialAlarm = showDialogFor!!,
            isNew = isNewAlarm,
            onDismiss = { showDialogFor = null },
            onSave = { updatedAlarm ->
                if (isNewAlarm) {
                    viewModel.addAlarm(
                        updatedAlarm.hour, 
                        updatedAlarm.minute, 
                        updatedAlarm.label, 
                        updatedAlarm.daysOfWeek, 
                        updatedAlarm.soundUri,
                        updatedAlarm.isMorningRoutine
                    )
                } else {
                    viewModel.updateAlarm(updatedAlarm)
                }
                showDialogFor = null
            }
        )
    }
}

@Composable
fun AlarmItem(alarm: AlarmEntry, onToggle: () -> Unit, onDelete: () -> Unit, onEdit: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (alarm.enabled) MaterialTheme.colorScheme.primaryContainer 
                            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = String.format(Locale.getDefault(), "%02d:%02d", alarm.hour, alarm.minute),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (alarm.enabled) MaterialTheme.colorScheme.onPrimaryContainer 
                                else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (alarm.label.isNotBlank()) {
                        Text(alarm.label, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                    }
                    if (alarm.isMorningRoutine) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                            Icon(Icons.Default.WbSunny, null, Modifier.size(16.dp), tint = Color(0xFFE65100))
                            Spacer(Modifier.width(4.dp))
                            Text("Met ochtendritueel", fontSize = 14.sp, color = Color(0xFFE65100))
                        }
                    }
                }
                
                Switch(
                    checked = alarm.enabled,
                    onCheckedChange = { onToggle() },
                    modifier = Modifier.scale(1.2f)
                )

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Verwijderen", tint = Color.Red)
                }
            }
            
            Text(
                text = getDaysDisplay(alarm.daysOfWeek),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

fun getDaysDisplay(daysString: String): String {
    val days = daysString.split(",").filter { it.isNotBlank() }.map { it.toInt() }
    if (days.size == 7) return "Elke dag"
    if (days.isEmpty()) return "Nooit"
    
    val dayNames = listOf("Zo", "Ma", "Di", "Wo", "Do", "Vr", "Za")
    return days.sorted().joinToString(", ") { dayNames[it - 1] }
}

data class SoundInfo(val name: String, val uri: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmEditDialog(initialAlarm: AlarmEntry, isNew: Boolean, onDismiss: () -> Unit, onSave: (AlarmEntry) -> Unit) {
    val context = LocalContext.current
    var hour by remember { mutableIntStateOf(initialAlarm.hour) }
    var minute by remember { mutableIntStateOf(initialAlarm.minute) }
    var label by remember { mutableStateOf(initialAlarm.label) }
    var isMorningRoutine by remember { mutableStateOf(initialAlarm.isMorningRoutine) }
    
    val selectedDays = remember { 
        mutableStateListOf<Int>().apply { 
            val days = initialAlarm.daysOfWeek.split(",").filter { it.isNotBlank() }.map { it.toInt() }
            addAll(if (days.isEmpty() && isNew) listOf(1,2,3,4,5,6,7) else days) 
        } 
    }
    
    // Fetch real system sounds
    val systemSounds = remember {
        val list = mutableListOf<SoundInfo>()
        list.add(SoundInfo("Standaard", RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString()))
        
        val rm = RingtoneManager(context)
        rm.setType(RingtoneManager.TYPE_ALARM)
        val cursor = rm.cursor
        var count = 0
        while (cursor.moveToNext() && count < 15) {
            val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
            val uri = rm.getRingtoneUri(cursor.position).toString()
            if (!list.any { it.name == title }) {
                list.add(SoundInfo(title, uri))
                count++
            }
        }
        list
    }

    var selectedSound by remember { 
        mutableStateOf(systemSounds.find { it.uri == initialAlarm.soundUri } ?: systemSounds.first()) 
    }
    
    var previewRingtone by remember { mutableStateOf<Ringtone?>(null) }

    fun stopPreview() {
        try {
            previewRingtone?.stop()
            previewRingtone = null
        } catch (e: Exception) {}
    }

    fun playPreview(uriString: String) {
        try {
            stopPreview()
            val ringtone = RingtoneManager.getRingtone(context, Uri.parse(uriString))
            ringtone?.play()
            previewRingtone = ringtone
        } catch (e: Exception) {}
    }

    DisposableEffect(Unit) {
        onDispose {
            stopPreview()
        }
    }

    Dialog(onDismissRequest = { 
        stopPreview()
        onDismiss() 
    }) {
        Card(
            Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(28.dp)
        ) {
            Column(
                Modifier.padding(24.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(if (isNew) "Nieuwe Wekker" else "Wekker Aanpassen", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                
                // --- Taak 2: TimeStepper (Plus/Min Knoppen) ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TimeStepper(
                        value = hour,
                        range = 0..23,
                        label = "Uren",
                        onValueChange = { hour = it },
                        fontSize = 52.sp,
                        buttonSize = 64.dp
                    )
                    Text(":", fontSize = 48.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp))
                    TimeStepper(
                        value = minute,
                        range = 0..59,
                        label = "Minuten",
                        onValueChange = { minute = it },
                        fontSize = 52.sp,
                        buttonSize = 64.dp
                    )
                }
                
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Naam van de wekker") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // --- Taak 2: Ochtendritueel Optie ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                        .clickable { isMorningRoutine = !isMorningRoutine }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Ochtendritueel", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text("Weerbericht openen", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = isMorningRoutine,
                            onCheckedChange = { isMorningRoutine = it },
                            modifier = Modifier.scale(1.1f)
                        )
                    }
                    Text(
                        "Indien ingeschakeld opent de app 's ochtends automatisch het weerbericht na het uitzetten.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Text("Kies Geluid:", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                var expanded by remember { mutableStateOf(false) }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Box(Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            val displayText = selectedSound.name
                            Text(displayText, fontSize = 18.sp, maxLines = 1)
                            Spacer(Modifier.weight(1f))
                            Icon(Icons.Default.ArrowDropDown, null)
                        }
                        DropdownMenu(
                            expanded = expanded, 
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.fillMaxWidth(0.7f).heightIn(max = 300.dp)
                        ) {
                            systemSounds.forEach { sound ->
                                DropdownMenuItem(
                                    text = { Text(sound.name, fontSize = 18.sp) },
                                    onClick = {
                                        selectedSound = sound
                                        expanded = false
                                        playPreview(sound.uri)
                                    }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = { playPreview(selectedSound.uri) },
                        modifier = Modifier.size(56.dp).background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.Default.PlayArrow, "Beluister", tint = MaterialTheme.colorScheme.primary)
                    }
                }

                Text("Dagen:", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    val dayNames = listOf("Zo", "Ma", "Di", "Wo", "Do", "Vr", "Za")
                    for (i in 1..7) {
                        val isSelected = selectedDays.contains(i)
                        Box(
                            Modifier
                                .size(38.dp)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    if (isSelected) selectedDays.remove(i) else selectedDays.add(i)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                dayNames[i-1], 
                                fontSize = 14.sp, 
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else Color.Black
                            )
                        }
                    }
                }
                
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = {
                            stopPreview()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f).height(60.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("ANNULEREN", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { 
                            stopPreview()
                            onSave(initialAlarm.copy(
                                hour = hour,
                                minute = minute,
                                label = label,
                                daysOfWeek = selectedDays.joinToString(","),
                                soundUri = selectedSound.uri,
                                isMorningRoutine = isMorningRoutine
                            ))
                        },
                        modifier = Modifier.weight(1f).height(60.dp),
                        enabled = selectedDays.isNotEmpty(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("OPSLAAN", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
