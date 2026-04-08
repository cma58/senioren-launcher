package com.seniorenlauncher.ui.screens

import android.app.TimePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Inventory
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.seniorenlauncher.data.model.Medication
import com.seniorenlauncher.data.model.MedicationLog
import com.seniorenlauncher.LauncherApp
import com.seniorenlauncher.ui.components.ScreenHeader
import com.seniorenlauncher.util.MedicationAlarmScheduler
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MedicationScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dao = LauncherApp.instance.database.medicationDao()
    val medications by dao.getActive().collectAsState(initial = emptyList())
    val allLogs by dao.getAllLogs().collectAsState(initial = emptyList())
    
    var showAddDialog by remember { mutableStateOf(false) }
    var editingMed by remember { mutableStateOf<Medication?>(null) }
    var showHistory by remember { mutableStateOf(false) }
    
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(horizontal = 12.dp, vertical = 8.dp)) {
        ScreenHeader(
            title = if (showHistory) "Logboek" else "Medicijnen", 
            onBack = { if (showHistory) showHistory = false else onBack() }
        )
        
        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { showHistory = false },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = if (!showHistory) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Medication, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("OVERZICHT")
            }
            Button(
                onClick = { showHistory = true },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = if (showHistory) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Outlined.History, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("LOGBOEK")
            }
        }

        if (showHistory) {
            MedicationHistoryView(allLogs, medications)
        } else {
            MedicationOverview(
                medications = medications, 
                dao = dao, 
                onAddClick = { showAddDialog = true },
                onEditClick = { editingMed = it }
            )
        }
    }

    if (showAddDialog) {
        MedicationEditDialog(
            onDismiss = { showAddDialog = false },
            onSave = { med ->
                scope.launch {
                    val id = dao.insert(med)
                    MedicationAlarmScheduler.scheduleAlarms(context, med.copy(id = id))
                    showAddDialog = false
                }
            }
        )
    }

    if (editingMed != null) {
        MedicationEditDialog(
            medication = editingMed,
            onDismiss = { editingMed = null },
            onSave = { updatedMed ->
                scope.launch {
                    MedicationAlarmScheduler.cancelAlarms(context, updatedMed)
                    dao.update(updatedMed)
                    MedicationAlarmScheduler.scheduleAlarms(context, updatedMed)
                    editingMed = null
                }
            }
        )
    }
}

@Composable
fun MedicationOverview(
    medications: List<Medication>, 
    dao: com.seniorenlauncher.data.db.MedicationDao, 
    onAddClick: () -> Unit,
    onEditClick: (Medication) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (medications.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(top = 60.dp), contentAlignment = Alignment.Center) {
                    Text("Nog geen medicijnen toegevoegd", fontSize = 20.sp, color = Color.Gray)
                }
            } else {
                val pendingMeds = medications.filter { it.isPending }
                if (pendingMeds.isNotEmpty()) {
                    Text("⚠️ NU INNEMEN", fontWeight = FontWeight.Black, fontSize = 22.sp, color = Color(0xFFC62828), modifier = Modifier.padding(start = 8.dp))
                    pendingMeds.forEach { med ->
                        MedicationActionCard(med, onTaken = {
                            scope.launch {
                                val updated = med.copy(
                                    isPending = false, 
                                    lastTakenDate = System.currentTimeMillis(),
                                    lastActionTime = System.currentTimeMillis(),
                                    stockCount = (med.stockCount - 1).coerceAtLeast(0)
                                )
                                dao.update(updated)
                                dao.insertLog(MedicationLog(
                                    medicationId = med.id, 
                                    date = System.currentTimeMillis(), 
                                    time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()), 
                                    status = "TAKEN"
                                ))
                            }
                        })
                    }
                }

                Text("💊 MIJN LIJST", fontWeight = FontWeight.Black, fontSize = 22.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 8.dp, top = 8.dp))
                medications.forEach { med ->
                    MedicationInfoCard(
                        med = med, 
                        onDelete = {
                            scope.launch {
                                MedicationAlarmScheduler.cancelAlarms(context, med)
                                dao.delete(med)
                            }
                        },
                        onEdit = { onEditClick(med) }
                    )
                }
            }
            Spacer(Modifier.height(100.dp))
        }

        LargeFloatingActionButton(
            onClick = onAddClick,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ) {
            Icon(Icons.Default.Add, null, modifier = Modifier.size(36.dp))
        }
    }
}

