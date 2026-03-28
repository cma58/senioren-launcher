package com.seniorenlauncher.ui.screens
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seniorenlauncher.data.model.LayoutType
import com.seniorenlauncher.ui.components.*
import java.text.SimpleDateFormat
import java.util.*

data class HomeApp(val id: String, val name: String, val emoji: String, val color: Color, val badge: Int? = null)

val ALL_APPS = listOf(
    HomeApp("phone","Bellen","📞",Color(0xFF38A169)),
    HomeApp("sms","Berichten","💬",Color(0xFF3B82F6),2),
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
)

@Composable
fun HomeScreen(onNavigate: (String) -> Unit, settingsVm: SettingsViewModel) {
    val settings by settingsVm.settings.collectAsState()
    val visible = ALL_APPS.filter { it.id in settings.visibleApps }
    val cols = when (settings.layout) { LayoutType.GRID_2x3 -> 2; LayoutType.GRID_3x4 -> 3; LayoutType.GRID_1x1 -> 1 }
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(horizontal = 12.dp, vertical = 8.dp)) {
        ClockWidget()
        Spacer(Modifier.height(8.dp))
        LazyVerticalGrid(columns = GridCells.Fixed(cols), Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(visible) { app -> BigButton(emoji = app.emoji, label = app.name, color = app.color, badge = app.badge,
                small = settings.layout == LayoutType.GRID_3x4, onClick = { onNavigate(app.id) },
                modifier = Modifier.fillMaxWidth().height(if (settings.layout == LayoutType.GRID_1x1) 100.dp else 80.dp)) }
        }
        Spacer(Modifier.height(8.dp))
        SOSButton(onClick = { onNavigate("sos") })
    }
}

@Composable
fun ClockWidget() {
    var time by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { time = System.currentTimeMillis(); kotlinx.coroutines.delay(1000) } }
    val cal = Calendar.getInstance().apply { timeInMillis = time }
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(SimpleDateFormat("HH:mm", Locale.getDefault()).format(cal.time),
            fontSize = 48.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground, letterSpacing = 3.sp)
        Text(SimpleDateFormat("EEEE d MMMM", Locale("nl")).format(cal.time),
            fontSize = 15.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
