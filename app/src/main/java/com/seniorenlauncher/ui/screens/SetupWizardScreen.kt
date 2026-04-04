package com.seniorenlauncher.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.seniorenlauncher.LauncherApp
import com.seniorenlauncher.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class SetupFlow { NONE, CAREGIVER, SENIOR }

@Composable
fun SetupWizardScreen(
    onFinished: () -> Unit,
    settingsVm: SettingsViewModel
) {
    var flow by remember { mutableStateOf(SetupFlow.NONE) }

    when (flow) {
        SetupFlow.NONE -> {
            FlowSelectionScreen(onFlowSelected = { flow = it })
        }
        SetupFlow.CAREGIVER -> {
            var caregiverStep by remember { mutableIntStateOf(1) }
            when (caregiverStep) {
                1 -> PermissionsSetupScreen(onNext = { caregiverStep = 2 }, isSenior = false)
                2 -> SosSetupScreen(onNext = { caregiverStep = 3 }, settingsVm = settingsVm)
                3 -> SecuritySetupScreen(onNext = { caregiverStep = 4 }, settingsVm = settingsVm)
                4 -> HandoverScreen(onNext = { flow = SetupFlow.SENIOR })
            }
        }
        SetupFlow.SENIOR -> {
            var seniorStep by remember { mutableIntStateOf(1) }
            when (seniorStep) {
                1 -> SeniorWelcomeStep(onNext = { seniorStep = 2 })
                2 -> PermissionsSetupScreen(onNext = { seniorStep = 3 }, isSenior = true)
                3 -> SeniorReadingStep(onNext = { seniorStep = 4 }, settingsVm = settingsVm)
                4 -> SeniorColorsStep(onNext = { seniorStep = 5 }, settingsVm = settingsVm)
                5 -> SeniorEmergencyStep(onNext = { 
                    settingsVm.completeSetup()
                    onFinished()
                })
            }
        }
    }
}

@Composable
fun FlowSelectionScreen(onFlowSelected: (SetupFlow) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welkom.\nWie stelt deze telefoon in?",
            fontSize = 32.sp,
            lineHeight = 42.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 64.dp)
        )

        SetupOptionCard(
            title = "Ik ben de Mantelzorger",
            description = "Ik stel dit toestel in voor iemand anders.",
            icon = Icons.Default.VolunteerActivism,
            color = Color(0xFF3B82F6),
            onClick = { onFlowSelected(SetupFlow.CAREGIVER) }
        )

        Spacer(Modifier.height(32.dp))

        SetupOptionCard(
            title = "Ik ben de Gebruiker",
            description = "Ik ga deze telefoon zelf gebruiken.",
            icon = Icons.Default.Person,
            color = Color(0xFF10B981),
            onClick = { onFlowSelected(SetupFlow.SENIOR) }
        )
    }
}

