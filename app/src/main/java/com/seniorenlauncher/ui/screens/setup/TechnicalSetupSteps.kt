package com.seniorenlauncher.ui.screens.setup

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Telephony
import android.telecom.TelecomManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seniorenlauncher.LauncherApp
import com.seniorenlauncher.data.model.EmergencyInfo
import com.seniorenlauncher.data.model.QuickContact
import com.seniorenlauncher.ui.screens.SettingsViewModel
import kotlinx.coroutines.launch

@Composable
fun DefaultAppsSetupScreen(onNext: () -> Unit, isSenior: Boolean = false) {
    val context = LocalContext.current
    
    var isDefaultHome by remember { mutableStateOf(false) }
    var isDefaultDialer by remember { mutableStateOf(false) }
    var isDefaultSms by remember { mutableStateOf(false) }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                isDefaultHome = isLauncherDefault(context)
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val roleManager = context.getSystemService(Context.ROLE_SERVICE) as RoleManager
                    isDefaultDialer = roleManager.isRoleHeld(RoleManager.ROLE_DIALER)
                    isDefaultSms = roleManager.isRoleHeld(RoleManager.ROLE_SMS)
                } else {
                    val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
                    isDefaultDialer = telecomManager.defaultDialerPackage == context.packageName
                    isDefaultSms = Telephony.Sms.getDefaultSmsPackage(context) == context.packageName
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val roleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { }

    val allDone = isDefaultHome && isDefaultDialer && isDefaultSms

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isSenior) "De telefoon veilig maken" else "Standaard Apps",
            fontSize = if (isSenior) 36.sp else 32.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            lineHeight = if (isSenior) 42.sp else 38.sp
        )
        
        Spacer(Modifier.height(16.dp))
        
        Text(
            text = if (isSenior) 
                "We maken de Senioren Launcher uw vaste hulp voor bellen en berichten. Klik op de rode vakken."
                else "Stel de launcher in als standaard for Home, Telefoon en SMS om de stabiliteit te garanderen.",
            fontSize = if (isSenior) 22.sp else 18.sp, 
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = if (isSenior) 30.sp else 24.sp
        )
        
        Spacer(Modifier.height(32.dp))

        DefaultAppRow(
            title = if (isSenior) "1. Basis scherm" else "Startscherm (Home)",
            description = if (isSenior) "Zodat u altijd de grote knoppen ziet." else "Maak dit de standaard launcher.",
            isGranted = isDefaultHome,
            icon = Icons.Default.Home,
            isSenior = isSenior,
            onClick = {
                val intent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_HOME)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
        )

        Spacer(Modifier.height(16.dp))

        DefaultAppRow(
            title = if (isSenior) "2. Telefoon knop" else "Telefoon (Bellen)",
            description = if (isSenior) "Om makkelijk uw familie te kunnen bellen." else "Nodig voor de versimpelde bel-interface.",
            isGranted = isDefaultDialer,
            icon = Icons.Default.Phone,
            isSenior = isSenior,
            onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val roleManager = context.getSystemService(Context.ROLE_SERVICE) as RoleManager
                    val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
                    roleLauncher.launch(intent)
                } else {
                    val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).apply {
                        putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, context.packageName)
                    }
                    roleLauncher.launch(intent)
                }
            }
        )

        Spacer(Modifier.height(16.dp))

        DefaultAppRow(
            title = if (isSenior) "3. Berichten knop" else "Berichten (SMS)",
            description = if (isSenior) "Zodat u veilig berichten kunt ontvangen." else "Nodig voor SOS en batterijmeldingen.",
            isGranted = isDefaultSms,
            icon = Icons.Default.Sms,
            isSenior = isSenior,
            onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val roleManager = context.getSystemService(Context.ROLE_SERVICE) as RoleManager
                    val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
                    roleLauncher.launch(intent)
                } else {
                    val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT).apply {
                        putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, context.packageName)
                    }
                    roleLauncher.launch(intent)
                }
            }
        )

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(if (isSenior) 100.dp else 80.dp),
            shape = RoundedCornerShape(24.dp),
            enabled = allDone,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (allDone) Color(0xFF10B981) else Color.Gray
            )
        ) {
            Text(
                text = if (allDone) (if (isSenior) "HET IS GELUKT, GA VERDER" else "VOLGENDE") 
                       else (if (isSenior) "STEL DE 3 KNOPPEN IN" else "STEL ALLES IN"), 
                fontSize = if (isSenior) 24.sp else 22.sp, 
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun isLauncherDefault(context: Context): Boolean {
    val intent = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_HOME) }
    val resolveInfo = context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
    return resolveInfo?.activityInfo?.packageName == context.packageName
}

@Composable
fun SosSetupScreen(onNext: () -> Unit, settingsVm: SettingsViewModel) {
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
        com.seniorenlauncher.ui.screens.ContactPickerDialog(
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
fun FallDetectionSetupScreen(onNext: () -> Unit, settingsVm: SettingsViewModel) {
    val settings by settingsVm.settings.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Valdetectie",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
        
        Spacer(Modifier.height(8.dp))
        
        Surface(
            color = Color(0xFFFFF7ED),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFFF97316))
        ) {
            Text(
                "BETA FASE: Deze functie is nog in ontwikkeling en dient als extra hulpmiddel, niet als vervanging voor professionele hulp.",
                modifier = Modifier.padding(12.dp),
                fontSize = 14.sp,
                color = Color(0xFF9A3412),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
        
        Spacer(Modifier.height(24.dp))
        
        Text(
            text = "De telefoon kan met zijn sensoren een harde val herkennen. Als dat gebeurt, slaat de telefoon alarm en worden de SOS-contacten gewaarschuwd.",
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(Modifier.height(32.dp))
        
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
                    Text("Valdetectie inschakelen", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Gebruikt bewegingssensoren.", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = settings.fallDetectionEnabled,
                    onCheckedChange = { settingsVm.toggleFallDetection() }
                )
            }
        }
        
        Spacer(Modifier.weight(1f))
        
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(70.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text("VOLGENDE", fontSize = 20.sp, fontWeight = FontWeight.Bold)
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
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("4-cijferige PIN", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Dit vergrendelt de instellingen for de senior.", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                
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