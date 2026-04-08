package com.seniorenlauncher.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.provider.CalendarContract
import android.util.Log
import androidx.core.app.NotificationCompat
import com.seniorenlauncher.LauncherApp
import com.seniorenlauncher.MainActivity
import kotlinx.coroutines.*
import java.util.Calendar

class CalendarReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()

        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch(Dispatchers.IO) {
            try {
                var eventTitle = "Geplande afspraak"
                var startTime: Long = -1
                var eventId: Long = System.currentTimeMillis()

                // 1. Veilig de meest recente melding ophalen uit CalendarAlerts (Samsung-proof)
                val alertUri = CalendarContract.CalendarAlerts.CONTENT_URI
                val projection = arrayOf(
                    CalendarContract.CalendarAlerts.TITLE,
                    CalendarContract.CalendarAlerts.EVENT_ID,
                    CalendarContract.CalendarAlerts.BEGIN
                )
                
                // Zoek naar meldingen die net zijn afgegaan
                val sortOrder = "${CalendarContract.CalendarAlerts.BEGIN} DESC LIMIT 1"

                context.contentResolver.query(alertUri, projection, null, null, sortOrder)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        eventTitle = cursor.getString(cursor.getColumnIndexOrThrow(CalendarContract.CalendarAlerts.TITLE)) ?: eventTitle
                        eventId = cursor.getLong(cursor.getColumnIndexOrThrow(CalendarContract.CalendarAlerts.EVENT_ID))
                        startTime = cursor.getLong(cursor.getColumnIndexOrThrow(CalendarContract.CalendarAlerts.BEGIN))
                    }
                }

                val cal = Calendar.getInstance()
                val now = cal.clone() as Calendar
                if (startTime != -1L) cal.timeInMillis = startTime

                val eventHour = cal.get(Calendar.HOUR_OF_DAY)
                val eventMinute = cal.get(Calendar.MINUTE)

                val eventTimeString = if (eventMinute == 0) "om $eventHour uur" else "om $eventHour uur $eventMinute"
                val ttsLabel = "U heeft een afspraak voor $eventTitle $eventTimeString."

                // 2. Bouw de Intent (EXTREEM DWINGEND)
                val notificationId = 9999
                val fullScreenIntent = Intent(context, MainActivity::class.java).apply {
                    data = null 
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    
                    putExtra("NAVIGATE_TO", "alarm_trigger")
                    putExtra("ALARM_LABEL", ttsLabel)
                    putExtra("ALARM_ID", eventId)
                    putExtra("ALARM_HOUR", now.get(Calendar.HOUR_OF_DAY)) 
                    putExtra("ALARM_MINUTE", now.get(Calendar.MINUTE))
                    putExtra("isAgendaEvent", true)
                    putExtra("agendaTitle", eventTitle)
                    
                    action = "com.seniorenlauncher.CALENDAR_ALARM_$eventId"
                }

                val pendingIntent = PendingIntent.getActivity(
                    context,
                    notificationId,
                    fullScreenIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                // 3. Bouw de Notificatie
                val notification = NotificationCompat.Builder(context, LauncherApp.CH_CALENDAR)
                    .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                    .setContentTitle("📅 AFSPRAAK")
                    .setContentText(eventTitle)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setFullScreenIntent(pendingIntent, true) // Forceert het wekken
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setOngoing(true)
                    .setAutoCancel(true)
                    .setVibrate(longArrayOf(0, 1000, 500, 1000))
                    .build()

                // --- DE DUBBELE KLAP VOOR SAMSUNG ---
                // We vuren de melding af EN we openen de app direct
                val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                nm.notify(notificationId, notification)

                delay(300) // Heel kort wachten

                try {
                    context.startActivity(fullScreenIntent)
                } catch (e: Exception) {
                    Log.e("CalendarReminder", "Directe start mislukt, FullScreenIntent doet hopelijk het werk")
                }

            } catch (e: Exception) {
                Log.e("CalendarReminder", "Fout", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
