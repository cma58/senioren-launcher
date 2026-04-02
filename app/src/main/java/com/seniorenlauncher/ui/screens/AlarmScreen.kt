package com.seniorenlauncher.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
    var showAddDialog by remember { mutableStateOf(false) }

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
                            onDelete = { viewModel.deleteAlarm(alarm) }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .size(72.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Wekker toevoegen", modifier = Modifier.size(36.dp))
        }
    }

    if (showAddDialog) {
        AddAlarmDialog(
            onDismiss = { showAddDialog = false },
            onSave = { hour, minute, label, days ->
                viewModel.addAlarm(hour, minute, label, days)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AlarmItem(alarm: AlarmEntry, onToggle: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAlarmDialog(onDismiss: () -> Unit, onSave: (Int, Int, String, String) -> Unit) {
    var hour by remember { mutableStateOf(8) }
    var minute by remember { mutableStateOf(0) }
    var label by remember { mutableStateOf("") }
    val selectedDays = remember { mutableStateListOf(1, 2, 3, 4, 5, 6, 7) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(28.dp)
        ) {
            Column(
                Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Nieuwe Wekker", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    NumberPicker(value = hour, range = 0..23, onValueChange = { hour = it })
                    Text(":", fontSize = 32.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                    NumberPicker(value = minute, range = 0..59, onValueChange = { minute = it })
                }
                
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Naam (bijv. Medicijnen)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Text("Dagen:", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    val dayNames = listOf("Zo", "Ma", "Di", "Wo", "Do", "Vr", "Za")
                    for (i in 1..7) {
                        val isSelected = selectedDays.contains(i)
                        Box(
                            Modifier
                                .size(36.dp)
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
                                fontSize = 12.sp, 
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
                        Text("ANNULEREN")
                    }
                    Button(
                        onClick = { 
                            onSave(hour, minute, label, selectedDays.joinToString(",")) 
                        },
                        modifier = Modifier.weight(1f).height(60.dp),
                        enabled = selectedDays.isNotEmpty(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("OPSLAAN")
                    }
                }
            }
        }
    }
}

@Composable
fun NumberPicker(value: Int, range: IntRange, onValueChange: (Int) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = { if (value < range.last) onValueChange(value + 1) else onValueChange(range.first) }) {
            Icon(Icons.Default.KeyboardArrowUp, null, Modifier.size(40.dp))
        }
        Text(
            text = String.format(Locale.getDefault(), "%02d", value),
            fontSize = 48.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary
        )
        IconButton(onClick = { if (value > range.first) onValueChange(value - 1) else onValueChange(range.last) }) {
            Icon(Icons.Default.KeyboardArrowDown, null, Modifier.size(40.dp))
        }
    }
}
