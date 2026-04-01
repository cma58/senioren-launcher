package com.seniorenlauncher.data.model
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AppTheme { CLASSIC, HIGH_CONTRAST, LIGHT }
enum class LayoutType { GRID_2x3, GRID_3x4, GRID_1x1 }

@Entity(tableName = "contacts")
data class QuickContact(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String, val phoneNumber: String,
    val photoUri: String? = null, val emoji: String = "👤",
    val color: Long = 0xFF3B82F6, val isSosContact: Boolean = false,
    val sortOrder: Int = 0
)

@Entity(tableName = "medications")
data class Medication(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String, 
    val dose: String, 
    val times: String, // Comma separated HH:mm
    val daysOfWeek: String = "1,2,3,4,5,6,7", // 1=Sun, 2=Mon...
    val color: Long = 0xFF3B82F6, 
    val active: Boolean = true,
    val lastReminderTime: Long = 0,
    val isPending: Boolean = false // New field to track if a reminder is currently active
)

@Entity(tableName = "medication_logs")
data class MedicationLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicationId: Long, val scheduledTime: String,
    val takenAt: Long? = null, val date: String
)

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String, val content: String, val color: Long = 0xFFFBBF24,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "calendar_events")
data class CalendarEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String, val dateTime: Long, val location: String = "",
    val color: Long = 0xFF3B82F6, val reminderMinutes: Int = 30
)

@Entity(tableName = "emergency_info")
data class EmergencyInfo(
    @PrimaryKey val id: Int = 1,
    val fullName: String = "Jan Peeters", val birthDate: String = "15-03-1942", val address: String = "Marktstraat 15, 9900 Eeklo",
    val bloodType: String = "A+", val allergies: String = "Penicilline, noten", val conditions: String = "Diabetes type 2, hoge bloeddruk",
    val doctorName: String = "Dr. Van Damme", val doctorPhone: String = "09 377 12 34", val hasPacemaker: Boolean = false,
    val iceContactName: String = "", val iceContactPhone: String = ""
)

@Entity(tableName = "alarms")
data class AlarmEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hour: Int, val minute: Int, val label: String,
    val daysOfWeek: String = "1,2,3,4,5,6,7", val enabled: Boolean = true
)

data class AppSettings(
    val theme: AppTheme = AppTheme.CLASSIC, 
    val layout: LayoutType = LayoutType.GRID_2x3,
    val fontSize: Int = 16, 
    val language: String = "nl", 
    val nightModeAuto: Boolean = true,
    val fallDetectionEnabled: Boolean = true, 
    val batteryAlertEnabled: Boolean = true,
    val locationSharingEnabled: Boolean = true,
    val chargingReminderEnabled: Boolean = true,
    val pinCode: String? = "1234", 
    val settingsLocked: Boolean = false,
    val visibleApps: Set<String> = setOf("phone","sms","whatsapp","video","camera","photos",
        "alarm","calendar","meds","weather","flashlight","magnifier","notes","radio","steps","emergency","sos", "settings"),
    val appMappings: Map<String, String> = emptyMap() // appId -> packageName
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
    AppInfo("whatsapp", "WhatsApp", "🟢", "🟢", 0xFF25D366),
    AppInfo("video", "Videobellen", "🎥", "🎥", 0xFF8B5CF6),
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
    AppInfo("settings", "Instellingen", "⚙️", "⚙️", 0xFF718096)
)
