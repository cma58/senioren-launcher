package com.seniorenlauncher.ui.screens

import android.app.Activity
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.telephony.SmsManager
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlarmOff
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.seniorenlauncher.LauncherApp
import com.seniorenlauncher.data.model.MedicationLog
import com.seniorenlauncher.receiver.AlarmReceiver
import com.seniorenlauncher.util.MedicationAlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AlarmTriggerScreen(
    label: String, 
    alarmId: Long = -1L, 
    soundName: String? = null, 
    isForcedRemote: Boolean = false,
    onDismiss: () -> Unit,
    onNavigateToWeather: () -> Unit = {}
) {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val activity = context as? Activity
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    val scope = rememberCoroutineScope()
    
    var canDrawOverlays by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                canDrawOverlays = Settings.canDrawOverlays(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val isMedication = label.startsWith("Medicijn:")
    val isFindPhone = label.equals("TELEFOON ZOEKEN", ignoreCase = true)
    val isRemoteMessage = isForcedRemote || activity?.intent?.getStringExtra("agendaTitle") == "BERICHT VAN BEHEERDER"
    val medPhotoUri = activity?.intent?.getStringExtra("MED_PHOTO")
    val isAgendaEvent = activity?.intent?.getBooleanExtra("isAgendaEvent", false) ?: false
    val agendaTitle = activity?.intent?.getStringExtra("agendaTitle") ?: ""

    var isNightLampMode by remember { mutableStateOf(false) }
    var currentTime by remember { mutableStateOf(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())) }
    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    var isTtsReady by remember { mutableStateOf(false) }

    DisposableEffect(activity) {
        activity?.let { act ->
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                    act.setShowWhenLocked(true)
                    act.setTurnScreenOn(true)
                }
                act.window.addFlags(
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                )
            } catch (e: Exception) { Log.e("AlarmTrigger", "Window flags error", e) }
        }
        onDispose {}
    }

    val wakeLock = remember {
        try {
            powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP, "SeniorenLauncher:AlarmWakeLock")
        } catch (e: Exception) { null }
    }

    val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ALARM)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()

    LaunchedEffect(Unit) {
        tts = TextToSpeech(appContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.forLanguageTag("nl-NL")
                tts?.setAudioAttributes(audioAttributes)
                isTtsReady = true
            }
        }
    }

    val ringtone: Ringtone? = remember {
        try {
            val uri = if (soundName != null) Uri.parse(soundName) else RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            RingtoneManager.getRingtone(appContext, uri ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))?.apply {
                this.audioAttributes = audioAttributes
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) isLooping = true
            }
        } catch (e: Exception) { null }
    }

    LaunchedEffect(isTtsReady) {
        if (isTtsReady) {
            if (isFindPhone) {
                while(true) {
                    tts?.speak("Ik ben hier! Uw telefoon ligt hier!", TextToSpeech.QUEUE_FLUSH, null, "ZOEK")
                    delay(4000)
                }
            } else {
                val msg = when {
                    isRemoteMessage -> "Bericht van familie: $label"
                    isMedication -> "Tijd voor uw medicijnen. $label"
                    isAgendaEvent -> "Herinnering. $label"
                    else -> "Goedemorgen. Het is tijd voor: $label"
                }
                if (isRemoteMessage) {
                    tts?.speak(msg, TextToSpeech.QUEUE_FLUSH, null, "Msg1")
                    delay(3000)
                    tts?.speak(msg, TextToSpeech.QUEUE_ADD, null, "Msg2")
                } else {
                    tts?.speak(msg, TextToSpeech.QUEUE_FLUSH, null, "AlarmTTS")
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        delay(5 * 60 * 1000L) 
        if (isMedication) handleMedicationMissed(appContext, alarmId)
        onDismiss()
    }

    LaunchedEffect(Unit) {
        try { wakeLock?.acquire(5 * 60 * 1000L); ringtone?.play() } catch (e: Exception) {}
        val timings = longArrayOf(0, 800, 400, 800, 400, 800, 1000)
        val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255, 0)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, 0))
            else vibrator.vibrate(timings, 0)
        } catch (e: Exception) {}
        while (true) { currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()); delay(1000) }
    }

    DisposableEffect(Unit) {
        onDispose {
            try { ringtone?.stop(); vibrator.cancel(); tts?.stop(); tts?.shutdown(); if (wakeLock?.isHeld == true) wakeLock.release() } catch (e: Exception) {}
        }
    }

    val backgroundColor = when {
        isMedication -> Color(0xFF7F1D1D)
        isRemoteMessage -> Color(0xFF1E3A8A)
        isAgendaEvent -> Color(0xFF065F46)
        isFindPhone -> Color(0xFF1E3A8A)
        isNightLampMode -> Color(0xFFD97706)
        else -> Color(0xFF0F1729)
    }

    Column(
        modifier = Modifier.fillMaxSize().background(backgroundColor).pointerInput(Unit) {
            detectTapGestures(onLongPress = { if(!isAgendaEvent && !isMedication && !isFindPhone) isNightLampMode = !isNightLampMode })
        }.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = when { 
                isRemoteMessage -> "📩 BERICHT"
                isMedication -> "💊 MEDICIJNEN"
                isAgendaEvent -> "📅 AFSPRAAK"
                isFindPhone -> "🔍 ZOEKEN"
                else -> "⏰ WEKKER" 
            }, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(top = 20.dp))
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 8.dp)) {
            if (isMedication && medPhotoUri != null) {
                AsyncImage(model = medPhotoUri, contentDescription = null, modifier = Modifier.size(240.dp).clip(RoundedCornerShape(32.dp)).background(Color.White.copy(alpha = 0.1f)), contentScale = ContentScale.Crop)
                Spacer(Modifier.height(24.dp))
                Text(text = label, fontSize = 40.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center)
            } else if (isRemoteMessage) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(100.dp))
                Spacer(Modifier.height(20.dp))
                Text(text = label, fontSize = 48.sp, fontWeight = FontWeight.Black, color = Color.White, textAlign = TextAlign.Center, lineHeight = 56.sp)
            } else {
                Text(text = currentTime, fontSize = 120.sp, fontWeight = FontWeight.Black, color = Color.White)
                Text(text = if (isAgendaEvent) agendaTitle else label, fontSize = 32.sp, fontWeight = FontWeight.Medium, color = Color.White.copy(alpha = 0.9f), textAlign = TextAlign.Center)
            }
        }

        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (!isAgendaEvent && !isFindPhone && !isRemoteMessage) {
                Button(onClick = { 
                    if (isMedication) MedicationAlarmScheduler.scheduleSnooze(appContext, alarmId, label)
                    else handleSnooze(context, label, alarmId, onDismiss)
                    onDismiss()
                }, modifier = Modifier.fillMaxWidth().height(90.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B6CB0).copy(alpha = 0.8f)), shape = RoundedCornerShape(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Timer, null, modifier = Modifier.size(32.dp)); Spacer(Modifier.width(12.dp)); Text("HERINNER OVER 15 MIN", fontSize = 22.sp, fontWeight = FontWeight.Black) }
                }
            }
            Button(onClick = { 
                if (isMedication) {
                    scope.launch(Dispatchers.IO) { handleMedicationTakenLogic(alarmId) }
                } else if (!isAgendaEvent && !isFindPhone && !isRemoteMessage) {
                    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                    if (currentHour in 5..10) onNavigateToWeather()
                }
                onDismiss() 
            }, modifier = Modifier.fillMaxWidth().height(if (isMedication || isRemoteMessage) 150.dp else 120.dp), colors = ButtonDefaults.buttonColors(containerColor = if (isMedication || isAgendaEvent || isFindPhone || isRemoteMessage) Color(0xFF10B981) else Color(0xFFEF4444)), shape = RoundedCornerShape(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) { Icon(if (isMedication || isAgendaEvent || isFindPhone || isRemoteMessage) Icons.Default.Check else Icons.Default.AlarmOff, null, modifier = Modifier.size(48.dp)); Spacer(Modifier.width(16.dp)); Text(text = when { isMedication -> "NU INGENOMEN"; isRemoteMessage || isAgendaEvent || isFindPhone -> "BEGREPEN"; else -> "UITZETTEN" }, fontSize = 32.sp, fontWeight = FontWeight.Black) }
            }
        }
        Spacer(Modifier.height(10.dp))
    }
}

