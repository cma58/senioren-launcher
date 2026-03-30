package com.seniorenlauncher.service

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.IBinder
import android.telephony.SmsManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.Task
import com.seniorenlauncher.LauncherApp
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class SOSService : Service() {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val tag = "SOSService"

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationCompat.Builder(this, LauncherApp.CH_SOS)
            .setContentTitle("SOS ALARM ACTIEF")
            .setContentText("Uw locatie wordt naar noodcontacten gestuurd.")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .build()

        startForeground(1001, notification)

        scope.launch {
            try {
                processSOS()
            } catch (e: Exception) {
                Log.e(tag, "Fout bij uitvoeren SOS", e)
            } finally {
                delay(5000)
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    private suspend fun processSOS() {
        val context = applicationContext
        val dao = LauncherApp.instance.database.contactDao()
        val sosContacts = dao.getSosContacts().first()

        if (sosContacts.isEmpty()) {
            Log.w(tag, "Geen SOS contacten gevonden")
            return
        }

        val location = try {
            getLocation()
        } catch (e: Exception) {
            Log.e(tag, "Fout bij ophalen locatie", e)
            null
        }

        val mapsUrl = location?.let { "https://www.google.com/maps?q=${it.latitude},${it.longitude}" }
        val message = if (mapsUrl != null) {
            "NOODGEVAL! Ik heb hulp nodig. Mijn locatie: $mapsUrl"
        } else {
            "NOODGEVAL! Ik heb hulp nodig. Kon mijn exacte locatie niet bepalen."
        }

        val smsManager = context.getSystemService(SmsManager::class.java)
        
        sosContacts.forEach { contact ->
            try {
                smsManager.sendTextMessage(contact.phoneNumber, null, message, null, null)
                Log.d(tag, "SMS verstuurd naar ${contact.name}")
            } catch (e: Exception) {
                Log.e(tag, "Fout bij versturen SMS naar ${contact.name}", e)
            }
        }
    }

    private suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { cont ->
        addOnCompleteListener { task ->
            if (task.isSuccessful) {
                cont.resume(task.result)
            } else {
                cont.resumeWithException(task.exception ?: Exception("Task failed"))
            }
        }
    }

    private suspend fun getLocation(): Location? {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        return try {
            val lastLocation = fusedLocationClient.lastLocation.awaitTask()
            if (lastLocation != null && (System.currentTimeMillis() - lastLocation.time) < 60000) {
                lastLocation
            } else {
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).awaitTask()
            }
        } catch (e: Exception) {
            Log.e(tag, "Fout in getLocation", e)
            null
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
