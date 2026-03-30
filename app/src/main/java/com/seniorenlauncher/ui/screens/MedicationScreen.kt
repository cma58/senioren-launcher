package com.seniorenlauncher.ui.screens

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.AccessTime
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
import com.seniorenlauncher.data.model.Medication
import com.seniorenlauncher.LauncherApp
import com.seniorenlauncher.ui.components.ScreenHeader
import com.seniorenlauncher.util.MedicationAlarmScheduler
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

@Composable
fun MedicationScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dao = LauncherApp.instance.database.medicationDao()
    val medications by dao.getActive().collectAsState(initial = emptyList())
    
    var showAddDialog by remember { mutableStateOf(false) }
    
    val dayNames = listOf("Zo", "Ma", "Di", "Wo", "Do", "Vr", "Za")
    
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(horizontal = 12.dp, vertical = 8.dp)) {
        ScreenHeader(title = "Medicijnen", onBack = onBack)
        
        Box(modifier = Modifier.weight(1f)) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (medications.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                        Text("Geen medicijnen toegevoegd", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("\uD83D\uDC8A", fontSize = 20.sp)
                                Spacer(Modifier.width(8.dp))
                                Text("Mijn Medicijnen", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3B82F6))
                            }
                            Spacer(Modifier.height(8.dp))
                            medications.forEach { med ->
                                val selectedDays = med.daysOfWeek.split(",").filter { it.isNotEmpty() }.map { it.toInt() }
                                val daysSummary = if (selectedDays.size == 7) "Elke dag" 
                                                 else selectedDays.sorted().joinToString(", ") { dayNames[it-1] }

                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(med.name, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                        Text("${med.dose} — ${med.times}", fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(daysSummary, fontSize = 14.sp, color = Color(0xFF3B82F6), fontWeight = FontWeight.Medium)
                                    }
                                    IconButton(onClick = {
                                        scope.launch { 
                                            MedicationAlarmScheduler.cancelAlarms(context, med)
                                            dao.delete(med) 
                                        }
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Verwijder", tint = Color.Red)
                                    }
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(80.dp))
            }
            
            LargeFloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 16.dp, end = 8.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Toevoegen", modifier = Modifier.size(36.dp))
            }
        }
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var dose by remember { mutableStateOf("") }
        val selectedTimes = remember { mutableStateListOf<String>() }
        val selectedDays = remember { mutableStateListOf(1, 2, 3, 4, 5, 6, 7) }
        
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Medicijn Toevoegen", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = name, 
                        onValueChange = { name = it }, 
                        label = { Text("Naam van medicijn") }, 
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = dose, 
                        onValueChange = { dose = it }, 
                        label = { Text("Dosering (bijv. 1 pil)") }, 
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Text("Herhaal op:", fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (i in 1..7) {
                            val isSelected = selectedDays.contains(i)
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        if (isSelected) selectedDays.remove(i) else selectedDays.add(i)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dayNames[i-1].take(1),
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Text("Tijden om in te nemen:", fontWeight = FontWeight.SemiBold)
                    
                    selectedTimes.forEachIndexed { index, time ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(time, modifier = Modifier.weight(1f))
                            IconButton(onClick = { 
                                selectedTimes.removeAt(index)
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Verwijder tijd", tint = Color.Red)
                            }
                        }
                    }
                    
                    Button(
                        onClick = {
                            val now = Calendar.getInstance()
                            TimePickerDialog(context, { _, h, m ->
                                val timeStr = String.format(Locale.getDefault(), "%02d:%02d", h, m)
                                if (!selectedTimes.contains(timeStr)) {
                                    selectedTimes.add(timeStr)
                                    selectedTimes.sort()
                                }
                            }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AccessTime, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Tijd toevoegen")
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = name.isNotBlank() && selectedTimes.isNotEmpty() && selectedDays.isNotEmpty(),
                    onClick = {
                        scope.launch {
                            val newMed = Medication(
                                name = name, 
                                dose = dose, 
                                times = selectedTimes.joinToString(","),
                                daysOfWeek = selectedDays.joinToString(",")
                            )
                            val id = dao.insert(newMed)
                            MedicationAlarmScheduler.scheduleAlarms(context, newMed.copy(id = id))
                            showAddDialog = false
                        }
                    }
                ) { Text("Opslaan") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Annuleren") }
            }
        )
    }
}
