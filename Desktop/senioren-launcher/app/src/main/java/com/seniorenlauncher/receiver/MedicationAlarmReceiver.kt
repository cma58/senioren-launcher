package com.seniorenlauncher.receiver
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.seniorenlauncher.LauncherApp

class MedicationAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        val label = intent.getStringExtra("label") ?: "Medicijn"
        val id = intent.getLongExtra("medication_id", 0)
        (ctx.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(VibrationEffect.createWaveform(longArrayOf(0,800,400,800),-1))
        NotificationManagerCompat.from(ctx).notify(id.toInt(), NotificationCompat.Builder(ctx, LauncherApp.CH_MEDS)
            .setContentTitle("💊 Tijd voor uw medicijn!").setContentText(label)
            .setSmallIcon(android.R.drawable.ic_dialog_alert).setPriority(NotificationCompat.PRIORITY_HIGH).build())
    }
}