@Composable
fun MedicationActionCard(med: Medication, onTaken: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
        border = androidx.compose.foundation.BorderStroke(3.dp, Color(0xFFC62828)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            if (med.photoUri != null) {
                AsyncImage(
                    model = med.photoUri,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(16.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(med.name, fontSize = 22.sp, fontWeight = FontWeight.Black)
                Text(med.dose, fontSize = 18.sp)
            }
            Button(
                onClick = onTaken,
                modifier = Modifier.height(70.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Check, null, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(8.dp))
                Text("KLAAR", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun MedicationInfoCard(med: Medication, onDelete: () -> Unit, onEdit: () -> Unit) {
    val isLowStock = med.stockCount <= med.lowStockThreshold
    
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onEdit() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (med.photoUri != null) {
                    AsyncImage(
                        model = med.photoUri,
                        contentDescription = null,
                        modifier = Modifier.size(50.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(12.dp))
                }
                Column(Modifier.weight(1f)) {
                    Text(med.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("${med.dose} • ${med.times}", fontSize = 16.sp, color = Color.Gray)
                }
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.LightGray)
                    }
                }
            }
            
            if (med.stockCount > 0 || isLowStock) {
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.background(if (isLowStock) Color(0xFFFFF3E0) else Color.Transparent, RoundedCornerShape(8.dp)).padding(4.dp)
                ) {
                    Icon(Icons.Outlined.Inventory, contentDescription = null, modifier = Modifier.size(16.dp), tint = if (isLowStock) Color(0xFFE65100) else Color.Gray)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = if (isLowStock) "BIJNA OP: nog ${med.stockCount} stuks" else "Voorraad: ${med.stockCount}",
                        fontSize = 14.sp,
                        fontWeight = if (isLowStock) FontWeight.Bold else FontWeight.Normal,
                        color = if (isLowStock) Color(0xFFE65100) else Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun MedicationHistoryView(logs: List<MedicationLog>, meds: List<Medication>) {
    if (logs.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Nog geen geschiedenis", color = Color.Gray)
        }
    } else {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(8.dp)) {
            items(logs) { log ->
                val med = meds.find { it.id == log.medicationId }
                val dateStr = SimpleDateFormat("EEEE d MMMM", Locale("nl", "NL")).format(Date(log.date))
                
                Card(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(if (log.status == "TAKEN") "✅" else "❌", fontSize = 24.sp)
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(med?.name ?: "Verwijderd medicijn", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("$dateStr om ${log.time}", fontSize = 14.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MedicationEditDialog(
    medication: Medication? = null,
    onDismiss: () -> Unit,
    onSave: (Medication) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(medication?.name ?: "") }
    var dose by remember { mutableStateOf(medication?.dose ?: "") }
    var stock by remember { mutableStateOf(medication?.stockCount?.toString() ?: "30") }
    var photoUri by remember { mutableStateOf(medication?.photoUri?.let { Uri.parse(it) }) }
    
    val selectedTimes = remember { 
        val list = mutableStateListOf<String>()
        medication?.times?.split(",")?.filter { it.isNotBlank() }?.let { list.addAll(it) }
        list
    }

    val dayNames = listOf("Zo", "Ma", "Di", "Wo", "Do", "Vr", "Za")
    val selectedDays = remember { 
        val list = mutableStateListOf<Int>()
        medication?.daysOfWeek?.split(",")?.filter { it.isNotBlank() }?.map { it.toInt() }?.let { list.addAll(it) } ?: list.addAll(listOf(1,2,3,4,5,6,7))
        list
    }
    
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        photoUri = uri
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            Modifier.fillMaxWidth().fillMaxHeight(0.95f),
            shape = RoundedCornerShape(28.dp)
        ) {
            Column(Modifier.padding(20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(if (medication == null) "Nieuw Medicijn" else "Aanpassen", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Naam") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = dose, onValueChange = { dose = it }, label = { Text("Dosering") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = stock, onValueChange = { stock = it }, label = { Text("Voorraad (aantal)") }, modifier = Modifier.fillMaxWidth())
                
                Text("Herhaal op:", fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    for (i in 1..7) {
                        val isSelected = selectedDays.contains(i)
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { if (isSelected) selectedDays.remove(i) else selectedDays.add(i) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(dayNames[i-1].take(1), color = if (isSelected) Color.White else Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Text("Foto:", fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (photoUri != null) {
                        AsyncImage(model = photoUri, contentDescription = null, modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                    }
                    Button(onClick = { galleryLauncher.launch("image/*") }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.PhotoLibrary, null)
                        Spacer(Modifier.width(8.dp))
                        Text("KIES FOTO")
                    }
                }

                Text("Tijden:", fontWeight = FontWeight.Bold)
                selectedTimes.forEach { time ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(time, fontSize = 18.sp, modifier = Modifier.weight(1f))
                        IconButton(onClick = { selectedTimes.remove(time) }) { Icon(Icons.Default.Close, null, tint = Color.Red) }
                    }
                }
                Button(onClick = {
                    val c = Calendar.getInstance()
                    TimePickerDialog(context, { _, h, m ->
                        val timeStr = String.format(Locale.getDefault(), "%02d:%02d", h, m)
                        if (!selectedTimes.contains(timeStr)) {
                            selectedTimes.add(timeStr)
                            selectedTimes.sort()
                        }
                    }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show()
                }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("TIJD TOEVOEGEN")
                }

                Spacer(Modifier.height(16.dp))
                
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("ANNULEREN") }
                    Button(
                        modifier = Modifier.weight(1f),
                        enabled = name.isNotBlank() && selectedTimes.isNotEmpty() && selectedDays.isNotEmpty(),
                        onClick = {
                            val baseMed = medication ?: Medication(name = "", dose = "", times = "")
                            onSave(baseMed.copy(
                                name = name,
                                dose = dose,
                                times = selectedTimes.joinToString(","),
                                daysOfWeek = selectedDays.joinToString(","),
                                stockCount = stock.toIntOrNull() ?: 0,
                                photoUri = photoUri?.toString()
                            ))
                        }
                    ) { Text("OPSLAAN") }
                }
            }
        }
    }
}
