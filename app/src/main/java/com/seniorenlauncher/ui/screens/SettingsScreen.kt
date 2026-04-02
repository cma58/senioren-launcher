package com.seniorenlauncher.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seniorenlauncher.data.model.*
import com.seniorenlauncher.ui.components.ScreenHeader

@Composable
fun SettingsScreen(vm: SettingsViewModel, onNavigate: (String) -> Unit, onBack: () -> Unit) {
    val settings by vm.settings.collectAsState()
    val fontSizeMultiplier = settings.fontSize / 16f
    
    var showPinScreen by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }

    if (settings.settingsLocked && !showPinScreen) {
        LockedSettingsScreen(
            onBack = onBack,
            onUnlockClick = { showPinScreen = true },
            fontSizeMultiplier = fontSizeMultiplier
        )
        return
    }

    if (showPinScreen) {
        PinEntryScreen(
            pin = enteredPin,
            onPinChange = { key ->
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
            },
            fontSizeMultiplier = fontSizeMultiplier
        )
        return
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(horizontal = 12.dp, vertical = 8.dp)) {
        ScreenHeader(title = "Instellingen", onBack = onBack)
        
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()), 
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Thema
            SettSection("🎨 Thema", fontSizeMultiplier) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppTheme.entries.forEach { t ->
                        val selected = settings.theme == t
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { vm.updateTheme(t) }
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(Modifier.size(20.dp).background(if (t == AppTheme.LIGHT) Color.Black else Color.White, CircleShape))
                            Text(
                                text = when(t){ 
                                    AppTheme.CLASSIC -> "Klassiek"
                                    AppTheme.HIGH_CONTRAST -> "Contrast"
                                    AppTheme.LIGHT -> "Licht" 
                                },
                                fontSize = 10.sp * fontSizeMultiplier,
                                fontWeight = FontWeight.SemiBold,
                                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Layout
            SettSection("📐 Layout", fontSizeMultiplier) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        LayoutType.GRID_2x3 to "2×3 Groot",
                        LayoutType.GRID_3x4 to "3×4 Compact",
                        LayoutType.GRID_1x1 to "1×1 Extra groot"
                    ).forEach { (type, label) ->
                        val selected = settings.layout == type
                        Button(
                            onClick = { vm.updateLayout(type) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = if (selected) ButtonDefaults.buttonColors() else ButtonDefaults.filledTonalButtonColors(),
                            contentPadding = PaddingValues(4.dp)
                        ) {
                            Text(label, fontSize = 11.sp * fontSizeMultiplier, textAlign = TextAlign.Center)
                        }
                    }
                }
            }

            // Lettergrootte
            SettSection("🔤 Lettergrootte", fontSizeMultiplier) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("A", fontSize = 12.sp)
                        Slider(
                            value = settings.fontSize.toFloat(), 
                            onValueChange = { vm.updateFontSize(it.toInt()) }, 
                            valueRange = 14f..30f, 
                            modifier = Modifier.weight(1f)
                        )
                        Text("A", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        "Voorbeeld tekst — ${settings.fontSize}px", 
                        fontSize = settings.fontSize.sp, 
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    )
                }
            }

            // Taal
            SettSection("🌍 Taal", fontSizeMultiplier) {
                val languages = listOf("nl" to "Nederlands", "fr" to "Français", "de" to "Deutsch", "en" to "English", "tr" to "Türkçe", "ar" to "العربية")
                Column {
                    languages.chunked(3).forEach { row ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            row.forEach { (code, name) ->
                                val selected = settings.language == code
                                Button(
                                    onClick = { vm.updateLanguage(code) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(6.dp),
                                    colors = if (selected) ButtonDefaults.buttonColors() else ButtonDefaults.filledTonalButtonColors(),
                                    contentPadding = PaddingValues(2.dp)
                                ) {
                                    Text(name, fontSize = 12.sp * fontSizeMultiplier)
                                }
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }

            // Nachtmodus
            SettSection("🌙 Nachtmodus", fontSizeMultiplier) {
                SettToggle("Automatisch na 21:00", settings.nightModeAuto, fontSizeMultiplier) { vm.toggleNightMode() }
            }

            // Zichtbare apps
            SettSection("📱 Zichtbare apps", fontSizeMultiplier) {
                ALL_APPS.forEachIndexed { index, app ->
                    val isVisible = app.id in settings.visibleApps
                    SettToggle("${app.emoji} ${app.name}", isVisible, fontSizeMultiplier) { 
                        vm.toggleAppVisibility(app.id) 
                    }
                    if (index < ALL_APPS.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            // Veiligheid
            SettSection("🛡️ Veiligheid", fontSizeMultiplier) {
                SettRow("📍 Locatie delen", fontSizeMultiplier) { /* Optioneel: later implementeren */ }
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                SettRow("🏥 Noodinfo bewerken", fontSizeMultiplier) { 
                    onNavigate("emergency") // Gaat naar EmergencyInfoScreen
                }
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                SettRow("🆘 SOS contacten instellen", fontSizeMultiplier) { 
                    onNavigate("sos_settings")
                }
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                SettToggle("🛡️ Valdetectie", settings.fallDetectionEnabled, fontSizeMultiplier) { vm.toggleFallDetection() }
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                SettToggle("🪫 Batterij-SMS onder 15%", settings.batteryAlertEnabled, fontSizeMultiplier) { vm.toggleBatteryAlert() }
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                SettToggle("🔌 Oplaadherinnering 22:00", settings.chargingReminderEnabled, fontSizeMultiplier) { vm.toggleChargingReminder() }
            }

            // Afstandsbediening
            SettSection("📱 Afstandsbediening", fontSizeMultiplier) {
                Text(
                    "Laat een familielid deze telefoon op afstand instellen via de web-app.",
                    fontSize = 13.sp * fontSizeMultiplier,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Column(
                    Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)).padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Koppelingscode", fontSize = 12.sp * fontSizeMultiplier, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("7 4 2 9", fontSize = 24.sp * fontSizeMultiplier, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, letterSpacing = 4.sp)
                }
            }

            // PIN Vergrendeling
            SettSection("🔒 PIN vergrendeling", fontSizeMultiplier) {
                Button(
                    onClick = { vm.lockSettings() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Instellingen vergrendelen", fontWeight = FontWeight.Bold, fontSize = 15.sp * fontSizeMultiplier)
                }
            }

            // Over
            SettSection("ℹ️ Over", fontSizeMultiplier) {
                Text(
                    "Senioren Launcher v1.0\nOpen Source · GPL-3.0 Licentie\nGeen tracking · Geen advertenties\nGemaakt met liefde voor onze ouderen ❤️",
                    fontSize = 13.sp * fontSizeMultiplier, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant, 
                    lineHeight = (18.sp * fontSizeMultiplier)
                )
            }
            
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
fun SettSection(title: String, fontSizeMultiplier: Float, content: @Composable ColumnScope.() -> Unit) {
    Card(
        Modifier.fillMaxWidth(), 
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                title, 
                fontSize = 15.sp * fontSizeMultiplier, 
                fontWeight = FontWeight.Bold, 
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
fun SettToggle(label: String, checked: Boolean, fontSizeMultiplier: Float, onToggle: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp), 
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp * fontSizeMultiplier, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = { onToggle() }, scale = 0.9f)
    }
}

@Composable
fun SettRow(label: String, fontSizeMultiplier: Float, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp * fontSizeMultiplier, color = MaterialTheme.colorScheme.onSurface)
        Text("→", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun LockedSettingsScreen(onBack: () -> Unit, onUnlockClick: () -> Unit, fontSizeMultiplier: Float) {
    Column(
        Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🔒", fontSize = 52.sp)
        Spacer(Modifier.height(12.dp))
        Text("Instellingen Vergrendeld", fontSize = 22.sp * fontSizeMultiplier, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(
            "Alleen de verzorger kan instellingen wijzigen", 
            fontSize = 14.sp * fontSizeMultiplier, 
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onUnlockClick,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("PIN Invoeren", fontSize = 16.sp * fontSizeMultiplier, fontWeight = FontWeight.Bold)
        }
        TextButton(onClick = onBack, modifier = Modifier.padding(top = 10.dp)) {
            Text("Terug", fontSize = 14.sp * fontSizeMultiplier)
        }
    }
}

@Composable
fun PinEntryScreen(pin: String, onPinChange: (String) -> Unit, onCancel: () -> Unit, fontSizeMultiplier: Float) {
    Column(
        Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Voer PIN in", fontSize = 22.sp * fontSizeMultiplier, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            repeat(4) { i ->
                Box(
                    Modifier
                        .size(18.dp)
                        .background(
                            if (pin.length > i) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surfaceVariant,
                            CircleShape
                        )
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val keys = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("", "0", "⌫")
            )
            keys.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { key ->
                        if (key.isEmpty()) {
                            Spacer(Modifier.size(56.dp))
                        } else {
                            Surface(
                                onClick = {
                                    onPinChange(key)
                                },
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(56.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(key, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        TextButton(onClick = onCancel) {
            Text("Annuleren", fontSize = 14.sp * fontSizeMultiplier)
        }
    }
}

// Extension to scale switch
@Composable
fun Switch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    scale: Float = 1f,
    enabled: Boolean = true,
    colors: SwitchColors = SwitchDefaults.colors(),
    interactionSource: androidx.compose.foundation.interaction.MutableInteractionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
) {
    androidx.compose.material3.Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier.scale(scale),
        enabled = enabled,
        colors = colors,
        interactionSource = interactionSource
    )
}
