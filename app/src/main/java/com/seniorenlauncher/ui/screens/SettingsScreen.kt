package com.seniorenlauncher.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import android.net.Uri
import com.seniorenlauncher.data.model.*
import com.seniorenlauncher.ui.components.ScreenHeader
import com.seniorenlauncher.util.UpdateManager
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(vm: SettingsViewModel, onNavigate: (String) -> Unit, onBack: () -> Unit) {
    val settings by vm.settings.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    
    var showPinScreen by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }
    
    // Update status
    var isCheckingUpdate by remember { mutableStateOf(false) }
    var updateResult by remember { mutableStateOf<String?>(null) }

    if (settings.settingsLocked && !showPinScreen) {
        LockedSettingsScreen(
            onBack = onBack,
            onUnlockClick = { 
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                showPinScreen = true 
            }
        )
        return
    }

    if (showPinScreen) {
        PinEntryScreen(
            pin = enteredPin,
            onPinChange = { key ->
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                if (key == "⌫") {
                    if (enteredPin.isNotEmpty()) enteredPin = enteredPin.dropLast(1)
                } else if (enteredPin.length < 4) {
                    enteredPin += key
                    if (enteredPin.length == 4) {
                        if (vm.verifyPin(enteredPin)) {
                            vm.unlockSettings()
                            showPinScreen = false
                            enteredPin = ""
                        } else {
                            enteredPin = ""
                        }
                    }
                }
            },
            onCancel = { 
                showPinScreen = false
                enteredPin = ""
            }
        )
        return
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(horizontal = 16.dp, vertical = 8.dp)) {
        ScreenHeader(title = "Beheer & Instellingen", onBack = onBack)
        
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()), 
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // --- GROEP 1: SCHERM & STIJL ---
            SettingsGroup(title = "📺 Scherm & Stijl", icon = Icons.Default.Palette) {
                Text("Kleurthema", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppTheme.entries.forEach { t ->
                        val selected = settings.theme == t
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { 
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    vm.updateTheme(t) 
                                }
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(Modifier.size(24.dp).background(if (t == AppTheme.LIGHT) Color.Black else Color.White, CircleShape))
                            Text(
                                text = when(t){ 
                                    AppTheme.CLASSIC -> "Klassiek"
                                    AppTheme.HIGH_CONTRAST -> "Contrast"
                                    AppTheme.LIGHT -> "Licht" 
                                },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                
                Spacer(Modifier.height(16.dp))

                Text("Knoppen indeling", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(LayoutType.GRID_2x3 to "Groot", LayoutType.GRID_3x4 to "Compact", LayoutType.GRID_1x1 to "Enorm").forEach { (type, label) ->
                        val selected = settings.layout == type
                        Button(
                            onClick = { 
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                vm.updateLayout(type) 
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = if (selected) ButtonDefaults.buttonColors() else ButtonDefaults.filledTonalButtonColors()
                        ) {
                            Text(label, fontSize = 14.sp)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text("Tekstgrootte", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("A", fontSize = 16.sp)
                    Slider(
                        value = settings.fontSize.toFloat(), 
                        onValueChange = { vm.updateFontSize(it.toInt()) }, 
                        valueRange = 16f..36f, 
                        modifier = Modifier.weight(1f)
                    )
                    Text("A", fontSize = 32.sp, fontWeight = FontWeight.Bold)
                }
                
                SettToggle("Automatische Nachtmodus", settings.nightModeAuto) { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    vm.toggleNightMode() 
                }
            }

            // --- GROEP 2: APPS & TAAL ---
            SettingsGroup(title = "🌍 Apps & Taal", icon = Icons.Default.Language) {
                Text("Taal", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                val languages = listOf("nl" to "Nederlands", "en" to "English", "fr" to "Français", "de" to "Deutsch")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    languages.forEach { (code, name) ->
                        val selected = settings.language == code
                        Button(
                            onClick = { 
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                vm.updateLanguage(code) 
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = if (selected) ButtonDefaults.buttonColors() else ButtonDefaults.filledTonalButtonColors(),
                            contentPadding = PaddingValues(4.dp)
                        ) {
                            Text(name, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text("Zichtbare Apps", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                ALL_APPS.filter { it.id != "settings" && it.id != "all_apps" }.forEach { app ->
                    SettToggle("${app.emoji} ${app.name}", app.id in settings.visibleApps) { 
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        vm.toggleAppVisibility(app.id) 
                    }
                }
            }

            // --- GROEP 3: VEILIGHEID & NOODHULP ---
            SettingsGroup(title = "🛡️ Veiligheid & Noodhulp", icon = Icons.Default.Security) {
                SettRow("🏥 Medische Noodinfo bewerken") { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onNavigate("emergency") 
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
                SettRow("🆘 SOS Contacten instellen") { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onNavigate("sos_settings") 
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
                SettToggle("🛡️ Anti-Scam Filter", settings.scamProtectionEnabled) { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    vm.toggleScamProtection() 
                }
                SettToggle("⚠️ Valdetectie", settings.fallDetectionEnabled) { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    vm.toggleFallDetection() 
                }
                SettToggle("🪫 Batterij-SMS (<15%)", settings.batteryAlertEnabled) { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    vm.toggleBatteryAlert() 
                }
                SettToggle("🔌 Oplaadherinnering (22:00)", settings.chargingReminderEnabled) { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    vm.toggleChargingReminder() 
                }
            }

            // --- GROEP 4: SYSTEEM & BEHEER ---
            SettingsGroup(title = "⚙️ Systeem & Beheer", icon = Icons.Default.Build) {
                // UPDATE SECTIE
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("Versie: ${com.seniorenlauncher.BuildConfig.VERSION_NAME}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            scope.launch {
                                isCheckingUpdate = true
                                val updateManager = UpdateManager(context)
                                val release = updateManager.checkForUpdates()
                                if (release != null) {
                                    updateResult = "Nieuwe versie gevonden!"
                                    updateManager.downloadAndInstall(release)
                                } else {
                                    updateResult = "U heeft de nieuwste versie."
                                }
                                isCheckingUpdate = false
                            }
                        },
                        enabled = !isCheckingUpdate,
                        modifier = Modifier.fillMaxWidth().height(64.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (isCheckingUpdate) CircularProgressIndicator(Modifier.size(24.dp), color = Color.White)
                        else Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SystemUpdate, null)
                            Spacer(Modifier.width(12.dp))
                            Text("CONTROLEER OP UPDATES", fontWeight = FontWeight.Black, fontSize = 18.sp)
                        }
                    }
                    updateResult?.let { Text(it, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 12.dp)) }
                }

                HorizontalDivider(Modifier.padding(vertical = 16.dp), thickness = 0.5.dp)

                SettRow("📱 Hulp op Afstand (Web)") { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onNavigate("remote_support") 
                }
                
                Spacer(Modifier.height(16.dp))
                
                Button(
                    onClick = { 
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        vm.lockSettings() 
                    },
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Lock, null)
                    Spacer(Modifier.width(12.dp))
                    Text("MENU VERGRENDELEN", fontWeight = FontWeight.Black, fontSize = 18.sp)
                }
            }

            // --- GROEP 5: WAARDERING & TOEKOMST ---
            SettingsGroup(title = "❤️ Waardering", icon = Icons.Default.Favorite) {
                Text(
                    "Dit is een open-source project gebouwd met liefde voor onze ouderen. Uw steun helpt bij het dekken van de kosten en het verder verbeteren van de app.",
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com/donate/?business=amine.chtaiti@gmail.com&no_recurring=0&currency_code=EUR"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.filledTonalButtonColors()
                ) {
                    Icon(Icons.Default.VolunteerActivism, null) // VolunteerActivism acts as a nice "heart in hand" icon
                    Spacer(Modifier.width(12.dp))
                    Text("STEUN HET PROJECT", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(Modifier.height(60.dp))
        }
    }
}

@Composable
fun SettingsGroup(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Card(
        Modifier.fillMaxWidth(), 
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Text(title, fontSize = 20.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(20.dp))
            content()
        }
    }
}

@Composable
fun SettToggle(label: String, checked: Boolean, onToggle: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable { onToggle() }.padding(vertical = 12.dp), 
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 18.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = { onToggle() }, scale = 1.2f)
    }
}

@Composable
fun SettRow(label: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(28.dp))
    }
}

@Composable
fun LockedSettingsScreen(onBack: () -> Unit, onUnlockClick: () -> Unit) {
    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Lock, null, modifier = Modifier.size(120.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(32.dp))
        Text("Instellingen Vergrendeld", fontSize = 32.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Text(
            "Dit menu is beveiligd om te voorkomen dat instellingen per ongeluk worden gewijzigd.", 
            fontSize = 20.sp, 
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(48.dp))
        Button(
            onClick = onUnlockClick,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth().height(90.dp)
        ) {
            Text("PINCODE INVOEREN", fontSize = 24.sp, fontWeight = FontWeight.Black)
        }
        TextButton(onClick = onBack, modifier = Modifier.padding(top = 24.dp)) {
            Text("TERUG NAAR START", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PinEntryScreen(pin: String, onPinChange: (String) -> Unit, onCancel: () -> Unit) {
    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Beheerders PIN", fontSize = 28.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(32.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            repeat(4) { i ->
                Box(
                    Modifier
                        .size(28.dp)
                        .background(
                            if (pin.length > i) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            CircleShape
                        )
                )
            }
        }
        Spacer(Modifier.height(48.dp))
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            val keys = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("", "0", "⌫")
            )
            keys.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    row.forEach { key ->
                        if (key.isEmpty()) {
                            Spacer(Modifier.size(80.dp))
                        } else {
                            Surface(
                                onClick = { onPinChange(key) },
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(80.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(key, fontSize = 32.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(32.dp))
        TextButton(onClick = onCancel) {
            Text("ANNULEREN", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun Switch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    scale: Float = 1f,
    enabled: Boolean = true
) {
    androidx.compose.material3.Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier.scale(scale),
        enabled = enabled
    )
}
