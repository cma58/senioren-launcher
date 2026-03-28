package com.seniorenlauncher.service
import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.telephony.SmsManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationServices
import com.seniorenlauncher.LauncherApp
import kotlinx.coroutines.*

class SOSService : Service() {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1001, NotificationCompat.Builder(this, LauncherApp.CH_SOS)
            .setContentTitle("SOS wordt verstuurd...").setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_MAX).build())
        scope.launch { /* GPS ophalen + SMS versturen naar SOS-contacten */ stopSelf() }
        return START_NOT_STICKY
    }
    override fun onDestroy() { scope.cancel(); super.onDestroy() }
}
