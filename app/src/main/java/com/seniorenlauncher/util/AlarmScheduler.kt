package com.seniorenlauncher.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.seniorenlauncher.data.model.AlarmEntry
import com.seniorenlauncher.receiver.AlarmReceiver
import java.util.*

object AlarmScheduler {
    fun scheduleAlarm(context: Context, alarm: AlarmEntry) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val days = alarm.daysOfWeek.split(",").filter { it.isNotBlank() }.map { it.toInt() }
        
        // Als er geen specifieke dagen zijn ingesteld (bijv. via SMS), nemen we 'vandaag' of 'morgen'
        val now = Calendar.getInstance()
        var minTime: Long = Long.MAX_VALUE
        
        if (days.isEmpty()) {
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, alarm.hour)
                set(Calendar.MINUTE, alarm.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (calendar.before(now)) {
                calendar.add(Calendar.DATE, 1)
            }
            minTime = calendar.timeInMillis
        } else {
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
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("ALARM_ID", alarm.id)
            putExtra("ALARM_LABEL", alarm.label)
            putExtra("ALARM_SOUND", alarm.soundUri)
            putExtra("ALARM_HOUR", alarm.hour)
            putExtra("ALARM_MINUTE", alarm.minute)
            putExtra("ALARM_MORNING_ROUTINE", alarm.isMorningRoutine)
            action = "com.seniorenlauncher.ALARM_ACTION_${alarm.id}"
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
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

    fun cancelAlarm(context: Context, alarm: AlarmEntry) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
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
