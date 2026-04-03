package com.seniorenlauncher.ui.screens

import android.graphics.drawable.Drawable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.seniorenlauncher.LauncherApp
import com.seniorenlauncher.data.model.*
import com.seniorenlauncher.ui.components.*
import com.seniorenlauncher.util.AppLauncher
import com.seniorenlauncher.service.NotificationListener
import java.text.SimpleDateFormat
import java.util.*

data class HomeApp(
    val id: String, 
    val name: String, 
    val emoji: String? = null, 
    val icon: Drawable? = null,
    val color: Color,
    val weatherOverlay: String? = null 
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(onNavigate: (String) -> Unit, settingsVm: SettingsViewModel, radioVm: RadioViewModel) {
    val context = LocalContext.current
    val settings by settingsVm.settings.collectAsState()
    val notifications by NotificationListener.notifications.collectAsState()
    
    val currentStation by radioVm.currentStation.collectAsState()
    val isPlaying by radioVm.isPlaying.collectAsState()
    
    val weatherVm: WeatherViewModel = viewModel()
    val weatherData by weatherVm.currentWeather.collectAsState()
    
    val dao = LauncherApp.instance.database.medicationDao()
    val pendingMeds by dao.getPending().collectAsState(initial = emptyList())
    
    val fontSizeMultiplier = settings.fontSize / 16f

    // --- Dynamic Layout Logic ---
    val cols = when (settings.layout) {
        LayoutType.GRID_1x1 -> 1
        LayoutType.GRID_2x3 -> 2
        LayoutType.GRID_3x4 -> 3
    }
    
    val appsPerPage = when (settings.layout) {
        LayoutType.GRID_1x1 -> 1
        LayoutType.GRID_2x3 -> 6
        LayoutType.GRID_3x4 -> 12
    }

    // Filter visible apps and include dynamically mapped apps with correct icons
    val mappedApps = settings.appMappings.keys
        .filter { it.startsWith("mapped_") && it in settings.visibleApps }
        .map { id ->
            val pkg = settings.appMappings[id] ?: ""
            val info = AppLauncher.getAppInfo(context, pkg)
            HomeApp(id, info?.name ?: "App", null, info?.icon, Color(0xFF718096))
        }

    val visibleStandardApps = ALL_APPS.filter { it.id in settings.visibleApps }.map { 
        HomeApp(
            id = it.id,
            name = it.name,
            emoji = if (it.id == "weather" && weatherData != null) getWeatherEmoji(weatherData!!.iconUrl) else it.emoji,
            icon = null,
            color = Color(it.color),
            weatherOverlay = if (it.id == "weather" && weatherData != null) "${weatherData!!.temp.toInt()}°" else null
        )
    }
    val allVisibleApps = (visibleStandardApps + mappedApps).distinctBy { it.id }
    
    val pageCount = Math.max(1, Math.ceil(allVisibleApps.size.toDouble() / appsPerPage.toDouble()).toInt())
    val pagerState = rememberPagerState(pageCount = { pageCount })

    var showAppPickerFor by remember { mutableStateOf<String?>(null) }
    var showPinDialogForSettings by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Top Bar: Clock & Notifications
        HomeTopBar(fontSizeMultiplier)

        // Status Card: Radio & Meds
        HomeStatusCard(
            currentStation = currentStation?.name,
            isPlaying = isPlaying,
            pendingMedsCount = pendingMeds.size,
            fontSizeMultiplier = fontSizeMultiplier,
            onRadioClick = { onNavigate("radio") },
            onMedsClick = { onNavigate("meds") },
            onPlayPause = { if (isPlaying) radioVm.pause() else radioVm.resume() },
            onStop = { radioVm.stop() }
        )

        // App Grid Pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            pageSpacing = 16.dp
        ) { pageIndex ->
            val startIdx = pageIndex * appsPerPage
            val endIdx = Math.min(startIdx + appsPerPage, allVisibleApps.size)
            val pageApps = allVisibleApps.subList(startIdx, endIdx)

            LazyVerticalGrid(
                columns = GridCells.Fixed(cols),
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                userScrollEnabled = false
            ) {
                items(pageApps) { app ->
                    BigButton(
                        emoji = app.emoji,
                        icon = app.icon,
                        label = app.name,
                        color = app.color,
                        small = settings.layout == LayoutType.GRID_3x4,
                        fontSizeMultiplier = fontSizeMultiplier,
                        badge = if (app.id == "sms") notifications.size else 0,
                        weatherText = app.weatherOverlay,
                        onClick = {
                            when {
                                app.id == "camera" -> AppLauncher.openSystemCamera(context)
                                app.id == "remote_support" -> onNavigate("remote_support")
                                app.id.startsWith("mapped_") -> {
                                    val pkg = settings.appMappings[app.id]
                                    if (pkg != null) AppLauncher.launchApp(context, pkg)
                                }
                                else -> onNavigate(app.id)
                            }
                        },
                        onLongClick = {
                            showAppPickerFor = app.id
                        }
                    )
                }
                
                // Add "New App" button if there's space on the last page or on a new page
                if (pageIndex == pageCount - 1) {
                    item {
                        BigButton(
                            emoji = "➕",
                            label = "Toevoegen",
                            color = Color(0xFF718096),
                            small = settings.layout == LayoutType.GRID_3x4,
                            fontSizeMultiplier = fontSizeMultiplier,
                            onClick = { showAppPickerFor = "new" }
                        )
                    }
                }
            }
        }

        // Settings Button at bottom
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
            FilledIconButton(
                onClick = { showPinDialogForSettings = true },
                modifier = Modifier.size(64.dp),
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Icon(Icons.Default.Settings, "Instellingen", modifier = Modifier.size(32.dp))
            }
        }
    }

    if (showAppPickerFor != null) {
        val pickerAppId = showAppPickerFor!!
        AppPickerDialog(
            appId = pickerAppId,
            onDismiss = { showAppPickerFor = null },
            onAppsSelected = { pkgs: List<String> ->
                val newMappings = mutableMapOf<String, String>()
                pkgs.forEach { pkg ->
                    val newId = "mapped_${System.currentTimeMillis()}_${pkg.hashCode()}"
                    newMappings[newId] = pkg
                }
                settingsVm.addAppMappingsBulk(newMappings)
                showAppPickerFor = null
            },
            onRemove = {
                settingsVm.updateVisibleApps(settings.visibleApps - pickerAppId)
                showAppPickerFor = null
            }
        )
    }
    
    if (showPinDialogForSettings) {
        PinDialog(
            correctPin = settings.pinCode ?: "1234",
            onDismiss = { showPinDialogForSettings = false },
            onSuccess = { 
                showPinDialogForSettings = false
                onNavigate("settings")
            }
        )
    }
}