private suspend fun handleMedicationTakenLogic(medId: Long) {
    val dao = LauncherApp.instance.database.medicationDao()
    val med = dao.getById(medId)
    if (med != null && System.currentTimeMillis() - med.lastActionTime > 5 * 60 * 1000) {
        dao.update(med.copy(isPending = false, lastTakenDate = System.currentTimeMillis(), lastActionTime = System.currentTimeMillis(), stockCount = (med.stockCount - 1).coerceAtLeast(0)))
        dao.insertLog(com.seniorenlauncher.data.model.MedicationLog(medicationId = med.id, date = System.currentTimeMillis(), time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()), status = "TAKEN"))
    }
}

private suspend fun handleMedicationMissed(appContext: Context, medId: Long) {
    withContext(Dispatchers.IO) {
        val dao = LauncherApp.instance.database.medicationDao()
        val med = dao.getById(medId)
        if (med != null && med.isPending) {
            dao.insertLog(com.seniorenlauncher.data.model.MedicationLog(medicationId = med.id, date = System.currentTimeMillis(), time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()), status = "MISSED"))
            dao.update(med.copy(isPending = false))
            val sosContacts = LauncherApp.instance.database.contactDao().getSosContactsSync()
            if (sosContacts.isNotEmpty()) {
                val message = "Sionro: Uw familielid heeft het medicijn '${med.name}' NIET ingenomen binnen 5 minuten."
                val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) appContext.getSystemService(SmsManager::class.java) else SmsManager.getDefault()
                sosContacts.forEach { contact -> try { smsManager?.sendTextMessage(contact.phoneNumber, null, message, null, null) } catch (e: Exception) { Log.e("AlarmTrigger", "SMS FOUT") } }
            }
        }
    }
}

private fun handleSnooze(context: Context, label: String, alarmId: Long, onDismiss: () -> Unit) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val snoozeTime = System.currentTimeMillis() + (15 * 60 * 1000L)
    val intent = Intent(context, AlarmReceiver::class.java).apply { putExtra("ALARM_ID", alarmId); putExtra("ALARM_LABEL", "$label (Sluimer)"); action = "com.seniorenlauncher.ALARM_SNOOZE_$alarmId" }
    val pi = PendingIntent.getBroadcast(context, (alarmId + 9999).toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    try { alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, snoozeTime, pi); Toast.makeText(context, "Tot over 15 minuten!", Toast.LENGTH_LONG).show() } catch (e: Exception) {}
    onDismiss()
}
