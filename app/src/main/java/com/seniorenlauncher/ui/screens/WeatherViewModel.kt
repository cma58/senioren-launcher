package com.seniorenlauncher.ui.screens
import com.seniorenlauncher.R
import com.seniorenlauncher.util.i18n

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.seniorenlauncher.LauncherApp
import com.seniorenlauncher.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.util.*

class WeatherViewModel : ViewModel() {
    private val dao = LauncherApp.instance.database.weatherDao()
    
    private val _currentWeather = MutableStateFlow<WeatherData?>(null)
    val currentWeather: StateFlow<WeatherData?> = _currentWeather.asStateFlow()

    private val _selectedLocation = MutableStateFlow<WeatherLocation?>(null)
    val selectedLocation: StateFlow<WeatherLocation?> = _selectedLocation.asStateFlow()

    private val _safetyStatus = MutableStateFlow(SafetyStatus.GREEN)
    val safetyStatus: StateFlow<SafetyStatus> = _safetyStatus.asStateFlow()

    private val _dayParts = MutableStateFlow<List<DayPartForecast>>(emptyList())
    val dayParts: StateFlow<List<DayPartForecast>> = _dayParts.asStateFlow()

    val savedLocations: StateFlow<List<WeatherLocation>> = dao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var lastRefreshTime = 0L
    private val REFRESH_INTERVAL = 15 * 60 * 1000 // 15 minuten cache

    init {
        refreshWeather()
    }

