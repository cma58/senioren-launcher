package com.seniorenlauncher.receiver
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.seniorenlauncher.service.FallDetectionService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            ctx.startForegroundService(Intent(ctx, FallDetectionService::class.java))
        }
    }
}
