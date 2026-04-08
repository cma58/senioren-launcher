package com.seniorenlauncher.ui.screens

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seniorenlauncher.LauncherApp
import com.seniorenlauncher.data.model.AlarmEntry
import com.seniorenlauncher.receiver.AlarmReceiver
import com.seniorenlauncher.util.AlarmScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.*

class AlarmViewModel : ViewModel() {
    private val dao = LauncherApp.instance.database.alarmDao()
    private val context = LauncherApp.instance.applicationContext

    val alarms: StateFlow<List<AlarmEntry>> = dao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addAlarm(hour: Int, minute: Int, label: String, daysOfWeek: String, soundUri: String? = null, isMorningRoutine: Boolean = false) {
        viewModelScope.launch {
            val alarm = AlarmEntry(
                hour = hour, 
                minute = minute, 
                label = label, 
                daysOfWeek = daysOfWeek, 
                enabled = true, 
                soundUri = soundUri,
                isMorningRoutine = isMorningRoutine
            )
            val id = dao.insert(alarm)
            val insertedAlarm = alarm.copy(id = id)
            AlarmScheduler.scheduleAlarm(context, insertedAlarm)
        }
    }

    fun updateAlarm(alarm: AlarmEntry) {
        viewModelScope.launch {
            dao.update(alarm)
            if (alarm.enabled) {
                AlarmScheduler.scheduleAlarm(context, alarm)
            } else {
                AlarmScheduler.cancelAlarm(context, alarm)
            }
        }
    }

    fun deleteAlarm(alarm: AlarmEntry) {
        viewModelScope.launch {
            AlarmScheduler.cancelAlarm(context, alarm)
            dao.delete(alarm)
        }
    }

    fun toggleAlarm(alarm: AlarmEntry) {
        viewModelScope.launch {
            val updated = alarm.copy(enabled = !alarm.enabled)
            dao.update(updated)
            if (updated.enabled) {
                AlarmScheduler.scheduleAlarm(context, updated)
            } else {
                AlarmScheduler.cancelAlarm(context, updated)
            }
        }
    }
}
