package com.seniorenlauncher.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.seniorenlauncher.LauncherApp
import com.seniorenlauncher.data.model.QuickContact
import com.seniorenlauncher.service.SOSService
import com.seniorenlauncher.ui.components.ScreenHeader
import kotlinx.coroutines.delay

@Composable
fun SOSScreen(onBack: () -> Unit) {
    // --- DEMO MODE TOGGLE ---
    val isDemoMode = false

    val context = LocalContext.current
    val dao = LauncherApp.instance.database.contactDao()
    val realSosContacts by dao.getSosContacts().collectAsState(initial = emptyList())
    
    // Samsung fix: Controleer of de app bovenop andere apps mag verschijnen
    var canDrawOverlays by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                canDrawOverlays = Settings.canDrawOverlays(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // --- DUMMY DATA ---
    val dummySosContacts = listOf(
        QuickContact(name = "Dochter Sofie", phoneNumber = "06 12345678", isSosContact = true),
        QuickContact(name = "Buurman Jan", phoneNumber = "06 87654321", isSosContact = true)
    )

    val sosContacts = if (isDemoMode) dummySosContacts else realSosContacts

    var isHolding by remember { mutableStateOf(false) }
    var holdProgress by remember { mutableStateOf(0f) }
    var sosTriggered by remember { mutableStateOf(false) }

    // Check permissions and GPS status
    var hasLocationPermission by remember { mutableStateOf(
        if (isDemoMode) true else ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    )}
    
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    var isGpsEnabled by remember { mutableStateOf(if (isDemoMode) true else locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) }

    // Launcher for GPS resolution
    val gpsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            isGpsEnabled = true
        }
    }

    // Re-check periodically
    LaunchedEffect(isDemoMode) {
        if (!isDemoMode) {
            while (true) {
                hasLocationPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                delay(2000)
            }
        }
    }

    LaunchedEffect(isHolding) {
        if (isHolding) {
            val startTime = System.currentTimeMillis()
            val duration = 3000f
            while (isHolding && holdProgress < 1f) {
                val elapsed = System.currentTimeMillis() - startTime
                holdProgress = (elapsed / duration).coerceAtMost(1f)
                delay(16)
            }
            if (holdProgress >= 1f) {
                sosTriggered = true
                if (!isDemoMode) triggerSOS(context)
                else Toast.makeText(context, "Demo Mode: SOS geactiveerd", Toast.LENGTH_SHORT).show()
            }
        } else {
            holdProgress = 0f
        }
    }

    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        ScreenHeader(title = "SOS Noodhulp", onBack = onBack)
        
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (!sosTriggered) {
                // --- SAMSUNG LOCKSCREEN FIX ---
                if (!canDrawOverlays) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .clickable {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                                )
                                context.startActivity(intent)
                            },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFED7D7)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, null, tint = Color(0xFFC53030), modifier = Modifier.size(32.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Samsung blokkeert onze hulp. Tik hier en zet 'Verschijnen bovenop' AAN.",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC53030)
                            )
                        }
                    }
                }

                if (!hasLocationPermission || !isGpsEnabled) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFEF4444))
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.GpsFixed, null, tint = Color(0xFFEF4444), modifier = Modifier.size(32.dp))
                            Spacer(Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    if (!hasLocationPermission) "Locatie-toegang nodig" else "GPS staat uit",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF991B1B)
                                )
                                Text(
                                    "Klik op de knop hiernaast om dit direct te herstellen.",
                                    fontSize = 14.sp,
                                    color = Color(0xFF991B1B)
                                )
                            }
                            Button(
                                onClick = {
                                    if (!hasLocationPermission) {
                                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                            data = android.net.Uri.fromParts("package", context.packageName, null)
                                        }
                                        context.startActivity(intent)
                                    } else {
                                        // Request to turn on GPS via Google Play Services dialog
                                        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build()
                                        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
                                        val client = LocationServices.getSettingsClient(context)
                                        val task = client.checkLocationSettings(builder.build())
                                        
                                        task.addOnFailureListener { exception ->
                                            if (exception is ResolvableApiException) {
                                                try {
                                                    val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution).build()
                                                    gpsLauncher.launch(intentSenderRequest)
                                                } catch (sendEx: IntentSender.SendIntentException) {
                                                    Log.e("SOS", "Error sending resolution", sendEx)
                                                }
                                            }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                            ) {
                                Text("NU AAN")
                            }
                        }
                    }
                }

                Text(
                    "HOU DE KNOP 3 SECONDEN IN\nOM HULP TE ROEPEN",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    lineHeight = 30.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Spacer(Modifier.height(40.dp))
                
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { holdProgress },
                        modifier = Modifier.size(280.dp),
                        color = Color(0xFFEF4444),
                        strokeWidth = 12.dp,
                        trackColor = Color.LightGray.copy(alpha = 0.3f),
                    )
                    
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    LaunchedEffect(isPressed) { isHolding = isPressed }
                    
                    val scale by animateFloatAsState(if (isPressed) 0.9f else 1f, label = "scale")

                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .scale(scale)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(Color(0xFFEF4444), Color(0xFFDC2626))))
                            .clickable(interactionSource = interactionSource, indication = null) {}
                            .border(8.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("SOS", fontSize = 60.sp, fontWeight = FontWeight.Black, color = Color.White)
                            Text("HULP", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
                
                Spacer(Modifier.height(40.dp))
                
                Text(
                    "Uw noodcontacten ontvangen direct\nuw locatie via SMS.",
                    textAlign = TextAlign.Center,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                SOSActiveUI(onBack)
            }
            
            Spacer(Modifier.height(24.dp))
            
            if (sosContacts.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Mensen die worden ingelicht:", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(Modifier.height(8.dp))
                        sosContacts.forEach { contact ->
                            Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("👤", fontSize = 20.sp)
                                Spacer(Modifier.width(8.dp))
                                Text(contact.name, fontSize = 18.sp)
                                Spacer(Modifier.weight(1f))
                                Text(contact.phoneNumber, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            } else {
                Text(
                    "Let op: Geen noodcontacten ingesteld!\nKlik op de knop in instellingen.",
                    color = Color(0xFFDC2626),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun SOSActiveUI(onBack: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "alpha"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Warning, null, modifier = Modifier.size(100.dp).scale(alpha), tint = Color(0xFFEF4444))
        Spacer(Modifier.height(24.dp))
        Text("NOODSIGNAAL VERSTUURD!", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color(0xFFDC2626), textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Text("Blijf rustig. Uw locatie is verzonden naar uw contactpersonen.", fontSize = 20.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 24.dp))
        Spacer(Modifier.height(48.dp))
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(0.8f).height(70.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("IK BEN VEILIG", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}

fun triggerSOS(context: Context) {
    val intent = Intent(context, SOSService::class.java)
    context.startForegroundService(intent)
}
