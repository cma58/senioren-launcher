package com.seniorenlauncher.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seniorenlauncher.LauncherApp
import com.seniorenlauncher.data.model.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {
    private val repository = LauncherApp.instance.settingsRepository

    val settings: StateFlow<AppSettings> = repository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    fun updateTheme(t: AppTheme) {
        viewModelScope.launch { repository.setTheme(t) }
    }

    fun updateLayout(l: LayoutType) {
        viewModelScope.launch { repository.setLayout(l) }
    }

    fun updateFontSize(s: Int) {
        viewModelScope.launch { repository.setFontSize(s) }
    }

    fun updateLanguage(l: String) {
        viewModelScope.launch { repository.setLanguage(l) }
    }

    fun toggleNightMode() {
        viewModelScope.launch { repository.setNightModeAuto(!settings.value.nightModeAuto) }
    }

    fun toggleFallDetection() {
        viewModelScope.launch { repository.setFallDetection(!settings.value.fallDetectionEnabled) }
    }

    fun toggleScamProtection() {
        viewModelScope.launch {
            repository.updateSettings { it.copy(scamProtectionEnabled = !it.scamProtectionEnabled) }
        }
    }

    fun toggleBatteryAlert() {
        viewModelScope.launch { repository.setBatteryAlert(!settings.value.batteryAlertEnabled) }
    }

    fun toggleDefaultLauncher() {
        // Handled in UI via intent
    }

    fun toggleChargingReminder() {
        viewModelScope.launch { repository.setChargingReminder(!settings.value.chargingReminderEnabled) }
    }
    
    fun setAppMapping(appId: String, packageName: String) {
        viewModelScope.launch {
            val m = settings.value.appMappings.toMutableMap()
            m[appId] = packageName
            repository.setAppMappings(m)
        }
    }

    fun addAppMappingsBulk(newMappings: Map<String, String>) {
        viewModelScope.launch {
            val m = settings.value.appMappings.toMutableMap()
            m.putAll(newMappings)
            repository.setAppMappings(m)
            
            val s = settings.value.visibleApps.toMutableSet()
            s.addAll(newMappings.keys)
            repository.setVisibleApps(s)
        }
    }

    fun updateVisibleApps(apps: Set<String>) {
        viewModelScope.launch {
            repository.setVisibleApps(apps)
        }
    }

    fun toggleAppVisibility(id: String) {
        viewModelScope.launch {
            val s = settings.value.visibleApps.toMutableSet()
            if (s.contains(id)) s.remove(id) else s.add(id)
            repository.setVisibleApps(s)
        }
    }

    fun lockSettings() {
        viewModelScope.launch { repository.setSettingsLocked(true) }
    }

    fun unlockSettings() {
        viewModelScope.launch { repository.setSettingsLocked(false) }
    }

    fun setPinCode(pin: String) {
        viewModelScope.launch { repository.setPinCode(pin) }
    }

    fun completeSetup() {
        viewModelScope.launch { repository.setHasCompletedSetup(true) }
    }

    fun acceptPrivacy() {
        viewModelScope.launch { repository.setPrivacyAccepted(true) }
    }

    fun verifyPin(pin: String): Boolean = pin == (settings.value.pinCode ?: "1234")

    fun setUserPhoneNumber(number: String?) {
        viewModelScope.launch { repository.setUserPhoneNumber(number) }
    }
}
