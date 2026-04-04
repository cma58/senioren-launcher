package com.seniorenlauncher.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AppTheme { CLASSIC, HIGH_CONTRAST, LIGHT }
enum class LayoutType { GRID_2x3, GRID_3x4, GRID_1x1 }

data class AppSettings(
    val theme: AppTheme = AppTheme.CLASSIC,
    val layout: LayoutType = LayoutType.GRID_2x3,
    val fontSize: Int = 18,
    val language: String = "nl",
    val nightModeAuto: Boolean = true,
    val visibleApps: Set<String> = setOf("phone", "sms", "camera", "photos", "radio", "meds", "emergency", "sos", "remote_support"),
    val appMappings: Map<String, String> = emptyMap(),
    val settingsLocked: Boolean = false,
    val pinCode: String? = "1234",
    val fallDetectionEnabled: Boolean = false,
    val batteryAlertEnabled: Boolean = true,
    val chargingReminderEnabled: Boolean = true,
    val hasCompletedSetup: Boolean = false
)

@Entity(tableName = "contacts")
data class QuickContact(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phoneNumber: String,
    val emoji: String = "👤",
    val color: Long = 0xFF718096,
    val isSosContact: Boolean = false,
    val sortOrder: Int = 0,
    val photoUri: String? = null
)

@Entity(tableName = "medications")
data class Medication(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val dose: String,
    val times: String, // Comma-separated times e.g., "08:00,20:00"
    val daysOfWeek: String = "1,2,3,4,5,6,7",
    val isTaken: Boolean = false,
    val lastTakenDate: Long = 0,
    val active: Boolean = true,
    val isPending: Boolean = false
)

@Entity(tableName = "medication_logs")
data class MedicationLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicationId: Long,
    val date: Long,
    val status: String
)

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "calendar_events")
data class CalendarEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val dateTime: Long,
    val description: String = ""
)

@Entity(tableName = "emergency_info")
data class EmergencyInfo(
    @PrimaryKey val id: Int = 1,
    val fullName: String = "",
    val birthDate: String = "",
    val address: String = "",
    val bloodType: String = "",
    val allergies: String = "",
    val conditions: String = "",
    val hasPacemaker: Boolean = false,
    val doctorName: String = "",
    val doctorPhone: String = "",
    val iceContactName: String = "",
    val iceContactPhone: String = ""
)

@Entity(tableName = "alarms")
data class AlarmEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val label: String,
    val daysOfWeek: String = "",
    val enabled: Boolean = true,
    val soundUri: String? = null
)

data class AppInfo(
    val id: String,
    val name: String,
    val icon: String,
    val emoji: String,
    val color: Long
)

val ALL_APPS = listOf(
    AppInfo("phone", "Bellen", "📞", "📞", 0xFF38A169),
    AppInfo("sms", "Berichten", "💬", "💬", 0xFF3B82F6),
    AppInfo("camera", "Camera", "📷", "📷", 0xFFEC4899),
    AppInfo("photos", "Foto's", "🖼️", "🖼️", 0xFFF59E0B),
    AppInfo("alarm", "Wekker", "⏰", "⏰", 0xFFEA580C),
    AppInfo("calendar", "Agenda", "📅", "📅", 0xFF0D9488),
    AppInfo("meds", "Medicijnen", "💊", "💊", 0xFFDC2626),
    AppInfo("weather", "Weer", "🌤️", "🌤️", 0xFF0EA5E9),
    AppInfo("flashlight", "Zaklamp", "🔦", "🔦", 0xFFFBBF24),
    AppInfo("magnifier", "Vergrootglas", "🔍", "🔍", 0xFF6366F1),
    AppInfo("notes", "Notities", "📝", "📝", 0xFF84CC16),
    AppInfo("radio", "Radio", "📻", "📻", 0xFFA855F7),
    AppInfo("steps", "Stappen", "🚶", "🚶", 0xFF14B8A6),
    AppInfo("emergency", "Noodinfo", "🏥", "🏥", 0xFFEF4444),
    AppInfo("sos", "SOS", "🆘", "🆘", 0xFFDC2626),
    AppInfo("remote_support", "Hulp op afstand", "👨‍🔧", "👨‍🔧", 0xFF4A5568),
    AppInfo("all_apps", "Alle Apps", "📱", "📱", 0xFF718096),
    AppInfo("settings", "Instellingen", "⚙️", "⚙️", 0xFF718096),
)
