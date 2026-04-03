package com.seniorenlauncher.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

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
    val description: String,
    val forecast: List<ForecastDay> = emptyList(),
    val clothingAdvice: String = ""
)

data class ForecastDay(
    val date: String,
    val minTemp: Double,
    val maxTemp: Double,
    val condition: String,
    val iconUrl: String
)
