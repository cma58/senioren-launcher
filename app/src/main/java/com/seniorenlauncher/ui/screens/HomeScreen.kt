package com.seniorenlauncher.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.seniorenlauncher.LauncherApp
import com.seniorenlauncher.data.model.LayoutType
import com.seniorenlauncher.ui.components.*
import com.seniorenlauncher.util.AppLauncher
import com.seniorenlauncher.service.NotificationListener
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class HomeApp(val id: String, val name: String, val emoji: String, val color: Color)

val ALL_APPS = listOf(
    HomeApp("phone","Bellen","📞",Color(0xFF38A169)),
    HomeApp("sms","Berichten","💬",Color(0xFF3B82F6)),
    HomeApp("whatsapp","WhatsApp","🟢",Color(0xFF25D366)),
    HomeApp("video","Videobellen","🎥",Color(0xFF8B5CF6)),
    HomeApp("camera","Camera","📷",Color(0xFFEC4899)),
    HomeApp("photos","Foto's","🖼️",Color(0xFFF59E0B)),
    HomeApp("alarm","Wekker","⏰",Color(0xFFEA580C)),
    HomeApp("calendar","Agenda","📅",Color(0xFF0D9488)),
    HomeApp("meds","Medicijnen","💊",Color(0xFFDC2626)),
    HomeApp("weather","Weer","🌤️",Color(0xFF0EA5E9)),
    HomeApp("flashlight","Zaklamp","🔦",Color(0xFFFBBF24)),
    HomeApp("magnifier","Vergrootglas","🔍",Color(0xFF6366F1)),
    HomeApp("notes","Notities","📝",Color(0xFF84CC16)),
    HomeApp("radio","Radio","📻",Color(0xFFA855F7)),
    HomeApp("steps","Stappen","🚶",Color(0xFF14B8A6)),
    HomeApp("emergency","Noodinfo","🏥",Color(0xFFEF4444)),
    HomeApp("sos","SOS","🆘",Color(0xFFDC2626)),
    HomeApp("all_apps","Alle Apps","📱",Color(0xFF718096)),
    HomeApp("settings","Instellingen","⚙️",Color(0xFF718096)),
)

