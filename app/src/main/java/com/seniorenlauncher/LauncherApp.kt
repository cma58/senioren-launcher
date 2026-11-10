package com.seniorenlauncher
import com.seniorenlauncher.R
import com.seniorenlauncher.util.i18n

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
        val smsChannel = NotificationChannel(CH_SMS, i18n(R.string.sms_berichten), NotificationManager.IMPORTANCE_HIGH).apply {
            description = i18n(R.string.meldingen_voor_nieuwe_sms_berichten)
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
        val medsChannel = NotificationChannel(CH_MEDS, i18n(R.string.medicijnen_2), NotificationManager.IMPORTANCE_HIGH).apply {
            description = i18n(R.string.herinneringen_voor_inname_van_medicatie)
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
        val alarmChannel = NotificationChannel(CH_ALARM, i18n(R.string.wekkers), NotificationManager.IMPORTANCE_MAX).apply {
            description = i18n(R.string.kanaal_voor_wekker_die_moet)
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
        val calendarChannel = NotificationChannel(CH_CALENDAR, i18n(R.string.agenda_wekkers), NotificationManager.IMPORTANCE_MAX).apply {
            description = i18n(R.string.grote_meldingen_voor_agenda_afspraken)
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
            NotificationChannel(CH_SOS, i18n(R.string.sos_noodgeval), NotificationManager.IMPORTANCE_HIGH),
            medsChannel,
            smsChannel,
            alarmChannel,
            calendarChannel,
            NotificationChannel(CH_RADIO, i18n(R.string.radio_speler), NotificationManager.IMPORTANCE_LOW),
            NotificationChannel(CH_BATTERY, i18n(R.string.batterij), NotificationManager.IMPORTANCE_DEFAULT),
            NotificationChannel(CH_GENERAL, i18n(R.string.algemeen), NotificationManager.IMPORTANCE_DEFAULT)
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
