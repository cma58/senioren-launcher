package com.seniorenlauncher.ui.screens

import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AlarmTriggerScreen(label: String, soundName: String? = null, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val vibrator = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as Vibrator
    
    val ringtone: Ringtone? = remember {
        try {
            val uri = when (soundName) {
                "Klassiek" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                "Digitaal" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                else -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            } ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            
            RingtoneManager.getRingtone(context, uri)?.apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    isLooping = true
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    var currentTime by remember { mutableStateOf(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())) }

    LaunchedEffect(Unit) {
        try {
            ringtone?.play()
        } catch (e: Exception) {}
        
        val timings = longArrayOf(0, 1000, 500, 1000, 500)
        val amplitudes = intArrayOf(0, 255, 0, 255, 0)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(timings, 0)
            }
        } catch (e: Exception) {}

        while (true) {
            currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            kotlinx.coroutines.delay(1000)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                ringtone?.stop()
                vibrator.cancel()
            } catch (e: Exception) {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1729))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "⏰ WEKKER",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        Spacer(Modifier.height(40.dp))
        
        Text(
            text = currentTime,
            fontSize = 100.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
        
        Spacer(Modifier.height(20.dp))
        
        Text(
            text = label,
            fontSize = 28.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.8f)
        )
        
        Spacer(Modifier.weight(1f))
        
        Button(
            onClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text(
                "STOPPEN",
                fontSize = 36.sp,
                fontWeight = FontWeight.Black
            )
        }
        
        Spacer(Modifier.height(40.dp))
    }
}
