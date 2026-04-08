package com.seniorenlauncher.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.seniorenlauncher.data.model.Medication
import com.seniorenlauncher.receiver.MedicationAlarmReceiver
import java.util.*

object MedicationAlarmScheduler {
    private const val TAG = "MedMads"

    fun scheduleAlarms(context: Context, medication: Medication) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Check for exact alarm permission on Android 12+
        val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

        val times = medication.times.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        
        times.forEachIndexed { index, timeStr ->
            try {
                val parts = timeStr.split(":")
                if (parts.size != 2) return@forEachIndexed
                
                val hour = parts[0].toInt()
                val minute = parts[1].toInt()
                
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    
                    if (before(Calendar.getInstance())) {
                        add(Calendar.DATE, 1)
                    }
                }
                
                if (canScheduleExact) {
                    scheduleExact(context, medication, index, calendar.timeInMillis, alarmManager)
                } else {
                    scheduleNonExact(context, medication, index, calendar.timeInMillis, alarmManager)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error scheduling alarm", e)
            }
        }
    }

    fun scheduleSnooze(context: Context, medicationId: Long, label: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val snoozeTime = Calendar.getInstance().apply {
            add(Calendar.MINUTE, 15)
        }.timeInMillis

        val intent = Intent(context, MedicationAlarmReceiver::class.java).apply {
            putExtra("label", label)
            putExtra("medication_id", medicationId)
            putExtra("is_snooze", true)
            action = "com.seniorenlauncher.MED_SNOOZE_$medicationId"
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (medicationId * 1000 + 999).toInt(),
            intent,
            flags
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, snoozeTime, pendingIntent)
        } catch (e: Exception) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, snoozeTime, pendingIntent)
        }
    }

    private fun scheduleExact(context: Context, medication: Medication, index: Int, time: Long, alarmManager: AlarmManager) {
        val intent = createBaseIntent(context, medication, index)
        val pendingIntent = createPendingIntent(context, (medication.id * 100 + index).toInt(), intent)
        
        try {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent)
        } catch (e: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent)
        }
    }

    private fun scheduleNonExact(context: Context, medication: Medication, index: Int, time: Long, alarmManager: AlarmManager) {
        val intent = createBaseIntent(context, medication, index)
        val pendingIntent = createPendingIntent(context, (medication.id * 100 + index).toInt(), intent)
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent)
    }

    private fun createBaseIntent(context: Context, medication: Medication, index: Int): Intent {
        return Intent(context, MedicationAlarmReceiver::class.java).apply {
            putExtra("label", "${medication.name} (${medication.dose})")
            putExtra("medication_id", medication.id)
            putExtra("alarm_index", index)
            action = "com.seniorenlauncher.MED_ALARM_${medication.id}_$index"
        }
    }

    private fun createPendingIntent(context: Context, requestCode: Int, intent: Intent): PendingIntent {
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun cancelAlarms(context: Context, medication: Medication) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val times = medication.times.split(",")
        times.forEachIndexed { index, _ ->
            val intent = createBaseIntent(context, medication, index)
            val pendingIntent = createPendingIntent(context, (medication.id * 100 + index).toInt(), intent)
            alarmManager.cancel(pendingIntent)
        }
        
        val snoozeIntent = Intent(context, MedicationAlarmReceiver::class.java).apply {
            action = "com.seniorenlauncher.MED_SNOOZE_${medication.id}"
        }
        val snoozePendingIntent = createPendingIntent(context, (medication.id * 1000 + 999).toInt(), snoozeIntent)
        alarmManager.cancel(snoozePendingIntent)
    }
}
