package com.seniorenlauncher.ui.screens

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun SystemPermissionsSetupScreen(onNext: () -> Unit, isSenior: Boolean = false) {
    val context = LocalContext.current
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    var canDrawOverlays by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    var canUseFullScreenIntent by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                notificationManager.canUseFullScreenIntent()
            } else true
        )
    }
    var canScheduleExactAlarms by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.canScheduleExactAlarms()
            } else true
        )
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                canDrawOverlays = Settings.canDrawOverlays(context)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    canUseFullScreenIntent = notificationManager.canUseFullScreenIntent()
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    canScheduleExactAlarms = alarmManager.canScheduleExactAlarms()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val allGranted = canDrawOverlays && canUseFullScreenIntent && canScheduleExactAlarms

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isSenior) "De telefoon slim maken" else "Wekker & SOS Rechten",
            fontSize = if (isSenior) 36.sp else 32.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            lineHeight = if (isSenior) 42.sp else 38.sp
        )
        
        Spacer(Modifier.height(16.dp))
        
        Text(
            text = if (isSenior) 
                "We moeten 3 knoppen aanzetten zodat uw wekker en SOS altijd goed werken. Tik op de rode vakken." 
                else "Deze instellingen zijn cruciaal voor een betrouwbare werking op Samsung en Android 14/15/16.",
            fontSize = if (isSenior) 22.sp else 18.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = if (isSenior) 30.sp else 24.sp
        )
        
        Spacer(Modifier.height(32.dp))

        PermissionRow(
            title = if (isSenior) "1. Scherm aanzetten" else "Verschijnen bovenop",
            description = if (isSenior) "Zodat u de wekker direct ziet als hij afgaat." else "Nodig om de wekker en SOS direct op het scherm te tonen.",
            isGranted = canDrawOverlays,
            icon = Icons.Default.Layers,
            isSenior = isSenior,
            onClick = {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                )
                context.startActivity(intent)
            }
        )

        Spacer(Modifier.height(16.dp))

        PermissionRow(
            title = if (isSenior) "2. Belangrijke meldingen" else "Volledig scherm meldingen",
            description = if (isSenior) "Zodat de telefoon ook in nood geluid maakt." else "Nodig om het scherm te wekken bij een alarm of SOS.",
            isGranted = canUseFullScreenIntent,
            icon = Icons.Default.Fullscreen,
            isSenior = isSenior,
            onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(intent)
                }
            }
        )

        Spacer(Modifier.height(16.dp))

        PermissionRow(
            title = if (isSenior) "3. De tijd bewaken" else "Exacte wekkers",
            description = if (isSenior) "Zodat uw medicijnen exact op tijd gemeld worden." else "Nodig om medicijn-herinneringen exact op tijd te laten afgaan.",
            isGranted = canScheduleExactAlarms,
            icon = Icons.Default.Alarm,
            isSenior = isSenior,
            onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(intent)
                }
            }
        )

        Spacer(modifier = Modifier.weight(1f))
        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isSenior) 100.dp else 80.dp),
            shape = RoundedCornerShape(24.dp),
            enabled = allGranted,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (allGranted) Color(0xFF10B981) else Color.Gray
            )
        ) {
            Text(
                text = if (allGranted) (if (isSenior) "HET IS GELUKT, GA VERDER" else "VOLGENDE") 
                       else (if (isSenior) "ZET DE 3 KNOPPEN AAN" else "STEL ALLES IN OM VERDER TE GAAN"),
                fontSize = if (isSenior) 24.sp else 20.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun PermissionRow(
    title: String,
    description: String,
    isGranted: Boolean,
    icon: ImageVector,
    isSenior: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isGranted) { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted) Color(0xFFE8F5E9) else Color(0xFFFEF2F2)
        ),
        border = androidx.compose.foundation.BorderStroke(
            if (isSenior) 4.dp else 2.dp, 
            if (isGranted) Color(0xFF10B981) else Color(0xFFEF4444)
        )
    ) {
        Row(
            modifier = Modifier.padding(if (isSenior) 24.dp else 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(if (isSenior) 64.dp else 56.dp)
                    .background(
                        if (isGranted) Color(0xFF10B981).copy(alpha = 0.1f) 
                        else Color(0xFFEF4444).copy(alpha = 0.1f), 
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon, 
                    null, 
                    modifier = Modifier.size(if (isSenior) 40.dp else 32.dp), 
                    tint = if (isGranted) Color(0xFF10B981) else Color(0xFFEF4444)
                )
            }
            
            Spacer(Modifier.width(if (isSenior) 24.dp else 20.dp))
            
            Column(Modifier.weight(1f)) {
                Text(
                    title, 
                    fontSize = if (isSenior) 22.sp else 18.sp, 
                    fontWeight = FontWeight.Bold,
                    color = if (isGranted) Color(0xFF1B5E20) else Color(0xFF991B1B)
                )
                Text(
                    description, 
                    fontSize = if (isSenior) 18.sp else 14.sp, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = if (isSenior) 24.sp else 18.sp
                )
            }
            
            Spacer(Modifier.width(12.dp))
            
            if (isGranted) {
                Text("✅", fontSize = if (isSenior) 32.sp else 24.sp)
            } else {
                Text("❌", fontSize = if (isSenior) 32.sp else 24.sp)
            }
        }
    }
}
