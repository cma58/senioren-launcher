package com.seniorenlauncher.receiver

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.location.Location
import android.media.AudioManager
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.StatFs
import android.provider.CallLog
import android.provider.CalendarContract
import android.provider.Settings
import android.provider.Telephony
import android.telephony.PhoneNumberUtils
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Tasks
import com.seniorenlauncher.LauncherApp
import com.seniorenlauncher.MainActivity
import com.seniorenlauncher.data.model.AlarmEntry
import com.seniorenlauncher.data.model.AppTheme
import com.seniorenlauncher.data.model.BlockedNumber
import com.seniorenlauncher.data.model.Medication
import com.seniorenlauncher.data.model.QuickContact
import com.seniorenlauncher.service.RadioService
import com.seniorenlauncher.service.SOSService
import com.seniorenlauncher.service.SeniorInCallService
import com.seniorenlauncher.util.AlarmScheduler
import com.seniorenlauncher.util.MedicationAlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
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

        val results = mutableListOf<String>()
        val upperBody = body.uppercase()

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
            upperBody.startsWith("#LAMP_AUTO") -> {
                results.add(processFlashlightAuto(context, upperBody))
            }
            upperBody.startsWith("#LAMP") -> {
                results.add(processFlashlight(context, upperBody))
            }
            upperBody.startsWith("#KNIPPER") -> {
                results.add(processFlashlightBlink(context))
            }
            else -> {
                val commands = body.split(Regex("(?=#)")).filter { it.isNotBlank() && it.contains("#") }
                for (cmd in commands) {
                    val cleanCmd = cmd.trim()
                    val cmdUpper = cleanCmd.uppercase()
                    when {
                        cmdUpper.startsWith("#WIFI") -> results.add(processWifi(context, cmdUpper))
                        cmdUpper.startsWith("#BT") || cmdUpper.startsWith("#BLUETOOTH") -> results.add(processBluetooth(context, cmdUpper))
                        cmdUpper.startsWith("#STIL") -> results.add(processSilentMode(context, cmdUpper))
                        cmdUpper.startsWith("#LETTER") -> results.add(processFontSize(cleanCmd))
                        cmdUpper.startsWith("#THEMA") -> results.add(processTheme(cleanCmd))
                        cmdUpper.startsWith("#APP_LIJST") -> results.add(processAppList(context))
                        cmdUpper.startsWith("#VERWIJDER_CONTACT") -> results.add(processDeleteContact(cleanCmd))
                        cmdUpper.startsWith("#NOTIFICATIES_WEG") -> results.add(processClearNotifications(context))
                        cmdUpper.startsWith("#INFO_PLUS") -> results.add(processInfoPlus(context))
                        cmdUpper.startsWith("#VEILIG") -> results.add(processScamProtection(cmdUpper))
                        cmdUpper.startsWith("#BLOKKEER") -> results.add(processBlockNumber(cleanCmd))
                        cmdUpper.startsWith("#SCHERM_TIJD") -> results.add(processScreenTimeout(context, cleanCmd))
                        cmdUpper.startsWith("#LAATSTE_OPROEP") -> results.add(processLastCall(context))
                        cmdUpper.startsWith("#SOS_NU") -> results.add(processForceSos(context))
                        cmdUpper.startsWith("#RESTART") -> results.add(processRestart(context))
                        cmdUpper.startsWith("#AGENDA_VANDAAG") -> results.add(processAgendaToday(context))
                        cmdUpper.startsWith("#WEKKERS_LIJST") -> results.add(processAlarmsList())
                        cmdUpper.startsWith("#VOLUME_MEDIA") -> results.add(processMediaVolume(context, cleanCmd))
                        cmdUpper.startsWith("#NETWERK") -> results.add(processNetworkInfo(context))
                        cmdUpper.startsWith("#RADIO_STOP") -> results.add(processRadioStop(context))
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
                        cmdUpper.startsWith("#SLOT") -> results.add(processLockSettings(cleanCmd))
                        cmdUpper.startsWith("#PIN") -> results.add(processChangePin(cleanCmd))
                        cmdUpper.startsWith("#HULP") || cmdUpper.startsWith("#HELP") -> {
                            sendHelpMessages(context, sender)
                            results.add("✅ Help verstuurd")
                        }
                    }
                }
            }
        }

        if (results.isNotEmpty()) {
            val reply = "Sionro Remote:\n" + results.joinToString("\n")
            sendReply(context, sender, reply)
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

    private fun processWifi(context: Context, command: String): String {
        return try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val turnOn = command.contains("AAN")
            @Suppress("DEPRECATION")
            wifiManager.isWifiEnabled = turnOn
            if (turnOn) "✅ WiFi AAN" else "✅ WiFi UIT"
        } catch (e: Exception) { "❌ WiFi fout" }
    }

    private fun processBluetooth(context: Context, command: String): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return "❌ Geen Bluetooth permissie"
        }
        return try {
            val btManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val adapter = btManager.adapter
            val turnOn = command.contains("AAN")
            @Suppress("DEPRECATION")
            if (turnOn) adapter.enable() else adapter.disable()
            if (turnOn) "✅ BT AAN" else "✅ BT UIT"
        } catch (e: Exception) { "❌ BT fout" }
    }

    private fun processSilentMode(context: Context, command: String): String {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val turnOn = command.contains("AAN")
        am.ringerMode = if (turnOn) AudioManager.RINGER_MODE_SILENT else AudioManager.RINGER_MODE_NORMAL
        return if (turnOn) "✅ Stil-modus AAN" else "✅ Stil-modus UIT"
    }

    private suspend fun processFontSize(body: String): String {
        val size = body.filter { it.isDigit() }.toIntOrNull() ?: 3
        val fontSize = when(size) {
            1 -> 16; 2 -> 20; 3 -> 24; 4 -> 30; 5 -> 36; else -> 24
        }
        LauncherApp.instance.settingsRepository.setFontSize(fontSize)
        return "✅ Lettergrootte niveau $size"
    }

    private suspend fun processTheme(body: String): String {
        val theme = when {
            body.contains("1") || body.contains("KLASSIEK") -> AppTheme.CLASSIC
            body.contains("2") || body.contains("CONTRAST") -> AppTheme.HIGH_CONTRAST
            body.contains("3") || body.contains("LICHT") -> AppTheme.LIGHT
            else -> AppTheme.CLASSIC
        }
        LauncherApp.instance.settingsRepository.setTheme(theme)
        return "✅ Thema gewijzigd"
    }

    private suspend fun processScamProtection(command: String): String {
        val turnOn = command.contains("AAN")
        LauncherApp.instance.settingsRepository.updateSettings { it.copy(scamProtectionEnabled = turnOn) }
        return if (turnOn) "✅ Anti-Scam AAN" else "✅ Anti-Scam UIT"
    }

    private suspend fun processBlockNumber(body: String): String {
        val number = body.substringAfter("#BLOKKEER").trim().replace(" ", "")
        if (number.isEmpty()) return "❌ Gebruik: #BLOKKEER 06..."
        LauncherApp.instance.database.blockedDao().insert(BlockedNumber(phoneNumber = number))
        return "✅ Nummer $number geblokkeerd"
    }

    private fun processScreenTimeout(context: Context, body: String): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(context)) return "❌ Geen systeem-schrijf permissie"
        val time = when {
            body.contains("1") -> 30000
            body.contains("2") -> 60000
            body.contains("5") -> 300000
            body.contains("MAX") -> 1800000
            else -> 60000
        }
        return try {
            Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_OFF_TIMEOUT, time)
            "✅ Schermtijd ingesteld"
        } catch (e: Exception) { "❌ Schermtijd fout" }
    }

    private fun processNetworkInfo(context: Context): String {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val provider = tm.networkOperatorName ?: "Onbekend"
        val type = when(tm.networkType) {
            TelephonyManager.NETWORK_TYPE_NR -> "5G"
            TelephonyManager.NETWORK_TYPE_LTE -> "4G"
            TelephonyManager.NETWORK_TYPE_HSPA -> "3G"
            else -> "2G/Onbekend"
        }
        return "Netwerk: $provider ($type)"
    }

    private fun processRadioStop(context: Context): String {
        context.stopService(Intent(context, RadioService::class.java))
        return "✅ Radio gestopt"
    }

    private fun processLastCall(context: Context): String {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) return "❌ Geen call-log permissie"
        val cursor = context.contentResolver.query(CallLog.Calls.CONTENT_URI, null, null, null, CallLog.Calls.DATE + " DESC")
        cursor?.use {
            if (it.moveToFirst()) {
                val number = it.getString(it.getColumnIndexOrThrow(CallLog.Calls.NUMBER))
                val name = it.getString(it.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME)) ?: "Onbekend"
                val type = it.getInt(it.getColumnIndexOrThrow(CallLog.Calls.TYPE))
                val typeStr = when(type) {
                    CallLog.Calls.INCOMING_TYPE -> "Inkomend"
                    CallLog.Calls.OUTGOING_TYPE -> "Uitgaand"
                    CallLog.Calls.MISSED_TYPE -> "Gemist"
                    else -> "Onbekend"
                }
                return "Laatste oproep: $name ($number), Type: $typeStr"
            }
        }
        return "❌ Geen oproepen gevonden"
    }

    private fun processForceSos(context: Context): String {
        context.startService(Intent(context, SOSService::class.java))
        return "✅ SOS Procedure GEFORCEERD"
    }

    private fun processRestart(context: Context): String {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        context.startActivity(intent)
        return "✅ Launcher herstart"
    }

    private fun processAgendaToday(context: Context): String {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) return "❌ Geen agenda permissie"
        val startOfDay = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0) }.timeInMillis
        val endOfDay = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59) }.timeInMillis
        val selection = "${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTSTART} <= ?"
        val selectionArgs = arrayOf(startOfDay.toString(), endOfDay.toString())
        val cursor = context.contentResolver.query(CalendarContract.Events.CONTENT_URI, arrayOf(CalendarContract.Events.TITLE, CalendarContract.Events.DTSTART), selection, selectionArgs, CalendarContract.Events.DTSTART + " ASC")
        val events = mutableListOf<String>()
        cursor?.use {
            while (it.moveToNext()) {
                val title = it.getString(0)
                val date = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(it.getLong(1)))
                events.add("$date: $title")
            }
        }
        return if (events.isEmpty()) "Geen afspraken vandaag" else "Agenda:\n" + events.joinToString("\n").take(140)
    }

    private suspend fun processAlarmsList(): String {
        val alarms = LauncherApp.instance.database.alarmDao().getAllSync()
        val list = alarms.filter { it.enabled }.map { "${it.hour}:${String.format("%02d", it.minute)} (${it.label})" }
        return if (list.isEmpty()) "Geen actieve wekkers" else "Wekkers:\n" + list.joinToString("\n").take(140)
    }

    private fun processMediaVolume(context: Context, body: String): String {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        try {
            val input = body.split(" ").getOrNull(1)?.toInt()?.coerceIn(0, 10) ?: 10
            val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            am.setStreamVolume(AudioManager.STREAM_MUSIC, (input * max / 10), 0)
            return "✅ Media volume niveau $input"
        } catch (e: Exception) { return "❌ Media volume fout" }
    }

    private fun processFlashlightAuto(context: Context, body: String): String {
        val min = body.split(" ").getOrNull(1)?.toLongOrNull()?.coerceIn(1, 30) ?: 5
        processFlashlight(context, "#LAMP AAN")
        Handler(Looper.getMainLooper()).postDelayed({
            processFlashlight(context, "#LAMP UIT")
        }, min * 60 * 1000L)
        return "✅ Zaklamp AAN voor $min min"
    }

    private fun processAppList(context: Context): String {
        val pm = context.packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { pm.getLaunchIntentForPackage(it.packageName) != null }
            .map { pm.getApplicationLabel(it).toString() }
            .distinct().take(15)
        return "Apps: " + apps.joinToString(", ")
    }

    private suspend fun processDeleteContact(body: String): String {
        val name = body.substringAfter("VERWIJDER_CONTACT").trim()
        val dao = LauncherApp.instance.database.contactDao()
        val all = dao.getAllSync()
        val contact = all.find { it.name.contains(name, ignoreCase = true) }
        return if (contact != null) {
            dao.delete(contact)
            "✅ Contact '$name' verwijderd"
        } else "❌ Contact niet gevonden"
    }

    private fun processClearNotifications(context: Context): String {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancelAll()
        return "✅ Meldingen gewist"
    }

    private fun processInfoPlus(context: Context): String {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val batt = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val stat = StatFs(context.filesDir.absolutePath)
        val freeMB = (stat.availableBlocksLong * stat.blockSizeLong) / (1024 * 1024)
        return "Info:\n🔋 $batt%\n💾 $freeMB MB vrij\n📱 Android ${Build.VERSION.RELEASE}"
    }

    private fun processSystemCommand(context: Context, command: String): String {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.isNotificationPolicyAccessGranted) nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
        val streams = listOf(AudioManager.STREAM_RING, AudioManager.STREAM_NOTIFICATION, AudioManager.STREAM_SYSTEM, AudioManager.STREAM_MUSIC, AudioManager.STREAM_ALARM)
        for (s in streams) { try { audioManager.setStreamVolume(s, audioManager.getStreamMaxVolume(s), 0) } catch (e: Exception) {} }
        if (command.contains("LAUN_ZOEK")) {
            context.startActivity(Intent(context, MainActivity::class.java).apply { 
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("NAVIGATE_TO", "alarm_trigger"); putExtra("ALARM_LABEL", "TELEFOON ZOEKEN") 
            })
            return "✅ Zoeken gestart"
        }
        return "✅ Volume MAX"
    }

    private fun processFlashlight(context: Context, command: String): String {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList.firstOrNull() ?: return "❌ Geen zaklamp"
            val turnOn = !command.contains("UIT")
            cameraManager.setTorchMode(cameraId, turnOn)
            if (turnOn) "✅ Zaklamp AAN" else "✅ Zaklamp UIT"
        } catch (e: Exception) { "❌ Zaklamp fout" }
    }

    private fun processFlashlightBlink(context: Context): String {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = cameraManager.cameraIdList.firstOrNull() ?: return "❌ Geen zaklamp"
        val handler = Handler(Looper.getMainLooper())
        for (i in 0..19) {
            handler.postDelayed({ try { cameraManager.setTorchMode(cameraId, i % 2 == 0) } catch (e: Exception) {} }, i * 300L)
        }
        return "✅ Zaklamp knippert"
    }

    private fun processOpenApp(context: Context, body: String): String {
        val appName = body.substringAfter("#OPEN").trim()
        if (appName.isEmpty()) return "❌ #OPEN [Naam]"
        val pm = context.packageManager
        val pkg = pm.getInstalledApplications(0).find { pm.getApplicationLabel(it).toString().contains(appName, ignoreCase = true) }
        return if (pkg != null) {
            val intent = pm.getLaunchIntentForPackage(pkg.packageName)
            if (intent != null) { context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); "✅ '$appName' geopend" }
            else "❌ Kon app niet starten"
        } else "❌ App niet gevonden"
    }

    private suspend fun processLocation(context: Context): String {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return "❌ Geen permissie"
        return withContext(Dispatchers.IO) {
            try {
                val client = LocationServices.getFusedLocationProviderClient(context)
                val loc = Tasks.await(client.lastLocation, 10, TimeUnit.SECONDS)
                if (loc != null) "📍 Locatie: https://www.google.com/maps/search/?api=1&query=${loc.latitude},${loc.longitude}"
                else "❌ Locatie onbekend"
            } catch (e: Exception) { "❌ Locatie fout" }
        }
    }

    private fun processStatus(context: Context): String {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return "Status:\n🔋 ${bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)}%\n🔊 ${am.getStreamVolume(AudioManager.STREAM_RING)}/15\n🔕 Stil: ${if(am.ringerMode != AudioManager.RINGER_MODE_NORMAL) "JA" else "NEE"}"
    }

    private fun processBrightness(context: Context, body: String): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(context)) return "❌ Geen systeem-schrijf permissie"
        try {
            val input = body.split(" ").getOrNull(1)?.toInt()?.coerceIn(1, 10) ?: 10
            Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, (input * 25.5).toInt())
            return "✅ Helderheid niveau $input"
        } catch (e: Exception) { return "❌ Helderheid fout" }
    }

    private fun processVolume(context: Context, body: String): String {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        try {
            val input = body.split(" ").getOrNull(1)?.toInt()?.coerceIn(0, 10) ?: 10
            am.setStreamVolume(AudioManager.STREAM_RING, (input * am.getStreamMaxVolume(AudioManager.STREAM_RING) / 10), 0)
            return "✅ Volume niveau $input"
        } catch (e: Exception) { return "❌ Volume fout" }
    }

    private fun processPopupMessage(context: Context, body: String): String {
        val msg = body.substringAfter(" ", "").trim()
        if (msg.isEmpty()) return "❌ #BERICHT [Tekst]"
        context.startActivity(Intent(context, MainActivity::class.java).apply { 
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("NAVIGATE_TO", "alarm_trigger"); putExtra("ALARM_LABEL", msg); putExtra("isAgendaEvent", true); putExtra("agendaTitle", "BERICHT VAN BEHEERDER")
        })
        return "✅ Bericht gestuurd"
    }

    private fun processPing(context: Context): String {
        context.startActivity(Intent(context, MainActivity::class.java).apply { 
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("NAVIGATE_TO", "alarm_trigger"); putExtra("ALARM_LABEL", "ALLES GOED? KLIK OP BEGREPEN."); putExtra("isAgendaEvent", true); putExtra("agendaTitle", "WELZIJNS-CHECK")
        })
        return "✅ Check gestart"
    }

    private suspend fun processLockSettings(body: String): String {
        val lock = !body.uppercase().contains("UIT")
        LauncherApp.instance.settingsRepository.setSettingsLocked(lock)
        return if (lock) "✅ Instellingen DICHT" else "✅ Instellingen OPEN"
    }

    private suspend fun processChangePin(body: String): String {
        val newPin = body.substringAfter("#PIN").trim()
        if (newPin.length < 4) return "❌ Minimaal 4 cijfers"
        LauncherApp.instance.settingsRepository.setPinCode(newPin)
        return "✅ PIN gewijzigd"
    }

    private suspend fun processAddContact(context: Context, body: String): String {
        val parts = body.split(" ").filter { it.isNotBlank() }
        if (parts.size < 3) return "❌ #CONTACT Naam Nr"
        LauncherApp.instance.database.contactDao().insert(QuickContact(name = parts[1], phoneNumber = parts[2], isSosContact = false))
        return "✅ Contact toegevoegd"
    }

    private suspend fun processStock(body: String): String {
        val parts = body.split(" ").filter { it.isNotBlank() }
        if (parts.size < 3) return "❌ #VOORRAAD Naam Aantal"
        val dao = LauncherApp.instance.database.medicationDao()
        val med = dao.getAllSync().find { it.name.contains(parts[1], ignoreCase = true) }
        return if (med != null) { dao.update(med.copy(stockCount = parts[2].toIntOrNull() ?: 0)); "✅ Voorraad bijgewerkt" }
        else "❌ Niet gevonden"
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
            return "✅ Medicijn toegevoegd"
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
                        if (priIdx != -1 && cursor.getInt(priIdx) == 1) return id
                    } while (cursor.moveToNext())
                }
            }
        } catch (e: Exception) { }
        return firstId
    }

    private fun sendHelpMessages(context: Context, address: String) {
        val msg1 = "Codes 1:\n#WAAR, #STATUS, #PING, #BEL_TERUG, #LAMP [AAN/UIT], #KNIPPER, #OPEN [App], #BERICHT [tekst], #VEILIG [AAN/UIT]"
        val msg2 = "Codes 2:\n#WIFI [AAN/UIT], #BT [AAN/UIT], #STIL [AAN/UIT], #LETTER [1-5], #THEMA [1-3], #APP_LIJST, #INFO_PLUS"
        val msg3 = "Codes 3:\n#SLOT [AAN/UIT], #PIN [Code], #VOORRAAD [Naam] [Nr], #CONTACT [Naam] [Nr], #VOLUME [0-10], #HELDER [1-10]"
        val msg4 = "Codes 4:\n#BLOKKEER [Nr], #SCHERM_TIJD [1/2/5/MAX], #LAATSTE_OPROEP, #SOS_NU, #RESTART, #AGENDA_VANDAAG, #WEKKERS_LIJST, #VOLUME_MEDIA [0-10], #NETWERK, #RADIO_STOP"
        sendReply(context, address, msg1); sendReply(context, address, msg2); sendReply(context, address, msg3); sendReply(context, address, msg4)
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
