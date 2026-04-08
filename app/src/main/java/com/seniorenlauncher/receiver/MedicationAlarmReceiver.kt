package com.seniorenlauncher.receiver

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.SmsManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.seniorenlauncher.LauncherApp
import com.seniorenlauncher.MainActivity
import com.seniorenlauncher.data.model.Medication
import com.seniorenlauncher.data.model.MedicationLog
import com.seniorenlauncher.util.MedicationAlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MedicationAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        val medicationId = intent.getLongExtra("medication_id", -1L)
        val isSnooze = intent.getBooleanExtra("is_snooze", false)
        val isEscalation = intent.getBooleanExtra("is_escalation", false)
        val label = intent.getStringExtra("label") ?: "Medicijn"

        if (medicationId == -1L) return

        CoroutineScope(Dispatchers.IO).launch {
            val dao = LauncherApp.instance.database.medicationDao()
            val med = dao.getById(medicationId) ?: return@launch

            if (isEscalation) {
                handleEscalation(ctx, med)
                return@launch
            }

            if (isSnooze && !med.isPending) return@launch

            if (!isSnooze) {
                dao.update(med.copy(isPending = true))
                MedicationAlarmScheduler.scheduleAlarms(ctx, med)
                // Plan de escalatie over 1 uur
                scheduleEscalation(ctx, med)
            }

            showFullScreenNotification(ctx, med, label)
        }
    }

    private fun scheduleEscalation(ctx: Context, med: Medication) {
        val alarmManager = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val escalationTime = System.currentTimeMillis() + (60 * 60 * 1000L) // 1 uur later

        val intent = Intent(ctx, MedicationAlarmReceiver::class.java).apply {
            putExtra("medication_id", med.id)
            putExtra("is_escalation", true)
            action = "com.seniorenlauncher.MED_ESCALATION_${med.id}"
        }

        val pendingIntent = PendingIntent.getBroadcast(
            ctx, (med.id + 8000).toInt(), intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, escalationTime, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, escalationTime, pendingIntent)
            }
        } catch (e: Exception) {
            Log.e("MedReceiver", "Escalation failed", e)
        }
    }

    private suspend fun handleEscalation(ctx: Context, med: Medication) {
        val dao = LauncherApp.instance.database.medicationDao()
        val currentMed = dao.getById(med.id)
        
        if (currentMed?.isPending == true) {
            // Medicijn is nog niet ingenomen na 1 uur -> SMS sturen naar mantelzorger
            val contactDao = LauncherApp.instance.database.contactDao()
            val sosContacts = contactDao.getSosContactsSync()
            
            if (sosContacts.isNotEmpty()) {
                val message = "LET OP: Uw familielid heeft het medicijn '${med.name}' (${med.dose}) van een uur geleden nog niet afgevinkt in de Senioren Launcher."
                val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    ctx.getSystemService(SmsManager::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    SmsManager.getDefault()
                }

                sosContacts.forEach { contact ->
                    try {
                        smsManager.sendTextMessage(contact.phoneNumber, null, message, null, null)
                    } catch (e: Exception) {
                        Log.e("MedEscalation", "SMS failed", e)
                    }
                }
            }
            
            // Log als gemist
            dao.insertLog(MedicationLog(
                medicationId = med.id, 
                date = System.currentTimeMillis(), 
                time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()), 
                status = "MISSED"
            ))
        }
    }

    private fun showFullScreenNotification(ctx: Context, med: Medication, label: String) {
        val notificationId = (med.id + 5000).toInt()
        val tapIntent = Intent(ctx, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("NAVIGATE_TO", "alarm_trigger")
            putExtra("ALARM_LABEL", "Medicijn: ${med.name}")
            putExtra("ALARM_ID", med.id)
            putExtra("MED_PHOTO", med.photoUri)
            action = "com.seniorenlauncher.MED_ALARM_${med.id}"
        }
        
        val pendingIntent = PendingIntent.getActivity(
            ctx, notificationId, tapIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(ctx, LauncherApp.CH_MEDS)
            .setContentTitle("💊 TIJD VOOR MEDICIJNEN")
            .setContentText("Neem nu in: ${med.name} (${med.dose})")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(pendingIntent, true) 
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 800, 400, 800, 400, 800))
            .build()

        val notificationManager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }
}
