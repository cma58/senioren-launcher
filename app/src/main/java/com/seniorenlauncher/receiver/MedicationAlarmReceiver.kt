package com.seniorenlauncher.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import com.seniorenlauncher.LauncherApp
import com.seniorenlauncher.MainActivity
import com.seniorenlauncher.data.model.Medication
import com.seniorenlauncher.util.MedicationAlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MedicationAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        val label = intent.getStringExtra("label") ?: "Medicijn"
        val id = intent.getLongExtra("medication_id", -1L)
        
        if (id == -1L) return

        // Vibrate
        val vibrator = ctx.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 800, 400, 800, 400, 800), -1))

        // Update database status to pending
        CoroutineScope(Dispatchers.IO).launch {
            val dao = LauncherApp.instance.database.medicationDao()
            val med = dao.getById(id)
            if (med != null) {
                dao.insert(med.copy(isPending = true))
                // Reschedule for tomorrow
                MedicationAlarmScheduler.scheduleAlarms(ctx, med)
            }
        }

        // Create Intent to open the app
        val tapIntent = Intent(ctx, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "meds")
        }
        val pendingIntent = PendingIntent.getActivity(
            ctx, id.toInt(), tapIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Show Notification
        val notification = NotificationCompat.Builder(ctx, LauncherApp.CH_MEDS)
            .setContentTitle("\uD83D\uDC8A Tijd voor uw medicijn!")
            .setContentText("Herinnering voor: $label")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setFullScreenIntent(pendingIntent, true) 
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(id.toInt(), notification)
    }
}
