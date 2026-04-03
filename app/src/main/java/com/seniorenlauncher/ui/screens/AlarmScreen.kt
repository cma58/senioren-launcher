package com.seniorenlauncher.ui.screens

import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.seniorenlauncher.data.model.AlarmEntry
import com.seniorenlauncher.ui.components.ScreenHeader
import java.util.Locale

@Composable
fun AlarmScreen(onBack: () -> Unit, viewModel: AlarmViewModel = viewModel()) {
    val alarms by viewModel.alarms.collectAsState()
    var showDialogFor by remember { mutableStateOf<AlarmEntry?>(null) }
    var isNewAlarm by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp)) {
            ScreenHeader("Wekker", onBack)
            
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
                    viewModel.addAlarm(updatedAlarm.hour, updatedAlarm.minute, updatedAlarm.label, updatedAlarm.daysOfWeek, updatedAlarm.soundUri)
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
    var hour by remember { mutableStateOf(String.format(Locale.getDefault(), "%02d", initialAlarm.hour)) }
    var minute by remember { mutableStateOf(String.format(Locale.getDefault(), "%02d", initialAlarm.minute)) }
    var label by remember { mutableStateOf(initialAlarm.label) }
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

    fun playPreview(uriString: String) {
        try {
            previewRingtone?.stop()
            val ringtone = RingtoneManager.getRingtone(context, Uri.parse(uriString))
            ringtone?.play()
            previewRingtone = ringtone
        } catch (e: Exception) {}
    }

    DisposableEffect(Unit) {
        onDispose {
            previewRingtone?.stop()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
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
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = hour,
                        onValueChange = { if (it.length <= 2 && it.all { c -> c.isDigit() }) hour = it },
                        modifier = Modifier.width(95.dp),
                        textStyle = LocalTextStyle.current.copy(fontSize = 42.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Text(":", fontSize = 42.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                    OutlinedTextField(
                        value = minute,
                        onValueChange = { if (it.length <= 2 && it.all { c -> c.isDigit() }) minute = it },
                        modifier = Modifier.width(95.dp),
                        textStyle = LocalTextStyle.current.copy(fontSize = 42.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Naam van de wekker") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Text("Kies Geluid:", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                var expanded by remember { mutableStateOf(false) }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Box(Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(selectedSound.name, fontSize = 18.sp, maxLines = 1)
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
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(60.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("ANNULEREN", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { 
                            val h = hour.toIntOrNull() ?: 0
                            val m = minute.toIntOrNull() ?: 0
                            if (h in 0..23 && m in 0..59) {
                                onSave(initialAlarm.copy(
                                    hour = h,
                                    minute = m,
                                    label = label,
                                    daysOfWeek = selectedDays.joinToString(","),
                                    soundUri = selectedSound.uri
                                ))
                            }
                        },
                        modifier = Modifier.weight(1f).height(60.dp),
                        enabled = selectedDays.isNotEmpty() && hour.isNotBlank() && minute.isNotBlank(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("OPSLAAN", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
