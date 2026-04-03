package com.seniorenlauncher.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.seniorenlauncher.data.model.ForecastDay
import com.seniorenlauncher.data.model.WeatherLocation
import com.seniorenlauncher.ui.components.ScreenHeader
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WeatherScreen(onBack: () -> Unit, viewModel: WeatherViewModel = viewModel()) {
    val weatherData by viewModel.currentWeather.collectAsState()
    val savedLocations by viewModel.savedLocations.collectAsState()
    val selectedLocation by viewModel.selectedLocation.collectAsState()
    
    var showLocationDialog by remember { mutableStateOf(false) }
    var citySearchQuery by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        ScreenHeader(title = "Weer & Advies", onBack = onBack)

        if (weatherData == null) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(16.dp))
                    Text("Weergegevens ophalen...", color = MaterialTheme.colorScheme.onBackground)
                }
            }
        } else {
            val data = weatherData!!
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Locatie Selector
                Card(
                    Modifier.fillMaxWidth().clickable { showLocationDialog = true },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Row(
                        Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(if (selectedLocation?.isCurrentLocation != false) Icons.Default.MyLocation else Icons.Default.LocationCity, null)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = selectedLocation?.cityName ?: "Huidige Locatie",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Text("WIJZIG", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    }
                }

                // Huidig Weer Kaart
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            getWeatherEmoji(data.iconUrl),
                            fontSize = 80.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        
                        Text(
                            "${data.temp.toInt()}°C",
                            fontSize = 72.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        Text(
                            data.condition,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // AI Kledingadvies Kaart
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4))
                ) {
                    Row(
                        Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("💡", fontSize = 40.sp)
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("KLEDINGADVIES", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color(0xFFF57F17))
                            Text(
                                data.clothingAdvice,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                lineHeight = 24.sp
                            )
                        }
                    }
                }

                // Komende Dagen
                Text("Komende 5 dagen", fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(Modifier.padding(16.dp)) {
                        data.forecast.forEach { day ->
                            ForecastRow(day)
                            if (day != data.forecast.last()) {
                                HorizontalDivider(Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)
                            }
                        }
                    }
                }
                
                Button(
                    onClick = { viewModel.refreshWeather() },
                    modifier = Modifier.fillMaxWidth().height(70.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Refresh, null)
                    Spacer(Modifier.width(12.dp))
                    Text("VERVERSEN", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                
                Spacer(Modifier.height(20.dp))
            }
        }
    }

    if (showLocationDialog) {
        Dialog(onDismissRequest = { showLocationDialog = false }) {
            Card(
                Modifier.fillMaxWidth().fillMaxHeight(0.8f),
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text("Locatie Wijzigen", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = citySearchQuery,
                        onValueChange = { citySearchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Zoek een stad...") },
                        trailingIcon = {
                            IconButton(onClick = { 
                                if (citySearchQuery.isNotBlank()) {
                                    viewModel.searchAndAddCity(citySearchQuery)
                                    citySearchQuery = ""
                                }
                            }) {
                                Icon(Icons.Default.Add, "Toevoegen")
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    LazyColumn(Modifier.weight(1f)) {
                        item {
                            LocationItem(
                                name = "Mijn Locatie (GPS)",
                                isCurrent = selectedLocation?.isCurrentLocation ?: true,
                                icon = Icons.Default.MyLocation,
                                onClick = {
                                    viewModel.selectCurrentLocation()
                                    showLocationDialog = false
                                }
                            )
                        }
                        
                        items(savedLocations) { loc ->
                            LocationItem(
                                name = loc.cityName,
                                isCurrent = selectedLocation?.id == loc.id,
                                icon = Icons.Default.LocationCity,
                                onClick = {
                                    viewModel.selectLocation(loc)
                                    showLocationDialog = false
                                },
                                onDelete = { viewModel.deleteLocation(loc) }
                            )
                        }
                    }
                    
                    Button(
                        onClick = { showLocationDialog = false },
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("SLUITEN")
                    }
                }
            }
        }
    }
}

@Composable
fun LocationItem(
    name: String, 
    isCurrent: Boolean, 
    icon: androidx.compose.ui.graphics.vector.ImageVector, 
    onClick: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null)
            Spacer(Modifier.width(12.dp))
            Text(name, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, null, tint = Color.Red)
                }
            }
        }
    }
}

@Composable
fun ForecastRow(day: ForecastDay) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(1f)) {
            Text(formatDate(day.date), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(day.condition, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        
        Text(getWeatherEmoji(day.iconUrl), fontSize = 32.sp)
        
        Row(Modifier.width(100.dp), horizontalArrangement = Arrangement.End) {
            Text("${day.maxTemp.toInt()}°", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(8.dp))
            Text("${day.minTemp.toInt()}°", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

fun formatDate(dateStr: String): String {
    return try {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)
        SimpleDateFormat("EEEE d MMM", Locale("nl")).format(date!!)
            .replaceFirstChar { it.uppercase() }
    } catch (e: Exception) {
        dateStr
    }
}

fun getWeatherEmoji(codeStr: String): String {
    val code = codeStr.toIntOrNull() ?: return "⛅"
    return when (code) {
        0 -> "☀️" // Clear
        1, 2, 3 -> "🌤️" // Clouds
        45, 48 -> "🌫️" // Fog
        51, 53, 55 -> "🌦️" // Drizzle
        61, 63, 65 -> "🌧️" // Rain
        71, 73, 75 -> "❄️" // Snow
        80, 81, 82 -> "🌦️" // Showers
        95 -> "⛈️" // Storm
        else -> "⛅"
    }
}
