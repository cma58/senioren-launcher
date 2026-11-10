package com.seniorenlauncher.ui.screens
import com.seniorenlauncher.R
import com.seniorenlauncher.util.i18n

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
import com.seniorenlauncher.data.model.Medication
import com.seniorenlauncher.ui.components.ScreenHeader
import kotlinx.coroutines.launch

@Composable
fun EmergencyInfoScreen(onBack: () -> Unit) {
    // --- DEMO MODE TOGGLE ---
    val isDemoMode = false

    val scope = rememberCoroutineScope()
    val db = LauncherApp.instance.database
    val emergencyDao = db.emergencyDao()
    val medDao = db.medicationDao()
    
    val realInfo by emergencyDao.get().collectAsState(initial = EmergencyInfo())
    val realMedications by medDao.getActive().collectAsState(initial = emptyList())
    
    // --- DUMMY DATA ---
    val dummyInfo = EmergencyInfo(
        fullName = "Hendrik de Jong",
        birthDate = "12-05-1945",
        address = "Hoofdstraat 42, Amsterdam",
        bloodType = "A+",
        allergies = "Geen",
        conditions = "Hoge bloeddruk",
        doctorName = "Huisarts de Vries",
        doctorPhone = "020 555 0123",
        iceContactName = "Dochter Sofie",
        iceContactPhone = "06 12345678",
        hasPacemaker = false
    )
    val dummyMeds = listOf(
        Medication(name = "Bloeddruk", dose = "1 pil", times = "08:00")
    )

    val currentInfo = if (isDemoMode) dummyInfo else realInfo ?: EmergencyInfo()
    val medications = if (isDemoMode) dummyMeds else realMedications
    
    var showEditDialog by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(horizontal = 12.dp, vertical = 8.dp)) {
        ScreenHeader(title = i18n(R.string.noodinfo), onBack = onBack)
        
        Box(modifier = Modifier.weight(1f)) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Medische informatie
                EmergencySection(
                    title = i18n(R.string.medische_informatie),
                    titleColor = Color(0xFFEF4444),
                    backgroundColor = Color(0xFFEF4444).copy(alpha = 0.1f)
                ) {
                    EmergencyRow(i18n(R.string.bloedgroep), currentInfo.bloodType)
                    EmergencyRow(i18n(R.string.allergien), currentInfo.allergies)
                    EmergencyRow(i18n(R.string.aandoeningen), currentInfo.conditions)
                    EmergencyRow(i18n(R.string.pacemaker), if (currentInfo.hasPacemaker) i18n(R.string.ja) else i18n(R.string.nee))
                }

                // Medicijnen
                EmergencySection(
                    title = i18n(R.string.huidige_medicijnen),
                    titleColor = Color(0xFF3B82F6),
                    backgroundColor = Color(0xFF3B82F6).copy(alpha = 0.1f)
                ) {
                    if (medications.isEmpty()) {
                        Text(i18n(R.string.geen_medicijnen_geregistreerd), fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        medications.forEach { med ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(med.name, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text("${med.dose} — ${med.times}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (med != medications.last()) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            }
                        }
                    }
                }

                // ICE Contacten
                EmergencySection(
                    title = i18n(R.string.ice_contacten),
                    titleColor = Color(0xFF10B981),
                    backgroundColor = Color(0xFF10B981).copy(alpha = 0.1f)
                ) {
                    EmergencyRow(i18n(R.string.naam), currentInfo.iceContactName.ifBlank { i18n(R.string.niet_ingesteld) })
                    EmergencyRow(i18n(R.string.telefoonnummer), currentInfo.iceContactPhone.ifBlank { i18n(R.string.niet_ingesteld) })
                }

                // Persoonlijke info
                EmergencySection(
                    title = i18n(R.string.persoonlijke_info),
                    titleColor = Color(0xFFF59E0B),
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    EmergencyRow(i18n(R.string.naam), currentInfo.fullName)
                    EmergencyRow(i18n(R.string.geboortedatum), currentInfo.birthDate)
                    EmergencyRow(i18n(R.string.adres), currentInfo.address)
                    EmergencyRow(i18n(R.string.huisarts), "${currentInfo.doctorName} — ${currentInfo.doctorPhone}")
                }
                
                Spacer(Modifier.height(80.dp))
            }

            if (!isDemoMode) {
                LargeFloatingActionButton(
                    onClick = { showEditDialog = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 16.dp, end = 8.dp),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = i18n(R.string.bewerken), modifier = Modifier.size(36.dp))
                }
            }
        }
    }

    if (showEditDialog && !isDemoMode) {
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
            title = { Text(i18n(R.string.noodinfo_bewerken), fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(i18n(R.string.persoonlijk), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(i18n(R.string.volledige_naam)) })
                    OutlinedTextField(value = birth, onValueChange = { birth = it }, label = { Text(i18n(R.string.geboortedatum)) })
                    OutlinedTextField(value = addr, onValueChange = { addr = it }, label = { Text(i18n(R.string.adres)) })
                    
                    Spacer(Modifier.height(8.dp))
                    Text(i18n(R.string.ice_contact_in_case_of), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    OutlinedTextField(value = iceName, onValueChange = { iceName = it }, label = { Text(i18n(R.string.naam_contactpersoon)) })
                    OutlinedTextField(value = icePhone, onValueChange = { icePhone = it }, label = { Text(i18n(R.string.telefoonnummer_contactpersoon)) })

                    Spacer(Modifier.height(8.dp))
                    Text(i18n(R.string.medisch), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    OutlinedTextField(value = blood, onValueChange = { blood = it }, label = { Text(i18n(R.string.bloedgroep)) })
                    OutlinedTextField(value = allergies, onValueChange = { allergies = it }, label = { Text(i18n(R.string.allergien)) })
                    OutlinedTextField(value = conditions, onValueChange = { conditions = it }, label = { Text(i18n(R.string.aandoeningen)) })
                    OutlinedTextField(value = docName, onValueChange = { docName = it }, label = { Text(i18n(R.string.huisarts_naam)) })
                    OutlinedTextField(value = docPhone, onValueChange = { docPhone = it }, label = { Text(i18n(R.string.huisarts_telefoon)) })
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = pacemaker, onCheckedChange = { pacemaker = it })
                        Text(i18n(R.string.heeft_pacemaker))
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
                }) { Text(i18n(R.string.opslaan_2)) }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text(i18n(R.string.annuleren_2)) }
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
