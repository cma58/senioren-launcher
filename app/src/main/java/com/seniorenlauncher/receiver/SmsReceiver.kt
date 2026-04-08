package com.seniorenlauncher.receiver

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.location.Location
import android.media.AudioManager
import android.os.BatteryManager
import android.os.Build
import android.provider.CalendarContract
import android.provider.Settings
import android.provider.Telephony
import android.telephony.PhoneNumberUtils
import android.telephony.SmsManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Tasks
import com.seniorenlauncher.LauncherApp
import com.seniorenlauncher.MainActivity
import com.seniorenlauncher.data.model.AlarmEntry
import com.seniorenlauncher.data.model.Medication
import com.seniorenlauncher.data.model.QuickContact
import com.seniorenlauncher.service.SeniorInCallService
import com.seniorenlauncher.util.AlarmScheduler
import com.seniorenlauncher.util.MedicationAlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_DELIVER_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        val fullBody = messages.joinToString("") { it.displayMessageBody ?: "" }
        val address = messages.firstOrNull()?.displayOriginatingAddress ?: "Onbekend"
        val timestamp = messages.firstOrNull()?.timestampMillis ?: System.currentTimeMillis()

        val cleanBody = fullBody.trim()
        val upperBody = cleanBody.uppercase()

        if (upperBody.startsWith("LAUN_") || cleanBody.startsWith("#")) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    handleRemoteCommand(context.applicationContext, cleanBody, address)
                } finally {
                    pendingResult.finish()
                }
            }
        } else {
            showNotification(context.applicationContext, address, fullBody)
        }

        saveMessageToInbox(context.applicationContext, address, fullBody, timestamp)
    }

    private suspend fun handleRemoteCommand(context: Context, body: String, sender: String) {
        val dao = LauncherApp.instance.database.contactDao()
        val sosContacts = dao.getSosContactsSync()
        
        if (!isAuthorized(sender, sosContacts)) {
            Log.w("SmsReceiver", "Onbevoegde toegang van: $sender")
            return
        }

        val upperBody = body.uppercase()
        val results = mutableListOf<String>()

        when {
            upperBody.contains("LAUN_GELUID") || upperBody.contains("LAUN_ZOEK") -> {
                results.add(processSystemCommand(context, upperBody))
            }
            upperBody.startsWith("#WAAR") -> {
                results.add(processLocation(context))
            }
            upperBody.startsWith("#SPEAKER") -> {
                SeniorInCallService.setForceSpeaker(true)
                results.add("✅ Luidspreker aan voor volgend gesprek")
            }
            upperBody.startsWith("#LAMP") -> {
                results.add(processFlashlight(context, upperBody))
            }
            else -> {
                val commands = body.split(Regex("(?=#)")).filter { it.isNotBlank() && it.contains("#") }
                for (cmd in commands) {
                    val cleanCmd = cmd.trim()
                    val cmdUpper = cleanCmd.uppercase()
                    when {
                        cmdUpper.startsWith("#OPEN") -> results.add(processOpenApp(context, cleanCmd))
                        cmdUpper.startsWith("#WEKKER") -> results.add(processAlarm(context, cleanCmd))
                        cmdUpper.startsWith("#MEDICIJN") -> results.add(processMedication(context, cleanCmd))
                        cmdUpper.startsWith("#VOORRAAD") -> results.add(processStock(cleanCmd))
                        cmdUpper.startsWith("#AGENDA") -> results.add(processCalendar(context, cleanCmd))
                        cmdUpper.startsWith("#STATUS") -> results.add(processStatus(context))
                        cmdUpper.startsWith("#BERICHT") -> results.add(processPopupMessage(context, cleanCmd))
                        cmdUpper.startsWith("#PING") -> results.add(processPing(context))
                        cmdUpper.startsWith("#CONTACT") -> results.add(processAddContact(context, cleanCmd))
                        cmdUpper.startsWith("#HELDER") -> results.add(processBrightness(context, cleanCmd))
                        cmdUpper.startsWith("#VOLUME") -> results.add(processVolume(context, cleanCmd))
                        cmdUpper.startsWith("#HULP") || cmdUpper.startsWith("#HELP") -> {
                            sendHelpMessages(context, sender)
                            results.add("✅ Help-info verstuurd")
                        }
                    }
                }
            }
        }

        if (results.isNotEmpty()) {
            val reply = "Sionro Update:\n" + results.joinToString("\n")
            sendReply(context, sender, reply)
            
            sosContacts.filter { !PhoneNumberUtils.compare(it.phoneNumber, sender) }.forEach { 
                sendReply(context, it.phoneNumber, "Sionro: Beheer actie door ${sender.takeLast(4)}.\nStatus: ${results.first()}")
            }
        }
    }

    private fun isAuthorized(sender: String, sosContacts: List<QuickContact>): Boolean {
        val cleanSender = sender.replace(Regex("[^0-9]"), "")
        return sosContacts.any { contact ->
            val cleanContact = contact.phoneNumber.replace(Regex("[^0-9]"), "")
            PhoneNumberUtils.compare(sender, contact.phoneNumber) || 
            (cleanSender.length >= 8 && cleanContact.length >= 8 && cleanSender.takeLast(8) == cleanContact.takeLast(8))
        }
    }

    private fun processSystemCommand(context: Context, command: String): String {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (nm.isNotificationPolicyAccessGranted) nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
        val streams = listOf(AudioManager.STREAM_RING, AudioManager.STREAM_NOTIFICATION, AudioManager.STREAM_SYSTEM, AudioManager.STREAM_MUSIC, AudioManager.STREAM_ALARM)
        for (s in streams) {
            try { audioManager.setStreamVolume(s, audioManager.getStreamMaxVolume(s), 0) } catch (e: Exception) {}
        }

        if (command.contains("LAUN_ZOEK")) {
            context.startActivity(Intent(context, MainActivity::class.java).apply { 
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("NAVIGATE_TO", "alarm_trigger")
                putExtra("ALARM_LABEL", "TELEFOON ZOEKEN") 
            })
            return "✅ Zoeksignaal gestart"
        }
        return "✅ Volume op MAX"
    }

    private fun processFlashlight(context: Context, command: String): String {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList.firstOrNull() ?: return "❌ Geen zaklamp gevonden"
            val turnOn = !command.contains("UIT")
            cameraManager.setTorchMode(cameraId, turnOn)
            if (turnOn) "✅ Zaklamp AAN" else "✅ Zaklamp UIT"
        } catch (e: Exception) {
            "❌ Fout bij zaklamp: ${e.message}"
        }
    }

    private fun processOpenApp(context: Context, body: String): String {
        val appName = body.substringAfter("#OPEN").trim()
        if (appName.isEmpty()) return "❌ Gebruik: #OPEN [Naam]"
        
        val pm = context.packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        
        val targetApp = packages.find { pkg ->
            pm.getApplicationLabel(pkg).toString().contains(appName, ignoreCase = true)
        }
        
        return if (targetApp != null) {
            val launchIntent = pm.getLaunchIntentForPackage(targetApp.packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                "✅ App '${pm.getApplicationLabel(targetApp)}' geopend"
            } else {
                "❌ Kon app niet starten"
            }
        } else {
            "❌ App '$appName' niet gevonden"
        }
    }

    private suspend fun processLocation(context: Context): String {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return "❌ Geen locatie permissie"
        }
        return withContext(Dispatchers.IO) {
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                val location = Tasks.await(fusedLocationClient.lastLocation, 10, TimeUnit.SECONDS)
                if (location != null) "📍 Locatie: https://www.google.com/maps/search/?api=1&query=${location.latitude},${location.longitude}"
                else "❌ Locatie onbekend"
            } catch (e: Exception) { "❌ Locatie fout" }
        }
    }

    private fun processStatus(context: Context): String {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val pct = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return "Status:\n🔋 Batterij: $pct%\n🔊 Volume: ${am.getStreamVolume(AudioManager.STREAM_RING)}/${am.getStreamMaxVolume(AudioManager.STREAM_RING)}\n🔕 Stil: ${if(am.ringerMode != AudioManager.RINGER_MODE_NORMAL) "JA" else "NEE"}"
    }

    private fun processBrightness(context: Context, body: String): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(context)) {
            return "❌ Fout: Geen permissie voor systeemaanpassing op de telefoon."
        }
        try {
            val parts = body.split(" ")
            val input = if (parts.size > 1) parts[1].toInt().coerceIn(1, 10) else 10
            val level = (input * 25.5).toInt().coerceIn(0, 255)
            Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, level)
            return "✅ Helderheid op niveau $input (1-10)"
        } catch (e: Exception) { return "❌ Helderheid fout" }
    }

    private fun processVolume(context: Context, body: String): String {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        try {
            val parts = body.split(" ")
            val max = am.getStreamMaxVolume(AudioManager.STREAM_RING)
            val input = if (parts.size > 1) parts[1].toInt().coerceIn(0, 10) else 10
            val level = (input * max / 10).coerceIn(0, max)
            am.setStreamVolume(AudioManager.STREAM_RING, level, 0)
            return "✅ Volume op niveau $input (0-10)"
        } catch (e: Exception) { return "❌ Volume fout" }
    }

    private fun processPopupMessage(context: Context, body: String): String {
        val msg = body.substringAfter(" ", "").trim()
        if (msg.isEmpty()) return "❌ Gebruik: #BERICHT [Tekst]"
        context.startActivity(Intent(context, MainActivity::class.java).apply { 
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("NAVIGATE_TO", "alarm_trigger")
            putExtra("ALARM_LABEL", msg)
            putExtra("isAgendaEvent", true) 
            putExtra("agendaTitle", "BERICHT VAN BEHEERDER")
        })
        return "✅ Bericht gestuurd"
    }

    private fun processPing(context: Context): String {
        context.startActivity(Intent(context, MainActivity::class.java).apply { 
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("NAVIGATE_TO", "alarm_trigger")
            putExtra("ALARM_LABEL", "ALLES GOED? KLIK OP BEGREPEN.")
            putExtra("isAgendaEvent", true) 
            putExtra("agendaTitle", "WELZIJNS-CHECK")
        })
        return "✅ Welzijns-check gestart"
    }

    private suspend fun processAddContact(context: Context, body: String): String {
        val parts = body.split(" ").filter { it.isNotBlank() }
        if (parts.size < 3) return "❌ Gebruik: #CONTACT Naam 0612345678"
        val dao = LauncherApp.instance.database.contactDao()
        dao.insert(QuickContact(name = parts[1], phoneNumber = parts[2], isSosContact = false))
        return "✅ Contact '${parts[1]}' toegevoegd"
    }

    private suspend fun processStock(body: String): String {
        val parts = body.split(" ").filter { it.isNotBlank() }
        if (parts.size < 3) return "❌ Gebruik: #VOORRAAD [Naam] [Aantal]"
        val name = parts[1]
        val count = parts[2].toIntOrNull() ?: return "❌ Aantal ongeldig"
        
        val dao = LauncherApp.instance.database.medicationDao()
        val allMeds = dao.getAllSync()
        val med = allMeds.find { it.name.contains(name, ignoreCase = true) }
        
        return if (med != null) {
            dao.update(med.copy(stockCount = count))
            "✅ Voorraad '${med.name}' bijgewerkt naar $count"
        } else {
            "❌ Medicijn '$name' niet gevonden"
        }
    }

    private suspend fun processAlarm(context: Context, body: String): String {
        val regex = Regex("#WEKKER\\s+(\\d{1,2})[:.,\\s]+(\\d{1,2})(?:\\s+(.*))?", RegexOption.IGNORE_CASE)
        val m = regex.find(body) ?: return "❌ #WEKKER 08:30"
        val h = m.groupValues[1].toInt(); val min = m.groupValues[2].toInt(); val l = m.groupValues[3].ifBlank { "Wekker" }
        if (h in 0..23 && min in 0..59) {
            val alarm = AlarmEntry(hour = h, minute = min, label = l, daysOfWeek = "1,2,3,4,5,6,7")
            val id = LauncherApp.instance.database.alarmDao().insert(alarm)
            AlarmScheduler.scheduleAlarm(context, alarm.copy(id = id))
            return "✅ Wekker om $h:${String.format("%02d", min)}"
        }
        return "❌ Tijd fout"
    }

    private suspend fun processMedication(context: Context, body: String): String {
        val regex = Regex("#MEDICIJN\\s+(\\d{1,2})[:.,\\s]+(\\d{1,2})\\s+([^\\s]+)(?:\\s+(.*))?", RegexOption.IGNORE_CASE)
        val m = regex.find(body) ?: return "❌ #MEDICIJN 12:00 Naam"
        val h = m.groupValues[1].toInt(); val min = m.groupValues[2].toInt(); val n = m.groupValues[3]; val d = m.groupValues[4].ifBlank { "1 pil" }
        if (h in 0..23 && min in 0..59) {
            val med = Medication(name = n, dose = d, times = String.format("%02d:%02d", h, min), stockCount = 30)
            val id = LauncherApp.instance.database.medicationDao().insert(med)
            MedicationAlarmScheduler.scheduleAlarms(context, med.copy(id = id))
            return "✅ Medicijn '$n' toegevoegd"
        }
        return "❌ Tijd fout"
    }

    private suspend fun processCalendar(context: Context, body: String): String {
        val regex = Regex("#AGENDA\\s+(\\d{1,2})[-/.,\\s]+(\\d{1,2})\\s+(\\d{1,2})[:.,\\s]+(\\d{1,2})\\s+(.*)", RegexOption.IGNORE_CASE)
        val m = regex.find(body) ?: return "❌ #AGENDA 24-12 14:00 Titel"
        val d = m.groupValues[1].toInt(); val mo = m.groupValues[2].toInt() - 1; val h = m.groupValues[3].toInt(); val mi = m.groupValues[4].toInt(); val t = m.groupValues[5]
        return try {
            val cal = Calendar.getInstance().apply { set(Calendar.MONTH, mo); set(Calendar.DAY_OF_MONTH, d); set(Calendar.HOUR_OF_DAY, h); set(Calendar.MINUTE, mi); set(Calendar.SECOND, 0) }
            if (cal.timeInMillis < System.currentTimeMillis()) cal.add(Calendar.YEAR, 1)
            val v = ContentValues().apply { put(CalendarContract.Events.DTSTART, cal.timeInMillis); put(CalendarContract.Events.DTEND, cal.timeInMillis + 3600000); put(CalendarContract.Events.TITLE, t); put(CalendarContract.Events.CALENDAR_ID, getPrimaryCalendarId(context)); put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id) }
            context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, v)
            "✅ Afspraak '$t' ingepland"
        } catch (e: Exception) { "❌ Agenda fout" }
    }

    private fun getPrimaryCalendarId(context: Context): Long {
        var firstId = 1L
        try {
            val projection = arrayOf(CalendarContract.Calendars._ID, CalendarContract.Calendars.IS_PRIMARY)
            context.contentResolver.query(CalendarContract.Calendars.CONTENT_URI, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idIdx = cursor.getColumnIndex(CalendarContract.Calendars._ID)
                    val priIdx = cursor.getColumnIndex(CalendarContract.Calendars.IS_PRIMARY)
                    firstId = cursor.getLong(idIdx)
                    do {
                        val id = cursor.getLong(idIdx)
                        val isPrimary = if (priIdx != -1) cursor.getInt(priIdx) == 1 else false
                        if (isPrimary) return id
                    } while (cursor.moveToNext())
                }
            }
        } catch (e: Exception) { }
        return firstId
    }

    private fun sendHelpMessages(context: Context, address: String) {
        val msg1 = "Sionro Remote Codes:\n#WAAR (Locatie)\n#STATUS (Status)\n#PING (Welzijns-check)\n#BERICHT [tekst]\n#OPEN [Naam]\n#LAMP [AAN/UIT]"
        val msg2 = "Andere:\n#VOORRAAD [Naam] [Aantal]\n#CONTACT Naam Nr\n#VOLUME [0-10]\n#HELDER [1-10]\n#WEKKER 08:30 Label\n#MEDICIJN 12:00 Naam"
        sendReply(context, address, msg1); sendReply(context, address, msg2)
    }

    private fun sendReply(context: Context, address: String, message: String) {
        try {
            val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) context.getSystemService(SmsManager::class.java) else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }
            val parts = smsManager.divideMessage(message)
            if (parts.size > 1) smsManager.sendMultipartTextMessage(address, null, parts, null, null)
            else smsManager.sendTextMessage(address, null, message, null, null)
        } catch (e: Exception) { Log.e("SmsReceiver", "Reply failed") }
    }

    private fun saveMessageToInbox(context: Context, address: String, body: String, date: Long) {
        val v = ContentValues().apply { put(Telephony.Sms.ADDRESS, address); put(Telephony.Sms.BODY, body); put(Telephony.Sms.DATE, date); put(Telephony.Sms.READ, 0); put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_INBOX) }
        try { context.contentResolver.insert(Telephony.Sms.Inbox.CONTENT_URI, v) } catch (e: Exception) {}
    }

    private fun showNotification(context: Context, address: String, body: String) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(context, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP; putExtra("NAVIGATE_TO", "sms"); putExtra("SMS_ADDRESS", address) }
        val pi = PendingIntent.getActivity(context, address.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val builder = NotificationCompat.Builder(context, LauncherApp.CH_SMS).setSmallIcon(android.R.drawable.stat_notify_chat).setContentTitle(address).setContentText(body).setPriority(NotificationCompat.PRIORITY_MAX).setAutoCancel(true).setContentIntent(pi)
        nm.notify(address.hashCode(), builder.build())
    }
}
