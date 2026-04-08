package com.seniorenlauncher.service
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.*
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import com.seniorenlauncher.LauncherApp
import kotlinx.coroutines.*
import kotlin.math.sqrt

class FallDetectionService : Service(), SensorEventListener {
    private lateinit var sensorMgr: SensorManager
    private var freeFall = false
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onCreate() { super.onCreate(); sensorMgr = getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationCompat.Builder(this, LauncherApp.CH_GENERAL)
            .setContentTitle("Valdetectie actief")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()
            
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(1002, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH)
        } else {
            startForeground(1002, notification)
        }
            
        sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let { sensorMgr.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        return START_STICKY
    }
    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        val a = sqrt(event.values[0]*event.values[0]+event.values[1]*event.values[1]+event.values[2]*event.values[2])
        if (a < 3f) freeFall = true
        if (freeFall && a > 25f) { freeFall = false; onFall() }
        if (freeFall && a > 5f && a < 25f) freeFall = false
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    private fun onFall() {
        (getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(VibrationEffect.createWaveform(longArrayOf(0,500,200,500),-1))
        scope.launch { 
            delay(30_000)
            val intent = Intent(this@FallDetectionService, SOSService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        }
    }
    override fun onDestroy() { sensorMgr.unregisterListener(this); scope.cancel(); super.onDestroy() }
}