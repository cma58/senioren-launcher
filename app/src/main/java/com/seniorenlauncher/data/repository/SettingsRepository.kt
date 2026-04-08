package com.seniorenlauncher.data.repository

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.seniorenlauncher.data.model.AppTheme
import com.seniorenlauncher.data.model.AppSettings
import com.seniorenlauncher.data.model.LayoutType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONObject

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    companion object {
        val THEME = stringPreferencesKey("theme")
        val LAYOUT = stringPreferencesKey("layout")
        val FONT_SIZE = intPreferencesKey("font_size")
        val LANGUAGE = stringPreferencesKey("language")
        val NIGHT_MODE_AUTO = booleanPreferencesKey("night_mode_auto")
        val FALL_DETECTION = booleanPreferencesKey("fall_detection")
        val BATTERY_ALERT = booleanPreferencesKey("battery_alert")
        val CHARGING_REMINDER = booleanPreferencesKey("charging_reminder")
        val PIN_CODE = stringPreferencesKey("pin_code")
        val SETTINGS_LOCKED = booleanPreferencesKey("settings_locked")
        val VISIBLE_APPS = stringPreferencesKey("visible_apps")
        val APP_MAPPINGS = stringPreferencesKey("app_mappings")
        val HAS_COMPLETED_SETUP = booleanPreferencesKey("has_completed_setup")
        val USER_PHONE_NUMBER = stringPreferencesKey("user_phone_number")

        // NIEUW: De sleutel voor het veilige SOS nummer van de mantelzorger
        val SOS_PHONE_NUMBER = stringPreferencesKey("sos_phone_number")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            theme = try { AppTheme.valueOf(prefs[THEME] ?: AppTheme.CLASSIC.name) } catch(e: Exception) { AppTheme.CLASSIC },
            layout = try { LayoutType.valueOf(prefs[LAYOUT] ?: LayoutType.GRID_2x3.name) } catch(e: Exception) { LayoutType.GRID_2x3 },
            fontSize = prefs[FONT_SIZE] ?: 18,
            language = prefs[LANGUAGE] ?: "nl",
            nightModeAuto = prefs[NIGHT_MODE_AUTO] ?: true,
            fallDetectionEnabled = prefs[FALL_DETECTION] ?: false,
            batteryAlertEnabled = prefs[BATTERY_ALERT] ?: true,
            chargingReminderEnabled = prefs[CHARGING_REMINDER] ?: true,
            pinCode = prefs[PIN_CODE] ?: "1234",
            settingsLocked = prefs[SETTINGS_LOCKED] ?: false,
            visibleApps = prefs[VISIBLE_APPS]?.split(",")?.filter { it.isNotEmpty() }?.toSet() ?: AppSettings().visibleApps,
            appMappings = prefs[APP_MAPPINGS]?.let { jsonStr ->
                try {
                    val json = JSONObject(jsonStr)
                    val map = mutableMapOf<String, String>()
                    json.keys().forEach { key -> map[key] = json.getString(key) }
                    map
                } catch(e: Exception) { emptyMap<String, String>() }
            } ?: emptyMap(),
            hasCompletedSetup = prefs[HAS_COMPLETED_SETUP] ?: false,
            userPhoneNumber = prefs[USER_PHONE_NUMBER]
        )
    }

    // NIEUW: Een aparte 'stroom' die de SmsReceiver kan uitlezen om de afzender te verifiëren
    val sosPhoneNumberFlow: Flow<String?> = context.dataStore.data.map { it[SOS_PHONE_NUMBER] }

    // NIEUW: De functie om het nummer van de mantelzorger veilig op te slaan
    suspend fun setSosPhoneNumber(number: String?) {
        context.dataStore.edit {
            if (number == null) it.remove(SOS_PHONE_NUMBER)
            else it[SOS_PHONE_NUMBER] = number
        }
    }

    suspend fun setTheme(theme: AppTheme) {
        context.dataStore.edit { it[THEME] = theme.name }
    }

    suspend fun setLayout(layout: LayoutType) {
        context.dataStore.edit { it[LAYOUT] = layout.name }
    }

    suspend fun setFontSize(size: Int) {
        context.dataStore.edit { it[FONT_SIZE] = size }
    }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { it[LANGUAGE] = lang }
    }

    suspend fun setNightModeAuto(enabled: Boolean) {
        context.dataStore.edit { it[NIGHT_MODE_AUTO] = enabled }
    }

    suspend fun setFallDetection(enabled: Boolean) {
        context.dataStore.edit { it[FALL_DETECTION] = enabled }
    }

    suspend fun setBatteryAlert(enabled: Boolean) {
        context.dataStore.edit { it[BATTERY_ALERT] = enabled }
    }

    suspend fun setChargingReminder(enabled: Boolean) {
        context.dataStore.edit { it[CHARGING_REMINDER] = enabled }
    }

    suspend fun setPinCode(pin: String) {
        context.dataStore.edit { it[PIN_CODE] = pin }
    }

    suspend fun setSettingsLocked(locked: Boolean) {
        context.dataStore.edit { it[SETTINGS_LOCKED] = locked }
    }

    suspend fun setVisibleApps(apps: Set<String>) {
        context.dataStore.edit { it[VISIBLE_APPS] = apps.joinToString(",") }
    }

    suspend fun setAppMappings(mappings: Map<String, String>) {
        val json = JSONObject(mappings)
        context.dataStore.edit { it[APP_MAPPINGS] = json.toString() }
    }

    suspend fun setHasCompletedSetup(completed: Boolean) {
        context.dataStore.edit { it[HAS_COMPLETED_SETUP] = completed }
    }

    suspend fun setUserPhoneNumber(number: String?) {
        context.dataStore.edit {
            if (number == null) it.remove(USER_PHONE_NUMBER)
            else it[USER_PHONE_NUMBER] = number
        }
    }
}