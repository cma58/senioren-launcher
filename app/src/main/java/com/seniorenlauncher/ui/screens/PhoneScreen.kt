package com.seniorenlauncher.ui.screens

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.CallLog
import android.provider.ContactsContract
import android.telecom.TelecomManager
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.seniorenlauncher.LauncherApp
import com.seniorenlauncher.data.model.QuickContact
import com.seniorenlauncher.ui.components.ScreenHeader
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class DeviceContact(val name: String, val number: String, val photoUri: String? = null, val isMe: Boolean = false)

data class CallLogEntry(
    val number: String,
    val name: String?,
    val type: Int,
    val date: Long,
    val duration: Long
)

@Composable
fun PhoneScreen(onNavigate: (String) -> Unit, onBack: () -> Unit, settingsVm: SettingsViewModel = viewModel()) {
    val localContext = LocalContext.current
    val scope = rememberCoroutineScope()
    val settings by settingsVm.settings.collectAsState()
    
    val telecomManager = localContext.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
    var isDefaultDialer by remember { 
        mutableStateOf(telecomManager.defaultDialerPackage == localContext.packageName) 
    }

    val roleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        isDefaultDialer = telecomManager.defaultDialerPackage == localContext.packageName
    }

    // --- Taak 1: Permissies Beheren ---
    val requiredPermissions = arrayOf(
        Manifest.permission.CALL_PHONE,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.READ_CALL_LOG
    )

    var permissionsGranted by remember {
        mutableStateOf(
            requiredPermissions.all { 
                ContextCompat.checkSelfPermission(localContext, it) == PackageManager.PERMISSION_GRANTED 
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        permissionsGranted = results.values.all { it }
    }

    val dao = remember { LauncherApp.instance.database.contactDao() }
    val favorieten by dao.getAll().collectAsState(initial = emptyList())
    
    var allContacts by remember { mutableStateOf(emptyList<DeviceContact>()) }
    
    // Mijn nummer detectie
    LaunchedEffect(settings.userPhoneNumber, permissionsGranted) {
        if (permissionsGranted) {
            val deviceContacts = fetchAllDeviceContacts(localContext)
            val myNumber = settings.userPhoneNumber ?: tryGetMyNumber(localContext)
            
            allContacts = if (myNumber != null) {
                val me = DeviceContact("Ik (Mijn nummer)", myNumber, isMe = true)
                listOf(me) + deviceContacts.filter { !isNumberMatch(it.number, myNumber) }
            } else {
                deviceContacts
            }
        }
    }

    var phoneNumber by remember { mutableStateOf("") }
    var activeTab by remember { mutableStateOf(PhoneTab.Dialer) }
    
    val matchedContact = favorieten.find { isNumberMatch(it.phoneNumber, phoneNumber) }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        ScreenHeader(
            title = when(activeTab) {
                PhoneTab.Dialer -> "Telefoon"
                PhoneTab.Recents -> "Recent"
                PhoneTab.Contacts -> "Kies Contact"
            }, 
            onBack = { 
                if (activeTab != PhoneTab.Dialer) activeTab = PhoneTab.Dialer 
                else onBack() 
            }
        )
        
        if (!isDefaultDialer) {
            DefaultDialerBanner {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val roleManager = localContext.getSystemService(RoleManager::class.java)
                    val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
                    roleLauncher.launch(intent)
                }
            }
        }

        // Tab Switcher
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PhoneTabButton(
                icon = Icons.Default.Dialpad,
                label = "TOETSEN",
                isSelected = activeTab == PhoneTab.Dialer,
                modifier = Modifier.weight(1f),
                onClick = { activeTab = PhoneTab.Dialer }
            )
            PhoneTabButton(
                icon = Icons.Default.History,
                label = "RECENT",
                isSelected = activeTab == PhoneTab.Recents,
                modifier = Modifier.weight(1f),
                onClick = { activeTab = PhoneTab.Recents }
            )
            PhoneTabButton(
                icon = Icons.Default.Person,
                label = "CONTACTEN",
                isSelected = activeTab == PhoneTab.Contacts,
                modifier = Modifier.weight(1f),
                onClick = { activeTab = PhoneTab.Contacts }
            )
        }

        if (!permissionsGranted) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Button(
                    onClick = { permissionLauncher.launch(requiredPermissions) },
                    modifier = Modifier.fillMaxWidth().height(100.dp).padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("GEEF TOESTEMMING VOOR TELEFOON", fontSize = 20.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                }
            }
        } else {
            Box(Modifier.weight(1f)) {
                when (activeTab) {
                    PhoneTab.Dialer -> {
                        DialerContent(
                            phoneNumber = phoneNumber,
                            matchedContact = matchedContact,
                            favorieten = favorieten,
                            allContacts = allContacts,
                            onNumberChange = { phoneNumber = it },
                            onCall = { makeDirectCall(localContext, phoneNumber) },
                            onSendMessage = { 
                                val trimmed = phoneNumber.trim()
                                if (trimmed.isNotEmpty()) {
                                    onNavigate("sms?address=${Uri.encode(trimmed)}")
                                } else {
                                    onNavigate("sms")
                                }
                            }
                        )
                    }
                    PhoneTab.Recents -> {
                        RecentsList(onCall = { number -> makeDirectCall(localContext, number) })
                    }
                    PhoneTab.Contacts -> {
                        AllContactsList(
                            favorieten = favorieten,
                            initialContacts = allContacts,
                            myNumber = settings.userPhoneNumber,
                            onToggleFavorite = { contact ->
                                scope.launch {
                                    val existing = favorieten.find { isNumberMatch(it.phoneNumber, contact.number) }
                                    if (existing != null) {
                                        dao.delete(existing)
                                    } else {
                                        dao.insert(QuickContact(name = contact.name, phoneNumber = contact.number, photoUri = contact.photoUri))
                                    }
                                }
                            },
                            onContactSelected = { selectedNumber ->
                                phoneNumber = selectedNumber
                                activeTab = PhoneTab.Dialer
                            },
                            onSetMyNumber = { settingsVm.setUserPhoneNumber(it) }
                        )
                    }
                }
            }
        }
    }
}

