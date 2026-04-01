package com.seniorenlauncher.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.seniorenlauncher.LauncherApp
import com.seniorenlauncher.service.FallDetectionService
import com.seniorenlauncher.util.MedicationAlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Start services
            ctx.startForegroundService(Intent(ctx, FallDetectionService::class.java))
            
            // Reschedule all medication alarms
            CoroutineScope(Dispatchers.IO).launch {
                val medications = LauncherApp.instance.database.medicationDao().getAllSync()
                medications.forEach { med ->
                    if (med.active) {
                        MedicationAlarmScheduler.scheduleAlarms(ctx, med)
                    }
                }
            }
        }
    }
}
