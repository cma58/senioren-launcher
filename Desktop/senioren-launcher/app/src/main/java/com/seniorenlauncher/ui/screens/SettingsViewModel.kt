package com.seniorenlauncher.ui.screens
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.seniorenlauncher.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel(app: Application) : AndroidViewModel(app) {
    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()
    private val _currentTheme = MutableStateFlow(AppTheme.CLASSIC)
    val currentTheme: StateFlow<AppTheme> = _currentTheme.asStateFlow()

    fun updateTheme(t: AppTheme) { _currentTheme.value = t; _settings.value = _settings.value.copy(theme = t) }
    fun updateLayout(l: LayoutType) { _settings.value = _settings.value.copy(layout = l) }
    fun updateFontSize(s: Int) { _settings.value = _settings.value.copy(fontSize = s) }
    fun toggleNightMode() { _settings.value = _settings.value.copy(nightModeAuto = !_settings.value.nightModeAuto) }
    fun toggleFallDetection() { _settings.value = _settings.value.copy(fallDetectionEnabled = !_settings.value.fallDetectionEnabled) }
    fun toggleBatteryAlert() { _settings.value = _settings.value.copy(batteryAlertEnabled = !_settings.value.batteryAlertEnabled) }
    fun toggleAppVisibility(id: String) {
        val s = _settings.value.visibleApps.toMutableSet()
        if (s.contains(id)) s.remove(id) else s.add(id)
        _settings.value = _settings.value.copy(visibleApps = s)
    }
    fun lockSettings() { _settings.value = _settings.value.copy(settingsLocked = true) }
    fun unlockSettings() { _settings.value = _settings.value.copy(settingsLocked = false) }
    fun verifyPin(pin: String): Boolean = pin == (_settings.value.pinCode ?: "1234")
}