// --- Taak 4: Nummer Opmaak ---
fun formatPhoneNumberDisplay(number: String): String {
    val clean = number.replace(Regex("[^0-9+]"), "")
    if (clean.length < 3) return clean
    
    return buildString {
        clean.forEachIndexed { index, c ->
            append(c)
            if (index == 1 || index == 3 || index == 5 || index == 7) {
                append(" ")
            }
        }
    }.trim()
}

fun normalizeNumber(number: String): String {
    return number.replace(Regex("[^0-9]"), "")
}

fun isNumberMatch(num1: String, num2: String): Boolean {
    val n1 = normalizeNumber(num1)
    val n2 = normalizeNumber(num2)
    if (n1.isEmpty() || n2.isEmpty()) return false
    
    if (n1 == n2) return true
    
    if (n1.length >= 9 && n2.length >= 9) {
        return n1.takeLast(9) == n2.takeLast(9)
    }
    
    return n1.contains(n2) || n2.contains(n1)
}

private fun tryGetMyNumber(context: Context): String? {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(context, "android.permission.READ_PHONE_NUMBERS") != PackageManager.PERMISSION_GRANTED) {
        return null
    }

    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val sm = context.getSystemService(SubscriptionManager::class.java)
            sm.activeSubscriptionInfoList?.firstOrNull()?.let { info ->
                sm.getPhoneNumber(info.subscriptionId)
            }
        } else {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            tm.line1Number
        }
    } catch (e: Exception) {
        null
    }
}

enum class PhoneTab { Dialer, Recents, Contacts }

