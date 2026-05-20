package com.seniorenlauncher.ui.screens.home

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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
    
    // Wifi status
    var isWifiConnected by remember { mutableStateOf(false) }

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

            // Wifi info ophalen
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            isWifiConnected = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true

            kotlinx.coroutines.delay(60000)
        }
    }

    Surface(
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Links: Batterij & Verbinding
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isCharging) Icons.Default.BatteryChargingFull else if (batteryLevel < 20) Icons.Default.BatteryAlert else Icons.Default.BatteryFull,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = if (batteryLevel < 20 && !isCharging) Color.Red else MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "$batteryLevel%",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = if (batteryLevel < 20 && !isCharging) Color.Red else MaterialTheme.colorScheme.onBackground
                    )
                }
                
                // Signaal & Wifi indicators
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isWifiConnected) Icons.Default.Wifi else Icons.Default.SignalCellularAlt,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (isWifiConnected) "WIFI" else "MOBIEL",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }

            // Midden: Klok
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    currentTime, 
                    fontSize = 64.sp, 
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = (-2).sp
                )
                Text(
                    currentDate.replaceFirstChar { it.uppercase() }, 
                    fontSize = 18.sp, 
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Rechts: Notificaties
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp))
                    .clickable { onNotificationsClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Notifications, 
                    contentDescription = "Meldingen",
                    modifier = Modifier.size(44.dp),
                    tint = if (notificationCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (notificationCount > 0) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(28.dp),
                        shape = CircleShape,
                        color = Color.Red,
                        tonalElevation = 4.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                "$notificationCount",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
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
            .height(90.dp)
            .fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFEF4444),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(28.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,
            pressedElevation = 2.dp
        ),
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color.White
            )
            Spacer(Modifier.width(20.dp))
            Text(
                "SOS HULP NODIG",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
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
