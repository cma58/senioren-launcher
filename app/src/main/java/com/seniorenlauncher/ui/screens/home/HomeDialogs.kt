package com.seniorenlauncher.ui.screens.home

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import android.net.wifi.WifiManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.text.style.TextAlign
import com.seniorenlauncher.util.PermissionUtils
import kotlinx.coroutines.delay
import android.media.ToneGenerator
import android.media.AudioManager
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.seniorenlauncher.util.AppLauncher
import com.seniorenlauncher.util.InstalledApp

import androidx.compose.ui.tooling.preview.Preview
import com.seniorenlauncher.ui.theme.SeniorenLauncherTheme

@Preview(showBackground = true)
@Composable
fun PreviewPinDialog() {
    SeniorenLauncherTheme {
        PinDialog(correctPin = "1234", onDismiss = {}, onSuccess = {})
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAppPickerDialog() {
    SeniorenLauncherTheme {
        AppPickerDialog(
            appId = "new",
            onDismiss = {},
            onAppsSelected = {},
            onRemove = {}
        )
    }
}

@Composable
fun PinDialog(
    correctPin: String,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var input by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(8.dp)
        ) {
            Column(
                Modifier
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Beveiligde Instellingen", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                
                Spacer(Modifier.height(8.dp))
                
                Text(
                    input.replace(Regex("."), "●").ifEmpty { " " },
                    fontSize = 24.sp,
                    letterSpacing = 8.sp,
                    color = if (error) Color.Red else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                
                Spacer(Modifier.height(8.dp))
                
                val rows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("C", "0", "OK")
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    rows.forEach { rowKeys ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rowKeys.forEach { key ->
                                Button(
                                    onClick = {
                                        when (key) {
                                            "C" -> if (input.isNotEmpty()) input = input.dropLast(1)
                                            "OK" -> {
                                                if (input == correctPin) onSuccess() else {
                                                    error = true
                                                    input = ""
                                                }
                                            }
                                            else -> {
                                                if (input.length < 8) {
                                                    error = false
                                                    input += key
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 50.dp, max = 65.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                ) {
                                    Text(key, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppPickerDialog(
    appId: String,
    onDismiss: () -> Unit,
    onAppsSelected: (List<String>) -> Unit,
    onRemove: () -> Unit
) {
    val context = LocalContext.current
    val installedApps by produceState<List<InstalledApp>>(initialValue = emptyList(), context) {
        value = AppLauncher.getInstalledApps(context, includeIcons = true)
    }
    var searchQuery by remember { mutableStateOf("") }
    val selectedPackages = remember { mutableStateListOf<String>() }

    val filteredApps = remember(searchQuery, installedApps) {
        if (searchQuery.isEmpty()) installedApps
        else installedApps.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f).padding(16.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Kies Apps", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    if (appId.startsWith("mapped_")) {
                        TextButton(onClick = onRemove) {
                            Text("Verwijderen", color = Color.Red)
                        }
                    }
                }
                
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Zoek app...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(Modifier.height(12.dp))
                
                LazyColumn(Modifier.weight(1f)) {
                    items(filteredApps) { app ->
                        val isSelected = selectedPackages.contains(app.packageName)
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    if (isSelected) selectedPackages.remove(app.packageName)
                                    else selectedPackages.add(app.packageName)
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = null
                            )
                            Spacer(Modifier.width(8.dp))
                            if (app.icon != null) {
                                AsyncImage(
                                    model = app.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp))
                                )
                            }
                            Spacer(Modifier.width(16.dp))
                            Text(app.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                }
                
                Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onDismiss, 
                        modifier = Modifier.weight(1f).heightIn(min = 60.dp).wrapContentHeight(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("ANNULEREN")
                    }
                    Button(
                        onClick = { onAppsSelected(selectedPackages.toList()) }, 
                        modifier = Modifier.weight(1f).heightIn(min = 60.dp).wrapContentHeight(),
                        enabled = selectedPackages.isNotEmpty(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("VOEG TOE (${selectedPackages.size})")
                    }
                }
            }
        }
    }
}

@Composable
fun WifiDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val wifiManager = remember { context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager }
    
    var hasPermission by remember { mutableStateOf(PermissionUtils.hasWifiPermissions(context)) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        hasPermission = results.values.all { it }
    }

    if (!hasPermission) {
        PermissionRequestDialog(
            title = "Wifi Toegang",
            description = "Om wifi informatie te kunnen tonen, hebben we toestemming nodig om nabijgelegen apparaten te scannen.",
            onDismiss = onDismiss,
            onGrant = {
                permissionLauncher.launch(PermissionUtils.getRequiredWifiPermissions().toTypedArray())
            }
        )
        return
    }

    val isWifiEnabled = remember { wifiManager.isWifiEnabled }
    
    val wifiInfo = if (isWifiEnabled) wifiManager.connectionInfo else null
    val signalLevel = if (wifiInfo != null) {
        WifiManager.calculateSignalLevel(wifiInfo.rssi, 5)
    } else 0
    
    val ssid = wifiInfo?.ssid?.removeSurrounding("\"") ?: "Niet verbonden"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Wifi Verbinding", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(24.dp))
                
                val wifiIcon = when {
                    !isWifiEnabled -> Icons.Default.WifiOff
                    signalLevel <= 1 -> Icons.Default.Wifi1Bar
                    signalLevel == 2 -> Icons.Default.Wifi2Bar
                    else -> Icons.Default.Wifi
                }

                Icon(
                    wifiIcon,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = if (isWifiEnabled) MaterialTheme.colorScheme.primary else Color.Gray
                )
                
                Spacer(Modifier.height(16.dp))
                
                if (isWifiEnabled) {
                    Text(
                        "Verbonden met:",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        ssid,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    
                    val strengthText = when(signalLevel) {
                        0 -> "Geen signaal"
                        1 -> "Zwak signaal"
                        2 -> "Matig signaal"
                        3 -> "Goed signaal"
                        else -> "Uitstekend signaal"
                    }
                    Text(
                        "Signaalsterkte: $strengthText",
                        fontSize = 16.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                } else {
                    Text(
                        "Uw Wifi staat UIT",
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )
                }
                
                Spacer(Modifier.height(32.dp))
                
                Button(
                    onClick = {
                        AppLauncher.openWifiSettings(context)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth().height(70.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Wifi Instellingen Openen", fontSize = 18.sp)
                }
                
                Spacer(Modifier.height(16.dp))
                
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("TERUG", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun PermissionRequestDialog(
    title: String,
    description: String,
    onDismiss: () -> Unit,
    onGrant: () -> Unit
) {
    val context = LocalContext.current
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Security,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(16.dp))
                Text(title, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Text(
                    description,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onGrant,
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("TOESTAAN", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(12.dp))
                TextButton(onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }) {
                    Text("Handmatig instellen")
                }
                TextButton(onClick = onDismiss) {
                    Text("ANNULEREN", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun SOSCountdownDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val context = LocalContext.current
    var secondsLeft by remember { mutableIntStateOf(5) }
    var hasPermission by remember { mutableStateOf(PermissionUtils.hasSosPermissions(context)) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        hasPermission = results.values.all { it }
    }

    if (!hasPermission) {
        PermissionRequestDialog(
            title = "SOS Toegang",
            description = "Om hulp te kunnen inschakelen hebben we toestemming nodig voor locatie, bellen en SMS.",
            onDismiss = onDismiss,
            onGrant = {
                permissionLauncher.launch(PermissionUtils.getRequiredSosPermissions().toTypedArray())
            }
        )
        return
    }
    
    LaunchedEffect(Unit) {
        val toneG = ToneGenerator(AudioManager.STREAM_ALARM, 100)
        while (secondsLeft > 0) {
            toneG.startTone(ToneGenerator.TONE_CDMA_PIP, 200)
            delay(1000)
            secondsLeft--
        }
        onConfirm()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Red),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp),
                    tint = Color.White
                )
                
                Spacer(Modifier.height(24.dp))
                
                Text(
                    "SOS ALARM WORDT VERSTUURD OVER:",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                
                Spacer(Modifier.height(16.dp))
                
                Text(
                    "$secondsLeft",
                    fontSize = 120.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                
                Spacer(Modifier.height(32.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Red),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("STOP / ANNULEER", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
fun BluetoothDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val bluetoothManager = remember { context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager }
    val isBluetoothEnabled = remember { bluetoothManager.adapter?.isEnabled == true }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Bluetooth", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(24.dp))
                
                Icon(
                    if (isBluetoothEnabled) Icons.Default.Bluetooth else Icons.Default.BluetoothDisabled,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = if (isBluetoothEnabled) Color(0xFF3F51B5) else Color.Gray
                )
                
                Spacer(Modifier.height(24.dp))
                
                Text(
                    if (isBluetoothEnabled) "Bluetooth staat AAN" else "Bluetooth staat UIT",
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )
                
                Spacer(Modifier.height(32.dp))
                
                Button(
                    onClick = {
                        AppLauncher.openBluetoothSettings(context)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth().height(70.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Bluetooth Instellingen", fontSize = 18.sp)
                }
                
                Spacer(Modifier.height(16.dp))
                
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("TERUG", fontSize = 16.sp)
                }
            }
        }
    }
}
