package com.seniorenlauncher.ui.screens.home

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.scale
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
            shape = RoundedCornerShape(40.dp),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(8.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)) // Slate 800
        ) {
            Column(
                Modifier
                    .padding(24.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("BEVEILIGDE INSTELLINGEN", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.White)
                
                Spacer(Modifier.height(24.dp))
                
                Surface(
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Black.copy(alpha = 0.3f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            input.replace(Regex("."), "●").ifEmpty { " " },
                            fontSize = 32.sp,
                            letterSpacing = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = if (error) Color(0xFFEF4444) else Color.White
                        )
                    }
                }
                
                Spacer(Modifier.height(24.dp))
                
                val rows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("C", "0", "OK")
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    rows.forEach { rowKeys ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rowKeys.forEach { key ->
                                val interactionSource = remember { MutableInteractionSource() }
                                val pressed by interactionSource.collectIsPressedAsState()
                                val scale by animateFloatAsState(
                                    targetValue = if (pressed) 0.92f else 1f,
                                    animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f),
                                    label = "scale"
                                )

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
                                    interactionSource = interactionSource,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(80.dp)
                                        .scale(scale),
                                    shape = RoundedCornerShape(24.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (key == "OK") Color(0xFF3B82F6) else Color.White.copy(alpha = 0.05f),
                                        contentColor = Color.White
                                    ),
                                    border = if (key != "OK") BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)) else null
                                ) {
                                    Text(key, fontSize = 24.sp, fontWeight = FontWeight.Black)
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
            shape = RoundedCornerShape(40.dp),
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f).padding(16.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
        ) {
            Column(Modifier.padding(24.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("KIES APPS", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White)
                    if (appId.startsWith("mapped_")) {
                        val interactionSource = remember { MutableInteractionSource() }
                        val pressed by interactionSource.collectIsPressedAsState()
                        val scale by animateFloatAsState(
                            targetValue = if (pressed) 0.94f else 1f,
                            animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f)
                        )
                        TextButton(
                            onClick = onRemove,
                            interactionSource = interactionSource,
                            modifier = Modifier.scale(scale)
                        ) {
                            Text("VERWIJDEREN", color = Color(0xFFEF4444), fontWeight = FontWeight.Black)
                        }
                    }
                }
                
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Zoek app...", fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8)) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Color(0xFF94A3B8)) },
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White.copy(alpha = 0.3f),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                        focusedContainerColor = Color.Black.copy(alpha = 0.2f),
                        unfocusedContainerColor = Color.Black.copy(alpha = 0.2f)
                    )
                )

                Spacer(Modifier.height(16.dp))
                
                LazyColumn(Modifier.weight(1f)) {
                    items(filteredApps) { app ->
                        val isSelected = selectedPackages.contains(app.packageName)
                        val interactionSource = remember { MutableInteractionSource() }
                        val pressed by interactionSource.collectIsPressedAsState()
                        val scale by animateFloatAsState(
                            targetValue = if (pressed) 0.98f else 1f,
                            animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f)
                        )

                        Row(
                            Modifier
                                .fillMaxWidth()
                                .scale(scale)
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) Color.White.copy(alpha = 0.05f) else Color.Transparent)
                                .clickable(interactionSource = interactionSource, indication = null) { 
                                    if (isSelected) selectedPackages.remove(app.packageName)
                                    else selectedPackages.add(app.packageName)
                                }
                                .padding(vertical = 12.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = null,
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF3B82F6))
                            )
                            Spacer(Modifier.width(12.dp))
                            if (app.icon != null) {
                                AsyncImage(
                                    model = app.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(56.dp).clip(RoundedCornerShape(16.dp))
                                )
                            }
                            Spacer(Modifier.width(16.dp))
                            Text(app.name, fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                    }
                }
                
                Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    val cancelInteractionSource = remember { MutableInteractionSource() }
                    val cancelPressed by cancelInteractionSource.collectIsPressedAsState()
                    val cancelScale by animateFloatAsState(
                        targetValue = if (cancelPressed) 0.94f else 1f,
                        animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f)
                    )

                    OutlinedButton(
                        onClick = onDismiss, 
                        interactionSource = cancelInteractionSource,
                        modifier = Modifier.weight(1f).height(80.dp).scale(cancelScale),
                        shape = RoundedCornerShape(40.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text("ANNULEREN", fontWeight = FontWeight.Black)
                    }

                    val addInteractionSource = remember { MutableInteractionSource() }
                    val addPressed by addInteractionSource.collectIsPressedAsState()
                    val addScale by animateFloatAsState(
                        targetValue = if (addPressed) 0.94f else 1f,
                        animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f)
                    )

                    Button(
                        onClick = { onAppsSelected(selectedPackages.toList()) }, 
                        interactionSource = addInteractionSource,
                        modifier = Modifier.weight(1f).height(80.dp).scale(addScale),
                        enabled = selectedPackages.isNotEmpty(),
                        shape = RoundedCornerShape(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                    ) {
                        Text("VOEG TOE", fontWeight = FontWeight.Black)
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
            shape = RoundedCornerShape(40.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
        ) {
            Column(
                Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("WIFI VERBINDING", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
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
                    modifier = Modifier.size(100.dp),
                    tint = if (isWifiEnabled) Color(0xFF3B82F6) else Color.Gray
                )
                
                Spacer(Modifier.height(24.dp))
                
                if (isWifiEnabled) {
                    Text(
                        "VERBONDEN MET:",
                        fontSize = 16.sp,
                        color = Color(0xFFCBD5E1),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        ssid,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        color = Color.White
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
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                } else {
                    Text(
                        "UW WIFI STAAT UIT",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        color = Color.White
                    )
                }
                
                Spacer(Modifier.height(32.dp))
                
                val settingsInteractionSource = remember { MutableInteractionSource() }
                val settingsPressed by settingsInteractionSource.collectIsPressedAsState()
                val settingsScale by animateFloatAsState(
                    targetValue = if (settingsPressed) 0.94f else 1f,
                    animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f)
                )

                Button(
                    onClick = {
                        AppLauncher.openWifiSettings(context)
                        onDismiss()
                    },
                    interactionSource = settingsInteractionSource,
                    modifier = Modifier.fillMaxWidth().height(80.dp).scale(settingsScale),
                    shape = RoundedCornerShape(40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                ) {
                    Text("WIFI INSTELLINGEN", fontSize = 20.sp, fontWeight = FontWeight.Black)
                }
                
                Spacer(Modifier.height(16.dp))
                
                val backInteractionSource = remember { MutableInteractionSource() }
                val backPressed by backInteractionSource.collectIsPressedAsState()
                val backScale by animateFloatAsState(
                    targetValue = if (backPressed) 0.94f else 1f,
                    animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f)
                )

                OutlinedButton(
                    onClick = onDismiss,
                    interactionSource = backInteractionSource,
                    modifier = Modifier.fillMaxWidth().height(80.dp).scale(backScale),
                    shape = RoundedCornerShape(40.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text("TERUG", fontSize = 18.sp, fontWeight = FontWeight.Black)
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
            shape = RoundedCornerShape(40.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
        ) {
            Column(
                Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Security,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = Color(0xFF3B82F6)
                )
                Spacer(Modifier.height(24.dp))
                Text(title.uppercase(), fontSize = 26.sp, fontWeight = FontWeight.Black, color = Color.White)
                Spacer(Modifier.height(16.dp))
                Text(
                    description,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Spacer(Modifier.height(32.dp))

                val grantInteractionSource = remember { MutableInteractionSource() }
                val grantPressed by grantInteractionSource.collectIsPressedAsState()
                val grantScale by animateFloatAsState(
                    targetValue = if (grantPressed) 0.94f else 1f,
                    animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f)
                )

                Button(
                    onClick = onGrant,
                    interactionSource = grantInteractionSource,
                    modifier = Modifier.fillMaxWidth().height(80.dp).scale(grantScale),
                    shape = RoundedCornerShape(40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                ) {
                    Text("TOESTAAN", fontSize = 20.sp, fontWeight = FontWeight.Black)
                }
                Spacer(Modifier.height(16.dp))

                TextButton(onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }) {
                    Text("HANDMATIG INSTELLEN", fontWeight = FontWeight.Black, color = Color(0xFF3B82F6))
                }
                TextButton(onClick = onDismiss) {
                    Text("ANNULEREN", color = Color(0xFFCBD5E1), fontWeight = FontWeight.Black)
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
            shape = RoundedCornerShape(40.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEF4444)),
            border = BorderStroke(2.dp, Color.White.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(120.dp),
                    tint = Color.White
                )
                
                Spacer(Modifier.height(24.dp))
                
                Text(
                    "SOS ALARM WORDT VERSTUURD OVER:",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                
                Spacer(Modifier.height(16.dp))
                
                Text(
                    "$secondsLeft",
                    fontSize = 140.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                
                Spacer(Modifier.height(32.dp))
                
                val stopInteractionSource = remember { MutableInteractionSource() }
                val stopPressed by stopInteractionSource.collectIsPressedAsState()
                val stopScale by animateFloatAsState(
                    targetValue = if (stopPressed) 0.94f else 1f,
                    animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f)
                )

                Button(
                    onClick = onDismiss,
                    interactionSource = stopInteractionSource,
                    modifier = Modifier.fillMaxWidth().height(90.dp).scale(stopScale),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(40.dp)
                ) {
                    Text("STOP / ANNULEER", fontSize = 26.sp, fontWeight = FontWeight.Black)
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
            shape = RoundedCornerShape(40.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
        ) {
            Column(
                Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("BLUETOOTH", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
                Spacer(Modifier.height(24.dp))
                
                Icon(
                    if (isBluetoothEnabled) Icons.Default.Bluetooth else Icons.Default.BluetoothDisabled,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp),
                    tint = if (isBluetoothEnabled) Color(0xFF6366F1) else Color(0xFF94A3B8)
                )
                
                Spacer(Modifier.height(24.dp))
                
                Text(
                    if (isBluetoothEnabled) "BLUETOOTH STAAT AAN" else "BLUETOOTH STAAT UIT",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    color = Color.White
                )
                
                Spacer(Modifier.height(32.dp))
                
                val btInteractionSource = remember { MutableInteractionSource() }
                val btPressed by btInteractionSource.collectIsPressedAsState()
                val btScale by animateFloatAsState(
                    targetValue = if (btPressed) 0.94f else 1f,
                    animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f)
                )

                Button(
                    onClick = {
                        AppLauncher.openBluetoothSettings(context)
                        onDismiss()
                    },
                    interactionSource = btInteractionSource,
                    modifier = Modifier.fillMaxWidth().height(80.dp).scale(btScale),
                    shape = RoundedCornerShape(40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                ) {
                    Text("INSTELLINGEN", fontSize = 20.sp, fontWeight = FontWeight.Black)
                }
                
                Spacer(Modifier.height(16.dp))
                
                val backInteractionSource = remember { MutableInteractionSource() }
                val backPressed by backInteractionSource.collectIsPressedAsState()
                val backScale by animateFloatAsState(
                    targetValue = if (backPressed) 0.94f else 1f,
                    animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f)
                )

                OutlinedButton(
                    onClick = onDismiss,
                    interactionSource = backInteractionSource,
                    modifier = Modifier.fillMaxWidth().height(80.dp).scale(backScale),
                    shape = RoundedCornerShape(40.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text("TERUG", fontSize = 18.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
