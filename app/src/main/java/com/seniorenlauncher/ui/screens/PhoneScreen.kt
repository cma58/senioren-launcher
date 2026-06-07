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
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.seniorenlauncher.LauncherApp
import com.seniorenlauncher.data.model.QuickContact
import com.seniorenlauncher.ui.components.ScreenHeader
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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF0F172A)
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            ScreenHeader(
                title = when(activeTab) {
                    PhoneTab.Dialer -> "Telefoon"
                    PhoneTab.Recents -> "Recent"
                    PhoneTab.Contacts -> "Contacten"
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
                    modifier = Modifier.padding(8.dp),
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
                        label = "LIJST",
                        isSelected = activeTab == PhoneTab.Contacts,
                        modifier = Modifier.weight(1f),
                        onClick = { activeTab = PhoneTab.Contacts }
                    )
                }
            }

            if (!permissionsGranted) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Button(
                        onClick = { permissionLauncher.launch(requiredPermissions) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .padding(16.dp),
                        shape = RoundedCornerShape(32.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("TOESTEMMING GEVEN", fontSize = 22.sp, fontWeight = FontWeight.Black)
                    }
                }
            } else {
                Box(Modifier.weight(1f)) {
                    AnimatedContent(
                        targetState = activeTab,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        label = "TabContent"
                    ) { tab ->
                        when (tab) {
                            PhoneTab.Dialer -> DialerContent(
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
                            PhoneTab.Recents -> RecentsList(onCall = { number -> makeDirectCall(localContext, number) })
                            PhoneTab.Contacts -> AllContactsList(
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
}

enum class PhoneTab { Dialer, Recents, Contacts }

@Composable
fun PhoneTabButton(icon: ImageVector, label: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier.height(56.dp).scale(scale),
        color = containerColor,
        shape = RoundedCornerShape(40.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
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
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display with "Glass-Cut" look
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .height(90.dp),
            shape = RoundedCornerShape(40.dp),
            color = Color(0xFF1E293B).copy(alpha = 0.5f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = formatPhoneNumberDisplay(phoneNumber),
                    fontSize = if (phoneNumber.length > 10) 32.sp else 42.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
                if (phoneNumber.isNotEmpty()) {
                    IconButton(
                        onClick = { onNumberChange(phoneNumber.dropLast(1)) },
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Backspace, null, modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        Text(
            text = matchedContact?.name ?: if (phoneNumber.isEmpty()) "TOETS EEN NUMMER" else "ONBEKEND",
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Keypad with 3D Depth
        val keys = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("*", "0", "#")
        )

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
        }

        Spacer(Modifier.height(24.dp))

        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // SMS Orb
            val interactionSourceSms = remember { MutableInteractionSource() }
            val isPressedSms by interactionSourceSms.collectIsPressedAsState()
            val scaleSms by animateFloatAsState(
                targetValue = if (isPressedSms) 0.88f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "scale"
            )
            Surface(
                onClick = onSendMessage,
                interactionSource = interactionSourceSms,
                modifier = Modifier.size(80.dp).scale(scaleSms),
                shape = CircleShape,
                color = Color(0xFF6366F1), // Indigo
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                shadowElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.Message, null, modifier = Modifier.size(36.dp), tint = Color.White)
                }
            }

            // Call Orb (Large)
            val interactionSourceCall = remember { MutableInteractionSource() }
            val isPressedCall by interactionSourceCall.collectIsPressedAsState()
            val scaleCall by animateFloatAsState(
                targetValue = if (isPressedCall) 0.88f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "scale"
            )
            Surface(
                onClick = onCall,
                interactionSource = interactionSourceCall,
                modifier = Modifier.size(110.dp).scale(scaleCall),
                shape = CircleShape,
                color = Color(0xFF10B981), // Emerald
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                shadowElevation = 12.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Call, null, modifier = Modifier.size(52.dp), tint = Color.White)
                }
            }

            // 112 Orb
            val interactionSource112 = remember { MutableInteractionSource() }
            val isPressed112 by interactionSource112.collectIsPressedAsState()
            val scale112 by animateFloatAsState(
                targetValue = if (isPressed112) 0.88f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "scale"
            )
            Surface(
                onClick = { showEmergencyConfirm = true },
                interactionSource = interactionSource112,
                modifier = Modifier.size(80.dp).scale(scale112),
                shape = CircleShape,
                color = Color(0xFFEF4444), // Red
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                shadowElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("112", fontWeight = FontWeight.Black, fontSize = 24.sp, color = Color.White)
                }
            }
        }
        
        Spacer(Modifier.height(32.dp))
    }

    if (showEmergencyConfirm) {
        AlertDialog(
            onDismissRequest = { showEmergencyConfirm = false },
            containerColor = Color(0xFF1E293B),
            title = { Text("NOODNUMMER BELLEN?", color = Color.White, fontWeight = FontWeight.Black) },
            text = { Text("Weet u zeker dat u 112 wilt bellen?", fontSize = 20.sp, color = Color.White.copy(alpha = 0.8f)) },
            confirmButton = {
                Button(
                    onClick = {
                        showEmergencyConfirm = false
                        makeDirectCall(context, "112")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.height(64.dp).fillMaxWidth(0.6f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("JA, BEL NU", fontWeight = FontWeight.Black, fontSize = 20.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmergencyConfirm = false }) {
                    Text("ANNULEREN", fontSize = 18.sp, color = Color.White)
                }
            }
        )
    }
}

@Composable
fun DialerButton(text: String, onClick: () -> Unit) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    Surface(
        onClick = { 
            vibrate(context)
            onClick() 
        },
        interactionSource = interactionSource,
        modifier = Modifier
            .size(85.dp)
            .scale(scale),
        shape = RoundedCornerShape(40.dp),
        color = Color(0xFF1E293B).copy(alpha = 0.5f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        shadowElevation = 2.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text, 
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
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
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
            items(recents) { entry ->
                CallLogItem(entry, onClick = { onCall(entry.number) })
            }
        }
    }
}

@Composable
fun CallLogItem(entry: CallLogEntry, onClick: () -> Unit) {
    val icon = when (entry.type) {
        CallLog.Calls.MISSED_TYPE -> Icons.Default.CallMissed
        CallLog.Calls.INCOMING_TYPE -> Icons.Default.CallReceived
        else -> Icons.Default.CallMade
    }
    val iconColor = if (entry.type == CallLog.Calls.MISSED_TYPE) Color(0xFFEF4444) else Color(0xFF10B981)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(40.dp),
        color = Color(0xFF1E293B).copy(alpha = 0.5f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth().scale(scale)
    ) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(56.dp).background(iconColor.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(32.dp))
            }
            Spacer(Modifier.width(20.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = entry.name ?: entry.number,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${formatCallTime(entry.date)} • ${entry.number}",
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(Icons.Default.Call, null, tint = Color(0xFF10B981), modifier = Modifier.size(32.dp))
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
    val context = LocalContext.current

    val filteredContacts = initialContacts.filter { 
        it.name.contains(searchQuery, ignoreCase = true) || isNumberMatch(it.number, searchQuery)
    }

    Column(Modifier.fillMaxSize()) {
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            placeholder = { Text("Zoek op naam of nummer...", fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.5f)) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.primary) },
            shape = RoundedCornerShape(40.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.5f),
                unfocusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.3f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true
        )

        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_INSERT).apply {
                    type = ContactsContract.Contacts.CONTENT_TYPE
                }
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth().height(76.dp).padding(bottom = 16.dp),
            shape = RoundedCornerShape(40.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Icon(Icons.Default.PersonAdd, null, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(12.dp))
            Text("NIEUW CONTACT TOEVOEGEN", fontSize = 18.sp, fontWeight = FontWeight.Black)
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
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
}

@Composable
fun ContactListItem(contact: DeviceContact, isFavorite: Boolean, onToggleFavorite: () -> Unit, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(40.dp),
        color = if (contact.isMe) Color(0xFF6366F1).copy(alpha = 0.2f) else Color(0xFF1E293B).copy(alpha = 0.5f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth().scale(scale)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            if (contact.photoUri != null) {
                AsyncImage(
                    model = contact.photoUri,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp).clip(CircleShape).border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    Modifier.size(60.dp).background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(contact.name.take(1).uppercase(), color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                }
            }
            Spacer(Modifier.width(20.dp))
            Column(Modifier.weight(1f)) {
                Text(contact.name, fontSize = 22.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                Text(contact.number, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = null,
                    tint = if (isFavorite) Color(0xFFFFB100) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}

fun formatPhoneNumberDisplay(number: String): String {
    val clean = number.replace(Regex("[^0-9+]"), "")
    if (clean.length < 3) return clean
    return buildString {
        clean.forEachIndexed { index, c ->
            append(c)
            if (index == 1 || index == 3 || index == 5 || index == 7) append(" ")
        }
    }.trim()
}

fun normalizeNumber(number: String): String = number.replace(Regex("[^0-9]"), "")

fun isNumberMatch(num1: String, num2: String): Boolean {
    val n1 = normalizeNumber(num1)
    val n2 = normalizeNumber(num2)
    if (n1.isEmpty() || n2.isEmpty()) return false
    if (n1 == n2) return true
    if (n1.length >= 9 && n2.length >= 9) return n1.takeLast(9) == n2.takeLast(9)
    return n1.contains(n2) || n2.contains(n1)
}

private fun tryGetMyNumber(context: Context): String? {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val sm = context.getSystemService(SubscriptionManager::class.java)
            sm.activeSubscriptionInfoList?.firstOrNull()?.let { sm.getPhoneNumber(it.subscriptionId) }
        } else {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            tm.line1Number
        }
    } catch (e: Exception) { null }
}

fun formatCallTime(timestamp: Long): String {
    val now = Calendar.getInstance()
    val time = Calendar.getInstance().apply { timeInMillis = timestamp }
    val format = if (now.get(Calendar.DATE) == time.get(Calendar.DATE)) "HH:mm" else "d MMM, HH:mm"
    return SimpleDateFormat(format, Locale.getDefault()).format(Date(timestamp))
}

fun fetchCallLog(context: Context): List<CallLogEntry> {
    val list = mutableListOf<CallLogEntry>()
    try {
        val cursor = context.contentResolver.query(CallLog.Calls.CONTENT_URI, null, null, null, CallLog.Calls.DATE + " DESC")
        cursor?.use {
            val numIdx = it.getColumnIndex(CallLog.Calls.NUMBER)
            val nameIdx = it.getColumnIndex(CallLog.Calls.CACHED_NAME)
            val typeIdx = it.getColumnIndex(CallLog.Calls.TYPE)
            val dateIdx = it.getColumnIndex(CallLog.Calls.DATE)
            while (it.moveToNext() && list.size < 50) {
                list.add(CallLogEntry(it.getString(numIdx) ?: "", it.getString(nameIdx), it.getInt(typeIdx), it.getLong(dateIdx), 0))
            }
        }
    } catch (e: Exception) {}
    return list
}

fun fetchAllDeviceContacts(context: Context): List<DeviceContact> {
    val contacts = mutableListOf<DeviceContact>()
    val cursor = context.contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC")
    cursor?.use {
        val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
        val numIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
        val photoIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI)
        while (it.moveToNext()) {
            val name = it.getString(nameIdx) ?: "Onbekend"
            val number = it.getString(numIdx) ?: ""
            val photo = if (photoIdx != -1) it.getString(photoIdx) else null
            if (number.isNotEmpty()) contacts.add(DeviceContact(name, number, photo))
        }
    }
    return contacts.distinctBy { normalizeNumber(it.number) }
}

fun makeDirectCall(context: Context, number: String) {
    if (number.isEmpty()) return
    val uri = Uri.parse("tel:${Uri.encode(number)}")
    try {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                telecomManager.placeCall(uri, null)
            } else {
                context.startActivity(Intent(Intent.ACTION_CALL, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
        } else {
            context.startActivity(Intent(Intent.ACTION_DIAL, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    } catch (e: Exception) {
        context.startActivity(Intent(Intent.ACTION_DIAL, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}

fun vibrate(context: Context) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
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
        color = Color(0xFFEF4444).copy(alpha = 0.2f),
        shape = RoundedCornerShape(40.dp),
        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
    ) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Warning, null, tint = Color(0xFFEF4444))
            Spacer(Modifier.width(16.dp))
            Text("Stel in als standaard beller.", Modifier.weight(1f), fontSize = 15.sp, fontWeight = FontWeight.Black, color = Color.White)
            Button(onClick = onRequest, shape = RoundedCornerShape(20.dp)) { Text("FIX", fontWeight = FontWeight.Black) }
        }
    }
}
