package com.seniorenlauncher.ui.screens

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.seniorenlauncher.LauncherApp
import com.seniorenlauncher.data.model.RadioCategory
import com.seniorenlauncher.data.model.RadioStation
import com.seniorenlauncher.ui.components.ScreenHeader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.URL

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RadioScreen(onBack: () -> Unit, radioVm: RadioViewModel = viewModel()) {
    val currentStation by radioVm.currentStation.collectAsState()
    val isPlaying by radioVm.isPlaying.collectAsState()
    val isLoading by radioVm.isLoading.collectAsState()
    val hasError by radioVm.hasError.collectAsState()
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dao = LauncherApp.instance.database.radioDao()
    val customStations by dao.getAll().collectAsState(initial = emptyList())
    
    var showAddDialog by remember { mutableStateOf(false) }

    val defaultStations = listOf(
        RadioStation(name = "Radio 1", url = "https://icecast.vrtcdn.be/radio1-high.mp3", emoji = "➊", category = "🇧🇪 België", colorValue = 0xFFEF4444L),
        RadioStation(name = "Radio 2", url = "https://icecast.vrtcdn.be/ra2ant-high.mp3", emoji = "➋", category = "🇧🇪 België", colorValue = 0xFFF59E0BL),
        RadioStation(name = "Joe", url = "https://stream.joe.be/joe", emoji = "☕", category = "🇧🇪 België", colorValue = 0xFFEC4899L),
        RadioStation(name = "Qmusic BE", url = "https://stream.qmusic.be/qmusic.aac", emoji = "Q", category = "🇧🇪 België", colorValue = 0xFFDC2626L),
        RadioStation(name = "NPO Radio 5", url = "https://icecast.omroep.nl/radio5-bb-mp3", emoji = "❺", category = "🇳🇱 Nederland", colorValue = 0xFFF59E0BL),
        RadioStation(name = "Radio 10", url = "https://stream.radio10.nl/radio10", emoji = "🔟", category = "🇳🇱 Nederland", colorValue = 0xFF84CC16L),
        RadioStation(name = "FunX Arab", url = "https://icecast.omroep.nl/funx-arab-bb-mp3", emoji = "🕌", category = "🇳🇱 Nederland", colorValue = 0xFFF59E0BL),
        RadioStation(name = "BBC World Service", url = "https://stream.live.vc.bbcmedia.co.uk/bbc_world_service", emoji = "🌍", category = "🇬🇧 International", colorValue = 0xFF991B1BL)
    )

    val allStations = defaultStations + customStations
    val categories = allStations.groupBy { it.category }.map { (title, stations) ->
        RadioCategory(title, stations)
    }.sortedBy { it.title }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp)) {
            ScreenHeader(title = "Radio", onBack = onBack)
            
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                categories.forEach { category ->
                    stickyHeader {
                        Surface(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.background) {
                            Text(category.title, fontSize = 22.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    items(category.stations) { station ->
                        val active = currentStation?.url == station.url
                        Card(
                            modifier = Modifier.fillMaxWidth().height(85.dp).clickable { radioVm.playStation(station) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = if (active) Color(station.colorValue) else MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(station.emoji, fontSize = 28.sp)
                                Spacer(Modifier.width(16.dp))
                                Text(station.name, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = if (active) Color.White else MaterialTheme.colorScheme.onSurface)
                                Spacer(Modifier.weight(1f))
                                if (station.isCustom) {
                                    IconButton(onClick = { scope.launch { dao.delete(station) } }) {
                                        Icon(Icons.Default.Delete, null, tint = if (active) Color.White else Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- VASTE PLAYER ONDERAAN ---
        if (currentStation != null || isPlaying || isLoading) {
            Card(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(12.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(if (hasError) "⚠️" else currentStation?.emoji ?: "📻", fontSize = 32.sp)
                        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(48.dp))
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(if (hasError) "Fout!" else if (isLoading) "Laden..." else "Nu bezig:", fontSize = 12.sp)
                        Text(currentStation?.name ?: "Zender", fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                    IconButton(onClick = { if (isPlaying) radioVm.pause() else radioVm.resume() }, modifier = Modifier.size(56.dp)) {
                        Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, null, modifier = Modifier.size(40.dp))
                    }
                    IconButton(onClick = { radioVm.playStation(currentStation!!) }, modifier = Modifier.size(56.dp)) {
                        Icon(Icons.Default.Stop, null, modifier = Modifier.size(32.dp))
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = if (currentStation != null) 110.dp else 24.dp, end = 24.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.Add, null)
        }
    }

    if (showAddDialog) {
        AddRadioDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, url, emoji, cat ->
                scope.launch {
                    dao.insert(RadioStation(name = name, url = url, emoji = emoji, category = cat, colorValue = 0xFF3B82F6L, isCustom = true))
                    showAddDialog = false
                }
            }
        )
    }
}

data class RadioSearchResult(val name: String, val url: String, val country: String)

@Composable
fun AddRadioDialog(onDismiss: () -> Unit, onSave: (String, String, String, String) -> Unit) {
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("📻") }
    var category by remember { mutableStateOf("⭐ Mijn Zenders") }
    
    var searchResults by remember { mutableStateOf<List<RadioSearchResult>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(Modifier.fillMaxWidth().fillMaxHeight(0.8f).padding(16.dp), shape = RoundedCornerShape(24.dp)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Zender Zoeken", fontSize = 24.sp, fontWeight = FontWeight.Black)
                
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name, 
                        onValueChange = { name = it }, 
                        label = { Text("Typ naam van zender...") }, 
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Button(
                        onClick = {
                            if (name.length > 2) {
                                isSearching = true
                                scope.launch {
                                    try {
                                        val results = withContext(Dispatchers.IO) {
                                            // Gebruik radio-browser API om zenders te zoeken
                                            val response = URL("https://de1.api.radio-browser.info/json/stations/byname/${name.trim().replace(" ", "%20")}").readText()
                                            val json = JSONArray(response)
                                            val list = mutableListOf<RadioSearchResult>()
                                            for (i in 0 until minOf(json.length(), 20)) {
                                                val obj = json.getJSONObject(i)
                                                list.add(RadioSearchResult(
                                                    obj.getString("name").trim(),
                                                    obj.getString("url_resolved"),
                                                    obj.optString("country", "Onbekend")
                                                ))
                                            }
                                            list
                                        }
                                        searchResults = results
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    } finally {
                                        isSearching = false
                                    }
                                }
                            }
                        },
                        modifier = Modifier.size(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        if (isSearching) CircularProgressIndicator(Modifier.size(24.dp), color = Color.White)
                        else Icon(Icons.Default.Search, null, modifier = Modifier.size(30.dp))
                    }
                }

                if (searchResults.isNotEmpty()) {
                    Text("Resultaten (klik om te kiezen):", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    LazyColumn(Modifier.weight(1f).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))) {
                        items(searchResults) { res ->
                            Column(Modifier.fillMaxWidth().clickable { 
                                name = res.name
                                url = res.url
                                searchResults = emptyList() // Sluit resultaten lijst
                            }.padding(16.dp)) {
                                Text(res.name, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                                Text(res.country, fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
                            }
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                        }
                    }
                } else if (!isSearching) {
                    // Als er geen resultaten zijn (of we hebben er al een gekozen), toon de rest van de velden
                    if (url.isNotBlank()) {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(8.dp))
                                Text("Link gevonden!", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    
                    OutlinedTextField(value = emoji, onValueChange = { emoji = it }, label = { Text("Emoji (icoon)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    
                    Text("Categorie:", fontWeight = FontWeight.Bold)
                    val cats = listOf("⭐ Mijn Zenders", "🇳🇱 Nederland", "🇧🇪 België")
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        cats.forEach { c ->
                            FilterChip(selected = category == c, onClick = { category = c }, label = { Text(c) })
                        }
                    }
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onDismiss) { Text("Annuleren", fontSize = 18.sp) }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { if (name.isNotBlank() && url.isNotBlank()) onSave(name, url, emoji, category) },
                        enabled = name.isNotBlank() && url.isNotBlank(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Opslaan", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
