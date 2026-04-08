package com.seniorenlauncher.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SafetyStatus { GREEN, ORANGE, RED }

data class DayPartForecast(
    val label: String,
    val temp: Int,
    val weatherCode: Int,
    val condition: String,
    val icon: String
)

@Entity(tableName = "weather_locations")
data class WeatherLocation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cityName: String,
    val latitude: Double,
    val longitude: Double,
    val isCurrentLocation: Boolean = false
)

data class WeatherData(
    val temp: Double,
    val condition: String,
    val iconUrl: String,
    val humidity: Int,
    val windSpeed: Double,
    val description: String = "",
    val forecast: List<ForecastDay> = emptyList(),
    val clothingAdvice: String = "",
    val clothingIcons: String = "",
    val gardenAdvice: String = "",
    val windowAdvice: String = "",
    val activityAdvice: String = "",
    val uvAdvice: String = ""
)

data class ForecastDay(
    val date: String,
    val minTemp: Double,
    val maxTemp: Double,
    val condition: String,
    val iconUrl: String,
    val precipitation: Double = 0.0,
    val uvIndex: Double = 0.0
)
