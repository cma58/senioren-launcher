package com.seniorenlauncher.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.seniorenlauncher.MainActivity

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val label = intent.getStringExtra("ALARM_LABEL") ?: "Wekker"
        val alarmId = intent.getLongExtra("ALARM_ID", -1L)
        val soundUri = intent.getStringExtra("ALARM_SOUND")

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "alarm_channel_v2"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Belangrijke Alarmen", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Kanaal voor wekker die moet blijven afgaan"
                setSound(null, null) 
                enableVibration(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        val fullScreenIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("NAVIGATE_TO", "alarm_trigger")
            putExtra("ALARM_LABEL", label)
            putExtra("ALARM_ID", alarmId)
            putExtra("ALARM_SOUND", soundUri)
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
            .setContentText("Tik om te stoppen: $label")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(pendingIntent, true)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .build()

        notificationManager.notify(alarmId.toInt(), notification)
        
        try {
            context.startActivity(fullScreenIntent)
        } catch (e: Exception) {}
    }
}
