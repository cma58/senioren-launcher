package com.seniorenlauncher.ui.screens
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seniorenlauncher.data.model.*
import com.seniorenlauncher.ui.components.ScreenHeader

@Composable
fun SettingsScreen(vm: SettingsViewModel, onBack: () -> Unit) {
    val settings by vm.settings.collectAsState()
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(horizontal = 12.dp, vertical = 8.dp)) {
        ScreenHeader(title = "Instellingen", onBack = onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SettSection("🎨 Thema") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppTheme.entries.forEach { t ->
                        OutlinedButton(onClick = { vm.updateTheme(t) }, Modifier.weight(1f),
                            colors = if (settings.theme == t) ButtonDefaults.outlinedButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            ) else ButtonDefaults.outlinedButtonColors()) {
                            Text(when(t){ AppTheme.CLASSIC->"Klassiek"; AppTheme.HIGH_CONTRAST->"Contrast"; AppTheme.LIGHT->"Licht" }, fontSize=12.sp)
                        }
                    }
                }
            }
            SettSection("📐 Layout") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LayoutType.entries.forEach { l ->
                        OutlinedButton(onClick = { vm.updateLayout(l) }, Modifier.weight(1f),
                            colors = if (settings.layout == l) ButtonDefaults.outlinedButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            ) else ButtonDefaults.outlinedButtonColors()) {
                            Text(when(l){ LayoutType.GRID_2x3->"2×3"; LayoutType.GRID_3x4->"3×4"; LayoutType.GRID_1x1->"1×1"}, fontSize=13.sp)
                        }
                    }
                }
            }
            SettSection("🔤 Lettergrootte") {
                Slider(value = settings.fontSize.toFloat(), onValueChange = { vm.updateFontSize(it.toInt()) }, valueRange = 14f..24f, steps = 4)
                Text("Voorbeeld — ${settings.fontSize}px", fontSize = settings.fontSize.sp, color = MaterialTheme.colorScheme.onSurface)
            }
            SettSection("🛡️ Veiligheid") {
                SettToggle("Valdetectie", settings.fallDetectionEnabled) { vm.toggleFallDetection() }
                SettToggle("Batterij-SMS bij <15%", settings.batteryAlertEnabled) { vm.toggleBatteryAlert() }
                SettToggle("Nachtmodus (auto 21:00)", settings.nightModeAuto) { vm.toggleNightMode() }
            }
            SettSection("📱 Zichtbare apps") {
                ALL_APPS.forEach { app ->
                    SettToggle("${app.emoji} ${app.name}", app.id in settings.visibleApps) { vm.toggleAppVisibility(app.id) }
                }
            }
            SettSection("ℹ️ Over") {
                Text("Senioren Launcher v1.0.0\nOpen Source · GPL-3.0\nGeen tracking · Geen advertenties",
                    fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 20.sp)
            }
        }
    }
}

@Composable
fun SettSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(14.dp)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
fun SettToggle(label: String, checked: Boolean, onToggle: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
        Switch(checked = checked, onCheckedChange = { onToggle() })
    }
}