@Composable
fun PhoneTabButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(60.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(4.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, modifier = Modifier.size(24.dp))
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun RecentsList(onCall: (String) -> Unit) {
    val context = LocalContext.current
    var recents by remember { mutableStateOf(emptyList<CallLogEntry>()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        recents = fetchCallLog(context)
        isLoading = false
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (recents.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Geen recente oproepen", fontSize = 20.sp, color = Color.Gray)
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
            items(recents) { entry ->
                CallLogItem(entry, onClick = { onCall(entry.number) })
            }
        }
    }
}

@Composable
fun CallLogItem(entry: CallLogEntry, onClick: () -> Unit) {
    val icon = when (entry.type) {
        CallLog.Calls.INCOMING_TYPE -> Icons.Default.CallReceived
        CallLog.Calls.OUTGOING_TYPE -> Icons.Default.CallMade
        CallLog.Calls.MISSED_TYPE -> Icons.Default.CallMissed
        else -> Icons.Default.Call
    }
    val iconColor = if (entry.type == CallLog.Calls.MISSED_TYPE) Color.Red else Color(0xFF38A169)

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = entry.name ?: entry.number,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (entry.name != null) {
                    Text(entry.number, fontSize = 16.sp, color = Color.Gray)
                }
                Text(
                    text = formatCallTime(entry.date),
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            IconButton(onClick = onClick) {
                Icon(Icons.Default.Call, null, tint = Color(0xFF38A169), modifier = Modifier.size(32.dp))
            }
        }
    }
}

fun formatCallTime(timestamp: Long): String {
    val now = Calendar.getInstance()
    val time = Calendar.getInstance().apply { timeInMillis = timestamp }
    val sdf = if (now.get(Calendar.DATE) == time.get(Calendar.DATE)) {
        SimpleDateFormat("HH:mm", Locale.getDefault())
    } else {
        SimpleDateFormat("d MMM, HH:mm", Locale.getDefault())
    }
    return sdf.format(Date(timestamp))
}

fun fetchCallLog(context: Context): List<CallLogEntry> {
    val list = mutableListOf<CallLogEntry>()
    try {
        val cursor = context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            null, null, null, CallLog.Calls.DATE + " DESC"
        )
        if (cursor != null) {
            val numberIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER)
            val nameIndex = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)
            val typeIndex = cursor.getColumnIndex(CallLog.Calls.TYPE)
            val dateIndex = cursor.getColumnIndex(CallLog.Calls.DATE)
            val durationIndex = cursor.getColumnIndex(CallLog.Calls.DURATION)

            while (cursor.moveToNext()) {
                list.add(CallLogEntry(
                    number = cursor.getString(numberIndex) ?: "",
                    name = cursor.getString(nameIndex),
                    type = cursor.getInt(typeIndex),
                    date = cursor.getLong(dateIndex),
                    duration = cursor.getLong(durationIndex)
                ))
                if (list.size >= 50) break
            }
            cursor.close()
        }
    } catch (e: SecurityException) {
        // Handle no permission
    }
    return list
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DialerContent(
    phoneNumber: String,
    matchedContact: QuickContact?,
    favorieten: List<QuickContact>,
    allContacts: List<DeviceContact>,
    onNumberChange: (String) -> Unit,
    onCall: () -> Unit,
    onSendMessage: () -> Unit
) {
    val context = LocalContext.current
    var showEmergencyConfirm by remember { mutableStateOf(false) }

    val suggestions = remember(phoneNumber, allContacts) {
        if (phoneNumber.isEmpty()) emptyList()
        else {
            allContacts.filter { isNumberMatch(it.number, phoneNumber) || it.name.contains(phoneNumber, ignoreCase = true) }.take(3)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Phone Number Display - Task 4: Readability
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = formatPhoneNumberDisplay(phoneNumber),
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
                if (phoneNumber.isNotEmpty()) {
                    IconButton(
                        onClick = { onNumberChange(phoneNumber.dropLast(1)) },
                        modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Backspace, "Verwijderen", modifier = Modifier.size(36.dp))
                    }
                }
            }
        }

        if (matchedContact != null || phoneNumber.isNotEmpty()) {
            Text(
                text = matchedContact?.name ?: "Onbekend nummer",
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Suggestions row
        if (suggestions.isNotEmpty() && matchedContact == null) {
            LazyRow(
                modifier = Modifier.fillMaxWidth().height(60.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                items(suggestions) { contact ->
                    SuggestionChip(contact) { onNumberChange(contact.number) }
                }
            }
        } else {
            Spacer(Modifier.height(60.dp))
        }

        // Keypad
        val keys = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("*", "0", "#")
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            keys.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    row.forEach { key ->
                        DialerButton(
                            text = key,
                            onClick = { if (phoneNumber.length < 15) onNumberChange(phoneNumber + key) }
                        )
                    }
                }
            }
            
            // --- Taak 4: Sneltoetsen ---
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { showEmergencyConfirm = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.weight(1f).height(65.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Emergency, null)
                    Spacer(Modifier.width(8.dp))
                    Text("NOOD 112", fontWeight = FontWeight.Black, fontSize = 18.sp)
                }
                Button(
                    onClick = { makeDirectCall(context, "1233") }, // Standaard KPN/T-Mobile voicemail
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B6CB0)),
                    modifier = Modifier.weight(1f).height(65.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Voicemail, null)
                    Spacer(Modifier.width(8.dp))
                    Text("VOICEMAIL", fontWeight = FontWeight.Black, fontSize = 18.sp)
                }
            }
        }

        // Action Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // SMS Button
            FloatingActionButton(
                onClick = onSendMessage,
                containerColor = Color(0xFF3182CE),
                contentColor = Color.White,
                modifier = Modifier.size(70.dp),
                shape = CircleShape
            ) {
                Icon(Icons.AutoMirrored.Filled.Message, "SMS", modifier = Modifier.size(36.dp))
            }

            // Call Button
            FloatingActionButton(
                onClick = onCall,
                containerColor = Color(0xFF38A169),
                contentColor = Color.White,
                modifier = Modifier.size(90.dp),
                shape = CircleShape
            ) {
                Icon(Icons.Default.Call, "Bellen", modifier = Modifier.size(48.dp))
            }
        }
    }

    if (showEmergencyConfirm) {
        AlertDialog(
            onDismissRequest = { showEmergencyConfirm = false },
            title = { Text("Noodnummer bellen?") },
            text = { Text("Weet u zeker dat u 112 wilt bellen?", fontSize = 20.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        showEmergencyConfirm = false
                        makeDirectCall(context, "112")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.height(60.dp).padding(horizontal = 8.dp)
                ) {
                    Text("JA, BEL 112", fontWeight = FontWeight.Black, fontSize = 18.sp)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEmergencyConfirm = false },
                    modifier = Modifier.height(60.dp)
                ) {
                    Text("ANNULEREN", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun SuggestionChip(contact: DeviceContact, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(contact.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(4.dp))
            Text(contact.number, fontSize = 14.sp, color = Color.Gray)
        }
    }
}

@Composable
fun DialerButton(text: String, onClick: () -> Unit) {
    val context = LocalContext.current
    Surface(
        onClick = { 
            vibrate(context)
            onClick() 
        },
        modifier = Modifier.size(85.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 4.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = text, fontSize = 36.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AllContactsList(
    favorieten: List<QuickContact>,
    initialContacts: List<DeviceContact>,
    myNumber: String?,
    onToggleFavorite: (DeviceContact) -> Unit,
    onContactSelected: (String) -> Unit,
    onSetMyNumber: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var showMyNumberDialog by remember { mutableStateOf(false) }
    var tempMyNumber by remember { mutableStateOf("") }
    val context = LocalContext.current

    val filteredContacts = initialContacts.filter { 
        it.name.contains(searchQuery, ignoreCase = true) || isNumberMatch(it.number, searchQuery)
    }

    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            placeholder = { Text("Zoek contact...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        // --- Taak 3: Nieuw Contact Knop ---
        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_INSERT).apply {
                    type = ContactsContract.Contacts.CONTENT_TYPE
                }
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth().height(70.dp).padding(bottom = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Icon(Icons.Default.PersonAdd, null, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(12.dp))
            Text("➕ NIEUW CONTACT", fontSize = 20.sp, fontWeight = FontWeight.Black)
        }

        if (myNumber == null && searchQuery.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clickable { showMyNumberDialog = true },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(16.dp))
                    Text("Stel mijn eigen nummer in", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
            items(filteredContacts) { contact ->
                val isFav = favorieten.any { isNumberMatch(it.phoneNumber, contact.number) }
                ContactListItem(
                    contact = contact,
                    isFavorite = isFav,
                    onToggleFavorite = { onToggleFavorite(contact) },
                    onClick = { onContactSelected(contact.number) }
                )
            }
        }
    }

    if (showMyNumberDialog) {
        AlertDialog(
            onDismissRequest = { showMyNumberDialog = false },
            title = { Text("Mijn nummer instellen") },
            text = {
                Column {
                    Text("Voer uw eigen telefoonnummer in.")
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tempMyNumber,
                        onValueChange = { tempMyNumber = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("06 12345678") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (tempMyNumber.isNotBlank()) {
                        onSetMyNumber(tempMyNumber)
                        showMyNumberDialog = false
                    }
                }) { Text("OPSLAAN") }
            },
            dismissButton = {
                TextButton(onClick = { showMyNumberDialog = false }) { Text("ANNULEREN") }
            }
        )
    }
}

@Composable
fun ContactListItem(contact: DeviceContact, isFavorite: Boolean, onToggleFavorite: () -> Unit, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (contact.isMe) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (contact.photoUri != null) {
                AsyncImage(
                    model = contact.photoUri,
                    contentDescription = null,
                    modifier = Modifier.size(50.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    Modifier.size(50.dp).clip(CircleShape).background(if (contact.isMe) Color(0xFF38A169) else MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(contact.name.take(1), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(contact.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(contact.number, fontSize = 16.sp, color = Color.Gray)
            }
            if (!contact.isMe) {
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Favoriet",
                        tint = if (isFavorite) Color(0xFFFFB100) else Color.Gray,
                        modifier = Modifier.size(32.dp)
                    )
                }
            } else {
                Icon(Icons.Default.Badge, null, tint = Color(0xFF38A169), modifier = Modifier.padding(end = 8.dp))
            }
        }
    }
}

fun fetchAllDeviceContacts(context: Context): List<DeviceContact> {
    val contacts = mutableListOf<DeviceContact>()
    val cursor = context.contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
    )
    cursor?.use {
        val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
        val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
        val photoIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI)
        
        while (it.moveToNext()) {
            val name = it.getString(nameIndex) ?: "Onbekend"
            val number = it.getString(numberIndex) ?: ""
            val photo = if (photoIndex != -1) it.getString(photoIndex) else null
            if (number.isNotEmpty()) {
                contacts.add(DeviceContact(name, number, photo))
            }
        }
    }
    return contacts.distinctBy { normalizeNumber(it.number) }
}

// --- Taak 2: Bellen via TelecomManager ---
fun makeDirectCall(context: Context, number: String) {
    if (number.isEmpty()) return
    
    val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
    val uri = Uri.parse("tel:${Uri.encode(number)}")
    
    try {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            // TelecomManager is preferred for default dialer apps
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                telecomManager.placeCall(uri, null)
            } else {
                val intent = Intent(Intent.ACTION_CALL, uri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
        } else {
            // Fallback naar DIAL scherm als we geen permissie hebben
            val dialIntent = Intent(Intent.ACTION_DIAL, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(dialIntent)
        }
    } catch (e: SecurityException) {
        val dialIntent = Intent(Intent.ACTION_DIAL, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(dialIntent)
    } catch (e: Exception) {
        // Algemene fallback
    }
}

fun makeDirectSms(context: Context, number: String) {
    if (number.isEmpty()) return
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("smsto:${Uri.encode(number)}")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

fun vibrate(context: Context) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        vibrator.vibrate(50)
    }
}

@Composable
fun DefaultDialerBanner(onRequest: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.onErrorContainer)
            Spacer(Modifier.width(12.dp))
            Text(
                "Stel in als standaard beller om direct te kunnen bellen.",
                Modifier.weight(1f),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            TextButton(onClick = onRequest) {
                Text("INSTELLEN")
            }
        }
    }
}
