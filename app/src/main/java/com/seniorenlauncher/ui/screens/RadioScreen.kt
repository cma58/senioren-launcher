package com.seniorenlauncher.ui.screens

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

    Box(Modifier.fillMaxSize().background(Color(0xFF0F172A))) {
        Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
            ScreenHeader(title = "Radio", onBack = onBack)
            
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                categories.forEach { category ->
                    stickyHeader {
                        Surface(Modifier.fillMaxWidth(), color = Color(0xFF0F172A)) {
                            Text(
                                text = category.title.uppercase(), 
                                fontSize = 14.sp, 
                                fontWeight = FontWeight.Black, 
                                modifier = Modifier.padding(top = 20.dp, bottom = 8.dp), 
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 2.sp
                            )
                        }
                    }

                    items(category.stations) { station ->
                        val active = currentStation?.url == station.url
                        Surface(
                            onClick = { radioVm.playStation(station) },
                            modifier = Modifier.fillMaxWidth().height(90.dp),
                            shape = RoundedCornerShape(40.dp),
                            color = if (active) Color(station.colorValue) else Color(0xFF1E293B).copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                            shadowElevation = if (active) 8.dp else 0.dp
                        ) {
                            Row(Modifier.fillMaxSize().padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(56.dp).background(Color.White.copy(alpha = 0.1f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(station.emoji, fontSize = 28.sp)
                                }
                                Spacer(Modifier.width(20.dp))
                                Text(
                                    station.name, 
                                    fontSize = 22.sp, 
                                    fontWeight = FontWeight.Black, 
                                    color = if (active) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.weight(1f))
                                if (station.isCustom) {
                                    IconButton(onClick = { scope.launch { dao.delete(station) } }) {
                                        Icon(Icons.Default.Delete, null, tint = if (active) Color.White.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.2f))
                                    }
                                }
                                if (active && isPlaying) {
                                    Icon(Icons.Default.VolumeUp, null, tint = Color.White, modifier = Modifier.size(24.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- ANDROID 17 FLOATING PLAYER ---
        if (currentStation != null) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                shadowElevation = 16.dp,
                shape = RoundedCornerShape(40.dp),
                color = Color(0xFF1E293B), // Slate 800
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(64.dp),
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.05f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(40.dp), strokeWidth = 3.dp, color = MaterialTheme.colorScheme.primary)
                                } else {
                                    Text(if (hasError) "⚠️" else currentStation?.emoji ?: "📻", fontSize = 32.sp)
                                }
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = if (hasError) "FOUT BIJ LADEN" else if (isLoading) "VERBINDEN..." else "NU AAN HET SPELEN", 
                                fontSize = 11.sp, 
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = currentStation?.name ?: "Zender", 
                                fontSize = 22.sp, 
                                fontWeight = FontWeight.Black, 
                                color = Color.White,
                                maxLines = 1
                            )
                        }
                        
                        // Volume "Orbs"
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                onClick = { radioVm.volumeDown() },
                                modifier = Modifier.size(50.dp),
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.1f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Remove, null, tint = Color.White)
                                }
                            }
                            Surface(
                                onClick = { radioVm.volumeUp() },
                                modifier = Modifier.size(50.dp),
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.1f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Add, null, tint = Color.White)
                                }
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(20.dp))
                    
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Play/Pause Orb
                        Surface(
                            onClick = { if (isPlaying) radioVm.pause() else radioVm.resume() },
                            modifier = Modifier.weight(1.5f).height(70.dp),
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.primary,
                            shadowElevation = 8.dp
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(32.dp))
                                Spacer(Modifier.width(12.dp))
                                Text(if (isPlaying) "PAUZE" else "SPEEL", fontWeight = FontWeight.Black, color = Color.White, fontSize = 18.sp)
                            }
                        }
                        
                        // Stop Orb
                        Surface(
                            onClick = { radioVm.stop() },
                            modifier = Modifier.weight(1f).height(70.dp),
                            shape = RoundedCornerShape(24.dp),
                            color = Color(0xFFEF4444).copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f))
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                Icon(Icons.Default.Stop, null, tint = Color(0xFFEF4444), modifier = Modifier.size(28.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("STOP", fontWeight = FontWeight.Black, color = Color(0xFFEF4444), fontSize = 18.sp)
                            }
                        }
                    }
                }
            }
        }

        // Luminous Add FAB
        Surface(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = if (currentStation != null) 210.dp else 24.dp, end = 24.dp)
                .size(76.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondary,
            shadowElevation = 12.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(36.dp), tint = Color.White)
            }
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
        Surface(
            Modifier.fillMaxWidth().fillMaxHeight(0.85f),
            shape = RoundedCornerShape(40.dp),
            color = Color(0xFF1E293B), // Slate 800
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Zender Toevoegen", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
                
                OutlinedTextField(
                    value = input, 
                    onValueChange = { input = it }, 
                    label = { Text(if (isUrl) "Link gedetecteerd!" else "Typ naam of plak link...") }, 
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                if (isUrl) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            Text("Gevonden: Directe Stream", fontWeight = FontWeight.Black, color = Color.White)
                            Text("Geef deze zender een naam:", fontSize = 14.sp, color = Color.White.copy(alpha = 0.6f))
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                value = name, 
                                onValueChange = { name = it }, 
                                placeholder = { Text("Bijv. Radio Extra") }, 
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                    url = input
                } else if (input.length > 2) {
                    Surface(
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
                        modifier = Modifier.fillMaxWidth().height(70.dp),
                        shape = RoundedCornerShape(40.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (isSearching) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(28.dp))
                            else Text("ZOEK OP INTERNET", fontWeight = FontWeight.Black, color = Color.White)
                        }
                    }

                    LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(searchResults) { res ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
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
                                shape = RoundedCornerShape(20.dp),
                                color = Color.White.copy(alpha = 0.05f)
                            ) {
                                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(res.name, fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color.White)
                                        Text(res.country, fontSize = 16.sp, color = Color.White.copy(alpha = 0.6f))
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
                        modifier = Modifier.fillMaxWidth().height(76.dp),
                        shape = RoundedCornerShape(40.dp)
                    ) {
                        Text("OPSLAAN & SPELEN", fontSize = 20.sp, fontWeight = FontWeight.Black)
                    }
                }

                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.CenterHorizontally).height(70.dp)) {
                    Text("ANNULEREN", fontSize = 18.sp, color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
