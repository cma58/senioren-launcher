package com.seniorenlauncher.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.seniorenlauncher.data.model.WeatherLocation
import com.seniorenlauncher.data.model.SafetyStatus
import com.seniorenlauncher.data.model.DayPartForecast
import com.seniorenlauncher.ui.components.ScreenHeader
import java.util.*

@Composable
fun WeatherScreen(onBack: () -> Unit, viewModel: WeatherViewModel = viewModel()) {
    val context = LocalContext.current
    val weatherData by viewModel.currentWeather.collectAsState()
    val safetyStatus by viewModel.safetyStatus.collectAsState()
    val dayParts by viewModel.dayParts.collectAsState()
    val selectedLocation by viewModel.selectedLocation.collectAsState()
    val savedLocations by viewModel.savedLocations.collectAsState()
    
    var showLocationDialog by remember { mutableStateOf(false) }

    var hasLocationPermission by remember { 
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) 
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                hasLocationPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                if (hasLocationPermission) viewModel.refreshWeather()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(Unit) {
        tts = TextToSpeech(context) { status -> if (status == TextToSpeech.SUCCESS) tts?.language = Locale.forLanguageTag("nl-NL") }
        onDispose { tts?.stop(); tts?.shutdown() }
    }

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
            ScreenHeader(title = "Weer in ${selectedLocation?.cityName ?: "uw regio"}", onBack = onBack)

            if (!hasLocationPermission) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    color = Color(0xFFEF4444).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(40.dp),
                    border = BorderStroke(2.dp, Color(0xFFEF4444)),
                    onClick = {
                        context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply { data = Uri.fromParts("package", context.packageName, null) })
                    }
                ) {
                    Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOff, null, tint = Color(0xFFEF4444), modifier = Modifier.size(32.dp))
                        Spacer(Modifier.width(16.dp))
                        Text("Locatie-toegang nodig", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                    }
                }
            }

            // Location Selection Button (Glass Pill)
            val interactionSourceLoc = remember { MutableInteractionSource() }
            val pressedLoc by interactionSourceLoc.collectIsPressedAsState()
            val scaleLoc by animateFloatAsState(if (pressedLoc) 0.94f else 1f, spring(dampingRatio = 0.7f), label = "scale")

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .height(80.dp)
                    .scale(scaleLoc)
                    .clickable(
                        interactionSource = interactionSourceLoc,
                        indication = null,
                        onClick = { showLocationDialog = true }
                    ),
                shape = RoundedCornerShape(40.dp),
                color = Color(0xFF1E293B),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.LocationOn, null, tint = Color(0xFF6366F1), modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("ANDERE STAD KIEZEN", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White)
                }
            }

            if (weatherData == null) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { 
                    CircularProgressIndicator(color = Color.White) 
                }
            } else {
                val data = weatherData!!
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    SafetyTrafficLight(safetyStatus, data.temp)
                    
                    // Main Weather Display (Bento Style)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(40.dp),
                        color = Color(0xFF1E293B),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                    ) {
                        Column(Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("KLEDINGADVIES", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color(0xFF6366F1), letterSpacing = 2.sp)
                            Spacer(Modifier.height(12.dp))
                            Text(data.clothingIcons, fontSize = 80.sp)
                        }
                    }

                    // Large Audio Orb
                    val interactionSourceAudio = remember { MutableInteractionSource() }
                    val pressedAudio by interactionSourceAudio.collectIsPressedAsState()
                    val scaleAudio by animateFloatAsState(if (pressedAudio) 0.94f else 1f, spring(dampingRatio = 0.7f), label = "scale")

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .scale(scaleAudio)
                            .clickable(
                                interactionSource = interactionSourceAudio,
                                indication = null,
                                onClick = {
                                    val statusText = when(safetyStatus) {
                                        SafetyStatus.RED -> if (data.temp > 30) "Blijf binnen, extreem warm." else "Blijf binnen, gevaarlijk weer."
                                        SafetyStatus.ORANGE -> "Let op, kans op regen of wind."
                                        SafetyStatus.GREEN -> "Heerlijk weer om buiten te komen!"
                                    }
                                    val speech = "Het is nu ${data.temp.toInt()} graden. $statusText ${data.gardenAdvice} ${data.windowAdvice} ${data.activityAdvice} ${data.uvAdvice}"
                                    tts?.speak(speech, TextToSpeech.QUEUE_FLUSH, null, "WeatherSpeak")
                                }
                            ),
                        shape = RoundedCornerShape(40.dp),
                        color = Color(0xFF6366F1), // Modern Indigo
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            Modifier.padding(horizontal = 24.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, null, modifier = Modifier.size(44.dp), tint = Color.White)
                            Spacer(Modifier.width(20.dp))
                            Text("LUISTER NAAR ADVIES", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                    }

                    if (data.gardenAdvice.isNotEmpty()) AdviceCard(data.gardenAdvice, Color(0xFF10B981), "🌱")
                    if (data.windowAdvice.isNotEmpty()) AdviceCard(data.windowAdvice, Color(0xFF3B82F6), "🪟")
                    if (data.activityAdvice.isNotEmpty()) AdviceCard(data.activityAdvice, Color(0xFF8B5CF6), "🚶")
                    if (data.uvAdvice.isNotEmpty()) AdviceCard(data.uvAdvice, Color(0xFFF59E0B), "☀️")

                    Text("DAGDELEN", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.White, modifier = Modifier.padding(top = 8.dp))
                    dayParts.forEach { part -> DayPartCard(part) }
                    
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }

    if (showLocationDialog) {
        LocationDialog(
            savedLocations = savedLocations,
            onDismiss = { showLocationDialog = false },
            onSelect = { loc -> viewModel.selectLocation(loc); showLocationDialog = false },
            onSearch = { viewModel.searchAndAddCity(it) },
            onDelete = { viewModel.deleteLocation(it) }
        )
    }
}

