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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.*

class AlarmViewModel : ViewModel() {
    private val dao = LauncherApp.instance.database.alarmDao()
    private val alarmManager = LauncherApp.instance.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val context = LauncherApp.instance.applicationContext

    val alarms: StateFlow<List<AlarmEntry>> = dao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addAlarm(hour: Int, minute: Int, label: String, daysOfWeek: String, soundUri: String? = null) {
        viewModelScope.launch {
            val alarm = AlarmEntry(hour = hour, minute = minute, label = label, daysOfWeek = daysOfWeek, enabled = true, soundUri = soundUri)
            val id = dao.insert(alarm)
            val insertedAlarm = alarm.copy(id = id)
            scheduleAlarm(insertedAlarm)
        }
    }

    fun updateAlarm(alarm: AlarmEntry) {
        viewModelScope.launch {
            dao.update(alarm)
            if (alarm.enabled) {
                scheduleAlarm(alarm)
            } else {
                cancelAlarm(alarm)
            }
        }
    }

    fun deleteAlarm(alarm: AlarmEntry) {
        viewModelScope.launch {
            cancelAlarm(alarm)
            dao.delete(alarm)
        }
    }

    fun toggleAlarm(alarm: AlarmEntry) {
        viewModelScope.launch {
            val updated = alarm.copy(enabled = !alarm.enabled)
            dao.update(updated)
            if (updated.enabled) {
                scheduleAlarm(updated)
            } else {
                cancelAlarm(updated)
            }
        }
    }

    private fun scheduleAlarm(alarm: AlarmEntry) {
        val days = alarm.daysOfWeek.split(",").filter { it.isNotBlank() }.map { it.toInt() }
        if (days.isEmpty()) return

        val now = Calendar.getInstance()
        var minTime: Long = Long.MAX_VALUE
        
        for (day in days) {
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, alarm.hour)
                set(Calendar.MINUTE, alarm.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                set(Calendar.DAY_OF_WEEK, day)
            }
            
            if (calendar.before(now)) {
                calendar.add(Calendar.WEEK_OF_YEAR, 1)
            }
            
            if (calendar.timeInMillis < minTime) {
                minTime = calendar.timeInMillis
            }
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("ALARM_ID", alarm.id)
            putExtra("ALARM_LABEL", alarm.label)
            putExtra("ALARM_SOUND", alarm.soundUri)
            action = "com.seniorenlauncher.ALARM_ACTION_${alarm.id}"
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, minTime, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, minTime, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, minTime, pendingIntent)
            }
        } catch (e: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, minTime, pendingIntent)
        }
    }

    private fun cancelAlarm(alarm: AlarmEntry) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.seniorenlauncher.ALARM_ACTION_${alarm.id}"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
