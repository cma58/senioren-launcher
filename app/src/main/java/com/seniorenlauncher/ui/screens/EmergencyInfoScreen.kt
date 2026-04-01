package com.seniorenlauncher.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seniorenlauncher.LauncherApp
import com.seniorenlauncher.data.model.EmergencyInfo
import com.seniorenlauncher.ui.components.ScreenHeader
import kotlinx.coroutines.launch

@Composable
fun EmergencyInfoScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val db = LauncherApp.instance.database
    val emergencyDao = db.emergencyDao()
    val medDao = db.medicationDao()
    
    val info by emergencyDao.get().collectAsState(initial = EmergencyInfo())
    val medications by medDao.getActive().collectAsState(initial = emptyList())
    val currentInfo = info ?: EmergencyInfo()
    
    var showEditDialog by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(horizontal = 12.dp, vertical = 8.dp)) {
        ScreenHeader(title = "Noodinfo", onBack = onBack)
        
        Box(modifier = Modifier.weight(1f)) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Medische informatie
                EmergencySection(
                    title = "🏥 Medische informatie",
                    titleColor = Color(0xFFEF4444),
                    backgroundColor = Color(0xFFEF4444).copy(alpha = 0.1f)
                ) {
                    EmergencyRow("Bloedgroep", currentInfo.bloodType)
                    EmergencyRow("Allergieën", currentInfo.allergies)
                    EmergencyRow("Aandoeningen", currentInfo.conditions)
                    EmergencyRow("Pacemaker", if (currentInfo.hasPacemaker) "Ja" else "Nee")
                }

                // Medicijnen (Automatisch uit de medicijnen lijst)
                EmergencySection(
                    title = "💊 Huidige medicijnen",
                    titleColor = Color(0xFF3B82F6),
                    backgroundColor = Color(0xFF3B82F6).copy(alpha = 0.1f)
                ) {
                    if (medications.isEmpty()) {
                        Text("Geen medicijnen geregistreerd", fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        medications.forEach { med ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(med.name, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text("${med.dose} — ${med.times}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        }
                    }
                }

                // ICE Contacten
                EmergencySection(
                    title = "📱 ICE Contacten",
                    titleColor = Color(0xFF10B981),
                    backgroundColor = Color(0xFF10B981).copy(alpha = 0.1f)
                ) {
                    EmergencyRow("Naam", currentInfo.iceContactName.ifBlank { "Niet ingesteld" })
                    EmergencyRow("Telefoonnummer", currentInfo.iceContactPhone.ifBlank { "Niet ingesteld" })
                }

                // Persoonlijke info
                EmergencySection(
                    title = "🏠 Persoonlijke info",
                    titleColor = Color(0xFFF59E0B),
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    EmergencyRow("Naam", currentInfo.fullName)
                    EmergencyRow("Geboortedatum", currentInfo.birthDate)
                    EmergencyRow("Adres", currentInfo.address)
                    EmergencyRow("Huisarts", "${currentInfo.doctorName} — ${currentInfo.doctorPhone}")
                }
                
                Spacer(Modifier.height(80.dp))
            }

            LargeFloatingActionButton(
                onClick = { showEditDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 16.dp, end = 8.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Bewerken", modifier = Modifier.size(36.dp))
            }
        }
    }

    if (showEditDialog) {
        var name by remember { mutableStateOf(currentInfo.fullName) }
        var birth by remember { mutableStateOf(currentInfo.birthDate) }
        var addr by remember { mutableStateOf(currentInfo.address) }
        var blood by remember { mutableStateOf(currentInfo.bloodType) }
        var allergies by remember { mutableStateOf(currentInfo.allergies) }
        var conditions by remember { mutableStateOf(currentInfo.conditions) }
        var docName by remember { mutableStateOf(currentInfo.doctorName) }
        var docPhone by remember { mutableStateOf(currentInfo.doctorPhone) }
        var pacemaker by remember { mutableStateOf(currentInfo.hasPacemaker) }
        var iceName by remember { mutableStateOf(currentInfo.iceContactName) }
        var icePhone by remember { mutableStateOf(currentInfo.iceContactPhone) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Noodinfo Bewerken", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Persoonlijk", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Volledige Naam") })
                    OutlinedTextField(value = birth, onValueChange = { birth = it }, label = { Text("Geboortedatum") })
                    OutlinedTextField(value = addr, onValueChange = { addr = it }, label = { Text("Adres") })
                    
                    Spacer(Modifier.height(8.dp))
                    Text("ICE Contact (In Case of Emergency)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    OutlinedTextField(value = iceName, onValueChange = { iceName = it }, label = { Text("Naam Contactpersoon") })
                    OutlinedTextField(value = icePhone, onValueChange = { icePhone = it }, label = { Text("Telefoonnummer Contactpersoon") })

                    Spacer(Modifier.height(8.dp))
                    Text("Medisch", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    OutlinedTextField(value = blood, onValueChange = { blood = it }, label = { Text("Bloedgroep") })
                    OutlinedTextField(value = allergies, onValueChange = { allergies = it }, label = { Text("Allergieën") })
                    OutlinedTextField(value = conditions, onValueChange = { conditions = it }, label = { Text("Aandoeningen") })
                    OutlinedTextField(value = docName, onValueChange = { docName = it }, label = { Text("Huisarts Naam") })
                    OutlinedTextField(value = docPhone, onValueChange = { docPhone = it }, label = { Text("Huisarts Telefoon") })
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = pacemaker, onCheckedChange = { pacemaker = it })
                        Text("Heeft Pacemaker")
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    scope.launch {
                        emergencyDao.save(EmergencyInfo(
                            fullName = name, birthDate = birth, address = addr,
                            bloodType = blood, allergies = allergies, conditions = conditions,
                            doctorName = docName, doctorPhone = docPhone, hasPacemaker = pacemaker,
                            iceContactName = iceName, iceContactPhone = icePhone
                        ))
                        showEditDialog = false
                    }
                }) { Text("Opslaan") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("Annuleren") }
            }
        )
    }
}

@Composable
fun EmergencySection(title: String, titleColor: Color, backgroundColor: Color, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = titleColor)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun EmergencyRow(label: String, value: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        HorizontalDivider(Modifier.padding(top = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }
}
