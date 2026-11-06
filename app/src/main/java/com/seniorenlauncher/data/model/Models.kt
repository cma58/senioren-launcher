package com.seniorenlauncher.data.model
import com.seniorenlauncher.R
import com.seniorenlauncher.util.i18n

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AppTheme { CLASSIC, HIGH_CONTRAST, LIGHT }
enum class LayoutType { GRID_2x3, GRID_3x4, GRID_1x1 }

data class AppSettings(
    val theme: AppTheme = AppTheme.CLASSIC,
    val layout: LayoutType = LayoutType.GRID_2x3,
    val fontSize: Int = 18,
    val language: String = "ru",
    val nightModeAuto: Boolean = true,
    val visibleApps: Set<String> = setOf("phone", "sms", "camera", "photos", "radio", "meds", "emergency", "sos", "remote_support", "wifi", "bluetooth", "settings"),
    val appMappings: Map<String, String> = emptyMap(),
    val settingsLocked: Boolean = false,
    val pinCode: String? = "1234",
    val fallDetectionEnabled: Boolean = false,
    val batteryAlertEnabled: Boolean = true,
    val chargingReminderEnabled: Boolean = true,
    val scamProtectionEnabled: Boolean = false,
    val hasCompletedSetup: Boolean = false,
    val privacyAccepted: Boolean = false,
    val userPhoneNumber: String? = null,
    val stepsToday: Int = 0
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

@Entity(tableName = "blocked_numbers")
data class BlockedNumber(
    @PrimaryKey val phoneNumber: String,
    val reason: String = "Blocked by remote admin"
)

@Entity(tableName = "medications")
data class Medication(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val dose: String,
    val times: String, 
    val daysOfWeek: String = "1,2,3,4,5,6,7",
    val isTaken: Boolean = false,
    val lastTakenDate: Long = 0,
    val active: Boolean = true,
    val isPending: Boolean = false,
    val stockCount: Int = 0,
    val lowStockThreshold: Int = 5,
    val photoUri: String? = null,
    val lastActionTime: Long = 0
)

@Entity(tableName = "medication_logs")
data class MedicationLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicationId: Long,
    val date: Long,
    val time: String,
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
    val soundUri: String? = null,
    val isMorningRoutine: Boolean = false
)

data class AppInfo(
    val id: String,
    val name: String,
    val icon: String,
    val emoji: String,
    val color: Long
)

val ALL_APPS: List<AppInfo> get() = listOf(
    AppInfo("phone", i18n(R.string.bellen), "📞", "📞", 0xFF38A169),
    AppInfo("sms", i18n(R.string.berichten), "💬", "💬", 0xFF3B82F6),
    AppInfo("camera", i18n(R.string.camera_2), "📷", "📷", 0xFFEC4899),
    AppInfo("photos", i18n(R.string.foto_s), "🖼️", "🖼️", 0xFFF59E0B),
    AppInfo("alarm", i18n(R.string.wekker), "⏰", "⏰", 0xFFEA580C),
    AppInfo("calendar", i18n(R.string.agenda), "📅", "📅", 0xFF0D9488),
    AppInfo("meds", i18n(R.string.medicijnen_2), "💊", "💊", 0xFFDC2626),
    AppInfo("weather", i18n(R.string.weer), "🌤️", "🌤️", 0xFF0EA5E9),
    AppInfo("flashlight", i18n(R.string.zaklamp), "🔦", "🔦", 0xFFFBBF24),
    AppInfo("magnifier", i18n(R.string.vergrootglas), "🔍", "🔍", 0xFF6366F1),
    AppInfo("notes", i18n(R.string.notities), "📝", "📝", 0xFF84CC16),
    AppInfo("radio", i18n(R.string.radio), "📻", "📻", 0xFFA855F7),
    AppInfo("wifi", i18n(R.string.wifi), "📶", "📶", 0xFF4A5568),
    AppInfo("bluetooth", i18n(R.string.bluetooth_2), "📶", "📶", 0xFF4A5568),
    AppInfo("steps", i18n(R.string.stappen), "🚶", "🚶", 0xFF14B8A6),
    AppInfo("emergency", i18n(R.string.noodinfo), "🏥", "🏥", 0xFFEF4444),
    AppInfo("sos", i18n(R.string.sos), "🆘", "🆘", 0xFFDC2626),
    AppInfo("remote_support", i18n(R.string.hulp_op_afstand), "👨‍🔧", "👨‍🔧", 0xFF4A5568),
    AppInfo("all_apps", i18n(R.string.alle_apps), "📱", "📱", 0xFF718096),
    AppInfo("settings", i18n(R.string.instellingen_2), "⚙️", "⚙️", 0xFF718096),
)
