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
    val name: String, val dose: String, val times: String,
    val color: Long = 0xFF3B82F6, val active: Boolean = true
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
    val fullName: String = "", val birthDate: String = "", val address: String = "",
    val bloodType: String = "", val allergies: String = "", val conditions: String = "",
    val doctorName: String = "", val doctorPhone: String = "", val hasPacemaker: Boolean = false
)

@Entity(tableName = "alarms")
data class AlarmEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hour: Int, val minute: Int, val label: String,
    val daysOfWeek: String = "1,2,3,4,5,6,7", val enabled: Boolean = true
)

data class AppSettings(
    val theme: AppTheme = AppTheme.CLASSIC, val layout: LayoutType = LayoutType.GRID_2x3,
    val fontSize: Int = 16, val language: String = "nl", val nightModeAuto: Boolean = true,
    val fallDetectionEnabled: Boolean = true, val batteryAlertEnabled: Boolean = true,
    val pinCode: String? = null, val settingsLocked: Boolean = false,
    val visibleApps: Set<String> = setOf("phone","sms","whatsapp","video","camera","photos",
        "alarm","calendar","meds","weather","flashlight","magnifier","notes","radio","steps","emergency","sos")
)
