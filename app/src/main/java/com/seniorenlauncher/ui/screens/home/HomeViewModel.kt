package com.seniorenlauncher.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seniorenlauncher.LauncherApp
import com.seniorenlauncher.data.model.AppSettings
import com.seniorenlauncher.data.model.WeatherData
import com.seniorenlauncher.data.model.ALL_APPS
import com.seniorenlauncher.ui.screens.HomeApp
import com.seniorenlauncher.util.AppLauncher
import kotlinx.coroutines.flow.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.ui.graphics.Color

data class HomeState(
    val settings: AppSettings = AppSettings(),
    val allVisibleApps: List<HomeApp> = emptyList(),
    val weatherData: WeatherData? = null,
    val pendingMedsCount: Int = 0
)

class HomeViewModel : ViewModel() {
    private val repository = LauncherApp.instance.settingsRepository
    private val medicationDao = LauncherApp.instance.database.medicationDao()

    private val _weatherData = MutableStateFlow<WeatherData?>(null)
    
    val state: StateFlow<HomeState> = combine(
        repository.settingsFlow,
        _weatherData,
        medicationDao.getPending()
    ) { settings, weather, pendingMeds ->
        HomeState(
            settings = settings,
            weatherData = weather,
            pendingMedsCount = pendingMeds.size,
            allVisibleApps = calculateVisibleApps(settings, weather)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeState())

    private fun calculateVisibleApps(settings: AppSettings, weatherData: WeatherData?): List<HomeApp> {
        val context = LauncherApp.instance.applicationContext
        
        val mappedApps = settings.appMappings.keys
            .filter { it.startsWith("mapped_") && it in settings.visibleApps }
            .map { id ->
                val pkg = settings.appMappings[id] ?: ""
                val info = AppLauncher.getAppInfo(context, pkg)
                HomeApp(
                    id = id,
                    name = info?.name ?: "App",
                    emoji = null,
                    icon = info?.icon,
                    vectorIcon = null,
                    color = Color(0xFF718096)
                )
            }

        val visibleStandardApps = ALL_APPS.filter { it.id in settings.visibleApps || it.id == "settings" || it.id == "wifi" || it.id == "bluetooth" }.map { 
            HomeApp(
                id = it.id,
                name = it.name,
                emoji = if (it.id == "weather" && weatherData != null) getHomeWeatherEmoji(weatherData.iconUrl) else it.emoji,
                icon = null,
                vectorIcon = if (it.id == "bluetooth") Icons.Default.Bluetooth else null,
                color = Color(it.color),
                weatherOverlay = if (it.id == "weather" && weatherData != null) "${weatherData.temp.toInt()}°" else null
            )
        }
        return (visibleStandardApps + mappedApps).distinctBy { it.id }
    }

    fun setWeatherData(data: WeatherData?) {
        _weatherData.value = data
    }

    private fun getHomeWeatherEmoji(iconUrl: String): String {
        return when {
            iconUrl.contains("01d") -> "☀️"
            iconUrl.contains("01n") -> "🌙"
            iconUrl.contains("02d") || iconUrl.contains("02n") -> "⛅"
            iconUrl.contains("03") || iconUrl.contains("04") -> "☁️"
            iconUrl.contains("09") || iconUrl.contains("10") -> "🌧️"
            iconUrl.contains("11") -> "⛈️"
            iconUrl.contains("13") -> "❄️"
            iconUrl.contains("50") -> "🌫️"
            else -> "☁️"
        }
    }
}
