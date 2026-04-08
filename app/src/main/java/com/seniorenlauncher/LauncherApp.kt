package com.seniorenlauncher

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import androidx.room.Room
import com.seniorenlauncher.data.db.LauncherDatabase
import com.seniorenlauncher.data.repository.SettingsRepository

class LauncherApp : Application() {
    lateinit var database: LauncherDatabase private set
    lateinit var settingsRepository: SettingsRepository private set
    
    override fun onCreate() {
        super.onCreate(); instance = this
        
        // MultiInstanceInvalidation zorgt dat database wijzigingen in de SmsReceiver 
        // direct zichtbaar zijn in het hoofdscherm (verschillende processen)
        database = Room.databaseBuilder(this, LauncherDatabase::class.java, "senioren_launcher.db")
            .enableMultiInstanceInvalidation()
            .fallbackToDestructiveMigration()
            .build()
        
        settingsRepository = SettingsRepository(this)
        
        val mgr = getSystemService(NotificationManager::class.java)
        
        // SMS Channel
        val smsChannel = NotificationChannel(CH_SMS, "SMS Berichten", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Meldingen voor nieuwe SMS-berichten"
            enableLights(true)
            enableVibration(true)
            setShowBadge(true)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()
            setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), audioAttributes)
        }

        // Medicijnen Channel
        val medsChannel = NotificationChannel(CH_MEDS, "Medicijnen", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Herinneringen voor inname van medicatie"
            enableLights(true)
            enableVibration(true)
            setShowBadge(true)
            setBypassDnd(true)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()
            setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), audioAttributes)
        }

        // Wekker Channel
        val alarmChannel = NotificationChannel(CH_ALARM, "Wekkers", NotificationManager.IMPORTANCE_MAX).apply {
            description = "Kanaal voor wekker die moet blijven afgaan"
            enableLights(true)
            enableVibration(true)
            setBypassDnd(true)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()
            setSound(null, null) // Geluid wordt door de Activity zelf afgehandeld voor meer controle
        }

        // Agenda Wekker Channel
        val calendarChannel = NotificationChannel(CH_CALENDAR, "Agenda Wekkers", NotificationManager.IMPORTANCE_MAX).apply {
            description = "Grote meldingen voor agenda afspraken"
            enableLights(true)
            enableVibration(true)
            setBypassDnd(true)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()
            setSound(null, null) 
        }

        mgr.createNotificationChannels(listOf(
            NotificationChannel(CH_SOS, "SOS Noodgeval", NotificationManager.IMPORTANCE_HIGH),
            medsChannel,
            smsChannel,
            alarmChannel,
            calendarChannel,
            NotificationChannel(CH_RADIO, "Radio Speler", NotificationManager.IMPORTANCE_LOW),
            NotificationChannel(CH_BATTERY, "Batterij", NotificationManager.IMPORTANCE_DEFAULT),
            NotificationChannel(CH_GENERAL, "Algemeen", NotificationManager.IMPORTANCE_DEFAULT)
        ))
    }

    companion object {
        lateinit var instance: LauncherApp private set
        const val CH_SOS = "sos_channel"
        const val CH_MEDS = "medication_channel_v3"
        const val CH_ALARM = "alarm_channel_v3"
        const val CH_SMS = "sms_v5"
        const val CH_CALENDAR = "calendar_reminder_channel_v5"
        const val CH_RADIO = "radio_channel"
        const val CH_BATTERY = "battery_channel"
        const val CH_GENERAL = "general_channel"
    }
}
