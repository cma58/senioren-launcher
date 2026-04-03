package com.seniorenlauncher.ui.screens

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.seniorenlauncher.LauncherApp
import com.seniorenlauncher.data.model.ForecastDay
import com.seniorenlauncher.data.model.WeatherData
import com.seniorenlauncher.data.model.WeatherLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import java.util.*

class WeatherViewModel : ViewModel() {
    private val dao = LauncherApp.instance.database.weatherDao()
    
    private val _currentWeather = MutableStateFlow<WeatherData?>(null)
    val currentWeather: StateFlow<WeatherData?> = _currentWeather.asStateFlow()

    private val _selectedLocation = MutableStateFlow<WeatherLocation?>(null)
    val selectedLocation: StateFlow<WeatherLocation?> = _selectedLocation.asStateFlow()

    val savedLocations: StateFlow<List<WeatherLocation>> = dao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        refreshWeather()
    }

    @SuppressLint("MissingPermission")
    fun refreshWeather() {
        val context = LauncherApp.instance.applicationContext
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        
        viewModelScope.launch {
            try {
                if (_selectedLocation.value == null || _selectedLocation.value?.isCurrentLocation == true) {
                    val location = fusedLocationClient.lastLocation.await()
                    if (location != null) {
                        _selectedLocation.value = WeatherLocation(
                            cityName = "Huidige Locatie",
                            latitude = location.latitude,
                            longitude = location.longitude,
                            isCurrentLocation = true
                        )
                        fetchWeather(location.latitude, location.longitude)
                    } else {
                        fetchWeather(52.3676, 4.9041) // Amsterdam fallback
                    }
                } else {
                    val loc = _selectedLocation.value!!
                    fetchWeather(loc.latitude, loc.longitude)
                }
            } catch (e: Exception) {
                fetchWeather(52.3676, 4.9041)
            }
        }
    }

    fun selectLocation(location: WeatherLocation) {
        _selectedLocation.value = location
        refreshWeather()
    }

    fun selectCurrentLocation() {
        _selectedLocation.value = null // Will trigger GPS fetch in refresh
        refreshWeather()
    }

    fun searchAndAddCity(cityName: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    // Use Geocoding API (Open-Meteo geocoding)
                    val url = "https://geocoding-api.open-meteo.com/v1/search?name=$cityName&count=1&language=nl&format=json"
                    val response = URL(url).readText()
                    val json = JSONObject(response)
                    val results = json.optJSONArray("results")
                    if (results != null && results.length() > 0) {
                        val first = results.getJSONObject(0)
                        val name = first.getString("name")
                        val lat = first.getDouble("latitude")
                        val lon = first.getDouble("longitude")
                        val country = first.optString("country", "")
                        
                        val newLoc = WeatherLocation(
                            cityName = "$name ($country)",
                            latitude = lat,
                            longitude = lon
                        )
                        dao.insert(newLoc)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun deleteLocation(location: WeatherLocation) {
        viewModelScope.launch {
            dao.delete(location)
            if (_selectedLocation.value?.id == location.id) {
                selectCurrentLocation()
            }
        }
    }

    private suspend fun fetchWeather(lat: Double, lon: Double) {
        withContext(Dispatchers.IO) {
            val url = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m&daily=weather_code,temperature_2m_max,temperature_2m_min&timezone=auto"
            
            try {
                val response = URL(url).readText()
                val json = JSONObject(response)
                val current = json.getJSONObject("current")
                val daily = json.getJSONObject("daily")
                
                val temp = current.getDouble("temperature_2m")
                val code = current.getInt("weather_code")
                
                val forecast = mutableListOf<ForecastDay>()
                val times = daily.getJSONArray("time")
                val maxTemps = daily.getJSONArray("temperature_2m_max")
                val minTemps = daily.getJSONArray("temperature_2m_min")
                val codes = daily.getJSONArray("weather_code")

                for (i in 0 until 5) {
                    forecast.add(ForecastDay(
                        date = times.getString(i),
                        minTemp = minTemps.getDouble(i),
                        maxTemp = maxTemps.getDouble(i),
                        condition = getWeatherDescription(codes.getInt(i)),
                        iconUrl = codes.getInt(i).toString()
                    ))
                }

                val data = WeatherData(
                    temp = temp,
                    condition = getWeatherDescription(code),
                    iconUrl = code.toString(),
                    humidity = current.getInt("relative_humidity_2m"),
                    windSpeed = current.getDouble("wind_speed_10m"),
                    description = "Vandaag is het ${getWeatherDescription(code)}",
                    forecast = forecast,
                    clothingAdvice = generateClothingAdvice(temp, code)
                )
                
                withContext(Dispatchers.Main) {
                    _currentWeather.value = data
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun generateClothingAdvice(temp: Double, code: Int): String {
        val baseAdvice = when {
            temp < 5 -> "Het is erg koud! Trek een dikke winterjas, muts en handschoenen aan."
            temp < 12 -> "Het is fris. Een warme jas of een dikke trui is aanbevolen."
            temp < 18 -> "Mild weer. Een lichte jas of vest is voldoende."
            temp < 25 -> "Lekker weer! Een T-shirt of dunne blouse is prima."
            else -> "Het is warm! Draag luchtige kleding en vergeet de zonnebrand niet."
        }
        
        val rainAdvice = if (code >= 51) "\nNeem ook een paraplu mee, er is kans op regen." else ""
        return baseAdvice + rainAdvice
    }

    fun getWeatherDescription(code: Int): String {
        return when (code) {
            0 -> "Onbewolkt"
            1, 2, 3 -> "Licht bewolkt"
            45, 48 -> "Mistig"
            51, 53, 55 -> "Miezeregen"
            61, 63, 65 -> "Regen"
            71, 73, 75 -> "Sneeuw"
            80, 81, 82 -> "Regenbuien"
            95 -> "Onweer"
            else -> "Wisselvallig"
        }
    }
}