@Composable
fun PinDialog(correctPin: String, onDismiss: () -> Unit, onSuccess: () -> Unit) {
    var input by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Beveiligde Instellingen", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Voer de pincode in om verder te gaan.", textAlign = TextAlign.Center)
                
                Spacer(Modifier.height(24.dp))
                
                Text(
                    input.replace(Regex("."), "●").ifEmpty { " " },
                    fontSize = 32.sp,
                    letterSpacing = 8.sp,
                    color = if (error) Color.Red else MaterialTheme.colorScheme.primary
                )
                
                Spacer(Modifier.height(24.dp))
                
                // Numpad
                val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "C", "0", "OK")
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.height(280.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(keys) { key ->
                        Button(
                            onClick = {
                                when (key) {
                                    "C" -> if (input.isNotEmpty()) input = input.dropLast(1)
                                    "OK" -> {
                                        if (input == correctPin) onSuccess() else {
                                            error = true
                                            input = ""
                                        }
                                    }
                                    else -> {
                                        if (input.length < 8) {
                                            error = false
                                            input += key
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.aspectRatio(1f),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                        ) {
                            Text(key, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppPickerDialog(
    appId: String,
    onDismiss: () -> Unit,
    onAppsSelected: (List<String>) -> Unit,
    onRemove: () -> Unit
) {
    val context = LocalContext.current
    val installedApps = remember { AppLauncher.getInstalledApps(context, includeIcons = true) }
    var searchQuery by remember { mutableStateOf("") }
    val selectedPackages = remember { mutableStateListOf<String>() }

    val filteredApps = remember(searchQuery, installedApps) {
        if (searchQuery.isEmpty()) installedApps
        else installedApps.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f).padding(16.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Kies Apps", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    if (appId.startsWith("mapped_")) {
                        TextButton(onClick = onRemove) {
                            Text("Verwijderen", color = Color.Red)
                        }
                    }
                }
                
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Zoek app...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(Modifier.height(12.dp))
                
                LazyColumn(Modifier.weight(1f)) {
                    items(filteredApps) { app ->
                        val isSelected = selectedPackages.contains(app.packageName)
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    if (isSelected) selectedPackages.remove(app.packageName)
                                    else selectedPackages.add(app.packageName)
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = null // Handled by row click
                            )
                            Spacer(Modifier.width(8.dp))
                            if (app.icon != null) {
                                AsyncImage(
                                    model = app.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp))
                                )
                            }
                            Spacer(Modifier.width(16.dp))
                            Text(app.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                }
                
                Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onDismiss, 
                        modifier = Modifier.weight(1f).height(60.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("ANNULEREN")
                    }
                    Button(
                        onClick = { onAppsSelected(selectedPackages.toList()) }, 
                        modifier = Modifier.weight(1f).height(60.dp),
                        enabled = selectedPackages.isNotEmpty(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("VOEG TOE (${selectedPackages.size})")
                    }
                }
            }
        }
    }
}

@Composable
fun HomeTopBar(fontSizeMultiplier: Float) {
    var currentTime by remember { mutableStateOf(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())) }
    var currentDate by remember { mutableStateOf(SimpleDateFormat("EEEE d MMMM", Locale.getDefault()).format(Date())) }
    
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            currentDate = SimpleDateFormat("EEEE d MMMM", Locale.getDefault()).format(Date())
            kotlinx.coroutines.delay(1000)
        }
    }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            currentTime, 
            fontSize = (64 * fontSizeMultiplier).sp, 
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            currentDate.replaceFirstChar { it.uppercase() }, 
            fontSize = (20 * fontSizeMultiplier).sp, 
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun HomeStatusCard(
    currentStation: String?,
    isPlaying: Boolean,
    pendingMedsCount: Int,
    fontSizeMultiplier: Float,
    onRadioClick: () -> Unit,
    onMedsClick: () -> Unit,
    onPlayPause: () -> Unit,
    onStop: () -> Unit
) {
    if (currentStation == null && pendingMedsCount == 0) return

    Card(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(Modifier.padding(16.dp)) {
            if (currentStation != null) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        Modifier.weight(1f).clickable { onRadioClick() },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📻", fontSize = 24.sp)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Nu op de radio:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(currentStation, fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    
                    Row {
                        IconButton(onClick = onPlayPause) {
                            Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, null)
                        }
                        IconButton(onClick = onStop) {
                            Icon(Icons.Default.Stop, null)
                        }
                    }
                }
            }
            
            if (currentStation != null && pendingMedsCount > 0) {
                HorizontalDivider(Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
            }
            
            if (pendingMedsCount > 0) {
                Row(
                    Modifier.fillMaxWidth().clickable { onMedsClick() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("💊", fontSize = 24.sp)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Je hebt nog $pendingMedsCount medicijnen in te nemen", 
                        fontSize = 16.sp, 
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
