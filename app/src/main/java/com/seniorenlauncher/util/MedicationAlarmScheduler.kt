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
    fun scheduleAlarms(context: Context, medication: Medication) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Check for exact alarm permission on Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.e("MedMads", "Cannot schedule exact alarms - permission missing")
                scheduleNonExactAlarm(context, medication, alarmManager)
                return
            }
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
                
                scheduleExact(context, medication, index, calendar.timeInMillis, alarmManager)
            } catch (e: Exception) {
                Log.e("MedMads", "Error scheduling alarm", e)
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
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (medicationId * 1000 + 999).toInt(), // Unique ID for snooze
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            snoozeTime,
            pendingIntent
        )
        Log.d("MedMads", "Snooze scheduled for 15 mins for med $medicationId")
    }

    private fun scheduleExact(context: Context, medication: Medication, index: Int, time: Long, alarmManager: AlarmManager) {
        val intent = Intent(context, MedicationAlarmReceiver::class.java).apply {
            putExtra("label", "${medication.name} (${medication.dose})")
            putExtra("medication_id", medication.id)
            putExtra("alarm_index", index)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (medication.id * 100 + index).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        try {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent)
        } catch (e: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent)
        }
    }

    private fun scheduleNonExactAlarm(context: Context, medication: Medication, alarmManager: AlarmManager) {
        val times = medication.times.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        times.forEachIndexed { index, timeStr ->
            try {
                val parts = timeStr.split(":")
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, parts[0].toInt())
                    set(Calendar.MINUTE, parts[1].toInt())
                    if (before(Calendar.getInstance())) add(Calendar.DATE, 1)
                }
                val intent = Intent(context, MedicationAlarmReceiver::class.java).apply {
                    putExtra("label", medication.name)
                    putExtra("medication_id", medication.id)
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    context, (medication.id * 100 + index).toInt(), intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            } catch (e: Exception) {}
        }
    }

    fun cancelAlarms(context: Context, medication: Medication) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val times = medication.times.split(",")
        times.forEachIndexed { index, _ ->
            val intent = Intent(context, MedicationAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                (medication.id * 100 + index).toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
        // Also cancel snooze
        val snoozeIntent = Intent(context, MedicationAlarmReceiver::class.java)
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context, (medication.id * 1000 + 999).toInt(), snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(snoozePendingIntent)
    }
}
