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

data class RadioSearchResult(val name: String, val url: String, val country: String)

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
        if (currentStation != null) {
            Card(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(12.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(if (hasError) "⚠️" else currentStation?.emoji ?: "📻", fontSize = 32.sp)
                            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(48.dp))
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(if (hasError) "Fout!" else if (isLoading) "Laden..." else "Nu bezig:", fontSize = 12.sp)
                            Text(currentStation?.name ?: "Zender", fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                        // Grote Volume Knoppen onderaan voor Senior
                        Row {
                            IconButton(onClick = { radioVm.volumeDown() }, modifier = Modifier.size(48.dp)) {
                                Icon(Icons.Default.Remove, null, modifier = Modifier.size(32.dp))
                            }
                            IconButton(onClick = { radioVm.volumeUp() }, modifier = Modifier.size(48.dp)) {
                                Icon(Icons.Default.Add, null, modifier = Modifier.size(32.dp))
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { if (isPlaying) radioVm.pause() else radioVm.resume() },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, null)
                            Spacer(Modifier.width(8.dp))
                            Text(if (isPlaying) "Pauze" else "Speel")
                        }
                        Button(
                            onClick = { radioVm.stop() },
                            modifier = Modifier.weight(0.6f).height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Stop, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Stop")
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = if (currentStation != null) 160.dp else 24.dp, end = 24.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.Add, null, modifier = Modifier.size(32.dp))
        }
    }

    if (showAddDialog) {
        AddRadioDialog(
            radioVm = radioVm,
            dao = dao,
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

@Composable
fun AddRadioDialog(
    radioVm: RadioViewModel,
    dao: com.seniorenlauncher.data.db.RadioDao,
    onDismiss: () -> Unit, 
    onSave: (String, String, String, String) -> Unit
) {
    val scope = rememberCoroutineScope()
    var input by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("📻") }
    
    var searchResults by remember { mutableStateOf<List<RadioSearchResult>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    val isUrl = input.startsWith("http")

    Dialog(onDismissRequest = onDismiss) {
        Card(Modifier.fillMaxWidth().fillMaxHeight(0.85f).padding(16.dp), shape = RoundedCornerShape(24.dp)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Zender Toevoegen", fontSize = 24.sp, fontWeight = FontWeight.Black)
                
                OutlinedTextField(
                    value = input, 
                    onValueChange = { input = it }, 
                    label = { Text(if (isUrl) "Link gedetecteerd!" else "Typ naam of plak link...") }, 
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (isUrl) {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Gevonden: Directe Stream", fontWeight = FontWeight.Bold)
                            Text("Geef deze zender een naam:", fontSize = 14.sp)
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(value = name, onValueChange = { name = it }, placeholder = { Text("Bijv. Radio Extra") }, modifier = Modifier.fillMaxWidth())
                        }
                    }
                    url = input
                } else if (input.length > 2) {
                    Button(
                        onClick = {
                            isSearching = true
                            scope.launch {
                                try {
                                    val response = withContext(Dispatchers.IO) {
                                        URL("https://de1.api.radio-browser.info/json/stations/byname/${input.trim().replace(" ", "%20")}").readText()
                                    }
                                    val json = JSONArray(response)
                                    val list = mutableListOf<RadioSearchResult>()
                                    for (i in 0 until minOf(json.length(), 10)) {
                                        val obj = json.getJSONObject(i)
                                        list.add(RadioSearchResult(obj.getString("name"), obj.getString("url_resolved"), obj.optString("country", "")))
                                    }
                                    searchResults = list
                                } catch (e: Exception) { } finally { isSearching = false }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        if (isSearching) CircularProgressIndicator(color = Color.White)
                        else Text("🔍 Zoek op internet")
                    }

                    LazyColumn(Modifier.weight(1f)) {
                        items(searchResults) { res ->
                            Card(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        val newStation = RadioStation(
                                            name = res.name,
                                            url = res.url,
                                            emoji = "📻",
                                            category = "⭐ Mijn Zenders",
                                            colorValue = 0xFF3B82F6L,
                                            isCustom = true
                                        )
                                        scope.launch {
                                            dao.insert(newStation)
                                            radioVm.playStation(newStation)
                                            onDismiss()
                                        }
                                    },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(res.name, fontWeight = FontWeight.Black, fontSize = 20.sp)
                                        Text(res.country, fontSize = 16.sp)
                                    }
                                    Icon(Icons.Default.AddCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                                }
                            }
                        }
                    }
                }

                if (isUrl && name.isNotBlank() && url.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { onSave(name, url, emoji, "⭐ Mijn Zenders") },
                        modifier = Modifier.fillMaxWidth().height(70.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Opslaan & Spelen", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }

                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text("Annuleren", fontSize = 18.sp)
                }
            }
        }
    }
}
