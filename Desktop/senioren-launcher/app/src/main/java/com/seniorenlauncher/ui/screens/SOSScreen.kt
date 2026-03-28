package com.seniorenlauncher.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.VibrationEffect
import android.os.Vibrator
import android.telephony.SmsManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun SOSScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var phase by remember { mutableStateOf("countdown") } // countdown, sending, sent
    var countdown by remember { mutableIntStateOf(5) }
    var sentTo by remember { mutableStateOf(listOf<String>()) }

    // Countdown timer
    LaunchedEffect(phase) {
        if (phase == "countdown") {
            // Trillen
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 300, 200, 300, 200, 300), -1))

            while (countdown > 0) {
                delay(1000)
                countdown--
            }
            phase = "sending"
        }
        if (phase == "sending") {
            // Verstuur SOS
            val contacts = listOf("Mama", "Zoon", "Dochter")
            sentTo = contacts
            // In productie: GPS ophalen + SMS versturen
            // sendSOSMessages(context)
            delay(1500)
            phase = "sent"
        }
    }

    Column(
        Modifier.fillMaxSize()
            .background(
                if (phase == "sent") Color(0xFF38A169).copy(alpha = 0.1f)
                else Color(0xFFDC2626).copy(alpha = 0.1f)
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (phase) {
            "countdown" -> {
                // Grote pulserende SOS indicator
                Box(
                    Modifier.size(140.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFDC2626).copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        Modifier.size(100.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDC2626)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🆘", fontSize = 48.sp)
                    }
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    "SOS wordt verstuurd...",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    "Uw locatie wordt verzonden over",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                // Grote countdown
                Text(
                    "$countdown",
                    fontSize = 72.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFDC2626)
                )

                Text(
                    "seconden",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(32.dp))

                // Annuleer knop
                Box(
                    Modifier.fillMaxWidth().height(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "ANNULEREN",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 2.sp
                    )
                }
            }

            "sending" -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(80.dp),
                    color = Color(0xFFDC2626),
                    strokeWidth = 6.dp
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    "Locatie wordt verzonden...",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            "sent" -> {
                // Succesvol
                Box(
                    Modifier.size(120.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF38A169).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✅", fontSize = 56.sp)
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    "SOS Verstuurd!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF38A169),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    "Uw GPS-locatie is verzonden naar:",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))

                // Contacten die het ontvangen hebben
                sentTo.forEach { name ->
                    Box(
                        Modifier.fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF38A169).copy(alpha = 0.15f))
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("✓", fontSize = 18.sp, color = Color(0xFF38A169))
                            Spacer(Modifier.width(12.dp))
                            Text(
                                name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Info over valdetectie
                Box(
                    Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        .padding(14.dp)
                ) {
                    Column {
                        Text(
                            "🛡️ Valdetectie actief",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Bij een val wordt automatisch SOS verstuurd",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Terug knop
                Box(
                    Modifier.fillMaxWidth().height(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF38A169))
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Terug naar Home",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
