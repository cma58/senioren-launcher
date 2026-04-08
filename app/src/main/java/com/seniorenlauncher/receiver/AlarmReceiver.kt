package com.seniorenlauncher.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.seniorenlauncher.LauncherApp
import com.seniorenlauncher.MainActivity

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val label = intent.getStringExtra("ALARM_LABEL") ?: "Wekker"
        val alarmId = intent.getLongExtra("ALARM_ID", -1L)
        val soundUri = intent.getStringExtra("ALARM_SOUND")
        val hour = intent.getIntExtra("ALARM_HOUR", -1)
        val minute = intent.getIntExtra("ALARM_MINUTE", -1)
        val isMorningRoutine = intent.getBooleanExtra("ALARM_MORNING_ROUTINE", false)

        if (alarmId == -1L) return

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Gebruik het centrale kanaal uit LauncherApp voor consistentie en Android 16 betrouwbaarheid
        val channelId = LauncherApp.CH_ALARM

        val fullScreenIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("NAVIGATE_TO", "alarm_trigger")
            putExtra("ALARM_LABEL", label)
            putExtra("ALARM_ID", alarmId)
            putExtra("ALARM_SOUND", soundUri)
            putExtra("ALARM_HOUR", hour)
            putExtra("ALARM_MINUTE", minute)
            putExtra("ALARM_MORNING_ROUTINE", isMorningRoutine)
            action = "com.seniorenlauncher.ALARM_OPEN_$alarmId"
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 
            alarmId.toInt(), 
            fullScreenIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("⏰ WEKKER")
            .setContentText("Tik om te openen: $label")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(pendingIntent, true)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        notificationManager.notify(alarmId.toInt(), notification)
        
        // Op Android 10+ is een FullScreenIntent verplicht, maar we proberen de activity 
        // ook direct te starten als de app al op de voorgrond is.
        try {
            context.startActivity(fullScreenIntent)
        } catch (e: Exception) {
            // Dit kan falen in de achtergrond op nieuwe Android versies, 
            // de FullScreenIntent neemt het dan over.
        }
    }
}
