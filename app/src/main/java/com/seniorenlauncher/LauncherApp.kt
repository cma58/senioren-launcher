package com.seniorenlauncher

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.room.Room
import com.seniorenlauncher.data.db.LauncherDatabase

class LauncherApp : Application() {
    lateinit var database: LauncherDatabase private set
    override fun onCreate() {
        super.onCreate(); instance = this
        database = Room.databaseBuilder(this, LauncherDatabase::class.java, "senioren_launcher.db")
            .fallbackToDestructiveMigration().build()
        val mgr = getSystemService(NotificationManager::class.java)
        mgr.createNotificationChannels(listOf(
            NotificationChannel(CH_SOS, "SOS Noodgeval", NotificationManager.IMPORTANCE_HIGH),
            NotificationChannel(CH_MEDS, "Medicijnen", NotificationManager.IMPORTANCE_HIGH),
            NotificationChannel(CH_RADIO, "Radio Speler", NotificationManager.IMPORTANCE_LOW),
            NotificationChannel(CH_BATTERY, "Batterij", NotificationManager.IMPORTANCE_DEFAULT),
            NotificationChannel(CH_GENERAL, "Algemeen", NotificationManager.IMPORTANCE_DEFAULT)
        ))
    }
    companion object {
        lateinit var instance: LauncherApp private set
        const val CH_SOS = "sos_channel"
        const val CH_MEDS = "medication_channel"
        const val CH_RADIO = "radio_channel"
        const val CH_BATTERY = "battery_channel"
        const val CH_GENERAL = "general_channel"
    }
}
