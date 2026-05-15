package com.seniorenlauncher.ui.screens.home

import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seniorenlauncher.ui.theme.SeniorenLauncherTheme
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeTopBar(notificationCount: Int, onNotificationsClick: () -> Unit) {
    val context = LocalContext.current
    var currentTime by remember { mutableStateOf(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())) }
    var currentDate by remember { mutableStateOf(SimpleDateFormat("EEEE d MMMM", Locale.getDefault()).format(Date())) }
    
    // Batterij status
    var batteryLevel by remember { mutableIntStateOf(100) }
    var isCharging by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Klok update
        while (true) {
            val now = Date()
            currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)
            currentDate = SimpleDateFormat("EEEE d MMMM", Locale.getDefault()).format(now)
            
            // Batterij info ophalen
            val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
                context.registerReceiver(null, filter)
            }
            batteryLevel = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

            kotlinx.coroutines.delay(30000)
        }
    }

    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        // Links: Batterij & Signaal
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isCharging) "⚡$batteryLevel%" else "$batteryLevel%",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (batteryLevel < 20 && !isCharging) Color.Red else MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = if (isCharging) Icons.Default.BatteryChargingFull else if (batteryLevel < 20) Icons.Default.BatteryAlert else Icons.Default.BatteryFull,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = if (batteryLevel < 20 && !isCharging) Color.Red else MaterialTheme.colorScheme.primary
                )
            }
            
            // Simpele signaal indicator
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.SignalCellularAlt,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Text(
                    "Bereik OK",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }

        // Midden: Klok
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.offset(y = 8.dp)
        ) {
            Text(
                currentTime, 
                fontSize = 54.sp, 
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                currentDate.replaceFirstChar { it.uppercase() }, 
                fontSize = 18.sp, 
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }

        // Rechts: Notificaties
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { onNotificationsClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Notifications, 
                contentDescription = "Meldingen",
                modifier = Modifier.size(40.dp),
                tint = if (notificationCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (notificationCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color.Red),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "$notificationCount",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun HomeSOSButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(80.dp)
            .fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
        shape = RoundedCornerShape(20.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = Color.White
            )
            Spacer(Modifier.width(16.dp))
            Text(
                "SOS HULP NODIG",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }
    }
}

@Composable
fun HomeStatusCard(
    pendingMedsCount: Int,
    onMedsClick: () -> Unit
) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f))
    ) {
        Row(
            Modifier.padding(24.dp).fillMaxWidth().clickable { onMedsClick() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("💊", fontSize = 40.sp)
            Spacer(Modifier.width(16.dp))
            Text(
                "Je hebt nog $pendingMedsCount medicijnen in te nemen", 
                fontSize = 20.sp, 
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

fun getHomeWeatherEmoji(iconUrl: String): String {
    return when {
        iconUrl.contains("01d") -> "☀️"
        iconUrl.contains("01n") -> "🌙"
        iconUrl.contains("02d") || iconUrl.contains("02n") -> "⛅"
        iconUrl.contains("03") || iconUrl.contains("04") -> "☁️"
        iconUrl.contains("09") || iconUrl.contains("10") -> "🌧️"
        iconUrl.contains("11") -> "⛈️"
        iconUrl.contains("13") -> "❄️"
        iconUrl.contains("50") -> "🌫️"
        else -> "☁️"
    }
}

// --- PREVIEWS ---

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun PreviewHomeTopBar() {
    SeniorenLauncherTheme {
        HomeTopBar(notificationCount = 3, onNotificationsClick = {})
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHomeSOSButton() {
    SeniorenLauncherTheme {
        Box(Modifier.padding(16.dp)) {
            HomeSOSButton(onClick = {})
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHomeStatusCard() {
    SeniorenLauncherTheme {
        HomeStatusCard(pendingMedsCount = 2, onMedsClick = {})
    }
}