@Composable
fun HomeScreen(onNavigate: (String) -> Unit, settingsVm: SettingsViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settings by settingsVm.settings.collectAsState()
    val notifications by NotificationListener.notifications.collectAsState()
    
    val dao = LauncherApp.instance.database.medicationDao()
    val pendingMeds by dao.getPending().collectAsState(initial = emptyList())
    
    val visible = ALL_APPS.filter { it.id in settings.visibleApps || it.id == "all_apps" }
    
    var showAppPickerFor by remember { mutableStateOf<HomeApp?>(null) }
    var showPinDialogForSettings by remember { mutableStateOf(false) }
    
    val cols = when (settings.layout) { 
        LayoutType.GRID_2x3 -> 2
        LayoutType.GRID_3x4 -> 3
        LayoutType.GRID_1x1 -> 1 
    }
    
    val fontSizeMultiplier = settings.fontSize / 16f
    
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(horizontal = 12.dp, vertical = 8.dp)) {
        ClockWidget(fontSizeMultiplier)
        
        // --- Medication Reminder Banner ---
        AnimatedVisibility(
            visible = pendingMeds.isNotEmpty(),
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column {
                Spacer(Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onNavigate("meds") },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFDC2626)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("💊", fontSize = 28.sp)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    if (pendingMeds.size == 1) "Medicijn Innemen!" else "Medicijnen Innemen!",
                                    fontWeight = FontWeight.ExtraBold, 
                                    color = Color(0xFF991B1B), 
                                    fontSize = 18.sp * fontSizeMultiplier
                                )
                                Text(
                                    pendingMeds.joinToString { it.name }, 
                                    color = Color(0xFF991B1B),
                                    fontSize = 16.sp * fontSizeMultiplier,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Button(
                                onClick = { 
                                    scope.launch {
                                        pendingMeds.forEach { med ->
                                            dao.insert(med.copy(isPending = false))
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.Check, null, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Klaar", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(cols), 
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp), 
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            items(visible) { app -> 
                val itemMinHeight = when(settings.layout) {
                    LayoutType.GRID_1x1 -> 120.dp
                    LayoutType.GRID_2x3 -> 100.dp
                    LayoutType.GRID_3x4 -> 85.dp
                }
                
                val badgeCount = NotificationListener.getBadgeCountByAppId(app.id, settings.appMappings, notifications)
                
                BigButton(
                    emoji = app.emoji, 
                    label = app.name, 
                    color = app.color, 
                    badge = if (badgeCount > 0) badgeCount else null,
                    small = settings.layout == LayoutType.GRID_3x4,
                    fontSizeMultiplier = fontSizeMultiplier,
                    onClick = { 
                        when (app.id) {
                            "phone" -> onNavigate("phone")
                            "sms" -> onNavigate("sms")
                            "settings" -> {
                                if (settings.settingsLocked) {
                                    showPinDialogForSettings = true
                                } else {
                                    onNavigate("settings")
                                }
                            }
                            "all_apps" -> onNavigate("all_apps")
                            "meds" -> onNavigate("meds")
                            "emergency" -> onNavigate("emergency")
                            "sos" -> onNavigate("sos")
                            "calendar" -> onNavigate("calendar")
                            "weather" -> onNavigate("weather")
                            "alarm" -> onNavigate("alarm")
                            "flashlight" -> onNavigate("flashlight")
                            "magnifier" -> onNavigate("magnifier")
                            "photos" -> onNavigate("photos")
                            "notes" -> onNavigate("notes")
                            "radio" -> onNavigate("radio")
                            "steps" -> onNavigate("steps")
                            else -> {
                                val customPackage = settings.appMappings[app.id]
                                val launched = AppLauncher.launchApp(context, app.id, customPackage)
                                if (!launched && !settings.settingsLocked) {
                                    showAppPickerFor = app
                                }
                            }
                        }
                    },
                    onLongClick = {
                        if (!settings.settingsLocked) {
                            val internalApps = listOf("phone", "sms", "settings", "sos", "emergency", "all_apps", "meds", "calendar", "weather", "alarm", "flashlight", "magnifier", "photos", "notes", "radio", "steps")
                            if (app.id !in internalApps) {
                                showAppPickerFor = app
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().heightIn(min = itemMinHeight)
                ) 
            }
        }
        
        Spacer(Modifier.height(8.dp))
        SOSButton(onClick = { onNavigate("sos") })
    }

    if (showAppPickerFor != null) {
        AppPickerDialog(
            app = showAppPickerFor!!,
            onDismiss = { showAppPickerFor = null },
            onAppSelected = { pkg ->
                settingsVm.setAppMapping(showAppPickerFor!!.id, pkg)
                AppLauncher.launchApp(context, showAppPickerFor?.id ?: "", pkg)
                showAppPickerFor = null
            }
        )
    }

    if (showPinDialogForSettings) {
        PinEntryDialog(
            correctPin = settings.pinCode ?: "1234",
            onSuccess = {
                showPinDialogForSettings = false
                onNavigate("settings")
            },
            onDismiss = { showPinDialogForSettings = false }
        )
    }
}

@Composable
fun PinEntryDialog(correctPin: String, onSuccess: () -> Unit, onDismiss: () -> Unit) {
    var enteredPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Lock, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(16.dp))
                Text("Beveiligde toegang", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("Voer de pincode in om de instellingen te wijzigen.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                
                Spacer(Modifier.height(24.dp))
                
                Text(
                    enteredPin.replace(Regex("."), "*").ifEmpty { " " },
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 8.sp,
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)).padding(horizontal = 24.dp, vertical = 8.dp)
                )
                
                if (error) {
                    Text("Onjuiste pincode!", color = Color.Red, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                }

                Spacer(Modifier.height(24.dp))

                // Number Pad
                val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "C", "0", "OK")
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.height(280.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(keys) { key ->
                        Button(
                            onClick = {
                                when (key) {
                                    "C" -> enteredPin = ""
                                    "OK" -> {
                                        if (enteredPin == correctPin) onSuccess()
                                        else {
                                            error = true
                                            enteredPin = ""
                                        }
                                    }
                                    else -> {
                                        if (enteredPin.length < 4) {
                                            enteredPin += key
                                            error = false
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxSize().aspectRatio(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = if (key == "OK") ButtonDefaults.buttonColors(containerColor = Color(0xFF38A169)) else ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                        ) {
                            Text(key, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                TextButton(onClick = onDismiss, modifier = Modifier.padding(top = 16.dp)) {
                    Text("Annuleren", fontSize = 18.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPickerDialog(app: HomeApp, onDismiss: () -> Unit, onAppSelected: (String) -> Unit) {
    val context = LocalContext.current
    val installedApps = remember { AppLauncher.getInstalledApps(context) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.8f).padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "Kies een app voor '${app.name}'",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(installedApps) { installed ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { onAppSelected(installed.packageName) }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(installed.name, fontSize = 18.sp)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
                
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End).padding(top = 8.dp)
                ) {
                    Text("Annuleren", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun ClockWidget(fontSizeMultiplier: Float = 1f) {
    var time by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { time = System.currentTimeMillis(); kotlinx.coroutines.delay(1000) } }
    val cal = Calendar.getInstance().apply { timeInMillis = time }
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(cal.time),
            fontSize = 54.sp * fontSizeMultiplier, 
            fontWeight = FontWeight.ExtraBold, 
            color = MaterialTheme.colorScheme.onBackground, 
            letterSpacing = 2.sp
        )
        Text(
            SimpleDateFormat("EEEE d MMMM", Locale("nl")).format(cal.time).replaceFirstChar { it.uppercase() },
            fontSize = 18.sp * fontSizeMultiplier, 
            fontWeight = FontWeight.Medium, 
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
