package com.seniorenlauncher.ui.screens

import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.seniorenlauncher.LauncherApp
import com.seniorenlauncher.data.model.*
import com.seniorenlauncher.service.NotificationListener
import com.seniorenlauncher.service.SOSService
import com.seniorenlauncher.ui.components.*
import com.seniorenlauncher.ui.screens.home.*
import com.seniorenlauncher.ui.theme.SeniorenLauncherTheme
import com.seniorenlauncher.util.AppLauncher

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
    val settings by settingsVm.settings.collectAsState()
    val activeNotifications by NotificationListener.activeNotificationsFlow.collectAsState()
    val badgeCounts by NotificationListener.notifications.collectAsState()
    
    val weatherVm: WeatherViewModel = viewModel()
    val weatherData by weatherVm.currentWeather.collectAsState()
    
    val dao = LauncherApp.instance.database.medicationDao()
    val pendingMeds by dao.getPending().collectAsState(initial = emptyList())

    var showAppPickerFor by remember { mutableStateOf<String?>(null) }
    var showPinDialogForSettings by remember { mutableStateOf(false) }

    HomeScreenContent(
        settings = settings,
        activeNotificationsCount = activeNotifications.size,
        badgeCounts = badgeCounts,
        weatherData = weatherData,
        pendingMedsCount = pendingMeds.size,
        onNavigate = onNavigate,
        onAddApp = { showAppPickerFor = "new" },
        onAppLongClick = { id -> showAppPickerFor = id },
        onSettingsClick = { showPinDialogForSettings = true }
    )

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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreenContent(
    settings: AppSettings,
    activeNotificationsCount: Int,
    badgeCounts: Map<String, Int>,
    weatherData: WeatherData?,
    pendingMedsCount: Int,
    onNavigate: (String) -> Unit,
    onAddApp: () -> Unit,
    onAppLongClick: (String) -> Unit,
    onSettingsClick: () -> Unit
) {
    val context = LocalContext.current
    
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
            emoji = if (it.id == "weather" && weatherData != null) getHomeWeatherEmoji(weatherData.iconUrl) else it.emoji,
            icon = null,
            color = Color(it.color),
            weatherOverlay = if (it.id == "weather" && weatherData != null) "${weatherData.temp.toInt()}°" else null
        )
    }
    val allVisibleApps = (visibleStandardApps + mappedApps).distinctBy { it.id }
    
    // Bereken het totaal aantal items inclusief de "Toevoegen" knop
    val totalItemsCount = allVisibleApps.size + 1
    val pageCount = Math.max(1, Math.ceil(totalItemsCount.toDouble() / appsPerPage.toDouble()).toInt())
    val pagerState = rememberPagerState(pageCount = { pageCount })

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Top Bar: Clock, Battery & Notifications
        HomeTopBar(
            notificationCount = activeNotificationsCount,
            onNotificationsClick = { onNavigate("notifications") }
        )

        // Status Card: Alleen nog voor Medicijnen
        if (pendingMedsCount > 0) {
            HomeStatusCard(
                pendingMedsCount = pendingMedsCount,
                onMedsClick = { onNavigate("meds") }
            )
        }

        // App Grid Pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            pageSpacing = 16.dp
        ) { pageIndex ->
            val startIdx = pageIndex * appsPerPage
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(cols),
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                userScrollEnabled = false
            ) {
                // Apps voor deze pagina
                val endIdx = Math.min(startIdx + appsPerPage, allVisibleApps.size)
                if (startIdx < allVisibleApps.size) {
                    val pageApps = allVisibleApps.subList(startIdx, endIdx)
                    items(pageApps) { app ->
                        BigButton(
                            emoji = app.emoji,
                            icon = app.icon,
                            label = app.name,
                            color = app.color,
                            small = settings.layout == LayoutType.GRID_3x4,
                            badge = if (app.id == "sms" || app.id == "phone") {
                               NotificationListener.getBadgeCountByAppId(app.id, settings.appMappings, badgeCounts)
                            } else 0,
                            weatherText = app.weatherOverlay,
                            onClick = {
                                when {
                                    app.id == "camera" -> AppLauncher.openSystemCamera(context)
                                    app.id == "wifi" -> AppLauncher.openWifiSettings(context)
                                    app.id == "remote_support" -> onNavigate("remote_support")
                                    app.id.startsWith("mapped_") -> {
                                        val pkg = settings.appMappings[app.id]
                                        if (pkg != null) AppLauncher.launchApp(context, pkg)
                                    }
                                    else -> onNavigate(app.id)
                                }
                            },
                            onLongClick = {
                                onAppLongClick(app.id)
                            }
                        )
                    }
                }
                
                // Voeg de "Toevoegen" knop toe als we op de juiste pagina zijn
                val addBtnIdx = allVisibleApps.size
                val start = startIdx
                val limit = startIdx + appsPerPage
                
                if (addBtnIdx in start until limit) {
                    item {
                        BigButton(
                            emoji = "➕",
                            label = "Toevoegen",
                            color = Color(0xFF718096),
                            small = settings.layout == LayoutType.GRID_3x4,
                            onClick = onAddApp
                        )
                    }
                }
            }
        }

        // Fixed SOS Button & Settings Button at bottom
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HomeSOSButton(
                onClick = {
                    val intent = Intent(context, SOSService::class.java)
                    context.startForegroundService(intent)
                }
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                FilledIconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier.size(64.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Icon(Icons.Default.Settings, "Instellingen", modifier = Modifier.size(32.dp))
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun PreviewHomeScreen() {
    SeniorenLauncherTheme {
        HomeScreenContent(
            settings = AppSettings(
                layout = LayoutType.GRID_2x3,
                visibleApps = setOf("phone", "sms", "camera", "photos", "meds", "weather", "wifi")
            ),
            activeNotificationsCount = 2,
            badgeCounts = mapOf("sms" to 1),
            weatherData = WeatherData(
                temp = 21.5,
                condition = "Partly Cloudy",
                iconUrl = "02d",
                humidity = 40,
                windSpeed = 10.0
            ),
            pendingMedsCount = 1,
            onNavigate = {},
            onAddApp = {},
            onAppLongClick = {},
            onSettingsClick = {}
        )
    }
}
