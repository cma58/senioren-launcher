package com.seniorenlauncher.ui.screens

import android.app.TimePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.vector.ImageVector
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
    
    Column(Modifier.fillMaxSize().background(Color(0xFF0F172A)).padding(horizontal = 20.dp)) {
        ScreenHeader(
            title = if (showHistory) "Logboek" else "Medicijnen", 
            onBack = { if (showHistory) showHistory = false else onBack() }
        )
        
        // Android 17 Glass Tab Switcher
        Surface(
            color = Color(0xFF1E293B).copy(alpha = 0.5f),
            shape = RoundedCornerShape(40.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(40.dp))
        ) {
            Row(
                modifier = Modifier.padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MedicationTabButton(
                    icon = Icons.Default.Medication,
                    label = "OVERZICHT",
                    isSelected = !showHistory,
                    modifier = Modifier.weight(1f),
                    onClick = { showHistory = false }
                )
                MedicationTabButton(
                    icon = Icons.Outlined.History,
                    label = "LOGBOEK",
                    isSelected = showHistory,
                    modifier = Modifier.weight(1f),
                    onClick = { showHistory = true }
                )
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
fun MedicationTabButton(icon: ImageVector, label: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        color = containerColor,
        shape = RoundedCornerShape(40.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Icon(icon, null, modifier = Modifier.size(24.dp), tint = contentColor)
            if (isSelected) {
                Spacer(Modifier.width(8.dp))
                Text(
                    label, 
                    fontSize = 14.sp, 
                    fontWeight = FontWeight.Black,
                    color = contentColor,
                    maxLines = 1
                )
            }
        }
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (medications.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(top = 60.dp), contentAlignment = Alignment.Center) {
                    Text("Nog geen medicijnen toegevoegd", fontSize = 20.sp, color = Color.Gray, fontWeight = FontWeight.Black)
                }
            } else {
                val pendingMeds = medications.filter { it.isPending }
                if (pendingMeds.isNotEmpty()) {
                    Text("⚠️ NU INNEMEN", fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color(0xFFEF4444), modifier = Modifier.padding(start = 4.dp))
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

                Text("💊 MIJN LIJST", fontWeight = FontWeight.Black, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 4.dp, top = 8.dp))
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

        // Luminous Add Button
        Surface(
            onClick = onAddClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .size(80.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = 12.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(40.dp), tint = Color.White)
            }
        }
    }
}

@Composable
fun MedicationActionCard(med: Medication, onTaken: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFEF4444).copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
        shape = RoundedCornerShape(40.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            if (med.photoUri != null) {
                AsyncImage(
                    model = med.photoUri,
                    contentDescription = null,
                    modifier = Modifier.size(70.dp).clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(16.dp))
            } else {
                Box(
                    Modifier.size(70.dp).background(Color(0xFFEF4444).copy(alpha = 0.2f), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Medication, null, tint = Color(0xFFEF4444), modifier = Modifier.size(32.dp))
                }
                Spacer(Modifier.width(16.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(med.name, fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.White)
                Text(med.dose, fontSize = 18.sp, color = Color.White.copy(alpha = 0.7f))
            }
            
            // Taken "Orb"
            Surface(
                onClick = onTaken,
                modifier = Modifier.size(70.dp),
                shape = CircleShape,
                color = Color(0xFF10B981),
                shadowElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Check, null, modifier = Modifier.size(36.dp), tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun MedicationInfoCard(med: Medication, onDelete: () -> Unit, onEdit: () -> Unit) {
    val isLowStock = med.stockCount <= med.lowStockThreshold
    
    Surface(
        onClick = onEdit,
        shape = RoundedCornerShape(40.dp),
        color = Color(0xFF1E293B).copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (med.photoUri != null) {
                    AsyncImage(
                        model = med.photoUri,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp).clip(CircleShape).border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(16.dp))
                } else {
                    Box(
                        Modifier.size(60.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Medication, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                    }
                    Spacer(Modifier.width(16.dp))
                }
                Column(Modifier.weight(1f)) {
                    Text(med.name, fontSize = 22.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                    Text("${med.dose} • ${med.times}", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White.copy(alpha = 0.3f))
                }
            }
            
            if (med.stockCount > 0 || isLowStock) {
                Spacer(Modifier.height(12.dp))
                Surface(
                    color = if (isLowStock) Color(0xFFEF4444).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Inventory, 
                            contentDescription = null, 
                            modifier = Modifier.size(18.dp), 
                            tint = if (isLowStock) Color(0xFFF87171) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isLowStock) "BIJNA OP: nog ${med.stockCount} stuks" else "Voorraad: ${med.stockCount}",
                            fontSize = 15.sp,
                            fontWeight = if (isLowStock) FontWeight.Black else FontWeight.Bold,
                            color = if (isLowStock) Color(0xFFF87171) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MedicationHistoryView(logs: List<MedicationLog>, meds: List<Medication>) {
    if (logs.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Nog geen geschiedenis", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    } else {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(logs) { log ->
                val med = meds.find { it.id == log.medicationId }
                val dateStr = SimpleDateFormat("EEEE d MMMM", Locale("nl", "NL")).format(Date(log.date))
                
                Surface(
                    Modifier.fillMaxWidth(),
                    color = Color(0xFF1E293B).copy(alpha = 0.5f),
                    shape = RoundedCornerShape(40.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(50.dp).background(if (log.status == "TAKEN") Color(0xFF10B981).copy(alpha = 0.1f) else Color(0xFFEF4444).copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(if (log.status == "TAKEN") "✅" else "❌", fontSize = 24.sp)
                        }
                        Spacer(Modifier.width(20.dp))
                        Column {
                            Text(med?.name ?: "Verwijderd medicijn", fontWeight = FontWeight.Black, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("$dateStr om ${log.time}", fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        Surface(
            Modifier.fillMaxWidth().fillMaxHeight(0.9f),
            shape = RoundedCornerShape(40.dp),
            color = Color(0xFF1E293B), // Slate 800
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Column(Modifier.padding(24.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(if (medication == null) "Nieuw Medicijn" else "Aanpassen", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
                
                OutlinedTextField(
                    value = name, 
                    onValueChange = { name = it }, 
                    label = { Text("Naam") }, 
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                OutlinedTextField(
                    value = dose, 
                    onValueChange = { dose = it }, 
                    label = { Text("Dosering") }, 
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                OutlinedTextField(
                    value = stock, 
                    onValueChange = { stock = it }, 
                    label = { Text("Voorraad (aantal)") }, 
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                
                Text("Herhaal op:", fontWeight = FontWeight.Black, color = Color.White)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    for (i in 1..7) {
                        val isSelected = selectedDays.contains(i)
                        Surface(
                            onClick = { if (isSelected) selectedDays.remove(i) else selectedDays.add(i) },
                            modifier = Modifier.size(42.dp),
                            shape = CircleShape,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.1f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(dayNames[i-1].take(1), color = Color.White, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }

                Text("Foto:", fontWeight = FontWeight.Black, color = Color.White)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (photoUri != null) {
                        AsyncImage(model = photoUri, contentDescription = null, modifier = Modifier.size(80.dp).clip(RoundedCornerShape(20.dp)), contentScale = ContentScale.Crop)
                    } else {
                        Box(Modifier.size(80.dp).background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.PhotoCamera, null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(32.dp))
                        }
                    }
                    Button(
                        onClick = { galleryLauncher.launch("image/*") }, 
                        modifier = Modifier.weight(1f).height(70.dp),
                        shape = RoundedCornerShape(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, null)
                        Spacer(Modifier.width(8.dp))
                        Text("KIES FOTO", fontWeight = FontWeight.Black)
                    }
                }

                Text("Tijden:", fontWeight = FontWeight.Black, color = Color.White)
                selectedTimes.forEach { time ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(time, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
                            IconButton(onClick = { selectedTimes.remove(time) }) { Icon(Icons.Default.Close, null, tint = Color(0xFFEF4444)) }
                        }
                    }
                }
                Button(
                    onClick = {
                        val c = Calendar.getInstance()
                        TimePickerDialog(context, { _, h, m ->
                            val timeStr = String.format(Locale.getDefault(), "%02d:%02d", h, m)
                            if (!selectedTimes.contains(timeStr)) {
                                selectedTimes.add(timeStr)
                                selectedTimes.sort()
                            }
                        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show()
                    }, 
                    modifier = Modifier.fillMaxWidth().height(70.dp),
                    shape = RoundedCornerShape(40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f))
                ) {
                    Icon(Icons.Default.Add, null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("TIJD TOEVOEGEN", fontWeight = FontWeight.Black, color = Color.White)
                }

                Spacer(Modifier.height(16.dp))
                
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f).height(70.dp)) { 
                        Text("ANNULEREN", color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Black) 
                    }
                    Button(
                        modifier = Modifier.weight(1f).height(70.dp),
                        enabled = name.isNotBlank() && selectedTimes.isNotEmpty() && selectedDays.isNotEmpty(),
                        shape = RoundedCornerShape(40.dp),
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
                    ) { Text("OPSLAAN", fontWeight = FontWeight.Black) }
                }
            }
        }
    }
}
