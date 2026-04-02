package com.seniorenlauncher.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
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
        val medicationId = intent.getLongExtra("medication_id", -1L)
        val isSnooze = intent.getBooleanExtra("is_snooze", false)
        val label = intent.getStringExtra("label") ?: "Medicijn"

        if (medicationId == -1L) return

        CoroutineScope(Dispatchers.IO).launch {
            val dao = LauncherApp.instance.database.medicationDao()
            val med = dao.getById(medicationId) ?: return@launch

            // Als het een snooze is, maar de medicatie is al ingenomen (isPending is false), stop dan de herhaling.
            if (isSnooze && !med.isPending) {
                Log.d("MedMads", "Snooze gestopt voor med $medicationId: al ingenomen.")
                return@launch
            }

            // Bij de eerste melding (geen snooze), zet de status op pending
            if (!isSnooze) {
                dao.update(med.copy(isPending = true))
                // Plan ook vast het alarm voor de volgende dag in
                MedicationAlarmScheduler.scheduleAlarms(ctx, med)
            }

            // Tril de telefoon
            val vibrator = ctx.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 800, 400, 800, 400, 800), -1))

            // Toon de notificatie
            showNotification(ctx, med, label)

            // Plan de volgende herinnering over 15 minuten als het nog niet is afgevinkt
            MedicationAlarmScheduler.scheduleSnooze(ctx, med.id, label)
        }
    }

    private fun showNotification(ctx: Context, med: Medication, label: String) {
        val tapIntent = Intent(ctx, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "meds")
        }
        val pendingIntent = PendingIntent.getActivity(
            ctx, med.id.toInt(), tapIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(ctx, LauncherApp.CH_MEDS)
            .setContentTitle("\uD83D\uDC8A Medicijn herinnering")
            .setContentText("Tijd voor: $label")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setFullScreenIntent(pendingIntent, true) 
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(med.id.toInt(), notification)
    }
}