@Composable
fun AdviceCard(text: String, accentColor: Color, icon: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(40.dp),
        color = accentColor.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
    ) {
        Row(Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(64.dp).background(accentColor.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 32.sp)
            }
            Spacer(Modifier.width(20.dp))
            Text(text, fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White)
        }
    }
}

@Composable
fun SafetyTrafficLight(status: SafetyStatus, temp: Double) {
    val (color, text) = when(status) {
        SafetyStatus.RED -> Color(0xFFEF4444) to if (temp > 30) "EXTREME HITTE: Blijf binnen" else "GEVAARLIJK WEER: Blijf binnen"
        SafetyStatus.ORANGE -> Color(0xFFF59E0B) to "PAS OP: Kans op regen of wind"
        SafetyStatus.GREEN -> Color(0xFF10B981) to "VEILIG: Heerlijk weer om buiten te zijn"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(40.dp),
        color = color.copy(alpha = 0.15f),
        border = BorderStroke(3.dp, color)
    ) {
        Row(Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Box(Modifier.size(16.dp).background(color, CircleShape))
            Spacer(Modifier.width(16.dp))
            Text(text.uppercase(), fontSize = 18.sp, fontWeight = FontWeight.Black, color = color, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun DayPartCard(part: DayPartForecast) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(40.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(part.label.uppercase(), fontSize = 12.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, letterSpacing = 1.sp)
                Text(part.condition, fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.White)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(getWeatherEmoji(part.icon), fontSize = 44.sp)
                Spacer(Modifier.width(20.dp))
                Text("${part.temp}°", fontSize = 32.sp, fontWeight = FontWeight.Black, color = Color.White)
            }
        }
    }
}

@Composable
fun LocationDialog(
    savedLocations: List<WeatherLocation>,
    onDismiss: () -> Unit,
    onSelect: (WeatherLocation) -> Unit,
    onSearch: (String) -> Unit,
    onDelete: (WeatherLocation) -> Unit
) {
    var query by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            Modifier.fillMaxWidth().fillMaxHeight(0.8f),
            shape = RoundedCornerShape(40.dp),
            color = Color(0xFF1E293B), // Slate 800
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Column(Modifier.padding(24.dp)) {
                Text("Locatie Kiezen", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
                Spacer(Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = query, 
                    onValueChange = { query = it }, 
                    modifier = Modifier.fillMaxWidth(), 
                    label = { Text("Zoek stad...", fontWeight = FontWeight.Bold) },
                    shape = RoundedCornerShape(40.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                
                Button(
                    onClick = { onSearch(query); query = "" }, 
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp).height(70.dp),
                    shape = RoundedCornerShape(40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) { 
                    Text("ZOEKEN & TOEVOEGEN", fontWeight = FontWeight.Black) 
                }
                
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(savedLocations) { loc: WeatherLocation ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onSelect(loc) },
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.05f)
                        ) {
                            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationCity, null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(12.dp))
                                Text(loc.cityName, fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White, modifier = Modifier.weight(1f))
                                IconButton(onClick = { onDelete(loc) }) { 
                                    Icon(Icons.Default.Delete, null, tint = Color.White.copy(alpha = 0.3f)) 
                                }
                            }
                        }
                    }
                }
                
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth().height(60.dp)) { 
                    Text("SLUITEN", color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

fun getWeatherEmoji(codeStr: String): String {
    val code = codeStr.toIntOrNull() ?: return "⛅"
    return when (code) {
        0 -> "☀️"; 1, 2, 3 -> "🌤️"; 45, 48 -> "🌫️"; 51, 53, 55 -> "🌦️"; 61, 63, 65 -> "🌧️"; 71, 73, 75 -> "❄️"; 80, 81, 82 -> "🌦️"; 95, 96, 99 -> "⛈️"; else -> "⛅"
    }
}