    @SuppressLint("MissingPermission")
    fun refreshWeather(force: Boolean = false) {
        val now = System.currentTimeMillis()
        if (!force && _currentWeather.value != null && (now - lastRefreshTime) < REFRESH_INTERVAL) {
            return // Gebruik gecachte data als het nog recent is
        }

        val context = LauncherApp.instance.applicationContext
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        
        viewModelScope.launch {
            try {
                val loc = _selectedLocation.value
                if (loc == null || loc.isCurrentLocation) {
                    // Probeer eerst de laatste bekende locatie (is direct beschikbaar)
                    var location = try { fusedLocationClient.lastLocation.await() } catch(e: Exception) { null }
                    
                    // Als die er niet is, haal dan een nieuwe op (kan even duren)
                    if (location == null) {
                        location = try { 
                            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null).await() 
                        } catch(e: Exception) { null }
                    }
                    
                    if (location != null) {
                        val newLoc = WeatherLocation(cityName = i18n(R.string.mijn_locatie), latitude = location.latitude, longitude = location.longitude, isCurrentLocation = true)
                        _selectedLocation.value = newLoc
                        fetchWeather(location.latitude, location.longitude)
                    } else {
                        fetchWeather(52.3676, 4.9041) // Fallback: Amsterdam
                    }
                } else {
                    fetchWeather(loc.latitude, loc.longitude)
                }
                lastRefreshTime = System.currentTimeMillis()
            } catch (e: Exception) {
                Log.e("WeatherVM", "Refresh failed", e)
            }
        }
    }

    private suspend fun fetchWeather(lat: Double, lon: Double) {
        withContext(Dispatchers.IO) {
            val url = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,weather_code,wind_speed_10m&hourly=temperature_2m,weather_code,precipitation&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_sum,uv_index_max&timezone=auto"
            
            try {
                val json = JSONObject(URL(url).readText())
                val current = json.getJSONObject("current")
                val hourly = json.getJSONObject("hourly")
                val daily = json.getJSONObject("daily")
                
                val temp = current.getDouble("temperature_2m")
                val code = current.getInt("weather_code")
                val wind = current.getDouble("wind_speed_10m")

                val forecast = parseForecast(daily)
                val newStatus = determineSafetyStatus(temp, code, wind)
                
                _safetyStatus.value = newStatus
                _dayParts.value = parseDayParts(hourly)

                _currentWeather.value = WeatherData(
                    temp = temp,
                    condition = getWeatherDescription(code),
                    iconUrl = code.toString(),
                    humidity = 0, 
                    windSpeed = wind,
                    forecast = forecast,
                    clothingIcons = getClothingIcons(temp, code),
                    gardenAdvice = getGardenAdvice(temp, forecast),
                    windowAdvice = getWindowAdvice(temp),
                    activityAdvice = getActivityAdvice(temp, code, wind),
                    uvAdvice = getUvAdvice(forecast.firstOrNull()?.uvIndex ?: 0.0)
                )
            } catch (e: Exception) {
                Log.e("WeatherVM", "Fetch failed", e)
            }
        }
    }

    private fun determineSafetyStatus(temp: Double, code: Int, wind: Double) = when {
        temp > 30 || temp < 0 || code in 95..99 -> SafetyStatus.RED
        code in 51..82 || wind > 40 -> SafetyStatus.ORANGE
        else -> SafetyStatus.GREEN
    }

    private fun getClothingIcons(temp: Double, code: Int) = when {
        temp < 10 -> "🧥 🧣 🧤"
        code >= 51 -> "☂️ 👢"
        temp > 20 && code <= 1 -> "🕶️ 👕"
        else -> "🧥 👕"
    }

    private fun getGardenAdvice(temp: Double, forecast: List<ForecastDay>): String {
        val willRain = forecast.take(2).any { it.precipitation > 2.0 }
        return when {
            willRain -> i18n(R.string.u_hoeft_de_planten_buiten)
            temp > 20 -> i18n(R.string.het_is_droog_vergeet_uw)
            else -> ""
        }
    }

    private fun getWindowAdvice(temp: Double) = when {
        temp > 25 -> i18n(R.string.tip_het_wordt_heet_houd)
        temp in 15.0..22.0 -> i18n(R.string.lekker_weer_om_even_een)
        else -> ""
    }

    private fun getActivityAdvice(temp: Double, code: Int, wind: Double) = when {
        temp in 15.0..22.0 && code < 51 && wind < 30 -> i18n(R.string.vandaag_is_perfect_voor_een)
        temp > 18 && code <= 1 && wind < 20 -> i18n(R.string.heerlijk_weer_om_even_op)
        else -> ""
    }

    private fun getUvAdvice(uvIndex: Double) = if (uvIndex > 5) i18n(R.string.let_op_de_zon_is) else ""

    private fun parseForecast(dailyJson: JSONObject): List<ForecastDay> {
        val list = mutableListOf<ForecastDay>()
        val times = dailyJson.getJSONArray("time")
        val maxT = dailyJson.getJSONArray("temperature_2m_max")
        val minT = dailyJson.getJSONArray("temperature_2m_min")
        val codes = dailyJson.getJSONArray("weather_code")
        val prec = dailyJson.getJSONArray("precipitation_sum")
        val uv = dailyJson.getJSONArray("uv_index_max")
        for (i in 0 until 5) {
            list.add(ForecastDay(times.getString(i), minT.getDouble(i), maxT.getDouble(i), getWeatherDescription(codes.getInt(i)), codes.getInt(i).toString(), prec.getDouble(i), uv.getDouble(i)))
        }
        return list
    }

    private fun parseDayParts(hourly: JSONObject): List<DayPartForecast> {
        val temps = hourly.getJSONArray("temperature_2m")
        val codes = hourly.getJSONArray("weather_code")
        return listOf(
            calculateDayPart(i18n(R.string.ochtend), 6, 12, temps, codes),
            calculateDayPart(i18n(R.string.middag), 12, 18, temps, codes),
            calculateDayPart(i18n(R.string.avond), 18, 23, temps, codes),
            calculateDayPart(i18n(R.string.nacht), 23, 30, temps, codes)
        )
    }

    private fun calculateDayPart(label: String, start: Int, end: Int, temps: org.json.JSONArray, codes: org.json.JSONArray): DayPartForecast {
        var tSum = 0.0; var count = 0; val cMap = mutableMapOf<Int, Int>()
        for (i in start until end) {
            if (i < temps.length()) {
                val t = temps.optDouble(i, Double.NaN)
                if (!t.isNaN()) { tSum += t; val c = codes.getInt(i); cMap[c] = (cMap[c] ?: 0) + 1; count++ }
            }
        }
        val avg = if (count > 0) (tSum / count).toInt() else 0
        val mc = cMap.maxByOrNull { it.value }?.key ?: 0
        return DayPartForecast(label, avg, mc, getWeatherDescription(mc), mc.toString())
    }

    fun selectLocation(loc: WeatherLocation) { _selectedLocation.value = loc; refreshWeather(force = true) }
    fun selectCurrentLocation() { _selectedLocation.value = null; refreshWeather(force = true) }

    fun searchAndAddCity(cityName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = "https://geocoding-api.open-meteo.com/v1/search?name=$cityName&count=1&language=${com.seniorenlauncher.util.LocaleHelper.saved(com.seniorenlauncher.LauncherApp.instance)}&format=json"
                val json = JSONObject(URL(url).readText())
                json.optJSONArray("results")?.getJSONObject(0)?.let {
                    dao.insert(WeatherLocation(cityName = "${it.getString("name")} (${it.optString("country", "")})", latitude = it.getDouble("latitude"), longitude = it.getDouble("longitude")))
                }
            } catch (e: Exception) {}
        }
    }

    fun deleteLocation(loc: WeatherLocation) { viewModelScope.launch { dao.delete(loc); if (_selectedLocation.value?.id == loc.id) selectCurrentLocation() } }

    private fun getWeatherDescription(code: Int) = when (code) {
        0 -> i18n(R.string.zonnig); 1, 2, 3 -> i18n(R.string.licht_bewolkt); 45, 48 -> i18n(R.string.mistig); 51, 53, 55 -> i18n(R.string.miezeregen); 61, 63, 65 -> i18n(R.string.regen); 71, 73, 75 -> i18n(R.string.sneeuw); 80, 81, 82 -> i18n(R.string.buien); 95, 96, 99 -> i18n(R.string.onweer); else -> i18n(R.string.wisselvallig)
    }

    private fun generateSafetyMessage(status: SafetyStatus, temp: Double) = when (status) {
        SafetyStatus.RED -> if (temp > 30) i18n(R.string.blijf_binnen_het_is_extreem) else i18n(R.string.blijf_binnen_gevaarlijk_glad_of)
        SafetyStatus.ORANGE -> i18n(R.string.let_op_kans_op_regen_2)
        SafetyStatus.GREEN -> i18n(R.string.heerlijk_weer_om_naar_buiten)
    }
}
