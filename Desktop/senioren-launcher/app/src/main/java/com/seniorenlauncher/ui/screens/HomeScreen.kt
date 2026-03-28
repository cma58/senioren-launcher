package com.seniorenlauncher.ui.screens

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.MediaStore
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seniorenlauncher.data.model.LayoutType
import com.seniorenlauncher.ui.components.*
import java.text.SimpleDateFormat
import java.util.*

data class HomeApp(val id: String, val name: String, val emoji: String, val color: Color, val badge: Int? = null)

val ALL_APPS = listOf(
    HomeApp("phone", "Bellen", "📞", Color(0xFF38A169)),
    HomeApp("sms", "Berichten", "💬", Color(0xFF3B82F6), 2),
    HomeApp("whatsapp", "WhatsApp", "🟢", Color(0xFF25D366)),
    HomeApp("video", "Videobellen", "🎥", Color(0xFF8B5CF6)),
    HomeApp("camera", "Camera", "📷", Color(0xFFEC4899)),
    HomeApp("photos", "Foto's", "🖼️", Color(0xFFF59E0B)),
    HomeApp("alarm", "Wekker", "⏰", Color(0xFFEA580C)),
    HomeApp("calendar", "Agenda", "📅", Color(0xFF0D9488)),
    HomeApp("meds", "Medicijnen", "💊", Color(0xFFDC2626)),
    HomeApp("weather", "Weer", "🌤️", Color(0xFF0EA5E9)),
    HomeApp("flashlight", "Zaklamp", "🔦", Color(0xFFFBBF24)),
    HomeApp("magnifier", "Vergrootglas", "🔍", Color(0xFF6366F1)),
    HomeApp("notes", "Notities", "📝", Color(0xFF84CC16)),
    HomeApp("radio", "Radio", "📻", Color(0xFFA855F7)),
    HomeApp("steps", "Stappen", "🚶", Color(0xFF14B8A6)),
    HomeApp("emergency", "Noodinfo", "🏥", Color(0xFFEF4444)),
    HomeApp("sos", "SOS", "🆘", Color(0xFFDC2626)),
)

// Apps die een externe app openen in plaats van een intern scherm
val EXTERNAL_APPS = mapOf(
    "camera" to Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA),
    "whatsapp" to Intent().apply {
        setPackage("com.whatsapp")
        action = Intent.ACTION_MAIN
        addCategory(Intent.CATEGORY_LAUNCHER)
    },
    "video" to Intent().apply {
        setPackage("com.google.android.apps.tachyon") // Google Duo/Meet
        action = Intent.ACTION_MAIN
        addCategory(Intent.CATEGORY_LAUNCHER)
    },
)

@Composable
fun HomeScreen(onNav: (String) -> Unit, vm: SettingsViewModel) {
    val context = LocalContext.current
    val s by vm.settings.collectAsState()
    val apps = ALL_APPS.filter { it.id in s.visibleApps }
    val cols = when (s.layout) {
        LayoutType.GRID_2x3 -> 2
        LayoutType.GRID_3x4 -> 3
        LayoutType.GRID_1x1 -> 1
    }

    Column(
        Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(12.dp, 8.dp)
    ) {
        Clock()
        Spacer(Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(cols),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(apps) { app ->
                BigButton(
                    emoji = app.emoji,
                    label = app.name,
                    color = app.color,
                    badge = app.badge,
                    small = s.layout == LayoutType.GRID_3x4,
                    onClick = {
                        if (app.id in EXTERNAL_APPS) {
                            openExternalApp(context, app.id)
                        } else {
                            onNav(app.id)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                        .height(if (s.layout == LayoutType.GRID_1x1) 100.dp else 80.dp)
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        SOSButton(onClick = { onNav("sos") })

        // Bottom navigatie
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf(
                Triple("🏠", "Home", "home"),
                Triple("📞", "Bellen", "phone"),
                Triple("💬", "Berichten", "sms"),
                Triple("⚙️", "Instellingen", "settings"),
            ).forEach { (icon, label, route) ->
                Column(
                    Modifier.clickable { onNav(route) }.padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(icon, fontSize = 20.sp)
                    Text(
                        label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun openExternalApp(context: Context, appId: String) {
    val intent = EXTERNAL_APPS[appId] ?: return
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        // App niet gevonden, open de Play Store of doe niets
        try {
            val pkg = when (appId) {
                "whatsapp" -> "com.whatsapp"
                "video" -> "com.google.android.apps.tachyon"
                else -> return
            }
            context.startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse("market://details?id=$pkg")))
        } catch (_: Exception) {}
    }
}

@Composable
fun Clock() {
    var time by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { time = System.currentTimeMillis(); kotlinx.coroutines.delay(1000) } }
    val c = Calendar.getInstance().apply { timeInMillis = time }

    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(c.time),
            fontSize = 52.sp, fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground, letterSpacing = 3.sp
        )
        Text(
            SimpleDateFormat("EEEE d MMMM", Locale("nl")).format(c.time),
            fontSize = 16.sp, fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
