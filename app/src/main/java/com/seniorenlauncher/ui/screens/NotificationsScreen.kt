package com.seniorenlauncher.ui.screens

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.service.notification.StatusBarNotification
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import com.seniorenlauncher.service.NotificationListener
import com.seniorenlauncher.ui.components.ScreenHeader

data class SingleNotification(
    val key: String,
    val title: String,
    val text: String,
    val intent: PendingIntent?,
    val timestamp: Long
)

data class GroupedNotification(
    val packageName: String,
    val appName: String,
    val items: List<SingleNotification>,
    val timestamp: Long
)

@Composable
fun NotificationsScreen(onBack: () -> Unit, onNavigate: (String) -> Unit) {
    val context = LocalContext.current
    val hasPermission = remember { isNotificationServiceEnabled(context) }
    val notifications by NotificationListener.activeNotificationsFlow.collectAsState()
    var showClearConfirm by remember { mutableStateOf(false) }
    
    val expandedStates = remember { mutableStateMapOf<String, Boolean>() }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        ScreenHeader(title = "Berichten & Herinneringen", onBack = onBack)

        if (!hasPermission) {
            PermissionRequiredScreen()
        } else {
            val grouped = remember(notifications) { groupAndFormatNotifications(context, notifications) }
            
            if (grouped.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Geen nieuwe berichten",
                        fontSize = 22.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    items(grouped) { group ->
                        val isExpanded = expandedStates[group.packageName] ?: false
                        NotificationGroupCard(
                            group = group,
                            isExpanded = isExpanded,
                            onToggleExpand = { expandedStates[group.packageName] = !isExpanded },
                            onNavigate = onNavigate
                        )
                    }

                    item {
                        Spacer(Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = { showClearConfirm = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(70.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Spacer(Modifier.width(12.dp))
                            Text("ALLES WISSEN", fontSize = 20.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Alle meldingen wissen?", fontSize = 22.sp, fontWeight = FontWeight.Bold) },
            text = { Text("Weet u zeker dat u alle meldingen wilt verwijderen? U kunt dit niet ongedaan maken.", fontSize = 18.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        NotificationListener.dismissAll()
                        showClearConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Ja, wis alles", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Nee, terug", fontSize = 18.sp)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun PermissionRequiredScreen() {
    val context = LocalContext.current
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.NotificationsOff,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(24.dp))
        Text(
            "Toegang nodig",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Om uw berichten van WhatsApp, SMS en andere apps hier te tonen, hebben we toestemming nodig.",
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            lineHeight = 28.sp
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = {
                context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            },
            modifier = Modifier.fillMaxWidth().height(70.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Instellingen openen", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun NotificationGroupCard(
    group: GroupedNotification,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (group.items.size == 1) {
                    handleNotificationClick(context, group.packageName, group.items.first(), onNavigate)
                } else {
                    onToggleExpand()
                }
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    group.appName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.weight(1f))
                
                // Clear entire group
                IconButton(onClick = {
                    group.items.forEach { NotificationListener.dismiss(it.key) }
                }) {
                    Icon(Icons.Default.Close, "Wis groep", tint = Color.Gray)
                }
            }
            
            Spacer(Modifier.height(4.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = if (group.items.size > 1) {
                            "${group.items.size} nieuwe berichten"
                        } else {
                            group.items.first().title
                        },
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 28.sp
                    )
                    
                    if (group.items.size == 1) {
                        Text(
                            group.items.first().text,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                if (group.items.size > 1) {
                    Icon(
                        if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded && group.items.size > 1) {
                Column(Modifier.padding(top = 16.dp)) {
                    group.items.forEach { item ->
                        HorizontalDivider(thickness = 0.5.dp, color = Color.Gray.copy(alpha = 0.3f))
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    handleNotificationClick(context, group.packageName, item, onNavigate)
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(item.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Text(item.text, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { NotificationListener.dismiss(item.key) }) {
                                Icon(Icons.Default.Close, contentDescription = "Wis", modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

fun groupAndFormatNotifications(context: Context, notifications: List<StatusBarNotification>): List<GroupedNotification> {
    val pm = context.packageManager
    return notifications
        .groupBy { it.packageName }
        .map { (pkg, sbns) ->
            val appName = try {
                pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
            } catch (e: Exception) {
                pkg.split(".").last().replaceFirstChar { it.uppercase() }
            }
            
            val items = sbns.map { sbn ->
                SingleNotification(
                    key = sbn.key,
                    title = sbn.notification.extras.getString(Notification.EXTRA_TITLE) ?: "",
                    text = sbn.notification.extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: "",
                    intent = sbn.notification.contentIntent,
                    timestamp = sbn.postTime
                )
            }.sortedByDescending { it.timestamp }

            GroupedNotification(
                packageName = pkg,
                appName = appName,
                items = items,
                timestamp = items.first().timestamp
            )
        }
        .sortedByDescending { it.timestamp }
}

fun isNotificationServiceEnabled(context: Context): Boolean {
    val packageNames = NotificationManagerCompat.getEnabledListenerPackages(context)
    return packageNames.contains(context.packageName)
}

fun handleNotificationClick(
    context: Context,
    packageName: String,
    item: SingleNotification,
    onNavigate: (String) -> Unit
) {
    // Lijsten met bekende standaard-apps van Android/Samsung/Google
    val smsPackages = listOf("com.google.android.apps.messaging", "com.android.mms", "com.samsung.android.messaging")
    val phonePackages = listOf("com.android.server.telecom", "com.google.android.dialer", "com.android.dialer", "com.samsung.android.dialer")
    val calendarPackages = listOf("com.google.android.calendar", "com.samsung.android.calendar")

    when {
        smsPackages.contains(packageName) -> {
            NotificationListener.dismiss(item.key) 
            onNavigate("sms") // Gaat exact naar de eigen MessagesScreen
        }
        phonePackages.contains(packageName) -> {
            NotificationListener.dismiss(item.key)
            onNavigate("phone") // Gaat exact naar de eigen PhoneScreen
        }
        calendarPackages.contains(packageName) -> {
            NotificationListener.dismiss(item.key)
            onNavigate("calendar") // Gaat exact naar de eigen CalendarScreen
        }
        else -> {
            // Voor externe apps (zoals WhatsApp)
            try {
                if (item.intent != null) {
                    val options = android.app.ActivityOptions.makeBasic()
                    if (android.os.Build.VERSION.SDK_INT >= 34) {
                        options.setPendingIntentBackgroundActivityStartMode(android.app.ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED)
                    } else if (android.os.Build.VERSION.SDK_INT >= 33) {
                        @Suppress("DEPRECATION")
                        options.isPendingIntentBackgroundActivityLaunchAllowed = true
                    }
                    val fillInIntent = android.content.Intent().apply { addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK) }
                    item.intent.send(context, 0, fillInIntent, null, null, null, options.toBundle())
                } else {
                    throw Exception("Intent is null")
                }
            } catch (e: Exception) {
                try {
                    val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
                    if (launchIntent != null) {
                        launchIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(launchIntent)
                    }
                } catch (e2: Exception) {}
            }
        }
    }
}