@Composable
fun SetupOptionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        border = androidx.compose.foundation.BorderStroke(2.dp, color)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, modifier = Modifier.size(48.dp), tint = color)
            Spacer(Modifier.width(20.dp))
            Column {
                Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
                Text(description, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun SosSetupScreen(onNext: () -> Unit, settingsVm: SettingsViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val emergencyDao = remember { LauncherApp.instance.database.emergencyDao() }
    val contactDao = remember { LauncherApp.instance.database.contactDao() }
    
    val selectedContacts = remember { mutableStateListOf<Pair<String, String>>() }
    var showContactPicker by remember { mutableStateOf(false) }
    var manualName by remember { mutableStateOf("") }
    var manualPhone by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Hoofdcontacten instellen",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
        
        Spacer(Modifier.height(16.dp))
        
        Text(
            text = "Kies tot maximaal 4 contactpersonen. Deze nummers worden direct als favoriet en SOS-contact ingesteld.",
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(Modifier.height(24.dp))

        // Handmatige invoer
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(Modifier.weight(1f)) {
                OutlinedTextField(
                    value = manualName,
                    onValueChange = { manualName = it },
                    label = { Text("Naam") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = manualPhone,
                    onValueChange = { manualPhone = it },
                    label = { Text("Nummer") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Button(
                onClick = {
                    if (manualName.isNotBlank() && manualPhone.isNotBlank() && selectedContacts.size < 4) {
                        selectedContacts.add(manualName to manualPhone)
                        manualName = ""
                        manualPhone = ""
                    }
                },
                modifier = Modifier.height(110.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = manualName.isNotBlank() && manualPhone.isNotBlank() && selectedContacts.size < 4
            ) {
                Icon(Icons.Default.Add, null)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Lijst van geselecteerde contacten
        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(20.dp)
        ) {
            LazyColumn(Modifier.padding(16.dp)) {
                if (selectedContacts.isEmpty()) {
                    item {
                        Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Nog geen contacten gekozen", color = Color.Gray)
                        }
                    }
                } else {
                    items(selectedContacts) { contact ->
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(contact.first, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text(contact.second, fontSize = 14.sp, color = Color.Gray)
                            }
                            IconButton(onClick = { selectedContacts.remove(contact) }) {
                                Icon(Icons.Default.Delete, null, tint = Color.Red)
                            }
                        }
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        
        if (selectedContacts.size < 4) {
            Button(
                onClick = { showContactPicker = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
            ) {
                Icon(Icons.Default.ContactPhone, null, modifier = Modifier.size(32.dp))
                Spacer(Modifier.width(16.dp))
                Text("KIES UIT CONTACTEN", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        Button(
            onClick = {
                scope.launch {
                    selectedContacts.forEachIndexed { index, contact ->
                        if (index == 0) {
                            emergencyDao.save(EmergencyInfo(iceContactName = contact.first, iceContactPhone = contact.second))
                        }
                        contactDao.insert(QuickContact(
                            name = contact.first, 
                            phoneNumber = contact.second, 
                            isSosContact = true, 
                            emoji = "🆘",
                            color = 0xFFDC2626
                        ))
                    }
                    onNext()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text("VOLGENDE", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }

    if (showContactPicker) {
        ContactPickerDialog(
            onDismiss = { showContactPicker = false },
            onContactSelected = { name, phone ->
                if (!selectedContacts.any { it.second == phone } && selectedContacts.size < 4) {
                    selectedContacts.add(name to phone)
                }
                showContactPicker = false
            }
        )
    }
}

@Composable
fun ContactPickerDialog(onDismiss: () -> Unit, onContactSelected: (String, String) -> Unit) {
    val context = LocalContext.current
    var contacts by remember { mutableStateOf(emptyList<Pair<String, String>>()) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val list = mutableListOf<Pair<String, String>>()
            val cursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER),
                null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )
            cursor?.use {
                val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                while (it.moveToNext()) {
                    val name = it.getString(nameIdx)
                    val num = it.getString(numIdx)
                    if (name != null && num != null) list.add(name to num)
                }
            }
            contacts = list.distinctBy { it.second.replace(" ", "") }
            isLoading = false
        }
    }

    val filteredContacts = remember(searchQuery, contacts) {
        if (searchQuery.isBlank()) contacts
        else contacts.filter { it.first.contains(searchQuery, ignoreCase = true) || it.second.contains(searchQuery) }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            Modifier.fillMaxWidth().fillMaxHeight(0.9f),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Kies een contact", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                
                Spacer(Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Zoek op naam of nummer...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(Modifier.height(16.dp))

                if (isLoading) {
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(Modifier.weight(1f)) {
                        items(filteredContacts) { contact ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { onContactSelected(contact.first, contact.second) }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    Modifier.size(44.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(contact.first.take(1).uppercase(), fontWeight = FontWeight.Bold)
                                }
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text(contact.first, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    Text(contact.second, fontSize = 14.sp, color = Color.Gray)
                                }
                            }
                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                        }
                    }
                }
                
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("ANNULEREN")
                }
            }
        }
    }
}

@Composable
fun SecuritySetupScreen(onNext: () -> Unit, settingsVm: SettingsViewModel) {
    var pin by remember { mutableStateOf("") }
    var remoteSupportEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Beveiliging instellen",
            fontSize = 32.sp,
            lineHeight = 38.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
        
        Spacer(Modifier.height(32.dp))
        
        // PIN Sectie
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("4-cijferige PIN", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Dit vergrendelt de instellingen voor de senior.", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                
                Spacer(Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) pin = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Bijv. 1234") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
        
        Spacer(Modifier.height(24.dp))
        
        // Hulp op afstand Sectie
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Hulp op afstand", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Maakt meekijken via RustDesk mogelijk.", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = remoteSupportEnabled,
                    onCheckedChange = { remoteSupportEnabled = it }
                )
            }
        }
        
        Spacer(Modifier.weight(1f))
        
        Button(
            onClick = {
                if (pin.length == 4) {
                    settingsVm.setPinCode(pin)
                    settingsVm.lockSettings()
                }
                onNext()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text("BEVEILIGING OPSLAAN", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun HandoverScreen(onNext: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("✨", fontSize = 80.sp)
        
        Text(
            text = "De techniek is klaar!",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
        
        Spacer(Modifier.height(24.dp))
        
        Text(
            text = "Geef de telefoon nu aan de gebruiker, zodat zij zelf kunnen kiezen hoe groot de letters en knoppen moeten zijn.",
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            lineHeight = 28.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(Modifier.height(64.dp))
        
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
        ) {
            Text("START VISUELE INSTELLINGEN", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// --- SENIOR FLOW STAPPEN ---

@Composable
fun SeniorWelcomeStep(onNext: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("👋", fontSize = 100.sp)
        Text("Welkom", fontSize = 40.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))
        Text(
            "We gaan uw telefoon samen heel makkelijk maken. U kunt hierbij niets fout doen.",
            fontSize = 26.sp,
            textAlign = TextAlign.Center,
            lineHeight = 36.sp
        )
        Spacer(Modifier.height(64.dp))
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(80.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text("BEGINNEN", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SeniorReadingStep(onNext: () -> Unit, settingsVm: SettingsViewModel) {
    val settings by settingsVm.settings.collectAsState()
    
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Welkom. Hoe groot wilt u de letters?", fontSize = 26.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(48.dp))
        
        ReadingOptionCard("Dit is normale tekst.", 18, settings.fontSize == 18) { settingsVm.updateFontSize(18) }
        Spacer(Modifier.height(16.dp))
        ReadingOptionCard("Dit is grote tekst.", 24, settings.fontSize == 24) { settingsVm.updateFontSize(24) }
        Spacer(Modifier.height(20.dp))
        ReadingOptionCard("DIT IS REUSACHTIG.", 30, settings.fontSize == 30) { settingsVm.updateFontSize(30) }
        
        Spacer(Modifier.weight(1f))
        
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(80.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text("VOLGENDE", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ReadingOptionCard(label: String, size: Int, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        border = if (selected) androidx.compose.foundation.BorderStroke(4.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Text(
            text = label,
            fontSize = size.sp,
            modifier = Modifier.padding(24.dp),
            textAlign = TextAlign.Center,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun SeniorColorsStep(onNext: () -> Unit, settingsVm: SettingsViewModel) {
    val settings by settingsVm.settings.collectAsState()
    
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Welkom. Welke kleuren vindt u het fijnst?", fontSize = 26.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(48.dp))
        
        SeniorThemeCard("Klassiek (Zacht)", AppTheme.CLASSIC, settings.theme == AppTheme.CLASSIC) { settingsVm.updateTheme(AppTheme.CLASSIC) }
        Spacer(Modifier.height(32.dp))
        SeniorThemeCard("Hoog Contrast (Fel)", AppTheme.HIGH_CONTRAST, settings.theme == AppTheme.HIGH_CONTRAST) { settingsVm.updateTheme(AppTheme.HIGH_CONTRAST) }
        
        Spacer(Modifier.weight(1f))
        
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(80.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text("VOLGENDE", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SeniorThemeCard(label: String, theme: AppTheme, selected: Boolean, onClick: () -> Unit) {
    val bgColor = if (theme == AppTheme.HIGH_CONTRAST) Color.Black else Color(0xFFF0F2F5)
    val textColor = if (theme == AppTheme.HIGH_CONTRAST) Color.Yellow else Color.Black
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        border = if (selected) androidx.compose.foundation.BorderStroke(4.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Box(Modifier.fillMaxSize().background(bgColor), contentAlignment = Alignment.Center) {
            Text(label, color = textColor, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SeniorEmergencyStep(onNext: () -> Unit) {
    val scope = rememberCoroutineScope()
    val contactDao = remember { LauncherApp.instance.database.contactDao() }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🆘", fontSize = 60.sp)
        Text("Wie wilt u bellen in geval van nood?", fontSize = 26.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(32.dp))
        
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Naam") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(fontSize = 20.sp)
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Telefoonnummer") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            textStyle = LocalTextStyle.current.copy(fontSize = 20.sp)
        )
        
        Spacer(Modifier.height(32.dp))
        
        Button(
            onClick = {
                if (name.isNotBlank() && phone.isNotBlank()) {
                    scope.launch {
                        contactDao.insert(QuickContact(name = name, phoneNumber = phone, isSosContact = true, emoji = "🆘", color = 0xFFDC2626))
                        onNext()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(80.dp),
            enabled = name.isNotBlank() && phone.isNotBlank(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text("BEWAAR CONTACT", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        
        Spacer(Modifier.height(24.dp))
        Text("De app zal hierna om toestemming vragen om dit nummer te mogen bellen.", fontSize = 16.sp, textAlign = TextAlign.Center, color = Color.Gray)
    }
}
