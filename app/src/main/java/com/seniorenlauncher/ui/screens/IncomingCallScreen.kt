package com.seniorenlauncher.ui.screens

import android.app.Activity
import android.content.Context
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.ContactsContract
import android.telecom.Call
import android.telecom.CallAudioState
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seniorenlauncher.service.SeniorInCallService

/**
 * Scherm voor inkomende en actieve gesprekken.
 * Geoptimaliseerd voor senioren en Android 16 compatibiliteit.
 * Vereist permissie: android.permission.WAKE_LOCK in Manifest voor proximity sensor.
 */
@Composable
fun IncomingCallScreen() {
    val currentCall by SeniorInCallService.currentCall.collectAsState()
    val callState by SeniorInCallService.callState.collectAsState()
    val audioState by SeniorInCallService.audioState.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity
    
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
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        onDispose {
            activity?.let { act ->
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                        act.setShowWhenLocked(false)
                        act.setTurnScreenOn(false)
                    }
                    act.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    val powerManager = remember { context.getSystemService(Context.POWER_SERVICE) as PowerManager }
    val proximityWakeLock = remember {
        try {
            if (powerManager.isWakeLockLevelSupported(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK)) {
                powerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, "SeniorenLauncher:ProximityLock")
            } else null
        } catch (e: Exception) {
            null
        }
    }

    DisposableEffect(callState) {
        // Activeer wang-preventie alleen tijdens een actief gesprek
        if (callState == Call.STATE_ACTIVE) {
            try {
                if (proximityWakeLock?.isHeld == false) {
                    proximityWakeLock.acquire(30 * 60 * 1000L /* 30 min timeout */)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            try {
                if (proximityWakeLock?.isHeld == true) {
                    proximityWakeLock.release()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        onDispose {
            try {
                if (proximityWakeLock?.isHeld == true) {
                    proximityWakeLock.release()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // --- Taak 3: Veilige Ringtone Cleanup ---
    DisposableEffect(callState) {
        var ringtone: Ringtone? = null
        if (callState == Call.STATE_RINGING) {
            try {
                val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                ringtone = RingtoneManager.getRingtone(context, notification)
                ringtone?.audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                ringtone?.play()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        onDispose {
            try {
                if (ringtone?.isPlaying == true) {
                    ringtone?.stop()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                ringtone = null
            }
        }
    }

    val rawNumber = currentCall?.details?.handle?.schemeSpecificPart ?: ""
    val contactName = remember(rawNumber) {
        if (rawNumber.isNotEmpty()) {
            getContactName(context, rawNumber)
        } else null
    }
    
    val displayName = contactName ?: rawNumber.ifEmpty { "Onbekend Nummer" }
    val isRinging = callState == Call.STATE_RINGING
    val isSpeakerOn = audioState?.route == CallAudioState.ROUTE_SPEAKER

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A202C))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isRinging) "INKOMENDE OPROEP" else "IN GESPREK",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = displayName,
                color = Color.White,
                fontSize = 45.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                lineHeight = 52.sp
            )
            if (contactName != null) {
                Text(
                    text = rawNumber,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        if (isRinging) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // WEIGEREN
                Button(
                    onClick = { SeniorInCallService.endCall() },
                    modifier = Modifier
                        .weight(1f)
                        .height(140.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53E3E)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CallEnd, null, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("WEIGEREN", fontSize = 20.sp, fontWeight = FontWeight.Black)
                    }
                }

                // OPNEMEN
                Button(
                    onClick = { SeniorInCallService.acceptCall() },
                    modifier = Modifier
                        .weight(1f)
                        .height(140.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38A169)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Call, null, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("OPNEMEN", fontSize = 20.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        } else {
            // Active call controls
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Speaker toggle
                Button(
                    onClick = { SeniorInCallService.toggleSpeaker() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSpeakerOn) Color(0xFF4A5568) else Color(0xFF2D3748)
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff, 
                            null, 
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(
                            if (isSpeakerOn) "LUIDSPREKER AAN" else "LUIDSPREKER UIT", 
                            fontSize = 24.sp, 
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // End call button
                Button(
                    onClick = { SeniorInCallService.endCall() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53E3E)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CallEnd, null, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.width(16.dp))
                        Text("OPHANGEN", fontSize = 28.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

private fun getContactName(context: android.content.Context, phoneNumber: String): String? {
    try {
        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber))
        val cursor = context.contentResolver.query(
            uri, 
            arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME), 
            null, null, null
        )
        cursor?.use {
            if (it.moveToFirst()) {
                val idx = it.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
                if (idx >= 0) return it.getString(idx)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}
