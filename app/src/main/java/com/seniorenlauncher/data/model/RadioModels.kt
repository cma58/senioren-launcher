package com.seniorenlauncher.data.model

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "radio_stations")
data class RadioStation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val url: String,
    val emoji: String,
    val category: String,
    val colorValue: Long,
    val isCustom: Boolean = false
) {
    val color: Color get() = Color(colorValue)
}

data class RadioCategory(
    val title: String,
    val stations: List<RadioStation>
)
