package com.seniorenlauncher.ui.screens
import com.seniorenlauncher.R
import com.seniorenlauncher.util.i18n

import androidx.compose.ui.tooling.preview.Preview
import com.seniorenlauncher.ui.theme.SeniorenLauncherTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
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
    SettingsScreenContent(
        settings = settings,
        onNavigate = onNavigate,
        onBack = onBack,
        onVerifyPin = { vm.verifyPin(it) },
        onUnlockSettings = { vm.unlockSettings() },
        onUpdateTheme = { vm.updateTheme(it) },
        onUpdateLayout = { vm.updateLayout(it) },
        onUpdateFontSize = { vm.updateFontSize(it) },
        onToggleNightMode = { vm.toggleNightMode() },
        onUpdateLanguage = { vm.updateLanguage(it) },
        onToggleAppVisibility = { vm.toggleAppVisibility(it) },
        onToggleScamProtection = { vm.toggleScamProtection() },
        onToggleFallDetection = { vm.toggleFallDetection() },
        onToggleBatteryAlert = { vm.toggleBatteryAlert() },
        onToggleChargingReminder = { vm.toggleChargingReminder() },
        onLockSettings = { vm.lockSettings() }
    )
}

@Composable
fun SettingsScreenContent(
    settings: AppSettings,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit,
    onVerifyPin: (String) -> Boolean,
    onUnlockSettings: () -> Unit,
    onUpdateTheme: (AppTheme) -> Unit,
    onUpdateLayout: (LayoutType) -> Unit,
    onUpdateFontSize: (Int) -> Unit,
    onToggleNightMode: () -> Unit,
    onUpdateLanguage: (String) -> Unit,
    onToggleAppVisibility: (String) -> Unit,
    onToggleScamProtection: () -> Unit,
    onToggleFallDetection: () -> Unit,
    onToggleBatteryAlert: () -> Unit,
    onToggleChargingReminder: () -> Unit,
    onLockSettings: () -> Unit
) {
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
                        if (onVerifyPin(enteredPin)) {
                            onUnlockSettings()
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

    Column(Modifier.fillMaxSize().background(Color(0xFF0F172A)).padding(horizontal = 16.dp, vertical = 8.dp)) {
        ScreenHeader(title = i18n(R.string.beheer_instellingen), onBack = onBack)
        
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()), 
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // --- GROEP 1: SCHERM & STIJL ---
            SettingsGroup(title = i18n(R.string.scherm_stijl), icon = Icons.Default.Palette) {
                Text(i18n(R.string.kleurthema), fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppTheme.entries.forEach { t ->
                        val selected = settings.theme == t
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (selected) Color(0xFF6366F1).copy(alpha = 0.2f) else Color(0xFF1E293B),
                                    RoundedCornerShape(20.dp)
                                )
                                .border(
                                    1.dp, 
                                    if (selected) Color(0xFF6366F1) else Color.White.copy(alpha = 0.1f),
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable { 
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onUpdateTheme(t) 
                                }
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(Modifier.size(24.dp).background(if (t == AppTheme.LIGHT) Color.Black else Color.White, CircleShape))
                            Text(
                                text = when(t){ 
                                    AppTheme.CLASSIC -> i18n(R.string.klassiek)
                                    AppTheme.HIGH_CONTRAST -> i18n(R.string.contrast)
                                    AppTheme.LIGHT -> i18n(R.string.licht) 
                                },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                
                Spacer(Modifier.height(16.dp))

                Text(i18n(R.string.knoppen_indeling), fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(LayoutType.GRID_2x3 to i18n(R.string.groot), LayoutType.GRID_3x4 to i18n(R.string.compact), LayoutType.GRID_1x1 to i18n(R.string.enorm)).forEach { (type, label) ->
                        val selected = settings.layout == type
                        Button(
                            onClick = { 
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onUpdateLayout(type) 
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp),
                            colors = if (selected) ButtonDefaults.buttonColors() else ButtonDefaults.filledTonalButtonColors()
                        ) {
                            Text(label, fontSize = 14.sp)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(i18n(R.string.tekstgrootte), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("A", fontSize = 16.sp)
                    Slider(
                        value = settings.fontSize.toFloat(), 
                        onValueChange = { onUpdateFontSize(it.toInt()) }, 
                        valueRange = 16f..36f, 
                        modifier = Modifier.weight(1f)
                    )
                    Text("A", fontSize = 32.sp, fontWeight = FontWeight.Bold)
                }
                
                SettToggle(i18n(R.string.automatische_nachtmodus), settings.nightModeAuto) { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onToggleNightMode() 
                }
            }

            // --- GROEP 2: APPS & TAAL ---
            SettingsGroup(title = i18n(R.string.apps_taal), icon = Icons.Default.Language) {
                Text(i18n(R.string.taal), fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                val languages = listOf("nl" to "Nederlands", "en" to "English", "fr" to "Français", "de" to "Deutsch", "ru" to "Русский")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    languages.forEach { (code, name) ->
                        val selected = settings.language == code
                        Button(
                            onClick = { 
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                com.seniorenlauncher.util.LocaleHelper.save(context, code)
                                com.seniorenlauncher.util.Str.refresh(context)
                                onUpdateLanguage(code)
                                (context as? android.app.Activity)?.recreate()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp),
                            colors = if (selected) ButtonDefaults.buttonColors() else ButtonDefaults.filledTonalButtonColors(),
                            contentPadding = PaddingValues(4.dp)
                        ) {
                            Text(name, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(i18n(R.string.zichtbare_apps), fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                ALL_APPS.filter { it.id != "settings" && it.id != "all_apps" }.forEach { app ->
                    SettToggle("${app.emoji} ${app.name}", app.id in settings.visibleApps) { 
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleAppVisibility(app.id) 
                    }
                }
            }

            // --- GROEP 3: VEILIGHEID & NOODHULP ---
            SettingsGroup(title = i18n(R.string.veiligheid_noodhulp), icon = Icons.Default.Security) {
                SettRow(i18n(R.string.medische_noodinfo_bewerken)) { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onNavigate("emergency") 
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
                SettRow(i18n(R.string.sos_contacten_instellen)) { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onNavigate("sos_settings") 
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
                SettToggle(i18n(R.string.anti_scam_filter), settings.scamProtectionEnabled) { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onToggleScamProtection() 
                }
                SettToggle(i18n(R.string.valdetectie), settings.fallDetectionEnabled) { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onToggleFallDetection() 
                }
                SettToggle(i18n(R.string.batterij_sms_15), settings.batteryAlertEnabled) { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onToggleBatteryAlert() 
                }
                SettToggle(i18n(R.string.oplaadherinnering_22_00), settings.chargingReminderEnabled) { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onToggleChargingReminder() 
                }
            }

            // --- GROEP 4: SYSTEEM & BEHEER ---
            SettingsGroup(title = i18n(R.string.systeem_beheer), icon = Icons.Default.Build) {
                // UPDATE SECTIE
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(i18n(R.string.versie, com.seniorenlauncher.BuildConfig.VERSION_NAME), fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            scope.launch {
                                isCheckingUpdate = true
                                val updateManager = UpdateManager(context)
                                val release = updateManager.checkForUpdates()
                                if (release != null) {
                                    updateResult = i18n(R.string.nieuwe_versie_gevonden)
                                    updateManager.downloadAndInstall(release)
                                } else {
                                    updateResult = i18n(R.string.u_heeft_de_nieuwste_versie)
                                }
                                isCheckingUpdate = false
                            }
                        },
                        enabled = !isCheckingUpdate,
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        shape = RoundedCornerShape(40.dp)
                    ) {
                        if (isCheckingUpdate) CircularProgressIndicator(Modifier.size(24.dp), color = Color.White)
                        else Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SystemUpdate, null)
                            Spacer(Modifier.width(12.dp))
                            Text(i18n(R.string.controleer_op_updates), fontWeight = FontWeight.Black, fontSize = 18.sp)
                        }
                    }
                    updateResult?.let { Text(it, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 12.dp)) }
                }

                HorizontalDivider(Modifier.padding(vertical = 16.dp), thickness = 0.5.dp)

                SettRow(i18n(R.string.hulp_op_afstand_web)) { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onNavigate("remote_support") 
                }
                
                HorizontalDivider(Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)

                SettRow(i18n(R.string.geavanceerde_instellingen)) {
                    // Placeholder voor toekomstige systeem-integraties
                }
                
                Spacer(Modifier.height(16.dp))
                
                Button(
                    onClick = { 
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLockSettings() 
                    },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(40.dp)
                ) {
                    Icon(Icons.Default.Lock, null)
                    Spacer(Modifier.width(12.dp))
                    Text(i18n(R.string.menu_vergrendelen), fontWeight = FontWeight.Black, fontSize = 18.sp)
                }
            }

            // --- GROEP 5: OVER & PRIVACY ---
            SettingsGroup(title = i18n(R.string.over_privacy), icon = Icons.Default.Info) {
                Text(
                    i18n(R.string.senioren_launcher_is_een_open),
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                SettRow(i18n(R.string.privacy_avg_details)) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onNavigate("privacy")
                }
            }

            // --- GROEP 6: WAARDERING & TOEKOMST ---
            SettingsGroup(title = i18n(R.string.waardering), icon = Icons.Default.Favorite) {
                Text(
                    i18n(R.string.dit_is_een_open_source),
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com/ncp/payment/U42MC9YY623QA"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    shape = RoundedCornerShape(40.dp),
                    colors = ButtonDefaults.filledTonalButtonColors()
                ) {
                    Icon(Icons.Default.VolunteerActivism, null) // VolunteerActivism acts as a nice "heart in hand" icon
                    Spacer(Modifier.width(12.dp))
                    Text(i18n(R.string.steun_het_project), fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
        shape = RoundedCornerShape(40.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
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
        Text(label, fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White)
        Icon(Icons.Default.ChevronRight, null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(28.dp))
    }
}

@Composable
fun LockedSettingsScreen(onBack: () -> Unit, onUnlockClick: () -> Unit) {
    Column(
        Modifier.fillMaxSize().background(Color(0xFF0F172A)).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Lock, null, modifier = Modifier.size(120.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(32.dp))
        Text(i18n(R.string.instellingen_vergrendeld), fontSize = 32.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, color = Color.White)
        Spacer(Modifier.height(16.dp))
        Text(
            i18n(R.string.dit_menu_is_beveiligd_om), 
            fontSize = 20.sp, 
            textAlign = TextAlign.Center,
            color = Color.White.copy(alpha = 0.7f)
        )
        Spacer(Modifier.height(48.dp))
        Button(
            onClick = onUnlockClick,
            shape = RoundedCornerShape(40.dp),
            modifier = Modifier.fillMaxWidth().height(90.dp)
        ) {
            Text(i18n(R.string.pincode_invoeren), fontSize = 24.sp, fontWeight = FontWeight.Black)
        }
        TextButton(onClick = onBack, modifier = Modifier.padding(top = 24.dp)) {
            Text(i18n(R.string.terug_naar_start), fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White.copy(alpha = 0.7f))
        }
    }
}

@Composable
fun PinEntryScreen(pin: String, onPinChange: (String) -> Unit, onCancel: () -> Unit) {
    Column(
        Modifier.fillMaxSize().background(Color(0xFF0F172A)).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(i18n(R.string.beheerders_pin), fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
        Spacer(Modifier.height(32.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            repeat(4) { i ->
                Box(
                    Modifier
                        .size(28.dp)
                        .background(
                            if (pin.length > i) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.1f),
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
                                color = Color.White.copy(alpha = 0.1f),
                                modifier = Modifier.size(80.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(key, fontSize = 32.sp, fontWeight = FontWeight.Black, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(32.dp))
        TextButton(onClick = onCancel) {
            Text(i18n(R.string.annuleren), fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color(0xFFEF4444))
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

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    SeniorenLauncherTheme {
        SettingsScreenContent(
            settings = AppSettings(),
            onNavigate = {},
            onBack = {},
            onVerifyPin = { true },
            onUnlockSettings = {},
            onUpdateTheme = {},
            onUpdateLayout = {},
            onUpdateFontSize = {},
            onToggleNightMode = {},
            onUpdateLanguage = {},
            onToggleAppVisibility = {},
            onToggleScamProtection = {},
            onToggleFallDetection = {},
            onToggleBatteryAlert = {},
            onToggleChargingReminder = {},
            onLockSettings = {}
        )
    }
}
