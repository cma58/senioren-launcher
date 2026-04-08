package com.seniorenlauncher.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(12.dp)) {
        ScreenHeader(title = "Weer in ${selectedLocation?.cityName ?: "uw regio"}", onBack = onBack)

        if (!hasLocationPermission) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable {
                    context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply { data = Uri.fromParts("package", context.packageName, null) })
                },
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEF4444)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOff, null, tint = Color.White)
                    Spacer(Modifier.width(16.dp))
                    Text("⚠️ Locatie uit. Klik hier om in te stellen.", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        Button(onClick = { showLocationDialog = true }, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Icon(Icons.Default.LocationOn, null)
            Spacer(Modifier.width(8.dp))
            Text("ANDERE STAD KIEZEN", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        if (weatherData == null) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            val data = weatherData!!
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SafetyTrafficLight(safetyStatus, data.temp)
                
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                    Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("KLEDINGADVIES", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.Gray)
                        Text(data.clothingIcons, fontSize = 60.sp)
                    }
                }

                Button(
                    onClick = {
                        val statusText = when(safetyStatus) {
                            SafetyStatus.RED -> if (data.temp > 30) "Blijf binnen, extreem warm." else "Blijf binnen, gevaarlijk weer."
                            SafetyStatus.ORANGE -> "Let op, kans op regen of wind."
                            SafetyStatus.GREEN -> "Heerlijk weer om buiten te komen!"
                        }
                        val speech = "Het is nu ${data.temp.toInt()} graden. $statusText ${data.gardenAdvice} ${data.windowAdvice} ${data.activityAdvice} ${data.uvAdvice}"
                        tts?.speak(speech, TextToSpeech.QUEUE_FLUSH, null, "WeatherSpeak")
                    },
                    modifier = Modifier.fillMaxWidth().height(90.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, null, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("LEES ALLES VOOR", fontSize = 22.sp, fontWeight = FontWeight.Black)
                }

                if (data.gardenAdvice.isNotEmpty()) AdviceCard(data.gardenAdvice, Color(0xFFE8F5E9), "🌱")
                if (data.windowAdvice.isNotEmpty()) AdviceCard(data.windowAdvice, Color(0xFFE3F2FD), "🪟")
                if (data.activityAdvice.isNotEmpty()) AdviceCard(data.activityAdvice, Color(0xFFE0F2F1), "🚶")
                if (data.uvAdvice.isNotEmpty()) AdviceCard(data.uvAdvice, Color(0xFFFFF3E0), "☀️")

                Text("Vandaag in delen", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                dayParts.forEach { part -> DayPartCard(part) }
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
fun AdviceCard(text: String, color: Color, icon: String) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = color)) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 32.sp)
            Spacer(Modifier.width(16.dp))
            Text(text, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        }
    }
}

@Composable
fun SafetyTrafficLight(status: SafetyStatus, temp: Double) {
    val (color, text) = when(status) {
        SafetyStatus.RED -> Color(0xFFC53030) to if (temp > 30) "🔴 Blijf binnen! Extreem warm." else "🔴 Blijf binnen! Gevaarlijk glad of storm."
        SafetyStatus.ORANGE -> Color(0xFFDD6B20) to "🟠 Let op: Kans op regen of wind."
        SafetyStatus.GREEN -> Color(0xFF2F855A) to "🟢 Heerlijk weer om naar buiten te gaan!"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        border = androidx.compose.foundation.BorderStroke(4.dp, color)
    ) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text, fontSize = 24.sp, fontWeight = FontWeight.Black, color = color, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun DayPartCard(part: DayPartForecast) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(part.label, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Text(part.condition, fontSize = 22.sp, fontWeight = FontWeight.Black)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(getWeatherEmoji(part.icon), fontSize = 40.sp)
                Spacer(Modifier.width(16.dp))
                Text("${part.temp}°", fontSize = 32.sp, fontWeight = FontWeight.Black)
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
        Card(Modifier.fillMaxWidth().fillMaxHeight(0.7f), shape = RoundedCornerShape(24.dp)) {
            Column(Modifier.padding(20.dp)) {
                Text("Kies een plaats", fontSize = 24.sp, fontWeight = FontWeight.Black)
                OutlinedTextField(value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Zoek stad...") })
                Button(onClick = { onSearch(query); query = "" }, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) { Text("ZOEKEN / TOEVOEGEN") }
                LazyColumn(Modifier.weight(1f)) {
                    items(savedLocations) { loc: WeatherLocation ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onSelect(loc) }) {
                            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(loc.cityName, fontSize = 18.sp, modifier = Modifier.weight(1f))
                                IconButton(onClick = { onDelete(loc) }) { Icon(Icons.Default.Delete, null, tint = Color.Red) }
                            }
                        }
                    }
                }
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("ANNULEREN") }
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
